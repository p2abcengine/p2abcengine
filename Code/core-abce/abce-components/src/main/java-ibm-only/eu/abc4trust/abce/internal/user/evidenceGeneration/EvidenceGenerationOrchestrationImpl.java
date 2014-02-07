//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.user.evidenceGeneration;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.exceptions.TokenIssuanceException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;

public class EvidenceGenerationOrchestrationImpl implements EvidenceGenerationOrchestration {

    private final CryptoEngineUser cryptoEngine;

    @Inject
    public EvidenceGenerationOrchestrationImpl(CryptoEngineUser cryptoEngine) {
        this.cryptoEngine = cryptoEngine;
        System.out.println("Hello from EvidenceGenerationOrchestrationImpl()");
    }

    @Override
    public IssuanceMessage createIssuanceToken(IssuanceTokenDescription itd, List<URI> creduids,
            List<Attribute> atts, List<URI> pseudonyms, URI ctxt) throws TokenIssuanceException {

        IssuanceToken issuanceToken = this.cryptoEngine.createIssuanceToken(itd, creduids, atts,
                pseudonyms, ctxt);

        // We need to wrap the IssuanceToken in an issuance message  (§4.4.2 of protocol spec)
        IssuanceMessage issuanceMessage = new IssuanceMessage();
        issuanceMessage.setContext(ctxt);

        ObjectFactory of = new ObjectFactory();
        issuanceMessage.getAny().add(of.createIssuanceToken(issuanceToken));

        return issuanceMessage;
    }

    @Override
    public PresentationToken createPresentationToken(PresentationTokenDescription td,
 List<URI> creds,
            List<URI> pseudonyms) throws CryptoEngineException {
        return this.cryptoEngine.createPresentationToken(td, creds, pseudonyms);
    }

    @Override
    public PseudonymWithMetadata createPseudonym(URI pseudonymUri, String scope, boolean exclusive,
            URI secretReference) {
        // TODO(enr): Not all crypto engines will support the creation of pseudonyms
        return this.cryptoEngine.createPseudonym(pseudonymUri, scope, exclusive, secretReference);
    }

    @Override
    public Secret createSecret() {
        // TODO(enr): Not all crypto engines will support the creation of secrets...
        return this.cryptoEngine.createSecret();
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred, URI raparsuid,
        List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {
      return this.cryptoEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts);
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred, URI raparsuid,
        List<URI> revokedatts, URI revinfouid) throws CryptoEngineException, CredentialWasRevokedException {
      return this.cryptoEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts, revinfouid);
    }

}

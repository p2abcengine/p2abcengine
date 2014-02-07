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

package eu.abc4trust.abce.internal.revocation;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.security.auth.login.CredentialException;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.showproof.accumulator.ValueHasBeenRevokedException;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.util.XmlUtils;

public class UserRevocation {

    private final KeyManager keyManager;

    @Inject
    public UserRevocation(KeyManager keyManager) {
        super();
        this.keyManager = keyManager;
    }

    public void compareRevocationHandleToNonRevocationEvidence(
            BigInteger revocationAttrValue, NonRevocationEvidence nre)
                    throws CryptoEngineException {
        Parser xmlParser = Parser.getInstance();
        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness w1 = (AccumulatorWitness) xmlParser
                .parse(witnessElement);

        BigInteger witness = w1.getValue();
        if (revocationAttrValue.compareTo(witness) != 0) {
            throw new CryptoEngineException(
                    "Revocation handle does not correspond to witness in non-revocation evidence");
        }
    }

    private Credential updateNonRevocationEvidence(Credential cred,
            RevocationInformation revInfo) throws CredentialWasRevokedException {

        Object nreElement = cred.getCryptoParams().getAny().get(1);
        NonRevocationEvidence nre = (NonRevocationEvidence) XmlUtils.unwrap(nreElement, NonRevocationEvidence.class);

        RevocationUtility.updateNonRevocationEvidence(nre, revInfo);
        return cred;
    }

    private RevocationInformation getCurrentRevocationInformation(URI raparsuid)
            throws CryptoEngineException {
        try {
            return this.keyManager.getCurrentRevocationInformation(raparsuid);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
    }

    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {

        // TODO(jdn): This might only be working if the existing NRE is the
        // original NRE.
        RevocationInformation revInfo = null;
        revInfo = this.getCurrentRevocationInformation(raparsuid);

        return this.updateNonRevocationEvidence(cred, revInfo);
    }

    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException, CredentialWasRevokedException {
        RevocationInformation revInfo = null;
        try {
            revInfo = this.keyManager.getRevocationInformation(raparsuid,
                    revinfouid);
            return this.updateNonRevocationEvidence(cred, revInfo);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }

    }

    public boolean isRevoked(Credential cred) throws CryptoEngineException {
    	try {
			CredentialSpecification credSpec = keyManager.getCredentialSpecification(cred.getCredentialDescription().getCredentialSpecificationUID());
			if(!credSpec.isRevocable()) return false;
		} catch (KeyManagerException e) {
			throw new CryptoEngineException(e);
		}
    	
        Object nreElement = cred.getCryptoParams().getAny().get(1);
        NonRevocationEvidence nre = (NonRevocationEvidence) XmlUtils.unwrap(
                nreElement, NonRevocationEvidence.class);

        URI revParamsUid = nre.getRevocationAuthorityParametersUID();

        Parser xmlParser = Parser.getInstance();
        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness w1 = (AccumulatorWitness) xmlParser
                .parse(witnessElement);

        try {
            RevocationInformation revInfo = this.keyManager
                    .getLatestRevocationInformation(revParamsUid);
            RevocationUtility.updateWitness(w1, revInfo);
            return false;
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        } catch (ValueHasBeenRevokedException ex) {
            return true;
        }
    }


}

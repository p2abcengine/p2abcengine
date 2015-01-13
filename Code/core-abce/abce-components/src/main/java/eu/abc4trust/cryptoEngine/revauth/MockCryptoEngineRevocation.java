//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.cryptoEngine.revauth;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

public class MockCryptoEngineRevocation implements CryptoEngineRevocation {

    @Override
    public RevocationInformation revoke(URI revParUid, List<Attribute> attributes)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationInformation updateRevocationInformation(URI revParUid)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }


    @Override
    public NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationAuthorityParameters setupRevocationAuthorityParameters(int keyLength,
            URI cryptographicMechanism, URI uid, Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference, Reference nonRevocationUpdateReference)
                    throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(URI revAuthParamsUid,
            int epoch) throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationInformation getRevocationInformation(URI revParamsUid, URI revInfoUid)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

}

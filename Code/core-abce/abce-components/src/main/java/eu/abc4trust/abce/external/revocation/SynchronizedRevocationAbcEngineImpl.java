//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
 
package eu.abc4trust.abce.external.revocation;
 
import java.net.URI;
import java.util.List;
 
import com.google.inject.Inject;
 
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
 
public class SynchronizedRevocationAbcEngineImpl implements RevocationAbcEngine {
 
    private final RevocationAbcEngine engine;
 
    @Inject
    public SynchronizedRevocationAbcEngineImpl(RevocationAbcEngine engine) {
        this.engine = engine;
    }
 
    @Override
    public synchronized RevocationAuthorityParameters setupRevocationAuthorityParameters(
            int keyLength, URI cryptographicMechanism,
            URI uid, Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference)
                    throws CryptoEngineException {
        return this.engine.setupRevocationAuthorityParameters(keyLength,
                        cryptographicMechanism, uid, revocationInfoReference, 
                        nonRevocationEvidenceReference, nonRevocationUpdateReference);
    }
 
    @Override
    public synchronized RevocationInformation revoke(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        return this.engine.revoke(revParUid, attributes);
    }
 
    @Override
    public synchronized NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        return this.engine.generateNonRevocationEvidence(revParUid,
                attributes);
    }
 
    @Override
    public synchronized NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(
            URI revAuthParamsUid, int epoch) throws CryptoEngineException {
        return this.engine.generateNonRevocationEvidenceUpdate(
                revAuthParamsUid, epoch);
    }
 
    @Override
    public synchronized RevocationInformation updateRevocationInformation(URI revParUid)
            throws CryptoEngineException {
        return this.engine.updateRevocationInformation(revParUid);
    }
 
 
    @Override
    public synchronized RevocationInformation getRevocationInformation(URI revParamsUid,
            URI revInfoUid) throws CryptoEngineException {
        return this.engine.getRevocationInformation(revParamsUid, revInfoUid);
    }
 
}
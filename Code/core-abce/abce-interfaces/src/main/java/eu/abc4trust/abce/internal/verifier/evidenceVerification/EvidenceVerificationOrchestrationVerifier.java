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

package eu.abc4trust.abce.internal.verifier.evidenceVerification;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.VerifierParameters;

public interface EvidenceVerificationOrchestrationVerifier {

    /**
     * This method dissects the given presentation token t in subtokens,
     * separating those subtokens based on privacy-ABCs from those based on
     * other ABC technologies such as X.509. For each of these subtokens, it
     * invokes CryptoEngine.verifyToken(subtoken) to verify the cryptographic
     * evidence of the subtoken. If all subtokens are deemed valid, this method
     * returns true, otherwise it returns a list of error messages.
     * 
     * @param t
     * @return
     * @throws CryptoEngineException
     */
    boolean verifyToken(PresentationToken t, VerifierParameters vp) throws TokenVerificationException,
    CryptoEngineException;


}

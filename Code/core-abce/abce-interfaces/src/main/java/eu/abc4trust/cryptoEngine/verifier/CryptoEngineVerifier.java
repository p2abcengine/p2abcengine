//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.cryptoEngine.verifier;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public interface CryptoEngineVerifier {
    /**
     * This method verifies that the cryptographic evidence in the given
     * presentation token t supports the description of t. If the evidence is
     * deemed valid, this method returns true, and a list of error messages
     * otherwise. In order to verify the token, this method may call upon the
     * KeyManager to obtain Issuer parameters, Inspector public keys, Revocation
     * Authority parameters, and the current revocation information by invoking
     * the methods KeyManager.getIssuerParameters(issuid),
     * KeyManager.getInspectorPublicKey(ipkuid),
     * KeyManager.getRevocationAuthorityParameters(rapuid), and
     * KeyMangaer.getCurrentRevocationInformation(rapuid).
     * 
     * @param t
     * @return
     * @throws TokenVerificationException
     *             A list of verification errors.
     * @throws CryptoEngineException
     */
    public boolean verifyToken(PresentationToken t, VerifierParameters vp)
            throws TokenVerificationException, CryptoEngineException;
    
    /**
     * Generate verifier parameters. Those will have to be added to the presentation or issuance
     * policy.
     * Verifier parameters advertise the cryptographic building blocks supported by the verifier as
     * well as verifier-specific information about some of those building blocks (such as
     * the list of issuers parameters that contain a safe RSA modulus that
     * can be used when doing range proofs).
     * @throws CryptoEngineException 
     */
    public VerifierParameters createVerifierParameters(SystemParameters sp) throws CryptoEngineException;

    /**
     * Generate a fresh nonce that can be used by a presentation policy
     */
    public byte[] createNonce();
}

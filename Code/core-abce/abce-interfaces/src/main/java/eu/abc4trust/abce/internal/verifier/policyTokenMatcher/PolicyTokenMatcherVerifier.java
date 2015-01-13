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

package eu.abc4trust.abce.internal.verifier.policyTokenMatcher;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.VerifierParameters;

public interface PolicyTokenMatcherVerifier {

    /**
     * This method checks that the given token t satisfies one of the given policies contained in p
     * and that the cryptographic evidence in token t is valid. If the input parameter store is set to
     * true, the token is saved in permanent storage and assigned a unique token identifier by means
     * of which it can later be retrieved. If all checks succeed, the method returns the verified
     * presentation token description. This is essentially the token stripped from all cryptographic
     * evidence. If store is true, the returned token includes the unique token identifier generated
     * by the TokenManager. If any of the checks fail, this method returns a list of error messages
     * giving more detail about the reason for failure. To verify a token, this method takes the
     * following steps:
     * 
     * 1. It first separates the token description from the cryptographic evidence and checks that the
     * token description satisfies a presentation policy by calling
     * PolicyTokenMatcher.matchPresentationTokenDescriptionAgainstPolicy(p, tdesc)
     * 
     * 2. It then passes the presentation token to the
     * EvidenceVerificationOrchestration.verifyToken(t) method to dissect the token in subtokens and
     * verify the cryptographic evidence.
     * 
     * 3. If store is true, it saves the token in permanent storage by calling
     * TokenManager.storeToken(t). The storeToken method returns a unique identifier for the stored
     * token.
     * 
     * 4. It returns the description of presentation token t, possibly enhanced with the unique
     * identifier obtained in the previous step.
     * 
     * @param p
     * @param t
     * @param store
     * @return
     * @throws TokenVerificationException List of error messages.
     * @throws CryptoEngineException
     */
    public PresentationTokenDescription verifyTokenAgainstPolicy(PresentationPolicyAlternatives p,
            PresentationToken t, boolean store) throws TokenVerificationException, CryptoEngineException;

    /**
     * This method decides whether the given presentation token description td satisfies one of the
     * presentation policy contained in p. In order to do so, it may call out to
     * TokenManager.isEstablishedPseudonym(pseudonym) to check whether a pseudonym contained in td is
     * an established pseudonym.
     * 
     * @param p
     * @param td
     * @return
     */
    public boolean matchPresentationTokenDescriptionAgainstPolicy(PresentationPolicyAlternatives p,
            PresentationTokenDescription td) throws TokenVerificationException;

	public PresentationTokenDescription verifyToken(PresentationToken t, VerifierParameters vp,
			boolean store) throws TokenVerificationException, CryptoEngineException;

	public boolean verifyTokenDescriptionAgainstPolicyAlternatives(
			PresentationPolicyAlternatives p, PresentationTokenDescription t);


}

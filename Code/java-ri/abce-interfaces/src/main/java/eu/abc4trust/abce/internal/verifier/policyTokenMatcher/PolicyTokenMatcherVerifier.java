//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.verifier.policyTokenMatcher;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;

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


}

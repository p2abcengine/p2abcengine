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

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.verifier.evidenceVerification.EvidenceVerificationOrchestrationVerifier;
import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.util.MyPresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;

public class PolicyTokenMatcherVerifierImpl implements PolicyTokenMatcherVerifier {

    private final EvidenceVerificationOrchestrationVerifier evidenceOrch;
    private final TokenManagerVerifier tokenManager;
    private final KeyManager keyManager;

    @Inject
    public PolicyTokenMatcherVerifierImpl(EvidenceVerificationOrchestrationVerifier evidenceOrch,
            TokenManagerVerifier tokenManager, KeyManager keyManager) {
        this.evidenceOrch = evidenceOrch;
        this.tokenManager = tokenManager;
        this.keyManager = keyManager;
    }

    @Override
    public boolean matchPresentationTokenDescriptionAgainstPolicy(PresentationPolicyAlternatives p,
            PresentationTokenDescription td) throws TokenVerificationException {
        MyPresentationPolicyAlternatives myp = new MyPresentationPolicyAlternatives(p);
        URI policyUri = td.getPolicyUID();
        MyPresentationPolicy pp = myp.findOrThrow(policyUri);
        return pp.isSatisfiedBy(td, this.tokenManager, keyManager);
    }

    @Override
    public PresentationTokenDescription verifyTokenAgainstPolicy(PresentationPolicyAlternatives p,
 PresentationToken t, boolean store)
            throws TokenVerificationException, CryptoEngineException {

        PresentationTokenDescription tokenDesc = t.getPresentationTokenDescription();
        if (! this.matchPresentationTokenDescriptionAgainstPolicy(p, tokenDesc)) {
            String errorMessage = "The presented token does not satisfy the policy";
            TokenVerificationException ex = new TokenVerificationException();
            ex.errorMessages.add(errorMessage);
            throw ex;
        }

        if(! this.evidenceOrch.verifyToken(t)) {
            String errorMessage = "The crypto evidence in the presentation token is not valid";
            TokenVerificationException ex = new TokenVerificationException();
            ex.errorMessages.add(errorMessage);
            throw ex;
        }

        if (store) {
            URI uriOfStoredToken = this.tokenManager.storeToken(t);
            tokenDesc.setTokenUID(uriOfStoredToken);
        }

        return tokenDesc;
    }

}

//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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
import eu.abc4trust.xml.VerifierParameters;

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
    	if (!verifyTokenDescriptionAgainstPolicyAlternatives(p, tokenDesc)){
            String errorMessage = "The presented token does not satisfy the policy";
            TokenVerificationException ex = new TokenVerificationException();
            ex.errorMessages.add(errorMessage);
            throw ex;
    	}
    	return verifyToken(t, p.getVerifierParameters(), store);
    }
    
    @Override
    public PresentationTokenDescription verifyToken(PresentationToken t, VerifierParameters vp, boolean store)
            throws TokenVerificationException, CryptoEngineException {

        PresentationTokenDescription tokenDesc = t.getPresentationTokenDescription();
        if(! this.evidenceOrch.verifyToken(t, vp)) {
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
    
    @Override
    public boolean verifyTokenDescriptionAgainstPolicyAlternatives(PresentationPolicyAlternatives p,
    		PresentationTokenDescription ptd) {
        try {
			return this.matchPresentationTokenDescriptionAgainstPolicy(p, ptd);
		} catch (TokenVerificationException e) {
			return false;
		}   	
    }
}

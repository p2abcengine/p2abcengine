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

package eu.abc4trust.abce.external.verifier;

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.verifier.policyTokenMatcher.PolicyTokenMatcherVerifier;
import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationInformation;

public class VerifierAbcEngineImpl implements VerifierAbcEngine {


    private final PolicyTokenMatcherVerifier policyTokenMatcher;
    private final TokenManagerVerifier tokenManager;
    private final KeyManager keyManager;

    @Inject
    public VerifierAbcEngineImpl(PolicyTokenMatcherVerifier policyTokenMatcher,
            TokenManagerVerifier tokenManager, KeyManager keyManager) {
        this.policyTokenMatcher = policyTokenMatcher;
        this.tokenManager = tokenManager;
        this.keyManager = keyManager;
    }


    @Override
    public PresentationToken getToken(URI tokenUid) {
        return this.tokenManager.getToken(tokenUid);
    }

    @Override
    public PresentationTokenDescription verifyTokenAgainstPolicy(
            PresentationPolicyAlternatives p, PresentationToken t, boolean store)
            throws TokenVerificationException, CryptoEngineException {
        return this.policyTokenMatcher.verifyTokenAgainstPolicy(p, t, store);
    }


    @Override
    public boolean deleteToken(URI tokenuid) {
        return this.tokenManager.deleteToken(tokenuid);
    }


    @Override
    public RevocationInformation getLatestRevocationInformation(URI revParamsUid)
            throws CryptoEngineException {
        try {
            return this.keyManager.getCurrentRevocationInformation(revParamsUid);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
    }

}

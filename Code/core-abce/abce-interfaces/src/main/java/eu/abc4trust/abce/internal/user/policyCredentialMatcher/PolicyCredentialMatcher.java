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

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public interface PolicyCredentialMatcher {

    /**
     * This method performs steps 0-3 of PolicyCredentialMatcher.createToken(p) and returns true if
     * and only if the list of possible presentation tokens is non-empty. Note that this does not
     * include any revocation checks, i.e., the response of this method is more an indication whether
     * a presentation policy can, in general, be fullfilled or not.
     * 
     * @param p
     * @return
     * @throws CredentialManagerException
     * @throws KeyManagerException 
     * @throws CryptoEngineException 
     */
    public boolean canBeSatisfied(String username, PresentationPolicyAlternatives p) throws CredentialManagerException, KeyManagerException, CryptoEngineException;

    public IssuanceMessage createIssuanceToken(String username, UiIssuanceReturn uir) throws CryptoEngineException;

    public UiIssuanceArguments createIssuanceToken(String username, IssuanceMessage im)
        throws CredentialManagerException, KeyManagerException, CryptoEngineException;

    public PresentationToken createPresentationToken(String username, UiPresentationReturn upr) throws CryptoEngineException;

    public UiPresentationArguments createPresentationToken(String username,
        PresentationPolicyAlternatives p)
            throws CredentialManagerException, KeyManagerException, CryptoEngineException;
}

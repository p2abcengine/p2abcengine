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

package eu.abc4trust.abce.internal.user.issuanceManager;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class IssuanceManagerUserImpl implements IssuanceManagerUser {

  private final PolicyCredentialMatcher policyCredMatcher;
  private final CryptoEngineUser cryptoEngine;

  @Inject
  IssuanceManagerUserImpl(PolicyCredentialMatcher policyCredMatcher, CryptoEngineUser cryptoEngine) {
    this.policyCredMatcher = policyCredMatcher;
    this.cryptoEngine = cryptoEngine;
  }

  @Override
  public boolean canBeSatisfied(String username, PresentationPolicyAlternatives p)
      throws CredentialManagerException, KeyManagerException, CryptoEngineException {
    return this.policyCredMatcher.canBeSatisfied(username, p);
  }


  
  @Override
  public boolean isRevoked(String username, Credential cred) throws CryptoEngineException {                       
  		return this.cryptoEngine.isRevoked(username, cred);               
  }

  @Override
  public UiPresentationArguments createPresentationToken(String username, PresentationPolicyAlternatives p) throws CredentialManagerException, KeyManagerException, CryptoEngineException {
    return this.policyCredMatcher.createPresentationToken(username, p);
  }

  @Override
  public PresentationToken createPresentationToken(String username, UiPresentationReturn upr) throws CryptoEngineException {
    return this.policyCredMatcher.createPresentationToken(username, upr);
  }

  @Override
  public IssuanceReturn issuanceProtocolStep(String username, IssuanceMessage im) throws CryptoEngineException, CredentialManagerException, KeyManagerException {
    IssuancePolicy ip = cryptoEngine.extractIssuancePolicy(im);
    if(ip != null) {
      // First message of advanced issuance: need to create an issuance token
      UiIssuanceArguments args = this.policyCredMatcher.createIssuanceToken(username, im);
      return new IssuanceReturn(args);
    } else {
      // Simple issuance OR subsequent message of advanced issuance
      IssuMsgOrCredDesc ret = this.cryptoEngine.issuanceProtocolStep(username, im);
      return new IssuanceReturn(ret);
    }
  }

  @Override
  public IssuanceMessage issuanceProtocolStep(String username, UiIssuanceReturn uir) throws CryptoEngineException {
    // We are still processing the first message of advanced issuance
     return this.policyCredMatcher.createIssuanceToken(username, uir);
  }

}

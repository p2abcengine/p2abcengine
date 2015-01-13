//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.guice.abcEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.ibm.zurich.idmx.device.ExternalSecretsHelperImpl;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;

import eu.abc4trust.abce.internal.inspector.credentialManager.NewInspectorKeyManager;
import eu.abc4trust.abce.internal.issuer.credentialManager.NewIssuerKeyManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.NewIssuerTokenManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.revocation.credentialManager.NewRevAuthPrivateDataManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.NewUserCredentialManager;
import eu.abc4trust.abce.internal.verifier.tokenManager.NewVerifierTokenManager;
import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.NewKeyManager;


public class NewKeyManagerModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(KeyManager.class).to(NewKeyManager.class).in(Singleton.class);

    this.bind(TokenManagerVerifier.class).to(NewVerifierTokenManager.class).in(Singleton.class);
    this.bind(TokenManagerIssuer.class).to(NewIssuerTokenManager.class).in(Singleton.class);
    this.bind(CredentialManager.class).to(NewUserCredentialManager.class).in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class)
        .to(NewIssuerKeyManager.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager.class)
        .to(NewInspectorKeyManager.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager.class)
        .to(NewRevAuthPrivateDataManager.class)
        .in(Singleton.class);
    
    this.bind(ExternalSecretsManager.class).to(ExternalSecretsManagerImpl.class).in(Singleton.class);
    this.bind(ExternalSecretsHelper.class).to(ExternalSecretsHelperImpl.class).in(Singleton.class);
  }

}

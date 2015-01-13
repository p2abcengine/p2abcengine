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
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;

import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.PersistentFileTokenStorageIssuer;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuerImpl;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerImpl;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCache;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheImpl;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentCredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentSecretStorage;
import eu.abc4trust.abce.internal.user.credentialManager.SecretStorage;
import eu.abc4trust.abce.internal.verifier.tokenManager.PersistentFileTokenStorage;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenManagerImpl;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenStorage;
import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.db.MockPersistentStorage;
import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerImpl;
import eu.abc4trust.keyManager.KeyStorage;
import eu.abc4trust.keyManager.PersistentKeyStorage;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcardManager.ExternalSecretsHelperImpl;
import eu.abc4trust.smartcardManager.ExternalSecretsManagerImpl;

public class FileBackedKeyManager extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(TokenManagerVerifier.class).to(TokenManagerImpl.class).in(Singleton.class);
    this.bind(TokenManagerIssuer.class).to(TokenManagerIssuerImpl.class).in(Singleton.class);
    this.bind(KeyManager.class).to(KeyManagerImpl.class).in(Singleton.class);
    this.bind(KeyStorage.class).to(PersistentKeyStorage.class).in(Singleton.class);
    this.bind(CredentialManager.class).to(CredentialManagerImpl.class).in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class)
        .to(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManagerImpl.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialStorage.class)
        .to(eu.abc4trust.abce.internal.issuer.credentialManager.PersistentCredentialStorage.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager.class)
        .to(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerImpl.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialStorage.class)
        .to(eu.abc4trust.abce.internal.inspector.credentialManager.PersistentCredentialStorage.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager.class)
        .to(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManagerImpl.class)
        .in(Singleton.class);
    this.bind(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialStorage.class)
        .to(eu.abc4trust.abce.internal.revocation.credentialManager.PersistentCredentialStorage.class)
        .in(Singleton.class);
    this.bind(TokenStorage.class).to(PersistentFileTokenStorage.class).in(Singleton.class);
    this.bind(TokenStorageIssuer.class).to(PersistentFileTokenStorageIssuer.class)
        .in(Singleton.class);
    this.bind(CredentialStorage.class).to(PersistentCredentialStorage.class).in(Singleton.class);
    this.bind(SecretStorage.class).to(PersistentSecretStorage.class).in(Singleton.class);
    this.bind(ImageCache.class).to(ImageCacheImpl.class).in(Singleton.class);
    this.bind(ImageCacheStorage.class).to(PersistentImageCacheStorage.class).in(Singleton.class);
    this.bind(CardStorage.class).in(Singleton.class);
    
    this.bind(ExternalSecretsManager.class).to(ExternalSecretsManagerImpl.class).in(Singleton.class);
    this.bind(ExternalSecretsHelper.class).to(ExternalSecretsHelperImpl.class).in(Singleton.class);
    
    this.bind(PersistentStorage.class).to(MockPersistentStorage.class).in(Singleton.class);
  }

}

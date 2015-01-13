//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.guice.cryptoEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.inspector.MockCryptoEngineInspector;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.issuer.MockCryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.revauth.MockCryptoEngineRevocation;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.user.MockCryptoEngineUser;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.cryptoEngine.verifier.MockCryptoEngineVerifier;

public class MockCryptoEngineModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(CryptoEngineIssuer.class).to(MockCryptoEngineIssuer.class).in(Singleton.class);
    this.bind(CryptoEngineVerifier.class).to(MockCryptoEngineVerifier.class).in(Singleton.class);
    this.bind(CryptoEngineUser.class).to(MockCryptoEngineUser.class).in(Singleton.class);
    this.bind(CryptoEngineInspector.class).to(MockCryptoEngineInspector.class).in(Singleton.class);
    this.bind(CryptoEngineRevocation.class).to(MockCryptoEngineRevocation.class).in(Singleton.class);
  }

}

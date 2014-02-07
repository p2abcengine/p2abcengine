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

package eu.abc4trust.abce.testharness;

import java.util.Random;

import com.google.inject.Module;

import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;

public class BridgingModuleFactory {

  public enum IssuerCryptoEngine {
    IDEMIX(ProductionModuleFactory.CryptoEngine.BRIDGED_WITH_IDEMIX_ISSUER), UPROVE(
        ProductionModuleFactory.CryptoEngine.BRIDGED_WITH_UPROVE_ISSUER);

    private final ProductionModuleFactory.CryptoEngine ce;

    IssuerCryptoEngine(ProductionModuleFactory.CryptoEngine ce) {
      this.ce = ce;
    }

    ProductionModuleFactory.CryptoEngine getCryptoEngine() {
      return ce;
    }
  }

  public static Module newModule(Random random, IssuerCryptoEngine issuerEngine,
      int uProvePortNumber) {
    return IntegrationModuleFactory.newModule(random, issuerEngine.getCryptoEngine(),
        uProvePortNumber);
  }

  public static Module newModule(Random random, int uProvePortNumber) {
    return newModule(random, IssuerCryptoEngine.IDEMIX, uProvePortNumber);
  }

  public static Module newModule(Random random, int uProvePortNumber,
      RevocationProxyAuthority revocationProxyAuthority) {
    return newModule(random, IssuerCryptoEngine.IDEMIX, uProvePortNumber, revocationProxyAuthority);
  }

  public static Module newModule(Random random, IssuerCryptoEngine issuerEngine,
      int uProvePortNumber, final RevocationProxyAuthority revocationProxyAuthority) {
    return IntegrationModuleFactory.newModule(random, issuerEngine.getCryptoEngine(),
        uProvePortNumber, revocationProxyAuthority);
  }

}

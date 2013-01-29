//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

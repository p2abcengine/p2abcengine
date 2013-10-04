//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.verifier;

import com.google.inject.Inject;

import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.PresentationToken;

/*
 * This is a mock implementation of the verifier's crypto engine,
 * which always returns true;
 */
public class MockCryptoEngineVerifier implements CryptoEngineVerifier {

  private final KeyManager keyManager;
  
  @Inject
  public MockCryptoEngineVerifier(KeyManager keyManager) {
    this.keyManager = keyManager;
    System.out.println("*** Using mock Crypto Engine for Verifier *** DO NOT USE IN PRODUCTION ***");
  }
  
  @Override
  public boolean verifyToken(PresentationToken t) throws TokenVerificationException {
    return true;
  }

}

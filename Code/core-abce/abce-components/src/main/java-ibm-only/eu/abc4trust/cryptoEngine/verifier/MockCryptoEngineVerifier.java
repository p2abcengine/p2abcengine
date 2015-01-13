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

package eu.abc4trust.cryptoEngine.verifier;

import java.security.SecureRandom;

import com.google.inject.Inject;

import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/*
 * This is a mock implementation of the verifier's crypto engine,
 * which always returns true;
 */
public class MockCryptoEngineVerifier implements CryptoEngineVerifier {
  
  @Inject
  public MockCryptoEngineVerifier() {
    System.out.println("*** Using mock Crypto Engine for Verifier *** DO NOT USE IN PRODUCTION ***");
  }
  
  @Override
  public boolean verifyToken(PresentationToken t, VerifierParameters vp) throws TokenVerificationException {
    return true;
  }

  @Override
  public VerifierParameters createVerifierParameters(SystemParameters sp) {
    return new ObjectFactory().createVerifierParameters();
  }

  @Override
  public byte[] createNonce() {
    byte[] ret = new byte[256 / 8];
    new SecureRandom().nextBytes(ret);
    return ret;
  }

}

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

package eu.abc4trust.smartcardManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;

import com.ibm.zurich.idmx.smartcard.IdemixSmartcardHelper;

import eu.abc4trust.smartcard.Utils;

// TODO(enr): Maybe we should create a new interface...
public class AbcSmartcardHelper implements IdemixSmartcardHelper {
  
  private byte[] getRealChallengePreimage(byte[] preimage, byte[] nonce) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
    	baos.write(new byte[]{0x01});
    	baos.write(nonce);
    	baos.write(preimage);
	} catch (IOException e) {
		throw new RuntimeException(e);
	}    
    return baos.toByteArray();
  }
  
  @Deprecated
  public BigInteger computeBaseForScopeExclusivePseudonym(URI scope) {
    throw new UnsupportedOperationException("This method should not be called");
  }
  
  @Override
  public BigInteger computeBaseForScopeExclusivePseudonym(URI scope, BigInteger modulus, BigInteger order) {
    return Utils.baseForScopeExclusivePseudonym(scope, modulus, order);
  }

  @Override
  public BigInteger computeChallenge(byte[] preimage, byte[] nonce) {
    byte[] realPreimage = getRealChallengePreimage(preimage, nonce);
    // TODO(enr): Check that cards agree on challenge size
    int challengeSizeBytes = 256 / 8;
    BigInteger challenge = Utils.hashToBigIntegerWithSize(realPreimage, challengeSizeBytes);
    
    /*
     * Idemix computes the response as       r + c*x
     * However the smartcard computes it as  r - c*x
     * We therefore need to negate the challenge served to Idemix so that the proof works out
     */
    return challenge.negate();
  }
}

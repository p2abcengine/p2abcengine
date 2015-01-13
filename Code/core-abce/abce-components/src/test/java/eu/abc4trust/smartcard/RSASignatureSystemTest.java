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

package eu.abc4trust.smartcard;

import static eu.abc4trust.smartcard.RSASignatureSystem.checkSignature;
import static eu.abc4trust.smartcard.RSASignatureSystem.generateSignature;
import static eu.abc4trust.smartcard.RSASignatureSystem.generateSigningKey;
import static eu.abc4trust.smartcard.RSASignatureSystem.getVerificationKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Test;

public class RSASignatureSystemTest {
  
	private static Random rand = new Random(42);
	
  /**
   * Get a pre-made signing key, that is guaranteed correct.
   * @return
   */
  public static RSAKeyPair getSigningKeyForTest() {
    final String PRIME_P = "70571188360506042307847613094148477057531302169803052679623481909129358488637545104501470532845339844023798273409769504097918895864412855792011909871648478844152575922095327155264832590283803768793767801574055427617240954120959617320247914728260768834799819590240340050829925369124350679555595149693218584041";
    final String PRIME_Q = "54478867395568573042610837720609215078493724470940321271631449682442854873765220814615060808964147462816294219038747356713702877245618106659966745126269198815804210241574902637127979291026371089896091053954434215237087886860024519378356774137224838311885787180097266064143125082787902596769955349464878468889";    
    BigInteger p = new BigInteger(PRIME_P);
    BigInteger q = new BigInteger(PRIME_Q);
    RSAKeyPair sk = new RSAKeyPair(p, q);
    sk.sizeModulusBytes = 2048 / 8;
    return sk;
  }
  
  public static RSAKeyPair getAnotherSigningKeyForTest() {
    final String PRIME_P = "4058440496293750523031841958512807038690472320799795073212812417314948221948254569078738719905884867405199113468179727835756248063294153921137400720716697";
    final String PRIME_Q = "5596542440395200023137226930607958682014506332218502418110715759860144713638340510437372964566817477466442368349664975564030439891195176652617726854665003";    
    BigInteger p = new BigInteger(PRIME_P);
    BigInteger q = new BigInteger(PRIME_Q);
    RSAKeyPair sk = new RSAKeyPair(p, q);
    sk.sizeModulusBytes = 256 / 8;
    return sk;
  }
  
  @Test /* This is a non-deterministic test */
  public void testGenerateKey() {
    
    int[] keyLensBits = {256, 512, 512+8, 512-8, 1024, 1248};
    
    for (int keyLengthBits: keyLensBits) {
      int keyLenBytes = keyLengthBits / 8;
      RSAKeyPair sk = generateSigningKey(keyLenBytes);
      
      RSAVerificationKey vk = getVerificationKey(sk);
      
      assertTrue(vk.n.equals(sk.getP().multiply(sk.getQ())));
      
      assertTrue(vk.n.bitLength()>=keyLengthBits);
      assertTrue(vk.n.bitLength()<=keyLengthBits+2);      
    }
  }
  
  @Test
  public void testSigningAndVerification() throws Exception {
    RSAKeyPair sk = getSigningKeyForTest();
    RSAVerificationKey vk = getVerificationKey(sk);
    
    byte[] message = "Hello".getBytes("UTF-8");
    byte[] nonce = "World".getBytes("UTF-8");
    
    RSASignature sig = generateSignature(sk, message, nonce, rand);
    assertTrue(checkSignature(sig, vk, message, nonce));
    assertFalse(checkSignature(sig, vk, nonce, message));
    
    byte[] message2 = "HelloWorld".getBytes("UTF-8");
    byte[] nonce2 = "".getBytes("UTF-8");
    RSASignature sig2 = generateSignature(sk, message2, nonce2, rand);
    assertTrue(checkSignature(sig2, vk, message2, nonce2));
    assertFalse(checkSignature(sig, vk, message2, nonce2));
    assertFalse(checkSignature(sig2, vk, message, nonce2));
    assertFalse(checkSignature(sig2, vk, message, nonce));
  }

}

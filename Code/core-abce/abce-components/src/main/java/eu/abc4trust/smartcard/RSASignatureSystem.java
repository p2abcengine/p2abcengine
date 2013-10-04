//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Implements standard RSA signatures as given in the ABC4TrustLite Manual in Appendix A
 * 
 * @author damgaard
 * 
 */
public class RSASignatureSystem {

	public static boolean checkSignature(RSASignature sig, RSAVerificationKey vk, byte[] message,
			byte[] nonce) {
		byte[] m;
		try{
			m = SmartcardCrypto.extraction(vk, sig, nonce);
		}catch(Exception e){
			System.out.println("got an extraction exception: "+e);
			e.printStackTrace();
			return false;
		}
		return Arrays.equals(message, m);
	}

	public static RSASignature generateSignature(RSAKeyPair sk, byte[] message, byte[] nonce, Random rand) {
		return SmartcardCrypto.generateSignature(message, nonce, sk, rand);
	}  

	public static RSAKeyPair generateSigningKey(int keyLengthBytes) {
		return generateSigningKey(new SecureRandom(), keyLengthBytes);
	}

	public static RSAKeyPair generateSigningKey(Random rand, int keyLengthBytes) {
		RSAKeyPair pair = null;
		int count = 1;
		int maxCount = 100;
		while(true){
			count++;
			if(count > maxCount){
				throw new RuntimeException("Tried generating a key "+maxCount+" times.. Now stopping");
			}
			BigInteger p = BigInteger.probablePrime(((keyLengthBytes*8)/2), rand);
			BigInteger q = BigInteger.probablePrime(((keyLengthBytes*8)/2), rand);
			pair = new RSAKeyPair(p, q);
			try{
				BigInteger.valueOf(3).modInverse(pair.getPhi());
				//Test if the bitlength is correct and that the number is odd.
				if(pair.getN().bitLength() != keyLengthBytes*8 || !pair.getN().testBit(0)){						
					continue;
				}
			}catch(Exception e){
				continue;
			}
			break;
		}
		System.out.println("Got a pair. n bytelength: " + pair.getN().toByteArray().length);
		System.out.println("n bitlength: " + pair.getN().bitLength());
		return pair;
	}

	public static RSAVerificationKey getVerificationKey(RSAKeyPair key) {
		RSAVerificationKey vkey = new RSAVerificationKey();
		vkey.n = key.getP().multiply(key.getQ());
		vkey.sizeModulusBytes = key.sizeModulusBytes;
		return vkey;
	}
}

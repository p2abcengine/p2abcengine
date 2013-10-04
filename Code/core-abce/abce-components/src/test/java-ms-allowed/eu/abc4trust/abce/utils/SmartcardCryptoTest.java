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

package eu.abc4trust.abce.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignature;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.SmartcardCrypto;


public class SmartcardCryptoTest {

	@Test
	public void testBackupRestore(){
		Random rand = new Random(42);
		byte[] toBackup = new byte[250];
		rand.nextBytes(toBackup);
		byte[] key = new byte[128/8]; //AES-128
		rand.nextBytes(key);
		short deviceID = 42;
		
		byte[] archive = SmartcardCrypto.backup(toBackup, key, deviceID, rand);
		byte[][] restorationData = SmartcardCrypto.restore(archive, key);
		short deviceID_prime = ByteBuffer.wrap(restorationData[0]).getShort();
		assertEquals("deviceID differs" + deviceID + " != " + deviceID_prime, deviceID, deviceID_prime);
		assertTrue("Backup restoration failed", Arrays.equals(toBackup, restorationData[1]));
	}
		
	@Test
	public void testAuth(){
		//for(int i = 0; i < 10; i++){
			byte[] data = new byte[200];
			byte[] challenge = new byte[50];
			Integer[] keySizes = new Integer[]{512, 1024, 1536, 2048};
			for(Integer keySize : keySizes){
				RSAKeyPair key = RSASignatureSystem.generateSigningKey(keySize/8);
				RSASignature sig = SmartcardCrypto.generateSignature(data, challenge, key, new SecureRandom());
				RSAVerificationKey ver = new RSAVerificationKey();
				ver.n = key.getN();
				assertTrue("RSASignatureSystem.checkSignature - failed for keysize : " + keySize, RSASignatureSystem.checkSignature(sig, ver, data, challenge));
			}				
		//}
	}
	
}

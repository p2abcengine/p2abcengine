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

package eu.abc4trust.ri.servicehelper.smartcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Element;

import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.smartcard.CredentialBases;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.smartcard.UProveParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;

public class PKIKeyTool {

	public static boolean TEST_KEYS = false;

	public static final int SIGNING_KEY_LENGTH = 1024;
	public static final int MAC_KEY_LENGTH = 128;

	//
	public static final Random random = new SecureRandom();

	@SuppressWarnings("unused")
	public static void generateSignatureKeys(String signatureKeysFolder, String signatureKeysPrefix)
			throws IOException {
		System.out.println("generateSignatureKeys : " + signatureKeysPrefix);



		RSAKeyPair sk_root;
		if (true) { // DONOT USER TEST KEYS!!!   if(!TEST_KEYS) {
			sk_root = RSASignatureSystem.generateSigningKey(SIGNING_KEY_LENGTH/8);
		} else {
//			if (signatureKeysPrefix.startsWith("pki")) {
//				sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getSigningKeyForTest();
//			} else {
//				sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getAnotherSigningKeyForTest();
//			}
		}

		RSAVerificationKey pk_root = RSASignatureSystem.getVerificationKey(sk_root);

		// TODO Verify if files exists...
		storeObjectInFile(sk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_sk");
		storeObjectInFile(pk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_pk");
	}
    @Deprecated
    public static SoftwareSmartcard initSoftwareSmartcard(ProductionModule.CryptoEngine cryptoEngine, SystemParameters systemParameters, RSAKeyPair sk_root, int pin, short deviceID, URI deviceUri) {
      return initSoftwareSmartcard(AbstractHelper.oldCryptoEngineToNewCryptoEngine(cryptoEngine), systemParameters, sk_root, pin, deviceID, deviceUri);
    }
	@Deprecated
	public static SoftwareSmartcard initSoftwareSmartcard(CryptoEngine cryptoEngine, SystemParameters systemParameters, RSAKeyPair sk_root, int pin, short deviceID, URI deviceUri) {
		System.out.println("initSmartCard_Software");
		if(cryptoEngine != CryptoEngine.IDEMIX) {
			throw new IllegalStateException("Can only generate SoftwareSmartcards for Idemix - for now");
		}

		//    SystemParameters systemParameters = issuerParameters.getSystemParameters();

		System.out.println("system parameters : " + systemParameters);

		//    SystemParameters sp = (SystemParameters) Parser.getInstance().parse(getResource("sp.xml"));
		//    StructureStore.getInstance().add("http://www.zurich.ibm.com/security/idmx/v2/sp.xml", sp);
		//    gp = (GroupParameters) Parser.getInstance().parse(getResource("gp.xml"));
		//    StructureStore.getInstance().add("http://www.zurich.ibm.com/security/idmx/v2/gp.xml", gp);
		//

		//
		eu.abc4trust.smartcard.SystemParameters sc_sysParams = createSmartcardSystemParameters(systemParameters);

		SoftwareSmartcard ssc = new SoftwareSmartcard(random);
		ssc.init(pin, sc_sysParams, sk_root, deviceID);		
		SmartcardBlob blob = new SmartcardBlob();
        try {
			blob.blob = deviceUri.toASCIIString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
		}
        ssc.storeBlob(pin, Smartcard.device_name, blob);
        
		System.out.println("SC is now init'ed " + ssc);

		return ssc;
	}

	public static URI signIssuerParametersWithAttendance(ProductionModule.CryptoEngine engine, IssuerParameters issuerParameters, Smartcard ssc, RSAKeyPair sk_root, 
	                                                        int keyIDForCounter, RSAVerificationKey coursePk, int minimumAttendance, BigInteger q, BigInteger p) {
	  return signIssuerParametersWithAttendance(AbstractHelper.oldCryptoEngineToNewCryptoEngine(engine), issuerParameters, ssc, sk_root, keyIDForCounter, coursePk, minimumAttendance, q, p);
	}
	
	public static URI signIssuerParametersWithAttendance(CryptoEngine engine, IssuerParameters issuerParameters, Smartcard ssc, RSAKeyPair sk_root, 
			int keyIDForCounter, RSAVerificationKey coursePk, int minimumAttendance, BigInteger q, BigInteger p) {
		System.out.println("signIssuerParameters");
		switch(engine){
		case IDEMIX:
			// TODO : For now only IDEMIX
			IssuerPublicKey isPK =
			(IssuerPublicKey) Parser.getInstance()
			.parse((Element) issuerParameters.getCryptoParams().getAny().get(0));
			//
			BigInteger R0 = isPK.getCapR()[0];
			BigInteger S = isPK.getCapS();
			BigInteger n = isPK.getN();
			CredentialBases credBases = new CredentialBases(R0, S, n);
			ssc.getNewNonceForSignature();

			System.out.println("params URI : " + issuerParameters.getParametersUID());
			URI parametersUri = issuerParameters.getParametersUID();

			SmartcardStatusCode result = 
					ssc.addIssuerParametersWithAttendanceCheck(sk_root, parametersUri, keyIDForCounter, credBases, coursePk, minimumAttendance);
			System.out.println("RESULT OF ADDING!" + result);
			if(! (result == SmartcardStatusCode.OK)) {
				throw new IllegalStateException("Could not add issuer params..." + result);
			}
			return parametersUri;
		case UPROVE:
			URI uProveIssuerUid = issuerParameters.getParametersUID();
			System.out.println("parameters uid: "+uProveIssuerUid);
			List<Object> cryptoParams = issuerParameters.getCryptoParams().getAny();
			
			//byte[] E = (byte[])cryptoParams.get(0); 
			@SuppressWarnings("unchecked")
			List<byte[]> G = (List<byte[]>)cryptoParams.get(1);			
			byte[] Gd = (byte[])cryptoParams.get(2);
			System.out.println("Gd: " + new BigInteger(1, Gd));
			boolean isDeviceSupported = (Boolean)cryptoParams.get(3);
			if(!isDeviceSupported){
				throw new RuntimeException("Issuer cannot be put on device. isDeviceSupported=false.");
			}
			//String UidH = (String)cryptoParams.get(4);
			//byte[] UidP = (byte[])cryptoParams.get(5);
			boolean usesRecommendedParameters = (Boolean)cryptoParams.get(6);
			System.out.println("UsesRecommended Parameters: " + usesRecommendedParameters);
			
			//BigInteger g = new BigInteger(1, G.get(0));
			BigInteger g = new BigInteger(1, Gd);
			//TODO: Figure out what params we need..
			UProveParams uProveParams = new UProveParams(g, p, q);
			ssc.getNewNonceForSignature();
			result = ssc.addUProveIssuerParametersWithAttendanceCheck(sk_root, uProveIssuerUid, keyIDForCounter, uProveParams, coursePk, minimumAttendance);
			if(result != SmartcardStatusCode.OK){
				throw new IllegalStateException("Could not add UProve issuer params... " + result);
			}
			return uProveIssuerUid;
		default:
			throw new RuntimeException("Cannot issue for other engines than IDEMIX and UPROVE");
		}
	}
    public static URI signIssuerParameters(ProductionModule.CryptoEngine engine, IssuerParameters issuerParameters, Smartcard ssc, RSAKeyPair sk_root,
                                           BigInteger q, BigInteger p) {
      return signIssuerParameters(AbstractHelper.oldCryptoEngineToNewCryptoEngine(engine), issuerParameters, ssc, sk_root, q, p);
    }
	public static URI signIssuerParameters(CryptoEngine engine, IssuerParameters issuerParameters, Smartcard ssc, RSAKeyPair sk_root,
			BigInteger q, BigInteger p) {
		System.out.println("signIssuerParameters");
		switch(engine){
		case IDEMIX:
			IssuerPublicKey isPK =
			(IssuerPublicKey) Parser.getInstance()
			.parse((Element) issuerParameters.getCryptoParams().getAny().get(0));
			//
			BigInteger R0 = isPK.getCapR()[0];
			BigInteger S = isPK.getCapS();
			BigInteger n = isPK.getN();
			System.out.println("Idemix modulus: " + n);
			CredentialBases credBases = new CredentialBases(R0, S, n);
			ssc.getNewNonceForSignature();

			System.out.println("params URI : " + issuerParameters.getParametersUID());
			URI parametersUri = issuerParameters.getParametersUID();

			SmartcardStatusCode result = ssc.addIssuerParameters(sk_root, parametersUri, credBases);
			System.out.println("RESULT OF ADDING! " + result);
			if(! (result == SmartcardStatusCode.OK)) {
				throw new IllegalStateException("Could not add issuer params... " + result);
			}
			return parametersUri;
		case UPROVE:
			System.out.println("algoID: "+issuerParameters.getAlgorithmID());
			System.out.println("credSpecUid: "+issuerParameters.getCredentialSpecUID());
			URI uProveIssuerUid = issuerParameters.getParametersUID();
			System.out.println("parameters uid: "+uProveIssuerUid);
			System.out.println("sys params: "+issuerParameters.getSystemParameters());
			
			List<Object> cryptoParams = issuerParameters.getCryptoParams().getAny();
			
			//byte[] E = (byte[])cryptoParams.get(0); 
			@SuppressWarnings("unchecked")
			List<byte[]> G = (List<byte[]>)cryptoParams.get(1);
			byte[] Gd = (byte[])cryptoParams.get(2);
			System.out.println("Gd: " + new BigInteger(1, Gd));
			boolean isDeviceSupported = (Boolean)cryptoParams.get(3);
			if(!isDeviceSupported){
				throw new RuntimeException("Issuer cannot be put on device. isDeviceSupported=false.");
			}
			//String UidH = (String)cryptoParams.get(4);
			//byte[] UidP = (byte[])cryptoParams.get(5);
			boolean usesRecommendedParameters = (Boolean)cryptoParams.get(6);
			System.out.println("UsesRecommended Parameters: " + usesRecommendedParameters);
			
			//BigInteger g = new BigInteger(1, G.get(0));
			BigInteger g = new BigInteger(1, Gd);
			UProveParams uProveParams = new UProveParams(g, p, q);
			ssc.getNewNonceForSignature();
			result = ssc.addUProveIssuerParameters(sk_root, uProveIssuerUid, uProveParams);
			if(result != SmartcardStatusCode.OK){
				throw new IllegalStateException("Could not add UProve issuer params... " + result);
			}
			return uProveIssuerUid;
		default:
			throw new RuntimeException("Cannot issue for other engines than IDEMIX and UPROVE");
		}
	}

	// copy from ABCE-COMPONENTS
	public static /*SmartcardSystemParameters*/ eu.abc4trust.smartcard.SystemParameters createSmartcardSystemParameters(SystemParameters sysParams) {
		IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(sysParams);
		GroupParameters gp = idemixSystemParameters.getGroupParameters();

		SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();

		BigInteger p = gp.getCapGamma();
		BigInteger g = gp.getG();
		BigInteger subgroupOrder = gp.getRho();
		int zkChallengeSizeBytes = 256 / 8;
		int zkStatisticalHidingSizeBytes = 80 / 8;
		int deviceSecretSizeBytes = 256 / 8;
		int signatureNonceLengthBytes = 128 / 8;
		int zkNonceSizeBytes = 256 / 8;
		int zkNonceOpeningSizeBytes = 256 / 8;

		scSysParams.setPrimeModulus(p);
		scSysParams.setGenerator(g);
		scSysParams.setSubgroupOrder(subgroupOrder);
		scSysParams.setZkChallengeSizeBytes(zkChallengeSizeBytes);
		scSysParams.setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
		scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
		scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
		scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
		scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);
		//    return scSysParams;
		return new eu.abc4trust.smartcard.SystemParameters(scSysParams);

	}
	

	public static eu.abc4trust.smartcard.SystemParameters createSmartcardSystemParameters(GroupParameters gp) {
		SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();
		
		BigInteger p = gp.getCapGamma();
		BigInteger g = gp.getG();
		BigInteger subgroupOrder = gp.getRho();
		int zkChallengeSizeBytes = 256 / 8;
		int zkStatisticalHidingSizeBytes = 80 / 8;
		int deviceSecretSizeBytes = 256 / 8;
		int signatureNonceLengthBytes = 128 / 8;
		int zkNonceSizeBytes = 256 / 8;
		int zkNonceOpeningSizeBytes = 256 / 8;
		
		scSysParams.setPrimeModulus(p);
		scSysParams.setGenerator(g);
		scSysParams.setSubgroupOrder(subgroupOrder);
		scSysParams.setZkChallengeSizeBytes(zkChallengeSizeBytes);
		scSysParams.setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
		scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
		scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
		scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
		scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);
		return new eu.abc4trust.smartcard.SystemParameters(scSysParams);
	}


	public static RSAKeyPair loadPrivateKey(String resourse) throws IOException,
	ClassNotFoundException {
		RSAKeyPair privateKey = loadObjectFromResource(resourse);
		return privateKey;
	}

	public static RSAVerificationKey loadPublicKey(String resourse) throws IOException,
	ClassNotFoundException {
		RSAVerificationKey publicKey = loadObjectFromResource(resourse);
		return publicKey;
	}


	public static void storeObjectInFile(Object object, String prefix, String name)
			throws IOException {
		File file = new File(prefix + name);
		System.out.println("storeObject " + object + " - in file " + file.getAbsolutePath());

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(object);
		fos.close();
	}


	@SuppressWarnings("unchecked")
	public static <T> T loadObjectFromResource(String name) throws IOException,
	ClassNotFoundException {
		System.out.println("Read Params Object from resouce : " + name);
		InputStream is = getInputStream(name);
		ObjectInputStream ois = new ObjectInputStream(is);

		Object object = ois.readObject();
		ois.close();
		is.close();

		return (T) object;
	}

	protected static InputStream getInputStream(String resource) throws IOException {
		InputStream is = AbstractHelper.class.getResourceAsStream(resource);
		if (is == null) {
			File f = new File(resource);
			if (!f.exists()) {
				throw new IllegalStateException("Resource not found :  " + resource);
			}

			is = new FileInputStream(f);
		}
		return is;
	}


	public static String toHex(byte[] mac) {
		StringBuilder macStr = new StringBuilder(); // "hex:");
		for (byte element : mac) {
			String hex = String.format("%02x", element);
			//      System.out.println("- " + hex + " == " + mac[j]);
			macStr.append(hex);
		}
		return macStr.toString();
	}

}

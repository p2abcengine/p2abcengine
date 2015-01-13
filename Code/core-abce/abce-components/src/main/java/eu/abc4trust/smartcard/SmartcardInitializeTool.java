//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SmartcardParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerImpl;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializerObjectGzip;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;

public class SmartcardInitializeTool {

	private static final byte[] accesscode = new byte[]{(byte) 0xDD, (byte) 0xE8, (byte) 0x90, (byte) 0x96, 0x3E, (byte) 0xF8, 0x09, 0x0E};

	//================================== Group Parameters ===================================

	//U-Prove Recommended Parameters (L2048N256)
	//OID = 1.3.6.1.4.1.311.75.1.1.1
	public static final BigInteger p = new BigInteger("ef0990061db67a9eaeba265f1b8fa12b553390a8175bcb3d0c2e5ee5dfb826e229ad37431148ce31f8b0e531777f19c1e381c623e600bff7c55a23a8e649ccbcf833f2dba99e6ad66e52378e92f7492b24ff8c1e6fb189fa8434f5402fe415249ae02bf92b3ed8eaaaa2202ec3417b2079da4f35e985bb42a421cfaba8160b66949983384e56365a4486c046229fc8c818f930b80a60d6c2c2e20c5df880534d4240d0d81e9a370eef676a1c3b0ed1d8ff30340a96b21b89f69c54ceb8f3df17e31bc20c5b601e994445a1d347a45d95f41ae07176c7380c60db2aceddeeda5c5980964362e3a8dd3f973d6d4b241bcf910c7f7a02ed3b60383a0102d8060c27", 16);
	public static final BigInteger q = new BigInteger("c8f750941d91791904c7186d62368ec19e56b330b669d08708f882e4edb82885", 16);
	//public static final BigInteger Gd = new BigInteger("487813c6d3efc50b646745573142de47649cc77789aa545d2fca97e9e5e94639810fda34e77cff614b3a86715c7ae093a1070987b183c3c7efa892e3dca1f98fcfaa39e1d649aaae00f89473db7c8cf92037ad771fc464cb6b76f18325a1b02ea41d29276a1cf9b9bd7b25bb5f9a219ab022c7ab8d25378bcc7b9ffcdb70971c03d320fbff71797338ff24007bd785cfbdaaf4bb219b079b96382dff211e23f554092c3aa8af79e8a60d21355e7d026b3c8207fe4feeaca8a9a8dc5fc8333817c67bf805bbe0c032b10839a9026ba9c9bb120bd4ceebacd3152b66b256e41e4a06224ba3f2d3ab99a26c364fe822c0d2c5e972545c2572561c795fbb68a34018", 16);
	//public static final BigInteger h = new BigInteger("bca29a2d4b226f594591ecedbd1859ccb0ba3d20186b30e0ffbf05ba25788a6720005194c1f005b2ced980ca160254bb48a0e2d756ddcc919afe9017a47905154177fb2c37fb6cc0f4423e8f4a8b8376e0043dddf06255050523d4ee1f68748d0d415732686f01d88d98c75bd1e25fa48cd5bf4cc69b6d67bf0dd5c9cf18ee91ae17ebf128151286de3ab17ac4025a91168d42532144b7357e423f1b8d9dbcee68df89b44150e496ff6d416e4376e2daf9e422807d276572cec335d0587a5d798022415e3737326251d304fd7129183357ef9c8d194447705360b5bb270a2ce6194e5894c1fafad3ca78af080f500227564d43cb63462b1084e9ccd55d002e19", 16);

	//Gd is the device generator used to encode the device secret as: Gd^x
    //p,q,g defines the group, but it seems like g is not used. However, I keep it here just in case:
    //public static final BigInteger g = new BigInteger("bca29a2d4b226f594591ecedbd1859ccb0ba3d20186b30e0ffbf05ba25788a6720005194c1f005b2ced980ca160254bb48a0e2d756ddcc919afe9017a47905154177fb2c37fb6cc0f4423e8f4a8b8376e0043dddf06255050523d4ee1f68748d0d415732686f01d88d98c75bd1e25fa48cd5bf4cc69b6d67bf0dd5c9cf18ee91ae17ebf128151286de3ab17ac4025a91168d42532144b7357e423f1b8d9dbcee68df89b44150e496ff6d416e4376e2daf9e422807d276572cec335d0587a5d798022415e3737326251d304fd7129183357ef9c8d194447705360b5bb270a2ce6194e5894c1fafad3ca78af080f500227564d43cb63462b1084e9ccd55d002e19", 16);

	private static final PseudonymSerializer pseudonymSerializer = new PseudonymSerializerObjectGzip(new CardStorage());
	
    // Signing Keys
    public static boolean TEST_KEYS = false;
    public static final int SIGNING_KEY_LENGTH = 1024;
    public static final int MAC_KEY_LENGTH = 128;


	// 
	final RSAKeyPair rootKey;
	final SystemParameters systemParameters;
	final URI pseudonymScope;
	public SmartcardInitializeTool(RSAKeyPair rootKey, SystemParameters systemParameters, URI scope) {
	  this.rootKey = rootKey;
	  this.systemParameters = systemParameters;
	  this.pseudonymScope = scope;
	}

	CryptoEngine cryptoEngine;
	List<IssuerParameters> issuerParametersList;
	public void setIssuerParameters(CryptoEngine cryptoEngine, List<IssuerParameters> issuerParameters) {
	  this.cryptoEngine = cryptoEngine;
	  this.issuerParametersList = issuerParameters;
	}

	CryptoEngine cryptoEngine_counterCredential;
    IssuerParameters issuerParameters_counterCredential;
    RSAVerificationKey coursePublicKey;	
    public void setIssuerParametersForCounterCredential(CryptoEngine cryptoEngine, IssuerParameters issuerParameters, RSAVerificationKey coursePublicKey) {
      this.cryptoEngine_counterCredential = cryptoEngine;
      this.issuerParameters_counterCredential = issuerParameters;
      this.coursePublicKey = coursePublicKey;
      System.err.println("issuerParams: "+issuerParameters);
    }
	
    public class InitializeResult {
      final BigInteger pseudonymValue;
      final int puk;
      public InitializeResult(BigInteger pv, int puk) {
        this.pseudonymValue = pv;
        this.puk = puk;
      }
      public BigInteger getPseudonymValue() {
        return pseudonymValue;
      }
      public int getPuk() {
        return puk;
      }
    }

	public InitializeResult initializeSmartcard(Smartcard smartcard, int pin, short deviceID, URI deviceURI,
			int minAttendance) throws IOException, ClassNotFoundException, Exception,
			UnsupportedEncodingException {		
		System.out.println("about to initialize the smartcard");

		eu.abc4trust.smartcard.SystemParameters scSysParams = createSmartcardSystemParameters(systemParameters);

		//Ensure that the output folder exists
		int puk = -1;
		if(smartcard instanceof HardwareSmartcard){
			if(((HardwareSmartcard)smartcard).getMode() == 0){
				((HardwareSmartcard) smartcard).setRootMode(accesscode);
			}
		}
		if(!smartcard.wasInit()){ 
			puk = smartcard.init(pin, scSysParams, rootKey, deviceID);
			if(puk == -1){
				throw new Exception("Initialization failed. Aborting!");
			}      
		}            

		//Now we attach an issuer based on the engine type

		for(IssuerParameters issuerParam : issuerParametersList) {
    		URI issuerUri = signIssuerParameters(cryptoEngine, issuerParam, systemParameters, smartcard, rootKey, q, p);
    		System.out.println("Signed Issuer : " + issuerUri);
    		//InitializeSmartcard.checkIssuerParameters(cryptoEngine, issuerParameters, pin, s);
		}
		
		if(cryptoEngine_counterCredential != null) {
		  URI issuerUri = signIssuerParametersWithAttendance(cryptoEngine_counterCredential, issuerParameters_counterCredential, systemParameters, smartcard, rootKey, 1, coursePublicKey, minAttendance, q, p);
          System.out.println("Signed Issuer with Attendance : " + issuerUri);
		}		
		if(smartcard instanceof HardwareSmartcard){
			((HardwareSmartcard)smartcard).setWorkingMode();
		}

		System.out.println("ISSUER PARAMS ADDED!");      

		//now that we have a pin on the card, we store the deviceURI
		SmartcardBlob blob = new SmartcardBlob();
		blob.blob = deviceURI.toASCIIString().getBytes("US-ASCII");
		smartcard.storeBlob(pin, Smartcard.device_name, blob);

		//Generate pseudonym
		BigInteger pseudonymValue = smartcard.computeScopeExclusivePseudonym(pin, pseudonymScope);
		
		PseudonymWithMetadata pwm = generatePseudonymWithMetadata(cryptoEngine, deviceURI, pseudonymValue, deviceID, pseudonymScope);
		
		// store on card
		URI pseudonymUri = pwm.getPseudonym().getPseudonymUID();
		if(pseudonymUri.toString().contains(":") && !pseudonymUri.toString().contains("_")){
			pseudonymUri = URI.create(pseudonymUri.toString().replaceAll(":", "_")); //change all ':' to '_'
        }
    	pseudonymUri = URI.create(CredentialManagerImpl.PSEUDONYM_PREFIX+pseudonymUri.toString());
    	SmartcardStatusCode code = smartcard.storePseudonym(pin, pseudonymUri, pwm, pseudonymSerializer);
    	if(code == SmartcardStatusCode.OK){
    		System.out.println("Pseudonym with uid "+pseudonymUri +" stored on the smartcard.");
    	}else{
    		System.err.println("Storing pseudonym on card failed with status: "+code);
    		throw new Exception("Storing pseudonym on card failed with status: "+code);
    	}
    	return new InitializeResult(pseudonymValue, puk);
	}
	
	private static void checkIssuerParameters(CryptoEngine engine, IssuerParameters ip, int pin, Smartcard sc){
		if(engine.equals(CryptoEngine.IDEMIX)){
          IssuerParametersFacade ipw = new IssuerParametersFacade(ip);
          ClPublicKeyWrapper pkw = new ClPublicKeyWrapper(ipw.getPublicKey());
			BigInteger issuer_n;
      try {
        issuer_n = pkw.getModulus().getValue();
      } catch (ConfigurationException e) {
        throw new RuntimeException(e);
      }
			TrustedIssuerParameters tip = sc.getIssuerParameters(pin, ip.getParametersUID());
			BigInteger card_n = tip.groupParams.getModulus();
			if(!issuer_n.equals(card_n)){
				throw new RuntimeException("n's for issuer parameter "+ip.getParametersUID()+" differed from the ones just put on the card!" +
			"\n ip_n: "+issuer_n+"\n card_n: "+ card_n);
			}
			System.out.println("Issuer parameter "+ip.getParametersUID()+" passed the equality test");
		}else{
			System.out.println("Not checking U-Prove parameters as the moduli is the same for all issuers.");
		}
	}

	public static PseudonymWithMetadata generatePseudonymWithMetadata(CryptoEngine cryptoEngine , URI secretUid, BigInteger pseudonymValue, int notPinButNumber, URI scope) {
	    ObjectFactory of = new ObjectFactory();
	    Pseudonym pseudonym = of.createPseudonym();
	    pseudonym.setSecretReference(secretUid);
	    pseudonym.setExclusive(true);
	    pseudonym.setPseudonymUID(scope);
	    
	    pseudonym.setPseudonymValue(pseudonymValue.toByteArray());
	    pseudonym.setScope(scope.toString());
	  
	    Metadata md = of.createMetadata();
	    PseudonymMetadata pmd = of.createPseudonymMetadata();
	    pmd.setHumanReadableData("Pregenerated pseudonym");
	    pmd.setMetadata(md);
	    PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
	    pwm.setPseudonym(pseudonym);
	    pwm.setPseudonymMetadata(pmd);
	    CryptoParams cryptoEvidence = of.createCryptoParams();
	    URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");
	  
        PseudonymCryptoFacade pcf = new PseudonymCryptoFacade();
        pcf.setScopeExclusivePseudonym(scope, secretUid, pseudonym.getPseudonymValue());
        pwm.setCryptoParams(pcf.getCryptoParams());
	    return pwm;
	}	

	public static void appendToPseudonymFile(File pseudonymFile, short ID, CryptoEngine cryptoEngine, BigInteger pseudonymValue) throws IOException {
		String lineFeed = System.getProperty("line.separator","\n");
		String pseudonymEncoded = URLEncoder.encode(Base64.encodeBytes(pseudonymValue.toByteArray()), "UTF-8");		
		String data = ID+";"+pseudonymEncoded+";"+cryptoEngine+lineFeed;
		appendToFile(pseudonymFile, data.getBytes("UTF-8"));
	}
	
    public static short getID(String line) {
        String[] split = line.split(";");
        return Short.parseShort(split[0]);
    }
    public static String getPseudonymB64(String line) throws UnsupportedEncodingException {
        String[] split = line.split(";");
        return URLDecoder.decode(split[1], "UTF-8");
	}
    public static BigInteger getPseudonymBigInteger(String line) throws UnsupportedEncodingException, IOException {
        String[] split = line.split(";");
        String b64 = URLDecoder.decode(split[1], "UTF-8");
        byte[] bytes = Base64.decode(b64);
        return new BigInteger(bytes);
    }
    public static CryptoEngine getCryptoEngine(String line) throws UnsupportedEncodingException {
        String[] split = line.split(";");
        return CryptoEngine.valueOf(split[2]);
    }

	public static void appendToFile(File f, byte[] data) throws IOException {
		FileOutputStream fos = new FileOutputStream(f, true);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				fos));
		out.write(data);
		out.flush();
		out.close();
	}
	
	
	private URI signIssuerParametersWithAttendance(CryptoEngine engine, IssuerParameters issuerParameters, SystemParameters sp, Smartcard ssc, RSAKeyPair sk_root, 
	            int keyIDForCounter, RSAVerificationKey coursePk, int minimumAttendance, BigInteger q, BigInteger p) {
	  System.out.println("signIssuerParametersWithAttendance");
	  System.out.println("Getting smartcardParametersFacade for sp: "+sp+", and issuer params: "+issuerParameters);
          SmartcardParametersFacade spf = new SmartcardParametersFacade(sp, issuerParameters);
          SmartcardParameters credBases;
          try {
            credBases = spf.getSmartcardParameters();
            System.out.println("credBases, generator1: "+credBases.getBaseForDeviceSecret());
            System.out.println("credBases, generator2: "+credBases.getBaseForCredentialSecretOrNull());
          } catch (CryptoEngineException e) {
            throw new RuntimeException(e);
          }
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
	}

	private URI signIssuerParameters(CryptoEngine engine, IssuerParameters issuerParameters, SystemParameters sp, Smartcard ssc, RSAKeyPair sk_root,
	            BigInteger q, BigInteger p) {
	  System.out.println("signIssuerParameters");
          SmartcardParametersFacade spf = new SmartcardParametersFacade(sp, issuerParameters);
          SmartcardParameters credBases;
          try {
            credBases = spf.getSmartcardParameters();
          } catch (CryptoEngineException e) {
            throw new RuntimeException(e);
          }
	      ssc.getNewNonceForSignature();
	      
	      System.out.println("params URI : " + issuerParameters.getParametersUID());
	      URI parametersUri = issuerParameters.getParametersUID();
	      
	      SmartcardStatusCode result = ssc.addIssuerParameters(sk_root, parametersUri, credBases);
	      System.out.println("RESULT OF ADDING! " + result);
	      if(! (result == SmartcardStatusCode.OK)) {
	        throw new IllegalStateException("Could not add issuer params... " + result);
	      }
	      return parametersUri;
	}

	// copy from ABCE-COMPONENTS
	private /*SmartcardSystemParameters*/ eu.abc4trust.smartcard.SystemParameters createSmartcardSystemParameters(SystemParameters sysParams) {
	  
	  SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();
	  
      EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(systemParameters);        
BigInteger p, g, subgroupOrder;
      try {
        p = spw.getDHModulus().getValue();
        g = spw.getDHGenerator1().getValue();
        subgroupOrder = spw.getDHSubgroupOrder().getValue();
      } catch (ConfigurationException e1) {
        throw new RuntimeException(e1);
      }
	  

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
	
	// Signing Key Tools Methods
	    
    @SuppressWarnings("unused")
    public static void generateSignatureKeys(String signatureKeysFolder, String signatureKeysPrefix)
        throws IOException {
      System.out.println("generateSignatureKeys : " + signatureKeysPrefix);

      //if (signatureKeysPrefix.startsWith("pki")) {
      //    sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getSigningKeyForTest();
      //} else {
      //    sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getAnotherSigningKeyForTest();
      //}

      RSAKeyPair sk_root = RSASignatureSystem.generateSigningKey(SIGNING_KEY_LENGTH/8);
      RSAVerificationKey pk_root = RSASignatureSystem.getVerificationKey(sk_root);
      
      // TODO Verify if files exists...
      storeObjectInFile(sk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_sk");
      storeObjectInFile(pk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_pk");
    }

	// Tools Methods

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


	private static void storeObjectInFile(Object object, String prefix, String name)
	    throws IOException {
	  File file = new File(prefix + name);
	  storeObjectInFile(object, file);
	}
	
	private static void storeObjectInFile(Object object, File file)
	    throws IOException {
	  
	  System.out.println("storeObject " + object + " - in file " + file.getAbsolutePath());

	  FileOutputStream fos = new FileOutputStream(file);
	  ObjectOutputStream oos = new ObjectOutputStream(fos);

	  oos.writeObject(object);
	  fos.close();
	}


	@SuppressWarnings("unchecked")
	private static <T> T loadObjectFromResource(String name) throws IOException,
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
	  InputStream is = SmartcardInitializeTool.class.getResourceAsStream(resource);
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
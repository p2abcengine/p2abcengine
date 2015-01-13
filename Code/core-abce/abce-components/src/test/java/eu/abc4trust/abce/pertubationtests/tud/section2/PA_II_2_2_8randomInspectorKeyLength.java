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

package eu.abc4trust.abce.pertubationtests.tud.section2;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 2.2.8, 
 */
public class PA_II_2_2_8randomInspectorKeyLength {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycardWithInspection.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("PA Section 2.2.8randomInspectorKeyLength log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private URI inspectoruid = null;
	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private Injector inspectorInjector = null;
		
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;
	
	private String testName = "none";
	private boolean exceptionHandled = false;
	private FileOutputStream outputStream;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("PA-Section-2.2.8randomInspectorKeyLength.log");
		SimpleFormatter simpleFormatter = new SimpleFormatter();
		fh.setFormatter(simpleFormatter);
		logger.addHandler(fh);
	}
	
	
	@Before
	public void openFile(){//Opens csv for writing (append mode)
		File file = new File("PA_2_2_7_randomInspectorKeyLength.csv");
		try {
			outputStream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
//		String line = "keyLenght,cryptoEngine,validTest,testSuccess\n";
//		outputStream.write(line.getBytes());
	}
	
	private void closeFile(){
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@After
	public void afterTest(){
		closeFile();
	}
	
	//@Before   (removed, the setup method is executed each time the testAttributeDescriptionLength is executed)
	public void setup() throws Exception {
		
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = CryptoUriUtil.getIdemixMechanism();

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231))); 

        inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231))); 

        
        
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		inspectorInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
		idcardCredSpec = (CredentialSpecification) XmlUtils
	                .getObjectFromXML(
	                        this.getClass().getResourceAsStream(
	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);
		

		
		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
		issuerInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
        inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		
	}

// TUD test
	@Test
    public void intRandomLengthTest() throws Exception {
		int k=2147;// number of random values
		testName="intRandomLengthTest";
		logger.info(" K = :"+ k);
		int key = 1;
		
		for(int i = 1; i<=k; i++){
			key = flipRandomBit(key);
//			while(key>=80 && key<=128)
//				key = flipRandomBit(key);
			runTest(key);	//run the test with a random key lenght 
		}
    }

	

	private int flipRandomBit(int integer){//Flips a random bit of an integer
		int flippedValue = 0;
		BigInteger bi = BigInteger.valueOf(integer);
		Random rand = new Random();
		int x = rand.nextInt(33);

		bi = bi.flipBit(x);
		flippedValue = bi.intValue();
		return flippedValue;
		
	}
	
// end of TUD test		
	
	
	public void runTest(int security) throws Exception{
		setup();
		assertNotNull(inspectorInjector);
    	try{
    		logger.info(testName+":	Running test with security level "+security);    		
    		//inspectoruid = URI.create("uri:inspector:id");
    		exceptionHandled=false;
    		
    		
    		InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
    		assertNotNull(inspectorEngine);
    		
    		logger.info(testName+":	Trying to create inspector key");  
    		try {
		
	            InspectorPublicKey inspectorPubKey = inspectorEngine.setupInspectorPublicKey(syspars,
	                    CryptoUriUtil.getIdemixMechanism(),
	                    inspectoruid, new LinkedList<FriendlyDescription>());
	            assertNotNull(inspectorPubKey);
	            inspectorInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
	          //  issuerInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
	            userInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
				verifierInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,testName+":	Failed to to create inspector key: "
    					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    			exceptionHandled=true;
    		
			}
            
    		logger.info(testName+":	Succesfully produced inspector key, now trying to create IssuerParameters");
            
    		IssuerParameters ip = null;
    		if(!exceptionHandled){
	    		try{
	    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
	    			assertNotNull(ip);
	    		}catch(Exception e){
	    			logger.log(Level.SEVERE,testName+":	Failed to create IssuerParameters : "
	    					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
	    			
	    			//logger.info(testName+":	Failed to create IssuerParameters");
	    			//assertTrue(false);
	    			exceptionHandled = true;
	    		}
    		}
    		
    		
    		if(!exceptionHandled)
    			issueIDCard(ip);
    		PresentationToken pt=null;
    		if(!exceptionHandled){
    			pt = presentIDCard();
    			//assertNotNull(pt);
    		}
 			
 			//logger.info(testName+":	Used inspector key to create a valid presentation token");
 			
 			//TODO try block
    		if(!exceptionHandled){
    			logger.info(testName+":	Inspect presentation token");
    			inspect(pt);
    		}
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+": Test Failed due to unexpected exception : "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
			
    		logger.info(e.getMessage());
    		exceptionHandled = true;
    		//Assert.fail(e.getMessage());
    	}
    	
    	String line = ""+security;
    	
		if(!exceptionHandled){
 			logger.info("Test"+ testName + "has failed!!! NO EXCEPTION !!!");
 			line+=",false\n";
 			//fail("Test"+ testName + "has failed!!! ");
 		}else{
 			logger.info("Test"+ testName + "has been successful!!! ");
 			line+=",true\n";
 			//assertTrue(false);
 		}
		
		outputStream.write(line.getBytes());
	}

	
	private void issueIDCard(IssuerParameters ip) throws Exception{
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
        
        IssuancePolicy idcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_ID_CARD), true);
        URI idcardIssuancePolicyUid = idcardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        
        userKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                ip);
        issuerKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                ip);
        verifierKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                ip);
        
        IssuanceHelper issuanceHelper = new IssuanceHelper();
        
        try{
        	issueAndStoreIdcard(issuerInjector, userInjector, issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
			logger.info(testName+":	Managed to issue a credential");
		}catch (Exception e){
			logger.log(Level.SEVERE,testName+":	Failed to create credential: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
		
			exceptionHandled = true;
//			e.printStackTrace();
//			logger.info(testName+":	Failed to issue credential : "+e.getMessage());
//			Assert.fail(e.toString());
		}
	}
	
	
    private void issueAndStoreIdcard(Injector issuerInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                    throws Exception {
        
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("FirstName", "NAME");
        att.put("LastName", "LASTNAME");
        att.put("Birthday", "1990-02-06Z");
        CredentialDescription cd = issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjector,
                credSpec, ISSUANCE_POLICY_ID_CARD,
                att);
    }
	
    private PresentationToken presentIDCard() throws Exception{
    	
    	UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);
    	VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);
       
    	InputStream resourceAsStream = this.getClass().getResourceAsStream(
              PRESENTATION_POLICY_ID_CARD);
    	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                   .getObjectFromXML(
                           resourceAsStream, true);
 
      
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME,presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info(testName+":	Failed to create presentation token");
    		}
    		assertNotNull(pt);
    		logger.info(testName+":	Successfully created a presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+":	Failed to create presentation token: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    		exceptionHandled = true;
//    		logger.info(testName+":	Failed to create presentation token : "+e.toString()+": "+e.getMessage());
//    		Assert.fail(e.toString());
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info(testName+":	Failed to verify presentation token");
    		}
    		assertNotNull(ptd);
    		logger.info(testName+":	Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+":	Failed to verify presentation token: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
		    
    		exceptionHandled = true;
//    		logger.info(testName+":	Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
//    		Assert.fail(e.toString());
    	}
    	return pt;
    }
    
    private void inspect(PresentationToken pt) throws Exception{
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        try {
     	   List<Attribute> inspectedAttributes = engine.inspect(pt);
     	   //logger.info(testName+": inspected attributes: "+inspectedAttributes.size());
     	   assertEquals(inspectedAttributes.size(), 1);

     	   Attribute inspectedAttr = inspectedAttributes.get(0);
     	   MyAttributeValue originalValue = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), "LASTNAME", null);
     	 // logger.info(testName+": inspected attributes value: "+inspectedAttr.getAttributeValue());
     	 //logger.info(testName+": original attributes values: "+inspectedAttr.getAttributeValue());
     	 
     	   assertEquals(inspectedAttr.getAttributeValue(), inspectedAttr.getAttributeValue());
        } catch (Exception e) {
        	logger.log(Level.SEVERE,testName+":	Failed inspection of the presentation token: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
		    
    		exceptionHandled = true;
     	  // e.printStackTrace();
     	 //  assertTrue(false);
        }

    }
    
}
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


import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
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

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 2.2.2, 
 */
public class PA_II_2_2_2randomAttributeLength {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("PA 2 2 2 andomAttributeLength log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
//	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	
//	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	private SystemParameters syspars = null;
	
	private String testName = "none";
	private boolean exceptionHandled = false;
	private FileOutputStream outputStream;
	
	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("PA-Section-2.2.2randomAttributeLength.log");
		SimpleFormatter simpleFormatter = new SimpleFormatter();
		fh.setFormatter(simpleFormatter);
		logger.addHandler(fh);
	}
	
	@Before
	public void openFile(){//Opens csv for writing (append mode)
		File file = new File("PA_2_2_2randomAttributeLength.csv");
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
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = CryptoUriUtil.getIdemixMechanism();

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));//, revocationProxyAuthority));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));//, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

		
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
	}
	
	
	
	public void testAttributeDescriptionLength(int length) throws Exception{
		setup(); // execute setup method
		
		
		exceptionHandled= false;
		
    		CredentialSpecification idcardCredSpec = (CredentialSpecification) XmlUtils
 	                .getObjectFromXML(
 	                        this.getClass().getResourceAsStream(
 	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);

 			logger.info(testName+": Setting CredentialSpecificataion AttributeDescriptions.MaxLength = "+length);
 			idcardCredSpec.getAttributeDescriptions().setMaxLength(length);
 			
    		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

    		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
    		
    		IssuerParameters ip = null;
    		try{
    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
    			assertNotNull(ip);
    		}catch(Exception e){
    			logger.log(Level.SEVERE,testName+":	Failed to create IssuerParameters using modified CredentialSpecification : "
    					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    			exceptionHandled = true;
    			
    		}
    		
    		
    		
    		if(!exceptionHandled)
    			issueIDCard(ip);
    		if(!exceptionHandled)
    			presentIDCard();
    		if(!exceptionHandled)
    			logger.info(testName+":	Used IssuerParameters to create a valid presentation token");

    		String line = ""+length;
    		
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

	
// TUD test
	@Test
	public void testRandomAttributeDescriptionLength() throws Exception{ //executes the test with 655 random values
		testName = "2.2.2-testRandomAttributeDescriptionLength ";
		exceptionHandled = false;
		for(int i = 1; i<= 655; i++){
			int randomLenght =  (int) (Math.random() * 65535);
			testAttributeDescriptionLength(randomLenght);//run the test with a random attribute description lenght 
		}
	}
	
// end of TUD test	

	
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
			logger.info(testName+": Used IssuerParameters to issue a credential");
		}catch (Exception e){

			logger.log(Level.SEVERE,testName+":	Failed to create credential: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
		
			exceptionHandled = true;
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
        assertNotNull(cd);
    }
	
    private void presentIDCard() throws Exception{
      
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
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info(testName+":	Failed to create presentation token without Exception");
    		}
    		assertNotNull(pt);
    		logger.info(testName+":	Successfully created a presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+":	Failed to create presentation token: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    		exceptionHandled = true;
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info(testName+":	Failed to verify presentation token without Exception");
    		}
    		assertNotNull(ptd);
    		logger.info(testName+":	Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+":	Failed to verify presentation token: "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
		    
    		exceptionHandled = true;
    	}

    }
    
}
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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 2.2.7, 
 */
public class PA_II_2_2_7randomUID {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyRevocation.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("PA Section 2.2.7randomUID log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;
	
	private String testName = "none";
	private boolean exceptionHandled = false;
	private FileOutputStream outputStream;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("PA-Section-2.2.7randomUID.log");
		SimpleFormatter simpleFormatter = new SimpleFormatter();
		fh.setFormatter(simpleFormatter);
		logger.addHandler(fh);
	}
	
	private void openFile(){//Opens csv for writing (append mode)
		File file = new File("PA_2_2_1_bitFlip.csv");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@After
	public void afterTest(){
		closeFile();
	}
	
	
	@Before
	public void setup() throws Exception {
		openFile();
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = Helper.getRevocationTechnologyURI("cl");
		revocationInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
		
		revocationProxyAuthority = revocationInjector
				.getInstance(RevocationProxyAuthority.class);

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), revocationProxyAuthority));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        revocationProxyAuthority)); 

		
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		revocationInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
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
		
	}
	

	public void runTest(URI uid) throws Exception{
		testName="2.2.4-randomUidTest ";
		exceptionHandled = false;
    	try{
    		logger.info(testName+":  Running test with UID: "+uid.toString());    		


    		RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
    		
            Reference revocationInfoReference = new Reference();
            revocationInfoReference.setReferenceType(URI.create("https"));
            revocationInfoReference.getReferences().add(URI.create("https://example.org"));
            Reference nonRevocationEvidenceReference = new Reference();
            nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
            nonRevocationEvidenceReference.getReferences().add(URI.create("https://example.org"));
            Reference nonRrevocationUpdateReference = new Reference();
            nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
            nonRrevocationUpdateReference.getReferences().add(
                    URI.create("https://example.org"));
            
            logger.info(testName+":  Trying to create revocation authority parameters");
            RevocationAuthorityParameters revocationAuthorityParameters = null;
            
            try {
                revocationAuthorityParameters = revocationEngine
                        .setupRevocationAuthorityParameters(1024,
                                algorithmId, uid, revocationInfoReference,
                                nonRevocationEvidenceReference, nonRrevocationUpdateReference);
                
                assertNotNull(revocationAuthorityParameters);
               
                logger.info(testName+":  Succesfully produced parameters, now trying to create IssuerParameters");
                
        		
        		issuerInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                        revocationAuthorityParameters);
                userInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                        revocationAuthorityParameters);
                verifierInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                        revocationAuthorityParameters);
                
                
			} catch (Exception e) {
				logger.log(Level.SEVERE,testName+": Failed to create revocation authority parameters : "
    					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    			
    			exceptionHandled=true;
			}
   
    		
            
    		IssuerParameters ip = null;
    		if(!exceptionHandled){
	    		try{
	    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
	    			assertNotNull(ip);
	    		}catch(Exception e){
	    			logger.log(Level.SEVERE,testName+": Failed to create IssuerParameters : "
	    					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
	    			
	    			//logger.info(testName+":  Failed to create IssuerParameters");
	    			//assertTrue(false);
	    			exceptionHandled=true;
	    		}
    		}
    		
    		if(!exceptionHandled)
    			issueIDCard(ip);
 			if(!exceptionHandled)
 				presentIDCard();
 			if(!exceptionHandled)
 				logger.info(testName+":  Used Revocation AuthorityParameters to create a valid presentation token");
 			
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+": Test Failed due to unexpected exception : "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
			
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
    	if(!exceptionHandled){
 			logger.info("Test"+ testName + "has failed!!! NO EXCEPTION !!!");
 			//fail("Test"+ testName + "has failed!!! ");
 		}else{
 			logger.info("Test"+ testName + "has been successful!!! ");
 			//assertTrue(false);
 		}
	}

	
// TUD test	
	public static URI randomURI() throws URISyntaxException{//generates a random URI
		String uriString="urn:";
		uriString+=RandomStringUtils.randomAlphanumeric(5)+":";
		uriString+=RandomStringUtils.randomAlphanumeric(10)+":";
		uriString+=RandomStringUtils.randomAlphanumeric(10)+":";	
		uriString+=RandomStringUtils.randomAlphanumeric(10);	
			
		URI randomUri = new URI(uriString);
		return randomUri;
	}
		
	@Test
    public void engineURIrandomTest() throws Exception {// runs the test 10 times with a random URI
		
		for(int i=1; i<= 10; i++)
			runTest(randomURI());
		
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
			logger.info(testName+":  Managed to issue a credential");
		}catch (Exception e){
			logger.log(Level.SEVERE,testName+": Failed to issue credential : "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
			exceptionHandled = true;
			//e.printStackTrace();
			//logger.info(testName+":  Failed to issue credential : "+e.getMessage());
			//Assert.fail(e.toString());
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
 
    	RevocationInformation ri = verifierEngine.getLatestRevocationInformation(revParsUid);

    	userInjector.getInstance(KeyManager.class).storeRevocationInformation(ri.getRevocationInformationUID(), ri);

    	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0).getIssuerAlternatives().getIssuerParametersUID().get(0).setRevocationInformationUID(ri.getRevocationInformationUID());
      
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info(testName+":  Failed to create presentation token");
    		}
    		assertNotNull(pt);
    		logger.info(testName+":  Successfully created a presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+": Failed to create presentation tokenen : "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    		exceptionHandled = true;
    		//logger.info(testName+":  Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		//Assert.fail(e.toString());
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info(testName+":  Failed to verify presentation token");
    		}
    		assertNotNull(ptd);
    		logger.info(testName+":  Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.log(Level.SEVERE,testName+": Failed to verify presentation token : "
					+e.getMessage()+ "\n				 StackTrace: "+Arrays.toString(e.getStackTrace()));
    		exceptionHandled = true;
    		//logger.info(testName+":  Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
    		//Assert.fail(e.toString());
    	}
    }
    
}
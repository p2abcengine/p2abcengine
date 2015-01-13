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

package eu.abc4trust.abce.pertubationtests.section2;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
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
 * Pertubation tests 2.2.1, 
 */
@Ignore
public class Test21 {

    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 2.2.1 log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	//private UProveUtils uproveUtils = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private CredentialSpecification idcardCredSpec = null;

	private IssuerAbcEngine issuerEngine = null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-2.2.1.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = CryptoUriUtil.getIdemixMechanism();
		//uproveUtils = new UProveUtils();
/*		revocationInjector = Guice
				.createInjector(BridgingModuleFactory.newModule(new Random(1231),
						uproveUtils.getIssuerServicePort()));
		
		revocationProxyAuthority = revocationInjector
				.getInstance(RevocationProxyAuthority.class);*/

        //userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
        //        new Random(1987), uproveUtils.getUserServicePort()));//, revocationProxyAuthority));
        
        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1987)));
        
        
//        verifierInjector = Guice
 //               .createInjector(BridgingModuleFactory.newModule(new Random(1231),
  //                      uproveUtils.getVerifierServicePort()));//, revocationProxyAuthority));
        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 


		idcardCredSpec = (CredentialSpecification) XmlUtils
	                .getObjectFromXML(
	                        this.getClass().getResourceAsStream(
	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);
		
		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
        
        
	}
	
	public void runTest(int key, CryptoEngine engine) throws Exception{
		logger.info("Running test with security parameter "+key +" and crypto engine type : "+engine.toString());
		SystemParameters sysParams = null;
		try{
			sysParams = testIssuer(key, engine);	
			System.out.println("got sys params");
		}catch(Exception e){
			logger.info("Failed to create SystemParameters : "+e.getMessage());
			assertTrue(false);
		}
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(sysParams);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(sysParams);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(sysParams);
		logger.info("Managed to create SystemParameters, trying to create IssuerParameters");
		IssuerParameters ip = null;
		try{
			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, sysParams, uid, hash, algorithmId, revParsUid, null);
		}catch(Exception e){
			logger.info("Failed to create IssuerParameters: "+e.getMessage());
		//	e.printStackTrace();
			assertTrue(false);
		}

		issueIDCard(ip);
		presentIDCard();
	}
	
	@Test
    public void intMaxTestIdemix() throws Exception {
		runTest(2147483647, CryptoEngine.IDEMIX);
    }

	@Test
    public void intMaxTestUProve() throws Exception {
		runTest(2147483647, CryptoEngine.UPROVE);
    }
	
	@Ignore
	@Test
    public void intMinTestIdemix() throws Exception {
		runTest(-2147483648, CryptoEngine.IDEMIX);
    }

	@Ignore
	@Test
    public void intMinTestUProve() throws Exception {
		runTest(-2147483648, CryptoEngine.UPROVE);
    }
	
	@Test
    public void intZeroTestIdemix() throws Exception {
    	runTest(0, CryptoEngine.IDEMIX);
    }

	@Test
    public void intZeroTestUProve() throws Exception {
    	runTest(0, CryptoEngine.UPROVE);
    }
	
	@Test
    public void intOneTestIdemix() throws Exception {
    	runTest(1, CryptoEngine.IDEMIX);
    }

	@Test
    public void intOneTestUProve() throws Exception {
    	runTest(1, CryptoEngine.UPROVE);
    }
	
	@Test
    public void intMinusOneTestIdemix() throws Exception {
    	runTest(-1, CryptoEngine.IDEMIX);
    }

	@Test
    public void intMinusOneTestUProve() throws Exception {
    	runTest(-1, CryptoEngine.UPROVE);
    }
	
	@Test
    public void intSeventyNineTestIdemix() throws Exception {
    	runTest(79, CryptoEngine.IDEMIX);
    }

	@Test
    public void intSeventyNineTestUProve() throws Exception {
    	runTest(79, CryptoEngine.UPROVE);
    }
	
	@Test
    public void intHundredTwentyNineTestIdemix() throws Exception {
    	runTest(256, CryptoEngine.IDEMIX);
    }

	@Test
    public void intHundredTwentyNineTestUProve() throws Exception {
    	runTest(129, CryptoEngine.UPROVE);
    }
	
    private SystemParameters testIssuer(int securityLevel, CryptoEngine chosenEngine){
    	try{
    	//	UProveUtils uproveUtils = new UProveUtils();
    	//	Injector revocationInjector = Guice
    	//		.createInjector(BridgingModuleFactory.newModule(new Random(1231),
    	//				uproveUtils.getIssuerServicePort()));

    		Injector revocationInjector = Guice
        			.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));

    		
    		RevocationProxyAuthority revocationProxyAuthority = revocationInjector
    				.getInstance(RevocationProxyAuthority.class);
    	
    		//issuerInjector = Guice
    		//		.createInjector(BridgingModuleFactory.newModule(new Random(1231),
            //    		chosenEngine, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));
    		issuerInjector = Guice
    				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                		chosenEngine, revocationProxyAuthority));
        
        
    		issuerEngine = issuerInjector
    				.getInstance(IssuerAbcEngine.class);

    		if(chosenEngine==CryptoEngine.IDEMIX){
    			System.out.println("calling return issuerEngine.setupSystemParameters(securityLevel);");
    			return issuerEngine.setupSystemParameters(securityLevel);
    		} else {
    			
    	 //       Injector fakeInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));
    		       Injector fakeInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), CryptoEngine.IDEMIX));
    		       System.out.println("calling return issuerEngine.setupSystemParameters(securityLevel);");
    			SystemParameters params = issuerEngine
                    .setupSystemParameters(securityLevel);
    			System.out.println("calling return fakeEngine.setupSystemParameters(securityLevel);");
    	        IssuerAbcEngine fakeEngine = fakeInjector.getInstance(IssuerAbcEngine.class);
    	        SystemParameters ideMixSystemParameters = 
    	       		 fakeEngine.setupSystemParameters(securityLevel);

    	      //  params.getAny().addAll(ideMixSystemParameters.getAny());
    	        XmlUtils.fixNestedContent(ideMixSystemParameters.getCryptoParams());
    	        params.getCryptoParams().getContent().addAll(ideMixSystemParameters.getCryptoParams().getContent());

    			
    			return params;
    		}
    	}catch(Exception e){
    		logger.info("Failed to create SystemParameters : "+e.getMessage());
    		Assert.fail(e.getMessage());
    		return null;
    	}
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
			logger.info("Used IssuerParameters to issue a credential");
		}catch (Exception e){
			//e.printStackTrace();
			logger.info("Failed to issue credential : "+e.getMessage());
			Assert.fail(e.toString());
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
       
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info("Failed to create presentation token");
    		}
    		assertNotNull(pt);
    		logger.info("Successfully created a presentation token");
    	}catch(Exception e){
    		logger.info("Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		Assert.fail(e.toString());
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info("Failed to verify presentation token");
    		}
    		assertNotNull(ptd);
    		logger.info("Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.info("Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
    		Assert.fail(e.toString());
    	}
    }

    
    
}

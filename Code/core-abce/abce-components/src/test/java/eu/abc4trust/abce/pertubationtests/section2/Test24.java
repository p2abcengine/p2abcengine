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
 * Pertubation tests 2.2.4, 
 */
public class Test24 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 2.2.4 log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
//	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector userInjector2 = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private Injector verifierInjector2 = null;
	
//	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-2.2.4.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = CryptoUriUtil.getIdemixMechanism();
/*		revocationInjector = Guice
				.createInjector(BridgingModuleFactory.newModule(new Random(1231),
						uproveUtils.getIssuerServicePort()));
		
		revocationProxyAuthority = revocationInjector
				.getInstance(RevocationProxyAuthority.class);*/

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));//, revocationProxyAuthority));
        userInjector2 = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1988)));//, revocationProxyAuthority));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));//, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        verifierInjector2 = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1232)));//, revocationProxyAuthority)); 

		
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
	}
	
	

	@Test
	public void performTest() throws Exception{
		try{
			
    		logger.info("Creating two sets of issuerparameters with identical uids");
    		SystemParameters syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
    		//TODO 

    		CredentialSpecification idcardCredSpec = (CredentialSpecification) XmlUtils
 	                .getObjectFromXML(
 	                        this.getClass().getResourceAsStream(
 	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);

            KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
            KeyManager userKeyManager2 = userInjector2.getInstance(KeyManager.class);
            KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
            KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
            KeyManager verifierKeyManager2 = verifierInjector2.getInstance(KeyManager.class);
    		
    		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		userInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		verifierInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);


    		
    		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

    		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
    		
    		userInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

    		verifierInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

    		logger.info("Creating first set of IssuerParameters");
    		IssuerParameters ip = null;
    		try{
    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
    		}catch(Exception e){
    			logger.info("Failed to create first set of IssuerParameters");
    			assertTrue(false);
    		}

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

            logger.info("Creating second set of IssuerParameters");

    		try{
    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
    		}catch(Exception e){
    			logger.info("Failed to create second set of IssuerParameters");
    			assertTrue(false);
    		}

            userKeyManager2.storeIssuerParameters(idcardIssuancePolicyUid,
                    ip);
            issuerKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                    ip);
            verifierKeyManager2.storeIssuerParameters(idcardIssuancePolicyUid,
                    ip);
    		
    		
            logger.info("Trying to Issue a credential using the first set of IssuerParameters");
            IssuanceHelper issuanceHelper = new IssuanceHelper();
            boolean first = false;
            try{
            	issueAndStoreIdcard(issuerInjector, userInjector, issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
    			logger.info("Used first set of IssuerParameters to issue a credential");
    			first = true;
    		}catch (Exception e){
    		//	e.printStackTrace();
    			logger.info("Failed to issue credential using first set of IssuerParameters: "+e.getMessage());
//    			Assert.fail(e.toString());
    		}

            logger.info("Trying to Issue a credential using the second set of IssuerParameters");
            boolean second = false;
            try{
            	issueAndStoreIdcard(issuerInjector, userInjector2, issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
    			logger.info("Used second set of IssuerParameters to issue a credential");
    			second = true;
    		}catch (Exception e){
    			e.printStackTrace();
    			logger.info("Failed to issue credential using second set of IssuerParameters: "+e.getMessage());
    			//Assert.fail(e.toString());
    		}
            
            
    		if(first){ 			
    			logger.info("Trying to perform presentation using first set of Issuer Parameters");
    			boolean succ = presentIDCard(userInjector, verifierInjector);
    			if(succ){
    				logger.info("Presentation using first set of Issuer Parameters was successful");
    			}else {
    				logger.info("Presentation using first set of Issuer Parameters failed");
    			}
    		}
    		
    		if(second){
    			logger.info("Trying to perform presentation using second set of Issuer Parameters");
    			boolean succ = presentIDCard(userInjector2, verifierInjector2);
    			if(succ){
    				logger.info("Presentation using second set of Issuer Parameters was successful");
    			}else {
    				logger.info("Presentation using second set of Issuer Parameters failed");
    			}
    		}
 			
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
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
	
    private boolean presentIDCard(Injector uInjector, Injector vInjector) throws Exception{
    	
    	UserAbcEngine userEngine = uInjector
                .getInstance(UserAbcEngine.class);
    	VerifierAbcEngine verifierEngine = vInjector
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
    		if(pt == null) return false;
    		logger.info("Successfully created a presentation token");
    	}catch(Exception e){
    		logger.info("Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		return false;
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info("Failed to verify presentation token");
    			return false;
    		}
    		logger.info("Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.info("Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
    		return false;
    	}
    	return true;
    }
    
}

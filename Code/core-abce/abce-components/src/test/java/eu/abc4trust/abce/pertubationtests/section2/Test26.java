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
 * Pertubation tests 2.2.6, 
 */
public class Test26 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyRevocation.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 2.2.6 log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private Injector userInjector2 = null;
	private Injector issuerInjector2 = null;
	private Injector verifierInjector2 = null;
		
	
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	private IssuerAbcEngine issuerEngine2 = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-2.2.6.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
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

        userInjector2 = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1988), revocationProxyAuthority));
		
		issuerInjector2 = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1232),
						CryptoEngine.IDEMIX, revocationProxyAuthority));

        verifierInjector2 = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1232),
                		revocationProxyAuthority)); 

        
		
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		issuerEngine2 = issuerInjector
				.getInstance(IssuerAbcEngine.class);

		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		issuerInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);

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
		
		userInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
		issuerInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
	}
	

	@Test
	public void runTest() throws Exception{
    	try{
    		logger.info("Generating first set of revocation authority parameters");    		


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
            
            RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                    .setupRevocationAuthorityParameters(1024,
                            algorithmId, revParsUid, revocationInfoReference,
                            nonRevocationEvidenceReference, nonRrevocationUpdateReference);
            
    		logger.info("Succesfully produced first set of revocation parameters, now trying to create IssuerParameters");
            issuerInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                    revocationAuthorityParameters);
            userInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                    revocationAuthorityParameters);
            verifierInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                    revocationAuthorityParameters);
            
    		IssuerParameters ip = null;
    		try{
    			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
    		}catch(Exception e){
    			logger.info("Failed to create IssuerParameters");
//    			assertTrue(false);
    		}

    		
    		
    		logger.info("Trying to issue credential using first set of revocation parameters");
    		boolean first = issueIDCard(ip, userInjector, issuerInjector, verifierInjector);
    		if(first) {
    			logger.info("Succesfully issued credential using the first set of revocation parameters");
    		}else{
    			logger.info("Failed to issue credential using the first set of revocation parameters");
    		}

    		logger.info("Trying to issue credential using second set of revocation parameters");
    		boolean sec = issueIDCard(ip, userInjector2, issuerInjector2, verifierInjector2);
    		if(sec) {
    			logger.info("Succesfully issued credential using the second set of revocation parameters");
    		}else{
    			logger.info("Failed to issue credential using the second set of revocation parameters");
    		}    		
    		if(first){
    			logger.info("Trying to perform presentation using first set of revocation authority parameters");
    			boolean succ = presentIDCard(userInjector, verifierInjector);
    			if(succ){
    				logger.info("Succesfully performed presentation using first set of revocation authority parameters");
    			}else{
    				logger.info("Failed to perform presentation using first set of revocation authority parameters");
    			}
    		}
    			
 			
    		if(sec){
    			logger.info("Trying to perform presentation using second set of revocation authority parameters");
    			boolean succ = presentIDCard(userInjector2, verifierInjector2);
    			if(succ){
    				logger.info("Succesfully performed presentation using second set of revocation authority parameters");
    			}else{
    				logger.info("Failed to perform presentation using second set of revocation authority parameters");
    			}
    		}
 			
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}


	
	private boolean issueIDCard(IssuerParameters ip, Injector uInjector, Injector iInjector, Injector vInjector) throws Exception{
        KeyManager userKeyManager = uInjector.getInstance(KeyManager.class);
        KeyManager issuerKeyManager = iInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = vInjector.getInstance(KeyManager.class);
        
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
        	issueAndStoreIdcard(iInjector, uInjector, issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
			logger.info("Managed to issue a credential");
		}catch (Exception e){
			logger.info("Failed to issue credential : "+e.getMessage());
			return false;
		}
        return true;
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
 
    	RevocationInformation ri = verifierEngine.getLatestRevocationInformation(revParsUid);

    	uInjector.getInstance(KeyManager.class).storeRevocationInformation(ri.getRevocationInformationUID(), ri);

    	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0).getIssuerAlternatives().getIssuerParametersUID().get(0).setRevocationInformationUID(ri.getRevocationInformationUID());
      
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info("Failed to create presentation token");
    			return false;
    		}
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

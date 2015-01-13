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

package eu.abc4trust.abce.pertubationtests.section4;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
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

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
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
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.AttributeInRevocation;
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
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierDrivenRevocationInPolicy;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 4.2.2, 
 */
public class Test22 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycardWithInspection.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 4.2.2 log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private URI inspectoruid = null;
	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector userInjector2 = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private Injector verifierInjector2 = null;
	private Injector inspectorInjector = null;
	private IssuerParameters issuerParams = null;

		
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-4.2.2.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = Helper.getRevocationTechnologyURI("cl");
		revocationInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
		
		revocationProxyAuthority = revocationInjector
				.getInstance(RevocationProxyAuthority.class);

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), revocationProxyAuthority));

        userInjector2 = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), revocationProxyAuthority));

		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                		  revocationProxyAuthority)); 

        verifierInjector2 = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                		 revocationProxyAuthority));
        
        inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
        		 revocationProxyAuthority)); 
        
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
		inspectorInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		revocationInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
		idcardCredSpec = (CredentialSpecification) XmlUtils
	                .getObjectFromXML(
	                        this.getClass().getResourceAsStream(
	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);
		
		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		userInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector2.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
        inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
		issuerInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		
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
                URI.create("http://example.org"));
        
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(1024,
                        algorithmId, revParsUid, revocationInfoReference,
                        nonRevocationEvidenceReference, nonRrevocationUpdateReference);
        
        issuerInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                revocationAuthorityParameters);
        userInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                revocationAuthorityParameters);
        verifierInjector.getInstance(KeyManager.class).storeRevocationAuthorityParameters(revParsUid,
                revocationAuthorityParameters);
		
		
        IssuancePolicy idcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_ID_CARD), true);
        URI idcardIssuancePolicyUid = idcardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();
		
		try{
			issuerParams = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
			
			userInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
	                issuerParams);
	        issuerInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
	                issuerParams);
	        verifierInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
	                issuerParams);
		}catch(Exception e){
		}

		InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
        InspectorPublicKey inspectorPubKey = inspectorEngine.setupInspectorPublicKey(syspars,
                CryptoUriUtil.getIdemixMechanism(),
                inspectoruid, new LinkedList<FriendlyDescription>()); 
		
        inspectorInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
        userInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
		verifierInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);

		
		
	}

		
	@Test
	public void runTestWrongVersion() throws Exception{
    	try{
    		logger.info("Running test with version 2.0");    		
            issueIDCard(userInjector, issuerInjector, verifierInjector);
           	logger.info("Issued credential");
            
           	
        	UserAbcEngine userEngine = userInjector
                    .getInstance(UserAbcEngine.class);
        	VerifierAbcEngine verifierEngine = verifierInjector
                    .getInstance(VerifierAbcEngine.class);
           
        	InputStream resourceAsStream = this.getClass().getResourceAsStream(
                  PRESENTATION_POLICY_ID_CARD);
        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
        	PresentationToken pt = createPresentation(presentationPolicyAlternatives, userEngine);
        	
        	presentationPolicyAlternatives.setVersion("2.0");
        	verifyToken(pt, presentationPolicyAlternatives, verifierEngine);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}

	@Test
	public void runTestPolicyUid() throws Exception{
    	try{
    		logger.info("Running duplicate policyUID");    		
            issueIDCard(userInjector, issuerInjector, verifierInjector);
           	logger.info("Issued credential");
            
           	
        	UserAbcEngine userEngine = userInjector
                    .getInstance(UserAbcEngine.class);
        	VerifierAbcEngine verifierEngine = verifierInjector
                    .getInstance(VerifierAbcEngine.class);
           
        	InputStream resourceAsStream = this.getClass().getResourceAsStream(
                  PRESENTATION_POLICY_ID_CARD);
        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
        	PresentationToken pt = createPresentation(presentationPolicyAlternatives, userEngine);
        	
        	presentationPolicyAlternatives.getPresentationPolicy().get(0).setPolicyUID(URI.create("something:else"));
        	verifyToken(pt, presentationPolicyAlternatives, verifierEngine);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	@Test
	public void runTestDiscloseNonExistantAttribute() throws Exception{
    	try{
    		logger.info("Running test supposed to disclose the AttributeType 'SomeString'");    		
            issueIDCard(userInjector, issuerInjector, verifierInjector);
           	logger.info("Issued credential");
            
           	
        	UserAbcEngine userEngine = userInjector
                    .getInstance(UserAbcEngine.class);
        	VerifierAbcEngine verifierEngine = verifierInjector
                    .getInstance(VerifierAbcEngine.class);
           
        	InputStream resourceAsStream = this.getClass().getResourceAsStream(
                  PRESENTATION_POLICY_ID_CARD);
        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
     
        	PresentationToken pt = createPresentation(presentationPolicyAlternatives, userEngine);
        	
        	
        	AttributeInPolicy nonExistantAttribute = presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
        	.getDisclosedAttribute().get(0);
        	
        	nonExistantAttribute.setAttributeType(URI.create("SomeString"));
        	
        	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
    		.getDisclosedAttribute().clear();
        	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
    		.getDisclosedAttribute().add(nonExistantAttribute);        
        	
        	
        	
        	verifyToken(pt, presentationPolicyAlternatives, verifierEngine);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	
	@Test
	public void runTestUnboundedContent() throws Exception{
    	try{
    		logger.info("Running test with unbounded content in InspectionGrounds");    		
            issueIDCard(userInjector, issuerInjector, verifierInjector);
           	logger.info("Issued credential");
            
           	
        	UserAbcEngine userEngine = userInjector
                    .getInstance(UserAbcEngine.class);
        	VerifierAbcEngine verifierEngine = verifierInjector
                    .getInstance(VerifierAbcEngine.class);
           
        	InputStream resourceAsStream = this.getClass().getResourceAsStream(
                  PRESENTATION_POLICY_ID_CARD);
        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
     
        	PresentationToken pt = createPresentation(presentationPolicyAlternatives, userEngine);
        	
        	AttributeInPolicy nonExistantAttribute = presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
                	.getDisclosedAttribute().get(0);

        	String longString = "longtext";
        	for(int i = 0; i< 10; i++){
        		longString += longString;
        	}
        	nonExistantAttribute.setInspectionGrounds(longString);
        	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
    		.getDisclosedAttribute().clear();
        	presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0)
    		.getDisclosedAttribute().add(nonExistantAttribute);        
        	
        	verifyToken(pt, presentationPolicyAlternatives, verifierEngine);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	@Test
	public void runTestUnknownCredAlias() throws Exception{
    	try{
    		logger.info("Running test using an unknown cred alias");    		
            issueIDCard(userInjector, issuerInjector, verifierInjector);
           	logger.info("Issued credential");
            
           	
        	UserAbcEngine userEngine = userInjector
                    .getInstance(UserAbcEngine.class);
        	VerifierAbcEngine verifierEngine = verifierInjector
                    .getInstance(VerifierAbcEngine.class);
           
        	InputStream resourceAsStream = this.getClass().getResourceAsStream(
                  PRESENTATION_POLICY_ID_CARD);
        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
        	PresentationToken pt = createPresentation(presentationPolicyAlternatives, userEngine);
        	
        	VerifierDrivenRevocationInPolicy vdRevocation = of.createVerifierDrivenRevocationInPolicy();
        	AttributeInRevocation attr = of.createAttributeInRevocation();
        	attr.setCredentialAlias(URI.create("thisisnotanalias"));
        	attr.setAttributeType(URI.create("LastName"));
        	vdRevocation.setRevocationParametersUID(revParsUid);
        	
        	vdRevocation.getAttribute().add(attr);
        	
        	presentationPolicyAlternatives.getPresentationPolicy().get(0)
        	.getVerifierDrivenRevocation().add(vdRevocation);
        	
        	verifyToken(pt, presentationPolicyAlternatives, verifierEngine);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	
	private boolean issueIDCard(Injector uInjector, Injector iInjector, Injector vInjector) throws Exception{
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
	
    private void verifyToken(PresentationToken pt, PresentationPolicyAlternatives presentationPolicyAlternatives, VerifierAbcEngine verifierEngine) throws Exception{
    	
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info("Failed to verify presentation token");
    			assertTrue(false);
    		}
    		logger.info("Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.info("Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
    		assertTrue(false);
    	}
    }
    
    private PresentationToken createPresentation(PresentationPolicyAlternatives presentationPolicyAlternatives, UserAbcEngine userEngine) throws Exception{
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info("Failed to create presentation token");
    			assertTrue(false);
    		}
    		logger.info("Successfully created a presentation token");
    		return pt;
    	}catch(Exception e){
    		logger.info("Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		throw new RuntimeException(e);
    	}
    	
    }
    
}

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

package eu.abc4trust.abce.pertubationtests.section6;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 6.2.1, 
 */
public class Test21 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycardWithInspection.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 6.2.1 log");
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
		
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-6.2.1.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
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
                new Random(1987)));//, revocationProxyAuthority));

		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));//, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        verifierInjector2 = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        
        inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        
        
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector2.getInstance(KeyManager.class).storeSystemParameters(syspars);

		inspectorInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
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

		
		issuerInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
        inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
		
		IssuerParameters ip = null;
		try{
			ip = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
		}catch(Exception e){
			logger.info("Failed to create IssuerParameters");
			assertTrue(false);
		}
		
		
        InspectorPublicKey inspectorPubKey = inspectorEngine.setupInspectorPublicKey(syspars,
                CryptoUriUtil.getIdemixMechanism(),
                inspectoruid, new LinkedList<FriendlyDescription>());
        

        inspectorInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
        userInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);
		verifierInjector.getInstance(KeyManager.class).storeInspectorPublicKey(inspectoruid, inspectorPubKey);

        issueIDCard(ip, userInjector, issuerInjector, verifierInjector);
        
        
        
	}

		
	@Test
	public void runTestWrongVersion() throws Exception{
    	try{
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
    		pt.setVersion("2.0");
    		
   		
   			logger.info("Trying to inspect presentation token with version=2.0");
   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}

	@Test
	public void runTestWithUnboundedNonce() throws Exception{
    	try{
   			logger.info("Trying to inspect presentation token with a large nonce");
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
    		Message m = pt.getPresentationTokenDescription().getMessage();
    		byte[] b= new byte[2147483640];
    		m.setNonce(b);
    		pt.getPresentationTokenDescription().setMessage(m);
    		
   		

   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}

	@Test
	public void runTestWithUnboundedApplicationData() throws Exception{
    	try{
   			logger.info("Trying to inspect presentation token with a large application data");
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
    		Message m = pt.getPresentationTokenDescription().getMessage();
    		byte[] b= new byte[2147483647];
    		m.getApplicationData().getContent().add(b);
    		pt.getPresentationTokenDescription().setMessage(m);
   		

   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}

	@Test
	public void runTestWithUnboundedPseudonymValue() throws Exception{
    	try{
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
   			logger.info("Trying to inspect presentation token with a large pseudonymValue");
    		byte[] b= new byte[2147483647];
    		pt.getPresentationTokenDescription().getPseudonym().get(0).setPseudonymValue(b);

   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	@Test
	public void runTestWithUnboundedCredspecUid() throws Exception{
    	try{
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
   			logger.info("Trying to inspect presentation token with a large CredSpec uid");
   			String longString = "longText";
   			for(int i = 0; i< 100; i++){
   				longString+=longString;
   			}
   			pt.getPresentationTokenDescription().getCredential().get(0).setCredentialSpecUID(URI.create(longString));

   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	@Test
	public void runTestWithUnboundedIssuerParamsUid() throws Exception{
    	try{
    		PresentationToken pt = presentIDCard(userInjector, verifierInjector);
   			logger.info("Trying to inspect presentation token with a large issuer uid");
   			String longString = "longText";
   			for(int i = 0; i< 100; i++){
   				longString+=longString;
   			}
   			pt.getPresentationTokenDescription().getCredential().get(0).setIssuerParametersUID(URI.create(longString));

   			if(inspect(pt)) logger.info("Succesfully inspected token");
   			else logger.info("Failed to inspect presentation token");
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
	
    private PresentationToken presentIDCard(Injector uInjector, Injector vInjector) throws Exception{
    	
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
    			return null;
    		}
    		logger.info("Successfully created a presentation token");
    	}catch(Exception e){
    		logger.info("Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		return null;
    	}
    	
    	try{	
    		PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
    		if(ptd == null){
    			logger.info("Failed to verify presentation token");
    			return null;
    		}
    		logger.info("Succesfully verified presentation token");
    	}catch(Exception e){
    		logger.info("Failed to verify presentation token : "+e.toString()+": "+e.getMessage());
    		return null;
    	}
    	return pt;
    }
    
    private boolean inspect(PresentationToken pt) throws Exception{
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        try {
     	   List<Attribute> inspectedAttributes = engine.inspect(pt);
     	   assertEquals(inspectedAttributes.size(), 1);

     	   Attribute inspectedAttr = inspectedAttributes.get(0);
     	   MyAttributeValue originalValue = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), "LASTNAME", null);
     	 
     	   System.out.println("ins "+inspectedAttr.getAttributeValue()+"\n"+originalValue.getIntegerValueOrNull());
     	   //assertEquals(inspectedAttr.getAttributeValue(), originalValue.getIntegerValueOrNull());
     	   return inspectedAttr.getAttributeValue().equals(originalValue.getIntegerValueOrNull());
        } catch (Exception e) {
     	   //e.printStackTrace();
     	   return false;
        }

    }
    
}

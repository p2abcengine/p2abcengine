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

package eu.abc4trust.abce.pertubationtests.section3;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import javax.xml.bind.JAXBElement;

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
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 3.2.3, 
 */
public class Test23 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    private static final String USERNAME = "defaultUser";
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 3.2.3 log");
    private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private URI inspectoruid = null;

//	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	private UserAbcEngine userEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;
	private IssuerParameters issuerParameters = null;
	private Random random =null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-3.2.3.log");
		logger.addHandler(fh);
		
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = CryptoUriUtil.getIdemixMechanism();
		random = new Random();

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));//, revocationProxyAuthority));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));//, revocationProxyAuthority));
						
        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
      
		userEngine = userInjector
				.getInstance(UserAbcEngine.class);
        
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
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
		
  //      inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
  //              idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		
		try{
			this.issuerParameters = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
		}catch(Exception e){
			logger.info("Failed to create IssuerParameters");
		}

		
	}
	     
	
	@Test
	public void runTest(){
		logger.info("Running test with nonexisting carryover");
		try{
			IssuancePolicy idcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_ID_CARD), true);
		
			KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
			KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
			KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
        
			URI idcardIssuancePolicyUid = idcardIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        
			userKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                issuerParameters);
			issuerKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                issuerParameters);
			verifierKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                issuerParameters);
       
       
            Map<String, Object> att = new HashMap<String, Object>();
            att.put("FirstName", "NAME");
            att.put("LastName", "LASTNAME");
            att.put("Birthday", "1990-02-06Z");
            
            //
        	List<Attribute> issuerAtts = this.populateIssuerAttributes(
                    att, idcardCredSpec);
        	
            IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(
                    idcardIssuancePolicy, issuerAtts);
            assertFalse(issuerIm.isLastMessage());
                      
            ObjectFactory of = new ObjectFactory();

            // Reply from user.
            IssuMsgOrCredDesc userIm = this.userEngine.issuanceProtocolStepFirstChoice(USERNAME, issuerIm
                    .getIssuanceMessage());
           

            
            logger.info("Modifying Issuance (on wire, after being produced by user, but before arriving at issuer\n Setting version=\"55\"");
            IssuanceToken it  = (IssuanceToken)((JAXBElement)userIm.im.getContent().get(0)).getValue();
            it.setVersion("55");
            userIm.im.getContent().clear();
            userIm.im.getContent().add(of.createIssuanceToken(it));
            
            while (!issuerIm.isLastMessage()) {

                assertNotNull(userIm.im);
                System.out.println("im: "+XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
                issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

                assertNotNull(issuerIm.getIssuanceMessage());
                userIm = userEngine.issuanceProtocolStepFirstChoice(USERNAME, issuerIm
                        .getIssuanceMessage());

                boolean userLastMessage = (userIm.cd != null);
                assertTrue(issuerIm.isLastMessage() == userLastMessage);
            }
            assertNull(userIm.im);
            assertNotNull(userIm.cd);
			logger.info("Managed to issue a credential");
		}catch (Exception e){
			e.printStackTrace();
			logger.info("Failed to issue credential : "+e.getMessage());
			Assert.fail(e.toString());
		}
		try{
			presentIDCard();
		}catch(Exception e){
				e.printStackTrace();
				logger.info("Failed to present credential : "+e.getMessage());
				Assert.fail(e.toString());
			}
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
    	return pt;
    }
 
    
    public List<Attribute> populateIssuerAttributes(
            Map<String, Object> issuerAttsMap,
            CredentialSpecification credentialSpecification) throws Exception {
        List<Attribute> issuerAtts = new LinkedList<Attribute>();
        ObjectFactory of = new ObjectFactory();

        
        for (AttributeDescription attdesc : credentialSpecification
                .getAttributeDescriptions().getAttributeDescription()) {
            Attribute att = of.createAttribute();
            att.setAttributeUID(URI.create("" + this.random.nextLong()));
            URI type = attdesc.getType();
            AttributeDescription attd = of.createAttributeDescription();
            attd.setDataType(attdesc.getDataType());
            attd.setEncoding(attdesc.getEncoding());
            attd.setType(type);
            att.setAttributeDescription(attd);
            Object value = issuerAttsMap.get(type.toString());
            if (value != null) {
                issuerAtts.add(att);
                att.setAttributeValue(value);
            }
        }
        return issuerAtts;
    }
    
}

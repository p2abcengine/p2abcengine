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

package eu.abc4trust.abce.pertubationtests.section5;


import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 5.2.1, 
 */
public class Test21 {
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyRevocation.xml";
    
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 5.2.1 log");
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
	private RevocationAbcEngine revocationEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-5.2.1.log");
		logger.addHandler(fh);
	}
	
	@Before
	public void setup() throws Exception {
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = Helper.getRevocationTechnologyURI("cl");//CryptoUriUtil.getIdemixMechanism();

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
		
		revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
		
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

		
	}
	
	@Test
	public void runTestTwoRevocationHandles() throws Exception{
    	try{
    		logger.info("Running test with 2 revocation handles");    		

            List<Attribute> attributes = new ArrayList<Attribute>(2);
            Attribute revocationHandleAttribute = of.createAttribute();
        	
        	revocationHandleAttribute.setAttributeValue(BigInteger.ONE);
        	revocationHandleAttribute.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
        	AttributeDescription attrDesc = of.createAttributeDescription();
        	attrDesc.setEncoding(new URI("urn:abc4trust:1.1:encodign:integer:unsigned"));
        	attrDesc.setDataType(new URI("xs:integer"));
        	attrDesc.setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        	revocationHandleAttribute.setAttributeDescription(attrDesc);
        	
        	attributes.add(revocationHandleAttribute);
            
        	revocationHandleAttribute = of.createAttribute();
        	
        	revocationHandleAttribute.setAttributeValue(BigInteger.ZERO);
        	revocationHandleAttribute.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
        	revocationHandleAttribute.setAttributeDescription(attrDesc);
        	
        	attributes.add(revocationHandleAttribute);
        	
        	logger.info("Trying to revoke 2 revocation handles simultaneously");
            revocationEngine.revoke(revParsUid, attributes);
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}

	@Test
	public void runTestMismatchedTypes() throws Exception{
    	try{
    		logger.info("Running test with misaligned types and values");    		

            List<Attribute> attributes = new ArrayList<Attribute>(2);
            Attribute revocationHandleAttribute = of.createAttribute();
        	
        	revocationHandleAttribute.setAttributeValue(URI.create("urn:some:uri"));
        	revocationHandleAttribute.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
        	AttributeDescription attrDesc = of.createAttributeDescription();
        	attrDesc.setEncoding(new URI("urn:abc4trust:1.1:encodign:integer:unsigned"));
        	attrDesc.setDataType(new URI("xs:anyURI"));
        	attrDesc.setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        	revocationHandleAttribute.setAttributeDescription(attrDesc);
        	
        	attributes.add(revocationHandleAttribute);
        	
        	logger.info("Trying to revoke 2 revocation handles simultaneously");
            revocationEngine.revoke(revParsUid, attributes);
    	}catch(Exception e){
    		e.printStackTrace();
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
	@Test
	public void runTestUnbounded() throws Exception{
    	try{
    		logger.info("Running test with misaligned types and values");    		

            List<Attribute> attributes = new ArrayList<Attribute>(2);
            Attribute revocationHandleAttribute = of.createAttribute();
        	
            String longString = "longtext";
            for(int i = 0; i<100; i++){
            	longString += longString;
            }
            
        	revocationHandleAttribute.setAttributeValue(longString);
        	revocationHandleAttribute.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
        	AttributeDescription attrDesc = of.createAttributeDescription();
        	attrDesc.setEncoding(new URI("urn:abc4trust:1.1:encodign:integer:unsigned"));
        	attrDesc.setDataType(new URI("xs:string"));
        	attrDesc.setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        	revocationHandleAttribute.setAttributeDescription(attrDesc);
        	
        	attributes.add(revocationHandleAttribute);
        	
        	logger.info("Trying to revoke 2 revocation handles simultaneously");
            revocationEngine.revoke(revParsUid, attributes);
    	}catch(Exception e){
    		e.printStackTrace();
    		logger.info(e.getMessage());
    		Assert.fail(e.getMessage());
    	}
	}
	
    
}

//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
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
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * This test checks 3 things:
 * That a user can get issued a simple identity card with firstname, lastname, and birthday.
 * That the cryptoengines can handle inspectable attributes.
 * That the inspection engine can decrypt an inspectable attribute of a presentationtoken.
 */
public class InspectionTest {

  private static final String USERNAME = "defaultUser";
    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String BIRTHDAY = "1990-02-06Z";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD_UTF = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycardUTFEncoded.xml";
    private static final String CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
    private static final String PRESENTATION_POLICY_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycardWithInspection.xml";
    private static final String PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_SAME_INSPECTOR = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyMultipleAttributesInspection.xml";
    private static final String PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_DIFFERENT_INSPECTORS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyMultipleAttributesDifferentInspectors.xml";
    private static final String PRESENTATION_POLICY_INSPECT_AND_REVOKABLE = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyInspectionAndRevocation.xml";
    private static final String PRESENTATION_POLICY_TWO_CREDS_SAME_ATTRIBUTE = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyInspectSameAttributeTwoCredentials.xml";
    private static final String PRESENTATION_POLICY_SIMPLE_STUDENT_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleStudentCard.xml";

    private static final URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");
    private static final URI SECOND_INSPECTOR_URI = URI.create("http://inspector.com/inspector/pub_key_v1");
    private static final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void simpleInspectionIdemixTest() throws Exception {
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", cl_technology, false);
	    entities.addEntity("USER");
	    entities.addEntity("VERIFIER");
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsSingle(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void simpleInspectionUProveTest() throws Exception {
    	URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", uprove_technology, false);
	    entities.addEntity("USER");
	    entities.addEntity("VERIFIER");
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);
	    runTestsSingle(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void multiInspectionUProveTest() throws Exception{
    	URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", uprove_technology, false);
	    entities.addEntity("USER");
	    entities.addEntity("VERIFIER");
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);
	    runTestsMulti(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void multiInspectionIdemixTest() throws Exception{
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", cl_technology, false);
	    entities.addEntity("USER");
	    entities.addEntity("VERIFIER");
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsMulti(keyLength, entities);
   }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void revocationInspectionIdemixTest() throws Exception{
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", cl_technology, true);
	    entities.addEntity("USER", true);
	    entities.addEntity("VERIFIER", true);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsRevocation(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void revocationInspectionUProveTest() throws Exception{
	    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", uprove_technology, true);
	    entities.addEntity("USER", true);
	    entities.addEntity("VERIFIER", true);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsRevocation(keyLength, entities);    	
    }
    
    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void revocationMultipleCredsInspectionIdemixTest() throws Exception{
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("ISSUER", cl_technology, true);
	    entities.addEntity("USER", true);
	    entities.addEntity("VERIFIER", true);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsMultipleCredsRevocation(keyLength, entities);    	
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void idemixTwoAttributesTwoInspectorsTest() throws Exception{
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("ISSUER", cl_technology, false);
	    entities.addEntity("USER", false);
	    entities.addEntity("VERIFIER", false);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);
	    entities.addEntity("INSPECTOR2", CryptoUriUtil.getIdemixMechanism(), false);


	    runTestsDifferentInspectors(keyLength, entities);    	
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void uproveTwoAttributesTwoInspectorsTest() throws Exception{
	    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("ISSUER", uprove_technology, false);
	    entities.addEntity("USER", false);
	    entities.addEntity("VERIFIER", false);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);
	    entities.addEntity("INSPECTOR2", CryptoUriUtil.getIdemixMechanism(), false);


	    runTestsDifferentInspectors(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void idemixTwoCredentialSameAttributeNameTest() throws Exception{
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("ISSUER", cl_technology, true);
	    entities.addEntity("USER", true);
	    entities.addEntity("VERIFIER", true);
	    entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

	    runTestsTwoCredSameAttribute(keyLength, entities);
    }

    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void uproveTwoCredentialSameAttributeNameTest() throws Exception{
    	URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    	int keyLength = 1024;

    	Entities entities = new Entities();

    	entities.addEntity("ISSUER", uprove_technology, true);
    	entities.addEntity("USER", true);
    	entities.addEntity("VERIFIER", true);
    	entities.addEntity("INSPECTOR", CryptoUriUtil.getIdemixMechanism(), false);

    	runTestsTwoCredSameAttribute(keyLength, entities);
    }

    private void runTestsSingle(int keyLength, Entities entities) throws Exception {
    	// Setp up engines
	    Collection<Injector> injectors = createEntities(entities);
	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

	    List<Object> parametersList = new ArrayList<Object>();

	    // Setup issuers
	    URI credentialTechnology = entities.getTechnology("GOVERNMENT");
	    URI inspectorTechnology = entities.getTechnology("INSPECTOR");
	    URI issuerParametersGovernmentUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("GOVERNMENT"), systemParameters,
	      credentialTechnology, issuerParametersGovernmentUID, 3));

	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.INSPECTOR_URI, systemParameters,
	  	      inspectorTechnology));
	    
	    // Store all issuer parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD);

	    IssuanceHelper issuanceHelper = new IssuanceHelper();
		
 	   // Step 1. Get an idcard.
	    System.out.println(">> Get idcard.");

	    this.issueAndStoreIdcard(entities.getInjector("GOVERNMENT"), entities.getInjector("USER"), 
	    		issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
	    
        // Step 2. Use the idcard to create (and verify) a presentationtoken.
        System.out.println(">> Verify.");

        PresentationToken pt = this.createPresentationToken(
                issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), PRESENTATION_POLICY_CREDENTIALS);
	    assertNotNull(pt);

        // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
        System.out.println(">> Inspect.");
        this.inspectSingle(pt, entities.getInjector("INSPECTOR"));
	}

    private void runTestsMulti(int keyLength, Entities entities) throws Exception {
    	// Setp up engines
	    Collection<Injector> injectors = createEntities(entities);
	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

	    List<Object> parametersList = new ArrayList<Object>();

	    // Setup issuers
	    URI credentialTechnology = entities.getTechnology("GOVERNMENT");
	    URI inspectorTechnology = entities.getTechnology("INSPECTOR");
	    URI issuerParametersGovernmentUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("GOVERNMENT"), systemParameters,
	      credentialTechnology, issuerParametersGovernmentUID, 3));

	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.INSPECTOR_URI, systemParameters,
	  	      inspectorTechnology));
	    
	    // Store all issuer parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD);

	    IssuanceHelper issuanceHelper = new IssuanceHelper();
		
 	   // Step 1. Get an idcard.
	    System.out.println(">> Get idcard.");

	    this.issueAndStoreIdcard(entities.getInjector("GOVERNMENT"), entities.getInjector("USER"), 
	    		issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);
	    
        // Step 2. Use the idcard to create (and verify) a presentationtoken.
        System.out.println(">> Verify.");

        PresentationToken pt = this.createPresentationToken(
                issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_SAME_INSPECTOR);
	    assertNotNull(pt);

        // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
        System.out.println(">> Inspect.");
        this.inspectFirstNameAndBirthday(pt, entities.getInjector("INSPECTOR"));
    }

    private void runTestsRevocation(int keyLength, Entities entities) throws Exception {
    	// Setp up engines
        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX));
        
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);
    	
	    Collection<Injector> injectors = createEntities(entities, revocationProxyAuthority);
	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);
	    revocationKeyManager.storeSystemParameters(systemParameters);
	    
	    List<Object> parametersList = new ArrayList<Object>();

        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);

        RevocationAuthorityParameters revocationAuthorityParameters = this.setupRevocationEngine(revocationInjector, REVOCATION_PARAMETERS_UID, keyLength);

        // Setup issuer and inspector
	    URI credentialTechnology = entities.getTechnology("GOVERNMENT");
	    URI inspectorTechnology = entities.getTechnology("INSPECTOR");
	    URI issuerParametersGovernmentUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("GOVERNMENT"), systemParameters,
	      credentialTechnology, issuerParametersGovernmentUID, 4, REVOCATION_PARAMETERS_UID));

	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.INSPECTOR_URI, systemParameters,
	  	      inspectorTechnology));
	    
	    // Store all issuer and inspector parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);

	    addRevocationKeyManagers(entities, revocationAuthorityParameters);
	    
	    IssuanceHelper issuanceHelper = new IssuanceHelper();
		
 	   // Step 1. Get an idcard.
	    System.out.println(">> Get idcard.");
	    CredentialDescription cd = this.issueAndStoreIdcard(entities.getInjector("GOVERNMENT"), entities.getInjector("USER"), 
	    		issuanceHelper, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);
	    
        // Step 2. Use the idcard to create (and verify) a presentationtoken.
        System.out.println(">> Verify.");

        PresentationToken pt = this.createPresentationTokenWithRevocation(
                issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, REVOCATION_PARAMETERS_UID);
	    assertNotNull(pt);

        // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
        System.out.println(">> Inspect.");
        Attribute revocationHandleAttribute = this.inspectRevocation(pt, entities.getInjector("INSPECTOR"));

        // Step 4. Revoke the credential.
        this.revokeCredential(revocationInjector,
                issuanceHelper, REVOCATION_PARAMETERS_UID, revocationHandleAttribute);

        RevocationInformation revocationInformation = revocationEngine
                .updateRevocationInformation(REVOCATION_PARAMETERS_UID);
        
        // Step 5. Verify revoked credential is revoked.
        this.revokedCredentialsShouldNotBeAllowed(
                entities.getInjector("USER"),
                entities.getInjector("VERIFIER"), issuanceHelper, revocationInformation, cd.getCredentialUID());
    }

    private void runTestsMultipleCredsRevocation(int keyLength, Entities entities) throws Exception {
    	// Setp up engines
    	Injector revocationInjector = Guice
    			.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
    					CryptoEngine.IDEMIX));

    	RevocationProxyAuthority revocationProxyAuthority = revocationInjector
    			.getInstance(RevocationProxyAuthority.class);
    	KeyManager revocationKeyManager = revocationInjector
    			.getInstance(KeyManager.class);

    	Collection<Injector> injectors = createEntities(entities, revocationProxyAuthority);
    	SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);
    	revocationKeyManager.storeSystemParameters(systemParameters);

    	List<Object> parametersList = new ArrayList<Object>();

    	RevocationAbcEngine revocationEngine = revocationInjector
    			.getInstance(RevocationAbcEngine.class);

    	RevocationAuthorityParameters revocationAuthorityParameters = this.setupRevocationEngine(revocationInjector, REVOCATION_PARAMETERS_UID, keyLength);
    	// Setup issuer and inspector
    	URI credentialTechnology = entities.getTechnology("ISSUER");
    	URI inspectorTechnology = entities.getTechnology("INSPECTOR");
    	URI issuerParametersGovernmentUID =
    			getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
    	parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
    			credentialTechnology, issuerParametersGovernmentUID, 4, REVOCATION_PARAMETERS_UID));

    	URI issuerParametersStudentcardUID =
    			getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_STUDENT_CARD);
    	parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
    			credentialTechnology, issuerParametersStudentcardUID, 7, REVOCATION_PARAMETERS_UID));

    	parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.INSPECTOR_URI, systemParameters,
    			inspectorTechnology));

    	// Store all issuer and inspector parameters to all key managers
    	entities.storePublicParametersToKeyManagers(parametersList);

    	// Store all credential specifications to all key managers
    	storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);
    	storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

    	addRevocationKeyManagers(entities, revocationAuthorityParameters);

    	IssuanceHelper issuanceHelper = new IssuanceHelper();

    	// Step 1. Get two idcards and a studentcard.
    	System.out.println(">> Get idcard.");
    	CredentialDescription cd = this.issueAndStoreIdcard(entities.getInjector("ISSUER"), entities.getInjector("USER"), 
    			issuanceHelper, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);

    	CredentialDescription cd2 = this.issueAndStoreIdcard(entities.getInjector("ISSUER"), entities.getInjector("USER"), 
    			issuanceHelper, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);

    	System.out.println(">> Get studentcard.");
    	CredentialDescription cd3 = this.issueAndStoreStudentCard(entities.getInjector("ISSUER"), entities.getInjector("USER"),
    			issuanceHelper, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

    	// Step 2a. Use the idcard to create (and verify) a presentationtoken.
    	System.out.println(">> Verify.");

    	PresentationToken pt = this.createPresentationTokenWithRevocation(
    			issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), cd.getCredentialUID(), PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, REVOCATION_PARAMETERS_UID);

    	PresentationToken pt2 = this.createPresentationTokenWithRevocation(
    			issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), cd2.getCredentialUID(), PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, REVOCATION_PARAMETERS_UID);

    	// Step 2b. Use Studentcard to create (and verify) a presentationtoken.
    	PresentationToken studentCardPT = this.createPresentationTokenWithRevocation(
    			issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), cd3.getCredentialUID(), PRESENTATION_POLICY_SIMPLE_STUDENT_CARD, REVOCATION_PARAMETERS_UID);
    	assertNotNull(pt);

    	// Step 3. Inspect the presentationtoken to reveal the revocation handle.
    	System.out.println(">> Inspect.");
    	Attribute revocationHandleAttribute = this.inspectRevocation(pt, entities.getInjector("INSPECTOR"));


    	// Step 4. Revoke the credential.
    	this.revokeCredential(revocationInjector,
    			issuanceHelper, REVOCATION_PARAMETERS_UID, revocationHandleAttribute);

    	RevocationInformation revocationInformation = revocationEngine
    			.updateRevocationInformation(REVOCATION_PARAMETERS_UID);

    	KeyManager hotelKeyManager = entities.getInjector("VERIFIER").getInstance(KeyManager.class);
    	KeyManager userKeyManager = entities.getInjector("USER").getInstance(KeyManager.class);

    	// Step 5. Verify revoked credential is revoked.
    	this.revokedCredentialsShouldNotBeAllowed(
    			entities.getInjector("USER"),
    			entities.getInjector("VERIFIER"), issuanceHelper, revocationInformation, cd.getCredentialUID());
    	userKeyManager.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
    	hotelKeyManager.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
    	entities.getInjector("USER").getInstance(CredentialManager.class).updateNonRevocationEvidence(USERNAME);

    	// Step 6. Verify that studentCredential is still valid. 
    	studentCardPT = this.createPresentationTokenWithRevocation(
    			issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), cd3.getCredentialUID(), PRESENTATION_POLICY_SIMPLE_STUDENT_CARD, REVOCATION_PARAMETERS_UID);

    	// Step 6b. Verify that id card 2 is still valid
    	this.createPresentationTokenWithRevocation(
    			issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), cd2.getCredentialUID(), PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, REVOCATION_PARAMETERS_UID);
    }
    
    private void runTestsDifferentInspectors(int keyLength, Entities entities) throws Exception {
       	// Setp up engines
	    Collection<Injector> injectors = createEntities(entities);
	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

	    List<Object> parametersList = new ArrayList<Object>();

	    // Setup issuers
	    URI credentialTechnology = entities.getTechnology("ISSUER");
	    URI inspectorTechnology = entities.getTechnology("INSPECTOR");
	    URI secondInspectorTechnology = entities.getTechnology("INSPECTOR2");
	    URI issuerParametersGovernmentUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
	      credentialTechnology, issuerParametersGovernmentUID, 3));

	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.INSPECTOR_URI, systemParameters,
	  	      inspectorTechnology));
	    
	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR2"), InspectionTest.SECOND_INSPECTOR_URI, systemParameters,
		  	      secondInspectorTechnology));
		    
	    // Store all issuer parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD_UTF);

	    IssuanceHelper issuanceHelper = new IssuanceHelper();
        
 	   // Step 1. Get an idcard.
	    System.out.println(">> Get idcard.");

	    this.issueAndStoreIdcard(entities.getInjector("ISSUER"), entities.getInjector("USER"), 
	    		issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD_UTF);
	    
        // Step 2. Use the idcard to create (and verify) a presentationtoken.
        System.out.println(">> Verify.");

        PresentationToken pt = this.createPresentationToken(
                issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_DIFFERENT_INSPECTORS);
	    assertNotNull(pt);

        // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
        System.out.println(">> Inspect.");
        this.inspectDifferentInspectors(pt, entities.getInjector("INSPECTOR"), entities.getInjector("INSPECTOR2"));
	}

    private void runTestsTwoCredSameAttribute(int keyLength, Entities entities) throws Exception{
       	// Setp up engines
        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX));
        
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);
    	
	    Collection<Injector> injectors = createEntities(entities, revocationProxyAuthority);

	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);
	    revocationKeyManager.storeSystemParameters(systemParameters);
	    
	    List<Object> parametersList = new ArrayList<Object>();

	    RevocationAuthorityParameters revocationAuthorityParameters = this.setupRevocationEngine(revocationInjector, REVOCATION_PARAMETERS_UID, keyLength);
	    addRevocationKeyManagers(entities, revocationAuthorityParameters);
	    		
	    // Setup issuers
	    URI credentialTechnology = entities.getTechnology("ISSUER");
	    URI inspectorTechnology = entities.getTechnology("INSPECTOR");
	    URI issuerParametersGovernmentUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
	      credentialTechnology, issuerParametersGovernmentUID, 3));

	    parametersList.add(setupInspector(entities.getInjector("INSPECTOR"), InspectionTest.SECOND_INSPECTOR_URI, systemParameters,
	  	      inspectorTechnology));
	    
	    URI issuerParametersStudentcardUID =
    			getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_STUDENT_CARD);
    	parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
    			credentialTechnology, issuerParametersStudentcardUID, 7, REVOCATION_PARAMETERS_UID));
	        
	    // Store all issuer parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD_UTF);
    	storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

	    IssuanceHelper issuanceHelper = new IssuanceHelper();
        
 	   // Step 1. Get an idcard and studentcard.
	    System.out.println(">> Get idcard.");

	    CredentialDescription cd1 = this.issueAndStoreIdcard(entities.getInjector("ISSUER"), entities.getInjector("USER"), 
	    		issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD_UTF);

        System.out.println(">> Get studentcard.");
        CredentialDescription cd2 = this.issueAndStoreStudentCard(entities.getInjector("ISSUER"), entities.getInjector("USER"),
                issuanceHelper, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

        // Step 2. Use the idcard to create (and verify) a presentationtoken.
        System.out.println(">> Verify.");

        PresentationToken pt = this.createPresentationToken(
                issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"), PRESENTATION_POLICY_TWO_CREDS_SAME_ATTRIBUTE);
        assertNotNull(pt);

        // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
        System.out.println(">> Inspect.");
        this.inspectIdenticalAttributeInDifferentCredentials(pt, entities.getInjector("INSPECTOR"));
    }

    private CredentialDescription issueAndStoreIdcard(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                    throws Exception {
        Map<String, Object> passportAtts = this.populateIdcardAttributes();
        return issuanceHelper.issueCredential(USERNAME, governmentInjector, userInjector,
                credSpec, ISSUANCE_POLICY_ID_CARD,
                passportAtts, null);
    }

    private CredentialDescription issueAndStoreStudentCard(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                    throws Exception {
        Map<String, Object> studentAtts = this.populateStudentcardAttributes();
        return issuanceHelper.issueCredential(USERNAME, governmentInjector, userInjector,
                credSpec, ISSUANCE_POLICY_STUDENT_CARD,
                studentAtts, null);
    }

    private RevocationAuthorityParameters setupRevocationEngine(Injector revocationInjector, URI revParamsUid, int keyLength) throws Exception{

        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("url"));
        revocationInfoReference.getReferences().add(URI.create("https://example.org/"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("url"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("https://example.org/"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("url"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("https://example.org/"));
		return revocationEngine.setupRevocationAuthorityParameters(keyLength,
                Helper.getRevocationTechnologyURI("cl"), revParamsUid,
                revocationInfoReference,
                nonRevocationEvidenceReference,
                nonRrevocationUpdateReference);

    }
    
    private Map<String, Object> populateIdcardAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("FirstName", NAME);
        att.put("LastName", LASTNAME);
        att.put("Birthday", BIRTHDAY);
        return att;
    }

    private Map<String, Object> populateStudentcardAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("Name", NAME);
        att.put("LastName", LASTNAME);
        att.put("StudentNumber", 1000);
        att.put("Issued", "2012-11-11Z");
        att.put("Expires", "2015-11-11Z");
        att.put("IssuedBy", "University of X");
        return att;
    }

    private PresentationToken createPresentationTokenWithRevocation(IssuanceHelper issuanceHelper,
    		Injector hotelInjector, Injector userInjector,
            String presentationPolicy, URI revParamsUid) throws Exception {
      return createPresentationTokenWithRevocation(issuanceHelper, hotelInjector, userInjector, null, presentationPolicy, revParamsUid);
    }
    
    private PresentationToken createPresentationTokenWithRevocation(IssuanceHelper issuanceHelper,
            Injector hotelInjector, Injector userInjector,
            URI selectedCredential, String presentationPolicy, URI revParamsUid)
                    throws Exception {

        VerifierAbcEngine verifierEngine = hotelInjector
                .getInstance(VerifierAbcEngine.class);
        RevocationInformation revocationInformation = verifierEngine
                .getLatestRevocationInformation(revParamsUid);

        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(USERNAME, userInjector,
                        presentationPolicy, revocationInformation, null);

        PresentationToken pt = p.first;
        assertNotNull(pt);
        return issuanceHelper.verify(hotelInjector, p.second, p.first);
    }

    private PresentationToken createPresentationToken(IssuanceHelper issuanceHelper,
            Injector verifierInjector, Injector userInjector,
            String presentationPolicy)
                    throws Exception {

        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(USERNAME, userInjector, presentationPolicy, null, null);

        PresentationToken pt = p.first;
        assertNotNull(pt);
        return issuanceHelper.verify(verifierInjector, p.second, p.first);
    }

    private void inspectFirstNameAndBirthday(PresentationToken pt, Injector inspectorInjector){
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        try {
            List<Attribute> inspectedAttributes = engine.inspect(pt);

            assertEquals(inspectedAttributes.size(), 2);

            Attribute inspectedFirstName = inspectedAttributes.get(1);
            Attribute inspectedBirthday = inspectedAttributes.get(0);
            
            assertEquals(NAME, inspectedFirstName.getAttributeValue());
            assertEquals(BIRTHDAY, inspectedBirthday.getAttributeValue().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Test should not fail here!", e);
        }
    }

    private void inspectSingle(PresentationToken pt, Injector inspectorInjector){
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        try {
            List<Attribute> inspectedAttributes = engine.inspect(pt);
            assertEquals(inspectedAttributes.size(), 1);

            Attribute inspectedAttr = inspectedAttributes.get(0);

            assertEquals(NAME,
                    inspectedAttr.getAttributeValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inspectIdenticalAttributeInDifferentCredentials(PresentationToken pt, Injector inspectorInjector){
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        try {
            List<Attribute> inspectedAttributes = engine.inspect(pt);
            assertEquals(inspectedAttributes.size(), 2);

            Attribute inspectedAttr = inspectedAttributes.get(0);
            Attribute inspectedAttr2 = inspectedAttributes.get(1);
            assertEquals(LASTNAME, inspectedAttr.getAttributeValue());
            assertEquals(LASTNAME, inspectedAttr2.getAttributeValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Attribute inspectRevocation(PresentationToken pt, Injector inspectorInjector){
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        Attribute revokedHandle = null;
        try {
            List<Attribute> inspectedAttributes = engine.inspect(pt);
            assertEquals(inspectedAttributes.size(), 2);

            Attribute inspectedAttr1 = inspectedAttributes.get(0);

            revokedHandle = inspectedAttributes.get(1);

            assertEquals(inspectedAttr1.getAttributeValue(), NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull("Failed to inspect revocation handle!", revokedHandle);
        return revokedHandle;
    }

    private void inspectDifferentInspectors(PresentationToken pt, Injector inspectorInjector, Injector secondInspectorInjector){
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        CryptoEngineInspector secondInspector = secondInspectorInjector.getInstance(CryptoEngineInspector.class);
        try{
            MyAttributeValue lastname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
            List<Attribute> inspectedAttributes = engine.inspect(pt);

            assertEquals(inspectedAttributes.size(),1 );

            Attribute inspectedFirstName = inspectedAttributes.get(0);
            assertEquals(inspectedFirstName.getAttributeValue(), NAME);

            inspectedAttributes = secondInspector.inspect(pt);
            assertEquals(1, inspectedAttributes.size());

            Attribute inspectedLastName = inspectedAttributes.get(0);
            assertEquals(LASTNAME, inspectedLastName.getAttributeValue());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void revokeCredential(Injector revocationInjector,
            IssuanceHelper issuanceHelper, URI revParamsUid,
            Attribute revocationHandleAttribute) throws CryptoEngineException {
        issuanceHelper.revokeCredential(revocationInjector, revParamsUid,
                revocationHandleAttribute);
    }
    
    private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            RevocationInformation revocationInformation, URI chosenCredential) throws Exception {
        try {
    
            this.loginToSpecificAccount(userInjector, verifierInjector,
                    issuanceHelper, REVOCATION_PARAMETERS_UID,
                    revocationInformation, chosenCredential, PRESENTATION_POLICY_INSPECT_AND_REVOKABLE);
            fail("We should not be allowed to log in with a revoked credential");
        } catch (TokenVerificationException ex) {
            assertTrue(
                    "We expect the verification to fail due to a revoked credential",
                    ex.getMessage()
                    .startsWith(
                            "The crypto evidence in the presentation token is not valid"));
        } catch (RuntimeException ex) {
          assertTrue(
            "We expect presentation token generation to fail",
            ex.getMessage().startsWith("Cannot generate presentationToken") ||
            ex.getMessage().startsWith("Cannot choose credential, URI does not exist!"));
        }
    }

    private PresentationToken loginToSpecificAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid, RevocationInformation revocationInformation,
            URI chosenCredential, String presentationPolicy)
                    throws Exception {
        
        
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createSpecificPresentationToken(USERNAME, userInjector,
                        presentationPolicy, chosenCredential, revocationInformation, null);
        return issuanceHelper.verify(verifierInjector, p.second, p.first);
    }
    
	private Collection<Injector> createEntities(Entities entities) {
		entities.initInjectors(null);
		return entities.getInjectors();
	}
	
	private Collection<Injector> createEntities(Entities entities, RevocationProxyAuthority revocationProxyAuthority) {
		entities.initInjectors(revocationProxyAuthority);
		return entities.getInjectors();
	}

	private URI getIssuanceParametersUIDFromIssuancePolicy(String pathToIssuancePolicy)
			throws UnsupportedEncodingException, JAXBException, SAXException {
		// Load issuance policy
		IssuancePolicy issuancePolicy =
				(IssuancePolicy) XmlUtils.getObjectFromXML(
						this.getClass().getResourceAsStream(pathToIssuancePolicy), true);

		// Get issuer parameters UID from credential template
		return issuancePolicy.getCredentialTemplate().getIssuerParametersUID();
	}

	private IssuerParameters setupIssuer(Injector issuerInjector, SystemParameters systemParameters,
			URI credentialTechnology, URI issuanceParametersUID,
			int maximalNumberOfAttributes) throws CryptoEngineException {
		return this.setupIssuer(issuerInjector, systemParameters, credentialTechnology, issuanceParametersUID, maximalNumberOfAttributes, null);
	}

	private IssuerParameters setupIssuer(Injector issuerInjector, SystemParameters systemParameters,
			URI credentialTechnology, URI issuanceParametersUID,
			int maximalNumberOfAttributes, URI revocationAuthority) throws CryptoEngineException {
		// Generate issuer parameters.
		IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

		IssuerParameters issuerParameters =
				issuerEngine.setupIssuerParameters(systemParameters,
						maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID,	revocationAuthority, new LinkedList<FriendlyDescription>());

		return issuerParameters;
	}

	private InspectorPublicKey setupInspector(Injector inspectorInjector, URI uid, SystemParameters systemParameters,
			URI inspectorTechnology) throws CryptoEngineException, CredentialManagerException {
		// Generate issuer parameters.
		InspectorAbcEngine inspectorAbcEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
		
		return inspectorAbcEngine.setupInspectorPublicKey(systemParameters, inspectorTechnology, uid, new LinkedList<FriendlyDescription>());
	}
	
	private void storeCredentialSpecificationToKeyManagers(Collection<Injector> injectors,
			String pathToCredentialSpecification) throws KeyManagerException,
			UnsupportedEncodingException, JAXBException, SAXException {
		// 	Load credential specifications.
		CredentialSpecification credSpec =
				(CredentialSpecification) XmlUtils.getObjectFromXML(
						this.getClass().getResourceAsStream(pathToCredentialSpecification), true);

		// Store credential specifications.
		URI specificationUID = credSpec.getSpecificationUID();
		for (Injector injector : injectors) {
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeCredentialSpecification(specificationUID, credSpec);
		}
	}

    private void addRevocationKeyManagers(Entities entities, RevocationAuthorityParameters revAuthParams) 
            throws KeyManagerException{
          for(Injector injector: entities.getInjectors()){
            KeyManager keyManager = injector.getInstance(KeyManager.class);
            keyManager.storeRevocationAuthorityParameters(revAuthParams.getParametersUID(), revAuthParams);
          }
        }
}
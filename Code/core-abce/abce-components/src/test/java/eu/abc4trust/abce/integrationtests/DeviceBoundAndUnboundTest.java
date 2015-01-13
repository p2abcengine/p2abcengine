//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test checks that a user can get issued a device bound credential and
 * a device unbound credential.
 */
public class DeviceBoundAndUnboundTest {

	static public final String CREDENTIAL_SPECIFICATION_ID_CARD = 
			"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
	static public final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = 
			"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml";
	static public final String ISSUANCE_POLICY_ID_CARD = 
			"/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
	static public final String ISSUANCE_POLICY_STUDENT_CARD = 
			"/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
	static public final String PRESENTATION_POLICY_CREDENTIALS = 
			"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyBoundAndUnbound.xml";


	private static final String USERNAME = "defaultUser";

	
	
	@Test(timeout = TestConfiguration.TEST_TIMEOUT)
	public void boundAndUnboundTest() throws Exception {
	    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
	    int keyLength = 1024;

	    Entities entities = new Entities();

	    entities.addEntity("GOVERNMENT", cl_technology, false);
	    entities.addEntity("UNIVERSITY", cl_technology, false);
	    entities.addEntity("USER");
	    entities.addEntity("VERIFIER");

	    runScenario(keyLength, entities);
	}
	
	private void runScenario(int keyLength, Entities entities) throws Exception{

	    Collection<Injector> injectors = createEntities(entities);
	    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

	    List<Object> parametersList = new ArrayList<Object>();

	    // Setup issuers
	    URI credentialTechnology = entities.getTechnology("UNIVERSITY");
	    URI issuerParametersUniversityUID =
	        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_STUDENT_CARD);
	    parametersList.add(setupIssuer(entities.getInjector("UNIVERSITY"), systemParameters,
	      credentialTechnology, issuerParametersUniversityUID, 9));

	    URI issuerParametersGovernmentUID =
		        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
		    parametersList.add(setupIssuer(entities.getInjector("GOVERNMENT"), systemParameters,
		      credentialTechnology, issuerParametersGovernmentUID, 3));

	    // Store all issuer parameters to all key managers
	    entities.storePublicParametersToKeyManagers(parametersList);

	    // Store all credential specifications to all key managers
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD);
	    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

	    IssuanceHelper issuanceHelper = new IssuanceHelper();
	    
	
		// Step 1. Issue credentials.
		this.issueAndStoreStudentCardCredential(entities.getInjector("UNIVERSITY"), entities.getInjector("USER"), issuanceHelper);
		
		this.issueAndStoreIdCardCredential(entities.getInjector("GOVERNMENT"), entities.getInjector("USER"), issuanceHelper);
	    
		// Step 2. Perform presentation.
	    System.out.println(">> Presentation.");
	    PresentationToken pt =
	        this.performPresentation(issuanceHelper, entities.getInjector("VERIFIER"),
	          entities.getInjector("USER"));
	    assertNotNull(pt);

		// Step 3. Perform verification
		PresentationTokenDescription presentationTokenDescription = 
				this.performVerification(pt, entities.getInjector("VERIFIER"), issuanceHelper);
		assertNotNull(presentationTokenDescription);		
	}

	private Map<String, Object> populateStudentCard() {
		Map<String, Object> atts = new HashMap<String, Object>();
		atts.put("Name", "John");
		atts.put("LastName", "Doe");
		atts.put("StudentNumber", 333);
		atts.put("Issued", "1995-05-05Z");
		atts.put("Expires", "2015-05-05Z");
		atts.put("IssuedBy", "some one");
		return atts;
	}

	private Collection<Injector> createEntities(Entities entities) {
		// Assert that required entities are present
		assert (entities.contains("UNIVERSITY"));
		assert (entities.contains("GOVERNMENT"));
		assert (entities.contains("USER"));
		assert (entities.contains("VERIFIER"));

		entities.initInjectors(null);

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
		// Generate issuer parameters.
		IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

		IssuerParameters issuerParameters =
				issuerEngine.setupIssuerParameters(systemParameters,
						maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID,	null, new LinkedList<FriendlyDescription>());

		return issuerParameters;
	}

	private void storeCredentialSpecificationToKeyManagers(Collection<Injector> injectors,
			String pathToCredentialSpecification) throws KeyManagerException,
			UnsupportedEncodingException, JAXBException, SAXException {
		// 	Load credential specifications.
		CredentialSpecification universityCredSpec =
				(CredentialSpecification) XmlUtils.getObjectFromXML(
						this.getClass().getResourceAsStream(pathToCredentialSpecification), true);

		// Store credential specifications.
		URI universitySpecificationUID = universityCredSpec.getSpecificationUID();
		for (Injector injector : injectors) {
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeCredentialSpecification(universitySpecificationUID, universityCredSpec);
		}
	}

	private void issueAndStoreStudentCardCredential(Injector issuerInjector, Injector userInjector,
			IssuanceHelper issuanceHelper) throws Exception {
		
		Map<String, Object> atts = this.populateStudentCard();
		issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjector,
				CREDENTIAL_SPECIFICATION_STUDENT_CARD, ISSUANCE_POLICY_STUDENT_CARD, atts, null);
	}
	
	private void issueAndStoreIdCardCredential(Injector issuerInjector, Injector userInjector,
			IssuanceHelper issuanceHelper) throws Exception {
		
		Map<String, Object> atts = IntegrationTestUtil.populateIdCard("1995-05-05Z");
		issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjector,
				CREDENTIAL_SPECIFICATION_ID_CARD, ISSUANCE_POLICY_ID_CARD, atts, null);
	}

	private PresentationToken performPresentation(IssuanceHelper issuanceHelper, Injector verifierInjector,
			Injector userInjector) throws Exception {
		Pair<PresentationToken, PresentationPolicyAlternatives> p =
				issuanceHelper.createPresentationToken(USERNAME, userInjector,
						PRESENTATION_POLICY_CREDENTIALS, null, null);

		return p.first;
	}

	private PresentationTokenDescription performVerification(
			PresentationToken presentationToken, Injector verifierInjector, IssuanceHelper issuanceHelper) throws Exception {

		return issuanceHelper.verify(verifierInjector, PRESENTATION_POLICY_CREDENTIALS, presentationToken).getPresentationTokenDescription();
	}

}

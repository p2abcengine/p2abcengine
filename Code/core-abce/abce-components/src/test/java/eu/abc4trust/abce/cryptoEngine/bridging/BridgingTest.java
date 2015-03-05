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

package eu.abc4trust.abce.cryptoEngine.bridging;

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertNotNull;

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

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.integrationtests.Entities;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class BridgingTest {

	private static final String PRESENTATION_POLICY_ALTERNATIVES_BRIDGING = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesBridging.xml";
	private static final String ISSUANCE_POLICY_PASSPORT = "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml";
	private static final String CREDENTIAL_SPECIFICATION_PASSPORT = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml";
	private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
	private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml";
	private static final URI REVOCATION_UID = URI.create("revocationUID1");

	private static final String USERNAME = "defaultUser";
	private static final String NAME = "John";
	private static final String LASTNAME = "Doe";

	@Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void bridgingTestUProve() throws Exception {
		URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
		int keyLength = 1024;

		Entities entities = new Entities();
		entities.addEntity("UNIVERSITY", uprove_technology, false);
		entities.addEntity("GOVERNMENT", uprove_technology, true);
		entities.addEntity("USER");
		entities.addEntity("HOTEL");
		runTest(keyLength, entities);
	}

	@Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void bridgingTestIdemix() throws Exception {
		URI cl_technology = Helper.getSignatureTechnologyURI("cl");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("UNIVERSITY", cl_technology, false);
		entities.addEntity("GOVERNMENT", cl_technology, true);
		entities.addEntity("USER");
		entities.addEntity("HOTEL");
		runTest(keyLength, entities);
	}

	@Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void bridgingTestDifferentIssuers() throws Exception {
		URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
		URI cl_technology = Helper.getSignatureTechnologyURI("brands");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("UNIVERSITY", uprove_technology, false);
		entities.addEntity("GOVERNMENT", cl_technology, true);
		entities.addEntity("USER");
		entities.addEntity("HOTEL");
		runTest(keyLength, entities);
	}

	private void runTest(int keyLength, Entities entities) throws Exception {
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

		RevocationAuthorityParameters revocationAuthorityParameters = this.setupRevocationEngine(revocationInjector, REVOCATION_UID, keyLength);

		// Setup issuers
		URI credentialTechnologyGovernment = entities.getTechnology("GOVERNMENT");
		URI issuerParametersGovernmentUID =
				getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_PASSPORT);
		parametersList.add(setupIssuer(entities.getInjector("GOVERNMENT"), systemParameters,
				credentialTechnologyGovernment, issuerParametersGovernmentUID, 7, REVOCATION_UID));

		URI credentialTechnologyUniversity = entities.getTechnology("UNIVERSITY");
		URI issuerParametersUniversityUID =
				getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_STUDENT_CARD);
		parametersList.add(setupIssuer(entities.getInjector("UNIVERSITY"), systemParameters,
				credentialTechnologyUniversity, issuerParametersUniversityUID, 6, null));

		//Generate dummy issuer parameters for range proofs (only necessary for UProve+UProve setup

		IssuerAbcEngine universityEngine = entities.getInjector("UNIVERSITY").getInstance(IssuerAbcEngine.class);

		IssuerParameters dummyForRangeProof =
				universityEngine.setupIssuerParameters(systemParameters, 0, URI.create("cl"),
						URI.create("vp:rangeProof"), null, null);
		parametersList.add(dummyForRangeProof);

		// Store all issuer and inspector parameters to all key managers
		entities.storePublicParametersToKeyManagers(parametersList);

		// Store all credential specifications to all key managers
		storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PASSPORT);
		storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

		addRevocationKeyManagers(entities, revocationAuthorityParameters);

		VerifierAbcEngine verifierEngine = entities.getInjector("HOTEL").getInstance(VerifierAbcEngine.class);

		VerifierParameters verifierParameters = verifierEngine.createVerifierParameters(systemParameters);

		IssuanceHelper issuanceHelper = new IssuanceHelper();


		// Step 1. Get passport.
		System.out.println(">> Get passport.");
		this.issueAndStorePassport(entities.getInjector("GOVERNMENT"), entities.getInjector("USER"),
				issuanceHelper, verifierParameters).getCredentialUID();

		// Step 2. Get student id.
		System.out.println(">> Get student id.");
		this.issueAndStoreStudentId(entities.getInjector("UNIVERSITY"), entities.getInjector("USER"),
				issuanceHelper, verifierParameters).getCredentialUID();

		// Step 4a. Book a hotel room using passport and student card.
		System.out.println(">> Verify.");
		@SuppressWarnings("unused")
		PresentationToken pt = this.bookHotelRoom(
				issuanceHelper, entities.getInjector("HOTEL"), entities.getInjector("USER"), verifierParameters);

		// TODO Verifier driven revocation is currently not implemented
		// Step 5. Do verifier driven revocation
		// ???

		// Step 4b. Booking a hotel room using passport and student card fails
		// because customer is blacklisted by hotel.
		//this.failBookingHotelRoom(hotelInjector,
		//        userInjector, issuanceHelper, verifierParameters);
	}

	private CredentialDescription issueAndStorePassport(Injector governmentInjector,
			Injector userInjector, IssuanceHelper issuanceHelper, VerifierParameters verifierParameters)
					throws Exception {
		Map<String, Object> passportAtts = this.populatePassportAttributes();
		return issuanceHelper.issueCredential(USERNAME, governmentInjector, userInjector,
				CREDENTIAL_SPECIFICATION_PASSPORT, ISSUANCE_POLICY_PASSPORT,
				passportAtts, verifierParameters);
	}

	private Map<String, Object> populatePassportAttributes() {
		Map<String, Object> att = new HashMap<String, Object>();
		att.put("Name", NAME);
		att.put("LastName", LASTNAME);
		att.put(REVOCATION_HANDLE_STR,
				"http://admin.ch/passport/revocation/parameters");
		att.put("PassportNumber", 895749);
		att.put("Issued", "2010-02-06Z");
		att.put("Expires", "2022-02-06Z");
		att.put("IssuedBy", "admin.ch");
		return att;
	}

	private CredentialDescription issueAndStoreStudentId(Injector univsersityInjector,
			Injector userInjector, IssuanceHelper issuanceHelper, VerifierParameters verifierParameters)
					throws Exception {
		Map<String, Object> atts = this
				.populateStudentIdIssuerAttributes();
		return issuanceHelper.issueCredential(USERNAME, univsersityInjector, userInjector,
				CREDENTIAL_SPECIFICATION_STUDENT_CARD,
				ISSUANCE_POLICY_STUDENT_CARD, atts, verifierParameters);
	}

	private Map<String, Object> populateStudentIdIssuerAttributes() {
		Map<String, Object> atts = new HashMap<String, Object>();
		atts.put("Name", NAME);
		atts.put("LastName", LASTNAME);
		atts.put("StudentNumber", 345);
		atts.put("Issued", "2012-02-02Z");
		atts.put("Expires", "2022-02-02Z");

		atts.put("IssuedBy", "ethz.ch");

		return atts;
	}


	private PresentationToken bookHotelRoom(
			IssuanceHelper issuanceHelper, Injector hotelInjector,
			Injector userInjector, VerifierParameters verifierParameters) throws Exception {
		return this.bookHotel(issuanceHelper, hotelInjector, userInjector, verifierParameters);
	}


	private PresentationToken bookHotel(IssuanceHelper issuanceHelper,
			Injector hotelInjector, Injector userInjector, VerifierParameters verifierParameters)
					throws Exception {
		Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
				.createPresentationToken(USERNAME, userInjector,
						PRESENTATION_POLICY_ALTERNATIVES_BRIDGING, verifierParameters);

		// Store all required cred specs in the verifier key manager.
		KeyManager hotelKeyManager = hotelInjector.getInstance(KeyManager.class);
		KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

		PresentationToken pt = p.first;
		assertNotNull(pt);
		for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
			hotelKeyManager.storeCredentialSpecification(
					cit.getCredentialSpecUID(),
					userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
		}

		return issuanceHelper.verify(hotelInjector, p.second, p.first);
	}

	// Used for testing deactivated feature.
	private void failBookingHotelRoom(
			Injector hotelInjector, Injector userInjector,
			IssuanceHelper issuanceHelper, VerifierParameters verifierParameters) throws Exception {
		PresentationToken pt = this.bookHotel(issuanceHelper, hotelInjector, userInjector, verifierParameters);
		// TODO: The user should not be able to create a presentation token as
		// his passport number is on the Hotel blacklist.
		Assert.assertNull(pt);
	}

	private Collection<Injector> createEntities(Entities entities, RevocationProxyAuthority revocationProxyAuthority) {
		entities.initInjectors(revocationProxyAuthority);
		return entities.getInjectors();
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
			int maximalNumberOfAttributes, URI revocationAuthority) throws CryptoEngineException {
		// Generate issuer parameters.
		IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

		IssuerParameters issuerParameters =
				issuerEngine.setupIssuerParameters(systemParameters,
						maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID,	revocationAuthority, new LinkedList<FriendlyDescription>());

		return issuerParameters;
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

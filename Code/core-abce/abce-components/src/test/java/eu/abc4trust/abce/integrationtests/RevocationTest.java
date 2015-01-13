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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test checks that a user can get issued a simple identity card with firstname, lastname, and
 * birthday.
 */
public class RevocationTest {
	private static final String USERNAME = "defaultUser";
	private static final URI REVOCATION_PARAMETERS_UID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
	private static final String ISSUANCE_POLICY_ID_CARD = IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD;
	private static final String CREDENTIAL_SPECIFICATION_ID_CARD = IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD;



	@Test(timeout = TestConfiguration.TEST_TIMEOUT)
	public void simpleRevocationIdemixTest() throws Exception {
		URI cl_technology = Helper.getSignatureTechnologyURI("cl");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("ISSUER", cl_technology, true);
		entities.addEntity("USER", true);
		entities.addEntity("VERIFIER", true);

		runTestSimple(keyLength, entities);
	}

	@Test(timeout = TestConfiguration.TEST_TIMEOUT)
	public void simpleRevocationUproveTest() throws Exception { 
		URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("ISSUER", uprove_technology, true);
		entities.addEntity("USER", true);
		entities.addEntity("VERIFIER", true);

		runTestSimple(keyLength, entities);
	}

	@Test(timeout = TestConfiguration.TEST_TIMEOUT)
	public void multipleRevokeRevocationIdemixTest() throws Exception {
		URI cl_technology = Helper.getSignatureTechnologyURI("cl");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("ISSUER", cl_technology, true);
		entities.addEntity("USER", true);
		entities.addEntity("VERIFIER", true);

		runTestMulti(keyLength, entities);
	}

	@Test(timeout = TestConfiguration.TEST_TIMEOUT)
	public void multipleRevokeRevocationUProveTest() throws Exception {
		URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
		int keyLength = 1024;

		Entities entities = new Entities();

		entities.addEntity("ISSUER", uprove_technology, true);
		entities.addEntity("USER", true);
		entities.addEntity("VERIFIER", true);

		runTestMulti(keyLength, entities);
	}

	private void runTestSimple(int keyLength, Entities entities) throws Exception {
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
		URI issuerParametersGovernmentUID =
				getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
		parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
				credentialTechnology, issuerParametersGovernmentUID, 4, REVOCATION_PARAMETERS_UID));

		// Store all issuer and inspector parameters to all key managers
		entities.storePublicParametersToKeyManagers(parametersList);

		// Store all credential specifications to all key managers
		storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD);

		addRevocationKeyManagers(entities, revocationAuthorityParameters);

		IssuanceHelper issuanceHelper = new IssuanceHelper();

		// Step 1. Get identity card.
		CredentialDescription cd =
				IntegrationTestUtil.issueAndStoreIdCard(entities.getInjector("ISSUER"), entities.getInjector("USER"), issuanceHelper,
						"1995-05-05Z");

		// Step 2. log in to some service using the identity card.
		PresentationToken pt =
				IntegrationTestUtil.loginWithIdCard(entities.getInjector("USER"), entities.getInjector("VERIFIER"), issuanceHelper,
						IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
		assertNotNull(pt);

		UserAbcEngine userEngine = entities.getInjector("USER").getInstance(UserAbcEngine.class);
		URI credUid = cd.getCredentialUID();
		assertTrue(!userEngine.isRevoked(USERNAME, credUid));

		Attribute revocationHandleAttribute = RevocationTest.this.getRevocationHandle(cd);

		// Step 3. Revoke credential.
		RevocationTest.this.revokeCredential(revocationInjector, issuanceHelper, REVOCATION_PARAMETERS_UID,
				revocationHandleAttribute);

		assertTrue(userEngine.isRevoked(USERNAME, credUid));

		// The verifier needs to retrive the latest revocation information
		// in order to put in the UID in the presentation policy.
		RevocationInformation revocationInformation =
				revocationEngine.updateRevocationInformation(REVOCATION_PARAMETERS_UID);


		// Step 4. Verify revoked credential is revoked.
		RevocationTest.this.revokedCredentialsShouldNotBeAllowed(entities.getInjector("USER"),
				entities.getInjector("VERIFIER"), issuanceHelper, revocationInformation, 0);
	}

	private void runTestMulti(int keyLength, Entities entities) throws Exception {
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
		URI issuerParametersGovernmentUID =
				getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_ID_CARD);
		parametersList.add(setupIssuer(entities.getInjector("ISSUER"), systemParameters,
				credentialTechnology, issuerParametersGovernmentUID, 4, REVOCATION_PARAMETERS_UID));

		// Store all issuer and inspector parameters to all key managers
		entities.storePublicParametersToKeyManagers(parametersList);

		// Store all credential specifications to all key managers
		storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_ID_CARD);

		addRevocationKeyManagers(entities, revocationAuthorityParameters);

		IssuanceHelper issuanceHelper = new IssuanceHelper();

		// Step 1. Get identity card.
		CredentialDescription cd1 =
				IntegrationTestUtil.issueAndStoreIdCard(entities.getInjector("ISSUER"), entities.getInjector("USER"), issuanceHelper,
						"1995-05-05Z");

		CredentialDescription cd2 =
				IntegrationTestUtil.issueAndStoreIdCard(entities.getInjector("ISSUER"), entities.getInjector("USER"), issuanceHelper,
						"1995-05-05Z");

		CredentialDescription cd3 =
				IntegrationTestUtil.issueAndStoreIdCard(entities.getInjector("ISSUER"), entities.getInjector("USER"), issuanceHelper,
						"1995-05-05Z");

		// Step 2. log in to some service using the identity card.
		PresentationToken pt =
				IntegrationTestUtil.loginWithIdCard(entities.getInjector("USER"), entities.getInjector("VERIFIER"), issuanceHelper,
						IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
		assertNotNull(pt);

		// Step 3. Revoke credentials.
		// Step 3a. Check that the credentials are not revoked.
		UserAbcEngine userEngine = entities.getInjector("USER").getInstance(UserAbcEngine.class);
		URI credUid1 = cd1.getCredentialUID();
		assertTrue(!userEngine.isRevoked(USERNAME, credUid1));

		URI credUid2 = cd2.getCredentialUID();
		assertTrue(!userEngine.isRevoked(USERNAME, credUid2));

		URI credUid3 = cd3.getCredentialUID();
		assertTrue(!userEngine.isRevoked(USERNAME, credUid3));

		// 3b. Revoke credentials
		Attribute revocationHandleAttribute = RevocationTest.this.getRevocationHandle(cd1);
		RevocationTest.this.revoke(revocationInjector, issuanceHelper, REVOCATION_PARAMETERS_UID,
				revocationHandleAttribute);

		assertTrue(userEngine.isRevoked(USERNAME, credUid1));

		revocationHandleAttribute = RevocationTest.this.getRevocationHandle(cd2);
		RevocationTest.this.revoke(revocationInjector, issuanceHelper, REVOCATION_PARAMETERS_UID,
				revocationHandleAttribute);

		assertTrue(userEngine.isRevoked(USERNAME, credUid2));

		assertTrue(!userEngine.isRevoked(USERNAME, credUid3));

		// The verifier needs to retrive the latest revocation information
		// in order to put in the UID in the presentation policy.
		RevocationInformation revocationInformation =
				revocationEngine.updateRevocationInformation(REVOCATION_PARAMETERS_UID);

		// User and verifier must manually obtain the latest revocation information
		// and update credentials accordingly.
		entities.getInjector("USER").getInstance(KeyManager.class).getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
		entities.getInjector("USER").getInstance(CredentialManager.class).updateNonRevocationEvidence(USERNAME);
		entities.getInjector("VERIFIER").getInstance(KeyManager.class).getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);



		// Step 4. Log in using the identity card.
		RevocationTest.this.revokedCredentialsShouldNotBeAllowed(entities.getInjector("USER"),
				entities.getInjector("VERIFIER"),
				issuanceHelper, revocationInformation, cd1.getCredentialUID());

		RevocationTest.this.revokedCredentialsShouldNotBeAllowed(entities.getInjector("USER"),
				entities.getInjector("VERIFIER"),
				issuanceHelper, revocationInformation, cd2.getCredentialUID());

		RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(entities.getInjector("USER"),
				entities.getInjector("VERIFIER"),
				issuanceHelper, revocationInformation, cd3.getCredentialUID());

		RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(entities.getInjector("USER"),
				entities.getInjector("VERIFIER"),
				issuanceHelper, revocationInformation, cd3.getCredentialUID());
	}

	private void revoke(Injector revocationInjector, IssuanceHelper issuanceHelper, URI revParamsUid,
			Attribute revocationHandleAttribute) throws CryptoEngineException {
		RevocationTest.this.revokeCredential(revocationInjector, issuanceHelper, revParamsUid,
				revocationHandleAttribute);
	}

	private void nonRevokedCredentialsShouldBeAllowed(Injector userInjector,
			Injector verifierInjector, IssuanceHelper issuanceHelper,
			RevocationInformation revocationInformation, URI chosenCredential) throws Exception {
		try {
			IntegrationTestUtil.loginToAccount(userInjector, verifierInjector, issuanceHelper,
					IntegrationTestUtil.REVOCATION_PARAMETERS_UID, chosenCredential,
					revocationInformation);
			assertTrue("We expect the verification to validate because the credential is not revoked",
					true);
		} catch (TokenVerificationException ex) {
			ex.printStackTrace();
			fail("We should be able to log in with a non-revoked credential");
		}

	}

	private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
			Injector verifierInjector, IssuanceHelper issuanceHelper,
			RevocationInformation revocationInformation, URI chosenPresentationToken) throws Exception {
		try {
			IntegrationTestUtil.loginToAccount(userInjector, verifierInjector, issuanceHelper,
					IntegrationTestUtil.REVOCATION_PARAMETERS_UID, chosenPresentationToken,
					revocationInformation);
			fail("We should not be allowed to log in with a revoked credential");
		} catch (TokenVerificationException ex) {
			// StringWriter sw = new StringWriter();
			// PrintWriter pw = new PrintWriter(sw);
			// ex.printStackTrace(pw);
			// assertTrue(sw.toString().contains("Incorrect T-value at position"));
			assertTrue("We expect the verification to fail due to a revoked credential", ex.getMessage()
					.startsWith("The crypto evidence in the presentation token is not valid"));
		} catch (RuntimeException ex) {
			assertTrue("We expect failure to generate a presentation Token",
					ex.getMessage().startsWith("Cannot generate presentationToken")
					|| ex.getMessage().startsWith("Cannot choose credential, URI does not exist!"));
		}
	}

	private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
			Injector verifierInjector, IssuanceHelper issuanceHelper,
			RevocationInformation revocationInformation, int chosenPresentationToken) throws Exception {
		try {
			IntegrationTestUtil.loginToAccount(userInjector, verifierInjector, issuanceHelper,
					IntegrationTestUtil.REVOCATION_PARAMETERS_UID, revocationInformation);//,
			//chosenPresentationToken);
			fail("We should not be allowed to log in with a revoked credential");
		} catch (TokenVerificationException ex) {
			// StringWriter sw = new StringWriter();
			// PrintWriter pw = new PrintWriter(sw);
			// ex.printStackTrace(pw);
			// assertTrue(sw.toString().contains("Incorrect T-value at position"));
			assertTrue("We expect the verification to fail due to a revoked credential", ex.getMessage()
					.startsWith("The crypto evidence in the presentation token is not valid"));
		} catch (RuntimeException ex) {
			assertTrue("We expect failure to generate a presentation Token",
					ex.getMessage().startsWith("Cannot generate presentationToken"));
		}

	}


	private Attribute getRevocationHandle(CredentialDescription cd) {
		for (Attribute attribute : cd.getAttribute()) {
			if (attribute.getAttributeDescription().getType()
					.compareTo(RevocationConstants.REVOCATION_HANDLE) == 0) {
				return attribute;
			}
		}
		return null;
	}

	private void revokeCredential(Injector revocationInjector, IssuanceHelper issuanceHelper,
			URI revParamsUid, Attribute revocationHandleAttribute) throws CryptoEngineException {
		issuanceHelper.revokeCredential(revocationInjector, revParamsUid, revocationHandleAttribute);
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

	private void addRevocationKeyManagers(Entities entities, RevocationAuthorityParameters revAuthParams) 
			throws KeyManagerException{
		for(Injector injector: entities.getInjectors()){
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeRevocationAuthorityParameters(revAuthParams.getParametersUID(), revAuthParams);
		}
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

}

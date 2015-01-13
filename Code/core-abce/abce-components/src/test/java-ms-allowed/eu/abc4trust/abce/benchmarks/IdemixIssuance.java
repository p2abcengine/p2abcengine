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

package eu.abc4trust.abce.benchmarks;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.benchmarks.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class IdemixIssuance {

	private static final String URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX =
			"urn:abc4trust:1.0:algorithm:idemix";
	private static final String USERNAME = "defaultUser";

	private static final String CREDSPEC =  "/eu/abc4trust/benchmarks/credspecs/credSchool.xml";

	private static final String ISSUANCE_POLICY = "/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool.xml";
	private static final int keyLength = 1024;

	private static final String PRESENTATION_POLICY_SODERHAMN_SCHOOL = "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml";

	private static final Logger logger = Logger
			.getLogger(IdemixIssuance.class.getCanonicalName());

	private static URI cryptoMechanism = null;

	private IssuancePolicy credSchoolIssuancePolicy = null;

	private KeyManager userKeyManager, issuerKeyManager, revAuthKeyManager;
	private CredentialSpecification credSpec;
	private IssuerParameters issParams = null;
	private Injector issuer, user, revAuthority;
	private SystemParameters sysparams;

	PseudonymWithMetadata pwm ; 
	private CryptoEngine cryptoEngine;
	private SecretWrapper secretWrapper;

	@Test
	public void issuanceWithIdemix() throws Exception {

		// Disable non-device-bound secrets
		PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;

		// The Guice injector is configured to return the same instance for
		// every invocation with the same class type. So the storage of
		// credentials is all done as side-effects.

		cryptoEngine = CryptoEngine.IDEMIX;
		issuer = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						cryptoEngine));

		user = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1987),
						cryptoEngine));

		revAuthority = Guice.createInjector(
				IntegrationModuleFactory.newModule(new Random(1231), cryptoEngine) );

		userKeyManager = user.getInstance(KeyManager.class);
		issuerKeyManager = issuer.getInstance(KeyManager.class);
		cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);

		this.runTestWithEngines();
	}



	private SecretWrapper getSecretWrapper(
			SystemParameters systemParameters, Random random)
					throws JAXBException, UnsupportedEncodingException, SAXException,
					ConfigurationException {

		// Secret needs to be newly generated
		SecretWrapper secretWrapper = getSecretWrapper(sysparams, new Random(1231));

		return secretWrapper;
	}

	private PseudonymWithMetadata getIdemixPseudonym(URI secretUid,
			CryptoEngineUser idemixUser, SystemParameters systemParameters) {
		String scope = "urn:soderhamn:registration";
		PseudonymWithMetadata pwm;
		try {
          pwm = idemixUser.createPseudonym(USERNAME, URI.create("soderhamndemo-idemix-uri"), scope, true, secretUid);
		} catch (CryptoEngineException e) {
			throw new RuntimeException(e);
		}
		return pwm;
	}

	private void runTestWithEngines()
			throws URISyntaxException, KeyManagerException, JAXBException,
			UnsupportedEncodingException, SAXException, Exception {

		CredentialManager credManager = user.getInstance(CredentialManager.class);

		IssuerAbcEngine issuerEngine = issuer.getInstance(IssuerAbcEngine.class);

		// Generate system parameters.
		sysparams = issuerEngine.setupSystemParameters(keyLength);
		System.out.println("System parameters just set up...");
		secretWrapper = this.getSecretWrapper(sysparams, new Random(1231));
		System.out.println("just retrieved the secret wrapper...");

		CryptoEngineUser userEngine =
				user.getInstance(CryptoEngineUser.class);

		userKeyManager.storeSystemParameters(sysparams);
		System.out.println("stored system parameters at the User...");
		issuerKeyManager.storeSystemParameters(sysparams);
		System.out.println("stored system parameters at the Issuer...");

		if (revAuthKeyManager != null) {
			revAuthKeyManager.storeSystemParameters(sysparams);
			System.out.println("stored system parameters at the Rev Authority...");
		}

		System.out.println("asking for the idemix pseudonym (getIdemixPseudonym)...");

		System.out.println("Just received the Idemix Pseudonym...!");

		// Generate revocation parameters.
		RevocationAbcEngine revocationEngine = revAuthority
				.getInstance(RevocationAbcEngine.class);
		Reference revocationInfoReference = new Reference();
		revocationInfoReference.setReferenceType(URI.create("https"));
		revocationInfoReference.getReferences().add(URI.create("https://example.org"));
		Reference nonRevocationEvidenceReference = new Reference();
		nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
		nonRevocationEvidenceReference.getReferences().add(URI.create("https://example.org"));
		Reference nonRrevocationUpdateReference = new Reference();
		nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
		nonRrevocationUpdateReference.getReferences().add(URI.create("https://example.org"));

		URI revParamsUid = new URI("revocationUID3");

		RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
				.setupRevocationAuthorityParameters(keyLength,
						cryptoMechanism, revParamsUid, revocationInfoReference,
						nonRevocationEvidenceReference, nonRrevocationUpdateReference);

		issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid,
				revocationAuthorityParameters);
		userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
				revocationAuthorityParameters);

		revAuthKeyManager = null;
		if (revAuthKeyManager != null) {
			revAuthKeyManager = revAuthority.getInstance(KeyManager.class);
		}

		// Setup issuance policies.
		System.out.println("Now loading the issuance policy to the Issuer...");

		credSchoolIssuancePolicy = (IssuancePolicy) this
				.loadResources(ISSUANCE_POLICY);

		System.out.println("Issuance policy loaded to the Issuer");

		// Load credential specifications.	    	   
		System.out.println("Now loading the credspec to the Issuer...");

		credSpec = (CredentialSpecification) this
				.loadResources(CREDSPEC);

		System.out.println("CredSpec Loaded to the Issuer.");

		issuerKeyManager.storeCredentialSpecification(
				credSpec.getSpecificationUID(),
				credSpec);

		// Generate issuer parameters.
		URI revocationId = new URI("revocationUID1");
		URI credUnivIssuancePolicyUid = credSchoolIssuancePolicy
				.getCredentialTemplate().getIssuerParametersUID();

		issParams = issuerEngine.setupIssuerParameters(
				sysparams, 10,  cryptoMechanism, credUnivIssuancePolicyUid, revocationId, null);

		issuerKeyManager.storeIssuerParameters(credUnivIssuancePolicyUid, issParams);

		// Store credential specifications.
		userKeyManager.storeCredentialSpecification(
				credSpec.getSpecificationUID(), credSpec);

		URI issuancePolicyUid = credSchoolIssuancePolicy
				.getCredentialTemplate().getIssuerParametersUID();
		userKeyManager.storeIssuerParameters(issuancePolicyUid,  issParams);

		if (!secretWrapper.isSecretOnSmartcard()) {
	            credManager.storeSecret(USERNAME, secretWrapper.getSecret());
		} else {

			CardStorage cardStorage = user.getInstance(CardStorage.class);
			int pin = 1234;
			cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(), pin);
		}
		secretWrapper.addIssuerParameters(issParams, sysparams);

		// Create a pseudonym
		PseudonymWithMetadata pwm = this.createPseudonym(
				secretWrapper.getSecretUID(),
				user.getInstance(CryptoEngineUser.class), sysparams);

	      credManager.storePseudonym(USERNAME, pwm);
		System.out.println("Pseudonym stored.");

		TokenStorageIssuer issuerTokenStore =
				issuer.getInstance(TokenStorageIssuer.class);
		String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());

		issuerTokenStore.addPseudonymPrimaryKey(primaryKey);

		pwm =  this.getIdemixPseudonym(secretWrapper.getSecretUID(), userEngine, sysparams);

		// Step 1. Get credential.
		logger.info("Now calling *this.issueAndStoreCredSchool(issuerinjector, userinjector)*...");
		this.issueAndStoreCredSchool (issuer, user);
		logger.info("method issueAndStoreCredential finished...");

		//issue one more credential
		this.issueAndStoreCredSchool (issuer, user);
		//testing whether the credential was issued. Fetching the list of available credentials from the credstore();

		System.out.println("Issuance completed, I am back from issueAndStore method. Let's now list the issued credentials...");
	      List<URI> credsInStore = credManager.listCredentials(USERNAME);
		for (URI currentCred : credsInStore){
	    	  System.out.println("Current credential: " + credManager.getCredential(USERNAME, currentCred)); 
		}
	}

	private Object loadResources(String issuancePolicyCredUniv)
			throws JAXBException, UnsupportedEncodingException, SAXException {
		return XmlUtils
				.getObjectFromXML(
						this.getClass().getResourceAsStream(
								issuancePolicyCredUniv), true);
	}

	private void issueAndStoreCredSchool(
			Injector schoolInjector,
			Injector userInjector)
					throws Exception {
		IssuanceHelper issuanceHelper = new IssuanceHelper();

		// This is a hack since the TokenManagerIssuer does not allow us to add
		// a pseudonym.


		// Step 1. Login with pseudonym.
		System.out.println(">> Login with pseudonym.");
		this.loginWithPseudonym(issuer, user, issuanceHelper);

		Map<String, Object> credAtts = this.populateCredWithAttributes();

		System.out.println("now calling *issuanceHelper.issueCredential(...)*...");
		logger.info("Now calling issueCredential(...)");

	      issuanceHelper.issueCredential(schoolInjector, userInjector,
				CREDSPEC, ISSUANCE_POLICY, credAtts, "");
	}

	private PseudonymWithMetadata createPseudonym(URI secretUid,
			CryptoEngineUser cryptoEngineUser,
			SystemParameters systemParameters) {
		String scope = "urn:soderhamn:registration";
		PseudonymWithMetadata pwm;
		try {
	          pwm = cryptoEngineUser.createPseudonym(USERNAME, 
					URI.create("soderhamndemo-idemix-uri"), scope, true, secretUid);
		} catch (CryptoEngineException e) {
			throw new RuntimeException(e);
		}
		return pwm;
	}


	private void loginWithPseudonym(Injector universityInjector, Injector userInjector,
			IssuanceHelper issuanceHelper) throws Exception {
		PresentationToken t =
				this.loginWithPseudonym(issuanceHelper, universityInjector, userInjector,
	                        PRESENTATION_POLICY_SODERHAMN_SCHOOL);
		assertNotNull(t);
	}


	private PresentationToken loginWithPseudonym(IssuanceHelper issuanceHelper,
	            Injector universityInjector, Injector userInjector, String policyResource) throws Exception {
		Pair<PresentationToken, PresentationPolicyAlternatives> p =
	                issuanceHelper.createPresentationToken(universityInjector, userInjector, policyResource);
		System.out.println("test, logingwithPseudonym, now we verify");
		return issuanceHelper.verify(universityInjector, p.second, p.first);
	}

	private Map<String, Object> populateCredWithAttributes() {
		Map<String, Object> att = new HashMap<String, Object>();
		att.put("urn:soderhamn:credspec:credSchool:firstname", "Joe");
		att.put("urn:soderhamn:credspec:credSchool:lastname", "Cocker");
		att.put("urn:soderhamn:credspec:credSchool:civicRegistrationNumber", "123456789");
		att.put("urn:soderhamn:credspec:credSchool:gender", "Female");
		Calendar cal = Calendar.getInstance();
		cal.set(2000, 1, 10);
		SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
		String dateValue = xmlDateFormat.format(cal.getTime());
		att.put("urn:soderhamn:credspec:credSchool:birthdate", dateValue);
		att.put("urn:soderhamn:credspec:credSchool:schoolname", "Soederhamn Skole");
		return att;
	}
}
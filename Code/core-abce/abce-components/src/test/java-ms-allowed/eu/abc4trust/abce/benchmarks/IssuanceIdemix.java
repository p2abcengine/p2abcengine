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
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
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
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Soderhamn scenario. Based on the test case for the Patras scenario with the new crypto architecture.
 */
public class IssuanceIdemix {

	private static final String USERNAME = "defaultUser";
	private static final String CREDSPEC =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool.xml";
	private static final String CREDSPEC12ATTS =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool12Atts.xml";
	private static final String CREDSPEC_CARRY_OVER = 
			"/eu/abc4trust/benchmarks/credspecs/credCarryOver.xml";
	
	private static final String ISSUANCE_POLICY_PSEUDONYM_PROOF = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool.xml";
	private static final String ISSUANCE_POLICY_FROM_SCRATCH = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool_empty.xml";
	private static final String ISSUANCE_POLICY12Atts = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool12Atts.xml";
	private static final String ISSUANCE_POLICY_CARRY_OVER = 
			"/eu/abc4trust/benchmarks/policies/issuance/issPolCarryOver.xml";

	private static final String PRESENTATION_POLICY_SODERHAMN_SCHOOL = 
			"/eu/abc4trust/benchmarks/policies/issuance/presentationPolicySoderhamnSchool.xml";

	private static final int keyLength = 1024;

	private static final Logger logger = Logger
			.getLogger(IdemixIssuance.class.getCanonicalName());

	
	@Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void issuanceHappyPathTest() throws Exception {
		runTest();
	}      

	private void runTest() throws Exception {
		Random rand = new Random(1235);
		Injector issuerInjector = getIssuerInjector(rand);
		Injector userInjector = getUserInjector(rand);
		Injector inspectorInjector = getInspectorInjector(rand);

		IssuerAbcEngine issuerABCEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);

		// Generate system parameters and load them into the key managers of all
		// parties
		SystemParameters systemParameters = issuerABCEngine
				.setupSystemParameters(keyLength);

		this.loadSystemParameters(systemParameters, issuerInjector,
				userInjector, inspectorInjector);

		// Generate a secret and load it to the appropriate places
		SecretWrapper secretWrapper = getSecretWrapper(systemParameters, rand);

		CredentialManager userCredentialManager = userInjector
				.getInstance(CredentialManager.class);
		if (!secretWrapper.isSecretOnSmartcard()) {
            userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
		} else {
			CardStorage cardStorage = userInjector
					.getInstance(CardStorage.class);

			int pin = 1234;
			cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(), pin);
		}

		// Create a pseudonym
		PseudonymWithMetadata pwm = this.createPseudonym(
				secretWrapper.getSecretUID(),
				userInjector.getInstance(CryptoEngineUser.class),
				systemParameters);

		this.setupEngines(issuerInjector, userInjector, inspectorInjector,
				pwm, secretWrapper);
		IssuanceHelper issHelper = new IssuanceHelper();

		this.runTests(userInjector, issuerInjector, inspectorInjector, issHelper);
	}

	private Injector getIssuerInjector(Random rand){    	
		return Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
	}    

	private Injector getUserInjector(Random rand){
		return Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
	}

	private Injector getInspectorInjector(Random rand){
		return Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
	}   

	private void loadSystemParameters(SystemParameters systemParameters,
			Injector... injectors) throws KeyManagerException {

		for (Injector injector : injectors) {
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeSystemParameters(systemParameters);
		}
	}

	private void setupEngines(Injector issuerInjector, Injector userInjector, 
			Injector inspectorInjector,	PseudonymWithMetadata pwm, SecretWrapper secretWrapper)
					throws KeyManagerException, JAXBException, UnsupportedEncodingException, 
					SAXException, URISyntaxException, Exception, CredentialManagerException {
		
		URI cl_technology = URI.create("cl");

		KeyManager schoolIssuerKeyManager = issuerInjector.getInstance(KeyManager.class);
		KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

		// Setup issuance policies
		IssuancePolicy issuancePolicySchoolCred = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY_PSEUDONYM_PROOF),true);

		IssuancePolicy issuanceFromScratchPolicy = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY_FROM_SCRATCH), true);

		IssuancePolicy issuancePolicySchoolCred12Atts = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY12Atts),	true);

		IssuancePolicy issuancePolicyCarryOver = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY_CARRY_OVER), true);

		// Load credential specifications.
		CredentialSpecification schoolCredSpec = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(CREDSPEC), true);

		CredentialSpecification schoolCredSpec12Atts = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(CREDSPEC12ATTS), true);

		CredentialSpecification credCarryOverSpec = (CredentialSpecification) XmlUtils.
				getObjectFromXML(this.getClass().getResourceAsStream(CREDSPEC_CARRY_OVER), true);

		// Store credential specifications.
		URI schoolCredSpecificationUID = schoolCredSpec.getSpecificationUID();
		URI schoolCredSpecificationUID12Atts = schoolCredSpec12Atts.getSpecificationUID();
		URI credCarryOverSpecUID = credCarryOverSpec.getSpecificationUID();

		schoolIssuerKeyManager.storeCredentialSpecification(schoolCredSpecificationUID, schoolCredSpec);
		schoolIssuerKeyManager.storeCredentialSpecification(credCarryOverSpecUID, credCarryOverSpec);
		schoolIssuerKeyManager.storeCredentialSpecification(schoolCredSpecificationUID12Atts, schoolCredSpec12Atts);

		userKeyManager.storeCredentialSpecification(schoolCredSpecificationUID, schoolCredSpec);
		userKeyManager.storeCredentialSpecification(credCarryOverSpecUID, credCarryOverSpec);  
		userKeyManager.storeCredentialSpecification(schoolCredSpecificationUID12Atts, schoolCredSpec12Atts);

		// Generate issuer parameters.
		SystemParametersWrapper schoolSysParamsWrapper = new SystemParametersWrapper(
				schoolIssuerKeyManager.getSystemParameters());
		URI revocationId = new URI("revocationUID1");

		int maximalNumberOfAttributes = 15;

		IssuerAbcEngine issuerABCEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

		//get issuer param Uids for each credential
		URI credShoolFromScratchIssParamUid = issuanceFromScratchPolicy
				.getCredentialTemplate().getCredentialSpecUID();
		URI credSchoolIssuancePolicyUid = issuancePolicySchoolCred
				.getCredentialTemplate().getIssuerParametersUID();
		URI credSchoolWith2AttsIssuancePolicyUid = issuancePolicySchoolCred12Atts
				.getCredentialTemplate().getIssuerParametersUID();
		URI credCarryOverIssParamsUid = issuancePolicyCarryOver
				.getCredentialTemplate().getIssuerParametersUID();

		//setup Issuer parameters for each credential
		IssuerParameters credSchoolFromScratchIssParams = issuerABCEngine.setupIssuerParameters(
				schoolSysParamsWrapper.getSystemParameters(), maximalNumberOfAttributes,
				cl_technology, credShoolFromScratchIssParamUid, revocationId, null);

		revocationId = new URI("revocationUID2");
		IssuerParameters credSchoolIssuerParameters = issuerABCEngine.setupIssuerParameters(
				schoolSysParamsWrapper.getSystemParameters(), maximalNumberOfAttributes,
				cl_technology, credSchoolIssuancePolicyUid, revocationId, null);

		revocationId = new URI("revocationUID3");
		IssuerParameters schoolIssuerParameters12Atts = issuerABCEngine.setupIssuerParameters(
				schoolSysParamsWrapper.getSystemParameters(), maximalNumberOfAttributes,
				cl_technology, credSchoolWith2AttsIssuancePolicyUid, revocationId, null);

		revocationId = new URI("revocationUID4");
		IssuerParameters credCarryOverIssParams = issuerABCEngine.setupIssuerParameters(
				schoolSysParamsWrapper.getSystemParameters(), maximalNumberOfAttributes,
				cl_technology, credCarryOverIssParamsUid, revocationId, null);

		schoolIssuerKeyManager.storeIssuerParameters(
				credShoolFromScratchIssParamUid, credSchoolIssuerParameters);
		schoolIssuerKeyManager.storeIssuerParameters(
				credSchoolIssuancePolicyUid, credSchoolIssuerParameters);
		schoolIssuerKeyManager.storeIssuerParameters(
				credSchoolWith2AttsIssuancePolicyUid, schoolIssuerParameters12Atts);
		schoolIssuerKeyManager.storeIssuerParameters(
				credCarryOverIssParamsUid, credCarryOverIssParams);

		userKeyManager.storeIssuerParameters(
				credShoolFromScratchIssParamUid, credSchoolIssuerParameters);
		userKeyManager.storeIssuerParameters(
				credSchoolIssuancePolicyUid, credSchoolIssuerParameters);
		userKeyManager.storeIssuerParameters(
				credCarryOverIssParamsUid, credCarryOverIssParams);
		userKeyManager.storeIssuerParameters(
				credSchoolWith2AttsIssuancePolicyUid, schoolIssuerParameters12Atts);

		// Load secret and store it.
		CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);

		if (secretWrapper.isSecretOnSmartcard()) {
			// add smartcard to manager
			CardStorage cardStorage = userInjector.getInstance(CardStorage.class);
			cardStorage.addSmartcard(
					secretWrapper.getSoftwareSmartcard(), secretWrapper.getPin());

			// sign issuer attributes and add to smartcard
			secretWrapper.addIssuerParameters(
					credSchoolFromScratchIssParams, schoolSysParamsWrapper.getSystemParameters());
			secretWrapper.addIssuerParameters(
					credSchoolIssuerParameters, schoolSysParamsWrapper.getSystemParameters());
			secretWrapper.addIssuerParameters(
					schoolIssuerParameters12Atts, schoolSysParamsWrapper.getSystemParameters());
			secretWrapper.addIssuerParameters(
					credCarryOverIssParams, schoolSysParamsWrapper.getSystemParameters());
		} else {
            userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
		}

		// Step 0. Create a pseudonym and store it in the user credential
		// manager.
        userCredentialManager.storePseudonym(USERNAME, pwm);

		// This is a hack since the TokenManagerIssuer does not allow us to add
		// a pseudonym.
		TokenStorageIssuer schoolTokenStorageManager = 
				issuerInjector.getInstance(TokenStorageIssuer.class);
		String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
		schoolTokenStorageManager.addPseudonymPrimaryKey(primaryKey);
	}

	private void runTests(Injector userInjector, Injector issuerInjector, 
			Injector courseEvaluationInjector, IssuanceHelper issuanceHelper) 
					throws Exception{

		logger.info("Start issuance from scratch...");
		issueAndStoreCourseCredential_fromScratch(
				issuerInjector,	userInjector, issuanceHelper);
		logger.info("\nIssuance from scratch OVER\n");

		logger.info(">> Login with pseudonym.");
		this.loginWithPseudonym(
				issuerInjector, userInjector, issuanceHelper);
		// Works until here
		// Step 1. Get university credential.

		logger.info("\nStart issuance proving pseudonym...\n");
		this.issueAndStoreSchoolCredential_ProvingPseudonym(
				issuerInjector, userInjector, issuanceHelper);
		logger.info("\nENDED advanced issuance proving pseudonym\n");


		logger.info("\nStarting issuance proving pseudonym with 12 attributes...\n");
		TimingsLogger.logTiming("issueAndStoreSchoolCredential12Atts", true);
		this.issueAndStoreSchoolCredential_12Atts_PseudonymProof(
				issuerInjector, userInjector, issuanceHelper);
		TimingsLogger.logTiming("issueAndStoreSchoolCredential12Atts", false);
		logger.info("\nCOMPLETED issuance proving pseudonym with 12 attributes...\n");

		logger.info("\nStarting carry over issuance ...\n");
		TimingsLogger.logTiming("issuanceCarryOver", true);
		this.issueCarryOverAttribute(issuerInjector, userInjector, issuanceHelper);
		TimingsLogger.logTiming("issuanceCarryOver", false);
		logger.info("\nCOMPLETED carry over issuance...\n");
	}

	private void issueCarryOverAttribute(Injector issuerInjector, Injector userInjector, 
			IssuanceHelper issuanceHelper) throws Exception{

		try{
			Map<String, Object> blankAtts = new HashMap<String, Object>();
			TimingsLogger.logTiming("issueAndStoreCarryOver", true);
			//issue the credential with carry over attributes
    	    issuanceHelper.issueCredential(issuerInjector, userInjector, CREDSPEC_CARRY_OVER, ISSUANCE_POLICY_CARRY_OVER, blankAtts, "");
		
			TimingsLogger.logTiming("issueAndStoreCarryOver", false);
		}
		catch (Exception e){
			logger.info("Exception caught: " + e.toString());
		}

	}

	private void loginWithPseudonym(Injector schoolInjector,
			Injector userInjector, IssuanceHelper issuanceHelper)
					throws Exception {

		TimingsLogger.logTiming("loginWithPseudonym", true);
		PresentationToken t = this.loginWithPseudonym(issuanceHelper,
				schoolInjector,
                userInjector);
		TimingsLogger.logTiming("loginWithPseudonym", false);
		assertNotNull(t);
	}

	private PresentationToken loginWithPseudonym(IssuanceHelper issuanceHelper,
            Injector schoolInjector, Injector userInjector) throws Exception {

		System.out.println("test, logingwithPseudonym, pre token generation");

		Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(schoolInjector, userInjector,
                        PRESENTATION_POLICY_SODERHAMN_SCHOOL);

		System.out.println("test, logingwithPseudonym, now we verify");
		return issuanceHelper.verify(schoolInjector, p.second, p.first);

	}

	private SecretWrapper getSecretWrapper(
			SystemParameters systemParameters, Random random)
					throws JAXBException, UnsupportedEncodingException, SAXException,
					ConfigurationException {

		EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(
				systemParameters);

		// Secret needs to be newly generated
		SecretWrapper secretWrapper = new SecretWrapper(random,
				spWrapper.getSystemParameters());

		return secretWrapper;
	}

	private void issueAndStoreSchoolCredential_ProvingPseudonym(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper)
					throws Exception {
		Map<String, Object> atts = this.populateSchoolCredentialAttributes();

		TimingsLogger.logTiming("issueCredential6Atts", true);
            issuanceHelper.issueCredential(issuerInjector, userInjector,
				CREDSPEC, ISSUANCE_POLICY_PSEUDONYM_PROOF, atts, "");
		TimingsLogger.logTiming("issueCredential6Atts", false);

		logger.info("Credential with 6 atts issued successfully.\n");
	}

	private void issueAndStoreSchoolCredential_12Atts_PseudonymProof(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper)
					throws Exception {

		Map<String, Object> atts = this.populateSchoolCredentialAttributes12();
		logger.info("Now issuing the credential with 12 atts");

		try{
        	issuanceHelper.issueCredential(issuerInjector, userInjector,
					CREDSPEC12ATTS, ISSUANCE_POLICY12Atts, atts, "");
		} catch (Error e) {
			logger.info(e.toString());
		}
		logger.info("Credential with 12 atts issued successfully.");
	}

	private void issueAndStoreCourseCredential_fromScratch(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper)
					throws Exception {
		Map<String, Object> atts = this.populateSchoolCredentialAttributes();
		TimingsLogger.logTiming("IssuanceFromScratch", true);
        issuanceHelper.issueCredential(issuerInjector, userInjector,
				CREDSPEC, ISSUANCE_POLICY_FROM_SCRATCH, atts, "");
		TimingsLogger.logTiming("IssuanceFromScratch", false);
	}

	private PseudonymWithMetadata createPseudonym(URI secretUid,
			CryptoEngineUser cryptoEngineUser,
			SystemParameters systemParameters) {
		String scope = "urn:soderhamn:registration";
		try {
          return cryptoEngineUser.createPseudonym(USERNAME, 
					URI.create("benchmarks-idemix-uri"), scope, true, secretUid);
		} catch (CryptoEngineException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> populateSchoolCredentialAttributes() {

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
	
	private Map<String, Object> populateSchoolCredentialAttributes12() {

		Map<String, Object> att = new HashMap<String, Object>();
		att.put("urn:soderhamn:credspec:credSchool12Atts:firstname", "Joe");
		att.put("urn:soderhamn:credspec:credSchool12Atts:lastname", "Cocker");
		att.put("urn:soderhamn:credspec:credSchool12Atts:civicRegistrationNumber", "123456789");
		att.put("urn:soderhamn:credspec:credSchool12Atts:gender", "Female");
		Calendar cal = Calendar.getInstance();
		cal.set(2000, 1, 10);
		SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
		String dateValue = xmlDateFormat.format(cal.getTime());
		att.put("urn:soderhamn:credspec:credSchool12Atts:birthdate", dateValue);
		att.put("urn:soderhamn:credspec:credSchool12Atts:spanish", "mexican");
		att.put("urn:soderhamn:credspec:credSchool12Atts:schoolname", "mexican");
		att.put("urn:soderhamn:credspec:credSchool12Atts:principal", "DrMario Fisteku");
		att.put("urn:soderhamn:credspec:credSchool12Atts:director", "Filan Fisteku");
		att.put("urn:soderhamn:credspec:credSchool12Atts:phonenumber", "044406463");
		att.put("urn:soderhamn:credspec:credSchool12Atts:address", "noaddress");
		att.put("urn:soderhamn:credspec:credSchool12Atts:city", "chicago");
		att.put("urn:soderhamn:credspec:credSchool12Atts:chocolate", "black");
		return att;
	}
}

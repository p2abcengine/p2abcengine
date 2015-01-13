//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
//import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
//import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IntegerParameter;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Benchmarking tests with the new crypto architecture.
 */
public class PresentationTests {

	private URI this_technology ;
	
	private static final String USERNAME = "username";

	private static final String CREDSPEC_SIMPLE = 
			"/eu/abc4trust/benchmarks/credspecs/credSchool_baseline.xml";
	private static final String CREDSPEC_NYM_PROOF =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool.xml";
	private static final String CREDSPEC_SCHOOL_REVOCABLE =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool_Revocable.xml";
	private static final String CREDSPEC12ATTS =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool12Atts.xml";
	private static final String CREDSPEC24ATTS =  
			"/eu/abc4trust/benchmarks/credspecs/credSchool24Atts.xml";
	private static final String CREDSPEC_CARRY_OVER = 
			"/eu/abc4trust/benchmarks/credspecs/credCarryOver.xml";

	private static final String ISSUANCE_POLICY_PSEUDONYM_PROOF = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool.xml";
	private static final String ISSUANCE_POLICY_FROM_SCRATCH = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool_empty.xml";
	private static final String ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool_revocable.xml";
	private static final String ISSUANCE_POLICY12Atts = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool12Atts.xml";
	private static final String ISSUANCE_POLICY24Atts = 
			"/eu/abc4trust/benchmarks/policies/issuance/isspol_credSchool24Atts.xml";
	private static final String ISSUANCE_POLICY_CARRY_OVER = 
			"/eu/abc4trust/benchmarks/policies/issuance/issPolCarryOver.xml";

	private static final String PRESENTATION_POLICY_SODERHAMN_SCHOOL = 
			"/eu/abc4trust/benchmarks/policies/issuance/presentationPolicySoderhamnSchool.xml";
	private static final String PRESENTATION_POLICY_BASELINE = 
			"/eu/abc4trust/benchmarks/policies/presentation/Baseline.xml";
	private static final String PRESENTATION_POLICY_12Atts = 
			"/eu/abc4trust/benchmarks/policies/presentation/12Atts.xml";
	private static final String PRESENTATION_POLICY_24Atts = 
			"/eu/abc4trust/benchmarks/policies/presentation/24Atts.xml";
	private static final String PRESENTATION_POLICY_1_CREDENTIAL = 
			"/eu/abc4trust/benchmarks/policies/presentation/1Credential.xml";
	private static final String PRESENTATION_POLICY_2CREDENTIALS = 
			"/eu/abc4trust/benchmarks/policies/presentation/2Credentials.xml";
	private static final String PRESENTATION_POLICY_3CREDENTIALS = 
			"/eu/abc4trust/benchmarks/policies/presentation/3Credentials.xml";
	private static final String PRESENTATION_POLICY_INSPECT_1_ATTRIBUTE = 
			"/eu/abc4trust/benchmarks/policies/presentation/Inspect_1_Att.xml";
	private static final String PRESENTATION_POLICY_INSPECT_2_ATTRIBUTES = 
			"/eu/abc4trust/benchmarks/policies/presentation/Inspect_2_Atts.xml";
	private static final String PRESENTATION_POLICY_INSPECT_3_ATTRIBUTES = 
			"/eu/abc4trust/benchmarks/policies/presentation/Inspect_3_Atts.xml";
	private static final String PRESENTATION_POLICY_REVOCATION = 
			"/eu/abc4trust/benchmarks/policies/presentation/Baseline+Revocation.xml";
	private static final String PRESENTATION_POLICY_CRED_NYM = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Pseudonym.xml";
	private static final String PRESENTATION_POLICY_CRED_NYM_KEYBINDING = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Pseudonym_KeyBinding.xml";
	private static final String PRESENTATION_POLICY_DISCLOSE1Att = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Disclose1Att.xml";
	private static final String PRESENTATION_POLICY_DISCLOSE2Atts = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Disclose2Atts.xml";
	private static final String PRESENTATION_POLICY_DISCLOSE3Atts = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Disclose3Atts.xml";
	private static final String PRESENTATION_POLICY_DISCLOSE4Atts = 
			"/eu/abc4trust/benchmarks/policies/presentation/CredSchool_Disclose4Atts.xml";
	private static final String PRESENTATION_POLICY_PREDICATE_CONSTANT = 
			"/eu/abc4trust/benchmarks/policies/presentation/Predicate_Equality_Constant.xml";
	private static final String PRESENTATION_POLICY_PREDICATE_ATTRIBUTE = 
			"/eu/abc4trust/benchmarks/policies/presentation/Predicate_Equality_Attribute.xml";

	static public final URI REVOCATION_PARAMETERS_UID = URI.create("revocation:authority:UID");

	private static final int firstKeyLength = 1024;
	private static final int NUMBER_OF_UPROVE_TOKENS = 70;

	private static final Logger logger = Logger
			.getLogger(PresentationTests.class.getCanonicalName());

	static CredentialDescription credRevocableDescription = null;

	BasicSmartcard s; //the smartcard

	private CredentialManager userCredentialManager;
	private CardStorage cardStorage;
	private SystemParameters systemParameters;
	private SecretWrapper secretWrapper;
	private URI cl_technology = URI.create("cl");
	private URI brands_technology = URI.create("brands");
	private int keyLength;

	@Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void issuanceWithCLSignature() throws Exception {
		logger.info("\nStarting issuance with CL signature scheme.\n");
		runTest(cl_technology, firstKeyLength);
	}   

	@Ignore @Test(timeout=TestConfiguration.TEST_TIMEOUT)
	public void issuanceWithBrandsSignature() throws Exception {
		logger.info("\nStarting issuance with Brands signature scheme.\n");
		runTest(brands_technology, firstKeyLength);
	}   

	private void runTest(URI technology, int seclevel) throws Exception, CredentialManagerException {

		this_technology = technology;
		int numBits = seclevel;
		this.keyLength = seclevel;
		Random rand = new Random(1235);

		RevocationProxyAuthority revocationProxyAuthority ;
		Injector revAuthInjector = this.getRevAuthInjector(rand);
		revocationProxyAuthority = revAuthInjector.getInstance(RevocationProxyAuthority.class);
		Injector issuerInjector = getIssuerInjector(rand, revocationProxyAuthority);
		Injector userInjector = getUserInjector(rand, revocationProxyAuthority);
		Injector verifierInjector = getVerifierInjector(rand, revocationProxyAuthority);
		Injector inspectorInjector = getInspectorInjector(rand, revocationProxyAuthority);

		IssuerAbcEngine issuerABCEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);

		// Generate system parameters and load them into the key managers all parties
		try {
			systemParameters = FileSystem.loadObjectFromResource("storage/system_params" 
					+ this.this_technology + this.keyLength);
			logger.info("Using system params from file storage/system_params...");
		} catch (IOException e) {
			logger.info("No systems params file - generating new parameters...");
			systemParameters = issuerABCEngine.setupSystemParameters(numBits);
			FileSystem.storeObjectInFile(systemParameters, "storage/system_params" 
					+ this.this_technology + this.keyLength);
		}
		this.loadSystemParameters(systemParameters, issuerInjector, revAuthInjector, 
				inspectorInjector, verifierInjector, userInjector);

		// Generate a secret and load it to the appropriate places
		secretWrapper = getSecretWrapper(systemParameters, rand);

		userCredentialManager = userInjector.getInstance(CredentialManager.class);

		if (!secretWrapper.isSecretOnSmartcard()) {
			userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
		} else {
			cardStorage = userInjector.getInstance(CardStorage.class);
			try {
				s = FileSystem.loadObjectFromResource("storage/smart_card " + 1024);
				logger.info("Loaded the smart card contents from storage/smart_card");
			} catch (IOException e) {
				logger.info("No smart card could be loaded - storing a new smart card...");
				s = secretWrapper.getSoftwareSmartcard();
				FileSystem.storeObjectInFile(s, "storage/smart_card");
			}
			int pin = 1234;
			cardStorage.addSmartcard(s, pin);
		}

		// Create a pseudonym
		PseudonymWithMetadata pwm = this.createPseudonym(secretWrapper.getSecretUID(),
				userInjector.getInstance(CryptoEngineUser.class), systemParameters);

		this.setupEngines(issuerInjector, revAuthInjector, verifierInjector, inspectorInjector, userInjector,
				pwm, secretWrapper);
		this.issueThoseCredentials2( userInjector, issuerInjector );
		this.listIssuedCredentials();
		this.testPolicies(issuerInjector, userInjector);
	}

	private void testPolicies(Injector issuerInjector, Injector userInjector) {
		String[] presPolicies = new String[]{ 
				PRESENTATION_POLICY_BASELINE, 
				PRESENTATION_POLICY_CRED_NYM,
				PRESENTATION_POLICY_CRED_NYM_KEYBINDING,
				PRESENTATION_POLICY_PREDICATE_CONSTANT,
				PRESENTATION_POLICY_PREDICATE_ATTRIBUTE,
//				PRESENTATION_POLICY_DISCLOSE1Att,
//				PRESENTATION_POLICY_DISCLOSE2Atts,
//				PRESENTATION_POLICY_DISCLOSE3Atts,
//				PRESENTATION_POLICY_DISCLOSE4Atts,
//				PRESENTATION_POLICY_1_CREDENTIAL,
//				PRESENTATION_POLICY_2CREDENTIALS ,
//				PRESENTATION_POLICY_3CREDENTIALS,
//				PRESENTATION_POLICY_12Atts,
//				PRESENTATION_POLICY_24Atts,
//				PRESENTATION_POLICY_INSPECT_1_ATTRIBUTE,
//				PRESENTATION_POLICY_INSPECT_2_ATTRIBUTES,
//				PRESENTATION_POLICY_INSPECT_3_ATTRIBUTES,
//				PRESENTATION_POLICY_REVOCATION
		};

		List<String> presentationPolicies = Arrays.asList(presPolicies); 

		try{
			File file = new File("Presentation token sizes " + this.this_technology  + " " + this.keyLength );
			if (!file.exists()) {
				file.createNewFile();
			}


			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter writer = new BufferedWriter(fw);
			writer.write("Policy\t Size (Bytes) \n");
			writer.close();
		} catch(Exception e) {
			logger.info(e.getMessage());
		}
		
		for (String policy : presentationPolicies) {
			try{
				int iterations = 1;
				for (int i = 0; i < iterations; i ++){
					this.testVerification(
							new IssuanceHelper(), issuerInjector, userInjector, policy);
				}
			} catch(Exception e) {
				logger.info(e.getMessage());
			}
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



	private void testRevocationCases(
			UserAbcEngine userEngine, RevocationAbcEngine revEngine, 
			IssuerAbcEngine issuerEngine, VerifierAbcEngine verifierEngine,
			IssuanceHelper issuanceHelper) throws Exception{

		//first see if you can login
		RevocationInformation revocationInformation = null;

		if (REVOCATION_PARAMETERS_UID != null) {
			try {
				System.out.println("Trying to login with the revocable credential for the second time...");

				revocationInformation = verifierEngine
						.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
			} catch (Exception ex) {
				revocationInformation = null;
				System.out.println("Failed to login with the revocable credential for the second time...");
				System.out.println(ex.getMessage());
			}
		}

		// Now try to revoke the credential and use the presentation token again.
		// First verify that we are not revoked.
		URI credUid = URI.create("urn:soderhamn:credspec:credSchool_revocable");

		Attribute revHandle = this.getRevocationHandle(credRevocableDescription);
		System.out.println("Revocation handle: " + revHandle);

		ObjectFactory of = new ObjectFactory();
		JAXBElement<Attribute> att = of.createAttribute(revHandle);

		System.out.println("revoking " + att + "  " + credUid + "...");
		TimingsLogger.logTiming("Revoke"+att, true);
		this.revokeCredential(revEngine, REVOCATION_PARAMETERS_UID, revHandle);
		TimingsLogger.logTiming("Revoke"+att, false);

		revHandle.setAttributeValue((BigInteger.valueOf(123243423)));
		att = of.createAttribute(revHandle);
		TimingsLogger.logTiming("Revoke"+att, true);
		this.revokeCredential(revEngine, REVOCATION_PARAMETERS_UID, revHandle);
		TimingsLogger.logTiming("Revoke"+att, false);

		revHandle.setAttributeValue((BigInteger.valueOf(25545555)));
		att = of.createAttribute(revHandle);
		TimingsLogger.logTiming("Revoke"+att, true);
		this.revokeCredential(revEngine, REVOCATION_PARAMETERS_UID, revHandle);
		TimingsLogger.logTiming("Revoke"+att, false);

		// The verifier needs to retrieve the latest revocation information
		// in order to put in the UID in the presentation policy.
		//	    RevocationAbcEngine revocationEngine =
		//	        revocationInjector.getInstance(RevocationAbcEngine.class);

		try {
			userEngine.updateNonRevocationEvidence(USERNAME);

			revocationInformation =
					revEngine.updateRevocationInformation(REVOCATION_PARAMETERS_UID);
		} catch (Exception e) {
			// ok - expected to fail
			System.out.println((e.getMessage().contains("Cannot generate presentationToken")));
		}
	}

	public void revokeCredential(RevocationAbcEngine revEngine, URI revParamsUid,
			Attribute revocationHandleAttribute) throws CryptoEngineException {

		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(revocationHandleAttribute);
		revEngine.revoke(revParamsUid, attributes);
	}

	private PresentationToken login(IssuanceHelper issuanceHelper, Injector verifierInjector,
			Injector userInjector,
			RevocationInformation revInfo) throws Exception {
		List<URI> chosenInspectors = new LinkedList<URI>();
		chosenInspectors.add(URI.create("urn:soderhamn:registration"));

		Pair<PresentationToken, PresentationPolicyAlternatives> p =
				issuanceHelper.createPresentationToken(userInjector, userInjector,
						PRESENTATION_POLICY_REVOCATION, revInfo);

		// Store all required cred specs in the verifier key manager.
		KeyManager issuerKeyManager = verifierInjector.getInstance(KeyManager.class);
		KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

		PresentationToken pt = p.first;
		assertNotNull(pt);
		for (CredentialInToken cit : pt.getPresentationTokenDescription().getCredential()) {
			issuerKeyManager.storeCredentialSpecification(cit.getCredentialSpecUID(),
					userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
		}
		return issuanceHelper.verify(verifierInjector, p.second, p.first);
	}

	private void listIssuedCredentials() throws Exception{
		List<URI> credsInStore = userCredentialManager.listCredentials(USERNAME);
		Credential currentCred = null;
		ObjectFactory  of = new ObjectFactory();
		String fileOutputName = "";
		try{
			fileOutputName = this.this_technology.toString().substring(
					this.this_technology.toString().lastIndexOf(':')) + "_"
					+ this.keyLength + ".txt";
		}
		catch(Exception i) {
			fileOutputName = this.this_technology.toString() + "_"
					+ this.keyLength + ".txt";
		}

		File file = new File("CredentialSizes_" + fileOutputName);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter writer = new BufferedWriter(fw);

		JAXBElement<Credential> currentCredXML = null;

		for (URI currentCredURI : credsInStore){

			currentCred = userCredentialManager.getCredential(USERNAME, currentCredURI);
			currentCredXML = of.createCredential(currentCred);
			System.out.println("Current credential: " + userCredentialManager
					.getCredential(USERNAME, currentCredURI).getCredentialDescription()
					.getCredentialSpecificationUID() + " size = " 
					+ this.sizeof(currentCred) + ", while currentCredXML size =  " 
					+ this.sizeof(currentCredXML) + "\n" + currentCredXML); 

			String fileName = currentCred.getCredentialDescription()
					.getCredentialSpecificationUID().toString() +  fileOutputName;
			fileName =   fileName. substring(
					fileName.lastIndexOf(':') + 1
					);
			System.out.println("FILENAME = " + fileName);
			FileSystem.storeObjectInFile(XmlUtils.toNormalizedXML(currentCredXML), fileName);
			System.out.println("Credential " + fileName + " has " 
					+ this.sizeof(XmlUtils.toNormalizedXML(currentCredXML) + " Bytes \n"));
			writer.write("Credential " + fileName + " has " 
					+ this.sizeof(XmlUtils.toNormalizedXML(currentCredXML) + " Bytes \n"));
			writer.write("\n");
		}
		writer.close();
	}

	private Injector getRevAuthInjector(Random rand) {
		if (this.this_technology == cl_technology)
		{  
			return Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
		} else if (this.this_technology == brands_technology){
			return Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.UPROVE));
		}
		return null;
	}

	private Injector getInjector(Random rand, RevocationProxyAuthority revocationProxyAuthority) {
		CryptoEngine cryptoEngine = null;
		if (this.this_technology == cl_technology) {  
			cryptoEngine = CryptoEngine.IDEMIX;
		} else if (this.this_technology == brands_technology) {
			cryptoEngine = CryptoEngine.UPROVE;
		}
		
		if (revocationProxyAuthority == null) {
			return Guice.createInjector(IntegrationModuleFactory.newModule(rand, cryptoEngine));
		} else {
			return Guice.createInjector(IntegrationModuleFactory.newModule(rand, cryptoEngine,revocationProxyAuthority));
		}
	}

	private Injector getVerifierInjector(Random rand,
			RevocationProxyAuthority revocationProxyAuthority) {
		return getInjector(rand, revocationProxyAuthority);
	}

	private Injector getIssuerInjector(Random rand,
			RevocationProxyAuthority revocationProxyAuthority) {
		return getInjector(rand, revocationProxyAuthority);
	}

	private Injector getUserInjector(Random rand, RevocationProxyAuthority revocationProxyAuthority) {
		return getInjector(rand, revocationProxyAuthority);
	}

	private Injector getInspectorInjector(Random rand,
			RevocationProxyAuthority revocationProxyAuthority) {
		return getInjector(rand, revocationProxyAuthority);
	}

	private void loadSystemParameters(SystemParameters systemParameters,
			Injector... injectors) throws KeyManagerException {

		for (Injector injector : injectors) {
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeSystemParameters(systemParameters);
		}
	}

	private void setupEngines( Injector issuerInjector, Injector revAuthInjector,
			Injector verifierInjector, Injector inspectorInjector, Injector userInjector,
			PseudonymWithMetadata pwm, SecretWrapper secretWrapper)	throws 
			KeyManagerException, JAXBException,	UnsupportedEncodingException, 
			SAXException, URISyntaxException, Exception, CredentialManagerException {

		KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
		KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
		KeyManager revAuthKeyManager = revAuthInjector.getInstance(KeyManager.class);
		KeyManager inspectorKeyManager = inspectorInjector.getInstance(KeyManager.class);
		KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
		// Setup issuance policies

		HashMap<IssuancePolicy, CredentialSpecification> issPolAndCredSpec = this.getIssPolsAndCredSpecs();

		// a structure to save pairs  issuance policy and credential specification
		this.setupEntities(issPolAndCredSpec, 
				issuerInjector, revAuthInjector, verifierInjector, inspectorInjector, userInjector,   
				issuerKeyManager, revAuthKeyManager, verifierKeyManager, inspectorKeyManager, userKeyManager);

		userCredentialManager.storePseudonym(USERNAME, pwm);

		// This is a hack since the TokenManagerIssuer does not allow us to add
		// a pseudonym.
		TokenStorageIssuer schoolTokenStorageManager = issuerInjector.getInstance(TokenStorageIssuer.class);
		String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
		schoolTokenStorageManager.addPseudonymPrimaryKey(primaryKey);
	}



	private void setupEntities( HashMap<IssuancePolicy, CredentialSpecification> issPolAndCredSpec,
			Injector issuerInjector, Injector revAuthInjector, Injector verifierInjector, 
			Injector inspectorInjector, Injector userInjector, KeyManager ... managers) 
			throws JAXBException, UnsupportedEncodingException,
					Exception, CredentialManagerException {

		//now iterate through the HashMap elements
		Iterator<Entry<IssuancePolicy, CredentialSpecification>> it = issPolAndCredSpec.entrySet().iterator();
		IssuerAbcEngine issuerABCEngine = issuerInjector.getInstance(IssuerAbcEngine.class);
		RevocationAbcEngine revAuthABCEngine = revAuthInjector.getInstance(RevocationAbcEngine.class);
		InspectorAbcEngine inspectorABCEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
		SystemParametersWrapper schoolSysParamsWrapper = new SystemParametersWrapper(systemParameters);

		IssuancePolicy currentPolicy = null;
		CredentialSpecification currentCredSpec = null;
		ArrayList<IssuerParameters> issParamsList = new ArrayList<IssuerParameters>();
		IssuerParameters currentIssuerParams = null;
		RevocationAuthorityParameters currentRevAuthParams = null;

		int maximalNumberOfAttributes = 25;

		try {
			currentRevAuthParams = FileSystem.loadObjectFromResource("storage/revAuth_params_" + this.keyLength);
			logger.info("Using revocation authority params from file revAuth_params_...");
		} catch (IOException e) {
			logger.info("No revocation authority params file - generating new parameters...");
			// Generate revocation parameters.

			Reference revocationInfoReference = new Reference();
			revocationInfoReference.setReferenceType(URI.create("https"));
			revocationInfoReference.getReferences().add(URI.create("http://example.org"));
			Reference nonRevocationEvidenceReference = new Reference();
			nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
			nonRevocationEvidenceReference.getReferences().add(URI.create("http://example.org"));
			Reference nonRrevocationUpdateReference = new Reference();
			nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
			nonRrevocationUpdateReference.getReferences().add(URI.create("http://example.org"));

			URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");
			System.out.println("Revocation technology used: " + revocationTechnology.toString());

			currentRevAuthParams = revAuthABCEngine.setupRevocationAuthorityParameters(this.keyLength, 
					revocationTechnology, this.REVOCATION_PARAMETERS_UID,
					revocationInfoReference, nonRevocationEvidenceReference, nonRrevocationUpdateReference);

			FileSystem.storeObjectInFile(currentRevAuthParams, "storage/revAuth_params_" + this.keyLength);
		}
		userCredentialManager  = userInjector.getInstance(CredentialManager.class);

		// Generate revocation parameters.

		Reference revocationInfoReference = new Reference();
		revocationInfoReference.setReferenceType(URI.create("https"));
		revocationInfoReference.getReferences().add(URI.create("http://example.org"));
		Reference nonRevocationEvidenceReference = new Reference();
		nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
		nonRevocationEvidenceReference.getReferences().add(URI.create("http://example.org"));
		Reference nonRrevocationUpdateReference = new Reference();
		nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
		nonRrevocationUpdateReference.getReferences().add(URI.create("http://example.org"));

		URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");
		System.out.println("Revocation technology used: " + revocationTechnology.toString());

		currentRevAuthParams = revAuthABCEngine.setupRevocationAuthorityParameters(this.keyLength, 
				revocationTechnology, this.REVOCATION_PARAMETERS_UID,
				revocationInfoReference, nonRevocationEvidenceReference, nonRrevocationUpdateReference);

		this.loadRevocationAuthParameters(currentRevAuthParams, 
				userInjector, issuerInjector, revAuthInjector, inspectorInjector, verifierInjector);

		URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");
		List<FriendlyDescription> friendlyDescription = Collections.emptyList();
		InspectorPublicKey inspectorPubKey =
				inspectorABCEngine.setupInspectorPublicKey(systemParameters,
						CryptoUriUtil.getIdemixMechanism(),	INSPECTOR_URI, friendlyDescription);

		while(it.hasNext()){
			
			Map.Entry<IssuancePolicy, CredentialSpecification> pairs = (Map.Entry<IssuancePolicy, CredentialSpecification>)it.next();
			currentPolicy = pairs.getKey();
			currentCredSpec = pairs.getValue();

			System.out.println("\n\ncurrentIssuancePolicyID = " + currentPolicy.getCredentialTemplate().getCredentialSpecUID());
			System.out.println("currentCredSpecID = " + currentCredSpec.getSpecificationUID() + "\n\n");
			URI policyUID = currentPolicy.getCredentialTemplate().getIssuerParametersUID();
			String policyId = policyUID.toString();
			String filename = policyId.substring(policyId.lastIndexOf(':') + 1);
			try {
				currentIssuerParams = FileSystem.loadObjectFromResource(
								"storage/issParams_" + filename + this.keyLength);
				issParamsList.add(currentIssuerParams);
			} catch (Exception e) {

				currentIssuerParams = issuerABCEngine.setupIssuerParameters(systemParameters, 
						maximalNumberOfAttributes, this_technology, policyUID, 
						PresentationTests.REVOCATION_PARAMETERS_UID, friendlyDescription);

				if (this.this_technology == brands_technology) {
					// set number  attributes!
					PublicKey pk = ((JAXBElement<PublicKey>) 
							currentIssuerParams.getCryptoParams().getContent().get(0)).getValue();
					String uproveTokensKey = "urn:idmx:3.0.0:issuer:publicKey:uprove:tokens";
					for (Parameter p : pk.getParameter()) {
						if (uproveTokensKey.equals(p.getName())){
							IntegerParameter ip = (IntegerParameter) p;
							ip.setValue(NUMBER_OF_UPROVE_TOKENS);
							break;
						}
					}
				}
				FileSystem.storeObjectInFile(currentIssuerParams, 
						"storage/nspectorPublicKey_" +  filename + this_technology + this.keyLength);

				issParamsList.add(currentIssuerParams);
				FileSystem.storeObjectInFile(currentIssuerParams, 
						"storage/issParams_" +  filename);
			}

			for (KeyManager keyManager : managers) {
				try{
					keyManager.storeSystemParameters(systemParameters);
					keyManager.storeCredentialSpecification(
							currentCredSpec.getSpecificationUID(), currentCredSpec);
					keyManager.storeIssuerParameters(
							currentIssuerParams.getParametersUID(), currentIssuerParams);
					keyManager.storeInspectorPublicKey(INSPECTOR_URI, inspectorPubKey);
				} catch(Exception ex){
					System.out.println(ex.getMessage());
				}
			}

			if (secretWrapper.isSecretOnSmartcard()) {
				// add smartcard to manager
				cardStorage = userInjector
						.getInstance(CardStorage.class);
				cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(),
						secretWrapper.getPin());

				// sign issuer attributes and add to smartcard
				secretWrapper.addIssuerParameters(currentIssuerParams, schoolSysParamsWrapper.getSystemParameters());
			}else {
				userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
			}
		}
	}

	private void loadRevocationAuthParameters(RevocationAuthorityParameters revAuthParams,
			Injector... injectors) throws KeyManagerException {

		for (Injector injector : injectors) {
			KeyManager keyManager = injector.getInstance(KeyManager.class);
			keyManager.storeRevocationAuthorityParameters(REVOCATION_PARAMETERS_UID, revAuthParams);
		}
	}

	private HashMap<IssuancePolicy, CredentialSpecification> getIssPolsAndCredSpecs() throws JAXBException,
	UnsupportedEncodingException, Exception, CredentialManagerException {


		HashMap<IssuancePolicy, CredentialSpecification> issPolAndCredSpec = 
				new HashMap<IssuancePolicy, CredentialSpecification>();

		IssuancePolicy issuancePolicyFromScratch = (IssuancePolicy) XmlUtils
				.getObjectFromXML(
						this.getClass().getResourceAsStream(ISSUANCE_POLICY_FROM_SCRATCH), 
						true);
		
		CredentialSpecification simpleCredSchoolSpec = (CredentialSpecification) 
				XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
						CREDSPEC_SIMPLE), true);
		//add the isspol, credspec pairs one by one
		issPolAndCredSpec.put(issuancePolicyFromScratch, simpleCredSchoolSpec); 

		IssuancePolicy issuancePolicyWithNym = (IssuancePolicy) XmlUtils
				.getObjectFromXML(
						this.getClass().getResourceAsStream(ISSUANCE_POLICY_PSEUDONYM_PROOF),
						true);
		CredentialSpecification schoolCredSpec = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(CREDSPEC_NYM_PROOF),
						true);
		issPolAndCredSpec.put(issuancePolicyWithNym, schoolCredSpec); 

		IssuancePolicy issuancePolicy12Attributes = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
						ISSUANCE_POLICY12Atts),
						true);
		CredentialSpecification schoolCredSpec12Atts = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
						CREDSPEC24ATTS),
						true);
		issPolAndCredSpec.put(issuancePolicy12Attributes, schoolCredSpec12Atts);

		IssuancePolicy issuancePolicy24Attributes = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY24Atts),
								true);
		CredentialSpecification schoolCredSpec24Atts = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								CREDSPEC24ATTS),
								true);
		issPolAndCredSpec.put(issuancePolicy24Attributes, schoolCredSpec24Atts);

		IssuancePolicy issPolCredCarryOver = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY_CARRY_OVER),
								true);
		CredentialSpecification credSpecCarryOver = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								CREDSPEC_CARRY_OVER),
								true);
		issPolAndCredSpec.put(issPolCredCarryOver, credSpecCarryOver);

		IssuancePolicy issPolicyRevocable = (IssuancePolicy) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE),
								true);
		CredentialSpecification credSpecRevocable = (CredentialSpecification) XmlUtils
				.getObjectFromXML(this.getClass().getResourceAsStream(
								CREDSPEC_SCHOOL_REVOCABLE),
								true);
		issPolAndCredSpec.put(issPolicyRevocable, credSpecRevocable);

		return issPolAndCredSpec;
	}

	private void setUpTheCard(
			ArrayList<IssuerParameters> issParsList, SecretWrapper secretWrapper ) throws IOException, JAXBException, SAXException, ConfigurationException, Exception{

		if (secretWrapper.isSecretOnSmartcard()) {
			//iterate through the list  issuer parameters
			IssuerParameters issParams;
			int numIssuerParams = issParsList.size();
			int i = 0;
			while(i < numIssuerParams){
				issParams = issParsList.get(i);
				//fetch and add the current issuerParameters in the list
				logger.info("adding new issuer parameters in the card :" + issParams.getParametersUID().toString());
				secretWrapper.addIssuerParameters(issParams, systemParameters);
				i++;
			}
			// add smartcard to manager
			cardStorage.addSmartcard(s,	secretWrapper.getPin());
		} 
		else {
			userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
		}
		s = secretWrapper.getSoftwareSmartcard();
	}



	private void issueThoseCredentials(Injector userInjector, 
			Injector issuerInjector) throws Exception{

		IssuanceHelper issuanceHelper = new IssuanceHelper();
		logger.info(">> Login with pseudonym.");
		this.loginWithPseudonym(issuerInjector, userInjector,issuanceHelper);

		URI carryOverURI = URI.create("urn:soderhamn:credspec:credCarryOver");
		URI simpleCredSpecURI = URI.create("urn:soderhamn:credspec:credSchool_simple");
		URI revocableURI = URI.create("urn:soderhamn:credspec:credSchool_revocable");
		URI credSchoolURI = URI.create("urn:soderhamn:credspec:credSchool");
		URI credSchool12AttsURI = URI.create("urn:soderhamn:credspec:credSchool12Atts");
		URI credSchool24Atts = URI.create("urn:soderhamn:credspec:credSchool24Atts");

		int numIssuanceRounds = 1;

		HashMap<IssuancePolicy, CredentialSpecification> issPolAndCredSpec = 
				this.getIssPolsAndCredSpecs();
		Iterator<Entry<IssuancePolicy, CredentialSpecification>> it = 
				issPolAndCredSpec.entrySet().iterator();
		try{
			while (it.hasNext()){
				CredentialSpecification credspec = it.next().getValue();
				if (credspec.getSpecificationUID().equals(revocableURI)) {
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueAndStoreCourseCredential_Revocable(issuerInjector, 
								userInjector, issuanceHelper);
					}
				} else if (credspec.getSpecificationUID().equals(simpleCredSpecURI)) {
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueAndStoreCourseCredential_fromScratch(issuerInjector, userInjector, issuanceHelper);
					}
				} else if (credspec.getSpecificationUID().equals(credSchoolURI)) {
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueAndStoreSchoolCredential_ProvingPseudonym(issuerInjector, userInjector, issuanceHelper);
					}
				} else if (credspec.getSpecificationUID().equals(credSchool12AttsURI)) {
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueAndStoreSchoolCredential_12Atts_PseudonymProof(issuerInjector, userInjector, issuanceHelper);
					}
				} else if (credspec.getSpecificationUID().equals(credSchool24Atts))	{
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueAndStoreSchoolCredential_24Atts_PseudonymProof(issuerInjector, userInjector, issuanceHelper);
					}
				} else if (credspec.getSpecificationUID().equals(carryOverURI))	{
					for (int i = 0; i < numIssuanceRounds; i ++) {
						this.issueCarryOverAttribute(issuerInjector, userInjector, issuanceHelper);
					}
				}
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void issueThoseCredentials2(Injector userInjector,
			Injector issuerInjector) throws Exception{
	
		IssuanceHelper issuanceHelper = new IssuanceHelper();
		logger.info(">> Login with pseudonym.");
		this.loginWithPseudonym(issuerInjector, userInjector,
				issuanceHelper);

		this.issueAndStoreCourseCredential_fromScratch(issuerInjector, userInjector, issuanceHelper);
		this.issueAndStoreSchoolCredential_ProvingPseudonym(issuerInjector, userInjector, issuanceHelper);
//		this.issueCarryOverAttribute(issuerInjector, userInjector, issuanceHelper);
//		this.issueAndStoreSchoolCredential_12Atts_PseudonymProof(issuerInjector, userInjector, issuanceHelper);
//		this.issueAndStoreSchoolCredential_24Atts_PseudonymProof(issuerInjector, userInjector, issuanceHelper);
	}

	private void loginWithPseudonym(Injector schoolInjector,
			Injector userInjector, IssuanceHelper issuanceHelper)
					throws Exception {
	
		PresentationToken t = this.loginWithPseudonym(issuanceHelper,
				schoolInjector,	userInjector);
		assertNotNull(t);
	}


	private PresentationToken testVerification(IssuanceHelper issuanceHelper,
			Injector schoolInjector, Injector userInjector,
            String presPolicy) throws Exception {

		List<URI> chosenInspectors = new LinkedList<URI>();
		chosenInspectors.add(URI.create("urn:soderhamn:registration"));
		System.out.println("-Proving_" + presPolicy );
		int pos = presPolicy.lastIndexOf("/");
		String policy = presPolicy.substring(pos + 1);

		TimingsLogger.logTiming("-Proving_" + policy, true);

		Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
				.createPresentationToken(schoolInjector, userInjector,
						presPolicy);
		assertNotNull(p);
		TimingsLogger.logTiming("-Proving_" + policy, false);

		TimingsLogger.logTiming("-Verifying_" + policy, true);

		PresentationToken pt  = issuanceHelper.verify(schoolInjector, p.second, p.first);
		TimingsLogger.logTiming("-Verifying_" + policy, false);

		assertNotNull(pt);
		return pt;
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

	private CredentialDescription issueAndStoreCourseCredential_fromScratch(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper) throws Exception {
		
		CredentialDescription cd = null;
		String filename = this_technology + "_" + this.keyLength + "_CRED_SIMPLE";
		Map<String, Object> atts = this.populateSchoolCredentialAttributesSimple();
		
		String methodname = ISSUANCE_POLICY_FROM_SCRATCH
				.substring(ISSUANCE_POLICY_FROM_SCRATCH.lastIndexOf("/") + 1);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength , true);
		cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector,
				CREDSPEC_SIMPLE, ISSUANCE_POLICY_FROM_SCRATCH, atts, filename);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength , false);

		logger.info("Credential from scratch  issued successfully.");
		return cd;
	}

	private CredentialDescription issueAndStoreCourseCredential_Revocable(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper) throws Exception {
		
		CredentialDescription cd = null;
		Map<String, Object> atts = this.populateSchoolCredentialAttributesREVOCATION();
		
		String filename = this_technology + "_" + this.keyLength + "_CRED_REVOCABLE";
		logger.info("now issuing revocable credential..:");
		try{
			String methodname = ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE
					.substring(ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE.lastIndexOf("/") + 1);

			TimingsLogger.logTiming(methodname+ "_" + this.this_technology +"_" + this.keyLength , true);
			cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector,
					CREDSPEC_SCHOOL_REVOCABLE, ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE, atts, filename);

			TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength , false);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			throw e;
		}

		logger.info("Credential from scratch  issued successfully.");
		return cd;
	}


	private CredentialDescription issueAndStoreSchoolCredential_ProvingPseudonym(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper) throws Exception {
		
		CredentialDescription cd = null;
		String filename = this_technology + "_" + this.keyLength + "_CRED_SCHOOL_6ATTS";
		Map<String, Object> atts = this.populateSchoolCredentialAttributes();

		String methodname = ISSUANCE_POLICY_PSEUDONYM_PROOF
				.substring(ISSUANCE_POLICY_PSEUDONYM_PROOF.lastIndexOf("/") + 1);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength , true);
		cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector,
				CREDSPEC_NYM_PROOF, ISSUANCE_POLICY_PSEUDONYM_PROOF, atts, filename);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength , false);
		return cd;
	}

	private CredentialDescription issueAndStoreSchoolCredential_12Atts_PseudonymProof(Injector issuerInjector,
			Injector userInjector, IssuanceHelper issuanceHelper) throws Exception {
		CredentialDescription cd = null;
		String filename = this_technology + "_" + this.keyLength + "_CRED12ATTS";
		Map<String, Object> atts = this.populateSchoolCredentialAttributes12();

		String methodname = ISSUANCE_POLICY12Atts.substring(ISSUANCE_POLICY12Atts.lastIndexOf("/") + 1);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, true);

		cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector,
				CREDSPEC12ATTS, ISSUANCE_POLICY12Atts, atts, filename);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, false);

		logger.info("Credential with 12 atts issued successfully.");
		return cd;

	}

	private CredentialDescription issueAndStoreSchoolCredential_24Atts_PseudonymProof(
			Injector issuerInjector, Injector userInjector,
			IssuanceHelper issuanceHelper)  throws Exception {
		CredentialDescription cd = null;
		Map<String, Object> atts = this.populateSchoolCredential24Attributes();
		String filename = this_technology + "_" + this.keyLength + "_CRED_24ATTS";

		String methodname = ISSUANCE_POLICY24Atts.substring(ISSUANCE_POLICY24Atts.lastIndexOf("/") + 1);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, true);

		cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector,
				CREDSPEC24ATTS, ISSUANCE_POLICY24Atts, atts, filename);

		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, false);
		logger.info("Credential with 24 atts issued successfully.");
		return cd;
	}

	private CredentialDescription issueCarryOverAttribute(
			Injector issuerInjector, Injector userInjector, IssuanceHelper issuanceHelper) 
			throws Exception{
		
		CredentialDescription cd = null;
		String filename = this_technology + "_" + this.keyLength + "_CRED_CARRY_OVER";
		String methodname = ISSUANCE_POLICY_CARRY_OVER
				.substring(ISSUANCE_POLICY_CARRY_OVER.lastIndexOf("/") + 1);
		
		Map<String, Object> blankAtts = this.populateSchoolCredentialCarryOverAtts();
		
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, true);
		//issue the credential with carry over attributes
		cd = new IssuanceHelper().issueCredential(issuerInjector, userInjector, 
				CREDSPEC_CARRY_OVER, ISSUANCE_POLICY_CARRY_OVER, blankAtts, filename);
		TimingsLogger.logTiming(methodname + "_" + this.this_technology +"_" + this.keyLength, false);
		logger.info("Credential with carry over issued successfully.");
		return cd;

	}
	
	

	private PseudonymWithMetadata createPseudonym(URI secretUid,
			CryptoEngineUser cryptoEngineUser, SystemParameters systemParameters) {
		String scope = "urn:soderhamn:registration";
		try {
			return cryptoEngineUser.createPseudonym(USERNAME, 
					URI.create("benchmarks-idemix-uri"), scope, true, secretUid);
		} catch (CryptoEngineException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> populateSchoolCredentialAttributesSimple() {
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

	private Map<String, Object> populateSchoolCredentialAttributesREVOCATION() {
		System.out.println("Now trying to run issuance  " + ISSUANCE_POLICY_FROM_SCRATCH_REVOCABLE);
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
		//att.put(REVOCATION_HANDLE_STR,
		//URI.create("urn:patras:revocation:handle1"));
		att.put("urn:soderhamn:credspec:credSchool12Atts:civicRegistrationNumber", "123456789");
		att.put("urn:soderhamn:credspec:credSchool12Atts:gender", "Female");
		Calendar cal = Calendar.getInstance();
		cal.set(2000, 1, 10);
		SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
		String dateValue = xmlDateFormat.format(cal.getTime());
		att.put("urn:soderhamn:credspec:credSchool12Atts:birthdate", dateValue);

		att.put("urn:soderhamn:credspec:credSchool12Atts:spanish", "mexican");
		return att;

	}

	private Map<String, Object> populateSchoolCredential24Attributes() {
		Map<String, Object> att = new HashMap<String, Object>();
		att.put("urn:soderhamn:credspec:credSchool24Atts:firstname", "Joe");
		att.put("urn:soderhamn:credspec:credSchool24Atts:lastname", "Cocker");
		att.put("urn:soderhamn:credspec:credSchool24Atts:civicRegistrationNumber", "123456789");
		att.put("urn:soderhamn:credspec:credSchool24Atts:gender", "Female");
		Calendar cal = Calendar.getInstance();
		cal.set(2000, 1, 10);
		SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
		String dateValue = xmlDateFormat.format(cal.getTime());
		att.put("urn:soderhamn:credspec:credSchool24Atts:birthdate", dateValue);

		att.put("urn:soderhamn:credspec:credSchool24Atts:spanish", "mexican");
		return att;
	}

	private Map<String, Object> populateSchoolCredentialCarryOverAtts() {
		Map<String, Object> att = new HashMap<String, Object>();
		att.put("urn:soderhamn:credspec:credCarryOver:lastname", "Cocker");
		att.put("urn:soderhamn:credspec:credCarryOver:civicRegistrationNumber", "123456789");
		att.put("urn:soderhamn:credspec:credCarryOver:gender", "Female");
		Calendar cal = Calendar.getInstance();
		cal.set(2000, 1, 10);
		SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
		String dateValue = xmlDateFormat.format(cal.getTime());
		att.put("urn:soderhamn:credspec:credCarryOver:birthdate", dateValue);
		att.put("urn:soderhamn:credspec:credCarryOver:schoolname", "Soederhamn Skole");
		return att;
	}
	
	public int sizeof(Object obj) throws IOException {
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
		objectOutputStream.writeObject(obj);
		objectOutputStream.flush();
		objectOutputStream.close();
		return byteOutputStream.toByteArray().length;
	}
}

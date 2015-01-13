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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Soderhamn scenario.
 */
public class SoderhamnPilotTest {

    private static final String USERNAME = "username";
    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnChild.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_CHILD =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnChild.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnClass.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_CLASS =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnClass.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnGuardian.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_GUARDIAN =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnGuardian.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnRole.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_ROLE =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnRole.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchoolWithRevocation.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_SUBJECT =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSubject.xml";

    private static final String PRESENTATION_POLICY_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml";

    private static final String PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeFrench.xml";

    private static final String PRESENTATION_POLICY_RA_SUJECT_MUST_BE_ENGLISH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeEnglish.xml";

    private static final String PRESENTATION_POLICY_RA_GENDER_MUST_BE_FEMALE_SUJECT_MUST_BE_FRENCH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRAGenderMustBeFemaleSubjectMustBeFrench.xml";

    private static final String PRESENTATION_POLICY_NUMBER_GENDER_FAIL =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyNumberGenderFail.xml";

    private URI revocationTechnology;
    
    @Test(timeout=TestConfiguration.TEST_TIMEOUT*60)
    public void soderhamnPilotIdemixTest() throws Exception {
      revocationTechnology = Helper.getRevocationTechnologyURI("cl");
      URI ce_technology = Helper.getSignatureTechnologyURI("cl");
      int keyLength = 1024;

      Entities entities = new Entities();

      entities.addEntity("CHILD", ce_technology, false);
      entities.addEntity("CLASS", ce_technology, false);
      entities.addEntity("GUARDIAN", ce_technology, false);
      entities.addEntity("ROLE", ce_technology, false);
      entities.addEntity("SUBJECT", ce_technology, false);
      entities.addEntity("SCHOOL", ce_technology, true);
      entities.addEntity("USER");
      entities.addEntity("VERIFIER");
      entities.addEntity("INSPECTOR");
            
      soderhamnPilotScenario(keyLength, entities);
    }
    
    @Test(timeout=TestConfiguration.TEST_TIMEOUT)
    public void soderhamnPilotUProveTest() throws Exception {
      revocationTechnology = Helper.getRevocationTechnologyURI("cl");
      URI ce_technology = Helper.getSignatureTechnologyURI("brands");
      int keyLength = 1024;

      Entities entities = new Entities();

      entities.addEntity("CHILD", ce_technology, false);
      entities.addEntity("CLASS", ce_technology, false);
      entities.addEntity("GUARDIAN", ce_technology, false);
      entities.addEntity("ROLE", ce_technology, false);
      entities.addEntity("SUBJECT", ce_technology, false);
      entities.addEntity("SCHOOL", ce_technology, true);
      entities.addEntity("USER");
      entities.addEntity("VERIFIER");
      entities.addEntity("INSPECTOR");
      
      soderhamnPilotScenario(keyLength, entities);
    }
    
    private void soderhamnPilotScenario(int keyLength, Entities entities) throws Exception{
      
      // Generate revocation parameters.      
      Injector revocationInjector = Guice
              .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                      CryptoEngine.IDEMIX));
      
      KeyManager revocationKeyManager = revocationInjector.getInstance(KeyManager.class);
      revocationKeyManager.storeSystemParameters(SystemParametersUtil.getDefaultSystemParameters_1024()); 
      
      RevocationProxyAuthority revocationProxyAuthority = revocationInjector
              .getInstance(RevocationProxyAuthority.class);
      RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
      URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
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
              .setupRevocationAuthorityParameters(keyLength,
                      revocationTechnology, revParamsUid, revocationInfoReference,
                      nonRevocationEvidenceReference, nonRrevocationUpdateReference);
      
      // Setup system by generating entities and system parameters
      Collection<Injector> injectors = createEntities(entities, revocationProxyAuthority);
      SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);
      
      List<Object> parametersList = new ArrayList<Object>();
      
      // Setup Child credential issuer
      URI credentialTechnology = entities.getTechnology("CHILD");
      URI issuerParametersUID =
          getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_CHILD);
      URI revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("CHILD"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));

      // Parameters for verifier parameters
      credentialTechnology = Helper.getSignatureTechnologyURI("cl");
      issuerParametersUID = URI.create("vp:rangeProof");
      parametersList.add(setupIssuer(entities.getInjector("CHILD"), systemParameters,
        credentialTechnology, issuerParametersUID, null, 0));
      
      // Setup Class credential issuer
      credentialTechnology = entities.getTechnology("CLASS");
      issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_CLASS);
      revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("CLASS"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
      
      // Setup Guardian credential issuer
      credentialTechnology = entities.getTechnology("GUARDIAN");
      issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_GUARDIAN);
      revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("GUARDIAN"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
      
      // Setup Role credential issuer
      credentialTechnology = entities.getTechnology("ROLE");
      issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_ROLE);
      revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("ROLE"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
      
      // Setup Subject credential issuer
      credentialTechnology = entities.getTechnology("SUBJECT");
      issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_SUBJECT);
      revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("SUBJECT"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
      
      // Setup School credential issuer
      credentialTechnology = entities.getTechnology("SCHOOL");
      issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_SODERHAMN_SCHOOL);
      revocationAuthorityUID = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
      parametersList.add(setupIssuer(entities.getInjector("SCHOOL"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
      
      // Store all issuer parameters to all key managers
      entities.storePublicParametersToKeyManagers(parametersList);
      
      // Generate verifier parameters
      VerifierParameters verifierParameter = entities.getInjector("USER").getInstance(VerifierAbcEngine.class).createVerifierParameters(systemParameters);

      // Store all credential specifications to all key managers
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL);
      
      // Setup user (generate secret and pseudonym and store issuer parameters)
      PseudonymWithMetadata pwm = 
          setupUser(entities.getInjector("USER"), systemParameters, parametersList);
      
      // This is a hack since the TokenManagerIssuer does not allow us to add a pseudonym.
      addPseudonymToIssuer(entities.getInjector("CHILD"), pwm);
      addPseudonymToIssuer(entities.getInjector("CLASS"), pwm);
      addPseudonymToIssuer(entities.getInjector("GUARDIAN"), pwm);
      addPseudonymToIssuer(entities.getInjector("ROLE"), pwm);
      addPseudonymToIssuer(entities.getInjector("SUBJECT"), pwm);
      addPseudonymToIssuer(entities.getInjector("SCHOOL"), pwm);
      
      addRevocationToIssuers(entities, revocationAuthorityParameters);
      
      IssuanceHelper issuanceHelper = new IssuanceHelper();
      
      // Step 1. Login with pseudonym.
      System.out.println(">> Login with pseudonym.");
      this.loginWithPseudonym(entities.getInjector("SCHOOL"), entities.getInjector("USER"), issuanceHelper);
      
   // Step 2a. Get child credential.
      System.out.println(">> Get child credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("CHILD"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD, ISSUANCE_POLICY_SODERHAMN_CHILD,
              this.populateChildAttributes(), null);

      // Step 2b. Get class credential.
      System.out.println(">> Get class credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("CLASS"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS, ISSUANCE_POLICY_SODERHAMN_CLASS,
              this.populateClassAttributes(), null);

      // Step 2c. Get Guardian credential.
      System.out.println(">> Get guardian credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("GUARDIAN"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN, ISSUANCE_POLICY_SODERHAMN_GUARDIAN,
              this.populateGuardianAttributes(), null);

      // Step 2d. Get Role credential.
      System.out.println(">> Get Role credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("ROLE"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE, ISSUANCE_POLICY_SODERHAMN_ROLE,
              this.populateRoleAttributes(), null);

      // Step 2e. Get School credential.
      System.out.println(">> Get school credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("SCHOOL"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL, ISSUANCE_POLICY_SODERHAMN_SCHOOL,
              this.populateSchoolAttributes(), null);

      // Step 2f. Get Subject credential.
      System.out.println(">> Get subject credential.");
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("SUBJECT"), entities.getInjector("USER"),
              CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT, ISSUANCE_POLICY_SODERHAMN_SUBJECT,
              this.populateSubjectAttributes("French"), null);

      /*
      CredentialManager userCredentialManager = entities.getInjector("USER").getInstance(CredentialManager.class);
      System.out.println("==== LIST Credentials ====");
      for(URI uri : userCredentialManager.listCredentials()) {
          Credential cred = userCredentialManager.getCredential(uri);
          CredentialDescription cd = cred.getCredentialDescription();
          System.out.println("- credential : " + cd.getCredentialUID() + " : " + cd.getAttribute());
          for(Attribute a : cd.getAttribute()) {
              System.out.println("-- attribute :  "  + a.getAttributeUID() + " : " + a.getAttributeValue());
          }
      }
      */
      // The verifier needs to retrive the latest revocation information
      // in order to put in the UID in the presentation policy.
      RevocationInformation revocationInformation = revocationEngine
              .updateRevocationInformation(revParamsUid);      

      // Step 3. Run presenations...
      System.out.println(">> Present 'subject' credential.");

      System.out.println(">> - we have 'French' - login by credential.");
      this.runPresentation(issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"),
        revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH, true, verifierParameter);

      // pseudonym established...
      System.out.println(">> - we have 'French' - log in by pseudonym.");
      this.runPresentation(issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"),
        revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH, true, verifierParameter);

      // policy cannot be satisfied...
      System.out.println(">> - we do NOT have 'English'.");
      this.runPresentation(issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"),
        revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_ENGLISH, false, verifierParameter);

      // policy satisfied...
      System.out.println(">> - we have 'female' and 'French'.");
      this.runPresentation(issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"),
        revocationInformation, PRESENTATION_POLICY_RA_GENDER_MUST_BE_FEMALE_SUJECT_MUST_BE_FRENCH, true, verifierParameter);

      // policy satisfied...
      System.out.println(">> - we have female.");
      this.runPresentation(issuanceHelper, entities.getInjector("VERIFIER"), entities.getInjector("USER"),
        revocationInformation, PRESENTATION_POLICY_NUMBER_GENDER_FAIL, true, verifierParameter);

    }
    
    private PseudonymWithMetadata setupUser(Injector userInjector, SystemParameters systemParameters,
                                            List<Object> parametersList) throws UnsupportedEncodingException, JAXBException,
                                            SAXException, ConfigurationException, CredentialManagerException {
      // Generate a secret and load it to the appropriate places
      SecretWrapper secretWrapper = new SecretWrapper(new Random(1234), systemParameters);

      
      CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);
    
      CardStorage cardStorage = userInjector.getInstance(CardStorage.class);

      int pin = 1234;
      cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(), pin);

      // sign issuer attributes and add to smartcard
      // NOTE: we assume the system parameters in the issuer parameters to match!
      for (Object parameters : parametersList) {
        if (IssuerParameters.class.isAssignableFrom(parameters.getClass())) {
          IssuerParameters ip = (IssuerParameters) parameters;
          // TODO(enr): Quick hack to allow CL issuer parameters for range proofs without exhausting smartcard memory
          if(ip.getParametersUID().toString().equals("vp:rangeProof")) {  
            continue;
          }
          secretWrapper.addIssuerParameters(ip,
            systemParameters);
        }
      }

      // Create a pseudonym
      PseudonymWithMetadata pwm =
          this.createPseudonym(secretWrapper.getSecretUID(),
            userInjector.getInstance(CryptoEngineUser.class), systemParameters);

      // Store it in the user credential manager.
      userCredentialManager.storePseudonym(USERNAME, pwm);

      return pwm;
    }
    
    private void addPseudonymToIssuer(Injector issuer, PseudonymWithMetadata pwm) throws IOException{
      TokenStorageIssuer tokenStorageManager =
          issuer.getInstance(TokenStorageIssuer.class);
      String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
      tokenStorageManager.addPseudonymPrimaryKey(primaryKey);
    }
    
    private void addRevocationToIssuers(Entities entities, RevocationAuthorityParameters revAuthParams) 
        throws KeyManagerException{
      for(Injector injector: entities.getInjectors()){
        KeyManager keyManager = injector.getInstance(KeyManager.class);
        keyManager.storeRevocationAuthorityParameters(IntegrationTestUtil.REVOCATION_PARAMETERS_UID, revAuthParams);
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
                                         URI credentialTechnology, URI issuanceParametersUID, URI revocationId,
                                         int maximalNumberOfAttributes) throws CryptoEngineException {
      // Generate issuer parameters.
      SystemParametersWrapper spWrapper = new SystemParametersWrapper(systemParameters);

      IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

      IssuerParameters issuerParameters =
          issuerEngine.setupIssuerParameters(spWrapper.getSystemParameters(),
            maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID, revocationId,
            null);

      return issuerParameters;
    }
    
    private void storeCredentialSpecificationToKeyManagers(Collection<Injector> injectors,
                                                           String pathToCredentialSpecification) throws KeyManagerException,
                                                           UnsupportedEncodingException, JAXBException, SAXException {

      // Load credential specifications.
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
    
    private Collection<Injector> createEntities(Entities entities, RevocationProxyAuthority revocationInjector) {

      // Assert that required entities are present
      assert (entities.contains("CHILD"));
      assert (entities.contains("CLASS"));
      assert (entities.contains("GUARDIAN"));
      assert (entities.contains("ROLE"));
      assert (entities.contains("SUBJECT"));
      assert (entities.contains("SCHOOL"));
      assert (entities.contains("USER"));
      assert (entities.contains("VERIFIER"));
      assert (entities.contains("INSPECTOR"));
      assert (!entities.contains("REVOCATION")); //do not contain revocation - should be separate

      entities.initInjectors(revocationInjector);

      return entities.getInjectors();
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
        List<URI> chosenInspectors = new LinkedList<URI>();
        // chosenInspectors.add(URI
        // .create("http://patras.gr/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> p =
                issuanceHelper.createPresentationToken(USERNAME, userInjector, policyResource, null, null);

        return issuanceHelper.verify(universityInjector, p.second, p.first);
    }

    private void runPresentation(
            IssuanceHelper issuanceHelper, Injector verifierInjector, Injector userInjector, RevocationInformation revocationInformation,
            String policyResource, boolean exprctSatisfied, VerifierParameters verifierParameters) throws Exception {


        if(exprctSatisfied) {
            Pair<PresentationToken, PresentationPolicyAlternatives> p =
                    issuanceHelper.createPresentationToken(USERNAME, userInjector, policyResource, revocationInformation, verifierParameters);
            System.out.println("Policy expected to be satisfied : " + p);
            assertNotNull("Policy expected to be satisfied", p);
            issuanceHelper.verify(verifierInjector, p.second, p.first);
        } else {
            Pair<PresentationToken, PresentationPolicyAlternatives> p =
                    issuanceHelper.createPresentationToken_NotSatisfied(USERNAME, userInjector, revocationInformation,
                            policyResource);
            System.out.println("Policy NOT expected to be satisfied : " + p);
            assertNull("Policy NOT expected to be satisfied", p);
        }
    }

    private Map<String, Object> populateChildAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credChild:child", "000501-2345");
        return att;
    }

    private Map<String, Object> populateClassAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credClass:classNumber", 7);
        att.put("urn:soderhamn:credspec:credClass:classGroup","classGroup");
        att.put("urn:soderhamn:credspec:credClass:classYear", 2012);
        return att;
    }

    private Map<String, Object> populateGuardianAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credGuardian:guardian", "guardian");
        return att;
    }

    private Map<String, Object> populateRoleAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credRole:pupil", true);
        att.put("urn:soderhamn:credspec:credRole:nurse", false);
        att.put("urn:soderhamn:credspec:credRole:teacher", false);
        att.put("urn:soderhamn:credspec:credRole:guardian", false);
        att.put("urn:soderhamn:credspec:credRole:role1", false);
        att.put("urn:soderhamn:credspec:credRole:role2", false);
        att.put("urn:soderhamn:credspec:credRole:role3", false);
        att.put("urn:soderhamn:credspec:credRole:role4", false);
        att.put("urn:soderhamn:credspec:credRole:role5", false);

        return att;
    }

    private Map<String, Object> populateSchoolAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credSchool:firstname", "Emily");
        att.put("urn:soderhamn:credspec:credSchool:lastname", "von Katthult Svensson");
        att.put("urn:soderhamn:credspec:credSchool:civicRegistrationNumber", "000501-2345");
        att.put("urn:soderhamn:credspec:credSchool:gender", "female");
        att.put("urn:soderhamn:credspec:credSchool:schoolname", "L\u00f6nneberga");

        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 10);
        SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
        String dateValue = xmlDateFormat.format(cal.getTime());
        att.put("urn:soderhamn:credspec:credSchool:birthdate", dateValue);
        return att;
    }

    private Map<String, Object> populateSubjectAttributes(String subject) {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credSubject:maths" , "maths".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:physics" , "physics".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:English" , "English".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:French" , "French".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject1" , "subject1".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject2" , "subject2".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject3" , "subject3".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject4" , "subject4".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject5" , "subject5".equals(subject));

        return att;
    }
    
    private PseudonymWithMetadata createPseudonym(URI secretUid, CryptoEngineUser cryptoEngineUser,
                                                  SystemParameters systemParameters) {
      String scope = "urn:soderhamn:registration";
      PseudonymWithMetadata pwm;
      try {
        pwm =
            cryptoEngineUser.createPseudonym(USERNAME, URI.create("soderhamndemo-idemix-uri"), scope, true,
              secretUid);
      } catch (CryptoEngineException e) {
        throw new RuntimeException(e);
      }
      return pwm;
    }

}

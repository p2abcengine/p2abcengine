//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//* Microsoft                                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
//* (C) Copyright Microsoft Corp. 2014. All Rights Reserved.          *
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

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.facades.InspectorParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Patras scenario.
 */
public class PatrasPilotTest {

  private static final String USERNAME = "defaultUser";
  private static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml";

  private static final String ISSUANCE_POLICY_PATRAS_UNIVERSITY =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_COURSE =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml";

  private static final String ISSUANCE_POLICY_PATRAS_COURSE =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml";

  private static final String PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml";

  private static final String PRESENTATION_POLICY_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasTombola.xml";

  private static final String ISSUANCE_POLICY_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasTombola.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasTombola.xml";

  private Random rand = new Random(1235);

  // TODO: Backup and restore of attendance credentials.

  private static final String COURSE_UID = "23330E";
  private static final String NAME = "John";
  private static final String LASTNAME = "Doe";
  private static final String UNIVERSITYNAME = "Patras";
  private static final String DEPARTMENTNAME = "CS";
  private static final int MATRICULATIONNUMBER = 1235332;


  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patrasPilotIdemixTest() throws Exception {
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    int keyLength = 1024;

    Entities entities = new Entities();

    entities.addEntity("UNIVERSITY", cl_technology, false);
    entities.addEntity("COURSE", cl_technology, true);
    entities.addEntity("USER");
    entities.addEntity("VERIFIER");
    entities.addEntity("INSPECTOR");
    entities.addEntity("TOMBOLA", cl_technology, true);

    patrasPilotScenario(keyLength, entities);
  }

  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patraPilotUProveTest() throws Exception {
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    int keyLength = 1024;

    Entities entities = new Entities();

    entities.addEntity("UNIVERSITY", uprove_technology, false);
    entities.addEntity("COURSE", uprove_technology, true);
    entities.addEntity("USER");
    entities.addEntity("VERIFIER");
    entities.addEntity("INSPECTOR");
    entities.addEntity("TOMBOLA", uprove_technology, true);

    patrasPilotScenario(keyLength, entities);
  }



  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patraPilotCombinedTestSameTechnology() throws Exception {
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    int keyLength = 1024;

    Entities entities = new Entities();

    entities.addEntity("UNIVERSITY", cl_technology, false);
    entities.addEntity("COURSE", uprove_technology, true);
    entities.addEntity("USER");
    entities.addEntity("VERIFIER");
    entities.addEntity("INSPECTOR");
    entities.addEntity("TOMBOLA", cl_technology, false);

    patrasPilotScenario(keyLength, entities);
  }
  
  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patraPilotCombinedTestDifferentTechnology() throws Exception {
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    int keyLength = 1024;

    Entities entities = new Entities();

    entities.addEntity("UNIVERSITY", uprove_technology, false);
    entities.addEntity("COURSE", cl_technology, true);
    entities.addEntity("USER");
    entities.addEntity("VERIFIER");
    entities.addEntity("INSPECTOR");
    entities.addEntity("TOMBOLA", cl_technology, false);

    patrasPilotScenario(keyLength, entities);
  }


  private void patrasPilotScenario(int keyLength, Entities entities) throws KeyManagerException,
  CryptoEngineException, UnsupportedEncodingException, JAXBException, SAXException,
  URISyntaxException, ConfigurationException, CredentialManagerException, IOException,
  Exception, eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException {
    // Setup system by generating entities and system parameters
    Collection<Injector> injectors = createEntities(entities);
    SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

    List<Object> parametersList = new ArrayList<Object>();

    // Setup university issuer
    URI credentialTechnology = entities.getTechnology("UNIVERSITY");
    URI issuerParametersUID =
        getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_PATRAS_UNIVERSITY);
    URI revocationAuthorityUID = new URI("revocationUID1");
    parametersList.add(setupIssuer(entities.getInjector("UNIVERSITY"), systemParameters,
      credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));

    // Setup course credential issuer
    credentialTechnology = entities.getTechnology("COURSE");
    issuerParametersUID = getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_PATRAS_COURSE);
    revocationAuthorityUID = new URI("revocationUID2");
    parametersList.add(setupIssuer(entities.getInjector("COURSE"), systemParameters,
      credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));

    // Setup tombola credential issuer
    if (entities.contains("TOMBOLA")) {
      credentialTechnology = entities.getTechnology("TOMBOLA");
      issuerParametersUID =
          getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_PATRAS_TOMBOLA);
      revocationAuthorityUID = new URI("revocationUID3");
      parametersList.add(setupIssuer(entities.getInjector("TOMBOLA"), systemParameters,
        credentialTechnology, issuerParametersUID, revocationAuthorityUID, 10));
    }

    // Store all issuer parameters to all key managers
    entities.storePublicParametersToKeyManagers(parametersList);

    // Store all credential specifications to all key managers
    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY);
    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PATRAS_COURSE);
    if (entities.contains("TOMBOLA")) {
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA);
    }

    // Setup user (generate secret and pseudonym)
    PseudonymWithMetadata pwm =
        setupUser(entities.getInjector("USER"), systemParameters, parametersList);


    // This is a hack since the TokenManagerIssuer does not allow us to add a pseudonym.
    TokenManagerIssuer universityTokenStorageManager = 
        entities.getInjector("UNIVERSITY").getInstance(TokenManagerIssuer.class);
    universityTokenStorageManager.addPeudonymForTest(pwm.getPseudonym().getPseudonymValue());
    TokenManagerIssuer courseTokenStorageManager = 
        entities.getInjector("COURSE").getInstance(TokenManagerIssuer.class);
    courseTokenStorageManager.addPeudonymForTest(pwm.getPseudonym().getPseudonymValue());

    IssuanceHelper issuanceHelper = new IssuanceHelper();
    // Step 1. Login with pseudonym.
    System.out.println(">> Login with pseudonym.");
    this.loginWithPseudonym(entities.getInjector("UNIVERSITY"), entities.getInjector("USER"),
      issuanceHelper);
    
    // Step 1. Get university credential.
    System.out.println(">> Get university credential.");
    this.issueAndStoreUniversityCredential(entities.getInjector("UNIVERSITY"),
      entities.getInjector("USER"), issuanceHelper);
    
    // Step 2. Get course credential.
    System.out.println(">> Get course credential.");
    this.issueAndStoreCourseCredential(entities.getInjector("COURSE"),
      entities.getInjector("USER"), issuanceHelper);
    
    // Verify against course evaluation using the course credential.
    System.out.println(">> Verify.");
    PresentationToken pt =
        this.logIntoCourseEvaluation(issuanceHelper, entities.getInjector("VERIFIER"),
          entities.getInjector("USER"));
    assertNotNull(pt);


    // Issue tombola credential
    if (entities.contains("TOMBOLA")) {

      Map<String, Object> atts = new HashMap<String, Object>();
      issuanceHelper.issueCredential(USERNAME, entities.getInjector("TOMBOLA"), entities.getInjector("USER"),
        CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA, ISSUANCE_POLICY_PATRAS_TOMBOLA, atts);

      // Inspect the tombola credential
      if (entities.contains("INSPECTOR")) {

        InspectorAbcEngine inspectorEngine =
            entities.getInjector("INSPECTOR").getInstance(InspectorAbcEngine.class);

        URI inspectionTechnology = CryptoUriUtil.getIdemixMechanism();
        URI inspectorPublicKeyUid = URI.create("urn:patras:inspector:tombola");
        List<FriendlyDescription> friendlyDescription = Collections.emptyList();
        InspectorPublicKey inspectorPublicKey =
            inspectorEngine.setupInspectorPublicKey(systemParameters, inspectionTechnology,
              inspectorPublicKeyUid, friendlyDescription);
        parametersList.add(inspectorPublicKey);

        // store inspector parameters to key managers (all the other parameters will be stored
        // again/overwritten)
        System.out.println("inspectorPublicKey : " + inspectorPublicKey.getPublicKeyUID());
        entities.storePublicParametersToKeyManagers(parametersList);

        // create presentation policy
        Pair<PresentationToken, PresentationPolicyAlternatives> p =
            issuanceHelper.createPresentationToken(USERNAME,
              entities.getInjector("USER"), PRESENTATION_POLICY_PATRAS_TOMBOLA, null, null);
        // verify..
        issuanceHelper.verify(entities.getInjector("INSPECTOR"), p.second, p.first);

        // inspect..
        List<Attribute> inspectedAttributes = inspectorEngine.inspect(p.first);
        System.out.println("inspectedAttributes");
        for (Attribute a : inspectedAttributes) {
          System.out.println(" a " + a.getAttributeUID() + " : " + a.getAttributeValue());
        }
      }
    }
  }

  private Collection<Injector> createEntities(Entities entities) {

    // Assert that required entities are present
    assert (entities.contains("UNIVERSITY"));
    assert (entities.contains("COURSE"));
    assert (entities.contains("USER"));
    assert (entities.contains("VERIFIER"));


    entities.initInjectors(null);

    return entities.getInjectors();
  }

  /*
   * NOTE: This does normally not need to be done. Rather, the issuer parameters UID are taken from
   * the issuer parameters and fed into the issuance policy.
   */
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
    IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

    IssuerParameters issuerParameters =
        issuerEngine.setupIssuerParameters(systemParameters,
          maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID, revocationId,
          null);

    return issuerParameters;
  }


  private PseudonymWithMetadata setupUser(Injector userInjector, SystemParameters systemParameters,
                                          List<Object> parametersList) throws UnsupportedEncodingException, JAXBException,
                                          SAXException, ConfigurationException, CredentialManagerException {
    
    URI secretUid = URI.create("secret-patras-test");
    Secret s = ExternalSecretsManagerImpl.generateSecret(systemParameters, BigInteger.valueOf(123456), secretUid);
    CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);
    userCredentialManager.storeSecret(USERNAME, s);

    CryptoEngineUser ce = userInjector.getInstance(CryptoEngineUser.class);
    // Create a pseudonym
    PseudonymWithMetadata pwm = this.createPseudonym(secretUid, ce, systemParameters);

    // Store it in the user credential manager.
    userCredentialManager.storePseudonym(USERNAME, pwm);

    return pwm;
  }
 


  private void storePublicParametersToKeyManagers(Collection<Injector> injectors,
                                                  List<Object> publicParameters) throws KeyManagerException, ConfigurationException {

    // Iterate over all key managers
    for (Injector injector : injectors) {
      KeyManager keyManager = injector.getInstance(KeyManager.class);

      // Iterate over all parameters
      for (Object parameters : publicParameters) {

        // Check for issuer parameters
        if (IssuerParameters.class.isAssignableFrom(parameters.getClass())) {
          IssuerParametersFacade ipWrapper =
              new IssuerParametersFacade((IssuerParameters) parameters);
          keyManager.storeIssuerParameters(ipWrapper.getIssuerParametersId(),
            ipWrapper.getIssuerParameters());

        }
        // Check for inspector parameters
        else if (InspectorPublicKey.class.isAssignableFrom(parameters.getClass())) {
          InspectorParametersFacade ipWrapper =
              new InspectorParametersFacade((InspectorPublicKey) parameters);
          keyManager.storeInspectorPublicKey(ipWrapper.getInspectorId(),
            ipWrapper.getInspectorParameters());


          // userKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);
          // inspectorKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);
        }
      }
    }
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

  private void loginWithPseudonym(Injector universityInjector, Injector userInjector,
                                  IssuanceHelper issuanceHelper) throws Exception {
    PresentationToken t =
        this.loginWithPseudonym(issuanceHelper, universityInjector, userInjector);
    assertNotNull(t);
  }

  private PresentationToken loginWithPseudonym(IssuanceHelper issuanceHelper,
                                               Injector universityInjector, Injector userInjector) throws Exception {
    System.out.println("test, logingwithPseudonym, pre token generation");
    Pair<PresentationToken, PresentationPolicyAlternatives> p =
        issuanceHelper.createPresentationToken(USERNAME, userInjector,
          PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN, null, null);
    System.out.println("test, logingwithPseudonym, now we verify");
    return issuanceHelper.verify(universityInjector, p.second, p.first);
  }

  private void issueAndStoreUniversityCredential(Injector issuerInjector, Injector userInjector,
                                                 IssuanceHelper issuanceHelper) throws Exception {
    Map<String, Object> atts = this.populateUniveristyAttributes();
    issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjector,
      CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY, ISSUANCE_POLICY_PATRAS_UNIVERSITY, atts, null);
  }

  private void issueAndStoreCourseCredential(Injector issuerInjector, Injector userInjector,
                                             IssuanceHelper issuanceHelper) throws Exception {
    Map<String, Object> atts = this.populateCourseAttributes();
    issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjector,
      CREDENTIAL_SPECIFICATION_PATRAS_COURSE, ISSUANCE_POLICY_PATRAS_COURSE, atts, null);

  }

  private PresentationToken logIntoCourseEvaluation(IssuanceHelper issuanceHelper,
                                                    Injector verifierInjector, Injector userInjector) throws Exception {
    /*
     * Verify for poll. The user must have: 1) A non-revoked university credential 2) A course
     * credential with the same matriculation number as the university credential 3) A certain
     * number of attendance credentials, which must be higher than a certain threshold 4) All
     * attendance credentials must have the same matriculation number as the university credential
     * 5) All attendance credentials must have a unique UID.
     */
    return this.login(issuanceHelper, verifierInjector, userInjector);
  }

  private PresentationToken login(IssuanceHelper issuanceHelper, Injector verifierInjector,
                                  Injector userInjector) throws Exception {
    Pair<PresentationToken, PresentationPolicyAlternatives> p =
        issuanceHelper.createPresentationToken(USERNAME, userInjector,
          PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION, null, null);

    // Store all required cred specs in the verifier key manager.
    KeyManager hotelKeyManager = verifierInjector.getInstance(KeyManager.class);
    KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

    PresentationToken pt = p.first;
    assertNotNull(pt);
    for (CredentialInToken cit : pt.getPresentationTokenDescription().getCredential()) {
      hotelKeyManager.storeCredentialSpecification(cit.getCredentialSpecUID(),
        userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
    }
    return issuanceHelper.verify(verifierInjector, p.second, p.first);
  }

  private PseudonymWithMetadata createPseudonym(URI secretUid, CryptoEngineUser cryptoEngineUser,
                                                SystemParameters systemParameters) {
    String scope = "urn:patras:registration";
    PseudonymWithMetadata pwm;
    try {
      pwm =
          cryptoEngineUser.createPseudonym(USERNAME, URI.create("patrasdemo-idemix-uri"), scope, true,
            secretUid);
    } catch (CryptoEngineException e) {
      throw new RuntimeException(e);
    }
    return pwm;
  }

  private Map<String, Object> populateCourseAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
    att.put(REVOCATION_HANDLE_STR, URI.create("urn:patras:revocation:handle2"));
    return att;
  }

  private Map<String, Object> populateUniveristyAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credUniv:firstname", NAME);
    att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
    att.put(REVOCATION_HANDLE_STR, URI.create("urn:patras:revocation:handle1"));
    att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
    att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
    att.put("urn:patras:credspec:credUniv:matriculationnr", MATRICULATIONNUMBER);
    return att;
  }
}

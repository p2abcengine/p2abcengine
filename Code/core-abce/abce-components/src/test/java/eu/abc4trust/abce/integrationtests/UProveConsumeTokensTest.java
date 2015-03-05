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

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.facades.InspectorParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.NotEnoughTokensException;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Patras scenario.
 */
public class UProveConsumeTokensTest {
  
  private static final String USERNAME = "username";

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

  private Random rand = new Random(1235);

  private static final String COURSE_UID = "23330E";
  private static final String NAME = "John";
  private static final String LASTNAME = "Doe";
  private static final String UNIVERSITYNAME = "Patras";
  private static final String DEPARTMENTNAME = "CS";
  private static final int MATRICULATIONNUMBER = 1235332;
  
  /**
   * Tests issuance of UProve credentials, using all tokens in the "course" 
   * credential and expecting an exception after last token is consumed. 
   * @throws Exception
   */
  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void consumeAllTokens() throws Exception {
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    int keyLength = 1024;

    Entities entities = new Entities();

    entities.addEntity("UNIVERSITY", uprove_technology, false);
    entities.addEntity("COURSE", uprove_technology, false);
    entities.addEntity("USER");
    entities.addEntity("VERIFIER");

    setupScenario(keyLength, entities);
    
    IssuanceHelper issuanceHelper = new IssuanceHelper();
    
    issueCredentials(issuanceHelper, entities);
    
    for(int i = 0; i < 5; i++){
      useToken(issuanceHelper, entities);
    }    
    
    try{
      useToken(issuanceHelper, entities);
      Assert.fail();
    }catch(CryptoEngineException e){
      if(e.getCause().getCause() instanceof NotEnoughTokensException){
        // No more tokens - correct!
      }      
    }catch(Exception ex){
      throw ex;
    }
  }

  private void setupScenario(int keyLength, Entities entities) throws KeyManagerException,
  CryptoEngineException, UnsupportedEncodingException, JAXBException, SAXException,
  URISyntaxException, ConfigurationException, CredentialManagerException, IOException,
  Exception, eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException{

    List<Object> parametersList = new ArrayList<Object>();
    
    // Setup system by generating entities and system parameters
    Collection<Injector> injectors = createEntities(entities);
    SystemParameters systemParameters = setupSystemParameters(entities, keyLength);

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

    // Store all issuer parameters to all key managers
    storePublicParametersToKeyManagers(injectors, parametersList);

    // Store all credential specifications to all key managers
    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY);
    storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PATRAS_COURSE);

    // Setup user (generate secret and pseudonym)
    PseudonymWithMetadata pwm =
        setupUser(entities.getInjector("USER"), systemParameters, parametersList);


    // This is a hack since the TokenManagerIssuer does not allow us to add a pseudonym.
    TokenStorageIssuer universityTokenStorageManager =
        entities.getInjector("UNIVERSITY").getInstance(TokenStorageIssuer.class);
    String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
    universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);
    TokenStorageIssuer courseTokenStorageManager =
        entities.getInjector("COURSE").getInstance(TokenStorageIssuer.class);
    primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
    courseTokenStorageManager.addPseudonymPrimaryKey(primaryKey);
  }
  
  private void issueCredentials(IssuanceHelper issuanceHelper, Entities entities) throws KeyManagerException,
  CryptoEngineException, UnsupportedEncodingException, JAXBException, SAXException,
  URISyntaxException, ConfigurationException, CredentialManagerException, IOException,
  Exception, eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException {

    // Step 1. Get university credential.
    System.out.println(">> Get university credential.");
    this.issueAndStoreUniversityCredential(entities.getInjector("UNIVERSITY"),
      entities.getInjector("USER"), issuanceHelper);
    // Step 2. Get course credential.
    System.out.println(">> Get course credential.");
    this.issueAndStoreCourseCredential(entities.getInjector("COURSE"),
      entities.getInjector("USER"), issuanceHelper);
    // Verify against course evaluation using the course credential.    
  }
  
  private void useToken(IssuanceHelper issuanceHelper, Entities entities) throws Exception{
    System.out.println(">> Verify.");
    PresentationToken pt =
        this.logIntoCourseEvaluation(issuanceHelper, entities.getInjector("VERIFIER"),
          entities.getInjector("USER"));
    assertNotNull(pt);
  }

  private Collection<Injector> createEntities(Entities entities) {
	// Assert that required entities are present
    assert (entities.contains("UNIVERSITY"));
    assert (entities.contains("COURSE"));
    assert (entities.contains("USER"));
    assert (entities.contains("VERIFIER"));

    entities.initInjectors();
    return entities.getInjectors();
  }

  private SystemParameters setupSystemParameters(Entities entities, int keyLength)
      throws KeyManagerException, CryptoEngineException {
    // Generate system parameters and load them into the key managers of all parties
    SystemParameters systemParameters;
    if(keyLength == 1024){
      systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    }else{
      systemParameters = SystemParametersUtil.getDefaultSystemParameters_2048();
    }
    loadSystemParametersIntoEntityKeyManagers(systemParameters, entities.getInjectors());
    return systemParameters;
  }

  private void loadSystemParametersIntoEntityKeyManagers(SystemParameters systemParameters,
                                                         Collection<Injector> injectors) throws KeyManagerException {

    for (Injector injector : injectors) {
      KeyManager keyManager = injector.getInstance(KeyManager.class);
      keyManager.storeSystemParameters(systemParameters);
    }
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
    SystemParametersWrapper spWrapper = new SystemParametersWrapper(systemParameters);

    IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

    IssuerParameters issuerParameters =
        issuerEngine.setupIssuerParameters(spWrapper.getSystemParameters(),
          maximalNumberOfAttributes, credentialTechnology, issuanceParametersUID, revocationId,
          null);

    return issuerParameters;
  }


  private PseudonymWithMetadata setupUser(Injector userInjector, SystemParameters systemParameters,
                                          List<Object> parametersList) throws UnsupportedEncodingException, JAXBException,
                                          SAXException, ConfigurationException, CredentialManagerException {

    SystemParametersWrapper spWrapper = new SystemParametersWrapper(systemParameters);

    // Generate a secret and load it to the appropriate places
    SecretWrapper secretWrapper = getSecretWrapper(systemParameters, rand);

    CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);
    if (!secretWrapper.isSecretOnSmartcard()) {
      userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
    } else {
      CardStorage cardStorage = userInjector.getInstance(CardStorage.class);

      int pin = 1234;
      cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(), pin);

      // sign issuer attributes and add to smartcard
      // NOTE: we assume the system parameters in the issuer parameters to match!
      for (Object parameters : parametersList) {
        if (IssuerParameters.class.isAssignableFrom(parameters.getClass())) {
          secretWrapper.addIssuerParameters((IssuerParameters) parameters,
            spWrapper.getSystemParameters());
        }
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

  private SecretWrapper getSecretWrapper(SystemParameters systemParameters, Random random)
      throws JAXBException, UnsupportedEncodingException, SAXException, ConfigurationException {

    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    // Secret needs to be newly generated
    SecretWrapper secretWrapper = new SecretWrapper(random, spWrapper.getSystemParameters());

    return secretWrapper;
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

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

/*
 * ~~~~ Copyright notice IBM ~~~~
 */

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.revocation.RevocationConstants;
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
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * 
 */
public class RevocationHelper {

  private static final String USERNAME = "defaultUser";
  private static final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");


  private final IssuanceHelper issuanceHelper;

  private final Injector revocationInjector;
  private final Injector issuerInjector;
  private final Injector[] userInjectors;
  private final SecretWrapper[] secretWrappers;
  private final PseudonymWithMetadata[] pseudonymsWithMetadata;
  private final Injector inspectorInjector;
  private final Injector verifierInjector;

  private final SystemParameters systemParameters;
  private final int numberOfUsers;

  private Random rand = new Random(1235);


  public RevocationHelper(int numberOfUsers, int keyLength, URI revocationTechnology)
      throws CryptoEngineException, KeyManagerException,
      eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException {

    issuanceHelper = new IssuanceHelper();

    this.numberOfUsers = numberOfUsers;
    userInjectors = new Injector[numberOfUsers];
    secretWrappers = new SecretWrapper[numberOfUsers];
    pseudonymsWithMetadata = new PseudonymWithMetadata[numberOfUsers];

    // Get revocation proxy
    revocationInjector = getRevocationInjector(rand);
    RevocationProxyAuthority revocationProxyAuthority =
        revocationInjector.getInstance(RevocationProxyAuthority.class);

    // Get different injectors
    issuerInjector = getUniversityInjector(rand, revocationProxyAuthority);
    for (int i = 0; i < numberOfUsers; i++) {
      userInjectors[i] = getUserInjector(rand, revocationProxyAuthority);
    }
    inspectorInjector = getInspectorInjector(rand, revocationProxyAuthority);
    verifierInjector = getVerifierInjector(rand, revocationProxyAuthority);

    // Generate system parameters and load them into the key managers of all parties
    if(keyLength == 1024){
      systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    }else{
      systemParameters = SystemParametersUtil.getDefaultSystemParameters_2048();
    }
    storeElementToKeyManagers(systemParameters, userInjectors, issuerInjector, inspectorInjector,
        revocationInjector, verifierInjector);


    // Get the revocation authority engine
    RevocationAbcEngine revocationEngine =
        revocationInjector.getInstance(RevocationAbcEngine.class);

    // Setup the revocation authority parameters and load them into the key managers
    RevocationAuthorityParametersFacade raParametersFacade =
        setupRevocationAuthorityParameters(keyLength, revocationTechnology, revocationEngine);
    storeElementToKeyManagers(raParametersFacade.getRevocationAuthorityParameters(), userInjectors,
        issuerInjector, verifierInjector);


    // Get the inspector engine
    InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);

    // Setup the inspector public key and load them into the key managers
    InspectorPublicKey inspectorPublicKey = setupInspectorPublicKey(inspectorEngine);
    storeElementToKeyManagers(inspectorPublicKey, userInjectors, inspectorInjector,
        verifierInjector);
  }

  private InspectorPublicKey setupInspectorPublicKey(InspectorAbcEngine inspectorEngine)
      throws CryptoEngineException,
      eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException {
    URI mechanism = CryptoUriUtil.getIdemixMechanism();
    URI inspectorPublicKeyUid = URI.create("urn:patras:inspector:tombola");
    List<FriendlyDescription> friendlyDescription = Collections.emptyList();
    InspectorPublicKey inspectorPublicKey =
        inspectorEngine.setupInspectorPublicKey(systemParameters, mechanism, inspectorPublicKeyUid,
            friendlyDescription);
    return inspectorPublicKey;
  }


  private Injector getInjector(Random rand, RevocationProxyAuthority revocationProxyAuthority) {
    Injector injector = null;
    if (revocationProxyAuthority == null) {
      injector =
          Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
    } else {
      injector =
          Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX,
              revocationProxyAuthority));
    }
    return injector;
  }


  private Injector getRevocationInjector(Random rand) {
    return getInjector(rand, null);
  }

  private Injector getUniversityInjector(Random rand,
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

  private Injector getVerifierInjector(Random rand,
      RevocationProxyAuthority revocationProxyAuthority) {
    return getInjector(rand, revocationProxyAuthority);
  }

  private <T> T[] concatenate(T[] firstArray, T[] secondArray) {
    if (firstArray == null) {
      return secondArray;
    }
    if (secondArray == null) {
      return firstArray;
    }
    T[] result = Arrays.copyOf(firstArray, firstArray.length + secondArray.length);
    System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);
    return result;
  }


  private <T> void storeElementToKeyManagers(T element, Injector[] userInjectors,
      Injector... injectors) throws KeyManagerException {

    for (Injector userInjector : concatenate(userInjectors, injectors)) {
      KeyManager keyManager = userInjector.getInstance(KeyManager.class);
      if (element instanceof SystemParameters) {
        keyManager.storeSystemParameters(systemParameters);
      } else if (element instanceof IssuerParameters) {
        IssuerParameters issuerParameters = (IssuerParameters) element;
        keyManager.storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);
      } else if (element instanceof CredentialSpecification) {
        CredentialSpecification credentialSpecification = (CredentialSpecification) element;
        keyManager.storeCredentialSpecification(credentialSpecification.getSpecificationUID(),
            credentialSpecification);
      } else if (element instanceof RevocationAuthorityParameters) {
        RevocationAuthorityParameters raParameters = (RevocationAuthorityParameters) element;
        keyManager.storeRevocationAuthorityParameters(REVOCATION_PARAMETERS_UID, raParameters);
      } else if (element instanceof InspectorPublicKey) {
        InspectorPublicKey inspectorPublicKey = (InspectorPublicKey) element;
        keyManager
            .storeInspectorPublicKey(inspectorPublicKey.getPublicKeyUID(), inspectorPublicKey);
      } else {
        throw new RuntimeException(" Element cannot be stored to KeyManagers: " + element);
      }
    }

  }


  // private void loadSystemParameters(SystemParameters systemParameters, Injector[] userInjectors,
  // Injector... injectors) throws KeyManagerException {
  //
  // for (Injector userInjector : userInjectors) {
  // KeyManager keyManager = userInjector.getInstance(KeyManager.class);
  // keyManager.storeSystemParameters(systemParameters);
  // }
  //
  // for (Injector injector : injectors) {
  // KeyManager keyManager = injector.getInstance(KeyManager.class);
  // keyManager.storeSystemParameters(systemParameters);
  // }
  // }
  //
  // private void loadRevocationAuthParameters(RevocationAuthorityParameters revAuthParams,
  // Injector[] userInjectors, Injector... injectors) throws KeyManagerException {
  //
  // for (Injector injector : userInjectors) {
  // KeyManager keyManager = injector.getInstance(KeyManager.class);
  // keyManager.storeRevocationAuthorityParameters(REVOCATION_PARAMETERS_UID, revAuthParams);
  // }
  //
  // for (Injector injector : injectors) {
  // KeyManager keyManager = injector.getInstance(KeyManager.class);
  // keyManager.storeRevocationAuthorityParameters(REVOCATION_PARAMETERS_UID, revAuthParams);
  // }
  // }
  //
  // private void storeIssuerParametersToKeyManagers(IssuerParameters issuerParameters,
  // Injector[] userInjectors, Injector... injectors) throws KeyManagerException {
  // for (Injector injector : userInjectors) {
  // injector.getInstance(KeyManager.class).storeIssuerParameters(
  // issuerParameters.getParametersUID(), issuerParameters);
  // }
  // for (Injector injector : injectors) {
  // injector.getInstance(KeyManager.class).storeIssuerParameters(
  // issuerParameters.getParametersUID(), issuerParameters);
  // }
  // }


  private RevocationAuthorityParametersFacade setupRevocationAuthorityParameters(int keyLength,
      URI revocationTechnology, RevocationAbcEngine revocationEngine) throws CryptoEngineException {

    // Setup revocation authority parameters
    Reference revocationInfoReference = new Reference();
    revocationInfoReference.setReferenceType(URI.create("url"));
    revocationInfoReference.getReferences().add(URI.create("https://www.example.org"));
    Reference nonRevocationEvidenceReference = new Reference();
    nonRevocationEvidenceReference.setReferenceType(URI.create("url"));
    nonRevocationEvidenceReference.getReferences().add(URI.create("https://www.example.org"));
    Reference nonRevocationUpdateReference = new Reference();
    nonRevocationUpdateReference.setReferenceType(URI.create("url"));
    nonRevocationUpdateReference.getReferences().add(URI.create("https://www.example.org"));
    RevocationAuthorityParameters revocationAuthorityParameters =
        revocationEngine.setupRevocationAuthorityParameters(keyLength, revocationTechnology,
            REVOCATION_PARAMETERS_UID, revocationInfoReference, nonRevocationEvidenceReference,
            nonRevocationUpdateReference);

    Assert.assertNotNull("RevocationInfoReference - should have been assigned",
        revocationAuthorityParameters.getRevocationInfoReference());
    Assert.assertNotNull("NonRevocationEvidenceReference - should have been  assigned",
        revocationAuthorityParameters.getNonRevocationEvidenceReference());
    Assert.assertNotNull("RevocationEvidenceUpdateReference - should have been  assigned",
        revocationAuthorityParameters.getNonRevocationEvidenceUpdateReference());

    // Setup the facade for the revocation authority parameters
    RevocationAuthorityParametersFacade raParametersFacade =
        new RevocationAuthorityParametersFacade(revocationAuthorityParameters);
    Assert.assertNotNull(raParametersFacade);
    Assert.assertNotNull("RevocationAuthorityParameters PublicKey - should be present",
        raParametersFacade.getPublicKey());

    return raParametersFacade;
  }



  public void setupUsers() throws JAXBException, SAXException, ConfigurationException,
      CredentialManagerException, IOException {

    TokenStorageIssuer universityTokenStorageManager =
        issuerInjector.getInstance(TokenStorageIssuer.class);

    for (int i = 0; i < numberOfUsers; i++) {
      // Generate a secret and load it to the appropriate places
      secretWrappers[i] = getSecretWrapper(systemParameters, rand);

      // Store secret to credential manager
      CredentialManager userCredentialManager =
          userInjectors[i].getInstance(CredentialManager.class);
      if (!secretWrappers[i].isSecretOnSmartcard()) {
        userCredentialManager.storeSecret(USERNAME, secretWrappers[i].getSecret());
      } else {
        CardStorage cardStorage = userInjectors[i].getInstance(CardStorage.class);

        int pin = 1234;
        cardStorage.addSmartcard(secretWrappers[i].getSoftwareSmartcard(), pin);
      }

      // Create a pseudonym
      pseudonymsWithMetadata[i] =
          createPseudonym(secretWrappers[i].getSecretUID(),
              userInjectors[i].getInstance(CryptoEngineUser.class), systemParameters);

      // Store the pseudonym in the user credential manager.
      userCredentialManager.storePseudonym(USERNAME, pseudonymsWithMetadata[i]);


      // This is a hack since the TokenManagerIssuer does not allow us to add a pseudonym.
      String primaryKey =
          DatatypeConverter.printBase64Binary(pseudonymsWithMetadata[i].getPseudonym()
              .getPseudonymValue());
      universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);
    }

  }

  private SecretWrapper getSecretWrapper(SystemParameters systemParameters, Random random)
      throws JAXBException, UnsupportedEncodingException, SAXException, ConfigurationException {

    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    // TODO remove this code as long as the system parameters are generated
    // Secret secret = (Secret) XmlUtils.getObjectFromXML(
    // this.getClass().getResourceAsStream(
    // "/eu/abc4trust/sampleXml/smartcard/sampleSecret_patras.xml"),
    // true);
    //
    // if (!spWrapper.getDHModulus().getValue()
    // .equals(secret.getSystemParameters().getPrimeModulus())
    // || !spWrapper
    // .getDHSubgroupOrder()
    // .getValue()
    // .equals(secret.getSystemParameters().getSubgroupOrder())) {

    // Secret needs to be newly generated
    SecretWrapper secretWrapper = new SecretWrapper(random, spWrapper.getSystemParameters());

    return secretWrapper;
    // TODO remove this code as long as the system parameters are generated
    // } else {
    // return new SecretWrapper(secret);
    // }
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



  public void setupIssuer(URI technology, String issuancePolicyName,
      String credentialSpecificationName) throws UnsupportedEncodingException, JAXBException,
      SAXException, KeyManagerException, CryptoEngineException, CredentialManagerException {


    // Setup issuance policies
    IssuancePolicy issuancePolicy =
        (IssuancePolicy) XmlUtils.getObjectFromXML(
            this.getClass().getResourceAsStream(issuancePolicyName), true);
    URI issuancePolicyUid = issuancePolicy.getCredentialTemplate().getIssuerParametersUID();


    // Load and store credential specification
    CredentialSpecification credSpec =
        (CredentialSpecification) XmlUtils.getObjectFromXML(
            this.getClass().getResourceAsStream(credentialSpecificationName), true);
    storeElementToKeyManagers(credSpec, null, issuerInjector, verifierInjector);


    // Generate issuer parameters.
    int maximalNumberOfAttributes = 10;
    IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

    IssuerParameters issuerParameters =
        issuerEngine.setupIssuerParameters(systemParameters, maximalNumberOfAttributes, technology,
            issuancePolicyUid, REVOCATION_PARAMETERS_UID, null);
    storeElementToKeyManagers(issuerParameters, userInjectors, issuerInjector, inspectorInjector,
        verifierInjector);
    storeIssuerParametersToSmartCards(issuerParameters);
  }

  private void storeIssuerParametersToSmartCards(IssuerParameters issuerParameters)
      throws CredentialManagerException {

    for (int i = 0; i < numberOfUsers; i++) {
      // Load secret and store it.
      CredentialManager userCredentialManager =
          userInjectors[i].getInstance(CredentialManager.class);

      SecretWrapper secretWrapper = secretWrappers[i];

      if (secretWrapper.isSecretOnSmartcard()) {
        // add smartcard to manager
        CardStorage cardStorage = userInjectors[i].getInstance(CardStorage.class);
        cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(), secretWrapper.getPin());

        // sign issuer attributes and add to smartcard
        secretWrapper.addIssuerParameters(issuerParameters, systemParameters);
      } else {
        userCredentialManager.storeSecret(USERNAME, secretWrapper.getSecret());
        // URI secretUid = secret.getSecretDescription().getSecretUID();
      }
    }
  }



  public void loginWithPseudonym(int numberOfThisUser, String policyName) throws Exception {
    PresentationToken t =
        this.loginWithPseudonym2(numberOfThisUser, policyName);
    assertNotNull(t);
  }

  private PresentationToken loginWithPseudonym2(int numberOfThisUser, String policyName) throws Exception {
    List<URI> chosenInspectors = new LinkedList<URI>();
    chosenInspectors.add(URI.create("http://patras.gr/inspector/pub_key_v1"));
    System.out.println("test, logingwithPseudonym, pre token generation");
    Pair<PresentationToken, PresentationPolicyAlternatives> p =
        issuanceHelper.createPresentationToken(USERNAME, userInjectors[numberOfThisUser],
            policyName, null, null);
    System.out.println("test, logingwithPseudonym, now we verify");
    return issuanceHelper.verify(issuerInjector, p.second, p.first);
  }



  public CredentialDescription issueAndStoreCredential(int numberOfThisUser,
      Map<String, Object> attributes, String credentialSpecificationName, String issuancePolicyName)
      throws Exception {
    return issuanceHelper.issueCredential(USERNAME, issuerInjector, userInjectors[numberOfThisUser],
        credentialSpecificationName, issuancePolicyName, attributes, null);
  }



  public PresentationToken logIntoCourseEvaluation(int numberOfThisUser,
      String presentationPolicyName) throws Exception {
    /*
     * Verify for poll. The user must have: 1) A non revoked university credential 2) A course
     * credential with the same matriculation number as the university credential 3) A certain
     * number of attendance credentials, which must be higher than a certain threshold 4) All
     * attendance credentials must have the same matriculation number as the university credential
     * 5) All attendance credentials must have a unique UID.
     */

    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(">> Verify against course evaluation of user " + numberOfThisUser + ".");
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

    // The verifier needs to retrieve the latest revocation information
    // in order to put in the UID in the presentation policy.
    RevocationInformation revocationInformation =
        issuanceHelper.getRevocationInformation(revocationInjector, REVOCATION_PARAMETERS_UID);

    return login(verifierInjector, userInjectors[numberOfThisUser], presentationPolicyName, revocationInformation);
  }


  private PresentationToken login(Injector verifierInjector, Injector userInjector,
      String presentationPolicyName,
      RevocationInformation revInfo) throws Exception {

    Pair<PresentationToken, PresentationPolicyAlternatives> p =
        issuanceHelper
            .createPresentationToken(USERNAME, userInjector, presentationPolicyName, revInfo, null);

    // Store all required cred specs in the verifier key manager.
    KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
    KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

    PresentationToken pt = p.first;
    assertNotNull(pt);
    for (CredentialInToken cit : pt.getPresentationTokenDescription().getCredential()) {
      verifierKeyManager.storeCredentialSpecification(cit.getCredentialSpecUID(),
          userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
    }
    verifierKeyManager.storeCurrentRevocationInformation(revInfo);
    return issuanceHelper.verify(verifierInjector, p.second, p.first);
  }


  public void updateNonRevocationEvidence(int numberOfThisUser) {

    if (numberOfThisUser < 0) {
      for (int i = 0; i < numberOfUsers; i++) {
        updateNonRevocationEvidence(i);
      }
    } else {
      UserAbcEngine userEngine = userInjectors[numberOfThisUser].getInstance(UserAbcEngine.class);

      try {
        userEngine.updateNonRevocationEvidence(USERNAME);
      } catch (CredentialManagerException e) {
        e.printStackTrace();
        Assert.fail("NRE cannot be updated.");
      }

      
      try {
    	KeyManager userKeyManager = userInjectors[numberOfThisUser].getInstance(KeyManager.class);
		userKeyManager.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
		userInjectors[numberOfThisUser].getInstance(CredentialManager.class).updateNonRevocationEvidence(USERNAME);
		KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
		verifierKeyManager.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
		KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
		issuerKeyManager.getLatestRevocationInformation(REVOCATION_PARAMETERS_UID);
		
      } catch (KeyManagerException | CredentialManagerException e) {
		e.printStackTrace();
        Assert.fail("KeyManager could not obtain latest revocation information.");
      }
      
//      VerifierAbcEngine verifierEngine = 
//      verifierInjector.getInstance(VerifierAbcEngine.class);
//      verifierEngine.
    }
  }

  public void testRevocation(int numberOfThisUser, CredentialDescription credDesc,
      String presentationPolicyName) throws Exception {

    // First verify that the credential is not revoked.
    UserAbcEngine userEngine = userInjectors[numberOfThisUser].getInstance(UserAbcEngine.class);
    URI credUid = credDesc.getCredentialUID();
    assertTrue(!userEngine.isRevoked(USERNAME, credUid));

    // Log in with the (still valid) credential
    if (presentationPolicyName != null) {
      PresentationToken pt = logIntoCourseEvaluation(numberOfThisUser, presentationPolicyName);
      assertNotNull(pt);
    }

    // Update the revocation evidence of this user
    Attribute revHandle = this.getRevocationHandle(credDesc);
    updateNonRevocationEvidence(numberOfThisUser);

    // Revoke credential and verify that it's been revoked
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(">> Revoking credential of user " + numberOfThisUser + " (credUID: "
        + credUid + ").");
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    issuanceHelper.revokeCredential(revocationInjector, REVOCATION_PARAMETERS_UID, revHandle);
    // retrieve accumulator value to verify after a second revocation of the same credential
    BigInt accumulatorValueAfterFirstRevocation =
        issuanceHelper.getAccumulatorValue(revocationInjector, REVOCATION_PARAMETERS_UID);
    assertTrue(userEngine.isRevoked(USERNAME, credUid));

    // try to log in with the revoked credential
    if (presentationPolicyName != null) {
      try {
        logIntoCourseEvaluation(numberOfThisUser, presentationPolicyName);
        Assert.fail("It should not be possible to log in using the revoked credential.");
      } catch (RuntimeException e) {
        // ok - expected to fail
        assertTrue(e.getMessage().contains("Cannot generate presentationToken"));
      }
    }

    // Revoke again to check whether accumulator is not changed in the process
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(">> Try to revoking the same credential again (user " + numberOfThisUser
        + " , credUID: " + credUid + ").");
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    issuanceHelper.revokeCredential(revocationInjector, REVOCATION_PARAMETERS_UID, revHandle);
    assertTrue(accumulatorValueAfterFirstRevocation.equals(issuanceHelper.getAccumulatorValue(
        revocationInjector, REVOCATION_PARAMETERS_UID)));

    // Update NRE - this will remove the credential from the list of credentials
    updateNonRevocationEvidence(numberOfThisUser);

    try {
      userEngine.isRevoked(USERNAME, credUid);
      Assert.fail("The credential should have been deleted.");
    } catch (CryptoEngineException e) {
      // This is expected as the credential has been deleted.
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


  public RevocationInformation getRevocationInformation() throws CryptoEngineException {
    return issuanceHelper.getRevocationInformation(revocationInjector, REVOCATION_PARAMETERS_UID);
  }


  private void setupEngines(int numberOfThisUser, PseudonymWithMetadata pwm,
      SecretWrapper secretWrapper, boolean tombolaTest, URI universityTechnology,
      URI courseTechnology, URI tombolaTechnology) throws KeyManagerException, JAXBException,
      UnsupportedEncodingException, SAXException, URISyntaxException, Exception,
      CredentialManagerException {



    // if (tombolaTest) {
    // // TODO : Verify New Tombola!!!
    //
    //
    // InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
    //
    //

    //
    // System.out.println("inspectorPublicKey : " + inspectorPublicKey.getPublicKeyUID());
    // userKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);
    // inspectorKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);
    //
    // int presentationTokenChoice = 0;
    // int pseudonymChoice = 0;
    // List<URI> chosenInspectors = new LinkedList<URI>();
    // chosenInspectors.add(inspectorPublicKeyUid);
    // // create presentation policy
    // Pair<PresentationToken, PresentationPolicyAlternatives> p =
    // issuanceHelper.createPresentationToken(userInjectors, userInjectors,
    // PRESENTATION_POLICY_PATRAS_TOMBOLA, new PolicySelector(presentationTokenChoice,
    // chosenInspectors, pseudonymChoice));
    // // verify..
    // issuanceHelper.verify(inspectorInjector, p.second(), p.first());
    //
    // // inspect..
    // List<Attribute> inspectedAttributes = inspectorEngine.inspect(p.first());
    // System.out.println("inspectedAttributes");
    // for (Attribute a : inspectedAttributes) {
    // System.out.println(" a " + a.getAttributeUID() + " : " + a.getAttributeValue());
    // }
    // }


  }



}

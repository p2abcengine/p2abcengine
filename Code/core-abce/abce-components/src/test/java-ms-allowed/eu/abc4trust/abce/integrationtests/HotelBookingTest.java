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
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class HotelBookingTest {
  private static final String USERNAME = "defaultUser";

    private static final String PRESENTATION_POLICY_ALTERNATIVES_HOTEL = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotelBooking.xml";
    private static final String ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditCard.xml";
    private static final String ISSUANCE_POLICY_PASSPORT = "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml";
    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
    private static final String CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml";
    private static final String CREDENTIAL_SPECIFICATION_PASSPORT = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml";

    private static final URI STATUS = URI.create("#status");
    private static final int SECURITY_CODE = 42;
    private static final int CARD_NUMBER = 555;
    private static final String SWISS_EXPRESS = "SwissExpress";
    private static final String NAME = "John";
    private static final String LASTNAME = "Doe";

    private static final URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
    private static final URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");

    private static final Logger logger = Logger
            .getLogger(HotelBookingTest.class.getCanonicalName());
    
    private static URI revocationTechnology = null;

    @Test
    public void hotelBookingTestIdemix() throws Exception{
      revocationTechnology = Helper.getRevocationTechnologyURI("cl");
      URI cl_technology = Helper.getSignatureTechnologyURI("cl");
      int keyLength = 1024;

      Entities entities = new Entities();

      entities.addEntity("CREDITCARD", cl_technology, true);
      entities.addEntity("STUDENTCARD", cl_technology, false);
      entities.addEntity("PASSPORT", cl_technology, false);
      entities.addEntity("USER");
      entities.addEntity("VERIFIER");
      entities.addEntity("INSPECTOR");

      runHotelBookingScenario(keyLength, entities);
    }
    
    @Test
    public void hotelBookingTestUProve() throws Exception{
      revocationTechnology = Helper.getRevocationTechnologyURI("cl");
      URI brands_technology = Helper.getSignatureTechnologyURI("brands");
      int keyLength = 1024;

      Entities entities = new Entities();

      entities.addEntity("CREDITCARD", brands_technology, true);
      entities.addEntity("STUDENTCARD", brands_technology, false);
      entities.addEntity("PASSPORT", brands_technology, false);
      entities.addEntity("USER");
      entities.addEntity("VERIFIER");
      entities.addEntity("INSPECTOR");

      runHotelBookingScenario(keyLength, entities);
    }
    
    @Test
    public void hotelBookingTestWithBridging() throws Exception{
      revocationTechnology = Helper.getRevocationTechnologyURI("cl");
      URI cl_technology = Helper.getSignatureTechnologyURI("cl");
      URI brands_technology = Helper.getSignatureTechnologyURI("brands");
      int keyLength = 1024;

      Entities entities = new Entities();

      entities.addEntity("CREDITCARD", cl_technology, true);
      entities.addEntity("STUDENTCARD", brands_technology, false);
      entities.addEntity("PASSPORT", cl_technology, false);
      entities.addEntity("USER");
      entities.addEntity("VERIFIER");
      entities.addEntity("INSPECTOR");

      runHotelBookingScenario(keyLength, entities);
    }
    
    private void runHotelBookingScenario(int keyLength, Entities entities) throws Exception{      
      Injector revocationInjector = setupRevocationInjector(keyLength);
      RevocationProxyAuthority revocationProxyAuthority = revocationInjector
          .getInstance(RevocationProxyAuthority.class);
      
      RevocationAuthorityParameters revocationAuthorityParameters = 
          setupRevocationAuthorityParameters(keyLength, revocationInjector);
      
      // Setup system by generating entities and system parameters
      Collection<Injector> injectors = createEntities(entities, revocationProxyAuthority);
      SystemParameters systemParameters = Entities.setupSystemParameters(entities, keyLength);

      List<Object> parametersList = new ArrayList<Object>();

      // Setup creditcard issuer
      URI credentialTechnology = entities.getTechnology("CREDITCARD");
      URI issuerParametersUID =
          getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD);      
      parametersList.add(setupIssuer(entities.getInjector("CREDITCARD"), systemParameters,
        credentialTechnology, issuerParametersUID, revParamsUid, 10));
      
      // Setup creditcard issuer
      credentialTechnology = entities.getTechnology("STUDENTCARD");
      issuerParametersUID =
          getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_STUDENT_CARD);
      parametersList.add(setupIssuer(entities.getInjector("STUDENTCARD"), systemParameters,
        credentialTechnology, issuerParametersUID, null, 10));
      
      // Setup creditcard issuer
      credentialTechnology = entities.getTechnology("PASSPORT");
      issuerParametersUID =
          getIssuanceParametersUIDFromIssuancePolicy(ISSUANCE_POLICY_PASSPORT);
      parametersList.add(setupIssuer(entities.getInjector("PASSPORT"), systemParameters,
        credentialTechnology, issuerParametersUID, null, 10));
      
      // Parameters for verifier parameters
      credentialTechnology = Helper.getSignatureTechnologyURI("cl");
      issuerParametersUID = URI.create("vp:rangeProof");
      parametersList.add(setupIssuer(entities.getInjector("STUDENTCARD"), systemParameters,
        credentialTechnology, issuerParametersUID, null, 0));
      
      // setup inspector public key.
      InspectorAbcEngine inspectorEngine =
          entities.getInjector("INSPECTOR").getInstance(InspectorAbcEngine.class);
      InspectorPublicKey inspectorPubKey =
              inspectorEngine.setupInspectorPublicKey(systemParameters,
                      CryptoUriUtil.getIdemixMechanism(),
                      HotelBookingTest.INSPECTOR_URI,
                      null);
      
      parametersList.add(inspectorPubKey);
      
      // Store all issuer parameters to all key managers
      entities.storePublicParametersToKeyManagers(parametersList);
      
      //store credentialSpecifications
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_STUDENT_CARD);
      storeCredentialSpecificationToKeyManagers(injectors, CREDENTIAL_SPECIFICATION_PASSPORT);      
      
      addRevocationToIssuers(entities, revocationAuthorityParameters);
      
      Injector governmentInjector = entities.getInjector("PASSPORT");
      Injector userInjector = entities.getInjector("USER");
      Injector studentInjector = entities.getInjector("STUDENTCARD");
      Injector bankInjector = entities.getInjector("CREDITCARD");
      Injector hotelInjector = entities.getInjector("VERIFIER");
      Injector inspectorInjector = entities.getInjector("INSPECTOR");
      
      IssuanceHelper issuanceHelper = new IssuanceHelper();
      
      VerifierParameters verifierParameter = entities.getInjector("USER").getInstance(VerifierAbcEngine.class).createVerifierParameters(systemParameters);

      
      // Step 1. Get passport.
      logger.info("Get passport.");
      URI passportCredentialUID = this.issueAndStorePassport(governmentInjector, userInjector,
              issuanceHelper, verifierParameter).getCredentialUID();

      // Step 2. Get student id.
      logger.info("Get student id.");
      URI studentcardCredentialUID = this.issueAndStoreStudentId(studentInjector, userInjector,
              issuanceHelper, verifierParameter).getCredentialUID();


      // Step 3. Get credit card using id and student card
      logger.info("Get credit card.");
      this.issueAndStoreCreditCard(bankInjector, userInjector,
              issuanceHelper, verifierParameter);

      // Step 4a. Book a hotel room using passport and credit card. This uses
      // the first alternative of the presentation policy.
      logger.info("Verify.");
      PresentationToken pt = this.bookHotelRoomUsingPassportAndCreditcard(
              issuanceHelper, hotelInjector, userInjector, revParamsUid, verifierParameter, passportCredentialUID);

      // Step 4b. Book a hotel room using passport and credit card. This uses
      // the second alternative of the presentation policy.
      pt = this.bookHotelRoomUsingStudentcardPassportAndCreditcard(
              hotelInjector, userInjector, issuanceHelper, revParamsUid, verifierParameter, studentcardCredentialUID);

      // Step 5. Inspect credit card data because of no-show.
      if (inspectorInjector != null) {
          this.inspectCreditCard(bankInjector, inspectorInjector, pt);
      }
      
      // Step 4c. Booking a hotel room using passport and credit card fails
      // because customer is blacklisted by hotel.
      // Not implemented yet.
      //this.failBookingHotelRoomUsingPassportAndCreditcard(hotelInjector,
      //        userInjector, issuanceHelper, verifierParameter);
    }
    
    private Injector setupRevocationInjector(int keyLength) throws Exception{
      // Generate revocation parameters.      
      Injector revocationInjector = Guice
              .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                      CryptoEngine.IDEMIX));
      
      KeyManager revocationKeyManager = revocationInjector.getInstance(KeyManager.class);
      SystemParameters systemParameters = null;
      if(keyLength == 1024){
        systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
      }else{
        systemParameters = SystemParametersUtil.getDefaultSystemParameters_2048();
      }
      revocationKeyManager.storeSystemParameters(systemParameters);             
      
      return revocationInjector;    
    }
    
    private RevocationAuthorityParameters setupRevocationAuthorityParameters(int keyLength, 
                                                                             Injector revocationInjector) 
                                                                                 throws Exception{
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
      return revocationAuthorityParameters;
    }
    
    private void addRevocationToIssuers(Entities entities, RevocationAuthorityParameters revAuthParams) 
        throws KeyManagerException{
      for(Injector injector: entities.getInjectors()){
        KeyManager keyManager = injector.getInstance(KeyManager.class);
        keyManager.storeRevocationAuthorityParameters(IntegrationTestUtil.REVOCATION_PARAMETERS_UID, revAuthParams);
      }
    }
    
    private Collection<Injector> createEntities(Entities entities, RevocationProxyAuthority revProxy) {
      
      // Assert that required entities are present
      assert (entities.contains("CREDITCARD"));
      assert (entities.contains("STUDENTCARD"));
      assert (entities.contains("PASSPORT"));
      assert (entities.contains("USER"));
      assert (entities.contains("VERIFIER"));
      assert (entities.contains("INSPECTOR"));
      assert (!entities.contains("REVOCATION")); //do not contain revocation - should be separate

      entities.initInjectors(revProxy);

      return entities.getInjectors();
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

    private CredentialDescription issueAndStorePassport(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, VerifierParameters verifierParameter)
                    throws Exception {
        Map<String, Object> passportAtts = this.populatePassportAttributes();
        return issuanceHelper.issueCredential(USERNAME, governmentInjector, userInjector,
                CREDENTIAL_SPECIFICATION_PASSPORT, ISSUANCE_POLICY_PASSPORT,
                passportAtts, verifierParameter);
    }

    private Map<String, Object> populatePassportAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("Name", NAME);
        att.put("LastName", LASTNAME);
        att.put(REVOCATION_HANDLE_STR,
                "http://admin.ch/passport/revocation/parameters");
        att.put("PassportNumber", 895749);
        att.put("Issued", "2012-02-06Z");
        att.put("Expires", "2022-02-06Z");
        att.put("IssuedBy", "admin.ch");
        return att;
    }

    private CredentialDescription  issueAndStoreStudentId(Injector univsersityInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, VerifierParameters verifierParameter)
                    throws Exception {
        Map<String, Object> atts = this
                .populateStudentIdIssuerAttributes();
        return issuanceHelper.issueCredential(USERNAME, univsersityInjector, userInjector,
                CREDENTIAL_SPECIFICATION_STUDENT_CARD,
                ISSUANCE_POLICY_STUDENT_CARD, atts, verifierParameter);
    }

    private Map<String, Object> populateStudentIdIssuerAttributes() {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put("Name", NAME);
        atts.put("LastName", LASTNAME);
        atts.put(REVOCATION_HANDLE_STR,
                "http://admin.ch/passport/revocation/parameters");
        atts.put("StudentNumber", 345);
        atts.put("Issued", "2012-02-02Z");
        atts.put("Expires", "2022-02-02Z");
        atts.put("IssuedBy", "ethz.ch");
        return atts;
    }

    private void issueAndStoreCreditCard(Injector bankInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, VerifierParameters verifierParameter)
                    throws Exception {
        Map<String, Object> atts = this.populateCreditCardIssuerAttributes();
        issuanceHelper.issueCredential(USERNAME, bankInjector, userInjector,
                CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD,
                ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD, atts, verifierParameter);
    }

    private Map<String, Object> populateCreditCardIssuerAttributes()
            throws Exception {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put(REVOCATION_HANDLE_STR,
                new BigInteger("123123123"));
        atts.put("CardType", SWISS_EXPRESS);
        atts.put("Status", STATUS);
        atts.put("SecurityCode", SECURITY_CODE);
        atts.put("CardNumber", CARD_NUMBER);
        return atts;
    }

    private PresentationToken bookHotelRoomUsingPassportAndCreditcard(
            IssuanceHelper issuanceHelper, Injector hotelInjector,
            Injector userInjector, URI revParamsUid, VerifierParameters verifierParameter,
            URI passportCredentialUID) throws Exception {
        
        
        // find passport credential uid and use that instead of presentationTokenChoice 
        return this.bookHotel(issuanceHelper, hotelInjector, userInjector,
                passportCredentialUID, revParamsUid, verifierParameter);
    }

    private PresentationToken bookHotelRoomUsingStudentcardPassportAndCreditcard(
            Injector hotelInjector, Injector userInjector,
            IssuanceHelper issuanceHelper, URI revParamsUid, VerifierParameters verifierParameter,
            URI studentcardCredentialUID) throws Exception {
        
        return this.bookHotel(issuanceHelper, hotelInjector, userInjector,
                studentcardCredentialUID, revParamsUid, verifierParameter);
    }

    private PresentationToken bookHotel(IssuanceHelper issuanceHelper,
            Injector hotelInjector, Injector userInjector,
            final URI credentialChoice,
            URI revParamsUid, VerifierParameters verifierParameter)
                    throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        chosenInspectors.add(HotelBookingTest.INSPECTOR_URI);
        VerifierAbcEngine hotelEngine = hotelInjector
                .getInstance(VerifierAbcEngine.class);
        RevocationInformation revocationInformation = null;
        if (revParamsUid != null) {
            try {
                revocationInformation = hotelEngine
                        .getLatestRevocationInformation(revParamsUid);
            } catch (CryptoEngineException ex) {
                revocationInformation = null;
            }
        }
        
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createSpecificPresentationToken(USERNAME, userInjector,
                        PRESENTATION_POLICY_ALTERNATIVES_HOTEL, credentialChoice,
                        revocationInformation, verifierParameter);

        return issuanceHelper.verify(hotelInjector, p.second, p.first);

    }

    private void failBookingHotelRoomUsingPassportAndCreditcard(
            Injector hotelInjector, Injector userInjector,
            IssuanceHelper issuanceHelper, VerifierParameters verifierParameter) throws Exception {
      // TODO(enr): Fix this
      org.junit.Assert.fail();
      /*
        int presentationTokenChoice = 0;
        int pseudonymChoice = 1;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(USERNAME, userInjector, userInjector,
                        PRESENTATION_POLICY_ALTERNATIVES_HOTEL,
                        new IdentitySelectionPrinter(
                                new PolicySelector(presentationTokenChoice,
                                        pseudonymChoice)));
        PresentationToken pt = p.first();
        // TODO: Verifier driven revocation is not yet implemented.
        // The user should not be able to create a presentation token as
        // his passport number is on the Hotel blacklist.
        // assertNull(pt);
        assertNotNull(pt);
        */
    }

    private void inspectCreditCard(Injector bankInjector,
            Injector inspectorInjector,
            PresentationToken pt) throws Exception {
        CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        List<Attribute> attributes = engine.inspect(pt);
        assertEquals(1, attributes.size());
        Attribute attribute = attributes.get(0);
        assertEquals(URI.create("CardNumber"), attribute
                .getAttributeDescription().getType());
        assertEquals(BigInteger.valueOf(555), attribute.getAttributeValue());

    }
}

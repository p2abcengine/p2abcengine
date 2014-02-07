//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class BridgingTest {

    private static final String PRESENTATION_POLICY_ALTERNATIVES_BRIDGING = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesBridging.xml";

    private static final String ISSUANCE_POLICY_PASSPORT = "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml";
    private static final String CREDENTIAL_SPECIFICATION_PASSPORT = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml";
    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml";

    private static final String NAME = "John";
    private static final String LASTNAME = "Doe";

    private static final URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");

   // @Ignore
    @Test
    public void bridgingTestUProve() throws Exception {
        this.setupIdenticalEngines(IssuerCryptoEngine.UPROVE);
    }
    // @Ignore
    @Test
    public void bridgingTestIdemix() throws Exception {
        this.setupIdenticalEngines(IssuerCryptoEngine.IDEMIX);
    }

    public void setupIdenticalEngines(IssuerCryptoEngine chosenEngine) throws Exception{

        // The Guice injector is configured to return the same instance for
        // every invocation with the same class type. So the storage of
        // credentials is all done as side-effects.

        UProveUtils uproveUtils = new UProveUtils();
        //Construct issuers
        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        chosenEngine, uproveUtils.getIssuerServicePort()));
        Injector universityInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        chosenEngine, uproveUtils.getIssuerServicePort()));

        //Construct user
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1987), uproveUtils.getUserServicePort()));

        //Construct verifier
        Injector hotelInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort()));

        Injector inspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
                uproveUtils.getInspectorServicePort()));



        int keyLength = 2048;

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);

        CryptoEngineInspector inspectorEngine = null;
        if (inspectorInjector != null) {
            inspectorEngine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        }

        KeyManager inspectorKeyManager = null;
        if (inspectorInjector != null) {
            inspectorKeyManager = inspectorInjector.getInstance(KeyManager.class);
        }

        KeyManager governmentKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager universityKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager hotelKeyManager = hotelInjector
                .getInstance(KeyManager.class);



        // Generate system parameters.
        SystemParameters systemParameters = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());
        } else {
            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getUproveMechanism());

            Injector idemixInjector = Guice
                    .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                            IssuerCryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));
            IssuerAbcEngine idemixIssuer = idemixInjector.getInstance(IssuerAbcEngine.class);
            SystemParameters idemixSystemParameters = idemixIssuer
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

            systemParameters.getAny().addAll(idemixSystemParameters.getAny());
        }

        governmentKeyManager.storeSystemParameters(systemParameters);
        universityKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        hotelKeyManager.storeSystemParameters(systemParameters);
        inspectorKeyManager.storeSystemParameters(systemParameters);


        // Setup issuance policies.
        IssuancePolicy passportIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_PASSPORT), true);
        URI passportIssuancePolicyUid = passportIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        IssuancePolicy studentCardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_STUDENT_CARD), true);
        URI studentCardIssuancePolicyUid = studentCardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification passportCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_PASSPORT), true);
        CredentialSpecification credentialSpecificationStudent = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_STUDENT_CARD), true);

        // Store credential specifications.
        governmentKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        userKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        hotelKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        universityKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        userKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        hotelKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI engineType = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            engineType = URI.create("Idemix");
        } else {
            engineType = URI.create("Uprove");
        }

        URI revocationId = new URI("revocationUID1");
        IssuerParameters governementPassportIssuerParameters = governmentEngine
                .setupIssuerParameters(passportCredSpec, systemParameters,
                        passportIssuancePolicyUid, hash, engineType, revocationId, null);

        revocationId = new URI("revocationUID2");
        IssuerParameters universityStudentCardIssuerParameters = universityEngine
                .setupIssuerParameters(
                        credentialSpecificationStudent, systemParameters,
                        studentCardIssuancePolicyUid, hash, engineType, revocationId, null);

        // store issuance parameters for government and user.
        governmentKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);
        userKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);
        hotelKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);

        // store parameters for university and user:
        universityKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);
        userKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);

        if (inspectorInjector != null) {
            InspectorPublicKey inspectorPubKey =
                    inspectorEngine.setupInspectorPublicKey(keyLength,
                            CryptoUriUtil.getIdemixMechanism(),
                            BridgingTest.INSPECTOR_URI);
            inspectorKeyManager.storeInspectorPublicKey(BridgingTest.INSPECTOR_URI, inspectorPubKey);
            userKeyManager.storeInspectorPublicKey(
                    BridgingTest.INSPECTOR_URI, inspectorPubKey);
            hotelKeyManager.storeInspectorPublicKey(
                    BridgingTest.INSPECTOR_URI, inspectorPubKey);
        }

        this.runTests(governmentInjector, userInjector, universityInjector, hotelInjector, issuanceHelper);
    }

    //@Ignore
    @Test
    public void bridgingTestDifferentIssuers() throws Exception {


        // The Guice injector is configured to return the same instance for
        // every invocation with the same class type. So the storage of
        // credentials is all done as side-effects.

        UProveUtils uproveUtils = new UProveUtils();
        //Construct issuers
        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        IssuerCryptoEngine.IDEMIX, uproveUtils.getIssuerServicePort()));
        Injector universityInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        IssuerCryptoEngine.UPROVE, uproveUtils.getIssuerServicePort()));

        //Construct user
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1987), uproveUtils.getUserServicePort()));

        //Construct verifier
        Injector hotelInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort()));

        Injector inspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
                uproveUtils.getInspectorServicePort()));

        // Create URIs.
        // int idemixKeyLength = 1024; // TODO: define the security level &
        // revocation
        int idemixKeyLength = 2048;
        int uproveKeylength = 2048;


        IssuanceHelper issuanceHelper = new IssuanceHelper();

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);

        CryptoEngineInspector inspectorEngine = null;
        if (inspectorInjector != null) {
            inspectorEngine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        }

        KeyManager inspectorKeyManager = null;
        if (inspectorInjector != null) {
            inspectorKeyManager = inspectorInjector.getInstance(KeyManager.class);
        }

        KeyManager governmentKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager universityKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager hotelKeyManager = hotelInjector
                .getInstance(KeyManager.class);



        // Generate system parameters.
        SystemParameters sysParam = SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(
                        idemixKeyLength, uproveKeylength);
        /*
        SystemParameters idemixParameters = governmentEngine
                .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

		SystemParameters uproveParameters = universityEngine
                .setupSystemParameters(2048, CryptoUriUtil.getUproveMechanism());
         */
        governmentKeyManager.storeSystemParameters(sysParam);



        universityKeyManager.storeSystemParameters(sysParam);

        userKeyManager.storeSystemParameters(sysParam);
        hotelKeyManager.storeSystemParameters(sysParam);
        if (inspectorKeyManager != null) {
            inspectorKeyManager.storeSystemParameters(sysParam);
        }

        // Setup issuance policies.
        IssuancePolicy passportIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_PASSPORT), true);
        URI passportIssuancePolicyUid = passportIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        IssuancePolicy studentCardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_STUDENT_CARD), true);
        URI studentCardIssuancePolicyUid = studentCardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();


        // Load credential specifications.
        CredentialSpecification passportCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_PASSPORT), true);
        CredentialSpecification credentialSpecificationStudent = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_STUDENT_CARD), true);


        // Store credential specifications.
        governmentKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        userKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        hotelKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        universityKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        userKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        hotelKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI revocationId = new URI("revocationUID1");
        IssuerParameters governementPassportIssuerParameters = governmentEngine
                .setupIssuerParameters(passportCredSpec, sysParam,
                        passportIssuancePolicyUid, hash, URI.create("Idemix"), revocationId, null);

        revocationId = new URI("revocationUID2");
        IssuerParameters universityStudentCardIssuerParameters = universityEngine
                .setupIssuerParameters(
                        credentialSpecificationStudent, sysParam,
                        studentCardIssuancePolicyUid, hash, URI.create("uprove"), revocationId, null);

        // store issuance parameters for government and user.
        governmentKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);
        userKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);
        hotelKeyManager.storeIssuerParameters(passportIssuancePolicyUid,
                governementPassportIssuerParameters);

        // store parameters for university and user:
        universityKeyManager.storeIssuerParameters(
                studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);
        userKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);

        if (inspectorInjector != null) {
            InspectorPublicKey inspectorPubKey =
                    inspectorEngine.setupInspectorPublicKey(idemixKeyLength,
                            CryptoUriUtil.getIdemixMechanism(),
                            BridgingTest.INSPECTOR_URI);
            inspectorKeyManager.storeInspectorPublicKey(BridgingTest.INSPECTOR_URI, inspectorPubKey);
            userKeyManager.storeInspectorPublicKey(
                    BridgingTest.INSPECTOR_URI, inspectorPubKey);
            hotelKeyManager.storeInspectorPublicKey(
                    BridgingTest.INSPECTOR_URI, inspectorPubKey);
        }


        this.runTests(governmentInjector, userInjector, universityInjector, hotelInjector, issuanceHelper);
    }

    private void runTests(Injector governmentInjector, Injector userInjector,
            Injector universityInjector, Injector hotelInjector, IssuanceHelper issuanceHelper) throws Exception{

        // Step 1. Get passport.
        System.out.println(">> Get passport.");
        this.issueAndStorePassport(governmentInjector, userInjector,
                issuanceHelper);

        //     userInjector.getInstance(CryptoEngineChoice.class).chosenEngine = CryptoEngine.UPROVE;
        // Step 2. Get student id.
        System.out.println(">> Get student id.");
        this.issueAndStoreStudentId(universityInjector, userInjector,
                issuanceHelper);

        // Step 4a. Book a hotel room using passport and credit card. This uses
        // the first alternative of the presentation policy.
        System.out.println(">> Verify.");
        @SuppressWarnings("unused")
        PresentationToken pt = this.bookHotelRoomUsingPassportAndCreditcard(
                issuanceHelper, hotelInjector, userInjector);

        // Step 4b. Booking a hotel room using passport and credit card fails
        // because customer is blacklisted by hotel.
        this.failBookingHotelRoomUsingPassportAndStudentCard(hotelInjector,
                userInjector, issuanceHelper);

    }

    private void issueAndStorePassport(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        Map<String, Object> passportAtts = this.populatePassportAttributes();
        issuanceHelper.issueCredential(governmentInjector, userInjector,
                CREDENTIAL_SPECIFICATION_PASSPORT, ISSUANCE_POLICY_PASSPORT,
                passportAtts);
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

    private void issueAndStoreStudentId(Injector univsersityInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        Map<String, Object> atts = this
                .populateStudentIdIssuerAttributes();
        issuanceHelper.issueCredential(univsersityInjector, userInjector,
                CREDENTIAL_SPECIFICATION_STUDENT_CARD,
                ISSUANCE_POLICY_STUDENT_CARD, atts);
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


    private PresentationToken bookHotelRoomUsingPassportAndCreditcard(
            IssuanceHelper issuanceHelper, Injector hotelInjector,
            Injector userInjector) throws Exception {
        int presentationTokenChoice = 0;
        return this.bookHotel(issuanceHelper, hotelInjector, userInjector,
                presentationTokenChoice, presentationTokenChoice);
    }


    private PresentationToken bookHotel(IssuanceHelper issuanceHelper,
            Injector hotelInjector, Injector userInjector,
            final int presentationTokenChoice, int pseudonymChoice)
                    throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        chosenInspectors.add(URI
                .create("http://thebestbank.com/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        //                        PRESENTATION_POLICY_ALTERNATIVES_HOTEL,
                        PRESENTATION_POLICY_ALTERNATIVES_BRIDGING,
                        new PolicySelector(presentationTokenChoice,
                                chosenInspectors, pseudonymChoice));

        // Store all required cred specs in the verifier key manager.
        KeyManager hotelKeyManager = hotelInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

        PresentationToken pt = p.first();
        assertNotNull(pt);
        for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
            hotelKeyManager.storeCredentialSpecification(
                    cit.getCredentialSpecUID(),
                    userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
        }

        return issuanceHelper.verify(hotelInjector, p.second(), p.first());
    }

    private void failBookingHotelRoomUsingPassportAndStudentCard(
            Injector hotelInjector, Injector userInjector,
            IssuanceHelper issuanceHelper) throws Exception {
        int presentationTokenChoice = 0;
        int pseudonymChoice = 1;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        //                        PRESENTATION_POLICY_ALTERNATIVES_HOTEL,
                        PRESENTATION_POLICY_ALTERNATIVES_BRIDGING,
                        new PolicySelector(presentationTokenChoice,
                                pseudonymChoice));
        PresentationToken pt = p.first();
        // TODO: The user should not be able to create a presentation token as
        // his passport number is on the Hotel blacklist.
        // assertNull(pt);
        assertNotNull(pt);
    }


}

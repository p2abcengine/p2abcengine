//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.integrationtests;

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.ui.idSelection.IdentitySelectionPrinter;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class HotelBookingTest {

    private static final String PRESENTATION_POLICY_ALTERNATIVES_HOTEL = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotelBooking.xml";
    private static final String ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditCard.xml";
    private static final String CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml";
    private static final String ISSUANCE_POLICY_PASSPORT = "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml";
    private static final String CREDENTIAL_SPECIFICATION_PASSPORT = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml";


    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml";
    private static final URI STATUS = URI.create("#status");
    private static final int SECURITY_CODE = 42;
    private static final int CARD_NUMBER = 555;
    private static final String SWISS_EXPRESS = "SwissExpress";
    private static final String NAME = "John";
    private static final String LASTNAME = "Doe";

    private static final URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");

    private static final Logger logger = Logger
            .getLogger(HotelBookingTest.class.getCanonicalName());

    @Test
    @Ignore
    public void hotelTestWithBridging() throws Exception {
        // The Guice injector is configured to return the same instance for
        // every invocation with the same class type. So the storage of
        // credentials is all done as side-effects.
        IssuerCryptoEngine cryptoEngine = IssuerCryptoEngine.IDEMIX;

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector universityInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector bankInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1986), IssuerCryptoEngine.UPROVE, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1987), UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        Injector hotelInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector inspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
                cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));


        // Create URIs.
        int keyLength = 2048;
        URI cryptoMechanism = new URI("urn:abc4trust:1.0:algorithm:bridging");

        this.runTestWithEngines(governmentInjector, universityInjector,
                bankInjector, userInjector, hotelInjector, inspectorInjector, keyLength,
                cryptoMechanism, revocationInjector);
    }


    @Test
    public void hotelTestWithIdemix() throws Exception {

        // The Guice injector is configured to return the same instance for
        // every invocation with the same class type. So the storage of
        // credentials is all done as side-effects.
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector bankInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1986),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        Injector userInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1987),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector hotelInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        Injector inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        // Create URIs.
        int keyLength = 1024;
        URI cryptoMechanism = new URI("urn:abc4trust:1.0:algorithm:idemix");

        this.runTestWithEngines(governmentInjector, universityInjector,
                bankInjector, userInjector, hotelInjector, inspectorInjector, keyLength,
                cryptoMechanism, revocationInjector);
    }

    @Test
    @Ignore //The tests involve inspection, which require bridging, hence UProve only will fail
    public void hotelTestWithUProve() throws Exception {
        // The Guice injector is configured to return the same instance for
        // every invocation with the same class type. So the storage of
        // credentials is all done as side-effects.
        CryptoEngine cryptoEngine = CryptoEngine.UPROVE;

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        UProveUtils.UPROVE_COMMON_PORT));
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        UProveUtils.UPROVE_COMMON_PORT));
        Injector bankInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1986), UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), UProveUtils.UPROVE_COMMON_PORT));
        Injector hotelInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        UProveUtils.UPROVE_COMMON_PORT));

        // Create URIs.
        int keyLength = 1024;
        URI cryptoMechanism = new URI("urn:abc4trust:1.0:algorithm:uprove");

        this.runTestWithEngines(governmentInjector, universityInjector,
                bankInjector, userInjector, hotelInjector, null, keyLength,
                cryptoMechanism, revocationInjector);
    }

    private void runTestWithEngines(Injector governmentInjector,
            Injector universityInjector,
            Injector bankInjector,
            Injector userInjector,
            Injector hotelInjector,
            Injector inspectorInjector,
            int keyLength,
            URI cryptoMechanism,
            Injector revocationInjector)
                    throws URISyntaxException, KeyManagerException, JAXBException,
                    UnsupportedEncodingException, SAXException, Exception {
        IssuanceHelper issuanceHelper = new IssuanceHelper();

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        IssuerAbcEngine bankEngine = bankInjector
                .getInstance(IssuerAbcEngine.class);
        CryptoEngineInspector inspectorEngine = null;
        if (inspectorInjector != null) {
            inspectorEngine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        }


        KeyManager governmentKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager universityKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager bankKeyManager = bankInjector.getInstance(KeyManager.class);
        KeyManager hotelKeyManager = hotelInjector
                .getInstance(KeyManager.class);

        KeyManager inspectorKeyManager = null;
        if (inspectorInjector != null) {
            inspectorKeyManager = inspectorInjector.getInstance(KeyManager.class);
        }
        KeyManager revocationKeyManager = null;
        if (revocationInjector != null) {
            revocationKeyManager = revocationInjector.getInstance(KeyManager.class);
        }

        @SuppressWarnings("unused")
        CredentialManager credManager = userInjector
        .getInstance(CredentialManager.class);

        // Generate system parameters.
        SystemParameters systemParameters = null;
        SystemParameters uproveParams = null;

        if(cryptoMechanism.equals(new URI("urn:abc4trust:1.0:algorithm:bridging"))){
            URI idemix = new URI("urn:abc4trust:1.0:algorithm:idemix");
            URI uprove = new URI("urn:abc4trust:1.0:algorithm:uprove");
            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, idemix);

            governmentKeyManager.storeSystemParameters(systemParameters);
            userKeyManager.storeSystemParameters(systemParameters);
            universityKeyManager.storeSystemParameters(systemParameters);


            hotelKeyManager.storeSystemParameters(systemParameters);
            if (inspectorKeyManager != null) {
                inspectorKeyManager.storeSystemParameters(systemParameters);
            }
            uproveParams= bankEngine.setupSystemParameters(keyLength, uprove);
            bankKeyManager.storeSystemParameters(uproveParams);
            revocationKeyManager.storeSystemParameters(systemParameters);
        } else {

            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, cryptoMechanism);

            governmentKeyManager.storeSystemParameters(systemParameters);
            userKeyManager.storeSystemParameters(systemParameters);
            universityKeyManager.storeSystemParameters(systemParameters);
            bankKeyManager.storeSystemParameters(systemParameters);
            hotelKeyManager.storeSystemParameters(systemParameters);
            if (inspectorKeyManager != null) {
                inspectorKeyManager.storeSystemParameters(systemParameters);
            }
            if (revocationKeyManager != null) {
                revocationKeyManager.storeSystemParameters(systemParameters);
            }

        }

        // Setup issuance policies.
        IssuancePolicy passportIssuancePolicy = (IssuancePolicy) this
                .loadResources(ISSUANCE_POLICY_PASSPORT);

        URI passportIssuancePolicyUid = passportIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        IssuancePolicy studentCardIssuancePolicy = (IssuancePolicy) this
                .loadResources(ISSUANCE_POLICY_STUDENT_CARD);
        URI studentCardIssuancePolicyUid = studentCardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        IssuancePolicy creditCardIssuancePolicy = (IssuancePolicy) this
                .loadResources(ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD);
        URI creditCardIssuancePolicyUid = creditCardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification passportCredSpec = (CredentialSpecification) this
                .loadResources(CREDENTIAL_SPECIFICATION_PASSPORT);
        CredentialSpecification credentialSpecificationStudent = (CredentialSpecification) this
                .loadResources(CREDENTIAL_SPECIFICATION_STUDENT_CARD);

        CredentialSpecification credentialSpecificationCreditcard = (CredentialSpecification) this
                .loadResources(CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD);

        // Store credential specifications.
        governmentKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        userKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        hotelKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);

        inspectorKeyManager.storeCredentialSpecification(
                passportCredSpec.getSpecificationUID(), passportCredSpec);


        universityKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        userKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        bankKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        hotelKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        inspectorKeyManager.storeCredentialSpecification(
                credentialSpecificationStudent.getSpecificationUID(),
                credentialSpecificationStudent);

        bankKeyManager.storeCredentialSpecification(
                credentialSpecificationCreditcard.getSpecificationUID(),
                credentialSpecificationCreditcard);

        userKeyManager.storeCredentialSpecification(
                credentialSpecificationCreditcard.getSpecificationUID(),
                credentialSpecificationCreditcard);

        hotelKeyManager.storeCredentialSpecification(
                credentialSpecificationCreditcard.getSpecificationUID(),
                credentialSpecificationCreditcard);

        inspectorKeyManager.storeCredentialSpecification(
                credentialSpecificationCreditcard.getSpecificationUID(),
                credentialSpecificationCreditcard);

        // Generate revocation parameters.
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("example.org"));

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI revParamsUid = new URI("revocationUID3");

        IssuerParameters governementPassportIssuerParameters = null;
        IssuerParameters universityStudentCardIssuerParameters = null;
        IssuerParameters bankCreditcardIssuerParameters = null;
        RevocationAuthorityParameters revocationAuthorityParameters = null;
        URI revocationId = new URI("revocationUID1");

        if(cryptoMechanism.equals(new URI("urn:abc4trust:1.0:algorithm:bridging"))){

            governementPassportIssuerParameters = governmentEngine
                    .setupIssuerParameters(passportCredSpec, systemParameters,
                            passportIssuancePolicyUid, hash, CryptoUriUtil.getIdemixMechanism(), revocationId, null);

            revocationId = new URI("revocationUID2");
            universityStudentCardIssuerParameters = universityEngine
                    .setupIssuerParameters(credentialSpecificationStudent,
                            systemParameters, studentCardIssuancePolicyUid, hash,
                            URI.create("uprove"), revParamsUid, null);


            revocationAuthorityParameters = revocationEngine
                    .setupRevocationAuthorityParameters(keyLength, cryptoMechanism,
                            revParamsUid, revocationInfoReference,
                            nonRevocationEvidenceReference,
                            nonRrevocationUpdateReference);



            revocationId = new URI("revocationUID3");
            bankCreditcardIssuerParameters = bankEngine.setupIssuerParameters(
                    credentialSpecificationCreditcard, uproveParams,
                    creditCardIssuancePolicyUid, hash, CryptoUriUtil.getUproveMechanism(), revocationId, null);
        } else {
            governementPassportIssuerParameters = governmentEngine
                    .setupIssuerParameters(passportCredSpec, systemParameters,
                            passportIssuancePolicyUid, hash, cryptoMechanism, revocationId, null);

            revocationId = new URI("revocationUID2");
            universityStudentCardIssuerParameters = universityEngine
                    .setupIssuerParameters(
                            credentialSpecificationStudent, systemParameters,
                            studentCardIssuancePolicyUid, hash, cryptoMechanism, revocationId, null);

            revocationId = new URI("revocationUID3");
            bankCreditcardIssuerParameters = bankEngine
                    .setupIssuerParameters(
                            credentialSpecificationCreditcard, systemParameters,
                            creditCardIssuancePolicyUid, hash, cryptoMechanism, revocationId, null);

        }

        // set human readable names
        FriendlyDescription f = new FriendlyDescription();
        f.setLang("en");
        f.setValue("Swiss Government");
        governementPassportIssuerParameters.getFriendlyIssuerDescription().add(f);
        f = new FriendlyDescription();
        f.setLang("en");
        f.setValue("ACME University");
        universityStudentCardIssuerParameters.getFriendlyIssuerDescription().add(f);
        f = new FriendlyDescription();
        f.setLang("en");
        f.setValue("The Best Bank Inc.");
        bankCreditcardIssuerParameters.getFriendlyIssuerDescription().add(f);

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
        bankKeyManager.storeIssuerParameters(studentCardIssuancePolicyUid,
                universityStudentCardIssuerParameters);

        // store issuance parameters for bank and user:
        bankKeyManager.storeIssuerParameters(creditCardIssuancePolicyUid,
                bankCreditcardIssuerParameters);
        userKeyManager.storeIssuerParameters(creditCardIssuancePolicyUid,
                bankCreditcardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(creditCardIssuancePolicyUid,
                bankCreditcardIssuerParameters);

        bankKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        hotelKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        governmentKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);

        // setup inspector public key.
        InspectorPublicKey inspectorPubKey =
                inspectorEngine.setupInspectorPublicKey(1024,
                        CryptoUriUtil.getIdemixMechanism(),
                        HotelBookingTest.INSPECTOR_URI);
        f = new FriendlyDescription();
        f.setLang("en");
        f.setValue("The Best Bank Inc.");
        inspectorPubKey.getFriendlyInspectorDescription().add(f);
        inspectorKeyManager.storeInspectorPublicKey(HotelBookingTest.INSPECTOR_URI, inspectorPubKey);
        userKeyManager.storeInspectorPublicKey(
                HotelBookingTest.INSPECTOR_URI, inspectorPubKey);
        hotelKeyManager.storeInspectorPublicKey(
                HotelBookingTest.INSPECTOR_URI, inspectorPubKey);
        bankKeyManager.storeInspectorPublicKey(
                HotelBookingTest.INSPECTOR_URI, inspectorPubKey);

        // Step 1. Get passport.
        logger.info("Get passport.");
        this.issueAndStorePassport(governmentInjector, userInjector,
                issuanceHelper);

        // Step 2. Get student id.
        logger.info("Get student id.");
        this.issueAndStoreStudentId(universityInjector, userInjector,
                issuanceHelper);


        // Step 3. Get credit card using id and student card
        logger.info("Get credit card.");
        this.issueAndStoreCreditCard(bankInjector, userInjector,
                issuanceHelper);

        // Step 4a. Book a hotel room using passport and credit card. This uses
        // the first alternative of the presentation policy.
        logger.info("Verify.");
        PresentationToken pt = this.bookHotelRoomUsingPassportAndCreditcard(
                issuanceHelper, hotelInjector, userInjector, revParamsUid);

        // Step 4b. Book a hotel room using passport and credit card. This uses
        // the second alternative of the presentation policy.
        pt = this.bookHotelRoomUsingStudentcardPassportAndCreditcard(
                hotelInjector, userInjector, issuanceHelper, revParamsUid);

        // Step 4c. Booking a hotel room using passport and credit card fails
        // because customer is blacklisted by hotel.
        // Not implemented yet.
        this.failBookingHotelRoomUsingPassportAndCreditcard(hotelInjector,
                userInjector, issuanceHelper);

        // Step 5. Inspect credit card data because of no-show.
        if (inspectorInjector != null) {
            this.inspectCreditCard(bankInjector, inspectorInjector, pt);
        }

    }


    private Object loadResources(String issuancePolicyPassport)
            throws JAXBException, UnsupportedEncodingException, SAXException {
        return XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                issuancePolicyPassport), true);
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
        att.put("Issued", "2012-02-06Z");
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
        atts.put(REVOCATION_HANDLE_STR,
                "http://admin.ch/passport/revocation/parameters");
        atts.put("StudentNumber", 345);
        atts.put("Issued", "2012-02-02Z");
        atts.put("Expires", "2022-02-02Z");
        atts.put("IssuedBy", "ethz.ch");
        return atts;
    }

    private void issueAndStoreCreditCard(Injector bankInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        Map<String, Object> atts = this.populateCreditCardIssuerAttributes();
        issuanceHelper.issueCredential(bankInjector, userInjector,
                CREDENTIAL_SPECIFICATION_REVOCABLE_CREDITCARD,
                ISSUANCE_POLICY_REVOCABLE_CREDIT_CARD, atts);
    }

    private Map<String, Object> populateCreditCardIssuerAttributes()
            throws Exception {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put(REVOCATION_HANDLE_STR,
                "http://admin.ch/passport/revocation/parameters");
        atts.put("CardType", SWISS_EXPRESS);
        atts.put("Status", STATUS);
        atts.put("SecurityCode", SECURITY_CODE);
        atts.put("CardNumber", CARD_NUMBER);
        return atts;
    }

    private PresentationToken bookHotelRoomUsingPassportAndCreditcard(
            IssuanceHelper issuanceHelper, Injector hotelInjector,
            Injector userInjector, URI revParamsUid) throws Exception {
        int presentationTokenChoice = 0;
        return this.bookHotel(issuanceHelper, hotelInjector, userInjector,
                presentationTokenChoice, presentationTokenChoice, revParamsUid);
    }

    private PresentationToken bookHotelRoomUsingStudentcardPassportAndCreditcard(
            Injector hotelInjector, Injector userInjector,
            IssuanceHelper issuanceHelper, URI revParamsUid) throws Exception {
        int presentationTokenChoice = 1;
        return this.bookHotel(issuanceHelper, hotelInjector, userInjector,
                presentationTokenChoice, presentationTokenChoice, revParamsUid);
    }

    private PresentationToken bookHotel(IssuanceHelper issuanceHelper,
            Injector hotelInjector, Injector userInjector,
            final int presentationTokenChoice, int pseudonymChoice,
            URI revParamsUid)
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
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_ALTERNATIVES_HOTEL,
                        revocationInformation,
                        new IdentitySelectionPrinter(
                                new PolicySelector(presentationTokenChoice,
                                        chosenInspectors, pseudonymChoice)));

        return issuanceHelper.verify(hotelInjector, p.second(), p.first());
    }

    private void failBookingHotelRoomUsingPassportAndCreditcard(
            Injector hotelInjector, Injector userInjector,
            IssuanceHelper issuanceHelper) throws Exception {
        int presentationTokenChoice = 0;
        int pseudonymChoice = 1;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
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

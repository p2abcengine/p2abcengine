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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test checks that a user can get issued a simple identity card with
 * firstname, lastname, and birthday.
 */
// Ignored due to problems related to storing credentials in persistant storage
//@Ignore
public class RevocationTest {
    private static final String URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX = "urn:abc4trust:1.0:algorithm:idemix";
    	 
    //@Ignore
    @Test
    public void revocationTest() throws Exception {
        this.runRevocationTest(new SimpleRevocationTest());
    }

    class SimpleRevocationTest implements IRevocationTest {
        @Override
        public void runTest(Injector revocationInjector,
                Injector governmentInjector, Injector userInjector,
                Injector verifierInjector, IssuanceHelper issuanceHelper,
                RevocationAbcEngine revocationEngine, URI revParamsUid)
                        throws Exception, CryptoEngineException {
            // Step 1. Get identity card.
            CredentialDescription cd = IntegrationTestUtil.issueAndStoreIdCard(
                    governmentInjector, userInjector, issuanceHelper,
                    "1995-05-05Z");

            // Step 2. Get an account (pseudonym) using the identity card.
            PresentationToken pt = IntegrationTestUtil.createAccount(
                    userInjector, verifierInjector, issuanceHelper,
                    IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
            assertNotNull(pt);

            Attribute revocationHandleAttribute = RevocationTest.this
                    .getRevocationHandle(cd);
            // Step 3. Revoke credential.
            RevocationTest.this.revokeCredential(revocationInjector,
                    issuanceHelper, revParamsUid, revocationHandleAttribute);

            // The verifier needs to retrive the latest revocation information
            // in order to put in the UID in the presentation policy.
            RevocationInformation revocationInformation = revocationEngine
                    .updateRevocationInformation(revParamsUid);

            // Step 4. Login using pseudonym.
            RevocationTest.this.revokedCredentialsShouldNotBeAllowed(
                    userInjector,
                    verifierInjector, issuanceHelper, revocationInformation, 0);
        }
    }

//    @Ignore
    @Test 
    public void multipleRevokeRevocationTest() throws Exception {
        this.runRevocationTest(new MultipleRevokeRevocationTest());
    }

    class MultipleRevokeRevocationTest implements IRevocationTest {
        @Override
        public void runTest(Injector revocationInjector,
                Injector governmentInjector, Injector userInjector,
                Injector verifierInjector, IssuanceHelper issuanceHelper,
                RevocationAbcEngine revocationEngine, URI revParamsUid)
                        throws Exception, CryptoEngineException {
            // Step 1. Get identity card.
            CredentialDescription cd1 = IntegrationTestUtil
                    .issueAndStoreIdCard(
                            governmentInjector, userInjector, issuanceHelper,
                            "1995-05-05Z");
            
            CredentialDescription cd2 = IntegrationTestUtil
                    .issueAndStoreIdCard(
                    		governmentInjector, userInjector, issuanceHelper, 
                    		"1995-05-05Z");
            
            CredentialDescription cd3 = IntegrationTestUtil
            		.issueAndStoreIdCard(
            				governmentInjector, userInjector, issuanceHelper, 
            				"1995-05-05Z");

            // Step 2. Get an account (pseudonym) using the identity card.
            PresentationToken pt = IntegrationTestUtil.createAccount(
                    userInjector, verifierInjector, issuanceHelper,
                    IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
            assertNotNull(pt);
            
            // Step 3. Revoke credentials.
            Attribute revocationHandleAttribute = RevocationTest.this
                    .getRevocationHandle(cd1);
            RevocationTest.this.revoke(revocationInjector, issuanceHelper,
                    revParamsUid,
                    revocationHandleAttribute);

            revocationHandleAttribute = RevocationTest.this
                    .getRevocationHandle(cd2);
            RevocationTest.this.revoke(revocationInjector, issuanceHelper,
                    revParamsUid,
                    revocationHandleAttribute);

            // The verifier needs to retrive the latest revocation information
            // in order to put in the UID in the presentation policy.
            RevocationInformation revocationInformation = revocationEngine
                    .updateRevocationInformation(revParamsUid);
            // Step 4. Login using pseudonym.
            RevocationTest.this.revokedCredentialsShouldNotBeAllowed(
                    userInjector,
                    verifierInjector, issuanceHelper, revocationInformation, cd1.getCredentialUID());

            RevocationTest.this.revokedCredentialsShouldNotBeAllowed(
                    userInjector,
                    verifierInjector, issuanceHelper, revocationInformation, cd2.getCredentialUID());

            RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(
                    userInjector,
                    verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

            RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(
                    userInjector,
                    verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());
        }

    }

    private void revoke(Injector revocationInjector,
            IssuanceHelper issuanceHelper, URI revParamsUid,
            Attribute revocationHandleAttribute) throws CryptoEngineException {
        RevocationTest.this.revokeCredential(revocationInjector,
                issuanceHelper, revParamsUid, revocationHandleAttribute);
    }

    private void nonRevokedCredentialsShouldBeAllowed(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            RevocationInformation revocationInformation,
            URI chosenPresentationToken) throws Exception {
        try {
            IntegrationTestUtil.loginToAccount(userInjector, verifierInjector,
                    issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID,
                    revocationInformation, chosenPresentationToken);
            assertTrue(
                    "We expect the verification to validate because the credential is not revoked",
                    true);
        } catch (TokenVerificationException ex) {
            ex.printStackTrace();
            fail("We should be able to log in with a non-revoked credential");
        }
    }

    private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            RevocationInformation revocationInformation,
            URI chosenPresentationToken) throws Exception {
        try {
        	IntegrationTestUtil.loginToAccount(userInjector, verifierInjector,
                    issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID,
                    revocationInformation, chosenPresentationToken);
            fail("We should not be allowed to log in with a revoked credential");
        } catch (TokenVerificationException ex) {
            // StringWriter sw = new StringWriter();
            // PrintWriter pw = new PrintWriter(sw);
            // ex.printStackTrace(pw);
            // assertTrue(sw.toString().contains("Incorrect T-value at position"));
            assertTrue(
                    "We expect the verification to fail due to a revoked credential",
                    ex.getMessage()
                    .startsWith(
                            "The crypto evidence in the presentation token is not valid"));
        }
    }
    
    private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            RevocationInformation revocationInformation,
            int chosenPresentationToken) throws Exception {
        try {
            IntegrationTestUtil.loginToAccount(userInjector, verifierInjector,
                    issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID,
                    revocationInformation, chosenPresentationToken);
            fail("We should not be allowed to log in with a revoked credential");
        } catch (TokenVerificationException ex) {
            // StringWriter sw = new StringWriter();
            // PrintWriter pw = new PrintWriter(sw);
            // ex.printStackTrace(pw);
            // assertTrue(sw.toString().contains("Incorrect T-value at position"));
            assertTrue(
                    "We expect the verification to fail due to a revoked credential",
                    ex.getMessage()
                    .startsWith(
                            "The crypto evidence in the presentation token is not valid"));
        }
    }

    private void runRevocationTest(IRevocationTest test)
            throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
        int keyLength = 1024;
        URI cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);
        Random random = new Random(1231);
        // Generate system parameters.
        Secret secret = this.getIdemixSecret();

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(random, cryptoEngine,
                        revocationProxyAuthority));

        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), cryptoEngine, revocationProxyAuthority));

        Injector verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, revocationProxyAuthority));

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);

        SystemParameters systemParameters = governmentEngine
                .setupSystemParameters(keyLength, cryptoMechanism);
        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);
        userCredentialManager.storeSecret(secret);

        // Setup issuance helper.
        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Setup key managers.
        KeyManager issuerKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector
                .getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);


        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        // Setup issuance policies.
        IssuancePolicy issuancePolicyIdCard = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD), true);

        URI idCardIssuancePolicyUid = issuancePolicyIdCard
                .getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification credSpecIdCard = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                        		IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD), true);

        // Store credential specifications.
        URI credSpecIdCardUID = credSpecIdCard.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);
        verifierKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);

        // Generate revocation parameters.
        RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
        URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getAny().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getAny().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getAny().add(
                URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        cryptoMechanism, revParamsUid, revocationInfoReference,
                        nonRevocationEvidenceReference, nonRrevocationUpdateReference);


        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        IssuerParameters issuerParametersGovernment = governmentEngine
                .setupIssuerParameters(credSpecIdCard, systemParameters,
                        idCardIssuancePolicyUid, hash, cryptoMechanism,
                        revParamsUid);

        issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        verifierKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);

        issuerKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        userKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        verifierKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);

        test.runTest(revocationInjector, governmentInjector,
                userInjector, verifierInjector, issuanceHelper,
                revocationEngine, revParamsUid);
    }

    interface IRevocationTest {

        public void runTest(Injector revocationInjector,
                Injector governmentInjector, Injector userInjector,
                Injector verifierInjector, IssuanceHelper issuanceHelper,
                RevocationAbcEngine revocationEngine, URI revParamsUid)
                        throws Exception, CryptoEngineException;

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

    private void revokeCredential(Injector revocationInjector,
            IssuanceHelper issuanceHelper, URI revParamsUid,
            Attribute revocationHandleAttribute) throws CryptoEngineException {
        issuanceHelper.revokeCredential(revocationInjector, revParamsUid,
                revocationHandleAttribute);
    }

    private Secret getIdemixSecret() throws JAXBException,
    UnsupportedEncodingException, SAXException {
        Secret secret = (Secret) XmlUtils.getObjectFromXML(
                this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"),
                        true);
        return secret;
    }

    @Test
    public void simpleRevocationBridgingTest() throws Exception {
        int keyLength = 2048;
        SecretWrapper uproveSecretWrapper = new SecretWrapper(
                IntegrationTestUtil.getUProveSecret());
        SecretWrapper idemixSecretWrapper = new SecretWrapper(
                this.getIdemixSecret());

        UProveUtils uproveUtils = new UProveUtils();
        // Get Injectors,

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        IssuerCryptoEngine.UPROVE, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));

        Injector fakeInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), CryptoEngine.IDEMIX));

        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort(), revocationProxyAuthority));

        Injector verifierInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231), uproveUtils.getVerifierServicePort(),
                        revocationProxyAuthority));

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);

        SystemParameters systemParameters = governmentEngine
                .setupSystemParameters(keyLength,
                        CryptoUriUtil.getUproveMechanism());

        IssuerAbcEngine fakeEngine = fakeInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters ideMixSystemParameters = fakeEngine
                .setupSystemParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism());

        systemParameters.getAny().addAll(ideMixSystemParameters.getAny());

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Setup key managers.
        KeyManager issuerKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector
                .getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);


        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        // Setup issuance policies.
        IssuancePolicy issuancePolicyIdCard = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                        		IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD), true);

        URI idCardIssuancePolicyUid = issuancePolicyIdCard
                .getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification credSpecIdCard = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                        		IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD), true);

        // Store credential specifications.
        URI credSpecIdCardUID = credSpecIdCard.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);
        verifierKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);

        // Generate revocation parameters.
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getAny().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getAny().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getAny().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRrevocationUpdateReference);


        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        IssuerParameters issuerParametersGovernment = governmentEngine
                .setupIssuerParameters(credSpecIdCard, systemParameters,
                        idCardIssuancePolicyUid, hash,
                        CryptoUriUtil.getUproveMechanism(), revParamsUid);

        issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        verifierKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);

        issuerKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        userKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        verifierKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);

        if (uproveSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    uproveSecretWrapper.getSoftwareSmartcard(),
                    uproveSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            uproveSecretWrapper.addIssuerParameters(issuerParametersGovernment);
        } else {
            userCredentialManager.storeSecret(uproveSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        if (idemixSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    idemixSecretWrapper.getSoftwareSmartcard(),
                    idemixSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            idemixSecretWrapper.addIssuerParameters(issuerParametersGovernment);
        } else {
            userCredentialManager.storeSecret(idemixSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        // Step 1. Get identity card.
        CredentialDescription cd = IntegrationTestUtil
                .issueAndStoreIdCard(governmentInjector, userInjector,
                        issuanceHelper, "1995-05-05Z");

        // Step 2. Get an account (pseudonym) using the identity card.
        PresentationToken pt = IntegrationTestUtil.createAccount(userInjector,
                verifierInjector, issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
        assertNotNull(pt);


        Attribute revocationHandleAttribute = RevocationTest.this
                .getRevocationHandle(cd);

        // Step 3. Revoke credential.
        RevocationTest.this.revokeCredential(revocationInjector,
                issuanceHelper, revParamsUid, revocationHandleAttribute);

        
        // The verifier needs to retrive the latest revocation information
        // in order to put in the UID in the presentation policy.
        RevocationInformation revocationInformation = revocationEngine
                .updateRevocationInformation(revParamsUid);

        // Step 4. Login using pseudonym.
        this.revokedCredentialsShouldNotBeAllowed(userInjector,
                verifierInjector, issuanceHelper, revocationInformation, 0);

        // System.out.println("Test done");
        int exitCode = userInjector.getInstance(UProveBindingManager.class)
                .stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = governmentInjector.getInstance(UProveBindingManager.class)
                .stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = verifierInjector.getInstance(
                UProveBindingManager.class).stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }

    @Test
    public void multipleRevocationBridgingTest() throws Exception {
        int keyLength = 2048;
        SecretWrapper uproveSecretWrapper = new SecretWrapper(
                IntegrationTestUtil.getUProveSecret());
        SecretWrapper idemixSecretWrapper = new SecretWrapper(
                this.getIdemixSecret());

        UProveUtils uproveUtils = new UProveUtils();

        // Get Injectors,

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.IDEMIX));

        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        IssuerCryptoEngine.UPROVE, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));

        Injector fakeInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), CryptoEngine.IDEMIX));

        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort(), revocationProxyAuthority));

        Injector verifierInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231), uproveUtils.getVerifierServicePort(),
                        revocationProxyAuthority));

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);

        SystemParameters systemParameters = governmentEngine
                .setupSystemParameters(keyLength,
                        CryptoUriUtil.getUproveMechanism());

        IssuerAbcEngine fakeEngine = fakeInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters ideMixSystemParameters = fakeEngine
                .setupSystemParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism());

        systemParameters.getAny().addAll(ideMixSystemParameters.getAny());

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Setup key managers.
        KeyManager issuerKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector
                .getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);

        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        // Setup issuance policies.
        IssuancePolicy issuancePolicyIdCard = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                        		IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD), true);

        URI idCardIssuancePolicyUid = issuancePolicyIdCard
                .getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification credSpecIdCard = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                        		IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD), true);

        // Store credential specifications.
        URI credSpecIdCardUID = credSpecIdCard.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);
        verifierKeyManager.storeCredentialSpecification(credSpecIdCardUID,
                credSpecIdCard);

        // Generate revocation parameters.
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getAny().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getAny().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getAny().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRrevocationUpdateReference);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        IssuerParameters issuerParametersGovernment = governmentEngine
                .setupIssuerParameters(credSpecIdCard, systemParameters,
                        idCardIssuancePolicyUid, hash,
                        CryptoUriUtil.getUproveMechanism(), revParamsUid);

        issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        verifierKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);

        issuerKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        userKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);
        verifierKeyManager.storeIssuerParameters(idCardIssuancePolicyUid,
                issuerParametersGovernment);

        if (uproveSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    uproveSecretWrapper.getSoftwareSmartcard(),
                    uproveSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            uproveSecretWrapper.addIssuerParameters(issuerParametersGovernment);
        } else {
            userCredentialManager.storeSecret(uproveSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        if (idemixSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    idemixSecretWrapper.getSoftwareSmartcard(),
                    idemixSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            idemixSecretWrapper.addIssuerParameters(issuerParametersGovernment);
        } else {
            userCredentialManager.storeSecret(idemixSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        // Step 1. Get identity card.
        CredentialDescription cd1 = IntegrationTestUtil
                .issueAndStoreIdCard(governmentInjector, userInjector,
                        issuanceHelper, "1995-05-05Z");

        CredentialDescription cd2 = IntegrationTestUtil
                .issueAndStoreIdCard(governmentInjector, userInjector,
                        issuanceHelper, "1995-05-05Z");

        CredentialDescription cd3 = IntegrationTestUtil.issueAndStoreIdCard(governmentInjector,
                userInjector, issuanceHelper, "1995-05-05Z");

        // Step 2. Get an account (pseudonym) using the identity card.
        PresentationToken pt = IntegrationTestUtil.createAccount(userInjector,
                verifierInjector, issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID);
        assertNotNull(pt);

        // Step 3. Revoke credentials.
        Attribute revocationHandleAttribute = RevocationTest.this
                .getRevocationHandle(cd1);
        this.revoke(revocationInjector, issuanceHelper, revParamsUid,
                revocationHandleAttribute);

        revocationHandleAttribute = RevocationTest.this
                .getRevocationHandle(cd2);
        this.revoke(revocationInjector, issuanceHelper, revParamsUid,
                revocationHandleAttribute);

        // The verifier needs to retrive the latest revocation information
        // in order to put in the UID in the presentation policy.
        RevocationInformation revocationInformation = revocationEngine
                .updateRevocationInformation(revParamsUid);

        // Step 4. Login using pseudonym.
        RevocationTest.this.revokedCredentialsShouldNotBeAllowed(userInjector,
                verifierInjector, issuanceHelper, revocationInformation, cd1.getCredentialUID());

        RevocationTest.this.revokedCredentialsShouldNotBeAllowed(userInjector,
                verifierInjector, issuanceHelper, revocationInformation, cd2.getCredentialUID());

        RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(userInjector,
                verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        RevocationTest.this.nonRevokedCredentialsShouldBeAllowed(userInjector,
                verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        // System.out.println("Test done");
        int exitCode = userInjector.getInstance(UProveBindingManager.class)
                .stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = governmentInjector.getInstance(UProveBindingManager.class)
                .stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = verifierInjector.getInstance(UProveBindingManager.class)
                .stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }
}
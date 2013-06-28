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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
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
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ReloadUProveTokensTest {
	private <T> T getObjectFromXML(String name, boolean value) throws UnsupportedEncodingException, JAXBException, SAXException {
		InputStream is = RevocationTest.class.getResourceAsStream(name);

		@SuppressWarnings("unchecked")
		T t = (T) XmlUtils.getObjectFromXML(is, value);

		return t;
	}
	
	//use an UProve token by executing a login to a service
    private void useOneToken(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            RevocationInformation revocationInformation,
            URI chosenPresentationToken) throws Exception {
        try {
            IntegrationTestUtil.loginToAccount(userInjector, verifierInjector,
                    issuanceHelper, IntegrationTestUtil.REVOCATION_PARAMETERS_UID,
                    revocationInformation, chosenPresentationToken);
            assertTrue(
                    "We expect the verification to validate because there should be more tokens left", true);
        } catch (TokenVerificationException ex) {
            ex.printStackTrace();
            fail("We should be able to log in with credential since more tokens are left");
        }
    }
	
    @Test
    public void reloadUproveTokensTest() throws Exception {
        int keyLength = 2048;
        SecretWrapper uproveSecretWrapper = new SecretWrapper(IntegrationTestUtil.getUProveSecret());

        UProveUtils uproveUtils = new UProveUtils();

        // Get Injectors,
        Injector revocationInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector.getInstance(RevocationProxyAuthority.class);

        Injector governmentInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231), 
       		 IssuerCryptoEngine.UPROVE, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));

        Injector fakeInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));

        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231), 
       		 uproveUtils.getUserServicePort(), revocationProxyAuthority));

        Injector verifierInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231), 
       		 uproveUtils.getVerifierServicePort(), revocationProxyAuthority));

        IssuerAbcEngine governmentEngine = governmentInjector.getInstance(IssuerAbcEngine.class);

        SystemParameters systemParameters = 
       		 governmentEngine.setupSystemParameters(keyLength, CryptoUriUtil.getUproveMechanism());
        

        IssuerAbcEngine fakeEngine = fakeInjector.getInstance(IssuerAbcEngine.class);
        SystemParameters ideMixSystemParameters = 
       		 fakeEngine.setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

        systemParameters.getAny().addAll(ideMixSystemParameters.getAny());

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Setup key managers.
        KeyManager issuerKeyManager = governmentInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector.getInstance(KeyManager.class);

        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        // Setup issuance policies.
        IssuancePolicy issuancePolicyIdCard = getObjectFromXML(IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD, true);

        URI idCardIssuancePolicyUid = issuancePolicyIdCard.getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification credSpecIdCard = getObjectFromXML(IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD, true);

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
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRrevocationUpdateReference);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        IssuerParameters issuerParametersGovernment = governmentEngine.setupIssuerParameters(
       		 credSpecIdCard, systemParameters, idCardIssuancePolicyUid, hash, 
       		 CryptoUriUtil.getUproveMechanism(), revParamsUid, null);

        issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid, revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid, revocationAuthorityParameters);
        verifierKeyManager.storeRevocationAuthorityParameters(revParamsUid, revocationAuthorityParameters);

        issuerKeyManager.storeIssuerParameters(idCardIssuancePolicyUid, issuerParametersGovernment);
        userKeyManager.storeIssuerParameters(idCardIssuancePolicyUid, issuerParametersGovernment);
        verifierKeyManager.storeIssuerParameters(idCardIssuancePolicyUid, issuerParametersGovernment);

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

        // Get identity card.
        String birthday = "1995-05-05Z";
        CredentialDescription cd3 = IntegrationTestUtil.issueAndStoreIdCard(governmentInjector, userInjector, issuanceHelper, birthday);

        // The verifier needs to retrive the latest revocation information
        // in order to put in the UID in the presentation policy.
        RevocationInformation revocationInformation = revocationEngine.updateRevocationInformation(revParamsUid);

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());

        useOneToken(userInjector, verifierInjector, issuanceHelper, revocationInformation, cd3.getCredentialUID());
        
        System.out.println("Test done");
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

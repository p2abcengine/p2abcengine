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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test tests the issuance of a CreditCard Credential using the UProve CryptoEngine implementation.
 * 
 * @author Raphael Dobers
 */

// Test separately with:
// mvn test -Dtest=UProveCredentialIssuanceIntegrationTest

public class UProveCredentialIssuanceIntegrationTest {

    @Test()
    public void test() throws Exception {
        UProveUtils uproveUtils = new UProveUtils();
        Injector issuerInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        uproveUtils.getIssuerServicePort()));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort()));
        Injector verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort()));

        // Step 1. Load the credit card specification into the keystore.
        CredentialSpecification creditCardSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/uprove/credentialSpecificationCreditcardVisa.xml"),
                                true);

        KeyManager keyManager = issuerInjector.getInstance(KeyManager.class);
        keyManager.storeCredentialSpecification(
                creditCardSpec.getSpecificationUID(), creditCardSpec);

        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        userKeyManager.storeCredentialSpecification(
                creditCardSpec.getSpecificationUID(), creditCardSpec);

        // Step 2. Load the issuance policy and attributes.
        IssuancePolicy ip = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/uprove/issuancePolicyCreditcardVisa.xml"),
                                true);

        // Populate CreditCard attribute values..
        List<Attribute> issuerAtts = new ArrayList<Attribute>();
        this.populateAttributes(issuerAtts);

        // Step 3. Init the issuer
        IssuerAbcEngine issuerEngine = issuerInjector
                .getInstance(IssuerAbcEngine.class);

        ReloadTokensInMemoryCommunicationStrategy reloadTokens = (ReloadTokensInMemoryCommunicationStrategy) userInjector.getInstance(ReloadTokensCommunicationStrategy.class);
        reloadTokens.setIssuerAbcEngine(issuerEngine);
        reloadTokens.setIssuancePolicy(ip);

        int idemixKeyLength = 2048;
        int uproveKeylength = 2048;
        URI cryptographicMechanism = CryptoUriUtil.getUproveMechanism();
        SystemParametersUtil sysParamUtil = new SystemParametersUtil();
        SystemParameters sysParam = SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(
                        idemixKeyLength, uproveKeylength);

        userKeyManager.storeSystemParameters(sysParam);
        keyManager.storeSystemParameters(sysParam);

        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();

        URI hash = CryptoUriUtil.getHashSha256();
        URI revocationId = new URI("issuer-cpr-rev-id");

        IssuerParameters issuerParameters = issuerEngine.setupIssuerParameters(creditCardSpec, sysParam, uid, hash, cryptographicMechanism, revocationId, null);

        // Store received issuer parameters in all keymanagers...
        keyManager.storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);
        userKeyManager.storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);

        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
        verifierKeyManager.storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);
        verifierKeyManager.storeSystemParameters(sysParam);

        // Step 4. Issue a credit card credential.
        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        this.doIssuanceProtocol(issuerEngine, userEngine, ip, issuerAtts);

        // Step 5. Generate a PresentationToken using a simple HotelBooking Policy with creditcard only credential

        PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/uprove/presentationPolicySimpleHotelBookingCreditCardOnly.xml"),
                                true);

        PresentationToken presentationToken = userEngine.createPresentationToken(presentationPolicyAlternatives);

        // FIXME: Using createPresentationToken again with the same presentationPolicyAlternatives causes a crash in MockIdentitySelection

        //PresentationToken presentationToken1 = userEngine.createPresentationToken(presentationPolicyAlternatives);

        PresentationPolicyAlternatives presentationPolicyAlternatives2 = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/uprove/presentationPolicySimpleHotelBookingCreditCardOnlyNoPseudonym.xml"),
                                true);


        PresentationToken presentationToken2 = userEngine.createPresentationToken(presentationPolicyAlternatives2);

        // We have run out of UProveTokens and must renew credential
        if(presentationToken2 == null) {
            this.doIssuanceProtocol(issuerEngine, userEngine, ip, issuerAtts);
            presentationToken2 = userEngine.createPresentationToken(presentationPolicyAlternatives2);
        }

        // Step 6. Verify the generated PresentationToken

        // Init the Issuer engine
        VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);
        boolean verifyOK = true;
        try {
            PresentationTokenDescription verifyResult = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, presentationToken, false);
            assertNotNull(verifyResult);
        }
        catch(TokenVerificationException ex) {
            verifyOK = false;
            System.out.println("Token Verify FAILED!!");
        }
        assertTrue(verifyOK);

        try {
            PresentationTokenDescription verifyResult2 = verifierEngine.verifyTokenAgainstPolicy(presentationPolicyAlternatives2, presentationToken2, false);
            assertNotNull(verifyResult2);
        }
        catch(TokenVerificationException ex) {
            verifyOK = false;
            System.out.println("Token Verify FAILED!!");
        }
        assertTrue(verifyOK);

        // try to shut down UProve Services...
        int exitCode = userInjector.getInstance(UProveBindingManager.class).stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = issuerInjector.getInstance(UProveBindingManager.class).stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        exitCode = verifierInjector.getInstance(UProveBindingManager.class).stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);

    }

    private void doIssuanceProtocol(IssuerAbcEngine issuerEngine, UserAbcEngine userEngine, IssuancePolicy ip, List<Attribute> issuerAtts ) throws Exception {

        // Initialize ABC4Trust Issuance Protocol Issuer side
        // Returns an IssuancePolicy from CryptoEngineIssuer to be passed to CryptoEngineUser initialization
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        // Initialize ABC4Trust Issuance Protocol Prover side
        // FIXME: userEngine indirectly calls CryptoEngineUser.createIssuanceToken() with the IssuancePolicy from the Issuer and returns an IssuanceToken
        // contained in the IssuanceMessage. This IssuanceToken is simply NOT used for now in the next step of the UProve Credential Issuance protocol.
        // However, this seems to be the only proper way for now to initialise CryptoEngineUser to a working state, despite the createIssuanceToken() step being
        // an optional step for the advanced Issuance setting not supported by UProve for now.
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(issuerIm
                .getIssuanceMessage());

        // Execute the UProve issuance protocol:

        // Prover side asks issuer for first message
        issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);
        assertFalse(issuerIm.isLastMessage());

        // Prover side generates second message based on the first message
        userIm = userEngine.issuanceProtocolStep(issuerIm.getIssuanceMessage());

        // Prover side asks issuer for third message based on the second message
        assertNotNull(userIm.im);
        issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);
        assertNotNull(issuerIm.getIssuanceMessage());

        // Prover side generates UProve Tokens based on the third message
        userIm = userEngine.issuanceProtocolStep(issuerIm.getIssuanceMessage());
        boolean userLastMessage = (userIm.cd != null);
        assertTrue(issuerIm.isLastMessage() == userLastMessage);

        assertNull(userIm.im);
        assertNotNull(userIm.cd);

        // CredentialDescription actualCredDesc = userIm.cd;
        // ObjectFactory of = new ObjectFactory();
        // String actualCredentialDesc =
        // XmlUtils.toNormalizedXML(of.createCredentialDescription(actualCredDesc));

        //System.out.println(actualCredentialDesc);
    }

    private void populateAttributes(
            List<Attribute> issuerAtts) throws Exception {
        ObjectFactory of = new ObjectFactory();

        Attribute cardType = of.createAttribute();
        cardType.setAttributeUID(new URI("card:type:blibliblib"));
        cardType.setAttributeValue("VISA");
        cardType.setAttributeDescription(of.createAttributeDescription());
        cardType.getAttributeDescription().setDataType(new URI("xs:string"));
        cardType.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        cardType.getAttributeDescription().setType(new URI("CardType"));
        issuerAtts.add(cardType);

        Attribute name = of.createAttribute();
        name.setAttributeUID(new URI("card:name:blibliblib"));
        name.setAttributeValue("Jens");
        name.setAttributeDescription(of.createAttributeDescription());
        name.getAttributeDescription().setDataType(new URI("xs:string"));
        name.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        name.getAttributeDescription().setType(new URI("Name"));
        issuerAtts.add(name);

        Attribute lastName = of.createAttribute();
        lastName.setAttributeUID(new URI("card:lastname:blibliblib"));
        lastName.setAttributeValue("Jensen");
        lastName.setAttributeDescription(of.createAttributeDescription());
        lastName.getAttributeDescription().setDataType(new URI("xs:string"));
        lastName.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        lastName.getAttributeDescription().setType(new URI("LastName"));
        issuerAtts.add(lastName);

        Attribute cardNumber = of.createAttribute();
        cardNumber.setAttributeUID(new URI("card:cardnumber"));
        cardNumber.setAttributeValue(234567812345678L);
        cardNumber.setAttributeDescription(of.createAttributeDescription());
        cardNumber.getAttributeDescription().setDataType(new URI("xs:integer"));
        cardNumber.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        cardNumber.getAttributeDescription().setType(new URI("CardNumber"));
        issuerAtts.add(cardNumber);

        Attribute expirationDate = of.createAttribute();
        expirationDate.setAttributeUID(new URI("card:expdate:blablablabla"));
        expirationDate.setAttributeValue("1976-12-30T10:42:42Z");
        expirationDate.setAttributeDescription(of.createAttributeDescription());
        expirationDate.getAttributeDescription().setDataType(new URI("xs:dateTime"));
        expirationDate.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:dateTime:unix:signed"));
        expirationDate.getAttributeDescription().setType(new URI("ExpirationDate"));
        issuerAtts.add(expirationDate);

        Attribute securityCode = of.createAttribute();
        securityCode.setAttributeUID(new URI("security:code:blablabla"));
        securityCode.setAttributeValue(42);
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);

        Attribute status = of.createAttribute();
        status.setAttributeUID(new URI("status:blablablabla"));
        status.setAttributeValue(new URI("Customer"));
        status.setAttributeDescription(of.createAttributeDescription());
        status.getAttributeDescription().setDataType(new URI("xs:anyURI"));
        status.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:anyUri:sha-256"));
        status.getAttributeDescription().setType(new URI("Status"));
        issuerAtts.add(status);

    }
}

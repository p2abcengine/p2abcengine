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

package eu.abc4trust.abce.integrationtests.idemix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Test for simple issuance of idmx credential
 * and then advanced issuance of the similar credential with carrying-over one attribute
 * which requires presenting the first credential (reveal attribute, prove inequality)
 * @author mdu
 */
public class AdvancedIssuanceIdemixCredentialTest {

    private static boolean DEBUG = false;

    @Test
    public void issueCredentialTestSameCredSpec() throws Exception{

        //---------------------------------------------------
        //Setup all instances
        //---------------------------------------------------

        ObjectFactory of = new ObjectFactory();
        Module userModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector userInjector = Guice.createInjector(userModule);
        UserAbcEngine userEngine = userInjector.getInstance(UserAbcEngine.class);


        Module issuerModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector issuerInjector = Guice.createInjector(issuerModule);
        IssuerAbcEngine issuerEngine = userInjector.getInstance(IssuerAbcEngine.class);

        Module verifierModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector verifierInjector = Guice.createInjector(verifierModule);

        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);

        //---------------------------------------------------
        //Issue a credential
        //---------------------------------------------------

        //read all objects

        CredentialSpecification creditCardSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/credentialSpecificationCreditcardVisa.xml"), true);

        //store cred spec
        if (!issuerKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (issuer)");
        }

        if (!userKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (user)");
        }

        if (!verifierKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (verifier)");
        }


        // load issuance policy
        IssuancePolicy ip =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisa.xml"), true);

        // Load secret
        Secret secret = (Secret) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        userInjector.getInstance(CredentialManager.class).storeSecret(secret);

        List<Attribute> issuerAttsSimple = new ArrayList<Attribute>();
        this.populateAttributesSimpleIssuance(issuerAttsSimple);

        //just an additional test that cred spec was stored
        CredentialSpecification credSpec=null;
        try {
            credSpec = issuerKeyManager.getCredentialSpecification(creditCardSpec.getSpecificationUID());
            System.out.println(XmlUtils.toNormalizedXML(of.createCredentialSpecification(credSpec)));
        } catch (KeyManagerException e1) {
            e1.printStackTrace();
        }

        // create all URIs
        int keyLength = 1024; // TODO: define the security level
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();
        URI hash = CryptoUriUtil.getHashSha256();
        URI revocationId = new URI("revocationAuthorityParametersUID");


        //generate system parameters
        SystemParameters sysParams = issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);

        //generate issuer parameters
        IssuerParameters issuerParameters = issuerEngine.setupIssuerParameters(creditCardSpec, sysParams, uid, hash, cryptoMechanism, revocationId, null);

        // store parameters for all parties:
        issuerKeyManager.storeIssuerParameters(uid, issuerParameters);
        userKeyManager.storeIssuerParameters(uid, issuerParameters);

        userKeyManager.storeSystemParameters(sysParams);

        // Issuer starts the issuance
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAttsSimple);
        assertFalse(issuerIm.isLastMessage());
        if (DEBUG) {
            System.out.println(XmlUtils.toXml((new ObjectFactory())
                    .createIssuanceMessage(issuerIm.getIssuanceMessage())));
        }

        // Reply from the user
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(issuerIm
                .getIssuanceMessage());
        CredentialDescription cd = null;
        // Ping-pong until both user and issuer finish

        while (!issuerIm.isLastMessage()) {
            if (DEBUG) {
                System.out.println(XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));			}
            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            userIm = userEngine.issuanceProtocolStep(issuerIm
                    .getIssuanceMessage());
            boolean userLastMessage = (userIm.cd != null);
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        cd = userIm.cd;
        String cds = XmlUtils.toXml(of.createCredentialDescription(cd));
        System.out.println(cds);
        assertNull(userIm.im);

        //---------------------------------------------------
        //Issue another credential with advanced issuance
        //---------------------------------------------------

        IssuancePolicy ipAdvanced =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisaAdvancedIdmx.xml"), true);

        List<Attribute> issuerAttsAdvanced = new ArrayList<Attribute>();
        this.populateAttributesAdvancedIssuance(issuerAttsAdvanced);

        // Issuer starts the issuance
        IssuanceMessageAndBoolean issuerImAdvanced = issuerEngine.initIssuanceProtocol(ipAdvanced, issuerAttsAdvanced);
        assertFalse(issuerImAdvanced.isLastMessage());

        if (DEBUG) {
            System.out.println(XmlUtils.toXml((new ObjectFactory())
                    .createIssuanceMessage(issuerImAdvanced
                            .getIssuanceMessage())));
        }

        // Reply from user
        IssuMsgOrCredDesc userImAdvanced = userEngine
                .issuanceProtocolStep(issuerImAdvanced.getIssuanceMessage());

        CredentialDescription cd1 = null;

        // Ping-pong until both user and issuer finish
        while (!issuerImAdvanced.isLastMessage()) {

            if (DEBUG) {
                System.out.println(XmlUtils.toXml(of.createIssuanceMessage(userImAdvanced.im)));
            }

            issuerImAdvanced = issuerEngine.issuanceProtocolStep(userImAdvanced.im);

            userImAdvanced = userEngine.issuanceProtocolStep(issuerImAdvanced
                    .getIssuanceMessage());

            boolean userLastMessage1 = (userImAdvanced.cd != null);

            assertTrue(issuerImAdvanced.isLastMessage() == userLastMessage1);
        }
        cd1 = userImAdvanced.cd;

        String cds1 = XmlUtils.toXml(of.createCredentialDescription(cd1));
        System.out.println(cds1);

        assertNull(userImAdvanced.im);

    }

    private void populateAttributesSimpleIssuance(List<Attribute> issuerAtts) throws Exception {
        ObjectFactory of = new ObjectFactory();

        Attribute status = of.createAttribute();
        status.setAttributeUID(new URI("status:blablablabla"));
        status.setAttributeValue(new URI("Student"));
        status.setAttributeDescription(of.createAttributeDescription());
        status.getAttributeDescription().setDataType(new URI("xs:anyURI"));
        status.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:anyUri:sha-256"));
        status.getAttributeDescription().setType(new URI("Status"));
        issuerAtts.add(status);

        Attribute cardType = of.createAttribute();
        cardType.setAttributeUID(new URI("card:type:blibliblib"));
        cardType.setAttributeValue("SwissExpress");
        cardType.setAttributeDescription(of.createAttributeDescription());
        cardType.getAttributeDescription().setDataType(new URI("xs:string"));
        cardType.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        cardType.getAttributeDescription().setType(new URI("CardType"));
        issuerAtts.add(cardType);

        Attribute securityCode = of.createAttribute();
        securityCode.setAttributeUID(new URI("security:code:blablabla"));
        securityCode.setAttributeValue(42);
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);

        Attribute pinCode = of.createAttribute();
        pinCode.setAttributeUID(new URI("pincode"));
        pinCode.setAttributeValue(42);
        pinCode.setAttributeDescription(of.createAttributeDescription());
        pinCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        pinCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        pinCode.getAttributeDescription().setType(new URI("SecurityCode"));
        //    issuerAtts.add(pinCode);

        Attribute pukCode = of.createAttribute();
        pukCode.setAttributeUID(new URI("security:code:blablabla"));
        pukCode.setAttributeValue(42);
        pukCode.setAttributeDescription(of.createAttributeDescription());
        pukCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        pukCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        pukCode.getAttributeDescription().setType(new URI("SecurityCode"));
        //   issuerAtts.add(pukCode);

        Attribute issuanceDate = of.createAttribute();
        issuanceDate.setAttributeUID(new URI("security:code:blablabla"));
        issuanceDate.setAttributeValue(42);
        issuanceDate.setAttributeDescription(of.createAttributeDescription());
        issuanceDate.getAttributeDescription().setDataType(new URI("xs:integer"));
        issuanceDate.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        issuanceDate.getAttributeDescription().setType(new URI("SecurityCode"));
        //  issuerAtts.add(issuanceDate);
    }

    private void populateAttributesAdvancedIssuance(List<Attribute> issuerAtts) throws Exception {
        ObjectFactory of = new ObjectFactory();

        Attribute cardType = of.createAttribute();
        cardType.setAttributeUID(new URI("card:type:bl"));
        cardType.setAttributeValue("RussianExpress");
        cardType.setAttributeDescription(of.createAttributeDescription());
        cardType.getAttributeDescription().setDataType(new URI("xs:string"));
        cardType.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        cardType.getAttributeDescription().setType(new URI("CardType"));
        issuerAtts.add(cardType);

        Attribute securityCode = of.createAttribute();
        securityCode.setAttributeUID(new URI("security:code:new"));
        securityCode.setAttributeValue(25);
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);

    }





}

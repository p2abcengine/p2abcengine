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
import eu.abc4trust.xml.Credential;
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
 * Test for simple issuance of the idmx credential
 * @author mdu
 */
public class IssueIdemixCredentialTest {

    private static boolean DEBUG = false;

    @Test
    public void issueCredentialTest() throws Exception{

        ObjectFactory of = new ObjectFactory();
        Module userModule = IdemixIntegrationModuleFactory.newModule(new Random(1234));
        Injector userInjector = Guice.createInjector(userModule);
        UserAbcEngine userEngine = userInjector.getInstance(UserAbcEngine.class);

        Module issuerModule = IdemixIntegrationModuleFactory.newModule(new Random(1234));
        Injector issuerInjector = Guice.createInjector(issuerModule);
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

        CredentialManager credManager = userInjector.getInstance(CredentialManager.class);
        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

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

        // load issuance policy
        IssuancePolicy ip =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisa.xml"), true);

        // Load secret
        Secret secret = (Secret) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        credManager.storeSecret(secret);

        List<Attribute> issuerAtts = new ArrayList<Attribute>();
        this.populateAttributes(issuerAtts);

        //just an additional test that cred spec was stored
        CredentialSpecification credSpec=null;
        try {
            credSpec = issuerKeyManager.getCredentialSpecification(creditCardSpec.getSpecificationUID());
            System.out.println(XmlUtils.toNormalizedXML(of.createCredentialSpecification(credSpec)));
        } catch (KeyManagerException e1) {
            e1.printStackTrace();
        }

        // create all URIs

        int keyLength = 1024; // TODO: why do we use it at all???
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();
        URI hash = CryptoUriUtil.getHashSha256(); //TODO
        URI revocationId = new URI("revocationUID"); //TODO

        //step 1 - generate system parameters

        SystemParameters sysParams = issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);

        //step 2 - generate issuer parameters

        IssuerParameters issuerParameters = issuerEngine.setupIssuerParameters(creditCardSpec, sysParams, uid, hash, cryptoMechanism, revocationId, null);

        // store parameters for all parties:
        issuerKeyManager.storeIssuerParameters(uid, issuerParameters);
        userKeyManager.storeIssuerParameters(uid, issuerParameters);

        userKeyManager.storeSystemParameters(sysParams);

        // Issuer starts the issuance
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        if (DEBUG) {
            System.out.println(XmlUtils.toXml((new ObjectFactory())
                    .createIssuanceMessage(issuerIm.getIssuanceMessage())));
        }

        // Reply from user
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(issuerIm
                .getIssuanceMessage());
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
        }


        CredentialDescription cd = null;

        // Ping-pong until both user and issuer finish
        while (!issuerIm.isLastMessage()) {

            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            userIm = userEngine.issuanceProtocolStep(issuerIm
                    .getIssuanceMessage());

            boolean userLastMessage = (userIm.cd != null);

            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        cd = userIm.cd;

        //get credential from the user credential store and check if it is as expected
        Credential storedCredential = credManager.getCredential(cd.getCredentialUID());
        // Check if the stored credential is the same as the expected credential
        String actualCredential = XmlUtils.toXml(of.createCredential(storedCredential));

        if (DEBUG) {
            System.out.println(actualCredential);
        }

        /*    String expectedCredential = XmlUtils.toXml(of.createCredential((Credential)
      XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/credentials/ccIssueCredentialTest.xml"), true)));
    assertEquals(expectedCredential, actualCredential);
         */

        assertNull(userIm.im);
    }


    private void populateAttributes(List<Attribute> issuerAtts) throws Exception {
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
    }




}

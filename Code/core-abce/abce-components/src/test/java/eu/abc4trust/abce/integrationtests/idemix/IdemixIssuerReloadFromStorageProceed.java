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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
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

@Ignore
public class IdemixIssuerReloadFromStorageProceed {
    private static boolean DEBUG = false;

    @Test
    public void reloadIssuerAndIssueCredentialTest() throws Exception{

        System.out.println("IdemixIssuerReloadFromStorageProceedTest.reloadIssuerAndIssueCredentialTest - start");

        File storageFolder = IdemixIssuerReloadFromStorageInit.getTemporaryStorageFolder();
        System.out.println(" - storageFolder : " + storageFolder.getAbsolutePath());

        ObjectFactory of = new ObjectFactory();

        // issuer
        Module issuerModule = IdemixIntegrationModuleFactory.newModule(new Random(1234), storageFolder, IdemixIssuerReloadFromStorageInit.ISSUER_STORAGE_PREFIX);
        Injector issuerInjector = Guice.createInjector(issuerModule);
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

        // user
        Module userModule = IdemixIntegrationModuleFactory.newModule(new Random(1234), storageFolder, IdemixIssuerReloadFromStorageInit.USER_STORAGE_PREFIX);
        Injector userInjector = Guice.createInjector(userModule);
        UserAbcEngine userEngine = userInjector.getInstance(UserAbcEngine.class);

        CredentialManager userCredManager = userInjector.getInstance(CredentialManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

        //read all objects


        CredentialSpecification creditCardSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/credentialSpecificationCreditcardVisa.xml"), true);

        //store cred spec - user only
        if (!userKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (user)");
        }

        // Load secret
        Secret secret = (Secret) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        userCredManager.storeSecret(secret);


        // load issuance policy
        IssuancePolicy ip =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisa.xml"), true);

        List<Attribute> issuerAtts = new ArrayList<Attribute>();
        this.populateAttributes(issuerAtts);

        //      //step 0 - SystemParameters included in IssuerParameters
        //
        //      try {
        //        SystemParameters sysParams = readObject(storageFolder, IdemixIssuerReloadFromStorageInitTest.ISSUER_STORAGE_PREFIX, "system_parameters");
        //      } catch(FileNotFoundException e) {
        //        // TODO : not able to read files on Jenkins...  So bail out for now!
        //        System.err.println("Could not find storge files in folder : " + storageFolder.getAbsolutePath());
        //        throw e;
        //      }

        //step 1 - generate issuer parameters
        System.out.println(" - generate issuer parameters");

        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();

        IssuerParameters issuerParameters = this.readObject(storageFolder, IdemixIssuerReloadFromStorageInit.ISSUER_STORAGE_PREFIX, "issuer_parameters");

        // store parameters on user:
        userKeyManager.storeIssuerParameters(uid, issuerParameters);

        // step 1 - generate system parameters
        int keyLength = 1024;
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        SystemParameters sysParams = issuerEngine.setupSystemParameters(
                keyLength, cryptoMechanism);

        userKeyManager.storeSystemParameters(sysParams);

        // step 2 - Issuer starts the issuance
        System.out.println(" - start issuance - Issuer ");
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        if (DEBUG) {
            System.out.println(XmlUtils.toXml((new ObjectFactory())
                    .createIssuanceMessage(issuerIm.getIssuanceMessage())));
        }

        // step 3 : Reply from user
        System.out.println(" - start issuance - User ");
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(issuerIm
                .getIssuanceMessage());
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
        }


        CredentialDescription cd = null;

        // Ping-pong until both user and issuer finish
        System.out.println(" - run issuance steps...");

        while (!issuerIm.isLastMessage()) {

            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            userIm = userEngine.issuanceProtocolStep(issuerIm
                    .getIssuanceMessage());

            boolean userLastMessage = (userIm.cd != null);

            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        cd = userIm.cd;

        //get credential from the user credential store and check if it is as expected
        Credential storedCredential;
        try {
            System.out.println(" - verify stored credential...");
            storedCredential = userCredManager.getCredential(cd.getCredentialUID());
            // Check if the stored credential is the same as the expected credential
            String actualCredential = XmlUtils.toXml(of.createCredential(storedCredential));
            System.out.println(" - verify stored : " + (storedCredential == null ? " NULL !!!" : "Ok : " + storedCredential));

            if (DEBUG) {
                System.out.println(actualCredential);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        /*    String expectedCredential = XmlUtils.toXml(of.createCredential((Credential)
    XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/credentials/ccIssueCredentialTest.xml"), true)));
  assertEquals(expectedCredential, actualCredential);
         */

        assertNull(userIm.im);

        System.out.println("IdemixIssuerReloadFromStorageProceedTest.reloadIssuerAndIssueCredentialTest - done");

    }

    @Test
    public void done() {
        System.out.println("IdemixIssuerReloadFromStorageProceedTest DONE");
        System.out.println("=============================================");
    }


    @SuppressWarnings("unchecked")
    private <T> T readObject(File storageFoler, String issuerStoragePrefix,
            String name)
                    throws IOException, ClassNotFoundException {
        InputStream is = new FileInputStream(new File(storageFoler, issuerStoragePrefix + name));
        ObjectInputStream ois = new ObjectInputStream(is);

        Object object = ois.readObject();
        ois.close();
        is.close();

        return (T) object;
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

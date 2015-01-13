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

package eu.abc4trust.abce.utils;

import java.net.URI;
import java.util.Random;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestration;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class CreateUProveXML {

  private static final String USERNAME = "defaultUser";
    /**
     * @param args
     * @throws CredentialManagerException
     * @throws KeyManagerException
     * @throws CryptoEngineException 
     */
    public static void main(String[] args) throws CredentialManagerException,
    KeyManagerException, CryptoEngineException {

        Injector issuerInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231)));

        int keyLength = 2048;
        URI cryptoMechanism = URI.create("urn:abc4trust:1.0:algorithm:uprove");
        CryptoEngineIssuer uproveIssuer = issuerInjector
                .getInstance(CryptoEngineIssuer.class);
        SystemParameters sysparams = createSystemParams(uproveIssuer,
                keyLength, cryptoMechanism);

        storeSysparams(sysparams, userInjector);

        CryptoEngineUser uprove = userInjector
                .getInstance(CryptoEngineUser.class);
        EvidenceGenerationOrchestration ego = userInjector.getInstance(EvidenceGenerationOrchestration.class);

        Secret secret = createSecret(ego);

        storeSecret(userInjector, secret);

        String scope = "urn:patras:registration";
        createPseudonym(uprove, secret, scope);
    }

    private static void storeSysparams(SystemParameters sysparams,
            Injector userInjector) throws KeyManagerException {
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

        userKeyManager.storeSystemParameters(sysparams);
    }

    private static SystemParameters createSystemParams(
            CryptoEngineIssuer uproveIssuer, int keyLength,
            URI cryptoMechanism) throws CryptoEngineException {

        SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
        ObjectFactory of = new ObjectFactory();
        JAXBElement<SystemParameters> sysParamsElement = of
                .createSystemParameters(systemParameters);
        try {
            System.out.println(" - SysParams : "
                    + XmlUtils.toXml(sysParamsElement));
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        return systemParameters;
    }

    private static void storeSecret(Injector userInjector, Secret secret)
            throws CredentialManagerException {
        // Store secret.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);

        userCredentialManager.storeSecret(USERNAME, secret);
    }

    private static void createPseudonym(CryptoEngineUser uprove,
            Secret secret, String scope) throws CryptoEngineException {
        PseudonymWithMetadata pwm = uprove.createPseudonym(USERNAME, 
                URI.create("uprove-uri"), scope, true,
                secret.getSecretDescription().getSecretUID());

        ObjectFactory of = new ObjectFactory();
        JAXBElement<PseudonymWithMetadata> createPseudonymWithMetadata = of
                .createPseudonymWithMetadata(pwm);
        try {
            System.out.println(" - Pseudonym : "
                    + XmlUtils.toXml(createPseudonymWithMetadata));
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    private static Secret createSecret(EvidenceGenerationOrchestration ego) {
        Secret secret = ego.createSecret(USERNAME);

        ObjectFactory of = new ObjectFactory();
        JAXBElement<Secret> s = of.createSecret(secret);
        try {
            System.out.println(" - secret : " + XmlUtils.toXml(s));
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return secret;
    }

}

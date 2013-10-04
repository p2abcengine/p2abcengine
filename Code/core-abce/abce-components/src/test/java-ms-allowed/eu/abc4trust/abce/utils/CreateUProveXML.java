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
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.uprove.issuer.UProveCryptoEngineIssuerImpl;
import eu.abc4trust.cryptoEngine.uprove.user.UProveCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class CreateUProveXML {

    /**
     * @param args
     * @throws CredentialManagerException
     * @throws KeyManagerException
     */
    public static void main(String[] args) throws CredentialManagerException,
    KeyManagerException {
        UProveUtils uproveUtils = new UProveUtils();

        Injector issuerInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        uproveUtils.getIssuerServicePort()));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort()));

        int keyLength = 2048;
        URI cryptoMechanism = URI.create("urn:abc4trust:1.0:algorithm:uprove");
        UProveCryptoEngineIssuerImpl uproveIssuer = issuerInjector
                .getInstance(UProveCryptoEngineIssuerImpl.class);
        SystemParameters sysparams = createSystemParams(uproveIssuer,
                keyLength, cryptoMechanism);

        storeSysparams(sysparams, userInjector);

        UProveCryptoEngineUserImpl uprove = userInjector
                .getInstance(UProveCryptoEngineUserImpl.class);

        Secret secret = createSecret(uprove);

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
            UProveCryptoEngineIssuerImpl uproveIssuer, int keyLength,
            URI cryptoMechanism) {

        SystemParameters systemParameters = uproveIssuer.setupSystemParameters(
                keyLength, cryptoMechanism);
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

        userCredentialManager.storeSecret(secret);
    }

    private static void createPseudonym(UProveCryptoEngineUserImpl uprove,
            Secret secret, String scope) {
        PseudonymWithMetadata pwm = uprove.createPseudonym(
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

    private static Secret createSecret(UProveCryptoEngineUserImpl uprove) {
        Secret secret = uprove.createSecret();

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

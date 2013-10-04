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

package eu.abce4trust.ri.test.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class Generate1024bitUProveIssuerParameters {

    static ObjectFactory of = new ObjectFactory();

    @Ignore
    @Test
    public void issuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        this.issuanceProtocol(engineSuffix);
    }

    private void issuanceProtocol(String engineSuffix) throws Exception {

        String credentialSpecificationFilename = "credentialSpecificationSoderhamnSchool.xml";
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(FileSystem
                        .getInputStream("src/test/resources/soderhamn_pilot/"
                                + credentialSpecificationFilename), true);

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;

        // UProveUtils uproveUtils = new UProveUtils();
        //
        CryptoEngine cryptoEngine = CryptoEngine.UPROVE;
        // AbceConfigurationImpl configuration = this
        // .setupStorageFilesForConfiguration(IssuanceHelper
        // .getFileStoragePrefix("uprove-1024-stroage",
        // cryptoEngine), cryptoEngine);
        // configuration.setUProvePathToExe(new
        // UProveUtils().getPathToUProveExe()
        // .getAbsolutePath());
        // configuration.setUProvePortNumber(uproveUtils.getIssuerServicePort());
        // configuration
        // .setUProveNumberOfCredentialsToGenerate(50);
        // configuration.setUProveRetryTimeout(5);
        // Injector issuerInjector =
        // Guice.createInjector(ProductionModuleFactory
        // .newModule(configuration, cryptoEngine));
        //
        // IssuerAbcEngine issuerAbcEngine = issuerInjector
        // .getInstance(IssuerAbcEngine.class);

        IssuanceHelper.initInstanceForService(CryptoEngine.UPROVE, "target/p1",
                "target/p2");
        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();

        this.loadSystemParameters(issuanceHelper.keyManager);
        SystemParameters systemParameters = issuanceHelper.keyManager
                .getSystemParameters();
        if (systemParameters == null) {
            systemParameters = issuanceHelper
                    .createNewSystemParametersWithIdemixSpecificKeylength(1024,
                            1024);
            this.storeSystemParameters(systemParameters);
        }

        URI hash = CryptoUriUtil.getHashSha256();

        URI algorithmId = URI.create("urn:abc4trust:1.0:algorithm:"
                + engineSuffix);

        IssuerParameters issuerParameters = issuanceHelper
                .setupIssuerParameters(cryptoEngine, credentialSpecification,
                        systemParameters, URI.create(issuerParametersUid),
                        hash, algorithmId, null,
                        new LinkedList<FriendlyDescription>());

        this.storeIssuerParameters(issuerParameters);
    }

    private void loadSystemParameters(KeyManager keyManager) {
        File systemParametersOnDisk = new File(
                "uprove-1024-system-parameters.xml");
        try {
            @SuppressWarnings("unchecked")
            JAXBElement<SystemParameters> systemParameters = (JAXBElement<SystemParameters>) XmlUtils
            .getJaxbElementFromXml(new FileInputStream(
                    systemParametersOnDisk), false);
            keyManager.storeSystemParameters(systemParameters.getValue());
        } catch (UnsupportedEncodingException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (KeyManagerException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }


    }

    private void storeIssuerParameters(IssuerParameters issuerParameters)
            throws JAXBException, ParserConfigurationException, SAXException,
            IOException, FileNotFoundException {

        issuerParameters.setHashAlgorithm(URI
                .create("urn:abc4trust:1.0:hashalgorithm:sha-256"));
        List<Object> any = issuerParameters.getCryptoParams().getAny();
        Object v = any.get(4);

        JAXBElement<IssuerParameters> issuerParametersAsJaxb = of
                .createIssuerParameters(issuerParameters);

        Object v0 = any.get(0);
        Object v1 = any.get(1);
        Object v2 = any.get(2);
        Object v3 = any.get(3);

        Node eis = new UProveSerializer().createByteElement("eis", (byte[]) v0);
        any.set(0, eis);

        String issuerParametersAsXml = XmlUtils
                .toNormalizedXML(issuerParametersAsJaxb);

        File issuerParametersOnDisk = new File(
                "uprove-1024-issuer-parameters.xml");

        FileOutputStream fileOutputStream = new FileOutputStream(issuerParametersOnDisk);
        PrintStream printStream = new PrintStream(fileOutputStream);
        printStream.print(issuerParametersAsXml);
        printStream.close();
    }

    private void storeSystemParameters(SystemParameters systemParameters)
            throws JAXBException, ParserConfigurationException, SAXException,
            IOException, FileNotFoundException {

        SystemParameters serializedSystemParameters = SystemParametersUtil
                .serialize(systemParameters);

        JAXBElement<SystemParameters> systemParametersAsJaxb = of
                .createSystemParameters(serializedSystemParameters);

        String systemParametersAsXml = XmlUtils
                .toNormalizedXML(systemParametersAsJaxb);

        File issuerParametersOnDisk = new File(
                "uprove-1024-system-parameters.xml");

        FileOutputStream fileOutputStream = new FileOutputStream(
                issuerParametersOnDisk);
        PrintStream printStream = new PrintStream(fileOutputStream);
        printStream.print(systemParametersAsXml);
        printStream.close();
    }

}
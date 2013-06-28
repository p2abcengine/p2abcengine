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

package eu.abc4trust.services.issuer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.services.Constants;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITIssuanceProtocol extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9500/abce-services/issuer";

    @Test
    public void issuanceProtocolIdemix() throws Exception {

        String engineSuffix = "idemix";

        this.issuanceProtocol(engineSuffix);
    }

    @Test
    public void issuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        this.issuanceProtocol(engineSuffix);
    }

    private void issuanceProtocol(String engineSuffix) throws Exception {
        String credentialSpecificationFilename = "credentialSpecificationSimpleIdentitycard.xml";
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        AbstractHelper.getInputStream("src/test/resources/"
                                + credentialSpecificationFilename), true);


        this.copyCredentialSpecification(credentialSpecificationFilename);

        this.deleteStorageDirectory();

        IssuerServiceFactory issuerServiceFactory = new IssuerServiceFactory();

        SystemParameters systemParameters = issuerServiceFactory
                .getSystemParameters(80,
                        URI.create("urn:abc4trust:1.0:algorithm:idemix"));

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;
        IssuerParameters issuerParameters = issuerServiceFactory
                .getIssuerParameters(issuerParametersUid);


        Injector userInjector = Guice.createInjector(BridgingModuleFactory
                .newModule(new Random(1987), UProveUtils.UPROVE_COMMON_PORT));

        IssuMsgOrCredDesc userIm = issuerServiceFactory.issueCredential(
                credentialSpecification,
                issuerServiceFactory, systemParameters, issuerParametersUid,
 issuerParameters,
                userInjector).first();
        assertNull(userIm.im);
        assertNotNull(userIm.cd);
    }

    private void deleteStorageDirectory() {
        File directory1 = new File("target" + File.separatorChar
                + "issuer_storage");
        File directory2 = new File("abce-services" + File.separatorChar
                + "target" + File.separatorChar + "issuer_storage");

        this.delete(directory1);
        this.delete(directory2);

    }

    private void delete(File directory) {
        if (directory.exists()) {
            this.deleteBody(directory);
        }

    }

    private void deleteBody(File file) {
        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    this.deleteBody(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    private void copyCredentialSpecification(String filename)
            throws IOException {
        File file = null;
        file = new File("src" + File.separatorChar + "test"
                + File.separatorChar + "resources" + File.separatorChar
                + filename);


        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist on path: \"" + filename + "\"");
        }

        new File(Constants.CREDENTIAL_SPECIFICATION_FOLDER).mkdirs();

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(new File(
                Constants.CREDENTIAL_SPECIFICATION_FOLDER + File.separatorChar
                + filename));

        byte[] bytes = new byte[1];
        while (fis.read(bytes) != -1) {
            fos.write(bytes);
        }

        fis.close();
        fos.close();
    }
}
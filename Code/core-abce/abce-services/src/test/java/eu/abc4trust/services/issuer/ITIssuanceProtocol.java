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

package eu.abc4trust.services.issuer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.Constants;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITIssuanceProtocol extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9200/abce-services/issuer";

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
                        FileSystem.getInputStream("src/test/resources/"
                                + credentialSpecificationFilename), true);


        this.copyCredentialSpecification(credentialSpecificationFilename);
        
        IssuerServiceFactory issuerServiceFactory = new IssuerServiceFactory();

        issuerServiceFactory.deleteStorageDirectory();
        issuerServiceFactory.deleteResourcesDirectory();

        SystemParameters systemParameters = issuerServiceFactory
                .getSystemParameters(1024);

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;
        IssuerParameters issuerParameters = issuerServiceFactory
                .getIssuerParameters(issuerParametersUid);


        Injector userInjector = Guice.createInjector(IntegrationModuleFactory
                .newModule(new Random(1987)));

        IssuMsgOrCredDesc userIm = issuerServiceFactory.issueCredential(
                credentialSpecification,
                issuerServiceFactory, systemParameters, issuerParametersUid,
                issuerParameters,
                userInjector).first;
        assertNull(userIm.im);
        assertNotNull(userIm.cd);        
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
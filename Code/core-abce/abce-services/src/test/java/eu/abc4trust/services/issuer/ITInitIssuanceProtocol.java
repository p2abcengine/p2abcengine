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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.abc4trust.services.Constants;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.ObjectFactory;

public class ITInitIssuanceProtocol extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9200/abce-services/issuer";

    @Test
    public void initIssuanceProtocolIdemix() throws Exception {

        String engineSuffix = "idemix";

        this.initIssuanceProtocol(engineSuffix);
    }

    @Test
    public void initIssuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        this.initIssuanceProtocol(engineSuffix);
    }

    private void initIssuanceProtocol(String engineSuffix) throws IOException,
    JAXBException, UnsupportedEncodingException, SAXException {
        this.copyCredentialSpecification("credentialSpecificationSimpleIdentitycard.xml");

        IssuerServiceFactory issuerServiceFactory = new IssuerServiceFactory();
        issuerServiceFactory.getSystemParameters(1024);

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;
        issuerServiceFactory.getIssuerParameters(issuerParametersUid);

        IssuanceMessageAndBoolean issuanceMessageAndBoolean = issuerServiceFactory
                .initIssuanceProtocol(issuerParametersUid,
                        "/issuancePolicyAndAttributes.xml");

        assertNotNull(issuanceMessageAndBoolean);
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
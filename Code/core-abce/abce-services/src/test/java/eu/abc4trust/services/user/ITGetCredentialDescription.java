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

package eu.abc4trust.services.user;

import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;

import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.services.TestScenarioFactory;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.ObjectFactory;

public class ITGetCredentialDescription extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9200/abce-services/issuer";

    @Test
    public void issuanceProtocolIdemix() throws Exception {

        String engineSuffix = "idemix";

        this.deleteStorageDirectory("user_storage");

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        URI credentialUid = testScenarioFactory.issuanceProtocol(engineSuffix).first.getCredentialUID();

        CredentialDescription credentialDescription = userServiceFactory
                .getCredentialDescription(credentialUid);

        assertNotNull(credentialDescription);
    }

    @Test
    public void issuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        URI credentialUid = testScenarioFactory.issuanceProtocol(engineSuffix).first.getCredentialUID();

        CredentialDescription credentialDescription = userServiceFactory
                .getCredentialDescription(credentialUid);

        assertNotNull(credentialDescription);
    }

}
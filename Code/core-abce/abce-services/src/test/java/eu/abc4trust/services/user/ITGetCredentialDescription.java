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

    final String baseUrl = "http://localhost:9500/abce-services/issuer";

    @Test
    public void issuanceProtocolIdemix() throws Exception {

        String engineSuffix = "idemix";

        this.deleteStorageDirectory("user_storage");

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        URI credentialUid = testScenarioFactory.issuanceProtocol(engineSuffix);

        CredentialDescription credentialDescription = userServiceFactory
                .getCredentialDescription(credentialUid);

        assertNotNull(credentialDescription);
    }

    @Test
    public void issuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        URI credentialUid = testScenarioFactory.issuanceProtocol(engineSuffix);

        CredentialDescription credentialDescription = userServiceFactory
                .getCredentialDescription(credentialUid);

        assertNotNull(credentialDescription);
    }

}
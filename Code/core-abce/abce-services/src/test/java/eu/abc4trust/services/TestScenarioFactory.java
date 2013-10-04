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

package eu.abc4trust.services;

import java.net.URI;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.user.UserServiceFactory;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.util.XmlUtils;

public class TestScenarioFactory extends ITAbstract {

    public URI issuanceProtocol(String engineSuffix) throws Exception {
        UserServiceFactory userServiceFactory = new UserServiceFactory();

        IssuancePolicyAndAttributes issuancePolicyAndAttributes = userServiceFactory
                .loadIssuancePolicyAndAttributes("src/test/resources/issuancePolicyAndAttributes.xml");
        return this.issuanceProtocol(engineSuffix, issuancePolicyAndAttributes);

    }

    public URI issuanceProtocol(String engineSuffix,
            IssuancePolicyAndAttributes issuancePolicyAndAttributes)
                    throws Exception {

        this.deleteStorageDirectory("user_storage");

        String credentialSpecificationFilename = "credentialSpecificationSimpleIdentitycard.xml";
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("src/test/resources/"
                                + credentialSpecificationFilename), true);

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        return userServiceFactory.issuanceProtocol(engineSuffix,
                credentialSpecification, issuerParametersUid,
                issuancePolicyAndAttributes);
    }
}

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

package eu.abc4trust.services;

import java.net.URI;

import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.user.UserServiceFactory;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.util.XmlUtils;

public class TestScenarioFactory extends ITAbstract {

    public Pair<CredentialDescription, URI> issuanceProtocol(String engineSuffix) throws Exception {
        UserServiceFactory userServiceFactory = new UserServiceFactory();

        IssuancePolicyAndAttributes issuancePolicyAndAttributes = userServiceFactory
                .loadIssuancePolicyAndAttributes("src/test/resources/issuancePolicyAndAttributes.xml");
        return this.issuanceProtocol(engineSuffix, issuancePolicyAndAttributes);
    }

    public Pair<CredentialDescription, URI> issuanceProtocol(String engineSuffix,
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

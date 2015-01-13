//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.services.TestScenarioFactory;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;


public class ITCanbeSatisfied extends ITAbstract {
    static ObjectFactory of = new ObjectFactory();

    @Test
    public void canBeSatisfiedFalse() throws Exception {
        UserServiceFactory userServiceFactory = new UserServiceFactory();

        String engineSuffix = "idemix";

        IssuancePolicyAndAttributes issuancePolicyAndAttributes = userServiceFactory
                .loadIssuancePolicyAndAttributes("/issuancePolicyAndAttributes.xml");

        issuancePolicyAndAttributes.getAttribute().get(2)
        .setAttributeValue("1995-05-05Z");
        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        testScenarioFactory.issuanceProtocol(engineSuffix,
                issuancePolicyAndAttributes);

        boolean b = this.canBeSatisfied();
        assertFalse(b);
    }

    @Test
    public void canBeSatisfiedTrue() throws Exception {
        UserServiceFactory userServiceFactory = new UserServiceFactory();

        String engineSuffix = "idemix";

        IssuancePolicyAndAttributes issuancePolicyAndAttributes = userServiceFactory
                .loadIssuancePolicyAndAttributes("/issuancePolicyAndAttributes.xml");

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        testScenarioFactory.issuanceProtocol(engineSuffix,
                issuancePolicyAndAttributes);

        boolean b = this.canBeSatisfied();
        assertTrue(b);
    }


    private boolean canBeSatisfied() throws Exception {

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        String presentationPolicyFilename = "presentationPolicySimpleIdentitycard.xml";
        PresentationPolicyAlternatives presentationPolicy = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("src/test/resources/"
                                + presentationPolicyFilename), true);

        boolean b = userServiceFactory.canBeSatisfied(presentationPolicy);

        return b;
    }

}

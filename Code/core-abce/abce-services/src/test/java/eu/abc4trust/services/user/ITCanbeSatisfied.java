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

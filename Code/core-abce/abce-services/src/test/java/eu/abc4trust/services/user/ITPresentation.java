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

import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;

import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.services.TestScenarioFactory;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.util.XmlUtils;

public class ITPresentation extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9500/abce-services/issuer";

    @Test
    public void presentationIdemix() throws Exception {

        String engineSuffix = "idemix";

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        testScenarioFactory.issuanceProtocol(engineSuffix);

        this.presentCredential();
    }

    @Test
    public void presentationUProve() throws Exception {

        String engineSuffix = "uprove";

        TestScenarioFactory testScenarioFactory = new TestScenarioFactory();
        testScenarioFactory.issuanceProtocol(engineSuffix);

        this.presentCredential();
    }

    private void presentCredential() throws Exception {

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        String presentationPolicyFilename = "presentationPolicySimpleIdentitycard.xml";
        PresentationPolicyAlternatives presentationPolicy = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("src/test/resources/"
                                + presentationPolicyFilename), true);

        UiPresentationArguments uiPresentationArguments = userServiceFactory
                .createPresentationToken(presentationPolicy);

        assertNotNull(uiPresentationArguments.uiContext);
        UiPresentationReturn uiPresentationReturn = new UiPresentationReturn();
        uiPresentationReturn.chosenInspectors = new LinkedList<String>();
        uiPresentationReturn.chosenPolicy = 0;
        uiPresentationReturn.chosenPresentationToken = 0;
        uiPresentationReturn.chosenPseudonymList = 0;
        uiPresentationReturn.metadataToChange = new HashMap<String, PseudonymMetadata>();
        uiPresentationReturn.uiContext = uiPresentationArguments.uiContext;

        PresentationToken presentationToken = userServiceFactory
                .createPresentationToken(uiPresentationReturn);

        assertNotNull(presentationToken);
    }
}
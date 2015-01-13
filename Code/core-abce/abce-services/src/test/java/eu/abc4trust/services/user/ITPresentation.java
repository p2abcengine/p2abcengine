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

    final String baseUrl = "http://localhost:9200/abce-services/issuer";

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
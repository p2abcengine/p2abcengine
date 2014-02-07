//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.internal.user.evidenceGeneration;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

public class EvidenceGenerationOrchestrationImplTest extends EasyMockSupport {

  CryptoEngineUser cryptoEngine;
  EvidenceGenerationOrchestrationImpl ego;

  @Before
  public void setUpMocks() {
    cryptoEngine = createMock(CryptoEngineUser.class);

    ego = new EvidenceGenerationOrchestrationImpl(cryptoEngine);
  }

  @Test
  public void testCreateIssuanceToken() throws Exception {
    // Load input
    IssuanceToken issuanceToken =
        (IssuanceToken) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/simpleIssuanceToken.xml"), true);

    IssuanceTokenDescription itd = new IssuanceTokenDescription();
    List<URI> creduids = new ArrayList<URI>();
    List<Attribute> atts = new ArrayList<Attribute>();
    List<URI> chosenPseudonyms = new ArrayList<URI>();
    URI ctxt = new URI("context2");

    // Program the mocks
    expect(cryptoEngine.createIssuanceToken(same(itd), same(creduids), same(atts),
                                            same(chosenPseudonyms), eq(ctxt)))
        .andReturn(issuanceToken);
    replayAll();

    // Run the method with the mocks in place
    IssuanceMessage actualIssanceMessage = ego.createIssuanceToken(itd, creduids, atts,
                                                                   chosenPseudonyms, ctxt);
    
    // Check that we got a correctly formed issuanceMessage back
    verifyAll();
    ObjectFactory of = new ObjectFactory();
    JAXBElement<?> actual = of.createIssuanceMessage(actualIssanceMessage);
    InputStream expected =
        getClass().getResourceAsStream("/eu/abc4trust/sampleXml/issuanceMessageWithToken.xml");
    assertEquals(XmlUtils.toNormalizedXML(expected), XmlUtils.toNormalizedXML(actual));
  }

}

//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Test;

import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;

public class MyPresentationPolicyAlternativesTest {

  @Test
  public void testFindOrThrow() throws Exception {
    PresentationPolicyAlternatives ppa =
        (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotel.xml"), true);
    MyPresentationPolicyAlternatives myppa = new MyPresentationPolicyAlternatives(ppa);
    
    URI policyUri1 = new URI("http://www.sweetdreamsuites.com/policies/booking/standard");
    MyPresentationPolicy res = myppa.findOrThrow(policyUri1);
    assertEquals(policyUri1, res.getPolicyUri());
    
    URI policyUri2 = new URI("blabla");
    try {
      myppa.findOrThrow(policyUri2);
      fail("Expected TokenVerificationException");
    } catch(TokenVerificationException ex) {
      // expected
      assertTrue(ex.getMessage().contains("Cannot find"));
    }
  }

}

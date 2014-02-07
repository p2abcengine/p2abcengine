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

package eu.abc4trust.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class MyPresentationPolicyTest{
  
  @Test
  public void testIsSatisfiedByEmpty() throws Exception {
    PresentationTokenDescription pt =
      (PresentationTokenDescription) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/presentationTokens/emptyPolicy.xml"), true);
    PresentationPolicyAlternatives ppa =
      (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/presentationPolicies/emptyPolicy.xml"), true);
    MyPresentationPolicy mypp = new MyPresentationPolicy(ppa.getPresentationPolicy().get(0));
    
    assertTrue(mypp.isSatisfiedBy(pt, null, null));
  }
  
  @Test
  public void testIsSatisfiedBy() throws Exception {
    PresentationToken pt =
      (PresentationToken) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml"), true);
    PresentationTokenDescription ptd = pt.getPresentationTokenDescription();
    PresentationPolicyAlternatives ppa =
      (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotel.xml"), true);
    MyPresentationPolicy mypp = new MyPresentationPolicy(ppa.getPresentationPolicy().get(0));
    
    assertTrue(mypp.isSatisfiedBy(ptd, null, null));
  }

}

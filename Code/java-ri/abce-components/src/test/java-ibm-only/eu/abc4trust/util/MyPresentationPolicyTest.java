//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

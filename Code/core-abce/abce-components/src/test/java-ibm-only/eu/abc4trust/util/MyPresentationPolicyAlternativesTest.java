//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

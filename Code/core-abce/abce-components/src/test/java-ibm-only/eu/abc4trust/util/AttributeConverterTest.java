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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;

public class AttributeConverterTest {

  @Test
  public void attributeConverterTest() throws UnsupportedEncodingException, JAXBException,
      SAXException {
    AttributeConverter ac = new AttributeConverterImpl();

    Credential studentCard =
        (Credential) XmlUtils.getObjectFromXML(
            this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/credentials/credentialCreditcard.xml"), true);
    
    boolean hasInteger = false;
    for(Attribute a: studentCard.getCredentialDescription().getAttribute()) {
      BigInteger value = ac.getIntegerValueOrNull(a);
      assertNotNull(value);
      if("urn:abc4trust:1.0:encoding:integer:unsigned".equals(a.getAttributeDescription().getEncoding().toString())) {
        assertEquals(value, a.getAttributeValue());
        hasInteger = true;
      }
    }
    assertTrue(hasInteger);
  }
  
  @Test
  public void attributeConverterFromFunctionAndEncodingTest() throws UnsupportedEncodingException, JAXBException, SAXException {
    AttributeConverter ac = new AttributeConverterImpl();

    PresentationPolicyAlternatives ppa =
        (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(
            this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/presentationPolicies/simpleHotelPolicy.xml"), true);
    assertNotNull(ppa);
    PresentationPolicy pp = ppa.getPresentationPolicy().get(0);
    AttributePredicate ap = pp.getAttributePredicate().get(0);
    URI[] encodings = new URI[] {
      URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned"),
      URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned"),
      URI.create("urn:abc4trust:1.0:encoding:date:unix:signed"),
      URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned")  
    };
    URI dataType = URI.create("xs:date");
    Object constantValue = ap.getAttributeOrConstantValue().get(1);
    for(URI encoding: encodings) {
      AttributeDescription ad = new AttributeDescription();
      ad.setEncoding(encoding);
      ad.setDataType(dataType);
      BigInteger value = ac.getValueUnderEncoding(constantValue, ad);
      // System.out.println(value);
      assertNotNull(value);
      Object a = ac.recoverValueFromEncodedValue(value, ad);
      assertNotNull(a);
      // System.out.println(a);
    }
  }
}

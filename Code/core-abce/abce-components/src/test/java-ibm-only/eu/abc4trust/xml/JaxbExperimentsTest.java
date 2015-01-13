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

package eu.abc4trust.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.xml.util.XmlUtils;

public class JaxbExperimentsTest {
  @Test
  public void abcPrefixPresentTest() throws URISyntaxException, Exception {

    ObjectFactory of = new ObjectFactory();
    CredentialSpecification cs = of.createCredentialSpecification();
    cs.setKeyBinding(false);
    cs.setVersion("1.0");
    cs.setRevocable(true);
    cs.setSpecificationUID(new URI("abc4trust.eu/sample-specification"));

    AttributeDescriptions ads = of.createAttributeDescriptions();
    ads.setMaxLength(1024);

    AttributeDescription ad1 = of.createAttributeDescription();
    ad1.setType(new URI("sometype"));
    ad1.setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
    ad1.setDataType(new URI("xs:string"));
    ads.getAttributeDescription().add(ad1);
    AttributeDescription ad2 = of.createAttributeDescription();
    ad2.setType(new URI("someothertype"));
    ad2.setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
    ad2.setDataType(new URI("xs:string"));
    ads.getAttributeDescription().add(ad2);

    cs.setAttributeDescriptions(ads);

    String xml = XmlUtils.toXml(of.createCredentialSpecification(cs));
    // Check that the "abc:" prefix gets put in the XML
    assertTrue(xml.contains("abc:CredentialSpecification"));
  }

  @Test
  public void createsXsAnyCorrectly() throws Exception {
    ObjectFactory of = new ObjectFactory();

    PresentationPolicy pp = of.createPresentationPolicy();
    pp.setPolicyUID(new URI("policy-uid"));
    // No message
    // No pseudonym
    // No credentials
    // No attribute predicates

    UnknownAttributes ua = of.createUnknownAttributes();
    // empty for this test

    CredentialTemplate ct = of.createCredentialTemplate();
    ct.setCredentialSpecUID(new URI("spec1"));
    ct.setIssuerParametersUID(new URI("issuerparams"));
    ct.setUnknownAttributes(ua);

    IssuancePolicy ip = of.createIssuancePolicy();
    ip.setPresentationPolicy(pp);
    ip.setCredentialTemplate(ct);
    ip.setVersion("1.0");

    IssuanceMessage im = of.createIssuanceMessage();
    im.setContext(new URI("context1"));
    im.getContent().add(of.createIssuancePolicy(ip));

    String xml = XmlUtils.toXml(of.createIssuanceMessage(im));
    assertTrue(xml.contains("IssuancePolicy"));
    assertTrue(xml.contains("PresentationPolicy"));
    assertTrue(xml.contains("CredentialTemplate"));
  }

  @Test
  public void unmarshallWithXsAny() throws JAXBException, UnsupportedEncodingException,
      SAXException {
    IssuanceMessage issuanceMessage =
        (IssuanceMessage) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/issuanceMessageWithPolicy.xml"), true);

    IssuancePolicy ip =
        (IssuancePolicy) XmlUtils.unwrap(issuanceMessage.getContent(), IssuancePolicy.class);

    assertTrue(ip != null);
  }

  @Test
  public void testValidationError() throws UnsupportedEncodingException, JAXBException,
      SAXException {
    try {
      @SuppressWarnings("unused")
      IssuancePolicy issuancePolicy =
          (IssuancePolicy) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
              "/eu/abc4trust/sampleXml/invalidXmlIssuancePolicy.xml"), true);

      fail("Expected an UnmarshallException");
    } catch (UnmarshalException e) {
      // expected
    }
  }

  @Test
  public void testXmlConversionWithoutValidation() throws UnsupportedEncodingException,
      JAXBException, SAXException {
    // When validation is turned off, this should work just fine
    @SuppressWarnings("unused")
    IssuancePolicy issuancePolicy =
        (IssuancePolicy) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/invalidXmlIssuancePolicy.xml"), false);
  }
  
  @Test
  public void testXmlEqualsAfterMarhshall() throws Exception {
    IssuanceMessage issuanceMessage =
      (IssuanceMessage) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/issuanceMessageWithPolicy.xml"), true);
    
    InputStream rhs = getClass().getResourceAsStream("/eu/abc4trust/sampleXml/issuanceMessageWithPolicy.xml");
    
    ObjectFactory of = new ObjectFactory();
    assertEquals(XmlUtils.toNormalizedXML(of.createIssuanceMessage(issuanceMessage)),
      XmlUtils.toNormalizedXML(rhs));
  }
  
  @Test
  public void testXmlNotEquals() throws Exception {
    IssuanceMessage issuanceMessage =
      (IssuanceMessage) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/issuanceMessageWithPolicy.xml"), true);
    
    issuanceMessage.setContext(new URI("invalid-context"));
    
    InputStream rhs = getClass().getResourceAsStream("/eu/abc4trust/sampleXml/issuanceMessageWithPolicy.xml");
    
    ObjectFactory of = new ObjectFactory();
    assertFalse(XmlUtils.toNormalizedXML(of.createIssuanceMessage(issuanceMessage)).equals(
      XmlUtils.toNormalizedXML(rhs)));
  }
  
  @Test
  public void testUnmarshallXsAnyTypeWithUri() throws Exception {
    Attribute attribute =
      (Attribute) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
          "/eu/abc4trust/sampleXml/sampleUriAttribute.xml"), true);
    
    Object attributeValue = attribute.getAttributeValue();
    
    Element a = (Element)attributeValue;
    assertEquals("Hello/world", a.getTextContent());
  }
  
  @Test
  public void testSelfGeneratedJaxbBeans() throws Exception {
    int value = 42;
    Map<URI, PseudonymMetadata> pseudonyms = new HashMap<URI, PseudonymMetadata>();
    PseudonymMetadata pm = new PseudonymMetadata();
    FriendlyDescription fd = new FriendlyDescription();
    fd.setLang("en");
    fd.setValue("Hello");
    pm.getFriendlyPseudonymDescription().add(fd);
    pm.metadata = new Metadata();
    pm.metadata.any = new ArrayList<Object>();
    pm.friendlyPseudonymDescription = new ArrayList<FriendlyDescription>();
    pm.friendlyPseudonymDescription.add(new FriendlyDescription());
    pm.friendlyPseudonymDescription.get(0).setLang("en");
    pm.friendlyPseudonymDescription.get(0).setValue("Sample pseudonym");
    pseudonyms.put(URI.create("nym://9"), pm);
    pseudonyms.put(URI.create("nym://8"), pm);
    List<URI> inspector = new ArrayList<URI>();
    inspector.add(URI.create("ins://4"));
    inspector.add(URI.create("ins://5"));
    inspector.add(URI.create("ins://6"));
    List<URI> pseudonym = new ArrayList<URI>();
    pseudonym.add(URI.create("nym://1"));
    pseudonym.add(URI.create("nym://2"));
    SptdReturn sptd = new SptdReturn(value, pseudonyms, pseudonym, inspector);
    List<Object> lo = new ArrayList<Object>();
    lo.add("Blbabla");
    SitdReturn sitd = new SitdReturn(value, pseudonyms, pseudonym, inspector, lo);
    
    String m1 = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(sptd), false);
    String m2 = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(sitd), false);
    SptdReturn r1 = (SptdReturn) XmlUtils.getObjectFromXML(m1, false);
    SitdReturn r2 = (SitdReturn) XmlUtils.getObjectFromXML(m2, false);
  }
}

  

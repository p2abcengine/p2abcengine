//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.net.URI;
import java.util.List;

import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;

/**
 * A class providing better methods that the JAXB Attribute class.
 * @author enr
 *
 */
public class MyAttribute {
  
  private final Attribute attribute;
  private final MyAttributeValue value;
  
  public MyAttribute(Attribute att) {
    this.attribute = att;
    EnumAllowedValues eav = new EnumAllowedValues(att.getAttributeDescription());
    checkCompatibleEncoding();
    this.value = MyAttributeEncodingFactory.parseValueFromEncoding(getEncoding(), att.getAttributeValue(), eav);
  }
  
  private void checkCompatibleEncoding() {
    if(!MyAttributeEncodingFactory.getDatatypeFromEncoding(getEncoding()).equals(getDataType())) {
      throw new RuntimeException("Attribute with wrong encoding");
    }
  }

  public URI getDataType() {
    return attribute.getAttributeDescription().getDataType();
  }
  
  public URI getEncoding() {
    return attribute.getAttributeDescription().getEncoding();
  }

  public MyAttributeValue getValue() {
    return value;
  }

  public URI getType() {
    return attribute.getAttributeDescription().getType();
  }

  public Object getAttributeValue() {
    return attribute.getAttributeValue();
  }

  public List<FriendlyDescription> getFriendlyAttributeName() {
    return attribute.getAttributeDescription().getFriendlyAttributeName();
  }
  
  public Attribute getXmlAttribute() {
    return attribute;
  }
}

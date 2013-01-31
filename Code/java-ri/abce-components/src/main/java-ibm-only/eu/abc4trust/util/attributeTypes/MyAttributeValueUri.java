//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeTypes;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Element;

public class MyAttributeValueUri extends MyAttributeValue {

  private URI value;
  
  public MyAttributeValueUri(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof String) {
      try {
        value = new URI((String)attributeValue);
      } catch (URISyntaxException e) {
        throw new RuntimeException("Cannot parse attribute value as URI: " + e.getMessage());
      }
    } else if (attributeValue instanceof URI) {
      value = (URI) attributeValue;
    } else if(attributeValue instanceof Element) {
      String svalue = ((Element)attributeValue).getTextContent();
      try {
        value = new URI(svalue);
      } catch (URISyntaxException e) {
        throw new RuntimeException("Cannot parse attribute value as URI: " + e.getMessage());
      }
    } else {
      throw new RuntimeException("Cannot parse attribute value as URI: " + attributeValue.getClass());
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueUri) {
      return ((MyAttributeValueUri)lhs).value.equals(value);
    } else {
      return false;
    }
  }

  @Override
  public boolean isLess(MyAttributeValue myAttributeValue) {
    throw new UnsupportedOperationException("Can't call 'less' on an URI");
  }
  
  protected URI getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }

}

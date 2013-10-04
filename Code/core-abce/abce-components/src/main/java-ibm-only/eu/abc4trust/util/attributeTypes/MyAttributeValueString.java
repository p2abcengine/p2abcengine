//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeTypes;

import org.w3c.dom.Element;

public class MyAttributeValueString extends MyAttributeValue {

  private String value;
  
  public MyAttributeValueString(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof String) {
      value = ((String)attributeValue);
    } else if(attributeValue instanceof Element) {
      value = ((Element)attributeValue).getTextContent();
    } else {
      throw new RuntimeException("Cannot parse attribute value as string");
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueString) {
      return ((MyAttributeValueString)lhs).value.equals(value);
    } else {
      return false;
    }
  }

  @Override
  public boolean isLess(MyAttributeValue myAttributeValue) {
    throw new UnsupportedOperationException("Can't call 'less' on an enum string");
  }
  
  protected String getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }
}

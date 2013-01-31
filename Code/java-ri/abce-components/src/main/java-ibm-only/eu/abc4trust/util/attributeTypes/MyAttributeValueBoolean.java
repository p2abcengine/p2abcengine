//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeTypes;

import org.w3c.dom.Element;

public class MyAttributeValueBoolean extends MyAttributeValue {

  private boolean value;
  
  public MyAttributeValueBoolean(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof Boolean) {
      value = ((Boolean)attributeValue).booleanValue();
    } else if(attributeValue instanceof Element) {
      String svalue = ((Element)attributeValue).getTextContent();
      value = Boolean.parseBoolean(svalue);
    } else {
      throw new RuntimeException("Cannot parse attribute value as boolean");
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueBoolean) {
      return ((MyAttributeValueBoolean)lhs).value == value;
    } else {
      return false;
    }
  }

  @Override
  public boolean isLess(MyAttributeValue myAttributeValue) {
    throw new UnsupportedOperationException("Can't call 'less' on an URI");
  }
  
  protected boolean getValue() {
    return value;
  }

  @Override
  public Object getValueAsObject() {
    return Boolean.valueOf(value);
  }

}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeTypes;

import java.math.BigInteger;

import org.w3c.dom.Element;

public class MyAttributeValueInteger extends MyAttributeValue {

  private BigInteger value;
  
  public MyAttributeValueInteger(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof BigInteger) {
      value = ((BigInteger)attributeValue);
    } else if(attributeValue instanceof String) {
        value = new BigInteger((String)attributeValue);
    } else if(attributeValue instanceof Integer) {
      value = BigInteger.valueOf((Integer)attributeValue);
    } else if(attributeValue instanceof Long) {
      value = BigInteger.valueOf((Long)attributeValue);
    } else if(attributeValue instanceof Element) {
      String svalue = ((Element)attributeValue).getTextContent();
      value = new BigInteger(svalue);
    } else {
      throw new RuntimeException("Cannot parse attribute value as integer " + attributeValue.getClass());
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueInteger) {
      return ((MyAttributeValueInteger)lhs).value.equals(value);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isLess(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueInteger) {
      BigInteger lhsInt = ((MyAttributeValueInteger)lhs).value;
      return (value.compareTo(lhsInt) < 0);
    } else {
      return false;
    }
  }
  
  protected BigInteger getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }

}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;

import java.math.BigInteger;
import java.net.URI;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueBoolean;

public class MyEncodingBoolean extends MyAttributeValueBoolean implements MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
  
  public MyEncodingBoolean(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingBoolean) {
      return getIntegerValue().equals(((MyEncodingBoolean) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    if (getValue()) {
      return BigInteger.ONE;
    } else {
      return BigInteger.ZERO;
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    if(integerValue.equals(BigInteger.ONE)) {
      return true;
    } else if(integerValue.equals(BigInteger.ZERO)) {
      return false;
    } else {
      throw new RuntimeException("Cannot recover boolean value");
    }
  }
}

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
import eu.abc4trust.util.attributeTypes.MyAttributeValueInteger;

public class MyEncodingIntegerSigned extends MyAttributeValueInteger
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:integer:signed");

  public MyEncodingIntegerSigned(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if (getValue().compareTo(MyAttributeEncodingFactory.SIGNED_OFFSET.negate()) < 0) {
      throw new RuntimeException("Unsigned integer must be larger than -2^128");
    } else if (getValue().add(MyAttributeEncodingFactory.SIGNED_OFFSET).compareTo(MyAttributeEncodingFactory.MAX_VALUE) >= 0) {
      throw new RuntimeException("Unsigned integer must be smaller than 2^256-2^128");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    return getValue().add(MyAttributeEncodingFactory.SIGNED_OFFSET);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    return integerValue.subtract(MyAttributeEncodingFactory.SIGNED_OFFSET);
  }
}

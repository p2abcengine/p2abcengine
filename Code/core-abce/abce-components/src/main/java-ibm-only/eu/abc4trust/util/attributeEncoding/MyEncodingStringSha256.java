//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;

import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueString;

public class MyEncodingStringSha256 extends MyAttributeValueString implements MyAttributeEncoding {

  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
  public MyEncodingStringSha256(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingStringSha256) {
      return getIntegerValue().equals(((MyEncodingStringSha256) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(getValue().getBytes("UTF-8"));
      return MyAttributeEncodingFactory.byteArrayToInteger(md.digest());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    throw new RuntimeException("Cannot recover original value from hashed strings.");
  }
}

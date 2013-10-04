//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueUri;

public class MyEncodingUriUtf8 extends MyAttributeValueUri implements MyAttributeEncoding {

  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8");
  // 256 bits = 32 bytes, of which we need one to encode the length, so 31 bytes available
  private final int MAX_STRING_LENGTH = 31;
  
  public MyEncodingUriUtf8(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    
    try {
      if (getValue().toString().getBytes("UTF-8").length > MAX_STRING_LENGTH) {
        throw new RuntimeException("URI too long: cannot use UTF-8 encoding!");
      }
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingUriUtf8) {
      return getIntegerValue().equals(((MyEncodingUriUtf8) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    try {
      return MyAttributeEncodingFactory.byteArrayToIntegerWithLength(getValue().toString().getBytes("UTF-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    byte[] arr = MyAttributeEncodingFactory.decodeByteArrayWithLength(integerValue);
    try {
      String ret =  new String(arr, "UTF-8");
      return URI.create(ret);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}

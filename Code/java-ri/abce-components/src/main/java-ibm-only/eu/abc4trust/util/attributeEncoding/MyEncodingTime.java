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
import eu.abc4trust.util.attributeTypes.MyAttributeValueTime;

public class MyEncodingTime extends MyAttributeValueTime
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned");

  public MyEncodingTime(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long secondsInDay = 60*60*24;
    long result = unixTime%secondsInDay;
    // Fix modulo for negative numbers
    if(result < 0) {
      result += secondsInDay;
    }
    return BigInteger.valueOf(result);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    int seconds = integerValue.mod(BigInteger.valueOf(60)).intValue();
    integerValue = integerValue.divide(BigInteger.valueOf(60));
    int minutes = integerValue.mod(BigInteger.valueOf(60)).intValue();
    integerValue = integerValue.divide(BigInteger.valueOf(60));
    int hours = integerValue.intValue();
    return String.format("%02d:%02d:%02dZ", hours, minutes, seconds);
  }
}

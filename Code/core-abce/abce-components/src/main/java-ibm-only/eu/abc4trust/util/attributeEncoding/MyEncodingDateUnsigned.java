//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;

import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.msv.datatype.xsd.datetime.TimeZone;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValueDate;

public class MyEncodingDateUnsigned extends MyAttributeValueDate
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");

  public MyEncodingDateUnsigned(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if(getIntegerValue().compareTo(BigInteger.ZERO) < 0) {
      throw new RuntimeException("Unsigned dates must be >= 1970");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long result = unixTime/secondsInDay;
    return BigInteger.valueOf(result);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    try {
      long unixTime = integerValue.longValue() * secondsInDay;
      GregorianCalendar cal = new GregorianCalendar();
      cal.setGregorianChange(new Date(Long.MIN_VALUE));
      cal.setTimeZone(TimeZone.ZERO);
      cal.setTimeInMillis(unixTime*1000);
      XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      xmlCal.setHour(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setMinute(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setSecond(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setFractionalSecond(null);
      return xmlCal;
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}

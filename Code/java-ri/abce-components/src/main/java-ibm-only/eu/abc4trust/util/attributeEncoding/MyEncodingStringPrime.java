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
import eu.abc4trust.util.attributeTypes.EnumIndexer;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueString;

public class MyEncodingStringPrime extends MyAttributeValueString implements MyAttributeEncoding {

  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:string:prime");
  private static PrimeIndexer primeIndexer = new PrimeIndexer();
  
  public MyEncodingStringPrime(Object attributeValue, /*Nullable*/ EnumAllowedValues eav) {
    super(attributeValue, eav);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingStringPrime) {
      return getIntegerValue().equals(((MyEncodingStringPrime) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    EnumAllowedValues eav = getAllowedValues();
    if (eav == null) {
      throw new RuntimeException("Enum encoding comes without allowed values. Abort");
    }
    int index = eav.getPosition(getValue());
    return primeIndexer.getNthPrime(index);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, EnumAllowedValues eav) {
    Integer index = primeIndexer.getIndexOfPrime(integerValue);
    if(index == null) {
      throw new RuntimeException("Cannot recover enum value: not a prime");
    }
    if (eav == null) {
      throw new RuntimeException("EnumAllowedValues is null");
    }
    return eav.getAllowedValues().get(index);
  }
  
  public static EnumIndexer getEnumIndexer() {
    return primeIndexer;
  }
  
  @Override
  protected PrimeIndexer getIndexer() {
    return primeIndexer;
  }
}

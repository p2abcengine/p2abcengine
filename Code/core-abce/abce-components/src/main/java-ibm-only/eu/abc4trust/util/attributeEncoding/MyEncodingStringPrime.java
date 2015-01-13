//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
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

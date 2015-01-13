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

package eu.abc4trust.util;

import java.math.BigInteger;
import java.net.URI;

import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;

public class AttributeConverterImpl implements AttributeConverter {

  @Override
  public BigInteger getIntegerValueOrNull(Attribute attribute) {
    if(attribute.getAttributeValue() == null) {
      return null;
    }
    return new MyAttribute(attribute).getValue().getIntegerValueOrNull();
  }

  @Override
  public BigInteger getValueUnderEncoding(Object attributeOrConstant, AttributeDescription ad) {
    Attribute att = new Attribute();
    att.setAttributeValue(attributeOrConstant);
    att.setAttributeDescription(ad);
    return getIntegerValueOrNull(att);
  }

  @Override
  public Object recoverValueFromEncodedValue(BigInteger value, AttributeDescription ad) {
    URI encoding = ad.getEncoding();
    EnumAllowedValues eav = new EnumAllowedValues(ad);
    try {
      MyAttributeValue ret =
          MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, value, eav);
      return ret.getValueAsObject();
    } catch (RuntimeException ex) {
      return "!!! Could not inspect !!! " + value;
    }
  }

}

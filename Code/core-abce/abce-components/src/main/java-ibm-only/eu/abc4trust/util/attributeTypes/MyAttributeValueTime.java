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

package eu.abc4trust.util.attributeTypes;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

public class MyAttributeValueTime extends MyAttributeValue {

  private XMLGregorianCalendar value;
  
  public MyAttributeValueTime(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    try {
      if (attributeValue instanceof XMLGregorianCalendar) {
        value = ((XMLGregorianCalendar) attributeValue);
      } else if (attributeValue instanceof Element) {
        String svalue = ((Element) attributeValue).getTextContent();
        value = DatatypeFactory.newInstance().newXMLGregorianCalendar(svalue);
      } else if (attributeValue instanceof String) {
        String svalue = (String) attributeValue;
        value = DatatypeFactory.newInstance().newXMLGregorianCalendar(svalue);
      } else {
        throw new RuntimeException("Cannot parse attribute value as date (XMLGregorianCalendar)");
      }
      if (value.getYear() != DatatypeConstants.FIELD_UNDEFINED
          || value.getMonth() != DatatypeConstants.FIELD_UNDEFINED
          || value.getDay() != DatatypeConstants.FIELD_UNDEFINED) {
        throw new RuntimeException("Cannot parse attribute value as time since it contains a date");
      }
      if (value.getTimezone() != DatatypeConstants.FIELD_UNDEFINED && value.getTimezone() != 0) {
        throw new RuntimeException("Time value cannot contain a non-UTC timezone");
      }
      // All times are WITHOUT TIMEZONES and UTC
      value.setTimezone(0);
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException("Cannot parse attribute value as date (XMLGregorianCalendar)");
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueTime) {
      XMLGregorianCalendar lhsTime = ((MyAttributeValueTime)lhs).value;
      return (value.compare(lhsTime) == DatatypeConstants.EQUAL);
    } else {
      return false;
    }
  }

  @Override
  public boolean isLess(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueTime) {
      XMLGregorianCalendar lhsTime = ((MyAttributeValueTime)lhs).value;
      return (value.compare(lhsTime) == DatatypeConstants.LESSER);
    } else {
      return false;
    }
  }
  
  protected XMLGregorianCalendar getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }
}

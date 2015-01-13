//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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

package eu.abc4trust.ri.servicehelper.issuer;

import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

public class AttributeValueConverter {

    public Object convertValue(String dataType, Object value) {
        // this.log.info("convertValue : " + dataType + " : " + value);
        try {
            if ("xs:string".equals(dataType)) {
                return value.toString();
            } else if ("xs:integer".equals(dataType)) {
                if ((value instanceof Integer) || (value instanceof Long)
                        || (value instanceof BigInteger)) {
                    return value;
                }
                throw new IllegalStateException(
                        "Attributes of type integer must be either Integer, Long or BigInteger : "
                                + value.getClass());
            } else if ("xs:dateTime".equals(dataType)) {
                Calendar cal = this.valueToCalendar(value);
                return DatatypeConverter.printDateTime(cal);
            } else if ("xs:date".equals(dataType)) {
                SimpleDateFormat xmlDateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd'Z'");
                if (value instanceof String) {
                    try {
                        // verify that sting is correct formatted!
                        xmlDateFormat.parse((String) value);
                        return value;
                    } catch (ParseException e) {
                        throw new IllegalStateException(
                                "Attributes of type xs:date - when presented as String must correctly formattet : yyyy-MM-dd'Z' - value was : "
                                        + value);
                    }
                } else {
                    Calendar cal = this.valueToCalendar(value);
                    return xmlDateFormat.format(cal.getTime());
                }
            } else if ("xs:time".equals(dataType)) {
                Calendar cal = this.valueToCalendar(value);
                return DatatypeConverter.printTime(cal);
            } else if ("xs:anyURI".equals(dataType)) {
                return new URI(value.toString());
            } else if ("xs:boolean".equals(dataType)) {
                if (value instanceof Boolean) {
                    return value;
                } else if (value instanceof String) {
                    return Boolean.valueOf((String) value);
                }
                throw new IllegalStateException(
                        "Attributes of type xs:boolean must be either Boolean or String (value == 'true' or 'false') : "
                                + value.getClass());
            }
        } catch (IllegalStateException e) {
            // rethrow
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not convert value to correct data type : " + value
                    + " - of class : " + value.getClass()
                    + " - datatype " + dataType);
        }
        IssuanceHelper.log.info("UNKNON ?? [" + dataType + "]");

        throw new IllegalStateException(
                "Attributes dataType not supported (yet) : " + dataType);
    }

    public Calendar valueToCalendar(Object value) {
        if (value instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) value);
            return cal;
        } else if (value instanceof Calendar) {
            return (Calendar) value;
        }
        throw new IllegalStateException(
                "Attributes of type date/dateTime/time must be either Date or Calendar (or for xs:date correctly formattet String): "
                        + value.getClass());
    }
}

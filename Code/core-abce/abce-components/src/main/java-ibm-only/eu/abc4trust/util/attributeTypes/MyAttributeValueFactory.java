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

import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;

import eu.abc4trust.util.Constants;
import eu.abc4trust.util.Constants.OperationType;


public class MyAttributeValueFactory {


  public static MyAttributeValue parseValue(URI dataType, Object attributeValue, /*Nullable*/ EnumAllowedValues av) {
    StringTokenizer st = new StringTokenizer(dataType.toString(), ":");
    String type = "";
    while(st.hasMoreTokens()) {
      // ignore namespace
      type = st.nextToken();
    }

    if (type.equals(Constants.STRING_TYPE)) {
      return new MyAttributeValueString(attributeValue, av);
    } else if (type.equals(Constants.BOOLEAN_TYPE)) {
      return new MyAttributeValueBoolean(attributeValue, av);
    } else if (type.equals(Constants.INTEGER_TYPE) || type.equals("int") || type.equals("long")) {
      return new MyAttributeValueInteger(attributeValue, av);
    } else if (type.equals(Constants.DATE_TYPE)) {
      return new MyAttributeValueDate(attributeValue, av);
    } else if (type.equals(Constants.TIME_TYPE)) {
      return new MyAttributeValueTime(attributeValue, av);
    } else if (type.equals(Constants.DATETIME_TYPE)) {
      return new MyAttributeValueDateTime(attributeValue, av);
    } else if (type.equals(Constants.URI_TYPE)) {
      return new MyAttributeValueUri(attributeValue, av);
   } else {
      throw new RuntimeException("Cannot parse attribute data type: '" + type + "'");
    }
  }

  public static OperationType operationTypeOfFunction(URI functionAsUri) {
    String function = functionAsUri.toString();
    if (function.equals("urn:oasis:names:tc:xacml:1.0:function:string-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:boolean-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:time-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")) {
      return OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")) {
      return OperationType.GREATER;
    } else if (function
        .equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")) {
      return OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than")) {
      return OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")) {
      return OperationType.LESSEQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than")) {
      return OperationType.GREATER;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")) {
      return OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than")) {
      return OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")) {
      return OperationType.LESSEQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")) {
      return OperationType.GREATER;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")) {
      return OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")) {
      return OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")) {
      return OperationType.LESSEQ;
    } else if (function.equals("urn:abc4trust:1.0:function:string-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:date-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:time-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-not-equal")) {
      return OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:string-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:date-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:time-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-equal-oneof")) {
      return OperationType.EQUALONEOF;
    } else {
      throw new RuntimeException("Cannot parse function name: '" + function + "'");
    }
  }

  public static String returnTypeOfFunction(URI functionAsUri) {
    String function = functionAsUri.toString();
    if (function.equals("urn:oasis:names:tc:xacml:1.0:function:string-equal")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:boolean-equal")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:time-equal")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")) {
      return Constants.URI_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")) {
      return Constants.INTEGER_TYPE;
    } else if (function
        .equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:string-not-equal")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-not-equal")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-not-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:date-not-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:time-not-equal")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-not-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-not-equal")) {
      return Constants.URI_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:string-equal-oneof")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-equal-oneof")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-equal-oneof")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:date-equal-oneof")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:time-equal-oneof")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-equal-oneof")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-equal-oneof")) {
      return Constants.URI_TYPE;
    } else {
      throw new RuntimeException("Cannot parse function name: '" + function + "'");
    }
  }

  public static MyAttributeValue parseValueFromFunction(URI function, Object param) {
    String returnType = "";
    returnType = returnTypeOfFunction(function);
    return parseValue(URI.create(returnType), param, null);
  }

  public static boolean evaulateFunction(URI function, List<MyAttributeValue> arguments) {
    String returnType = returnTypeOfFunction(function);
    if(!checkTypes(arguments, returnType)) {
      throw new RuntimeException("Incorrect type of arguments, when evaluating function: '"
        + function + "'");
    }
    OperationType op = operationTypeOfFunction(function);
    if (!isArgumentNumberCorrect(op, arguments.size())) {
      throw new RuntimeException("Incorrect number of arguments when evaluating function: '"
          + function + "'");
    }
    switch (op) {
      case EQUAL:
        return arguments.get(0).isCompatibleAndEquals(arguments.get(1));
      case NOTEQUAL:
        return arguments.get(0).isCompatibleAndNotEquals(arguments.get(1));
      case EQUALONEOF: {
        MyAttributeValue first = arguments.get(0);
        for (int i = 1; i < arguments.size(); ++i) {
          if (first.isCompatibleAndEquals(arguments.get(i))) {
            return true;
          }
        }
        return false;
      }
      case LESS:
        return arguments.get(0).isCompatibleAndLess(arguments.get(1));
      case LESSEQ:
        return arguments.get(0).isCompatibleAndLessOrEqual(arguments.get(1));
      case GREATER:
        return arguments.get(1).isCompatibleAndLess(arguments.get(0));
      case GREATEREQ:
        return arguments.get(1).isCompatibleAndLessOrEqual(arguments.get(0));
      default:
        throw new RuntimeException("Problem with evaluating function: '" + function + "'");
    }
  }

  private static boolean checkTypes(List<MyAttributeValue> arguments, String returnType) {
    Class<?> expectedClass = getClassOfType(returnType);
    for(MyAttributeValue argument:arguments) {
      if ( ! expectedClass.isInstance(argument)) {
        return false;
      }
    }
    return true;
  }
  
  private static Class<?> getClassOfType(String type) {
    if (type.equals(Constants.STRING_TYPE)) {
      return MyAttributeValueString.class;
    } else if (type.equals(Constants.BOOLEAN_TYPE)) {
      return MyAttributeValueBoolean.class;
    } else if (type.equals(Constants.INTEGER_TYPE)) {
      return MyAttributeValueInteger.class;
    } else if (type.equals(Constants.DATE_TYPE)) {
      return MyAttributeValueDate.class;
    } else if (type.equals(Constants.TIME_TYPE)) {
      return MyAttributeValueTime.class;
    } else if (type.equals(Constants.DATETIME_TYPE)) {
      return MyAttributeValueDateTime.class;
    } else if (type.equals(Constants.URI_TYPE)) {
      return MyAttributeValueUri.class;
    } else {
      throw new RuntimeException("Cannot parse attribute data type: '" + type + "'");
    }
  }

  private static boolean isArgumentNumberCorrect(OperationType op, int size) {
    switch (op) {
      case EQUAL:
        return (size == 2);
      case NOTEQUAL:
        return (size == 2);
      case EQUALONEOF:
        return (size >= 2);
      case LESS:
        return (size == 2);
      case LESSEQ:
        return (size == 2);
      case GREATER:
        return (size == 2);
      case GREATEREQ:
        return (size == 2);
      default:
        throw new RuntimeException("Problem with evaluating operation: '" + op + "'");
    }
  }

}

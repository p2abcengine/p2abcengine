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

import static eu.abc4trust.util.attributeTypes.MyAttributeValueFactory.evaulateFunction;
import static eu.abc4trust.util.attributeTypes.MyAttributeValueFactory.parseValueFromFunction;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.abc4trust.util.attributeEncoding.MyEncodingStringSha256;
import eu.abc4trust.util.attributeEncoding.MyEncodingStringUtf8;

public class MyAttributeValueFactoryTest {

  private final URI stringEqualFunction;
  private final URI stringEqualOneof;

  public MyAttributeValueFactoryTest() throws Exception {
    stringEqualFunction = new URI("urn:oasis:names:tc:xacml:1.0:function:string-equal");
    stringEqualOneof = new URI("urn:abc4trust:1.0:function:string-equal-oneof");
  }


  @Test
  public void testParseValueFromFunction() throws Exception {
    assertEquals(MyAttributeValueString.class, parseValueFromFunction(stringEqualFunction, "hello")
        .getClass());
    assertEquals(MyAttributeValueDate.class, parseValueFromFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:date-greater-than"), "2010-01-01")
        .getClass());
  }

  @Test
  public void testMismatchedTypes() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    arguments.clear();
    arguments.add(new MyAttributeValueString("hello", null));
    arguments.add(new MyAttributeValueUri("hello", null));
    try {
      evaulateFunction(stringEqualFunction, arguments);
      fail("Expected an exception");
    } catch (Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Incorrect type of arguments"));
    }
  }
  
  @Test
  public void testEncodings() throws Exception {
    final String HELLO = "hello";
    
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    arguments.clear();
    arguments.add(new MyEncodingStringSha256(HELLO, null));
    arguments.add(new MyEncodingStringUtf8(HELLO, null));
    assertFalse(evaulateFunction(stringEqualFunction, arguments));
    
    arguments.clear();
    arguments.add(new MyEncodingStringUtf8(HELLO, null));
    arguments.add(new MyEncodingStringUtf8(HELLO, null));
    assertTrue(evaulateFunction(stringEqualFunction, arguments));
    
    arguments.clear();
    arguments.add(new MyEncodingStringSha256(HELLO, null));
    arguments.add(new MyEncodingStringSha256(HELLO, null));
    assertTrue(evaulateFunction(stringEqualFunction, arguments));
    
    arguments.clear();
    arguments.add(new MyAttributeValueString(HELLO, null));
    arguments.add(new MyEncodingStringSha256(HELLO, null));
    assertTrue(evaulateFunction(stringEqualFunction, arguments));
  }

  @Test
  public void testIncorrectNumberOfArguments() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    // Too much
    arguments.clear();
    arguments.add(new MyAttributeValueString("hello", null));
    arguments.add(new MyAttributeValueString("hello", null));
    arguments.add(new MyAttributeValueString("hello", null));
    try {
      evaulateFunction(stringEqualFunction, arguments);
      fail("Expected an exception");
    } catch (Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Incorrect number of arguments"));
    }

    // Too few
    arguments.clear();
    arguments.add(new MyAttributeValueString("hello", null));
    try {
      evaulateFunction(stringEqualFunction, arguments);
      fail("Expected an exception");
    } catch (Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Incorrect number of arguments"));
    }

    // No arguments
    arguments.clear();
    try {
      evaulateFunction(stringEqualFunction, arguments);
      fail("Expected an exception");
    } catch (Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Incorrect number of arguments"));
    }
  }

  @Test
  public void testEqualsOneOf() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    // First
    arguments.clear();
    arguments.add(new MyAttributeValueString("arg0", null));
    arguments.add(new MyAttributeValueString("arg0", null));
    arguments.add(new MyAttributeValueString("arg1", null));
    arguments.add(new MyAttributeValueString("arg2", null));
    arguments.add(new MyAttributeValueString("arg3", null));
    assertTrue(evaulateFunction(stringEqualOneof, arguments));

    // Middle
    arguments.set(0, new MyAttributeValueString("arg2", null));
    assertTrue(evaulateFunction(stringEqualOneof, arguments));

    // End
    arguments.set(0, new MyAttributeValueString("arg3", null));
    assertTrue(evaulateFunction(stringEqualOneof, arguments));

    // Not present
    arguments.set(0, new MyAttributeValueString("hello", null));
    assertFalse(evaulateFunction(stringEqualOneof, arguments));

    // Not present, with some duplicate arguments
    arguments.clear();
    arguments.add(new MyAttributeValueString("arg0", null));
    arguments.add(new MyAttributeValueString("arg1", null));
    arguments.add(new MyAttributeValueString("arg1", null));
    arguments.add(new MyAttributeValueString("arg1", null));
    assertFalse(evaulateFunction(stringEqualOneof, arguments));
  }

  @Test
  public void testEqualsForAllTypes() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    // String
    arguments.clear();
    arguments.add(new MyAttributeValueString("hello", null));
    arguments.add(new MyAttributeValueString("hello", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:string-not-equal"), arguments));

    // Boolean
    arguments.clear();
    arguments.add(new MyAttributeValueBoolean(Boolean.valueOf(true), null));
    arguments.add(new MyAttributeValueBoolean(true, null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:boolean-not-equal"), arguments));

    // Integer
    arguments.clear();
    arguments.add(new MyAttributeValueInteger(Integer.valueOf(42), null));
    arguments.add(new MyAttributeValueInteger(BigInteger.valueOf(42), null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:integer-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:integer-not-equal"), arguments));

    // Date with timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04Z", null));
    arguments.add(new MyAttributeValueDate("2011-12-04Z", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:date-not-equal"), arguments));

    // Date without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04", null));
    arguments.add(new MyAttributeValueDate("2011-12-04", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));

    // Date with and without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04Z", null));
    arguments.add(new MyAttributeValueDate("2011-12-04", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));

    // Time without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueTime("17:39:42", null));
    arguments.add(new MyAttributeValueTime("17:39:42", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:time-equal"),
        arguments));

    // DateTime with timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:42:42Z", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:42:42Z", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // DateTime without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:dateTime-not-equal"),
        arguments));

    // DateTime with and without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14Z", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // DateTime across timezone (same)
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T19:44:15+02:00", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T11:44:15-06:00", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // URI
    arguments.clear();
    arguments.add(new MyAttributeValueUri(new URI("abc4trust.eu/blabla"), null));
    arguments.add(new MyAttributeValueUri(new URI("abc4trust.eu/blabla"), null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"),
        arguments));
    assertFalse(evaulateFunction(new URI("urn:abc4trust:1.0:function:anyURI-not-equal"), arguments));
  }

  @Test
  public void testUnequalsForAllTypes() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    // String
    arguments.clear();
    arguments.add(new MyAttributeValueString("hello", null));
    arguments.add(new MyAttributeValueString("world", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:string-not-equal"), arguments));

    // Boolean
    arguments.clear();
    arguments.add(new MyAttributeValueBoolean(Boolean.valueOf(true), null));
    arguments.add(new MyAttributeValueBoolean(false, null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:boolean-not-equal"), arguments));

    // Integer
    arguments.clear();
    arguments.add(new MyAttributeValueInteger(6 * 9, null));
    arguments.add(new MyAttributeValueInteger(BigInteger.valueOf(42), null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:integer-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:integer-not-equal"), arguments));

    // Date with timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04Z", null));
    arguments.add(new MyAttributeValueDate("2011-12-05Z", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:date-not-equal"), arguments));

    // Date without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04", null));
    arguments.add(new MyAttributeValueDate("2011-12-05", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));

    // Date with and without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-12-04Z", null));
    arguments.add(new MyAttributeValueDate("2011-12-05", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
        arguments));
    
    // Time without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueTime("17:39:42", null));
    arguments.add(new MyAttributeValueTime("17:31:42", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:time-equal"),
        arguments));

    // DateTime with timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:42:42Z", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:42:41Z", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // DateTime without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-05T17:43:14", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:dateTime-not-equal"), arguments));

    // DateTime with and without timezone
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:14Z", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T17:43:15", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // DateTime across timezone (different)
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-12-04T19:44:15+02:00", null));
    arguments.add(new MyAttributeValueDateTime("2011-12-04T11:44:15-07:00", null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
        arguments));

    // URI
    arguments.clear();
    arguments.add(new MyAttributeValueUri(new URI("abc4trust.eu/hello"), null));
    arguments.add(new MyAttributeValueUri(new URI("abc4trust.eu/world"), null));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"),
        arguments));
    assertTrue(evaulateFunction(new URI("urn:abc4trust:1.0:function:anyURI-not-equal"), arguments));
  }

  @Test
  public void testComparisons() throws Exception {
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    arguments.clear();
    arguments.add(new MyAttributeValueInteger(4, null));
    arguments.add(new MyAttributeValueInteger(7, null));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"), arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal"), arguments));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:integer-less-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueInteger(42, null));
    arguments.add(new MyAttributeValueInteger(42, null));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"), arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:integer-less-than"), arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueInteger(142, null));
    arguments.add(new MyAttributeValueInteger(42, null));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"), arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:integer-less-than"), arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueDate("2010-10-10", null));
    arguments.add(new MyAttributeValueDate("2011-11-11", null));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:date-greater-than"), arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal"), arguments));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-less-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueDate("2010-01-01", null));
    arguments.add(new MyAttributeValueDate("2010-01-01", null));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:date-greater-than"), arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-less-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueDate("2011-11-11", null));
    arguments.add(new MyAttributeValueDate("2010-10-10", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-greater-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:date-less-than"),
        arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"), arguments));
    
    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2010-10-10T10:10:10Z", null));
    arguments.add(new MyAttributeValueDateTime("2011-11-11T10:10:10Z", null));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than"), arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal"), arguments));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2010-01-01T10:10:10Z", null));
    arguments.add(new MyAttributeValueDateTime("2010-01-01T10:10:10Z", null));
    assertFalse(evaulateFunction(
        new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than"), arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal"), arguments));

    arguments.clear();
    arguments.add(new MyAttributeValueDateTime("2011-11-11T10:10:10Z", null));
    arguments.add(new MyAttributeValueDateTime("2010-10-10T10:10:10Z", null));
    assertTrue(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than"),
        arguments));
    assertTrue(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal"), arguments));
    assertFalse(evaulateFunction(new URI("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than"),
        arguments));
    assertFalse(evaulateFunction(new URI(
        "urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal"), arguments));
  }
}

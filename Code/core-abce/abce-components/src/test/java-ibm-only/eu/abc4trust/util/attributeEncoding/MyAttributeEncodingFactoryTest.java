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

import static eu.abc4trust.util.attributeTypes.MyAttributeValueFactory.evaulateFunction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueFactory;
import eu.abc4trust.util.attributeTypes.MyAttributeValueString;

public class MyAttributeEncodingFactoryTest {

  private static final URI ENCODING_URI_SHA = URI.create("urn:abc4trust:1.0:encoding:anyUri:sha-256");
  private static final URI ENCODING_STRING_SHA = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
  private static final URI ENCODING_URI_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8");
  private static final URI ENCODING_STRING_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:string:utf-8");
  private static final URI ENCODING_STRING_PRIME = URI.create("urn:abc4trust:1.0:encoding:string:prime");
  
  //héllø
  static final String TEST_STRING = "h\u00E9ll\u00F8";
  static final URI TEST_STRING_URI = URI.create(TEST_STRING);
  private static final String HASH_OF_TEST_STRING =
      "1a1c79e944a1e3e83ed79157b6c09660d8fe0b2361ab3a177c12276e5d8474a6";
  private static final String HASH_OF_TEST_STRING_DECIMAL =
      "11810447181182089587910711761501265245927260413470757310280760064159296812198";
  
  @Test
  public void testStringSha256() {
    // We have that  SHA-256(TEST_STRING) == 1a1c79...
    final  BigInteger expectedHex = new BigInteger(HASH_OF_TEST_STRING, 16);
    // same in decimal
    final BigInteger expectedDec = new BigInteger(HASH_OF_TEST_STRING_DECIMAL);
    assertEquals(expectedHex, expectedDec);
    
    MyAttributeValue v = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_SHA, TEST_STRING, null);
    
    assertEquals(v.getIntegerValueOrNull(), expectedHex);
  }
  
  @Test
  public void testStringUtf8() {
    final BigInteger expectedHex = new BigInteger("68C3A96C6CC3B807", 16);
    final BigInteger expectedDec = new BigInteger("7549063683549411335", 10);
    assertEquals(expectedHex, expectedDec);
    
    MyAttributeValue v = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, TEST_STRING, null);
    
    assertEquals(v.getIntegerValueOrNull(), expectedHex);
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_STRING_UTF_8, v.getIntegerValueOrNull(), null);
    
    MyAttributeValue longString = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, "1234567890123456789012345678901", null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_STRING_UTF_8, longString.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testStringDifferentEncodings() {
    final URI stringDatatype = URI.create("xs:string");
    final BigInteger expectedUtf8 = new BigInteger("68C3A96C6CC3B807", 16);
    final BigInteger expectedSha = new BigInteger(HASH_OF_TEST_STRING, 16);
    
    List<MyAttributeValue> val = new ArrayList<MyAttributeValue>();
    val.add(MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, TEST_STRING, null));
    val.add(MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_SHA, TEST_STRING, null));
    val.add(MyAttributeValueFactory.parseValue(stringDatatype, TEST_STRING, null));
    
    for(MyAttributeValue v: val) {
      assertEquals(expectedUtf8, v.getIntegerValueUnderEncoding(ENCODING_STRING_UTF_8));
      assertEquals(expectedSha, v.getIntegerValueUnderEncoding(ENCODING_STRING_SHA));
    }
  }
  
  @Test
  public void testGetEncodings() {
    final URI stringDatatype = URI.create("xs:string");
    final BigInteger expectedUtf8 = new BigInteger("68C3A96C6CC3B807", 16);
    final BigInteger expectedSha = new BigInteger(HASH_OF_TEST_STRING, 16);
    
    MyAttributeValue v1 = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, TEST_STRING, null);
    MyAttributeValue v2 = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_SHA, TEST_STRING, null);
    MyAttributeValue v3 = MyAttributeValueFactory.parseValue(stringDatatype, TEST_STRING, null);
    
    Set<MyAttributeValue> set1 = new HashSet<MyAttributeValue>();
    set1.add(v1);
    set1.add(v3);
    assertEquals(expectedUtf8, v3.getCompatibleIntegerValue(set1));
    
    Set<MyAttributeValue> set2 = new HashSet<MyAttributeValue>();
    set2.add(v2);
    set2.add(v3);
    assertEquals(expectedSha, v3.getCompatibleIntegerValue(set2));
    
    Set<MyAttributeValue> set3 = new HashSet<MyAttributeValue>();
    set3.add(v3);
    try {
      v3.getCompatibleIntegerValue(set3);
      fail();
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("No encoding"));
    }
    
    Set<MyAttributeValue> set4 = new HashSet<MyAttributeValue>();
    set4.add(v1);
    set4.add(v2);
    set4.add(v3);
    try {
      v3.getCompatibleIntegerValue(set4);
      fail();
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("Too many encodings"));
    }
    
    assertEquals(v1.getEncodingOrNull(), ENCODING_STRING_UTF_8);
    assertEquals(v2.getEncodingOrNull(), ENCODING_STRING_SHA);
    assertEquals(v3.getEncodingOrNull(), null);
    

  }
  
  @Test
  public void overlongString() {
    // Max string length is 31 characters
    final String maxlengthString = "1234567890123456789012345678901";
    final String overlongString = maxlengthString + "X";
    
    MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, maxlengthString, null);
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_UTF_8, overlongString, null);
      fail("Expected an exception due to overlong string");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("too long"));
    }
  }
  
  @Test
  public void overlongUri() {
    // Max string length is 31 characters
    final URI maxlengthString = URI.create("1234567890123456789012345678901");
    final URI overlongString = URI.create(maxlengthString + "X");
    
    MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_UTF_8, maxlengthString, null);
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_UTF_8, overlongString, null);
      fail("Expected an exception due to overlong string");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("too long"));
    }
  }
  
  @Test
  public void testUriSha256() {
    // We have that  SHA-256(TEST_STRING) == 1a1c79...
    final  BigInteger expectedHex = new BigInteger(HASH_OF_TEST_STRING, 16);
    // same in decimal
    final BigInteger expectedDec = new BigInteger(HASH_OF_TEST_STRING_DECIMAL);
    assertEquals(expectedHex, expectedDec);
    
    MyAttributeValue v = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_SHA, TEST_STRING_URI, null);
    
    assertEquals(v.getIntegerValueOrNull(), expectedHex);
  }
  
  @Test
  public void testUriUtf8() {
    final BigInteger expectedHex = new BigInteger("68C3A96C6CC3B807", 16);
    final BigInteger expectedDec = new BigInteger("7549063683549411335", 10);
    assertEquals(expectedHex, expectedDec);
    
    MyAttributeValue v = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_UTF_8, TEST_STRING_URI, null);
    
    assertEquals(v.getIntegerValueOrNull(), expectedHex);
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_URI_UTF_8, v.getIntegerValueOrNull(), null);
    MyAttributeValue longString = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_UTF_8, "1234567890123456789012345678901", null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_URI_UTF_8, longString.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testUriDifferentEncodings() {
    final URI uriDatatype = URI.create("xs:anyURI");
    final BigInteger expectedUtf8 = new BigInteger("68C3A96C6CC3B807", 16);
    final BigInteger expectedSha = new BigInteger(HASH_OF_TEST_STRING, 16);
    
    List<MyAttributeValue> val = new ArrayList<MyAttributeValue>();
    val.add(MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_UTF_8, TEST_STRING_URI, null));
    val.add(MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_URI_SHA, TEST_STRING_URI, null));
    val.add(MyAttributeValueFactory.parseValue(uriDatatype, TEST_STRING_URI, null));
    
    for(MyAttributeValue v: val) {
      assertEquals(expectedUtf8, v.getIntegerValueUnderEncoding(ENCODING_URI_UTF_8));
      assertEquals(expectedSha, v.getIntegerValueUnderEncoding(ENCODING_URI_SHA));
    }
  }
  
  @Test
  public void testDateTimeUnixUnsigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:unsigned");
    
    final String today = "2012-01-30T23:05:27Z";
    final String min   = "1970-01-01T00:00:00Z";
    final String past  = "1969-12-31T23:59:59Z";
    final BigInteger unixToday = BigInteger.valueOf(1327964727);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    
    assertEquals(unixToday, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, m.getIntegerValueOrNull());
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
      fail("Expected error on dateTime <= 1970");
    } catch(RuntimeException e) {
      assertTrue(e.getMessage().contains(">= 1970"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testDateTimeUnixSigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed");
    
    final String today = "2012-01-30T23:05:27Z";
    final String min   = "1970-01-01T00:00:00Z";
    final String past  = "1969-12-31T23:59:59Z";
    final String past2  = "1582-10-14T00:00:00Z";
    final BigInteger unixToday = BigInteger.valueOf(1327964727);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    MyAttributeValue p = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
    MyAttributeValue p2 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past2, null);
    
    assertEquals(unixToday.add(MyAttributeEncodingFactory.SIGNED_OFFSET), t.getIntegerValueOrNull());
    assertEquals(MyAttributeEncodingFactory.SIGNED_OFFSET, m.getIntegerValueOrNull());
    assertEquals(BigInteger.valueOf(-1).add(MyAttributeEncodingFactory.SIGNED_OFFSET), p.getIntegerValueOrNull());
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p2.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testDateUnixSigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:date:unix:signed");
    
    final String today = "2012-01-30";
    final String min   = "1970-01-01";
    final String past  = "1969-12-31";
    final String past2  = "1582-10-03"; // before the Gregorian calendar change
    final BigInteger unixToday = BigInteger.valueOf(15369);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    MyAttributeValue p = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
    MyAttributeValue p2 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past2, null);
    
    assertEquals(unixToday.add(MyAttributeEncodingFactory.SIGNED_OFFSET), t.getIntegerValueOrNull());
    assertEquals(MyAttributeEncodingFactory.SIGNED_OFFSET, m.getIntegerValueOrNull());
    assertEquals(BigInteger.valueOf(-1).add(MyAttributeEncodingFactory.SIGNED_OFFSET), p.getIntegerValueOrNull());
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p2.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testDateUnixUnsigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");
    final String today = "2012-01-30";
    final String min   = "1970-01-01Z";
    final String past  = "1969-12-31";
    final BigInteger unixToday = BigInteger.valueOf(15369);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    
    assertEquals(unixToday, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, m.getIntegerValueOrNull());
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
      fail("Unsigned dates before 1970 should fail");    
    } catch(RuntimeException e) {
      assertTrue(e.getMessage().contains(">= 1970"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testDateSince1870() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned");
    final String today = "2012-01-30";
    final String min   = "1870-01-01Z";
    final String past  = "1869-12-31";
    final BigInteger unixToday = BigInteger.valueOf(15369+36524);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    
    assertEquals(unixToday, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, m.getIntegerValueOrNull());
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
      fail("Unsigned dates before 1870 should fail");    
    } catch(RuntimeException e) {
      assertTrue(e.getMessage().contains(">= 1870"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testDateSince2010() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned");
    final String today = "2012-01-30";
    final String min   = "2010-01-01Z";
    final String past  = "1999-12-31";
    final BigInteger unixToday = BigInteger.valueOf(15369-14610);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, today, null);
    MyAttributeValue m = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, min, null);
    
    assertEquals(unixToday, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, m.getIntegerValueOrNull());
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, past, null);
      fail("Unsigned dates before 2010 should fail");    
    } catch(RuntimeException e) {
      assertTrue(e.getMessage().contains(">= 2010"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, m.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testTime() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned");
    
    String testTime = "23:05:27";
    BigInteger answer = BigInteger.valueOf(83127);
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, testTime, null);
    MyAttributeValue midnight = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, "00:00:00", null);
    MyAttributeValue max = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, "23:59:59Z", null);
    
    assertEquals(answer, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, midnight.getIntegerValueOrNull());
    assertEquals(BigInteger.valueOf(86399), max.getIntegerValueOrNull());
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, midnight.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, max.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testBoolean() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
    
    MyAttributeValue t = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, Boolean.TRUE, null);
    MyAttributeValue f = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, Boolean.FALSE, null);
    
    assertEquals(BigInteger.ONE, t.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, f.getIntegerValueOrNull());
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, t.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, f.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testIntegerUnsigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:integer:unsigned");
    
    // A number less than 2^128
    String prime128bits = "340282366920938463463374607431768211297";
    // A number less than 2^256
    String prime256bits = "115792089237316195423570985008687907853269984665640564039457584007913129639747";
    
    MyAttributeValue z = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.ZERO, null);
    MyAttributeValue o = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.ONE, null);
    MyAttributeValue p128 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, new BigInteger(prime128bits), null);
    MyAttributeValue p256 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, new BigInteger(prime256bits), null);
    
    assertEquals(BigInteger.ZERO, z.getIntegerValueOrNull());
    assertEquals(BigInteger.ONE, o.getIntegerValueOrNull());
    assertEquals(new BigInteger(prime128bits), p128.getIntegerValueOrNull());
    assertEquals(new BigInteger(prime256bits), p256.getIntegerValueOrNull());
    
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.valueOf(-1), null);
      fail("Should have caught negative integer.");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("larger than 0"));
    }
    
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.valueOf(2).pow(256), null);
      fail("Should have caught 2^256.");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("smaller than 2^256"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, z.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, o.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p128.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p256.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testIntegerSigned() {
    final URI encoding = URI.create("urn:abc4trust:1.0:encoding:integer:signed");
    
    // A number less than 2^128
    String prime128bits = "340282366920938463463374607431768211297";

    MyAttributeValue z = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.ZERO, null);
    MyAttributeValue o = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.ONE, null);
    MyAttributeValue p128 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, new BigInteger(prime128bits), null);
    MyAttributeValue n128 = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, new BigInteger(prime128bits).negate(), null);
    MyAttributeValue min = MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.valueOf(2).pow(128).negate(), null);
    
    assertEquals(MyAttributeEncodingFactory.SIGNED_OFFSET, z.getIntegerValueOrNull());
    assertEquals(BigInteger.ONE.add(MyAttributeEncodingFactory.SIGNED_OFFSET), o.getIntegerValueOrNull());
    assertEquals(new BigInteger(prime128bits).add(MyAttributeEncodingFactory.SIGNED_OFFSET), p128.getIntegerValueOrNull());
    assertEquals(MyAttributeEncodingFactory.SIGNED_OFFSET.subtract(new BigInteger(prime128bits)), n128.getIntegerValueOrNull());
    assertEquals(BigInteger.ZERO, min.getIntegerValueOrNull());
    
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.valueOf(2).pow(128).add(BigInteger.ONE).negate(), null);
      fail("Should have caught -2^128-1.");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("larger than -2^128"));
    }
    
    try {
      MyAttributeEncodingFactory.parseValueFromEncoding(encoding, BigInteger.valueOf(2).pow(256).subtract(BigInteger.valueOf(2).pow(128)), null);
      fail("Should have caught 2^256-2^128.");
    } catch(RuntimeException e) {
      // expected
      assertTrue(e.getMessage().contains("smaller than 2^256-2^128"));
    }
    
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, z.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, o.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, p128.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, n128.getIntegerValueOrNull(), null);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, min.getIntegerValueOrNull(), null);
  }
  
  @Test
  public void testStringPrime() {
    List<String> allowedValues = new ArrayList<String>();
    allowedValues.add("ENTRY_ZERO");  //2
    allowedValues.add("ENTRY_ONE");   //3
    allowedValues.add("ENTRY_TWO");   //5
    allowedValues.add("ENTRY_THREE"); //7
    allowedValues.add("ENTRY_FOUR");  //11
    
    EnumAllowedValues eav = new EnumAllowedValues(allowedValues);
    
    MyAttributeValue v0 = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_PRIME, "ENTRY_ZERO", eav);
    MyAttributeValue v3 = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_PRIME, "ENTRY_THREE", eav);
    MyAttributeValue v4 = MyAttributeEncodingFactory.parseValueFromEncoding(ENCODING_STRING_PRIME, "ENTRY_FOUR", eav);
    
    
    assertEquals(v0.getIntegerValueOrNull(), BigInteger.valueOf(2));
    assertEquals(v3.getIntegerValueOrNull(), BigInteger.valueOf(7));
    assertEquals(v4.getIntegerValueOrNull(), BigInteger.valueOf(11));
    
    assertEquals("ENTRY_FOUR", MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_STRING_PRIME, v4.getIntegerValueOrNull(), eav).getValueAsObject());
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_STRING_PRIME, v3.getIntegerValueOrNull(), eav);
    MyAttributeEncodingFactory.recoverValueFromBigInteger(ENCODING_STRING_PRIME, v0.getIntegerValueOrNull(), eav);

    // Test function evaluation
    List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();

    final URI STRING_EQ_ONEOF = URI.create("urn:abc4trust:1.0:function:string-equal-oneof");
    arguments.clear();
    arguments.add(v4);
    arguments.add(new MyAttributeValueString("ENTRY_ONE", null));
    arguments.add(new MyAttributeValueString("ENTRY_TWO", null));
    arguments.add(new MyAttributeValueString("ENTRY_FOUR", null));
    arguments.add(new MyAttributeValueString("ENTRY_ZERO", null));
    assertTrue(evaulateFunction(STRING_EQ_ONEOF, arguments));
    arguments.set(3, v3);
    assertFalse(evaulateFunction(STRING_EQ_ONEOF, arguments));
    
    //System.out.println(":" + MyAttributeEncodingFactory.getEncodingForEachAllowedValue(ENCODING_STRING_PRIME, new EnumAllowedValues(allowedValues)));
  }
}

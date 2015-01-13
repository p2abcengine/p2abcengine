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


package eu.abc4trust.cryptoEngine.user.credCompressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class CompressorUtilsTest {

  @Test
  public void testLengthWriting() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for(int i=0;i<300;++i) {
      CompressorUtils.writeLength(baos, i);
    }
    for(int i=1;i<21;++i) {
      int pow = 1<<i;
      CompressorUtils.writeLength(baos, pow-1);
      CompressorUtils.writeLength(baos, pow);
      CompressorUtils.writeLength(baos, pow+1);
    }
    CompressorUtils.writeLength(baos, (1<<21)-1);
    
    byte[] res = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    for(int i=0;i<300;++i) {
      assertEquals(i, CompressorUtils.getLength(bais));
    }
    for(int i=1;i<21;++i) {
      int pow = 1<<i;
      assertEquals(pow-1, CompressorUtils.getLength(bais));
      assertEquals(pow, CompressorUtils.getLength(bais));
      assertEquals(pow+1, CompressorUtils.getLength(bais));
    }
    assertEquals((1<<21)-1, CompressorUtils.getLength(bais));
  }
  
  @Test
  public void TestBigInteger() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for(int i=-300;i<300;++i) {
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(i));
    }
    for(int i=1;i<520;++i) {
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).add(BigInteger.ONE));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE).negate());
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).negate());
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).add(BigInteger.ONE).negate());
    }
    for(int i=512;i<10000;i+=13) {
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).add(BigInteger.ONE));
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE).negate());
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).negate());
      CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2).pow(i).add(BigInteger.ONE).negate());
    }
    
    byte[] res = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    for(int i=-300;i<300;++i) {
      assertEquals(BigInteger.valueOf(i), CompressorUtils.readBigInteger(bais));
    }
    for(int i=1;i<520;++i) {
      assertEquals(BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).add(BigInteger.ONE), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE).negate(), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).negate(), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).add(BigInteger.ONE).negate(), CompressorUtils.readBigInteger(bais));
    }
    for(int i=512;i<10000;i+=13) {
      assertEquals(BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).add(BigInteger.ONE), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).subtract(BigInteger.ONE).negate(), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).negate(), CompressorUtils.readBigInteger(bais));
      assertEquals(BigInteger.valueOf(2).pow(i).add(BigInteger.ONE).negate(), CompressorUtils.readBigInteger(bais));
    }
  }
  
  @Test
  public void TestString() {
    String s1 = "";
    String s2 = "a";
    String s3 = "Hello";
    String s4 = "";
    for(int i=0;i<100;++i) {
      s4 += "1234567890";
    }
    String s5 = "\u6b63\u5f0f\u793e\u540d";
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CompressorUtils.writeString(baos, s1);
    CompressorUtils.writeString(baos, s2);
    CompressorUtils.writeString(baos, s3);
    CompressorUtils.writeString(baos, s4);
    CompressorUtils.writeCompressedData(baos, s4.getBytes());
    CompressorUtils.writeString(baos, s5);
    byte[] res = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    assertEquals(s1, CompressorUtils.readString(bais));
    assertEquals(s2, CompressorUtils.readString(bais));
    assertEquals(s3, CompressorUtils.readString(bais));
    assertEquals(s4, CompressorUtils.readString(bais));
    assertTrue(Arrays.equals(s4.getBytes(), CompressorUtils.readCompressedData(bais)));
    assertEquals(s5, CompressorUtils.readString(bais));
  }
  
  @Test
  public void TestStringSmart() {
    String longString = "";
    for(int i=0;i<100;++i) {
      longString += "1234567890";
    }
    
    List<String> knownStrings = new ArrayList<String>();
    knownStrings.add("http://abc4trust.eu/");
    knownStrings.add("http://www.ibm.com/");
    knownStrings.add("http://abc4trust.eu/test/");
    Map<String, Integer> reversedList = CompressorUtils.invertList(knownStrings);
    String s1 = "";
    String s2 = "http://www.ibm.com/";
    String s3 = "http://abc4trust.eu/test/";
    String s4 = "http://abc4trust.eu/test/\u6b63\u5f0f\u793e\u540d";
    String s5 = "\u6b63\u5f0f\u793e\u540d";
    String s6 = "http://abc4trust.eu/\u6b63\u5f0f\u793e\u540d";
    String s7 = longString;
    String s8 = "http://abc4trust.eu/test/" + longString;
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int k=0;
    CompressorUtils.writeStringSmart(baos, s1, reversedList);
    assertEquals(2, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s2, reversedList);
    assertEquals(2, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s3, reversedList);
    assertEquals(2, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s4, reversedList);
    assertEquals(15, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s5, reversedList);
    assertEquals(14, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s6, reversedList);
    assertEquals(15, baos.toByteArray().length-k); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s7, reversedList);
    assertTrue(baos.toByteArray().length-k < 60); k=baos.toByteArray().length;
    CompressorUtils.writeStringSmart(baos, s8, reversedList);
    assertTrue(baos.toByteArray().length-k < 60); k=baos.toByteArray().length;
    
    byte[] res = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    assertEquals(s1, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s2, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s3, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s4, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s5, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s6, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s7, CompressorUtils.readStringSmart(bais, knownStrings));
    assertEquals(s8, CompressorUtils.readStringSmart(bais, knownStrings));
  }
  
  @Test
  public void testBoolean() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CompressorUtils.writeBoolean(baos, Boolean.TRUE);
    CompressorUtils.writeBoolean(baos, Boolean.FALSE);
    byte[] res = baos.toByteArray();
    
    assertEquals(2, res.length);

    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    assertTrue(CompressorUtils.readBoolean(bais));
    assertFalse(CompressorUtils.readBoolean(bais));
  }
  
  @Test
  public void testBooleanMixed() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CompressorUtils.writeString(baos, "s1");
    CompressorUtils.writeBoolean(baos, Boolean.TRUE);
    CompressorUtils.writeString(baos, "s2");
    CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(1));
    CompressorUtils.writeBoolean(baos, Boolean.FALSE);
    CompressorUtils.writeBigInteger(baos, BigInteger.valueOf(2));

    byte[] res = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(res);
    assertEquals("s1", CompressorUtils.readString(bais));
    assertTrue(CompressorUtils.readBoolean(bais));
    assertEquals("s2", CompressorUtils.readString(bais));
    assertEquals(1, CompressorUtils.readBigInteger(bais).longValue());
    assertFalse(CompressorUtils.readBoolean(bais));
    assertEquals(2, CompressorUtils.readBigInteger(bais).longValue());
  }

}

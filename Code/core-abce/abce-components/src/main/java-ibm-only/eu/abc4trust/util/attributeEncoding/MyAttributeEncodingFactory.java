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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.EnumAllowedValuesWithIndexer;
import eu.abc4trust.util.attributeTypes.EnumIndexer;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;

public class MyAttributeEncodingFactory {
  
  private static final Class<?> classes[] =
      new Class[] {MyEncodingStringSha256.class, MyEncodingStringUtf8.class,
          MyEncodingUriSha256.class, MyEncodingUriUtf8.class, MyEncodingDateTimeSigned.class,
          MyEncodingDateTimeUnsigned.class, MyEncodingDateUnsigned.class,
          MyEncodingDateSigned.class, MyEncodingDateSince1870.class,
          MyEncodingDateSince2010.class, MyEncodingTime.class, MyEncodingBoolean.class,
          MyEncodingIntegerUnsigned.class, MyEncodingIntegerSigned.class, MyEncodingStringPrime.class};
  
  // 2^128
  public static final BigInteger SIGNED_OFFSET = BigInteger.valueOf(2).pow(128);
  // 2^256
  public static final BigInteger MAX_VALUE = BigInteger.valueOf(2).pow(256);
  
  public static MyAttributeValue parseValueFromEncoding(URI encoding, Object attributeValue, /*Nullable*/ EnumAllowedValues eav) {
    MyAttributeValue ret = parseValueFromEncodingNoCheck(encoding, attributeValue, eav);
    if(! encoding.equals(ret.getEncodingOrNull())) {
      throw new RuntimeException("Problem with encoding: " + encoding + "/" + ret.getEncodingOrNull());
    }
    return ret;
  }
  
  private static MyAttributeValue parseValueFromEncodingNoCheck(URI encoding, Object attributeValue, /*Nullable*/ EnumAllowedValues eav) {  
    try {
      for(Class<?> c:classes) {
        if(c.getField("ENCODING").get(null).equals(encoding)) {
          Constructor<?> cons = c.getConstructor(Object.class, EnumAllowedValues.class);
          return (MyAttributeValue) cons.newInstance(attributeValue, eav);
        }
      }
    } catch(InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        // Unwrap the exception if it is an unchecked exception
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    }
    
    throw new RuntimeException("Cannot parse attribute encoding: '" + encoding + "'");
  }
  
  public static Map<String, BigInteger> getEncodingForEachAllowedValue(URI encoding, EnumAllowedValues eav) {  
    try {
      for(Class<?> c:classes) {
        if(c.getField("ENCODING").get(null).equals(encoding)) {
          Method m =  c.getMethod("getEnumIndexer");
          EnumIndexer indexer = (EnumIndexer)m.invoke(null);
          EnumAllowedValuesWithIndexer eavwi = new EnumAllowedValuesWithIndexer(indexer, eav.getAllowedValues());
          return eavwi.getEncodingForEachAllowedValue();
        }
      }
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        // Unwrap the exception if it is an unchecked exception
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    }
    
    throw new RuntimeException("Cannot parse attribute encoding: '" + encoding + "'");
  }
  
  public static MyAttributeValue recoverValueFromBigInteger(URI encoding, BigInteger attributeValue, /*Nullable*/ EnumAllowedValues eav) {
    MyAttributeValue ret = recoverValueFromBigIntegerNoCheck(encoding, attributeValue, eav);
    if(! encoding.equals(ret.getEncodingOrNull())) {
      throw new RuntimeException("Problem with encoding: " + encoding + "/" + ret.getEncodingOrNull());
    } else if(! attributeValue.equals(ret.getIntegerValueOrNull())) {
      throw new RuntimeException("Problem with recovering attribute value from integer encoding: E=" + encoding + "/V=" + attributeValue + " : " + ret.toString());
    }
    return ret;
  }
  
  private static MyAttributeValue recoverValueFromBigIntegerNoCheck(URI encoding, BigInteger attributeValue, /*Nullable*/ EnumAllowedValues eav) {  
    try {
      for(Class<?> c:classes) {
        if(c.getField("ENCODING").get(null).equals(encoding)) {
          Method m =  c.getMethod("recoverValueFromIntegerValue", BigInteger.class, EnumAllowedValues.class);
          Object decoded = m.invoke(null, attributeValue, eav);
          Constructor<?> cons = c.getConstructor(Object.class, EnumAllowedValues.class);
          return (MyAttributeValue) cons.newInstance(decoded, eav);
        }
      }
    } catch(InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        // Unwrap the exception if it is an unchecked exception
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    }
    
    throw new RuntimeException("Cannot recover value for attribute encoding: '" + encoding + "'");
  }
  
  private static URI XS_STRING = URI.create("xs:string");
  private static URI XS_URI = URI.create("xs:anyURI");
  private static URI XS_DATETIME = URI.create("xs:dateTime");
  private static URI XS_DATE = URI.create("xs:date");
  private static URI XS_TIME = URI.create("xs:time");
  private static URI XS_INTEGER = URI.create("xs:integer");
  private static URI XS_BOOLEAN = URI.create("xs:boolean");
  
  public static URI getDatatypeFromEncoding(URI encoding) {
    if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"))) {
      return XS_STRING;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:string:utf-8"))) {
      return XS_STRING;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:string:prime"))) {
      return XS_STRING;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:anyUri:sha-256"))) {
      return XS_URI;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8"))) {
      return XS_URI;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed"))) {
      return XS_DATETIME;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:unsigned"))) {
      return XS_DATETIME;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned"))) {
      return XS_DATE;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:date:unix:signed"))) {
      return XS_DATE;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned"))) {
      return XS_DATE;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned"))) {
      return XS_DATE;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned"))) {
      return XS_TIME;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned"))) {
      return XS_BOOLEAN;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:integer:unsigned"))) {
      return XS_INTEGER;
    } else if (encoding.equals(URI.create("urn:abc4trust:1.0:encoding:integer:signed"))) {
      return XS_INTEGER;
    } else {
      return null;
    }
  }
  
  public static BigInteger byteArrayToInteger(byte[] arr) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i<arr.length;++i) {
      sb.append(String.format("%02X", arr[i]));
    }
    return new BigInteger(sb.toString(), 16);
  }
  
  public static BigInteger byteArrayToIntegerWithLength(byte[] arr) {
    StringBuilder sb = new StringBuilder();
    if(arr.length > 255) {
      throw new RuntimeException("Array too long! Size must fit in one byte.");
    }
    for(int i=0;i<arr.length;++i) {
      sb.append(String.format("%02X", arr[i]));
    }
    sb.append(String.format("%02X", arr.length));

    return new BigInteger(sb.toString(), 16);
  }

  public static byte[] decodeByteArrayWithLength(BigInteger integerValue) {
    int len = integerValue.mod(BigInteger.valueOf(256)).intValue();
    integerValue = integerValue.divide(BigInteger.valueOf(256));
    byte[] arr = new byte[len];
    for (int i=len-1;i>=0;--i) {
      arr[i] = integerValue.mod(BigInteger.valueOf(256)).byteValue();
      integerValue = integerValue.divide(BigInteger.valueOf(256));
    }
    return arr;
  }
}

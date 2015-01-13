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

package eu.abc4trust.cryptoEngine.user.credCompressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressorUtils {
  

  private static final int STRING_NULL = 0;
  private static final int STRING_FROM_LIST = 1;
  private static final int STRING_PREFIX_FROM_LIST = 2;
  private static final int STRING_FROM_BYTES = 3;
  private static final int STRING_COMPRESSED = 4;
  private static final int STRING_PREFIX_FROM_LIST_COMPRESSED = 5;

  public static void writeLength(ByteArrayOutputStream baos, int len) {
    if(len<0) {
      throw new RuntimeException("Invalid length < 0");
    }
    if(len<=127) {
      //MSB = 0
      baos.write(len);
    } else if(len <= 16383) {
      // MSB = 10
      int lowbyte = len%64;
      int highbyte = len/64;
      baos.write(lowbyte + 128);
      baos.write(highbyte);
    } else if (len <= 2097151) {
      // MSB = 110
      int lowbyte = len%32;
      int midbyte = (len/32)%256;
      int highbyte = len/32/256;
      baos.write(lowbyte + 128 + 64);
      baos.write(midbyte);
      baos.write(highbyte);
    } else {
      throw new RuntimeException("Invalid length > 2^21-1");
    }
  }
  
  static int sizeOfWriteLength(int len) {
    if(len<0) {
      throw new RuntimeException("Invalid length < 0");
    }
    if(len<=127) {
      return 1;
    } else if(len <= 16383) {
      return 2;
    } else if (len <= 2097151) {
      return 3;
    } else {
      throw new RuntimeException("Invalid length > 2^21-1");
    }
  }
  
  public static int getLength(ByteArrayInputStream bais) {
    int lowbyte = bais.read();
    if((lowbyte & 128)==0) {
      // MSB = 0
      return lowbyte;
    } else if ((lowbyte & 64) == 0) {
      // MSB = 10
      lowbyte-=128;
      int highbyte = bais.read();
      return lowbyte + highbyte*64;
    } else if ((lowbyte & 32) == 0) {
      // MSB = 110
      lowbyte -= 128+64;
      int midbyte = bais.read();
      int highbyte = bais.read();
      return lowbyte + midbyte*32 + highbyte*32*256;
    } else {
      throw new RuntimeException("Cannot parse length");
    }
  }
  
  public static void writeData(ByteArrayOutputStream baos, byte[] data) {
    writeLength(baos, data.length);
    baos.write(data, 0, data.length);
  }
  public static byte[] readData(ByteArrayInputStream bais) {
    int len = getLength(bais);
    byte[] data = new byte[len];
    bais.read(data, 0, len);
    return data;
  }
  static byte[] getCompressedData(byte[] data) {
    ByteArrayOutputStream ser = new ByteArrayOutputStream();
    GZIPOutputStream gs;
    try {
      gs = new GZIPOutputStream(ser);
      gs.write(data);
      gs.close();
    } catch (IOException e) {
      throw new RuntimeException("Cannot compress data", e);
    }
    
    byte[] compressed = ser.toByteArray();
    return compressed;
  }
  public static void writeCompressedData(ByteArrayOutputStream baos, byte[] data) {
    byte[] compressed = getCompressedData(data);
    writeLength(baos, data.length);
    writeData(baos, compressed);
  }
  public static byte[] readCompressedData(ByteArrayInputStream bais) {
    int uncompressedLength = getLength(bais);
    byte[] output = new byte[uncompressedLength];
    byte[] compressed = readData(bais);
    GZIPInputStream gs;
    try {
      gs = new GZIPInputStream(new ByteArrayInputStream(compressed));
      for(int i=0;i<uncompressedLength;++i) {
        output[i] = (byte)gs.read();
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot decompress", e);
    }

    return output;
  }
  static boolean worthToCompress(String s) {
    try {
      byte[] compressed = getCompressedData(s.getBytes("UTF-8"));
      int size = compressed.length + sizeOfWriteLength(compressed.length);
      return size < s.length();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Problem with encoding", e);
    }
  }
  public static void writeBigInteger(ByteArrayOutputStream baos, BigInteger bi) {
    writeData(baos, bi.toByteArray());
  }
  public static BigInteger readBigInteger(ByteArrayInputStream bais) {
    return new BigInteger(readData(bais));
  }
  public static void writeString(ByteArrayOutputStream baos, String s) {
    try {
      writeData(baos, s.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Cannot write string", e);
    }
  }
  public static void writeStringCompressed(ByteArrayOutputStream baos, String s) {
    try {
      writeCompressedData(baos, s.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Cannot write string", e);
    }
  }
  public static String readString(ByteArrayInputStream bais) {
    try {
      return new String(readData(bais), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Cannot read string", e);
    }
  }
  public static String readStringCompressed(ByteArrayInputStream bais) {
    try {
      return new String(readCompressedData(bais), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Cannot read string", e);
    }
  }
  
  public static void writeStringSmart(ByteArrayOutputStream baos, String s, Map<String, Integer> knownStrings) {
    
    if(s==null) {
      baos.write(STRING_NULL);
      return;
    }
    
    // String from list
    if(knownStrings.get(s) != null) {
      baos.write(STRING_FROM_LIST);
      writeLength(baos, knownStrings.get(s));
      return;
    }
    // String prefix from list
    int index=-1;
    String bestPrefix="";
    for(String prefix: knownStrings.keySet()) {
      if(s.startsWith(prefix)  && prefix.length()>=bestPrefix.length()) {
        bestPrefix = prefix;
        index = knownStrings.get(prefix);
      }
    }
    if(index!=-1) {
      String rest = s.substring(bestPrefix.length());
      
      if(worthToCompress(rest)) {
        baos.write(STRING_PREFIX_FROM_LIST_COMPRESSED);
        writeLength(baos, index);
        writeStringCompressed(baos, rest);
      } else {
        baos.write(STRING_PREFIX_FROM_LIST);
        writeLength(baos, index);
        writeString(baos, rest);
      }

      return;
    }
    
    if(worthToCompress(s)) {
      baos.write(STRING_COMPRESSED);
      writeStringCompressed(baos, s);
      return;
    }
    // Write out whole string
    baos.write(STRING_FROM_BYTES);
    writeString(baos, s);
    System.err.println("Unlisted string: " + s);
  }
  
  public static Map<String, Integer> invertList(List<String> ls) {
    Map<String, Integer> ret = new HashMap<String, Integer>();
    for(int i=0;i<ls.size();++i) {
      ret.put(ls.get(i), Integer.valueOf(i));
    }
    return ret;
  }
  
  public static String readStringSmart(ByteArrayInputStream bais, List<String> knownStrings) {
    switch(bais.read()) {
      case STRING_NULL:
      {
        return null;
      }
      case STRING_FROM_LIST:
      {
        int index = getLength(bais);
        return knownStrings.get(index);
      }
      case STRING_PREFIX_FROM_LIST:
      {
        int index = getLength(bais);
        String prefix = knownStrings.get(index);
        return prefix + readString(bais);
      }
      case STRING_PREFIX_FROM_LIST_COMPRESSED:
      {
        int index = getLength(bais);
        String prefix = knownStrings.get(index);
        return prefix + readStringCompressed(bais);
      }
      case STRING_COMPRESSED:
      {
        return readStringCompressed(bais);
      }
      case STRING_FROM_BYTES:
      {
        return readString(bais);
      }
      default:
      {
        throw new RuntimeException("Cannot read smart string");
      }
    }
  }
  
  public static Boolean readBoolean(ByteArrayInputStream bais) {
    int b = bais.read();
    if(b==0) {
      return Boolean.FALSE;
    } else {
      return Boolean.TRUE;
    }
  }
  public static void writeBoolean(ByteArrayOutputStream baos, Object o) {
    if(o instanceof Boolean) {
      writeBoolean(baos, Boolean.valueOf((Boolean)o));
    } else {
      writeBoolean(baos, Boolean.valueOf(o.toString()));
    }
  }
  public static void writeBoolean(ByteArrayOutputStream baos, Boolean b) {
    if(b) {
      baos.write(1);
    } else {
      baos.write(0);
    }
  }

}

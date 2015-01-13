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

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.*;
import static org.junit.Assert.*;

import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.util.XmlUtils;

public class PseudonymSerializerTest {
  
  List<PseudonymSerializer> getClasses() {
    List<PseudonymSerializer> cslist = new ArrayList<PseudonymSerializer>();
    CardStorage cardStorage = new CardStorage();
    cslist.add(new PseudonymSerializerXml(cardStorage));
    cslist.add(new PseudonymSerializerGzipXml(cardStorage));
    cslist.add(new PseudonymSerializerObject(cardStorage));
    cslist.add(new PseudonymSerializerObjectGzip(cardStorage));
    return cslist;
  }
  
  @Test
  public void friendlyGeneratorTest() {
    Map<String, String> tests = new TreeMap<String, String>();
    
    tests.put("hello", "hello");
    tests.put("hello:world", "world");
    tests.put("a:b:c", "c");
    tests.put("a:b:", "b");
    tests.put("http://blabla/", "//blabla/");
    tests.put("http://blabla/:blibli", "blibli");
    tests.put("hello:a+b", "a b");
    tests.put("hello:H%C3%A9llo", "H\u00e9llo");
    tests.put("hello:a/b", "a/b");
    
    for(String scope: tests.keySet()) {
      FriendlyDescription fd = AbstractPseudonymSerializer.generateFriendlyDescription(scope);
      assertEquals(tests.get(scope), fd.getValue());
      assertEquals("en", fd.getLang());
    }
  }
  
  @Test
  public void testConsistency() throws Exception {
    List<PwmWithName> clist = new ArrayList<PwmWithName>();
    clist.add(new PwmWithName("credCompressor/idemix3-standard-pseudonym.xml"));
    // clist.add(new PwmWithName("credCompressor/idemix3-exclusive-pseudonym.xml"));
    
    List<PseudonymSerializer> cslist = getClasses();
    cslist.add(new PseudonymSerializerBest(new CardStorage()));
    
    boolean ok = true;
    for(PwmWithName cn: clist) {
      System.out.print(cn.name + ": ");
      for(PseudonymSerializer cs: cslist) {
        try {
          int size = checkSerializeAndUnserialize(cs, cn.cred, cn.name);
          System.out.print(cs.magicHeader()+"="+size+"   ");
        } catch(Exception e) {
          System.out.println("FAIL");
          ok = false;
        }
      }
      System.out.println();
    }
    assertTrue(ok);
  }
  
  @Test
  public void testInconsistency() throws Exception {
    PwmWithName c = new PwmWithName("credCompressor/idemix3-standard-pseudonym.xml");
    for(PseudonymSerializer cs1: getClasses()) {
      for(PseudonymSerializer cs2: getClasses()) {
        byte[] ser = cs1.serializePseudonym(c.cred);
        try {
          cs2.unserializePseudonym(ser, URI.create("uprove-uri"));
          if (cs1.getClass() != cs2.getClass()) {
            fail("Serialization with different classes should fail");
          }
        } catch(Exception e) {
          if (cs1.getClass() == cs2.getClass()) {
            fail("Serialization with same class should not fail");
          }
        }
      }
    }
  }
  
  private int checkSerializeAndUnserialize(PseudonymSerializer cs, PseudonymWithMetadata c, String cname)
      throws Exception {
    byte[] ser = cs.serializePseudonym(c);
    PseudonymWithMetadata copy = cs.unserializePseudonym(ser, URI.create("uprove-uri"));
    
    // TODO: there is no real comparison function for credentials...
    ObjectFactory of = new ObjectFactory();
    assertEquals("Serializer: " + cs.getClass().getName() + "  -- Cred: " + cname ,
                 XmlUtils.toXml(of.createPseudonymWithMetadata(c)),
                 XmlUtils.toXml(of.createPseudonymWithMetadata(copy)));
    return ser.length;
  }
  
  
  private class PwmWithName {
    public final PseudonymWithMetadata cred;
    public final String name;
    
    PwmWithName(String path) throws Exception {
      this.name = path;
      cred=
          (PseudonymWithMetadata) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                  "/eu/abc4trust/sampleXml/" + path), true);
    }
  }
}

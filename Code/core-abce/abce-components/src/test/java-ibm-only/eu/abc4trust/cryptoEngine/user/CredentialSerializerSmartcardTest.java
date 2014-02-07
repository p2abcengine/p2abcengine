//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.cryptoEngine.user;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class CredentialSerializerSmartcardTest {
  
  private final static String PATH = "/eu/abc4trust/sampleXml/credCompressor/";
  
  @Test
  public void testConsistencyUprovePlain() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("cc-uprove", "cc-uprove", km, false));
    loadSystemParameters(km, "");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyBridging() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("bri-stud", "bri-stud", km, false));
    clist.add(new CredWithName("bri-pass", km, true));
    loadSystemParameters(km, "-bri");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyEnum() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("enum-cc", km, true));
    loadSystemParameters(km, "-enum-cc");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyRev1024() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("rev-id", km, true));
    loadSystemParameters(km, "-rev-id");
    loadRaParameters(km, "rev-id");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyRev2048() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("rev2048", km, true));
    loadSystemParameters(km, "-rev2048");
    loadRaParameters(km, "rev2048");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyIdemix1024() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("student", km, true));
    clist.add(new CredWithName("cc", km, false));
    clist.add(new CredWithName("pass", km, false));
    loadSystemParameters(km, "");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void testConsistencyIdemix2048() throws Exception {
    KeyManager km = EasyMock.createMock(KeyManager.class);
    
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("2048-stud", km, true));
    clist.add(new CredWithName("2048-cc", km, false));
    clist.add(new CredWithName("2048-pass", km, false));
    loadSystemParameters(km, "-2048");
    
    EasyMock.replay(km);
    
    CredentialSerializerSmartcard csc = new CredentialSerializerSmartcard(km, new ArrayList<String>());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      int size = checkSerializeAndUnserialize(csc, cn.cred, cn.name, cn.check, cn.uprove);
      System.out.print(csc.magicHeader()+"="+size+"   ");
      System.out.println();
    }
    
    EasyMock.verify(km);
  }
  
  @Test
  public void guiceTest() {
    Module pm = ProductionModuleFactory.newModule();
    Injector i = Guice.createInjector(pm);
    CredentialSerializerSmartcard csc = i.getInstance(CredentialSerializerSmartcard.class);
    assertNotNull(csc);
  }
  
  private int checkSerializeAndUnserialize(CredentialSerializerSmartcard cs, Credential c, String cname, boolean check, boolean uprove)
      throws Exception {
    byte[] ser = cs.serializeCredential(c);
    Credential copy = cs.unserializeCredential(ser, c.getCredentialDescription().getCredentialUID(), c.getCredentialDescription().getSecretReference());
    
    if(uprove) {
      c.getCryptoParams().getAny().clear();
      copy.getCryptoParams().getAny().clear();
    }
    
    // TODO: there is no real comparison function for credentials... need to inspect manually
    try {
      ObjectFactory of = new ObjectFactory();
      assertEquals("Serializer: " + cs.getClass().getName() + "  -- Cred: " + cname ,
                   XmlUtils.toXml(of.createCredential(c)),
                   XmlUtils.toXml(of.createCredential(copy)));
    } catch(ComparisonFailure cf) {
      System.out.println(cf.getMessage());
      if(check) {
        throw cf;
      }
    }
    return ser.length;
  }
  
  
  private class CredWithName {
    public final Credential cred;
    public final CredentialSpecification cs;
    public final String name;
    public final IssuerParameters ip;
    public final boolean check;
    public final boolean uprove;
    
    CredWithName(String path, KeyManager km, boolean check) throws Exception {
      name = path;
      cred=
          (Credential) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                  PATH + "cred-" + path + ".xml"), true);
      cs=
          (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
            PATH + "cs-" + path + ".xml"), true);
      String filename = PATH + "ip-" + path + ".obj";
      InputStream buffer = new BufferedInputStream( this.getClass().getResourceAsStream(filename) );
      ObjectInput input = new ObjectInputStream( buffer );
      ip = (IssuerParameters) input.readObject();
      input.close();
      
      EasyMock.expect(km.getCredentialSpecification(cs.getSpecificationUID())).andReturn(cs).anyTimes();
      EasyMock.expect(km.getIssuerParameters(ip.getParametersUID())).andReturn(ip).anyTimes();
      this.check = check;
      this.uprove = false;
    }
    
    CredWithName(String file1, String path, KeyManager km, boolean check) throws Exception {
      name = path;
      String filename = PATH + "cred-" + file1 + ".obj";
      InputStream buffer = new BufferedInputStream( this.getClass().getResourceAsStream(filename) );
      ObjectInput input = new ObjectInputStream( buffer );
      cred = (Credential) input.readObject();
      input.close();
      cs=
          (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
            PATH + "cs-" + path + ".xml"), true);
      ip = new IssuerParameters();
      
      EasyMock.expect(km.getCredentialSpecification(cs.getSpecificationUID())).andReturn(cs).anyTimes();
      EasyMock.expect(km.getIssuerParameters(cred.getCredentialDescription().getIssuerParametersUID())).andReturn(ip).anyTimes();
      this.check = check;
      this.uprove = true;
    }
  }
  
  void loadSystemParameters(KeyManager km, String postfix) throws Exception {
    String filename = PATH + "sp" + postfix + ".obj";
    InputStream buffer = new BufferedInputStream( this.getClass().getResourceAsStream(filename) );
    ObjectInput input = new ObjectInputStream( buffer );
    SystemParameters sp = (SystemParameters) input.readObject();
    input.close();
    EasyMock.expect(km.getSystemParameters()).andReturn(sp).anyTimes();
  }
  
  void loadRaParameters(KeyManager km, String postfix) throws Exception {
    String filename = PATH + "rap-" + postfix + ".obj";
    InputStream buffer = new BufferedInputStream( this.getClass().getResourceAsStream(filename) );
    ObjectInput input = new ObjectInputStream( buffer );
    RevocationAuthorityParameters sp = (RevocationAuthorityParameters) input.readObject();
    input.close();
    EasyMock.expect(km.getRevocationAuthorityParameters(sp.getParametersUID())).andReturn(sp).anyTimes();
  }
}

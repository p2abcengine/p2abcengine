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

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

public class CredentialSerializerTest {
  
  List<CredentialSerializer> getClasses() {
    List<CredentialSerializer> cslist = new ArrayList<CredentialSerializer>();
    cslist.add(new CredentialSerializerXml());
    cslist.add(new CredentialSerializerGzipXml());
    cslist.add(new CredentialSerializerObject());
    cslist.add(new CredentialSerializerObjectGzip());
    return cslist;
  }
  
  @Test
  public void testConsistency() throws Exception {
    List<CredWithName> clist = new ArrayList<CredWithName>();
    clist.add(new CredWithName("credentialValidCreditCardTheBestBank.xml"));
    clist.add(new CredWithName("credentialValidCreditCard.xml"));
    clist.add(new CredWithName("credentialStudentId.xml"));
    clist.add(new CredWithName("credentialSimpleIdentityCardYetAnotherCountry.xml"));
    clist.add(new CredWithName("credentialSimpleIdentityCardAnotherCountry.xml"));
    clist.add(new CredWithName("credentialSimpleIdentityCard.xml"));
    clist.add(new CredWithName("credentialPassport2.xml"));
    clist.add(new CredWithName("credentialPassport.xml"));
    clist.add(new CredWithName("credentialCreditcardRevocableAmex.xml"));
    clist.add(new CredWithName("credentialCreditcardForTestingUI.xml"));
    clist.add(new CredWithName("credentialCreditcardDifferentOrder.xml"));
    clist.add(new CredWithName("credentialCreditcard.xml"));
    clist.add(new CredWithName("sampleIdemixCredential.xml"));
    
    List<CredentialSerializer> cslist = getClasses();
    cslist.add(new CredentialSerializerBest());
    
    for(CredWithName cn: clist) {
      System.out.print(cn.name + ": ");
      for(CredentialSerializer cs: cslist) {
        int size = checkSerializeAndUnserialize(cs, cn.cred, cn.name);
        System.out.print(cs.magicHeader()+"="+size+"   ");
      }
      System.out.println();
    }
  }
  
  @Test
  public void testInconsistency() throws Exception {
    CredWithName c = new CredWithName("sampleIdemixCredential.xml");
    for(CredentialSerializer cs1: getClasses()) {
      for(CredentialSerializer cs2: getClasses()) {
        byte[] ser = cs1.serializeCredential(c.cred);
        try {
          cs2.unserializeCredential(ser);
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
  
  private int checkSerializeAndUnserialize(CredentialSerializer cs, Credential c, String cname)
      throws Exception {
    byte[] ser = cs.serializeCredential(c);
    Credential copy = cs.unserializeCredential(ser);
    
    // TODO: there is no real comparison function for credentials...
    ObjectFactory of = new ObjectFactory();
    assertEquals("Serializer: " + cs.getClass().getName() + "  -- Cred: " + cname ,
                 XmlUtils.toXml(of.createCredential(c)),
                 XmlUtils.toXml(of.createCredential(copy)));
    return ser.length;
  }
  
  
  private class CredWithName {
    public final Credential cred;
    public final String name;
    
    CredWithName(String path) throws Exception {
      name = path;
      cred=
          (Credential) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                  "/eu/abc4trust/sampleXml/credentials/" + path), true);
    }
  }
}

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

package eu.abc4trust.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.util.XmlUtils;

public class MyCredentialSpecificationTest {

  private static final String CRED_SPEC_REV_CC = "credentialSpecificationRevocableCreditcard";

  private void testCredentialAgainstSpec(String credName, String specName) throws Exception {
    CredentialSpecification cs =
        (CredentialSpecification) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/credspecs/" + specName + ".xml"), true);

    MyCredentialSpecification mycs = new MyCredentialSpecification(cs);
    
    CredentialDescription cd =
        ((Credential) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/credentials/" + credName + ".xml"), true)).getCredentialDescription();
    
    mycs.validateOrThrow(cd);
  }
  
  @Test
  public void testValid() throws Exception {
    testCredentialAgainstSpec("credentialCreditcard", CRED_SPEC_REV_CC);
    testCredentialAgainstSpec("credentialValidCreditCard", CRED_SPEC_REV_CC);
    testCredentialAgainstSpec("credentialPassport", "credentialSpecificationPassport");
    testCredentialAgainstSpec("credentialPassport2", "credentialSpecificationPassport");
    testCredentialAgainstSpec("credentialStudentId", "credentialSpecificationStudentCard");
  }
  
  @Test
  public void testDuplicateAttributeInCredential() throws Exception {
    try {
      testCredentialAgainstSpec("invalidDuplicateAttribute", CRED_SPEC_REV_CC);
      fail("Expected error: duplicate attribute Name");
    } catch(Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Duplicate attribute type Name"));
    }
  }
  
  @Test
  public void testMissingAttributeInCredential() throws Exception {
    try {
      testCredentialAgainstSpec("invalidMissingAttribute", CRED_SPEC_REV_CC);
      fail("Expected error: missing attribute Name");
    } catch(Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("size does not match spec"));
    }
  }
  
  @Test
  public void testUnknownAttributeInCredential() throws Exception {
    try {
      testCredentialAgainstSpec("invalidUnknownAttribute", CRED_SPEC_REV_CC);
      fail("Expected error: unknown attribute FirstName");
    } catch(Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Unknown attribute type: FirstName"));
    }
  }
  
  @Test
  public void testWrongEncoding() throws Exception {
    try {
      testCredentialAgainstSpec("invalidWrongEncoding", CRED_SPEC_REV_CC);
      fail("Expected error: wrong encoding");
    } catch(Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Wrong encoding"));
      assertTrue(ex.getMessage().contains("Expected urn:abc4trust:1.0:encoding:string:sha-256"));
      assertTrue(ex.getMessage().contains("actual urn:abc4trust:1.0:encoding:string:utf-8"));
    }
  }
  
  @Test
  public void testWrongDatatype() throws Exception {
    try {
      testCredentialAgainstSpec("invalidWrongDatatype", CRED_SPEC_REV_CC);
      fail("Expected error: wrong data type");
    } catch(Exception ex) {
      // expected
      assertTrue(ex.getMessage().contains("Wrong data type"));
      assertTrue(ex.getMessage().contains("Expected xs:date"));
      assertTrue(ex.getMessage().contains("actual xs:dateTime"));
    }
  }
  
  @Test
  public void testDifferentAttributeOrder() throws Exception {
    testCredentialAgainstSpec("credentialCreditcardDifferentOrder", CRED_SPEC_REV_CC);
  }

}

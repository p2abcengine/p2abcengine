//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
package eu.abc4trust.services.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.Constants;
import eu.abc4trust.services.verifier.VerifierServiceFactory;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;

public class ITInspect {

  private static ObjectFactory of = new ObjectFactory();
  
  @BeforeClass
  public static void setupInspector() throws Exception{        
    SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    
    new File(Constants.ISSUER_RESOURCES_FOLDER).mkdirs();
    VerifierServiceFactory.copyCredentialSpecification("credentialSpecificationSimpleIdentitycard.xml");
    
    FileSystem.storeObjectAsXMLInFile(of.createSystemParameters(systemParameters), 
      Constants.SYSTEM_PARAMETER_RESOURCE);
  }
  
  @Test
  public void inspect() throws Exception{
    InspectorServiceFactory inspectorServiceFactory = new InspectorServiceFactory();
    
    InspectorPublicKey publicKey = inspectorServiceFactory.getInspectorPublicKey(1024, "idemix");
    
    AttributeList attributeList = inspectorServiceFactory.inspect(publicKey);
    assertNotNull(attributeList);
    assertEquals("John", attributeList.getAttributes().get(0).getAttributeValue());    
  }
}

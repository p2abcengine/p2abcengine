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
package eu.abc4trust.services.verifier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.services.Constants;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;

public class ITDeleteToken {    
  
  private static Injector userInjector_idemix;
  
  @BeforeClass
  public static void issueCredentials() throws Exception{
    VerifierServiceFactory verifierServiceFactory = new VerifierServiceFactory();
    
    verifierServiceFactory.deleteResourcesDirectory();
    verifierServiceFactory.deleteStorageDirectory();
    verifierServiceFactory.deleteVerifierStorage();
    
    new File(Constants.ISSUER_RESOURCES_FOLDER).mkdirs();
    
    VerifierServiceFactory.copyCredentialSpecification("credentialSpecificationSimpleIdentitycard.xml");
    
    Injector issuerInjector_idemix = Guice.createInjector(IntegrationModuleFactory
      .newModule(new Random(1987)));
    userInjector_idemix = Guice.createInjector(IntegrationModuleFactory
      .newModule(new Random(1987)));
    
    URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
    
    verifierServiceFactory.issueCredential(userInjector_idemix, issuerInjector_idemix, cryptoMechanism);    
  }
  
  @Test
  public void deleteTokenFromVerifierStorage() throws Exception{
    VerifierServiceFactory verifierServiceFactory = new VerifierServiceFactory();
    PresentationTokenDescription ptd = verifierServiceFactory.getPresentationToken("idemix", userInjector_idemix, true);
    URI tokenUID = ptd.getTokenUID();
    PresentationToken pt = verifierServiceFactory.getPresentationTokenFromVerifierStorage(tokenUID);
    assertNotNull(pt);
    
    verifierServiceFactory.deletePresentationTokenFromVerifierStorage(tokenUID);
    
    pt = verifierServiceFactory.getPresentationTokenFromVerifierStorage(tokenUID);
    assertNull(pt);
  }
  
}

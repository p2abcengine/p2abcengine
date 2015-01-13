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
package eu.abc4trust.services.inspector;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.AbstractTestFactory;
import eu.abc4trust.services.Constants;
import eu.abc4trust.services.verifier.VerifierServiceFactory;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class InspectorServiceFactory extends AbstractTestFactory{

  static ObjectFactory of = new ObjectFactory();

  final String baseUrl = "http://localhost:9200/abce-services/inspector";
  
  final static String INSPECTOR_UID = "http://thebestbank.com/inspector/pub_key_v1";

  private static final String USERNAME = "username";
  
  public InspectorPublicKey getInspectorPublicKey(int keyLength, String cryptoMechanism){
    String requestString = "/setupInspectorPublicKey/?keyLength="+ keyLength+
        "&cryptoMechanism="+cryptoMechanism+
        "&uid="+INSPECTOR_UID;
    Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

    InspectorPublicKey publicKey = resource
            .post(InspectorPublicKey.class);
    return publicKey;
  }
  
  public AttributeList inspectPresentationToken(PresentationToken pt){
    String requestString = "/inspect";
    Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    
    AttributeList attList = resource.post(AttributeList.class, of.createPresentationToken(pt));
    return attList;
  }
  
  public AttributeList inspect(InspectorPublicKey inspectorPublicKey) throws Exception{
    Injector issuerInjector = Guice.createInjector(IntegrationModuleFactory
      .newModule(new Random(1987)));
    Injector userInjector = Guice.createInjector(IntegrationModuleFactory
      .newModule(new Random(1987)));
    
    KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
    userKeyManager.storeInspectorPublicKey(URI.create(INSPECTOR_UID), inspectorPublicKey);
    
    VerifierServiceFactory verifierServiceFactory = new VerifierServiceFactory();
    verifierServiceFactory.issueCredential(userInjector, issuerInjector, CryptoUriUtil.getIdemixMechanism());
    
    Injector verifierInjector = Guice.createInjector(IntegrationModuleFactory
      .newModule(new Random(1987)));
    
    KeyManager keyManager = verifierInjector.getInstance(KeyManager.class);
    keyManager.storeSystemParameters(SystemParametersUtil.getDefaultSystemParameters_1024());
    keyManager.storeInspectorPublicKey(URI.create(INSPECTOR_UID), inspectorPublicKey);
    
    CredentialSpecification credSpec = this.getCredentialSpecificationForTest();    
    keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);
    
    String[] issuerParamsResourceList =
        this.getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_param", "xml");
    List<IssuerParameters> issuerParamsList =
        FileSystem.loadXmlListFromResources(issuerParamsResourceList);
    for(IssuerParameters ip : issuerParamsList){
      keyManager.storeIssuerParameters(ip.getParametersUID(), ip);
    }
    
    PresentationPolicyAlternatives ppa = this.getPresentationPolicyAlternativesForInspectionTest();
    
    UserAbcEngine user = userInjector.getInstance(UserAbcEngine.class);
    // TODO(enr): Do we need to add this?
    // chosenInspectors.add(INSPECTOR_UID);
    PresentationToken pt = user.createPresentationTokenFirstChoice(USERNAME, ppa);
    
    VerifierAbcEngine verifier = verifierInjector.getInstance(VerifierAbcEngine.class);        
    verifier.verifyTokenAgainstPolicy(ppa, pt, false);
    
    return inspectPresentationToken(pt);
  }
  
  private String[] getFilesFromDir(String folderName, final String filter, final String extension) {
    String[] resourceList;
    URL url = AbstractHelper.class.getResource(folderName);
    File folder = null;
    if (url != null) {
      folder = new File(url.getFile());
    } else {
      folder = new File(folderName);
    }

    File[] fileList = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File arg0, String arg1) {
        if (arg1.indexOf(filter) != -1 && arg1.endsWith(extension)) {
          return true;
        } else {
          return false;
        }
      }
    });
    if (fileList == null) {
      System.out.println("Folder " + folderName
          + " does not exist! \n Trying to continue without these resources");
      return new String[0];
    }


    resourceList = new String[fileList.length];
    for (int i = 0; i < fileList.length; i++) {
      resourceList[i] = fileList[i].getAbsolutePath();
    }
    return resourceList;
  }
}

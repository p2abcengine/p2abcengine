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
package eu.abc4trust.services.verifier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.inject.Injector;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.AbstractTestFactory;
import eu.abc4trust.services.Constants;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationPolicyAlternativesAndPresentationToken;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;

public class VerifierServiceFactory extends AbstractTestFactory{

  private static final String USERNAME = "username";

  static ObjectFactory of = new ObjectFactory();

  final String baseUrl = "http://localhost:9200/abce-services/verification";

  public PresentationTokenDescription getPresentationToken(String cryptoMechanism, Injector userInjector, boolean storeToken) 
      throws CannotSatisfyPolicyException, CredentialManagerException, KeyManagerException, CryptoEngineException{
    String store = "false";
    if(storeToken){
      store = "true";
    }
    String requestString = "/verifyTokenAgainstPolicy/?store="+store;
    Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

    PresentationPolicyAlternativesAndPresentationToken ppa_pt = 
        of.createPresentationPolicyAlternativesAndPresentationToken();    

    PresentationPolicyAlternatives ppa = getPresentationPolicyAlternativesForTest();    
    
    UserAbcEngine user = userInjector.getInstance(UserAbcEngine.class);
    PresentationToken pt = user.createPresentationTokenFirstChoice(USERNAME, ppa);

    ppa_pt.setPresentationPolicyAlternatives(ppa);
    ppa_pt.setPresentationToken(pt);        
    
    PresentationTokenDescription presentationTokenDescription = resource
        .post(PresentationTokenDescription.class, of.createPresentationPolicyAlternativesAndPresentationToken(ppa_pt));
    return presentationTokenDescription;
  }
  
  public PresentationToken getPresentationTokenFromVerifierStorage(URI tokenUID){
    String requestString = "/getToken/?tokenUID="+tokenUID;
    Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    
    PresentationToken pt = resource.get(PresentationToken.class);
    return pt;
  }
  
  public void deletePresentationTokenFromVerifierStorage(URI tokenUID){
    String requestString = "/deleteToken/?tokenUID="+tokenUID;
    Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    
    resource.post();
  }

  public CredentialDescription issueCredential(Injector userInjector, Injector issuerInjector, 
                                               URI cryptoMechanism) throws Exception{
    CredentialSpecification credentialSpecification = getCredentialSpecificationForTest();
    IssuancePolicyAndAttributes issuancePolicyAndAttributes = getIssuancePolicyAndAttributesForTest();
    SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    
    CredentialDescription credDesc = 
        issueCredential(credentialSpecification, systemParameters, 
          issuerInjector, userInjector, issuancePolicyAndAttributes, 
          cryptoMechanism);
    
    return credDesc;
  }

  public CredentialDescription issueCredential(
    CredentialSpecification credentialSpecification,
    SystemParameters systemParameters,
    Injector issuerInjector, Injector userInjector,
    IssuancePolicyAndAttributes issuancePolicyAndAttributes,
    URI cryptoMechanism)
        throws Exception {

    KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
    KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
    
    issuerKeyManager.storeSystemParameters(systemParameters);
    userKeyManager.storeSystemParameters(systemParameters);
    
    FileSystem.storeObjectAsXMLInFile(of.createSystemParameters(systemParameters), 
      Constants.SYSTEM_PARAMETER_RESOURCE);
    
    URI uid = issuancePolicyAndAttributes.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
    
    IssuerAbcEngine issuer = issuerInjector
        .getInstance(IssuerAbcEngine.class);
    
    String ce = "idemix";
    if(cryptoMechanism.equals(CryptoUriUtil.getUproveMechanism())){
      ce = "uprove";
    }
    uid = URI.create(uid.toString()+":"+ce);
    issuancePolicyAndAttributes.getIssuancePolicy().getCredentialTemplate().setIssuerParametersUID(uid);
    
      IssuerParameters issuerParameters = issuer.setupIssuerParameters(systemParameters, 10, 
        cryptoMechanism, uid, null, null);
      FileSystem.storeObjectAsXMLInFile(of.createIssuerParameters(issuerParameters), 
        Constants.ISSUER_RESOURCES_FOLDER+"/issuer_parameters_"+ce+".xml");    
    
    issuerKeyManager.storeCredentialSpecification(
      credentialSpecification.getSpecificationUID(), credentialSpecification);
    userKeyManager.storeCredentialSpecification(
      credentialSpecification.getSpecificationUID(), credentialSpecification);
    
    issuerKeyManager.storeIssuerParameters(issuerParameters.getParametersUID(),
      issuerParameters);
    userKeyManager.storeIssuerParameters(issuerParameters.getParametersUID(), 
      issuerParameters);
    
    
    issuerKeyManager.storeIssuerParameters(uid, issuerParameters);
    userKeyManager.storeIssuerParameters(uid, issuerParameters);    

    // Init issuance protocol.
    IssuancePolicy issuancePolicy = issuancePolicyAndAttributes.getIssuancePolicy();
    List<Attribute> attribute = issuancePolicyAndAttributes.getAttribute();

    IssuanceMessageAndBoolean issuerIssuanceMessage = issuer
        .initIssuanceProtocol(issuancePolicy, attribute);
    assertNotNull(issuerIssuanceMessage);

    // Reply from user.
    UserAbcEngine user = userInjector.getInstance(UserAbcEngine.class);
    
    IssuMsgOrCredDesc userIR = user.issuanceProtocolStepFirstChoice(USERNAME, issuerIssuanceMessage.getIssuanceMessage());
    
    assertNotNull(userIR.im);
    
    IssuanceMessage userIssuanceMessage = userIR.im;
    
    // int round = 1;
    while (!issuerIssuanceMessage.isLastMessage()) {
      // System.out.println("Issuance round: " + round);

      assertNotNull(userIssuanceMessage);

      // Issuer issuance protocol step.
      issuerIssuanceMessage = issuer
          .issuanceProtocolStep(userIssuanceMessage);
      assertNotNull(issuerIssuanceMessage);

      assertNotNull(issuerIssuanceMessage.getIssuanceMessage());
      
      userIR = user.issuanceProtocolStepFirstChoice(USERNAME, issuerIssuanceMessage.getIssuanceMessage());
      
      boolean userLastMessage = (userIR.cd != null);
      if(!userLastMessage){
        userIssuanceMessage = userIR.im;
      }
      assertTrue(issuerIssuanceMessage.isLastMessage() == userLastMessage);
    }

    return userIR.cd;
  }
  
  public static void copyCredentialSpecification(String filename)
      throws IOException {
    File file = null;
    file = new File("src" + File.separatorChar + "test"
        + File.separatorChar + "resources" + File.separatorChar
        + filename);


    if (!file.exists()) {
      throw new FileNotFoundException("File does not exist on path: \"" + filename + "\"");
    }

    new File(Constants.CREDENTIAL_SPECIFICATION_FOLDER).mkdirs();

    FileInputStream fis = new FileInputStream(file);
    FileOutputStream fos = new FileOutputStream(new File(
      Constants.CREDENTIAL_SPECIFICATION_FOLDER + File.separatorChar
      + filename));

    byte[] bytes = new byte[1];
    while (fis.read(bytes) != -1) {
      fos.write(bytes);
    }

    fis.close();
    fos.close();
  }

}
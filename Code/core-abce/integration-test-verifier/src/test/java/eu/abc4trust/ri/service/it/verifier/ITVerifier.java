//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.service.it.verifier;


import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.smartcard.SoftwareSmartcardGenerator;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITVerifier {

  @BeforeClass
  public static void resetHelpers() {
    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
  }

  public ITVerifier() {}

  static ObjectFactory of = new ObjectFactory();

  public static final String UNIVERSITY_IDEMIX = "UNIVERSITY_IDEMIX";
  public static final String UNIVERSITY_UPROVE = "UNIVERSITY_UPROVE";
  private static final String USERNAME = "defaultUser";

  public static final SpecAndPolicy universityIdemix = new SpecAndPolicy(UNIVERSITY_IDEMIX,
      CryptoTechnology.IDEMIX, 6, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
  public static final SpecAndPolicy universityUProve = new SpecAndPolicy(UNIVERSITY_UPROVE,
      CryptoTechnology.UPROVE, 6, 10,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");


  public void initIssuer() throws Exception {
    if (IssuanceHelper.isInit()) {
      System.out.println("initIssuer - already setup..");
      return;
    }
    System.out.println("setup IssuanceHelper");
    IssuanceHelper.resetInstance();

    String issuer_fileStoragePrefix;
    if (new File("target").exists()) {
      issuer_fileStoragePrefix = "target/issuer_";
    } else {
      issuer_fileStoragePrefix = "integration-test-verifier/target/issuer_";
    }

    IssuanceHelper.initInstance(1024, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
        new SpecAndPolicy[] {universityIdemix, universityUProve},
        new ArrayList<RevocationAuthorityParameters>());

    System.out.println("IssuanceHelper - done!");
  }

  private void initHelper(CryptoTechnology cryptoTechnology, String user) {

    try {
      UserHelper.resetInstance();

      String targetFolderName;
      if (new File("target").exists()) {
        targetFolderName = "target/";
      } else {
        targetFolderName = "integration-test-verifier/target/";
      }

      String fileStoragePrefix =
          targetFolderName + "user_" + user + "_" + cryptoTechnology.toString().toLowerCase() + "_";

      String[] credSpecResourceList =
          {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"};


      String systemParamsResource = targetFolderName + "issuer_system_params.xml";
      SystemParameters systemParameters = FileSystem.loadXmlFromResource(systemParamsResource);

      List<IssuerParameters> issuerParamsList =
          FileSystem.findAndLoadXmlResourcesInDir(targetFolderName, "issuer_params");
      List<InspectorPublicKey> inspectorPublicKeyList = null;
      List<RevocationAuthorityParameters> revocationAuthorityParametersList = null;
      List<CredentialSpecification> credSpecList =
          FileSystem.loadXmlListFromResources(credSpecResourceList);


      UserHelper.initInstance(systemParameters, issuerParamsList, fileStoragePrefix, credSpecList,
          inspectorPublicKeyList, revocationAuthorityParametersList);

      URI scope = URI.create("urn:patras:registration");
      SoftwareSmartcard softwareSmartcard =
          SoftwareSmartcardGenerator.initSmartCard(1234, scope, systemParameters, issuerParamsList,
              null, 0);
      BigInteger pseValue = softwareSmartcard.computeScopeExclusivePseudonym(1234, scope);

      // add to mangager
      UserHelper.getInstance().cardStorage.addSmartcard(softwareSmartcard, 1234);

      IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(pseValue);
      System.out.println("UserHelper Done");

      System.out.println("UserService init ! DONE");

    } catch (Exception e) {
      throw new IllegalStateException("Could not start up!", e);
    }
  }

  final static String baseUrl = "http://localhost:9091/integration-test-verifier";

  public void initVerifier() throws Exception {
    Client client = Client.create();
    Builder initResource = client.resource(baseUrl + "/init").accept(MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("INIT OK !" + response);
  }

  @Test
  public void testVerify_Patras_Idemix() throws Exception {
    System.out.println("---- test Patras University Idemix----");
    initIssuer();
    initVerifier();
    initHelper(CryptoTechnology.IDEMIX, "student_42");
    issueCredential(universityIdemix);
    runVerification("presentationPolicyPatrasUniversityLogin.xml");
  }

  @Test
  public void testVerify_Patras_UProve() throws Exception {
    System.out.println("---- test Patras University UProve----");
    initIssuer();
    initVerifier();
    initHelper(CryptoTechnology.UPROVE, "student_42");
    issueCredential(universityUProve);
    runVerification("presentationPolicyPatrasUniversityLogin.xml");
  }

  private void issueCredential(SpecAndPolicy specAndPolicy) throws Exception {
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();

    attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
    attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
    attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", 42);

    attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Stewart");
    attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Dent");

    IssuanceMessage service_im =
        IssuanceHelper.getInstance().initIssuance(specAndPolicy.key, attributeValueMap);

    while (true) {
      IssuMsgOrCredDesc resp = null;
      // invoke user
      resp = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, service_im);
      System.out.println(" - user im : " + resp);

      if (resp.im == null) {
        System.out.println(" - user cd : " + resp);
        break;
      }
      System.out.println(" - user im : " + resp);

      // invoke issuer!
      System.out.println(" - invoke ABCE - next step!");

      IssuanceMessageAndBoolean im_and_boolean = IssuanceHelper.getInstance().issueStep(resp.im);

      service_im = im_and_boolean.getIssuanceMessage();
    }
    System.out.println("User now has Credential!");

  }

  public void runVerification(String policy) throws Exception {

    System.out.println("---- runVerification : " + policy);

    System.out.println("ITVerifier - getPolicy from server...");
    Client client = Client.create();
    Builder policyResource =
        client.resource(ITVerifier.baseUrl + "/policy/" + policy).accept(MediaType.TEXT_XML);;

    PresentationPolicyAlternatives pp = ((JAXBElement<PresentationPolicyAlternatives>) (Object)
        policyResource.get(PresentationPolicyAlternatives.class)).getValue();

    System.out.println("ITVerifier - Policy From Server : " + pp);
    System.out.println("USER SIDE PresentationPolicyAlternatives : " + pp);
    // System.out.println("- original size of application data : " +
    // pp.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent().size());
    System.out.println("- XML " + XmlUtils.toXml(of.createPresentationPolicyAlternatives(pp)));

    System.out.println("ITVerifier - call ABCE...");
    // try {
    // boolean satisfy = UserHelper.getInstance().getEngine().canBeSatisfied(pp);
    // System.out.println("WE CAN CREATE PRESENTATION TOKEN !!! " + satisfy);
    // } catch (Throwable e) {
    // System.err.println("Call to ABCE Failed...");
    // throw new Exception("Userengine failed to create Presentation Token : ", e);
    // }

    PresentationToken pt;
    try {
      pt = UserHelper.getInstance().getEngine().createPresentationTokenFirstChoice(USERNAME, pp);
      System.out.println("WE HAVE A PRESENTATION TOKEN !!! " + pt);
      System.out.println("- " + XmlUtils.toXml(of.createPresentationToken(pt)));
    } catch (Throwable e) {
      System.err.println("Call to ABCE Failed...");
      throw new Exception("Userengine failed to create Presentation Token : ", e);
    }
    if (pt == null) {
      throw new Exception("Presentation policy cannot be satisfied");
    }
    System.out.println("ITVerifier - present PresentationToken..." + pt);


    Builder verifyResource =
        client.resource(ITVerifier.baseUrl + "/verify/" + policy).type(MediaType.APPLICATION_XML)
            .accept(MediaType.TEXT_XML);

    JAXBElement<PresentationToken> request = of.createPresentationToken(pt);
    verifyResource.post(request);
    System.out.println("ITVerifier - DONE...");

  }
}

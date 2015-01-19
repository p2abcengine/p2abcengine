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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.14 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// * *
// * This file is licensed under the Apache License, Version 2.0 (the *
// * "License"); you may not use this file except in compliance with *
// * the License. You may obtain a copy of the License at: *
// * http://www.apache.org/licenses/LICENSE-2.0 *
// * Unless required by applicable law or agreed to in writing, *
// * software distributed under the License is distributed on an *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY *
// * KIND, either express or implied. See the License for the *
// * specific language governing permissions and limitations *
// * under the License. *
// */**/****************************************************************

package eu.abc4trust.ri.service.it.inspector;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.service.inspector.InspectService;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.smartcard.SoftwareSmartcardGenerator;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.Attribute;
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

public class ITInspector {

  public ITInspector() {
    System.out.println("ITInspector");
  }

  final String baseUrl = "http://localhost:9092/integration-test-inspector";
  private static final String USERNAME = "defaultUser";

  ObjectFactory of = new ObjectFactory();

  @Test @Ignore
  public void inspectPresentationToken() throws Exception {

    System.out.println("---- inspectPresentationToken ----");

    this.initIssuer();

    System.out.println("-- init inspector in service");
    Client client = Client.create();
    Builder initResource =
        client.resource(this.baseUrl + "/inspector/init/patras").accept(MediaType.TEXT_PLAIN);
    String response = initResource.get(String.class);
    System.out.println("-- init inspector in service - Response : " + response);

    System.out.println("-- init local engines for issuer, verifier and user");

    this.initVerifierAndUser(CryptoTechnology.IDEMIX);

    if (UserHelper.getInstance().credentialManager.listCredentials(USERNAME).size() > 0) {
      System.out.println("We allready have credentials!");
    } else {

      Map<String, Object> attributeValueMap = new HashMap<String, Object>();
      IssuanceMessage service_im;
      attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
      attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
      attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", 42);

      attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Stewart");
      attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Dent");

      service_im = IssuanceHelper.getInstance().initIssuance(UNIVERSITY_IDEMIX, attributeValueMap);

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
    System.out.println(" :: " + UserHelper.getInstance().credentialManager.listCredentials(USERNAME));

    // create presentation token!

    System.out.println("Create PresentationPolicy");
    String applicationData = null;
    String policyName = "presentationPolicyPatrasUniversityWithInspection.xml";

    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    PresentationPolicyAlternatives policy =
        VerificationHelper.getInstance().createPresentationPolicy(policyName, nonce,
            applicationData, null);
    System.out.println("Create PresentationPolicy " + policy);
    System.out.println("Create PresentationPolicy "
        + XmlUtils.toXml(of.createPresentationPolicyAlternatives(policy)));

    UserHelper.getInstance().getEngine().canBeSatisfied(USERNAME, policy);
    System.out.println("Policy can be satisfied!");
    //
    PresentationToken presentationToken =
        UserHelper.getInstance().getEngine().createPresentationTokenFirstChoice(USERNAME, policy);
    System.out.println("Created PresentationToken " + presentationToken);
    System.out.println("Created PresentationToken "
        + XmlUtils.toXml(of.createPresentationToken(presentationToken)));

    VerificationHelper.getInstance().verifyToken(policyName, nonce, applicationData,
        presentationToken);
    System.out.println("Verify OK!");

    // // local in memory Inspect...
    runLocalInspection(presentationToken);

    // send to inspector!
    Builder inspectResource =
        client.resource(this.baseUrl + "/inspector/inspect?issuedValue=42").accept(
            MediaType.TEXT_PLAIN);
    System.out.println("Inspect!");

    String inspectionResult =
        inspectResource.post(String.class, this.of.createPresentationToken(presentationToken));

    System.out.println("Inspect ! OK " + inspectionResult);
  }


  public static final String UNIVERSITY_IDEMIX = "UNIVERSITY_IDEMIX";

  public static final SpecAndPolicy universityIdemix = new SpecAndPolicy(UNIVERSITY_IDEMIX,
      CryptoTechnology.IDEMIX, 6, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");

  public void initIssuer() throws Exception {
    System.out.println("initIssuer(CryptoEngine...");

    System.out.println("setup IssuanceHelper");
    IssuanceHelper.resetInstance();

    // File folder;
    String issuer_fileStoragePrefix;
    if (new File("target").exists()) {
      issuer_fileStoragePrefix = "target/issuer_";
    } else {
      issuer_fileStoragePrefix = "integration-test-inspector/target/issuer_";
    }

    IssuanceHelper.initInstance(1024, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
        new SpecAndPolicy[] {universityIdemix}, new ArrayList<RevocationAuthorityParameters>());

    System.out.println("IssuanceHelper - done!");
  }

  public void initVerifierAndUser(CryptoTechnology cryptoTechnology) throws Exception {
    System.out.println("initIssuerAndUser : " + cryptoTechnology);

    File folder;
    // String issuer_fileStoragePrefix;
    String verifier_fileStoragePrefix;
    String user_fileStoragePrefix;
    if (new File("target").exists()) {
      verifier_fileStoragePrefix = "target/verifier_";
      user_fileStoragePrefix = "target/user_";
      folder = new File("target");
    } else {
      verifier_fileStoragePrefix = "integration-test-inspector/target/verifier_";
      user_fileStoragePrefix = "integration-test-inspector/target/user_";
      folder = new File("integration-test-inspector/target");
    }
    user_fileStoragePrefix += cryptoTechnology.toString().toLowerCase() + "_";

    List<InspectorPublicKey> inspectorPublicKeyList =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "inspector_inspector_publickey");

    System.out.println("Setup VerificationHelper");
    VerificationHelper.resetInstance();

    String[] presentationPolicyResources =
        {"/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityForRevocation.xml", 
         "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityWithInspection.xml"};

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);


    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "issuer_issuer_params_", "idemix");

    String systemParamsResource =
        folder.getName() + "/issuer_" + VerificationHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParameters = FileSystem.loadXmlFromResource(systemParamsResource);

    VerificationHelper.initInstance(systemParameters, issuerParamsList, credSpecList,
        inspectorPublicKeyList, new ArrayList<RevocationAuthorityParameters>(),
        verifier_fileStoragePrefix, presentationPolicyResources);

    System.out.println("VerificationHelper - Done");

    System.out.println("Setup UserHelper");
    UserHelper.resetInstance();

    // todo Inspector keys must be added to init of UserHelper...
    UserHelper.initInstance(systemParameters, issuerParamsList, user_fileStoragePrefix,
        credSpecList, inspectorPublicKeyList, new ArrayList<RevocationAuthorityParameters>());
    URI scope = URI.create("urn:patras:registration");
    SoftwareSmartcard softwareSmartcard =
        SoftwareSmartcardGenerator.initSmartCard(1234, scope, systemParameters, issuerParamsList,
            null, 0);
    BigInteger pseValue = softwareSmartcard.computeScopeExclusivePseudonym(1234, scope);

    // add to mangager
    UserHelper.getInstance().cardStorage.addSmartcard(softwareSmartcard, 1234);

    IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(pseValue);
    System.out.println("UserHelper Done");
  }



  // ===================================================

  public void runLocalInspection(PresentationToken presentationToken) throws Exception {
    System.out.println("=========== LOCAL INSPECT - IN VM ==============");
    setupInspector();
    inspect(presentationToken);
  }

  public void setupInspector() throws Exception {

    System.out.println("=========== LOCAL INSPECT - IN VM ==============");

    // URI[] inspectorPublicKeyUIDs = {URI.create("urn:soderhamn:inspectorpk")};
    String fileStoragePrefix;
    String spResource;
    if (new File("target").exists()) {
      fileStoragePrefix = "target/inspector_";
      spResource = "target/issuer_" + AbstractHelper.SYSTEM_PARAMS_XML_NAME;
    } else {
      fileStoragePrefix = "integration-test-inspector/target/inspector_";
      spResource =
          "integration-test-inspector/target/issuer_" + AbstractHelper.SYSTEM_PARAMS_XML_NAME;
    }

    // Init Inspector :
    SystemParameters systemParams = FileSystem.loadXmlFromResource(spResource);

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    InspectorHelper.resetInstance();
    InspectorHelper.initInstance(fileStoragePrefix, fileStoragePrefix, systemParams,
        InspectService.inspectorPublicKeyUIDs, credSpecList);
    System.out.println("SysParaams "
        + InspectorHelper.getInstance().keyManager.getSystemParameters());
  }

  public void inspect(PresentationToken presentationToken) throws Exception {

    List<Attribute> atts = InspectorHelper.getInstance().inspect(presentationToken);
    if (atts != null) {
      System.out.println("- inspected attributes : " + atts);
      for (Attribute a : atts) {
        System.out.println("- " + a.getAttributeUID() + " : " + a.getAttributeValue() + " : "
            + a.getAttributeDescription().getDataType() + " : "
            + a.getAttributeDescription().getEncoding());
      }
    } else {
      System.out.println("- inspected attributes is null : " + atts);
    }
  }

}

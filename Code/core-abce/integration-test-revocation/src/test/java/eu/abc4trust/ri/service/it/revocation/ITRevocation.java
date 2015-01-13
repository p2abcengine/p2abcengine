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

package eu.abc4trust.ri.service.it.revocation;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.LoggingFilter;

import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.service.revocation.RevocationService;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.smartcard.SoftwareSmartcardGenerator;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITRevocation {
  final static String baseUrl = "http://localhost:9094/integration-test-revocation";
  private static final String USERNAME = "defaultUser";

  public ITRevocation() {}

  ObjectFactory of = new ObjectFactory();

//  @Test
  public void verifyPresentationTokenWithRevocation() throws Exception {

    System.out.println("---- verifyPresentationTokenWithRevocation ----");

    this.initIssuer();

    System.out.println("-- init revocation authority in service");
    Client client = Client.create();
    client.addFilter(new LoggingFilter());
    Builder initResource = client.resource(baseUrl + "/init/patras").accept(MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("-- init revocation authority in service DONE" + response);

    // re-init Issuer With RevocationAuthorities
    this.initIssuer();

    System.out.println("-- init local engines for issuer, verifier and user");

    this.initVerifierAndUser(CryptoTechnology.IDEMIX);

    CredentialDescription credentialDescription = null;

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    IssuanceMessage service_im;

    attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
    attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
    attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", 42);

    attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Stewart");
    attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Dent");

    service_im = IssuanceHelper.getInstance().initIssuance(UNIVERSITY_IDEMIX, attributeValueMap);
    // }

    while (true) {
//      System.out.println("ISSUER IM : \n"
//          + XmlUtils.toXml(new ObjectFactory().createIssuanceMessage((service_im))));
      System.out.println("ISSUER IM : " + service_im);
      IssuMsgOrCredDesc user_im = null;
      // invoke user
      user_im = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, service_im);
      System.out.println(" - user im : " + user_im);

      if (user_im.im == null) {
        System.out.println(" - protocol finished... BREAK : " + user_im);
        System.out.println(" - protocol finished... BREAK : " + user_im.im);
        System.out.println(" - protocol finished... BREAK : " + user_im.cd);
        credentialDescription = user_im.cd;
        break;
      }
//      System.out.println("USER IM : \n"
//          + XmlUtils.toXml(new ObjectFactory().createIssuanceMessage((user_im.im))));
      System.out.println("USER IM : " + user_im.im);

      // invoke issuer!
      System.out.println(" - invoke ABCE - next step!");

      IssuanceMessageAndBoolean im_and_boolean = IssuanceHelper.getInstance().issueStep(user_im.im);

      service_im = im_and_boolean.getIssuanceMessage();
      if (im_and_boolean.isLastMessage()) {
        System.out.println("LastMessage ON SERVER ! " + service_im.getContext() + " ; "
            + im_and_boolean.getIssuanceLogEntryURI() + " : "
            + im_and_boolean.getIssuanceMessage().getContent());
        for (Object o : service_im.getContent()) {
          if (o instanceof JAXBElement<?>) {
            Object abc = ((JAXBElement<?>) o).getValue();
            System.out.println(" - " + abc);
            if (abc instanceof CredentialDescription) {
              credentialDescription = (CredentialDescription) abc;
            }
          } else {
            System.out.println(" - " + o);
          }
        }
      } else {
        System.out.println("NOT LAST!");
      }
    }
    System.out.println("User now has Credential! " + credentialDescription);
    System.out.println(" :: " + UserHelper.getInstance().credentialManager.listCredentials(USERNAME));
    URI credUri = UserHelper.getInstance().credentialManager.listCredentials(USERNAME).get(0);
    credentialDescription =
        UserHelper.getInstance().credentialManager.getCredentialDescription(USERNAME, credUri);
    System.out.println("credentialDescription : "
        + XmlUtils.toXml(of.createCredentialDescription(credentialDescription)));
    // run presentation with Revokable Credential...
    this.runPresenationWithRevokableCredential(true);

    // DO REVOCATION!
    Attribute attribute = null;
    for (Attribute a : credentialDescription.getAttribute()) {
      if (a.getAttributeDescription().getType().compareTo(RevocationConstants.REVOCATION_HANDLE) == 0) {
        attribute = a;
      }
    }
    RevocationInformation riFromRevocation = null;
    if (attribute != null) {
      System.out.println("Revoke Credential - by Attribute ! : " + attribute);
      AttributeList list = new AttributeList();
      list.getAttributes().add(attribute);
      Builder revokeResource =
          client.resource(
              baseUrl
                  + "/revocation/revokeAttribute/"
                  + URLEncoder.encode(RevocationService.patrasRevocationAuthority.toString(),
                      "UTF-8")).accept(MediaType.APPLICATION_XML);
      riFromRevocation = ((JAXBElement<RevocationInformation>)(Object)
          revokeResource.post(RevocationInformation.class, this.of.createAttribute(attribute))).getValue();

      System.out.println("XXX GET RI 1 " + riFromRevocation.getRevocationInformationUID());
    }

    this.runPresenationWithRevokableCredential(false);

    System.out.println("Revocation Test ! OK ");

  }


  private void runPresenationWithRevokableCredential(
      /* RevocationInformation revocationInformation, */boolean verify_ok) throws Exception {
    // create presentation token!


    System.out.println("#####################################################");
    System.out.println("Create PresentationPolicy");
    String applicationData = null;
    // String policyName = "presentationPolicySoderhamnSchoolWithInspection.xml";
    String policyName = "presentationPolicyPatrasUniversityForRevocation.xml";
    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    PresentationPolicyAlternatives policy =
        VerificationHelper.getInstance().createPresentationPolicy(policyName, nonce,
            applicationData, null);
    System.out.println("Created PresentationPolicy " + policy);
    System.out.println("Created PresentationPolicy "
        + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(policy)));

    //
    PresentationToken presentationToken =
        UserHelper.getInstance().getEngine().createPresentationTokenFirstChoice(USERNAME, policy);

    if (!verify_ok && (presentationToken == null)) {
      System.out.println("Could not create presentationToken - this was expected!");
      return;
    }

    System.out.println("Created PresentationToken " + presentationToken);
    System.out.println("Created PresentationToken "
        + XmlUtils.toXml(this.of.createPresentationToken(presentationToken)));

    Exception failure = null;
    try {
      VerificationHelper.getInstance().verifyToken(policy, // policyName, nonce, applicationData,
          presentationToken);
    } catch (Exception e) {
      failure = e;
    }
    if (verify_ok) {
      if (failure == null) {
        System.out.println("Verify OK!");
      } else {
        throw new Exception("Verification Should not Fail ??", failure);
      }
    } else {
      if (failure == null) {
        System.out.println("Verify Should Fail!");
        throw new IllegalStateException("Verify Should Fail!");
      } else {
        System.out.println("Verify Failed as expected!");
      }
    }


  }

  public static final String UNIVERSITY_IDEMIX = "UNIVERSITY_IDEMIX";

  public static final SpecAndPolicy universityIdemix = new SpecAndPolicy(UNIVERSITY_IDEMIX,
      CryptoTechnology.IDEMIX, null, 6, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml",
      RevocationService.patrasRevocationAuthority.toString());

  public void initIssuer() throws Exception {
    System.out.println("initIssuer(CryptoEngine...");

    System.out.println("setup IssuanceHelper");
    IssuanceHelper.resetInstance();

    // File folder;
    String issuer_fileStoragePrefix;
    File folder;
    if (new File("target").exists()) {
      issuer_fileStoragePrefix = "target/issuer_";
      folder = new File("target");
    } else {
      issuer_fileStoragePrefix = "integration-test-user/target/issuer_";
      folder = new File("integration-test-user/target");
    }

    List<RevocationAuthorityParameters> revocationAuthorityParameters =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "revocation_revocation_authority");

    IssuanceHelper.initInstance(1024, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
        new SpecAndPolicy[] {universityIdemix}, revocationAuthorityParameters);

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

    String[] presentationPolicyResources = {
        "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityForRevocation.xml"};

    String[] credSpecResourceList = {
        "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);


    String systemParamsResource =
        folder.getName() + "/issuer_" + VerificationHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamsResource);

    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "issuer_issuer_params_", "idemix");

    List<RevocationAuthorityParameters> revocationAuthorityParameters =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "revocation_revocation_authority");

    VerificationHelper.initInstance(systemParams, issuerParamsList, credSpecList,
        inspectorPublicKeyList, revocationAuthorityParameters, verifier_fileStoragePrefix,
        presentationPolicyResources);
    System.out.println("VerificationHelper - Done");

    System.out.println("Setup UserHelper");
    UserHelper.resetInstance();

    // todo Inspector keys must be added to init of UserHelper...
    UserHelper.initInstance(systemParams, issuerParamsList, user_fileStoragePrefix, credSpecList,
        inspectorPublicKeyList, revocationAuthorityParameters);

    URI scope = URI.create("urn:patras:registration");
    SoftwareSmartcard softwareSmartcard =
        SoftwareSmartcardGenerator.initSmartCard(1234, scope, systemParams, issuerParamsList, null,
            0);
    BigInteger pseValue = softwareSmartcard.computeScopeExclusivePseudonym(1234, scope);

    // add to mangager
    UserHelper.getInstance().cardStorage.addSmartcard(softwareSmartcard, 1234);

    IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(pseValue);
    System.out.println("UserHelper Done");

  }
}

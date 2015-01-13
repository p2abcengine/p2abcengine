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

package eu.abc4trust.ri.service.verifier;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 */
@Path("/")
public class VerificationService {

  public static final boolean USE_KEYS_FROM_ISSUER_TEST = true;

  public VerificationService() throws Exception {
    System.out.println("VerificationService");
  }

  private void initVerificationHelper() {
    if (VerificationHelper.isInit()) {
      System.out.println("VerificationHelper - already setup!");
      return;
    }
    try {
      VerificationHelper.resetInstance();

      System.out.println("VerificationHelper - try to - init!");

      String targetFolderName;
      if (new File("target").exists()) {
        targetFolderName = "target/";
      } else {
        targetFolderName = "integration-test-verifier/target/";
      }

      String fileStoragePrefix = targetFolderName + "verifier_";


      List<IssuerParameters> issuerParamsList =
          FileSystem.findAndLoadXmlResourcesInDir(targetFolderName, "issuer_params");

      String[] credSpecResourceList =
          {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"};
      List<CredentialSpecification> credSpecList =
          FileSystem.loadXmlListFromResources(credSpecResourceList);

      String[] presentationPoliciesResouces =
          {"/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml"};

      List<InspectorPublicKey> inspectorPublicKeyList = null;
      List<RevocationAuthorityParameters> revAuthParamsList = null;
      String systemParamsResource = targetFolderName + "issuer_system_params.xml";
      SystemParameters systemParameters = FileSystem.loadXmlFromResource(systemParamsResource);

      VerificationHelper.initInstance(systemParameters, issuerParamsList, credSpecList,
          inspectorPublicKeyList, revAuthParamsList, fileStoragePrefix,
          presentationPoliciesResouces);

      System.out.println("IssuanceHelper - done!");
    } catch (Exception e) {
      System.out.println("Create Domain FAILED " + e);
      e.printStackTrace();
    }
  }



  ObjectFactory of = new ObjectFactory();

  @GET()
  @Path("/init")
  @Produces(MediaType.TEXT_PLAIN)
  public String init() {
    System.out.println("issuance service.init...");
    this.initVerificationHelper();
    return "OK";
  }

  @GET()
  @Path("/reset")
  @Produces(MediaType.TEXT_PLAIN)
  public String reset() {
    System.out.println("Service Reset");
    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
    VerificationHelper.resetInstance();

    return "OK";
  }


  static String appData = null; // "AppData";
  static byte[] nonce;

  @Path("/policy/{PolicyId}")
  @GET()
  @Produces({MediaType.TEXT_XML})
  public JAXBElement<PresentationPolicyAlternatives> getPolicy(
      @PathParam("PolicyId") final String policyId) throws Exception {

    System.out.println("VerificationService - Policy - with Policy : " + policyId);

    nonce = VerificationHelper.getInstance().generateNonce();

    // TODO : add revinfouid map
    PresentationPolicyAlternatives ppa =
        VerificationHelper.getInstance().createPresentationPolicy(policyId, nonce, appData, null);

    // System.out.println("PresentationPolicyAlternatives : " + ppa);
    // System.out.println("- original size of application data : " +
    // ppa.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent().size());

    return this.of.createPresentationPolicyAlternatives(ppa);
  }

  @Path("/verify/{PolicyId}")
  @POST()
  @Produces({MediaType.TEXT_XML})
  public Response verifyPolicy(@PathParam("PolicyId") final String policyId,
      final JAXBElement<PresentationToken> presentationToken_jaxb) throws Exception {
    System.out.println("VerificationService - Verify - with Policy : " + policyId);
    PresentationToken presentationToken = presentationToken_jaxb.getValue();
    try {
      System.out.println(" - XML : "
          + XmlUtils.toXml(this.of.createPresentationToken(presentationToken), false));
    } catch (Exception e) {
      System.out.println(" - XML ERROR ??? : " + e);
    }

    if ("presentationPolicyPatrasUniversityLogin.xml".equals(policyId)) {
      // get pseudonym value !
      BigInteger mustMatch = null;
      // TODO(enr): This check doesn't make any sense, please don't hardcode values in test
      /*
       * BigInteger presentedPseudonymValue = AbstractHelper.getPseudonymValue(presentationToken,
       * "urn:patras:registration"); if(clientEngine == CryptoEngine.IDEMIX) { mustMatch =
       * IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_Idemix; } else { mustMatch =
       * IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_UProve; }
       * if(mustMatch.equals(presentedPseudonymValue)) {
       * System.out.println("Correct PseudonymValue presented"); } else { throw new
       * IllegalStateException(clientEngine.toString() + " pseudonym expected with value " +
       * mustMatch + " - got : " + presentedPseudonymValue); }
       * 
       * 
       * // get cryptoengine ProductionModule.CryptoEngine usedUserCryptoEngine =
       * AbstractHelper.getCryptoEngineForPseudonym(presentationToken, "urn:patras:registration");
       * System.out.println("usedUserCryptoEngine : " + usedUserCryptoEngine); if(clientEngine ==
       * AbstractHelper.oldCryptoEngineToNewCryptoEngine(usedUserCryptoEngine)) {
       * System.out.println("- Correct User ABCE CryptoEngine used " + usedUserCryptoEngine); } else
       * { throw new IllegalStateException(clientEngine.toString() +
       * " expected to be used by User - but seems to be : " + usedUserCryptoEngine); }
       */
    }

    try {
      boolean ok =
          VerificationHelper.getInstance().verifyToken(policyId, nonce, appData, presentationToken);

      if (ok) {
        System.out.println(" - verify OK");
        return Response.status(202).build();
      } else {
        System.out.println(" - verify FAILED");
        return Response.status(406).build();
      }
    } catch (Exception e) {
      System.err.println("- verify Exception");
      e.printStackTrace();
      throw e;
    }

  }
}

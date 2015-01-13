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

package eu.abc4trust.ri.service.it.issuer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializerObjectGzip;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.smartcard.SoftwareSmartcardGenerator;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerificationCall;
import eu.abc4trust.xml.util.XmlUtils;

public abstract class AbstractIT {

  private static final PseudonymSerializer pseudonymSerializer = new PseudonymSerializerObjectGzip(
      new CardStorage());

  static ObjectFactory of = new ObjectFactory();
  private static final String USERNAME = "defaultUser";

  String[] credSpecResourceList = {
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
      "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
      "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml",
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"};

  String[] issuerParamsResourceList_base = new String[] {
      "/issuer_issuer_params_my_country_identitycard_issuancekey_v1.0",
      "/issuer_issuer_params_urn_patras_issuer_credUniv",
      "/issuer_issuer_params_urn_patras_issuer_credCourse",
      "/issuer_issuer_params_urn_soderhamn_issuer_credSchool",
      "/issuer_issuer_params_urn_soderhamn_issuer_credSubject"};


  String[] presentationPolicyResources = {
      "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml",
      "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml",
      "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeFrench.xml",
      "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeEnglish.xml",
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml",
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml"};


  public SoftwareSmartcard initHelper(CryptoTechnology cryptoTechnology, String userStorage,
      String testcase, URI scope) {

    try {
      //
      initIssuer();

      //
      UserHelper.resetInstance();
      System.out.println("UserService initHelpe!..");

      String fileStoragePrefix;
      String user_fileStoragePrefix;
      File folder;
      if (new File("target").exists()) {
        fileStoragePrefix = "target/"; // + user + "_";
        folder = new File("target");
      } else {
        fileStoragePrefix = "integration-test-issuer/target/"; // + user + "_";
        folder = new File("integration-test-issuer/target");
      }
      user_fileStoragePrefix = fileStoragePrefix + userStorage;

      List<IssuerParameters> issuerParamsList =
          FileSystem.findAndLoadXmlResoucesInDir(folder, testcase, "issuer_params");
      List<CredentialSpecification> credSpecList =
          FileSystem.loadXmlListFromResources(credSpecResourceList);

      List<InspectorPublicKey> inspectorPublicKeyList = null;
      List<RevocationAuthorityParameters> revAuthParamsList = null;

      String systemParamsResource = fileStoragePrefix + "issuer_system_params.xml";
      SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamsResource);

      UserHelper.initInstance(systemParams, issuerParamsList, user_fileStoragePrefix, credSpecList,
          inspectorPublicKeyList, revAuthParamsList);

      KeyManager keyManager = UserHelper.getInstance().keyManager;
      IssuerParameters exists =
          keyManager
              .getIssuerParameters(new URI("http://my.country/identitycard/issuancekey_v1.0"));
      if (exists != null) {
        System.out.println("Issuer Params - created as expected!");
      } else {
        System.out.println("Issuer Params - ?? Not found!");
      }

      System.out.println("UserService static init ! DONE");


      String softwareSmartcardFileName = user_fileStoragePrefix + "_software_smartcard";
      SoftwareSmartcard smartcard = null;
      try {
        smartcard = FileSystem.loadObjectFromResource(softwareSmartcardFileName);
      } catch (IOException ignore) {}
      // add smartcard for test!
      if (smartcard == null) {
        // create smartcard...
        List<IssuerParameters> issuerParamsResourceListForCrypto =
            FileSystem.findAndLoadXmlResoucesInDir(folder, "issuer_params", testcase, cryptoTechnology
                .toString().toLowerCase());

        smartcard =
            SoftwareSmartcardGenerator.initSmartCard(1234, scope, systemParams,
                issuerParamsResourceListForCrypto, null, 0);

        if (scope != null) {
          BigInteger pseValue = smartcard.computeScopeExclusivePseudonym(1234, scope);
          // register Psedonym
          Client client = Client.create();
          Builder initResource =
              client.resource(this.baseUrl + "/issue/register?pseudonym=" + pseValue).accept(
                  MediaType.TEXT_PLAIN);
          String response = initResource.get(String.class);
          System.out.println("INIT OK !" + response);

        }
      }
      UserHelper.getInstance().cardStorage.addSmartcard(smartcard, 1234);

      // ======================================================================

      VerificationHelper.resetInstance();
      System.out.println(" - Init Verificateion Helper...");

      String verifier_fileStoragePrefix = fileStoragePrefix + "verifier_generic_";

      VerificationHelper.initInstance(systemParams, issuerParamsList, credSpecList,
          inspectorPublicKeyList, revAuthParamsList, verifier_fileStoragePrefix,
          this.presentationPolicyResources);

      return smartcard;
    } catch (Exception e) {
      throw new IllegalStateException("Could not start up!", e);
    }
  }

  // private String[] createIssuerParamsWithCryptoEngine(File folder, String[] baseResourceList,
  // CryptoTechnology... engines) {
  // List<String> resourceList = new ArrayList<String>();
  // for(CryptoTechnology engine : engines) {
  // String ce_append = "_" + engine.toString().toLowerCase();
  // for(String resource : baseResourceList) {
  // resourceList.add(folder.getAbsolutePath() + resource + ce_append);
  // }
  // }
  // return resourceList.toArray(new String[0]);
  // }

  public void initIssuer() throws Exception {
    Client client = Client.create();
    Builder initResource =
        client.resource(this.baseUrl + "/issue/init").accept(MediaType.TEXT_PLAIN);
    String response = initResource.get(String.class);
    System.out.println("INIT OK !" + response);
  }


  // public void XinitPseudonym(SoftwareSmartcard softwareSmartcard, String scopeString, int
  // matNumber) throws Exception {
  // CryptoEngineUser ceu = UserHelper.getInstance().cryptoEngineUser;
  //
  // URI secretUid = URI.create("secret://sample-1234");
  // URI pseudonymUri = URI.create("foo-bar-pseudonym-uid-" + matNumber);
  // boolean exclusive = true;
  // PseudonymWithMetadata pwm = ceu.createPseudonym(pseudonymUri, scopeString, exclusive,
  // secretUid);
  //
  // System.out.println("---Created pseudonym---");
  // System.out.println(XmlUtils.toXml(new ObjectFactory().createPseudonymWithMetadata(pwm)));
  //
  // CredentialManager userCredentialManager = UserHelper.getInstance().credentialManager;
  // userCredentialManager.storePseudonym(pwm);
  // }


  // @After
  public void resetIssuerEngine() throws Exception {
    System.out.println("resetIssuerEngine...");
    Client client = Client.create();
    Builder initResource =
        client.resource(this.baseUrl + "/issue/reset/").accept(MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("Reset OK !" + response);

    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
    VerificationHelper.resetInstance();
    System.out.println("resetIssuerEngine DONE");
  }

  // final String baseUrl = "http://localhost:19500/pilot-patras";
  final String baseUrl = "http://localhost:9090/integration-test-issuer";

  protected void runIssuance(String serverMethod, String issuanceKey) throws Exception {
    this.runIssuance(serverMethod, issuanceKey, null);
  }

  protected void runIssuance(String serverMethod, String issuanceKey, String scope)
      throws Exception {

    System.out.println("- run issuance with key : " + issuanceKey);

    Client client = Client.create();
    Builder issueStartResource =
        client.resource(this.baseUrl + "/issue/" + serverMethod + "/" + issuanceKey)
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

    IssuanceMessage server_im = ((JAXBElement<IssuanceMessage>) (Object) issueStartResource.get(IssuanceMessage.class)).getValue();

    //IssuanceMessage server_im = issueStartResource.get(IssuanceMessage.class);
    System.out.println(" - initial message - server : " + server_im);
    System.out.println(" - initial message - server : "
        + XmlUtils.toXml(of.createIssuanceMessage(server_im)));

    System.out.println(" - initial message - client - engine "
        + UserHelper.getInstance().getEngine());
    IssuMsgOrCredDesc user_im =
        UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
    System.out.println(" - initial message - client - created ");
    System.out.println(" - initial message - client - created " + user_im.im);
    System.out.println(" - initial message - client - created "
        + AbstractHelper.getPseudonymValue(user_im.im, scope));
    System.out.println(" - initial message - client - created " + user_im.cd);
    System.out.println(" - initial message - client : "
        + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), false));

    int stepCount = 1;
    boolean lastmessage = false;
    while (!lastmessage) {
      Builder issueStepResource =
          client.resource(this.baseUrl + "/issue/step").type(MediaType.APPLICATION_XML)
              .accept(MediaType.TEXT_XML);

      // send to server and receive new im
      System.out.println(" - contact server");
      server_im = ((JAXBElement<IssuanceMessage>) (Object) 
          issueStepResource.post(IssuanceMessage.class, of.createIssuanceMessage(user_im.im))).getValue();
      System.out.println(" - got response");
      System.out.println(" - step message - server : " + stepCount + " : "
          + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));

      // process in
      user_im = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
      System.out.println(" - step message - client :" + stepCount);

      lastmessage = (user_im.cd != null);
      if (!lastmessage) {
        System.out.println(" - initial message - step : " + stepCount + " : "
            + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), false));
      }
    }
    System.out.println(" - done...");
    System.out.println(" - done : credentialDescription : "
        + XmlUtils.toXml(of.createCredentialDescription(user_im.cd), false));

  }



  /**
   * NOTE : Verification does not run over HTTP
   * 
   * @param policy
   * @param satisfiedExpected
   * @param scope
   * @throws Exception
   */
  protected void runVerification(CryptoTechnology clientEngine, String policy,
      boolean satisfiedExpected, String scope) throws Exception {
    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    PresentationPolicyAlternatives ppa =
        VerificationHelper.getInstance().createPresentationPolicy(policy, nonce, null, null);

    PresentationToken presentationToken;
    try {
      presentationToken = UserHelper.getInstance().getEngine().createPresentationTokenFirstChoice(USERNAME, ppa);
    } catch (CannotSatisfyPolicyException e) {
      if (satisfiedExpected) {
        System.out.println("Credentials does not satisfy policy");
        Assert.assertTrue("Credentials does not satisfy policy", false);
        return;
      } else {
        // this is ok... . failed as expected - user side...
        System.out.println("Credentials does not satisfy policy - This was expected");
        return;
      }
    }
    if (presentationToken == null) {
      if (satisfiedExpected) {
        System.out.println("Credentials does not satisfy policy");
        Assert.assertTrue("Credentials does not satisfy policy", false);
        return;
      } else {
        // this is ok... . failed as expected - user side...
        System.out.println("Credentials does not satisfy policy - This was expected");
        return;
      }
    }

    System.out.println("PresentationToken pseudonym value for scope - " + scope + " : "
        + AbstractHelper.getPseudonymValue(presentationToken, scope));
    System.out.println("PresentationToken : "
        + XmlUtils.toXml(of.createPresentationToken(presentationToken)));
    boolean ok = false;
    try {
      ok = VerificationHelper.getInstance().verifyToken(ppa, presentationToken);
    } catch (Exception e) {
      System.err.println("Credentials not accepted by Verifier ???");
      e.printStackTrace();
      Assert.assertTrue("Credentials not accepted by Verifier ???", false);
    }
    try {
      VerificationCall vc = of.createVerificationCall();
      vc.setPresentationPolicyAlternatives(ppa);
      vc.setPresentationToken(presentationToken);
      System.out.println("VerificationCall : "
          + XmlUtils.toXml(of.createVerificationCall(vc), true));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (ok) {
      if (satisfiedExpected) {
        // this is ok... . failed as expected - user side...
        System.out.println("Verifier accepted");
        return;
      } else {
        System.out.println("Verifier accepted - But : Credentials does not satisfy policy");
        Assert.assertTrue("Verifier accepted - But : Credentials does not satisfy policy", false);
        return;
      }

    } else {
      if (satisfiedExpected) {
        System.out.println("Verifier rejected : Credentials does not satisfy policy");
        Assert.assertTrue("Verifier rejected : Credentials does not satisfy policy", false);
        return;
      } else {
        // this is ok... . failed as expected - user side...
        System.out
            .println("Verifier rejected : Credentials does not satisfy policy - This was expected");
        return;
      }
    }
  }
}

//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.service.it.verifier;


import java.io.File;
import java.io.FilenameFilter;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.service.verifier.VerificationService;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.util.XmlUtils;

public class ITVerifier {

  public ITVerifier() {
    System.out.println("ITVerifier");
  }

  static ObjectFactory of = new ObjectFactory();

  public static final boolean USE_KEYS_FROM_ISSUER_TEST =
      VerificationService.USE_KEYS_FROM_ISSUER_TEST;

  private void initHelper(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String user) {

    try {
      UserHelper.resetInstance();

      File folder;
      String fileStorageFolder;
      String fileStoragePrefix;
      if (ITVerifier.USE_KEYS_FROM_ISSUER_TEST) {
        if (new File("../integration-test-issuer/target/").exists()) {
          System.out.println("use storage in ../integration-test-issuer/target... (MVN from project)");
          fileStorageFolder = "../integration-test-issuer/target/";
        } else {
          fileStorageFolder = "integration-test-issuer/target/";
          System.out.println("use storage in integration-test-issuer/target... (MVN from root)");
        }
      } else {
        fileStorageFolder = "src/test/resources/storage/";
      }
      folder = new File(fileStorageFolder);

      if (clientEngine == CryptoEngine.IDEMIX) {
        fileStoragePrefix = fileStorageFolder + user + "_idemix_";
      } else {
        fileStoragePrefix = fileStorageFolder + user + "_uprove_";
      }

      // String[] credSpecResourceList =
      // { // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcard.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasCourse.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasUniversity.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
      // "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCard.xml"};

      String[] credSpecResourceList =
        { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml"
          , "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"
          , "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml"
          , "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"
          , "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"
        };

      // String systemParamsResource = fileStorageFolder + "issuer_system_params";

      String[] issuerParamsResourceList;
      System.out.println("look for issuer storage files in : " + folder.getAbsolutePath());

      // File systemParamsFile = new File(folder, "issuer_system_params");
      // if (systemParamsFile.exists()) {
      // systemParamsResource = systemParamsFile.getAbsolutePath();
      // }
      // System.out.println("systemparams file : " + systemParamsResource);

      File[] issuerParamsFileList = folder.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          if (arg1.startsWith("issuer_issuer_params_")) {
            System.out.println("Test : " + arg1);
            if (arg1.indexOf("patras") != -1) {
              System.out.println("- reject 'patras'" + arg1);
              return false;
            }
            return true;
          } else {
            return false;
          }
        }
      });
      System.out.println("issuerparams files : " + issuerParamsFileList + " : "
          + issuerParamsFileList.length);
      issuerParamsResourceList = new String[issuerParamsFileList.length];

      for (int ix = 0; ix < issuerParamsFileList.length; ix++) {
        System.out.println(" - " + issuerParamsFileList[ix].getAbsolutePath());
        issuerParamsResourceList[ix] = issuerParamsFileList[ix].getAbsolutePath();
      }
      String[] inspectorPublicKeyResourceList = new String[0];

      UserHelper.initInstance(cryptoEngine, issuerParamsResourceList, fileStoragePrefix,
          credSpecResourceList, inspectorPublicKeyResourceList);

      // // add secret for test
      // Secret secret =
      // (Secret) XmlUtils.getObjectFromXML(
      // this.getClass().getResourceAsStream(
      // "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
      // URI secretUID = secret.getSecretDescription().getSecretUID();
      // System.out.println("System - adding smart card secret : "
      // + secret.getSecretDescription().getSecretUID());
      //
      // try {
      // @SuppressWarnings("unused")
      // Secret exists_secret = UserHelper.getInstance().credentialManager.getSecret(secretUID);
      // System.out.println("Secret Already Exists!! " + secretUID);
      // } catch (SecretNotInStorageException e) {
      // System.out.println("Secret Not In Storage!");
      //
      // UserHelper.getInstance().credentialManager.storeSecret(secret);
      // }

      System.out.println("UserService init ! DONE");

    } catch (Exception e) {
      throw new IllegalStateException("Could not start up!", e);
    }
  }

  final static String baseUrl = "http://localhost:9091/integration-test-verifier";

  public static void initVerifier(CryptoEngine cryptoEngine, CryptoEngine clientEngine)
      throws Exception {
    Client client = Client.create();
    Builder initResource =
        client.resource(baseUrl + "/init/" + cryptoEngine + "?clientEngine=" + clientEngine)
            .accept(MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("INIT OK !" + response);
  }

  @After
  public void resetIssuerEngine() throws Exception {
    Client client = Client.create();
    Builder initResource = client.resource(baseUrl + "/reset/").accept(MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("Reset OK !" + response);
    
    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
    VerificationHelper.resetInstance();
  }


  // @Test
  public void testVerifyIdcard_Alice() throws Exception {
    System.out.println("---- testVerifyIdcard_Alice ----");
    this.initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "alice");
    this.runVerification("presentationPolicySimpleIdentitycard.xml");
  }

  // @Test
  public void testVerifyIdcard_Stewart() throws Exception {
    System.out.println("---- testVerifyIdcard_Stewart ----");
    this.initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "stewart");
    try {
      this.runVerification("presentationPolicySimpleIdentitycard.xml");
      Assert.assertTrue("This should not verify :", false);
    } catch (Exception e) {
      System.out.println("Presentation policy cannot be satisfied - We Expect this!");
      Assert.assertEquals("Presentation policy cannot be satisfied", e.getMessage());
    }
  }

  @Test
  public void testVerify_Patras_Idemix() throws Exception {
    System.out.println("---- testVerifyIdcard_Alice Idemix----");
    ITVerifier.initVerifier(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX);

    this.initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "student_42");
    this.runVerification("presentationPolicyPatrasUniversityLogin.xml");
  }

  @Test
  public void testVerify_Patras_UProve() throws Exception {
    System.out.println("---- testVerifyIdcard_Alice UProve----");
    ITVerifier.initVerifier(CryptoEngine.UPROVE, CryptoEngine.UPROVE);

    this.initHelper(CryptoEngine.UPROVE, CryptoEngine.UPROVE, "student_42");
    this.runVerification("presentationPolicyPatrasUniversityLogin.xml");
  }

  public void runVerification(String policy) throws Exception {

    System.out.println("---- runVerification : " + policy);

    System.out.println("ITVerifier - getPolicy from server...");
    Client client = Client.create();
    Builder policyResource =
        client.resource(ITVerifier.baseUrl + "/policy/" + policy).accept(MediaType.TEXT_XML);;

    PresentationPolicyAlternatives pp = policyResource.get(PresentationPolicyAlternatives.class);

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
      pt = UserHelper.getInstance().getEngine().createPresentationToken(pp);
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

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

package eu.abc4trust.ri.servicehelper.smartcard;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardInitializeTool;
import eu.abc4trust.smartcard.SmartcardInitializeTool.InitializeResult;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

// @Ignore
public class SetupPatrasPilotTest {

  @BeforeClass
  public static void alwaysResetEngine() throws Exception {
    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
  }
  
  private static final String USERNAME = "defaultUser";

  public static String ISSUER_RESOURCES_FOLDER = "sc_issuer_resources";
  public static String ISSUER_STORAGE_FOLDER = "sc_issuer_storage";
  public static String USER_STORAGE_FOLDER = "sc_user_storage";

  public static final String CREDSPEC_UNIVERSITY = "credSpecUniversity";
  public static final String CREDSPEC_COURCE = "credSpecCource";


  public static final SpecAndPolicy university = new SpecAndPolicy(CREDSPEC_UNIVERSITY,
      CryptoTechnology.IDEMIX, 42, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
  public static final SpecAndPolicy cource = new SpecAndPolicy(CREDSPEC_COURCE,
      CryptoTechnology.UPROVE, 42, 1,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");

  public static final String[] CRED_SPEC_RESOURCE_LIST = {
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",};


  private String getFolderName(String basis) {
    File test = new File("service-helper/target");
    System.out.println("Exists : " + basis + " : " + test.exists());
    if (!test.exists()) {
      return new File("target/" + basis).getAbsolutePath();
    } else {
      return new File("service-helper/target" + basis).getAbsolutePath();
    }
  }

  @Test
  public void verifyExistenceOfFolders() throws Exception {
    for (String foldername : new String[] {this.getFolderName(ISSUER_RESOURCES_FOLDER),
        this.getFolderName(ISSUER_STORAGE_FOLDER), this.getFolderName(USER_STORAGE_FOLDER)}) {
      File folder = new File(foldername);
      if (!folder.exists()) {
        System.out.println("Create Folder !" + folder.getAbsolutePath());
        boolean created = folder.mkdirs();
        if (!created) {
          throw new Exception("Could not create Folder : " + foldername);
        }
      } else {
        if (!folder.isDirectory()) {
          throw new Exception("File exists with name of Folder : " + foldername);
        }
      }
    }
  }

  @Test
  public void setupIssuerParams() throws Exception {
    System.out.println("setupIssuerParams..");
    IssuanceHelper.resetInstance();

    String systemAndIssuerParamsPrefix = this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/";
    String fileStoragePrefix = this.getFolderName(ISSUER_STORAGE_FOLDER) + "/";

    IssuanceHelper.initInstance(1024, systemAndIssuerParamsPrefix, fileStoragePrefix,
        new SpecAndPolicy[] {university, cource}, new ArrayList<RevocationAuthorityParameters>());

    System.out.println("setupIssuerParams Done");
  }

  @Test
  public void generatePKIKeys() throws Exception {
    System.out.println("generatePKIKeys");
    System.out.println(" - pki");
    PKIKeyTool.generateSignatureKeys(this.getFolderName(ISSUER_STORAGE_FOLDER), "pki_keys");
    System.out.println(" - cas : ");
    PKIKeyTool.generateSignatureKeys(this.getFolderName(ISSUER_STORAGE_FOLDER), "cas_keys");
    System.out.println(" - DONE : ");
  }

  @Test @Ignore("Smartcard not working")
  public void initSmartCard() throws Exception {
    System.out.println("initSmartCard : ISSUER_RESOURCES_FOLDER : "
        + this.getFolderName(ISSUER_RESOURCES_FOLDER));

    // load PKI
    RSAKeyPair pki_sk_root =
        FileSystem.loadObjectFromResource(this.getFolderName(ISSUER_STORAGE_FOLDER)
            + "/pki_keys_sk");
    // @SuppressWarnings("unused")
    // RSAVerificationKey pki_pk_root =
    // PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_STORAGE_FOLDER) +
    // "/pki_keys_pk");

    RSAVerificationKey cas_public =
        FileSystem.loadObjectFromResource(this.getFolderName(ISSUER_STORAGE_FOLDER)
            + "/cas_keys_pk");


    Random random = new Random(42); // PKIKeyTool.random;
    short deviceID = 42;
    // gen pin : 7388 with Random 42
    int newPin = random.nextInt(9999);
    // gen puk
    int newPuk = random.nextInt(999999);
    // gen mac
    byte[] macKeyForBackup = new byte[PKIKeyTool.MAC_KEY_LENGTH / 8];
    random.nextBytes(macKeyForBackup);

    int sc_id_int = random.nextInt(999999999);
    String sc_id = String.format("%09d", sc_id_int);

    // max_length_256
    URI deviceUri = URI.create("secret://software-smartcard-" + sc_id);


    List<IssuerParameters> issuerParametersList =
        FileSystem.findAndLoadXmlResourcesInDir(this.getFolderName(ISSUER_RESOURCES_FOLDER),
            "issuer_params_urn_patras_issuer");
    List<InspectorPublicKey> inspectorPublicKeyList = new ArrayList<InspectorPublicKey>();
    List<RevocationAuthorityParameters> revParamsList = new ArrayList<RevocationAuthorityParameters>();
    String systemParamResource =
        this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/" + UserHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParameters = FileSystem.loadXmlFromResource(systemParamResource);

    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(CRED_SPEC_RESOURCE_LIST);

    // ?? TODO : (IDEMIX) UserEngine has to be initialized
    UserHelper.resetInstance();
    UserHelper.initInstance(systemParameters, issuerParametersList,
        this.getFolderName(USER_STORAGE_FOLDER) + "/", credSpecList, inspectorPublicKeyList,
        revParamsList);

    SystemParameters sysP = UserHelper.getInstance().keyManager.getSystemParameters();

    //
    URI scope = new URI("urn:patras:registration");
    SmartcardInitializeTool smartcardTool = new SmartcardInitializeTool(pki_sk_root, sysP, scope);

    //
    List<IssuerParameters> issuerParameters_credUniv =
        FileSystem.loadXmlListFromResources(new String[] { this.getFolderName(ISSUER_RESOURCES_FOLDER)
            + "/issuer_params_urn_patras_issuer_credUniv_idemix.xml" } );

    smartcardTool.setIssuerParameters(CryptoEngine.IDEMIX, issuerParameters_credUniv);
    
    IssuerParameters issuerParameters_credCourse =
        FileSystem.loadXmlFromResource(this.getFolderName(ISSUER_RESOURCES_FOLDER)
            + "/issuer_params_urn_patras_issuer_credCourse_uprove.xml");

    smartcardTool.setIssuerParametersForCounterCredential(CryptoEngine.IDEMIX,
        issuerParameters_credCourse, cas_public);

    Smartcard softwareSmartcard = new SoftwareSmartcard();

    int minAttendance = 42;
    InitializeResult result =
        smartcardTool.initializeSmartcard(softwareSmartcard, newPin, deviceID, deviceUri,
            minAttendance);
    System.out.println("Result of initializeSmartcard : " + result);


    // add to mangager
    UserHelper.getInstance().cardStorage.addSmartcard(softwareSmartcard, newPin);

    //
    BigInteger pseudonym = softwareSmartcard.computeScopeExclusivePseudonym(newPin, scope);

    System.out.println("Smartcard has been initialized");
    System.out
        .println("PIN   PUK     MAC(as Hex)                       DEVICE                                 PSEUDONYM");
    String save =
        String.format(
            "%04d  %06d  %s  %s  %s",
            new Object[] {newPin, newPuk, PKIKeyTool.toHex(macKeyForBackup), deviceUri,
                pseudonym.toString()});
    System.out.println(save);

    // Store SoftwareSmartcard
    // TODO : SoftwareSmart must be made Serializable
    // PKIKeyTool.storeObjectInFile(softwareSmartcard, USER_STORAGE_FOLDER + "/",
    // "software_smartcard");


    // // SMART CARD STUFF
    // PseudonymWithMetadata gotThat_credentialManager =
    // UserHelper.getInstance().credentialManager.getPseudonym(scope);
    // System.out.println("gotThat_credentialManager : " + gotThat_credentialManager);
    //
    // // try to run issuance...
    // runIssuance();
    //
  }

  ObjectFactory of = new ObjectFactory();

  public void runIssuance() throws Exception {


    Map<String, Object> attributeValueMap = new HashMap<String, Object>();

    attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
    attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
    attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", 1234);
    attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Smartcard");
    attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "User");


    // issuer init
    IssuanceMessage server_im =
        IssuanceHelper.getInstance().initIssuance(CREDSPEC_UNIVERSITY, attributeValueMap);

    System.out.println(" - initial message - server : " + server_im);
    System.out.println(" - initial message - server : "
        + XmlUtils.toXml(this.of.createIssuanceMessage(server_im)));

    System.out.println(" - user 1st step!");
    // user 1st step
    IssuMsgOrCredDesc user_im =
        UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
    System.out.println(" - initial message - client - created ");
    System.out.println(" - initial message - client - created " + user_im.im);
    System.out.println(" - initial message - client - created " + user_im.cd);
    System.out.println(" - initial message - client : "
        + XmlUtils.toXml(this.of.createIssuanceMessage(user_im.im), false));

    int stepCount = 1;
    boolean lastmessage = false;
    while (!lastmessage) {

      System.out.println(" - contact server");
      IssuanceMessageAndBoolean server_im_step = IssuanceHelper.getInstance().issueStep(user_im.im);

      // send to server and receive new im
      server_im = server_im_step.getIssuanceMessage();
      System.out.println(" - got response");
      System.out.println(" - step message - server : " + stepCount + " : "
          + XmlUtils.toXml(this.of.createIssuanceMessage(server_im), false));

      // process in
      user_im = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
      System.out.println(" - step message - client :" + stepCount);

      lastmessage = (user_im.cd != null);
      if (!lastmessage) {
        System.out.println(" - initial message - step : " + stepCount + " : "
            + XmlUtils.toXml(this.of.createIssuanceMessage(user_im.im), false));
      }
    }
    System.out.println(" - done...");
    System.out.println(" - done : credentialDescription : "
        + XmlUtils.toXml(this.of.createCredentialDescription(user_im.cd), false));

  }

}

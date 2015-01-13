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

package eu.abc4trust.ri.servicehelper.user;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Unit test for simple App.
 */
public class UserHelperTest {
  
  private static final String USERNAME = "defaultUser";

  @Before
  public void alwaysResetEngine() throws Exception {
    System.out.println("test_alwaysResetEngine");
    UserHelper.resetInstance();
  }

  private final ObjectFactory of = new ObjectFactory();

  private void initHelper(CryptoEngine cryptoEngine, CryptoEngine userRunEngine, String user)
      throws Exception {
    System.out.println("UserService init ! " + cryptoEngine + " - " + userRunEngine);
    UserHelper.resetInstance();

    String fileStoragePrefix;
    if (new File("target").exists()) {
      fileStoragePrefix = "target/user_";
    } else {
      fileStoragePrefix = "service-helper/target/user_";
    }
    fileStoragePrefix += cryptoEngine.toString().toLowerCase() + "_";

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCard.xml"};

    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    // String systemParamsResource = null;
    List<IssuerParameters> issuerParamsList = null;
    List<InspectorPublicKey> inspectorPublicKeyList = null;
    List<RevocationAuthorityParameters> revAuthParamsList = null;
    String systemParamResource = fileStoragePrefix + "_" + UserHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    // FileSystem.storeObjectInFile(systemParameters, systemParamResource);

    UserHelper.initInstance(systemParameters, issuerParamsList, fileStoragePrefix, credSpecList,
        inspectorPublicKeyList, revAuthParamsList);

    Secret secret;
    if (userRunEngine == CryptoEngine.IDEMIX) {
      System.out.println("- client engine is IDEMIX - load secret !");
      InputStream is =
          FileSystem.getInputStream("/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml");
      System.out.println("IS : " + is);

      secret = (Secret) XmlUtils.getObjectFromXML(is, true);
    } else if (userRunEngine == CryptoEngine.UPROVE) {
      System.out.println("- client engine is UPROVE - load secret !");
      InputStream is =
          FileSystem.getInputStream("/eu/abc4trust/sampleXml/patras/uprove-secret.xml");
      System.out.println("IS : " + is);
      secret = (Secret) XmlUtils.getObjectFromXML(is, true);

    } else {
      throw new IllegalStateException("CryptoEngine not supported : " + cryptoEngine);
    }
    URI secretUid = secret.getSecretDescription().getSecretUID();
    System.out.println("System - adding smart card secret : "
        + secret.getSecretDescription().getSecretUID());

    try {
      @SuppressWarnings("unused")
      Secret exists_secret = UserHelper.getInstance().credentialManager.getSecret(USERNAME, secretUid);
      System.out.println("Secret Already Exists!! " + secretUid);
    } catch (SecretNotInStorageException e) {
      System.out.println("Secret Not In Storage!");

      UserHelper.getInstance().credentialManager.storeSecret(USERNAME, secret);
    }

    String soderhamnScope = "urn:soderhamn:registration";
    String patrasScope = "urn:patras:registration";
    String[] scopes = {patrasScope, soderhamnScope};
    if (userRunEngine == CryptoEngine.IDEMIX) {

      // / pseudonym values calculated from static secret.xml

      // pseudonym with scope "urn:patras:soderhamn" - for static secret...
      String soderhamnPseudonymValue_Idemix_BigIntegerString =
          "7182912214328715993001213338745037155907989549925821040422889908214204788325151860770186523958719728156650451613538167260524080170009468881580303865722570930081177852883024643099586509243093473269076414693362716790450673670374672110025461473081868058503473935185916622839938346159830940554988719939705317043234910874742373062937820781046500606824478392562799772097913829238611646808503142537123965625181848188301645827730173074615456213922957108066231400458378638530518799021597987886974102878921896139036544150438439561901250380909624759762995674162580981150284591936471177564381642630859171305543858439218391969187";
      BigInteger soderhamnPseudonymValue =
          new BigInteger(soderhamnPseudonymValue_Idemix_BigIntegerString);

      // pseudonym with scope "urn:patras:registration" - for static secret...
      String patrasPseudonymValue_Idemix_BigIntegerString =
          "13113619309688455399943141047039588255012224402685936272493453785722607281295670863960311415709688624896181140637153337931764122797063639228028292556539597983270104663481061345843768312132112143065003200113982068496676609329599900685254308588931392680059865648913882240137003267016796606864192446927431006063143596502930331212298127485942432631258503855952748174323704253071654542663409996358469311691467823181733982163671649264944640161635575269481101875411220966819942294763879176180443963538681672879852525484918494941678429730605927666250736885168466826481197438440303225695399549810605219740737926981960982885828";
      BigInteger patrasPseudonymValue =
          new BigInteger(patrasPseudonymValue_Idemix_BigIntegerString);

      BigInteger[] pseudonymValues = {patrasPseudonymValue, soderhamnPseudonymValue};
      for (int i = 0; i < scopes.length; i++) {
        String scope = scopes[i];
        @SuppressWarnings("unused")
        URI scopeUri = URI.create(scope);
        BigInteger pseudonymValue = pseudonymValues[i];

        URI pseudonymUID = URI.create(scope + ":pseudonymuid:42");
        try {
          @SuppressWarnings("unused")
          PseudonymWithMetadata pseudo =
              UserHelper.getInstance().credentialManager.getPseudonym(USERNAME, pseudonymUID);
          System.out.println(" - pseudo exists! " + scope);
          // System.out.println(" - pseudo exists! : " + scope + " : " +
          // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
        } catch (CredentialManagerException e) {

          PseudonymWithMetadata pwm =
              this.createPseudonym(secretUid, scope, pseudonymUID, pseudonymValue);

          UserHelper.getInstance().credentialManager.storePseudonym(USERNAME, pwm);

          System.out.println(" - " + scope + " : pseudo Created!");
          // System.out.println(" - " + scope + " : pseudo Created! : " +
          // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
        }
      }
    } else if (userRunEngine == CryptoEngine.UPROVE) {
      // @SuppressWarnings("unused")
      // SystemParameters systemParameters =
      // AbstractHelper.loadObjectFromResource(folder.getAbsolutePath() +
      // "/issuer_system_params_uprove");
      // UserHelper.getInstance().keyManager.storeSystemParameters(systemParameters);

      String uprovePseudonymResourse = "/eu/abc4trust/sampleXml/patras/uprove-pseudonym.xml";
      for (String scope : scopes) {
        System.out.println("Create Pseudonym for scope : " + scope);
        URI pseudonymUID = URI.create(scope); // + ":pseudonymuid:uprove:42");
        try {
          @SuppressWarnings("unused")
          PseudonymWithMetadata pseudo =
              UserHelper.getInstance().credentialManager.getPseudonym(USERNAME, pseudonymUID);
          System.out.println(" - pseudo exists!");
          // System.out.println(" - pseudo exists! : " +
          // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
        } catch (CredentialManagerException e) {

          PseudonymWithMetadata pwm =
              (PseudonymWithMetadata) XmlUtils.getObjectFromXML(this.getClass()
                  .getResourceAsStream(uprovePseudonymResourse), true);
          pwm.getPseudonym().setScope(scope);
          pwm.getPseudonym().setPseudonymUID(pseudonymUID);

          UserHelper.getInstance().credentialManager.storePseudonym(USERNAME, pwm);

          // System.out.println(" - " + scope + " : pseudo Created! : " +
          // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
          System.out.println(" - " + scope + " : pseudo Created!");
        }

      }

    }

    System.out.println("UserService init ! DONE");

  }

  private PseudonymWithMetadata createPseudonym(URI secretUid, String scope, URI pseudonymUID,
      BigInteger pseudonymValue) {
    // System.out.println("PSE BIGINT! : " + pseudonymValue);
    byte[] pv = pseudonymValue.toByteArray();
    Pseudonym pseudonym = this.of.createPseudonym();
    pseudonym.setSecretReference(secretUid);
    pseudonym.setExclusive(true);
    pseudonym.setPseudonymUID(pseudonymUID);
    pseudonym.setPseudonymValue(pv);
    pseudonym.setScope(scope);

    Metadata md = this.of.createMetadata();
    PseudonymMetadata pmd = this.of.createPseudonymMetadata();
    FriendlyDescription fd = new FriendlyDescription();
    fd.setLang("en");
    fd.setValue("Pregenerated pseudonym");
    pmd.getFriendlyPseudonymDescription().add(fd);
    pmd.setMetadata(md);
    PseudonymWithMetadata pwm = this.of.createPseudonymWithMetadata();
    pwm.setPseudonym(pseudonym);
    pwm.setPseudonymMetadata(pmd);

    CryptoParams cryptoEvidence = this.of.createCryptoParams();
    // URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");

    // StructureStore.getInstance().add(groupParameterId.toString(), groupParameters);
    PseudonymCryptoFacade pcf = new PseudonymCryptoFacade();
    pcf.setScopeExclusivePseudonym(URI.create(scope), secretUid, pv);
    pwm.setCryptoParams(pcf.getCryptoParams());
    return pwm;
  }

  @Test(expected = IllegalStateException.class)
  public void test_notInit() throws Exception {
    System.out.println("test_notInit!");
    UserHelper.getInstance();
  }



  @Test()
  public void test_init_idemix() throws Exception {
    this.initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "alice_idemix");
  }

  @Test()
  public void test_init_uprove() throws Exception {
    this.initHelper(CryptoEngine.UPROVE, CryptoEngine.UPROVE, "alice_uprove");
  }

  @Test()
  public void test_init_bridged() throws Exception {
    this.initHelper(CryptoEngine.UPROVE, CryptoEngine.IDEMIX, "alice_bridged");
  }

  // @Test()
  public void test_verifyWithJSonCallback() throws Exception {
    UserHelper helper = UserHelper.getInstance();

    PresentationPolicyAlternatives ppa =
        (PresentationPolicyAlternatives) XmlUtils
            .getObjectFromXML(
                UserHelperTest.class
                    .getResourceAsStream("/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml"),
                true);

    PresentationToken pt = helper.getEngine().createPresentationTokenFirstChoice(USERNAME, ppa);

    System.out.println("test_verifyWithJSonCallback : " + pt);

  }


  public void getPseudonymValue(PresentationToken pt, String scope, BigInteger expectedValue)
      throws Exception {

  }

  // @Test()
  public void test_getPseudonymValue_idemix() throws Exception {
    this.initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "alice_idemix");
    InputStream inputStream =
        UserHelperTest.class
            .getResourceAsStream("/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml");
    @SuppressWarnings("unused")
    PresentationPolicyAlternatives ppa =
        (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(inputStream, true);

  }

}

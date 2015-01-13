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

package eu.abc4trust.ri.service.it.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
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
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITUser {

  private static final String IDCARD_ISSUANCE_WITH_KEY = "idcardIssuanceWithKey";
  private static final String CREDITCARD_VISA = "CREDITCARD_VISA";
  private static final String CREDITCARD_AMEX = "CREDITCARD_AMEX";
  private static final String PASSPORT_CH = "PASSPORT_CH";
  private static final String STUDENT_CARD = "STUDENT_CARD";

  private static final String SODERHAMN_SCHOOL_IDEMIX = "SODERHAMN_SCHOOL_IDEMIX";
  private static final String SODERHAMN_SCHOOL_UPROVE = "SODERHAMN_SCHOOL_UPROVE";
  private static final String SODERHAMN_SUBJECT_IDEMIX = "SODERHAMN_SUBJECT_IDEMIX";
  private static final String SODERHAMN_SUBJECT_UPROVE = "SODERHAMN_SUBJECT_UPROVE";

  private static final String PATRAS_UNIVERSITY_IDEMIX = "PATRAS_UNIVERSITY_IDEMIX";
  private static final String PATRAS_UNIVERSITY_UPROVE = "PATRAS_UNIVERSITY_UPROVE";
  private static final String PATRAS_COURSE_IDEMIX = "PATRAS_COURSE_IDEMIX";
  private static final String PATRAS_COURSE_UPROVE = "PATRAS_COURSE_UPROVE";


  static ObjectFactory of = new ObjectFactory();

  // static String issuer_fileStoragePrefix = null;


  public ITUser() throws Exception {
    System.out.println("ITUser");
  }

  public void initIssuerAndVerifier() throws Exception {
    if (IssuanceHelper.isInit()) {
      return;
    }
    System.out.println("initIssuerAndVerifier...");

    System.out.println("setup IssuanceHelper");

    IssuanceHelper.resetInstance();

    File folder;
    String issuer_fileStoragePrefix;
    String verifier_fileStoragePrefix;
    if (new File("target").exists()) {
      issuer_fileStoragePrefix = "target/issuer_";
      verifier_fileStoragePrefix = "target/verifier_";
      folder = new File("target");
    } else {
      issuer_fileStoragePrefix = "integration-test-user/target/issuer_";
      verifier_fileStoragePrefix = "integration-test-user/target/verifier_";
      folder = new File("integration-test-user/target");
    }

    // SpecAndPolicy soderhamn_school_idemix = new SpecAndPolicy(SODERHAMN_SCHOOL_IDEMIX,
    // CryptoTechnology.IDEMIX, 0,
    // "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml");
    // SpecAndPolicy soderhamn_school_uprove = new SpecAndPolicy(SODERHAMN_SCHOOL_UPROVE,
    // CryptoTechnology.UPROVE, 0,
    // "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml");
    // SpecAndPolicy soderhamn_subject_idemix = new SpecAndPolicy(SODERHAMN_SUBJECT_IDEMIX,
    // CryptoTechnology.IDEMIX, 0,
    // "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSubject.xml");
    // SpecAndPolicy soderhamn_subject_uprove = new SpecAndPolicy(SODERHAMN_SUBJECT_UPROVE,
    // CryptoTechnology.UPROVE, 0,
    // "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSubject.xml");

    SpecAndPolicy patras_university_idemix =
        new SpecAndPolicy(PATRAS_UNIVERSITY_IDEMIX, CryptoTechnology.IDEMIX, 6, 0,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
    SpecAndPolicy patras_university_uprove =
        new SpecAndPolicy(PATRAS_UNIVERSITY_UPROVE, CryptoTechnology.UPROVE, 6, 10,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
    SpecAndPolicy patras_cource_idemix =
        new SpecAndPolicy(PATRAS_COURSE_IDEMIX, CryptoTechnology.IDEMIX, 2, 0,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");
    SpecAndPolicy patras_cource_uprove =
        new SpecAndPolicy(PATRAS_COURSE_UPROVE, CryptoTechnology.UPROVE, 2, 1,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");


    SpecAndPolicy[] specAndPolicyList =
        new SpecAndPolicy[] {
            // soderhamn_school_idemix, soderhamn_school_uprove, soderhamn_subject_idemix,
            // soderhamn_subject_uprove,
            patras_university_idemix, patras_university_uprove, patras_cource_idemix,
            patras_cource_uprove};
    IssuanceHelper.initInstance(1024, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
        specAndPolicyList, new ArrayList<RevocationAuthorityParameters>());


    System.out.println("IssuanceHelper - done!");

    System.out.println("Setup VerificationHelper");
    VerificationHelper.resetInstance();

    String[] presentationPolicyResources =
        {"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeFrench.xml",
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeEnglish.xml",
            "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml",
            "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml"};

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "issuer_params");
    List<InspectorPublicKey> inspectorPublicKeyList = null;
    List<RevocationAuthorityParameters> revAuthParamsList = null;

    String systemParamResource = issuer_fileStoragePrefix + UserHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamResource);

    VerificationHelper.initInstance(systemParams, issuerParamsList, credSpecList,
        inspectorPublicKeyList, revAuthParamsList, verifier_fileStoragePrefix,
        presentationPolicyResources);


  }

  final static String baseUrl = "http://localhost:9119/integration-test-user";

  public void initUser(CryptoTechnology cryptoTechnology, String user) throws Exception {
    Client client = Client.create();
    Builder initResource =
        client.resource(baseUrl + "/init/" + cryptoTechnology.toString() + "?user=" + user).accept(
            MediaType.TEXT_PLAIN);

    String response = initResource.get(String.class);
    System.out.println("INIT OK - pseudonymValue!" + response);

    BigInteger pseValue = new BigInteger(response);
    IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(pseValue);
  }

  // @After
  // public void resetIssuerEngine() throws Exception {
  // Client client = Client.create();
  // Builder initResource = client.resource(baseUrl + "/reset/").accept(MediaType.TEXT_PLAIN);
  //
  // String response = initResource.get(String.class);
  // System.out.println("Reset OK !" + response);
  //
  // IssuanceHelper.resetInstance();
  // UserHelper.resetInstance();
  // VerificationHelper.resetInstance();
  // }
  //
  // ***************************************************************************************

  // @Test
  public void test_Soderhamn_Emil_Idemix() throws Exception {
    this.soderhamn_Emil_IssueCredentials(CryptoTechnology.IDEMIX, "emil_idemix");
    this.soderhamn_Emil_PresentCredentials(CryptoTechnology.IDEMIX, "emil_idemix");
  }

  // @Test
  public void test_Soderhamn_Emil_UProve() throws Exception {
    this.soderhamn_Emil_IssueCredentials(CryptoTechnology.IDEMIX, "emil_uprove");
    this.soderhamn_Emil_PresentCredentials(CryptoTechnology.IDEMIX, "emil_uprove");
  }

  private void soderhamn_Emil_IssueCredentials(CryptoTechnology cryptTechnology, String user)
      throws Exception {
    System.out.println("START : soderhamn_Emil_IssueCredentials : " + cryptTechnology);
    this.initIssuerAndVerifier();
    this.initUser(cryptTechnology, user);

    // School credential
    System.out.println("Issue Soderhamn School Credential!");
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:firstname", "Emil");
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:lastname", "von Katthult Svensson");
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:civicRegistrationNumber", "42");
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:gender", "M");
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:schoolname", "L\u00f6nneberga");

    Calendar cal = Calendar.getInstance();
    cal.set(2000, 01, 10);
    attributeValueMap.put("urn:soderhamn:credspec:credSchool:birthdate", cal);

    // call issuer
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance("SODERHAMN_SCHOOL_" + cryptTechnology,
            attributeValueMap);
    this.finishIssuing(im_with_policy);


    // Subject credential
    System.out.println("Issue Soderhamn School Credential!");
    attributeValueMap = new HashMap<String, Object>();
    String subject = "French";
    // attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject", "French");
    attributeValueMap.put("urn:soderhamn:credspec:credSubject:maths", "maths".equals(subject));
    attributeValueMap.put("urn:soderhamn:credspec:credSubject:physics", "physics".equals(subject));
    attributeValueMap.put("urn:soderhamn:credspec:credSubject:English", "English".equals(subject));
    attributeValueMap.put("urn:soderhamn:credspec:credSubject:French", "French".equals(subject));
    attributeValueMap
        .put("urn:soderhamn:credspec:credSubject:subject1", "subject1".equals(subject));
    attributeValueMap
        .put("urn:soderhamn:credspec:credSubject:subject2", "subject2".equals(subject));
    attributeValueMap
        .put("urn:soderhamn:credspec:credSubject:subject3", "subject3".equals(subject));
    attributeValueMap
        .put("urn:soderhamn:credspec:credSubject:subject4", "subject4".equals(subject));
    attributeValueMap
        .put("urn:soderhamn:credspec:credSubject:subject5", "subject5".equals(subject));

    // call issuer
    im_with_policy =
        IssuanceHelper.getInstance().initIssuance("SODERHAMN_SUBJECT_" + cryptTechnology,
            attributeValueMap);
    this.finishIssuing(im_with_policy);

    System.out.println("DONE : soderhamn_Emil_IssueCredentials..");
  }

  private void soderhamn_Emil_PresentCredentials(CryptoTechnology cryptTechnology, String user)
      throws Exception {
    this.initIssuerAndVerifier();
    this.initUser(cryptTechnology, user);

    // School credential
    System.out.println("Present Soderhamn Smartcard Pseudonym!");
    this.runVerification("presentationPolicySoderhamnSchool.xml", true);

    // Subject credential
    System.out.println("Present Soderhamn Subject Credential - pseudonym being established!");
    this.runVerification("presentationPolicyRASubjectMustBeFrench.xml", true);

    // Subject pseudonym
    System.out.println("Present Soderhamn Subject Credential - pseudonym is used!");
    this.runVerification("presentationPolicyRASubjectMustBeFrench.xml", true);

    // Subject credential - english not satisfied
    System.out.println("Present Soderhamn Subject Credential - Not Satisfied!!");
    this.runVerification("presentationPolicyRASubjectMustBeEnglish.xml", false);

  }

  // ***************************************************************************************

  @Test
  public void test_Patras_Student_Idemix() throws Exception {
    this.initIssuerAndVerifier();
    this.initUser(CryptoTechnology.IDEMIX, "student_idemix");
    this.patras_Student_IssueCredentials(CryptoTechnology.IDEMIX, "student_idemix", 42);
    this.patras_Student_PresentCredentials(CryptoTechnology.IDEMIX, "student_idemix", 42);
  }

  // @Test
  public void test_Patras_Student_UProve() throws Exception {
    this.initIssuerAndVerifier();
    this.initUser(CryptoTechnology.UPROVE, "student_idemix");
    this.patras_Student_IssueCredentials(CryptoTechnology.UPROVE, "student_uprove", 42);
    this.patras_Student_PresentCredentials(CryptoTechnology.UPROVE, "student_uprove", 42);
  }

  private void patras_Student_IssueCredentials(CryptoTechnology cryptoTechnology, String user,
      int matriculationnumber) throws Exception {


    // University credential
    System.out.println("Issue Patras University Credential!");
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
    attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
    attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", matriculationnumber);

    if (matriculationnumber == 42) {
      attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Stewart");
      attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Dent");
    } else if (matriculationnumber == 1235332) {
      attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "John");
      attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Doe");
    } else if (matriculationnumber == 666) {
      attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Eve");
      attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "Cheater");
    } else {
      throw new IllegalStateException(
          "Matriculationnumber issuance only defined for john(1235332), stewart (42) + Eve (666)!");
    }

    // call issuer
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance("PATRAS_UNIVERSITY_" + cryptoTechnology,
            attributeValueMap);
    this.finishIssuing(im_with_policy);


    // Course credential
    System.out.println("Issue Patras Course Credential!");
    attributeValueMap.clear();
    attributeValueMap.put("urn:patras:credspec:credCourse:courseid", "The-very-cool-course");
    // attributeValueMap.put("urn:patras:credspec:credCourse:matriculationnr", matriculationnumber);

    // call issuer
    im_with_policy =
        IssuanceHelper.getInstance().initIssuance("PATRAS_COURSE_" + cryptoTechnology,
            attributeValueMap);
    this.finishIssuing(im_with_policy);

  }


  private void patras_Student_PresentCredentials(CryptoTechnology cryptoTechnology, String user,
      int matriculationnumber) throws Exception {

    // School credential
    System.out.println("Present Patras Smartcard Pseudonym!");
    this.runVerification("presentationPolicyPatrasUniversityLogin.xml", true);

    // Course credential
    System.out.println("Present Patras Course Credential - pseudonym is established!");
    this.runVerification("presentationPolicyPatrasCourseEvaluation.xml", true);

    // Course credential
    System.out.println("Present Patras Course Credential - pseudonym is used!");
    this.runVerification("presentationPolicyPatrasCourseEvaluation.xml", true);
  }

  // ***************************************************************************************

  // @Test
  public void testIssuance_Alice_SimpleIdentitycard() throws Exception {
    // testListCredentials();
    this.initUser(CryptoTechnology.IDEMIX, "alice");

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("Firstname", "Alice");
    attributeValueMap.put("Lastname", "Nexdoor");
    Calendar bd = Calendar.getInstance();
    bd.set(1970, 1, 1, 0, 0, 0);
    attributeValueMap.put("Birthday", bd);

    // call issuer
    System.out.println(" - invoke ABCE - using IssuanceHelper!");
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance(IDCARD_ISSUANCE_WITH_KEY, attributeValueMap);
    this.finishIssuing(im_with_policy);

  }

  // @Test
  public void testVerification_simpleIdcard() throws Exception {

    System.out.println("---- testVerification_simpleIdcard ----");
    this.runVerification("presentationPolicySimpleIdentitycard.xml", true);
  }


  // NOTE ONLY ONE TEST CAN RUN AGAINST USER - for now - needs to changes 'storage'
  // @Test
  public void testIssuance_Stewart_IDCardAndCreditcards() throws Exception {

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("Firstname", "Stewart");
    attributeValueMap.put("Lastname", "Dent");
    Calendar bd = Calendar.getInstance();
    bd.set(1995, 1, 1, 0, 0, 0);
    attributeValueMap.put("Birthday", bd);

    // call issuer
    System.out.println(" - invoke ABCE - using IssuanceHelper!");
    IssuanceMessage idcard_im =
        IssuanceHelper.getInstance().initIssuance(IDCARD_ISSUANCE_WITH_KEY, attributeValueMap);
    this.finishIssuing(idcard_im);

    this.issuePassport("stewart");
    this.issueStudentCard();

  }

  // NOTE ONLY ONE TEST CAN RUN AGAINST USER - for now - needs to changes 'storage'
  // @Test
  public void testIssuance_JohnDow_Creditcards() throws Exception {
    this.testListCredentials();

    this.issueCreditCard("johndow", CREDITCARD_VISA);

    this.issueCreditCard("johndow", CREDITCARD_AMEX);

  }


  private void issueCreditCard(String user, String card) throws Exception {
    System.out.println("issueCreditCard " + card);
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    int cardNumber = 42;
    if (CREDITCARD_VISA.equals(card)) {
      attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
          "http://visa.com/creditcard/revocation/parameters");
    } else {
      attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
          "http://amex.com/amexcard/revocation/parameters");
      cardNumber += 1000;
    }
    Calendar cal = Calendar.getInstance();
    if ("alice".equals(user)) {
      attributeValueMap.put("Name", "Alice");
      attributeValueMap.put("LastName", "Nextdoor");
      cal.set(2014, 1, 1, 0, 0, 0);
      attributeValueMap.put("CardType", "Gold");
    } else if ("johndow".equals(user)) {
      cardNumber += 20000;
      attributeValueMap.put("Name", "John");
      attributeValueMap.put("LastName", "Dow");
      cal.set(2016, 1, 1, 0, 0, 0);
      attributeValueMap.put("CardType", "Black");
    }

    attributeValueMap.put("CardNumber", BigInteger.valueOf(cardNumber));
    attributeValueMap.put("ExpirationDate", cal.getTime());
    attributeValueMap.put("SecurityCode", 1);
    attributeValueMap.put("Status", "status");

    System.out.println(" - invoke ABCE - using IssuanceHelper!");
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance(card, attributeValueMap);
    this.finishIssuing(im_with_policy);

  }

  private void issuePassport(String user) throws Exception {
    System.out.println("issuePassport " + user);
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    if ("stewart".equals(user)) {
      attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
          "http://admin.ch/passport/revocation/parameters");
      attributeValueMap.put("Name", "Stewart");
      attributeValueMap.put("LastName", "Dent");
      attributeValueMap.put("PassportNumber", 1);
      Calendar cal = Calendar.getInstance();
      cal.set(2012, 1, 1, 0, 0, 0);
      attributeValueMap.put("Issued", cal.getTime());
      cal.set(2015, 1, 1, 0, 0, 0);
      attributeValueMap.put("Expires", cal.getTime());
      attributeValueMap.put("IssuedBy", "service_issuer_integration_test");

    } else if ("alice".equals(user)) {
      attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
          "http://admin.ch/passport/revocation/parameters");
      attributeValueMap.put("Name", "Alice");
      attributeValueMap.put("LastName", "Nextdoor");
      attributeValueMap.put("PassportNumber", 42);
      Calendar cal = Calendar.getInstance();
      cal.set(2011, 1, 1, 0, 0, 0);
      attributeValueMap.put("Issued", cal.getTime());
      cal.set(2014, 1, 1, 0, 0, 0);
      attributeValueMap.put("Expires", cal.getTime());
      attributeValueMap.put("IssuedBy", "service_issuer_integration_test");
    } else {
      throw new IllegalStateException("Passport - user not defined in testcase : " + user);
    }

    System.out.println(" - invoke ABCE - using IssuanceHelper!");
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance(PASSPORT_CH, attributeValueMap);
    this.finishIssuing(im_with_policy);

  }

  private void issueStudentCard() throws Exception {
    System.out.println("issueStudentCard ");
    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    /*
     * <abc:AttributeDescription Type="Name" DataType="xs:string"
     * Encoding="urn:abc4trust:1.0:encoding:string:sha-256"/> <abc:AttributeDescription
     * Type="LastName" DataType="xs:string" Encoding="urn:abc4trust:1.0:encoding:string:sha-256"/>
     * <abc:AttributeDescription Type="StudentNumber" DataType="xs:integer"
     * Encoding="urn:abc4trust:1.0:encoding:integer:signed"/> <abc:AttributeDescription
     * Type="Issued" DataType="xs:date" Encoding="urn:abc4trust:1.0:encoding:date:unix:signed"/>
     * <abc:AttributeDescription Type="Expires" DataType="xs:date"
     * Encoding="urn:abc4trust:1.0:encoding:date:unix:signed"/> <abc:AttributeDescription
     * Type="IssuedBy" DataType="xs:string" Encoding="urn:abc4trust:1.0:encoding:string:sha-256"/>
     */
    int studentNumber = 42;
    attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
        "http://visa.com/creditcard/revocation/parameters");
    attributeValueMap.put("Name", "Stewart");
    attributeValueMap.put("LastName", "Dent");
    attributeValueMap.put("StudentNumber", BigInteger.valueOf(studentNumber));
    Calendar calIss = Calendar.getInstance();
    calIss.set(2012, 1, 1, 0, 0, 0);
    Calendar calExpr = Calendar.getInstance();
    calExpr.set(2014, 1, 1, 0, 0, 0);
    attributeValueMap.put("Issued", calIss.getTime());
    attributeValueMap.put("Expires", calExpr.getTime());
    attributeValueMap.put("IssuedBy", "service_issuer_integration_test");

    System.out.println(" - invoke ABCE - using IssuanceHelper!");
    IssuanceMessage im_with_policy =
        IssuanceHelper.getInstance().initIssuance(STUDENT_CARD, attributeValueMap);
    this.finishIssuing(im_with_policy);

  }

  private void finishIssuing(IssuanceMessage service_im) throws Exception {
    // simulates plugin
    Client client = Client.create();


    Builder issuanceProtocolStep =
        client.resource(ITUser.baseUrl + "/user/issuanceProtocolStep")
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

    while (true) {
      IssuanceMessage resp = null;
      // invoke user
      try {
        resp = ((JAXBElement<IssuanceMessage>)(Object)
            issuanceProtocolStep.post(IssuanceMessage.class, of.createIssuanceMessage(service_im))).getValue();
        System.out.println(" - response : " + resp);
      } catch (UniformInterfaceException e) {
        if (e.getResponse().getStatus() == 204) {
          System.out.println("Status 204 from user service !!");
          System.out.println(" - done...");
          return;
        } else {
          System.err.println(" - UniformInterfaceException : " + e.getResponse());
          System.err.println(" - UniformInterfaceException : " + e.getResponse().getStatus());
          throw new Exception("Unexpected responce from UserService : "
              + e.getResponse().getStatus());
        }
      }
      // invoke issuer!
      System.out.println(" - invoke ABCE - next step!");

      IssuanceMessageAndBoolean im_and_boolean = IssuanceHelper.getInstance().issueStep(resp);

      service_im = im_and_boolean.getIssuanceMessage();
    }
  }

  private void runVerification(String policy, boolean satisfiesPresentationPolicy) throws Exception {

    System.out.println("---- runVerification : " + policy);

    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    // get presentation policy
    PresentationPolicyAlternatives presentationPolicyAlternatives = 
        VerificationHelper.getInstance().createPresentationPolicy(policy, nonce, null, null);
    //
    Client client = Client.create();

    Builder verificationStartResource =
        client.resource(ITUser.baseUrl + "/user/createPresentationToken")
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

    // post to UserService
    PresentationToken pt_object;
    try {
      pt_object = ((JAXBElement<PresentationToken>) (Object)
          verificationStartResource.post(PresentationToken.class,
              of.createPresentationPolicyAlternatives(presentationPolicyAlternatives))).getValue();
    } catch (UniformInterfaceException e) {
      if (e.getResponse().getStatus() == 406) {
        if (satisfiesPresentationPolicy) {
          Assert.assertTrue("Policy : " + policy + " - could not be satisfied - NOT EXPECTED",
              false);
        } else {
          System.out.println("Policy : " + policy + " - could not be satisfied - AS EXPECTED ");
          Assert
              .assertTrue("Policy : " + policy + " - could not be satisfied - AS EXPECTED ", true);
        }
        return;
      } else {
        throw e;
      }
    }

    // verify response
    System.out.println(" - response from UserService : " + pt_object);
    try {
      System.out.println(" - response from UserService : "
          + XmlUtils.toXml(of.createPresentationToken(pt_object)));
    } catch (Exception e) {
      System.err.println(" - could not validate PresentationToken XML!");
      e.printStackTrace();
    }
    PresentationToken presentationToken;
    if (pt_object instanceof PresentationToken) {
      presentationToken = pt_object;
      // VerificationHelper.getInstance().verifyToken(policy, nonce, null, presentationToken);
      VerificationHelper.getInstance().verifyToken(presentationPolicyAlternatives,
          presentationToken);
      System.out.println("OK From Verification Helper !");
    } else {
      System.out.println("Wrong Response from UserService");
      throw new Exception("Wrong Response from UserService");
    }
    System.out.println(" - done...");
  }

  // @Test
  public void testUpdateNonRevocationEvidence() throws Exception {

    System.out.println("---- testUpdateNonRevocationEvidence ----");
    Client client = Client.create();

    Builder updateNonRevocationEvidence =
        client.resource(ITUser.baseUrl + "/user/updateNonRevocationEvidence")
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

    try {
      updateNonRevocationEvidence.post();
      System.out.println("- OK");
    } catch (Exception e) {
      System.err.println("- FAILED");
      e.printStackTrace();
    }
  }


  static URI firstCredential = null;

  // @Test
  public void testListCredentials() throws Exception {

    System.out.println("---- testListCredentials ----");
    Client client = Client.create();

    Builder listCredentials =
        client.resource(ITUser.baseUrl + "/user/listCredentials").type(MediaType.APPLICATION_XML)
            .accept(MediaType.TEXT_PLAIN);

    try {
      String resp = listCredentials.post(String.class);
      System.out.println("Credentials in UserEngine : \n" + resp);
      BufferedReader reader = new BufferedReader(new StringReader(resp));
      List<URI> list = new ArrayList<URI>();
      String line = null;
      while ((line = reader.readLine()) != null) {
        URI credential = new URI(line);
        this.printCredentialDescription(credential);

        if (firstCredential == null) {
          firstCredential = credential;
        }
        list.add(credential);
      }
      System.out.println("- OK " + list);
    } catch (Exception e) {
      System.err.println("- FAILED");
      e.printStackTrace();
    }
    System.out.println("- First credential used in other tests ! " + firstCredential);
  }

  // @Test
  public void testGetCredentialDescription() throws Exception {
    URI uri;
    if (firstCredential != null) {
      uri = firstCredential;
    } else {
      uri = new URI("http://asdf.gh/jkl");
    }
    this.printCredentialDescription(uri);
  }

  private void printCredentialDescription(URI credentialUID) throws Exception {

    System.out.println("---- testGetCredentialDescription ---- credential : " + firstCredential);
    Client client = Client.create();


    Builder getCredentialDescription =
        client.resource(ITUser.baseUrl + "/user/getCredentialDescription")
            .type(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_XML);

    CredentialDescription resp =
        getCredentialDescription.post(CredentialDescription.class, credentialUID.toString());
    System.out.println("- OK " + resp);
    System.out.println("- OK " + XmlUtils.toXml(of.createCredentialDescription(resp), true));
  }

  // @Test
  public void testDeleteCredentialDescription() throws Exception {

    System.out.println("---- testDeleteCredential ----");
    Client client = Client.create();


    Builder deleteCredentialDescription =
        client.resource(ITUser.baseUrl + "/user/deleteCredential").type(MediaType.TEXT_PLAIN)
            .accept(MediaType.TEXT_XML);

    URI uri;
    if (firstCredential != null) {
      uri = firstCredential;
    } else {
      uri = new URI("http://asdf.gh/jkl");
    }

    deleteCredentialDescription.post(uri.toString());
    System.out.println("- OK");
  }

  protected void copySystemParameters() throws IOException {
    this.copySystemParameters("issuer_system_params");
    this.copySystemParameters("issuer_system_params_bridged_human_readable_only_for_reference.xml");
  }

  private void copySystemParameters(String filename) throws IOException {
    File file = null;
    file =
        new File("src" + File.separatorChar + "test" + File.separatorChar + "resources"
            + File.separatorChar + filename);

    if (!file.exists()) {
      throw new FileNotFoundException("File does not exist on path: \"" + filename + "\"");
    }

    // new File().mkdirs();

    FileInputStream fis = new FileInputStream(file);
    FileOutputStream fos = new FileOutputStream(new File("target" + File.separatorChar + filename));

    byte[] bytes = new byte[1];
    while (fis.read(bytes) != -1) {
      fos.write(bytes);
    }

    fis.close();
    fos.close();
  }
}

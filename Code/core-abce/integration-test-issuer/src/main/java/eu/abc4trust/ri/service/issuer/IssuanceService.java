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

package eu.abc4trust.ri.service.issuer;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.util.XmlUtils;

@Path("/issue")
public class IssuanceService {

  public static final String IDCARD_IDEMIX = "IDCARD_IDEMIX";
  public static final String IDCARD_UPROVE = "IDCARD_UPROVE";
//    private static final String CREDITCARD_VISA = "creditcardVisa";
//    private static final String CREDITCARD_AMEX = "creditcardAmex";
//    private static final String PASSPORT_CH = "passportCH";
//    private static final String STUDENTCARD = "studentcard";

  public static final String UNIVERSITY_IDEMIX = "UNIVERSITY_IDEMIX";
  public static final String UNIVERSITY_UPROVE = "UNIVERSITY_UPROVE";
  public static final String COURCE_IDEMIX = "COURSE_IDEMIX";
  public static final String COURCE_UPROVE = "COURSE_UPROVE";

  public static final String SODERHAMN_SCHOOL_IDEMIX = "SCHOOL_IDEMIX";
  public static final String SODERHAMN_SCHOOL_UPROVE = "SCHOOL_UPROVE";
  public static final String SODERHAMN_SUBJECT_IDEMIX = "SUBJECT_IDEMIX";
  public static final String SODERHAMN_SUBJECT_UPROVE = "SUBJECT_UPROVE";

    ObjectFactory of = new ObjectFactory();

    public IssuanceService() {
        System.out.println("IssuanceService ");
        initIssuanceHelper();
    }

    private void initIssuanceHelper() {
        if(IssuanceHelper.isInit()) {
          System.out.println("IssuanceHelper - already - init!");
          return;
        }
        try {
            IssuanceHelper.resetInstance();
            System.out.println("IssuanceHelper - try to - init!");

            String fileStoragePrefix; // = "issuer_";
            String folderName;
            if (new File("target").exists()) {
                fileStoragePrefix = "target/issuer_";
                folderName = "target";
            } else {
                fileStoragePrefix = "integration-test-issuer/target/issuer_";
                folderName = "integration-test-issuer/target";
            }

            SpecAndPolicy idcard_idemix =
                    new SpecAndPolicy(IDCARD_IDEMIX,
                      CryptoTechnology.IDEMIX,
                      42,
                      0,
                      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
                        "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");

//            SpecAndPolicy idcard_uprove =
//                new SpecAndPolicy(IDCARD_UPROVE,
//                  CryptoTechnology.UPROVE,
//                  42,
//                  10,
//                  "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
//                    "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");
//
//            SpecAndPolicy universityIdemix =
//                    new SpecAndPolicy(UNIVERSITY_IDEMIX,
//                      CryptoTechnology.IDEMIX,
//                      6,
//                      0,
//                            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
//                            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
//            SpecAndPolicy universityUProve =
//                new SpecAndPolicy(UNIVERSITY_UPROVE,
//                  CryptoTechnology.UPROVE,
//                  6,
//                  10,
//                        "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
//                        "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
//            SpecAndPolicy courceIdemix =
//                    new SpecAndPolicy(COURCE_IDEMIX,
//                      CryptoTechnology.IDEMIX,
//                      2,
//                      0,
//                            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
//                            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");
//            SpecAndPolicy courceUProve =
//                new SpecAndPolicy(COURCE_UPROVE,
//                  CryptoTechnology.UPROVE,
//                  2,
//                  1,
//                        "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
//                        "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");


            String systemAndIssuerParamsPrefix = fileStoragePrefix;

            // Create a list to allow for rev auth parameters to be passed
            SpecAndPolicy[] specsAndPolicies = {idcard_idemix}; // , idcard_uprove, universityIdemix, universityUProve, courceIdemix, courceUProve}; // , soderhamnSchoolIdemix, soderhamnSchoolUProve, soderhamnSubjectIdemix, soderhamnSubjectUProve};

            List<RevocationAuthorityParameters> revAuthParams = FileSystem.findAndLoadXmlResourcesInDir(folderName, "revocation_authority");

            IssuanceHelper.initInstance(1024, systemAndIssuerParamsPrefix, fileStoragePrefix, specsAndPolicies, revAuthParams);

            System.out.println("IssuanceHelper - done!");
        } catch (Exception e) {
            System.out.println("Create Domain FAILED " + e);
            e.printStackTrace();
        }
    }

    @GET()
    @Path("/init")
    @Produces(MediaType.TEXT_PLAIN)
    public String init() {
        System.out.println("issuance service / IssuanceHelper...");
        return "OK";
    }

    @GET()
    @Path("/register")
    @Produces(MediaType.TEXT_PLAIN)
    public String register(@QueryParam("pseudonym") String pseudonym) throws Exception {
        System.out.println("issuance service - register pseudonym : " + pseudonym);
        BigInteger val = new BigInteger(pseudonym);
        IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(val);
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


    @GET()
    @Path("/start/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> start(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("variant") String variant, @QueryParam("user") String user) throws Exception {

        System.out.println("IssuanceService - start - credentialRequest : " + credentialRequest);
        System.out.println("- variant : " + variant);
        System.out.println("- user : " + user);

        
        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        String credspecAndPolicyKey;

        // TODO - add more cases
        if (credentialRequest.startsWith("IDCARD")) {

            if ("alice".equals(user)) {
                attributeValueMap.put("FirstName", "Alice");
                attributeValueMap.put("LastName", "Nexdoor");
                Calendar bd = Calendar.getInstance();
                bd.set(1970, 1, 1, 0, 0, 0);
                attributeValueMap.put("Birthday", bd);
            } else if ("stewart".equals(user)) {
                attributeValueMap.put("FirstName", "Stewart");
                attributeValueMap.put("LastName", "Dent");
                Calendar bd = Calendar.getInstance();
                bd.set(1995, 1, 1, 0, 0, 0);
                attributeValueMap.put("Birthday", bd);
            } else {
                throw new IllegalStateException("IDCard issuance only defined for alice and stewart!");
            }

            // select witch credspec + policy to use in helper
            credspecAndPolicyKey = credentialRequest.toUpperCase();
//        } else if ("passport".equals(credentialRequest)) {
//            if (!"ch".equals(variant)) {
//                throw new IllegalStateException("Passport - only 'ch' passports supported..");
//            }
//
//            if ("stewart".equals(user)) {
//                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
//                        "http://admin.ch/passport/revocation/parameters");
//                attributeValueMap.put("Name", "Stewart");
//                attributeValueMap.put("LastName", "Dent");
//                attributeValueMap.put("PassportNumber", 1);
//                Calendar cal = Calendar.getInstance();
//                cal.set(2012, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Issued", cal.getTime());
//                cal.set(2015, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Expires", cal.getTime());
//                attributeValueMap.put("IssuedBy", "service_issuer_integration_test");
//
//            } else if ("alice".equals(user)) {
//                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
//                        "http://admin.ch/passport/revocation/parameters");
//                attributeValueMap.put("Name", "Alice");
//                attributeValueMap.put("LastName", "Nextdoor");
//                attributeValueMap.put("PassportNumber", 42);
//                Calendar cal = Calendar.getInstance();
//                cal.set(2011, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Issued", cal.getTime());
//                cal.set(2014, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Expires", cal.getTime());
//                attributeValueMap.put("IssuedBy", "service_issuer_integration_test");
//            } else {
//                throw new IllegalStateException("Passport - user not defined in testcase : " + user);
//            }
//
//            // select witch credspec + policy to use in helper
//            credspecAndPolicyKey = PASSPORT_CH;
//        } else if ("creditcard".equals(credentialRequest)) {
//            if ("visa".equals(variant)) {
//                credspecAndPolicyKey = CREDITCARD_VISA;
//                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
//                        "http://visa.com/creditcard/revocation/parameters");
//            } else if ("amex".equals(variant)) {
//                credspecAndPolicyKey = CREDITCARD_AMEX;
//                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
//                        "http://amex.com/amexcard/revocation/parameters");
//            } else {
//                throw new IllegalStateException("Creditcard - unknow variant : " + variant);
//            }
//
//            if ("stewart".equals(user)) {
//                attributeValueMap.put("CardType", "Normal");
//                attributeValueMap.put("Name", "Stewart");
//                attributeValueMap.put("LastName", "Dent");
//                attributeValueMap.put("CardNumber", 1);
//                Calendar cal = Calendar.getInstance();
//                cal.set(2015, 1, 1, 0, 0, 0);
//                attributeValueMap.put("ExpirationDate", cal.getTime());
//                attributeValueMap.put("SecurityCode", 1);
//                attributeValueMap.put("Status", "status");
//
//            } else if ("alice".equals(user)) {
//                attributeValueMap.put("CardType", "Gold");
//                attributeValueMap.put("Name", "Alice");
//                attributeValueMap.put("LastName", "Nextdoor");
//                attributeValueMap.put("CardNumber", 42);
//                Calendar cal = Calendar.getInstance();
//                cal.set(2014, 1, 1, 0, 0, 0);
//                attributeValueMap.put("ExpirationDate", cal.getTime());
//                attributeValueMap.put("SecurityCode", 42);
//                attributeValueMap.put("Status", "status");
//            } else {
//                throw new IllegalStateException("Creditcard - user not defined in testcase : " + user);
//            }
//        } else if ("studentcard".equals(credentialRequest)) {
//
//            if ("stewart".equals(user)) {
//                /*
//                 * <abc:AttributeDescription Type="http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"
//                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="Name"
//                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="LastName"
//                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="StudentNumber"
//                 * DataType="xs:integer" Encoding="UTF-8"/> <abc:AttributeDescription Type="Issued"
//                 * DataType="xs:dateTime" Encoding="UTF-8"/> <abc:AttributeDescription Type="Expires"
//                 * DataType="xs:dateTime" Encoding="UTF-8"/> <abc:AttributeDescription Type="IssuedBy"
//                 * DataType="xs:string" Encoding="UTF-8"/>
//                 */
//                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
//                        "http://www.ethz.ch/studentid/revocation/parameters");
//                attributeValueMap.put("Name", "Stewart");
//                attributeValueMap.put("LastName", "Dent");
//                attributeValueMap.put("StudentNumber", 1);
//                Calendar cal = Calendar.getInstance();
//                cal.set(2012, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Issued", cal.getTime());
//                cal.set(2015, 1, 1, 0, 0, 0);
//                attributeValueMap.put("Expires", cal.getTime());
//                attributeValueMap.put("IssuedBy", "IssuedBy");
//
//            } else {
//                throw new IllegalStateException("Studentcard - user not defined in testcase : " + user);
//            }
//            credspecAndPolicyKey = STUDENTCARD;
        } else {
            throw new IllegalStateException(
                    "Unknown credential. For now only 'idcard', 'passport', 'creditcard' and 'studentcard' are supported.."
                            + credentialRequest);
        }

        IssuanceMessage im_with_policy;
        try {
            System.out.println(" - invoke ABCE - using IssuanceHelper! " + credspecAndPolicyKey + " : "
                    + attributeValueMap);

            im_with_policy =
                    IssuanceHelper.getInstance().initIssuance(credspecAndPolicyKey, attributeValueMap);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" - failed to initIssuanceProtocol - using IssuanceHelper!");
            throw new IllegalStateException("Failed to initIssuanceProtocol - using IssuanceHelper!");
        }

        System.out.println(" - return inital message - for context : " + im_with_policy.getContext());
        System.out.println(" - return inital message : " + XmlUtils.toXml(this.of.createIssuanceMessage(im_with_policy)));
        return this.of.createIssuanceMessage(im_with_policy);
    }


    @POST()
    @Path("/step")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> issueStep(final JAXBElement<IssuanceMessage> issuanceMessage_jb) throws Exception {

        IssuanceMessage issuanceMessage = issuanceMessage_jb.getValue();
        System.out.println("IssuanceService - step - request - context : " + issuanceMessage.getContext());
        System.out.println(" - step - request from client  : " + XmlUtils.toXml(this.of.createIssuanceMessage(issuanceMessage)));

        IssuanceMessageAndBoolean response;
        try {
            response = IssuanceHelper.getInstance().issueStep(issuanceMessage);
        } catch (Exception e) {
            System.err
            .println("- got Exception from IssuaceHelper/ABCE Engine - processing IssuanceMessage from user...");
            e.printStackTrace();
            throw new IllegalStateException("Failed to proces IssuanceMessage from server");
        }

        if (response.isLastMessage()) {
            System.out.println(" - last message for context : "
                    + response.getIssuanceMessage().getContext());
            try {
                System.out.println(" - IssuanceMessage : "
                        + XmlUtils.toXml(this.of.createIssuanceMessage(response
                                .getIssuanceMessage())));
            } catch (Exception e) {
                System.out.println(" - IssuanceMessage - LOG FAILED!: ");
                e.printStackTrace();
            }
        } else {
            System.out.println(" - more steps context : "
                    + response.getIssuanceMessage().getContext());
        }
        System.out.println(" - step - response to client  : "
                + XmlUtils.toXml(this.of.createIssuanceMessage(response
                        .getIssuanceMessage())));

        return this.of.createIssuanceMessage(response.getIssuanceMessage());
    }


    // PATRAS DEMO ISSUANCE

    @GET()
    @Path("/startPatras/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startUniversity(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("matriculationnumber") int matriculationnumber) throws Exception {

        System.out.println("IssuanceService - start - issue University Credential: "
                + credentialRequest);
        System.out.println("- Matriculationnumber : " + matriculationnumber);

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();

        if (credentialRequest.startsWith("UNIVERSITY")) {
  
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
        } else if (credentialRequest.startsWith("COURSE")) {
  
          if ((matriculationnumber == 42) || (matriculationnumber == 1235332) || (matriculationnumber == 666)) {
              // ok - matriculationnumber accepted...
            attributeValueMap.put("urn:patras:credspec:credCourse:courseid", "The-very-cool-course");
          } else {
              throw new IllegalStateException(
                      "Matriculationnumber issuance only defined for john(1235332) and stewart (42)!");
          }
        } else {
          throw new IllegalStateException(
            "Unknown credentialRequest. For now only 'UNIVERSITY_<technology> and COURSE_<technology> are supported.."
                    + credentialRequest);
        }
        // attributeValueMap.put("urn:patras:credspec:credCourse:matriculationnr", matriculationnumber);

        return this.finishStartIssuance(credentialRequest, attributeValueMap);
    }


    // SODERHAMN DEMO ISSUANCE

    @GET()
    @Path("/startSoderhamn/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startSchool(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("pupil") String pupil) throws Exception {

        System.out.println("IssuanceService - start - issue School Credential: "
                + credentialRequest);
        System.out.println("- Pupil : " + pupil);

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        if (credentialRequest.startsWith("SCHOOL")) {

            if("Emil".equalsIgnoreCase(pupil)) {
    
                // School credential
                System.out.println("Issue Soderhamn School Credential!");
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:firstname", "Emil");
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:lastname", "von Katthult Svensson");
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:civicRegistrationNumber", "42");
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:gender", "M");
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:schoolname", "L\u00f6nneberga");
    
                Calendar cal = Calendar.getInstance();
                cal.set(2000, 01, 10);
                SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
                String dateValue = xmlDateFormat.format(cal.getTime());
                attributeValueMap.put("urn:soderhamn:credspec:credSchool:birthdate", dateValue);
    
            } else {
                throw new IllegalStateException(
                        "Pupil (name) supported : 'Emil'!");
            }
        } else if (credentialRequest.startsWith("SUBJECT")) {
            String subject;
            if("Emil".equalsIgnoreCase(pupil)) {
                subject = "French";
                //            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject", "French");
    
            } else {
                throw new IllegalStateException(
                        "Pupil (name) supported : 'Emil'!");
            }
    
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:maths" , "maths".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:physics" , "physics".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:English" , "English".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:French" , "French".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject1" , "subject1".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject2" , "subject2".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject3" , "subject3".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject4" , "subject4".equals(subject));
            attributeValueMap.put("urn:soderhamn:credspec:credSubject:subject5" , "subject5".equals(subject));
        }
        return this.finishStartIssuance(credentialRequest, attributeValueMap);
    }


    private JAXBElement<IssuanceMessage> finishStartIssuance(String specAndPolicyId,
            Map<String, Object> attributeValueMap) throws Exception {

        IssuanceMessage im_with_policy;
        try {
            System.out.println(" - invoke ABCE - using IssuanceHelper! " + specAndPolicyId + " : "
                    + attributeValueMap);

            im_with_policy =
                    IssuanceHelper.getInstance().initIssuance(specAndPolicyId, attributeValueMap);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" - failed to initIssuanceProtocol - using IssuanceHelper!");
            throw new IllegalStateException("Failed to initIssuanceProtocol - using IssuanceHelper!");
        }

        System.out.println(" - return inital message - for context : " + im_with_policy.getContext());
        System.out.println(" - return inital message : " + XmlUtils.toXml(this.of.createIssuanceMessage(im_with_policy)));
        return this.of.createIssuanceMessage(im_with_policy);
    }


}

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

package eu.abc4trust.ri.service.issuer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

@Path("/issue")
public class IssuanceService {

    private static final String IDCARD_ISSUANCE_WITH_KEY = "idcardIssuanceWithKey";
    private static final String CREDITCARD_VISA = "creditcardVisa";
    private static final String CREDITCARD_AMEX = "creditcardAmex";
    private static final String PASSPORT_CH = "passportCH";
    private static final String STUDENTCARD = "studentcard";

    private static final String CREDSPEC_UNIVERSITY = "credSpecUniversity";
    private static final String CREDSPEC_COURCE = "credSpecCource";
    @SuppressWarnings("unused")
    private static final String CREDSPEC_ATTENDANCE = "credSpecAttendance";

    private static final String SODERHAMN_SCHOOL = "SODERHAMN_SCHOOL";
    private static final String SODERHAMN_SUBJECT = "SODERHAMN_SUBJECT";

    ObjectFactory of = new ObjectFactory();

    public IssuanceService() {
        System.out.println("IssuanceService ");
    }

    private static CryptoEngine clientEngine = null;
    private void initIssuanceHelper(CryptoEngine cryptoEngine, CryptoEngine clientEngine) {
        if(cryptoEngine==CryptoEngine.BRIDGED) {
            IssuanceService.clientEngine = clientEngine;
        } else {
            IssuanceService.clientEngine = cryptoEngine;
        }

        try {
            IssuanceHelper.resetInstance();
            System.out.println("IssuanceHelper - try to - init!");

            String uprovePath;
            String fileStoragePrefix; // = "issuer_";
            String folderName;
            if (new File("target").exists()) {
                fileStoragePrefix = "target/issuer_";
                folderName = "target";
                uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
            } else {
                fileStoragePrefix = "integration-test-issuer/target/issuer_";
                folderName = "integration-test-issuer/target";
                uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
            }
            if(System.getProperty("PathToUProveExe",null) == null) {
                System.setProperty("PathToUProveExe", uprovePath);
            }

            SpecAndPolicy idcard =
                    new SpecAndPolicy(IDCARD_ISSUANCE_WITH_KEY,
                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
                            "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");
            // null, "http://my.country/identitycard/issuancekey_v1.0", null);
            //        SpecAndPolicy creditcard_visa =
            //            new SpecAndPolicy(CREDITCARD_VISA,
            //                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
            //                "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardVisa.xml");
            //        SpecAndPolicy creditcard_amex =
            //            new SpecAndPolicy(
            //                CREDITCARD_AMEX,
            //                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcardAmex.xml",
            //                "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardAmex.xml");
            //        SpecAndPolicy passport_ch =
            //            new SpecAndPolicy(PASSPORT_CH,
            //                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
            //                "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml");
            //
            //        SpecAndPolicy studentcard =
            //            new SpecAndPolicy(
            //                STUDENTCARD,
            //                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml",
            //                "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml");

            //
            SpecAndPolicy university =
                    new SpecAndPolicy(CREDSPEC_UNIVERSITY,
                            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
                            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
            SpecAndPolicy cource =
                    new SpecAndPolicy(CREDSPEC_COURCE,
                            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
                            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");
            //        SpecAndPolicy attendance =
            //            new SpecAndPolicy(CREDSPEC_ATTENDANCE,
            //                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml",
            //                "/eu/abc4trust/sampleXml/issuance/issuancePolicyPatrasAttendance.xml");

            SpecAndPolicy soderhamn_school = new SpecAndPolicy(SODERHAMN_SCHOOL, "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml");
            SpecAndPolicy soderhamn_subject = new SpecAndPolicy(SODERHAMN_SUBJECT, "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml","/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSubject.xml");

            String systemAndIssuerParamsPrefix = fileStoragePrefix;

            // Create a list to allow for rev auth parameters to be passed
            SpecAndPolicy[] specsAndPolicies = {idcard, university, cource, soderhamn_school, soderhamn_subject};

            String[] revAuthParamsResource = this.getRevParams(folderName);

            IssuanceHelper.initInstance(cryptoEngine, systemAndIssuerParamsPrefix, fileStoragePrefix, specsAndPolicies, revAuthParamsResource);
            // ,creditcard_visa,
            // creditcard_amex,
            // passport_ch,
            // studentcard,
            // university,
            // cource,
            // attendance);

            // Idemix : soderhamn 'registration' on static default secret...
            IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(IssuanceHelper.TEST_CONSTANTS.soderhamnPseudonymValue_Idemix);

            // Idemix : patras 'registration' on static default secret...
            IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_Idemix);

            // UPROVE : default pseudonym value...
            IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_UProve);

            System.out.println("IssuanceHelper - done!");
        } catch (Exception e) {
            System.out.println("Create Domain FAILED " + e);
            e.printStackTrace();
        }
    }

    private String[] getRevParams(String path){
        File folder = new File(path);

        File[] revAuthParamsFileList = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.startsWith("revocation_authority_")) {
                    System.out.println("Test : " + arg1);
                    return true;
                } else {
                    return false;
                }
            }
        });
        String[] ret = new String[revAuthParamsFileList.length];
        for(int i = 0; i< ret.length; i++){
            ret[i] = revAuthParamsFileList[i].getAbsolutePath();
        }
        return ret;
    }

    @GET()
    @Path("/init/{CryptoEngine}")
    @Produces(MediaType.TEXT_PLAIN)
    public String init(@PathParam("CryptoEngine") final String cryptoEngineName, final @QueryParam("clientEngine") CryptoEngine clientEngine) {
        System.out.println("issuance service.init : " + cryptoEngineName + " - client : " + clientEngine);
        CryptoEngine cryptoEngine = CryptoEngine.valueOf(cryptoEngineName);
        this.initIssuanceHelper(cryptoEngine, clientEngine);
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


    @POST()
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
        if ("idcard".equals(credentialRequest)) {

            if ("alice".equals(user)) {
                attributeValueMap.put("Firstname", "Alice");
                attributeValueMap.put("Lastname", "Nexdoor");
                Calendar bd = Calendar.getInstance();
                bd.set(1970, 1, 1, 0, 0, 0);
                attributeValueMap.put("Birthday", bd);
            } else if ("stewart".equals(user)) {
                attributeValueMap.put("Firstname", "Stewart");
                attributeValueMap.put("Lastname", "Dent");
                Calendar bd = Calendar.getInstance();
                bd.set(1995, 1, 1, 0, 0, 0);
                attributeValueMap.put("Birthday", bd);
            } else {
                throw new IllegalStateException("IDCard issuance only defined for alice and stewart!");
            }

            // select witch credspec + policy to use in helper
            credspecAndPolicyKey = IDCARD_ISSUANCE_WITH_KEY;
        } else if ("passport".equals(credentialRequest)) {
            if (!"ch".equals(variant)) {
                throw new IllegalStateException("Passport - only 'ch' passports supported..");
            }

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

            // select witch credspec + policy to use in helper
            credspecAndPolicyKey = PASSPORT_CH;
        } else if ("creditcard".equals(credentialRequest)) {
            if ("visa".equals(variant)) {
                credspecAndPolicyKey = CREDITCARD_VISA;
                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
                        "http://visa.com/creditcard/revocation/parameters");
            } else if ("amex".equals(variant)) {
                credspecAndPolicyKey = CREDITCARD_AMEX;
                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
                        "http://amex.com/amexcard/revocation/parameters");
            } else {
                throw new IllegalStateException("Creditcard - unknow variant : " + variant);
            }

            if ("stewart".equals(user)) {
                attributeValueMap.put("CardType", "Normal");
                attributeValueMap.put("Name", "Stewart");
                attributeValueMap.put("LastName", "Dent");
                attributeValueMap.put("CardNumber", 1);
                Calendar cal = Calendar.getInstance();
                cal.set(2015, 1, 1, 0, 0, 0);
                attributeValueMap.put("ExpirationDate", cal.getTime());
                attributeValueMap.put("SecurityCode", 1);
                attributeValueMap.put("Status", "status");

            } else if ("alice".equals(user)) {
                attributeValueMap.put("CardType", "Gold");
                attributeValueMap.put("Name", "Alice");
                attributeValueMap.put("LastName", "Nextdoor");
                attributeValueMap.put("CardNumber", 42);
                Calendar cal = Calendar.getInstance();
                cal.set(2014, 1, 1, 0, 0, 0);
                attributeValueMap.put("ExpirationDate", cal.getTime());
                attributeValueMap.put("SecurityCode", 42);
                attributeValueMap.put("Status", "status");
            } else {
                throw new IllegalStateException("Creditcard - user not defined in testcase : " + user);
            }
        } else if ("studentcard".equals(credentialRequest)) {

            if ("stewart".equals(user)) {
                /*
                 * <abc:AttributeDescription Type="http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"
                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="Name"
                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="LastName"
                 * DataType="xs:string" Encoding="UTF-8"/> <abc:AttributeDescription Type="StudentNumber"
                 * DataType="xs:integer" Encoding="UTF-8"/> <abc:AttributeDescription Type="Issued"
                 * DataType="xs:dateTime" Encoding="UTF-8"/> <abc:AttributeDescription Type="Expires"
                 * DataType="xs:dateTime" Encoding="UTF-8"/> <abc:AttributeDescription Type="IssuedBy"
                 * DataType="xs:string" Encoding="UTF-8"/>
                 */
                attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
                        "http://www.ethz.ch/studentid/revocation/parameters");
                attributeValueMap.put("Name", "Stewart");
                attributeValueMap.put("LastName", "Dent");
                attributeValueMap.put("StudentNumber", 1);
                Calendar cal = Calendar.getInstance();
                cal.set(2012, 1, 1, 0, 0, 0);
                attributeValueMap.put("Issued", cal.getTime());
                cal.set(2015, 1, 1, 0, 0, 0);
                attributeValueMap.put("Expires", cal.getTime());
                attributeValueMap.put("IssuedBy", "IssuedBy");

            } else {
                throw new IllegalStateException("Studentcard - user not defined in testcase : " + user);
            }
            credspecAndPolicyKey = STUDENTCARD;
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
    public JAXBElement<IssuanceMessage> issueStep(final IssuanceMessage issuanceMessage) throws Exception {

        System.out.println("IssuanceService - step - request - context : " + issuanceMessage.getContext());
        System.out.println(" - step - request from client  : " + XmlUtils.toXml(this.of.createIssuanceMessage(issuanceMessage)));

        IssuanceMessageAndBoolean response;
        try {
            response = IssuanceHelper.getInstance().issueStep(clientEngine, issuanceMessage);
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
    @Path("/startUniversity/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startUniversity(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("matriculationnumber") int matriculationnumber) throws Exception {

        System.out.println("IssuanceService - start - issue University Credential: "
                + credentialRequest);
        System.out.println("- Matriculationnumber : " + matriculationnumber);

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

        return this.finishStartIssuance(CREDSPEC_UNIVERSITY, attributeValueMap);
    }

    @GET()
    @Path("/startCourse/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startCourse(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("matriculationnumber") int matriculationnumber) throws Exception {

        System.out.println("IssuanceService - start - issue Course Credential: " + credentialRequest);
        System.out.println("- Matriculationnumber : " + matriculationnumber);

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();

        if ((matriculationnumber == 42) || (matriculationnumber == 1235332) || (matriculationnumber == 666)) {
            // ok - matriculationnumber accepted...
        } else {
            throw new IllegalStateException(
                    "Matriculationnumber issuance only defined for john(1235332) and stewart (42)!");
        }

        attributeValueMap.put("urn:patras:credspec:credCourse:courseid", "The-very-cool-course");
        // attributeValueMap.put("urn:patras:credspec:credCourse:matriculationnr", matriculationnumber);

        return this.finishStartIssuance(CREDSPEC_COURCE, attributeValueMap);
    }


    // SODERHAMN DEMO ISSUANCE

    @GET()
    @Path("/startSchool/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startSchool(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("pupil") String pupil) throws Exception {

        System.out.println("IssuanceService - start - issue School Credential: "
                + credentialRequest);
        System.out.println("- Pupil : " + pupil);

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();

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

        return this.finishStartIssuance(SODERHAMN_SCHOOL, attributeValueMap);
    }

    @GET()
    @Path("/startSubject/{CredentialRequest}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> startSubject(
            @PathParam("CredentialRequest") final String credentialRequest,
            @QueryParam("pupil") String pupil) throws Exception {

        System.out.println("IssuanceService - start - issue Subject Credential: "
                + credentialRequest);
        System.out.println("- Pupil : " + pupil);

        String subject;
        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
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

        return this.finishStartIssuance(SODERHAMN_SUBJECT, attributeValueMap);
    }


    //  @GET()
    //  @Path("/startAttendance")
    //  // /{CredentialRequest}")
    //  @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    //  @Produces(MediaType.TEXT_XML)
    //  public JAXBElement<IssuanceMessage> startAttendance(
    //      /* @PathParam ("CredentialRequest") final String credentialRequest, */@QueryParam("attendanceId") int attendanceId)
    //      throws Exception {
    //
    //    System.out.println("IssuanceService - start - issue Attendance Credential: "); // +
    //                                                                                   // credentialRequest);
    //    System.out.println("- attendanceId : " + attendanceId);
    //
    //    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    //
    //    if (attendanceId >= 1 && attendanceId <= 7) {
    //      // ok - matriculationnumber accepted...
    //    } else {
    //      throw new IllegalStateException("attendanceId shold be between 1 and 7!");
    //    }
    //
    //    attributeValueMap.put("AttendanceUid", "attendance_" + attendanceId);
    //    attributeValueMap.put("CourseUid", "The-very-cool-course");
    //    attributeValueMap.put("LectureUid", "lecture");
    //
    //    // patch Policy
    //    SpecAndPolicy spec_n_policy =
    //        IssuanceHelper.getInstance().initSpecAndPolicy(CREDSPEC_ATTENDANCE);
    //    // System.out.println("CredSpec URI : " +
    //    // spec_n_policy.getCredentialSpecification().getSpecificationUID());
    //    // System.out.println("CredSpec URI : " +
    //    // spec_n_policy.getIssuancePolicy().getCredentialTemplate().getCredentialSpecUID());
    //    String issuerParamUid =
    //        spec_n_policy.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID()
    //            .toString();
    //    System.out.println("issuerParamUid : " + issuerParamUid);
    //    issuerParamUid =
    //        issuerParamUid.substring(0, issuerParamUid.lastIndexOf("/") + 1) + +attendanceId;
    //    System.out.println("issuerParamUid : " + issuerParamUid);
    //    spec_n_policy.getIssuancePolicy().getCredentialTemplate()
    //        .setIssuerParametersUID(new URI(issuerParamUid));
    //    System.out.println("CredSpec URI : "
    //        + spec_n_policy.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID());
    //    return finishStartIssuance(CREDSPEC_ATTENDANCE, attributeValueMap);
    //  }


    private JAXBElement<IssuanceMessage> finishStartIssuance(String specAndPolicyId,
            Map<String, Object> attributeValueMap) throws Exception {

        IssuanceMessage im_with_policy;
        try {
            System.out.println(" - invoke ABCE - using IssuanceHelper! " + specAndPolicyId + " : "
                    + attributeValueMap);

            im_with_policy =
                    IssuanceHelper.getInstance().initIssuance(clientEngine, specAndPolicyId, attributeValueMap);

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

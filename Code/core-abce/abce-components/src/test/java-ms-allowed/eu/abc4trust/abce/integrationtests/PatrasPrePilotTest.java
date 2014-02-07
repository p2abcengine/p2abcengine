//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.integrationtests;

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Patras scenario.
 */
public class PatrasPrePilotTest {
    private static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN = "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml";

    private static final String CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY = "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml";

    private static final String ISSUANCE_POLICY_PATRAS_UNIVERSITY = "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml";

    private static final String CREDENTIAL_SPECIFICATION_PATRAS_COURSE = "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml";

    private static final String ISSUANCE_POLICY_PATRAS_COURSE = "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml";

    private static final String PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION = "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluationPrePilotTesting.xml";



    // TODO: Backup and restore of attendance credentials.

    private static final String COURSE_UID = "23330E";
    private static final String SHA256 = "urn:abc4trust:1.0:encoding:string:sha-256";
    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String UNIVERSITYNAME = "Patras";
    private static final String DEPARTMENTNAME = "CS";
    private static final int MATRICULATIONNUMBER = 1235332;
    private static final String ATTENDANCE_UID = "attendance";
    private static final String LECTURE_UID = "lecture";

    @Ignore
    @Test
    public void patraPilotHappypathIdemixTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
        // Create URIs.

        int keyLength = 1024; // TODO: define the security level & revocation
        URI cryptoMechanism = new URI("urn:abc4trust:1.0:algorithm:idemix");
        Secret secret = this.getSecret();
        // Generate system parameters.
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters systemParameters = universityEngine
                .setupSystemParameters(keyLength, cryptoMechanism);
        PseudonymWithMetadata pwm = this.getIdemixPseudonym(secret
                .getSecretDescription().getSecretUID(),
                (GroupParameters) systemParameters.getAny().get(1));
        this.runTest(cryptoEngine, keyLength, cryptoMechanism, pwm, secret,
                systemParameters, universityInjector);
    }

    @Ignore
    @Test
    public void patraPilotHappypathUProveTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.UPROVE;
        // Create URIs.
        int keyLength = 1024; // TODO: define the security level & revocation
        URI cryptoMechanism = new URI("urn:abc4trust:1.0:algorithm:uprove");
        Secret secret = this.getSecret();
        PseudonymWithMetadata pwm = this.getUProvePseudonym(secret
                .getSecretDescription().getSecretUID());
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters systemParameters = universityEngine
                .setupSystemParameters(keyLength, cryptoMechanism);
        this.runTest(cryptoEngine, keyLength, cryptoMechanism, pwm, secret,
                systemParameters, universityInjector);
    }

    private void runTest(CryptoEngine cryptoEngine, int keyLength,
            URI cryptoMechanism, PseudonymWithMetadata pwm, Secret secret,
            SystemParameters systemParameters, Injector universityInjector)
                    throws KeyManagerException, JAXBException,
                    UnsupportedEncodingException, SAXException, URISyntaxException,
                    Exception, CredentialManagerException {
        IssuanceHelper issuanceHelper = new IssuanceHelper();
        Injector courseEvaluationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);

        KeyManager issuerKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = courseEvaluationInjector
                .getInstance(KeyManager.class);


        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);

        // Setup issuance policies.
        IssuancePolicy issuancePolicyUniversity = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                ISSUANCE_POLICY_PATRAS_UNIVERSITY),
                                true);

        IssuancePolicy issuancePolicyCredCourse = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                ISSUANCE_POLICY_PATRAS_COURSE),
                                true);

        URI universityIssuancePolicyUid = issuancePolicyUniversity.getCredentialTemplate()
                .getIssuerParametersUID();
        URI courseIssuancePolicyUid = issuancePolicyCredCourse
                .getCredentialTemplate()
                .getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification universityCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY),
                                true);
        CredentialSpecification credCourseSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                CREDENTIAL_SPECIFICATION_PATRAS_COURSE), true);

        // Store credential specifications.
        URI universitySpecificationUID = universityCredSpec
                .getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(
                universitySpecificationUID, universityCredSpec);

        URI credCourseSpecificationUID = credCourseSpec.getSpecificationUID();
        userKeyManager.storeCredentialSpecification(credCourseSpecificationUID,
                credCourseSpec);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI revocationId = new URI("revocationUID1");
        IssuerParameters universityIssuerParameters = universityEngine
                .setupIssuerParameters(universityCredSpec, systemParameters,
                        universityIssuancePolicyUid, hash, URI.create("uprove"),revocationId, null);

        // ObjectFactory of = new ObjectFactory();
        // System.out.println(" - universityParameters : "
        // + XmlUtils.toXml(of
        // .createIssuerParameters(universityIssuerParameters)));

        revocationId = new URI("revocationUID2");
        IssuerParameters credCourseIssuerParameters = universityEngine
                .setupIssuerParameters(credCourseSpec, systemParameters,
                        courseIssuancePolicyUid, hash, URI.create("uprove"),revocationId, null);

        issuerKeyManager.storeIssuerParameters(universityIssuancePolicyUid,
                universityIssuerParameters);
        userKeyManager.storeIssuerParameters(universityIssuancePolicyUid,
                universityIssuerParameters);
        verifierKeyManager.storeIssuerParameters(universityIssuancePolicyUid,
                universityIssuerParameters);

        issuerKeyManager.storeIssuerParameters(courseIssuancePolicyUid,
                credCourseIssuerParameters);
        userKeyManager.storeIssuerParameters(courseIssuancePolicyUid,
                credCourseIssuerParameters);
        verifierKeyManager.storeIssuerParameters(courseIssuancePolicyUid,
                credCourseIssuerParameters);

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);

        userCredentialManager.storeSecret(secret);

        // Step 0. Create a pseudonym and store it in the user credential
        // manager.
        userCredentialManager.storePseudonym(pwm);

        // This is a hack since the TokenManagerIssuer does not allow us to add
        // a pseudonym.
        @SuppressWarnings("unused")
        TokenStorageIssuer universityTokenStorageManager = universityInjector
        .getInstance(TokenStorageIssuer.class);
        @SuppressWarnings("unused")
        String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
        // universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);

        // Step 1. Login with pseudonym.
        System.out.println(">> Login with pseudonym.");
        this.loginWithPseudonym(universityInjector, userInjector,
                issuanceHelper);

        // Step 1. Get university credential.
        System.out.println(">> Get university credential.");
        this.issueAndStoreUniversityCredential(universityInjector,
                userInjector, issuanceHelper);

        // Step 2. Get course credential.
        System.out.println(">> Get course credential.");
        this.issueAndStoreCourseCredential(universityInjector, userInjector,
                issuanceHelper);

        // Verify against course evaluation using the course credential.
        System.out.println(">> Verify.");
        PresentationToken pt = this.logIntoCourseEvaluation(issuanceHelper,
                courseEvaluationInjector, userInjector);
        assertNotNull(pt);
    }

    private void loginWithPseudonym(Injector universityInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        int presentationTokenChoice = 0;
        int pseudonymChoice = 0;
        PresentationToken t = this.loginWithPseudonym(issuanceHelper,
                universityInjector,
                userInjector, presentationTokenChoice, pseudonymChoice);
        assertNotNull(t);
    }

    private PresentationToken loginWithPseudonym(IssuanceHelper issuanceHelper,
            Injector universityInjector, Injector userInjector,
            int presentationTokenChoice, int pseudonymChoice) throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        chosenInspectors.add(URI
                .create("http://patras.gr/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(universityInjector, userInjector,
                        PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN,
                        new PolicySelector(presentationTokenChoice,
                                chosenInspectors, pseudonymChoice));

        return issuanceHelper.verify(universityInjector, p.second(), p.first());
    }

    private Secret getSecret() throws JAXBException,
    UnsupportedEncodingException, SAXException {
        Secret secret = (Secret) XmlUtils.getObjectFromXML(
                this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"),
                        true);
        return secret;
    }

    private void issueAndStoreUniversityCredential(Injector issuerInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        Map<String, Object> atts = this.populateUniveristyAttributes();
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY,
                ISSUANCE_POLICY_PATRAS_UNIVERSITY, atts);
    }

    private void issueAndStoreCourseCredential(Injector issuerInjector,
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        Map<String, Object> atts = this.populateCourseAttributes();
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_PATRAS_COURSE,
                ISSUANCE_POLICY_PATRAS_COURSE, atts);

    }

    private PresentationToken logIntoCourseEvaluation(
            IssuanceHelper issuanceHelper, Injector verifierInjector,
            Injector userInjector) throws Exception {
        /*
         * Verify for poll. The user must have:
         * 1) A nonrevoked university credential
         * 2) A course credential with the same matriculation number
         * as the university credential
         * 3) A certain number of attendance credentials, which must be higher than a certain threshold
         * 4) All attendance credentials must have the same matriculation number as the
         * university credential
         * 5) All attendance credentials must have a unique UID.
         */
        int presentationTokenChoice = 0;
        int pseudonymChoice = 0;
        return this.login(issuanceHelper, verifierInjector, userInjector,
                presentationTokenChoice, pseudonymChoice);
    }

    private PresentationToken login(IssuanceHelper issuanceHelper,
            Injector verifierInjector, Injector userInjector,
            int presentationTokenChoice, int pseudonymChoice) throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        chosenInspectors.add(URI
                .create("http://thebestbank.com/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION,
                        new PolicySelector(presentationTokenChoice,
                                chosenInspectors, pseudonymChoice));

        // Store all required cred specs in the verifier key manager.
        KeyManager hotelKeyManager = verifierInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

        PresentationToken pt = p.first();
        assertNotNull(pt);
        for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
            hotelKeyManager.storeCredentialSpecification(
                    cit.getCredentialSpecUID(),
                    userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
        }

        return issuanceHelper.verify(verifierInjector, p.second(), p.first());
    }

    private PseudonymWithMetadata getUProvePseudonym(URI secretUid) {
        ObjectFactory of = new ObjectFactory();

        byte[] pv = new byte[3];
        pv[0] = 42;
        pv[1] = 84;
        pv[2] = 117;
        Pseudonym pseudonym = of.createPseudonym();
        pseudonym.setSecretReference(secretUid);
        pseudonym.setExclusive(true);
        pseudonym.setPseudonymUID(URI.create("foo-bar-pseudonym-uid"));
        pseudonym.setPseudonymValue(pv);
        String scope = "urn:patras:evaluation";
        pseudonym.setScope(scope);

        Metadata md = of.createMetadata();
        PseudonymMetadata pmd = of.createPseudonymMetadata();
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("Pregenerated pseudonym");
        pmd.getFriendlyPseudonymDescription().add(fd);
        pmd.setMetadata(md);
        PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
        pwm.setPseudonymMetadata(pmd);
        CryptoParams cryptoEvidence = of.createCryptoParams();
        URI scopeUri = URI.create(scope);
        URI groupParameterId = URI
                .create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");
        StoredDomainPseudonym dp = new StoredDomainPseudonym(scopeUri,
                secretUid, groupParameterId);
        cryptoEvidence.getAny().add(
                XMLSerializer.getInstance().serializeAsElement(dp));
        pwm.setCryptoParams(cryptoEvidence);
        return pwm;
    }

    private PseudonymWithMetadata getIdemixPseudonym(URI secretUid,
            GroupParameters groupParameters) {
        ObjectFactory of = new ObjectFactory();

        byte[] pv = new byte[3];
        pv[0] = 42;
        pv[1] = 84;
        pv[2] = 117;
        Pseudonym pseudonym = of.createPseudonym();
        pseudonym.setSecretReference(secretUid);
        pseudonym.setExclusive(true);
        pseudonym.setPseudonymUID(URI.create("foo-bar-pseudonym-uid"));
        pseudonym.setPseudonymValue(pv);
        String scope = "urn:patras:registration";
        pseudonym.setScope(scope);

        Metadata md = of.createMetadata();
        PseudonymMetadata pmd = of.createPseudonymMetadata();
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("Pregenerated pseudonym");
        pmd.getFriendlyPseudonymDescription().add(fd);
        pmd.setMetadata(md);
        PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
        pwm.setPseudonymMetadata(pmd);
        CryptoParams cryptoEvidence = of.createCryptoParams();
        URI scopeUri = URI.create(scope);
        URI groupParameterId = URI
                .create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");
        StructureStore.getInstance().add(groupParameterId.toString(),
                groupParameters);
        StoredDomainPseudonym dp = new StoredDomainPseudonym(scopeUri,
                secretUid, groupParameterId);
        cryptoEvidence.getAny().add(
                XMLSerializer.getInstance().serializeAsElement(dp));
        pwm.setCryptoParams(cryptoEvidence);
        return pwm;
    }

    @SuppressWarnings("unused")
    private void populateIssuerAttributesForAttendance(
            List<Attribute> issuerAtts, int inx) {
        ObjectFactory of = new ObjectFactory();

        Attribute attendanceUid = of.createAttribute();
        attendanceUid.setAttributeUID(URI.create("fij9823jrkg8itnf"));
        attendanceUid.setAttributeValue(ATTENDANCE_UID+"_"+inx);
        attendanceUid.setAttributeDescription(of.createAttributeDescription());
        attendanceUid.getAttributeDescription()
        .setDataType(URI.create("xs:string"));
        attendanceUid.getAttributeDescription().setEncoding(URI.create(SHA256));
        attendanceUid.getAttributeDescription().setType(
                URI.create("AttendanceUid"));
        issuerAtts.add(attendanceUid);

        Attribute courseUid = of.createAttribute();
        courseUid.setAttributeUID(URI.create("lj9823jrkg8itnf"));
        courseUid.setAttributeValue(COURSE_UID);
        courseUid.setAttributeDescription(of.createAttributeDescription());
        courseUid.getAttributeDescription()
        .setDataType(URI.create("xs:string"));
        courseUid.getAttributeDescription().setEncoding(URI.create(SHA256));
        courseUid.getAttributeDescription().setType(URI.create("CourseUid"));
        issuerAtts.add(courseUid);

        Attribute lectureUid = of.createAttribute();
        lectureUid.setAttributeUID(URI.create("lka3jrkg8itnf"));
        lectureUid.setAttributeValue(LECTURE_UID);
        lectureUid.setAttributeDescription(of.createAttributeDescription());
        lectureUid.getAttributeDescription().setDataType(
                URI.create("xs:string"));
        lectureUid.getAttributeDescription().setEncoding(URI.create(SHA256));
        lectureUid.getAttributeDescription().setType(URI.create("LectureUid"));
        issuerAtts.add(lectureUid);
    }

    private Map<String, Object> populateCourseAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
        att.put("urn:patras:credspec:credCourse:matriculationnr",
                MATRICULATIONNUMBER);
        att.put(REVOCATION_HANDLE_STR,
                URI.create("urn:patras:revocation:handle2"));
        return att;
    }

    private Map<String, Object> populateUniveristyAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:patras:credspec:credUniv:firstname", NAME);
        att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
        att.put(REVOCATION_HANDLE_STR,
                URI.create("urn:patras:revocation:handle1"));
        att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
        att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
        att.put("urn:patras:credspec:credUniv:matriculationnr",
                MATRICULATIONNUMBER);
        return att;
    }
}
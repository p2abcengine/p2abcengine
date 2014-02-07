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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Patras scenario.
 */
public class PatrasPilotTest {
    private static final String URN_ABC4TRUST_1_0_ALGORITHM_UPROVE = "urn:abc4trust:1.0:algorithm:uprove";

    private static final String URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX = "urn:abc4trust:1.0:algorithm:idemix";

    @SuppressWarnings("unused")
    private static final String URN_ABC4TRUST_1_0_ALGORITHM_BRIDGING = "urn:abc4trust:1.0:algorithm:bridging";

    private static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN = "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml";

    private static final String CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY = "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml";

    private static final String ISSUANCE_POLICY_PATRAS_UNIVERSITY = "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml";

    private static final String CREDENTIAL_SPECIFICATION_PATRAS_COURSE = "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml";

    private static final String ISSUANCE_POLICY_PATRAS_COURSE = "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml";

    private static final String PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION = "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml";

    private static final String PRESENTATION_POLICY_PATRAS_TOMBOLA = "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasTombola.xml";

    private static final String ISSUANCE_POLICY_PATRAS_TOMBOLA = "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasTombola.xml";

    private static final String CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA = "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasTombola.xml";

    // TODO: Backup and restore of attendance credentials.

    private static final String COURSE_UID = "23330E";
    @SuppressWarnings("unused")
    private static final String SHA256 = "urn:abc4trust:1.0:encoding:string:sha-256";
    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String UNIVERSITYNAME = "Patras";
    private static final String DEPARTMENTNAME = "CS";
    private static final int MATRICULATIONNUMBER = 1235332;
    @SuppressWarnings("unused")
    private static final String ATTENDANCE_UID = "attendance";
    @SuppressWarnings("unused")
    private static final String LECTURE_UID = "lecture";

    @Test
    //   @Ignore
    public void patraPilotHappypathIdemixTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
        int keyLength = 1024;
        URI cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);
        Random random = new Random(1231);
        // Generate system parameters.
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(random, cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters systemParameters = universityEngine
                .setupSystemParameters(keyLength, cryptoMechanism);
        SecretWrapper secretWrapper = new SecretWrapper(this.getIdemixSecret());

        this.runIdemixTest(cryptoEngine, keyLength, cryptoMechanism,
                universityInjector, universityEngine, systemParameters,
                secretWrapper);
    }

    @Ignore
    @Test
    public void patraPilotHappypathIdemixSoftwareSmartcardTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;

        // Disable non-device-bound secrets
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;
        int keyLength = 1024;
        URI cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);
        Random random = new Random(1231);
        // Generate system parameters.
        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(random,
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        SystemParameters systemParameters = universityEngine
                .setupSystemParameters(keyLength, cryptoMechanism);
        // smartcard secret - with IDEMIX engine
        SecretWrapper secretWrapper = new SecretWrapper(cryptoEngine, random, systemParameters);

        this.runIdemixTest(cryptoEngine, keyLength, cryptoMechanism,
                universityInjector, universityEngine, systemParameters,
                secretWrapper);
    }

    private void runIdemixTest(CryptoEngine cryptoEngine,
            int keyLength,
            URI cryptoMechanism, Injector universityInjector,
            IssuerAbcEngine universityEngine,
            SystemParameters systemParameters, SecretWrapper secretWrapper)
                    throws URISyntaxException, CredentialManagerException,
                    KeyManagerException, JAXBException, UnsupportedEncodingException,
                    SAXException, Exception {
        // Create URIs.
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        IdemixCryptoEngineUserImpl userEngine = userInjector
                .getInstance(IdemixCryptoEngineUserImpl.class);

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);
        if (!secretWrapper.isSecretOnSmartcard()) {
            userCredentialManager.storeSecret(secretWrapper.getSecret());
        }

        PseudonymWithMetadata pwm = this.getIdemixPseudonym(
                secretWrapper.getSecretUID(), userEngine, systemParameters);
        Injector courseEvaluationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        boolean tombolaTest = true;
        this.setupEngines(courseEvaluationInjector, keyLength, cryptoMechanism, pwm,
                secretWrapper,
                systemParameters, universityInjector, userInjector, tombolaTest);
    }

    @Test
    // @Ignore
    public void patraPilotHappypathUProveTest() throws Exception {

        // Create URIs.
        int keyLength = 2048;
        URI cryptoMechanism = URI.create(URN_ABC4TRUST_1_0_ALGORITHM_UPROVE);
        UProveUtils uproveUtils = new UProveUtils();

        SecretWrapper secretWrapper = new SecretWrapper(this.getUProveSecret());
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort()));

        PseudonymWithMetadata pwm = this.getUProvePseudonym(secretWrapper.getSecretUID(), userInjector);

        Injector universityInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        uproveUtils.getIssuerServicePort()));
        SystemParameters systemParameters = this
                .getUProveSystemParameters(universityInjector);
        Injector courseEvaluationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort()));

        // No Tobola for UProve - We are missing Carry Over
        boolean tombolaTest = false;
        this.setupEngines(courseEvaluationInjector, keyLength, cryptoMechanism, pwm,
                secretWrapper,
                systemParameters, universityInjector, userInjector, tombolaTest);

//        int exitCode = userInjector.getInstance(UProveBindingManager.class)
//                .stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = universityInjector.getInstance(UProveBindingManager.class)
//                .stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = courseEvaluationInjector.getInstance(
//                UProveBindingManager.class).stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }

    // @Ignore
    @Test
    public void patrasPilotBridgingTest() throws Exception {
        int idemixKeyLength = 2048;
        int uproveKeylength = 2048;
        SecretWrapper uproveSecretWrapper = new SecretWrapper(this.getUProveSecret());
        SecretWrapper idemixSecretWrapper = new SecretWrapper(this.getIdemixSecret());

        UProveUtils uproveUtils = new UProveUtils();

        // Get Injectors,
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1231), uproveUtils.getUserServicePort()));
        Injector universityInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231), IssuerCryptoEngine.UPROVE,
                        uproveUtils.getIssuerServicePort()));
        Injector courseEvaluationInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231), IssuerCryptoEngine.IDEMIX,
                        uproveUtils.getVerifierServicePort()));
        IssuanceHelper issuanceHelper = new IssuanceHelper();

        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);


        // We must instantiate a delegatorengine in order to workaround circular dependencies
        // since we need an instance of the idemixengine
        @SuppressWarnings("unused")
        UserAbcEngine userAbcEngine = userInjector.getInstance(UserAbcEngine.class);

        IssuerAbcEngine courseEngine = courseEvaluationInjector.getInstance(IssuerAbcEngine.class);

        IdemixCryptoEngineUserImpl userIdemixEngine = userInjector
                .getInstance(IdemixCryptoEngineUserImpl.class);

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector
                .getInstance(CredentialManager.class);
        if (!idemixSecretWrapper.isSecretOnSmartcard()) {
            userCredentialManager.storeSecret(idemixSecretWrapper.getSecret());
        }

        /*
        SystemParameters idemixSystemParameters = courseEngine
                .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

        //	Change here if we want dynamic system parameters for uprove
        SystemParameters uproveSystemParameters = this.getUProveSystemParameters(universityInjector);
         */

        SystemParameters sysParam = SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(
                        idemixKeyLength, uproveKeylength);

        IdemixCryptoEngineUserImpl.loadIdemixSystemParameters(sysParam);





        PseudonymWithMetadata upwm = this.getUProvePseudonym(uproveSecretWrapper.getSecretUID(), userInjector);
        /*    PseudonymWithMetadata ipwm = this.getIdemixPseudonym(
                idemixSecretWrapper.getSecretUID(), userEngine, idemixSystemParameters); */




        KeyManager issuerKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = courseEvaluationInjector
                .getInstance(KeyManager.class);


        issuerKeyManager.storeSystemParameters(sysParam);
        //        userKeyManager.storeSystemParameters(idemixSystemParameters);
        userKeyManager.storeSystemParameters(sysParam);
        verifierKeyManager.storeSystemParameters(sysParam);

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
                .setupIssuerParameters(universityCredSpec, sysParam,
                        universityIssuancePolicyUid, hash, URI.create("uprove"),revocationId, null);

        // ObjectFactory of = new ObjectFactory();
        // System.out.println(" - universityParameters : "
        // + XmlUtils.toXml(of
        // .createIssuerParameters(universityIssuerParameters)));

        revocationId = new URI("revocationUID2");
        IssuerParameters credCourseIssuerParameters = universityEngine
                .setupIssuerParameters(credCourseSpec, sysParam,
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
        //   CredentialManager userCredentialManager = userInjector
        //           .getInstance(CredentialManager.class);

        if (uproveSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    uproveSecretWrapper.getSoftwareSmartcard(),
                    uproveSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            uproveSecretWrapper.addIssuerParameters(universityIssuerParameters);
            uproveSecretWrapper.addIssuerParameters(credCourseIssuerParameters);
        } else {
            userCredentialManager.storeSecret(uproveSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        if (idemixSecretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(
                    idemixSecretWrapper.getSoftwareSmartcard(),
                    idemixSecretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            idemixSecretWrapper.addIssuerParameters(universityIssuerParameters);
            idemixSecretWrapper.addIssuerParameters(credCourseIssuerParameters);
        } else {
            userCredentialManager.storeSecret(idemixSecretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }


        // Step 0. Create a pseudonym and store it in the user credential
        // manager.
        userCredentialManager.storePseudonym(upwm);
        // we only store the uprove pseudonym as a start

        // This is a hack since the TokenManagerIssuer does not allow us to add
        // a pseudonym.
        TokenStorageIssuer universityTokenStorageManager = universityInjector
                .getInstance(TokenStorageIssuer.class);
        String primaryKey = DatatypeConverter.printBase64Binary(upwm.getPseudonym().getPseudonymValue());
        universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);

        this.runTests(userInjector, universityInjector, courseEvaluationInjector, issuanceHelper);
//        // System.out.println("Test done");
//        int exitCode = userInjector.getInstance(UProveBindingManager.class)
//                .stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = universityInjector.getInstance(UProveBindingManager.class)
//                .stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = courseEvaluationInjector.getInstance(
//                UProveBindingManager.class).stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }

    private void setupEngines(Injector courseEvaluationInjector, int keyLength,
            URI cryptoMechanism, PseudonymWithMetadata pwm, SecretWrapper secretWrapper,
            SystemParameters systemParameters, Injector universityInjector,
            Injector userInjector, boolean tombolaTest)
                    throws KeyManagerException, JAXBException,
                    UnsupportedEncodingException, SAXException, URISyntaxException,
                    Exception, CredentialManagerException {
        IssuanceHelper issuanceHelper = new IssuanceHelper();

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

        if (secretWrapper.isSecretOnSmartcard()) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector
                    .getInstance(CardStorage.class);
            cardStorage.addSmartcard(secretWrapper.getSoftwareSmartcard(),
                    secretWrapper.getPin());

            // sign issuer attributes and add to smartcard
            secretWrapper.addIssuerParameters(universityIssuerParameters);
            secretWrapper.addIssuerParameters(credCourseIssuerParameters);
        } else {
            userCredentialManager.storeSecret(secretWrapper.getSecret());
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }


        // Step 0. Create a pseudonym and store it in the user credential
        // manager.
        userCredentialManager.storePseudonym(pwm);

        // This is a hack since the TokenManagerIssuer does not allow us to add
        // a pseudonym.
        TokenStorageIssuer universityTokenStorageManager = universityInjector
                .getInstance(TokenStorageIssuer.class);
        String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());
        universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);

        this.runTests(userInjector, universityInjector, courseEvaluationInjector, issuanceHelper);

        if(tombolaTest) {
            // TODO : Verify New Tombola!!!

            IssuancePolicy issuancePolicyTombola = (IssuancePolicy) XmlUtils
                    .getObjectFromXML(
                            this.getClass()
                            .getResourceAsStream(
                                    ISSUANCE_POLICY_PATRAS_TOMBOLA),
                                    true);

            try {
                System.out.println("IssuancePolicy " + XmlUtils.toXml(new ObjectFactory().createIssuancePolicy(issuancePolicyTombola)));
            } catch(Exception e) {

            }
            URI tombolaIssuancePolicyUid = issuancePolicyTombola.getCredentialTemplate()
                    .getIssuerParametersUID();

            CredentialSpecification tombolaCredSpec = (CredentialSpecification) XmlUtils
                    .getObjectFromXML(
                            this.getClass().getResourceAsStream(
                                    CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA),
                                    true);
            URI tombolaSpecificationUID = tombolaCredSpec.getSpecificationUID();
            issuerKeyManager.storeCredentialSpecification(tombolaSpecificationUID, tombolaCredSpec);
            verifierKeyManager.storeCredentialSpecification(tombolaSpecificationUID, tombolaCredSpec);

            IssuerParameters tombolaIssuerParameters = universityEngine
                    .setupIssuerParameters(tombolaCredSpec, systemParameters,
                            tombolaIssuancePolicyUid, hash, URI.create("uprove"),revocationId, null);

            issuerKeyManager.storeIssuerParameters(tombolaIssuancePolicyUid,
                    tombolaIssuerParameters);
            userKeyManager.storeIssuerParameters(tombolaIssuancePolicyUid,
                    tombolaIssuerParameters);
            verifierKeyManager.storeIssuerParameters(tombolaIssuancePolicyUid,
                    tombolaIssuerParameters);

            Map<String, Object> atts = new HashMap<String, Object>();
            issuanceHelper.issueCredential(universityInjector, userInjector,
                    CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA,
                    ISSUANCE_POLICY_PATRAS_TOMBOLA, atts);

            Injector inspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
              UProveUtils.UPROVE_COMMON_PORT));

            InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
            KeyManager injectorKeyManager = inspectorInjector.getInstance(KeyManager.class);
            injectorKeyManager.storeSystemParameters(systemParameters);

            CredentialManager inspectorCredentialManager = inspectorInjector.getInstance(CredentialManager.class);

            injectorKeyManager.storeCredentialSpecification(tombolaSpecificationUID, tombolaCredSpec);
            injectorKeyManager.storeIssuerParameters(tombolaIssuancePolicyUid,
                    tombolaIssuerParameters);

            URI mechanism = CryptoUriUtil.getIdemixMechanism();
            int inspectorKeySize = 1024;
            URI inspectorPublicKeyUid = URI.create("urn:patras:inspector:tombola");
            InspectorPublicKey inspectorPublicKey = inspectorEngine.setupInspectorPublicKey(inspectorKeySize, mechanism, inspectorPublicKeyUid );

            System.out.println("inspectorPublicKey : " + inspectorPublicKey.getPublicKeyUID());
            userKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);
            verifierKeyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);

            VEPublicKey vePubKey =
                    (VEPublicKey) Parser.getInstance().parse(
                            (org.w3c.dom.Element) inspectorPublicKey.getCryptoParams().getAny().get(0));
            StructureStore.getInstance().add(inspectorPublicKey.getPublicKeyUID().toString(), vePubKey);


            int presentationTokenChoice = 0;
            int pseudonymChoice = 0;
            List<URI> chosenInspectors = new LinkedList<URI>();
            chosenInspectors.add(inspectorPublicKeyUid);
            // create presentation policy
            Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                    .createPresentationToken(userInjector, userInjector,
                            PRESENTATION_POLICY_PATRAS_TOMBOLA,
                            new PolicySelector(presentationTokenChoice,
                                    chosenInspectors, pseudonymChoice));
            // verify..
            issuanceHelper.verify(courseEvaluationInjector, p.second(), p.first());

            // inspect..
            List<Attribute> inspectedAttributes = inspectorEngine.inspect(p.first());
            System.out.println("inspectedAttributes");
            for(Attribute a : inspectedAttributes) {
                System.out.println(" a " + a.getAttributeUID() + " : " + a.getAttributeValue());
            }
        }

    }

    private void runTests(Injector userInjector, Injector universityInjector, Injector courseEvaluationInjector, IssuanceHelper issuanceHelper) throws Exception{

        // Step 1. Login with pseudonym.
        System.out.println(">> Login with pseudonym.");
        this.loginWithPseudonym(universityInjector, userInjector,
                issuanceHelper);
        // Works until here
        // Step 1. Get university credential.
        System.out.println(">> Get university credential.");
        this.issueAndStoreUniversityCredential(universityInjector,
                userInjector, issuanceHelper);
        // this also seems to be working
        // Step 2. Get course credential.
        System.out.println(">> Get course credential.");
        this.issueAndStoreCourseCredential(universityInjector, userInjector,
                issuanceHelper);
        //also seems ok
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
        System.out.println("test, logingwithPseudonym, pre token generation");
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(universityInjector, userInjector,
                        PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN,
                        new PolicySelector(presentationTokenChoice,
                                chosenInspectors, pseudonymChoice));
        System.out.println("test, logingwithPseudonym, now we verify");
        return issuanceHelper.verify(universityInjector, p.second(), p.first());
    }

    private Secret getIdemixSecret()
            throws JAXBException, UnsupportedEncodingException, SAXException {
        Secret secret = (Secret) XmlUtils.getObjectFromXML(
                this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"),
                        true);
        return secret;
    }

    private Secret getUProveSecret()
            throws JAXBException, UnsupportedEncodingException, SAXException {
        Secret secret = (Secret) XmlUtils.getObjectFromXML(
                this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/patras/uprove-secret.xml"),
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

    private PseudonymWithMetadata getUProvePseudonym(URI secretUid,
            Injector userInjector) throws Exception {
        PseudonymWithMetadata pwm = (PseudonymWithMetadata) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/patras/uprove-pseudonym.xml"),
                                true);
        return pwm;
    }

    private SystemParameters getUProveSystemParameters(
            Injector universityInjector) throws Exception {
        SystemParameters sysParams = (SystemParameters) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                //                                "/eu/abc4trust/sampleXml/patras/uprove-systemParameters.xml"),
                                "/eu/abc4trust/systemparameters/bridged-systemParameters.xml"),
                                true);
        return sysParams;
    }

    private PseudonymWithMetadata getIdemixPseudonym(URI secretUid,
            IdemixCryptoEngineUserImpl idemixUser,
            SystemParameters systemParameters) {
        String scope = "urn:patras:registration";
        try {
            IdemixCryptoEngineUserImpl.loadIdemixSystemParameters(systemParameters);
        } catch (CryptoEngineException ex) {
            ex.printStackTrace();
            fail(ex.getLocalizedMessage());
        }
        PseudonymWithMetadata pwm = idemixUser.createPseudonym(
                URI.create("patrasdemo-idemix-uri"), scope, true, secretUid);
        return pwm;
    }

    private Map<String, Object> populateCourseAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
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

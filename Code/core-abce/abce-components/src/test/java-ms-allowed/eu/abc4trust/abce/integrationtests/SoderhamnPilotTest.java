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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.CredentialBases;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSASignatureSystemTest;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Soderhamn scenario.
 */
public class SoderhamnPilotTest {

    private static final String URN_ABC4TRUST_1_0_ALGORITHM_UPROVE =
            "urn:abc4trust:1.0:algorithm:uprove";

    private static final String URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX =
            "urn:abc4trust:1.0:algorithm:idemix";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnChild.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_CHILD =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnChild.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnClass.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_CLASS =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnClass.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnGuardian.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_GUARDIAN =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnGuardian.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnRole.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_ROLE =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnRole.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchoolWithRevocation.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml";

    private static final String CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT =
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubjectWithRevocation.xml";
    private static final String ISSUANCE_POLICY_SODERHAMN_SUBJECT =
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSubject.xml";

    private static final String PRESENTATION_POLICY_SODERHAMN_SCHOOL =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml";

    private static final String PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeFrench.xml";

    private static final String PRESENTATION_POLICY_RA_SUJECT_MUST_BE_ENGLISH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeEnglish.xml";

    private static final String PRESENTATION_POLICY_RA_GENDER_MUST_BE_FEMALE_SUJECT_MUST_BE_FRENCH =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRAGenderMustBeFemaleSubjectMustBeFrench.xml";

    private static final String PRESENTATION_POLICY_NUMBER_GENDER_FAIL =
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicyNumberGenderFail.xml";

    @Test
    public void soderhamnPilotHappypathIdemixTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
        int keyLength = 1024; // TODO: define the security level & revocation
        URI cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);
        Random random = new Random(1231);
        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        Injector issuerInjector = Guice.createInjector(IntegrationModuleFactory.newModule(random, cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);
        // Generate system parameters.
        SystemParameters systemParameters =
                issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);
        SecretWrapper secretWrapper = new SecretWrapper(this.getIdemixSecret());

        this.runIdemixTest(cryptoEngine, keyLength, cryptoMechanism, issuerInjector, issuerEngine, revocationInjector,
                systemParameters, secretWrapper);
    }

    @Ignore
    @Test
    public void soderhamnPilotHappypathIdemixSoftwareSmartcardTest() throws Exception {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;

        // Disable non-device-bound secrets
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;
        int keyLength = 1024; // TODO: define the security level & revocation
        URI cryptoMechanism = new URI(URN_ABC4TRUST_1_0_ALGORITHM_IDEMIX);
        Random random = new Random(1231);
        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);
        Injector issuerInjector = Guice.createInjector(IntegrationModuleFactory.newModule(random, cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);
        // Generate system parameters.
        SystemParameters systemParameters =
                issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);
        // smartcard secret - with IDEMIX engine
        SecretWrapper secretWrapper = new SecretWrapper(cryptoEngine, random, systemParameters);

        this.runIdemixTest(cryptoEngine, keyLength, cryptoMechanism, issuerInjector, issuerEngine, revocationInjector,
                systemParameters, secretWrapper);
    }

    private void runIdemixTest(CryptoEngine cryptoEngine, int keyLength, URI cryptoMechanism,
            Injector issuerInjector, IssuerAbcEngine issuerEngine, Injector revocationInjector, SystemParameters systemParameters,
            SecretWrapper secretWrapper) throws URISyntaxException, CredentialManagerException,
            KeyManagerException, JAXBException, UnsupportedEncodingException, SAXException, Exception {
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);
        // Create URIs.
        Injector userInjector =
                Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));

        IdemixCryptoEngineUserImpl userEngine =
                userInjector.getInstance(IdemixCryptoEngineUserImpl.class);

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);
        if (!secretWrapper.useSoftwareSmartcard) {
            userCredentialManager.storeSecret(secretWrapper.secret);
        }

        PseudonymWithMetadata pwm =
                this.getIdemixPseudonym(secretWrapper.getSecretUID(), userEngine, systemParameters);
        Injector verifierInjector =
                Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), cryptoEngine, UProveUtils.UPROVE_COMMON_PORT, revocationProxyAuthority));
        this.runTest(verifierInjector, keyLength, cryptoMechanism, pwm, secretWrapper,
                systemParameters, issuerInjector, userInjector, revocationInjector);
    }

    @Test
    //@Ignore
    public void soderhamnPilotHappypathUProveTest() throws Exception {
        // Create URIs.
        int keyLength = 2048; // TODO: define the security level & revocation
        URI cryptoMechanism = URI.create(URN_ABC4TRUST_1_0_ALGORITHM_UPROVE);
        UProveUtils uproveUtils = new UProveUtils();

        Injector revocationInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231),
                        CryptoEngine.UPROVE, UProveUtils.UPROVE_COMMON_PORT));
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        SecretWrapper secretWrapper = new SecretWrapper(this.getUProveSecret());
        Injector userInjector =
                Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), uproveUtils.getUserServicePort(), revocationProxyAuthority));

        PseudonymWithMetadata pwm = this.getUProvePseudonym(secretWrapper.getSecretUID(), userInjector);

        Injector issuerInjector =
                Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), uproveUtils.getIssuerServicePort(), revocationProxyAuthority));
        SystemParameters systemParameters = this.getUProveSystemParameters(issuerInjector);
        Injector verifierInjector =
                Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231), uproveUtils.getVerifierServicePort(), revocationProxyAuthority));

        this.runTest(verifierInjector, keyLength, cryptoMechanism, pwm, secretWrapper,
                systemParameters, issuerInjector, userInjector, revocationInjector);
//        // System.out.println("Test done");
//        int exitCode = userInjector.getInstance(UProveBindingManager.class).stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = issuerInjector.getInstance(UProveBindingManager.class).stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        exitCode = verifierInjector.getInstance(UProveBindingManager.class).stop();
//        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }

    private void runTest(Injector verifierInjector, int keyLength, URI cryptoMechanism,
            PseudonymWithMetadata pwm, SecretWrapper secretWrapper, SystemParameters systemParameters,
            Injector issuerInjector, Injector userInjector, Injector revocationInjector) throws KeyManagerException, JAXBException,
            UnsupportedEncodingException, SAXException, URISyntaxException, Exception,
            CredentialManagerException {

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector.getInstance(KeyManager.class);


        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        // Generate revocation parameters.
        RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
        URI revParamsUid = IntegrationTestUtil.REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(
                URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        cryptoMechanism, revParamsUid, revocationInfoReference,
                        nonRevocationEvidenceReference, nonRrevocationUpdateReference);

        // Setup issuance policies.
        IssuancePolicy childIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_CHILD), true);
        IssuancePolicy classIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_CLASS), true);
        IssuancePolicy guradianIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_GUARDIAN), true);
        IssuancePolicy roleIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_ROLE), true);
        IssuancePolicy schoolIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_SCHOOL), true);
        IssuancePolicy subjectIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_SODERHAMN_SUBJECT), true);

        URI childIssuancePolicyUid =
                childIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        URI classIssuancePolicyUid =
                classIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        URI guardianIssuancePolicyUid =
                guradianIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        URI roleIssuancePolicyUid = roleIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        URI schoolIssuancePolicyUid =
                schoolIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        URI subjectIssuancePolicyUid =
                subjectIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();

        // Load credential specifications.
        CredentialSpecification childCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD), true);
        CredentialSpecification classCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS), true);
        CredentialSpecification guardianCredSpec =
                (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN),
                        true);
        CredentialSpecification roleCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE), true);
        CredentialSpecification schoolCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL), true);
        CredentialSpecification subjectCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT), true);


        // Store credential specifications.
        URI childSpecificationUID = childCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(childSpecificationUID, childCredSpec);
        userKeyManager.storeCredentialSpecification(childSpecificationUID, childCredSpec);
        verifierKeyManager.storeCredentialSpecification(childSpecificationUID, childCredSpec);

        URI classSpecificationUID = classCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(classSpecificationUID, classCredSpec);
        userKeyManager.storeCredentialSpecification(classSpecificationUID, classCredSpec);
        verifierKeyManager.storeCredentialSpecification(classSpecificationUID, classCredSpec);

        URI guardianSpecificationUID = guardianCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(guardianSpecificationUID, guardianCredSpec);
        userKeyManager.storeCredentialSpecification(guardianSpecificationUID, guardianCredSpec);
        verifierKeyManager.storeCredentialSpecification(guardianSpecificationUID, guardianCredSpec);

        URI roleSpecificationUID = roleCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(roleSpecificationUID, roleCredSpec);
        userKeyManager.storeCredentialSpecification(roleSpecificationUID, roleCredSpec);
        verifierKeyManager.storeCredentialSpecification(roleSpecificationUID, roleCredSpec);

        URI schoolSpecificationUID = schoolCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(schoolSpecificationUID, schoolCredSpec);
        userKeyManager.storeCredentialSpecification(schoolSpecificationUID, schoolCredSpec);
        verifierKeyManager.storeCredentialSpecification(schoolSpecificationUID, schoolCredSpec);

        URI subjectSpecificationUID = subjectCredSpec.getSpecificationUID();
        issuerKeyManager.storeCredentialSpecification(subjectSpecificationUID, subjectCredSpec);
        userKeyManager.storeCredentialSpecification(subjectSpecificationUID, subjectCredSpec);
        verifierKeyManager.storeCredentialSpecification(subjectSpecificationUID, subjectCredSpec);

        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        // URI revocationId = new URI("revocationUID1");

        IssuerParameters childIssuerParameters =
                issuerEngine.setupIssuerParameters(childCredSpec, systemParameters, childIssuancePolicyUid,
                        hash, URI.create("uprove"),
                        revParamsUid, null);

        IssuerParameters classIssuerParameters =
                issuerEngine.setupIssuerParameters(classCredSpec, systemParameters, classIssuancePolicyUid,
                        hash, URI.create("uprove"),
                        revParamsUid, null);

        IssuerParameters guardianIssuerParameters =
                issuerEngine.setupIssuerParameters(guardianCredSpec, systemParameters,
                        guardianIssuancePolicyUid, hash, URI.create("uprove"),
                        revParamsUid, null);

        IssuerParameters roleIssuerParameters =
                issuerEngine.setupIssuerParameters(roleCredSpec, systemParameters, roleIssuancePolicyUid,
                        hash, URI.create("uprove"),
                        revParamsUid, null);

        IssuerParameters schoolIssuerParameters =
                issuerEngine.setupIssuerParameters(schoolCredSpec, systemParameters,
                        schoolIssuancePolicyUid, hash, URI.create("uprove"),
                        revParamsUid, null);

        IssuerParameters subjectIssuerParameters =
                issuerEngine.setupIssuerParameters(subjectCredSpec, systemParameters,
                        subjectIssuancePolicyUid, hash, URI.create("uprove"),
                        revParamsUid, null);

        //
        issuerKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        verifierKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);

        //
        issuerKeyManager.storeIssuerParameters(childIssuancePolicyUid, childIssuerParameters);
        userKeyManager.storeIssuerParameters(childIssuancePolicyUid, childIssuerParameters);
        verifierKeyManager.storeIssuerParameters(childIssuancePolicyUid, childIssuerParameters);

        issuerKeyManager.storeIssuerParameters(classIssuancePolicyUid, classIssuerParameters);
        userKeyManager.storeIssuerParameters(classIssuancePolicyUid, classIssuerParameters);
        verifierKeyManager.storeIssuerParameters(classIssuancePolicyUid, classIssuerParameters);

        issuerKeyManager.storeIssuerParameters(guardianIssuancePolicyUid, guardianIssuerParameters);
        userKeyManager.storeIssuerParameters(guardianIssuancePolicyUid, guardianIssuerParameters);
        verifierKeyManager.storeIssuerParameters(guardianIssuancePolicyUid, guardianIssuerParameters);

        issuerKeyManager.storeIssuerParameters(roleIssuancePolicyUid, roleIssuerParameters);
        userKeyManager.storeIssuerParameters(roleIssuancePolicyUid, roleIssuerParameters);
        verifierKeyManager.storeIssuerParameters(roleIssuancePolicyUid, roleIssuerParameters);

        issuerKeyManager.storeIssuerParameters(schoolIssuancePolicyUid, schoolIssuerParameters);
        userKeyManager.storeIssuerParameters(schoolIssuancePolicyUid, schoolIssuerParameters);
        verifierKeyManager.storeIssuerParameters(schoolIssuancePolicyUid, schoolIssuerParameters);

        issuerKeyManager.storeIssuerParameters(subjectIssuancePolicyUid, subjectIssuerParameters);
        userKeyManager.storeIssuerParameters(subjectIssuancePolicyUid, subjectIssuerParameters);
        verifierKeyManager.storeIssuerParameters(subjectIssuancePolicyUid, subjectIssuerParameters);

        // Load secret and store it.
        CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);

        if (secretWrapper.useSoftwareSmartcard) {
            // add smartcard to manager
            CardStorage cardStorage = userInjector.getInstance(CardStorage.class);
            cardStorage.addSmartcard(secretWrapper.softwareSmartcard, secretWrapper.pin);

            // sign issuer attributes and add to smartcard
            secretWrapper.addIssuerParameters(childIssuerParameters);
            secretWrapper.addIssuerParameters(classIssuerParameters);
            secretWrapper.addIssuerParameters(guardianIssuerParameters);
            secretWrapper.addIssuerParameters(roleIssuerParameters);
            secretWrapper.addIssuerParameters(schoolIssuerParameters);
            secretWrapper.addIssuerParameters(subjectIssuerParameters);
        } else {
            userCredentialManager.storeSecret(secretWrapper.secret);
            // URI secretUid = secret.getSecretDescription().getSecretUID();
        }

        // Step 0. Create a pseudonym and store it in the user credential
        // manager.
        userCredentialManager.storePseudonym(pwm);

        // This is a hack since the TokenManagerIssuer does not allow us to add
        // a pseudonym.
        TokenStorageIssuer universityTokenStorageManager =
                issuerInjector.getInstance(TokenStorageIssuer.class);
        String primaryKey = DatatypeConverter.printBase64Binary(pwm.getPseudonym().getPseudonymValue());

        universityTokenStorageManager.addPseudonymPrimaryKey(primaryKey);

        // Step 1. Login with pseudonym.
        System.out.println(">> Login with pseudonym.");
        this.loginWithPseudonym(issuerInjector, userInjector, issuanceHelper);

        // Step 2a. Get child credential.
        System.out.println(">> Get child credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_CHILD, ISSUANCE_POLICY_SODERHAMN_CHILD,
                this.populateChildAttributes());

        // Step 2b. Get class credential.
        System.out.println(">> Get class credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_CLASS, ISSUANCE_POLICY_SODERHAMN_CLASS,
                this.populateClassAttributes());

        // Step 2c. Get Guardian credential.
        System.out.println(">> Get guardian credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_GUARDIAN, ISSUANCE_POLICY_SODERHAMN_GUARDIAN,
                this.populateGuardianAttributes());

        // Step 2d. Get Role credential.
        System.out.println(">> Get Role credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_ROLE, ISSUANCE_POLICY_SODERHAMN_ROLE,
                this.populateRoleAttributes());

        // Step 2e. Get School credential.
        System.out.println(">> Get school credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_SCHOOL, ISSUANCE_POLICY_SODERHAMN_SCHOOL,
                this.populateSchoolAttributes());

        // Step 2f. Get Guardian credential.
        System.out.println(">> Get subject credential.");
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_SODERHAMN_SUBJECT, ISSUANCE_POLICY_SODERHAMN_SUBJECT,
                this.populateSubjectAttributes("French"));

        //CredentialManager userCredentialManager = userInjector.getInstance(CredentialManager.class);
        System.out.println("==== LIST Credentials ====");
        for(URI uri : userCredentialManager.listCredentials()) {
            Credential cred = userCredentialManager.getCredential(uri);
            CredentialDescription cd = cred.getCredentialDescription();
            System.out.println("- credential : " + cd.getCredentialUID() + " : " + cd.getAttribute());
            for(Attribute a : cd.getAttribute()) {
                System.out.println("-- attribute :  "  + a.getAttributeUID() + " : " + a.getAttributeValue());
            }
        }
        // The verifier needs to retrive the latest revocation information
        // in order to put in the UID in the presentation policy.
        RevocationInformation revocationInformation = revocationEngine
                .updateRevocationInformation(revParamsUid);


        // Step 3. Run presenations...
        System.out.println(">> Present 'subject' credential.");

        System.out.println(">> - we have 'French'.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH, true, 0, 0);

        System.out.println(">> - we have 'French' - login by credential.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH, true, 0, 0);

        // pseudonym established...
        System.out.println(">> - we have 'French' - log in by pseudonym.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_FRENCH, true, 0, 0);

        // policy cannot be satisfied...
        System.out.println(">> - we do NOT have 'English'.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_RA_SUJECT_MUST_BE_ENGLISH, false, 0, 0);

        // policy satisfied...
        System.out.println(">> - we have 'female' and 'French'.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_RA_GENDER_MUST_BE_FEMALE_SUJECT_MUST_BE_FRENCH, true, 0, 0);

        // policy satisfied...
        System.out.println(">> - we have female.");
        this.runPresentation(issuanceHelper, verifierInjector, userInjector, revocationInformation, PRESENTATION_POLICY_NUMBER_GENDER_FAIL, true, 0, 0);


    }

    private void loginWithPseudonym(Injector universityInjector, Injector userInjector,
            IssuanceHelper issuanceHelper) throws Exception {
        int presentationTokenChoice = 0;
        int pseudonymChoice = 0;
        PresentationToken t =
                this.loginWithPseudonym(issuanceHelper, universityInjector, userInjector,
                        PRESENTATION_POLICY_SODERHAMN_SCHOOL, presentationTokenChoice, pseudonymChoice);
        assertNotNull(t);
    }

    private PresentationToken loginWithPseudonym(IssuanceHelper issuanceHelper,
            Injector universityInjector, Injector userInjector, String policyResource,
            int presentationTokenChoice, int pseudonymChoice) throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        // chosenInspectors.add(URI
        // .create("http://patras.gr/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> p =
                issuanceHelper.createPresentationToken(universityInjector, userInjector, policyResource,
                        new PolicySelector(presentationTokenChoice, chosenInspectors, pseudonymChoice));

        return issuanceHelper.verify(universityInjector, p.second(), p.first());
    }

    private void runPresentation(
            IssuanceHelper issuanceHelper, Injector verifierInjector, Injector userInjector, RevocationInformation revocationInformation,
            String policyResource, boolean exprctSatisfied, int presentationTokenChoice, int pseudonymChoice) throws Exception {
        List<URI> chosenInspectors = new LinkedList<URI>();
        //     chosenInspectors.add(URI
        //     .create("http://patras.gr/inspector/pub_key_v1"));
        PolicySelector policySelection =
                new PolicySelector(presentationTokenChoice, chosenInspectors, pseudonymChoice);

        if(exprctSatisfied) {
            Pair<PresentationToken, PresentationPolicyAlternatives> p =
                    issuanceHelper.createPresentationToken(verifierInjector, userInjector, policyResource, revocationInformation,
                            policySelection);
            System.out.println("Policy expected to be satisfied : " + p);
            assertNotNull("Policy expected to be satisfied", p);
            issuanceHelper.verify(verifierInjector, p.second(), p.first());
        } else {
            Pair<PresentationToken, PresentationPolicyAlternatives> p =
                    issuanceHelper.createPresentationToken_NotSatisfied(verifierInjector, userInjector, revocationInformation,
                            policyResource, policySelection);
            System.out.println("Policy NOT expected to be satisfied : " + p);
            assertNull("Policy NOT expected to be satisfied", p);
        }
    }

    private Secret getIdemixSecret() throws JAXBException, UnsupportedEncodingException, SAXException {
        Secret secret =
                (Secret) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        return secret;
    }

    private Secret getUProveSecret() throws JAXBException, UnsupportedEncodingException, SAXException {
        Secret secret =
                (Secret) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/patras/uprove-secret.xml"), true);
        return secret;
    }

    private PseudonymWithMetadata getUProvePseudonym(URI secretUid, Injector userInjector)
            throws Exception {
        PseudonymWithMetadata pwm =
                (PseudonymWithMetadata) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/patras/uprove-pseudonym.xml"), true);
        pwm.getPseudonym().setPseudonymUID(URI.create("urn:soderhamn:registration"));
        return pwm;
    }

    private SystemParameters getUProveSystemParameters(Injector universityInjector) throws Exception {
        SystemParameters sysParams =
                (SystemParameters) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/patras/uprove-systemParameters.xml"), true);
        return sysParams;
    }

    private PseudonymWithMetadata getIdemixPseudonym(URI secretUid,
            IdemixCryptoEngineUserImpl idemixUser, SystemParameters systemParameters) {
        String scope = "urn:soderhamn:registration";
        try {
            IdemixCryptoEngineUserImpl.loadIdemixSystemParameters(systemParameters);
        } catch (CryptoEngineException ex) {
            ex.printStackTrace();
            fail(ex.getLocalizedMessage());
        }
        PseudonymWithMetadata pwm =
                idemixUser.createPseudonym(URI.create("soderhamndemo-idemix-uri"), scope, true, secretUid);
        return pwm;
    }

    private Map<String, Object> populateChildAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credChild:child", "000501-2345");
        //        att.put(REVOCATION_HANDLE_STR,
        //                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }

    private Map<String, Object> populateClassAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        //        att.put("urn:soderhamn:credspec:credClass:class", "class");

        att.put("urn:soderhamn:credspec:credClass:classNumber", 7);
        att.put("urn:soderhamn:credspec:credClass:classGroup","classGroup");
        att.put("urn:soderhamn:credspec:credClass:classYear", 2012);

        //        att.put(REVOCATION_HANDLE_STR,
        //                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }

    private Map<String, Object> populateGuardianAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credGuardian:guardian", "guardian");
        att.put(REVOCATION_HANDLE_STR,
                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }

    private Map<String, Object> populateRoleAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        //        att.put("urn:soderhamn:credspec:credRole:role", "role");
        att.put("urn:soderhamn:credspec:credRole:pupil", true);
        att.put("urn:soderhamn:credspec:credRole:nurse", false);
        att.put("urn:soderhamn:credspec:credRole:teacher", false);
        att.put("urn:soderhamn:credspec:credRole:guardian", false);
        att.put("urn:soderhamn:credspec:credRole:role1", false);
        att.put("urn:soderhamn:credspec:credRole:role2", false);
        att.put("urn:soderhamn:credspec:credRole:role3", false);
        att.put("urn:soderhamn:credspec:credRole:role4", false);
        att.put("urn:soderhamn:credspec:credRole:role5", false);

        //        att.put(REVOCATION_HANDLE_STR,
        //                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }

    private Map<String, Object> populateSchoolAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:soderhamn:credspec:credSchool:firstname", "Emily");
        att.put("urn:soderhamn:credspec:credSchool:lastname", "von Katthult Svensson");
        att.put("urn:soderhamn:credspec:credSchool:civicRegistrationNumber", "000501-2345");
        att.put("urn:soderhamn:credspec:credSchool:gender", "female");
        att.put("urn:soderhamn:credspec:credSchool:schoolname", "L\u00f6nneberga");

        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 10);
        SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
        String dateValue = xmlDateFormat.format(cal.getTime());
        att.put("urn:soderhamn:credspec:credSchool:birthdate", dateValue);

        //        att.put(REVOCATION_HANDLE_STR,
        //                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }

    private Map<String, Object> populateSubjectAttributes(String subject) {
        Map<String, Object> att = new HashMap<String, Object>();
        //        att.put("urn:soderhamn:credspec:credSubject:subject", subject);

        att.put("urn:soderhamn:credspec:credSubject:maths" , "maths".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:physics" , "physics".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:English" , "English".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:French" , "French".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject1" , "subject1".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject2" , "subject2".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject3" , "subject3".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject4" , "subject4".equals(subject));
        att.put("urn:soderhamn:credspec:credSubject:subject5" , "subject5".equals(subject));

        //        att.put("urn:soderhamn:credspec:credSubject:subjectprime", 1);
        //        att.put(REVOCATION_HANDLE_STR,
        //                URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        return att;
    }


    private class SecretWrapper {

        private final boolean useSoftwareSmartcard;

        //
        RSAKeyPair sk_root = RSASignatureSystemTest.getSigningKeyForTest();
        @SuppressWarnings("unused")
        RSAVerificationKey pk_root = RSASignatureSystem.getVerificationKey(this.sk_root);
        SoftwareSmartcard softwareSmartcard;

        int pin = 1234;
        @SuppressWarnings("unused")
        int puk;

        Secret secret;


        public SecretWrapper(Secret secret) {
            this.useSoftwareSmartcard = false;
            this.secret = secret;
        }

        public SecretWrapper(CryptoEngine cryptoEngine, Random random, SystemParameters systemParameters) {
            this.useSoftwareSmartcard = true;

            URI deviceUri = URI.create("secret://software-smartcard-" + random.nextInt(9999999));
            short deviceID = 2;

            GroupParameters groupParameters = (GroupParameters) systemParameters.getAny().get(1);

            SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();

            BigInteger p = groupParameters.getCapGamma();
            BigInteger g = groupParameters.getG();
            BigInteger subgroupOrder = groupParameters.getRho();
            int zkChallengeSizeBytes = 256 / 8;
            int zkStatisticalHidingSizeBytes = 80 / 8;
            int deviceSecretSizeBytes = 256 / 8;
            int signatureNonceLengthBytes = 128 / 8;
            int zkNonceSizeBytes = 256 / 8;
            int zkNonceOpeningSizeBytes = 256 / 8;

            scSysParams.setPrimeModulus(p);
            scSysParams.setGenerator(g);
            scSysParams.setSubgroupOrder(subgroupOrder);
            scSysParams.setZkChallengeSizeBytes(zkChallengeSizeBytes);
            scSysParams.setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
            scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
            scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
            scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
            scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);

            eu.abc4trust.smartcard.SystemParameters sc_sysParams =
                    new eu.abc4trust.smartcard.SystemParameters(scSysParams);

            this.softwareSmartcard = new SoftwareSmartcard(random);
            this.puk = this.softwareSmartcard.init(this.pin, sc_sysParams, this.sk_root, deviceID);
            SmartcardBlob blob = new SmartcardBlob();
            try {
                blob.blob = deviceUri.toASCIIString().getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
            }
            this.softwareSmartcard.storeBlob(this.pin, Smartcard.device_name, blob);
            System.out.println("SoftwareSmartcard is now init'ed " + this.softwareSmartcard);

        }

        @SuppressWarnings("unused")
        public boolean isSecretOnSmartcard() {
            return this.useSoftwareSmartcard;
        }

        public URI getSecretUID() {
            if (this.useSoftwareSmartcard) {
                return this.softwareSmartcard.getDeviceURI(this.pin);
            } else {
                return this.secret.getSecretDescription().getSecretUID();
            }
        }

        public void addIssuerParameters(IssuerParameters issuerParameters) {
            if (this.useSoftwareSmartcard) {
                IssuerPublicKey isPK =
                        (IssuerPublicKey) Parser.getInstance().parse(
                                (Element) issuerParameters.getCryptoParams().getAny().get(0));
                //
                BigInteger R0 = isPK.getCapR()[0];
                BigInteger S = isPK.getCapS();
                BigInteger n = isPK.getN();
                CredentialBases credBases = new CredentialBases(R0, S, n);

                this.softwareSmartcard.getNewNonceForSignature();
                URI parametersUri = issuerParameters.getParametersUID();

                SmartcardStatusCode universityResult =
                        this.softwareSmartcard.addIssuerParameters(this.sk_root, parametersUri, credBases);
                if (universityResult != SmartcardStatusCode.OK) {
                    throw new RuntimeException("Could not add IssuerParams to smartcard... "
                            + universityResult);
                }

            }
        }
    }

}

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

package eu.abc4trust.abce.integrationtests.idemix;

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_STR;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.smartcard.CredentialBases;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSASignatureSystemTest;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.util.TemporaryFileFactory;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Test using (Software)Smartcards....
 * 
 * @author hgk
 */
public class SoftwareSmartcardTest {

    private Module createProductionModule() {
        AbceConfigurationImpl abceConfiguration = new AbceConfigurationImpl();
        abceConfiguration.setKeyStorageFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setCredentialFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setPseudonymsFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setTokensFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setSecretStorageFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setPrng(new Random(1985));
        abceConfiguration.setDefaultImagePath("default.jpg");
        abceConfiguration.setIssuerSecretKeyFile(TemporaryFileFactory.createTemporaryFile());
        abceConfiguration.setInspectorSecretKeyFile(TemporaryFileFactory.createTemporaryFile());

        abceConfiguration.setImageCacheDir(TemporaryFileFactory.createTemporaryFile());

        return ProductionModuleFactory.newModule(abceConfiguration, CryptoEngine.IDEMIX);
    }

    public static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN =
            "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml";
    public static final String CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY =
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml";
    public static final String ISSUANCE_POLICY_PATRAS_UNIVERSITY =
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml";

    public static final String CREDENTIAL_SPECIFICATION_PATRAS_COURSE =
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml";
    public static final String ISSUANCE_POLICY_PATRAS_COURSE =
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml";
    public static final String PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION =
            "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml";

    public static final int IDEMIX_KEY_LENGTH = 1024;
    public static final URI IDEMIX_CRYPTO_MECHANISM = URI
            .create("urn:abc4trust:1.0:algorithm:idemix");


    // - issuance policy for university - will have attribute pseudonym->established set to value of
    // PRE_ESTABLISH_PSEUDONYM
    // if true
    // - pseudonym object will be created and stored in local user credential manager..
    // - pseudonym value will be exchanged with issuer - who will call issuerStorageManager.addPseudonymPrimaryKey( val )
    // if false
    // - pseudonym object will be created on login to university / IDM
    public static final boolean PRE_ESTABLISH_PSEUDONYM = true;

    private static final boolean SKIP_ISSUANCE_OF_UNIVERSITY_CREDENTIAL_AS_THIS_IS_NOT_NEEDED_IN_FIRST_ROUND =
            false;

    @Ignore
    @Test
    public void patrasUniversityCredential() throws Exception {

        ObjectFactory of = new ObjectFactory();
        Random random42 = new Random(42);

        // 1 - basic setup

        // 1.a DependencyInjection
        // - user
        Module userModule = this.createProductionModule();
        Injector userInjector = Guice.createInjector(userModule);
        UserAbcEngine userEngine = userInjector.getInstance(UserAbcEngine.class);

        CredentialManager userCredManager = userInjector.getInstance(CredentialManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        AbcSmartcardManager smartcardManager = userInjector.getInstance(AbcSmartcardManager.class);

        // - issuer
        Module issuerModule = this.createProductionModule();
        Injector issuerInjector = Guice.createInjector(issuerModule);
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        TokenStorageIssuer issuerStorageManager = issuerInjector.getInstance(TokenStorageIssuer.class);

        // - verifier
        Module verifierModule = this.createProductionModule();
        Injector verifierInjector = Guice.createInjector(verifierModule);
        VerifierAbcEngine verifierEngine = verifierInjector.getInstance(VerifierAbcEngine.class);

        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);

        // 1.b read resources
        CredentialSpecification universityCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY), true);

        // store university cred spec
        if (!issuerKeyManager.storeCredentialSpecification(universityCredSpec.getSpecificationUID(),
                universityCredSpec)) {
            throw new RuntimeException("University Cred spec was not stored (issuer)");
        }

        if (!userKeyManager.storeCredentialSpecification(universityCredSpec.getSpecificationUID(),
                universityCredSpec)) {
            throw new RuntimeException("University Cred spec was not stored (user)");
        }

        CredentialSpecification courseCredSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_PATRAS_COURSE), true);

        // store university cred spec
        if (!issuerKeyManager.storeCredentialSpecification(courseCredSpec.getSpecificationUID(),
                courseCredSpec)) {
            throw new RuntimeException("Cousrse Cred spec was not stored (issuer)");
        }

        if (!userKeyManager.storeCredentialSpecification(courseCredSpec.getSpecificationUID(),
                courseCredSpec)) {
            throw new RuntimeException("Cousrse Cred spec was not stored (user)");
        }

        // load issuance policy
        IssuancePolicy universityIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_PATRAS_UNIVERSITY), true);

        IssuancePolicy courseIssuancePolicy =
                (IssuancePolicy) XmlUtils.getObjectFromXML(
                        this.getClass().getResourceAsStream(ISSUANCE_POLICY_PATRAS_COURSE), true);

        // get pseudonym : urn:patras:registration
        PseudonymInPolicy registrationPseudonym =
                universityIssuancePolicy.getPresentationPolicy().getPseudonym().get(0);


        //
        System.out.println(" registrationPseudonym - established : "
                + registrationPseudonym.isEstablished());

        // If Pseudonym is not established - it should be created on demand
        registrationPseudonym.setEstablished(PRE_ESTABLISH_PSEUDONYM);
        System.out.println(" registrationPseudonym - established : "
                + registrationPseudonym.isEstablished());


        // 2 - system and issuer params

        SystemParameters systemParameters =
                issuerEngine.setupSystemParameters(IDEMIX_KEY_LENGTH, IDEMIX_CRYPTO_MECHANISM);

        issuerKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        verifierKeyManager.storeSystemParameters(systemParameters);

        // - issuer
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");

        URI universityIssuerParametersUID =
                universityIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();

        URI universityRevocationParamsUid =
                URI.create(universityCredSpec.getSpecificationUID() + "/revocationUID");

        URI courseIssuerParametersUID =
                courseIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();

        URI courseRevocationParamsUid =
                URI.create(courseCredSpec.getSpecificationUID() + "/revocationUID");

        GroupParameters groupParameters = (GroupParameters) systemParameters.getAny().get(1);

        // - university - for issuer
        IssuerParameters universityIssuerParameters =
                issuerEngine.setupIssuerParameters(universityCredSpec, systemParameters,
                        universityIssuerParametersUID, hash, URI.create("Idemix"),
                        universityRevocationParamsUid, null);

        issuerKeyManager.storeIssuerParameters(universityIssuerParametersUID,
                universityIssuerParameters);
        userKeyManager.storeIssuerParameters(universityIssuerParametersUID, universityIssuerParameters);
        verifierKeyManager.storeIssuerParameters(universityIssuerParametersUID,
                universityIssuerParameters);

        // - course - for issuer
        IssuerParameters courseIssuerParameters =
                issuerEngine.setupIssuerParameters(courseCredSpec, systemParameters,
                        courseIssuerParametersUID, hash, URI.create("Idemix"), courseRevocationParamsUid, null);

        issuerKeyManager.storeIssuerParameters(courseIssuerParametersUID, courseIssuerParameters);
        userKeyManager.storeIssuerParameters(courseIssuerParametersUID, courseIssuerParameters);
        verifierKeyManager.storeIssuerParameters(courseIssuerParametersUID, courseIssuerParameters);

        //

        URI scope = URI.create("urn:patras:registration");

        // 3. Setup SoftwareSmartcard

        // Disable non-device-bound secrets
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;

        // 3.a Init Smartcard - Anjas Document
        RSAKeyPair sk_root = RSASignatureSystemTest.getSigningKeyForTest();
        @SuppressWarnings("unused")
        RSAVerificationKey pk_root = RSASignatureSystem.getVerificationKey(sk_root);

        int pin = 1234;
        @SuppressWarnings("unused")
        int puk; //set at init
        //byte[] macKeyForBackup = "BBBBBBBBBBBBBBBB".getBytes();
        URI deviceUri = URI.create("secret://software-smartcard-42");
        short deviceID = 42;


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


        SoftwareSmartcard softwareSmartcard = new SoftwareSmartcard(random42);
        puk = softwareSmartcard.init(pin, sc_sysParams, sk_root, deviceID);
        SmartcardBlob blob = new SmartcardBlob();
        blob.blob = deviceUri.toASCIIString().getBytes("US-ASCII");
        softwareSmartcard.storeBlob(pin, Smartcard.device_name, blob);
        System.out.println("SoftwareSmartcard is now init'ed " + softwareSmartcard);

        // 3b Sign IssuerParams - Anjas Document
        {
            IssuerPublicKey university_IsPK =
                    (IssuerPublicKey) Parser.getInstance().parse(
                            (Element) universityIssuerParameters.getCryptoParams().getAny().get(0));
            //
            BigInteger R0 = university_IsPK.getCapR()[0];
            BigInteger S = university_IsPK.getCapS();
            BigInteger n = university_IsPK.getN();
            CredentialBases universityCredBases = new CredentialBases(R0, S, n);

            @SuppressWarnings("unused")
            byte[] universityNonce = softwareSmartcard.getNewNonceForSignature();
            URI universityParametersUri = universityIssuerParameters.getParametersUID();


            SmartcardStatusCode universityResult =
                    softwareSmartcard.addIssuerParameters(sk_root, universityParametersUri,
                            universityCredBases);
            if (universityResult != SmartcardStatusCode.OK) {
                throw new RuntimeException("Could not add University IssuerParams to smartcard... "
                        + universityResult);
            }
        }
        {
            IssuerPublicKey course_IsPK =
                    (IssuerPublicKey) Parser.getInstance().parse(
                            (Element) courseIssuerParameters.getCryptoParams().getAny().get(0));
            //
            BigInteger R0 = course_IsPK.getCapR()[0];
            BigInteger S = course_IsPK.getCapS();
            BigInteger n = course_IsPK.getN();
            CredentialBases courseCredBases = new CredentialBases(R0, S, n);

            @SuppressWarnings("unused")
            byte[] courseNonce = softwareSmartcard.getNewNonceForSignature();
            URI courseParametersUri = courseIssuerParameters.getParametersUID();

            SmartcardStatusCode courseResult =
                    softwareSmartcard.addIssuerParameters(sk_root, courseParametersUri,
                            courseCredBases);
            if (courseResult != SmartcardStatusCode.OK) {
                throw new RuntimeException("Could not add Course IssuerParams to smartcard... "
                        + courseResult);
            }
        }

        System.out.println("SoftwareSmartcard now has signed IssuerParams");
        //

        // 3.c - add smartcard to SmartcardManager
        @SuppressWarnings("deprecation")
        boolean added = smartcardManager.addSmartcard(softwareSmartcard, pin);
        assertTrue("Smartcard was not added ??", added);


        // 3d : preEstablishPseudonym
        // - generate
        BigInteger smartcard_scopeExclusivePseudonym =
                softwareSmartcard.computeScopeExclusivePseudonym(pin, scope);

        String smartcard_scopeExclusivePseudonym_bytes_b64 =
                DatatypeConverter.printBase64Binary(smartcard_scopeExclusivePseudonym.toByteArray());
        System.out.println("SoftwareSmartcard scopeExclusivePseudonym : " + scope + " : "
                + smartcard_scopeExclusivePseudonym + " : " + smartcard_scopeExclusivePseudonym_bytes_b64);

        if (PRE_ESTABLISH_PSEUDONYM) {
            // pseudonym value must be store on issuer side!
            System.out
            .println("registerSmartcardScopeExclusivePseudonym - on issuer- register new pseudonym");
            issuerStorageManager.addPseudonymPrimaryKey(smartcard_scopeExclusivePseudonym_bytes_b64);

            // generate pseudonym and store on user side
            System.out.println("Generate pseudonym and store on user side");
            byte[] pv = smartcard_scopeExclusivePseudonym.toByteArray();
            Pseudonym pseudonym = of.createPseudonym();
            URI secretUid = softwareSmartcard.getDeviceURI(1234);
            pseudonym.setSecretReference(secretUid);
            pseudonym.setExclusive(true);
            pseudonym.setPseudonymUID(URI.create("foo-bar-pseudonym-uid-42"));
            pseudonym.setPseudonymValue(pv);
            pseudonym.setScope(scope.toString());

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

            // Idemix ?
            URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");
            // StructureStore.getInstance().add(groupParameterId.toString(), groupParameters);

            System.out.println("GroupParametersID : " + groupParameters.getSystemParamsLocation());

            StoredDomainPseudonym dp = new StoredDomainPseudonym(scope, secretUid, groupParameterId);
            cryptoEvidence.getAny().add(XMLSerializer.getInstance().serializeAsElement(dp));
            pwm.setCryptoParams(cryptoEvidence);

            System.out.println("Pseudonym Created");

            //
            userCredManager.storePseudonym(pwm);
            System.out.println("Pseudonym Stored");
        }



        // 4. Test University / IDM
        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // 4.a Log in with Pseudonym...
        System.out.println("\n### Log in with Pseudonym");
        int universityPresentationTokenChoice = 0;
        int universityPseudonymChoice = 0;
        List<URI> universityChosenInspectors = new LinkedList<URI>();
        // chosenInspectors.add(URI.create("http://patras.gr/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> university_pair_pt_ppa =
                issuanceHelper.createPresentationToken(issuerInjector, userInjector,
                        PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN, new PolicySelector(
                                universityPresentationTokenChoice, universityChosenInspectors,
                                universityPseudonymChoice));

        // if Pseudonym Pre-established - verify that PseudonymValue is correct
        PresentationToken university_pt = university_pair_pt_ppa.first();
        PresentationPolicyAlternatives university_ppa = university_pair_pt_ppa.second();
        if (PRE_ESTABLISH_PSEUDONYM) {
            System.out.println("PT : " + XmlUtils.toXml(of.createPresentationToken(university_pt)));
            PseudonymInToken presentedPseudonym =
                    university_pt.getPresentationTokenDescription().getPseudonym().get(0);
            byte[] presentedPseudonymValue = presentedPseudonym.getPseudonymValue();
            BigInteger presentedPseudonymBigInteger = new BigInteger(presentedPseudonymValue);
            System.out.println("SoftwareSmartcard scopeExclusivePseudonym : " + scope + " : "
                    + smartcard_scopeExclusivePseudonym);
            System.out.println("SoftwareSmartcard scopeExclusivePseudonym : " + scope + " : "
                    + DatatypeConverter.printBase64Binary(smartcard_scopeExclusivePseudonym.toByteArray()));
            System.out.println();
            System.out.println("Presented scopeExclusivePseudonym         : "
                    + presentedPseudonym.getScope() + " : " + presentedPseudonymBigInteger);
            System.out.println("Presented scopeExclusivePseudonym         : "
                    + presentedPseudonym.getScope() + " : "
                    + DatatypeConverter.printBase64Binary(presentedPseudonymBigInteger.toByteArray()));

            boolean testOk = true;
            if (!smartcard_scopeExclusivePseudonym.equals(presentedPseudonymBigInteger)) {
                testOk = false;
                System.err.println("Presented Pseudonym was not from Smartcard Secret");

                System.out.println("# of Secrets " + userCredManager.listSecrets().size());
                List<SecretDescription> sdList = userCredManager.listSecrets();
                for (SecretDescription sd : sdList) {
                    URI secretUid = sd.getSecretUID();

                    BigInteger controlPseValue =
                            smartcardManager.computeScopeExclusivePseudonym(secretUid, scope);
                    System.out.println("Re Calculated scopeExclusivePseudonym     : " + secretUid + " : "
                            + controlPseValue);
                    System.out.println("Re Calculated scopeExclusivePseudonym     : " + secretUid + " : "
                            + DatatypeConverter.printBase64Binary(controlPseValue.toByteArray()));
                }
            }
            assertTrue("PseudonymValue's from Smartcard and PresentationToken do not macth", testOk);
        }

        // verify
        verifierEngine.verifyTokenAgainstPolicy(university_ppa, university_pt, true);

        // 4.b Run issuance of university credential...

        if (SKIP_ISSUANCE_OF_UNIVERSITY_CREDENTIAL_AS_THIS_IS_NOT_NEEDED_IN_FIRST_ROUND) {
            System.out
            .println("SKIP_ISSUANCE_OF_UNIVERSITY_CREDENTIAL_AS_THIS_IS_NOT_NEEDED_IN_FIRST_ROUND");
        } else {
            System.out.println("\n### Issue University Credential");
            List<Attribute> universityAttrs =
                    issuanceHelper.populateIssuerAttributes(this.univeristyPopulateAttributes(),
                            CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY);
            LocalPolicySelector universityIssuancePolicySelector = new LocalPolicySelector(0, 0);

            System.out.println("- userCredManager.listSecrets() : " + userCredManager.listSecrets());
            this.local_runIssuanceProtocol(issuerEngine, userEngine, universityIssuancePolicy,
                    universityAttrs, universityIssuancePolicySelector);
        }



        // 5. Test Course
        // 5.a Run issuance...

        System.out.println("\n### Issue Course Credential");
        List<Attribute> courseAttrs =
                issuanceHelper.populateIssuerAttributes(this.coursePopulateAttributes(),
                        CREDENTIAL_SPECIFICATION_PATRAS_COURSE);
        issuanceHelper.runIssuanceProtocol(issuerEngine, userEngine, courseIssuancePolicy, courseAttrs);


        // 5.b Run Presentation...
        int coursePresentationTokenChoice = 0;
        int coursePseudonymChoice = 0;
        List<URI> courseChosenInspectors = new LinkedList<URI>();
        IdentitySelection coursePresentationPolicySelector = new LocalPolicySelector(
                coursePresentationTokenChoice, courseChosenInspectors, coursePseudonymChoice);
        // chosenInspectors.add(URI.create("http://patras.gr/inspector/pub_key_v1"));
        Pair<PresentationToken, PresentationPolicyAlternatives> course_pair_pt_ppa =
                issuanceHelper.createPresentationToken(issuerInjector, userInjector,
                        PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION, coursePresentationPolicySelector);

        // verify
        PresentationToken course_pt = course_pair_pt_ppa.first();
        PresentationPolicyAlternatives course_ppa = course_pair_pt_ppa.second();

        verifierEngine.verifyTokenAgainstPolicy(course_ppa, course_pt, true);

        System.out.println("Credentials for Patras issued : " + userCredManager.listCredentials());
        System.out.println("Psdudonyms for Patras issued  : "
                + userCredManager.listPseudonyms(scope.toString(), true));
        System.out.println("Psdudonyms for Patras issued  : "
                + userCredManager.listPseudonyms(scope.toString(), true).get(0).getPseudonym().getScope());
        System.out.println("Psdudonyms for Patras issued  : "
                + userCredManager.listPseudonyms(scope.toString(), false));
        System.out.println("Psdudonyms for Patras issued  : "
                + userCredManager.listPseudonyms(scope.toString(), false).get(0).getPseudonym().getScope());


    }

    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String UNIVERSITYNAME = "Patras";
    private static final String DEPARTMENTNAME = "CS";
    private static final int MATRICULATIONNUMBER = 1235332;


    private Map<String, Object> univeristyPopulateAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:patras:credspec:credUniv:firstname", NAME);
        att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
        att.put(REVOCATION_HANDLE_STR,
                URI.create("urn:patras:revocation:handle1"));
        att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
        att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
        att.put("urn:patras:credspec:credUniv:matriculationnr", MATRICULATIONNUMBER);
        return att;
    }

    private static final String COURSE_UID = "23330E";

    private Map<String, Object> coursePopulateAttributes() {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
        att.put("urn:patras:credspec:credCourse:matriculationnr", MATRICULATIONNUMBER);
        att.put(REVOCATION_HANDLE_STR,
                URI.create("urn:patras:revocation:handle2"));
        return att;
    }


    public CredentialDescription local_runIssuanceProtocol(IssuerAbcEngine issuerEngine,
            UserAbcEngine userEngine, IssuancePolicy ip, List<Attribute> issuerAtts,
            IdentitySelection policySelector) throws Exception {
        // Issuer starts the issuance.
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        // ObjectFactory of = new ObjectFactory();
        // JAXBElement<?> actual = of.createIssuanceMessage(issuerIm.im);
        // System.out.println(XmlUtils.toNormalizedXML(actual));

        // Reply from user.
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(
                issuerIm.getIssuanceMessage(), policySelector);
        //     JAXBElement<?> actual = of.createIssuanceMessage(userIm.im);
        //     System.out.println("=========== Presentation From User : ");
        //     System.out.println(XmlUtils.toNormalizedXML(actual));
        while (!issuerIm.isLastMessage()) {
            assertNotNull(userIm.im);
            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            // for (Object o : issuerIm.im.getAny()) {
            // System.out.println(o.getClass());
            // CredentialDescription cd = (CredentialDescription) XmlUtils
            // .unwrap(o, CredentialDescription.class);
            // for (Attribute a : cd.getAttribute()) {
            // System.out.println(a.getAttributeDescription().getType()
            // + ", " + a.getAttributeValue());
            // }
            // }

            //       actual = of.createIssuanceMessage(issuerIm.im);
            //       System.out.println(XmlUtils.toNormalizedXML(actual));

            assertNotNull(issuerIm.getIssuanceMessage());
            userIm = userEngine.issuanceProtocolStep(issuerIm
                    .getIssuanceMessage());

            // if (userIm.im != null) {
            // actual = of.createIssuanceMessage(userIm.im);
            // System.out.println(XmlUtils.toNormalizedXML(actual));
            // }
            boolean userLastMessage = (userIm.cd != null);
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        assertNull(userIm.im);
        assertNotNull(userIm.cd);
        return userIm.cd;
    }

    public class LocalPolicySelector implements IdentitySelection {

        private final int selectedPolicyNumber;
        List<URI> chosenInspectors;
        private final int selectedPseudonymNumber;

        public LocalPolicySelector(int selectedPolicyNumber, int selectedPseudonymNumber) {
            this.selectedPolicyNumber = selectedPolicyNumber;
            this.chosenInspectors = new LinkedList<URI>();
            this.selectedPseudonymNumber = selectedPseudonymNumber;
        }

        public LocalPolicySelector(int selectedPolicyNumber, List<URI> chosenInspectors,
                int selectedPseudonymNumber) {
            this.selectedPolicyNumber = selectedPolicyNumber;
            this.chosenInspectors = chosenInspectors;
            this.selectedPseudonymNumber = selectedPseudonymNumber;
        }

        public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
                Map<URI, CredentialDescription> credentialDescriptions,
                Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
                List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids,
                List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice) {
            throw new UnsupportedOperationException();


            /*
             * java.util.logging.Logger.getLogger("LOG TIHS").log(java.util.logging
             * .Level.WARNING, "XXX");
             * System.out.println("### selectPresentationTokenDescription");
             * System.out.println("*** - policies               : " +
             * policies.size());
             * System.out.println("*** - credentialDescriptions : " +
             * credentialDescriptions.size());
             * System.out.println("*** - pseudonyms             : " +
             * pseudonyms.size());
             * System.out.println("*** - inspectors             : " +
             * inspectors.size());
             * System.out.println("### - tokens                 : " +
             * tokens.size());
             * System.out.println("*** - credentialUids         : " +
             * credentialUids.size());
             * System.out.println("*** - pseudonymChoice        : " +
             * pseudonymChoice.size());
             * System.out.println("*** - inspectorChoice        : " +
             * inspectorChoice.size());
             * 
             * 
             * Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI,
             * PseudonymMetadata>();
             * 
             * List<URI> chosenPseudonyms = null; //
             * System.out.println(pseudonymChoice); Set<List<URI>>
             * pseudonymChoices =
             * pseudonymChoice.get(this.selectedPolicyNumber); for (int inx = 0;
             * inx < (this.selectedPseudonymNumber + 1); inx++) {
             * chosenPseudonyms = pseudonymChoices.iterator().next(); } //
             * System.out.println(chosenPseudonyms); if
             * (this.chosenInspectors.isEmpty()) { for (List<Set<URI>> uris :
             * inspectorChoice) { for (Set<URI> uriset : uris) {
             * this.chosenInspectors.addAll(uriset); } } } //
             * chosenInspectors.addAll
             * (inspectorChoice.get(0).get(0).iterator());
             * 
             * SptdReturn r = new SptdReturn(this.selectedPolicyNumber,
             * metaDataToChange, chosenPseudonyms, this.chosenInspectors);
             * return r;
             */
        }

        @Override
        public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
                Map<URI, CredentialDescription> credentialDescriptions,
                Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
                List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
                List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
                List<List<Set<URI>>> inspectorChoice) {

            java.util.logging.Logger.getLogger("LOG TIHS").log(java.util.logging.Level.WARNING, "XXX");
            System.out.println("### selectIssuanceTokenDescription");
            System.out.println("*** - policies               : " + policies.size());
            System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
            System.out.println("*** - pseudonyms             : " + pseudonyms.size());
            System.out.println("*** - inspectors             : " + inspectors.size());
            System.out.println("### - tokens                 : " + tokens.size());
            System.out.println("*** - credentialUids         : " + credentialUids.size());
            System.out.println("*** - selfClaimedAttributes  : " + selfClaimedAttributes.size());
            System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
            System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());


            Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI, PseudonymMetadata>();

            List<URI> chosenPseudonyms = null;
            // System.out.println(pseudonymChoice);
            Set<List<URI>> pseudonymChoices = pseudonymChoice.get(this.selectedPolicyNumber);
            for (int inx = 0; inx < (this.selectedPseudonymNumber + 1); inx++) {
                chosenPseudonyms = pseudonymChoices.iterator().next();
                System.out.println("Choose Pseudonym : " + inx + " : " + chosenPseudonyms);
            }
            System.out.println("Choose Pseudonym : " + chosenPseudonyms);
            // System.out.println(chosenPseudonyms);
            if (this.chosenInspectors.isEmpty()) {
                for (List<Set<URI>> uris : inspectorChoice) {
                    for (Set<URI> uriset : uris) {
                        this.chosenInspectors.addAll(uriset);
                    }
                }
            }
            // chosenInspectors.addAll(inspectorChoice.get(0).get(0).iterator());

            List<Object> chosenAttributes = new ArrayList<Object>();

            SitdReturn r =
                    new SitdReturn(this.selectedPolicyNumber, metaDataToChange, chosenPseudonyms,
                            this.chosenInspectors, chosenAttributes);
            return r;

        }

    }

}

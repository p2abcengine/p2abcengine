//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;

import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.revocationProxy.InMemoryCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxyCommunicationStrategy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.services.helpers.RevocationHelper;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.IssuerParametersInput;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationReferences;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ITTicketTutorial extends ITAbstract {

    private static final CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
    private static final String USERNAME = "default_user";

    private static final String revocation_fileStoragePrefix = "target/revocation_storage/";
    private static final String issuerParamsPrefix = "target/issuer_resources/";
    private static final String issuerFileStoragePrefix = "target/issuer_storage/";
    private static final String userFileStoragePrefix = "target/user_storage/";
    private static final String verifierFileStoragePrefix = "target/verifier_storage/";

    private static final URI cryptographicMechanism = URI
            .create("urn:abc4trust:1.0:algorithm:idemix");

    private static final int revocation_keyLength = 1024;

    private SystemParameters sysparams;
    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9500/abce-services/issuer";

    private final Module inMemoryRevocationCommunicationStrategy = new AbstractModule() {

        @Override
        protected void configure() {
            this.bind(RevocationProxyCommunicationStrategy.class)
            .to(InMemoryCommunicationStrategy.class)
            .in(Singleton.class);
        }

    };

    @Test
    public void completeFlow() throws Exception {
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;

        this.deleteStorageDirectory("revocation_storage");
        this.deleteStorageDirectory("issuer_resources");
        this.deleteStorageDirectory("issuer_storage");
        this.deleteStorageDirectory("user_storage");
        this.deleteStorageDirectory("verifier_storage");

        // Setup revocation helper.
        final RevocationHelper revocationHelper = this.setupRevocationHelper();

        Module revocationProxyAuthorityModule = new AbstractModule() {
            @Override
            protected void configure() {
                this.bind(RevocationProxyAuthority.class).toInstance(
                        revocationHelper.revocationProxyAuthority);
                this.bind(RevocationProxyCommunicationStrategy.class)
                        .to(InMemoryCommunicationStrategy.class)
                        .in(Singleton.class);
            }
        };

        // Setup issuance helper.
        IssuanceHelper issuanceHelper = this
                .setupIssuanceHelper(revocationProxyAuthorityModule);

        // Setup user helper.
        UserHelper userHelper = this
                .setupUserHelper(revocationProxyAuthorityModule);

        // Setup verification helper.
        VerificationHelper verificationHelper = this
                .setupVerificationHelper(revocationProxyAuthorityModule);

        // Setup System Parameters.
        SystemParameters systemParameters = this.setupSystemParameters(issuanceHelper);
sysparams = systemParameters;
        // Store System parameters at Revocation Authority.
        this.storeSystemParametersAtRevocationAuthority(systemParameters);

        // Store System parameters at User.
        this.storeSystemParametersAtUser(userHelper, systemParameters);

        // Store System parameters at Verifier.
        this.storeSystemParametersAtVerifier(verificationHelper,
                systemParameters);

        CredentialSpecification credentialSpecification = this
                .getCredentialSpecification();

        // Store credential specification at issuer.
        this.storeCredentialSpecificationAtIssuer(issuanceHelper, credentialSpecification);

        // Store credential specification at user.
        this.storeCredentialSpecificationAtUser(userHelper,
                credentialSpecification);

        // Store credential specification at Verifier.
        this.storeCredentialSpecificationAtVerifier(verificationHelper,
                credentialSpecification);

        // Setup Revocation Authority Parameters.
        RevocationAuthorityParameters revocationAuthorityParameters = this.setupRevocationAuthorityParameters(revocationHelper);

        // Store Revocation Authority Parameters at issuer.
        this.storeRevocationAuthorityParametersAtIssuer(issuanceHelper,
                revocationAuthorityParameters);

        // Store Revocation Authority Parameters at user.
        this.storeRevocationAuthorityParametersAtUser(userHelper,
                revocationAuthorityParameters);

        // Store Revocation Authority Parameters at verifier.
        this.storeRevocationAuthorityParametersAtVerifier(verificationHelper,
                revocationAuthorityParameters);

        // Setup issuer parameters.
        IssuerParameters issuerParameters = this
                .setupIssuerParameters(issuanceHelper);

        // Store Issuer Parameters at user.
        this.storeIssuerParametersAtUser(userHelper, issuerParameters);

        // Store Issuer Parameters at verifier.
        this.storeIssuerParametersAtVerifier(verificationHelper,
                issuerParameters);

        // Create smartcard at user.
//        this.createSmartcardAtUser(userHelper,
//                issuerParameters.getParametersUID());

        // Init issuance protocol.
        IssuanceMessageAndBoolean issuanceMessageAndBoolean = this
                .initIssuanceProtocol(issuanceHelper);

        // Extract issuance message.
        IssuanceMessage issuanceMessage = issuanceMessageAndBoolean
                .getIssuanceMessage();

        // First issuance protocol step (first step for the user.
        IssuanceReturn issuanceReturn = this.issuanceProtocolStep(userHelper,
                issuanceMessage);

        // First issuance protocol step - UI (first step for the user).
        UiIssuanceReturn uiIssuanceReturn = this.getUiIssuanceReturn(issuanceReturn);

        IssuanceMessage secondIssuanceMessage = this.issuanceProtocolStepUi(
                userHelper, uiIssuanceReturn);

        // Second issuance protocol step (second step for the issuer).
        IssuanceMessageAndBoolean secondIssuanceMessageAndBoolean = this
                .issuanceProtocolStep(issuanceHelper, secondIssuanceMessage);

        // Extract issuance message.
        IssuanceMessage thirdIssuanceMessage = secondIssuanceMessageAndBoolean
                .getIssuanceMessage();

        // Third issuance protocol step (second step for the user).
        IssuanceReturn secondIssuanceReturn = this.issuanceProtocolStep(
                userHelper, thirdIssuanceMessage);

        assertNotNull(secondIssuanceReturn.cd);
        assertNull(secondIssuanceReturn.im);
        assertNull(secondIssuanceReturn.uia);

        // Create presentation policy alternatives.
        PresentationPolicyAlternatives modifiedPresentationPolicyAlternatives = this
                .createPresentationPolicyAlternatives(verificationHelper);

        // Create presentation UI return.
        UiPresentationArguments uiPresentationArguments = this
                .createPresentationToken(userHelper,
                        modifiedPresentationPolicyAlternatives);

        UiPresentationReturn uiPresentationReturn = this
                .createUiPresentationReturn(uiPresentationArguments.uiContext);

        // Create presentation token.
        PresentationToken presentationToken = this
                .createPresentationTokenUi(userHelper, uiPresentationReturn);

        // Verify presentation token against presentation policy.
        PresentationTokenDescription presentationTokenDescription = this
                .verifyTokenAgainstPolicy(verificationHelper,
                        presentationToken,
                        modifiedPresentationPolicyAlternatives);


        assertNotNull(presentationTokenDescription);
    }

    private PresentationTokenDescription verifyTokenAgainstPolicy(
            VerificationHelper verificationHelper,
            PresentationToken presentationToken,
            PresentationPolicyAlternatives modifiedPresentationPolicyAlternatives)
                    throws Exception {
        PresentationTokenDescription ptd = verificationHelper.engine
                .verifyTokenAgainstPolicy(
                        modifiedPresentationPolicyAlternatives,
                        presentationToken, false);

        return ptd;
    }

    private PresentationToken createPresentationTokenUi(UserHelper userHelper,
            UiPresentationReturn uiPresentationReturn) throws Exception {
        PresentationToken presentationToken = userHelper.getEngine()
                .createPresentationToken(USERNAME, uiPresentationReturn);
        return presentationToken;
    }

    private UiPresentationReturn createUiPresentationReturn(URI uiContext) {
        UiPresentationReturn uiPresentationReturn = new UiPresentationReturn();
        uiPresentationReturn.chosenInspectors = new LinkedList<String>();
        uiPresentationReturn.chosenPolicy = 0;
        uiPresentationReturn.chosenPresentationToken = 0;
        uiPresentationReturn.chosenPseudonymList = 0;
        uiPresentationReturn.metadataToChange = new HashMap<String, PseudonymMetadata>();
        uiPresentationReturn.uiContext = uiContext;
        return uiPresentationReturn;
    }

    private UiPresentationArguments createPresentationToken(
            UserHelper userHelper,
            PresentationPolicyAlternatives modifiedPresentationPolicyAlternatives)
                    throws Exception {
        UiPresentationArguments uiPresentationArguments = userHelper
                .getEngine().createPresentationToken(USERNAME,
                        modifiedPresentationPolicyAlternatives);
        return uiPresentationArguments;
    }

    private PresentationPolicyAlternatives createPresentationPolicyAlternatives(
            VerificationHelper verificationHelper) throws Exception {
        // TODO(jdn): is this the cause of the bug?
        Map<URI, URI> revocationInformationUids = new HashMap<URI, URI>();
        String applicationData = null;

        PresentationPolicyAlternatives presentationPolicy = this
                .getPresentationPolicyAlternatives();
        //Was createPresentationPolicy, without nonce
        verificationHelper.modifyPresentationPolicy(presentationPolicy, verificationHelper.generateNonce(),
                applicationData, revocationInformationUids);
        return presentationPolicy;
    }

    private PresentationPolicyAlternatives getPresentationPolicyAlternatives()
            throws Exception {
        String filename = "presentationPolicyAlternatives.xml";
        PresentationPolicyAlternatives o = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("tutorial-resources/"
                                + filename), true);
        return o;
    }

    private IssuanceMessageAndBoolean issuanceProtocolStep(
            IssuanceHelper issuanceHelper, IssuanceMessage secondIssuanceMessage)
                    throws Exception {
        IssuanceMessageAndBoolean response = IssuanceHelper.getInstance()
                .issueStep(secondIssuanceMessage);
        return response;
    }

    private UiIssuanceReturn getUiIssuanceReturn(IssuanceReturn issuanceReturn) {
        UiIssuanceReturn uiIssuanceReturn = new UiIssuanceReturn();
        uiIssuanceReturn.chosenInspectors = new LinkedList<String>();
        uiIssuanceReturn.chosenIssuanceToken = 0;
        uiIssuanceReturn.chosenPseudonymList = 0;
        uiIssuanceReturn.metadataToChange = new HashMap<String, PseudonymMetadata>();
        uiIssuanceReturn.uiContext = issuanceReturn.uia.uiContext;
        return uiIssuanceReturn;
    }

    private IssuanceMessage issuanceProtocolStepUi(UserHelper userHelper,
            UiIssuanceReturn uiIssuanceReturn) throws Exception {
        IssuanceMessage issuanceMessage = userHelper.getEngine()
                .issuanceProtocolStep(USERNAME, uiIssuanceReturn);
        return issuanceMessage;
    }

    private IssuanceReturn issuanceProtocolStep(UserHelper userHelper,
            IssuanceMessage issuanceMessage) throws Exception {
        IssuanceReturn issuanceReturn = userHelper.getEngine()
                .issuanceProtocolStep(USERNAME, issuanceMessage);
        return issuanceReturn;
    }

    private IssuanceMessageAndBoolean initIssuanceProtocol(
            IssuanceHelper issuanceHelper) throws Exception {
        IssuancePolicyAndAttributes issuancePolicyAndAttributes = this
                .getIssuancePolicyAndAttributes();
        IssuancePolicy ip = issuancePolicyAndAttributes.getIssuancePolicy();
        List<Attribute> attributes = issuancePolicyAndAttributes.getAttribute();
        IssuanceMessageAndBoolean issuanceMessageAndBoolean = issuanceHelper
                .initIssuanceProtocol(ip, attributes);
        return issuanceMessageAndBoolean;
    }

    private IssuancePolicyAndAttributes getIssuancePolicyAndAttributes()
            throws Exception {
        String filename = "issuancePolicyAndAttributes.xml";
        IssuancePolicyAndAttributes o = (IssuancePolicyAndAttributes) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("tutorial-resources/"
                                + filename), true);
        return o;
    }
/* NO LONGER IN USE?
    private void createSmartcardAtUser(UserHelper userHelper,
            URI issuerParametersUid) throws Exception {
        Parser xmlSerializer = Parser.getInstance();
        Random random = new SecureRandom();

        KeyManager keyManager = userHelper.keyManager;
        SystemParameters systemParameters = keyManager.getSystemParameters();

        // Element gpAsElement = (Element) systemParameters.getAny().get(1);
        //
        // GroupParameters gp = (GroupParameters)
        // xmlSerializer.parse(gpAsElement);
        //
        // systemParameters.getAny().add(1, gp);

        SecretWrapper secretWrapper = new SecretWrapper(CryptoEngine.IDEMIX,
                random, systemParameters);
        IssuerParameters issuerParameters = keyManager
                .getIssuerParameters(issuerParametersUid);
        secretWrapper.addIssuerParameters(issuerParameters);
        BasicSmartcard softwareSmartcard = secretWrapper.getSoftwareSmartcard();

        userHelper.cardStorage.addSmartcard(softwareSmartcard, 1234);
    }
*/
    private void storeIssuerParametersAtVerifier(
            VerificationHelper verificationHelper,
            IssuerParameters issuerParameters) throws Exception {
        verificationHelper.keyManager.storeIssuerParameters(
                issuerParameters.getParametersUID(), issuerParameters);
    }

    private void storeIssuerParametersAtUser(UserHelper userHelper,
            IssuerParameters issuerParameters) throws Exception {
        userHelper.keyManager.storeIssuerParameters(
                issuerParameters.getParametersUID(), issuerParameters);
    }

    private void storeRevocationAuthorityParametersAtVerifier(
            VerificationHelper verificationHelper,
            RevocationAuthorityParameters revocationAuthorityParameters)
                    throws Exception {
        verificationHelper.keyManager.storeRevocationAuthorityParameters(
                revocationAuthorityParameters.getParametersUID(),
                revocationAuthorityParameters);

    }

    private void storeRevocationAuthorityParametersAtUser(
            UserHelper userHelper,
            RevocationAuthorityParameters revocationAuthorityParameters)
                    throws Exception {
        userHelper.keyManager.storeRevocationAuthorityParameters(
                revocationAuthorityParameters.getParametersUID(),
                revocationAuthorityParameters);
    }

    private void storeRevocationAuthorityParametersAtIssuer(
            IssuanceHelper issuanceHelper,
            RevocationAuthorityParameters revocationAuthorityParameters)
                    throws Exception {
        issuanceHelper.keyManager.storeRevocationAuthorityParameters(
                revocationAuthorityParameters.getParametersUID(),
                revocationAuthorityParameters);
    }

    private RevocationAuthorityParameters setupRevocationAuthorityParameters(
            RevocationHelper revocationHelper) throws Exception {
        RevocationReferences references = this.getRevocationReferences();
        Reference revocationInfoReference = references
                .getRevocationInfoReference();
        Reference nonRevocationEvidenceReference = references
                .getNonRevocationEvidenceReference();
        Reference nonRevocationUpdateReference = references
                .getNonRevocationEvidenceUpdateReference();

        URI uid = URI.create("http://ticketcompany/revocation");
        RevocationAuthorityParameters raParams = RevocationHelper
                .setupParameters(cryptographicMechanism,
                        revocation_keyLength, uid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRevocationUpdateReference,
                        revocation_fileStoragePrefix);
        return raParams;
    }

    private RevocationReferences getRevocationReferences() throws Exception {
        String filename = "revocationReferences.xml";
        RevocationReferences revocationReferences = (RevocationReferences) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("tutorial-resources/"
                                + filename), true);
        return revocationReferences;
    }

    private void storeSystemParametersAtVerifier(
            VerificationHelper verificationHelper,
            SystemParameters systemParameters) throws Exception {
        verificationHelper.keyManager.storeSystemParameters(systemParameters);
    }

    private void storeSystemParametersAtUser(UserHelper userHelper,
            SystemParameters systemParameters) throws Exception {
        userHelper.keyManager.storeSystemParameters(systemParameters);
    }

    private void storeSystemParametersAtRevocationAuthority(
            SystemParameters systemParameters)
                    throws Exception {
        KeyManager keyManager = UserStorageManager
                .getKeyManager(RevocationService.fileStoragePrefix);
        keyManager.storeSystemParameters(systemParameters);
    }

    private void storeCredentialSpecificationAtVerifier(
            VerificationHelper verificationHelper,
            CredentialSpecification credentialSpecification) throws Exception {
        verificationHelper.keyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);
    }

    private void storeCredentialSpecificationAtUser(UserHelper userHelper,
            CredentialSpecification credentialSpecification) throws Exception {
        userHelper.keyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);
    }

    private void storeCredentialSpecificationAtIssuer(
            IssuanceHelper issuanceHelper,
            CredentialSpecification credentialSpecification) throws Exception {
        issuanceHelper.keyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);

    }

    private CredentialSpecification getCredentialSpecification()
            throws Exception {
        String filename = "credentialSpecificationVIPSoccerTicket.xml";
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("tutorial-resources/"
                                + filename), true);
        return credentialSpecification;
    }

    private IssuanceHelper setupIssuanceHelper(
            Module revocationProxyAuthorityModule) throws Exception {
        IssuanceHelper.initInstanceForService(ITTicketTutorial.issuerParamsPrefix,
                ITTicketTutorial.issuerFileStoragePrefix);

        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();
        return issuanceHelper;
    }

    private VerificationHelper setupVerificationHelper(
            Module revocationProxyAuthorityModule) throws Exception {
        String[] credSpecResources = new String[0];
        String[] revAuthResourceList = new String[0];
        String[] inspectorResourceList = new String[0];

        String[] issuerParamsResourceList = new String[0];

        List<IssuerParameters> issuerParamsList = new ArrayList<IssuerParameters>();
        List<CredentialSpecification> credSpecsList = new ArrayList<CredentialSpecification>();
        List<InspectorPublicKey> inspectorKeyList = new ArrayList<InspectorPublicKey>();
        List<RevocationAuthorityParameters> revAuthParamsList = new ArrayList<RevocationAuthorityParameters>();
        
        VerificationHelper.initInstance(sysparams, issuerParamsList, credSpecsList,
                inspectorKeyList, revAuthParamsList,
                ITTicketTutorial.verifierFileStoragePrefix);

        VerificationHelper verificationHelper = VerificationHelper
                .getInstance();
        return verificationHelper;
    }

    private RevocationHelper setupRevocationHelper() throws Exception {
        this.initRevocationHelper();
        RevocationHelper revocationHelper = RevocationHelper.getInstance();
        return revocationHelper;
    }

    private void initRevocationHelper() throws Exception {
        RevocationHelper.initInstance(RevocationService.fileStoragePrefix,
                this.inMemoryRevocationCommunicationStrategy);
    }

    private UserHelper setupUserHelper(Module revocationProxyAuthorityModule)
            throws Exception {
        UserHelper.initInstanceForService(cryptoEngine,
                ITTicketTutorial.userFileStoragePrefix);
        UserHelper instance = UserHelper.getInstance();
        return instance;
    }

    private SystemParameters setupSystemParameters(IssuanceHelper issuanceHelper)
            throws IOException, KeyManagerException, Exception {
        int idemixKeylength = 1024;

        SystemParameters systemParameters = issuanceHelper.createNewSystemParametersKeylength(idemixKeylength);
        return systemParameters;
    }

    public IssuerParametersInput getIssuerParametersInput()
            throws IOException, JAXBException, UnsupportedEncodingException,
            SAXException {
        String filename = "/issuerParametersInput.xml";
        IssuerParametersInput issuerParametersInput = (IssuerParametersInput) XmlUtils
                .getObjectFromXML(new FileInputStream("tutorial-resources/"
                        + filename), true);

        return issuerParametersInput;
    }

    private IssuerParameters setupIssuerParameters(IssuanceHelper instance)
            throws Exception {
        URI hash = CryptoUriUtil.getHashSha256();

        KeyManager keyManager = instance.keyManager;
        SystemParameters systemParameters = keyManager.getSystemParameters();

        IssuerParametersInput issuerParametersInput = this
                .getIssuerParametersInput();
        URI credentialSpecUid = issuerParametersInput.getCredentialSpecUID();
        CredentialSpecification credspec = keyManager
                .getCredentialSpecification(credentialSpecUid);

        if (credspec == null) {
            throw new IllegalStateException(
                    "Could not find credential specification \""
                            + credentialSpecUid + "\"");
        }

        URI issuerParametersUid = issuerParametersInput.getParametersUID();
        URI revocationParametersUid = issuerParametersInput
                .getRevocationParametersUID();
        List<FriendlyDescription> friendlyDescriptions = issuerParametersInput
                .getFriendlyIssuerDescription();
        String systemAndIssuerParamsPrefix = Constants.ISSUER_RESOURCES_FOLDER
                + "/";
        IssuerParameters issuerParameters = instance.setupIssuerParameters(
                cryptoEngine, credspec, systemParameters, issuerParametersUid,
                hash, revocationParametersUid, systemAndIssuerParamsPrefix,
                friendlyDescriptions);
        return issuerParameters;

    }
}
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

package eu.abc4trust.ri.service.it.issuer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;

import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.utils.XMLSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
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
import eu.abc4trust.xml.VerificationCall;
import eu.abc4trust.xml.util.XmlUtils;

public abstract class AbstractIT {

    static ObjectFactory of = new ObjectFactory();

    String[] credSpecResourceList =
        { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml"
            , "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"
            , "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml"
            , "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml"
            , "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"
        };

    String[] issuerParamsResourceList_base = new String[]
            {"/issuer_issuer_params_my_country_identitycard_issuancekey_v1.0",
            "/issuer_issuer_params_urn_patras_issuer_credUniv",
            "/issuer_issuer_params_urn_patras_issuer_credCourse",
            "/issuer_issuer_params_urn_soderhamn_issuer_credSchool",
            "/issuer_issuer_params_urn_soderhamn_issuer_credSubject" };


    String[] presentationPolicyResources =
        {"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml"
            ,"/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml"
            ,"/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeFrench.xml"
            ,"/eu/abc4trust/sampleXml/soderhamn/presentationPolicyRASubjectMustBeEnglish.xml"
            ,"/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml"
            ,"/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml"
        };


    public void initHelper(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String userStorage) {
        try {
            UserHelper.resetInstance();
            System.out.println("UserService initHelper - cryptoEngine ! " + cryptoEngine + " - clientEngine " + clientEngine);

            String uprovePath;
            String fileStoragePrefix;
            String user_fileStoragePrefix;
            File folder;
            if (new File("target").exists()) {
                fileStoragePrefix = "target/"; // + user + "_";
                folder = new File("target");
                uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
            } else {
                fileStoragePrefix = "integration-test-issuer/target/"; //+ user + "_";
                folder = new File("integration-test-issuer/target");
                uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
            }
            System.setProperty("PathToUProveExe", uprovePath);
            user_fileStoragePrefix = fileStoragePrefix + userStorage;


            String[] issuerParamsResourceList;
            if(cryptoEngine == CryptoEngine.BRIDGED) {
                issuerParamsResourceList = this.createIssuerParamsWithCryptoEngine(folder, this.issuerParamsResourceList_base, CryptoEngine.IDEMIX, CryptoEngine.UPROVE);
            } else {
                issuerParamsResourceList = this.createIssuerParamsWithCryptoEngine(folder, this.issuerParamsResourceList_base, cryptoEngine);
            }

            String[] inspectorPublicKeyResourceList = new String[0];

            // String systemParamsResource = null;
            UserHelper.initInstance(cryptoEngine/* , systemParamsResource */,
                    issuerParamsResourceList, user_fileStoragePrefix, this.credSpecResourceList, inspectorPublicKeyResourceList);

            KeyManager keyManager = UserHelper.getInstance().keyManager;
            IssuerParameters exists =
                    keyManager.getIssuerParameters(new URI(
                            "http://my.country/identitycard/issuancekey_v1.0"));
            if (exists != null) {
                System.out.println("Issuer Params - created as expected!");
            } else {
                System.out.println("Issuer Params - ?? Not found!");
            }

            System.out.println("UserService static init ! DONE");


            // add secret for test
            Secret secret;
            if(clientEngine == CryptoEngine.IDEMIX) {
                SystemParameters systemParameters = keyManager
                        .getSystemParameters();

                Secret tmpSecret = (Secret) XmlUtils.getObjectFromXML(
                        ITIssuer.class.getResourceAsStream(
                                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
                secret = new Secret();
                secret.setSecretDescription(tmpSecret.getSecretDescription());
                secret.setSecretKey(tmpSecret.getSecretKey());
                secret.setSystemParameters(SystemParametersUtil
                        .createSmartcardSystemParameters(systemParameters));

            } else if(clientEngine == CryptoEngine.UPROVE) {
                secret = (Secret) XmlUtils.getObjectFromXML(
                        ITIssuer.class.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/patras/uprove-secret.xml"),
                                true);

            } else {
                throw new IllegalStateException("Client(User) CryptoEngine not supported : " + clientEngine);
            }
            URI secretUID = secret.getSecretDescription().getSecretUID();
            System.out.println("System - adding smart card secret : "
                    + secret.getSecretDescription().getSecretUID());

            try {
                @SuppressWarnings("unused")
                Secret exists_secret = UserHelper.getInstance().credentialManager.getSecret(secretUID);
                System.out.println("Secret Already Exists!! " + secretUID);
            } catch (SecretNotInStorageException e) {
                System.out.println("Secret Not In Storage!");

                UserHelper.getInstance().credentialManager.storeSecret(secret);
            }

            // ======================================================================

            VerificationHelper.resetInstance();
            System.out.println(" - Init Verificateion Helper - :  " + cryptoEngine + " : " + cryptoEngine);

            String verifier_fileStoragePrefix = fileStoragePrefix + "verifier_" + cryptoEngine.toString().toLowerCase() + "_";

            VerificationHelper.initInstance(cryptoEngine, issuerParamsResourceList, this.credSpecResourceList, inspectorPublicKeyResourceList,
                    verifier_fileStoragePrefix, this.presentationPolicyResources);


        } catch (Exception e) {
            throw new IllegalStateException("Could not start up!", e);
        }
    }

    private String[] createIssuerParamsWithCryptoEngine(File folder, String[] baseResourceList,
            CryptoEngine... engines) {
        List<String> resourceList = new ArrayList<String>();
        for(CryptoEngine engine : engines) {
            String ce_append = "_" + engine.toString().toLowerCase();
            for(String resource : baseResourceList) {
                resourceList.add(folder.getAbsolutePath() + resource + ce_append);
            }
        }
        return resourceList.toArray(new String[0]);
    }

    public void initIssuer(CryptoEngine cryptoEngine, CryptoEngine clientEngine) throws Exception {
        Client client = Client.create();
        Builder initResource =
                client.resource(this.baseUrl + "/issue/init/" + cryptoEngine+"?clientEngine="+clientEngine).accept(MediaType.TEXT_PLAIN);

        String response = initResource.get(String.class);
        System.out.println("INIT OK !" + response);
    }


    public void initPseudonym(CryptoEngine cryptoEngine, String scopeString, int matNumber) throws Exception {

        URI scope = URI.create(scopeString);
        if(cryptoEngine == CryptoEngine.IDEMIX) {

            String pseudonymValue_BigIntegerString;
            if(scopeString.equals("urn:patras:registration")) {
                pseudonymValue_BigIntegerString = IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_Idemix_BigIntegerString;
            } else if(scopeString.equals("urn:soderhamn:registration")) {
                pseudonymValue_BigIntegerString = IssuanceHelper.TEST_CONSTANTS.soderhamnPseudonymValue_Idemix_BigIntegerString;
            } else {
                throw new IllegalStateException("Scope not supported for pseudonym " + scopeString);
            }
            URI secretUid = URI.create("secret://sample-1234");

            BigInteger pseudonymValue = new BigInteger(pseudonymValue_BigIntegerString);

            ObjectFactory of = new ObjectFactory();
            Pseudonym pseudonym = of.createPseudonym();
            //      URI secretUid = smartcard.getDeviceURI(1234);
            pseudonym.setSecretReference(secretUid);
            pseudonym.setExclusive(true);
            pseudonym.setPseudonymUID(URI.create("foo-bar-pseudonym-uid-" + matNumber));

            //   BigInteger pseudonymValue = smartcard.computeScopeExclusivePseudonym(pin, scope);

            pseudonym.setPseudonymValue(pseudonymValue.toByteArray());
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

            URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");

            //      StructureStore.getInstance().add(groupParameterId.toString(), groupParameters);
            StoredDomainPseudonym dp = new StoredDomainPseudonym(scope, secretUid, groupParameterId);
            cryptoEvidence.getAny().add(XMLSerializer.getInstance().serializeAsElement(dp));
            pwm.setCryptoParams(cryptoEvidence);


            CredentialManager userCredentialManager = UserHelper.getInstance().credentialManager;
            userCredentialManager.storePseudonym(pwm);

        } else if(cryptoEngine == CryptoEngine.UPROVE) {
            PseudonymWithMetadata pwm = (PseudonymWithMetadata) XmlUtils
                    .getObjectFromXML(
                            this.getClass()
                            .getResourceAsStream(
                                    "/eu/abc4trust/sampleXml/patras/uprove-pseudonym.xml"),
                                    true);
            pwm.getPseudonym().setScope(scopeString);
            pwm.getPseudonym().setPseudonymUID(scope);

            CredentialManager userCredentialManager = UserHelper.getInstance().credentialManager;
            userCredentialManager.storePseudonym(pwm);
        }

    }


    @After
    public void resetIssuerEngine() throws Exception {
        System.out.println("resetIssuerEngine...");
        Client client = Client.create();
        Builder initResource = client.resource(this.baseUrl + "/issue/reset/").accept(MediaType.TEXT_PLAIN);

        String response = initResource.get(String.class);
        System.out.println("Reset OK !" + response);

        IssuanceHelper.resetInstance();
        UserHelper.resetInstance();
        VerificationHelper.resetInstance();
        System.out.println("resetIssuerEngine DONE");
    }

    // final String baseUrl = "http://localhost:19500/pilot-patras";
    final String baseUrl = "http://localhost:9090/integration-test-issuer";

    protected void runIssuance(String serverMethod, String issuanceKey) throws Exception {
        this.runIssuance(serverMethod, issuanceKey, null);
    }
    protected void runIssuance(String serverMethod, String issuanceKey, String scope) throws Exception {

        System.out.println("- run issuance with key : " + issuanceKey);

        Client client = Client.create();
        Builder issueStartResource =
                client.resource(this.baseUrl + "/issue/" + serverMethod + "/" + issuanceKey)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

        IssuanceMessage server_im = issueStartResource.get(IssuanceMessage.class);
        System.out.println(" - initial message - server : " + server_im);
        System.out.println(" - initial message - server : "
                + XmlUtils.toXml(of.createIssuanceMessage(server_im)));

        System.out.println(" - initial message - client - engine " + UserHelper.getInstance().getEngine());
        IssuMsgOrCredDesc user_im =
                UserHelper.getInstance().getEngine().issuanceProtocolStep(server_im);
        System.out.println(" - initial message - client - created ");
        System.out.println(" - initial message - client - created " + user_im.im);
        System.out.println(" - initial message - client - created " + AbstractHelper.getPseudonymValue(user_im.im, scope));
        System.out.println(" - initial message - client - created " + user_im.cd);
        System.out.println(" - initial message - client : "
                + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), false));

        int stepCount = 1;
        boolean lastmessage = false;
        while (!lastmessage) {
            Builder issueStepResource =
                    client.resource(this.baseUrl + "/issue/step").type(MediaType.APPLICATION_XML)
                    .accept(MediaType.TEXT_XML);

            // send to server and receive new im
            System.out.println(" - contact server");
            server_im =
                    issueStepResource.post(IssuanceMessage.class, of.createIssuanceMessage(user_im.im));
            System.out.println(" - got response");
            System.out.println(" - step message - server : " + stepCount + " : "
                    + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));

            // process in
            user_im = UserHelper.getInstance().getEngine().issuanceProtocolStep(server_im);
            System.out.println(" - step message - client :" + stepCount);

            lastmessage = (user_im.cd != null);
            if (!lastmessage) {
                System.out.println(" - initial message - step : " + stepCount + " : "
                        + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), false));
            }
        }
        System.out.println(" - done...");
        System.out.println(" - done : credentialDescription : "
                + XmlUtils.toXml(of.createCredentialDescription(user_im.cd), false));

    }



    /**
     * NOTE : Verification does not run over HTTP
     * 
     * @param policy
     * @param satisfiedExpected
     * @param scope
     * @throws Exception
     */
    protected void runVerification(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String policy, boolean satisfiedExpected, String scope) throws Exception {
        byte[] nonce = VerificationHelper.getInstance().generateNonce();
        PresentationPolicyAlternatives ppa =
                VerificationHelper.getInstance().createPresentationPolicy(policy, nonce, null, null);

        PresentationToken presentationToken;
        try {
            presentationToken = UserHelper.getInstance().getEngine().createPresentationToken(ppa);
        } catch (CannotSatisfyPolicyException e) {
            if (satisfiedExpected) {
                System.out.println("Credentials does not satisfy policy");
                Assert.assertTrue("Credentials does not satisfy policy", false);
                return;
            } else {
                // this is ok... . failed as expected - user side...
                System.out.println("Credentials does not satisfy policy - This was expected");
                return;
            }
        }
        if (presentationToken == null) {
            if (satisfiedExpected) {
                System.out.println("Credentials does not satisfy policy");
                Assert.assertTrue("Credentials does not satisfy policy", false);
                return;
            } else {
                // this is ok... . failed as expected - user side...
                System.out.println("Credentials does not satisfy policy - This was expected");
                return;
            }
        }

        System.out.println("PresentationToken pseudonym value for scope - " + scope + " : " + AbstractHelper.getPseudonymValue(presentationToken, scope));
        System.out.println("PresentationToken : " + XmlUtils.toXml(of.createPresentationToken(presentationToken)));
        boolean ok = false;
        try {
            ok = VerificationHelper.getInstance().verifyToken(ppa, presentationToken);
        } catch (Exception e) {
            System.err.println("Credentials not accepted by Verifier ???");
            e.printStackTrace();
            Assert.assertTrue("Credentials not accepted by Verifier ???", false);
        }
        try {
            VerificationCall vc = of.createVerificationCall();
            vc.setPresentationPolicyAlternatives(ppa);
            vc.setPresentationToken(presentationToken);
            System.out.println("VerificationCall : " + XmlUtils.toXml(of.createVerificationCall(vc), true));
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (ok) {
            if (satisfiedExpected) {
                // this is ok... . failed as expected - user side...
                System.out.println("Verifier accepted");
                return;
            } else {
                System.out.println("Verifier accepted - But : Credentials does not satisfy policy");
                Assert.assertTrue("Verifier accepted - But : Credentials does not satisfy policy", false);
                return;
            }

        } else {
            if (satisfiedExpected) {
                System.out.println("Verifier rejected : Credentials does not satisfy policy");
                Assert.assertTrue("Verifier rejected : Credentials does not satisfy policy", false);
                return;
            } else {
                // this is ok... . failed as expected - user side...
                System.out
                .println("Verifier rejected : Credentials does not satisfy policy - This was expected");
                return;
            }
        }
    }

    protected void copySystemParameters() throws IOException {
        this.copySystemParameters("issuer_system_params_bridged");
        this.copySystemParameters("issuer_system_params_bridged_human_readable_only_for_reference.xml");
    }

    private void copySystemParameters(String filename) throws IOException {
        File file = null;
        file = new File("src" + File.separatorChar + "main"
                + File.separatorChar + "resources" + File.separatorChar
                + filename);

        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist on path: \""
                    + filename + "\"");
        }

        // new File().mkdirs();

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(new File("target"
                + File.separatorChar + filename));

        byte[] bytes = new byte[1];
        while (fis.read(bytes) != -1) {
            fos.write(bytes);
        }

        fis.close();
        fos.close();
    }
}

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

package eu.abc4trust.ri.service.it.revocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.Test;

import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.utils.XMLSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.util.XmlUtils;

public class ITRevocation {
    final static String baseUrl = "http://localhost:9094/integration-test-revocation";

    public ITRevocation () {
    }

    ObjectFactory of = new ObjectFactory();

    // @Ignore
    @Test
    public void verifyPresentationTokenWithRevocation() throws Exception {

        System.out.println("---- verifyPresentationTokenWithRevocation ----");

        this.initIssuer(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX);


        System.out.println("-- init revocation authority in service");
        Client client = Client.create();
        Builder initResource =
                client.resource(baseUrl + "/init/soderhamn").accept(MediaType.TEXT_PLAIN);

        String response = initResource.get(String.class);
        System.out.println("-- init revocation authority in service DONE" + response);

        // re-init Issuer With RevocationAuthorities
        this.initIssuer(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX);

        System.out.println("-- init local engines for issuer, verifier and user");

        this.initVerifierAndUser(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX);

        CredentialDescription credentialDescription = null;

        System.out.println("-- issue School credential");
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

        IssuanceMessage service_im =
                IssuanceHelper.getInstance().initIssuance(CryptoEngine.IDEMIX, SODERHAMN_SCHOOL,
                        attributeValueMap);

        while (true) {
            System.out.println("ISSUER IM : \n" + XmlUtils.toXml(new ObjectFactory().createIssuanceMessage((service_im))));

            IssuMsgOrCredDesc user_im = null;
            // invoke user
            user_im = UserHelper.getInstance().getEngine().issuanceProtocolStep(service_im);
            System.out.println(" - user im : " + user_im);

            if (user_im.im == null) {
                System.out.println(" - protocol finished..." + user_im);
                break;
            }
            System.out.println("USER IM : \n" + XmlUtils.toXml(new ObjectFactory().createIssuanceMessage((user_im.im))));

            // invoke issuer!
            System.out.println(" - invoke ABCE - next step!");

            IssuanceMessageAndBoolean im_and_boolean =
                    IssuanceHelper.getInstance().issueStep(CryptoEngine.IDEMIX, user_im.im);

            service_im = im_and_boolean.getIssuanceMessage();
            if (im_and_boolean.isLastMessage()) {
                System.out.println("LastMessage ON SERVER ! "
                        + service_im.getContext() + " ; "
                        + im_and_boolean.getIssuanceLogEntryURI() + " : "
                        + im_and_boolean.getIssuanceMessage().getAny());
                for(Object o : service_im.getAny()) {
                    if(o instanceof JAXBElement<?>) {
                        Object abc = ((JAXBElement<?>)o).getValue();
                        System.out.println(" - " + abc);
                        if(abc instanceof CredentialDescription) {
                            credentialDescription = (CredentialDescription) abc;
                        }
                    } else {
                        System.out.println(" - " + o);
                    }
                }
            }
        }
        System.out.println("User now has Credential!");
        System.out.println(" :: " + UserHelper.getInstance().credentialManager.listCredentials());

        // run presentation with Revokable Credential...
        this.runPresenationWithRevokableCredential(true);

        // DO REVOCATION!
        Attribute attribute = null;
        for (Attribute a : credentialDescription.getAttribute()) {
            if (a.getAttributeDescription().getType()
                    .compareTo(RevocationConstants.REVOCATION_HANDLE) == 0) {
                attribute = a;
            }
        }
        RevocationInformation riFromRevocation=null;
        if(attribute!=null) {
            System.out.println("Revoke Credential - by Attribute ! : " + attribute);
            Builder revokeResource =
                    client.resource(baseUrl + "/revocation/revokeAttribute/" + URLEncoder.encode(soderhamnRevocationAuthority.toString(), "UTF-8")).accept(MediaType.APPLICATION_XML);
            riFromRevocation = revokeResource.post(RevocationInformation.class, this.of.createAttribute(attribute));
            System.out.println("XXX GET RI 1 " + riFromRevocation.getInformationUID());
        }

        if(attribute!=null) {
            System.out.println("Revoke Credential - by just handle ! : " + attribute.getAttributeValue());
            Builder revokeResource =
                    client.resource(baseUrl + "/revocation/revokeHandle/" + URLEncoder.encode(soderhamnRevocationAuthority.toString(), "UTF-8") + "?revocationHandle=" + URLEncoder.encode(attribute.getAttributeValue().toString(), "UTF-8")).accept(MediaType.APPLICATION_XML);
            riFromRevocation = revokeResource.post(RevocationInformation.class);
            System.out.println("XXX GET RI 2 " + riFromRevocation.getInformationUID());
        }


        // run presentation with Revokable Credential...  - should not verify!
        this.runPresenationWithRevokableCredential(false);

        System.out.println("Revocation Test ! OK ");

    }


    private void runPresenationWithRevokableCredential(/*RevocationInformation revocationInformation, */boolean verify_ok) throws Exception {
        // create presentation token!


        System.out.println("#####################################################");
        System.out.println("Create PresentationPolicy");
        String applicationData = null;
        //      String policyName = "presentationPolicySoderhamnSchoolWithInspection.xml";
        String policyName = "presentationPolicySoderhamnSchoolForRevocation.xml";
        byte[] nonce = VerificationHelper.getInstance().generateNonce();
        PresentationPolicyAlternatives policy =
                VerificationHelper.getInstance().createPresentationPolicy(policyName, nonce,
                        applicationData, null);
        System.out.println("Created PresentationPolicy " + policy);
        System.out.println("Created PresentationPolicy "
                + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(policy)));

        //
        PresentationToken presentationToken =
                UserHelper.getInstance().getEngine().createPresentationToken(policy);

        if(!verify_ok && (presentationToken == null)){
            System.out.println("Could not create presentationToken - this was expected!");
            return;
        }

        System.out.println("Created PresentationToken " + presentationToken);
        System.out.println("Created PresentationToken "
                + XmlUtils.toXml(this.of.createPresentationToken(presentationToken)));

        Exception failure = null;
        try {
            VerificationHelper.getInstance().verifyToken(policy, // policyName, nonce, applicationData,
                    presentationToken);
        } catch (Exception e) {
            failure = e;
        }
        if(verify_ok) {
            if(failure == null) {
                System.out.println("Verify OK!");
            } else {
                throw new Exception("Verification Should not Fail ??", failure);
            }
        } else {
            if(failure == null) {
                System.out.println("Verify Should Fail!");
                throw new IllegalStateException("Verify Should Fail!");
            } else {
                System.out.println("Verify Failed as expected!");
            }
        }


    }

    public static final URI soderhamnRevocationAuthority = URI.create("urn:soderhamn:revocationauthority:default");

    private static final String SODERHAMN_SCHOOL = "SODERHAMN_SCHOOL";
    private static final SpecAndPolicy soderhamn_school = new SpecAndPolicy(SODERHAMN_SCHOOL,
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchoolWithRevocation.xml",
            // "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml", null, "urn:soderhamn:revocationauthority:default");


    public void initIssuer(CryptoEngine issuerEngine, CryptoEngine clientRunEngine)
            throws Exception {
        System.out.println("initIssuer(CryptoEngine " + issuerEngine + " - clientEngine : "
                + clientRunEngine);

        System.out.println("setup IssuanceHelper");
        IssuanceHelper.resetInstance();

        File folder;
        String issuer_fileStoragePrefix;
        if (new File("target").exists()) {
            issuer_fileStoragePrefix = "target/issuer_";
            folder = new File("target");
        } else {
            issuer_fileStoragePrefix = "integration-test-user/target/issuer_";
            folder = new File("integration-test-user/target");
        }
        issuer_fileStoragePrefix += issuerEngine.toString().toLowerCase() + "_";

        String [] revocationAuthorityParameters_resources =
                filesToResourceList(folder, "revocation_revocation_authority");


        IssuanceHelper.initInstance(issuerEngine, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
                new SpecAndPolicy[] { soderhamn_school }, revocationAuthorityParameters_resources);


        // IDEMIX Pseudonym Values!
        {
            // pseudonym with scope "urn:patras:soderhamn" - for static secret...
            IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(
                    IssuanceHelper.TEST_CONSTANTS.soderhamnPseudonymValue_Idemix);

        }
        // UPROVE
        {
            // pseudonym with scope "urn:patras:registration" - for static secret...
            IssuanceHelper.getInstance().registerSmartcardScopeExclusivePseudonym(
                    IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_UProve);
        }

        System.out.println("IssuanceHelper - done!");
    }

    public void initVerifierAndUser(CryptoEngine issuerEngine, CryptoEngine clientRunEngine)
            throws Exception {
        System.out.println("initVerifierAndUser(CryptoEngine " + issuerEngine + " - clientEngine : "
                + clientRunEngine);

        this.copySystemParameters();

        File folder;
        //String issuer_fileStoragePrefix;
        String verifier_fileStoragePrefix;
        String user_fileStoragePrefix;
        if (new File("target").exists()) {
            verifier_fileStoragePrefix = "target/verifier_";
            user_fileStoragePrefix = "target/user_";
            folder = new File("target");
        } else {
            verifier_fileStoragePrefix = "integration-test-revocation/target/verifier_";
            user_fileStoragePrefix = "integration-test-revocation/target/user_";
            folder = new File("integration-test-revocation/target");
        }
        //issuer_fileStoragePrefix += issuerEngine.toString().toLowerCase() + "_";
        verifier_fileStoragePrefix += issuerEngine.toString().toLowerCase() + "_";
        user_fileStoragePrefix += clientRunEngine.toString().toLowerCase() + "_";

        String[] inspectorPublicKeyResourceList =
                filesToResourceList(folder, "inspector_inspector_publickey");

        String [] revocationAuthorityParametersResourceList =
                filesToResourceList(folder, "revocation_revocation_authority");

        System.out.println("Setup VerificationHelper");
        VerificationHelper.resetInstance();

        String[] presentationPolicyResources =
            {"/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchoolForRevocation.xml"};

        String[] credSpecResourceList =
            { soderhamn_school.specResource };
        //            {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};

        // File systemParamsFile = new File(folder, "issuer_system_params");
        // String systemParamsResource = systemParamsFile.getAbsolutePath();


        String[] issuerParamsResourceList =
                filesToResourceList(folder, "issuer_" + issuerEngine.toString().toLowerCase()
                        + "_issuer_params_");

        VerificationHelper.initInstance(issuerEngine, issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, revocationAuthorityParametersResourceList,
                verifier_fileStoragePrefix, presentationPolicyResources);

        System.out.println("VerificationHelper - Done");

        System.out.println("Setup UserHelper");
        UserHelper.resetInstance();
        UserHelper.WIPE_STOARAGE_FILES = true;
        // todo Inspector keys must be added to init of UserHelper...
        UserHelper.initInstance(issuerEngine, issuerParamsResourceList, user_fileStoragePrefix,
                credSpecResourceList, inspectorPublicKeyResourceList, revocationAuthorityParametersResourceList);

        UserHelper instance = UserHelper.getInstance();
        Secret tmpSecret;
        if (clientRunEngine == CryptoEngine.IDEMIX) {
            System.out.println("- client engine is IDEMIX - load secret !");
            InputStream is =
 FileSystem
                    .getInputStream("/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml");
            System.out.println("IS : " + is);

            tmpSecret = (Secret) XmlUtils.getObjectFromXML(is, true);
        } else if (clientRunEngine == CryptoEngine.UPROVE) {
            System.out.println("- client engine is UPROVE - load secret !");
            InputStream is =
 FileSystem
                    .getInputStream("/eu/abc4trust/sampleXml/patras/uprove-secret.xml");
            System.out.println("IS : " + is);
            tmpSecret = (Secret) XmlUtils.getObjectFromXML(is, true);

        } else {
            throw new IllegalStateException("CryptoEngine not supported : " + clientRunEngine);
        }

        Secret secret = new Secret();
        secret.setSecretDescription(tmpSecret.getSecretDescription());
        secret.setSecretKey(tmpSecret.getSecretKey());
        secret.setSystemParameters(SystemParametersUtil
                .createSmartcardSystemParameters(instance.keyManager
                        .getSystemParameters()));

        URI secretUid = secret.getSecretDescription().getSecretUID();
        System.out.println("System - adding smart card secret : "
                + secret.getSecretDescription().getSecretUID());

        try {
            @SuppressWarnings("unused")
            Secret exists_secret = instance.credentialManager
            .getSecret(secretUid);
            System.out.println("Secret Already Exists!! " + secretUid);
        } catch (SecretNotInStorageException e) {
            System.out.println("Secret Not In Storage!");

            instance.credentialManager.storeSecret(secret);
        }

        String soderhamnScope = "urn:soderhamn:registration";
        String patrasScope = "urn:patras:registration";
        String[] scopes = {patrasScope, soderhamnScope};
        if (clientRunEngine == CryptoEngine.IDEMIX) {

            // / pseudonym values calculated from static secret.xml
            BigInteger[] pseudonymValues =
                {IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_Idemix,
                    IssuanceHelper.TEST_CONSTANTS.soderhamnPseudonymValue_Idemix};
            for (int i = 0; i < scopes.length; i++) {
                String scope = scopes[i];
                @SuppressWarnings("unused")
                URI scopeUri = URI.create(scope);
                BigInteger pseudonymValue = pseudonymValues[i];

                URI pseudonymUID = URI.create(scope + ":pseudonymuid:42");
                try {
                    @SuppressWarnings("unused")
                    PseudonymWithMetadata pseudo =
                    instance.credentialManager
                    .getPseudonym(pseudonymUID);
                    System.out.println(" - pseudo exists! " + scope);
                    // System.out.println(" - pseudo exists! : " + scope + " : " +
                    // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
                } catch (CredentialManagerException e) {

                    PseudonymWithMetadata pwm =
                            this.createPseudonym(secretUid, scope, pseudonymUID, pseudonymValue);

                    instance.credentialManager.storePseudonym(pwm);

                    System.out.println(" - " + scope + " : pseudo Created!");
                    // System.out.println(" - " + scope + " : pseudo Created! : " +
                    // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
                }
            }
        } else if (clientRunEngine == CryptoEngine.UPROVE) {
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
                    instance.credentialManager
                    .getPseudonym(pseudonymUID);
                    System.out.println(" - pseudo exists!");
                    // System.out.println(" - pseudo exists! : " +
                    // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
                } catch (CredentialManagerException e) {

                    PseudonymWithMetadata pwm =
                            (PseudonymWithMetadata) XmlUtils.getObjectFromXML(this.getClass()
                                    .getResourceAsStream(uprovePseudonymResourse), true);
                    pwm.getPseudonym().setScope(scope);
                    pwm.getPseudonym().setPseudonymUID(pseudonymUID);

                    instance.credentialManager.storePseudonym(pwm);

                    // System.out.println(" - " + scope + " : pseudo Created! : " +
                    // XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
                    System.out.println(" - " + scope + " : pseudo Created!");
                }

            }


        }
        System.out.println("UserHelper Done");

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
        StoredDomainPseudonym dp =
                new StoredDomainPseudonym(URI.create(scope), secretUid,
                        URI.create(IdemixConstants.groupParameterId));
        cryptoEvidence.getAny().add(XMLSerializer.getInstance().serializeAsElement(dp));
        pwm.setCryptoParams(cryptoEvidence);
        return pwm;
    }


    public static String[] filesToResourceList(File folder, final String filter) throws Exception {
        File[] resourceFileList = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.startsWith(filter)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        System.out.println("Resource files : " + filter + " : " + resourceFileList + " : " + resourceFileList.length);
        String[] resourceList = new String[resourceFileList.length];

        for (int ix = 0; ix < resourceFileList.length; ix++) {
            resourceList[ix] = resourceFileList[ix].getAbsolutePath();
            System.out.println("Found Resource! : " + resourceList[ix]);
        }
        return resourceList;
    }

    protected void copySystemParameters() throws IOException {
        this.copySystemParameters("issuer_system_params_bridged");
        this.copySystemParameters("issuer_system_params_bridged_human_readable_only_for_reference.xml");
    }

    private void copySystemParameters(String filename) throws IOException {
        File file = null;
        file = new File("src" + File.separatorChar + "test"
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

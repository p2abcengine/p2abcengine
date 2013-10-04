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

package eu.abc4trust.ri.service.it.inspector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.junit.Test;

import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.utils.XMLSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.Attribute;
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
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.util.XmlUtils;

public class ITInspector {

    public ITInspector() {
        System.out.println("ITInspector");
    }

    final String baseUrl = "http://localhost:9092/integration-test-inspector";

    ObjectFactory of = new ObjectFactory();

    @Test
    public void inspectPresentationToken() throws Exception {

        this.copySystemParameters();

        System.out.println("---- inspectPresentationToken ----");

        this.initIssuer(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX);


        System.out.println("-- init inspector in service");
        Client client = Client.create();
        Builder initResource =
                client.resource(this.baseUrl + "/inspector/init/soderhamn").accept(MediaType.TEXT_PLAIN);

        String response = initResource.get(String.class);
        System.out.println("-- init inspector in service " + response);

        System.out.println("-- init local engines for issuer, verifier and user");

        this.initVerifierAndUser(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX);

        if(UserHelper.getInstance().credentialManager.listCredentials().size()>0) {
            System.out.println("We allready have credentials!");
        } else {
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
                IssuMsgOrCredDesc resp = null;
                // invoke user
                resp = UserHelper.getInstance().getEngine().issuanceProtocolStep(service_im);
                System.out.println(" - user im : " + resp);

                if (resp.im == null) {
                    System.out.println(" - user cd : " + resp);
                    break;
                }
                System.out.println(" - user im : " + resp);

                // invoke issuer!
                System.out.println(" - invoke ABCE - next step!");

                IssuanceMessageAndBoolean im_and_boolean =
                        IssuanceHelper.getInstance().issueStep(CryptoEngine.IDEMIX, resp.im);

                service_im = im_and_boolean.getIssuanceMessage();
            }
            System.out.println("User now has Credential!");
        }
        System.out.println(" :: " + UserHelper.getInstance().credentialManager.listCredentials());

        // create presentation token!

        System.out.println("Create PresentationPolicy");
        String applicationData = null;
        String policyName = "presentationPolicySoderhamnSchoolWithInspection.xml";
        byte[] nonce = VerificationHelper.getInstance().generateNonce();
        PresentationPolicyAlternatives policy =
                VerificationHelper.getInstance().createPresentationPolicy(policyName, nonce,
                        applicationData, null);
        System.out.println("Create PresentationPolicy " + policy);

        //
        PresentationToken presentationToken =
                UserHelper.getInstance().getEngine().createPresentationToken(policy);
        System.out.println("Created PresentationToken " + presentationToken);
        //    System.out.println("Created PresentationToken "
        //        + XmlUtils.toXml(of.createPresentationToken(presentationToken)));

        VerificationHelper.getInstance().verifyToken(policyName, nonce, applicationData,
                presentationToken);
        System.out.println("Verify OK!");


        // send to inspector!
        Builder inspectResource =
                client.resource(this.baseUrl + "/inspector/inspect?issuedValue=42").accept(MediaType.TEXT_PLAIN);
        System.out.println("Inspect!");

        String inspectionResult =
                inspectResource.post(String.class, this.of.createPresentationToken(presentationToken));

        System.out.println("Inspect ! OK " + inspectionResult);
    }


    // @Test
    public void OLDinspectPresentationToken() throws Exception {


        System.out.println("---- inspectPresentationToken ----");
        ObjectFactory of = new ObjectFactory();

        System.out.println("ITInspector - getPolicy...");
        Client client = Client.create();

        PresentationToken pt;
        try {
            String xml_resource =
                    "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml";
            InputStream is = this.getClass().getResourceAsStream(xml_resource);
            pt = (PresentationToken) XmlUtils.getObjectFromXML(is, false);
            System.out.println(" - sample XML created");
        } catch (Exception e1) {
            e1.printStackTrace();
            System.err.println(" - could init sample XML - create default");
            pt = new PresentationToken();
            pt.setVersion("1.0");
        }

        Builder verifyResource =
                client.resource(this.baseUrl + "/inspect/1").type(MediaType.APPLICATION_XML)
                .accept(MediaType.TEXT_XML);

        JAXBElement<PresentationToken> request = of.createPresentationToken(pt);
        verifyResource.post(request);
        System.out.println(" - done");
    }

    private static final String SODERHAMN_SCHOOL = "SODERHAMN_SCHOOL";
    private static final SpecAndPolicy soderhamn_school = new SpecAndPolicy(SODERHAMN_SCHOOL,
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/issuancePolicySoderhamnSchool.xml");

    public void initIssuer(CryptoEngine issuerEngine, CryptoEngine clientRunEngine)
            throws Exception {
        System.out.println("initIssuer(CryptoEngine " + issuerEngine + " - clientEngine : "
                + clientRunEngine);

        System.out.println("setup IssuanceHelper");
        IssuanceHelper.resetInstance();

        //    File folder;
        String issuer_fileStoragePrefix;
        if (new File("target").exists()) {
            issuer_fileStoragePrefix = "target/issuer_";
        } else {
            issuer_fileStoragePrefix = "integration-test-user/target/issuer_";
        }
        issuer_fileStoragePrefix += issuerEngine.toString().toLowerCase() + "_";

        IssuanceHelper.initInstance(issuerEngine, issuer_fileStoragePrefix, issuer_fileStoragePrefix,
                soderhamn_school);


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
        System.out.println("initIssuerAndUser(CryptoEngine " + issuerEngine + " - clientEngine : "
                + clientRunEngine);

        File folder;
        //String issuer_fileStoragePrefix;
        String verifier_fileStoragePrefix;
        String user_fileStoragePrefix;
        if (new File("target").exists()) {
            verifier_fileStoragePrefix = "target/verifier_";
            user_fileStoragePrefix = "target/user_";
            folder = new File("target");
        } else {
            verifier_fileStoragePrefix = "integration-test-inspector/target/verifier_";
            user_fileStoragePrefix = "integration-test-inspector/target/user_";
            folder = new File("integration-test-inspector/target");
        }
        verifier_fileStoragePrefix += issuerEngine.toString().toLowerCase() + "_";
        user_fileStoragePrefix += clientRunEngine.toString().toLowerCase() + "_";

        String[] inspectorPublicKeyResourceList =
                this.filesToResourceList(folder, "inspector_inspector_publickey");

        System.out.println("Setup VerificationHelper");
        VerificationHelper.resetInstance();

        String[] presentationPolicyResources =
            {"/eu/abc4trust/sampleXml/soderhamn/presentationPolicySoderhamnSchoolWithInspection.xml"};

        String[] credSpecResourceList =
            {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};

        // File systemParamsFile = new File(folder, "issuer_system_params");
        // String systemParamsResource = systemParamsFile.getAbsolutePath();


        String[] issuerParamsResourceList =
                this.filesToResourceList(folder, "issuer_" + issuerEngine.toString().toLowerCase()
                        + "_issuer_params_");

        VerificationHelper.initInstance(issuerEngine, issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList,
                verifier_fileStoragePrefix, presentationPolicyResources);

        System.out.println("VerificationHelper - Done");

        System.out.println("Setup UserHelper");
        UserHelper.resetInstance();

        // todo Inspector keys must be added to init of UserHelper...
        UserHelper.initInstance(clientRunEngine, issuerParamsResourceList, user_fileStoragePrefix,
                credSpecResourceList, inspectorPublicKeyResourceList);

        UserHelper instance = UserHelper.getInstance();
        if (instance.credentialManager.listCredentials().size() > 0) {
            System.out.println("We allready have credentials!");
        } else {
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


    private String[] filesToResourceList(File folder, final String filter) throws Exception {
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
        System.out.println("Resource files : " + resourceFileList + " : " + resourceFileList.length);
        String[] resourceList = new String[resourceFileList.length];

        for (int ix = 0; ix < resourceFileList.length; ix++) {
            resourceList[ix] = resourceFileList[ix].getAbsolutePath();
            System.out.println("Found Resource! : " + resourceList[ix]);
        }
        return resourceList;
    }


    //   ===================================================

    // @Test
    public void inspectPresentationTokenFromResource() throws Exception {
        System.out.println("=========== LOCAL INSPECT - IN VM ==============");

        URI[] inspectorPublicKeyUIDs = {URI.create("urn:soderhamn:inspectorpk")};
        String fileStoragePrefix;
        String presentationTokenResoruce;
        String spResource;
        if (new File("target").exists()) {
            fileStoragePrefix = "target/inspector_";
            presentationTokenResoruce = "target/";
            spResource = "target/issuer_idemix_system_params_bridged";
        } else {
            fileStoragePrefix = "integration-test-inspector/target/inspector_";
            presentationTokenResoruce = "integration-test-inspector/target/";
            spResource = "integration-test-inspector/target/issuer_idemix_system_params_bridged";
        }
        //  presentationTokenResoruce += "presentationTokenWithInspection.jaxbobj";
        presentationTokenResoruce += "presentationTokenWithInspection.xml";

        // Init Inspector :
        String[] credSpecResourceList =
            {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};

        InspectorHelper.resetInstance();
        InspectorHelper.initInstance(fileStoragePrefix, fileStoragePrefix, spResource, inspectorPublicKeyUIDs,
                credSpecResourceList);
        System.out.println("SysParaams " + InspectorHelper.getInstance().keyManager.getSystemParameters());
        // Load Presentation Token from resource
        System.out.println(" - resource : " + presentationTokenResoruce + " : " + presentationTokenResoruce.endsWith("xml"));
        PresentationToken  presentationToken = null;
        if(presentationTokenResoruce.endsWith("xml")) {
            String ptXml = FileSystem
                    .loadObjectFromResource(presentationTokenResoruce);
            //      System.out.println("XML : " + ptXml);
            presentationToken = (PresentationToken) XmlUtils.getObjectFromXML(new ByteArrayInputStream(ptXml.getBytes()), true);
        } else {
            presentationToken = FileSystem
                    .loadObjectFromResource(presentationTokenResoruce);
        }


        //    .toXml(of.createPresentationToken(presentationToken));
        //    System.out.println("Inspect PresentationToken " + ptXml);


        List<Attribute> atts = InspectorHelper.getInstance().inspect(presentationToken);
        if(atts != null) {
            System.out.println("- inspected attributes : " + atts);
            for(Attribute a : atts) {
                //        MyAttributeValue v = MyAttributeEncodingFactory.parseValueFromEncoding(a.getAttributeDescription().getEncoding(), "42", null);

                System.out.println("- " + a.getAttributeUID() + " : " + a.getAttributeValue() + " : " + a.getAttributeDescription().getDataType() + " : " + a.getAttributeDescription().getEncoding());
            }
        } else {
            System.out.println("- inspected attributes is null : " + atts);
        }

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

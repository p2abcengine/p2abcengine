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

package eu.abc4trust.ri.servicehelper.smartcard;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceMessageAndBoolean;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

@Ignore
public class SetupPatrasPilotTest {

    public static String ISSUER_RESOURCES_FOLDER = "sc_issuer_resources";
    public static String ISSUER_STORAGE_FOLDER = "sc_issuer_storage";
    public static String USER_STORAGE_FOLDER = "sc_user_storage";

    public static final String CREDSPEC_UNIVERSITY = "credSpecUniversity";
    public static final String CREDSPEC_COURCE = "credSpecCource";

    public static final SpecAndPolicy university = new SpecAndPolicy(CREDSPEC_UNIVERSITY,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
    public static final SpecAndPolicy cource = new SpecAndPolicy(CREDSPEC_COURCE,
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
            "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");
    public static final String[] CRED_SPEC_RESOURCE_LIST = {
        "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
    "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"};


    private String getFolderName(String basis) {
        File test = new File("service-helper/target");
        System.out.println("Exists : "+ basis + " : " + test.exists());
        if (! test.exists()) {
            return new File("target/" + basis).getAbsolutePath();
        } else {
            return new File("service-helper/target" + basis).getAbsolutePath();
        }
    }
    @Test
    public void verifyExistenceOfFolders() throws Exception {
        for (String foldername : new String[] {this.getFolderName(ISSUER_RESOURCES_FOLDER), this.getFolderName(ISSUER_STORAGE_FOLDER),
                this.getFolderName(USER_STORAGE_FOLDER)}) {
            File folder = new File(foldername);
            if (!folder.exists()) {
                System.out.println("Create Folder !" + folder.getAbsolutePath());
                boolean created = folder.mkdirs();
                if (!created) {
                    throw new Exception("Could not create Folder : " + foldername);
                }
            } else {
                if (!folder.isDirectory()) {
                    throw new Exception("File exists with name of Folder : " + foldername);
                }
            }
        }
    }

    @Test
    public void setupIssuerParams() throws Exception {
        System.out.println("setupIssuerParams..");
        IssuanceHelper.resetInstance();

        String systemAndIssuerParamsPrefix = this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/";
        String fileStoragePrefix = this.getFolderName(ISSUER_STORAGE_FOLDER) + "/";

        IssuanceHelper.initInstance(CryptoEngine.IDEMIX, systemAndIssuerParamsPrefix,
                fileStoragePrefix, university, cource);

        System.out.println("setupIssuerParams Done");
    }

    @Test
    public void generatePKIKeys() throws Exception {
        System.out.println("generatePKIKeys");
        System.out.println(" - pki");
        PKIKeyTool.generateSignatureKeys(this.getFolderName(ISSUER_STORAGE_FOLDER), "pki_keys");
        System.out.println(" - cas : ");
        PKIKeyTool.generateSignatureKeys(this.getFolderName(ISSUER_STORAGE_FOLDER), "cas_keys");
        System.out.println(" - DONE : ");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void initSmartCard() throws Exception {
        System.out.println("initSmartCard : ISSUER_RESOURCES_FOLDER : " + this.getFolderName(ISSUER_RESOURCES_FOLDER));

        // load PKI
        RSAKeyPair pki_sk_root =
                PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_STORAGE_FOLDER) + "/pki_keys_sk");
        @SuppressWarnings("unused")
        RSAVerificationKey pki_pk_root =
        PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_STORAGE_FOLDER) + "/pki_keys_pk");


        Random random = new Random(42); // PKIKeyTool.random;
        short deviceID = 42;
        // gen pin : 7388 with Random 42
        int newPin = random.nextInt(9999);
        // gen puk
        int newPuk = random.nextInt(999999);
        // gen mac
        byte[] macKeyForBackup = new byte[PKIKeyTool.MAC_KEY_LENGTH / 8];
        random.nextBytes(macKeyForBackup);

        int sc_id_int = random.nextInt(999999999);
        String sc_id = String.format("%09d", sc_id_int);

        // max_length_256
        URI deviceUri = URI.create("secret://software-smartcard-" + sc_id);


        String[] issuer_params_resource_list =
            {this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/issuer_params_urn_patras_issuer_credUniv_idemix",
                this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/issuer_params_urn_patras_issuer_credCourse_idemix"};

        String[] inspectorPublicKeyResourceList = new String[0];

        // ?? TODO : (IDEMIX) UserEngine has to be initialized
        UserHelper.initInstance(CryptoEngine.IDEMIX, issuer_params_resource_list, this.getFolderName(USER_STORAGE_FOLDER)
                + "/", CRED_SPEC_RESOURCE_LIST, inspectorPublicKeyResourceList);

        SystemParameters sysP =
                PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_RESOURCES_FOLDER) + "/system_params_idemix");
        UserHelper.getInstance().keyManager.storeSystemParameters(sysP);


        //
        IssuerParameters issuerParameters_credUniv =
                PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_RESOURCES_FOLDER)
                        + "/issuer_params_urn_patras_issuer_credUniv_idemix");


        //
        Smartcard softwareSmartcard =
                PKIKeyTool.initSoftwareSmartcard(CryptoEngine.IDEMIX, sysP, pki_sk_root, newPin, deviceID, deviceUri);
        System.out.println("DEVICE URI FORM SC : " + softwareSmartcard.getDeviceURI(newPin));

        //
        PKIKeyTool.signIssuerParameters(CryptoEngine.IDEMIX, issuerParameters_credUniv, softwareSmartcard, newPin, pki_sk_root, null, null);

        //
        IssuerParameters issuerParameters_credCourse =
                PKIKeyTool.loadObjectFromResource(this.getFolderName(ISSUER_RESOURCES_FOLDER)
                        + "/issuer_params_urn_patras_issuer_credCourse_idemix");

        PKIKeyTool.signIssuerParameters(CryptoEngine.IDEMIX, issuerParameters_credCourse, softwareSmartcard, newPin, pki_sk_root, null, null);



        // add to mangager
        AbcSmartcardManager smartcardManager = UserHelper.getInstance().smartcardManager;
        System.out.println("smartcardManager : " + smartcardManager);
        System.out.println("smartcardManager : " + smartcardManager.smartcardLoaded(deviceUri));
        smartcardManager.addSmartcard(softwareSmartcard, 7388);
        System.out.println("smartcardManager : " + smartcardManager.smartcardLoaded(deviceUri));

        //
        URI scope = new URI("urn:patras:registration");
        BigInteger pseudonym = softwareSmartcard.computeScopeExclusivePseudonym(newPin, scope);

        System.out.println("Smartcard has been initialized");
        System.out
        .println("PIN   PUK     MAC(as Hex)                       DEVICE                                 PSEUDONYM");
        String save =
                String.format(
                        "%04d  %06d  %s  %s  %s",
                        new Object[] {newPin, newPuk, PKIKeyTool.toHex(macKeyForBackup), deviceUri,
                                pseudonym.toString()});
        System.out.println(save);

        // Store SoftwareSmartcard
        // TODO : SoftwareSmart must be made Serializable
        // PKIKeyTool.storeObjectInFile(softwareSmartcard, USER_STORAGE_FOLDER + "/",
        // "software_smartcard");


        //    // SMART CARD STUFF
        //    PseudonymWithMetadata gotThat_credentialManager =
        //        UserHelper.getInstance().credentialManager.getPseudonym(scope);
        //    System.out.println("gotThat_credentialManager : " + gotThat_credentialManager);
        //
        //    // try to run issuance...
        //    runIssuance();
        //
    }

    ObjectFactory of = new ObjectFactory();

    public void runIssuance() throws Exception {


        Map<String, Object> attributeValueMap = new HashMap<String, Object>();

        attributeValueMap.put("urn:patras:credspec:credUniv:university", "Patras");
        attributeValueMap.put("urn:patras:credspec:credUniv:department", "CTI");
        attributeValueMap.put("urn:patras:credspec:credUniv:matriculationnr", 1234);
        attributeValueMap.put("urn:patras:credspec:credUniv:firstname", "Smartcard");
        attributeValueMap.put("urn:patras:credspec:credUniv:lastname", "User");


        // issuer init
        IssuanceMessage server_im =
                IssuanceHelper.getInstance().initIssuance(CREDSPEC_UNIVERSITY, attributeValueMap);

        System.out.println(" - initial message - server : " + server_im);
        System.out.println(" - initial message - server : "
                + XmlUtils.toXml(this.of.createIssuanceMessage(server_im)));

        System.out.println(" - user 1st step!");
        // user 1st step
        IssuMsgOrCredDesc user_im =
                UserHelper.getInstance().getEngine().issuanceProtocolStep(server_im);
        System.out.println(" - initial message - client - created ");
        System.out.println(" - initial message - client - created " + user_im.im);
        System.out.println(" - initial message - client - created " + user_im.cd);
        System.out.println(" - initial message - client : "
                + XmlUtils.toXml(this.of.createIssuanceMessage(user_im.im), false));

        int stepCount = 1;
        boolean lastmessage = false;
        while (!lastmessage) {

            System.out.println(" - contact server");
            IssuanceMessageAndBoolean server_im_step = IssuanceHelper.getInstance().issueStep(user_im.im);

            // send to server and receive new im
            server_im = server_im_step.im;
            System.out.println(" - got response");
            System.out.println(" - step message - server : " + stepCount + " : "
                    + XmlUtils.toXml(this.of.createIssuanceMessage(server_im), false));

            // process in
            user_im = UserHelper.getInstance().getEngine().issuanceProtocolStep(server_im);
            System.out.println(" - step message - client :" + stepCount);

            lastmessage = (user_im.cd != null);
            if (!lastmessage) {
                System.out.println(" - initial message - step : " + stepCount + " : "
                        + XmlUtils.toXml(this.of.createIssuanceMessage(user_im.im), false));
            }
        }
        System.out.println(" - done...");
        System.out.println(" - done : credentialDescription : "
                + XmlUtils.toXml(this.of.createCredentialDescription(user_im.cd), false));

    }

}

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

@Ignore
public class IdemixIssuerReloadFromStorageInit {

    public static final String STORAGE_PREFIX = "idemix_reload_";
    public static final String ISSUER_STORAGE_PREFIX = STORAGE_PREFIX + "issuer_";
    public static final String USER_STORAGE_PREFIX = STORAGE_PREFIX + "user_";

    @SuppressWarnings("unused")
    public static File getTemporaryStorageFolder() {
        System.out.println("IdemixIssuerReloadFromStorageInitTest.getTemporaryStorageFolder - start");

        String tmpDirName = System.getProperty("java.io.tmpdir", null);
        if(false && (tmpDirName!=null)) {
            File tmpDir = new File(tmpDirName);
            if(tmpDir.exists()) {
                System.out.println(" - try to use tmp folder " + tmpDir.getAbsolutePath());
                File folder = new File(tmpDir, "idemix_tmp_folder");
                if(folder.exists()) {
                    System.out.println(" - idemix_tmp_folder found " + folder.getAbsolutePath());
                    return folder;
                } else {
                    System.out.println(" - idemix_tmp_folder not found ");
                    boolean ok = folder.mkdir();
                    if(ok) {
                        System.out.println(" - idemix_tmp_folder created " + folder.getAbsolutePath());
                        return folder;
                    } else {
                        //
                        System.out.println(" - could not create tmp folder : " + folder.getName());
                    }
                }
            }
        }

        File folder = new File("target");
        if (folder.exists()) {
            return folder;
        }
        folder = new File("abce-components/target");
        if (folder.exists()) {
            return folder;
        }
        // ??
        return null;
    }

    @Test
    public void removeStorageFiles() throws Exception {
        File storageFolder = getTemporaryStorageFolder();
        File[] oldStorageFiles = storageFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.startsWith(STORAGE_PREFIX)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        for(File f : oldStorageFiles) {
            System.out.println("Delete old storage file : " + f.getName());
            f.delete();
        }
    }

    @Test
    public void initIdemixAndIssueKeys() throws Exception {
        System.out.println("IdemixIssuerReloadFromStorageInitTest.initIdemixAndIssueKeys - start");
        File storageFolder = getTemporaryStorageFolder();
        System.out.println(" - storageFolder : " + storageFolder.getAbsolutePath());

        Module issuerModule =
            IdemixIntegrationModuleFactory.newModule(new Random(1234), storageFolder, ISSUER_STORAGE_PREFIX);
        Injector issuerInjector = Guice.createInjector(issuerModule);
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);

        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);

        // read all objects


        CredentialSpecification creditCardSpec =
                (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/idemixIntegration/credentialSpecificationCreditcardVisa.xml"),
                                true);

        // store cred spec
        if (!issuerKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(),
                creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (issuer)");
        }

        // load issuance policy
        IssuancePolicy ip =
                (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisa.xml"),
                                true);

        // create all URIs

        int keyLength = 1024; // TODO: why do we use it at all???
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();
        URI hash = CryptoUriUtil.getHashSha256(); // TODO
        URI revocationId = new URI("revocationUID"); // TODO

        // step 1 - generate system parameters

        SystemParameters sysParams = issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);

        //    saveObject(sysParams, storageFolder, ISSUER_STORAGE_PREFIX, "system_parameters");


        // step 2 - generate and store issuer parameters

        IssuerParameters issuerParameters =
                issuerEngine.setupIssuerParameters(creditCardSpec, sysParams, uid, hash, cryptoMechanism, revocationId);

        this.saveObject(issuerParameters, storageFolder, ISSUER_STORAGE_PREFIX, "issuer_parameters");

        // store parameters for all parties:
        issuerKeyManager.storeIssuerParameters(uid, issuerParameters);

        System.out.println("IdemixIssuerReloadFromStorageInitTest.initIdemixAndIssueKeys - done");

    }

    private void saveObject(Object object, File storageFolder, String issuerStoragePrefix, String name)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(storageFolder, issuerStoragePrefix + name));
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(object);
        oos.flush();
        fos.close();
    }

    @Test
    public void done() {
        System.out.println("IdemixIssuerReloadFromStorageInitTest DONE");
        System.out.println("==========================================");
    }

}

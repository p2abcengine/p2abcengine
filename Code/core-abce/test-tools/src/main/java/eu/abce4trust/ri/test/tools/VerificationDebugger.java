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

package eu.abce4trust.ri.test.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.util.XmlUtils;

public class VerificationDebugger {

    public static boolean verifyTokenAgainstPolicy(
            String presentationPolicyFile, String presentationTokenFile,
            String debuggerResourceFolder) throws IOException,
            URISyntaxException, JAXBException, UnsupportedEncodingException,
            SAXException, FileNotFoundException {
        initVerificationHelper(debuggerResourceFolder);

        PresentationToken presentationToken = getPresentationToken(presentationTokenFile);
        PresentationPolicyAlternatives presentationPolicyAlternatives = getPresentationPolicyAlternatives(presentationPolicyFile);
        String policyId = getPolicyId(presentationPolicyFile);

        PresentationPolicy presentationPolicy = presentationPolicyAlternatives
                .getPresentationPolicy().get(0);
        Message message = presentationPolicy.getMessage();

        byte[] nonce = message.getNonce();
        ApplicationData applicationData = message.getApplicationData();

        String appData = null;
        if (applicationData != null) {
            appData = applicationData.toString();
        }

        try {
            boolean result = VerificationHelper.getInstance().verifyToken(
                    policyId, nonce, appData, presentationToken);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static PresentationPolicyAlternatives getPresentationPolicyAlternatives(
            String presentationPolicyFile) throws UnsupportedEncodingException,
            JAXBException, SAXException, FileNotFoundException {
        InputStream is = new FileInputStream(presentationPolicyFile);
        PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                .getObjectFromXML(is, false);
        return presentationPolicyAlternatives;
    }

    private static PresentationToken getPresentationToken(
            String presentationTokenFile) throws JAXBException,
            UnsupportedEncodingException, SAXException, FileNotFoundException {
        InputStream is = new FileInputStream(presentationTokenFile);
        PresentationToken presentationToken = (PresentationToken) XmlUtils
                .getObjectFromXML(is, false);
        return presentationToken;
    }

    private static String getPolicyId(String presentationTokenFile) {
        int ix = presentationTokenFile.lastIndexOf("/");
        String policyId = presentationTokenFile;
        if (ix != -1) {
            policyId = presentationTokenFile.substring(ix + 1);
        }
        return policyId;
    }

    private static void initVerificationHelper(String debuggerResourceFolder)
            throws IOException,
            URISyntaxException {
        CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;

        VerificationHelper.resetInstance();

        if (!new File("target").exists()) {
            new File("target").createNewFile();
        }

        String fileStoragePrefix = "target/verifier_";

        System.out.println("use storage in " + debuggerResourceFolder + "...");

        File folder = new File(debuggerResourceFolder);
        File[] issuerParamsFileList = findFilesStartingWith(folder,
                "issuer_params_");
        String[] issuerParamsResourceList = convertFileListToStringList(issuerParamsFileList);

        System.out.println("issuerparams files : " + issuerParamsFileList + " : "
                + issuerParamsFileList.length);

        File[] credSpecResourceFileList = findFilesStartingWith(folder,
                "credentialSpecification");
        String[] credSpecResourceList = convertFileListToStringList(credSpecResourceFileList);

        System.out.println("credspec files : " + credSpecResourceFileList
                + " : " + credSpecResourceFileList.length);

        File[] presentationPoliciesResoucesFileList = findFilesStartingWith(
                folder, "presentationPolicy");
        String[] presentationPoliciesResouces = convertFileListToStringList(presentationPoliciesResoucesFileList);

        System.out.println("presentation policy files : "
                + presentationPoliciesResoucesFileList + " : "
                + presentationPoliciesResoucesFileList.length);

        // String[] t = new String[presentationPoliciesResouces.length + 1];
        // System.arraycopy(presentationPoliciesResouces, 0, t, 0,
        // presentationPoliciesResouces.length);
        // t[presentationPoliciesResouces.length] = presentationPolicyFile;
        // presentationPoliciesResouces = t;

        String[] inspectorPublicKeyResourceList = new String[0];

        VerificationHelper.initInstance(cryptoEngine, issuerParamsResourceList,
                credSpecResourceList, inspectorPublicKeyResourceList,
                fileStoragePrefix, presentationPoliciesResouces);
    }

    private static String[] convertFileListToStringList(
            File[] issuerParamsFileList) {
        String[] issuerParamsResourceList = new String[issuerParamsFileList.length];
        for (int ix = 0; ix < issuerParamsFileList.length; ix++) {
            System.out.println(" - "
                    + issuerParamsFileList[ix].getAbsolutePath());
            issuerParamsResourceList[ix] = issuerParamsFileList[ix]
                    .getAbsolutePath();
        }
        return issuerParamsResourceList;
    }

    private static File[] findFilesStartingWith(File folder,
            final String startsWith) {
        File[] issuerParamsFileList = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.startsWith(startsWith)) {
                    System.out.println("Test : " + arg1);
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (issuerParamsFileList == null) {
            return new File[0];
        }
        return issuerParamsFileList;
    }

}


//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abce4trust.ri.test.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
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
            URISyntaxException, JAXBException, SAXException {
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
        List<IssuerParameters> issuerParamsList =
            FileSystem.loadXmlListFromResources(issuerParamsResourceList);

        System.out.println("issuerparams files : " + issuerParamsFileList + " : "
                + issuerParamsFileList.length);

        File[] credSpecResourceFileList = findFilesStartingWith(folder,
                "credentialSpecification");
        String[] credSpecResourceList = convertFileListToStringList(credSpecResourceFileList);
        List<CredentialSpecification> credSpecList =
            FileSystem.loadXmlListFromResources(credSpecResourceList);

        System.out.println("credspec files : " + credSpecResourceFileList
                + " : " + credSpecResourceFileList.length);

        File[] presentationPoliciesResoucesFileList = findFilesStartingWith(
                folder, "presentationPolicy");
        String[] presentationPoliciesResouces = convertFileListToStringList(presentationPoliciesResoucesFileList);

        System.out.println("presentation policy files : "
                + presentationPoliciesResoucesFileList + " : "
                + presentationPoliciesResoucesFileList.length);


        List<InspectorPublicKey> inspectorPublicKeyList = null;
        List<RevocationAuthorityParameters> revAuthList = null;
        
        VerificationHelper.initInstance(null, issuerParamsList,
          credSpecList, inspectorPublicKeyList, revAuthList ,
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


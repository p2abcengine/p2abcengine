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
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.VerificationCall;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
@SuppressWarnings("unused")
public class PilotSoderhamnTest {

    ObjectFactory of = new ObjectFactory();



    private final String[] credSpecResourceList =
        { "/soderhamn_pilot/credentialSpecificationSoderhamnSchool.xml",
            "/soderhamn_pilot/credentialSpecificationSoderhamnChild.xml",
        "/soderhamn_pilot/credentialSpecificationSoderhamnSubject.xml" };

    private final String[] issuerParamsResourceList =
        { "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credSchool_idemix",
            "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credChild_idemix",
        "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credSubject_idemix" };

    private final String[] inspectorPublicKeyResourceList =
        { "/soderhamn_pilot/inspector_publickey_urn_soderhamn_inspectorpk" };

    private final String[] presentationPolicyResources = {};

    private final String[] revocationAuthorityParametersResourceList =
        { "/soderhamn_pilot/revocation_authority_urn_soderhamn_revocationauthority_default" };

    public void init_soderhamn_verifier() throws Exception {
        System.out.println("---- init_soderhamn_verifier ---- ");

        String fileStoragePrefix;
        if( new File("target").exists()) {
            fileStoragePrefix = "target/soderhamn/verifier_";
        } else {
            fileStoragePrefix = "test-tools/target/soderhamn/verifier_";
        }
        // TODO FIX!
        //    String systemParamsResource = null;
//        VerificationHelper.resetInstance();
//        VerificationHelper.initInstance(null, issuerParamsResourceList, this.credSpecResourceList, this.inspectorPublicKeyResourceList, this.revocationAuthorityParametersResourceList, fileStoragePrefix, this.presentationPolicyResources);
    }

    public VerificationCall getVerificationCall(String resource) throws Exception {
        InputStream is = FileSystem.getInputStream(resource);

        VerificationCall vc = (VerificationCall) XmlUtils.getJaxbElementFromXml(is, true).getValue();
        return vc;
    }
    @Test()
    public void test_loadVerificationCall() throws Exception {
        System.out.println("---- test_loadPresentationToken ---- ");
        this.getVerificationCall("/soderhamn_pilot/token_cred_gender_english_fail.xml");
        this.getVerificationCall("/soderhamn_pilot/token_cred_gender_child_fail.xml");

    }


    public void performTestOfVerificationCall(String xml) throws Exception {
        VerificationCall verificationCall = this.getVerificationCall(xml);
        PresentationPolicyAlternatives presentationPolicyAlternatives = verificationCall.getPresentationPolicyAlternatives();
        PresentationToken presentationToken = verificationCall.getPresentationToken();

        boolean ok = VerificationHelper.getInstance().verifyToken(presentationPolicyAlternatives, presentationToken);
        System.out.println("Verification OK : " + ok);
    }

    @Ignore
    @Test()
    public void test_run_gender_english() throws Exception {
        System.out.println("---- test_run_gender_english ---- ");
        this.setupTrustedSSLCA();
        this.init_soderhamn_verifier();

        this.performTestOfVerificationCall("/soderhamn_pilot/token_cred_gender_english_fail.xml");
    }

    // @Test()
    public void test_run_gender_child() throws Exception {
        System.out.println("---- test_run_gender_child ---- ");
        this.setupTrustedSSLCA();
        this.init_soderhamn_verifier();

        this.performTestOfVerificationCall("/soderhamn_pilot/token_cred_gender_child_fail.xml");

    }

    //
    private void setupTrustedSSLCA() {
        System.out.println("setupTrustedSSLCA" );

        try {
            InputStream is = FileSystem
                    .getInputStream("/soderhamn_pilot/cacerts");
            if(is == null) {
                // no cacerts - skip
                System.out.println("No cacerts keystore - skip");
                return;
            }
            System.out.println("- load cacerts form a new java");
            KeyStore cacerts = KeyStore.getInstance("JKS");
            cacerts.load(is, "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate idm_ca = (X509Certificate) cf
                    .generateCertificate(FileSystem
                            .getInputStream("/soderhamn_pilot/idm_ca.pem"));

            System.out.println("- add NSN IDM CA : " + idm_ca.getSubjectX500Principal() + " : " + idm_ca.getSerialNumber());
            cacerts.setCertificateEntry("nsn_idm_ca", idm_ca);

            TrustManagerFactory tmf  = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(cacerts);

            X509TrustManager trustManager = null;
            TrustManager tms[] = tmf.getTrustManagers();
            for (TrustManager tm : tms) {
                if (tm instanceof X509TrustManager) {
                    trustManager = (X509TrustManager)tm;
                    break;
                }
            }

            System.out.println("- created new trust manager " + trustManager.getAcceptedIssuers().length);
            //        for(X509Certificate c : trustManager.getAcceptedIssuers()) {
            //            System.out.println(" " + c.getSubjectX500Principal() + " : " + c.getSerialNumber()) ;
            //        }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());

            System.out.println("- set DefaultSSLSocketFactor to use our own TrustManager..");
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        } catch(Exception e) {
            System.err.println("Failed to add NSN IDM CA Certificate : " + e);
            e.printStackTrace();
        }
    }

}

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
  
  

  private String[] credSpecResourceList =
    { "/soderhamn_pilot/credentialSpecificationSoderhamnSchool.xml",
      "/soderhamn_pilot/credentialSpecificationSoderhamnChild.xml",
      "/soderhamn_pilot/credentialSpecificationSoderhamnSubject.xml" };

  private String[] issuerParamsResourceList = 
    { "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credSchool_idemix",
      "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credChild_idemix",
      "/soderhamn_pilot/issuer_params_urn_soderhamn_issuer_credSubject_idemix" };

  private String[] inspectorPublicKeyResourceList = 
    { "/soderhamn_pilot/inspector_publickey_urn_soderhamn_inspectorpk" };

  private String[] presentationPolicyResources = {};
  
  private String[] revocationAuthorityParametersResourceList = 
    { "/soderhamn_pilot/revocation_authority_urn_soderhamn_revocationauthority_default" };

  public void init_soderhamn_verifier() throws Exception {
    System.out.println("---- init_soderhamn_verifier ---- ");

    String fileStoragePrefix;
    if( new File("target").exists()) {
      fileStoragePrefix = "target/soderhamn/verifier_";
    } else {
      fileStoragePrefix = "test-tools/target/soderhamn/verifier_";
    }

//    String systemParamsResource = null;
    VerificationHelper.resetInstance();
    VerificationHelper.initInstance(CryptoEngine.IDEMIX, /*systemParamsResource, */issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, revocationAuthorityParametersResourceList, fileStoragePrefix, presentationPolicyResources);
  }

  public VerificationCall getVerificationCall(String resource) throws Exception {
    InputStream is = VerificationHelper.getInputStream(resource); 
    
    VerificationCall vc = (VerificationCall) XmlUtils.getJaxbElementFromXml(is, true).getValue();
    return vc;
  }
  @Test()
  public void test_loadVerificationCall() throws Exception {
    System.out.println("---- test_loadPresentationToken ---- ");
    getVerificationCall("/soderhamn_pilot/token_cred_gender_english_fail.xml");
    getVerificationCall("/soderhamn_pilot/token_cred_gender_child_fail.xml");
    
  }

  
  public void performTestOfVerificationCall(String xml) throws Exception {
      VerificationCall verificationCall = getVerificationCall(xml);
      PresentationPolicyAlternatives presentationPolicyAlternatives = verificationCall.getPresentationPolicyAlternatives();
      PresentationToken presentationToken = verificationCall.getPresentationToken();
      
      boolean ok = VerificationHelper.getInstance().verifyToken(presentationPolicyAlternatives, presentationToken);
      System.out.println("Verification OK : " + ok);
  }
  
  @Ignore
  @Test()
  public void test_run_gender_english() throws Exception {
    System.out.println("---- test_run_gender_english ---- ");
    setupTrustedSSLCA();
    init_soderhamn_verifier();
    
    performTestOfVerificationCall("/soderhamn_pilot/token_cred_gender_english_fail.xml");
  }
  
  // @Test()
  public void test_run_gender_child() throws Exception {
    System.out.println("---- test_run_gender_child ---- ");
    setupTrustedSSLCA();
    init_soderhamn_verifier();
    
    performTestOfVerificationCall("/soderhamn_pilot/token_cred_gender_child_fail.xml");
    
  }  
  
  //
  private void setupTrustedSSLCA() {
    System.out.println("setupTrustedSSLCA" );
    
    try {
        InputStream is = VerificationHelper.getInputStream("/soderhamn_pilot/cacerts");
        if(is == null) {
            // no cacerts - skip
            System.out.println("No cacerts keystore - skip");
            return;
        }
        System.out.println("- load cacerts form a new java");
        KeyStore cacerts = KeyStore.getInstance("JKS");
        cacerts.load(is, "changeit".toCharArray());

        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate idm_ca = (X509Certificate) cf.generateCertificate(VerificationHelper.getInputStream("/soderhamn_pilot/idm_ca.pem"));

        System.out.println("- add NSN IDM CA : " + idm_ca.getSubjectX500Principal() + " : " + idm_ca.getSerialNumber());
        cacerts.setCertificateEntry("nsn_idm_ca", idm_ca);
        
        TrustManagerFactory tmf  = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(cacerts);

        X509TrustManager trustManager = null;
        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
          if (tms[i] instanceof X509TrustManager) {
            trustManager = (X509TrustManager)tms[i];
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

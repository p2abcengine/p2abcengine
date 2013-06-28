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

package eu.abc4trust.ri.servicehelper.verifier;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
@SuppressWarnings("unused")
public class VerificationHelperTest {

  ObjectFactory of = new ObjectFactory();
  
  @Test(expected=IllegalStateException.class)
  public void test_notInit() throws Exception {
    VerificationHelper.getInstance();
  }

//  @Test
//  public void test_notIsInit() throws Exception {
//    boolean isInit = VerificationHelper.isInit();
//    Assert.assertFalse(isInit);
//  }

  
  private String[] presentationPolicyResources = { "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyPatrasCourseEvaluation.xml", 
                                                   "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml",
                                                   "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotelBooking.xml" };

  private String[] presentationPolicy = { "presentationPolicyPatrasCourseEvaluation.xml", "presentationPolicySimpleIdentitycard.xml", "presentationPolicyAlternativesHotelBooking.xml" };

  private String[] credSpecResourceList =
    { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml" };

  @Test()
  public void test_init() throws Exception {
    System.out.println("---- test_init ---- ");

    String fileStoragePrefix;
    if( new File("target").exists()) {
      fileStoragePrefix = "target/verifier_";
    } else {
      fileStoragePrefix = "service-helper/target/verifier_";
    }

//    String systemParamsResource = null;
    String[] issuerParamsResourceList = new String[0];
    String[] inspectorPublicKeyResourceList = new String[0];
    VerificationHelper.initInstance(CryptoEngine.MOCK, /*systemParamsResource, */issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, fileStoragePrefix, presentationPolicyResources);
  }

  @Test()
  public void test_initIssuance() throws Exception {
    System.out.println("---- test_initIssuance ---- ");
    VerificationHelper helper = VerificationHelper.getInstance();

    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    String xml = helper.createPresentationPolicy_String("presentationPolicySimpleIdentitycard.xml", nonce, null);

    System.out.println("test_initIssuance  XML : " + xml);
  }

  
  @Test()
  public void test_initApplicationData() throws Exception {
    System.out.println("---- test_initApplicationData ---- ");
    VerificationHelper helper = VerificationHelper.getInstance();

    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    PresentationPolicyAlternatives ppa = helper.createPresentationPolicy("presentationPolicySimpleIdentitycard.xml", nonce, null, null);

//    PresentationPolicyAlternatives ppa = helper.createPresentationPolicy("presentationPolicyAlternativesHotelBooking.xml", null);

    System.out.println("PresentationPolicyAlternatives : " + ppa);
    System.out.println("- original  : " + ppa.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent().size());
    
    String xml = XmlUtils.toXml(of.createPresentationPolicyAlternatives(ppa));
    System.out.println("XML : " + xml);
    
    PresentationPolicyAlternatives converted = (PresentationPolicyAlternatives) XmlUtils.getJaxbElementFromXml(new ByteArrayInputStream(xml.getBytes()), true).getValue(); 
    System.out.println("- converted : " + converted.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent().size());

  }

  @Test()
  public void pactchXml() {
    System.out.println("---- pactchXml ---- ");
    String xml = "<abc:ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\">1994-01-06Z</abc:ConstantValue>";

    String patched = xml.replace("ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\"", "ConstantValue");
    System.out.println("XML : " + patched);
  }
}

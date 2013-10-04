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

package eu.abc4trust.ri.service.unittest.verifier;

import junit.framework.Assert;

import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.util.XmlUtils;

public class UTVerifier {

  public UTVerifier() throws Exception {
    System.out.println("UTVerifier");

    if (VerificationHelper.isInit()) {
      System.out.println(" - Helper already init'ed");

    } else {
      System.out.println(" - Init Helper");
      String fileStoragePrefix = "target/verifier_";

//      String systemParamsResource = null;
      String[] issuerParamsResourceList = new String[0];

      String[] credSpecResourceList =
        { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml" };

      String[] presentationPolicyResourceList =
          {"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml"};

      String[] inspectorPublicKeyResourceList = new String[0];
      VerificationHelper.initInstance(CryptoEngine.IDEMIX, /*systemParamsResource, */issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, fileStoragePrefix, presentationPolicyResourceList);
    }
  }

  static ObjectFactory of = new ObjectFactory();
//
  @Test
  public void testPresentIdcard_AcceptenceTest() throws Exception {
    verifyPresentationToken("/presentationTokens/idcard1.xml");
  }

  @Test
  public void testPresentIdcard_IntegrationTest() throws Exception {
    verifyPresentationToken("/presentationTokens/idcard2.xml");
  }

  private void verifyPresentationToken(String resource) throws Exception {
    PresentationToken presentationToken = (PresentationToken) XmlUtils. getObjectFromXML(UTVerifier.class.getResourceAsStream(resource), true);

    byte[] nonce = VerificationHelper.getInstance().generateNonce();
    boolean ok = VerificationHelper.getInstance().verifyToken("presentationPolicySimpleIdentitycard.xml", nonce, null, presentationToken);
    Assert.assertTrue(ok);
  }

}

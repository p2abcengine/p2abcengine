//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestration;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.ContextGeneratorSequential;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;


public class PolicyCredentialMatcherImplTest extends EasyMockSupport {
  
  private static final URI PSEUDONYM_NAME = URI.create("zz-some-pseudonym-uri");
  private static final String USERNAME = "defaultUser";

  private CredentialManager credentialManager;
  private EvidenceGenerationOrchestration evidenceOrchestration;
  private IdentitySelection identitySelection;
  private ContextGenerator contextGenerator;
  private KeyManager keyManager;
  private Logger logger;

  private PolicyCredentialMatcherImpl pcm;

  @Before
  public void setupMocks() {
    credentialManager = createMock(CredentialManager.class);
    evidenceOrchestration = createMock(EvidenceGenerationOrchestration.class);
    identitySelection = createMock(IdentitySelection.class);
    keyManager = createMock(KeyManager.class);
    contextGenerator = new ContextGeneratorSequential();
    logger = createMock(Logger.class);

    pcm = new PolicyCredentialMatcherImpl(credentialManager, evidenceOrchestration,
                                          contextGenerator, keyManager, logger);
  }

//  @SuppressWarnings("unchecked")
//  @Test
//  public void testPolicyHotel() throws Exception {
//    PresentationPolicyAlternatives hotelPolicy =
//        (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(
//            getClass().getResourceAsStream(
//                "/eu/abc4trust/sampleXml/presentationPolicies/simpleHotelPolicy.xml"), true);
//
//    programCredManager();
//
//    int uiChoice = 0;
//    SptdReturn uiReturn =
//        new SptdReturn(uiChoice, new HashMap<URI, PseudonymMetadata>(), new ArrayList<URI>(),
//            new ArrayList<URI>());
//    uiReturn.chosenInspectors.add(URI.create("http://thebestbank.com/inspector/pub_key_v1"));
//    uiReturn.chosenPseudonyms.add(URI.create("abc4t://nym/2"));
//    Capture<Map<URI, CredentialDescription>> capturedCdList =
//        new Capture<Map<URI, CredentialDescription>>();
//    Capture<List<PresentationTokenDescription>> capturedTokens =
//        new Capture<List<PresentationTokenDescription>>();
//    //Capture<Map<URI, PseudonymWithMetadata>> capturedPwm =
//    //    new Capture<Map<URI, PseudonymWithMetadata>>();
//    Capture<List<List<URI>>> capturedCu = new Capture<List<List<URI>>>();
//    Capture<List<Set<List<URI>>>> capturedPc = new Capture<List<Set<List<URI>>>>();
//    Capture<List<List<Set<URI>>>> capturedIc = new Capture<List<List<Set<URI>>>>();
//    expect(
//        identitySelection.selectPresentationTokenDescription(notNull(Map.class),
//          capture(capturedCdList), notNull(Map.class), notNull(Map.class),
//          capture(capturedTokens), capture(capturedCu),
//            capture(capturedPc), capture(capturedIc))).andReturn(uiReturn);
//
//    Capture<PresentationTokenDescription> presTokenInCreate =
//        new Capture<PresentationTokenDescription>();
//    Capture<List<URI>> credsInCreate = new Capture<List<URI>>();
//    Capture<List<URI>> chosenPseudonyms = new Capture<List<URI>>();
//    Capture<VerifierParameters> verifierParameters = new Capture<VerifierParameters>();
//
//    PresentationToken pt = new PresentationToken();
//    expect(
//        evidenceOrchestration.createPresentationToken(USERNAME,capture(presTokenInCreate), capture(verifierParameters),
//          capture(credsInCreate), capture(chosenPseudonyms))).andReturn(pt);
//
//    logger.info(anyObject(String.class));
//    expectLastCall().anyTimes();
//    
//    PseudonymWithMetadata pwm = new PseudonymWithMetadata();
//    pwm.setPseudonym(new Pseudonym());
//    expect(evidenceOrchestration.createPseudonym(USERNAME,anyObject(URI.class), anyObject(String.class),
//      eq(false), anyObject(URI.class))).andReturn(pwm);
//    credentialManager.storePseudonym(USERNAME,pwm);
//    
//    populateKeyManager();
//    populateCredentialManager();
//
//    replayAll();
//    pcm.createPresentationToken(USERNAME,hotelPolicy, identitySelection);
//    verifyAll();
//
//    Map<URI, CredentialDescription> credentialList = capturedCdList.getValue();
//
//    // Should create the right token
//    assertSame(presTokenInCreate.getValue(), capturedTokens.getValue().get(uiChoice));
//    assertEquals(credsInCreate.getValue(), capturedCu.getValue().get(uiChoice));
//    assertEquals(chosenPseudonyms.getValue(), uiReturn.chosenPseudonyms);
//
//    // There should be 3 credentials: two passports, and one credit card
//    {
//      Set<URI> expectedSet = new HashSet<URI>();
//      expectedSet.add(URI.create("passport/12344546"));
//      expectedSet.add(URI.create("passport/98383309"));
//      expectedSet.add(URI.create("cc/bestbank/7263774644748533"));
//      assertEquals(expectedSet, credentialList.keySet());
//    }
//
//    // Check captured peudonymWithMetadata
//    /*{
//      Map<URI, PseudonymWithMetadata> pwm = capturedPwm.getValue();
//      // Only 1 pseudonym should be proposed: a newly created one
//      assertEquals(2, pwm.size());
//      assertNotNull(pwm.get(PSEUDONYM_NAME));
//      PseudonymWithMetadata p = pwm.get(URI.create("pseudonym/1"));
//      assertNotNull(p);
//      assertEquals("http://www.sweetdreamsuites.com", p.getPseudonym().getScope());
//      assertEquals(false, p.getPseudonym().isExclusive());
//    }*/
//
//    // Automated checking of output presentation policies (first choice only)
//    {
//      PresentationToken hotelTokenOption1 =
//          (PresentationToken) XmlUtils.getObjectFromXML(
//              getClass().getResourceAsStream(
//                  "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml"),
//              true);
//
//      String xmlExpected =
//          XmlUtils.toXml(new ObjectFactory().createPresentationTokenDescription(hotelTokenOption1
//              .getPresentationTokenDescription()));
//      String xmlActual =
//          XmlUtils.toXml(new ObjectFactory().createPresentationTokenDescription(presTokenInCreate
//              .getValue()));
//      assertEquals(xmlExpected, xmlActual);
//    }
//
//    // There should be 2 assignments: check that these are present
//    {
//      List<List<URI>> expectedList = new ArrayList<List<URI>>();
//      {
//        List<URI> expectedAssignment = new ArrayList<URI>();
//        expectedAssignment.add(new URI("passport/12344546"));
//        expectedAssignment.add(new URI("cc/bestbank/7263774644748533"));
//        expectedList.add(expectedAssignment);
//      }
//      {
//        List<URI> expectedAssignment = new ArrayList<URI>();
//        expectedAssignment.add(new URI("passport/98383309"));
//        expectedAssignment.add(new URI("cc/bestbank/7263774644748533"));
//        expectedList.add(expectedAssignment);
//      }
//      assertEquals(expectedList, capturedCu.getValue());
//    }
//
//    // Check that for each assignment we have an inspector
//    {
//      Set<URI> expectedInspectorsPerAttribute = new HashSet<URI>();
//      expectedInspectorsPerAttribute.add(new URI("http://thebestbank.com/inspector/pub_key_v1"));
//      expectedInspectorsPerAttribute.add(new URI("http://admin.ch/inspector/pub_key_v1"));
//      List<Set<URI>> expectedInspectorsPerCredential = new ArrayList<Set<URI>>();
//      expectedInspectorsPerCredential.add(expectedInspectorsPerAttribute);
//      List<List<Set<URI>>> expectedInspectors = new ArrayList<List<Set<URI>>>();
//      for (int i = 0; i < 2; ++i) {
//        expectedInspectors.add(expectedInspectorsPerCredential);
//      }
//      assertEquals(expectedInspectors, capturedIc.getValue());
//    }
//  }

  private void populateCredentialManager() throws CredentialManagerException {
    expect(credentialManager.listSecrets(USERNAME)).andReturn(new ArrayList<SecretDescription>());
    Secret s = new Secret();
    s.setSecretDescription(new SecretDescription());
    s.getSecretDescription().setSecretUID(contextGenerator.getUniqueContext(URI.create("abc4t://secret")));
    expect(evidenceOrchestration.createSecret(USERNAME)).andReturn(s);
    credentialManager.storeSecret(USERNAME,s);
    expect(credentialManager.listSecrets(USERNAME)).andReturn(Collections.singletonList(s.getSecretDescription()));
  }

  private void populateKeyManager() throws Exception {
    {
      URI inspector1 = URI.create("http://thebestbank.com/inspector/pub_key_v1");
      InspectorPublicKey ipk1 = new InspectorPublicKey();
      ipk1.setPublicKeyUID(inspector1);
      expect(keyManager.getInspectorPublicKey(inspector1)).andReturn(ipk1);
    }
    {
      URI inspector2 = URI.create("http://admin.ch/inspector/pub_key_v1");
      InspectorPublicKey ipk2 = new InspectorPublicKey();
      ipk2.setPublicKeyUID(inspector2);
      expect(keyManager.getInspectorPublicKey(inspector2)).andReturn(ipk2);
    }
    
    // Credential spec for passports
    {
      CredentialSpecification passportSpec =
      ((CredentialSpecification) XmlUtils.getObjectFromXML(
          getClass().getResourceAsStream(
              "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml"), true));
      expect(keyManager.getCredentialSpecification(URI.create("http://admin.ch/passport/specification"))).andReturn(passportSpec).atLeastOnce();
      CredentialSpecification passportSpec2 =
      ((CredentialSpecification) XmlUtils.getObjectFromXML(
          getClass().getResourceAsStream(
              "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml"), true));    
      passportSpec2.setSpecificationUID(URI.create("http://www.bundesregierung.de/passport"));
      expect(keyManager.getCredentialSpecification(URI.create("http://www.bundesregierung.de/passport"))).andReturn(passportSpec2).atLeastOnce();
      
      CredentialSpecification ccSpec =
      ((CredentialSpecification) XmlUtils.getObjectFromXML(
          getClass().getResourceAsStream(
              "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml"), true));
      expect(keyManager.getCredentialSpecification(URI.create("http://visa.com/creditcard/specification"))).andReturn(ccSpec).atLeastOnce();  
      
      expect(keyManager.getCredentialSpecification(URI.create("http://governo.it/passport"))).andReturn(null).atLeastOnce();
      expect(keyManager.getCredentialSpecification(URI.create("http://amex.com/amexcard/specification"))).andReturn(null).atLeastOnce();
    }
    
    // Dummy issuer parameters
    {
      IssuerParameters ip = new IssuerParameters();
      ip.setParametersUID(URI.create("issuer"));
      expect(keyManager.getIssuerParameters(anyObject(URI.class))).andReturn(ip).anyTimes();
    }
  }

  private void programCredManager() throws Exception {
    ObjectFactory of = new ObjectFactory();
    // Passports
    {
      List<URI> expectedIssuers = new ArrayList<URI>();
      expectedIssuers.add(new URI("http://admin.ch/passport/issuancekey_v1.0"));
      expectedIssuers.add(new URI("http://governo.it/id/chiave2048"));
      expectedIssuers.add(new URI("http://www.bundesregierung.de/idkarte/schluessel"));
      List<URI> expectedCredSpecs = new ArrayList<URI>();
      expectedCredSpecs.add(new URI("http://admin.ch/passport/specification"));
      expectedCredSpecs.add(new URI("http://governo.it/passport"));
      expectedCredSpecs.add(new URI("http://www.bundesregierung.de/passport"));

      List<CredentialDescription> passportList = new ArrayList<CredentialDescription>();
      {
        Credential passport =
            ((Credential) XmlUtils.getObjectFromXML(
                getClass().getResourceAsStream(
                    "/eu/abc4trust/sampleXml/credentials/credentialPassport.xml"), true));
        passportList.add(passport.getCredentialDescription());
        expect(credentialManager.getCredential(USERNAME,passport.getCredentialDescription().getCredentialUID())).andReturn(passport);
      }
      {
        Credential passport =
            ((Credential) XmlUtils.getObjectFromXML(
                getClass().getResourceAsStream(
                    "/eu/abc4trust/sampleXml/credentials/credentialPassport2.xml"), true));
        passportList.add(passport.getCredentialDescription());
        expect(credentialManager.getCredential(USERNAME,passport.getCredentialDescription().getCredentialUID())).andReturn(passport);
      }

      expect(credentialManager.getCredentialDescription(USERNAME,eq(expectedIssuers), eq(expectedCredSpecs)))
          .andReturn(passportList);
    }
    // Credit cards
    {
      List<URI> expectedIssuers = new ArrayList<URI>();
      expectedIssuers.add(new URI("http://www.amex.com/abc/isskey"));
      expectedIssuers.add(new URI("http://thebestbank.com/cc/issuancekey_v1.0"));
      List<URI> expectedCredSpecs = new ArrayList<URI>();
      expectedCredSpecs.add(new URI("http://visa.com/creditcard/specification"));
      expectedCredSpecs.add(new URI("http://amex.com/amexcard/specification"));

      List<CredentialDescription> ccList = new ArrayList<CredentialDescription>();
      {
        Credential credCard =
            ((Credential) XmlUtils.getObjectFromXML(
                getClass().getResourceAsStream(
                    "/eu/abc4trust/sampleXml/credentials/credentialCreditcard.xml"), true));
        expect(credentialManager.getCredential(USERNAME,credCard.getCredentialDescription().getCredentialUID())).andReturn(credCard).anyTimes();
        ccList.add(credCard.getCredentialDescription());
      }
      {
        Credential credCard =
            ((Credential) XmlUtils.getObjectFromXML(
                getClass().getResourceAsStream(
                    "/eu/abc4trust/sampleXml/credentials/credentialValidCreditCard.xml"), true));
        expect(credentialManager.getCredential(USERNAME,credCard.getCredentialDescription().getCredentialUID())).andReturn(credCard);
        ccList.add(credCard.getCredentialDescription());
      }

      expect(credentialManager.getCredentialDescription(USERNAME,eq(expectedIssuers), eq(expectedCredSpecs)))
          .andReturn(ccList);
    }
    // Pseudonyms
    {
      List<PseudonymWithMetadata> pwmChoice = new ArrayList<PseudonymWithMetadata>();
      PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
      pwm.setPseudonym(of.createPseudonym());
      pwm.getPseudonym().setPseudonymUID(PSEUDONYM_NAME);
      pwmChoice.add(pwm);
      expect(credentialManager.listPseudonyms(USERNAME,"http://www.sweetdreamsuites.com", false)).andReturn(pwmChoice);
    }

  }

  public static void main(String args[]) throws Exception {
    generateEmptyPresentationPolicyAlternatives();
  }

  private static void generateEmptyPresentationPolicyAlternatives() throws Exception {
    // Output same as file eu/abc4trust/sampleXml/presentationPolicies/emptyPolicy.xml

    ObjectFactory of = new ObjectFactory();
    PresentationPolicy pp = of.createPresentationPolicy();
    pp.setPolicyUID(new URI("eu.abc4trust/empty-presentation-policy"));

    PresentationPolicyAlternatives ppa = of.createPresentationPolicyAlternatives();
    ppa.getPresentationPolicy().add(pp);
    ppa.setVersion("1.0");

    System.out.println(XmlUtils.toXml(of.createPresentationPolicyAlternatives(ppa)));
  }
}

//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * This test checks 3 things:
 * That a user can get issued a simple identity card with firstname, lastname, and birthday.
 * That the cryptoengines can handle inspectable attributes.
 * That the inspection engine can decrypt an inspectable attribute of a presentationtoken. 
 */
//@Ignore //Disabled to save time. Similar technique can be used to stress test other components.
public class StressVerifierTest {

    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    
    private static final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");
        
    
    @Test
    @Ignore 
    public void stressVerifierTest() throws Exception {
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_STUDENT_CARD);
        
        this.runTestsSingle(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), PRESENTATION_POLICY_CREDENTIALS, CREDENTIAL_SPECIFICATION_STUDENT_CARD);
        int exitCode = 0;
//        for(Injector i: injectors){
//        	exitCode = i.getInstance(UProveBindingManager.class)
//                    .stop();
//            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        } 
    }

    @Test
    @Ignore
    public void stressIssuerTest() throws Exception {
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_STUDENT_CARD);
        
        this.stressIssuer(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), PRESENTATION_POLICY_CREDENTIALS, CREDENTIAL_SPECIFICATION_STUDENT_CARD);
//        int exitCode = 0;
//        for(Injector i: injectors){
//        	exitCode = i.getInstance(UProveBindingManager.class)
//                    .stop();
//            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
//        } 
    }
 
    public List<Injector> setupEngines(IssuerCryptoEngine chosenEngine, String credentialSpecification) throws Exception{

    	List<Injector> injectors = new ArrayList<Injector>();
        UProveUtils uproveUtils = new UProveUtils();

   /*     Injector revocationInjector = Guice
                .createInjector(new BridgingTestModule(new Random(1231),
                        CryptoEngine.IDEMIX,22999)); 
     */
        Injector revocationInjector = Guice
        			.createInjector(BridgingModuleFactory.newModule(new Random(1231),
        					uproveUtils.getIssuerServicePort()));
        
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        //Construct issuers
        Injector universityInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        chosenEngine, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));
                
        //Construct user
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1987), uproveUtils.getUserServicePort(), revocationProxyAuthority));

        
        //Construct verifier
        Injector hotelInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort(), revocationProxyAuthority)); 


        injectors.add(universityInjector);
        injectors.add(userInjector);
        injectors.add(hotelInjector);
        injectors.add(revocationInjector);


        int keyLength = 1024;
        if(chosenEngine.equals(IssuerCryptoEngine.UPROVE)) keyLength = 2048;

        IssuerAbcEngine universityEngine = universityInjector
                .getInstance(IssuerAbcEngine.class);
        
        // Generate system parameters.
        SystemParameters systemParameters = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            systemParameters = universityEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());
        } else {
            systemParameters = universityEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getUproveMechanism());

            Injector idemixInjector = Guice
                    .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                            IssuerCryptoEngine.IDEMIX, 8893));
            IssuerAbcEngine idemixIssuer = idemixInjector.getInstance(IssuerAbcEngine.class);
            SystemParameters idemixSystemParameters = idemixIssuer
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

            systemParameters.getAny().addAll(idemixSystemParameters.getAny());
        }


        KeyManager universityKeyManager = universityInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager hotelKeyManager = hotelInjector
                .getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);
        
        universityKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        hotelKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);
   
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        URI revParamsUid = REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRrevocationUpdateReference);

        // Store revocationauthority parameters
        universityKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        hotelKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        

        // Setup issuance policies.
        IssuancePolicy studentcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_STUDENT_CARD), true);
        URI studentcardIssuancePolicyUid = studentcardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();
        

        // Load credential specifications.
        CredentialSpecification studentcardCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                credentialSpecification), true);
        
        // Store credential specifications.
        universityKeyManager.storeCredentialSpecification(
                studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        userKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        hotelKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        
        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI engineType = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            engineType = URI.create("Idemix");
        } else {
            engineType = URI.create("Uprove");
        }

        URI revocationId = new URI("revocationUID1");
        IssuerParameters universityStudentcardIssuerParameters = universityEngine
                .setupIssuerParameters(studentcardCredSpec, systemParameters,
                        studentcardIssuancePolicyUid, hash, engineType, revocationId, null);


        // store issuance parameters for government and user.
        universityKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                universityStudentcardIssuerParameters);
        userKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                universityStudentcardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                universityStudentcardIssuerParameters);
        
        return injectors;
    }
    
    private void stressIssuer(Injector universityInjector, Injector userInjector,
            Injector hotelInjector, Injector revocationInjector, String presentationPolicy, String credSpec) throws Exception{
        
    	
    	int amount = 200;
        // Step 1. Get an idcard.
        System.out.println(">> Get idcard.");
        Map<String, Object> studentAtts = this.populateStudentcardAttributes();

        CountDownLatch latch = new CountDownLatch(amount);
        CountDownLatch errorLatch = new CountDownLatch(amount);

        class IssThread extends Thread{
        		private Map<String,Object> studentAtts = null;
        		IssuanceHelper issuanceHelper = new IssuanceHelper();
        		Injector universityInjector = null;
        		Injector userInjector = null;
        		String credSpec = null;
        		CountDownLatch latch = null;
				CountDownLatch errorLatch = null;

				@Override
				public void run() {
					try{
		        		issuanceHelper.issueCredential(universityInjector, userInjector,
		                        credSpec, ISSUANCE_POLICY_STUDENT_CARD,
		                        studentAtts);	

						latch.countDown();
						System.out.println("latch is now: "+latch.getCount());
					}catch(Exception e){
						//try {
				//			this.wait(120);
					//	} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
					//		e1.printStackTrace();
				//		}
						System.out.println("I DID IT!!0");
						e.printStackTrace();
						latch.countDown();
						errorLatch.countDown();
					}
				}
				
				public void setup(Injector uni, Injector user, String cred, CountDownLatch latch, CountDownLatch errorLatch, Map<String,Object> atts){
					this.credSpec = cred;
					this.universityInjector = uni;
					this.userInjector = user;
					this.studentAtts =atts;
					this.latch = latch;
					this.errorLatch = errorLatch;
				}
        }
		
        IssThread[] threads = new IssThread[amount];
		        
        for(int  i =0; i< amount; i++){
		     	   
        	threads[i] = new IssThread();        	   

        	threads[i].setup(universityInjector, userInjector, credSpec, latch, errorLatch, studentAtts);
        }
    
		for(int  i =0; i< amount; i++){
		   threads[i].start();
		}
		latch.await();
		System.out.println("errors: "+errorLatch.getCount());
		assertEquals(errorLatch.getCount(),amount);

     	   
        
        
        
      //  PresentationToken pt = this.createPresentationTokenWithRevocation(
      //          issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy, REVOCATION_PARAMETERS_UID);
    }

       private void runTestsSingle(Injector universityInjector, Injector userInjector,
               Injector hotelInjector, Injector revocationInjector, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
    	   IssuanceHelper issuanceHelper = new IssuanceHelper();
           System.out.println(">> Get idcard.");
           this.issueAndStoreStudentCard(universityInjector, userInjector,
                   issuanceHelper, credSpec);


           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           
           int amount = 1000;
           
           @SuppressWarnings("unchecked")
		Pair<PresentationToken, PresentationPolicyAlternatives>[] tokens = new Pair[amount];
           
           
           VerifierAbcEngine verifier = hotelInjector.getInstance(VerifierAbcEngine.class);
           CountDownLatch latch = new CountDownLatch(amount);
           CountDownLatch errorLatch = new CountDownLatch(amount);
           
           class VerThread extends Thread{
				private PresentationToken pt = null;
				private PresentationPolicyAlternatives ppa = null;
				private VerifierAbcEngine engine = null;
				CountDownLatch latch = null;
				CountDownLatch errorLatch = null;

				@Override
				public void run() {
					try{
						PresentationTokenDescription pte = engine.verifyTokenAgainstPolicy(ppa,  pt, false);
						assertNotNull(pte);
						latch.countDown();
					}catch(Exception e){
						latch.countDown();
						errorLatch.countDown();
						e.printStackTrace();
					}
				}
				
				public void setup(PresentationToken ipt, PresentationPolicyAlternatives ippa, VerifierAbcEngine ver, CountDownLatch latch, CountDownLatch errorLatch){
					pt = ipt;
					ppa = ippa;
					engine = ver;
					this.latch = latch;
					this.errorLatch = errorLatch;
				}

        	   
           }
           VerThread[] threads = new VerThread[amount];
           
           for(int  i =0; i< amount; i++){
        	   tokens[i] = this.createPresentationTokenWithRevocation(
        		                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy, REVOCATION_PARAMETERS_UID);
        	   
        	   threads[i] = new VerThread();        	   

        	   threads[i].setup(tokens[i].first(), tokens[i].second(), verifier, latch, errorLatch);
           }
System.out.println(">>>\n>>>\n>>>\n      STARTING VERIFICATION\n>>>\n>>>\n>>>");           
           for(int  i =0; i< amount; i++){
        	   threads[i].start();
           }
           latch.await();
           assertEquals(errorLatch.getCount(),amount);
         //  PresentationToken pt = this.createPresentationTokenWithRevocation(
         //          issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy, REVOCATION_PARAMETERS_UID);
       }
       
  
       private void issueAndStoreStudentCard(Injector governmentInjector,
               Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                       throws Exception {
           Map<String, Object> studentAtts = this.populateStudentcardAttributes();
           issuanceHelper.issueCredential(governmentInjector, userInjector,
                   credSpec, ISSUANCE_POLICY_STUDENT_CARD,
                   studentAtts);
       }
       

       private Map<String, Object> populateStudentcardAttributes() {
           Map<String, Object> att = new HashMap<String, Object>();
           att.put("FirstName", NAME);
           att.put("LastName", LASTNAME);
           att.put("Birthday", "1912-11-11Z");
           //att.put("StudentNumber", 1000);
           //att.put("Issued", "2012-11-11Z");
          // att.put("Expires", "2015-11-11Z");
          // att.put("IssuedBy", "University of X");
           return att;
       }
       
       
       private Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationTokenWithRevocation(IssuanceHelper issuanceHelper,
               Injector hotelInjector, Injector userInjector,
               int pseudonymChoice, String presentationPolicy, URI revParamsUid)
                       throws Exception {
    	   int presentationTokenChoice = 0;
           List<URI> chosenInspectors = new LinkedList<URI>();
           
           InputStream resourceAsStream = this.getClass().getResourceAsStream(
                   presentationPolicy);
           PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
           
           for(PresentationPolicy pp: presentationPolicyAlternatives.getPresentationPolicy()){
        	   for(CredentialInPolicy cip: pp.getCredential()){
        		   for(AttributeInPolicy aip: cip.getDisclosedAttribute()){
        			   chosenInspectors.add(aip.getInspectorAlternatives().getInspectorPublicKeyUID().get(0));  
        		   }        	   
        	   }
        	   
           }
           
           VerifierAbcEngine verifierEngine = hotelInjector
                   .getInstance(VerifierAbcEngine.class);
        //   RevocationInformation revocationInformation = verifierEngine
        //           .getLatestRevocationInformation(revParamsUid);
           PolicySelector polSelect = new PolicySelector(presentationTokenChoice, chosenInspectors,pseudonymChoice);
           
           
           Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                   .createPresentationToken(userInjector, userInjector,
                		   presentationPolicy/*, revocationInformation*/, polSelect);

           // Store all required cred specs in the verifier key manager.
           KeyManager hotelKeyManager = hotelInjector.getInstance(KeyManager.class);
           KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

           PresentationToken pt = p.first();
           assertNotNull(pt);
           for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
               hotelKeyManager.storeCredentialSpecification(
                       cit.getCredentialSpecUID(),
                       userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
           }

           return p;
       }
       
}
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
package eu.abc4trust.abce.pertubationtests.section4;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.SynchronizedIssuerAbcEngineImpl;
import eu.abc4trust.abce.external.user.SynchronizedUserAbcEngineImpl;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.SynchronizedVerifierAbcEngineImpl;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.pertubationtests.section4.StressTestModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 4.1.1, 
 */
public class Test11 {
	
    private static final String USERNAME = "username";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 4.1.1 log");
    private ObjectFactory of = new ObjectFactory();

    /*
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private URI inspectoruid = null;
//	private Injector revocationInjector = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
//	private Injector inspectorInjector = null;
	private UProveUtils uproveUtils;
		
	private RevocationProxyAuthority revocationProxyAuthority = null;

	private IssuerAbcEngine issuerEngine = null;
	private UserAbcEngine userEngine = null;
	private VerifierAbcEngine verifierEngine = null;
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;
	private IssuerParameters issuerParameters = null;
	private Random random =null;
	private IssuancePolicy idcardIssuancePolicy;
	private PresentationPolicyAlternatives presentationPolicyAlternatives;
	*/
	
	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-4.1.1.log");
		logger.addHandler(fh);
		
	}
	
	
	public void setup(IssuerCryptoEngine ce) throws Exception {
	}
	
	
	@Test
	public void testR500S60Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(500, 60, "concurrent-verification-R500-S60", "UPROVE", IssuerCryptoEngine.UPROVE);
	}
	
	@Test
	public void testR500S120Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(500, 120, "concurrent-verification-R500-S120", "UPROVE", IssuerCryptoEngine.UPROVE);
	}

	@Test
	public void testR500S180Uprove()throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(500, 180, "concurrent-verification-R500-S180", "UPROVE", IssuerCryptoEngine.UPROVE);
	}

	@Test
	public void testR1000S60Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1000, 60, "concurrent-verification-R1000-S60", "UPROVE", IssuerCryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1000S120Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1000, 120, "concurrent-verification-R1000-S120", "UPROVE", IssuerCryptoEngine.UPROVE);
	}

	@Test
	public void testR1000S180Uprove()throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1000, 180, "concurrent-verification-R1000-S180", "UPROVE", IssuerCryptoEngine.UPROVE);
	}

	@Test
	public void testR1500S60Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1500, 60, "concurrent-verification-R1500-S60", "UPROVE", IssuerCryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1500S120Uprove() throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1500, 120, "concurrent-verification-R1500-S120", "UPROVE", IssuerCryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1500S180Uprove()throws Exception{
		setup(IssuerCryptoEngine.UPROVE);
		runTestMany(1500, 180, "concurrent-verification-R1500-S180", "UPROVE", IssuerCryptoEngine.UPROVE);
	}
	
	
	@Test
	public void testR500S60Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(500, 60, "concurrent-verification-R500-S60", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	
	@Test
	public void testR500S120Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(500, 120, "concurrent-verification-R500-S120", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR500S180Idemix()throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(500, 180, "concurrent-verification-R500-S180", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1000S60Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1000, 60, "concurrent-verification-R1000-S60", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	
	@Test
	public void testR1000S120Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1000, 120, "concurrent-verification-R1000-S120", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1000S180Idemix()throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1000, 180, "concurrent-verification-R1000-S180", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1500S60Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1500, 60, "concurrent-verification-R1500-S60", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
		
	@Test
	public void testR1500S120Idemix() throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1500, 120, "concurrent-verification-R1500-S120", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1500S180Idemix()throws Exception{
		setup(IssuerCryptoEngine.IDEMIX);
		runTestMany(1500, 180, "concurrent-verification-R1500-S180", "IDEMIX", IssuerCryptoEngine.IDEMIX);
	}
	
	
	public synchronized void runTestMany(int request, long seconds, 
			String testName, String engine, IssuerCryptoEngine ce) throws Exception{
    	try{
    		this.wait(3000);
    		
    		URI uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
    		URI revParsUid = URI.create("urn:revocation:uid");
    		URI inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
    		URI hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
    		URI algorithmId = CryptoUriUtil.getIdemixMechanism();
    		Random random = new Random();

            Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                    new Random(1987)));//, revocationProxyAuthority));
    		
    		Injector issuerInjector = Guice
    				.createInjector(StressTestModuleFactory.newModule(new Random(1231),
    						ce));//, revocationProxyAuthority));

            Injector verifierInjector = Guice
                    .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 
          
    		UserAbcEngine userEngine = new SynchronizedUserAbcEngineImpl(userInjector
    				.getInstance(UserAbcEngine.class));
            
    		IssuerAbcEngine issuerEngine = new SynchronizedIssuerAbcEngineImpl(issuerInjector
    				.getInstance(IssuerAbcEngine.class));
    		
    		VerifierAbcEngine verifierEngine = new SynchronizedVerifierAbcEngineImpl(verifierInjector
    				.getInstance(VerifierAbcEngine.class));
    		 
    		SystemParameters syspars = SystemParametersUtil.getDefaultSystemParameters_1024();

    		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
//    		inspectorInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		
    		CredentialSpecification idcardCredSpec = (CredentialSpecification) XmlUtils
    	                .getObjectFromXML(
    	                        this.getClass().getResourceAsStream(
    	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);
    		
    		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

    		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
    		
    		issuerInjector.getInstance(KeyManager.class).storeCredentialSpecification(
    	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
    		
      //      inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
      //              idcardCredSpec.getSpecificationUID(), idcardCredSpec);
            IssuancePolicy idcardIssuancePolicy = (IssuancePolicy) XmlUtils
                    .getObjectFromXML(
                            this.getClass().getResourceAsStream(
                                    ISSUANCE_POLICY_ID_CARD), true);
            URI idcardIssuancePolicyUid = idcardIssuancePolicy.getCredentialTemplate().getIssuerParametersUID();

        	PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                    .getObjectFromXML(
                    		this.getClass().getResourceAsStream(
                                    PRESENTATION_POLICY_ID_CARD), true);

        	IssuerParameters issuerParameters = null;
    		try{
    			issuerParameters = issuerEngine.setupIssuerParameters(idcardCredSpec, syspars, uid, hash, algorithmId, revParsUid, null);
    		}catch(Exception e){
    			logger.info("Failed to create IssuerParameters");
    		}
            userInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
                    issuerParameters);
            issuerInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
                    issuerParameters);
            verifierInjector.getInstance(KeyManager.class).storeIssuerParameters(idcardIssuancePolicyUid,
                    issuerParameters);


    		
    		
    		
    		
    		
    		logger.info("Running test with "+request+" concurrent requests for "+seconds+" seconds and engine "+engine+".");    		
    		logger.info("Processing initial steps, ie. issuance and creation of presentation tokens.");
    		
    		File file = new File("PA411_VerifyTokenAgainstPolicy.csv");
    		FileOutputStream outputStream = null;
    		try {
    			outputStream = new FileOutputStream(file,true);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		}
    		String line = ""+engine+","+request+","+seconds;
    		
    		issueCredential(idcardCredSpec, issuerEngine, userEngine, idcardIssuancePolicy);
    		
    		Collection<UserThread> userThreads = new LinkedList<UserThread>();
        	for (int i=0; i< request; i++){
    			PresentationToken pt = createPresentationToken(userInjector, presentationPolicyAlternatives);
                userThreads.add(new UserThread(pt, verifierEngine, presentationPolicyAlternatives, "test-"+request+"#"+i));
    		}
    		logger.info("Ready to start concurrent requests.");
    		long startExecutionTime = System.currentTimeMillis()/1000;
    		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    		
    		ExecutorService executor = Executors.newFixedThreadPool(request);
    		logger.info("Starting executor with timeout: "+seconds);
    		executor.invokeAll(userThreads, seconds, TimeUnit.SECONDS);
    		//executor.shutdownNow();
    		executor.shutdown();
    		//executor.awaitTermination(200, TimeUnit.SECONDS);
    		
    		long endExecutionTime = System.currentTimeMillis()/1000;
    		long excutionTime = endExecutionTime - startExecutionTime;
    			
    		int totalRequests = 0;
    		int totalErrors = 0;
    		long actualMemoryUsage = 0;
    		for(UserThread ut : userThreads){
 			   totalRequests+=ut.getRequestAttempts();
 			   totalErrors+=ut.getRequestErrors();
 			   if(ut.getMaxUsedMemory() > actualMemoryUsage)
 				   actualMemoryUsage = ut.getMaxUsedMemory();
    		}
    		actualMemoryUsage-=startUsedMemory;
    		actualMemoryUsage=actualMemoryUsage/ 1024 / 1024;
    		logger.info(testName+" Finished. total requests: "+totalRequests+" total errors: "+totalErrors);
    			
    		line+=","+totalRequests+","+totalErrors+","+excutionTime+","+actualMemoryUsage+"\n";
    		outputStream.write(line.getBytes());
    		
    		outputStream.close();
    	}catch(Exception e){
    		logger.info(e.getMessage());
    		e.printStackTrace();
    	}
	}
	
	
	private class UserThread implements Callable<Boolean>{
		private PresentationToken pt;
		private VerifierAbcEngine verifierEngine;
		private PresentationPolicyAlternatives presentationPolicyAlternatives;
		private int requestAttempts =0;
		private int requestErrors =0;
		private long maxUsedMemory = 0;
		private String title;
		
		UserThread(PresentationToken pt, VerifierAbcEngine verifierEngine,
				PresentationPolicyAlternatives presentationPolicyAlternatives, String title){
			this.pt = pt;
			this.verifierEngine = verifierEngine;
			this.presentationPolicyAlternatives = presentationPolicyAlternatives;
			this.title = title;
		}
		
		public int getRequestAttempts(){
			return this.requestAttempts;
		}
		
		public int getRequestErrors(){
			return this.requestErrors;
		}
		public long getMaxUsedMemory(){
			return this.maxUsedMemory;
		}
		
		public Boolean call() {
			while (!Thread.currentThread().isInterrupted()) {
				try{
					requestAttempts++;
					PresentationTokenDescription ptd = verifierEngine.
							verifyTokenAgainstPolicy(presentationPolicyAlternatives, pt, false);
		    		if(ptd == null){
		    			logger.info("Failed to verify presentation token");
		    		}
		    		System.out.println(title+" finished");
		    		assertNotNull(ptd);
		    		long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					if(memory > maxUsedMemory){
						maxUsedMemory = memory;
					}
				//	return true;
				}catch(Exception e){
					requestErrors++;
					e.printStackTrace();
				}
			}
			return true;
		}
	}
	
	
   
    private void issueCredential(CredentialSpecification idcardCredSpec,
    		IssuerAbcEngine issuerEngine, UserAbcEngine userEngine,
    		IssuancePolicy idcardIssuancePolicy) throws Exception{
		Map<String, Object> att = new HashMap<String, Object>();
        att.put("FirstName", "NAME");
        att.put("LastName", "LASTNAME");
        att.put("Birthday", "1990-02-06Z");
        List<Attribute> issuerAtts = this.populateIssuerAttributes(
                att, idcardCredSpec, new Random());
   	
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(
                idcardIssuancePolicy, issuerAtts);
        assertFalse(issuerIm.isLastMessage());
        IssuanceReturn userIm = userEngine.issuanceProtocolStep(USERNAME, issuerIm
                .getIssuanceMessage());

        while (!issuerIm.isLastMessage()) {

            assertNotNull(userIm.im);
            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            assertNotNull(issuerIm.getIssuanceMessage());
            userIm = userEngine.issuanceProtocolStep(USERNAME, issuerIm
                    .getIssuanceMessage());

            boolean userLastMessage = (userIm.cd != null);
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        assertNull(userIm.im);
        assertNotNull(userIm.cd);
		logger.info("Managed to issue a credential");        
    }
	
    private PresentationToken createPresentationToken(Injector userInjector, 
    		PresentationPolicyAlternatives presentationPolicyAlternatives) throws Exception{
    	
    	UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);
    
    	PresentationToken pt = null;
    	try{
    		pt = userEngine.createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
    		if(pt == null){
    			logger.info("Failed to create presentation token");
    		}
    		assertNotNull(pt);
    		return pt;
    	}catch(Exception e){
    		logger.info("Failed to create presentation token : "+e.toString()+": "+e.getMessage());
    		Assert.fail(e.toString());
    		return null;
    	}
    }
    public List<Attribute> populateIssuerAttributes(
            Map<String, Object> issuerAttsMap,
            CredentialSpecification credentialSpecification, Random random) throws Exception {
        List<Attribute> issuerAtts = new LinkedList<Attribute>();
        ObjectFactory of = new ObjectFactory();

        
        for (AttributeDescription attdesc : credentialSpecification
                .getAttributeDescriptions().getAttributeDescription()) {
            Attribute att = of.createAttribute();
            att.setAttributeUID(URI.create("" + random.nextLong()));
            URI type = attdesc.getType();
            AttributeDescription attd = of.createAttributeDescription();
            attd.setDataType(attdesc.getDataType());
            attd.setEncoding(attdesc.getEncoding());
            attd.setType(type);
            att.setAttributeDescription(attd);
            Object value = issuerAttsMap.get(type.toString());
            if (value != null) {
                issuerAtts.add(att);
                att.setAttributeValue(value);
            }
        }
        return issuerAtts;
    }
    
}
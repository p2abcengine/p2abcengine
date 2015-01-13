//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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



package eu.abc4trust.abce.pertubationtests.tud.section2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;



public class PA_2_1_1 {
	private static final int CONCURRENT_REQUESTS = 50;
	private static final int REQUEST_DURATION = 60;//seconds
	
	
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
//    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
//    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    
    
    private static final Logger logger = java.util.logging.Logger.getLogger("PA Section 2.1.1 log");
   // private ObjectFactory of = new ObjectFactory();

    
	private URI uid = null;
	private URI revParsUid = null;
	private URI hash = null;
	private URI algorithmId = null;
	private Injector userInjector = null;
	private Injector issuerInjector = null;
	private Injector verifierInjector = null;
	private Injector inspectorInjector = null;
	private URI inspectoruid;
	private CredentialSpecification idcardCredSpec = null;
	//private RevocationProxyAuthority revocationProxyAuthority = null;
	private Injector revocationInjector =  null;
	private IssuerAbcEngine issuerEngine = null;
	private SystemParameters syspars = null;
	private String testName="none";
	
	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		logger.setLevel(Level.ALL);
		Handler fh = new FileHandler("PA-Section-2.1.1.log");
		SimpleFormatter simpleFormatter = new SimpleFormatter();
		fh.setFormatter(simpleFormatter);
		
		logger.addHandler(fh);
	}
	
	//@Before
	public void setup() throws Exception {
		
		uid = URI.create("http://my.country/identitycard/issuancekey_v1.0");
		revParsUid = URI.create("urn:revocation:uid");
		inspectoruid = URI.create("http://thebestbank.com/inspector/pub_key_v1");
		hash = URI.create("urn:abc4trust:1.0:hashalgorithm:sha-256");
		algorithmId = Helper.getRevocationTechnologyURI("cl");
		revocationInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
		
//		revocationProxyAuthority = revocationInjector
//				.getInstance(RevocationProxyAuthority.class);

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231))); 

        inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        
        
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
		syspars = issuerEngine.setupSystemParameters(1024);
		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		inspectorInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		revocationInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
		
		
		idcardCredSpec = (CredentialSpecification) XmlUtils
	                .getObjectFromXML(
	                        this.getClass().getResourceAsStream(
	                                CREDENTIAL_SPECIFICATION_ID_CARD), true);
		

		
		userInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);

		verifierInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
		issuerInjector.getInstance(KeyManager.class).storeCredentialSpecification(
	            idcardCredSpec.getSpecificationUID(), idcardCredSpec);
		
        inspectorInjector.getInstance(KeyManager.class).storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);
        
	}

	public void stressTestMaxSetupSystemParameters(int concurrentRequests,int seconds, CryptoEngine chosenEngine) throws Exception{
		logger.info(testName+"  PA Flow: Running test with "+concurrentRequests +" concurrent requests for "+seconds +" seconds and crypto engine type: "+chosenEngine.toString());
		
		File file = new File("PA211_SetupSystemParameters.csv");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = ""+chosenEngine.toString()+","+concurrentRequests+","+seconds;
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
            		chosenEngine));
    
		issuerEngine = issuerInjector
				.getInstance(IssuerAbcEngine.class);
	System.out.println("issuerEngine: "+issuerEngine);
		
		URI cryptoEngine;
		if(chosenEngine==CryptoEngine.IDEMIX)
			cryptoEngine = CryptoUriUtil.getIdemixMechanism();
		else
			cryptoEngine = CryptoUriUtil.getUproveMechanism();
		
		
		CountDownLatch latch = new CountDownLatch(concurrentRequests);
	
		class SetupParametersThread extends Thread{
			private long endTime;
			private IssuerAbcEngine issuerEngine;
			private URI cryptoEngine;
			private int requestAttempts;
			private int requestErrors;
	
			CountDownLatch latch = null;

			private long maxUsedMemory = 0;
			
			public long getMaxUsedMemory() {
				return maxUsedMemory;
			}
			public int getRequestAttempts() {
				return requestAttempts;
			}
			public int getRequestErrors() {
				return requestErrors;
			}
			
			public void setup(long endTime, IssuerAbcEngine issuerEngine, URI cryptoEngine, CountDownLatch latch){
				this.endTime = endTime;
				this.issuerEngine = issuerEngine;
				this.cryptoEngine = cryptoEngine;
				this.latch = latch;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			@Override
			public void run() {
				int securityLevel = 1024;
				
				while (System.currentTimeMillis() < endTime) {
					try{
						requestAttempts++;
						SystemParameters sysParams = null;
						sysParams = issuerEngine
				                .setupSystemParameters(securityLevel);
						if(sysParams == null){
							logger.log(Level.SEVERE,""+testName+"	Failed to create SystemParameters : System Parameters NULL");
							requestErrors++;
						}else{
							//logger.log(Level.INFO,"	Succesfully created SystemParameters");
						}
					
						long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if(memory > maxUsedMemory)
							maxUsedMemory = memory;
						
					}catch(Exception e){
						requestErrors++;
						logger.log(Level.SEVERE,""+testName+"	Failed to create SystemParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}

				}
				//logger.log(Level.INFO,testName+"	Thread Finished");
				latch.countDown();
			}
		}
		
		long endTime = System.currentTimeMillis() + (seconds*1000);
		System.out.println("("+System.currentTimeMillis()+") : " +endTime );
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		SetupParametersThread[] threads = new SetupParametersThread[concurrentRequests];
        
        for(int  i =0; i< concurrentRequests; i++){
		     	   
        	threads[i] = new SetupParametersThread();        	   

        	threads[i].setup(endTime, issuerEngine, cryptoEngine, latch);
        }
    System.out.println("ready to start threads ("+System.currentTimeMillis()+") : " +endTime );
		for(int  i =0; i< concurrentRequests; i++){
		   threads[i].start();
		}
		System.out.println("done starting them all");
		latch.await();
		
		long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(int  i =0; i< concurrentRequests; i++){
			   totalRequests+=threads[i].getRequestAttempts();
			   totalErrors+=threads[i].getRequestErrors();
			   if(threads[i].getMaxUsedMemory() > actualMemoryUsage)
				   actualMemoryUsage = threads[i].getMaxUsedMemory();
		}
		actualMemoryUsage-=startUsedMemory;
		actualMemoryUsage=actualMemoryUsage/ 1024 / 1024;
		logger.info(testName+" Finished. total requests: "+totalRequests+" total errors: "+totalErrors);
		
		line+=","+totalRequests+","+totalErrors+","+excutionTime+","+actualMemoryUsage+"\n";
		outputStream.write(line.getBytes());
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stressTestMaxSetupIssuerParameters(int concurrentRequests,int seconds, CryptoEngine chosenEngine) throws Exception{
		logger.info(testName+"  PA Flow: Running test with "+concurrentRequests +" concurrent requests for "+seconds +" seconds and crypto engine type: "+chosenEngine.toString());
		
		File file = new File("PA211_SetupIssuerParameters.csv");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = ""+chosenEngine.toString()+","+concurrentRequests+","+seconds;
		
		
		setup();
		algorithmId = CryptoUriUtil.getIdemixMechanism();
		CountDownLatch latch = new CountDownLatch(concurrentRequests);
	
		class SetupParametersThread extends Thread{
			private long endTime;
			private SystemParameters sysParameters;
			private int requestAttempts;
			private int requestErrors;
			
			CountDownLatch latch = null;

			private long maxUsedMemory = 0;
			
			public long getMaxUsedMemory() {
				return maxUsedMemory;
			}
			public int getRequestAttempts() {
				return requestAttempts;
			}
			public int getRequestErrors() {
				return requestErrors;
			}
			
			public void setup(long endTime, SystemParameters sysParameters, CountDownLatch latch){
				this.sysParameters = sysParameters;
				this.endTime = endTime;
				this.latch = latch;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			@Override
			public void run() {
				
				while (System.currentTimeMillis() < endTime) {
					try{
						requestAttempts++;
						IssuerParameters ip = null;
						ip = issuerEngine.setupIssuerParameters(idcardCredSpec, sysParameters, uid, hash, algorithmId, revParsUid, null);
						
						if(ip == null){
							logger.log(Level.SEVERE,testName+"	Failed to create IssuerParameters : Issuer Parameters NULL");
							requestErrors++;
						}else{
							//logger.log(Level.INFO,"	Succesfully created IssuerParameters");
						}
						
						long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if(memory > maxUsedMemory)
							maxUsedMemory = memory;
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create IssuerParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}

				}
				//logger.log(Level.INFO,testName+"	Thread Finished");
				latch.countDown();
			}
		}
		
		long endTime = System.currentTimeMillis() + seconds*1000;
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		SetupParametersThread[] threads = new SetupParametersThread[concurrentRequests];
        
        for(int  i =0; i< concurrentRequests; i++){
		     	   
        	threads[i] = new SetupParametersThread();        	   

        	threads[i].setup(endTime, syspars, latch);
        }
    
		for(int  i =0; i< concurrentRequests; i++){
		   threads[i].start();
		}
		
		latch.await();
		long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(int  i =0; i< concurrentRequests; i++){
			   totalRequests+=threads[i].getRequestAttempts();
			   totalErrors+=threads[i].getRequestErrors();
			   if(threads[i].getMaxUsedMemory() > actualMemoryUsage)
				   actualMemoryUsage = threads[i].getMaxUsedMemory();
		}
		actualMemoryUsage-=startUsedMemory;
		actualMemoryUsage=actualMemoryUsage/ 1024 / 1024;
		logger.info(testName+" Finished. total requests: "+totalRequests+" total errors: "+totalErrors);
		
		line+=","+totalRequests+","+totalErrors+","+excutionTime+","+actualMemoryUsage+"\n";
		outputStream.write(line.getBytes());
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public void stressTestMaxSetupRevocationAuthorityParameter(int concurrentRequests,int seconds, CryptoEngine chosenEngine) throws Exception{
		logger.info(testName+"  PA Flow: Running test with "+concurrentRequests +" concurrent requests for "+seconds +" seconds and crypto engine type: "+chosenEngine.toString());
		
		File file = new File("PA211_SetupRevocationAuthorityParameter.csv");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = ""+chosenEngine.toString()+","+concurrentRequests+","+seconds;
		
		setup();

		RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
		
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("https://example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("https://example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(
                URI.create("https://example.org"));
        

        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        class SetupParametersThread extends Thread{
			private long endTime;
			private int requestAttempts;
			private int requestErrors;
			
			private RevocationAbcEngine revocationEngine;
			private Reference revocationInfoReference;
			private Reference nonRevocationEvidenceReference; 
			private Reference nonRrevocationUpdateReference;
			 
			CountDownLatch latch = null;

			private long maxUsedMemory = 0;
			
			public long getMaxUsedMemory() {
				return maxUsedMemory;
			}
			public int getRequestAttempts() {
				return requestAttempts;
			}
			public int getRequestErrors() {
				return requestErrors;
			}
			
			public void setup(long endTime, RevocationAbcEngine revocationEngine, Reference revocationInfoReference, 
					Reference nonRevocationEvidenceReference, Reference nonRrevocationUpdateReference, CountDownLatch latch){
				this.revocationEngine = revocationEngine;
				this.revocationInfoReference = revocationInfoReference;
				this.nonRevocationEvidenceReference = nonRevocationEvidenceReference;
				this.nonRrevocationUpdateReference = nonRrevocationUpdateReference;
				this.endTime = endTime;
				this.latch = latch;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			@Override
			public void run() {
				
				while (System.currentTimeMillis() < endTime) {
					try{
						requestAttempts++;
						RevocationAuthorityParameters revocationAuthorityParameters = null;
						revocationAuthorityParameters = revocationEngine
				                .setupRevocationAuthorityParameters(1024,
				                        algorithmId, revParsUid, revocationInfoReference,
				                        nonRevocationEvidenceReference, nonRrevocationUpdateReference);
						if(revocationAuthorityParameters == null){
							logger.log(Level.SEVERE,testName+"	Failed to create revocationAuthorityParameters : revocationAuthorityParameters NULL");
							requestErrors++;
						}else{
							//logger.log(Level.INFO,"	Succesfully created IssuerParameters");
						}
						
						long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if(memory > maxUsedMemory)
							maxUsedMemory = memory;
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create revocationAuthorityParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}

				}
				//logger.log(Level.INFO,testName+"	Thread Finished");
				latch.countDown();
			}
		}
		
		long endTime = System.currentTimeMillis() + seconds*1000;
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		SetupParametersThread[] threads = new SetupParametersThread[concurrentRequests];
        
        for(int  i =0; i< concurrentRequests; i++){
		     	   
        	threads[i] = new SetupParametersThread();        	   

        	threads[i].setup(endTime, revocationEngine, revocationInfoReference, 
					nonRevocationEvidenceReference, nonRrevocationUpdateReference, latch);
        }
    
		for(int  i =0; i< concurrentRequests; i++){
		   threads[i].start();
		}
		
		latch.await();
		long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(int  i =0; i< concurrentRequests; i++){
			   totalRequests+=threads[i].getRequestAttempts();
			   totalErrors+=threads[i].getRequestErrors();
			   if(threads[i].getMaxUsedMemory() > actualMemoryUsage)
				   actualMemoryUsage = threads[i].getMaxUsedMemory();
		}
		actualMemoryUsage-=startUsedMemory;
		actualMemoryUsage=actualMemoryUsage/ 1024 / 1024;
		logger.info(testName+" Finished. total requests: "+totalRequests+" total errors: "+totalErrors);
		
		line+=","+totalRequests+","+totalErrors+","+excutionTime+","+actualMemoryUsage+"\n";
		outputStream.write(line.getBytes());
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stressTestMaxSetupInspectionPublicKey(int concurrentRequests,int seconds, CryptoEngine chosenEngine) throws Exception{
		logger.info(testName+"  PA Flow: Running test with "+concurrentRequests +" concurrent requests for "+seconds +" seconds and crypto engine type: "+chosenEngine.toString());
		
		File file = new File("PA211_SetupInspectionPublicKey.csv");
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = ""+chosenEngine.toString()+","+concurrentRequests+","+seconds;
		
		
		setup();

        InspectorAbcEngine inspectorEngine = inspectorInjector.getInstance(InspectorAbcEngine.class);
		
	
//        InspectorPublicKey inspectorPubKey = inspectorEngine.setupInspectorPublicKey(1024,
//                    CryptoUriUtil.getIdemixMechanism(),
//                    inspectoruid);
         
		
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        class SetupParametersThread extends Thread{
			private long endTime;
			private int requestAttempts;
			private int requestErrors;
			
			private URI inspectoruid;
			private InspectorAbcEngine inspectorEngine;
			 
			CountDownLatch latch = null;
			private long maxUsedMemory = 0;
			
			public long getMaxUsedMemory() {
				return maxUsedMemory;
			}
			
			public int getRequestAttempts() {
				return requestAttempts;
			}
			public int getRequestErrors() {
				return requestErrors;
			}
			
			public void setup(long endTime, InspectorAbcEngine inspectorEngine, URI inspectoruid, CountDownLatch latch){
				this.inspectorEngine = inspectorEngine;
				this.inspectoruid = inspectoruid;
				this.endTime = endTime;
				this.latch = latch;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			@Override
			public void run() {
				
				while (System.currentTimeMillis() < endTime) {
					try{
						requestAttempts++;
						InspectorPublicKey inspectorPubKey = null;
				        inspectorPubKey = inspectorEngine.setupInspectorPublicKey(syspars,
			                    CryptoUriUtil.getIdemixMechanism(),
			                    inspectoruid, new LinkedList<FriendlyDescription>());
				        
						if(inspectorPubKey == null){
							logger.log(Level.SEVERE,testName+"	Failed to create InspectorPublicKey : InspectorPublicKey NULL");
							requestErrors++;
						}else{
							//logger.log(Level.INFO,"	Succesfully created IssuerParameters");
						}
						
						long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if(memory > maxUsedMemory)
							maxUsedMemory = memory;
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create InspectorPublicKey : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}

				}
				//logger.log(Level.INFO,testName+"	Thread Finished");
				latch.countDown();
			}
		}
		
		long endTime = System.currentTimeMillis() + seconds*1000;
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		SetupParametersThread[] threads = new SetupParametersThread[concurrentRequests];
        
        for(int  i =0; i< concurrentRequests; i++){
		     	   
        	threads[i] = new SetupParametersThread();        	   

        	threads[i].setup(endTime, inspectorEngine, inspectoruid, latch);
        }
    
		for(int  i =0; i< concurrentRequests; i++){
		   threads[i].start();
		}
		
		latch.await();
		long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(int  i =0; i< concurrentRequests; i++){
			   totalRequests+=threads[i].getRequestAttempts();
			   totalErrors+=threads[i].getRequestErrors();
			   if(threads[i].getMaxUsedMemory() > actualMemoryUsage)
				   actualMemoryUsage = threads[i].getMaxUsedMemory();
		}
		actualMemoryUsage-=startUsedMemory;
		actualMemoryUsage=actualMemoryUsage/ 1024 / 1024;
		logger.info(testName+" Finished. total requests: "+totalRequests+" total errors: "+totalErrors);
		
		line+=","+totalRequests+","+totalErrors+","+excutionTime+","+actualMemoryUsage+"\n";
		outputStream.write(line.getBytes());
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



// PERTUBATION TESTS	
	@Test
    public void stressTestMaxSetupSystemParametersIDEMIX() throws Exception {
		testName="stressTestMaxSetupSystemParametersIDEMIX";
		stressTestMaxSetupSystemParameters(500, 60 , CryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1000, 60 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1500, 60 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(500, 120 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1000, 120 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1500, 120 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(500, 180 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1000, 180 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupSystemParameters(1500, 180 , IssuerCryptoEngine.IDEMIX);
		
	//	stressTestMaxSetupSystemParameters(10, 60 , IssuerCryptoEngine.IDEMIX);
  
	}


	@Ignore
	@Test
    public void stressTestMaxSetupSystemParametersUPROVE() throws Exception {
		testName="stressTestMaxSetupSystemParametersUPROVE";
		stressTestMaxSetupSystemParameters(500, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1000, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1500, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(500, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1000, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1500, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(500, 180 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1000, 180 , CryptoEngine.UPROVE);
		stressTestMaxSetupSystemParameters(1500, 180 , CryptoEngine.UPROVE);
	//	stressTestMaxSetupSystemParameters(CONCURRENT_REQUESTS, REQUEST_DURATION, IssuerCryptoEngine.UPROVE);
    }
	
	@Test
    public void stressTestMaxSetupIssuerParametersIDEMIX() throws Exception {
		testName="stressTestMaxSetupIssuerParametersIDEMIX";
		stressTestMaxSetupIssuerParameters(500, 60 , CryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1000, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1500, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1000, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(500, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1000, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupIssuerParameters(1500, 180 , IssuerCryptoEngine.IDEMIX);
//	stressTestMaxSetupIssuerParameters(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
	
	@Test
    public void stressTestMaxSetupRevocationAuthorityParameterIDEMIX() throws Exception {
		testName="stressTestMaxSetupRevocationAuthorityParameterIDEMIX";
		stressTestMaxSetupRevocationAuthorityParameter(500, 60 , CryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1000, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1500, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1000, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(500, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1000, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupRevocationAuthorityParameter(1500, 180 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupRevocationAuthorityParameter(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
	
	@Test
    public void stressTestMaxSetupInspectionPublicKeyIDEMIX() throws Exception {
		testName="stressTestMaxSetupInspectionPublicKeyIDEMIX";
		stressTestMaxSetupInspectionPublicKey(500, 60 , CryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1000, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1500, 60 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1000, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1500, 120 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(500, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1000, 180 , IssuerCryptoEngine.IDEMIX);
//		stressTestMaxSetupInspectionPublicKey(1500, 180 , IssuerCryptoEngine.IDEMIX);
	//	stressTestMaxSetupInspectionPublicKey(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
// END OF  PERTUBATION TESTS

    
    
}
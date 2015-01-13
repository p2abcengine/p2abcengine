//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
package eu.abc4trust.abce.pertubationtests.section2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.inspector.SynchronizedInspectorAbcEngineImpl;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.SynchronizedIssuerAbcEngineImpl;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.revocation.SynchronizedRevocationAbcEngineImpl;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
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
		algorithmId = CryptoUriUtil.getIdemixMechanism();
		revocationInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));
		
//		revocationProxyAuthority = revocationInjector
//				.getInstance(RevocationProxyAuthority.class);

        userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987)));//, revocationProxyAuthority));
		
		issuerInjector = Guice
				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
						CryptoEngine.IDEMIX));//, revocationProxyAuthority));

        verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        inspectorInjector = Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 

        
        
		issuerEngine = new SynchronizedIssuerAbcEngineImpl(issuerInjector
				.getInstance(IssuerAbcEngine.class));
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

	public void stressTestMaxSetupSystemParameters(int concurrentRequests, long seconds, CryptoEngine chosenEngine) throws Exception{
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
            		chosenEngine));//, revocationProxyAuthority));
    
		issuerEngine = new SynchronizedIssuerAbcEngineImpl(issuerInjector
				.getInstance(IssuerAbcEngine.class));
		
		URI cryptoEngine;
		if(chosenEngine==CryptoEngine.IDEMIX)
			cryptoEngine = URI.create("Idemix");
		else
			cryptoEngine = URI.create("UProve");
		
	
		class SetupParametersThread implements Callable<Boolean>{
			private IssuerAbcEngine issuerEngine;
			private URI cryptoEngine;
			private int requestAttempts;
			private int requestErrors;
			
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
			
			public void setup(IssuerAbcEngine issuerEngine, URI cryptoEngine){
				this.issuerEngine = issuerEngine;
				this.cryptoEngine = cryptoEngine;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			
			public Boolean call() {
				int securityLevel = 1024;
				
				while (!Thread.currentThread().isInterrupted()) {
					try{
						requestAttempts++;
						SystemParameters sysParams = null;
						sysParams = issuerEngine
				                .setupSystemParameters(securityLevel);
						if(sysParams == null){
							logger.log(Level.SEVERE,""+testName+"	Failed to create SystemParameters : System Parameters NULL");
							requestErrors++;
						} else{
							long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(memory > maxUsedMemory){
								maxUsedMemory = memory;
							}
							//return true;
						}
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,""+testName+"	Failed to create SystemParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}
				}
				return true;
			}
		}
		
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		Collection<SetupParametersThread> threads = new LinkedList<SetupParametersThread>();
        
        for(int  i =0; i< concurrentRequests; i++){
		     	   
        	SetupParametersThread spt = new SetupParametersThread();        	   

        	spt.setup(issuerEngine, cryptoEngine);
        	threads.add(spt);
        }
    	ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
		logger.info("Starting executor with timeout: "+seconds);
		executor.invokeAll(threads, seconds, TimeUnit.SECONDS);
		executor.shutdown();
	
        
		long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(SetupParametersThread ut : threads){
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
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stressTestMaxSetupIssuerParameters(int concurrentRequests, long seconds, CryptoEngine chosenEngine) throws Exception{
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
		
		class SetupParametersThread implements Callable<Boolean>{
			private SystemParameters sysParameters;
			private int requestAttempts;
			private int requestErrors;
			private String id; 
			
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
			
			public void setup(SystemParameters sysParameters, String id){
				this.sysParameters = sysParameters;
				this.requestAttempts = 0;
				this.requestErrors = 0;
				this.id = id;
			}
			
			public Boolean call() {
				
				while (!Thread.currentThread().isInterrupted()) {
					try{
						requestAttempts++;
						IssuerParameters ip = null;
						ip = issuerEngine.setupIssuerParameters(idcardCredSpec, sysParameters, uid, hash, algorithmId, revParsUid, null);
						System.out.println("id produced params " +id);
						if(ip == null){
							logger.log(Level.SEVERE,testName+"	Failed to create IssuerParameters : Issuer Parameters NULL");
							requestErrors++;
						}else{

							long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(memory > maxUsedMemory)
								maxUsedMemory = memory;
							
						//	return true;
						}
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create IssuerParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}
				}
				return true;
			}
		}
		
		long endTime = System.currentTimeMillis() + seconds*1000;
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		Collection<SetupParametersThread> threads = new LinkedList<SetupParametersThread>();
        
        for(int  i =0; i< concurrentRequests; i++){
        	SetupParametersThread thread = new SetupParametersThread();        	   

        	thread.setup(syspars, ""+i);
        	threads.add(thread);
        }
    
    	ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
		logger.info("Starting executor with timeout: "+seconds);
		executor.invokeAll(threads, seconds, TimeUnit.SECONDS);
		executor.shutdown();
	
        
        long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(SetupParametersThread ut : threads){
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

		RevocationAbcEngine revocationEngine = new SynchronizedRevocationAbcEngineImpl(
				revocationInjector.getInstance(RevocationAbcEngine.class));
		
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(
                URI.create("example.org"));
        

        class SetupParametersThread implements Callable<Boolean>{
			private int requestAttempts;
			private int requestErrors;
			
			private RevocationAbcEngine revocationEngine;
			private Reference revocationInfoReference;
			private Reference nonRevocationEvidenceReference; 
			private Reference nonRrevocationUpdateReference;
			
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
			
			public void setup(RevocationAbcEngine revocationEngine, Reference revocationInfoReference, 
					Reference nonRevocationEvidenceReference, Reference nonRrevocationUpdateReference){
				this.revocationEngine = revocationEngine;
				this.revocationInfoReference = revocationInfoReference;
				this.nonRevocationEvidenceReference = nonRevocationEvidenceReference;
				this.nonRrevocationUpdateReference = nonRrevocationUpdateReference;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
	
			public Boolean call() {
				
				while (!Thread.currentThread().isInterrupted()) {
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
							long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(memory > maxUsedMemory)
								maxUsedMemory = memory;
						//	return true;
						}
						
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create revocationAuthorityParameters : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}
				}
				return true;
			}
		}
		
		long endTime = System.currentTimeMillis() + seconds*1000;
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		Collection<SetupParametersThread> threads = new LinkedList<SetupParametersThread>();
        
        for(int  i =0; i< concurrentRequests; i++){
        	SetupParametersThread thread = new SetupParametersThread();        	   

        	thread.setup(revocationEngine, revocationInfoReference, 
					nonRevocationEvidenceReference, nonRrevocationUpdateReference);
        	threads.add(thread);
        }
    
    	ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
		logger.info("Starting executor with timeout: "+seconds);
		executor.invokeAll(threads, seconds, TimeUnit.SECONDS);
		executor.shutdown();
	
        long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(SetupParametersThread ut : threads){
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
		try {
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stressTestMaxSetupInspectionPublicKey(int concurrentRequests, long seconds, CryptoEngine chosenEngine) throws Exception{
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

        InspectorAbcEngine inspectorEngine = new SynchronizedInspectorAbcEngineImpl(
        		inspectorInjector.getInstance(InspectorAbcEngine.class));
		
	
        class SetupParametersThread implements Callable<Boolean>{
			private int requestAttempts;
			private int requestErrors;
			private ObjectFactory of = new ObjectFactory();
			private URI inspectoruid;
			private InspectorAbcEngine inspectorEngine;
			 
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
			
			public void setup(InspectorAbcEngine inspectorEngine, URI inspectoruid){
				this.inspectorEngine = inspectorEngine;
				this.inspectoruid = inspectoruid;
				this.requestAttempts = 0;
				this.requestErrors = 0;
			}
			
			public Boolean call() {
				while (!Thread.currentThread().isInterrupted()) {
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
							long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
							if(memory > maxUsedMemory)
								maxUsedMemory = memory;
				//			return true;
						}
						
						
					}catch(Exception e){
						requestErrors++;
						
						logger.log(Level.SEVERE,testName+"	Failed to create InspectorPublicKey : "
								+e.getMessage()+"\n				StackTraces: "+Arrays.toString(e.getStackTrace()));
					}

				}
				return true;
			}
		}
		long startExecutionTime = System.currentTimeMillis()/1000;
		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		Collection<SetupParametersThread> threads = new LinkedList<SetupParametersThread>();
        
        for(int  i =0; i< concurrentRequests; i++){
        	SetupParametersThread thread = new SetupParametersThread();        	   

        	thread.setup(inspectorEngine, inspectoruid);
        	threads.add(thread);
        }
    
    	ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
		logger.info("Starting executor with timeout: "+seconds);
		executor.invokeAll(threads, seconds, TimeUnit.SECONDS);
		executor.shutdown();
	
        long endExecutionTime = System.currentTimeMillis()/1000;
		long excutionTime = endExecutionTime - startExecutionTime;
		
		int totalRequests = 0;
		int totalErrors = 0;
		long actualMemoryUsage = 0;
		for(SetupParametersThread ut : threads){
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
		stressTestMaxSetupSystemParameters(1000, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(1500, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(1000, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(1500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(500, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(1000, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupSystemParameters(1500, 180 , CryptoEngine.IDEMIX);
		
		stressTestMaxSetupSystemParameters(10, 60 , CryptoEngine.IDEMIX);
  
	}


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
    public void stressTestMaxSetupIssuerParametersUPROVE() throws Exception {
		testName="stressTestMaxSetupIssuerParametersUPROVE";
		stressTestMaxSetupIssuerParameters(500, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1000, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1500, 60 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(500, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1000, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1500, 120 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(500, 180 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1000, 180 , CryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1500, 180 , CryptoEngine.UPROVE);
	//	stressTestMaxSetupIssuerParameters(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
	
	
	@Test
    public void stressTestMaxSetupIssuerParametersIDEMIX() throws Exception {
		testName="stressTestMaxSetupIssuerParametersIDEMIX";
		stressTestMaxSetupIssuerParameters(500, 6000 , CryptoEngine.UPROVE);
/*		stressTestMaxSetupIssuerParameters(1000, 60 , IssuerCryptoEngine.UPROVE);
		stressTestMaxSetupIssuerParameters(1500, 60 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(500, 120 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(1000, 120 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(1500, 120 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(500, 180 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(1000, 180 , IssuerCryptoEngine.IDEMIX);
		stressTestMaxSetupIssuerParameters(1500, 180 , IssuerCryptoEngine.IDEMIX);
*/	//	stressTestMaxSetupIssuerParameters(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
	
	@Test
    public void stressTestMaxSetupRevocationAuthorityParameterIDEMIX() throws Exception {
		testName="stressTestMaxSetupRevocationAuthorityParameterIDEMIX";
		stressTestMaxSetupRevocationAuthorityParameter(500, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1000, 60 ,CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1500, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1000, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(500, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1000, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupRevocationAuthorityParameter(1500, 180 , CryptoEngine.IDEMIX);
	//	stressTestMaxSetupRevocationAuthorityParameter(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
	
	@Test
    public void stressTestMaxSetupInspectionPublicKeyIDEMIX() throws Exception {
		testName="stressTestMaxSetupInspectionPublicKeyIDEMIX";
		stressTestMaxSetupInspectionPublicKey(500, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1000, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1500, 60 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1000, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1500, 120 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(500, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1000, 180 , CryptoEngine.IDEMIX);
		stressTestMaxSetupInspectionPublicKey(1500, 180 , CryptoEngine.IDEMIX);
	//	stressTestMaxSetupInspectionPublicKey(CONCURRENT_REQUESTS, REQUEST_DURATION , IssuerCryptoEngine.IDEMIX);
    }
// END OF  PERTUBATION TESTS

    
    
}
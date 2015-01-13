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
package eu.abc4trust.abce.pertubationtests.section3;


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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.SynchronizedIssuerAbcEngineImpl;
import eu.abc4trust.abce.external.user.SynchronizedUserAbcEngineImpl;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
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
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Pertubation tests 3.1.1, 
 */
public class Test11 {
	
    private static final String USERNAME = "username";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    
    
    private static final Logger logger = java.util.logging.Logger.getLogger("Section 3.1.1 log");
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
	
	private SystemParameters syspars = null;
	private CredentialSpecification idcardCredSpec = null;
	private IssuerParameters issuerParameters = null;
	private Random random =null;
	private IssuancePolicy idcardIssuancePolicy;*/

	@BeforeClass
	public static void setupLogger() throws SecurityException, IOException {
		Handler fh = new FileHandler("Section-3.1.1.log");
		logger.addHandler(fh);
		
	}
	
	
	public synchronized void setup(CryptoEngine ce) throws Exception {

	}
	

	@Test
	public void testR500S60Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(500, 60, "concurrent-issuanceProtocolStep-R500-S60", "UPROVE", CryptoEngine.UPROVE);
	}
	
	
	@Test
	public void testR500S120Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(500, 120, "concurrent-issuanceProtocolStep-R500-S120", "UPROVE", CryptoEngine.UPROVE);
	}
	
	@Test
	public void testR500S180Uprove()throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(500, 180, "concurrent-issuanceProtocolStep-R500-S180", "UPROVE", CryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1000S60Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1000, 60, "concurrent-issuanceProtocolStep-R1000-S60", "UPROVE", CryptoEngine.UPROVE);
	}
	
	
	@Test
	public void testR1000S120Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1000, 120, "concurrent-issuanceProtocolStep-R1000-S120", "UPROVE", CryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1000S180Uprove()throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1000, 180, "concurrent-issuanceProtocolStep-R1000-S180", "UPROVE", CryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1500S60Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1500, 60, "concurrent-issuanceProtocolStep-R1500-S60", "UPROVE", CryptoEngine.UPROVE);
	}
		
	@Test
	public void testR1500S120Uprove() throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1500, 120, "concurrent-issuanceProtocolStep-R1500-S120", "UPROVE", CryptoEngine.UPROVE);
	}
	
	@Test
	public void testR1500S180Uprove()throws Exception{
		setup(CryptoEngine.UPROVE);
		runTestMany(1500, 180, "concurrent-issuanceProtocolStep-R1500-S180", "UPROVE", CryptoEngine.UPROVE);
	}
	
	
	@Test
	public void testR500S60Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(500, 60, "concurrent-issuanceProtocolStep-R500-S60", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	
	@Test
	public void testR500S120Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(500, 120, "concurrent-issuanceProtocolStep-R500-S120", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR500S180Idemix()throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(500, 180, "concurrent-issuanceProtocolStep-R500-S180", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1000S60Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1000, 60, "concurrent-issuanceProtocolStep-R1000-S60", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	
	@Test
	public void testR1000S120Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1000, 120, "concurrent-issuanceProtocolStep-R1000-S120", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1000S180Idemix()throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1000, 180, "concurrent-issuanceProtocolStep-R1000-S180", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1500S60Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1500, 60, "concurrent-issuanceProtocolStep-R1500-S60", "IDEMIX", CryptoEngine.IDEMIX);
	}
		
	@Test
	public void testR1500S120Idemix() throws Exception{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1500, 120, "concurrent-issuanceProtocolStep-R1500-S120", "IDEMIX", CryptoEngine.IDEMIX);
	}
	
	@Test
	public void testR1500S180Idemix()throws Exception{
		try{
		setup(CryptoEngine.IDEMIX);
		runTestMany(1500, 180, "concurrent-issuanceProtocolStep-R1500-S180", "IDEMIX", CryptoEngine.IDEMIX);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public synchronized void runTestMany(int request, long seconds, String testName, String engine, CryptoEngine ce) throws Exception{
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
    				.createInjector(IntegrationModuleFactory.newModule(new Random(1231),
    						ce));//, revocationProxyAuthority));

            Injector verifierInjector = Guice
                    .createInjector(IntegrationModuleFactory.newModule(new Random(1231)));//, revocationProxyAuthority)); 
          
    		UserAbcEngine userEngine = new SynchronizedUserAbcEngineImpl(userInjector
    				.getInstance(UserAbcEngine.class));
            
    		IssuerAbcEngine issuerEngine = new SynchronizedIssuerAbcEngineImpl(issuerInjector
    				.getInstance(IssuerAbcEngine.class));
    		
    		SystemParameters syspars = SystemParametersUtil.getDefaultSystemParameters_1024();
    		
    		issuerInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		userInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);
    		verifierInjector.getInstance(KeyManager.class).storeSystemParameters(syspars);

    		
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
    		logger.info("Processing initial steps, ie. first leg of issuance.");
    		
    		File file = new File("PA311_IssuanceProtocolStep.csv");
    		FileOutputStream outputStream = null;
    		try {
    			outputStream = new FileOutputStream(file,true);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		}

    		
    		String line = ""+engine+","+request+","+seconds;
    		Collection<UserThread> userThreads = new LinkedList<UserThread>();
    		for (int i=0; i< request; i++){
    			Map<String, Object> att = new HashMap<String, Object>();
                att.put("FirstName", "NAME-"+i);
                att.put("LastName", "LASTNAME-"+i);
                att.put("Birthday", "1990-02-06Z");
                List<Attribute> issuerAtts = this.populateIssuerAttributes(
                        att, idcardCredSpec, random);
           	System.out.println("initial step : "+i);
                IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(
                        idcardIssuancePolicy, issuerAtts);
                assertFalse(issuerIm.isLastMessage());
                IssuanceReturn userIm = userEngine.issuanceProtocolStep(USERNAME, issuerIm
                        .getIssuanceMessage());
                userThreads.add(new UserThread(userIm, issuerEngine, ("req"+request+"sec"+seconds+"-"+i)));
    		}
    		logger.info("Ready to start concurrent requests.");
    		long startExecutionTime = System.currentTimeMillis()/1000;
    		long startUsedMemory  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
   
    		ExecutorService executor = Executors.newFixedThreadPool(request);
    		List<Future<Boolean>> futures = executor.invokeAll(userThreads, seconds, TimeUnit.SECONDS);
    	//	for(Future f: futures){
    	//		f.cancel(true);
    	//	}
    		//executor.shutdownNow();
    		executor.shutdown();
    		
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
		private IssuanceReturn userIm;
		private String title;
		private IssuerAbcEngine issuerEngine;
		private IssuanceMessageAndBoolean issuerIm = null;
		private int requestAttempts =0;
		private int requestErrors =0;
		private long maxUsedMemory = 0;
		
		UserThread(IssuanceReturn userIm2, IssuerAbcEngine issuerEngine, String title){
			this.userIm = userIm2;
			this.issuerEngine = issuerEngine;
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
					this.issuerIm = issuerEngine.issuanceProtocolStep(this.userIm.im);
					System.out.println(title+" finished");
				}catch(Exception e){
					requestErrors++;
					e.printStackTrace();
					
				}
				if(this.issuerIm != null){
					long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					if(memory > maxUsedMemory){
						maxUsedMemory = memory;
					}
					return true;
				}
			}
			return true;
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
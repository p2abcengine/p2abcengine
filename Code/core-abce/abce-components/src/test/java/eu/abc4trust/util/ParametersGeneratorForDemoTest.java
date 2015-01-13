//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class ParametersGeneratorForDemoTest {

	@Test
	public void testFriendlyPolicyDescrGenerator() throws Exception {
		
	    //---------------------------------------------------
        //Setup all instances
        //---------------------------------------------------
		
	    ObjectFactory of = new ObjectFactory();

        Injector userInjector = Guice.createInjector(ProductionModuleFactory.newModuleWithoutPersistance());
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        
        CryptoEngineVerifier cryptoEngineVerifier = userInjector.getInstance(CryptoEngineVerifier.class);       
        CryptoEngineIssuer cryptoEngineIssuer = userInjector.getInstance(CryptoEngineIssuer.class);
               
        
        //---------------------------------------------------
        // Create System Parameters
        //---------------------------------------------------
        
        
        SystemParameters sysParams = SystemParametersUtil.getDefaultSystemParameters_2048();
        userKeyManager.storeSystemParameters(sysParams);

        //---------------------------------------------------
        // Create Issuer Parameters
        //---------------------------------------------------
        
        // for the movie streaming service
        
        URI ipUidVoucher = URI.create("https://movies.idemixcloud.zurich.ibm.com/parameters/voucher");
        FriendlyDescription fdVoucher = new FriendlyDescription();
        fdVoucher.setLang("en");
        fdVoucher.setValue("Movie Streaming Service");
        
        List<FriendlyDescription> listFD = new ArrayList<FriendlyDescription>();
        listFD.add(fdVoucher);
       
        IssuerParametersAndSecretKey isParamsVoucher = cryptoEngineIssuer.setupIssuerParameters(
        		sysParams, 10, URI.create("IDEMIX"), ipUidVoucher, URI.create("notsupported"), listFD);
       
        userKeyManager.storeIssuerParameters(ipUidVoucher, isParamsVoucher.issuerParameters);
        
        System.out.println("Issuer Parameters Movies");
        System.out.println(XmlUtils.toXml(of.createIssuerParameters(isParamsVoucher.issuerParameters)));
        System.out.println(XmlUtils.toXml(of.createIssuerSecretKey(isParamsVoucher.issuerSecretKey)));
        
        // for the eGovernment
                    
        URI ipUidIdCard = URI.create("https://egov.idemixcloud.zurich.ibm.com/parameters/idcard");
        FriendlyDescription fdIdCard = new FriendlyDescription();
        fdIdCard.setLang("en");
        fdIdCard.setValue("eGovernment");
        
        List<FriendlyDescription> listFDegov = new ArrayList<FriendlyDescription>();
        listFDegov.add(fdIdCard);
       
        IssuerParametersAndSecretKey isParamsAndKeyEGov = cryptoEngineIssuer.setupIssuerParameters(
        		sysParams, 10, URI.create("IDEMIX"), ipUidIdCard, URI.create("notsupported"), listFDegov);
       
        userKeyManager.storeIssuerParameters(ipUidIdCard, isParamsAndKeyEGov.issuerParameters);
        System.out.println("Issuer Parameters eGov");
        System.out.println(XmlUtils.toXml(of.createIssuerParameters(isParamsAndKeyEGov.issuerParameters)));
        System.out.println(XmlUtils.toXml(of.createIssuerSecretKey(isParamsAndKeyEGov.issuerSecretKey)));
        
        // global issuer parameters (needed for range proofs)
        
        URI ipUidGlobal = URI.create("https://idemixcloud.zurich.ibm.com/parameters/issuer/global");
        FriendlyDescription fdGlobal = new FriendlyDescription();
        fdGlobal.setLang("en");
        fdGlobal.setValue("Global Issuer");
        
        List<FriendlyDescription> listFDGlobal = new ArrayList<FriendlyDescription>();
        listFDGlobal.add(fdGlobal);
       
        IssuerParametersAndSecretKey isParamsGlobal = cryptoEngineIssuer.setupIssuerParameters(
        		sysParams, 3, URI.create("IDEMIX"), ipUidGlobal, URI.create("notsupported"), listFDGlobal);
       
        userKeyManager.storeIssuerParameters(ipUidGlobal, isParamsGlobal.issuerParameters);
        
        System.out.println("Issuer Parameters Global");
        System.out.println(XmlUtils.toXml(of.createIssuerParameters(isParamsGlobal.issuerParameters)));
        System.out.println(XmlUtils.toXml(of.createIssuerSecretKey(isParamsGlobal.issuerSecretKey)));
           
        
        //---------------------------------------------------
        // Create Verifier Parameters
        //---------------------------------------------------
        
        VerifierParameters vpMovies = cryptoEngineVerifier.createVerifierParameters(sysParams);
        
        vpMovies.setSystemParametersId(sysParams.getSystemParametersUID());
        vpMovies.setVerifierParametersId(URI.create("https://movies.idemixcloud.zurich.ibm.com/parameters/verifier"));
        vpMovies.setVersion("Version 1");
       
        System.out.println(XmlUtils.toXml(of.createVerifierParameters(vpMovies)));
        	
	}

}

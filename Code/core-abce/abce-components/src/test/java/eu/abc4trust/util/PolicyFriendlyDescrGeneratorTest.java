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

package eu.abc4trust.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.util.XmlUtils;

public class PolicyFriendlyDescrGeneratorTest {

	@Test @org.junit.Ignore
	public void testFriendlyPolicyDescrGenerator() throws Exception {
		
	    //---------------------------------------------------
        //Setup all instances
        //---------------------------------------------------
		

        Injector userInjector = Guice.createInjector(ProductionModuleFactory.newModuleWithoutPersistance());
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
       
        
        //---------------------------------------------------
        // Create Issuer Parameters
        //---------------------------------------------------
        
        IssuerParameters isParamsVoucher = new IssuerParameters();
        URI ipUidVoucher = URI.create("https://movies.idemixcloud.zurich.ibm.com/parameters/voucher");
        FriendlyDescription fdVoucher = new FriendlyDescription();
        fdVoucher.setLang("en");
        fdVoucher.setValue("Movie Streaming Service");
        isParamsVoucher.getFriendlyIssuerDescription().add(fdVoucher);
        List<FriendlyDescription> listFD = new ArrayList<FriendlyDescription>();
        listFD.add(fdVoucher);
              
        userKeyManager.storeIssuerParameters(ipUidVoucher, isParamsVoucher);
           
        IssuerParameters isParamsIdCard = new IssuerParameters();
        URI ipUidIdCard = URI.create("https://egov.idemixcloud.zurich.ibm.com/parameters/idcard");
        isParamsIdCard.setParametersUID(ipUidIdCard);
        FriendlyDescription fdIdCard = new FriendlyDescription();
        fdIdCard.setLang("en");
        fdIdCard.setValue("eGovernment");
        isParamsIdCard.getFriendlyIssuerDescription().add(fdIdCard);
        userKeyManager.storeIssuerParameters(isParamsIdCard.getParametersUID(), isParamsIdCard);               
        
        //---------------------------------------------------
        // Read and store credential specifications
        //---------------------------------------------------
        
        CredentialSpecification idCardSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/demo/credentialSpecificationIdCard.xml"), true);

        if (idCardSpec !=null){
        	userKeyManager.storeCredentialSpecification(idCardSpec.getSpecificationUID(), idCardSpec);
        }
        
        CredentialSpecification voucherSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/demo/credentialSpecificationMovieVoucher.xml"), true);

        if (voucherSpec !=null){
        	userKeyManager.storeCredentialSpecification(voucherSpec.getSpecificationUID(), voucherSpec);
        }
        
        //---------------------------------------------------
        //Retrieve presentation policies
        //---------------------------------------------------
	
		PresentationPolicyAlternatives ppDemoVoucherAlt =
	                (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
	                        "/eu/abc4trust/demo/presentationPolicyMovieStreamingOnlyVoucher.xml"), true);
				
		PresentationPolicyAlternatives ppDemoVoucherId12Alt =
                (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/demo/presentationPolicyMovieStreamingVoucherAndAgeOver12.xml"), true);
					
		PresentationPolicyAlternatives ppDemoVoucherId18Alt =
                (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/demo/presentationPolicyMovieStreamingVoucherAndAgeOver18.xml"), true);
		
        //---------------------------------------------------
        //Retrieve issuance policies
        //---------------------------------------------------
	
	 	IssuancePolicy ipDemoVoucherAlt =
	                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
	                        "/eu/abc4trust/demo/issuancePolicyMovieVoucher.xml"), true);
		
		IssuancePolicy ipDemoID =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/demo/issuancePolicyIdCard.xml"), true);
				
	    //---------------------------------------------------
        //Generate friendly descriptions of pp
        //---------------------------------------------------
		
		printPolicy(ppDemoVoucherAlt, userKeyManager);
		printPolicy(ppDemoVoucherId12Alt, userKeyManager);
		printPolicy(ppDemoVoucherId18Alt, userKeyManager);
		printIssuancePolicy(ipDemoID, userKeyManager);
		printIssuancePolicy(ipDemoVoucherAlt, userKeyManager);
				
		//TODO assertTrue
		
	}
	
	private static void printPolicy(PresentationPolicyAlternatives ppa, KeyManager userKeyManager){
		
		List<String> ret;
		try {
			ret = PolicyFriendlyDescrGenerator.generateFriendlyPresentationPolicyDescription(ppa, userKeyManager);
			for(String item : ret){
				System.out.println(item);
			}
		} catch (KeyManagerException e) {
			e.printStackTrace();
		}
	
	}
	
	private static void printIssuancePolicy(IssuancePolicy ip, KeyManager userKeyManager){
		
		List<String> ret;
		try {
			ret = PolicyFriendlyDescrGenerator.generateFriendlyIssuancePolicyDescription(ip, userKeyManager);
			for(String item : ret){
				System.out.println(item);
			}
		} catch (KeyManagerException e) {
			e.printStackTrace();
		}
	
	}
	

}

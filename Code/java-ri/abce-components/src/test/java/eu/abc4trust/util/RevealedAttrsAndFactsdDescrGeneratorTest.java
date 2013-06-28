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

package eu.abc4trust.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Module;

import eu.abc4trust.abce.integrationtests.idemix.IdemixIntegrationModuleFactory;
import eu.abc4trust.returnTypes.ui.RevealedAttributeValue;
import eu.abc4trust.returnTypes.ui.RevealedFact;
import eu.abc4trust.returnTypes.ui.RevealedFactsAndAttributeValues;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.util.XmlUtils;

public class RevealedAttrsAndFactsdDescrGeneratorTest {

	@Test
	public void testFriendlyTokenDescriptionGenerator() throws Exception {
		
	    //---------------------------------------------------
        //Setup all instances
        //---------------------------------------------------

	    Module userModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        //Injector userInjector = Guice.createInjector(userModule);
        //KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

        //---------------------------------------------------
        //Store credential specifications
        //---------------------------------------------------
        
        Map<URI,CredentialSpecification> uriCredspecs = new HashMap<URI, CredentialSpecification>();

        CredentialSpecification creditCardSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcardVisaForFriendlyGenTest.xml"), true);

        if (creditCardSpec !=null){
        	uriCredspecs.put(creditCardSpec.getSpecificationUID(), creditCardSpec);
        }
        
        CredentialSpecification passportSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml"), true);

        if (passportSpec !=null){
        	uriCredspecs.put(passportSpec.getSpecificationUID(), passportSpec);
        }
        
        //---------------------------------------------------
        //Retrieve presentation tokens
        //---------------------------------------------------
	
		PresentationToken ptCard =
	                (PresentationToken) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
	                        "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenCreditCardForFriendlyGenTest.xml"), true);

		PresentationToken ptHotel =
                (PresentationToken) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml"), true);

		
	    //---------------------------------------------------
        //Generate friendly descriptions of revealed facts and attribute values
        //---------------------------------------------------

		RevealedFactsAndAttributeValues rvs = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(ptCard.getPresentationTokenDescription(),uriCredspecs);
		printRevealedAttributes(rvs);
		printRevealedFacts(rvs);
				
		RevealedFactsAndAttributeValues rvs2 = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(ptHotel.getPresentationTokenDescription(), uriCredspecs);
		printRevealedAttributes(rvs2);
		printRevealedFacts(rvs2);
	}
	
	private void printRevealedFacts(RevealedFactsAndAttributeValues revAttsAndValues){
		for(RevealedFact rf: revAttsAndValues.revealedFacts){
			for(FriendlyDescription fd: rf.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue());
			}			
		}
	}
	
	private void printRevealedAttributes(RevealedFactsAndAttributeValues revAttsAndValues){
		for(RevealedAttributeValue ra: revAttsAndValues.revealedAttributeValues){
			for(FriendlyDescription fd: ra.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue());
			}			
		}
	}

}

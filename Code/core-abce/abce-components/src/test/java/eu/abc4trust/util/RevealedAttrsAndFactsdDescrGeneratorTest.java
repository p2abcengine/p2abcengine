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

import org.junit.Test;

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
        //Test
        //---------------------------------------------------
	
        runTest("/eu/abc4trust/sampleXml/presentationTokens/presentationTokenCreditCardForFriendlyGenTest.xml", uriCredspecs);
        runTest("/eu/abc4trust/sampleXml/presentationTokens/presentationTokenCreditCardForFriendlyGenTestOnlyDisclosed.xml", uriCredspecs);
        runTest("/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml", uriCredspecs);
		
	}
	
	private void runTest(String ptdFileName,  Map<URI,CredentialSpecification> uriCredspecs) throws Exception{
	     PresentationToken pt =
          (PresentationToken) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                  ptdFileName), true);
	     RevealedFactsAndAttributeValues rvs = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(pt.getPresentationTokenDescription(),uriCredspecs);
	        printRevealedAttributes(rvs);
	        printRevealedFacts(rvs);
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

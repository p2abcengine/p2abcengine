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

package eu.abc4trust.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

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
		printRevealedAttributes(rvs,
				new String[][] {
				{ "en: The value of Card Type from Visa Credit Card: gold" }
				, {"sv: Värdet av Status från INGEN VÄNLIG BESKRIVNING: 123123"
					, "en: The value of Status from Visa Credit Card: 123123" }
		});
		printRevealedFacts(rvs
				, new String[][] {
				{ "sv: Värdet av Utgångsdatum från INGEN VÄNLIG BESKRIVNING är efter eller lika med 06.01.2012 01:00"
					,"en: The value of Expiration Date from Visa Credit Card is after or on 06.01.2012 01:00" }
				,{"sv: Värdet av Utgångsdatum från INGEN VÄNLIG BESKRIVNING är före eller lika med 06.01.2014 01:00"
					, "en: The value of Expiration Date from Visa Credit Card is before or on 06.01.2014 01:00"
				}});

		RevealedFactsAndAttributeValues rvs2 = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(ptHotel.getPresentationTokenDescription(), uriCredspecs);
		printRevealedAttributes(rvs2, new String[][] {{}, {}});
		printRevealedFacts(rvs2, new String[][] { 
				{ "sv: Värdet av Utgångsdatum från INGEN VÄNLIG BESKRIVNING är efter eller lika med 06.01.2012",
				"en: The value of Expiration Date from Visa Credit Card is after or on 06.01.2012" }
		});
	}

	private void printRevealedAttributes(RevealedFactsAndAttributeValues revAttsAndValues, String[][] assertions){
		System.out.println("printRevealedAttributes ");
		for(int i=0; i<revAttsAndValues.revealedAttributeValues.size(); i++) {
			RevealedAttributeValue ra = revAttsAndValues.revealedAttributeValues.get(i);
			for(int j=0; j<ra.descriptions.size(); j++) {
				FriendlyDescription fd = ra.descriptions.get(j);
				String langDesc = fd.getLang()+": "+fd.getValue();
				System.out.println(langDesc);
				Assert.assertEquals(langDesc, assertions[i][j]);
			}
		}
	}
	private void printRevealedFacts(RevealedFactsAndAttributeValues revAttsAndValues, String[][] assertions){
		System.out.println("printRevealedFacts ");
		for(int i=0; i<revAttsAndValues.revealedFacts.size(); i++) {
			RevealedFact rf = revAttsAndValues.revealedFacts.get(i);
			for(int j=0; j<rf.descriptions.size(); j++){
				FriendlyDescription fd = rf.descriptions.get(j);
				String langRevealed = fd.getLang()+": "+fd.getValue();
				System.out.println(langRevealed);
				Assert.assertEquals(langRevealed, assertions[i][j]);
			}           
		}
	}

}
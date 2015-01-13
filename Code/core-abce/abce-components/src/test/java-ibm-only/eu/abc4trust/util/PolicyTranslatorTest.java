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


import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class PolicyTranslatorTest{
  
  @Test
  public void testPolicyTranslatorOne() throws Exception {
	  
		// Step 1. Load credential& credspec from XML.
	     Credential creditCard =
              (Credential) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
                  "/eu/abc4trust/sampleXml/credentials/credentialCreditcard.xml"), true);
	  
	     CredentialSpecification creditCardSpec =
	                (CredentialSpecification) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
	                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcardVisa.xml"), true);
	  
	  
		// Step 2. Load presentation token from XML.
		PresentationToken token = (PresentationToken) XmlUtils
				.getObjectFromXML(
						this.getClass()
						.getResourceAsStream(
								"/eu/abc4trust/sampleXml/presentationTokens/presentationTokenCreditCard.xml"),
								true);
		
		Map<URI, Credential> mapCred = new HashMap<URI, Credential>();
		Map<String, CredentialSpecification> mapCredSpec = new HashMap<String,CredentialSpecification>();
		
		PresentationTokenDescription ptd = token.getPresentationTokenDescription();
		
		CredentialInToken cd = ptd.getCredential().get(0);
		URI credentialAlias = cd.getAlias();
		mapCred.put(credentialAlias, creditCard);
		
		mapCredSpec.put(credentialAlias.toString(), creditCardSpec);
		  
		PolicyTranslator pt = new PolicyTranslator(ptd, mapCredSpec);
		for (MyPredicate mp: pt.getAllPredicates()){
		  for (MyAttributeReference mar:mp.getArgumentReferences()){
			  System.out.println(mar.getAttributeReference());
		  }
		String ps = mp.getPredicateAsString();
		System.out.println(ps);
					
		assertTrue(ps.equals("#creditcard: #creditcardExpirationDate GREATEREQ(dateTime) constant"));
  }
  }

}

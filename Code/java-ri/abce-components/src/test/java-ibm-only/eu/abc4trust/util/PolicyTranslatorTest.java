//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;


import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class PolicyTranslatorTest{
  
  @Ignore
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
					
		assertTrue(ps.equals("#creditcardExpirationDate GREATEREQ (dateTime) constant"));
  }
  }

}

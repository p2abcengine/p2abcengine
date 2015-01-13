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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.ui.RevealedAttributeValue;
import eu.abc4trust.returnTypes.ui.RevealedFact;
import eu.abc4trust.returnTypes.ui.RevealedFactsAndAttributeValues;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.PseudonymInToken;

public class PolicyFriendlyDescrGenerator {
	
	
	public static List<String> generateFriendlyPresentationPolicyDescription(PresentationPolicyAlternatives ppa, KeyManager km) throws KeyManagerException{	
		
		PresentationPolicy pp = ppa.getPresentationPolicy().get(0);
		return generateFriendlyPresentationPolicyDescription(pp, km);
	}
		
	
	private static List<String> generateFriendlyPresentationPolicyDescription(PresentationPolicy pp, KeyManager km) throws KeyManagerException{	
		
		PresentationTokenDescription ptd = convertPolicyToTokenDescription(pp);

	    Map<URI,CredentialSpecification> uriCredSpecs = new HashMap<URI, CredentialSpecification>();
	    Map<URI,IssuerParameters> uriIsParams = new HashMap<URI, IssuerParameters>();
	    	   	    
		for (CredentialInToken credInToken: ptd.getCredential()){
			URI uriSpec = credInToken.getCredentialSpecUID();
			URI uriIsPar = credInToken.getIssuerParametersUID();
		    uriCredSpecs.put(uriSpec, km.getCredentialSpecification(uriSpec));
		    uriIsParams.put(uriIsPar, km.getIssuerParameters(uriIsPar));
		}	  
	    
		return composeHumanReadablePolicy(ptd, uriCredSpecs, uriIsParams);
	}
	

	
	private static PresentationTokenDescription convertPolicyToTokenDescription(PresentationPolicy pp){		
	  ObjectFactory of = new ObjectFactory();
      PresentationTokenDescription ptd = of.createPresentationTokenDescription();   
      ptd.setMessage(pp.getMessage());
      
      for (AttributePredicate pred : pp.getAttributePredicate()) {
			      ptd.getAttributePredicate().add(pred);
      }
      
      ptd.setPolicyUID(pp.getPolicyUID());
      
      for (PseudonymInPolicy policyPseudonym : pp.getPseudonym()) {
          PseudonymInToken pseudonym = of.createPseudonymInToken();
          ptd.getPseudonym().add(pseudonym);
          pseudonym.setAlias(policyPseudonym.getAlias());
          pseudonym.setExclusive(policyPseudonym.isExclusive());
          pseudonym.setScope(policyPseudonym.getScope());
          pseudonym.setSameKeyBindingAs(policyPseudonym.getSameKeyBindingAs());
          // Pseudonym value will be filled out later
          pseudonym.setPseudonymValue(null);
      }
      
      // now just picking the first one
	  for (CredentialInPolicy credInPolicy: pp.getCredential()){
		  CredentialInToken c = of.createCredentialInToken();
		  c.setAlias(credInPolicy.getAlias());
	      c.setSameKeyBindingAs(credInPolicy.getSameKeyBindingAs());
	      c.setCredentialSpecUID(credInPolicy.getCredentialSpecAlternatives().getCredentialSpecUID().get(0));
	      c.setIssuerParametersUID(credInPolicy.getIssuerAlternatives().getIssuerParametersUID().get(0).getValue());
	      
	      for(AttributeInPolicy attrInPolicy : credInPolicy.getDisclosedAttribute()){
	    	  AttributeInToken attrInToken = of.createAttributeInToken();
	    	  attrInToken.setAttributeType(attrInPolicy.getAttributeType());
	    	  c.getDisclosedAttribute().add(attrInToken);
	      }
	      	      
	      ptd.getCredential().add(c);
	  }	      
      
      return ptd;
	}
	
	private static List<String> composeHumanReadablePolicy(PresentationTokenDescription ptd, Map<URI, CredentialSpecification> uriCredSpecs, Map<URI,IssuerParameters> uriIsParams){
		
		RevealedFactsAndAttributeValues rvs = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(ptd, uriCredSpecs, uriIsParams);
		
		List<String> ret = new ArrayList<String>();
	
		//TODO: enable credential posession string again
		/*for(CredentialInToken credInToken: ptd.getCredential()){
			ret.add(formCredentialPosessionString(uriIsParams.get(credInToken.getIssuerParametersUID()), uriCredSpecs.get(credInToken.getCredentialSpecUID())));
		}*/
		
				
		for (RevealedAttributeValue revealedAttrValue : rvs.revealedAttributeValues){
			for (FriendlyDescription fd : revealedAttrValue.descriptions){
				ret.add(fd.getValue());
			}
		}
		
		for (RevealedFact rf : rvs.revealedFacts){
			for (FriendlyDescription fd : rf.descriptions){
				if (fd.getLang().equals("en")) ret.add(fd.getValue());
				else ret.add("Only English language is supported currently");
			}
		}
		return ret;
	}
	
	private static String formCredentialPosessionString(IssuerParameters issParams, CredentialSpecification credSpec){
    	Map<String,FriendlyDescription> credLangDescMap = new HashMap<String,FriendlyDescription>();
    	Map<String,FriendlyDescription> issLangDescMap = new HashMap<String,FriendlyDescription>();
    	StringBuilder sb = new StringBuilder();
    	  	
    	for (FriendlyDescription credFrDescription: credSpec.getFriendlyCredentialName()){
    		credLangDescMap.put(credFrDescription.getLang(), credFrDescription);
    	}
    	
    	for (FriendlyDescription issFrDescription: issParams.getFriendlyIssuerDescription()){
    		issLangDescMap.put(issFrDescription.getLang(), issFrDescription);
    	}
    	
    	for (String lang:credLangDescMap.keySet()){
    		//COMPOSE THE RETURN STRING:
			sb.append(Messages.getString(Messages.POSSESSIONOF,lang)+" "); //$NON-NLS-1$
        	if (credLangDescMap.get(lang)!=null){
				sb.append(credLangDescMap.get(lang).getValue() +" "); //$NON-NLS-1$
			} else {
				sb.append(Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang)); //$NON-NLS-1$
			} 
        	sb.append(Messages.getString(Messages.FROM,lang)+" ");  //$NON-NLS-1$
        	if (issLangDescMap.get(lang)!=null){
				sb.append(issLangDescMap.get(lang).getValue() +" "); //$NON-NLS-1$
			} else {
				sb.append(Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang)); //$NON-NLS-1$
			}
		}
    	return sb.toString();
	}
    	
	
	public static List<String> generateFriendlyIssuancePolicyDescription(IssuancePolicy ip, KeyManager km) throws KeyManagerException{	
		
		CredentialSpecification credSpec = km.getCredentialSpecification(ip.getCredentialTemplate().getCredentialSpecUID());

		IssuerParameters issParams= km.getIssuerParameters(ip.getCredentialTemplate().getIssuerParametersUID());
		
		List<String> ret2  = generateFriendlyPresentationPolicyDescription(ip.getPresentationPolicy(), km);
		
		List<String> ret = generateFriendlyCredentialTemplate(ip.getCredentialTemplate(), credSpec, issParams);
		
		ret.addAll(ret2); //TODO: maybe return two lists and also lists of lists for different language support
		
		return ret;
		
	}


	private static List<String> generateFriendlyCredentialTemplate(CredentialTemplate credentialTemplate, CredentialSpecification credSpec, IssuerParameters ip) {
		List<String> ret = new ArrayList<String>();
		
    	Map<String,FriendlyDescription> credLangDescMap = new HashMap<String,FriendlyDescription>();
    	Map<String,FriendlyDescription> issLangDescMap = new HashMap<String,FriendlyDescription>();
    	StringBuilder sb = new StringBuilder();
    	  	  	
    	for (FriendlyDescription credFrDescription: credSpec.getFriendlyCredentialName()){
    		credLangDescMap.put(credFrDescription.getLang(), credFrDescription);
    	}
    	
    	for (FriendlyDescription issFrDescription: ip.getFriendlyIssuerDescription()){
    		issLangDescMap.put(issFrDescription.getLang(), issFrDescription);
    	}
    	
    	for (String lang:credLangDescMap.keySet()){
    		//COMPOSE THE RETURN STRING:
    		//THE ISSUER_NAME
        	if (issLangDescMap.get(lang)!=null){
				sb.append(issLangDescMap.get(lang).getValue() +" "); //$NON-NLS-1$
			} else {
				sb.append(Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang)); //$NON-NLS-1$
			}
        	//WOULD LIKE TO DELIVER
			sb.append(Messages.getString(Messages.WOULD_LIKE_TO_DELIVER,lang)); //$NON-NLS-1$
			//CRED_NAME
        	if (credLangDescMap.get(lang)!=null){
				sb.append(" " + credLangDescMap.get(lang).getValue());
			} else {
				sb.append(" " + Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang)); //$NON-NLS-1$
			}
        	//INTO YOU CREDENTIAL WALLET
        	sb.append(" " + Messages.getString(Messages.TO_YOUR_CREDENTIAL_WALLET,lang)); //$NON-NLS-1$

        	
        	//TODO: add language support - for now only English is taken!
        	if (lang.equals("en")) {ret.add(sb.toString());}
        	else ret.add("Only English language is supported currently");
		}
    	
		return ret;
	}
	
}

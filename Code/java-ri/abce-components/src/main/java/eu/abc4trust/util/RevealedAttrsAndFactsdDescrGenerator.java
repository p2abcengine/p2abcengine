//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import javax.print.attribute.PrintRequestAttribute;


import eu.abc4trust.returnTypes.ui.RevealedAttributeValue;
import eu.abc4trust.returnTypes.ui.RevealedFact;
import eu.abc4trust.returnTypes.ui.RevealedFactsAndAttributeValues;
import eu.abc4trust.util.Constants.OperationType;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;

public class RevealedAttrsAndFactsdDescrGenerator {
	
	
	public static RevealedFactsAndAttributeValues generateFriendlyDesciptions(PresentationTokenDescription ptd, 
			Map<URI,CredentialSpecification> credSpecs){
		PolicyTranslator pt = new PolicyTranslator(ptd, getAliasCredSpecList(ptd,credSpecs));
		RevealedFactsAndAttributeValues ret = composeFriendlyDescOfRevealedFactsAndAttributeValues(pt);	
		return ret;
	}
	
    private static RevealedFactsAndAttributeValues composeFriendlyDescOfRevealedFactsAndAttributeValues(
			PolicyTranslator policyTranslator) {    	
    	
    	//Compose the return value
    	RevealedFactsAndAttributeValues ret = new RevealedFactsAndAttributeValues();
    	    	
    	//Get info that we need from the Policy translator
	    Map<MyAttributeReference,List<FriendlyDescription>> attrFriendlyDescr = policyTranslator.getAttrRefFriedlyDescrList();
		Map<String,List<FriendlyDescription>> credFriendlyDescr = policyTranslator.getCredAliasFriedlyDescrList();
		
		Map<MyAttributeReference,MyAttributeValue> allDisclosedAttrsAndValues = policyTranslator.getAllDisclosedAttributesAndValues();
    	
		//get mypredicates
		List<MyPredicate> myPredicates = policyTranslator.getAllPredicates();
		
    	//Create revealed attribute values descriptions:
		
		for (MyAttributeReference mar: allDisclosedAttrsAndValues.keySet()){
			RevealedAttributeValue attValue = new RevealedAttributeValue();	
			//get friendly name of the credential
			List<FriendlyDescription> credFriendly = credFriendlyDescr.get(mar.getCredentialAlias());
			//get friendly name of the attribute
			List<FriendlyDescription> attrFriendly = attrFriendlyDescr.get(mar);
			//get the attribute value
			MyAttributeValue atValue = allDisclosedAttrsAndValues.get(mar);
			attValue.descriptions = composeFriendlyAttributeValue(credFriendly, attrFriendly, atValue);				
			//add to the return value				
			ret.revealedAttributeValues.add(attValue);
		} 
				
		//Create revealed attribute values descriptions:	
		for (MyPredicate mp: myPredicates){	
			RevealedFact revFact = composeFriendlyFact(mp, credFriendlyDescr, attrFriendlyDescr);
			if (revFact!=null) {
				ret.revealedFacts.add(revFact);				
			}
		} 	
		
		//print
		printRevealedFactsAndValues(ret);
		
		return ret;
	}

    private static RevealedFact composeFriendlyFact(MyPredicate mp, Map<String,List<FriendlyDescription>> credFriendlyDescr, Map<MyAttributeReference,List<FriendlyDescription>> attrFriendlyDescr){
    	ObjectFactory of = new ObjectFactory();
    	RevealedFact ret = new RevealedFact();
    	
	 
		String constant = new String("");
		String function = new String(" unspecified function ");
		MyAttributeReference attribute = null;
		try {
			function = mp.getFriendlyFunction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!mp.getFunction().equals(OperationType.EQUALONEOF)){		
			if (mp.getLeftRef().isConstant()){
				constant = mp.getLeftVal().toString();
			} else {
				attribute = mp.getLeftRef();
			}			
			if (mp.getRightRef().isConstant()){
				constant = mp.getRightVal().toString();
			} else {
				attribute = mp.getRightRef();
			}
		} else {
			for (Map<MyAttributeReference,MyAttributeValue> atRefAndValue: mp.getArguments()){
				for (MyAttributeReference mar: atRefAndValue.keySet()){
					if (mar.isConstant()){
					constant = constant+ atRefAndValue.get(mar).toString()+", ";
					} else {
						attribute = mar;
					}
				}
			}
			
			//remove last ","
			constant = constant.substring(0, constant.length() - 2); 
		}
		
		Map<String,FriendlyDescription> attrLangDescMap = new HashMap<String,FriendlyDescription>();
    	Map<String,FriendlyDescription> credLangDescMap = new HashMap<String,FriendlyDescription>();
			
		if (attribute==null){
			//this means that the attribute was disclosed either implicitly or through EQUAL predicate - 
			// it will appear in revealed attributes
			return null;
		} else {
    	
    	//get friendly name of the credential or create an empty one
		List<FriendlyDescription> credFriendly = credFriendlyDescr.get(attribute.getCredentialAlias());
		
		if ((credFriendly == null)||(credFriendly.isEmpty())){
			FriendlyDescription credFrDescription = createEmptyFriendlyDescription();
			credLangDescMap.put(credFrDescription.getLang(), credFrDescription);   		
		} else {
    		for (FriendlyDescription credFrDescription: credFriendly) {
        		credLangDescMap.put(credFrDescription.getLang(), credFrDescription);   		
        	}
		}
		
		
		//get friendly name of the attribute or create an empty one
		List<FriendlyDescription> attrFriendly = attrFriendlyDescr.get(attribute);
		
		if ((attrFriendly == null)||(attrFriendly.isEmpty())){
			FriendlyDescription attrFrDescription = createEmptyFriendlyDescription();
			attrLangDescMap.put(attrFrDescription.getLang(), attrFrDescription); 
		} else {    		
    		for (FriendlyDescription attrFrDescription: attrFriendly){
    			attrLangDescMap.put(attrFrDescription.getLang(), attrFrDescription);
    		}
    	}
		
    	for (String lang:attrLangDescMap.keySet()){
    	
    			FriendlyDescription frednlyDescription = of.createFriendlyDescription();
    			//COMPOSE THE RETURN STRING:
    			StringBuilder sb = new StringBuilder();
    			sb.append("The value of "); 
    			sb.append(attrLangDescMap.get(lang).getValue());
    			sb.append(translateFromToLang(lang));
    			if (credLangDescMap.get(lang)!=null){
    				sb.append(credLangDescMap.get(lang).getValue());
    			} else {
    				sb.append("NO FRIENDLY DESCRIPTION IN LANG=" + lang);
    			}
				sb.append(function);  
				sb.append(constant);  	

				frednlyDescription.setLang(lang);
				frednlyDescription.setValue(sb.toString());
				ret.descriptions.add(frednlyDescription);
    		}
		}
    	return ret;
    }
    
    private static List<FriendlyDescription> composeFriendlyAttributeValue(List<FriendlyDescription> credentialName, List<FriendlyDescription> attributeName, MyAttributeValue attributeValue){
    	ObjectFactory of = new ObjectFactory();
    	List<FriendlyDescription> ret = new ArrayList<FriendlyDescription>();
    	
      	Map<String,FriendlyDescription> attrLangDescMap = new HashMap<String,FriendlyDescription>();
    	Map<String,FriendlyDescription> credLangDescMap = new HashMap<String,FriendlyDescription>();
    	
    	for (FriendlyDescription atrFrDescription: attributeName) {
    		attrLangDescMap.put(atrFrDescription.getLang(), atrFrDescription);   		
    	}
    	
    	for (FriendlyDescription credFrDescription: credentialName){
    		credLangDescMap.put(credFrDescription.getLang(), credFrDescription);
    	}
    	
    	for (String lang:attrLangDescMap.keySet()){
        	StringBuilder sb = new StringBuilder();
			//COMPOSE THE RETURN STRING:
			sb.append("The value of "); 
        	sb.append(attrLangDescMap.get(lang).getValue());
        	sb.append(translateFromToLang(lang));
        	sb.append(credLangDescMap.get(lang).getValue());
        	sb.append(": ");
        	sb.append(attributeValue.toString());
    		FriendlyDescription revealedFrDescr = of.createFriendlyDescription();
    		revealedFrDescr.setLang(lang);
    		revealedFrDescr.setValue(sb.toString()); 
    		ret.add(revealedFrDescr);
    	}
    	

    	return ret;
    }
    
    private static String translateFromToLang(String language){
    	return " from ";
    }
    
    private static FriendlyDescription createEmptyFriendlyDescription(){
    	ObjectFactory of = new ObjectFactory();
    	FriendlyDescription frednlyDescription = of.createFriendlyDescription();
		frednlyDescription.setLang("en");
		frednlyDescription.setValue("NO FRIENDLY DESCRIPTION");
		return frednlyDescription;
    }
    
	private static Map<String, CredentialSpecification> getAliasCredSpecList(PresentationTokenDescription ptd, Map<URI,CredentialSpecification> credSpecs){

    	Map<String, CredentialSpecification> aliasCredSpecs = new HashMap<String, CredentialSpecification>();  
		int credIndex = 0; 
        for (CredentialInToken cit: ptd.getCredential()) {     		
        		URI credentialAlias = cit.getAlias();       	             	      
        		if (credentialAlias == null) {       	         
        			credentialAlias = URI.create("abc4trust.eu/credential/"+ credIndex);        	     
        		}     	      
        		credIndex ++;       
        		CredentialSpecification credSpec = credSpecs.get(cit.getCredentialSpecUID());     
        		if (credSpec==null) {
        			throw new RuntimeException("There is no correct credential specification stored");
        		}
        		aliasCredSpecs.put(credentialAlias.toString(), credSpec);    	
        }
        return aliasCredSpecs;

	}
	
	private static void printRevealedFactsAndValues(RevealedFactsAndAttributeValues revAttsAndValues){
		for(RevealedFact rf: revAttsAndValues.revealedFacts){
			for(FriendlyDescription fd: rf.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue());
			}			
		}
		for(RevealedAttributeValue ra: revAttsAndValues.revealedAttributeValues){
			for(FriendlyDescription fd: ra.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue());
			}			
		}
	}
	
}

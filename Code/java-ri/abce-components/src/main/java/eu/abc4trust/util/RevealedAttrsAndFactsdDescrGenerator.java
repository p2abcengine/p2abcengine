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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import javax.print.attribute.PrintRequestAttribute;
import javax.xml.datatype.XMLGregorianCalendar;


import eu.abc4trust.returnTypes.ui.RevealedAttributeValue;
import eu.abc4trust.returnTypes.ui.RevealedFact;
import eu.abc4trust.returnTypes.ui.RevealedFactsAndAttributeValues;
import eu.abc4trust.util.Constants.OperationType;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueDate;
import eu.abc4trust.util.attributeTypes.MyAttributeValueDateTime;
import eu.abc4trust.util.attributeTypes.MyAttributeValueTime;
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
		String constant = new String(""); //$NON-NLS-1$
		MyAttributeReference attribute = null;
		
		if (!mp.getFunction().equals(OperationType.EQUALONEOF)){		
			if (mp.getLeftRef().isConstant()){
				if (mp.getTypeOfArguments().equals(Constants.DATE_TYPE)){
					constant = getFriendlyDate(mp.getLeftVal());
				} else if (mp.getTypeOfArguments().equalsIgnoreCase(Constants.DATETIME_TYPE)){
				 	constant = getFriendlyDateTime(mp.getLeftVal());
				} else { 
					constant = mp.getLeftVal().toString();
				}
			} else {
				attribute = mp.getLeftRef();
			}			
			if (mp.getRightRef().isConstant()){
				if (mp.getTypeOfArguments().equals(Constants.DATE_TYPE)){
						constant = getFriendlyDate(mp.getRightVal());
				} else if (mp.getTypeOfArguments().equalsIgnoreCase(Constants.DATETIME_TYPE)){
					 	constant = getFriendlyDateTime(mp.getRightVal());
				} else { 
				constant = mp.getRightVal().toString();
				} 
			}else {
				attribute = mp.getRightRef();
			}
		} else {
			for (Map<MyAttributeReference,MyAttributeValue> atRefAndValue: mp.getArguments()){
				for (MyAttributeReference mar: atRefAndValue.keySet()){
					if (mar.isConstant()){
					constant = constant+ atRefAndValue.get(mar).toString()+", "; //$NON-NLS-1$
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
			//this means that the attribute was disclosed either implicitly or through EQUAL/EQUAL-ONE-OF predicate - 
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
    			sb.append(translateTheValueOfLang(lang) +" "); 
    			sb.append(attrLangDescMap.get(lang).getValue()+ " ");
    			sb.append(translateFromToLang(lang));
    			if (credLangDescMap.get(lang)!=null){
    				sb.append(credLangDescMap.get(lang).getValue());
    			} else {
    				sb.append(Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang) + lang); //$NON-NLS-1$
    			}
				try {
					sb.append(" "+getFriendlyFunction(mp.getFunction(), lang, mp.getTypeOfArguments())+" ");
				} catch (Exception e) {
					sb.append(Messages.NO_FRIENDLY_DESCRIPTION);
				}  
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
			sb.append(translateTheValueOfLang(lang)+" "); 
        	sb.append(attrLangDescMap.get(lang).getValue()+" ");
        	sb.append(translateFromToLang(lang));
        	if (credLangDescMap.get(lang)!=null){
				sb.append(credLangDescMap.get(lang).getValue());
			} else {
				sb.append(Messages.getString(Messages.NO_FRIENDLY_DESCRIPTION,lang) + lang); //$NON-NLS-1$
			}
        	sb.append(": "); //$NON-NLS-1$
        	sb.append(attributeValue.toString());
    		FriendlyDescription revealedFrDescr = of.createFriendlyDescription();
    		revealedFrDescr.setLang(lang);
    		revealedFrDescr.setValue(sb.toString()); 
    		ret.add(revealedFrDescr);
    	}
    	

    	return ret;
    }
    
    private static String translateFromToLang(String language){
    	return Messages.getString(Messages.FROM, language); //$NON-NLS-1$
    }
    
    private static String translateTheValueOfLang(String language){
    	return Messages.getString(Messages.THE_VALUE_OF, language); //$NON-NLS-1$
    }
    
    private static FriendlyDescription createEmptyFriendlyDescription(){
    	ObjectFactory of = new ObjectFactory();
    	FriendlyDescription frednlyDescription = of.createFriendlyDescription();
		frednlyDescription.setLang("en"); //$NON-NLS-1$
		frednlyDescription.setValue("NO FRIENDLY DESCRIPTION"); //$NON-NLS-1$
		return frednlyDescription;
    }
    
	private static String getFriendlyFunction(OperationType function, String lang, String argumentsType) throws Exception {
		if (function == null)
			throw new Exception("No function specified");
		boolean dateFlag = argumentsType.equals(Constants.DATE_TYPE)||argumentsType.equals(Constants.DATETIME_TYPE);
		switch (function) {
		case EQUAL: return Messages.getString(Messages.EQUAL, lang);
		case NOTEQUAL: return Messages.getString(Messages.NOTEQUAL, lang);
		case GREATER: 
			if (dateFlag) {
				return Messages.getString(Messages.GREATER_DATE, lang);
			} else {
				return Messages.getString(Messages.GREATER, lang);
			}
		case GREATEREQ: 
			if (dateFlag) {
				return Messages.getString(Messages.GREATEREQ_DATE, lang);
			}  else {
				return Messages.getString(Messages.GREATEREQ, lang);
			}
				
		case LESS: 
			if (dateFlag) {
				return Messages.getString(Messages.LESS_DATE, lang);
			} else {
				return Messages.getString(Messages.LESS, lang);
			}
		case LESSEQ: 
			if (dateFlag) {
				return Messages.getString(Messages.LESSEQ_DATE, lang);
			} else {
				return Messages.getString(Messages.LESSEQ, lang);
			}
		case EQUALONEOF: return Messages.getString(Messages.EQUALONEOF, lang);
		default: throw new Exception("Unknown operator: " + function.toString());
	}
   }
	
	private static String getFriendlyDate(MyAttributeValue av){
		MyAttributeValueDate date = (MyAttributeValueDate) av;
		if (date.getValueAsObject() instanceof XMLGregorianCalendar) {
			 XMLGregorianCalendar value = (XMLGregorianCalendar) date.getValueAsObject();
			 GregorianCalendar dateNormal = value.toGregorianCalendar();
			 DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			 return df.format(dateNormal.getTime());			 
		} else return av.toString();
	};
	
	private static String getFriendlyDateTime(MyAttributeValue av){
		MyAttributeValueDateTime date = (MyAttributeValueDateTime) av;
		if (date.getValueAsObject() instanceof XMLGregorianCalendar) {
			 XMLGregorianCalendar value = (XMLGregorianCalendar) date.getValueAsObject();
			 GregorianCalendar dateNormal = value.toGregorianCalendar();
			 DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			 return df.format(dateNormal.getTime());			 
		} else return av.toString();
	};
	
    
	private static Map<String, CredentialSpecification> getAliasCredSpecList(PresentationTokenDescription ptd, Map<URI,CredentialSpecification> credSpecs){

    	Map<String, CredentialSpecification> aliasCredSpecs = new HashMap<String, CredentialSpecification>();  
		int credIndex = 0; 
        for (CredentialInToken cit: ptd.getCredential()) {     		
        		URI credentialAlias = cit.getAlias();       	             	      
        		if (credentialAlias == null) {       	         
        			credentialAlias = URI.create("abc4trust.eu/credential/"+ credIndex);  //$NON-NLS-1$
        		}     	      
        		credIndex ++;       
        		CredentialSpecification credSpec = credSpecs.get(cit.getCredentialSpecUID());     
        		if (credSpec==null) {
        			throw new RuntimeException("There is no correct credential specification stored"); //$NON-NLS-1$
        		}
        		aliasCredSpecs.put(credentialAlias.toString(), credSpec);    	
        }
        return aliasCredSpecs;

	}
	
	private static void printRevealedFactsAndValues(RevealedFactsAndAttributeValues revAttsAndValues){
		for(RevealedFact rf: revAttsAndValues.revealedFacts){
			for(FriendlyDescription fd: rf.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue()); //$NON-NLS-1$
			}			
		}
		for(RevealedAttributeValue ra: revAttsAndValues.revealedAttributeValues){
			for(FriendlyDescription fd: ra.descriptions){
				System.out.println(fd.getLang()+": "+fd.getValue()); //$NON-NLS-1$
			}			
		}
	}
	
}

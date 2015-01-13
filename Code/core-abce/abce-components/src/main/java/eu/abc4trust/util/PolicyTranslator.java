//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.abc4trust.util.Constants.OperationType;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueFactory;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.AttributePredicate.Attribute;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Policy parser, helper for Identity Mixer integration.
 */
public final class PolicyTranslator{
	
	//TODO: add null checks!!!
	
    private final Map<String, CredentialSpecification> credentialSpecList;
    private final PresentationTokenDescription ptd;
    private final String messageToSign;
    private final Map<String, List<MyAttributeReference>> credentialAttrsList;
    private final Map<String, MyAttributeReference> attrRefsCache;
    private final Map<MyAttributeReference, MyAttributeValue> allAttrsAndValues;
    private final List<MyAttributeReference> allDisclosedAttrs;
    private final Map<MyAttributeReference, MyAttributeValue> allDisclosedAttrsAndValues;
    private final List<MyAttributeReference> explicitlyDisclosedAttrs;
    private final List<MyAttributeReference> inspectableAttrs;
    private final Map<MyAttributeReference, BigInteger> inspectableAttrValues;
    private final Set<Set<MyAttributeReference>> attrRefEqivClasses;
    private final Set<Set<MyAttributeReference>> revealedAttrRefEqivClasses;
    private final Map<MyAttributeReference, URI> attrRefToInspectorKey;
    private       List<MyPredicate> myPredicates;
    private final Map<MyPredicate, List<CredentialInToken>> predicateCredInToken;
    private final HashMap<String,CredentialInToken> aliasCredInTokenList;

    private List<MyAttributeReference> oneOfAttrs = null;
    //Friednly descriptions
    private final Map<String, List<FriendlyDescription>> credAliasFriedlyDescrList;
    private Map<MyAttributeReference, List<FriendlyDescription>> attrRefFriedlyDescrList;

    public PolicyTranslator(PresentationTokenDescription ptd,
            Map<String, CredentialSpecification> aliasCredSpecs) {

        this.ptd = ptd;

        this.oneOfAttrs = new ArrayList<MyAttributeReference>();

        this.myPredicates = new ArrayList<MyPredicate>();
        this.allDisclosedAttrs = new ArrayList<MyAttributeReference>();
        this.explicitlyDisclosedAttrs = new ArrayList<MyAttributeReference>();
        this.allAttrsAndValues = new HashMap<MyAttributeReference, MyAttributeValue>();
        this.attrRefEqivClasses = new HashSet<Set<MyAttributeReference>>();
        this.revealedAttrRefEqivClasses = new HashSet<Set<MyAttributeReference>>();
        this.predicateCredInToken = new LinkedHashMap<MyPredicate,List<CredentialInToken>>();
        //        this.predicateCredInTokenWithCommitments = new HashMap<MyPredicate,List<CredentialInTokenWithCommitments>>();
        this.credentialAttrsList = new HashMap<String, List<MyAttributeReference>>();
        this.attrRefToInspectorKey = new HashMap<MyAttributeReference, URI>();
        this.inspectableAttrValues = new HashMap<MyAttributeReference, BigInteger>();

        this.messageToSign = this.parseMsgToSign();
        this.attrRefsCache = new HashMap<String,MyAttributeReference>();

        this.credentialSpecList = aliasCredSpecs;
        this.aliasCredInTokenList = new LinkedHashMap<String, CredentialInToken>();
        //       this.aliasCredInTokenWithCommitmentsList = new HashMap<String, CredentialInTokenWithCommitments>();
        this.inspectableAttrs = new ArrayList<MyAttributeReference>();
        this.allDisclosedAttrsAndValues = new HashMap<MyAttributeReference, MyAttributeValue>();

        //parse attributes that are revealed explicitly and under inspector pk
        this.parseAllExplicitlyDisclosedAndInspectableAttributes();


        //parse all predicates to use them for creating idemix proof spec
        this.parseAllPredicates();

        //figure out all implicitly revealed attributes from equality predicates
        try {
            this.createEquivalenceClassesForRevealedAndOneOfAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //build a list of all (impl and expl) disclosed attributes and their values
        this.setAllDisclosedAttributes();

        this.completeCredentialDeclarations(); //generate all other attribute references to use in Idemix proof spec generator

        this.updateAllPredicates();
        
        //get friendly credential names, map them to credential aliases and attribute references
        this.credAliasFriedlyDescrList = this.composeCredAliasFriendlyDescrList(aliasCredSpecs);
        this.attrRefFriedlyDescrList = this.composeAttrRefsFriendlyDescrList(aliasCredSpecs, credentialAttrsList);

    };


    /**
     * Prepares a string to sign
     * @return
     */
    private String parseMsgToSign() {
        StringBuilder sb = new StringBuilder();
        if (this.ptd.getMessage()!=null){
            Message msgFromToken = this.ptd.getMessage();
            if (msgFromToken.getNonce()!=null){
                BigInteger nonce = new BigInteger(msgFromToken.getNonce());
                sb.append("Nonce: " + nonce+"\n");
            }
            if (msgFromToken.getFriendlyPolicyName()!=null){
                for (FriendlyDescription fd: msgFromToken.getFriendlyPolicyName()){
                    sb.append(fd.getLang() +": ");
                    sb.append(fd.getValue() +"\n");
                }
            }
            if (msgFromToken.getFriendlyPolicyDescription()!=null){
                for (FriendlyDescription fd: msgFromToken.getFriendlyPolicyDescription()){
                    sb.append(fd.getLang()+": ");
                    sb.append(fd.getValue()+"\n");
                }
            }
            if (msgFromToken.getApplicationData()!=null){
                ObjectFactory of = new ObjectFactory();
                String xml;
                try {
                    xml = XmlUtils.toNormalizedXML(of.createApplicationData(msgFromToken.getApplicationData()));
                } catch (Exception e) {
                    String errorMessage = "Could not serialize Application data: " + e.getMessage();
                    e.printStackTrace();
                    throw new RuntimeException(errorMessage);
                }
                sb.append(xml);
            }
        }
        // System.out.println(sb.toString());
        if (!sb.toString().equals("null")){
            return sb.toString();
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The Identity Mixer implementation does not support proofs over attributes that are revealed. Therefore, all the
    // arguments with attribute references whose value is disclosed are substituted with their disclosed values.
    // The attribute reference is changed to Constant
    private void updateAllPredicates() {
        List<CredentialInToken> credsInPredicate = new ArrayList<CredentialInToken>();
        List<MyPredicate> newPredicateList = new ArrayList<MyPredicate>();
        for(MyPredicate predicate: this.myPredicates){
            for (MyAttributeReference attrRefInPredicate: predicate.getArgumentReferences()){
                if (this.allDisclosedAttrsAndValues.keySet().contains(attrRefInPredicate)){
                    MyAttributeValue value = this.allDisclosedAttrsAndValues.get(attrRefInPredicate);
                    predicate.addArgumentVal(attrRefInPredicate, value);
                } else if ((!attrRefInPredicate.getAttributeReference().equals(Constants.CONSTANT))&&
                        (!credsInPredicate.contains(this.aliasCredInTokenList.get(attrRefInPredicate.getCredentialAlias())))){
                    credsInPredicate.add(this.aliasCredInTokenList.get(attrRefInPredicate.getCredentialAlias()));
                }
            }
            newPredicateList.add(predicate);
            this.predicateCredInToken.put(predicate, credsInPredicate);
        }
        this.myPredicates = newPredicateList;
    }

    /**
     * Finalizes the Map<CredentialAlias, List<MyCredentialReferences>>
     * Helper for creating Idemix Proof Spec
     */
    private void completeCredentialDeclarations(){

        for(String credAlias: this.credentialSpecList.keySet()){  //contains a cred spec for a credential that will be issued in the case if credTemplate is not null
            List<MyAttributeReference> oldRefsList = this.credentialAttrsList.get(credAlias);
            CredentialSpecification currentCredSpec = this.credentialSpecList.get(credAlias);
            List<AttributeDescription> allAttributesInCred = currentCredSpec.getAttributeDescriptions().getAttributeDescription();
            if ((oldRefsList==null)||((oldRefsList!=null)&&(oldRefsList.size()!=allAttributesInCred.size()))){
                for(AttributeDescription ad: allAttributesInCred){
                    if (!this.attrRefsCache.containsKey(credAlias+ad.getType().toString())){
                        MyAttributeReference newref = new MyAttributeReference(URI.create(credAlias), ad.getType());
                        this.attrRefsCache.put(credAlias+ad.getType().toString(), newref);
                        this.addAttributeRefToCredentialAlias(credAlias, newref);
                    }
                }
            }
        }
    }

    /**
     * Adds specified attribute reference to a list of attribute references for given credential, specifies by alias
     * @param credentialAlias
     * @param myAttributeRef
     */
    private void addAttributeRefToCredentialAlias(String credentialAlias, MyAttributeReference myAttributeRef){
        List<MyAttributeReference> currentRefs = new ArrayList<MyAttributeReference>();
        if (this.credentialAttrsList.get(credentialAlias)!=null){
            currentRefs = this.credentialAttrsList.get(credentialAlias);
        }
        currentRefs.add(myAttributeRef);
        this.credentialAttrsList.put(credentialAlias, currentRefs);
        this.attrRefsCache.put(myAttributeRef.getAttributeReference(), myAttributeRef);
    }


    /**
     * Parses all predicates from XML-based object to MyPredicate to use for creating Idemix ProofSpec
     */
    private void parseAllPredicates(){
        List<AttributePredicate> attrPredicates = this.ptd.getAttributePredicate();
        for (AttributePredicate attrPredicate: attrPredicates){
            MyPredicate myPredicate = this.parsePredicate(attrPredicate);
            this.myPredicates.add(myPredicate);
        }
    }


    /**
     * Parses a predicate from XML-based object to MyPredicate to use for creating Idemix ProofSpec
     */
    private MyPredicate parsePredicate(AttributePredicate attrPredicate){

        MyPredicate myPredicate = new MyPredicate();
        for (Object param : attrPredicate.getAttributeOrConstantValue()) { //parse arguments first
            if (param instanceof Attribute) {  // if it is an attribute

                AttributePredicate.Attribute attParam = (AttributePredicate.Attribute) param;

                URI credentialAlias = attParam.getCredentialAlias(); //get cred alias of the attributes in the predicate
                MyAttributeReference myAttributeRef = null;
                URI attributeType = attParam.getAttributeType();
                String key = credentialAlias.toString()+attributeType.toString();
                if (!this.attrRefsCache.containsKey(key)){
                    myAttributeRef = new MyAttributeReference(credentialAlias, attributeType);
                    this.attrRefsCache.put(key, myAttributeRef);
                    //add new ref to a map <CredAlias, List<MyAttrReference>>
                    this.addAttributeRefToCredentialAlias(credentialAlias.toString(), myAttributeRef);
                } else {
                    myAttributeRef = this.attrRefsCache.get(key);
                }
                myPredicate.addArgument(myAttributeRef,null);
                URI encoding = this.getAttributeEncoding(credentialAlias, attributeType);
                myPredicate.setEncoding(encoding);

            } else {  // if not an attribute - extract a constant as a BigInteger!
                MyAttributeValue constantValue =
                        MyAttributeValueFactory.parseValueFromFunction(attrPredicate.getFunction(), param);
                MyAttributeReference myAttributeRef = null;
                try {
                    myAttributeRef = new MyAttributeReference(new URI(""), new URI(Constants.CONSTANT));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                myPredicate.addArgument(myAttributeRef, constantValue);
            }
        }
        String type = MyAttributeValueFactory.returnTypeOfFunction(attrPredicate.getFunction());
        OperationType mode = MyAttributeValueFactory.operationTypeOfFunction(attrPredicate.getFunction());

        myPredicate.setTypeOfFunction(type);
        myPredicate.setModeOfOperation(mode);

        return myPredicate;
    }

    private URI getAttributeEncoding(URI credentialAlias, URI attributeType) {
        CredentialSpecification credSpec = this.credentialSpecList.get(credentialAlias.toString());
        URI encoding = null;
        for(AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()){
            if(ad.getType().equals(attributeType)){
                encoding = ad.getEncoding();
            }
        }
        return encoding;
    }


    /**
     * Determines in which of the given equivalence classes the given attribute reference is contained in and returns this class.<br/>
     * In case the reference is not contained in a class yet, a new class containing the given reference is created and returned.
     * 
     * @param attrRef
     * @param equivalenceClasses
     * @return
     */
    private static Set<MyAttributeReference> getEquivalenceClass(final MyAttributeReference attrRef, Set<Set<MyAttributeReference>> equivalenceClasses) {
        for (Set<MyAttributeReference> equivClass : equivalenceClasses) {
            if (equivClass.contains(attrRef)) {
                return equivClass;
            }
        }

        // No class found, thus, attrRef is not contained in a class yet.
        // Create a new class and add attrRef to this new class.
        Set<MyAttributeReference> newClass = new HashSet<MyAttributeReference>();
        newClass.add(attrRef);

        // Add the new class to the set of classes and return the newly created class.
        equivalenceClasses.add(newClass);
        return newClass;
    }

    /**
     * Merges the two equivalence classes of the two given attribute references.<br/>
     * First, determines the classes for the individual references and then merges them in case they are different.<br/>
     * <br/>
     * @param attrRef1 The first attribute reference.
     * @param attrRef2 The second attribute reference.
     * @param equivalenceClasses The set of all equivalence classes.
     */
    private static void mergeEquivalenceClasses(final MyAttributeReference attrRef1, final MyAttributeReference attrRef2, Set<Set<MyAttributeReference>> equivalenceClasses) {
        Set<MyAttributeReference> equivClassLeft = getEquivalenceClass(attrRef1, equivalenceClasses);
        Set<MyAttributeReference> equivClassRight = getEquivalenceClass(attrRef2, equivalenceClasses);

        if (equivClassLeft != equivClassRight) {
            // Remove both classes and re-add the merged class. Note: do not merge the classes into one and
            // remove the other class: this leads to unspecified behavior because members of a Java Set must
            // not be changed in a manner that affects equals comparisons while the object is an element in
            // the set (see JavaDoc of Set).

            equivalenceClasses.remove(equivClassLeft);
            equivalenceClasses.remove(equivClassRight);

            equivClassLeft.addAll(equivClassRight);
            equivalenceClasses.add(equivClassLeft);
        }
    }

    /**
     * Builds sets of attributes that are in direct or indirect equality relationship (==) with another attribute,
     *     and all attributes that are disclosed.
     *     Here it does not matter whether the attribute is disclosed explicitly (via the given disclosedAttributes mapping) or
     *     implicitly (via transitive equality relations in the given predicates).<br/>
     *     The map entries for these attributes point to identifiers that have proof mode {@link ProofMode#REVEALED}.
     * <br/>
     * To determine above entries, the method creates equivalence classes of attribute references where all attribute references
     * that are contained in the same class are equal according to the given predicates. Note that also transitive equality is considered.<br/>
     * Then, equivalence classes are marked as 'revealed' if at least one of the attribute references contained in the class is revealed.<br/>
     * <br/>
     */
    private final void createEquivalenceClassesForRevealedAndOneOfAttributes() throws Exception {

        for (MyPredicate predicate : this.myPredicates) {
            //get arguments from the predicate, define their type (const/attr)
            //first we consider ONEOF predicate - multiple arguments
            if (predicate.getFunction().equals(Constants.OperationType.EQUALONEOF)){
                MyAttributeReference l = predicate.getLeftRef();
                this.oneOfAttrs.add(l);
            } else {
                //consider only predicates with 2 arguments
                MyAttributeReference l = predicate.getLeftRef();
                MyAttributeReference r = predicate.getRightRef();
                boolean lIsConstant = l.isConstant();
                boolean rIsConstant = r.isConstant();
                //first we check equality predicates:
                if (predicate.getFunction().equals(Constants.OperationType.EQUAL)){
                    if (!lIsConstant && !rIsConstant) {
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        // PS(a.b == c.d) => to make a.b and c.d refer to the same identifiers in the proof specification
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        mergeEquivalenceClasses(l,  r, this.attrRefEqivClasses);

                    } else if (!lIsConstant && rIsConstant) {
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        // PS(a.b == constExp) => reveal a.b
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        this.revealedAttrRefEqivClasses.add(getEquivalenceClass(l, this.attrRefEqivClasses));
                        MyAttributeValue objValue = MyAttributeEncodingFactory.parseValueFromEncoding(predicate.getEncoding(), predicate.getRightVal().getValueAsObject(), predicate.getRightVal().getAllowedValues());
                        this.allAttrsAndValues.put(l, objValue);
                    } else if (lIsConstant && !rIsConstant) {
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        // PS(constExp == a.b) => reveal a.b
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        this.revealedAttrRefEqivClasses.add(getEquivalenceClass(r, this.attrRefEqivClasses));
                        MyAttributeValue objValue = MyAttributeEncodingFactory.parseValueFromEncoding(predicate.getEncoding(), predicate.getLeftVal().getValueAsObject(), predicate.getLeftVal().getAllowedValues());
                        this.allAttrsAndValues.put(r, objValue);
                    } else if (lIsConstant && rIsConstant) {
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        // PS(constExp == constExp) => Nothing to prove for Idemix.
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        
                    } else {
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        // PS(constNonAttrExp == nonConstNonAttrExp) 		=> Not supported.
                        // PS(nonConstNonAttrExp == constNonAttrExp) 		=> Not supported.
                        // PS(nonConstNonAttrExp == nonConstNonAttrExp) 	=> Not supported.
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        throw new Exception("Generation of an Idemix Proof Specification for expression '"+predicate.getPredicateAsString()+"' is currently not supported.");
                    }
                }
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Mark those classes as 'revealed' that contain attributes that are revealed by the claim.
            for (MyAttributeReference attRef : this.explicitlyDisclosedAttrs) {
                this.revealedAttrRefEqivClasses.add(getEquivalenceClass(attRef, this.attrRefEqivClasses));

            }
        }//end for mypredicates
    }

    /**
     * This method builds a list of all implicitly and explicitly disclosed attributes
     * and their values used.
     */
    private void setAllDisclosedAttributes(){
        for(Set<MyAttributeReference> eqClass: this.revealedAttrRefEqivClasses){
            for(MyAttributeReference atRef: eqClass){
                this.allDisclosedAttrs.add(atRef);
                MyAttributeValue myAttributeValue = this.allAttrsAndValues.get(atRef);
                this.allDisclosedAttrsAndValues.put(atRef, myAttributeValue);
            }
        }
    }

    /**
     * This method extracts all explicitly disclosed and inspectable attributes from the token description
     * @param ptd
     * @param aliasCreds
     * @return Map matching alias of the credential with the type of the disclosed attribute
     */

    private void parseAllExplicitlyDisclosedAndInspectableAttributes(){
        List<CredentialInToken> credsintok = null;
        int count = 0;
        credsintok = this.ptd.getCredential();
        count = credsintok.size();
        for (int i = 0; i< count; i++){
            CredentialInToken credintok = null;
            CredentialSpecification credSpec = null;
            List<AttributeInToken> attrsintok = null;
            credintok = credsintok.get(i);
            credSpec = this.credentialSpecList.get(credintok.getAlias().toString());
            attrsintok = credintok.getDisclosedAttribute();

            List<AttributeDescription> attrDescriptions = credSpec.getAttributeDescriptions().getAttributeDescription();

            for (AttributeInToken attrintok : attrsintok){
                MyAttributeValue objValue = null;
                MyAttributeReference ref = null;
                if(credintok != null) {
                    ref = new MyAttributeReference(credintok.getAlias(),
                            attrintok.getAttributeType());
                }
                if (attrintok.getInspectorPublicKeyUID()!=null){
                    this.inspectableAttrs.add(ref);
                    this.attrRefsCache.put(credintok.getAlias().toString()+attrintok.getAttributeType().toString(), ref);
                    MyAttributeReference attrRef = new MyAttributeReference(credintok.getAlias(), attrintok.getAttributeType());
                    this.attrRefToInspectorKey.put(attrRef, attrintok.getInspectorPublicKeyUID());
                    this.inspectableAttrValues.put(attrRef, null);
                }else{
                    this.explicitlyDisclosedAttrs.add(ref);
                    this.allDisclosedAttrs.add(ref);
                    Object val = attrintok.getAttributeValue();
                    for (AttributeDescription ad: attrDescriptions){
                        if (ad.getType().equals(attrintok.getAttributeType())){
                            objValue = MyAttributeEncodingFactory.parseValueFromEncoding(ad.getEncoding(), val, new EnumAllowedValues(ad));
                        }
                         this.allDisclosedAttrsAndValues.put(ref, objValue);
                    }
                }
                this.allAttrsAndValues.put(ref, objValue);
                this.attrRefsCache.put(credintok.getAlias().toString()+attrintok.getAttributeType().toString(), ref);
                this.addAttributeRefToCredentialAlias(credintok.getAlias().toString(), ref);
            }
            List<URI> credUIDs = new ArrayList<URI>();

            credUIDs.add(credintok.getCredentialSpecUID());
            credUIDs.add(credintok.getIssuerParametersUID());
            this.aliasCredInTokenList.put(credintok.getAlias().toString(), credintok);
        }
    }

    public final void createListOfEqualOneOfAttributes(){

    }
    
    private Map<String,List<FriendlyDescription>> composeCredAliasFriendlyDescrList
	(Map<String, CredentialSpecification> aliasCredSpecs){
    	Map<String,List<FriendlyDescription>> ret = new HashMap<String, List<FriendlyDescription>>();
    	for (String alias: aliasCredSpecs.keySet()){
    		ret.put(alias, aliasCredSpecs.get(alias).getFriendlyCredentialName());
    	}
    	return ret;
    }

    private Map<MyAttributeReference,List<FriendlyDescription>> composeAttrRefsFriendlyDescrList
    (Map<String, CredentialSpecification> aliasCredSpecs, Map<String,List<MyAttributeReference>> credAliasAttributeRefList){
    	Map<MyAttributeReference, List<FriendlyDescription>> ret = new HashMap<MyAttributeReference, List<FriendlyDescription>>();
    	for (String alias: aliasCredSpecs.keySet()){
    		CredentialSpecification credSpec = aliasCredSpecs.get(alias);
    		for (AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()){
    			ret.put(attrRefsCache.get(alias+ad.getType().toString()), ad.getFriendlyAttributeName());
    		}
    	}
    	return ret;
    }

    
    //Return stuff
    /**
	* @return the credAliasFriedlyDescrList
	*/
    public Map<String, List<FriendlyDescription>> getCredAliasFriedlyDescrList() {
    	return credAliasFriedlyDescrList;
    }

	public Map<MyAttributeReference, List<FriendlyDescription>> getAttrRefFriedlyDescrList() {
		return attrRefFriedlyDescrList;
	}

    public List<MyPredicate> getAllPredicates(){
        return this.myPredicates;
    }

    public Map<MyAttributeReference, MyAttributeValue> getAllDisclosedAttributesAndValues() {
        return this.allDisclosedAttrsAndValues;
    }

    public Map<String, CredentialSpecification> getCredSpecList() {
        return this.credentialSpecList;
    }

    public String getMessageToSign(){
        return this.messageToSign;
    }


}

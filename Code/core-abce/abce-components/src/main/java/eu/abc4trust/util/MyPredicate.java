//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.util.Constants.OperationType;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;

/**
 * Helper class for parsing the policy
 */
public class MyPredicate {
	
	
	//format of my predicate: 
	//typeOfArguments: {Constants.DATE, ...} defines type of arguments
	//function: {OperationType.EQUAL, ...}, defines mode of operation
	//arguments: {(Reference, Value) = left, (Reference, Value)=right, (Reference, Value) ...}, 
	//Reference can be "CONSTANT" to distinguish between constants and attribute values from credentials for creating Idemix Claim and proof
	
	private String typeOfArguments;
	private URI encodingOfArguments;
	private OperationType function;
	private List<Map<MyAttributeReference, MyAttributeValue>> arguments;
	private List<MyAttributeReference> argumentRefs;
	
	public MyPredicate() {
		this.arguments = new ArrayList<Map<MyAttributeReference, MyAttributeValue>>();
		this.function = OperationType.EQUAL;  //just to init
		this.typeOfArguments = new String("");
		this.encodingOfArguments = URI.create("");
		this.argumentRefs = new ArrayList<MyAttributeReference>();
}
	
	public MyPredicate getMyPredicate(){
		return this;
	}
	
	public String getTypeOfArguments(){
		return this.typeOfArguments;
	}
	
	public OperationType getFunction(){
		return this.function;
	}
	
	public void setTypeOfFunction(String type){
		this.typeOfArguments = type;
	}
	
	public void setEncoding(URI encoding){
		this.encodingOfArguments = encoding;
	}
	
	public URI getEncoding(){
		return this.encodingOfArguments;
	}
	
	public void setModeOfOperation(OperationType mode){
		this.function = mode;
	}
	
	/**
	 * Adds an argument to the predicate
	 * @param ref
	 * @param value
	 */
	
	public void addArgument(MyAttributeReference ref, MyAttributeValue value){
		Map<MyAttributeReference,MyAttributeValue> map = new HashMap<MyAttributeReference, MyAttributeValue>();	
		map.put(ref, value);
		addArgument(map);
		argumentRefs.add(ref);
	}
	
	/**
	 * Adds an argument to the predicate
	 * @param map
	 */
	public void addArgument(Map<MyAttributeReference, MyAttributeValue> map){
		arguments.add(map);	
	}
	
	/**
	 * Adds a reference of the argument to the predicate
	 * @param map
	 */
	public void addArgumentRef(MyAttributeReference ref){
		argumentRefs.add(ref);	
	}
	
	/**
	 * Adds a value to the argument, replaces the reference
	 * @param map
	 */
	public void addArgumentVal(MyAttributeReference ref, MyAttributeValue value){
		List<Map<MyAttributeReference,MyAttributeValue>> newArguments = new ArrayList<Map<MyAttributeReference,MyAttributeValue>>();
		List<MyAttributeReference> newArgumentRefs = new ArrayList<MyAttributeReference>();
		for (Map<MyAttributeReference,MyAttributeValue> map: arguments){
			MyAttributeReference mar = (MyAttributeReference) map.keySet().toArray()[0];
			if ((mar.getAttributeReference().equals(ref.getAttributeReference()))){
				MyAttributeReference newmar = new MyAttributeReference(URI.create(""), URI.create(Constants.CONSTANT));
			//	MyAttributeValue newValue = MyAttributeEncodingFactory.parseValueFromEncoding(this.encodingOfArguments, value);					
				map.remove(mar);
				map.put(newmar, value);
				newArgumentRefs.add(newmar);
			} else {
				newArgumentRefs.add(mar);
			}
			newArguments.add(map);
		}
		arguments = newArguments;
		argumentRefs = newArgumentRefs;
	}
	
	/**
	 * Returns the inversion of the given {@link OperationType inequality operator}.
	 * 
	 * @param op The operator whose inverted operator shall be returned.
	 * @return the inversion of the given inequality operator.
	 * @throws Exception in case the given operator is null or unknown.
	 */
	private static OperationType invertInequalityOperator(final OperationType op) throws Exception {
		if (op==null)
			throw new Exception("Unknown operator: " + op);
		
		switch (op) {
			case GREATER: return OperationType.LESS;
			case GREATEREQ: return OperationType.LESSEQ;
			case LESS: return OperationType.GREATER;
			case LESSEQ: return OperationType.GREATEREQ;
			case NOTEQUAL: return OperationType.NOTEQUAL;
			default: throw new Exception("Unknown operator: " + op);
		}
	}
	
	public MyPredicate invertPredicate() {
		MyPredicate ret = new MyPredicate();
		try {
			ret.setModeOfOperation(invertInequalityOperator(function));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret.addArgument(getRight());
		ret.addArgument(getLeft());
		ret.addArgumentRef(getRightRef());
		ret.addArgumentRef(getLeftRef());
		ret.setTypeOfFunction(getTypeOfArguments());
		ret.setEncoding(getEncoding());
			
		return ret;
	}
	
	public List<Map<MyAttributeReference, MyAttributeValue>> getArguments(){
		return arguments;	
	}
	
	/*
	 * returns only references of the arguments
	 */
	public List<MyAttributeReference> getArgumentReferences() {
		return argumentRefs;
	}
	
	//only for 2 paramater functions:
	public MyAttributeReference getRightRef(){
		//TODO: check for 2-parameters function
		return argumentRefs.get(1);	
	}
	
	//only for 2 parameter function
	public MyAttributeReference getLeftRef(){
		//TODO: check for 2-parameters function
		return argumentRefs.get(0);	
	}
	
	//only for 2 paramater functions:
	public MyAttributeValue getRightVal(){
		//TODO: check for 2-parameters function
		if (arguments.get(1).get((MyAttributeReference)getRightRef())!=null){
		return arguments.get(1).get((MyAttributeReference)getRightRef());
		}else return null;
	}
	
	//only for 2 parameter function
	public MyAttributeValue getLeftVal(){
		//TODO: check for 2-parameters
		return arguments.get(0).get((MyAttributeReference)getLeftRef());
	}
	
	//only for 2 paramater functions:
	public Map<MyAttributeReference, MyAttributeValue> getRight(){
		//TODO: check for 2-parameters function
		return arguments.get(1);
	}
	
	//only for 2 parameter function
	public Map<MyAttributeReference, MyAttributeValue> getLeft(){
		//TODO: check for 2-parameters
		return arguments.get(0);
	}
	
	/**
	 * Returns predicate as a String
	 * @return
	 */
	public String getPredicateAsString(){
		String str = new String("");

		str = getLeftRef().getCredentialAlias()+": "+ getLeftRef().getAttributeReference()+ " "+
		this.function.toString()+"("+this.typeOfArguments+") "+
				getRightRef().getAttributeReference();		
		
		return str;
		
	}
	

	public String getFriendlyFunction() throws Exception {
			if (this.function == null)
				throw new Exception("No function specified");			
			switch (this.function) {
			case EQUAL: return " is equal to ";
			case NOTEQUAL: return " is not equal to ";
			case GREATER: return " is greater than ";
			case GREATEREQ: return " is greater or equal than ";
			case LESS: return " is less than ";
			case LESSEQ: return " is less or equal than ";
			case EQUALONEOF: return " equals to one of: "; 
			default: throw new Exception("Unknown operator: " + this.function.toString());
		}
	}
		

	public boolean evaluateConstantExpression() {
		MyAttributeValue l = this.getLeftVal();
		MyAttributeValue r = this.getRightVal();
		
		switch (this.getFunction()) {
			case EQUAL:		        
				return l.isCompatibleAndEquals(r);	
				
			case NOTEQUAL:		       
				return l.isCompatibleAndNotEquals(r);
			     
			//TODO: add support for one-of
				//case EQUALONEOF: {
			    
			case LESS:			       
				return l.isCompatibleAndLess(r);
			      
			case LESSEQ:			        
				return l.isCompatibleAndLessOrEqual(r);

			case GREATER:		        
				return r.isCompatibleAndLess(l);
			      
			case GREATEREQ:		       
				return r.isCompatibleAndLessOrEqual(l);
			     
			default:
				throw new RuntimeException("Problem with evaluating function: '" + function + "'");

	}
}
	

}

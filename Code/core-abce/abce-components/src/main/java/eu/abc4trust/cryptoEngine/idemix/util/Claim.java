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

package eu.abc4trust.cryptoEngine.idemix.util;

import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmx.showproof.Proof;

import eu.abc4trust.util.MyAttributeReference;
import eu.abc4trust.util.MyPredicate;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.PseudonymInToken;



/**
 * A claim comprises:
 * <ol>
 * <li>a statement that a user makes about her credential attributes.
 * <li>a possibly empty set of disclosed attributes.
 * </ol>
 */
public interface Claim {
	
	/**
	 * Returns the claim's statement.<br/>
	 * As the 'claim language' (or, 'statement language') is a subset of the 'policy language', we reuse the {@link Policy} class to represent the claim's statement.
	 * However, the following constraints apply to a policy when used as statement:
	 * <ul>
	 * <li>Credential declarations must have exactly one issuer.
	 * <li>Disclosure requests (i.e., "reveal-lines") must not be used.
	 * </ul>
	 * Note that the credentials assigned to references that appear in the same credential declaration of a given claim must all have the same issuer.
	 * @return the claim's statement.
	 */
	public Map<MyPredicate, List<CredentialInToken>> getPredicateCredentialList();
	
	/**
	 * Returns the attributes that are disclosed by this claim.<br/>
	 * The attributes are returned as map where the map's keys are references to attributes of credentials declared in this claim's statement.
	 * The map's values are the values of the disclosed attributes.<br/>
	 * @return the attributes that are disclosed by this claim.
	 */
	public Map<MyAttributeReference, MyAttributeValue> getAllDisclosedAttributesAndValues();
	
	public List<MyAttributeReference> getInspectableAttributes();
	
	public Proof getEvidence();
	
	public List<PseudonymInToken> getPseudonyms(); 
	
	//TODO: handle revocation

}
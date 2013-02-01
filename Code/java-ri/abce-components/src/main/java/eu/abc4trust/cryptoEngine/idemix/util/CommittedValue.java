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

package eu.abc4trust.cryptoEngine.idemix.util;

import java.math.BigInteger;
import java.net.URI;
import java.util.Vector;

public class CommittedValue {

	private String alias;
	private URI credSpecUID;
	private final Vector<BigInteger> bases;
	private final Vector<BigInteger> exponents;
	private BigInteger modulus;
	private BigInteger commitmentValue;
	private String attributeType;
	private URI inspectorPublicKey;

	public CommittedValue(){
		this.modulus = null;
		this.exponents = new Vector<BigInteger>();
		this.bases = new Vector<BigInteger>();
		this.setCredSpecUID(null);
		this.alias = null;
		this.attributeType = null;
		this.inspectorPublicKey = null;
		this.setCommitmentValue(null);
	}
	
	public BigInteger getModulus() {
		return modulus;
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus;
	}

	public Vector<BigInteger> getExponents() {
		return exponents;
	}

	public Vector<BigInteger> getBases() {
		return bases;
	}

	public URI getCredSpecUID() {
		return credSpecUID;
	}

	public void setCredSpecUID(URI credSpecUID) {
		this.credSpecUID = credSpecUID;
	}

	public String getAlias(){
		return this.alias;
	}
	
	public void setAlias(String alias){
		this.alias = alias;
	}

	public BigInteger getCommitmentValue() {
		return commitmentValue;
	}

	public void setCommitmentValue(BigInteger commitmentValue) {
		this.commitmentValue = commitmentValue;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	
	public String getAttributeType(){
		return this.attributeType;
	}

	public URI getInspectorPublicKey() {
		return inspectorPublicKey;
	}

	public void setInspectorPublicKey(URI inspectorPublicKey) {
		this.inspectorPublicKey = inspectorPublicKey;
	}
	
	
}

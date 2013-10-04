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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Used when initializing a smartcard with UProve
 * @author kasperdamgard
 *
 */
public class UProveParams implements Serializable, GroupParameters{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2360399659922062949L;
	public BigInteger g, p, q, f;
		
	/**
	 * 
	 * @param g generator
	 * @param p modulus
	 * @param q group order
	 * f cofactor is computed as (p-1)/q
	 */
	public UProveParams(BigInteger g, BigInteger p, BigInteger q){
		this.g = g;
		this.p = p;
		this.q = q;
		this.f = p.subtract(BigInteger.ONE).divide(q);
	}

	@Override
	public boolean isIdemixGroupParameters() {
		return false;
	}

	@Override
	public BigInteger getModulus() {
		return p;
	}	
}

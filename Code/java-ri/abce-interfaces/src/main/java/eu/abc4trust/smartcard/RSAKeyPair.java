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

public class RSAKeyPair implements Serializable{
	
	private static final long serialVersionUID = 42L;
	
	private BigInteger n, phi;
	private BigInteger p, q;
	
	public int sizeModulusBytes;
	
	public RSAKeyPair(BigInteger p, BigInteger q){
		this.n = p.multiply(q);
		this.phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		this.p = p;
		this.q = q;
	}
	
	public BigInteger getP(){
		return p;
	}
	
	public BigInteger getQ(){
		return q;
	}	
	
	public BigInteger getN(){
		return n;
	}
	
	public BigInteger getPhi(){
		return phi;
	}
	
}

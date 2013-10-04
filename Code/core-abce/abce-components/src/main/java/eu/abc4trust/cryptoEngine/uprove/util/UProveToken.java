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

package eu.abc4trust.cryptoEngine.uprove.util;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Serializable wrapper class for UProveTokens
 * 
 * @author Raphael Dobers
 */

public class UProveToken implements Serializable {

	private static final long serialVersionUID = -2530388270212043647L;

	private byte[] h;
	private Boolean isDeviceProtected;
	private byte[] pi;
	private byte[] sigmaCPrime;
	private byte[] sigmaRPrime;
	private byte[] sigmaZPrime;
	private byte[] ti;
	private byte[] uidp;

	private Set<URI> pseudonyms;

	public byte[] getH() {
		return h;
	}

	public void setH(byte[] h) {
		this.h = h;
	}

	public Boolean isDeviceProtected() {
		return isDeviceProtected;
	}

	public void setDeviceProtected(Boolean isDeviceProtected) {
		this.isDeviceProtected = isDeviceProtected;
	}

	public byte[] getPi() {
		return pi;
	}

	public void setPi(byte[] pi) {
		this.pi = pi;
	}

	public byte[] getSigmaCPrime() {
		return sigmaCPrime;
	}

	public void setSigmaCPrime(byte[] sigmaCPrime) {
		this.sigmaCPrime = sigmaCPrime;
	}

	public byte[] getSigmaRPrime() {
		return sigmaRPrime;
	}

	public void setSigmaRPrime(byte[] sigmaRPrime) {
		this.sigmaRPrime = sigmaRPrime;
	}

	public byte[] getSigmaZPrime() {
		return sigmaZPrime;
	}

	public void setSigmaZPrime(byte[] sigmaZPrime) {
		this.sigmaZPrime = sigmaZPrime;
	}

	public byte[] getTi() {
		return ti;
	}

	public void setTi(byte[] ti) {
		this.ti = ti;
	}

	public byte[] getUidp() {
		return uidp;
	}

	public void setUidp(byte[] uidp) {
		this.uidp = uidp;
	}

	public Set<URI> getPseudonyms() {
		if (pseudonyms == null)	this.pseudonyms = new HashSet<URI>();
		return pseudonyms;
	}

}

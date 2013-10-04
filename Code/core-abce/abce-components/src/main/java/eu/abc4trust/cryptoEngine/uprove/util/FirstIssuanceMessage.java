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
import java.util.List;

public class FirstIssuanceMessage implements Serializable {

	/**
	 * Serializable wrapper class for UProve FirstIssuanceMessage
	 * 
	 * @author Raphael Dobers
	 */

	private static final long serialVersionUID = 4994890920830740702L;
	private List<byte[]> sigmaA;
	private List<byte[]> sigmaB;
	private byte[] sigmaZ;
	private String sessionKey;

	public List<byte[]> getSigmaA() {
		return sigmaA;
	}

	public void setSigmaA(List<byte[]> sigmaA) {
		this.sigmaA = sigmaA;
	}

	public List<byte[]> getSigmaB() {
		return sigmaB;
	}

	public void setSigmaB(List<byte[]> sigmaB) {
		this.sigmaB = sigmaB;
	}

	public byte[] getSigmaZ() {
		return sigmaZ;
	}

	public void setSigmaZ(byte[] sigmaZ) {
		this.sigmaZ = sigmaZ;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
}

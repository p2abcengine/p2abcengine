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

public class ThirdIssuanceMessage implements Serializable {

	/**
	 * Serializable wrapper class for UProve ThirdIssuanceMessage
	 * 
	 * @author Raphael Dobers
	 */

	private static final long serialVersionUID = 2950487234794029834L;
	private List<byte[]> sigmaR;
	private String sessionKey;

	public List<byte[]> getSigmaR() {
		return sigmaR;
	}

	public void setSigmaR(List<byte[]> sigmaR) {
		this.sigmaR = sigmaR;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
}

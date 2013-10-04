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

/**
 * Serializable wrapper class for UProveKeyAndTokens composite
 * 
 * @author Raphael Dobers
 */

public class UProveKeyAndToken implements Serializable {

	private static final long serialVersionUID = 1590959893940809029L;

	private byte[] privateKey;
	private UProveToken token;

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public UProveToken getToken() {
		return token;
	}

	public void setToken(UProveToken token) {
		this.token = token;
	}
}

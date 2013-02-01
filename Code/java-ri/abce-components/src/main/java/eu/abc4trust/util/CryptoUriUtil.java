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

package eu.abc4trust.util;

import java.net.URI;

public final class CryptoUriUtil {
	private static String uProveStr = "urn:abc4trust:1.0:algorithm:uprove";
	private static String ideMixStr = "urn:abc4trust:1.0:algorithm:idemix";
	private static String hashSha256Str = "urn:abc4trust:1.0:hashalgorithm:sha-256";
	
	private CryptoUriUtil() {
		throw new AssertionError();
	}
	
	public static URI getIdemixMechanism() {
		return URI.create(ideMixStr);
	}
	
	public static URI getUproveMechanism() {
		return URI.create(uProveStr);
	}
	
	public static URI getHashSha256() {
		return URI.create(hashSha256Str);
	}
	
	public static String getUproveString() {
		return uProveStr;
	}
	
	public static String getIdeMixString() {
		return ideMixStr;
	}
	
	public static String getHashSha256String() {
		return hashSha256Str;
	}
	
}

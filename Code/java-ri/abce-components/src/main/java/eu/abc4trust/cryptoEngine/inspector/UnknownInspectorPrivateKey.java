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

package eu.abc4trust.cryptoEngine.inspector;

public class UnknownInspectorPrivateKey extends Exception {
	private static final long serialVersionUID = 7831873935896973639L;
	public UnknownInspectorPrivateKey(String message) {
        super(message);
    }
	public UnknownInspectorPrivateKey(Exception ex) {
		super(ex);
	}

	
}

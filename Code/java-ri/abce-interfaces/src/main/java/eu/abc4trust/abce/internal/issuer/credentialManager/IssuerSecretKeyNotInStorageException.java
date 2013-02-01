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

package eu.abc4trust.abce.internal.issuer.credentialManager;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;

public class IssuerSecretKeyNotInStorageException extends
CredentialManagerException {

    private static final long serialVersionUID = 4628293709210896635L;

    public IssuerSecretKeyNotInStorageException(String message) {
        super(message);
    }

}

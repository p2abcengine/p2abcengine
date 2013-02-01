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

import java.net.URI;
import java.util.List;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.xml.SecretKey;

public interface CredentialManager {
    /**
     * This method returns the issuer secret key in the credential store.
     * 
     * @return
     * @throws CredentialManagerException
     */
    public List<URI> listIssuerSecretKeys()
            throws CredentialManagerException;

    /**
     * This method returns the issuer secret key with the given unique issuer
     * parameters identifier issuerParamsUid.
     * 
     * @param issuerParamsUid
     * @return
     * @throws CredentialManagerException
     */
    public SecretKey getIssuerSecretKey(URI issuerParamsUid)
            throws CredentialManagerException;

    /**
     * This method stores the given issuer secret key in the credential store.
     * 
     * @param issuerSecretKey
     * @throws CredentialManagerException
     */
    public void storeIssuerSecretKey(URI issuerParamsUid,
            SecretKey issuerSecretKey)
                    throws CredentialManagerException;
}


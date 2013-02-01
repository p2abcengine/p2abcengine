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

package eu.abc4trust.abce.internal.inspector.credentialManager;

import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.SecretKey;

public interface CredentialManager {
    /*
     * This method returns the inspector secret key in the credential store.
     * 
     * @return
     * @throws CredentialManagerException
     *
     */
    public List<URI> listInspectorSecretKeys()
            throws CredentialManagerException;

    /**
     * This method returns the inspector secret key with the given unique inspector
     * parameters identifier inspectorKeyUID.
     * 
     * @param inspectorKeyUID
     * @return
     * @throws CredentialManagerException
     */
    public SecretKey getInspectorSecretKey(URI inspectorKeyUID)
            throws CredentialManagerException;

    /**
     * This method stores the given inspector secret key in the credential store.
     * 
     * @param inspectorKeyUID
     * @param inspectorSecretKey
     * @throws CredentialManagerException
     */
    public void storeInspectorSecretKey(URI inspectorKeyUID,
            SecretKey inspectorSecretKey)
                    throws CredentialManagerException;

}

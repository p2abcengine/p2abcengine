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

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface CredentialStorage {
	List<URI> listInspectorSecretKeys() throws Exception;
    byte[] getInspectorSecretKey(URI inspectorPkUid) throws Exception;
    void addInspectorSecret(URI inspectorPkUid, byte[] bytes) throws IOException, Exception;
}

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

import java.io.File;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentCredentialStorage implements CredentialStorage {

    private final File inspectorSecretKeyFile;

    @Inject
    public PersistentCredentialStorage(
            @Named("InspectorSecretKeyStorageFile") File inspectorSecretKeyFile) {
        super();
        this.inspectorSecretKeyFile = inspectorSecretKeyFile;
    }

    @Override
    public List<URI> listInspectorSecretKeys() throws Exception {
        return StorageUtil.getAllUris(this.inspectorSecretKeyFile);
    }

    @Override
    public byte[] getInspectorSecretKey(URI inspectorPkUid) throws Exception {
        return StorageUtil.getData(this.inspectorSecretKeyFile, inspectorPkUid);
    }

    @Override
    public void addInspectorSecret(URI inspectorPkUid, byte[] bytes)
            throws Exception {
    	// Check to see if we have a key with the inspectorPkUid in the storage layer.
    	// If we do delete it and add the new.
    	if (StorageUtil.HasUri(this.inspectorSecretKeyFile, inspectorPkUid)) {
    		StorageUtil.deleteData(this.inspectorSecretKeyFile, inspectorPkUid);
    	}
        StorageUtil.appendData(this.inspectorSecretKeyFile, inspectorPkUid, bytes);
    }
}
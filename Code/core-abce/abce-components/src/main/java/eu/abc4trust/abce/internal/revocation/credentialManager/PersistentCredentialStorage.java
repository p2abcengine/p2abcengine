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

package eu.abc4trust.abce.internal.revocation.credentialManager;

import java.io.File;
import java.net.URI;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentCredentialStorage implements CredentialStorage {
    private final File secretKeyFile;

    @Inject
    public PersistentCredentialStorage(
            @Named("RevocationAuthoritySecretKeyStorageFile") File secretKeyFile) {
        super();
        this.secretKeyFile = secretKeyFile;
    }

    @Override
    public byte[] getData(URI inspectorPkUid) throws Exception {
        return StorageUtil.getData(this.secretKeyFile, inspectorPkUid);
    }

    @Override
    public void addData(URI uid, byte[] bytes) throws Exception {
        // Check to see if we have a key with the uid in the storage
        // layer.
        // If we do delete it and add the new.
        if (StorageUtil.HasUri(this.secretKeyFile, uid)) {
            StorageUtil.deleteData(this.secretKeyFile, uid);
        }
        StorageUtil.appendData(this.secretKeyFile, uid, bytes);
    }

    @Override
    public void delete(URI uid) throws Exception {
        if (StorageUtil.HasUri(this.secretKeyFile, uid)) {
            StorageUtil.deleteData(this.secretKeyFile, uid);
        }
    }
}

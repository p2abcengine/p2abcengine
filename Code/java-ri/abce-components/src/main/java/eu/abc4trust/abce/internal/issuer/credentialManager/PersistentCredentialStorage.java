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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentCredentialStorage implements CredentialStorage {

    private final File issuerSecretKeyFile;

    @Inject
    public PersistentCredentialStorage(
            @Named("IssuerSecretKeyStorageFile") File issuerSecretKeyFile) {
        super();
        this.issuerSecretKeyFile = issuerSecretKeyFile;
    }

    @Override
    public List<URI> listIssuerSecretKeys() throws Exception {
        return StorageUtil.getAllUris(this.issuerSecretKeyFile);
    }

    @Override
    public byte[] getIssuerSecretKey(URI issuerParamsUid) throws Exception {
        return StorageUtil.getData(this.issuerSecretKeyFile, issuerParamsUid);
    }

    @Override
    public void addIssuerSecret(URI issuerParamsUid, byte[] bytes)
            throws IOException {
        StorageUtil
        .appendData(this.issuerSecretKeyFile, issuerParamsUid, bytes);
    }

}

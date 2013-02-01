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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentCredentialStorage implements CredentialStorage {

    private final File pseudonymFile;
    private final File credentialFile;

    @Inject
    public PersistentCredentialStorage(
            @Named("CredentialStorageFile") File credentialsFile,
            @Named("PseudonymsStorageFile") File pseudonymsFile) {
        super();
        this.credentialFile = credentialsFile;
        this.pseudonymFile = pseudonymsFile;
    }

    @Override
    public byte[] getCredential(URI creduid) throws Exception {
        return StorageUtil.getData(this.credentialFile, creduid);
    }

    @Override
    public byte[] getPseudonymWithData(URI pseudonymUid) throws Exception {
        return StorageUtil.getData(this.pseudonymFile, pseudonymUid);
    }

    @Override
    public List<URI> listCredentials() throws Exception {
        return StorageUtil.getAllUris(this.credentialFile);
    }

    @Override
    public void addCredential(URI credUid, byte[] credBytes) throws IOException {
        StorageUtil.appendData(this.credentialFile, credUid, credBytes);
    }

    @Override
    public void addPseudonymWithMetadata(URI pseudonymUri, byte[] pwmBytes)
            throws IOException {
        StorageUtil.appendData(this.pseudonymFile, pseudonymUri, pwmBytes);
    }

    @Override
    public void deletePseudonymWithMetadata(URI pseudonymUri) throws Exception {
        StorageUtil.deleteData(this.pseudonymFile, pseudonymUri);
    }

    @Override
    public void deleteCredential(URI credUid) throws Exception {    	
        StorageUtil.deleteData(this.credentialFile, credUid);
    }

    @Override
    public List<byte[]> listPseudonyms() throws Exception {
        return StorageUtil.getAllValues(this.pseudonymFile);
    }

}

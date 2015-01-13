//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
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
    public byte[] getCredential(String username, URI creduid) throws Exception {
        return StorageUtil.getData(this.credentialFile, creduid);
    }

    @Override
    public byte[] getPseudonymWithData(String username, URI pseudonymUid) throws Exception {
        return StorageUtil.getData(this.pseudonymFile, pseudonymUid);
    }

    @Override
    public List<URI> listCredentials(String username) throws Exception {
        return StorageUtil.getAllUris(this.credentialFile);
    }

    @Override
    public void addCredential(String username, URI credUid, byte[] credBytes) throws IOException {
        StorageUtil.appendData(this.credentialFile, credUid, credBytes);
    }

    @Override
    public void addPseudonymWithMetadata(String username, URI pseudonymUri, byte[] pwmBytes)
            throws IOException {
        StorageUtil.appendData(this.pseudonymFile, pseudonymUri, pwmBytes);
    }

    @Override
    public void deletePseudonymWithMetadata(String username, URI pseudonymUri) throws Exception {
        StorageUtil.deleteData(this.pseudonymFile, pseudonymUri);
    }

    @Override
    public void deleteCredential(String username, URI credUid) throws Exception {    	
        StorageUtil.deleteData(this.credentialFile, credUid);
    }

    @Override
    public List<byte[]> listPseudonyms(String username) throws Exception {
        return StorageUtil.getAllValues(this.pseudonymFile);
    }

}

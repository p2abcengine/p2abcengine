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

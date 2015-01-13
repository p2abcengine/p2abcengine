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

public class PersistentSecretStorage implements SecretStorage {

    private final File secretFile;

    @Inject
    public PersistentSecretStorage(
            @Named("SecretStorageFile") File secretFile) {
        super();
        this.secretFile = secretFile;

    }

    @Override
    public byte[] getSecret(String username, URI creduid) throws Exception {
        return StorageUtil.getData(this.secretFile, creduid);
    }


    @Override
    public List<URI> listSecrets(String username) throws Exception {
        return StorageUtil.getAllUris(this.secretFile);
    }

    @Override
    public void addSecret(String username, URI secuid, byte[] secBytes) throws IOException {
        StorageUtil.appendData(this.secretFile, secuid, secBytes);
    }

    @Override
    public void deleteSecret(String username, URI secuid) throws Exception {
        StorageUtil.deleteData(this.secretFile, secuid);
    }

}

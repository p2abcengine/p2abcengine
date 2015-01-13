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
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.xml.SecretKey;



public class CredentialManagerImpl implements CredentialManager {
  private final CredentialStorage storage;

  @Inject
  public CredentialManagerImpl(CredentialStorage credentialStore) {
    this.storage = credentialStore;
  }

  @Override
  public List<URI> listInspectorSecretKeys() throws CredentialManagerException {
    try {
      return this.storage.listInspectorSecretKeys();
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public SecretKey getInspectorSecretKey(URI inspectorKeyUID) throws CredentialManagerException {
    if (inspectorKeyUID == null) {
      throw new CredentialManagerException("Inspector UID key is null");
    }
    ObjectInputStream objectInput = null;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      byte[] tokenBytes = this.storage.getInspectorSecretKey(inspectorKeyUID);
      if (tokenBytes == null) {
        throw new CredentialManagerException("Inspector secret key with UID: \"" + inspectorKeyUID
            + "\" is not in storage");
      }
      byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
      objectInput = new ObjectInputStream(byteArrayInputStream);
      SecretKey inspectorSecretKey = (SecretKey) objectInput.readObject();
      return inspectorSecretKey;
    } catch (CredentialManagerException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectInput);
      StorageUtil.closeIgnoringException(byteArrayInputStream);
    }
  }

  @Override
  public void storeInspectorSecretKey(URI inspectorKeyUID, SecretKey inspectorSecretKey)
      throws CredentialManagerException {
    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutput = new ObjectOutputStream(byteArrayOutputStream);
      objectOutput.writeObject(inspectorSecretKey);
      byte[] bytes = byteArrayOutputStream.toByteArray();
      this.storage.addInspectorSecret(inspectorKeyUID, bytes);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }
  }
}

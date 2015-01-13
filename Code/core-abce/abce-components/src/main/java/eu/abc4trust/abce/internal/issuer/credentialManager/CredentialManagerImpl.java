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

package eu.abc4trust.abce.internal.issuer.credentialManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.xml.SecretKey;

public class CredentialManagerImpl implements CredentialManager {

  private final CredentialStorage storage;

  @Inject
  public CredentialManagerImpl(CredentialStorage credentialStore) {
    // WARNING: Due to circular dependencies you MUST NOT dereference
    // cryptoEngineUser
    // in this constructor.
    // (Guice does some magic to support circular dependencies).

    this.storage = credentialStore;
  }

  @Override
  public List<URI> listIssuerSecretKeys() throws CredentialManagerException {
    try {
      return this.storage.listIssuerSecretKeys();
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public SecretKey getIssuerSecretKey(URI issuerParamsUid) throws CredentialManagerException {
    if (issuerParamsUid == null) {
      throw new CredentialManagerException("Issuer parameters UID is null");
    }
    ObjectInputStream objectInput = null;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      byte[] tokenBytes = this.storage.getIssuerSecretKey(issuerParamsUid);
      if (tokenBytes == null) {
        throw new IssuerSecretKeyNotInStorageException("Issuer secret key with UID: \""
            + issuerParamsUid + "\" is not in storage");
      }
      byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
      objectInput = new ObjectInputStream(byteArrayInputStream);
      SecretKey issuerSecretKey = (SecretKey) objectInput.readObject();
      return issuerSecretKey;
    } catch (IssuerSecretKeyNotInStorageException ex) {
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
  public void storeIssuerSecretKey(URI issuerParamsUid, SecretKey issuerSecretKey)
      throws CredentialManagerException {

    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutput = new ObjectOutputStream(byteArrayOutputStream);
      objectOutput.writeObject(issuerSecretKey);
      byte[] bytes = byteArrayOutputStream.toByteArray();
      this.storage.addIssuerSecret(issuerParamsUid, bytes);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }
  }
}

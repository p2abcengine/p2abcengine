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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SecretKey;

public class CredentialManagerImpl implements CredentialManager {

  private final CredentialStorage storage;

  @Inject
  public CredentialManagerImpl(CredentialStorage credentialStore) {
    this.storage = credentialStore;
  }

  private Object loadObject(URI uid) throws CredentialManagerException {
    if (uid == null) {
      throw new CredentialManagerException("UID is null");
    }
    ObjectInputStream objectInput = null;
    ByteArrayInputStream byteArrayInputStream = null;
    Object o = null;
    try {
      byte[] tokenBytes = this.storage.getData(uid);
      if (tokenBytes == null) {
        throw new CredentialManagerException("Object with UID: \"" + uid + "\" is not in storage");
      }
      byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
      objectInput = new ObjectInputStream(byteArrayInputStream);
      o = objectInput.readObject();
    } catch (CredentialManagerException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectInput);
      StorageUtil.closeIgnoringException(byteArrayInputStream);
    }
    return o;
  }

  private void storeObject(Object o, URI uid) throws CredentialManagerException {
    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutput = new ObjectOutputStream(byteArrayOutputStream);
      objectOutput.writeObject(o);
      byte[] bytes = byteArrayOutputStream.toByteArray();
      this.storage.addData(uid, bytes);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }
  }

  @Override
  public SecretKey getSecretKey(URI keyUid) throws CredentialManagerException {
    Object o = this.loadObject(keyUid);
    return (SecretKey) o;
  }

  @Override
  public void storeSecretKey(URI revocationAuthorityKeyUID, SecretKey secretKey)
      throws CredentialManagerException {
    this.storeObject(secretKey, revocationAuthorityKeyUID);
  }

  @Override
  public NonRevocationEvidence getNonRevocationEvidence(URI uid) throws CredentialManagerException {
    Object o = this.loadObject(uid);
    NonRevocationEvidence value = (NonRevocationEvidence) o;
    return value;
  }

  @Override
  public void storeNonRevocationEvidence(NonRevocationEvidence nre)
      throws CredentialManagerException {
    URI uid = nre.getNonRevocationEvidenceUID();
    this.storeObject(nre, uid);
  }

  @Override
  public void storeRevocationHistory(URI historyUID, RevocationHistory revHistory)
      throws CredentialManagerException {
    this.storeObject(revHistory, historyUID);
  }

  @Override
  public void addRevocationLogEntry(URI logEntryUID, RevocationLogEntry revLogEntry)
      throws CredentialManagerException {
    this.storeObject(revLogEntry, logEntryUID);
  }

  @Override
  public void deleteRevocationLogEntry(URI logEntryUID) throws CredentialManagerException {
    try {
      RevocationLogEntry revLogEntry = this.getRevocationLogEntry(logEntryUID);
      if (revLogEntry != null) {
        this.storage.delete(logEntryUID);
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public RevocationLogEntry getRevocationLogEntry(URI logEntryUID)
      throws CredentialManagerException {
    Object o = this.loadObject(logEntryUID);
    RevocationLogEntry value = (RevocationLogEntry) o;
    return value;
  }

  @Override
  public RevocationHistory getRevocationHistory(URI historyUID) throws CredentialManagerException {
    Object o = this.loadObject(historyUID);
    RevocationHistory value = (RevocationHistory) o;
    return value;
  }
}

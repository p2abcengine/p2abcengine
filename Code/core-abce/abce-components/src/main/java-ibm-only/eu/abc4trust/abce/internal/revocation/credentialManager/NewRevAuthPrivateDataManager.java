//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.util.XmlUtils;

public class NewRevAuthPrivateDataManager implements CredentialManager {

  private final PersistentStorage ps;

  @Inject
  public NewRevAuthPrivateDataManager(PersistentStorage ps) {
    this.ps = ps;
  }

  @Override
  public SecretKey getSecretKey(URI uid) throws CredentialManagerException {
    final SecretKey ret = (SecretKey) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.REV_AUTH_SECRET_KEY, uid));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
	return ret;
  }

  @Override
  public void storeSecretKey(URI secretKeyUID, SecretKey revAuthSecretKey)
      throws CredentialManagerException {
    ps.replaceItem(SimpleParamTypes.REV_AUTH_SECRET_KEY, secretKeyUID,
        ByteSerializer.writeAsBytes(revAuthSecretKey));
  }

  @Override
  public void storeRevocationHistory(URI historyUID, RevocationHistory revHistory)
      throws CredentialManagerException {
    ps.replaceItem(SimpleParamTypes.REV_AUTH_SECRET_KEY, historyUID,
      ByteSerializer.writeAsBytes(revHistory));
  }

  @Override
  public void addRevocationLogEntry(URI logEntryUID, RevocationLogEntry revLogEntry)
      throws CredentialManagerException {
    ps.insertItem(SimpleParamTypes.REV_AUTH_LOG_ENTRY, revLogEntry.getRevocationLogEntryUID(),
        ByteSerializer.writeAsBytes(revLogEntry));
  }

  @Override
  public void deleteRevocationLogEntry(URI logEntryUID) throws CredentialManagerException {
    ps.deleteItem(SimpleParamTypes.REV_AUTH_LOG_ENTRY, logEntryUID);
  }

  @Override
  public RevocationLogEntry getRevocationLogEntry(URI logEntryUID)
      throws CredentialManagerException {
    return (RevocationLogEntry) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.REV_AUTH_LOG_ENTRY, logEntryUID));
  }

  @Override
  public RevocationHistory getRevocationHistory(URI historyUID) throws CredentialManagerException {
    return (RevocationHistory) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.REVOCATION_HISTORY, historyUID));
  }

  @Override
  public void storeNonRevocationEvidence(NonRevocationEvidence nre)
      throws CredentialManagerException {
    ps.insertItem(SimpleParamTypes.NON_REVOCATION_EVIDENCE, nre.getNonRevocationEvidenceUID(),
        ByteSerializer.writeAsBytes(nre));
  }

  @Override
  public NonRevocationEvidence getNonRevocationEvidence(URI uid) throws CredentialManagerException {
    return (NonRevocationEvidence) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.NON_REVOCATION_EVIDENCE, uid));
  }

}

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

import java.net.URI;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SecretKey;

public interface CredentialManager {

    public SecretKey getSecretKey(URI uid)
            throws CredentialManagerException;


    public void storeSecretKey(URI secretKeyUID, SecretKey revAuthSecretKey)
            throws CredentialManagerException;

    public void storeRevocationHistory(URI historyUID, RevocationHistory revHistory)
            throws CredentialManagerException;

    public void addRevocationLogEntry(URI logEntryUID, RevocationLogEntry revLogEntry)
            throws CredentialManagerException;

    public void deleteRevocationLogEntry(URI logEntryUID)
            throws CredentialManagerException;

    public RevocationLogEntry getRevocationLogEntry(URI logEntryUID)
            throws CredentialManagerException;

    public RevocationHistory getRevocationHistory(URI historyUID)
            throws CredentialManagerException;

    public void storeNonRevocationEvidence(NonRevocationEvidence nre)
            throws CredentialManagerException;

    public NonRevocationEvidence getNonRevocationEvidence(URI uid)
            throws CredentialManagerException;

}

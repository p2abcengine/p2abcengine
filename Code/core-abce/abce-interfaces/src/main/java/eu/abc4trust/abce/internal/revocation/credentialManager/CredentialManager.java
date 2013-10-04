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

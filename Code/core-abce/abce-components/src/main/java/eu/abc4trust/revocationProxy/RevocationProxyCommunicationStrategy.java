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

package eu.abc4trust.revocationProxy;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationMessage;

public interface RevocationProxyCommunicationStrategy {

    CryptoParams requestRevocationHandle(RevocationMessage m,
            Reference nonRevocationEvidenceReference)
                    throws RevocationProxyException;

    CryptoParams requestRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException;

    CryptoParams revocationEvidenceUpdate(RevocationMessage m,
            Reference nonRevocationEvidenceUpdateReference)
                    throws RevocationProxyException;

    CryptoParams getCurrentRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException;
}

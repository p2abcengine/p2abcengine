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

package eu.abc4trust.abce.internal.issuer.tokenManagerIssuer;

import java.io.IOException;
import java.net.URI;


/**
 * Storage for Issuance tokens.
 * 
 * @author Raphael Dobers
 */

public interface TokenStorageIssuer {

    public byte[] getToken(URI tokenuid) throws Exception;
    public void addToken(URI tokenuid, byte[] token) throws IOException;
    public boolean checkForPseudonym(String primaryKey) throws IOException;
    public void addPseudonymPrimaryKey(String primaryKey) throws IOException;
    public boolean deleteToken(URI tokenuid) throws Exception;
    public void addIssuanceLogEntry(URI entryuid, byte[] entry) throws IOException;
    public byte[] getIssuanceLogEntry(URI entryuid) throws Exception;
    public boolean deleteIssuanceLogEntry(URI entryuid) throws Exception;
}

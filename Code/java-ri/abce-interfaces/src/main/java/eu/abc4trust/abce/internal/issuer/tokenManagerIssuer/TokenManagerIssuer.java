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

import java.net.URI;

import eu.abc4trust.abce.internal.TokenManager;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceToken;

public interface TokenManagerIssuer extends TokenManager {
    /**
     * This method saves the given issuance token it in permanent storage and assigns a unique
     * identifier to the token by means of which it can later be retrieved. The return value is the
     * unique identifier.
     * 
     * @param it
     * @return
     */
    public URI storeToken(IssuanceToken it);

    // /**
    // * This method saves the issuance data that contains issuance token and
    // the attribute values provided by the issuer
    // * it in permanent storage and assigns a unique identifier to the token by
    // * means of which it can later be retrieved. The return value is the
    // unique identifier.
    // *
    // * @param it
    // * @return
    // */
    //
    // public URI storeIssuerAttributes(IssuerAttributes iats, URI
    // issuanceLogEntryURI);


    /**
     * This method looks up a the issuance data by the unique identifier
     * issuanceDataUid.
     * 
     * @param tokenuid
     * @return
     */
    public IssuanceLogEntry getIssuanceLogEntry(URI issuanceDataUid);


    /**
     * This method deletes the issuance data referenced by the unique
     * identifier issuanceDataUid. It returns true in case of successful deletion, and false otherwise.
     * 
     * @param tokenuid
     * @return
     */
    public boolean deleteIssuanceLogEntry(URI issuanceDataUid);

    /**
     * This method saves the issuance data that contains issuance token and
     * the attribute values provided by the issuer.
     * It is stored in permanent storage and assigned a unique identifier to the token by
     * means of which it can later be retrieved. The return value is the
     * unique identifier.
     *  
     * @param issuance data
     * @return
     */
    public URI storeIssuanceLogEntry(IssuanceLogEntry issuanceLogEntry);
    
    

    //old methods we dont need anymore - to be deleted

    @Deprecated
    public IssuanceToken getToken(URI tokenuid);

    @Deprecated
    public boolean deleteToken(URI tokenuid);

}

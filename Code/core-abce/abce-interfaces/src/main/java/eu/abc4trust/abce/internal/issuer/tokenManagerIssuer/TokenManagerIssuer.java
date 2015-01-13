//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

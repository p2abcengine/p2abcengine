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

package eu.abc4trust.revocationProxy;

import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationMessage;

public interface RevocationProxy {
    /**
     * This method carries out one step in a possibly interactive protocol with a Revocation
     * Authority. Depending on the revocation mechanism, interaction with the Revocation Authority may
     * be needed during credential issuance, during creation or verification of a presentation token,
     * or during the separately triggered creation or update of non-revocation evidence. This method
     * will be called by the CryptoEngine of the User, Verifier, or Issuer whenever it needs to
     * interact with the Revocation Authority. The method acts as a proxy for the communication with
     * the Revocation Authority: in the parameters revpars it looks up the appropriate endpoint to
     * contact the Revocation Authority, sends the outgoing revocation message m, and returns the
     * response received from the Revocation Authority to the local CryptoEngine. The returned message
     * will have the same Context attribute as the outgoing message m, so that the different messages
     * in a protocol execution can be linked.
     * 
     * @param m
     * @param revpars
     * @return
     */
    public RevocationMessage processRevocationMessage(RevocationMessage m,
            RevocationAuthorityParameters revpars) throws Exception;
}

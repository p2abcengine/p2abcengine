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

package eu.abc4trust.revocationProxy.revauth;

import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.xml.RevocationMessage;

public interface RevocationProxyAuthority {
  /**
   * This method carries out one step in a possibly interactive protocol with a User or an Issuer
   * during which the User obtains or updates her non-revocation evidence. Depending on the
   * revocation mechanism, such protocols may be part of the issuance of a credential, the creation
   * of a presentation token, or of an independent update of the non-revocation evidence. The method
   * takes in incoming revocation message m and returns an outgoing revocation message that is to be
   * returned as a response to the caller. The outgoing message will have the same Context attribute
   * as the incoming message, so that the different messages in a protocol execution can be linked.
   * The method also returns a boolean to indicate whether this is the last message in the flow. If
   * so, any state information kept for this context can be safely removed.
   * 
   * @throws Exception
   */
  public RevocationMessageAndBoolean processRevocationMessage(RevocationMessage m)
      throws Exception;
}

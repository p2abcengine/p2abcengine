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

package eu.abc4trust.revocationProxy.revauth;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.revocationProxy.RevocationProxyException;
import eu.abc4trust.xml.RevocationMessage;

public interface RevocationProxyAuthority {
    /**
     * This method carries out one step in a possibly interactive protocol with
     * a User or an Issuer during which the User obtains or updates her
     * non-revocation evidence. Depending on the revocation mechanism, such
     * protocols may be part of the issuance of a credential, the creation of a
     * presentation token, or of an independent update of the non-revocation
     * evidence. The method takes in incoming revocation message m and returns
     * an outgoing revocation message that is to be returned as a response to
     * the caller. The outgoing message will have the same Context attribute as
     * the incoming message, so that the different messages in a protocol
     * execution can be linked. The method also returns a boolean to indicate
     * whether this is the last message in the flow. If so, any state
     * information kept for this context can be safely removed.
     * 
     * @param m
     * @return
     * @throws RevocationProxyException
     * @throws CryptoEngineException
     */
    public RevocationMessageAndBoolean processRevocationMessage(
            RevocationMessage m) throws RevocationProxyException;
}

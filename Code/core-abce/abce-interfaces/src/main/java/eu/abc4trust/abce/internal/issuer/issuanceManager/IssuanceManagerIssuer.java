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

package eu.abc4trust.abce.internal.issuer.issuanceManager;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

public interface IssuanceManagerIssuer {
    /**
     * This method is invoked by the Issuer to initiate an issuance protocol
     * based on the given issuance policy ip and the attribute values atts to be
     * embedded in the new credential. It returns an IssuanceMessage that is to
     * be sent to the User and fed to the IssuanceManager.issuanceProtocolStep
     * method on the User’s side. The IssuanceMessage contains a Context
     * attribute that will be the same for all message exchanges in this
     * issuance protocol, to facilitate linking the different flows of the
     * protocol. It also outputs a boolean indicating whether this is the last
     * flow of the issuance protocol.
     * 
     * In case of an issuance “from scratch”, i.e., for which the User does not
     * have to prove ownership of existing credentials or established
     * pseudonyms, the given issuance policy ip merely specifies the credential
     * specification and the issuer parameters for the credential to be issued.
     * In this case, the returned issuance message is the first message in the
     * actual cryptographic issuance protocol. This method will then directly
     * invoke CryptoEngine.initIssuanceProtocol(ip, atts, ctxt) on input the
     * issuance policy (containing only the credential specification UID ), the
     * list of known attributes and a freshly generated Context value ctxt that
     * is used for local "bookkeeping" of the cryptographic state and for tying
     * the issuance protocol messages together.
     * 
     * The returned issuance message will contain the fresh Issuer Context
     * attribute that links the different messages of this issuance protocol
     * together.
     * 
     * In case of an “advanced” issuance, i.e., where the User has to prove
     * ownership of existing credentials or pseudonyms to carry over attributes,
     * a user secret, or a device secret, the returned IssuanceMessage is simply
     * a wrapper around the issuance policy ip with a fresh Context attribute.
     * 
     * @param ip
     * @param atts
     * @return
     * @throws CryptoEngineException
     */
    public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
            List<Attribute> atts) throws CryptoEngineException;

    /**
     * This method performs one step in an interactive issuance protocol. If the
     * incoming issuance message m does not contain an issuance token received
     * from a User, it calls the CryptoEngine on m:
     * CryptoEngine.issuanceProtocolStep(m) The method then returns the output
     * of the CryptoEngine, which can be either an outgoing issuance message, or
     * a description of the newly issued credential at successful completion of
     * the protocol. In the former case, the Context attribute of the outgoing
     * message has the same value as that of the incoming message, allowing to
     * link the different messages of this issuance protocol. If the incoming
     * issuance message m does contain an issuance token it, then this method
     * generates the first message of the actual issuance protocol in an
     * “advanced” issuance, i.e., where the User has to prove ownership of
     * existing credentials or pseudonyms to carry over attributes, a user
     * secret, or a device secret. The issuance policy ip and the list of
     * attribute type-value pairs atts was given as input to the
     * IssuanceManager.initIssuanceProtocol when the Context for this issuance
     * protocol instance was defined. This method first verifies the validity of
     * the Issuance Token by calling PolicyTokenMatcher on input Issuance Policy
     * and Issuance Token.
     * PolicyTokenMatcher.verifyIssuanceTokenAgainstPolicy(ip, it, store) The
     * PolicyTokenMatcher verifies in two steps whether the issuance token
     * description satisfies the policy and whether the cryptographic evidence
     * supports the token description. If both checks succeed, it obtains the
     * verified presentation token description (including a unique token
     * identifier in case store was set to true). If the verification of the
     * Issuance Token was successful, it subsequently invokes the CryptoEngine
     * to perform the first round of the actual issuance protocol. To this end,
     * the method passes the issuance policy, the issuance token (for the
     * carried-over attributes), the attributes chosen by the Issuer and the
     * context attribute from the issuance message m to the CryptoEngine.
     * CryptoEngine.initIssuanceProtocol(ip, it, atts, ctxt) The output of this
     * method is an IssuanceMessage which is the first move in the actual
     * issuance protocol. The returned issuance message will contain the same
     * Context identifier ctxt that was included in the issuance message m to
     * link the different messages of this issuance protocol.
     * 
     * @param m
     * @return
     * @throws CryptoEngineException
     */
    public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException;

    /**
     * This method is responsible for extracting the correct issuanceMessage from
     * the given policy. It more or less only generates a nonce and wraps everything
     * in xml.
     * @param clonedIssuancePolicy
     * @return
     * @throws CryptoEngineException
     */
    public IssuanceMessageAndBoolean initReIssuanceProtocol(
            IssuancePolicy clonedIssuancePolicy) throws CryptoEngineException;

    public IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException;

    /**
     * This method looks up an issuance log entry of previously issued
     * credentials that contains a verified issuance token together with the
     * attribute values provided by the issuer. The issuance log entry
     * identifier issuanceEntryUid is the identifier that was included in the
     * issuance token description that was returned when the token was verified.
     * 
     * @throws Exception
     */
    public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid)
            throws Exception;

    /**
     * This method looks for an IssuanceTokenDescription inside the issuance message. This method
     * returns the issuance token, or NULL if none could be found. It is guaranteed that this method
     * returns a non-null value before a new credential is actually issued, so that the upper layers
     * may abort the issuance protocol if a certain condition is not satisfied (such as the absence of
     * a registered pseudonym).
     */
    public IssuanceTokenDescription extractIssuanceTokenDescription(IssuanceMessage issuanceMessage);

}

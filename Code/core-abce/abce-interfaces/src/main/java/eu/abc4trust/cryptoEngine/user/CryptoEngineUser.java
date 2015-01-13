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

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenIssuanceException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.VerifierParameters;

public interface CryptoEngineUser {
    /**
     * This method generates the cryptographic evidence for the given
     * presentation token description td using the credentials with UIDs
     * mentioned in creds (given in the order that the credentials appear in
     * td). This method also saves newly established pseudonyms including their
     * scope and any cryptographic metadata (to allow later re-authentication
     * under the pseudonym) in permanent storage. The User-generated metadata
     * will be associated to the pseudonym by the
     * PolicyCredentialMatcher.createToken method when the token is returned.
     * The token is created by taking the following steps:
     * 
     * 1. Fetch the full credentials and pseudonyms required to generate the
     * token by repetitively calling the methods
     * CredentialManager.getCredential(creduid)
     * CredentialManager.getPseudonymWithMetaData(p:Pseudonym).
     * 
     * 2. Fetch the Issuer parameters, Inspector public keys and Revocation
     * Authority parameters needed to generate the token from the KeyManger by
     * invoking the following methods: KeyManager.getIssuerParameters(issuid)
     * KeyManager.getInspectorPublicKey(ipkuid)
     * KeyManager.getRevocationParameters(rapuid)
     * 
     * 3. Invoke mechanism-specific cryptographic routines to generate the
     * cryptographic evidence for the token.
     * 
     * 4. Store the cryptographic metadata used to generate newly established
     * pseudonyms in the credential store using the method
     * CredentialManager.storePseudonymWithMetadata(pseudowithmeta).
     * 
     * 5. Assemble the full (sub)presentation token from the token description
     * and the generated evidence and return the (sub)token.
     * 
     * @param td
     * @param creds
     * @param pseudonyms
     * @return
     * @throws CryptoEngineException
     */
    public PresentationToken createPresentationToken(String username, PresentationTokenDescription td,
            VerifierParameters vp, List<URI> creds, List<URI> pseudonyms)
                throws CryptoEngineException;

    /**
     * This method generates the extended cryptographic evidence for the given issuance token
     * description itd using the credentials listed in creduids (given in the order that the
     * credentials appear in itd). This method also keeps state information (such as the randomness of
     * commitments) that might be required in a later step of the issuance protocol. To be able to
     * identify the information again, the method associates the data to the unique Context attribute
     * ctxt.
     * 
     * 1. As in the normal createToken method, it first fetches the full credentials and pseudonyms
     * that are specified in creduids and the issuance token description and also obtains all required
     * key material with the help of the KeyManager. (See the CryptoEngine.createToken method for a
     * detailed description)
     * 
     * 2.The method invokes mechanism-specific cryptographic routines to generate the cryptographic
     * evidence for the issuance token.
     * 
     * 3.It keeps the state information that might be required in a subsequent step of the issuance
     * protocol in a temporary storage associated to the context.
     * 
     * 4.It stores the newly generated pseudonyms including their cryptographic metadata using the
     * CredentialManager (See the CryptoEngine.createToken method for a detailed description)
     * 
     * 5.If the newly issued credential is subject to Issuer-driven revocation restrictions, then,
     * depending on the revocation mechanism, the CryptoEngine may have to interact with the
     * Revocation Authority during issuance. If so, then this method prepares a mechanism-specific
     * RevocationMessage m and calls RevocationProxy.processRevocationMessage(m, rapars).
     * 
     * 6.It returns the (Sub)Issuance Token to the EvidenceGenerationOrchestration.
     * 
     * @param itd
     * @param creduids
     * @param atts
     * @param pseudonyms
     * @param ctxt
     * @return
     * @throws TokenIssuanceException
     */
     public IssuanceMessage createIssuanceToken(String username, IssuanceMessage im, IssuanceTokenDescription itd,
         List<URI> creduids, List<URI> pseudonyms, List<Attribute> atts)
             throws CryptoEngineException;

    /**
     * On input an incoming issuance message m, this method first extracts the
     * context attribute and obtains the cryptographic state information that is
     * stored under the same context value. It then invokes the
     * mechanism-specific cryptographic routines for one step in an interactive
     * issuance protocol. If the newly issued credential is subject to
     * Issuer-driven revocation, then, depending on the revocation mechanism,
     * this method may interact with the Revocation Authority by calling
     * RevocationProxy.processRevocationMessage(m, revpars). The method either
     * returns an outgoing issuance message or a description of the newly issued
     * credential to indicate a successful completion of the protocol. In the
     * former case, the method eventually also stores new cryptographic state
     * information associated to the Context attribute, and attaches the Context
     * attribute to the outgoing message. If the invoked cryptographic routines
     * complete the issuance protocol, the method stores the obtained credential
     * with all the cryptographic metadata in the credential store by calling
     * CredentialManager.storeCredential(cred: Credential) and returns the
     * credential description.
     * 
     * @param m
     * @return
     * @throws CryptoEngineException
     */
    public IssuMsgOrCredDesc issuanceProtocolStep(String username, IssuanceMessage m)
            throws CryptoEngineException;

    /**
     * This method updates the non-revocation evidence stored in credential cred with respect to
     * Revocation Authority parameters raparsuid and with respect to attribute combination
     * revokedatts, or tries to create such non-revocation evidence when it does not exist yet. It
     * returns the credential with updated non-revocation evidence. This method always updates the
     * non-revocation to the most current state. It calls
     * KeyManager.getCurrentRevocationInformation(raparsuid) to obtain the most recent revocation
     * information, and possibly calls the RevocationProxy.processRevocationMessage(m, rapars) method
     * to interact with the Revocation Authority.
     * 
     * If the credential was revoked, this method must throw a CredentialWasRevokedException.
     * 
     * This method is also responsible for updating the credential in the credential manager if
     * needed.
     * 
     * @param cred
     * @param raparsuid
     * @param revokedatts
     * @return
     * @throws CredentialWasRevokedException 
     */
    public Credential updateNonRevocationEvidence(String username, Credential cred, URI raparsuid,
            List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException;

    /**
     * This method updates the non-revocation evidence stored in credential cred with respect to
     * Revocation Authority parameters raparsuid and with respect to attribute combination
     * revokedatts, or tries to create such non-revocation evidence when it does not exist yet. It
     * returns the credential with updated non-revocation evidence. Contrary to the previous method,
     * this method updates the non-revocation information so that it can be verified against the given
     * revocation information revinfouid, which may not be the latest revocation information for the
     * Revocation Authority parameters. It may call the RevocationProxy.processRevocationMessage(m,
     * rapars) method to interact with the Revocation Authority.
     * 
     * If the credential was revoked, this method must throw a CredentialWasRevokedException.
     * 
     * This method is also responsible for updating the credential in the credential manager if
     * needed.
     * 
     * @param cred
     * @param raparsuid
     * @param revokedatts
     * @param revinfouid
     * @return
     * @throws CredentialWasRevokedException 
     */
    public Credential updateNonRevocationEvidence(String username, Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException, CredentialWasRevokedException;

    /**
     * Create a new pseudonym.
     * This method will have to call the smartcard manager to generate the proper value of
     * the pseudonym.
     * The caller is responsible for storing the pseudonym.
     * The pseudonym will not contain any metadata.
     * @throws CryptoEngineException 
     */
    public PseudonymWithMetadata createPseudonym(String username, URI pseudonymUri, String scope, boolean exclusive,
            URI secretReference) throws CryptoEngineException;

    /**
     * This method checks if the given credential has been revoked. Returns true
     * if the credential is revoked and false otherwise.
     * 
     * @param cred
     * @return
     * @throws CryptoEngineException
     */
    public boolean isRevoked(String username, Credential cred) throws CryptoEngineException;
    
    /**
     * Extract the issuance policy in the issuance message, or return null if the message does
     * not contain one. This message will return a non-null value for the first message of an
     * advanced issuance only.
     * If this method returns non-null, it is mandatory to generate an issuance token description
     * and call createIssuanceToken() (instead of issuanceProtocolStep()).
     * @param issuanceMessage
     * @return
     */
    public IssuancePolicy extractIssuancePolicy(IssuanceMessage issuanceMessage);

}

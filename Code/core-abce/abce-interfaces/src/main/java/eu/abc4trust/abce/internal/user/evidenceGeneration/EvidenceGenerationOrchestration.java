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

package eu.abc4trust.abce.internal.user.evidenceGeneration;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenIssuanceException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.VerifierParameters;

public interface EvidenceGenerationOrchestration {
    /**
     * This method generates the cryptographic evidence for the given
     * presentation token description td using the credentials with UIDs
     * mentioned in creds (given in the order that the credentials appear in
     * td). The output of the method is a presentation token containing both the
     * token description and the evidence. If the presentation token can be
     * separated into multiple subtokens (e.g., one subtoken using a Privacy-ABC
     * technology and another one using X.509), then this method splits up the
     * token description td and the credential identifiers creds per subtoken
     * and, for each subtoken, generates the cryptographic evidence by calling
     * CryptoEngine.createToken(subtokendesc, subcreds). It then assembles all
     * returned subtokens into a single presentation token. If there is only a
     * single subtoken, this method simply makes a single call
     * CryptoEngine.createToken(tokendesc, creds) and returns the resulting
     * presentation token.
     * 
     * @param td
     * @param creds
     * @return
     * @throws CryptoEngineException
     */
      public PresentationToken createPresentationToken(String username, PresentationTokenDescription td,
          VerifierParameters vp, List<URI> creds, List<URI> pseudonyms) throws CryptoEngineException;

    /**
     * This method orchestrates the generation of the cryptographic evidence for the given issuance
     * token description itd using the credentials with UIDs mentioned in creduids (given in the order
     * that the credentials appear in itd). The output of the method is an issuance message that
     * encapsulates a token containing both the issuance token description and the evidence. Here the
     * evidence can also contain additional cryptographic data which will subsequently be used in the
     * issuance protocol for the carried-over attributes. If the issuance token can be separated into
     * multiple subtokens (e.g., one subtoken using a Privacy-ABC technology and another one using
     * X.509), then this method splits up the token description itd and the credential identifiers
     * creds per subtoken and, for each subtoken, generates the cryptographic evidence by calling
     * CryptoEngine.createIssuanceToken(subissuancetokendesc, subcreduids, atts, ctxt). This call also
     * includes a Context attribute ctxt to allow the CryptoEngine to bind cryptographic state
     * information of different subtokens to one issuance session. It finally assembles all returned
     * subtokens into a single Issuance token, which in turn is wrapped into an IssuanceMessage with
     * Context ctxt. If there is only a single subtoken, this method simply makes a single call
     * CryptoEngine.createIssuanceToken(issuancetokendesc, creduidsatts, ctxt)
     * 
     * @param itd
     * @param creduids
     * @param atts
     * @param ctxt
     * @return
     * @throws TokenIssuanceException
     */
     public IssuanceMessage createIssuanceToken(String username, IssuanceMessage im, IssuanceTokenDescription itd,
         List<URI> creduids, List<URI> pseudonyms, List<Attribute> atts)
         throws CryptoEngineException;

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
     * Create a new (non-device-bound) secret.
     * This method must generate a random UID for the secret.
     * The caller is responsible for storing the secret.
     */
    public Secret createSecret(String username);
    
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
     */
    public Credential updateNonRevocationEvidence(String username, Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException, CredentialWasRevokedException;

    public IssuancePolicy extractIssuancePolicy(IssuanceMessage im);
}

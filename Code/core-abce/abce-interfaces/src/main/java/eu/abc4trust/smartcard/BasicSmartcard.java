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

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.util.Set;

import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PseudonymWithMetadata;

public interface BasicSmartcard {
  /**
   * Return true if the smartcard was already initialized.
   * @return
   */
  public boolean wasInit();
  
  // -----------------------------------------------
  // Methods requiring the correct PIN to be entered
  // The smartcard will lock itself if the pin was entered incorrectly 3 times,
  // (but the PIN can be reset with the PUK at any time).
  
  /**
   * Get a unique URI identifying the device.
   * @param pin
   * @return
   */
  public URI getDeviceURI(int pin);

  /**
   * Returns the deviceID given to the device at initialization.
   * @param pin
   * @return
   */
  short getDeviceID(int pin);

  /**
   * Return the issuer parameters for a credential.
   * @param pin
   * @param credentialUri
   * @return
   */
  public TrustedIssuerParameters getIssuerParametersOfCredential(int pin, URI credentialId);
  
  /**
   * Returns the cryptographic parameters for this card
   * (bases for the pseudonyms, bit lengths of the secret, parameters of the ZK proof, etc).
   * @param pin
   * @return
   */
  public SystemParameters getSystemParameters(int pin);
  
  /**
   * Returns the value of a scope exclusive pseudonym.
   * This is  hash(scope)^deviceSecret (mod p)
   */
  public BigInteger computeScopeExclusivePseudonym(int pin, URI scope);
  
  /**
   * Returns the stored pseudonym under the given URI. 
   * Returns null if not found. 
   */
  public PseudonymWithMetadata getPseudonym(int pin, URI pseudonymId, PseudonymSerializer serializer);
  
  /**
   * Returns the value g^deviceSecret, needed for making pseudonyms
   * (remember that a pseudonym is of the form g^deviceSecret * h^randomness (mod p) ).
   */
  public BigInteger computeDevicePublicKey(int pin);
  
  /**
   * Run the first step of a sigma protocol.
   * This proof is done if and only if all the courses in courseIds have been sufficiently attended
   * (or if they have a one-time exception for allowing a proof without sufficient attendance ---
   * needed for the issuance protocol).
   * @param pin
   * @param credentialIds The Ids of all the credentials that should be included
   *     in the proof
   * @param scopeExclusivePseudonyms The URI of all scope exclusive pseudonyms that should be
   *     included in the proof.
   * @param includeDevicePublicKeyProof Set to true if you need a proof for the device public key
   *     g^deviceSecret (mod p), required for proving knowledge of pseudonyms.
   * @return
   */
  public ZkProofCommitment prepareZkProof(int pin, Set<URI> credentialIds, 
      Set<URI> scopeExclusivePseudonyms, boolean includeDevicePublicKeyProof);  
  
  /**
   * Finalizes the zero-knowledge proof. You will have to call zkNonceOpen before this function.
   * The challenge used will be hash(challengeHashPreimage || hash( XOR of all nonces ) )
   * @param pin
   * @param challengeHashPreimage
   * @param zkNonces A list of all the nonce openings for all the smartcards involved in the proof
   *   where the nth item of the list corresponds to the nth commitment given to the zkNonceOpen
   *   function.
   * @return
   */
  public ZkProofResponse finalizeZkProof(int pin, BigInteger challenge,
                                         Set<URI> credentialIDs, Set<URI> scopeExclusivePseudonyms);
  
  /**
   * Computes the value  R0^deviceSecrez * S^v (mod n) (which is the part of the CL-credential
   * which the card is responsible for).
   * @param pin
   * @param credentialId
   * @return
   */
  public BigInteger computeCredentialFragment(int pin, URI credentialId);
  
  /**
   * Returns true if the given credential is stored by the smartcard.
   * @param pin
   * @param credentialUri
   * @return
   */
  public boolean credentialExists(int pin, URI credentialUri);
  
  /**
   * Allocate a new credential on the card.
   * This method does nothing if credentialUri is already defined.
   * Note that the credentialId MAY be the same as the credentialUri (in credential description).
   * @param pin
   * @param credentialId
   * @param credentialSpecUri
   * @param issuerParameters
   * @return
   */
  public SmartcardStatusCode allocateCredential(int pin, URI credentialId, URI issuerParameters);
  
  /**
   * Returns the credential stored under the given URI. 
   * If not found, it returns null.
   */
  public Credential getCredential(int pin, URI credentialId, CredentialSerializer serializer);
  
  /**
   * Delete the credential with the requested identifier.
   * @return
   */
  public SmartcardStatusCode deleteCredential(int pin, URI credentialId);
  
}

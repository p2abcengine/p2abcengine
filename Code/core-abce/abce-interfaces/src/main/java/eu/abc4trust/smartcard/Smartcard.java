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

import java.net.URI;
import java.util.Map;
import java.util.Set;

import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PseudonymWithMetadata;


/**
 * Interface for a smartcard.
 * 
 * This is the interface that the smartcard reader should implement, but apart from
 * the smartcardPresent() function, it is expected that the reader only does
 * marshalling/unmarshalling of datastructures, and lets the card do all the computation.
 * 
 * Life of a smartcard
 * -------------------
 * The smartcard is shipped to the university, who will call the init() method to fix the
 * school verification key (PKI root), the system parameters, and the pin of the device.
 * 
 * Afterwards the university can register several different issuers. It can do so
 * by first calling getNewNonce() and then either the
 * addIssuerParametersWithAttendanceCheck() or addIssuerParameters() functions.
 * You SHOULD use different parameters (different moduli n) for different issuers.
 * Note that these functions require their arguments to be signed by the school (PKI root),
 * and that each signature requires a fresh nonce.
 * Note that with the addIssuerParametersWithAttendanceCheck() function the school also defines
 * a course verification key (PKI leaf) which is needed to check signatures for incrementing
 * the counter.
 * (This step can also be performed later if desired).
 * 
 * The card can now be sent to the student.
 * 
 * When the student wants to get a credential, the crypto engine will do the following steps:
 * [1] allocateCredential() to tell the card that it should reserve space for a new credential
 * (the card will determine if it should also attach attendance data based on the issuer
 * parameters that were used). Note that the credentialId MAY be the
 * same as the credentialUid in the credential description.
 * [2] computeCredentialFragment() to determine the value (RO^deviceSecret * S^v (mod n)) that
 * the crypto engine will need to get the CL-credential issued by the issuer.
 * (Remember that a CL-credential request has the form RO^m0 * R1^m1 * R2^m2 ... * S^v .)
 * [3] prepareZkProof() to do the first step (ZK-commit) of a Sigma protocol to prove knowledge
 * of deviceSecret and v. (At this point, the credential is in a "not issued" state, and thus
 * the card will allow ONE zero-knowledge proof to be done without doing an attendance check;
 * if he screws up, the student needs to delete the credential and re-start from [1])
 * The card also commits to a 256-bit nonce.
 * [4] The crypto engine now transforms that ZK-commitment to a ZK-commitment for the CL-credential
 * request RO^deviceSecret * R1^m1 * R2^m2 ... * S^v * S^v' and computes the preimage of the
 * hash for the Fiat-Shamir challenge (i.e., the pre-image is just all the ZK-commitments and
 * system parameters and the verifier nonce concatenated together).
 * [5] The crypto engine calls zkNonceOpen() with a list of the nonce commitment from all the
 * cards involved in the proof (including the commitment of the current card). The smartcard
 * responds with the nonce and the opening to the nonce commitment.
 * [6] finalizeZkProof(preimageOfHash, List of nonces/nonceOpenings) to do the last step
 * (ZK-response) of a the Sigma protocol.
 * The crypto engine now transforms that ZK-response to the response for the full CL-credential
 * request. The issuer then issues the CL-credential to the crypto engine.
 * [7] The crypto engine stores the newly generated credential on the card with storeBlob()
 * (the card makes no attempt to actually interpret the contents).
 * 
 * After step [4] the credential is an "issued" state. This means that if that credential has a
 * counter attached, the card will refuse to do any further prepareZkProof() for that credential
 * while the counter value is lower than the minimum specified by the issuer
 * and that the card now honors requests for incrementCourseCounter().
 * 
 * When the students attends a lecture, he will waive his smartcard at a teaching assistant's
 * computer. The following then happens:
 * [1] The TA's computer requests a nonce from the card getNewNonce().
 * [2] The TA's computer calls the incrementCourseCounter() function.
 * The card accepts that function call only if the arguments to the function are signed by the
 * course key. One of the arguments of the incrementCourseCounter() function is a lectureId:
 * this argument SHOULD increase monotonically from lecture to lecture (and remain constant
 * during the lecture), you may use the date for example.
 * 
 * When the student wants to use one of the credentials on his card, the ABCE layer and crypto
 * engine do the following:
 * [1] Call getBlobs() to get a list of all credentials/pseudonyms etc. stored on the card.
 * [2] Call prepareZkProof() just as for issuance (with the difference that the card will
 * refuse to do this call if the counter of any credential is too low).
 * [3] Call zkNonceOpen() and finalizeZkProof(preimageOfHash), just as for issuance.
 * 
 * Life of a pseudonym
 * -------------------
 * Once the card has been initialized, the card is ready to generate pseudonyms.
 * 
 * To issue a pseudonym the following steps are performed by the crypto engine:
 * [1] Call computeScopeExclusivePseudonym() for scope exclusive pseudonyms to get the
 * value (T = hash(scope)^deviceSecret (mod p)) or
 * [1'] Call computeDevicePublicKey() to get the value (T = g^deviceSecret (mod p))
 * Note that a normal pseudonym has the form  (T' = g^deviceSecret * h^r)
 * [2] Call prepareZkProof() to do the first step (ZK-commit) of a Sigma protocol to prove knowledge
 * of deviceSecret.
 * The crypto engine now transforms that ZK-commitment to a ZK-commitment for the normal
 * pseudonyms: T' = g^deviceSecret * h^r  and computes the pre-image of the
 * hash for the Fiat-Shamir challenge.
 * [3] Call zkNonceOpen() just as for the issuance protocol.
 * [4] Call finalizeZkProof(preimageOfHash) to do the last step (ZK-response) of a the Sigma
 * protocol.
 * The crypto engine now transforms that ZK-response to the response for the full normal
 * pseudonym (does nothing for scope-exclusive pseudonyms).
 * [5] The crypto engine stores the newly generated pseudonym on the card with storeBlob()
 * (the card makes no attempt to actually interpret the contents).
 * 
 * To prove possession of a pseudonym, the same steps are done as for the credentials.
 * 
 * 
 * Life of a backup
 * ----------------
 * At any time, a student can call backupAttendanceData() to get a MAC'd backup of
 * the counters, the credential fragments, the device public key  g^deviceSecret (mod p),
 * and the contents of the blobStore.
 * (The MAC key was set during the init() function call, and SHOULD be unique to each student)
 * If the student loses his card, he is supposed to go to the university with his latest
 * backup. The university will then inspect the backup to determine which credentials should
 * be re-issued, and which pseudonyms the student had, etc.
 * The university then issues a new card, the student get himself new credentials issued.
 * After all the credentials have been re-issued, the counters can be set to the same values as
 * they had at the time the backup was made: the university first calls getNonce() to get a
 * fresh nonce, and then restoreAttendanceData() to restore the counters (this method also
 * requires the PIN of the student).
 * 
 * Note that once a smartcard is lost, all the credentials and pseudonyms depending on the
 * device secret become worthless.
 * The backup exposes all the pseudonyms and credentials the student had, so there is a loss
 * of privacy.
 * The security of the backup critically depends on the fact that the student does not get
 * the MAC key (only a trusted university representative gets it), and that the card is
 * tamper-proof.
 * 
 * 
 * Lives of a PIN and PUK
 * ----------------------
 * The card is protected by a PIN. The card does almost nothing if the PIN was not entered.
 * The PIN and PUK are set during the init() function call.
 * The card becomes locked if a student enters a PIN incorrectly 3 times.
 * The student can reset his PIN and unlock the card with the PUK.
 * The card will become bricked (with no way of recovering it) if the PUK was entered incorrectly
 * 10 times.
 * Students can change their PIN (but not their PUK).
 * 
 */
public interface Smartcard extends BasicSmartcard {
	
  public static final URI device_name = URI.create("deviceName");

/**
   * Return true if there is a smartcard present in the reader.
   * @return
   */
  public boolean smartcardPresent();
  
  /**
   * Trusted initialization of the smartcard for Idemix.
   * If the smartcard was not yet initialized, this methods sets the pin,
   * the puk (personal unlock code), the parameters for the pseudonyms,
   * the school verification key, and a message authentication key for backups,
   * and generates a new device secret.
   * This method fails if it is called again.
   * @param newPin
   * @param pseuParams
   * @param rootKey
   * @param deviceID an ID for the card for personalization
   * @param deviceURI A unique identifier to assign to the device
   * @return the PUK of the device
   */
  public int init(int newPin, SystemParameters pseuParams,
                      RSAKeyPair rootKey, short deviceID);
  
  /**
   * Returns the number of trials left for entering PIN.
   */
  public int pinTrialsLeft();
  
  /**
   * Returns the number of trials left for entering PUK.
   */
  public int pukTrialsLeft();
  
  /**
   * Reset the PIN with the personal unlock code.
   * The card will be bricked if the PUK was entered incorrectly 10 times.
   * @return 
   */
  public SmartcardStatusCode resetPinWithPuk(int puk, int newPin);
  
  // -----------------------------------------------
  // Methods requiring the correct PIN to be entered
  // The smartcard will lock itself if the pin was entered incorrectly 3 times,
  // (but the PIN can be reset with the PUK at any time).
  
  /**
   * Store the provided blob (any binary data, such as credentials or pseudonyms bound to
   * this smartcard's secrets) in the PIN-protected blobstore of the card.
   * If a blob with the same URI already exists, it will be overwritten.
   * 
   * It is the caller's responsibility (i.e., the ABCE engines') to determine the data type
   * of a blob when it is retrived.
   * @param pin
   * @param uri
   * @param blob
   * @return
   */
  public SmartcardStatusCode storeBlob(int pin, URI uri, SmartcardBlob blob);
  
  /**
   * Delete the blob with the requested URI from the blobstore.
   * @param pin
   * @param uri
   * @return
   */
  public SmartcardStatusCode deleteBlob(int pin, URI uri);
  
  /**
   * Return the entire blobstore.
   * @param pin
   * @return
   */
  public Map<URI, SmartcardBlob> getBlobs(int pin);
  
  /**
   * Return the set of the URIs of all the blobs in the blobstore.
   * @param pin
   * @return
   */
  public Set<URI> getBlobUris(int pin);
  
  /**
   * Return the blob with the given URI from the blobstore.
   * @param pin
   * @return
   */
  public SmartcardBlob getBlob(int pin, URI uri);
  
  /**
   * Change the pin of the smartcard.
   * @param pin
   * @param newPin
   * @return
   */
  public SmartcardStatusCode changePin(int pin, int newPin);
  
  /**
   * Return the list of all credentials currently stored.
   * @param pin
   * @return
   */
  public Set<URI> listCredentialsUris(int pin);
  
  /**
   * Stores a credential in the blob-store of the card after compressing it using 
   * whatever algorithm is used by the given CredentialSerializer. 
   * @param pin
   * @param credUid
   * @param cred
   * @param serializer
   * @return
   */
  public SmartcardStatusCode storeCredential(int pin, URI credUid, Credential cred, CredentialSerializer serializer);
  
  /**
   * Stores the given pseudonym under the pseudoUid. Uses compression before storing it. 
   * @param pin
   * @param pseudoUid
   * @param pseudonym
   * @return
   */
  public SmartcardStatusCode storePseudonym(int pin, URI pseudoUid, PseudonymWithMetadata pseudonym, PseudonymSerializer serializer);
    
  /**
   * Removes the pseudonym stored in the blob-store from the card. 
   * @param pin
   * @param pseudonymUri
   * @return
   */
  public SmartcardStatusCode deletePseudonym(int pin, URI pseudonymUri);
  
  /**
   * Return the list of all courses currently stored.
   * @param pin
   * @return
   */
  public Set<Course> listCourses(int pin);
  
  /**
   * Return the course associated with a particular issuer.
   * @param pin
   * @param issuerUri
   * @return
   */
  public Course getCourse(int pin, URI issuerUri);
  
  /**
   * Return the authentication key with the identifier keyID. 
   * @param pin
   * @param keyID
   * @return
   */
  public RSAVerificationKey readAuthenticationKey(int pin, int keyID);
  
  /**
   * Return the issuer parameters with a given URI.
   * @param pin
   * @param credentialUri
   * @return
   */
  public TrustedIssuerParameters getIssuerParameters(int pin, URI paramsUri);
  
  /**
   * Return the list of trusted issuer parameters on this card
   * @param pin
   * @param credentialUri
   * @return
   */
  public Set<TrustedIssuerParameters> getIssuerParametersList(int pin);
  
  /**
   * Returns a backup of the course attendance data MAC'd with the device MAC key.
   * @param pin
   * @return
   */
  public SmartcardBackup backupAttendanceData(int pin, String password);
  
 //-------------------------------------------------------------------
 // Methods requiring a signature from the school or from the course TA
  
  /**
   * Delete a trusted issuer from the card.
   * This will fail if any of the credentials on the card uses that issuer.
   */
  public SmartcardStatusCode deleteIssuer(int pin, URI issuerParameters, RSAKeyPair rootKey);  
  
  /**
   * Generate a new nonce and store it.
   * The next signature the card receives is expected to contain that nonce.
   * @return
   */
  public byte[] getNewNonceForSignature();
  
  /**
   * Put trusted issuer parameters on the smartcard.
   * All credentials using that issuer parameters will have an attendance counter associated
   * with it.
   * This method will fail if the card already knows an issuer with URI parametersUri.
   * @param sig
   * @param parametersUri The URI to give to the issuer parameters
   * @param credBases The issuer parameters proper
   * @param courseKey Signing key needed for incrementing the course counter
   * @param minimumAttendance Minimal number of attendance before the card does a proof of
   *     knowledge of a credential fragment that uses this issuer parameters.
   * @return
   */
  public SmartcardStatusCode addIssuerParametersWithAttendanceCheck(RSAKeyPair rootKey,
       URI parametersUri, int keyIDForCounter, SmartcardParameters credBases, RSAVerificationKey courseKey,
       int minimumAttendance);
  
  /**
   * Put trusted issuer parameters WITHOUT ATTENCE CHECK on the smartcard.
   * @param sig
   * @param parametersUri
   * @param credBases
   * @return
   */
  public SmartcardStatusCode addIssuerParameters(RSAKeyPair rootKey, URI parametersUri,
                                                 SmartcardParameters credBases);
  
  /**
   * Increment the course attendance counter of the requested credential.
   * Courses are identified by the issuer UID, as there can only be one course per issuer.
   * The lectureId must increase monotonically from one call to the next, and must be
   * strictly positive (otherwise the attendance counter is not incremented).
   * This method will fail if the card never had to prove knowledge of the credential fragement
   * (i.e., the associated credential has been generated by the ABC engine).
   * @param sig A signature from the course TA authenticating the other parameters of this method.
   * @param issuerId
   * @param lectureId
   * @return
   */
  public SmartcardStatusCode incrementCourseCounter(int pin, RSAKeyPair rootKey, URI issuerId,
                                                    int lectureId);
  
  /**
   * Restore the attendance data (provided that sig contains a valid signature from the school)
   * (This method should be used when a student loses his card, but still has his backup of the
   * attendance data; in this case the new card will called with this method to initialize the 
   * new card and restore the attendance data.
   * Note that the credentials will have to be re-issued and all pseudonyms (including scope
   * exclusive ones) will have to regenerated, as the card secret will be lost.)
   * 
   * Each card has a hardcoded secret encoded that it uses to decrypt the archive given.
   * @param password 
   * @param backup
   * @param pseuParams
   * @param rootKey
   * @return
   */
  public SmartcardStatusCode restoreAttendanceData(int pin, String password, SmartcardBackup backup);

  /**
   * Reads the cards counter with counterID and returns the value of the counter. 
   * @param pin
   * @param counterID
   * @return
   */
  public int getCounterValue(int pin, URI issuerId);

  /**
   * Removes the blobs holding the credential with the given URI
   * @param pin
   * @param uri
   */
  public void removeCredentialUri(int pin, URI uri);

}

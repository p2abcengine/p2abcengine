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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;

public interface CredentialManager {
    /**
     * This method returns the descriptions of credentials in the credential
     * store with Issuer parameterUID occurring in the input parameter issuers
     * and with credential specification UID occurring in credspecs. The
     * credential description contains the attributes of the credential as well
     * as possibly a "friendly name" or picture for the credential.
     * 
     * @param issuers
     * @param credspecs
     * @return
     * @throws CredentialManagerException
     */
    public List<CredentialDescription> getCredentialDescription(String username, 
            List<URI> issuers, List<URI> credspecs)
                    throws CredentialManagerException;

    /**
     * This method returns the description of the credential with the given
     * unique identifier creduid.
     * 
     * @param creduid
     * @return
     * @throws CredentialManagerException
     */
    public CredentialDescription getCredentialDescription(String username, URI creduid)
            throws CredentialManagerException;

    /**
     * This method attaches the non-cryptographic metadata md to the stored
     * pseudonym p. The metadata given here replace the old metatdata.
     * 
     * @param p
     * @param md
     * @throws CredentialManagerException
     */
    public void attachMetadataToPseudonym(String username, Pseudonym p, PseudonymMetadata md)
            throws CredentialManagerException;

    /**
     * This method returns the full credential (including description,
     * cryptographic metadata, and stored non-revocation evidence) with the
     * given unique identifier.
     * 
     * @param creduid
     * @return
     * @throws CredentialManagerException
     */
    public Credential getCredential(String username, URI creduid)
            throws CredentialManagerException;
    
    /**
     * This method stores the given pseudonym and the attached cryptographic
     * metadata in the credential store.
     * 
     * @param pwm
     * @throws CredentialManagerException
     */
    public void storePseudonym(String username, PseudonymWithMetadata pwm)
            throws CredentialManagerException;

    /**
     * This method returns true if the credential with identifier creduid has
     * been revoked by the Revocation Authority with parameters identifier
     * revparsuid with respect to the combination of attributes revokedatts, and
     * returns false otherwise. As a side effect, this method fetches the latest
     * revocation information from the Revocation Authority by calling
     * KeyManager.getRevocationInformation(revparsuid) and, if necessary,
     * updates the non-revocation evidence of the specified credential with
     * respect to the given Revocation Authority and combination of attributes
     * by calling CryptoEngine.updateNonRevocationEvidence(cred, revparsuid,
     * revokedatts) and storing the returned credential with the updated
     * non-revocation evidence. If the credential has been revoked, the
     * CryptoEngine.updateNonRevocationEvidence method returns an error saying
     * so, and this method returns false.
     * 
     * @param creduid
     * @param revparsuid
     * @param revokedatts
     * @return
     * @throws CredentialManagerException
     */
    public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid,
            List<URI> revokedatts) throws CredentialManagerException;

    /**
     * This method returns true if the credential with identifier creduid has
     * been revoked by the Revocation Authority with parameters identifier
     * revparsuid with respect to the combination of attributes revokedatts and
     * with respect to the current revocation information revinfouid. The main
     * difference with the previous method is that it does not fetch the latest
     * revocation information from the Revocation Authority, but uses the input
     * parameter revinfouid instead. It does so by calling
     * CryptoEngine.updateNonRevocationEvidence(cred, revparsuid, revokedatts,
     * revinfouid) and storing the returned credential with the updated
     * non-revocation evidence. If the credential has been revoked, the
     * CryptoEngine.updateNonRevocationEvidence method returns an error saying
     * so, and this method returns false.
     * 
     * @param creduid
     * @param revparsuid
     * @param revokedatts
     * @param revinfouid
     * @return
     * @throws CredentialManagerException
     */
    public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid,
            List<URI> revokedatts, URI revinfouid)
                    throws CredentialManagerException;

    /**
     * This method updates the non-revocation evidence associated to all
     * credentials in the credential store by calling
     * CryptoEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts)for
     * all credentials cred in the store and all non-revocation evidences
     * associated to them. The returned credential with updated non-revocation
     * evidence is stored in the credential store.
     * 
     * 
     * @throws CredentialManagerException
     */
    public void updateNonRevocationEvidence(String username)
 throws CredentialManagerException;

    /**
     * This method saves the given credential, consisting of a credential
     * description and the cryptographic data in permanent storage.
     * If the
     * credential does not have currently have a unique identifier (in
     * credential description),
     * this method enhances the credential description
     * by a unique identifier which is assigned to the credential;
     * if the credential is to be stored on a smartcard, this method MAY change the
     * unique identifier;
     * otherwise
     * this method keeps the unique identifier untouched. The method also
     * downloads the image defined in the credential description. The return
     * value is the unique identifier.
     * 
     * @param cred
     * @return
     * @throws CredentialManagerException
     */
    public URI storeCredential(String username, Credential cred)
            throws CredentialManagerException;
    
    /**
     * This method replaces the stored credential with the same URI as the given credential
     * with the given credential. In particular, it does not delete the internal Smartcard 
     * credential as the combination deleteCredential, storeCredential would do. 
     * It casts an exception if an error occurs during the update.
     * @param cred
     * @return
     * @throws CredentialManagerException
     */
    public void updateCredential(String username, Credential cred) throws CredentialManagerException;
    
    /**
     * This method returns an array of the unique identifiers of all stored
     * credentials.
     * 
     * @return
     * @throws CredentialManagerException
     */
    public List<URI> listCredentials(String username) throws CredentialManagerException;

    /**
     * This method deletes the credential with the given identifier from the
     * credential store. If deleting is not possible (e.g. if the refered
     * credential does not exist) the method returns false, and true otherwise.
     * 
     * @param creduid
     * @return
     * @throws CredentialManagerException
     */
    public boolean deleteCredential(String username, URI creduid)
            throws CredentialManagerException;

    /**
     * This method returns a list of pseudonyms with their metadata with the given scope from the
     * pseudonym store. If the exclusive flag is set, then only return scope-exclusive
     * pseudonyms (if the flag is not set, return both scope-exclusive and non-scope-exclusive
     * pseudonyms).
     * @param scope
     * @param exclusive
     * @return
     * @throws CredentialManagerException
     */
    public List<PseudonymWithMetadata> listPseudonyms(String username, String scope, boolean onlyExclusive)
            throws CredentialManagerException;

    /**
     * This method returns the pseudonym and its metadata with the given unique
     * identifier from the pseudonym store.
     * 
     * @param pseudonymUid
     * @return
     * @throws CredentialManagerException
     */
    public PseudonymWithMetadata getPseudonym(String username, URI pseudonymUid)
            throws CredentialManagerException;

    /**
     * This method deletes the pseudonym with the given identifier from the pseudonym store.
     * If deleting is not possible, the method returns false.
     * @param pseudonymUid
     * @throws CredentialManagerException
     */
    public boolean deletePseudonym(String username, URI pseudonymUid) throws CredentialManagerException;


    /**
     * This method saves the given secret, consisting of a secret description
     * and the cryptographic data in permanent storage. The method may throw an
     * exception if the secret already exists.
     * 
     * @param cred
     * @return
     * @throws CredentialManagerException
     */
    public void storeSecret(String username, Secret cred)
            throws CredentialManagerException;

    /**
     * This method returns an array of the secret descriptions of all the stored
     * secrets.
     * 
     * @return
     * @throws CredentialManagerException
     */
    public List<SecretDescription> listSecrets(String username) throws CredentialManagerException;

    /**
     * This method deletes the secret with the given identifier from the
     * credential store. If deleting is not possible (e.g. if the refered secret
     * does not exist) the method returns false, and true otherwise.
     * 
     * @param creduid
     * @return
     * @throws CredentialManagerException
     */
    public boolean deleteSecret(String username, URI secuid)
            throws CredentialManagerException;

    /**
     * This method returns the full secret (including description, and
     * cryptographic metadata) with the given unique identifier.
     * 
     * @param creduid
     * @return
     * @throws CredentialManagerException
     */
    public Secret getSecret(String username, URI secuid)
 throws CredentialManagerException;

    /**
     * This method attaches the non-cryptographic secret description desc to the
     * stored secret with UID desc.getSecretUID(), overwriting any existing
     * description.
     * 
     * @param p
     * @param md
     * @throws CredentialManagerException
     */
    public void updateSecretDescription(String username, SecretDescription desc)
            throws CredentialManagerException;
}

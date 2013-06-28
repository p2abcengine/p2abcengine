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

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.util.DummyForNewABCEInterfaces;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public interface PolicyCredentialMatcher {
    /**
     * This method returns a presentation token that satisfies the given
     * presentation policy (or more precisely, at least one of them in case p
     * contains several presentations policies). It performs the following steps
     * in doing so.
     * 
     * 0. If there are currently no non-device-bound secrets, then generate one.
     * 
     * 1. Obtain the descriptions of all “relevant” credentials in the
     * credential store. With “relevant”, we mean a first selection of
     * credentials based only on the combinations of allowed Issuer parameters
     * and credential specifications. This list is obtained by repetitively
     * calling CredentialManager.getCredentialDescriptions(issuers, credspecs)
     * for each credential required by the presentation policy. The parameters
     * issuers and credspecs are the lists of acceptable Issuer parameter UIDs
     * and credential specification UIDs for this credential as specified in the
     * presentation policy. Each returned credential description contains the
     * credential UID, i.e., a unique identifier for the credential in the
     * credential store.
     * 
     * 2. Obtain the description of all “relevant” established pseudonyms in the
     * credential store by calling
     * CredentialManager.getPseudonymsWithMetaData(scope), where scope is an
     * attribute that is specified in the presentation policy and indicates a
     * string to which previously established pseudonyms are associated. If the
     * presentation policy asks to present different pseudonyms with different
     * scopes, this method may be called iteratively for all scope strings.
     * 
     * 3. Generate the description (i.e., the token without the cryptographic
     * evidence) of all possible presentation tokens that can be created to
     * satisfy the presentation policy using different combinations of
     * credentials and pseudonyms. The list of presentation tokens also includes
     * tokens that establish new pseudonyms (rather than re-using existing
     * pseudonyms) for those pseudonyms where the policy allows it.
     * 
     * 4. Invoke the Identity Selection object idSelectionCallback to choose
     * among the different possible presentation tokens by calling
     * IdentitySelection.selectPresentationTokenDescription which returns the
     * selected presentation token description, the list of credential UIDs to
     * generate it, and a list of metadata for the pseudonyms.
     * 
     * 5. For all credentials used in the selected token that have Issuer-driven
     * or Verifier-driven revocation restrictions and for which the presentation
     * token description (and hence the presentation policy) explicitly mention
     * the revocation information UID with respect to which the non-revocation
     * status must be shown, call CredentialManager.hasBeenRevoked(creduid,
     * revparsuid, revokedatts, revinfouid); for all other revocation
     * restrictions mentioned in the presentation token description, call
     * CredentialManager.hasBeenRevoked(creduid, revparsuid, revokedatts). Both
     * of these methods check whether the credential has been revoked and update
     * the non-revocation evidence if necessary, but the first offers the
     * privacy advantage of not “pinging” the Revocation Authority unnecessarily
     * for the latest revocation information. If any of the credentials turns
     * out to be revoked, go back to step 4 with the updated set of possible
     * presentation tokens and let the User/IdentitySelection select a different
     * presentation token.
     * 
     * 6. Let the evidence generation orchestration generate the cryptographic
     * evidence for the chosen token by calling
     * EvidenceGenerationOrchestration.createToken(tokendesc, creduids) which
     * returns the full presentation token, including cryptographic evidence.
     * 
     * 7. Attach user-defined metadata to all pseudonyms used in the
     * presentation token by calling
     * CredentialManager.attachMetadataToPseudonym(pseudonym, metadata) for
     * every pseudonym in the token.
     * 
     * 8. Return the presentation token.
     * 
     * Alternatively, one could switch the order of steps 4 and 5 so that the
     * createToken method first checks whether any credential involved in any of
     * the possible presentation tokens has been revoked. This approach has the
     * advantage of never having to ask the User to make a new selection because
     * a credential was revoked, but has the drawback in efficiency and privacy
     * that the Revocation Authorities of all possible presentation tokens get
     * “pinged” during presentation, rather than just the Revocation Authorities
     * of the selected presentation token.
     * 
     * @param p
     * @param idSelectionCallback
     * @return
     * @throws CredentialManagerException
     * @throws CryptoEngineException
     * @throws KeyManagerException 
     * @throws IdentitySelectionException 
     */
    @Deprecated
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
            IdentitySelection idSelectionCallback)
            throws CredentialManagerException, CryptoEngineException, KeyManagerException, IdentitySelectionException;
    
    @Deprecated
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
                                                     IdentitySelectionUi idSelectionCallback)
                                                         throws CredentialManagerException,
                                                         KeyManagerException, CryptoEngineException, IdentitySelectionException;

    /**
     * This method performs steps 0-3 of PolicyCredentialMatcher.createToken(p) and returns true if
     * and only if the list of possible presentation tokens is non-empty. Note that this does not
     * include any revocation checks, i.e., the response of this method is more an indication whether
     * a presentation policy can, in general, be fullfilled or not.
     * 
     * @param p
     * @return
     * @throws CredentialManagerException
     * @throws KeyManagerException 
     */
    public boolean canBeSatisfied(PresentationPolicyAlternatives p) throws CredentialManagerException, KeyManagerException;

    /**
     * This method returns an issuance message with an issuance token that satisfies the given
     * issuance policy and that contains the extended cryptographic evidence, as well as the
     * cryptographic data to support the "carried-over" attributes. It first performs the same
     * credential/pseudonym selection steps as in the presentation scenario to investigate whether the
     * User has the necessary credentials and/or established pseudonyms to satisfy the issuance
     * policy, and in particular the presentation policy part within it. If there are multiple ways in
     * which the policy can be satisfied (e.g., by using different sets of credentials), this method
     * will invoke the identity selection object (which should invoke a user interface to let the user
     * choose her preferred way of generating the presentation token.) The steps 1-3, 6-7 are similar
     * or identical to the steps in the Policy CredentialMatcher.createPresentationToken() method, see
     * the description above for a detailed description.
     * 
     * 0. If there are currently no non-device-bound secrets, then generate one.
     * 
     * 1. CredentialManager.getCredentialDescriptions(issuers, credspecs)
     * 
     * 2. CredentialManager.getPseudonymsWithMetaData(scope).
     * 
     * 3. Generates all possible issuance token descriptions using the given credentials, pseudonyms,
     * and self-claimed attributes
     * 
     * 4. The Identity Selection object idSelectionCallback is invoked to select the preferred token
     * description, credentials, pseudonyms and attributes for the issuance token. To this end, the
     * method passes a list of credential descriptions, a list of pseudonyms, a list of the possible
     * issuance token descriptions containing the credential template that was specified in the
     * issuance token, corresponding credential-identifier lists. As in the presentation case, the
     * Identity Selection responds with the chosen description and credentials.
     * IdentititySelectionUI.selectIssuanceTokenDescription.
     * 
     * 5. For all credentials used in the selected token description that have Issuer-driven or
     * Verifier-driven revocation restrictions, the methods CredentialManager.hasBeenRevoked(creduid,
     * revparsuid, revokedatts, revinfouid) and CredentialManager.hasBeenRevoked(creduid, revparsuid,
     * revokedatts) are called to check whether the credential has been revoked, and to update the
     * non-revocation evidence if necessary. If any of the credentials turns out to be revoked, go
     * notify the user and go back to step 4 to let the User select a different presentation token.
     * 
     * 6. When the preferred issuance token description was selected, it invokes the
     * EvidenceGenerationOrchestration to generate the issuance token. The call also includes the new
     * attributes newatts and the context attribute extracted from the IssuanceMessage to allow
     * book-keeping of local status information.
     * EvidenceGenerationOrchestration.createIssuanceToken(issuancetokendesc, creduids, newatts, ctxt)
     * 
     * 7. CredentialManager.attachMetadataToPseudonym(pseudonym, metadata) (for every pseudonym in the
     * token.)
     * 
     * 8. Return the IssuanceMessage that contains the Issuance Token.
     * 
     * @param ip
     * @param idSelectionCallback
     * @param ctxt
     * @return
     * @throws CredentialManagerException
     * @throws KeyManagerException 
     * @throws IdentitySelectionException 
     */
    @Deprecated
    public IssuanceMessage createIssuanceToken(IssuanceMessage im,
        IdentitySelectionUi idSelectionCallback) throws CredentialManagerException, KeyManagerException, IdentitySelectionException;

    public IssuanceMessage createIssuanceToken(UiIssuanceReturn uir);

    public UiIssuanceArguments createIssuanceToken(IssuanceMessage im, DummyForNewABCEInterfaces d)
        throws CredentialManagerException, KeyManagerException;

    public PresentationToken createPresentationToken(UiPresentationReturn upr) throws CryptoEngineException;

    public UiPresentationArguments createPresentationToken(
        PresentationPolicyAlternatives p, DummyForNewABCEInterfaces d)
            throws CredentialManagerException, KeyManagerException;
}

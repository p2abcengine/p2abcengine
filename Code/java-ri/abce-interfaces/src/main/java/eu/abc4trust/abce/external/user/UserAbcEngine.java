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

package eu.abc4trust.abce.external.user;

import java.net.URI;
import java.util.List;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public interface UserAbcEngine {

  /**
   * This method, on input a presentation policy p, decides whether the credentials in the User’s
   * credential store could be used to produce a valid presentation token satisfying the policy p.
   * If so, this method returns true, otherwise, it returns false.
   * 
   * @param p
   * @return
   * @throws CredentialManagerException
   */
  public boolean canBeSatisfied(PresentationPolicyAlternatives p) throws CredentialManagerException;

  /**
   * This method, on input a presentation policy p, returns a presentation token that satisfies the
   * policy p, or returns an error if no such token could be created. This method will investigate
   * whether the User has the necessary credentials and/or established pseudonyms to create a token
   * that satisfies the policy. If there are one or more ways in which the policy can be satisfied
   * (e.g., by satisfying different alternatives in the policy, or by using different sets of
   * credentials to satisfy one alternative), this method will invoke an identity selection –
   * possibly presented as a user interface (the executable code of which is installed on the User’s
   * machine as part of the ABCE framework) – to let the user choose her preferred way of generating
   * the presentation token or cancel the action. If the policy cannot be satisfied (if the
   * canBeSatisfied method would have returned false), then the method returns an error.
   * 
   * @param p
   * @return
   * @throws CredentialManagerException
   * @throws CryptoEngineException
   * @throws IdentitySelectionException 
   */
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p)
      throws CannotSatisfyPolicyException, CredentialManagerException, CryptoEngineException, IdentitySelectionException;

  /**
   * This method is the same as the one-argument variant, except that the policy credential matcher
   * will use the provided idSelectionCallback object instead of using the default build-in
   * idSelection object. You may use idSelectionCallback as callback. idSelectionCallback will be
   * called zero or once.
   * 
   * @param p
   * @param idSelectionToUse
   * @return
   * @throws CannotSatisfyPolicyException
   * @throws CredentialManagerException
   * @throws CryptoEngineException
   * @throws IdentitySelectionException 
   */
  @Deprecated
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelection idSelectionCallback) throws CannotSatisfyPolicyException,
      CredentialManagerException, CryptoEngineException, IdentitySelectionException;

  /**
   * This method is the same as the one-argument variant, except that the policy credential matcher
   * will use the provided idSelectionCallback object instead of using the default build-in
   * idSelection object. You may use idSelectionCallback as callback. idSelectionCallback will be
   * called zero or once.
   * 
   * @param p
   * @param idSelectionToUse
   * @return
   * @throws CannotSatisfyPolicyException
   * @throws CredentialManagerException
   * @throws CryptoEngineException
   * @throws IdentitySelectionException 
   */
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelectionUi idSelectionCallback) throws CannotSatisfyPolicyException,
      CredentialManagerException, CryptoEngineException, IdentitySelectionException;

  /**
   * This method performs one step in an interactive issuance protocol. On input an incoming
   * issuance message m obtained from the Issuer, it either returns the outgoing issuance message
   * that is to be sent back to the Issuer, or returns a description of the newly issued credential
   * at successful completion of the protocol. In the former case, the Context attribute of the
   * outgoing message has the same value as that of the incoming message, allowing the Issuer to
   * link the different messages of this issuance protocol.
   * 
   * If this is the first time this method is called for a given context, the method expects the
   * issuance mesasage to conatain an issuance policy ip, and returns an issuance message that
   * contains an issuance token that satisfies the issuance policy and that also contains the
   * self-claimed attributes in atts, or throws an exception if no such token could be created. This
   * method will investigate whether the User has the necessary credentials and/or established
   * pseudonyms to create an issuance token that satisfies the issuance policy. If there are
   * multiple ways in which the policy can be satisfied (e.g., by using different sets of
   * credentials), this method will invoke an identity selection to choose the preferred way of
   * generating the presentation token.
   * 
   * @param m
   * @return
   * @throws CryptoEngineException
   * @throws IdentitySelectionException 
   * @throws CredentialManagerException
   * @throws KeyManagerException
   */
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
      throws CannotSatisfyPolicyException, CryptoEngineException, IdentitySelectionException;

  /**
   * This method is the same as the one-argument variant, except that the policy credential matcher
   * will use the provided idSelectionCallback object instead of using the default build-in
   * idSelection object. You may use idSelectionCallback as callback. idSelectionCallback will be
   * called zero or once.
   * 
   * @param p
   * @param idSelectionToUse
   * @return
   * @throws CryptoEngineException
   * @throws IdentitySelectionException 
   * @throws KeyManagerException
   * @throws CredentialManagerException
   */
  @Deprecated
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
      IdentitySelection idSelectionCallback) throws CannotSatisfyPolicyException,
      CryptoEngineException, IdentitySelectionException;

  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
      IdentitySelectionUi idSelectionCallback) throws CannotSatisfyPolicyException,
      CryptoEngineException, IdentitySelectionException;

  /**
   * This method updates the non-revocation evidence associated to all credentials in the credential
   * store. Calling this method at regular time intervals reduces the likelihood of having to update
   * non-revocation evidence at the time of presentation, thereby not only speeding up the
   * presentation process, but also offering improved privacy as the Revocation Authority is no
   * longer “pinged” at the moment of presentation.
   * 
   * @throws CredentialManagerException
   */
  public void updateNonRevocationEvidence() throws CredentialManagerException;

  /**
   * This method returns an array of all unique credential identifiers (UIDs) available in the
   * Credential Manager.
   * 
   * @return
   * @throws CredentialManagerException
   */
  public List<URI> listCredentials() throws CredentialManagerException;

  /**
   * This method returns the description of the credential with the given unique identifier. The
   * unique credential identifier creduid is the identifier which was included in the credential
   * description that was returned at successful completion of the issuance protocol.
   * 
   * @param credUid
   * @return
   * @throws CredentialManagerException
   */
  public CredentialDescription getCredentialDescription(URI credUid)
      throws CredentialManagerException;

  /**
   * This method deletes the credential with the given identifier from the credential store. If
   * deleting is not possible (e.g. if the refered credential does not exist) the method returns
   * false, and true otherwise.
   * 
   * @param credUid
   * @return
   * @throws CredentialManagerException
   */
  public boolean deleteCredential(URI credUid) throws CredentialManagerException;
}

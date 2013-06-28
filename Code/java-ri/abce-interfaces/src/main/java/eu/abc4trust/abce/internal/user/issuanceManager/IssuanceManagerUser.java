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

package eu.abc4trust.abce.internal.user.issuanceManager;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.util.DummyForNewABCEInterfaces;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public interface IssuanceManagerUser {

  public boolean canBeSatisfied(PresentationPolicyAlternatives p)
      throws CredentialManagerException, KeyManagerException;

  /**
   * This method performs one step in an interactive issuance protocol. The context attribute of the
   * issuance message m (obtained from the Issuer) must be the same for all messages in a given
   * issuance protocol. This method either returns the outgoing issuance message that is to be sent
   * back to the Issuer, or returns a description of the newly issued credential at successful
   * completion of the protocol. In the former case, the Context attribute of the outgoing message
   * has the same value as that of the incoming message, allowing the Issuer to link the different
   * messages of this issuance protocol.
   * 
   * 
   * Each time this method is invoked with a fresh context: On input an incoming issuance message m
   * (obtained from the Issuer) containing an issuance policy, it returns an outgoing issuance
   * message that is to be sent back to the Issuer. The Context attribute of the outgoing message
   * has the same value as that of the incoming message, allowing the Issuer to link the different
   * messages of this issuance protocol. This method will investigate whether the User has the
   * necessary credentials and/or established pseudonyms to create an issuance token that satisfies
   * the issuance policy. If there are multiple ways in which the policy can be satisfied (e.g., by
   * using different sets of credentials), this method will invoke the identity selection object
   * idSelectionCallback (which should spawn a user interface) to let the user choose her preferred
   * way of generating the presentation token. This method invokes the method
   * PolicyCredentialMatcher.createIssuanceToken(ip, ctxt, idSelectionCallback) on the issuance
   * policy ip and context attribute ctxt from the issuance message and the identity selection
   * object idSelectionCallback to use.
   * 
   * On subsequent calls, this method calls CryptoEngine.issuanceProtocolStep(m) and returns the
   * obtained output, which is either an outgoing issuance message or the description of the newly
   * issued credential.
   * 
   * @param m An issuance message from the issuer containing an issuance policy
   * @param idSelectionCallback
   * @return
   * @throws CredentialManagerException
   * @throws CryptoEngineException
   * @throws KeyManagerException
   * @throws IdentitySelectionException 
   */
  @Deprecated
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage im,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException;

  @Deprecated
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException;

  @Deprecated
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException;

  @Deprecated
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException;

  public boolean isRevoked(Credential cred) throws CryptoEngineException;

  
  public UiPresentationArguments createPresentationToken(PresentationPolicyAlternatives p, DummyForNewABCEInterfaces d) throws CredentialManagerException, KeyManagerException;

  public PresentationToken createPresentationToken(UiPresentationReturn upr) throws CryptoEngineException;

  public IssuanceReturn issuanceProtocolStep(IssuanceMessage im, DummyForNewABCEInterfaces d) throws CryptoEngineException, CredentialManagerException, KeyManagerException;

  public IssuanceMessage issuanceProtocolStep(UiIssuanceReturn uir);

}

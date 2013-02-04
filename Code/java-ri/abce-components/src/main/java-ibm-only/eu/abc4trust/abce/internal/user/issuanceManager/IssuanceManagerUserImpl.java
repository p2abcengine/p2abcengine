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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class IssuanceManagerUserImpl implements IssuanceManagerUser {

  private final PolicyCredentialMatcher policyCredMatcher;
  private final CryptoEngineUser cryptoEngine;
  private final Set<URI> usedContexts;

  @Inject
  IssuanceManagerUserImpl(PolicyCredentialMatcher policyCredMatcher, CryptoEngineUser cryptoEngine) {
    this.policyCredMatcher = policyCredMatcher;
    this.cryptoEngine = cryptoEngine;
    this.usedContexts = new HashSet<URI>();
  }

  @Override
  public boolean canBeSatisfied(PresentationPolicyAlternatives p)
      throws CredentialManagerException, KeyManagerException {
    return this.policyCredMatcher.canBeSatisfied(p);
  }

  @Override
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage im,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException {
    if (!this.usedContexts.contains(im.getContext())) {
      try {
        this.usedContexts.add(im.getContext());
        IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc();
        ret.im = this.policyCredMatcher.createIssuanceToken(im, idSelectionCallback);
        ret.cd = null;
        return ret;
      } catch (CredentialManagerException ex) {
        throw new CryptoEngineException(ex);
      }
    } else {
      return this.cryptoEngine.issuanceProtocolStep(im);
    }
  }

  @Override
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException {
    try {
      return this.policyCredMatcher.createPresentationToken(p, idSelectionCallback);
    } catch (CredentialManagerException ex) {
      throw new CryptoEngineException(ex);
    }
  }

  @Override
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException {
    try {
      return this.policyCredMatcher.createPresentationToken(p, idSelectionCallback);
    } catch (CredentialManagerException ex) {
      throw new CryptoEngineException(ex);
    }
  }

  @Override
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage im,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException {
    if (!this.usedContexts.contains(im.getContext())) {
      try {
        this.usedContexts.add(im.getContext());
        IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc();
        ret.im = this.policyCredMatcher.createIssuanceToken(im, idSelectionCallback);
        ret.cd = null;
        return ret;
      } catch (CredentialManagerException ex) {
        throw new CryptoEngineException(ex);
      }
    } else {
      return this.cryptoEngine.issuanceProtocolStep(im);
    }
  }

}

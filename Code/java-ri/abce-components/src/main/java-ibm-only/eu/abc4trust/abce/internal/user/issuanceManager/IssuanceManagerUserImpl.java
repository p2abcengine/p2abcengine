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
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
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
import eu.abc4trust.ui.idSelection.IdentitySelectionUiConverter;
import eu.abc4trust.ui.idSelection.IdentitySelectionUiPrinter;
import eu.abc4trust.util.DummyForNewABCEInterfaces;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class IssuanceManagerUserImpl implements IssuanceManagerUser {

  private final PolicyCredentialMatcher policyCredMatcher;
  private final CryptoEngineUser cryptoEngine;
  private final LinkedHashSet<URI> usedContexts;
  
  private static final int MAX_CONTEXTS = 1000;

  @Inject
  IssuanceManagerUserImpl(PolicyCredentialMatcher policyCredMatcher, CryptoEngineUser cryptoEngine) {
    this.policyCredMatcher = policyCredMatcher;
    this.cryptoEngine = cryptoEngine;
    this.usedContexts = new LinkedHashSet<URI>();
  }

  @Override
  public boolean canBeSatisfied(PresentationPolicyAlternatives p)
      throws CredentialManagerException, KeyManagerException {
    return this.policyCredMatcher.canBeSatisfied(p);
  }

  @Override
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage im,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException {
    IdentitySelectionUi ui = new IdentitySelectionUiPrinter(new IdentitySelectionUiConverter(idSelectionCallback));
    return issuanceProtocolStep(im, ui);
  }

  @Override
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelection idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException {
    IdentitySelectionUi ui = new IdentitySelectionUiPrinter(new IdentitySelectionUiConverter(idSelectionCallback));
    return createPresentationToken(p, ui);
  }

  @Override
  public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException {
    UiPresentationArguments upa = createPresentationToken(p, new DummyForNewABCEInterfaces());
    if(upa == null) {
      return null;
    }
    UiPresentationReturn upr = idSelectionCallback.selectPresentationTokenDescription(upa);
    return createPresentationToken(upr);
  }

  @Override
  public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage im,
      IdentitySelectionUi idSelectionCallback) throws CryptoEngineException, KeyManagerException, IdentitySelectionException, CredentialManagerException {
    IssuanceReturn ir = issuanceProtocolStep(im, new DummyForNewABCEInterfaces());
    if(ir.uia != null) {
      UiIssuanceReturn uir = idSelectionCallback.selectIssuanceTokenDescription(ir.uia);
      return new IssuMsgOrCredDesc(issuanceProtocolStep(uir));
    } else {
      return new IssuMsgOrCredDesc(ir);
    }
  }
  
  @Override
  public boolean isRevoked(Credential cred) throws CryptoEngineException {                       
  		return this.cryptoEngine.isRevoked(cred);               
  }

  @Override
  public UiPresentationArguments createPresentationToken(PresentationPolicyAlternatives p, DummyForNewABCEInterfaces d) throws CredentialManagerException, KeyManagerException {
    return this.policyCredMatcher.createPresentationToken(p, d);
  }

  @Override
  public PresentationToken createPresentationToken(UiPresentationReturn upr) throws CryptoEngineException {
    return this.policyCredMatcher.createPresentationToken(upr);
  }

  @Override
  public IssuanceReturn issuanceProtocolStep(IssuanceMessage im, DummyForNewABCEInterfaces d) throws CryptoEngineException, CredentialManagerException, KeyManagerException {
    if (!this.usedContexts.contains(im.getContext())) {
      addContext(im.getContext());
      UiIssuanceArguments args = this.policyCredMatcher.createIssuanceToken(im, d);
      return new IssuanceReturn(args);
    } else {
      IssuMsgOrCredDesc ret = this.cryptoEngine.issuanceProtocolStep(im);
      return new IssuanceReturn(ret);
    }
  }

  @Override
  public IssuanceMessage issuanceProtocolStep(UiIssuanceReturn uir) {
     return this.policyCredMatcher.createIssuanceToken(uir);
  }
  
  private void addContext(URI context) {
    this.usedContexts.add(context);
    // Trim set to size
    Iterator<URI> it = this.usedContexts.iterator();
    while(usedContexts.size() > MAX_CONTEXTS) {
      it.next();
      it.remove();
    }
  }

}

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

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.issuanceManager.IssuanceManagerUser;
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

public class UserAbcEngineImpl implements UserAbcEngine {

    private final IssuanceManagerUser issuanceManager;
    private final CredentialManager credentialManager;
    private final IdentitySelection defaultIdentitySelection;

    @Inject
    public UserAbcEngineImpl(IssuanceManagerUser issuanceManager,
            CredentialManager credentialManager,
            IdentitySelection defaultIdentitySelection) {
        this.issuanceManager = issuanceManager;
        this.credentialManager = credentialManager;
        this.defaultIdentitySelection = defaultIdentitySelection;
    }

    @Override
    public boolean canBeSatisfied(PresentationPolicyAlternatives p) throws CredentialManagerException {
        try {
          return this.issuanceManager.canBeSatisfied(p);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives p)
            throws CannotSatisfyPolicyException, CredentialManagerException,
            CryptoEngineException, IdentitySelectionException {
        try {
          return this.issuanceManager.createPresentationToken(p, this.defaultIdentitySelection);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public CredentialDescription getCredentialDescription(URI credUid)
            throws CredentialManagerException {
        return this.credentialManager.getCredentialDescription(credUid);
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException, CannotSatisfyPolicyException, IdentitySelectionException {
        try {
          return this.issuanceManager.issuanceProtocolStep(m, this.defaultIdentitySelection);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public List<URI> listCredentials() throws CredentialManagerException {
        return this.credentialManager.listCredentials();
    }

    @Override
    public void updateNonRevocationEvidence() throws CredentialManagerException {
        this.credentialManager.updateNonRevocationEvidence();
    }

    @Override
    public boolean deleteCredential(URI creduid) throws CredentialManagerException {
        return this.credentialManager.deleteCredential(creduid);
    }

    @Override
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
            IdentitySelection idSelectionCallback)
                    throws CannotSatisfyPolicyException, CredentialManagerException,
                    CryptoEngineException, IdentitySelectionException {
        try {
          return this.issuanceManager.createPresentationToken(p, idSelectionCallback);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
            IdentitySelection idSelectionCallback)
            throws CannotSatisfyPolicyException, CryptoEngineException, IdentitySelectionException {
        try {
          return this.issuanceManager.issuanceProtocolStep(m, idSelectionCallback);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives p,
        IdentitySelectionUi idSelectionCallback) throws CannotSatisfyPolicyException,
        CredentialManagerException, CryptoEngineException, IdentitySelectionException {
      try {
        return this.issuanceManager.createPresentationToken(p, idSelectionCallback);
      } catch (KeyManagerException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
        IdentitySelectionUi idSelectionCallback) throws CannotSatisfyPolicyException,
        CryptoEngineException, IdentitySelectionException {
      try {
        return this.issuanceManager.issuanceProtocolStep(m, idSelectionCallback);
      } catch (KeyManagerException e) {
        throw new RuntimeException(e);
      }
    }
    
}

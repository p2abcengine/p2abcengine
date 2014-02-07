//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.external.user;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
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
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class SynchronizedUserAbcEngineImpl implements UserAbcEngine {

    private final UserAbcEngine engine;

    @Inject
    public SynchronizedUserAbcEngineImpl(UserAbcEngine engine) {
        this.engine = engine;
    }

    @Override
    public synchronized boolean canBeSatisfied(PresentationPolicyAlternatives p)
            throws CredentialManagerException {
        return this.engine.canBeSatisfied(p);
    }

    @Override
    public synchronized PresentationToken createPresentationToken(
            PresentationPolicyAlternatives p)
                    throws CannotSatisfyPolicyException, CredentialManagerException,
                    CryptoEngineException, IdentitySelectionException {
        return this.engine.createPresentationToken(p);
    }

    @Override
    @Deprecated
    public synchronized PresentationToken createPresentationToken(
            PresentationPolicyAlternatives p,
            IdentitySelection idSelectionCallback)
                    throws CannotSatisfyPolicyException, CredentialManagerException,
                    CryptoEngineException, IdentitySelectionException {
        return this.engine.createPresentationToken(p, idSelectionCallback);
    }

    @Override
    public synchronized PresentationToken createPresentationToken(
            PresentationPolicyAlternatives p,
            IdentitySelectionUi idSelectionCallback)
                    throws CannotSatisfyPolicyException, CredentialManagerException,
                    CryptoEngineException, IdentitySelectionException {
        return this.engine.createPresentationToken(p, idSelectionCallback);
    }

    @Override
    public synchronized IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CannotSatisfyPolicyException, CryptoEngineException,
            IdentitySelectionException, CredentialManagerException {
        return this.engine.issuanceProtocolStep(m);
    }

    @Override
    @Deprecated
    public synchronized IssuMsgOrCredDesc issuanceProtocolStep(
            IssuanceMessage m,
            IdentitySelection idSelectionCallback)
                    throws CannotSatisfyPolicyException, CryptoEngineException,
                    IdentitySelectionException, CredentialManagerException {
        return this.engine.issuanceProtocolStep(m, idSelectionCallback);
    }

    @Override
    public synchronized IssuMsgOrCredDesc issuanceProtocolStep(
            IssuanceMessage m,
            IdentitySelectionUi idSelectionCallback)
                    throws CannotSatisfyPolicyException, CryptoEngineException,
                    IdentitySelectionException, CredentialManagerException {
        return this.engine.issuanceProtocolStep(m, idSelectionCallback);
    }

    @Override
    public synchronized void updateNonRevocationEvidence()
            throws CredentialManagerException {
        this.engine.updateNonRevocationEvidence();
    }

    @Override
    public synchronized List<URI> listCredentials()
            throws CredentialManagerException {
        return this.engine.listCredentials();
    }

    @Override
    public synchronized CredentialDescription getCredentialDescription(
            URI credUid)
                    throws CredentialManagerException {
        return this.engine.getCredentialDescription(credUid);
    }

    @Override
    public synchronized boolean deleteCredential(URI credUid)
            throws CredentialManagerException {
        return this.engine.deleteCredential(credUid);
    }

    @Override
    public synchronized boolean isRevoked(URI credUid)
            throws CryptoEngineException {
        return this.engine.isRevoked(credUid);
    }

    @Override
    public synchronized UiPresentationArguments createPresentationToken(
            PresentationPolicyAlternatives p, DummyForNewABCEInterfaces d) throws CannotSatisfyPolicyException,
            CredentialManagerException, KeyManagerException {
        return this.engine.createPresentationToken(p, d);
    }

    @Override
    public synchronized PresentationToken createPresentationToken(UiPresentationReturn upr)
            throws CredentialManagerException, CryptoEngineException {
        return this.engine.createPresentationToken(upr);
    }

    @Override
    public synchronized IssuanceReturn issuanceProtocolStep(IssuanceMessage im, DummyForNewABCEInterfaces d)
            throws CannotSatisfyPolicyException, CryptoEngineException, CredentialManagerException, KeyManagerException {
        return this.engine.issuanceProtocolStep(im, d);
    }

    @Override
    public synchronized IssuanceMessage issuanceProtocolStep(UiIssuanceReturn uir)
            throws CryptoEngineException {
        return this.engine.issuanceProtocolStep(uir);
    }

}

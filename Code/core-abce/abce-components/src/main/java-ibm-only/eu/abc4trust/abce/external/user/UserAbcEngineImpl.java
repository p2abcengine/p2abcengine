//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.issuanceManager.IssuanceManagerUser;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.util.PolicyFriendlyDescrGenerator;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class UserAbcEngineImpl implements UserAbcEngine {

    private final IssuanceManagerUser issuanceManager;
    private final CredentialManager credentialManager;
    private final KeyManager keyManager;
    private final IdentitySelection defaultIdentitySelection;

    @Inject
    public UserAbcEngineImpl(IssuanceManagerUser issuanceManager,
            CredentialManager credentialManager,
            KeyManager keyManager,
            IdentitySelection defaultIdentitySelection) {
        this.issuanceManager = issuanceManager;
        this.credentialManager = credentialManager;
        this.defaultIdentitySelection = defaultIdentitySelection;
        this.keyManager = keyManager;
    }

    @Override
    public boolean canBeSatisfied(String username, PresentationPolicyAlternatives p) throws CredentialManagerException, CryptoEngineException {
        try {
          return this.issuanceManager.canBeSatisfied(username, p);
        } catch (KeyManagerException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public CredentialDescription getCredentialDescription(String username, URI credUid)
            throws CredentialManagerException {
        return this.credentialManager.getCredentialDescription(username, credUid);
    }

    @Override
    public List<URI> listCredentials(String username) throws CredentialManagerException {
        return this.credentialManager.listCredentials(username);
    }

    @Override
    public void updateNonRevocationEvidence(String username) throws CredentialManagerException {
        this.credentialManager.updateNonRevocationEvidence(username);
    }

    @Override
    public boolean deleteCredential(String username, URI creduid) throws CredentialManagerException {
        return this.credentialManager.deleteCredential(username, creduid);
    }


    
    @Override
    public boolean isRevoked(String username, URI credUid) throws CryptoEngineException {           
    	try {               
    		Credential cred = this.credentialManager.getCredential(username, credUid);               
    		return this.issuanceManager.isRevoked(username, cred);          
    	} catch (CredentialManagerException ex) {              
    		throw new CryptoEngineException(ex);                   
    	}       
    }

    @Override
    public UiPresentationArguments createPresentationToken(String username, 
        PresentationPolicyAlternatives p) throws CannotSatisfyPolicyException,
        CredentialManagerException, KeyManagerException, CryptoEngineException {
      return this.issuanceManager.createPresentationToken(username, p);
    }

    @Override
    public PresentationToken createPresentationToken(String username, UiPresentationReturn upr)
        throws CredentialManagerException, CryptoEngineException {
      return this.issuanceManager.createPresentationToken(username, upr);
    }

    @Override
    public IssuanceReturn issuanceProtocolStep(String username, IssuanceMessage im)
        throws CannotSatisfyPolicyException, CryptoEngineException, CredentialManagerException, KeyManagerException {
      return this.issuanceManager.issuanceProtocolStep(username, im);
    }

    @Override
    public IssuanceMessage issuanceProtocolStep(String username, UiIssuanceReturn uir)
        throws CryptoEngineException {
      return this.issuanceManager.issuanceProtocolStep(username, uir);
    }

    @Override
    public PresentationToken createPresentationTokenFirstChoice(String username,
        PresentationPolicyAlternatives p) throws CannotSatisfyPolicyException,
        CredentialManagerException, KeyManagerException, CryptoEngineException {
      UiPresentationArguments arg = this.createPresentationToken(username, p);
      if(arg == null) {
        return null;
      }
      UiPresentationReturn ret = new UiPresentationReturn(arg);
      return this.createPresentationToken(username, ret);
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStepFirstChoice(String username, IssuanceMessage im)
        throws CannotSatisfyPolicyException, CryptoEngineException, CredentialManagerException,
        KeyManagerException {
      IssuanceReturn arg = this.issuanceProtocolStep(username, im);
      if(arg == null) {
        return null;
      }
      if(arg.uia != null) {
        UiIssuanceReturn uiret = new UiIssuanceReturn(arg.uia);
        arg.uia = null;
        arg.im = this.issuanceProtocolStep(username, uiret);
      }
      IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc(arg);
      return ret;
    }

	@Override
	public List<String> createHumanReadablePresentationPolicy(
			PresentationPolicyAlternatives ppa) throws KeyManagerException {
		return PolicyFriendlyDescrGenerator.generateFriendlyPresentationPolicyDescription(ppa, keyManager);
	}

	@Override
	public List<String> createHumanReadableIssuancePolicy(IssuancePolicy ip)
			throws KeyManagerException {
		return PolicyFriendlyDescrGenerator.generateFriendlyIssuancePolicyDescription(ip, keyManager);
	}
    
}

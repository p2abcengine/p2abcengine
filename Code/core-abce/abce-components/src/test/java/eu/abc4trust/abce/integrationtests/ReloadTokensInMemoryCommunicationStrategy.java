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

package eu.abc4trust.abce.integrationtests;

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadInformation;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.user.UProveCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.user.UProveIssuanceHandling;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ui.idSelection.MockIdentitySelectionUi;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This class handles the communication between user abce and issuer abce in our tests.
 * The class handles the communication by calling the relevant methods directly on the engines.
 * E.g. there is no web service calls involved
 * 
 */
public class ReloadTokensInMemoryCommunicationStrategy implements ReloadTokensCommunicationStrategy {

    private IssuerAbcEngine issuerAbcEngine;
    private IssuancePolicy issuancePolicy;
    private final UProveIssuanceHandling issuanceHandling;
    private final CredentialManager credManager;
    private final PolicyCredentialMatcher policyCredMatcher;

    @Inject
    public ReloadTokensInMemoryCommunicationStrategy(UProveIssuanceHandling issuanceHandling, CredentialManager credManager, PolicyCredentialMatcher policyCredMatcher) {
        this.issuanceHandling = issuanceHandling;
        this.credManager = credManager;
        this.policyCredMatcher = policyCredMatcher;
    }

    public void setIssuerAbcEngine(IssuerAbcEngine issuerAbcEngine) {
        this.issuerAbcEngine = issuerAbcEngine;
    }

    public void setIssuancePolicy(IssuancePolicy issuancePolicy) {
        this.issuancePolicy =issuancePolicy;
    }

    @Override
    public Credential reloadTokens(Credential cred) throws ReloadException {
        if ((this.issuerAbcEngine==null) || (this.issuancePolicy==null)) {
            throw new RuntimeException("Cannot reload tokens. ReloadTokensInMemoryCommunicationStrategy is not initialized properly");
        }

        try {
            System.out.println("issuancePolicy used for re-issuance:\n" + XmlUtils.toXml(new ObjectFactory().createIssuancePolicy(this.issuancePolicy)));
            IssuanceMessageAndBoolean issuerIm = this.issuerAbcEngine.initReIssuanceProtocol(
                    this.issuancePolicy);

            IssuMsgOrCredDesc userIm = new IssuMsgOrCredDesc();
            UProveCryptoEngineUserImpl.RELOADING_TOKENS = true;
            userIm.im = this.policyCredMatcher.createIssuanceToken(
                    issuerIm.getIssuanceMessage(),
                    new MockIdentitySelectionUi());
            UProveCryptoEngineUserImpl.RELOADING_TOKENS = false;
            while (!issuerIm.isLastMessage()) {
                issuerIm = this.issuerAbcEngine.reIssuanceProtocolStep(userIm.im);
                userIm = this.issuanceHandling.issuanceProtocolStep(issuerIm
                        .getIssuanceMessage(), cred.getCredentialDescription()
                        .getCredentialUID());
            }

            return this.credManager.getCredential(userIm.cd.getCredentialUID());
        } catch (CredentialManagerException e) {
            e.printStackTrace();
            throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        } catch (CryptoEngineException e) {
            e.printStackTrace();
            throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        } catch (IdentitySelectionException e) {
            e.printStackTrace();
            throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        } catch (KeyManagerException e) {
            e.printStackTrace();
            throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        } catch(Exception e){
            e.printStackTrace();
            throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        }
    }

    @Override
    public void setCredentialInformation(URI context, ReloadInformation info) {
        //Empty
    }

    @Override
    public void addCredentialIssuer(URI context,
            CredentialDescription credDesc, String issuanceUrl,
            String issuanceStepUrl) {
        //Empty
    }

}

//* Licensed Materials - Property of IBM, Miracle A/S,                *
//* and Alexandra Instituttet A/S                                     *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.integrationtests;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadInformation;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.user.UProveIssuanceHandling;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceMessageAndBoolean;
import eu.abc4trust.ui.idSelection.MockIdentitySelectionUi;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuancePolicy;

/**
 * This class handles the communication between user abce and issuer abce in our tests.
 * The class handles the communication by calling the relevant methods directly on the engines. 
 * E.g. there is no web service calls involved
 * 
 */
public class ReloadTokensInMemoryCommunicationStrategy implements ReloadTokensCommunicationStrategy {

	private IssuerAbcEngine issuerAbcEngine;
	private IssuancePolicy issuancePolicy;
	private List<Attribute> issuerAttributes;
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

	public void setIssuerAttributes(List<Attribute> issuerAttributes) {
		this.issuerAttributes = issuerAttributes;
	}


	@Override
	public Credential reloadTokens(Credential cred) throws ReloadException {
		if (issuerAbcEngine==null || issuancePolicy==null || issuerAttributes==null)
			throw new RuntimeException("Cannot reload tokens. ReloadTokensInMemoryCommunicationStrategy is not initialized properly");
		//issuerAbcEngine.initIssuanceProtocol(ip, attributes);

		try {
			IssuanceMessageAndBoolean issuerIm = issuerAbcEngine.initIssuanceProtocol(
					issuancePolicy, issuerAttributes);

			IssuMsgOrCredDesc userIm = new IssuMsgOrCredDesc();
			userIm.im = policyCredMatcher.createIssuanceToken(issuerIm.im, new MockIdentitySelectionUi());

			while (!issuerIm.lastMessage) {
				issuerIm = issuerAbcEngine.issuanceProtocolStep(userIm.im);
				userIm = issuanceHandling.issuanceProtocolStep(issuerIm.im, cred.getCredentialDescription().getCredentialUID());
			}

			return credManager.getCredential(userIm.cd.getCredentialUID());
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

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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;

public class ReloadTokensWebServiceCommunicationStrategy implements ReloadTokensCommunicationStrategy {
	private final UProveIssuanceHandling issuanceHandling;
	private final CredentialManager credManager;
	private final PolicyCredentialMatcher policyCredMatcher;
	private final ReloadStorageManager storageManager;

	private final Map<String, ReloadInformation> infoMap = new HashMap<String, ReloadInformation>();

	@Inject
	public ReloadTokensWebServiceCommunicationStrategy(UProveIssuanceHandling issuanceHandling, CredentialManager credManager, PolicyCredentialMatcher policyCredMatcher, ReloadStorageManager storageManager) {
		this.issuanceHandling = issuanceHandling;	
		this.credManager = credManager;
		this.policyCredMatcher = policyCredMatcher;
		this.storageManager = storageManager;
	}

	@Override
	public Credential reloadTokens(Credential cred)
			throws ReloadException {

		System.out.println("reloadTokens called");
		try {

			ReloadInformation info = storageManager.retrive(cred);

			ObjectFactory of = new ObjectFactory();
			Client client = Client.create();
			Builder issueStartResource = client.resource(info.issuanceUrl)
					.type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

			IssuanceMessage serverIm = issueStartResource.post(IssuanceMessage.class);

			IssuMsgOrCredDesc userIm = new IssuMsgOrCredDesc();
			ReloadTokensIdentitySelection idSelection = new ReloadTokensIdentitySelection(cred, info);

			userIm.im = policyCredMatcher.createIssuanceToken(serverIm, idSelection);
			boolean lastmessage = false;

			while (!lastmessage) {
				Builder issueStepResource = client.resource(info.issuanceStepUrl).type(MediaType.APPLICATION_XML)
						.accept(MediaType.TEXT_XML);
				serverIm = issueStepResource.post(IssuanceMessage.class, of.createIssuanceMessage(userIm.im));

				userIm = issuanceHandling.issuanceProtocolStep(serverIm, 
						cred.getCredentialDescription().getCredentialUID());
				lastmessage = (userIm.cd != null);
			}

			//assert ( userIm.cd.getCredentialUID() == credUid )
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
		infoMap.put(context.toString(), info);
	}

	@Override
	public void addCredentialIssuer(URI context, CredentialDescription credDesc, String issuanceUrl, String issuanceStepUrl)  {
		ReloadInformation info = infoMap.get(context.toString());
		if (info==null) {
			System.out.println("NOTE: ReloadTokens: No credential information found for \"" + context.toString()+ "\" cannot store reload information. Will not be able to reload tokens later");
      return;
    }

		storageManager.store(credDesc, issuanceUrl, issuanceStepUrl, info);
	}



}

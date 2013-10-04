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

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;

public interface ReloadTokensCommunicationStrategy {

	/*
	 * This method calls the issuer, initiates an issuance and updates the credential in the credential manager.
	 * 
	 * 
	 * @param cred
	 * @return the updated credential (do not reuse cred afterwards). Throws if unsuccesful
	 * @throws CryptoEngineException
	 * @throws CredentialManagerException
	 * @throws KeyManagerException
	 */
	public Credential reloadTokens(Credential cred) throws ReloadException;


	/*
	 * To be called while issuing a new credential. Store in persistent storage (e.g. smartcard) information 
	 * required to reload tokens for the credential. 
	 * Note: this info will only be stored if addCredentialIssuer is subsequently called
	 *  
	 * @param context
	 * @param info
	 */
	public void setCredentialInformation(URI context, ReloadInformation info);

	/*
	 * Stores the urls for the issuance webservice that issued the credential attached to the credDesc parameter.
	 * Sideeffect: we expect a ReloadInformation to be set also for this issuance (identified by the issuance context)
	 * And this info is also stored.
	 * 
	 * @param context
	 * @param credDesc
	 * @param issuanceUrl
	 * @param issuanceStepUrl
	 */
	public void addCredentialIssuer(URI context, CredentialDescription credDesc, String issuanceUrl, String issuanceStepUrl);


	//exception
	public static class ReloadException extends RuntimeException {
		//we just inheirit from Exception to give the application a type to catch
		//no additional funcationality added

		private static final long serialVersionUID = 7802212044327859056L;

		public ReloadException(String string) {
			 super(string);
		}
	}
	
}

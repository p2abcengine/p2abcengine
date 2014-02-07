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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.IssuanceMessage;

/**
 * 
 * Interface mainly to allow guice to handle circular references between CredentialManager and CryptoEngineUser
 *
 */
public interface UProveIssuanceHandling {
	
	/**
	 * user side implementation of issuanceProtocol for UProve.
	 * 
	 * @param m IssuanceMessage
	 * @param credentialUri if null then new credential is saved in credentialManager as a new credential. Otherwise
	 *        the uri is used as identifier for an existing credential which is then updated (overwritten) when the
	 *        issuance protocol is completed
	 * @return
	 * @throws CryptoEngineException
	 */
	public abstract IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m,
			URI prevCredentialUri) throws CryptoEngineException;

}
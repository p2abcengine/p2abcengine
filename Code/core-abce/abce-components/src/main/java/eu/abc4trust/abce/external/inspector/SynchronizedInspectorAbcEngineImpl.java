//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.abce.external.inspector;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

public class SynchronizedInspectorAbcEngineImpl implements InspectorAbcEngine {

	private final InspectorAbcEngine engine;

	@Inject
	public SynchronizedInspectorAbcEngineImpl(InspectorAbcEngine engine) {
		this.engine = engine;
	}

	@Override
	public synchronized List<Attribute> inspect(PresentationToken t) throws CryptoEngineException {
		return this.engine.inspect(t);
	}


	@Override
	public InspectorPublicKey setupInspectorPublicKey(SystemParameters sp,
			URI mechanism, URI uid,
			List<FriendlyDescription> friendlyInspectorDescription)
					throws CryptoEngineException, CredentialManagerException {
		return this.engine.setupInspectorPublicKey(sp, mechanism, uid, friendlyInspectorDescription);
	}


}
 
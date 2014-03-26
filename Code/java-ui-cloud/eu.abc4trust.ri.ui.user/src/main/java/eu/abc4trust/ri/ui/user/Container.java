//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.14                                           *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
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

package eu.abc4trust.ri.ui.user;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.xml.ABCEBoolean;

public enum Container {

	INSTANCE;
	private List<CredentialInUi> credentials = new ArrayList<CredentialInUi>();
	private Map<String, PresentationContainer> presentations = new HashMap<String, PresentationContainer>();

	public void addCredential(CredentialInUi credential) {
		credentials.add(credential);
	}

	public List<CredentialInUi> listCredentials() {
		return credentials;
	}

	public void removeCredential(CredentialInUi credential) {
		try {
			Client client = Client.create();
			WebResource resource;
			resource = client
					.resource("http://localhost:9200/user/deleteCredential/"
							+ URLEncoder.encode(credential.uri, "UTF-8"));
			resource.type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML)
					.delete(new GenericType<JAXBElement<ABCEBoolean>>() {
					});
			credentials.remove(credential);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void addPresentation(String userNonce, PresentationContainer pc) {
		presentations.put(userNonce, pc);
	}
	
	public PresentationContainer getPresentation(String userNonce) {
		return presentations.get(userNonce);
	}
	
	public PresentationContainer getAndRemovePresentation(String userNonce) {
		return presentations.remove(userNonce);
	}
}

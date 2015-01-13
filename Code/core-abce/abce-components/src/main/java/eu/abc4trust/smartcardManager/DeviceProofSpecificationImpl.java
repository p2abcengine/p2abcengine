//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.smartcardManager;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;

public class DeviceProofSpecificationImpl implements DeviceProofSpecification {

	final private Map<URI, Set<URI>> credList;
	final private Set<URI> pkList;
	final private Map<URI, Set<URI>> sepList;
	private final String username;

	@Override
	public void addCredentialProof(URI deviceId, URI credentialUri) {
		if(! credList.containsKey(deviceId)) {
			credList.put(deviceId, new HashSet<URI>());
		}
		credList.get(deviceId).add(credentialUri);
	}

	@Override
	public void addScopeExclusivePseudonymProof(URI deviceId, URI scope) {
		if(! sepList.containsKey(deviceId)) {
			sepList.put(deviceId, new HashSet<URI>());
		}
		sepList.get(deviceId).add(scope);
	}

	@Override
	public void addPublicKeyProof(URI deviceId) {
		pkList.add(deviceId);
	}

	// Package private
	public DeviceProofSpecificationImpl(String username) {
		credList = new HashMap<URI, Set<URI>>();
		pkList = new HashSet<URI>();
		sepList = new HashMap<URI, Set<URI>>();
		this.username = username;
	}
	
	public String getUsername() {
	  return username;
	}


	Set<URI> computeListOfInvolvedSmartcards() {
		Set<URI> ret = new HashSet<URI>();
		for(URI sc: credList.keySet()) {
			ret.add(sc);
		}
		for(URI sc: pkList) {
			ret.add(sc);
		}
		for(URI sc: sepList.keySet()) {
			ret.add(sc);
		}
		return ret;
	}

	Set<URI> getListOfInvolvedCredentials(URI deviceId) {
		Set<URI> ret =  credList.get(deviceId);
		if (ret == null) {
			return new HashSet<URI>();
		} else {
			return ret;
		}
	}

	boolean isProofOfPublicKey(URI deviceId) {
		return pkList.contains(deviceId);
	}

	Set<URI> getListOfScopeExclusivePseudonyms(URI deviceId) {
		Set<URI> ret = sepList.get(deviceId);
		if (ret == null) {
			return new HashSet<URI>();
		} else {
			return ret;
		}
	}
}

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

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;

public class DeviceProofResponseImpl implements DeviceProofResponse {

	private final Map<URI, Map<URI, BigInteger>> crResp;
	private final Map<URI, BigInteger> pkResp;

	@Override
	public BigInteger getResponseForCredentialSecretKey(URI deviceUid,
			URI credentialUri) {
		return crResp.get(deviceUid).get(credentialUri);
	}

	@Override
	public BigInteger getResponseForDeviceSecretKey(URI deviceUid) {
		return pkResp.get(deviceUid);
	}

	// Package private
	DeviceProofResponseImpl() {
		pkResp = new HashMap<URI, BigInteger>();
		crResp = new HashMap<URI, Map<URI,BigInteger>>();
	}

	void setResponseForCredentialRandomizer(URI deviceUid, URI credUri, BigInteger value) {
	    if(! crResp.containsKey(deviceUid)) {
	      crResp.put(deviceUid, new HashMap<URI, BigInteger>());
	    }
	    crResp.get(deviceUid).put(credUri, value);
	  }
	  
	  void setResponseForDeviceSecretKey(URI deviceUid, BigInteger value) {
	    pkResp.put(deviceUid, value);
	  }
	
}

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
import java.util.TreeMap;

import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;

public class DeviceProofCommitmentImpl implements DeviceProofCommitment{

	private final Map<URI, Map<URI, BigInteger>> credCom;
	private final Map<URI, BigInteger> pkCom;
	private final Map<URI, Map<URI, BigInteger>> sepCom;
	private final TreeMap<URI, byte[]> zkNonceCommitments;
	private final DeviceProofSpecificationImpl spec;

	@Override
	public BigInteger getCommitmentForCredential(URI deviceUid,
			URI credentialUri) {
		return credCom.get(deviceUid).get(credentialUri);
	}

	@Override
	public BigInteger getCommitmentForScopeExclusivePseudonym(URI deviceUid,
			URI scope) {
		return sepCom.get(deviceUid).get(scope);
	}

	@Override
	public BigInteger getCommitmentForPublicKey(URI deviceUid) {
		return pkCom.get(deviceUid);
	}

	DeviceProofCommitmentImpl(DeviceProofSpecificationImpl spec) {
		credCom = new HashMap<URI, Map<URI,BigInteger>>();
		pkCom = new HashMap<URI, BigInteger>();
		sepCom = new HashMap<URI, Map<URI,BigInteger>>();
		zkNonceCommitments = new TreeMap<URI, byte[]>();
		this.spec = spec;
	}

	public void setCommitmentForCredential(URI smartcardUri, URI credUri, BigInteger value) {
		if(! credCom.containsKey(smartcardUri)) {
			credCom.put(smartcardUri, new HashMap<URI, BigInteger>());
		}
		credCom.get(smartcardUri).put(credUri, value);
	}

	public void setCommitmentForPublicKey(URI smartcardUri, BigInteger value) {
		pkCom.put(smartcardUri, value);
	}

	public void setCommitmentForScopeExclusivePseudonym(URI smartcardUri, URI scope, BigInteger value) {
		if(! sepCom.containsKey(smartcardUri)) {
			sepCom.put(smartcardUri, new HashMap<URI, BigInteger>());
		}
		sepCom.get(smartcardUri).put(scope, value);
	}

	public DeviceProofSpecificationImpl getProofSpec() {
		return spec;
	}

}

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

package eu.abc4trust.smartcardManager;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.ibm.zurich.idmx.smartcard.IdemixProofResponse;

class ScmProofResponse implements IdemixProofResponse {

  private final Map<URI, Map<URI, BigInteger>> crResp;
  private final Map<URI, BigInteger> pkResp;
  
  @Override
  public BigInteger responseForCredentialRandomizer(URI smartcardUri, URI credUri) {
    return crResp.get(smartcardUri).get(credUri);
  }

  @Override
  public BigInteger responseForDeviceSecretKey(URI smartcardUri) {
    return pkResp.get(smartcardUri);
  }
  
  // Package private
  ScmProofResponse() {
    pkResp = new HashMap<URI, BigInteger>();
    crResp = new HashMap<URI, Map<URI,BigInteger>>();
  }
  
  void setResponseForCredentialRandomizer(URI smartcardUri, URI credUri, BigInteger value) {
    if(! crResp.containsKey(smartcardUri)) {
      crResp.put(smartcardUri, new HashMap<URI, BigInteger>());
    }
    crResp.get(smartcardUri).put(credUri, value);
  }
  
  void setResponseForDeviceSecretKey(URI smartcardUri, BigInteger value) {
    pkResp.put(smartcardUri, value);
  }

}

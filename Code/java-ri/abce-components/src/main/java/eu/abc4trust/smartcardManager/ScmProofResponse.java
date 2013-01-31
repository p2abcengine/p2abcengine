//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

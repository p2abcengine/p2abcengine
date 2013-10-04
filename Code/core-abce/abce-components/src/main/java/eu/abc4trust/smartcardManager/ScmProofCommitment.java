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

package eu.abc4trust.smartcardManager;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.zurich.idmx.smartcard.IdemixProofCommitment;

import eu.abc4trust.smartcard.Utils;
import eu.abc4trust.smartcard.ZkNonceCommitmentOpening;

class ScmProofCommitment implements IdemixProofCommitment {
  
  private final Map<URI, Map<URI, BigInteger>> credCom;
  private final Map<URI, BigInteger> pkCom;
  private final Map<URI, Map<URI, BigInteger>> sepCom;
  private final TreeMap<URI, byte[]> zkNonceCommitments;
  private final TreeMap<URI, ZkNonceCommitmentOpening> zkNonceOpenings;
  private final ScmProofSpec spec;

  @Override
  public BigInteger commitmentForCredential(URI smartcardUri, URI credUri) {
    return credCom.get(smartcardUri).get(credUri);
  }

  @Override
  public BigInteger commitmentForPublicKey(URI smartcardUri) {
    return pkCom.get(smartcardUri);
  }

  @Override
  public BigInteger commitmentForScopeExclusivePseudonym(URI smartcardUri, URI scope) {
    return sepCom.get(smartcardUri).get(scope);
  }
  
  // Package private
  
  ScmProofCommitment(ScmProofSpec spec) {
    credCom = new HashMap<URI, Map<URI,BigInteger>>();
    pkCom = new HashMap<URI, BigInteger>();
    sepCom = new HashMap<URI, Map<URI,BigInteger>>();
    zkNonceCommitments = new TreeMap<URI, byte[]>();
    zkNonceOpenings = new TreeMap<URI, ZkNonceCommitmentOpening>();
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
  
  public ScmProofSpec getProofSpec() {
    return spec;
  }
  
  public void setNonceCommitment(URI smartcardUri, byte[] nonceCommitment) {
    zkNonceCommitments.put(smartcardUri, nonceCommitment);
  }
  
  @Deprecated
  public void setNonceOpening(URI smartcardUri, ZkNonceCommitmentOpening nonceOpening) {
    byte[] expected = zkNonceCommitments.get(smartcardUri);
    byte[] actual = Utils.computeCommitment(nonceOpening);
    if(! Arrays.equals(expected, actual)) {
      throw new RuntimeException("Opening does not match");
    }
    zkNonceOpenings.put(smartcardUri, nonceOpening);
  }
  
  public List<byte[]> getListOfNonceCommitments() {
    return new ArrayList<byte[]>(zkNonceCommitments.values());
  }
  
  public List<ZkNonceCommitmentOpening> getListOfNonceOpenings() {
    return new ArrayList<ZkNonceCommitmentOpening>(zkNonceOpenings.values());
  }
  
  public byte[] getNonce() {
    
//    if (zkNonceCommitments.size() != zkNonceOpenings.size()) {
//      return null;
//    }
    //TODO: Currently this only works for using 1 smartcard.
    List<byte[]> nonces = new ArrayList<byte[]>();
    //for (ZkNonceCommitmentOpening o: zkNonceOpenings.values()) {
    for (byte[] nonce: zkNonceCommitments.values()) {
      nonces.add(nonce);
    }
    return nonces.get(0);
    //return Utils.hashConcat(nonces);
  }

public byte[] getMyNonce(URI sc) {
	return zkNonceCommitments.get(sc);
}

}

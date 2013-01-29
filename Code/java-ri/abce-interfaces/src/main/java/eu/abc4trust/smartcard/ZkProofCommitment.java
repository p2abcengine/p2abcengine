//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ZkProofCommitment {
  ZkProofSpecification spec;
  // Associates credentialId with ZK-commitment for credentials
  public Map<URI, BigInteger> commitmentForCreds;
  // Associates the scope with ZK-commitment for scope-exclusive pseudonym
  public Map<URI, BigInteger> commitmentForScopeExclusivePseudonyms;
  // This value may be null if it is not needed
  public BigInteger commitmentForDevicePublicKey;
  // The commitment to the nonce of the smartcard:  H(nonce || opening)
  public byte[] nonceCommitment;
  
  public ZkProofCommitment() {
    commitmentForCreds = new HashMap<URI, BigInteger>();
    commitmentForScopeExclusivePseudonyms = new HashMap<URI, BigInteger>();
  }
}

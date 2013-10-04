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

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

public class ZkProofState {
  public ZkProofCommitment commitment;
  public ZkProofWitness witnesses;
  //Associates the credentialId with the ZK-randomness for credential
  public Map<URI, BigInteger> randomnessForCourses;
  public BigInteger randomnessForDeviceSecret;
  public byte[] nonce;
}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

public class ZkProofWitness {
  // Associates credentialId with course randomizer (the value v)
  public Map<URI, BigInteger> courseRandomizer;
  public BigInteger deviceSecret;
}

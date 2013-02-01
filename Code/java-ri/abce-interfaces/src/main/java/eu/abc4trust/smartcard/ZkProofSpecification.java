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

public class ZkProofSpecification {
  public Map<URI, GroupParameters> credentialBases;
  public Map<URI, BigInteger> credFragment;
  public Map<URI, BigInteger> scopeExclusivePseudonymValues;
  // devicePublicKey may be null if it is not needed for the proof
  public BigInteger devicePublicKey;
  public SystemParameters parametersForPseudonyms;
  
  public int zkChallengeSizeBytes;
  public int zkStatisticalHidingSizeBytes;
  public int deviceSecretSizeBytes;
  public int zkNonceSizeBytes;
  
  public ZkProofSpecification(SystemParameters params) {
    zkChallengeSizeBytes = params.zkChallengeSizeBytes;
    zkStatisticalHidingSizeBytes = params.zkStatisticalHidingSizeBytes;
    deviceSecretSizeBytes = params.deviceSecretSizeBytes;
    zkNonceSizeBytes = params.zkNonceSizeBytes;
  }
}

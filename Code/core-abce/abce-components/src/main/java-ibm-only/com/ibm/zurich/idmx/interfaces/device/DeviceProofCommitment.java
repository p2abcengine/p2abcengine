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

// * Licensed Materials - Property of IBM *
// * com.ibm.zurich.idmx.2.3.40 *
// * (C) Copyright IBM Corp. 2013. All Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************
package com.ibm.zurich.idmx.interfaces.device;

import java.math.BigInteger;
import java.net.URI;

//TODO: Remove this file once the new crypto architecture is ready.

public interface DeviceProofCommitment {
  /**
   * Returns the T-Value for the proof of possession of the credential public key. The device
   * chooses r_x and (if applicable) r_v as getRandomizerSizeBytes()-bytes strings and returns T =
   * gd^{r_x} * gr^{r_v} (mod n) or C = gr^{r_x} (mod n), respectively. If there are multiple proofs
   * performed on a device, the same value r_x is use for all the proofs involving that device.
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getCommitmentForCredential(URI deviceUid, URI credentialUri);

  /**
   * Returns the T-Value for the proof of possession of the given scope exclusive pseudonym. The
   * device chooses r_x as a getRandomizerSizeBytes()-bytes string and returns T =
   * (hash(scope)^cofactor)^{r_x} (mod p). If there are multiple proofs performed on a device, the
   * same value r_x is use for all the proofs involving that device.
   * 
   * @param deviceUid
   * @param scope
   * @return
   */
  public BigInteger getCommitmentForScopeExclusivePseudonym(URI deviceUid, URI scope);

  /**
   * Returns the T-Value for the proof of possession of the public key of the given device. The
   * device chooses r_x as a getRandomizerSizeBytes()-bytes string and returns T = g^{r_x} (mod p).
   * If there are multiple proofs performed on a device, the same value r_x is use for all the
   * proofs involving that device.
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getCommitmentForPublicKey(URI deviceUid);
}

//* Licensed Materials - Property of IBM                              *
//* com.ibm.zurich.idmx.2.3.40                                        *
//* (C) Copyright IBM Corp. 2013. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************
package com.ibm.zurich.idmx.interfaces.device;

import java.math.BigInteger;
import java.net.URI;

//TODO: Remove this file once the new crypto architecture is ready.

public interface DeviceProofResponse {
  /**
   * Returns the S-value associated with the credential secret key (v).
   * Returns s_v = r_v - c * v, where c is the challenge.
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getResponseForCredentialSecretKey(URI deviceUid, URI credentialUri);
  
  /**
   * Returns the S-value associated with the device secret key (x).
   * Returns s_x = r_x - c * x, where c is the challenge.
   * @param deviceUid
   * @return
   */
  public BigInteger getResponseForDeviceSecretKey(URI deviceUid);
}

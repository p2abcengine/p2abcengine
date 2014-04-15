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

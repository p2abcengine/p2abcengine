//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.math.BigInteger;

import eu.abc4trust.xml.SmartcardSystemParameters;

/**
 * A scope exclusive pseudonym is of the form:
 * hash(scope)^deviceSecret (mod p)
 * 
 * A normal pseudonym is of the form:
 * g^deviceSecret * h^r (mod p)
 * where r is a random number associated with the pseudonym (and h is defined elsewhere).
 * 
 * g (mod p) should generate a subgroup of order subgroupOrder.
 *
 */
public class SystemParameters implements Serializable {
  // If updated - change serialVersionUID
  private static final long serialVersionUID = 1L;

  //
  public BigInteger p;
  public BigInteger g;
  public BigInteger subgroupOrder;
  public int zkChallengeSizeBytes = 256 / 8;
  public int zkStatisticalHidingSizeBytes = 80 / 8;
  public int deviceSecretSizeBytes = 256 / 8;
  public int signatureNonceLengthBytes = 128 / 8;
  public int zkNonceSizeBytes = 128 / 8;
  
  SmartcardSystemParameters getSmartcardPublicKey() {
    SmartcardSystemParameters pk = new SmartcardSystemParameters();
    pk.setPrimeModulus(p);
    pk.setGenerator(g);
    pk.setSubgroupOrder(subgroupOrder);
    pk.setZkChallengeSizeBytes(zkChallengeSizeBytes);
    pk.setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
    pk.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
    pk.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
    pk.setZkNonceSizeBytes(zkNonceSizeBytes);
    return pk;
  }
  
  public SystemParameters() {}
  
  public SystemParameters(SmartcardSystemParameters smartcardSystemParameters) {
    p = smartcardSystemParameters.getPrimeModulus();
    g = smartcardSystemParameters.getGenerator();
    subgroupOrder = smartcardSystemParameters.getSubgroupOrder();
    zkChallengeSizeBytes = smartcardSystemParameters.getZkChallengeSizeBytes();
    zkStatisticalHidingSizeBytes = smartcardSystemParameters.getZkStatisticalHidingSizeBytes();
    deviceSecretSizeBytes = smartcardSystemParameters.getDeviceSecretSizeBytes();
    signatureNonceLengthBytes = smartcardSystemParameters.getSignatureNonceLengthBytes();
    zkNonceSizeBytes = smartcardSystemParameters.getZkNonceSizeBytes();
  }
}

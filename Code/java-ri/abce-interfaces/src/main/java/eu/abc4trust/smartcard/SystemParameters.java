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
 * @author enr
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

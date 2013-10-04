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

/**
 * A verification key for RSA.
 * n = p * q
 * @author enr
 *
 */
public class RSAVerificationKey implements Serializable {
  private static final long serialVersionUID = 1L;
  // If you update this structure, don't forget to change also in eu.abc4trust.smartcard.Utils;
  public BigInteger n;
  // The size of the modulus in bytes (actually the modulus p*q must be strictly larger than
  // 2^(8*sizeModulusBytes);
  public int sizeModulusBytes = 1248 / 8;
}

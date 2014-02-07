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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Specifies the first two bases used for CL-credentials as well as the modulus.
 * A CL-credential is of the form:
 * 
 * Z = S^v * R0^m0 * R1^m1 * ... * Rn^mn * A^e (mod n)
 * @author enr
 *
 */
public class CredentialBases implements Serializable, GroupParameters {
  // If updated - change serialVersionUID
  private static final long serialVersionUID = 1L;

  //
  // If you update this structure, don't forget to change also in eu.abc4trust.smartcard.Utils;
  public final BigInteger R0;
  public final BigInteger S;
  public final BigInteger n;
  
  public CredentialBases(BigInteger r0, BigInteger s, BigInteger n) {
    this.R0 = r0;
    this.S = s;
    this.n = n;
  }
  
  @Override
  public boolean isIdemixGroupParameters(){
	  return true;
  }

  @Override
  public BigInteger getModulus() {
	  return n;
  }
  
}

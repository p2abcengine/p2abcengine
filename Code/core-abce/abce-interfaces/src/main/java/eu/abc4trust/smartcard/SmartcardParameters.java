//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

public class SmartcardParameters implements Serializable {
  
  private static final long serialVersionUID = 8278410166997142242L;

  private final BigInteger modulus;
  
  /* Nullable */
  private final BigInteger subgroupOrder;
  
  private final BigInteger base1;
  
  /* Nullable */
  private final BigInteger base2;
  
  public SmartcardParameters(BigInteger modulus, BigInteger subgroupOrder, BigInteger base1, BigInteger base2) {
    this.modulus = modulus;
    this.subgroupOrder = subgroupOrder;
    this.base1 = base1;
    this.base2 = base2;
  }
  
  public BigInteger getModulus() {
    return modulus;
  }
  
  /*Nullable*/
  public BigInteger getOrderOrNull() {
    return subgroupOrder;
  }
  
  /*Nullable*/
  public BigInteger getCofactorOrNull() {
    if(subgroupOrder == null) {
      return null;
    }
    // Cofactor = (p-1)/q
    return modulus.subtract(BigInteger.ONE).divide(subgroupOrder);
  }
  
  public BigInteger getBaseForDeviceSecret() {
    return base1;
  }
  
  /*Nullable*/
  public BigInteger getBaseForCredentialSecretOrNull() {
    return base2;
  }
  
  public static SmartcardParameters forOneBaseCl(BigInteger n, BigInteger R0) {
    return new SmartcardParameters(n, null, R0, null);
  }
  
  public static SmartcardParameters forTwoBaseCl(BigInteger n, BigInteger R0, BigInteger S) {
    return new SmartcardParameters(n, null, R0, S);
  }
  
  public static SmartcardParameters forOneBaseUProve(BigInteger p, BigInteger q, BigInteger gD) {
    return new SmartcardParameters(p, q, gD, null);
  }
  
  public static SmartcardParameters forTwoBaseUProve(BigInteger p, BigInteger q, BigInteger gD, BigInteger gR) {
    return new SmartcardParameters(p, q, gD, gR);
  }
}

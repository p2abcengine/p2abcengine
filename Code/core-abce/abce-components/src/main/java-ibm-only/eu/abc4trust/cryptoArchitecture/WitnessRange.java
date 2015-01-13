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

package eu.abc4trust.cryptoArchitecture;

import java.math.BigInteger;

abstract public class WitnessRange {
  
  static WitnessRange newRangeModulo(BigInteger max) {
    return new RangeModulo(max);
  }
  static WitnessRange newRangeInteger(BigInteger min, BigInteger max) {
    return new IntegerRange(min, max);
  }
  static WitnessRange newRangeInteger(BigInteger max) {
    return new IntegerRange(max);
  }
  static WitnessRange combine(WitnessRange lhs, WitnessRange rhs) {
    if (lhs instanceof RangeModulo && rhs instanceof RangeModulo &&
        lhs.getWitnessMax().equals(rhs.getWitnessMax())) {
      // If both are modulo the same value, then the combined range is the same
      return lhs;
    } else {
      return newRangeInteger(
       lhs.getWitnessMin().min(rhs.getWitnessMin()),
       lhs.getWitnessMax().max(rhs.getWitnessMax())
      );
    }
  }
  
  public abstract BigInteger getWitnessMin();
  // max is exclusive
  public abstract BigInteger getWitnessMax();
  public abstract BigInteger getRandomizerMin();
  public abstract BigInteger getRandomizerMax();
  public abstract BigInteger getVerificationIntervalMin();
  public abstract BigInteger getVerificationIntervalMax();
}

class RangeModulo extends WitnessRange {
  private final BigInteger max;
  RangeModulo(BigInteger max) {
    this.max = max;
  }
  public BigInteger getWitnessMin() {
    return BigInteger.ZERO;
  }
  public BigInteger getWitnessMax() {
    return max;
  }
  public BigInteger getRandomizerMin() {
    return getWitnessMin();
  }
  public BigInteger getRandomizerMax() {
    return getWitnessMax();
  }
  public BigInteger getVerificationIntervalMin() {
    return getWitnessMin();
  }
  public BigInteger getVerificationIntervalMax() {
    return getWitnessMax();
  }
}

class IntegerRange extends WitnessRange {
  private final BigInteger min;
  private final BigInteger max;
  private static final int challengeSizeBits = 256; // TODO parametrize this
  private static final int statisticalSizeBits = 80; // TODO parametrize this
  IntegerRange(BigInteger max) {
    this.min = BigInteger.ZERO;
    this.max = max;
  }
  IntegerRange(BigInteger min, BigInteger max) {
    this.min = min;
    this.max = max;
  }
  public BigInteger getWitnessMin() {
    return min;
  }
  public BigInteger getWitnessMax() {
    return max;
  }
  public BigInteger getRandomizerMin() {
    return BigInteger.ZERO;
  }
  public BigInteger getRandomizerMax() {
    return max.multiply(BigInteger.valueOf(2).pow(challengeSizeBits+statisticalSizeBits));
  }
  public BigInteger getVerificationIntervalMin() {
    return getRandomizerMin().subtract(getChallengeMax().multiply(getWitnessMax()));
  }
  public BigInteger getVerificationIntervalMax() {
    return getRandomizerMax().subtract(getChallengeMin().multiply(getWitnessMin()));
  }
  private BigInteger getChallengeMin() {
    return BigInteger.ZERO;
  }
  private BigInteger getChallengeMax() {
    return BigInteger.valueOf(2).pow(challengeSizeBits);
  }
}





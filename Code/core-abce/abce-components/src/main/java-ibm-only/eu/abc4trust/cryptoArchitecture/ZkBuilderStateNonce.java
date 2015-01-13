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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class ZkBuilderStateNonce {
  private final ZkBuilder builder;
  ZkBuilderStateNonce(ZkBuilder builder) {
    this.builder = builder;
  }

  /**
   * Open the commitment made to a nonce.
   * Typically, one would call getNonceCommitments() before this method, and in the next round
   * check that all the openings are correct.
   * The commitment satisfies  commitment = HASH(nonce, opening)
   * The builder will check that this relation holds.
   * @param commitment
   * @param nonce
   * @param opening
   */
  void openNonceCommitment(byte[] commitment, byte[] nonce, byte[] opening) {
    builder.openNonceCommitment(commitment, nonce, opening);
  }
  /**
   * Returns a list of all nonce commitment used in this proof.
   * @return
   */
  List<byte[]> getNonceCommitments() {
    return builder.getNonceCommitments();
  }
  
  /**
   * Returns the part of the challenge that depends on common values and T-values,
   * excluding the nonces.
   */
  byte[] getChallengeWithoutNonce() {
    return builder.getChallengeWithoutNonce();
  }
  
  /**
   * Returns true if the given witness is revealed.
   * @param witnessName
   * @return
   */
  boolean isRevealedWitness(String witnessName) {
    return builder.isRevealedWitness(witnessName);
  }
  /**
   * Returns the value of the given witness if it is a revealed witness,
   * or null otherwise.
   * @param witnessName
   * @return
   */
  BigInteger getRevealedWitness(String witnessName) {
    return builder.getRevealedWitness(witnessName);
  }
  
  /**
   * Returns the range a given witness can take.
   * @param witnessName
   * @return
   */
  WitnessRange getRangeForWitness(String witnessName) {
    return builder.getRangeForWitness(witnessName);
  }
  
  /**
   * Returns the R-value of a given managed witness.
   * @param witnessName
   * @return
   */
  BigInteger getRandomizer(String witnessName) {
    return builder.getRandomizer(witnessName);
  }
  /**
   * Returns the given common value, assuming it was an integer common value.
   * @param commonValue
   * @return
   */
  BigInteger getCommonValueAsInteger(String commonValue) {
    return builder.getCommonValueAsInteger(commonValue);
  }
  /**
   * Returns the given common value, assuming it was NOT an integer common value.
   * @param commonValue
   * @return
   */
  Serializable getCommonValueAsObject(String commonValue) {
    return builder.getCommonValueAsObject(commonValue);
  }
}

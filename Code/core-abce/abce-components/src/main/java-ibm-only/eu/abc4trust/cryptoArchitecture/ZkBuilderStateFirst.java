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

public class ZkBuilderStateFirst {
  
  private final ZkBuilder builder;
  
  ZkBuilderStateFirst(ZkBuilder builder) {
    this.builder = builder;
  }
  
  /**
   * Add a common value value to the proof.
   * Common values are sent to the verifier, and contribute to the challenge, but otherwise
   * have no special meaning to the proof.
   * A typical use case of a common value is so that the verifier can reconstruct the exact
   * statement proven by the prover (for example for commitments).
   * If the common value is not an integer, you should call addCommonValueAsObject instead.
   * @param cvName
   * @param value
   */
  void addCommonValueAsInteger(String cvName, BigInteger value) {
    builder.addCommonValueAsInteger(cvName, value);
  }
  /**
   * Add a common value value to the proof.
   * Common values are sent to the verifier, and contribute to the challenge, but otherwise
   * have no special meaning to the proof.
   * A typical use case of a common value is so that the verifier can reconstruct the exact
   * statement proven by the prover (for example for commitments).
   * If the common value is an integer, you should call addCommonValueAsInteger instead.
   * @param cvName
   * @param value
   * @param hashContribution This value will be used when computing the hash contribution of this
   * value for the challenge. You may set this to null if you do not want a contribution from
   * this object.
   */
  void addCommonValueAsObject(String cvName, Serializable value,
                              /*Nullable*/ byte[] hashContribution) {
    builder.addCommonValueAsObject(cvName, value, hashContribution);
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
   * Add a commitment to a nonce (for computing the challenge, useful typically for smartcards).
   * This method will disregard multiple calls to this method with the same value of commitment
   * (to make sure the nonce doesn't get XOR'ed away).
   * A module which calls this method is responsible for calling openNonceCommitment() in a
   * subsequent round.
   * @param commitment
   */
  void addNonceCommitment(byte[] commitment) {
    builder.addNonceCommitment(commitment);
  }
  /**
   * Add a T-value (also called the zero-knowledge "commitment", i.e., the result of the
   * first round of a Sigma-protocol).
   * @param key  The key should contain the module name
   * @param value
   */
  void addTValue(String key, BigInteger tValue) {
    builder.addTValue(key, tValue);
  }
  /**
   * Tell the builder the value of a witness, so that the module may compute the
   * S-value automatically. You may call this method in the second round as well.
   * @param witnessName
   * @param value
   */
  void addUnrevealedWitnessValue(String witnessName, BigInteger value) {
    builder.addUnrevealedWitnessValue(witnessName, value);
  }
}

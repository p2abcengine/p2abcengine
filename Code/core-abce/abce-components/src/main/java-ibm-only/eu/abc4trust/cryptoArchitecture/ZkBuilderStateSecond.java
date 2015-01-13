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

public class ZkBuilderStateSecond {
  private final ZkBuilder builder;
  ZkBuilderStateSecond(ZkBuilder builder) {
    this.builder = builder;
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
   * It is the responsibility of the caller to call setHashContributionOfCommonValue with
   * a sensible value to ensure that the prover provided a correct hashContribution (this
   * step is optional if the prover set the hashContribution to null).
   * @param commonValue
   * @return
   */
  Serializable getCommonValueAsObject(String commonValue) {
    return builder.getCommonValueAsObject(commonValue);
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
   * Get the nonce (committed value) from a nonce commitment.
   * @param commitment
   * @return
   */
  byte[] getNonceFromCommitment(byte[] commitment) {
    return builder.getNonceFromCommitment(commitment);
  }
  /**
   * Get the opening (commitment randomness) from a nonce commitment.
   * @param commitment
   * @return
   */
  byte[] getNonceOpeningFromCommitment(byte[] commitment) {
    return builder.getNonceOpeningFromCommitment(commitment);
  }
  
  /**
   * Returns the challenge used in this zero-knowledge proof.
   * We have that   challengeWithNonce = HASH(challengeWithoutNonce, XOR_nonces (nonce) ).
   * @return
   */
  BigInteger getChallengeWithNonce() {
    return builder.getChallengeWithNonce();
  }
  /**
   * Returns the XOR of all challenge nonces
   */
  byte[] getChallengeNonce() {
    return builder.getChallengeNonce();
  }
  /**
   * Add an S-value (also called the zero-knowledge "opening", i.e., the result of the
   * last round of a Sigma-protocol).
   * For all witnesses we should have:   Svalue = Rvalue - challengeWithNonce * witnessValue.
   * This method must be called for all unmanaged witnesses.
   * For all managed witnesses for which addUnrevealedWitnessValue has been called,
   * the S-value will be computed automatically, but this method
   * may be called nevertheless (in which case the builder will check if the value is the same as
   * the one it computed -- this may go wrong if the witness lives in different groups).
   * @param witnessName
   * @param value
   */
  void addSValue(String witnessName, BigInteger sValue) {
    builder.addSValue(witnessName, sValue);
  }
  /**
   * Tell the builder the value of a witness, so that the module may compute the
   * S-value automatically. You may call this for revealed witnesses.
   * For managed unreaveled witnesses, you should call this method or addSValue().
   * @param witnessName
   * @param value
   */
  void addUnrevealedWitnessValue(String witnessName, BigInteger value) {
    builder.addUnrevealedWitnessValue(witnessName, value);
  }
}

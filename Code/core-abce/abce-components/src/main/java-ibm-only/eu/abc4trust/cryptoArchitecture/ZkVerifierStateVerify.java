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
import java.util.logging.Logger;

public class ZkVerifierStateVerify {
  private final ZkVerifier verifier;
  ZkVerifierStateVerify(ZkVerifier verifier) {
    this.verifier = verifier;
  }

  public Logger getLogger() {
    return verifier.getLogger();
  }
  
  /**
   * Returns the S-value for the given witness.
   * @param witnessName
   * @return
   */
  BigInteger getSvalue(String witnessName) {
    return verifier.getSvalue(witnessName);
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
    return verifier.getCommonValueAsObject(commonValue);
  }
  /**
   * Call this method after getCommonValueAsObject() to make sure that the prover provided
   * a correct value for the hashContribution of the common value.
   * @param commonValue
   * @param hashContribution
   */
  void setHashContributionOfCommonValue(String commonValue, byte[] hashContribution) {
    verifier.setHashContributionOfCommonValue(commonValue, hashContribution);
  }
  
  /**
   * Returns true if the given witness is revealed.
   * @param witnessName
   * @return
   */
  boolean isRevealedWitness(String witnessName) {
    return verifier.isRevealedWitness(witnessName);
  }
  /**
   * Returns the value of the given witness if it is a revealed witness,
   * or null otherwise.
   * @param witnessName
   * @return
   */
  BigInteger getRevealedWitness(String witnessName) {
    return verifier.getRevealedWitness(witnessName);
  }
  
  /**
   * Returns the range a given witness can take.
   * @param witnessName
   * @return
   */
  WitnessRange getRangeForWitness(String witnessName) {
    return verifier.getRangeForWitness(witnessName);
  }
  /**
   * Returns the given common value, assuming it was an integer common value.
   * @param commonValue
   * @return
   */
  BigInteger getCommonValueAsInteger(String commonValue) {
    return verifier.getCommonValueAsInteger(commonValue);
  }
  /**
   * Add a T-value (also called the zero-knowledge "commitment", i.e., the result of the
   * first round of a Sigma-protocol).
   * @param key  The key should contain the module name
   * @param value
   */
  void addTValue(String key, BigInteger tValue) {
    verifier.addTValue(key, tValue);
  }
  /**
   * Returns the part of the challenge that depends on common values and T-values,
   * excluding the nonces.
   */
  byte[] getChallengeWithoutNonce() {
    return verifier.getChallengeWithoutNonce();
  }
  
  /**
   * Returns the XOR of all challenge nonces
   */
  byte[] getChallengeNonce() {
    return verifier.getChallengeNonce();
  }
  
  /**
   * Returns the challenge used in this zero-knowledge proof.
   * We have that   challengeWithNonce = HASH(challengeWithoutNonce, XOR_nonces (nonce) ).
   * @return
   */
  BigInteger getChallengeWithNonce() {
    return verifier.getChallengeWithNonce();
  }
}

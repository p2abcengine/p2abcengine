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
import java.util.Map;
import java.util.logging.Logger;

/**
 * This method is responsible for verifying a zero-knowledge proof,
 * under the instruction of a zkDirector and the zkModules contained inside the zkDirector.
 * A subset of the functionality of this class (in the form of the ZkVerifierState* classes)
 * is made available to the zkModules during the various phases of verification the proof.
 * This is the "builder" in the builder design pattern (Gamma et al.); the director is
 * ZkDirector, and the product is a boolean.
 * @author enr
 */
public class ZkVerifier {

  private final ZkProof proof;
  private final Logger logger;
  
  ZkVerifier(ZkProof proof, Logger logger) {
    this.proof = proof;
    this.logger = logger;
  }
  
  Logger getLogger() {
    return logger;
  }
  
  // -------------------------------------------------------------------------------------
  // Factories than create objects that expose a subset of the functionality of this class
  
  ZkVerifierStateCollect getStateCollect() {
    return new ZkVerifierStateCollect(this);
  }
  ZkVerifierStateVerify getStateVerify() {
    return new ZkVerifierStateVerify(this);
  }
  
  // -------------------------------------------------------------------------------------
  // Methods that are made available to the ZkModules (though the ZkVerifierState* classes)
  
  /**
   * Register a witness that is to be used in the zero-knowledge proof.
   * This method (or registerUnmanagedWitness) may be called for revealed witnesses,
   * and must be called for all unrevealed witnesses.
   * Unrevealed witnesses registered with this method will be managed, in the sense that:
   * 1) a randomizer (R-value) of the correct length will be provided,
   * 2) the range of the witness will be checked during verification,
   * 3) the builder will compute the S-values for you.
   * @param witnessName
   * @param r The range of acceptable values for the witness (the builder will take the maximum
   * of all the ranges provided for a witness).
   */
  void registerManagedWitness(String witnessName, WitnessRange r) {
    // TODO: method stub
  }
  
  /**
   * Register a witness that is to be used in the zero-knowledge proof.
   * This method (or registerManagedWitness) must be called for all unrevealed witnesses.
   * You should not call this for revealed witnesses.
   * A typical use case is for witnesses residing on a smartcard, where the smartcard takes
   * care of computing the R-values.
   * Unrevealed witnesses registered with this method will not be managed, in the sense that:
   * 1) a randomizer (R-value) of the correct length will be not be provided,
   * 2) S-values will have to be computed by the module.
   * However, the range of the witness will be checked during verification.
   * @param witnessName
   * @param r The range of acceptable values for the witness (the builder will take the maximum
   * of all the ranges provided for a witness).
   */
  void registerUnmanagedWitness(String witnessName, WitnessRange r) {
    // TODO: method stub
  }
  
  /**
   * Mark a witness a living in a controlled group (a group with an order known to the prover).
   * Calling this method is optional.
   * If there are any witnesses which 1) live only in controlled groups and 2) are integer witnesses
   * (as opposed to witnesses modulo the group order), the proof will fail.
   * To remedy to this situation, you will need to add integer commitment modules for the
   * corresponding witness to the proof.
   * @param witnessName
   */
  void witnessInControlledGroup(String witnessName) {
    // TODO: method stub
  }
  /**
   * Mark a witness a living in an uncontrolled group (a group with an order unknown to the prover).
   * Calling this method is optional.
   * If there are any witnesses which 1) live only in controlled groups and 2) are integer witnesses
   * (as opposed to witnesses modulo the group order), the proof will fail.
   * To remedy to this situation, you will need to add integer commitment modules for the
   * corresponding witness to the proof.
   * @param witnessName
   */
  void witnessInUncontrolledGroup(String witnessName) {
    // TODO: method stub
  }
  
  /**
   * Assign a value to a witness.
   * This value will be sent to the verifier along with the proof.
   * The verifier may call this method if we wishes to double-check the value of the witness
   * himself.
   * @param witnessName
   * @param value
   */
  void revealWitness(String witnessName, BigInteger value) {
    // TODO: method stub
  }
  
  /**
   * Declare that the two (managed) witnesses have the same value.
   * If one of the witnesses was revealed, then the other one becomes revealed as well
   * automatically.
   * @param witnessName1
   * @param witnessName2
   */
  void witnessesAreEqual(String witnessName1, String witnessName2) {
    // TODO: method stub
  }
  
  /**
   * Declare that (managed) witnesses satisfy a linear relation.
   * If all witnesses on the right-hand size are revealed, then the value on the left-hand size
   * becomes revealed also automatically.
   * It is the responsibility of the caller to ensure that the result witness is still within
   * bounds.
   * The exact relation being proven is:
   * result = constant + sum_keys( nameAndMultiplier[key] * key )
   * @param resultWitnessName
   * @param constant
   * @param nameAndMultiplier
   */
  void linearCombinationOfWitnesses(String resultWitnessName, BigInteger constant,
                                    Map<String, BigInteger> nameAndMultiplier) {
    // TODO: method stub
  }
  
  /**
   * Returns the S-value for the given witness.
   * @param witnessName
   * @return
   */
  BigInteger getSvalue(String witnessName) {
    // TODO: method stub
    return null;
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
    // TODO: method stub
    return null;
  }
  /**
   * Call this method after getCommonValueAsObject() to make sure that the prover provided
   * a correct value for the hashContribution of the common value.
   * @param commonValue
   * @param hashContribution
   */
  void setHashContributionOfCommonValue(String commonValue, byte[] hashContribution) {
    // TODO: method stub
  }
  
  /**
   * Returns true if the given witness is revealed.
   * @param witnessName
   * @return
   */
  boolean isRevealedWitness(String witnessName) {
    // TODO: method stub
    return false;
  }
  /**
   * Returns the value of the given witness if it is a revealed witness,
   * or null otherwise.
   * @param witnessName
   * @return
   */
  BigInteger getRevealedWitness(String witnessName) {
    // TODO: method stub
    return null;
  }
  
  /**
   * Returns the range a given witness can take.
   * @param witnessName
   * @return
   */
  WitnessRange getRangeForWitness(String witnessName) {
    // TODO: method stub
    return null;
  }
  /**
   * Returns the given common value, assuming it was an integer common value.
   * @param commonValue
   * @return
   */
  BigInteger getCommonValueAsInteger(String commonValue) {
    // TODO: method stub
    return null;
  }
  /**
   * Add a T-value (also called the zero-knowledge "commitment", i.e., the result of the
   * first round of a Sigma-protocol).
   * @param key  The key should contain the module name
   * @param value
   */
  void addTValue(String key, BigInteger tValue) {
    // TODO: method stub
  }
  /**
   * Returns the part of the challenge that depends on common values and T-values,
   * excluding the nonces.
   */
  byte[] getChallengeWithoutNonce() {
    // TODO: method stub
    return null;
  }
  
  /**
   * Returns the XOR of all challenge nonces
   */
  byte[] getChallengeNonce() {
    // TODO: method stub
    return null;
  }
  
  /**
   * Returns the challenge used in this zero-knowledge proof.
   * We have that   challengeWithNonce = HASH(challengeWithoutNonce, XOR_nonces (nonce) ).
   * @return
   */
  BigInteger getChallengeWithNonce() {
    // TODO: method stub
    return null;
  }
  
  // -----------------------------------------
  // Methods called directly by the zkDirector
  
  /**
   * Check if we can re-compute the challenge from all the T-values
   * @return
   */
  boolean checkChallenge() {
    // TODO Auto-generated method stub
    return false;
  }
  /**
   * Check if the S-values of the witnesses satisfy the equality/linear relations
   * @return
   */
  boolean performWitnessIntegrityCheck() {
    // TODO Auto-generated method stub
    return false;
  }
}

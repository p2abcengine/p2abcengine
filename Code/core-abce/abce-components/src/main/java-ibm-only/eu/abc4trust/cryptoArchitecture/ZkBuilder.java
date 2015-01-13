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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This method is responsible for building a zero-knowledge proof or a ZK proof description,
 * under the instruction of a zkDirector and the zkModules contained inside the zkDirector.
 * A subset of the functionality of this class (in the form of the ZkBuilderState* classes)
 * is made available to the zkModules during the various phases of building the proof.
 * This is the "builder" in the builder design pattern (Gamma et al.); the director is
 * ZkDirector, and the product is a ZkProof (when building proofs) or a string (when building
 * a description).
 * @author enr
 */
public class ZkBuilder {
  
  ZkBuilder(){};
  
  // -------------------------------------------------------------------------------------
  // Factories than create objects that expose a subset of the functionality of this class
  
  ZkBuilderStateCollect getStateCollect() {
    return new ZkBuilderStateCollect(this);
  }
  ZkBuilderStateFirst getStateFirst() {
    return new ZkBuilderStateFirst(this);
  }
  ZkBuilderStateNonce getStateNonce() {
    return new ZkBuilderStateNonce(this);
  }
  ZkBuilderStateSecond getStateSecond() {
    return new ZkBuilderStateSecond(this);
  }
  ZkBuilderStateDescribe getStateDescribe() {
    return new ZkBuilderStateDescribe(this);
  }
  
  // -------------------------------------------------------------------------------------
  // Methods that are made available to the ZkModules (though the ZkBuilderState* classes)
  
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
   * @param witnessName
   * @param value
   */
  void revealWitness(String witnessName, BigInteger value) {
    // TODO: method stub
  }
  
  /**
   * Add a common value to the proof.
   * Common values are sent to the verifier, and contribute to the challenge, but otherwise
   * have no special meaning to the proof.
   * A typical use case of a common value is so that the verifier can reconstruct the exact
   * statement proven by the prover (for example for commitments).
   * If the common value is not an integer, you should call addCommonValueAsObject instead.
   * @param cvName
   * @param value
   */
  void addCommonValueAsInteger(String cvName, BigInteger value) {
    // TODO: method stub
  }
  /**
   * Add a common value to the proof.
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
   * The builder will honor linear equations that are in "reduced echelon form", but will not
   * implement a full linear equation solver.
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
   * Returns the R-value of a given managed witness.
   * @param witnessName
   * @return
   */
  BigInteger getRandomizer(String witnessName) {
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
   * Returns the given common value, assuming it was NOT an integer common value.
   * @param commonValue
   * @return
   */
  Serializable getCommonValueAsObject(String commonValue) {
    // TODO: method stub
    return null;
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
    // TODO: method stub
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
    // TODO: method stub
  }
  /**
   * Get the nonce (committed value) from a nonce commitment.
   * @param commitment
   * @return
   */
  byte[] getNonceFromCommitment(byte[] commitment) {
    return null;
  }
  /**
   * Get the opening (commitment randomness) from a nonce commitment.
   * @param commitment
   * @return
   */
  byte[] getNonceOpeningFromCommitment(byte[] commitment) {
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
    // TODO: method stub
  }
  
  /**
   * Tell the builder the value of a witness, so that the module may compute the
   * S-value automatically. You may call this for revealed witnesses.
   * For managed unreaveled witnesses, you should call this method or addSValue().
   * @param witnessName
   * @param value
   */
  void addUnrevealedWitnessValue(String witnessName, BigInteger value) {
    // TODO: method stub
  }
  
  /**
   * Returns a list of all nonce commitments used in this proof.
   * @return
   */
  List<byte[]> getNonceCommitments() {
    // TODO: method stub
    return new ArrayList<byte[]>();
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
   * This method generates the product (a zero-knowledge proof) ready to be shipped to the
   * verifier.
   * @return
   */
  ZkProof serializeProof() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Compute the challenge of the zero-knowledge proof, which depends on the challengeWithoutNonce
   * and the XOR of all nonces. 
   */
  void computeFinalChallenge() {
    // TODO Auto-generated method stub
    
  }

  /**
   * Compute the part of the challenge that depends on common values and T-values,
   * excluding the nonces.
   */
  void computeChallengeWithoutNonce() {
    // TODO Auto-generated method stub
    
  }

  /**
   * Check which witnesses are revealed based on the constraints, and generate
   */
  void generateRandomValuesForWitnesses() {
    // TODO Auto-generated method stub
    
  }
}

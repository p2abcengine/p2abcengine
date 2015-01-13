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

/**
 * ZkModules implement a part of a zero-knowledge proof, such as an equation
 * (representation, U-prove token, Camenisch-Lysyanskaya credential, verifiable encryption, etc.),
 * or a set of constraints (witness equality, greater-than proofs, etc.).
 * ZkModules follow the "Composite" design pattern (Gamma et al.): it is expected
 * that for example a greater-than proof makes use of several Representation modules internally.
 * @author enr
 *
 */
abstract public class ZkModule {
  private final String name;
  private static final String SEPARATOR = ":";
  private static final String LPAREN = "{";
  private static final String RPAREN = "}";
  
  /**
   * Constructor for top-level modules.
   * @param name The name should identify an instance of a module, and must be unique in a given
   * proof. The name should not contain colons or brackets.
   * When creating sub-modules, please name them as  concat(parentName, submoduleName).
   */
  public ZkModule(String name) {
    this.name = name;
  }
  
  public final String getName() {
    return name;
  }
  
  // ----------------
  // BUILDING A PROOF
  // ----------------
  /**
   * In this phase, the zkModule should register all of its witnesses to the proofState
   * and announce linear combinations / equality between witnesses, etc.
   * The zkModule must also give a value to all witnesses that are being revealed (by this module)
   * in this phase.
   * If this zkModule is stateful, then the state should be reset when this method is called.
   * @param proofState
   */
  void collectWitnessesForProof(ZkBuilderStateCollect proofState) {
  }
  
  /**
   * In this phase, the zkModule should: 1) add common values to the proofState;
   * 2) compute T-values (commitments) for the Zero-knowledge proof;
   * 3) optionally add a commitment to a zk-nonce.
   * When this method is being called, the module must check if any of its
   * witnesses are revealed (by another module).
   * The proofState provides the randomness (R-values) for all managed witnesses.
   * This method is called after collectWitnessesForProof.
   * @param proofState
   */
  void firstRound(ZkBuilderStateFirst proofState) {
  }
  
  /**
   * In this phase, the zkModule can get a list of all zk-nonce commitments, and
   * should open the commitments to all zk-nonces it created.
   * This method is called after firstRound.
   * @param proofState
   */
  void openZkNonce(ZkBuilderStateNonce proofState) {
  }
  
  /**
   * In this phase, the zkModule should provide the S-values (response) for all (unrevealed and
   * unmanaged)
   * witnesses it registered.
   * The zkModule can retrieve the opening of all zk-nonce commitments, and the challenge1 if it
   * wishes the check the finalChallenge.
   * This method is called after openZkNonce, and is the last method called.
   * @param proofState
   */
  void secondRound(ZkBuilderStateSecond proofState) {
  }
  
  // -----------------
  // VERIFYING A PROOF
  // -----------------
  /**
   * In this phase, the zkModule should register all of its witnesses to the proofState
   * and announce linear combinations / equality between witnesses, etc.
   * The zkModule may also give a value to witnesses that are being revealed (by this module)
   * in this phase, if so, the zkVerifier will check that the value is the same as in the proof.
   * If this zkModule is stateful, then the state should be reset when this method is called.
   * Note that for some zkModules, it makes sense to use the same logic inside this function and
   * collectWitnessesForProof.
   * @param proofState
   */
  void collectWitnessesForVerify(ZkVerifierStateCollect proofState) {
  }
  
  /**
   * In this phase, the zkModule should re-compute the T-values of all equations.
   * This module may also wish to perform extra checks on the common values (for example for
   * U-Prove, where a common value might be a signature to be checked by the verifier). If the
   * common values are not integers, this method is responsible for checking that the
   * hashContribution of the common value is correct.
   * This method is called after collectWitnessesForVerify.
   * @param proofState
   * @return
   */
  boolean verify(ZkVerifierStateVerify proofState) {
    return false; 
  }
  
  // ------------------
  // DESCRIBING A PROOF
  // ------------------
  /**
   * Generate a description of this module's contribution to the proof.
   * This description is intended to be displayed to the user.
   * This method is called after collectWitnessesForProof.
   * 
   * @param proofState
   * @return
   */
  String describeProof(ZkBuilderStateDescribe proofState) {
    return "(No description)";
  }
  
  // --------------
  // HELPER METHODS
  // --------------
  /**
   * Helper method to concatenate parent zkModule names with children zkModule name,
   * or concatenate the zkModule name with a local witness name.
   * @param lhs
   * @param rhs
   * @return
   */
  public static final String concat(String lhs, String rhs) {
    return LPAREN + lhs + SEPARATOR + rhs + RPAREN;
  }
}

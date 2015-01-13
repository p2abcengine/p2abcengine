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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is directing the build process of a zero-knowledge (ZK) proof, the build process of
 * a ZK proof description, or the verification process of a ZK proof.
 * This is the "director" in the Builder design pattern (Gamma et al.).
 * When building ZK proofs, the builder is ZkBuilder and the product is a ZkProof.
 * When building proof descriptions, the builder is also ZkBuilder and the product is a string.
 * When verifying Zk proofs, the builder is ZkVerifier and the product is a boolean (and some
 * error messages in the logger).
 * @author enr
 */
public class ZkDirector {
  
  List<ZkModule> modules;
  
  public ZkDirector() {
    this.modules = new ArrayList<ZkModule>();
  }
  
  public void addModule(ZkModule module) {
    modules.add(module);
  }
  
  /**
   * Builds a non-interactive ZK proof based on the modules that have been added.
   * @return
   */
   public ZkProof buildProof() {
    ZkBuilder state = new ZkBuilder();
    
    // 1 - collect all witnesses  (this should reset the state of the modules)
    for(ZkModule module: modules) {
      module.collectWitnessesForProof(state.getStateCollect());
    }
    // 2 - generate random value for all (unrevealed) witnesses
    state.generateRandomValuesForWitnesses();
    // 3 - do the first round for all modules (collect T-values, common values, nonce commitments)
    for(ZkModule module: modules) {
      module.firstRound(state.getStateFirst());
    }
    // 4 - compute challenge1 (based on common values, T-values, revealed values, etc)
    state.computeChallengeWithoutNonce();
    // 5 - ask all modules to open the nonce commitments
    for(ZkModule module: modules) {
      module.openZkNonce(state.getStateNonce());
    }
    // 6 - compute the real challenge (based on XOR of all nonces, and challenge1)
    state.computeFinalChallenge();
    // 7 - do the second round for all modules (collect S-values)
    for(ZkModule module: modules) {
      module.secondRound(state.getStateSecond());
    }
    // 8 - serialize proof and return
    ZkProof product = state.serializeProof();
    return product;
  }
  
   /**
    * Builds a description of a ZK proof based on the modules that have been added.
    * @return
    */
  public String describeProof() {
    ZkBuilder state = new ZkBuilder();
    // 1 - collect all witnesses  (this should reset the state of the modules)
    for(ZkModule module: modules) {
      module.collectWitnessesForProof(state.getStateCollect());
    }
    StringBuilder sb = new StringBuilder();
    // 2 - ask for a description of the proof from each module
    for(ZkModule module: modules) {
      sb.append(module.describeProof(state.getStateDescribe()));
      sb.append("\n");
    }
    return sb.toString();
  }
  
  /**
   * Verifies a ZK proof based on the modules that have been added.
   * @param proof The proof to be verified
   * @param errorLogger The logger where error / info messages will be logged.
   * @return
   */
  public boolean verifyProof(ZkProof proof, Logger logger) {
    ZkVerifier state = new ZkVerifier(proof, logger);
    boolean ok = true;
    // 1 - collect all witnesses  (this should reset the state of the modules)
    for(ZkModule module: modules) {
      module.collectWitnessesForVerify(state.getStateCollect());
    }
    // 2 - integrity check of all witnesses
    ok = state.performWitnessIntegrityCheck();
    if (!ok) {
      logger.severe("Witness integrity check failed");
      return false;
    }
    // 3 - ask all modules to check their part of the proof and supply the re-computed T-values
    for(ZkModule module: modules) {
      ok = module.verify(state.getStateVerify());
      if(!ok) {
        logger.severe("Verify() failed on module " + module.getName());
        return false;
      }
    }
    // 4 - Finally check that the challenge was correctly computed
    ok = state.checkChallenge();
    if(!ok) {
      logger.severe("Challenge check failed.");
      return false;
    }
    return ok;
  }
  
  /**
   * Verifies a ZK proof based on the modules that have been added.
   * Logs all errors to the class's logger.
   */
  public boolean verifyProof(ZkProof proof) {
    return verifyProof(proof, Logger.getLogger(this.getClass().getName()));
  }
}

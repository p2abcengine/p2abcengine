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
import java.util.Map;

public interface ZkStateCollect {

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
  void registerManagedWitness(String witnessName, WitnessRange r);
  
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
  void registerUnmanagedWitness(String witnessName, WitnessRange r);
  /**
   * Mark a witness a living in a controlled group (a group with an order known to the prover).
   * Calling this method is optional.
   * If there are any witnesses which 1) live only in controlled groups and 2) are integer witnesses
   * (as opposed to witnesses modulo the group order), the proof will fail.
   * To remedy to this situation, you will need to add integer commitment modules for the
   * corresponding witness to the proof.
   * @param witnessName
   */
  void witnessInControlledGroup(String witnessName);
  /**
   * Mark a witness a living in an uncontrolled group (a group with an order unknown to the prover).
   * Calling this method is optional.
   * If there are any witnesses which 1) live only in controlled groups and 2) are integer witnesses
   * (as opposed to witnesses modulo the group order), the proof will fail.
   * To remedy to this situation, you will need to add integer commitment modules for the
   * corresponding witness to the proof.
   * @param witnessName
   */
  void witnessInUncontrolledGroup(String witnessName);
  
  /**
   * Assign a value to a witness.
   * This value will be sent to the verifier along with the proof.
   * The verifier may call this method if we wishes to double-check the value of the witness
   * himself.
   * @param witnessName
   * @param value
   */
  void revealWitness(String witnessName, BigInteger value);
  /**
   * Declare that the two (managed) witnesses have the same value.
   * If one of the witnesses was revealed, then the other one becomes revealed as well
   * automatically.
   * @param witnessName1
   * @param witnessName2
   */
  void witnessesAreEqual(String witnessName1, String witnessName2);
  
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
                                    Map<String, BigInteger> nameAndMultiplier);
}

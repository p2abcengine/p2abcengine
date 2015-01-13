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

public class ZkBuilderStateCollect implements ZkStateCollect {
  private final ZkBuilder builder;
  
  ZkBuilderStateCollect(ZkBuilder builder) {
    this.builder = builder;
  }

  @Override
  public void registerManagedWitness(String witnessName, WitnessRange r) {
    builder.registerManagedWitness(witnessName, r);
  }

  @Override
  public void registerUnmanagedWitness(String witnessName, WitnessRange r) {
    builder.registerUnmanagedWitness(witnessName, r);
  }

  @Override
  public void witnessInControlledGroup(String witnessName) {
    builder.witnessInControlledGroup(witnessName);
  }

  @Override
  public void witnessInUncontrolledGroup(String witnessName) {
    builder.witnessInUncontrolledGroup(witnessName);
  }

  @Override
  public void revealWitness(String witnessName, BigInteger value) {
    builder.revealWitness(witnessName, value);
  }

  @Override
  public void witnessesAreEqual(String witnessName1, String witnessName2) {
    builder.witnessesAreEqual(witnessName1, witnessName2);
  }

  @Override
  public void linearCombinationOfWitnesses(String resultWitnessName, BigInteger constant,
      Map<String, BigInteger> nameAndMultiplier) {
    builder.linearCombinationOfWitnesses(resultWitnessName, constant, nameAndMultiplier);
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

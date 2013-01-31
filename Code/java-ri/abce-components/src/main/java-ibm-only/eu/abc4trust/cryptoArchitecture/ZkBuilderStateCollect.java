//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

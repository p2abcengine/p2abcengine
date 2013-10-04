//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoArchitecture;

import java.math.BigInteger;
import java.util.Map;
import java.util.logging.Logger;

public class ZkVerifierStateCollect implements ZkStateCollect {
  private final ZkVerifier verifier;
  
  ZkVerifierStateCollect(ZkVerifier verifier) {
    this.verifier = verifier;
  }
  
  @Override
  public void registerManagedWitness(String witnessName, WitnessRange r) {
    verifier.registerManagedWitness(witnessName, r);
  }

  @Override
  public void registerUnmanagedWitness(String witnessName, WitnessRange r) {
    verifier.registerUnmanagedWitness(witnessName, r);
  }

  @Override
  public void witnessInControlledGroup(String witnessName) {
    verifier.witnessInControlledGroup(witnessName);
  }

  @Override
  public void witnessInUncontrolledGroup(String witnessName) {
    verifier.witnessInUncontrolledGroup(witnessName);
  }

  @Override
  public void revealWitness(String witnessName, BigInteger value) {
    verifier.revealWitness(witnessName, value);
  }

  @Override
  public void witnessesAreEqual(String witnessName1, String witnessName2) {
    verifier.witnessesAreEqual(witnessName1, witnessName2);
  }

  @Override
  public void linearCombinationOfWitnesses(String resultWitnessName, BigInteger constant,
      Map<String, BigInteger> nameAndMultiplier) {
    verifier.linearCombinationOfWitnesses(resultWitnessName, constant, nameAndMultiplier);
  }
  
  public Logger getLogger() {
    return verifier.getLogger();
  }
}

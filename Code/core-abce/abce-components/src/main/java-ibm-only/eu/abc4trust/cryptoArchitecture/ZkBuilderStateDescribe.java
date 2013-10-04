//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoArchitecture;

import java.math.BigInteger;

public class ZkBuilderStateDescribe {
  private final ZkBuilder builder;
  
  ZkBuilderStateDescribe(ZkBuilder builder) {
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
}

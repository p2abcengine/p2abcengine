//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.math.BigInteger;
import java.net.URI;

public class ContextGeneratorSequential implements ContextGenerator{
  private int counter;
  
  public ContextGeneratorSequential() {
    counter = 0;
    System.out.println("*** Using sequential ContextGenerator *** DO NOT USE IN PRODUCTION");
  }
  
  @Override
  public URI getUniqueContext(URI prefix) {
    counter++;
    return URI.create(prefix + "/" + counter);
  }

  @Override
  public BigInteger getRandomNumber(long bits) {
    counter++;
    // return 2^bits - counter
    return BigInteger.valueOf(2).pow((int) bits).add(BigInteger.valueOf(-counter));
  }
}

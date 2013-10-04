//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;

public class ContextGeneratorRandom implements ContextGenerator {

  private static final int SIZE_OF_RANDOM_NUMBER_IN_BITS = 80;
  private static final int MAX_SIZE_RANDOM_IN_BITS = 65536;
  
  private final SecureRandom secureRandom;
  
  public ContextGeneratorRandom() {
    secureRandom = new SecureRandom();
  }
  
  @Override
  public URI getUniqueContext(URI prefix) {
    BigInteger randomNumber = new BigInteger(SIZE_OF_RANDOM_NUMBER_IN_BITS, secureRandom);
    return URI.create(prefix + "/" + randomNumber.toString(Character.MAX_RADIX));
  }

  @Override
  public BigInteger getRandomNumber(long bits) {
    if(bits >= 0 && bits <= MAX_SIZE_RANDOM_IN_BITS) {
      return new BigInteger((int)bits, secureRandom);
    } else {
      throw new RuntimeException("Cannot generate a random number of " + bits +
        " bits. Expected between 0 and " + MAX_SIZE_RANDOM_IN_BITS + ".");
    }
  }

}

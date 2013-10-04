//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;


public class PrimeIndexerTest {
  
  @Test
  public void testPrimeSequence() {
    PrimeIndexer pi = new PrimeIndexer();
    assertEquals(pi.getNthPrime(0), BigInteger.valueOf(2));
    assertEquals(pi.getNthPrime(1), BigInteger.valueOf(3));
    assertEquals(pi.getNthPrime(2), BigInteger.valueOf(5));
    assertEquals(pi.getNthPrime(3), BigInteger.valueOf(7));
    assertEquals(pi.getNthPrime(4), BigInteger.valueOf(11));
    assertEquals(pi.getNthPrime(5), BigInteger.valueOf(13));
    assertEquals(pi.getNthPrime(6), BigInteger.valueOf(17));
    assertEquals(pi.getNthPrime(9989), BigInteger.valueOf(104659));
    assertEquals(pi.getNthPrime(39), BigInteger.valueOf(173));
    pi = new PrimeIndexer();
    assertEquals(pi.getNthPrime(39), BigInteger.valueOf(173));
    assertEquals(pi.getIndexOfPrime(BigInteger.valueOf(104729)), Integer.valueOf(9999));
    assertEquals(pi.getIndexOfPrime(BigInteger.valueOf(2)), Integer.valueOf(0));
    assertEquals(pi.getIndexOfPrime(BigInteger.valueOf(3)), Integer.valueOf(1));
    assertEquals(pi.getIndexOfPrime(BigInteger.valueOf(17)), Integer.valueOf(6));
  }
}

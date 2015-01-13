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

package eu.abc4trust.util.attributeEncoding;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.util.attributeTypes.EnumIndexer;

public class PrimeIndexer implements EnumIndexer {
  private List<BigInteger> primes;
  private Map<BigInteger, Integer> primePosition;
  
  public PrimeIndexer() {
    primes = new ArrayList<BigInteger>();
    primePosition = new HashMap<BigInteger, Integer>();
  }
  
  public BigInteger getNthPrime(int n) {
    if (n<0) {
      throw new RuntimeException("getNthPrime argument must be > 0");
    } else if (n < primes.size()) {
      return primes.get(n);
    } else {
      generatePrimesToUp(n);
      return primes.get(n);
    }
  }
  
  public Integer getIndexOfPrime(BigInteger p) {
    while (primes.get(primes.size()-1).compareTo(p) <= 0) {
      generatePrimesToUp(primes.size());
    }
    return primePosition.get(p);
  }
  
  private void addToPrimes(BigInteger p) {
    primePosition.put(p, primes.size());
    primes.add(p);
  }

  private void generatePrimesToUp(int n) {
    if(primes.size() == 0) {
      // We add 2 here so we never have to worry about it again
      addToPrimes(BigInteger.valueOf(2));
      addToPrimes(BigInteger.valueOf(3));
    }
    
    for(BigInteger next = primes.get(primes.size()-1).add(BigInteger.valueOf(2))
        ; primes.size() <= n; next = next.add(BigInteger.valueOf(2)) ) {
      // Test 'next' for primality; invariant: next is odd
      boolean ok = true;
      // Try all known primes up to the square root of 'next'  (skip 2, we know that next is odd)
      for(int i=1; primes.get(i).multiply(primes.get(i)).compareTo(next) <= 0; ++i) {
        if (next.mod(primes.get(i)).equals(BigInteger.ZERO)) {
          ok=false;
          break;
        }
      }
      if(ok) {
        addToPrimes(next);
      }
    }
  }

  @Override
  public BigInteger getRepresentationOfIndex(int index) {
    return getNthPrime(index);
  }

  @Override
  public Integer getIndexFromRepresentation(BigInteger repr) {
    return getIndexOfPrime(repr);
  }
}

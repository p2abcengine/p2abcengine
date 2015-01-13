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

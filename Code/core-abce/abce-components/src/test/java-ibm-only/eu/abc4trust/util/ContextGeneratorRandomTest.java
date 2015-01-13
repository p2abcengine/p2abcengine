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

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ContextGeneratorRandomTest {
  
  ContextGeneratorRandom contextGenerator;
  
  @Before
  public void setup() {
    contextGenerator = new ContextGeneratorRandom();
  }

  @Test
  public void testUniqueness() throws URISyntaxException {
    final int NUMBER_OF_CONTEXTS_TO_GENERATE = 100;
    final URI prefix = new URI("abc4trust.eu/test-context");
    
    Set<URI> contexts = new HashSet<URI>();
    for(int i=0;i<NUMBER_OF_CONTEXTS_TO_GENERATE;++i) {
      URI context = contextGenerator.getUniqueContext(prefix);
      
      // Will not insert duplicates
      contexts.add(context);
    }
    
    assertEquals(NUMBER_OF_CONTEXTS_TO_GENERATE, contexts.size());
  }
  
  @Test
  public void testPrefixPresent() throws URISyntaxException {
    final URI prefix = new URI("abc4trust.eu/test-context");
    
    URI context = contextGenerator.getUniqueContext(prefix);
    
    assertTrue(context.toString().startsWith(prefix.toString()));
  }

}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

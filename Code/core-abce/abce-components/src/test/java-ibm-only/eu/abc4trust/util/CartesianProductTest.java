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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CartesianProductTest {

  @Test
  public void testEqualySizedLists() throws Exception {
    List<String> s1 = new ArrayList<String>();
    s1.add("a");
    s1.add("b");
    List<String> s2 = new ArrayList<String>();
    s2.add("c");
    s2.add("d");
    List<String> s3 = new ArrayList<String>();
    s3.add("e");
    s3.add("f");
    
    List<List<String>> l = new ArrayList<List<String>>();
    l.add(s1);
    l.add(s2);
    l.add(s3);
    
    List<ArrayList<String>> cp = CartesianProduct.cartesianProduct(l);
    
    ////////////////////////////////////////////
    // build the expected result
    List<List<String>> resultSet = new ArrayList<List<String>>();
    List<String> rl = new ArrayList<String>();
    rl.add("a");
    rl.add("c");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("a");
    rl.add("c");
    rl.add("f");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("a");
    rl.add("d");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("a");
    rl.add("d");
    rl.add("f");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("c");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("c");
    rl.add("f");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("d");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("d");
    rl.add("f");
    resultSet.add(rl);
    
    assertEquals(resultSet, cp);
  }
  
  @Test
  public void testUnequalySizedLists() throws Exception {
    List<String> s1 = new ArrayList<String>();
    s1.add("a");
    s1.add("b");
    List<String> s2 = new ArrayList<String>();
    s2.add("c");
    List<String> s3 = new ArrayList<String>();
    s3.add("e");
    s3.add("f");
    
    List<List<String>> l = new ArrayList<List<String>>();
    l.add(s1);
    l.add(s2);
    l.add(s3);
    
    List<ArrayList<String>> cp = CartesianProduct.cartesianProduct(l);
    
    ////////////////////////////////////////////
    // build the expected result
    List<List<String>> resultSet = new ArrayList<List<String>>();
    List<String> rl = new ArrayList<String>();
    rl.add("a");
    rl.add("c");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("a");
    rl.add("c");
    rl.add("f");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("c");
    rl.add("e");
    resultSet.add(rl);
    rl = new ArrayList<String>();
    rl.add("b");
    rl.add("c");
    rl.add("f");
    resultSet.add(rl);
    
    assertEquals(resultSet, cp);
  }
  
  @Test
  public void testEmptyList() throws Exception {
    List<String> s1 = new ArrayList<String>();
    s1.add("a");
    s1.add("b");
    List<String> s2 = new ArrayList<String>();
    s2.add("c");
    s2.add("d");
    List<String> s3 = new ArrayList<String>();
    
    /*
     * s3 is empty, therefore the cartesian product should contain zero elements.
     * (i.e. you would not be able to satisfy the presentation policy in this case)
     */
    
    List<List<String>> l = new ArrayList<List<String>>();
    l.add(s1);
    l.add(s2);
    l.add(s3);
    
    List<ArrayList<String>> cp = CartesianProduct.cartesianProduct(l);
    
    List<List<String>> resultSet = new ArrayList<List<String>>();
    assertEquals(resultSet, cp);
  }
  
  @Test
  public void testEmpty() throws Exception {
    List<List<String>> l = new ArrayList<List<String>>();
    
    /* l is empty, therefore the cartesian product should contain one element: the empty set
     * (this models the fact that if you have no restrictions on the presentation policy
     * you can satisfy the policy with the empty set of credentials).
     */
    
    List<ArrayList<String>> cp = CartesianProduct.cartesianProduct(l);
    
    List<List<String>> resultSet = new ArrayList<List<String>>();
    resultSet.add(new ArrayList<String>());
    assertEquals(resultSet, cp);
  }
  
  @Test
  public void testTooLarge() throws Exception {
    final int PRODUCT_ELEMENTS = 100;
    
    List<List<String>> l = new ArrayList<List<String>>();
    for (int i=0;i<PRODUCT_ELEMENTS;++i) {
      List<String> s1 = new ArrayList<String>();
      s1.add("a");
      s1.add("b");
      l.add(s1);
    }
    
    try {
      CartesianProduct.cartesianProduct(l);
      // Will probably never get here since we will run out of memory
      fail("Expected exception");
    } catch(Exception ex) {
      assertTrue(ex.getMessage().contains(BigInteger.valueOf(2).pow(PRODUCT_ELEMENTS).toString()));
    }
  }

}

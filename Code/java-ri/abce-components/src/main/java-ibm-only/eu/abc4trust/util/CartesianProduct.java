//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartesianProduct {
  /**
   * Returns the n-ary Cartesian product of the given list. The position of an element inside the
   * returned lists is guaranteed to correspond to the position of the set from which the element
   * was taken.
   * 
   * @param <T> the type that is contained in the given sets
   * @param sets the sets that are the basis for the cartesian product. The order of the sets
   *        determine in which order the elements appear in the resulting lists.
   * @return the n-ary Cartesian product of the given sets.
   * @throws Exception in case the resulting list would be larger than 100K elements
   */
  public static <T> List<ArrayList<T>> cartesianProduct(List<List<T>> sets) throws Exception {
    final BigInteger MAX_LIST_SIZE = BigInteger.valueOf(100000);

    BigInteger productSize = BigInteger.ONE;
    for (List<T> set : sets) {
      productSize = productSize.multiply(BigInteger.valueOf(set.size()));
    }

    if (productSize.compareTo(MAX_LIST_SIZE) == 1) {
      throw new Exception("Will not generate carthesian product for more than " + MAX_LIST_SIZE
          + " elements " + "(size would be " + productSize + "). Not proceeding.");
    }

    List<ArrayList<T>> cartesianProduct = new ArrayList<ArrayList<T>>(productSize.intValue());
    for (int i = 0; i < productSize.intValue(); i++) {
      cartesianProduct.add(new ArrayList<T>(sets.size()));
    }
    
    if(productSize.equals(BigInteger.ZERO)) {
      // Empty product
      return cartesianProduct;
    }

    int loopSize = productSize.intValue();
    for (int i = 0; i < sets.size(); i++) {
      List<T> set = sets.get(i);

      loopSize /= set.size();
      Iterator<T> it = set.iterator();
      T e = null;
      for (int j = 0; j < productSize.intValue(); j++) {
        if ((j % loopSize) == 0) {
          if (it.hasNext()) {
            e = it.next();
          } else {
            it = set.iterator();
            e = it.next();
          }
        }

        cartesianProduct.get(j).add(e);
      }
    }

    return cartesianProduct;
  }
}

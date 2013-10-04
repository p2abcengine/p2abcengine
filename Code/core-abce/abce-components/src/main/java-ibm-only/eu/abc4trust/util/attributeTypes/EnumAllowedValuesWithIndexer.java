//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeTypes;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnumAllowedValuesWithIndexer extends EnumAllowedValues {
  private final EnumIndexer indexer;
  
  public EnumAllowedValuesWithIndexer(EnumIndexer indexer, List<String> allowedValues) {
    super(allowedValues);
    this.indexer = indexer;
  }
  
  public Map<String, BigInteger> getEncodingForEachAllowedValue() {
    Map<String, BigInteger> res = new HashMap<String, BigInteger>();
    List<String> list = getAllowedValues();
    for(int i=0;i<list.size();++i) {
      res.put(list.get(i), indexer.getRepresentationOfIndex(i));
    }
    return res;
  }
}

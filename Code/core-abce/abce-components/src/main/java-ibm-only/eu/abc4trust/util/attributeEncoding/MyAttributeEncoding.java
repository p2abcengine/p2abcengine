//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util.attributeEncoding;

import java.math.BigInteger;
import java.net.URI;


public interface MyAttributeEncoding {
  public BigInteger getIntegerValue();
  public URI getEncoding();
  // Java doesn't allow static methods in interfaces...
  //public static Object recoverValueFromIntegerValue(BigInteger integerValue);
}

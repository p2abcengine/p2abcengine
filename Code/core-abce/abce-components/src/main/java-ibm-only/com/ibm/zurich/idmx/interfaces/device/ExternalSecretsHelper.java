//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

// * Licensed Materials - Property of IBM *
// * com.ibm.zurich.idmx.2.3.40 *
// * (C) Copyright IBM Corp. 2013. All Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************
package com.ibm.zurich.idmx.interfaces.device;

import java.math.BigInteger;
import java.net.URI;

//TODO: Remove this file once the new crypto architecture is ready.

/**
 * Helper class for the verifier for operations on external devices.
 * 
 * @author enr
 */
public interface ExternalSecretsHelper {
  /**
   * Returns the base used for scope exclusive pseudonyms for the given scope, modulus and subgroup
   * order. Returns (hash(scope)^cofactor) (mod p), where cofactor = (p-1)/subgroupOrder
   * 
   * @param scope
   * @param modulus The value p
   * @param subgroupOrder
   * @return
   */
  public BigInteger getBaseForScopeExclusivePseudonym(URI scope, BigInteger modulus,
      BigInteger subgroupOrder);
}

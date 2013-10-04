//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.internal;

import eu.abc4trust.xml.PseudonymInToken;

public interface TokenManager {
  /**
   * This method checks whether the given pseudonym p is an established pseudonym, i.e., whether the
   * pseudonym occurs in any stored presentation tokens.
   * 
   * @param p
   * @return
   */
  public boolean isEstablishedPseudonym(PseudonymInToken p);
}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.verifier.tokenManagerVerifier;

import java.net.URI;

import eu.abc4trust.abce.internal.TokenManager;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymInToken;

public interface TokenManagerVerifier extends TokenManager {
  /**
   * This method checks whether the given pseudonym p is an established pseudonym, i.e., whether the
   * pseudonym occurs in any stored presentation tokens.
   * 
   * @param p
   * @return
   */
  public boolean isEstablishedPseudonym(PseudonymInToken p);

  /**
   * This method saves the given presentation token t in permanent storage and assigns a unique
   * identifier to the token by means of which it can later be retrieved. The return value is the
   * unique identifier.
   * 
   * @param t
   * @return
   */
  public URI storeToken(PresentationToken t);
  

  /**
   * This method looks up a previously verified presentation token by the unique identifier
   * tokenuid.
   * 
   * @param tokenuid
   * @return
   */
  public PresentationToken getToken(URI tokenuid);

  /**
   * This method deletes the previously verified presentation token referenced by the unique
   * identifier tokenuid. It returns true in case of successful deletion, and false otherwise.
   * 
   * @param tokenuid
   * @return
   */
  public boolean deleteToken(URI tokenuid);

}

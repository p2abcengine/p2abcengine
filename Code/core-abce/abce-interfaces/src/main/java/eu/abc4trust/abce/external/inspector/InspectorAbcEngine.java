//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.abce.external.inspector;

import java.net.URI;
import java.util.List;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

public interface InspectorAbcEngine {
  /**
   * This method generates a fresh decryption key and corresponding encryption key for the given key
   * length and cryptographic mechanism. It stores the decryption key in the trusted storage and
   * returns the inspector public key with the given identifier uid. The identifier associated with
   * the key will be used in presentation/issuance policies as the unique reference to a particular
   * Inspector.
   * 
   * @param keyLength
   * @param mechanism
   * @param uid
   * @return
   * @throws CredentialManagerException 
   * @throws Exception
   */
  public InspectorPublicKey setupInspectorPublicKey(SystemParameters sp, URI mechanism, URI uid,
      List<FriendlyDescription> friendlyInspectorDescription) throws CryptoEngineException, CredentialManagerException;

  /**
   * This method takes as input a presentation token with inspectable attributes and returns the
   * decrypted attribute type-value pairs for which the Inspector has the inspection secret key.
   * 
   * @param t
   * @return
   * @throws Exception
   */
  public List<Attribute> inspect(PresentationToken t) throws CryptoEngineException;
}

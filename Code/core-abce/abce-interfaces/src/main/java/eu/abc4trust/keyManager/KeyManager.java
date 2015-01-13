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

package eu.abc4trust.keyManager;

import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;

public interface KeyManager {
  /**
   * This method returns the Issuer parameters with the given unique identifier issuid, or returns
   * nothing (null) if no such parameters can be obtained in a trusted way.
   * 
   * @param issuid
   * @return
   */
  public IssuerParameters getIssuerParameters(URI issuid) throws KeyManagerException;
  
  public List<URI> listIssuerParameters() throws KeyManagerException;

  /**
   * This method stores the Issuer parameters with the given unique identifier issuid in a trusted
   * way.
   * 
   * @param issuid
   * @param issuerParameters
   * @return
   */
  boolean storeIssuerParameters(URI issuid, IssuerParameters issuerParameters)
      throws KeyManagerException;

  /**
   * This method returns the Inspector public key with the given unique identifier ipkuid, or
   * returns nothing (null) if no such public key can be obtained in a trusted way.
   * 
   * @param ipkuid
   * @return
   * @throws Exception
   */
  public InspectorPublicKey getInspectorPublicKey(URI ipkuid) throws KeyManagerException;

  /**
   * This method stores the Inspector public key with the given unique identifier ipkuid in a
   * trusted way.
   * 
   * @param ipkuid
   * @return
   */
  public boolean storeInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey)
      throws KeyManagerException;

  /**
   * This method returns the Revocation Authority parameters with the given unique identifier
   * rapuid, or returns nothing (null) if no such parameters can be obtained in a trusted way.
   * 
   * @param rapuid
   * @return
   */
  public RevocationAuthorityParameters getRevocationAuthorityParameters(URI rapuid)
      throws KeyManagerException;

  /**
   * This method returns the revocation information with identifier revinfouid for the given
   * Revocation Authority parameters rapuid. The KeyManager may have the requested revocation
   * information cached, but if not, it looks up the appropriate endpoint in the Revocation
   * Authority parameters and fetches the requested revocation information from the Revocation
   * Authority directly. The requested revocation information revinfouid may not be the latest
   * revocation information. Note the difference with the getCurrentRevocationInformation method,
   * which always returns the latest revocation information.
   * 
   * @param rapuid
   * @param revinfouid
   * @return
   */
  public RevocationInformation getRevocationInformation(URI rapuid, URI revinfouid)
      throws KeyManagerException;

  /**
   * This method returns the current revocation information for the given Revocation Authority
   * parameters rapuid. The KeyManager may have the current revocation information cached, but if
   * not, it looks up the appropriate endpoint in the Revocation Authority parameters and fetches
   * the latest revocation information from the Revocation Authority directly.
   * 
   * @param rapuid
   * @return
   */
  public RevocationInformation getCurrentRevocationInformation(URI rapuid)
      throws KeyManagerException;

  /**
   * This method retrieves and returns the latest revocation information for the given Revocation
   * Authority parameters rapuid.
   * 
   * @param rapuid
   * @return
   */
  public RevocationInformation getLatestRevocationInformation(URI rapuid)
      throws KeyManagerException;

  /**
   * This method stores the revocation information.
   * 
   * @param informationUID
   * @param revocationInformation
   * @return
   * @throws KeyManagerException
   */
  public void storeRevocationInformation(URI informationUID,
      RevocationInformation revocationInformation) throws KeyManagerException;

  /**
   * @param revocationInformationId
   * @param delegateeElement
   * @throws KeyManagerException
   */
  public void storeCurrentRevocationInformation(RevocationInformation delegateeElement)
      throws KeyManagerException;

  /**
   * This method stores the Revocation Authority parameters with the given unique identifier ipkuid
   * in a trusted way.
   * 
   * @param ipkuid
   * @return
   */
  boolean storeRevocationAuthorityParameters(URI issuid,
      RevocationAuthorityParameters revocationAuthorityParameters) throws KeyManagerException;

  /**
   * This method returns the Credential Specification with the given unique identifier credspecuid,
   * or returns nothing (null) if no specification can be obtained in a trusted way.
   * 
   * @param credspec
   * @return
   * @throws KeyManagerException
   */
  CredentialSpecification getCredentialSpecification(URI credspec) throws KeyManagerException;

  /**
   * This method stores the Credential specification with the given unique identifier uid in a
   * trusted way.
   * 
   * @param uid
   * @param credentialSpecification
   * @return
   * @throws KeyManagerException
   */
  boolean storeCredentialSpecification(URI uid, CredentialSpecification credentialSpecification)
      throws KeyManagerException;

  /**
   * This method stores the system parameters. There is only one set of valid system parameters at
   * any given time for a given incarnation of the ABCE.
   * 
   * @param systemParameters
   * @return
   * @throws KeyManagerException
   */
  public boolean storeSystemParameters(SystemParameters systemParameters)
      throws KeyManagerException;

  /**
   * This method returns the system parameters. There is only one set of valid system parameters at
   * any given time for a given incarnation of the ABCE.
   * 
   * @throws KeyManagerException
   * 
   */
  public SystemParameters getSystemParameters() throws KeyManagerException;

  /**
   * This method returns true if the key manager has system parameters stored.
   * 
   * @throws KeyManagerException
   * 
   */
  public boolean hasSystemParameters() throws KeyManagerException;
}

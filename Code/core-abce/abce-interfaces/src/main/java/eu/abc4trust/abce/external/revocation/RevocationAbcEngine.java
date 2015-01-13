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

package eu.abc4trust.abce.external.revocation;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

public interface RevocationAbcEngine {
    /**
     * For a given key length and revocation mechanism, this method generates a
     * fresh secret key for the Revocation Authority and corresponding public
     * Revocation Authority parameters, as well as the initial revocation
     * information. The secret key is stored in trusted storage. Also included
     * in the returned Revocation Authority parameters are the given identifier
     * uid as well as the endpoints where Users,Verifiers and Issuers can obtain
     * the latest revocation information (revocationInfoReference), initial
     * non-revocation evidence (nonRevocationEvidenceReference), and updates to
     * their non-revocation evidence (nonRevocationUpdateReference).
     * 
     * @param keyLength
     * @param cryptographicMechanism
     * @param uid
     * @param infoRef
     * @param evidenceRef
     * @param updateRef
     * @return
     * @throws CryptoEngineException
     */
    public RevocationAuthorityParameters setupRevocationAuthorityParameters(
            int keyLength,
            URI cryptographicMechanism,
            URI uid, Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference)
                    throws CryptoEngineException;

    /**
     * This method revokes the attribute values specified by the input parameter
     * atts with respect to the revocation parameters specified by their unique
     * identifier rparsuid. When atts contains multiple attribute type-value
     * pairs, then the combination of these attribute values is revoked, i.e.,
     * all credentials that have the combination of attribute values specified
     * in atts are revoked. In the special case of Issuer-driven revocation,
     * atts contains one attribute value that is the revocation handle, so that
     * only the unique credential with that revocation handle has been revoked.
     * 
     * @param revParUid
     * @param attributes
     * @return
     * @throws CryptoEngineException
     */
    public RevocationInformation revoke(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException;

    /**
     * 
     * @param revParUid
     * @param attributes
     * @return
     * @throws CryptoEngineException
     */
    public NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException;

    /**
     * 
     * @param revAuthParamsUid
     * @param epoch
     * @return
     * @throws CryptoEngineException
     */
    public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(
            URI revAuthParamsUid, int epoch) throws CryptoEngineException;

    /**
     * This method returns the current revocation information.
     * 
     * 
     * @param revAuthParamsUid
     * @return
     * @throws CryptoEngineException
     */
    public RevocationInformation updateRevocationInformation(
            URI revAuthParamsUid)
                    throws CryptoEngineException;

    /**
     * This method returns the revocation information associated with the given
     * parameters.
     * 
     * 
     * @param revParUid
     * @param revInfoUid
     * @return
     * @throws CryptoEngineException
     */
    public RevocationInformation getRevocationInformation(URI revParamsUid,
            URI revInfoUid) throws CryptoEngineException;

}

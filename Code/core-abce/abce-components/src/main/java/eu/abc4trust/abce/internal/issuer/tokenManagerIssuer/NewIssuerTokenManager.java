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
package eu.abc4trust.abce.internal.issuer.tokenManagerIssuer;

import java.net.URI;
import java.util.UUID;

import com.google.inject.Inject;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.db.TokenTypes;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.PseudonymInToken;

public class NewIssuerTokenManager implements TokenManagerIssuer {

  private final PersistentStorage ps;

  @Inject
  public NewIssuerTokenManager(PersistentStorage ps) {
    this.ps = ps;
  }

  @Override
  public boolean isEstablishedPseudonym(PseudonymInToken p) {
    return ps.isPseudonymInToken(TokenTypes.ISSUANCE_TOKEN, p.getPseudonymValue());
  }

  @Override
  public URI storeToken(IssuanceToken it) {
    URI tokenuid = URI.create("itoken-" + UUID.randomUUID().toString());
    boolean result =
        ps.insertItem(SimpleParamTypes.ISSUANCE_TOKEN, tokenuid, ByteSerializer.writeAsBytes(it));
    if (!result) {
      return null;
    }

    for (PseudonymInToken p : it.getIssuanceTokenDescription().getPresentationTokenDescription()
        .getPseudonym()) {
      result = ps.associatePseudonym(TokenTypes.ISSUANCE_TOKEN, tokenuid, p.getPseudonymValue());
      if (!result) {
        return null;
      }
    }
    return result ? tokenuid : null;
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(URI issuanceDataUid) {
    return (IssuanceLogEntry) ByteSerializer.readFromBytes(ps.getItem(
      SimpleParamTypes.ISSUANCE_LOG_ENTRY, issuanceDataUid));
  }

  @Override
  public boolean deleteIssuanceLogEntry(URI issuanceDataUid) {
    boolean result = deleteToken(issuanceDataUid);
    if(!result) {
      return false;
    }
    return ps.deleteItem(SimpleParamTypes.ISSUANCE_LOG_ENTRY, issuanceDataUid);
  }

  @Override
  public URI storeIssuanceLogEntry(IssuanceLogEntry issuanceLogEntry) {
    URI tokenId = storeToken(issuanceLogEntry.getIssuanceToken());
    
    boolean result =
        ps.insertItem(SimpleParamTypes.ISSUANCE_LOG_ENTRY, tokenId, ByteSerializer.writeAsBytes(issuanceLogEntry));
    
    return result ? tokenId : null;
  }

  @Override
  @Deprecated
  public IssuanceToken getToken(URI tokenuid) {
    return (IssuanceToken) ByteSerializer.readFromBytes(ps.getItem(
      SimpleParamTypes.ISSUANCE_TOKEN, tokenuid));
  }

  @Override
  @Deprecated
  public boolean deleteToken(URI tokenuid) {
    return ps.deleteItem(SimpleParamTypes.ISSUANCE_TOKEN, tokenuid);
  }
  
  // Visible for test
  @Override
  public void addPeudonymForTest(byte[] pseudonymValue) {
    URI tokenuid = URI.create("itoken-" + UUID.randomUUID().toString());
    boolean result =
        ps.insertItem(SimpleParamTypes.ISSUANCE_TOKEN, tokenuid, new byte[0]);
    if (!result) {
      throw new RuntimeException("Could not add dummy issuance token");
    }

    result = ps.associatePseudonym(TokenTypes.ISSUANCE_TOKEN, tokenuid, pseudonymValue);
    if (!result) {
      throw new RuntimeException("Could not add pseudonym to dummy issuance token");
    }
  }

}

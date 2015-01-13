//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.abce.internal.issuer.issuanceManager;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineReIssuer;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

public class IssuanceManagerIssuerImpl implements IssuanceManagerIssuer {

  private final CryptoEngineIssuer cryptoEngineIssuer;
  private final ContextGenerator contextGenerator;
  private final CryptoEngineReIssuer cryptoEngineReIssuer;
  private final TokenManagerIssuer tokenManager;
  private final KeyManager keyManager;

  @Inject
  public IssuanceManagerIssuerImpl(CryptoEngineIssuer cryptoEngineIssuer,
      ContextGenerator contextGenerator, CryptoEngineReIssuer cryptoEngineReIssuer,
      TokenManagerIssuer tokenManager, KeyManager keyManager) {
    this.cryptoEngineIssuer = cryptoEngineIssuer;
    this.contextGenerator = contextGenerator;
    this.cryptoEngineReIssuer = cryptoEngineReIssuer;
    this.tokenManager = tokenManager;
    this.keyManager = keyManager;
  }

  @Override
  public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip, List<Attribute> atts)
      throws CryptoEngineException {

    URI context = null;
    context = this.contextGenerator.getUniqueContext(URI.create("abc4trust.eu/issuance-protocol"));

    return this.cryptoEngineIssuer.initIssuanceProtocol(ip, atts, context);
  }

  @Override
  public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    IssuanceTokenAndIssuancePolicy tap =
        cryptoEngineIssuer.extractIssuanceTokenAndPolicy(m);
    if (tap == null) {
      return this.cryptoEngineIssuer.issuanceProtocolStep(m);
    } else {
      IssuancePolicy ip = tap.getIssuancePolicy();
      IssuanceToken it = tap.getIssuanceToken();
      MyPresentationPolicy mypp = new MyPresentationPolicy(ip.getPresentationPolicy());
      if (!mypp.isSatisfiedBy(it.getIssuanceTokenDescription()
              .getPresentationTokenDescription(), this.tokenManager, this.keyManager)) {
          throw new CryptoEngineException("Issuance Token not satisfied");
      }
      this.tokenManager.storeToken(it);
      tokenManager.storeToken(tap.getIssuanceToken());
      return this.cryptoEngineIssuer.issuanceProtocolStep(m);
    }
  }

  @Override
  public IssuanceMessageAndBoolean initReIssuanceProtocol(IssuancePolicy clonedIssuancePolicy)
      throws CryptoEngineException {
    URI context = null;
    context =
        this.contextGenerator.getUniqueContext(URI.create("abc4trust.eu/reissuance-protocol"));
    return this.cryptoEngineReIssuer.initReIssuanceProtocol(clonedIssuancePolicy, context);
  }

  @Override
  public IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    return this.cryptoEngineReIssuer.reIssuanceProtocolStep(m);
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid) throws Exception {
    return this.cryptoEngineIssuer.getIssuanceLogEntry(issuanceEntryUid);
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(IssuanceMessage issuanceMessage) {
    return this.cryptoEngineIssuer.extractIssuanceTokenDescription(issuanceMessage);
  }

}

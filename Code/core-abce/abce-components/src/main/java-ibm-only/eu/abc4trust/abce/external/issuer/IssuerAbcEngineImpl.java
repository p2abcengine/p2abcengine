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

package eu.abc4trust.abce.external.issuer;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.issuanceManager.IssuanceManagerIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

public class IssuerAbcEngineImpl implements IssuerAbcEngine {

  private final IssuanceManagerIssuer issuanceManager;
  private final CryptoEngineIssuer cryptoEngine;
  private final CredentialManager credentialManager;
  private final KeyManager keyManager;

  @Inject
  public IssuerAbcEngineImpl(IssuanceManagerIssuer issuanceManager,
      CryptoEngineIssuer cryptoEngine, CredentialManager credentialManager,
      KeyManager keyManager) {
    this.issuanceManager = issuanceManager;
    this.cryptoEngine = cryptoEngine;
    this.credentialManager = credentialManager;
    this.keyManager = keyManager;
  }

  @Override
  public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
      List<Attribute> attributes) throws CryptoEngineException {
    return this.issuanceManager.initIssuanceProtocol(ip, attributes);
  }

  @Override
  public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    return this.issuanceManager.issuanceProtocolStep(m);
  }

  @Override
  public IssuerParameters setupIssuerParameters(SystemParameters syspars,
      int maximalNumberOfAttributes, URI technology, URI uid, URI revocationAuthority,
      List<FriendlyDescription> friendlyIssuerDescription) throws CryptoEngineException {

    IssuerParametersAndSecretKey ret =
        this.cryptoEngine.setupIssuerParameters(syspars, maximalNumberOfAttributes, technology,
            uid, revocationAuthority, friendlyIssuerDescription);

    SecretKey secretKey = ret.issuerSecretKey;

    try {
      credentialManager.storeIssuerSecretKey(uid, secretKey);
    } catch (CredentialManagerException e) {
      throw new CryptoEngineException(e);
    }

    return ret.issuerParameters;
  }

  @Override
  public SystemParameters setupSystemParameters(int keyLength) throws CryptoEngineException, KeyManagerException { 
    if(TestConfiguration.OVERRIDE_SECURITY_LEVEL) {
      System.err.println("!!! OVERRIDE SECURITY LEVEL " + keyLength
        + " -> 750 because of TestConfiguration.OVERRIDE_SECURITY_LEVEL");
      keyLength = 750;
    }
    SystemParameters sp = this.cryptoEngine.setupSystemParameters(keyLength);
    keyManager.storeSystemParameters(sp);
    return sp;
  }

  @Override
  public IssuanceMessageAndBoolean initReIssuanceProtocol(IssuancePolicy clonedIssuancePolicy)
      throws CryptoEngineException {
    return this.issuanceManager.initReIssuanceProtocol(clonedIssuancePolicy);
  }

  @Override
  public IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    return this.issuanceManager.reIssuanceProtocolStep(m);
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid) throws Exception {
    return this.issuanceManager.getIssuanceLogEntry(issuanceEntryUid);
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(IssuanceMessage issuanceMessage) {
    return issuanceManager.extractIssuanceTokenDescription(issuanceMessage);
  }

  @Override
  @Deprecated
  public IssuerParameters setupIssuerParameters(CredentialSpecification credspec,
      SystemParameters syspars, URI uid, URI hash, URI algorithmId, URI revParsUid,
      List<FriendlyDescription> friendlyDescriptions) throws CryptoEngineException {
    return setupIssuerParameters(syspars, credspec.getAttributeDescriptions()
        .getAttributeDescription().size(), algorithmId, uid, revParsUid, friendlyDescriptions);
  }

}

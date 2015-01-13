//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.abce.external.issuer;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;

public class SynchronizedIssuerAbcEngineImpl implements IssuerAbcEngine {

  private final IssuerAbcEngine engine;

  @Inject
  public SynchronizedIssuerAbcEngineImpl(IssuerAbcEngine engine) {
    this.engine = engine;
  }

  @Override
  public synchronized SystemParameters setupSystemParameters(int keyLength)
      throws CryptoEngineException, KeyManagerException {
    return this.engine.setupSystemParameters(keyLength);
  }

  @Override
  public synchronized IssuerParameters setupIssuerParameters(SystemParameters syspars,
      int maximalNumberOfAttributes, URI technology, URI uid, URI revocationAuthority,
      List<FriendlyDescription> friendlyIssuerDescription) throws CryptoEngineException {
    return this.engine.setupIssuerParameters(syspars, maximalNumberOfAttributes, technology, uid,
        revocationAuthority, friendlyIssuerDescription);
  }

  @Override
  public synchronized IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
      List<Attribute> attributes) throws CryptoEngineException {
    return this.engine.initIssuanceProtocol(ip, attributes);
  }

  @Override
  public synchronized IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    return this.engine.issuanceProtocolStep(m);
  }

  @Override
  public synchronized IssuanceMessageAndBoolean initReIssuanceProtocol(
      IssuancePolicy clonedIssuancePolicy) throws CryptoEngineException {
    return this.engine.initReIssuanceProtocol(clonedIssuancePolicy);
  }

  @Override
  public synchronized IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    return this.engine.reIssuanceProtocolStep(m);
  }

  @Override
  public synchronized IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid) throws Exception {
    return this.engine.getIssuanceLogEntry(issuanceEntryUid);
  }

  @Override
  public synchronized IssuanceTokenDescription extractIssuanceTokenDescription(
      IssuanceMessage issuanceMessage) {
    return this.engine.extractIssuanceTokenDescription(issuanceMessage);
  }

  @Override
  @Deprecated
  public synchronized IssuerParameters setupIssuerParameters(CredentialSpecification credspec,
      SystemParameters syspars, URI uid, URI hash, URI algorithmId, URI revParsUid,
      List<FriendlyDescription> friendlyDescriptions) throws CryptoEngineException {
    return this.engine.setupIssuerParameters(credspec, syspars, uid, hash, algorithmId, revParsUid,
        friendlyDescriptions);
  }

}

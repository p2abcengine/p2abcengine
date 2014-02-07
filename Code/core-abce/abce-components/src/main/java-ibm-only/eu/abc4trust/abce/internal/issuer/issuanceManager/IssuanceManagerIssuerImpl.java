//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.internal.issuer.issuanceManager;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.uprove.issuer.UProveCryptoEngineReIssuerImpl;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;

public class IssuanceManagerIssuerImpl implements IssuanceManagerIssuer {

    private final CryptoEngineIssuer cryptoEngineIssuer;
    private final ContextGenerator contextGenerator;
    private final UProveCryptoEngineReIssuerImpl cryptoEngineReIssuer;

    @Inject
    public IssuanceManagerIssuerImpl(CryptoEngineIssuer cryptoEngineIssuer,
            ContextGenerator contextGenerator, UProveCryptoEngineReIssuerImpl cryptoEngineReIssuer) {
        this.cryptoEngineIssuer = cryptoEngineIssuer;
        this.contextGenerator = contextGenerator;
        this.cryptoEngineReIssuer = cryptoEngineReIssuer;
        System.out.println("Hello from IssuanceManagerIssuerImpl()");
    }

    @Override
    public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
            List<Attribute> atts) throws CryptoEngineException {

        URI context = null;
        context = this.contextGenerator.getUniqueContext(URI.create("abc4trust.eu/issuance-protocol"));

        return this.cryptoEngineIssuer.initIssuanceProtocol(ip, atts, context);
    }

    @Override
    public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {
        return this.cryptoEngineIssuer.issuanceProtocolStep(m);
    }

 @Override
 public IssuanceMessageAndBoolean initReIssuanceProtocol(
 IssuancePolicy clonedIssuancePolicy) throws CryptoEngineException {
 URI context = null;
 context = this.contextGenerator.getUniqueContext(URI.create("abc4trust.eu/reissuance-protocol"));
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

}

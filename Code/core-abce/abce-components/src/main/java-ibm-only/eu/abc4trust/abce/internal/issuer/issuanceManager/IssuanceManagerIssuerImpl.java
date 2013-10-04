//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

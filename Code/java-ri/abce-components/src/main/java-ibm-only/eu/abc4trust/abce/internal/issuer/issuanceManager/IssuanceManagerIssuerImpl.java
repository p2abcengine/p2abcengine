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
import eu.abc4trust.returnTypes.IssuanceMessageAndBoolean;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;

public class IssuanceManagerIssuerImpl implements IssuanceManagerIssuer {

    private final CryptoEngineIssuer cryptoEngineIssuer;
    private final ContextGenerator contextGenerator;

    @Inject
    public IssuanceManagerIssuerImpl(CryptoEngineIssuer cryptoEngineIssuer,
            ContextGenerator contextGenerator) {
        this.cryptoEngineIssuer = cryptoEngineIssuer;
        this.contextGenerator = contextGenerator;
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

}

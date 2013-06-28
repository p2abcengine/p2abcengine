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

package eu.abc4trust.abce.external.issuer;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.issuer.issuanceManager.IssuanceManagerIssuer;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

public class IssuerAbcEngineImpl implements IssuerAbcEngine {

    private final IssuanceManagerIssuer issuanceManager;
    private final CryptoEngineIssuer cryptoEngine;
    @Inject
    public IssuerAbcEngineImpl(IssuanceManagerIssuer issuanceManager,
            CryptoEngineIssuer cryptoEngine) {
        this.issuanceManager = issuanceManager;
        this.cryptoEngine = cryptoEngine;
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
    public IssuerParameters setupIssuerParameters(CredentialSpecification credspec,
            SystemParameters syspars, URI uid, URI hash, URI algorithm, URI revParsUid, List<FriendlyDescription> friendlyIssuerDescription) {

        // TODO(enr): switch the cryptoEngine based on the algorithm
        IssuerParametersAndSecretKey ret = this.cryptoEngine.setupIssuerParameters(credspec, syspars, uid, hash, revParsUid, friendlyIssuerDescription);

        //TODO(enr): What do we do with the secret key?
        SecretKey secretKey = ret.issuerSecretKey;

        return ret.issuerParameters;
    }

    @Override
    public SystemParameters setupSystemParameters(int keyLength, URI cryptographicMechanism) {
        return this.cryptoEngine.setupSystemParameters(keyLength, cryptographicMechanism);
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

}

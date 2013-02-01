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

package eu.abc4trust.revocationProxy;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationMessage;

public class InMemoryCommunicationStrategy implements RevocationProxyCommunicationStrategy {

    private final RevocationProxyAuthority otherProxy;

    @Inject
    public InMemoryCommunicationStrategy(RevocationProxyAuthority otherProxy) {
        this.otherProxy = otherProxy;
    }

    private RevocationMessageAndBoolean getResponceFromRevocationAuthority(
            RevocationMessage m, Reference revocationInfoReference)
                    throws RevocationProxyException {

        RevocationMessageAndBoolean msg = this.otherProxy
                .processRevocationMessage(m);
        if (msg==null) {
            throw new RuntimeException("Failed to receive message from service");
        }

        if (msg.revmess == null) {
            throw new RuntimeException(
                    "Failed to obtain revocation information");
        }
        return msg;
    }

    @Override
    public CryptoParams requestRevocationHandle(RevocationMessage m,
            Reference nonRevocationEvidenceReference)
                    throws RevocationProxyException {
        RevocationMessageAndBoolean msg = this
                .getResponceFromRevocationAuthority(m,
                        nonRevocationEvidenceReference);

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().addAll(msg.revmess.getCryptoParams().getAny());
        return cryptoParams;
    }

    @Override
    public CryptoParams requestRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {
        RevocationMessageAndBoolean msg = this
                .getResponceFromRevocationAuthority(m, revocationInfoReference);

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().addAll(msg.revmess.getCryptoParams().getAny());
        return cryptoParams;
    }

    @Override
    public CryptoParams revocationEvidenceUpdate(RevocationMessage m,
            Reference nonRevocationEvidenceUpdateReference)
                    throws RevocationProxyException {
        RevocationMessageAndBoolean msg = this
                .getResponceFromRevocationAuthority(m,
                        nonRevocationEvidenceUpdateReference);

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().addAll(msg.revmess.getCryptoParams().getAny());
        return cryptoParams;
    }

    @Override
    public CryptoParams getCurrentRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {
        RevocationMessageAndBoolean msg = this
                .getResponceFromRevocationAuthority(m, revocationInfoReference);

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().addAll(msg.revmess.getCryptoParams().getAny());
        return cryptoParams;
    }

}

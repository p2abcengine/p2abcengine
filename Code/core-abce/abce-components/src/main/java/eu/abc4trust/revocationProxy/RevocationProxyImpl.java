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

import org.w3c.dom.Element;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationMessage;

public class RevocationProxyImpl implements RevocationProxy {
    private RevocationProxyCommunicationStrategy communicationStrategy = null;

    @Inject
    public RevocationProxyImpl(RevocationProxyCommunicationStrategy communicationStrategy) {
        this.communicationStrategy = communicationStrategy;
    }

    @Override
    public RevocationMessage processRevocationMessage(RevocationMessage m,
            RevocationAuthorityParameters revpars) throws Exception {
        CryptoParams cryptoParams = null;

        RevocationMessageType revocationMessageType = RevocationUtility.unserializeRevocationMessageType((Element)m.getCryptoParams().getAny().get(0));
        switch (revocationMessageType) {
        case REQUEST_REVOCATION_HANDLE:
            cryptoParams = this.requestRevocationHandle(m, revpars.getNonRevocationEvidenceReference());
            break;
        case REQUEST_REVOCATION_INFORMATION:
            cryptoParams = this.requestRevocationInformation(m,
                    revpars.getRevocationInfoReference());
            break;
        case GET_CURRENT_REVOCATION_INFORMATION:
            cryptoParams = this.getCurrentRevocationInformation(m,
                    revpars.getRevocationInfoReference());
            break;
        case UPDATE_REVOCATION_EVIDENCE:
            cryptoParams = this.revocationEvidenceUpdate(m,
                    revpars.getNonRevocationEvidenceUpdateReference());
            break;

        default:
            break;
        }
        RevocationMessage rm = new RevocationMessage();
        rm.setContext(m.getContext());
        rm.setRevocationAuthorityParametersUID(m
                .getRevocationAuthorityParametersUID());
        rm.setCryptoParams(cryptoParams);

        return rm;
    }

    private CryptoParams getCurrentRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {
        return this.communicationStrategy.getCurrentRevocationInformation(m,
                revocationInfoReference);
    }

    private CryptoParams requestRevocationHandle(RevocationMessage m,
            Reference nonRevocationEvidenceReference)
                    throws RevocationProxyException {
        return this.communicationStrategy.requestRevocationHandle(m,
                nonRevocationEvidenceReference);
    }

    private CryptoParams requestRevocationInformation(RevocationMessage m,
            Reference revocationInformationReference)
                    throws RevocationProxyException {
        return this.communicationStrategy.requestRevocationInformation(m,
                revocationInformationReference);
    }

    private CryptoParams revocationEvidenceUpdate(RevocationMessage m,
            Reference nonRevocationEvidenceUpdateReference)
                    throws RevocationProxyException {
        return this.communicationStrategy.revocationEvidenceUpdate(m,
                nonRevocationEvidenceUpdateReference);
    }

}

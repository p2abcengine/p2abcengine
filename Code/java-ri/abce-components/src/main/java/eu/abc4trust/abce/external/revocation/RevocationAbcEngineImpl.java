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

package eu.abc4trust.abce.external.revocation;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

public class RevocationAbcEngineImpl implements RevocationAbcEngine {

    private final CryptoEngineRevocation cryptoEngine;

    @Inject
    public RevocationAbcEngineImpl(CryptoEngineRevocation cryptoEngine) {
        this.cryptoEngine = cryptoEngine;
    }

    @Override
    public RevocationAuthorityParameters setupRevocationAuthorityParameters(
            int keyLength, URI cryptographicMechanism,
            URI uid, Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference)
                    throws CryptoEngineException {
        return this.cryptoEngine.setupRevocationAuthorityParameters(keyLength,
                cryptographicMechanism, uid,
                revocationInfoReference, nonRevocationEvidenceReference,
                nonRevocationUpdateReference);
    }

    @Override
    public RevocationInformation revoke(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        return this.cryptoEngine.revoke(revParUid, attributes);
    }

    @Override
    public NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        return this.cryptoEngine.generateNonRevocationEvidence(revParUid,
                attributes);
    }

    @Override
    public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(
            URI revAuthParamsUid, int epoch) throws CryptoEngineException {
        return this.cryptoEngine.generateNonRevocationEvidenceUpdate(
                revAuthParamsUid, epoch);
    }

    @Override
    public RevocationInformation updateRevocationInformation(URI revParUid)
            throws CryptoEngineException {
        return this.cryptoEngine.updateRevocationInformation(revParUid);
    }


    @Override
    public RevocationInformation getRevocationInformation(URI revParamsUid,
            URI revInfoUid) throws CryptoEngineException {
        return this.cryptoEngine.getRevocationInformation(revParamsUid,
                revInfoUid);
    }

}

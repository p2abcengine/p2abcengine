//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.revauth;

import java.net.URI;
import java.util.List;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

public class MockCryptoEngineRevocation implements CryptoEngineRevocation {

    @Override
    public RevocationInformation revoke(URI revParUid, List<Attribute> attributes)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationInformation updateRevocationInformation(URI revParUid)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }


    @Override
    public NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
            List<Attribute> attributes) throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationAuthorityParameters setupRevocationAuthorityParameters(int keyLength,
            URI cryptographicMechanism, URI uid, Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference, Reference nonRevocationUpdateReference)
                    throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(URI revAuthParamsUid,
            int epoch) throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public RevocationInformation getRevocationInformation(URI revParamsUid, URI revInfoUid)
            throws CryptoEngineException {
        throw new RuntimeException("Method not implemented");
    }

}

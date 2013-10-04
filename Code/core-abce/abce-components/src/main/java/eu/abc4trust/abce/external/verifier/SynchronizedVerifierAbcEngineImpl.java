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

package eu.abc4trust.abce.external.verifier;

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationInformation;

public class SynchronizedVerifierAbcEngineImpl implements VerifierAbcEngine {

    private final VerifierAbcEngine engine;

    @Inject
    public SynchronizedVerifierAbcEngineImpl(VerifierAbcEngine engine) {
        this.engine = engine;
    }

    @Override
    public synchronized PresentationTokenDescription verifyTokenAgainstPolicy(
            PresentationPolicyAlternatives p, PresentationToken t, boolean store)
                    throws TokenVerificationException, CryptoEngineException {

        return this.engine.verifyTokenAgainstPolicy(p, t, store);
    }

    @Override
    public synchronized PresentationToken getToken(URI tokenUid) {
        return this.engine.getToken(tokenUid);
    }

    @Override
    public synchronized boolean deleteToken(URI tokenUid) {
        return this.engine.deleteToken(tokenUid);
    }

    @Override
    public synchronized RevocationInformation getLatestRevocationInformation(URI revParamsUid)
            throws CryptoEngineException {
        return this.engine.getLatestRevocationInformation(revParamsUid);
    }

}

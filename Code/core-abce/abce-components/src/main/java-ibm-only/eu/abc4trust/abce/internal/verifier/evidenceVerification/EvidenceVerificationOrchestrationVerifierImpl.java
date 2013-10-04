//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.verifier.evidenceVerification;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationToken;

public class EvidenceVerificationOrchestrationVerifierImpl
implements
EvidenceVerificationOrchestrationVerifier {


    private final CryptoEngineVerifier cryptoEngine;

    @Inject
    public EvidenceVerificationOrchestrationVerifierImpl(CryptoEngineVerifier cryptoEngine) {
        this.cryptoEngine = cryptoEngine;
        //System.out.println("Hello from EvidenceVerificationOrchestrationVerifierImpl()");
    }

    @Override
    public boolean verifyToken(PresentationToken t)
            throws TokenVerificationException, CryptoEngineException {
        return this.cryptoEngine.verifyToken(t);
    }

}

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

package eu.abc4trust.abce.internal.verifier.evidenceVerification;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationToken;

public interface EvidenceVerificationOrchestrationVerifier {

    /**
     * This method dissects the given presentation token t in subtokens,
     * separating those subtokens based on privacy-ABCs from those based on
     * other ABC technologies such as X.509. For each of these subtokens, it
     * invokes CryptoEngine.verifyToken(subtoken) to verify the cryptographic
     * evidence of the subtoken. If all subtokens are deemed valid, this method
     * returns true, otherwise it returns a list of error messages.
     * 
     * @param t
     * @return
     * @throws CryptoEngineException
     */
    boolean verifyToken(PresentationToken t) throws TokenVerificationException,
    CryptoEngineException;


}

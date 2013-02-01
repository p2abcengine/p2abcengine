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

package eu.abc4trust.abce.internal.revocation;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixClaim;
import eu.abc4trust.cryptoEngine.idemix.util.RevocationProofData;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;

public class RevocationProof {

    public static final String ABC4TRUST_REVOCATION_TEMP_NAME = "abc4trust:revocation:tempName";
    private final Map<URI, CredentialInToken> credSpecUidToCredInToken;
    private final Map<URI, CredentialInTokenWithCommitments> credSpecUidToCredInTokenWithComm;
    private final KeyManager keyManager;

    @Inject
    public RevocationProof(KeyManager keyManager) {
        super();
        this.credSpecUidToCredInToken = new HashMap<URI, CredentialInToken>();
        this.credSpecUidToCredInTokenWithComm = new HashMap<URI, CredentialInTokenWithCommitments>();
        this.keyManager = keyManager;
    }

    public RevocationProofData revocationProofDatum(int epoch,
            URI credSpecUid, URI revParamsUid, AccumulatorWitness witness) {

        String tempName = ABC4TRUST_REVOCATION_TEMP_NAME;

        RevocationProofData revocationProofDatum = new RevocationProofData(
                credSpecUid, revParamsUid, epoch, tempName, witness);
        return revocationProofDatum;
    }

    public AccumulatorWitness extractWitness(NonRevocationEvidence nre) {

        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness w1 = (AccumulatorWitness) Parser.getInstance()
                .parse(witnessElement);
        return w1;
    }

    public CredentialInToken getCredentialInToken(URI credSpecUid,
            IdemixClaim ic) throws CryptoEngineException {
        for (CredentialInToken c : ic.getCredentialList().values()) {
            if (c.getCredentialSpecUID().compareTo(credSpecUid) == 0) {
                return c;
            }
        }
        throw new CryptoEngineException(
                "Could not find credential in token with credential specification UID: \""
                        + credSpecUid + "\"");
    }

    public CredentialInTokenWithCommitments getCredentialInToken(
            URI credSpecUid, List<CredentialInTokenWithCommitments> credentials)
                    throws CryptoEngineException {
        for (CredentialInTokenWithCommitments c : credentials) {
            if (c.getCredentialSpecUID().compareTo(credSpecUid) == 0) {
                return c;
            }
        }
        throw new CryptoEngineException(
                "Could not find credential in token with credential specification UID: \""
                        + credSpecUid + "\"");
    }

    public URI getCredentialRevocationInformationUid(URI credSpecUid)
            throws CryptoEngineException {
        CredentialInToken credInToken = this.credSpecUidToCredInToken
                .get(credSpecUid);
        if (credInToken == null) {
            CredentialInTokenWithCommitments credInTokenWithComm = this.credSpecUidToCredInTokenWithComm
                    .get(credSpecUid);
            return credInTokenWithComm.getRevocationInformationUID();
        }
        return credInToken.getRevocationInformationUID();
    }

    public void addCredInToken(URI credentialSpecUID,
            CredentialInTokenWithCommitments credInToken) {
        this.credSpecUidToCredInTokenWithComm.put(credentialSpecUID,
                credInToken);
    }

    public void addCredInToken(URI credentialSpecUID,
            CredentialInToken credInToken) {
        this.credSpecUidToCredInToken.put(credentialSpecUID, credInToken);

    }

    public boolean isIdemix(Credential cred) throws CryptoEngineException {
        URI idemixMechanism = CryptoUriUtil.getIdemixMechanism();
        return this.isEngine(cred, idemixMechanism);
    }

    public boolean isUProve(Credential cred) throws CryptoEngineException {
        URI uproveMechanism = CryptoUriUtil.getUproveMechanism();
        return this.isEngine(cred, uproveMechanism);
    }

    private boolean isEngine(Credential cred, URI mechanism)
            throws CryptoEngineException {
        URI issuerParametersUID = cred.getCredentialDescription()
                .getIssuerParametersUID();
        IssuerParameters issuerParams;
        try {
            issuerParams = this.keyManager
                    .getIssuerParameters(issuerParametersUID);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        boolean isMechanism = issuerParams.getAlgorithmID().equals(mechanism);
        return isMechanism;
    }
}

//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.revocation;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.VerifierInput;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationInformation;

public class VerifierRevocation {

    private final KeyManager keyManager;
    // private final List<CredentialInTokenWithCommitments>
    // credentialInTokenWithCommitments;
    private final List<CredentialInToken> credentialInToken;

    @Inject
    public VerifierRevocation(KeyManager keyManager) {
        super();
        this.keyManager = keyManager;
        // this.credentialInTokenWithCommitments = new
        // LinkedList<CredentialInTokenWithCommitments>();
        this.credentialInToken = new LinkedList<CredentialInToken>();
    }

    // private List<CredentialInTokenWithCommitments>
    // getCredentialsInTokensWithCommitments() {
    // return this.credentialInTokenWithCommitments;
    // }

    private List<CredentialInToken> getCredentialsInTokens() {
        return this.credentialInToken;
    }

    public void addCredentialInToken(CredentialInToken cred) {
        this.credentialInToken.add(cred);
    }

    // public void addCredentialInToken(CredentialInTokenWithCommitments cred) {
    // this.credentialInTokenWithCommitments.add(cred);
    // }

    public Map<URI, RevocationInformation> getRevInfoUidToRevInfo()
            throws CryptoEngineException {
        Map<URI, RevocationInformation> revInfoUidToRevInfo = new HashMap<URI, RevocationInformation>();

        List<CredentialInToken> credentials = this.getCredentialsInTokens();
        this.addRevInfoFromCredInToken(credentials, revInfoUidToRevInfo);

        // List<CredentialInTokenWithCommitments> credentialWCs = this
        // .getCredentialsInTokensWithCommitments();
        // this.addRevInfoFromCredInTokenWithCommitments(credentialWCs,
        // revInfoUidToRevInfo);

        return revInfoUidToRevInfo;
    }

    // private void addRevInfoFromCredInTokenWithCommitments(
    // List<CredentialInTokenWithCommitments> credentialWCs,
    // Map<URI, RevocationInformation> revInfoUidToRevInfo)
    // throws CryptoEngineException {
    // for (CredentialInTokenWithCommitments cred : credentialWCs) {
    // try {
    // CredentialSpecification credSpec = this.getCredSpec(cred);
    // if (credSpec.isRevocable()) {
    // URI revParamsUid = this.getRevocationParametersUid(cred);
    // URI revInfoUid = cred.getRevocationInformationUID();
    // this.addRevInfoUidToRevInfo(revInfoUidToRevInfo,
    // revParamsUid, revInfoUid);
    // }
    // } catch (KeyManagerException ex) {
    // throw new CryptoEngineException(ex);
    // }
    // }
    // }

    private void addRevInfoFromCredInToken(List<CredentialInToken> credentials,
            Map<URI, RevocationInformation> revInfoUidToRevInfo)
                    throws CryptoEngineException {
        for (CredentialInToken cred : credentials) {
            try {
                CredentialSpecification credSpec = this.getCredSpec(cred);
                if (credSpec.isRevocable()) {
                    URI revParamsUid = this.getRevocationParametersUid(cred);
                    URI revInfoUid = cred.getRevocationInformationUID();
                    this.addRevInfoUidToRevInfo(revInfoUidToRevInfo,
                            revParamsUid, revInfoUid);
                }
            } catch (KeyManagerException ex) {
                throw new CryptoEngineException(ex);
            }
        }
    }

    private RevocationInformation addRevInfoUidToRevInfo(
            Map<URI, RevocationInformation> revInfoUidToRevInfo,
            URI revParamsUid, URI revInfoUid) throws KeyManagerException,
            CryptoEngineException {
        RevocationInformation revInfo = this.keyManager
                .getRevocationInformation(revParamsUid, revInfoUid);

        if (revInfo == null) {
            throw new CryptoEngineException(
                    "Could not find revocation information with UID: \""
                            + revInfoUid + "\"");
        }

        revInfoUidToRevInfo.put(revInfoUid, revInfo);
        return revInfo;
    }

    private CredentialSpecification getCredSpec(CredentialInToken cred)
            throws KeyManagerException {
        URI credentialSpecUID = cred.getCredentialSpecUID();
        CredentialSpecification credSpec = this.keyManager
                .getCredentialSpecification(credentialSpecUID);
        return credSpec;
    }

    private URI getRevocationParametersUid(CredentialInToken cred)
            throws KeyManagerException {
        URI issuerParametersUID = cred.getIssuerParametersUID();
        IssuerParameters issuerParameters = this.keyManager
                .getIssuerParameters(issuerParametersUID);
        return issuerParameters.getRevocationParametersUID();
    }

    public void addAccumulators(VerifierInput verifierInput)
            throws CryptoEngineException {
        List<AccumulatorState> states = this.getAccumulatorStates();
        for (int inx = 0; inx < states.size(); inx++) {
            AccumulatorState state = states.get(inx);
            verifierInput.accumulatorStates
            .put(RevocationProof.ABC4TRUST_REVOCATION_TEMP_NAME + inx,
                    state);
        }
    }

    private List<AccumulatorState> getAccumulatorStates()
            throws CryptoEngineException {
        List<AccumulatorState> accumulatorStates = new LinkedList<AccumulatorState>();
        List<CredentialInToken> revocableCredentials = this
                .getRevocableCredentials();
        Map<URI, RevocationInformation> revInfoUidToRevInfo = this
                .getRevInfoUidToRevInfo();
        for (CredentialInToken credInToken : revocableCredentials) {
            URI revInfoUid = credInToken.getRevocationInformationUID();
            RevocationInformation revInfo = revInfoUidToRevInfo.get(revInfoUid);
            if (revInfo == null) {
                throw new CryptoEngineException(
                        "Could not find revocation information");
            }

            Element stateElement = (Element) revInfo.getCryptoParams().getAny()
                    .get(0);
            Parser parser = Parser.getInstance();
            AccumulatorState state = (AccumulatorState) parser
                    .parse(stateElement);
            accumulatorStates.add(state);
        }
        return accumulatorStates;
    }

    private List<CredentialInToken> getRevocableCredentials()
            throws CryptoEngineException {
        List<CredentialInToken> revocableCredentials = new LinkedList<CredentialInToken>();
        for (CredentialInToken credInToken : this.credentialInToken) {
            URI credentialSpecUID = credInToken.getCredentialSpecUID();
            CredentialSpecification credentialSpec;
            try {
                credentialSpec = this.keyManager
                        .getCredentialSpecification(credentialSpecUID);
            } catch (KeyManagerException ex) {
                throw new CryptoEngineException(ex);
            }
            if (credentialSpec.isRevocable()) {
                revocableCredentials.add(credInToken);
            }
        }
        return revocableCredentials;
    }

    public void clearCredentialInToken() {
        this.credentialInToken.clear();
    }

    public List<CredentialInToken> getCredentialInToken() {
        return this.credentialInToken;
    }

    // private CredentialSpecification getCredSpec(
    // CredentialInTokenWithCommitments cred) throws KeyManagerException {
    // URI credentialSpecUID = cred.getCredentialSpecUID();
    // CredentialSpecification credSpec = this.keyManager
    // .getCredentialSpecification(credentialSpecUID);
    // return credSpec;
    // }
    //
    // private URI getRevocationParametersUid(CredentialInTokenWithCommitments
    // cred)
    // throws KeyManagerException {
    // URI issuerParametersUID = cred.getIssuerParametersUID();
    // IssuerParameters issuerParameters = this.keyManager
    // .getIssuerParameters(issuerParametersUID);
    // return issuerParameters.getRevocationParametersUID();
    // }

}

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

package eu.abc4trust.cryptoEngine.uprove.verifier;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PresentationProofComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;

import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.SystemParameters;

/**
 * CryptoEngineVerifier implementation that uses UProve through WebServices interop. Depends on the .NET/Mono based server part running on localhost
 * 
 * @author Raphael Dobers
 */

public class UProveCryptoEngineVerifierImpl implements CryptoEngineVerifier {

    private final KeyManager keyManager;
    private final UProveBindingManager binding;
    private final UProveUtils utils;

    @Inject
    public UProveCryptoEngineVerifierImpl(KeyManager keyManager,
            UProveBindingManager bindingManager) {
        this.keyManager = keyManager;

        // Setup WebService connection to .NET based U-Prove.
        this.binding = bindingManager;
        this.binding.setupBiding("Verifier");

        this.utils = new UProveUtils();
        
        System.out.println("Hello from UProveCryptoEngineVerifierImpl()");
    }


    @Override
    public boolean verifyToken(PresentationToken pt) throws TokenVerificationException {

    	
    	SystemParameters syspars;
        try {
            syspars = this.keyManager.getSystemParameters();
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }

        int keyLength = new UProveSystemParameters(syspars).getKeyLength();
    	String sessionKey = utils.getSessionKey(binding, keyLength);
    	
        CryptoParams cryptoEvidence = pt.getCryptoEvidence();
        boolean verifyTokenResult = true;
        PresentationTokenDescription ptd = pt.getPresentationTokenDescription();

       
        Map<URI, Element> credCryptoParams = new HashMap<URI, Element>();
        Map<URI, Element> nymCryptoParams = new HashMap<URI, Element>();

        for (Object o: cryptoEvidence.getAny()) {
            Element e = (Element) o;
            if (e.getTagName().equals(
                    UProveSerializer.U_PROVE_CREDENTIAL_AND_PSEUDONYM)) {
                NodeList elementsByTagName = e
                        .getElementsByTagName(UProveSerializer.CREDENTIAL_ALIAS);
                if (elementsByTagName.getLength() != 1) {
                	binding.logout(sessionKey);
                    throw new RuntimeException("UProveCredentialAndPseudonym element had: " + elementsByTagName.getLength() + " CredentialAlias elements, expected 1");
                }
                Node credentialAliasNode = elementsByTagName.item(0);
                URI credentialAlias = URI.create(credentialAliasNode
                        .getTextContent());
                credCryptoParams.put(credentialAlias, e);
            }
            if (e.getTagName().equals(UProveSerializer.U_PROVE_PSEUDONYM)) {
                NodeList elementsByTagName = e
                        .getElementsByTagName(UProveSerializer.PSEUDONYM_ALIAS);
                if (elementsByTagName.getLength() != 1) {
                	binding.logout(sessionKey);
                    throw new RuntimeException("UProveCredentialAndPseudonym element had: " + elementsByTagName.getLength() + " CredentialAlias elements, expected 1");
                }
                Node pseudonymAliasNode = elementsByTagName.item(0);
                URI pseudonymAlias = URI.create(pseudonymAliasNode
                        .getTextContent());
                nymCryptoParams.put(pseudonymAlias, e);
            }
        }

        UProveSerializer serializer = new UProveSerializer();

        List<CredentialInToken> credentials = ptd.getCredential();
        
        for (int inx = 0; inx < credentials.size(); inx++) {
            CredentialInToken credInToken = credentials.get(inx);
            URI credAlias = this.utils.getAlias(inx, credInToken);

            Element uproveEvidence = credCryptoParams.get(credAlias);
            if (uproveEvidence == null) {
                return false;
            }
            
            PresentationProofComposite presentationProof = serializer
                    .deserializeToPresentationProofComposite(uproveEvidence);
            
            // Get UProve disclosedAttributes from the cryptoevidence
            ArrayOfint arrayOfIntDisclosedParam = serializer
                    .deserializeToDisclosedIndices(uproveEvidence);

            // Get UProve committed indices from the cryptoevidence
            ArrayOfint arrayOfIntCommittedParam = serializer
                    .deserializeToCommittedIndices(uproveEvidence);
            
            // Get the UProveTokenComposite from the cryptoevidence
            UProveTokenComposite compositeToken = serializer
                    .deserializeToCompositeToken(uproveEvidence);

            
            IssuerParametersComposite ipc = serializer
                    .deserializeToIssuerParametersComposite(uproveEvidence);

            // Map message to U-Prove message string.
            String applicationMessage = this.utils
                    .normalizeApplicationMessage(ptd);

            String verifierScopeParam = "null";

            // Verifier verifies the given proof
            System.out.println("Verifying UProve Token proof..."); // FIXME:
            
            boolean tmpResult = this.binding.verifyTokenProof(
                    presentationProof, arrayOfIntDisclosedParam,
                    arrayOfIntCommittedParam, applicationMessage,
                    verifierScopeParam, ipc, compositeToken, sessionKey);
            System.out.println("Token Verify result: " + tmpResult);
            if(!tmpResult) verifyTokenResult = false;  
        }


        List<PseudonymInToken> pseudonyms = ptd.getPseudonym();
        for (int inx = 0; inx < pseudonyms.size(); inx++) {
            PseudonymInToken pseudonym = pseudonyms.get(inx);

            URI nymAlias = this.utils.getAlias(inx, pseudonym);

            Element uproveEvidence = nymCryptoParams.get(nymAlias);
            if (uproveEvidence == null) {
                return false;
            }

            PseudonymComposite pseudonymComposite = serializer
                    .deserializeToPseudonymComposite(uproveEvidence);
            IssuerParametersComposite ipc = serializer
                    .deserializePseudonymEvidenceToIssuerParametersComposite(uproveEvidence);

            this.binding.verifyIssuerParameters(ipc, sessionKey);

            String applicationMessage = this.utils
                    .normalizeApplicationMessage(ptd);

            String scope = "null";
            if (pseudonym.isExclusive()) {
                scope = pseudonym.getScope();
            }


            System.out.println("Verifying U-Prove Pseudonym proof...");

            boolean tmpResult = this.binding.verifyPseudonym(
                    applicationMessage, scope, pseudonymComposite, sessionKey);
            if(!tmpResult) verifyTokenResult = false;
            System.out.println("Token pseudonym result: "
                    + verifyTokenResult);
        }
        binding.logout(sessionKey);
        return verifyTokenResult;
    }
}

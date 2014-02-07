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

package eu.abc4trust.abce.integrationtests;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceClient;

import org.datacontract.schemas._2004._07.abc4trust_uprove.ArrayOfUProveKeyAndTokenComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerKeyAndParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PresentationProofComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SecondIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.ThirdIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveKeyAndTokenComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;

import abc4trust_uprove.service1.IService1;
import abc4trust_uprove.service1.Service1;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;


/**
 * Client-side test class for UProve/Java WebService interop.
 * 
 * @author Raphael Dobers
 */

public class WSClientTest {

    public static void main(String[] args) {

        String issuerUid = "http://issuer/uprove/issuerparams/software";
        byte[] attributeEncoding = new byte[] { 1, 1, 1 };
        String hash = "SHA-256";
        int securityLevel = 2048;
        // The number of tokens to issue.
        Integer numberOfTokensParam = new Integer(10);

        // Setup WebService connection to .NET based U-Prove.
        // user.
        IService1 userBinding = createBinding(8070);

        // Issuer.
        IService1 issuerBinding = createBinding(8090);

        // Verifier.
        IService1 verifierBinding = createBinding(8060);
        final UProveUtils utils = new UProveUtils();

        String sessionKeyIssuer = null;
        String sessionKeyUser = null;
        String sessionKeyVerifier = null;
        try {
            // Setup issuer parameters.
            System.out.println("Setting up issuer parameters...");
            
            sessionKeyIssuer = utils.getSessionKey(issuerBinding, securityLevel);
            sessionKeyUser = utils.getSessionKey(userBinding, securityLevel);
            sessionKeyVerifier = utils.getSessionKey(verifierBinding, securityLevel);
            IssuerKeyAndParametersComposite issuerKeyAndParametersComposite = issuerBinding
                    .setupIssuerParameters(issuerUid, attributeEncoding, hash, sessionKeyIssuer);

            IssuerParametersComposite ipc = getIssuerParametersComposite(
                    issuerBinding, issuerKeyAndParametersComposite);

            verifyIssuerParameters(issuerBinding, ipc, sessionKeyIssuer);
            verifyIssuerParameters(userBinding, ipc, sessionKeyUser);
            verifyIssuerParameters(verifierBinding, ipc, sessionKeyVerifier);

            setIssuerPrivateKeys(issuerBinding, issuerKeyAndParametersComposite, sessionKeyIssuer);

            ArrayOfstring arrayOfStringAttributesParam = getAttributes();

            ArrayOfUProveKeyAndTokenComposite compositeTokens = testIssuanceOfTokens(
                    numberOfTokensParam, userBinding, issuerBinding, ipc,
                    arrayOfStringAttributesParam, sessionKeyUser, sessionKeyIssuer);

            if (compositeTokens == null) {
                System.exit(2);
            }

            testTokenPresentation(userBinding, verifierBinding, ipc,
                    arrayOfStringAttributesParam, compositeTokens, sessionKeyUser, sessionKeyVerifier);

            testNonBoundPseudonyms(userBinding, verifierBinding, sessionKeyUser, sessionKeyVerifier);
            
        } catch (com.sun.xml.ws.client.ClientTransportException cte) {
            System.out.println("No Connection to UProve Web Service!");
            // Start the UProve Service here
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	issuerBinding.logout(sessionKeyIssuer);
        	userBinding.logout(sessionKeyUser);
        	verifierBinding.logout(sessionKeyVerifier);
        }
        
    }

    private static void testNonBoundPseudonyms(IService1 userBinding,
            IService1 verfierBinding, String sessionKeyUser, String sessionKeyVerifier) {
        // test pseudonyms not bound to a U-Prove credential.
        String messageParam = "message";

        String verifierScopeParam = "verifier scope";

        // 1. scope-exclusive pseudonym.
        PseudonymComposite pseudonymComposite = userBinding.presentPseudonym(
                messageParam, verifierScopeParam, sessionKeyUser);
        boolean verifyPseudonymResult = verfierBinding.verifyPseudonym(
                messageParam, verifierScopeParam, pseudonymComposite, sessionKeyVerifier);
        System.out.println("Verify scope-exclusive pseudonym result: "
                + verifyPseudonymResult);

        // 2. verified pseudonym.
        pseudonymComposite = userBinding.presentPseudonym(messageParam, null, sessionKeyUser);
        verifyPseudonymResult = verfierBinding.verifyPseudonym(messageParam,
                null, pseudonymComposite, sessionKeyVerifier);
        System.out.println("Verify pseudonym result: " + verifyPseudonymResult);

        // 2. verified pseudonym.
        pseudonymComposite = userBinding.presentPseudonym(messageParam, null, sessionKeyUser);
        verifyPseudonymResult = verfierBinding.verifyPseudonym(messageParam,
                null, pseudonymComposite, sessionKeyVerifier);
        System.out.println("Verify pseudonym result: " + verifyPseudonymResult);
    }

    private static void testTokenPresentation(IService1 userBinding,
            IService1 verfierBinding, IssuerParametersComposite ipc,
            ArrayOfstring arrayOfStringAttributesParam,
            ArrayOfUProveKeyAndTokenComposite compositeTokens, String sessionKeyUser, String sessionKeyVerifier) {
        // Present U-Prove token.
        System.out.println("=== Present U-Prove tokens ===");

        String messageParam = "message";

        ArrayOfint arrayOfIntDisclosedParam = new ArrayOfint();
        arrayOfIntDisclosedParam.getInt().add(new Integer(2));

        ArrayOfint arrayOfIntCommittedParam = new ArrayOfint();
        arrayOfIntCommittedParam.getInt().add(new Integer(1));

        UProveKeyAndTokenComposite compositeKeyAndToken = compositeTokens
                .getUProveKeyAndTokenComposite().get(5);
        UProveTokenComposite uproveToken = compositeKeyAndToken.getToken()
                .getValue();
        byte[] tokenPrivateKey = compositeKeyAndToken.getPrivateKey()
                .getValue();
        System.out.println("Presenting U-Prove token: " + uproveToken);
        System.out.println("Generating proof for U-Prove Token...");

        // If the presentation policy contains a pseudonym element, then if
        // an available token has been saved for the scope, use it,
        // otherwise, pick an unused UProve token and save it for the scope.
        // Then if the exclusive xml attribute is set, set the
        // verifierScopeParam.
        String verifierScopeParam = "verifier scope";
        // abc:PresentationPolicy:pseudonym:scope (if exclusive attribute is
        // true), or null else (if pseudonym xml element is not present in
        // policy), pick the first available U-Prove token and delete after
        // use.

        // User generates proof for the above token
        PresentationProofComposite proof = userBinding.proveToken(
                arrayOfStringAttributesParam, arrayOfIntDisclosedParam,
                arrayOfIntCommittedParam, messageParam, verifierScopeParam,
                ipc, uproveToken, tokenPrivateKey, sessionKeyUser);
        // proof.disclosedAttributes ->
        // presentationtoken:disclosedAttribute:attributevalue
        // proof.ps -> presentationtoken:pseudonym:pseudonymvalue
        // _rest_ of the proof -> cryptoevidence into new xml elements
        // also, uproveToken needs to be added to the cryptoevidence
        System.out.println("Received U-Prove Token proof: " + proof);

        // Verifier verifies the given proof
        System.out.println("Verifying U-Prove Token proof...");
        boolean verifyTokenResult = verfierBinding.verifyTokenProof(proof,
                arrayOfIntDisclosedParam, arrayOfIntCommittedParam,
                messageParam, verifierScopeParam, ipc, uproveToken, sessionKeyVerifier);
        System.out.println("Token Verify result: " + verifyTokenResult);
    }

    private static ArrayOfUProveKeyAndTokenComposite testIssuanceOfTokens(
            Integer numberOfTokensParam, IService1 userBinding,
            IService1 issuerBinding, IssuerParametersComposite ipc,
            ArrayOfstring arrayOfStringAttributesParam, String sessionKeyUser, String sessionKeyIssuer) {

        System.out.println("=== Issuing U-Prove tokens ===");

        // User side asks issuer for first message
        System.out.println("User asks issuer for the first message...");
        // TODO: Map all 3 messages to abc:IssuanceMessage new xml elements
        FirstIssuanceMessageComposite firstMessage = issuerBinding
                .getFirstMessage(arrayOfStringAttributesParam, ipc,
                        numberOfTokensParam, sessionKeyIssuer, null);

        if (firstMessage == null) {
            System.out.println("First message is null.");
            System.exit(1);
        }

        // User side generates second message based on the first message
        System.out
        .println("User generates the second message based on first message...");
        SecondIssuanceMessageComposite secondMessage = userBinding
                .getSecondMessage(arrayOfStringAttributesParam, ipc,
                        numberOfTokensParam, firstMessage, sessionKeyUser);

        if (secondMessage == null) {
            System.out.println("Second message is null.");
            System.exit(1);
        }

        // User side asks issuer for third message based on the second
        // message
        System.out
        .println("User asks issuer for the third message based on the second message...");
        ThirdIssuanceMessageComposite thirdMessage = issuerBinding
                .getThirdMessage(secondMessage, sessionKeyIssuer);

        if (thirdMessage == null) {
            System.out.println("Third message is null.");
            System.exit(1);
        }

        // for (byte[] a :
        // thirdMessage.getSigmaR().getValue().getBase64Binary()) {
        // System.out.println(Arrays.toString(a));
        // }

        // User side generates Tokens based on the third message
        System.out
        .println("User generates tokens based on the third message...");
        // The compositeTokens array is an ABC4Trust Credential!
        ArrayOfUProveKeyAndTokenComposite compositeTokens = userBinding
                .generateTokens(thirdMessage, sessionKeyUser);
        if (compositeTokens == null) {
            System.out.println("User token generation failed!");
        }
        return compositeTokens;
    }

    private static void setIssuerPrivateKeys(IService1 issuerBinding,
            IssuerKeyAndParametersComposite issuerKeyAndParametersComposite,
            String sessionKey) {
        // 'store' the issuer private key.
        byte[] issuerPrivateKey = issuerKeyAndParametersComposite
                .getPrivateKey().getValue();
        // Setup issuerPrivateKey in WebService
        System.out.println("Setting Issuer private key...");
        issuerBinding
                .setIssuerPrivateKey(issuerPrivateKey, sessionKey);
    }

    private static IssuerParametersComposite getIssuerParametersComposite(
            IService1 issuerBinding,
            IssuerKeyAndParametersComposite issuerKeyAndParametersComposite) {
        // fetch the issuer parameters.
        IssuerParametersComposite ipc = issuerKeyAndParametersComposite
                .getIssuerParameters().getValue();

        return ipc;
    }

    public static void verifyIssuerParameters(IService1 binding,
            IssuerParametersComposite ipc, String sessionKey) {
        // Verify the issuer parameters.
        System.out.println("Verifying Issuerparameters...");
        boolean verifiedIssuerParameters = binding.verifyIssuerParameters(ipc, sessionKey);
        System.out.println("Original IssuerParameters Verify result: "
                + verifiedIssuerParameters);
    }

    private static ArrayOfstring getAttributes() {
        ArrayOfstring arrayOfStringAttributesParam = new ArrayOfstring();
        arrayOfStringAttributesParam.getString().add("first attribute value");
        arrayOfStringAttributesParam.getString().add("second attribute value");
        arrayOfStringAttributesParam.getString().add("third attribute value");
        return arrayOfStringAttributesParam;
    }

    private static IService1 createBinding(int port) {
        URL wsdlUrl = WSClientTest.class.getResource("/uprove/WEB-INF/wsdl/abc4trust-uprove.wsdl");

        WebServiceClient ann = Service1.class.getAnnotation(WebServiceClient.class);
        Service1 service = new Service1(wsdlUrl, new QName(ann.targetNamespace(), ann.name()));

        IService1 binding = service.getWSHttpBindingIService1();
        BindingProvider bp = ((BindingProvider) binding);
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://127.0.0.1:" + port + "/abc4trust-webservice/");

        // Test if the UProve WebService is running..
        try {
            String pingResponse = binding.ping();
            if (pingResponse.equals("pong")) {
                System.out.println("Connection to U-Prove Web Service is OK!");
            }
            else {
                System.out
                .println("Received bad response from U-Prove Web Service ping()!");
            }
        }
        catch(com.sun.xml.ws.client.ClientTransportException cte) {
            System.out.println("No Connection to U-Prove Web Service!");
            // Start the UProve Service here
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return binding;
    }
}

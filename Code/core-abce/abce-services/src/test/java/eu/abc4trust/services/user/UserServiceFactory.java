//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.services.user;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.services.AbstractTestFactory;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.URISet;

public class UserServiceFactory extends AbstractTestFactory {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9200/abce-services/user";

    public Pair<CredentialDescription, URI> issueCredential(
            CredentialSpecification credentialSpecification,
            SystemParameters systemParameters, String issuerParametersUid,
            IssuerParameters issuerParameters, Injector issuerInjector,
            IssuancePolicyAndAttributes issuancePolicyAndAttributes)
                    throws CryptoEngineException, IOException, JAXBException,
                    SAXException, KeyManagerException {

        KeyManager keyManager = issuerInjector.getInstance(KeyManager.class);
        keyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);

        keyManager.storeIssuerParameters(issuerParameters.getParametersUID(),
                issuerParameters);

        this.storeSystemParameters(systemParameters);

        this.storeIssuerParameters(issuerParameters);

        this.storeCredentialSpecification(credentialSpecification);

        this.createSmartcard(issuerParameters.getParametersUID());

        URI issuanceLogEntryUid = null;

        IssuerAbcEngine issuer = issuerInjector
                .getInstance(IssuerAbcEngine.class);

        // Init issuance protocol.
        IssuancePolicy issuancePolicy = issuancePolicyAndAttributes.getIssuancePolicy();
        issuancePolicy.getCredentialTemplate().setIssuerParametersUID(
                issuerParameters.getParametersUID());

        List<Attribute> attribute = issuancePolicyAndAttributes.getAttribute();

        IssuanceMessageAndBoolean issuerIssuanceMessage = issuer
                .initIssuanceProtocol(issuancePolicy, attribute);
        assertNotNull(issuerIssuanceMessage);

        issuanceLogEntryUid = issuerIssuanceMessage.getIssuanceLogEntryURI();

        // Reply from user.
        IssuanceReturn userIm = this
                .issuanceProtocolStep(issuerIssuanceMessage
                        .getIssuanceMessage());

        UiIssuanceArguments uia = userIm.uia;
        assertNotNull(uia);

        UiIssuanceReturn uir = new UiIssuanceReturn(uia.uiContext, 0,
                new HashMap<String, PseudonymMetadata>(), 0,
                new LinkedList<String>());
        IssuanceMessage userIssuanceMessage = this.issuanceProtocolStep(uir);

        // int round = 1;
        while (!issuerIssuanceMessage.isLastMessage()) {
            // System.out.println("Issuance round: " + round);

            assertNotNull(userIssuanceMessage);

            // Issuer issuance protocol step.
            issuerIssuanceMessage = issuer
                    .issuanceProtocolStep(userIssuanceMessage);
            assertNotNull(issuerIssuanceMessage);

            issuanceLogEntryUid = issuerIssuanceMessage
                    .getIssuanceLogEntryURI();

            assertNotNull(issuerIssuanceMessage.getIssuanceMessage());
            userIm = this.issuanceProtocolStep(issuerIssuanceMessage
                    .getIssuanceMessage());

            boolean userLastMessage = (userIm.cd != null);
            if(!userLastMessage){
            	userIssuanceMessage = userIm.im;
            }
            assertTrue(issuerIssuanceMessage.isLastMessage() == userLastMessage);
        }

        return new Pair<CredentialDescription, URI>(userIm.cd,
                issuanceLogEntryUid);
    }

    private void createSmartcard(URI parametersUID)
            throws UnsupportedEncodingException {
        String encodedParametersUid = URLEncoder.encode(parametersUID.toString(), "UTF-8");
        String requestString = "/createSmartcard/" + encodedParametersUid;
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        resource.post();
    }

    private IssuanceMessage issuanceProtocolStep(UiIssuanceReturn uir) {
        String requestString = "/issuanceProtocolStepUi/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        IssuanceMessage issuanceMessage = resource.post(IssuanceMessage.class,
                ObjectFactoryReturnTypes.wrap(uir));

        return issuanceMessage;
    }

    private IssuanceReturn issuanceProtocolStep(
            IssuanceMessage issuanceMessage) {
        String requestString = "/issuanceProtocolStep/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        // IssuanceReturn issuanceReturn =
        ClientResponse postRes = resource.post(ClientResponse.class,
                of.createIssuanceMessage(issuanceMessage));

        // BufferedInputStream bis = new BufferedInputStream(postRes
        // .getEntityInputStream());
        // byte[] bs = new byte[1024];
        //
        // int i;
        // StringBuilder sb = new StringBuilder();
        // try {
        // i = bis.read(bs);
        // while (-1 != i) {
        // sb.append(new String(bs));
        // sb.append("\n");
        // i = bis.read(bs);
        // }
        // } catch (IOException ex) {
        // ex.printStackTrace();
        // }
        //
        // System.out.println("" + sb.toString());
        IssuanceReturn issuanceReturn = postRes.getEntity(IssuanceReturn.class);

        return issuanceReturn;
    }

    public void storeSystemParameters(SystemParameters systemParameters) {
        String requestString = "/storeSystemParameters/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        ABCEBoolean b = resource.post(ABCEBoolean.class,
                of.createSystemParameters(systemParameters));
        assertTrue(b.isValue());
    }

    private void storeCredentialSpecification(
            CredentialSpecification credentialSpecification) {

        String credSpecUid;
        try {
            credSpecUid = URLEncoder.encode(credentialSpecification
                    .getSpecificationUID().toString(), "UTF-8");

            String requestString = "/storeCredentialSpecification/" + credSpecUid;
            Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

            ABCEBoolean b = resource.put(ABCEBoolean.class,
                    of.createCredentialSpecification(credentialSpecification));

            assertTrue(b.isValue());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

    }

    private void storeIssuerParameters(IssuerParameters issuerParameters) {

        String uid;
        try {
            uid = URLEncoder.encode(issuerParameters.getParametersUID()
                    .toString(), "UTF-8");

            String requestString = "/storeIssuerParameters/" + uid;
            Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

            ABCEBoolean b = resource.put(ABCEBoolean.class,
                    of.createIssuerParameters(issuerParameters));

            assertTrue(b.isValue());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

    }

    public Pair<CredentialDescription, URI> issuanceProtocol(String engineSuffix,
            CredentialSpecification credentialSpecification,
            String issuerParametersUid,
            IssuancePolicyAndAttributes issuancePolicyAndAttributes)
                    throws CryptoEngineException,
                    IOException, JAXBException, SAXException, KeyManagerException {

        Injector issuerInjector = Guice.createInjector(IntegrationModuleFactory
                .newModule(new Random(1987)));

        IssuerAbcEngine issuerAbcEngine = issuerInjector
                .getInstance(IssuerAbcEngine.class);
        KeyManager issuerKeyManager = issuerInjector
        		.getInstance(KeyManager.class);

        SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();

        issuerKeyManager.storeSystemParameters(systemParameters);
        
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        if(engineSuffix.equals("uprove")){
          cryptoMechanism = CryptoUriUtil.getUproveMechanism();
        }
        
        IssuerParameters issuerParameters = issuerAbcEngine.
            setupIssuerParameters(systemParameters, 10, 
          cryptoMechanism, URI.create(issuerParametersUid), null, 
          new LinkedList<FriendlyDescription>());
        
        Pair<CredentialDescription, URI> p = this.issueCredential(
                credentialSpecification,
                systemParameters, issuerParametersUid,
                issuerParameters, issuerInjector, issuancePolicyAndAttributes);
        assertNotNull(p.first);
        return p;
    }

    public UiPresentationArguments createPresentationToken(
            PresentationPolicyAlternatives presentationPolicyAlternatives) {
        String requestString = "/createPresentationToken/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        // IssuanceReturn issuanceReturn =
        UiPresentationArguments uiPresentationArguments = resource
                .post(UiPresentationArguments.class,
                        of.createPresentationPolicyAlternatives(presentationPolicyAlternatives));

        return uiPresentationArguments;
    }

    public PresentationToken createPresentationToken(
            UiPresentationReturn uiPresentationReturn) {

        String requestString = "/createPresentationTokenUi/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        PresentationToken presentationToken = resource.post(
                PresentationToken.class,
                ObjectFactoryReturnTypes.wrap(uiPresentationReturn));

        return presentationToken;
    }

    public boolean canBeSatisfied(
            PresentationPolicyAlternatives presentationPolicy) {
        String requestString = "/canBeSatisfied/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        ABCEBoolean b = resource.post(ABCEBoolean.class,
                of.createPresentationPolicyAlternatives(presentationPolicy));

        return b.isValue();
    }

    public boolean deleteCredential(URI credentialUid) {
        String credUid;
        try {
            credUid = URLEncoder.encode(credentialUid.toString(), "UTF-8");

            String requestString = "/deleteCredential/" + credUid;
            Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

            ABCEBoolean b = resource.delete(ABCEBoolean.class);

            return b.isValue();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public URISet listCredentials() {
        String requestString = "/listCredentials/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        URISet credentials = resource.get(URISet.class);

        return credentials;
    }

    public void updateNonRevocationEvidence() {
        String requestString = "/updateNonRevocationEvidence/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        resource.post();
    }

    public CredentialDescription getCredentialDescription(URI credentialUid) {
        String credUid;
        try {
            credUid = URLEncoder.encode(credentialUid.toString(), "UTF-8");

            String requestString = "/getCredentialDescription/" + credUid;
            Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

            CredentialDescription credentialDescription = resource
                    .get(CredentialDescription.class);

            return credentialDescription;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}

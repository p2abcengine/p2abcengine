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

package eu.abc4trust.abce.testharness;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class IssuanceHelper {
    private final Random random = new Random(43);

    public IssuanceHelper() {
        super();
    }

    public List<Attribute> populateIssuerAttributes(
            Map<String, Object> issuerAttsMap,
            String credentialSpecificationFilename) throws Exception {
        List<Attribute> issuerAtts = new LinkedList<Attribute>();
        ObjectFactory of = new ObjectFactory();

        CredentialSpecification credentialSpecification = this
                .loadCredentialSpecification(credentialSpecificationFilename);

        for (AttributeDescription attdesc : credentialSpecification
                .getAttributeDescriptions().getAttributeDescription()) {
            Attribute att = of.createAttribute();
            att.setAttributeUID(URI.create("" + this.random.nextLong()));
            URI type = attdesc.getType();
            AttributeDescription attd = of.createAttributeDescription();
            attd.setDataType(attdesc.getDataType());
            attd.setEncoding(attdesc.getEncoding());
            attd.setType(type);
            att.setAttributeDescription(attd);
            Object value = issuerAttsMap.get(type.toString());
            if (value != null) {
                issuerAtts.add(att);
                att.setAttributeValue(value);
            }
        }
        return issuerAtts;
    }

    @Deprecated
    public CredentialDescription issueCredential(String username, Injector issuerInjector,
                                                 Injector userInjector,
                                                 String credentialSpecificationFilename,
                                                 String issuancePolicyFilename, Map<String, Object> issuerAttsMap)
                                                         throws Exception {
      return issueCredential(username, issuerInjector, userInjector, credentialSpecificationFilename, issuancePolicyFilename, issuerAttsMap, null);
    }
    
    public CredentialDescription issueCredential(String username, Injector issuerInjector,
            Injector userInjector,
            String credentialSpecificationFilename,
            String issuancePolicyFilename, Map<String, Object> issuerAttsMap, VerifierParameters verifierParameters)
                    throws Exception {

        List<Attribute> issuerAtts = this.populateIssuerAttributes(
                issuerAttsMap, credentialSpecificationFilename);

        // Step 1. Load the credential specification into the keymanager.
        CredentialSpecification credentialSpecification = this.loadCredentialSpecification(credentialSpecificationFilename);

        KeyManager issuerKeyManager = issuerInjector
                .getInstance(KeyManager.class);
        issuerKeyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);

        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        userKeyManager.storeCredentialSpecification(
                credentialSpecification.getSpecificationUID(),
                credentialSpecification);

        // Step 2. Load the issuance policy and attributes.
        IssuancePolicy ip = (IssuancePolicy) XmlUtils.getObjectFromXML(this
                .getClass().getResourceAsStream(issuancePolicyFilename), true);
        if(verifierParameters != null) {
          ip.setVerifierParameters(verifierParameters);
        }

        // Step 3. Issue credential.
        IssuerAbcEngine issuerEngine = issuerInjector
                .getInstance(IssuerAbcEngine.class);
        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        //For reload of tokens set these on the UProve reloader
        IssuanceHelper testHelper = new IssuanceHelper();
        CredentialDescription cd = testHelper.runIssuanceProtocol(username, issuerEngine,
                userEngine, ip, issuerAtts);
        assertNotNull(cd);

        // ObjectFactory of = new ObjectFactory();
        // JAXBElement<CredentialDescription> xcd = of
        // .createCredentialDescription(cd);
        // System.out.println(XmlUtils.toNormalizedXML(xcd));
        return cd;
    }

    public CredentialSpecification loadCredentialSpecification(
            String credentialSpecificationFilename) throws JAXBException,
            UnsupportedEncodingException, SAXException {
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                credentialSpecificationFilename), true);
        return credentialSpecification;
    }

    public CredentialDescription runIssuanceProtocol(String username, 
            IssuerAbcEngine issuerEngine, UserAbcEngine userEngine,
            IssuancePolicy ip, List<Attribute> issuerAtts)
                    throws Exception {

        // Issuer starts the issuance.
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(
                ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        // Reply from user.
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStepFirstChoice(username, issuerIm
                .getIssuanceMessage());
        
        int round = 1;
        while (!issuerIm.isLastMessage()) {
            System.out.println("Issuance round: " + round);
            assertNotNull(userIm.im);
            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);

            assertNotNull(issuerIm.getIssuanceMessage());
            userIm = userEngine.issuanceProtocolStepFirstChoice(username, issuerIm
                    .getIssuanceMessage());

            boolean userLastMessage = (userIm.cd != null);
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        assertNull(userIm.im);
        assertNotNull(userIm.cd);
        return userIm.cd;
    }

    @Deprecated
    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(
      String username, Injector userInjector,
            String presentationPolicyFilename) throws Exception {
        return this.createPresentationToken(username, userInjector,
                presentationPolicyFilename, null, null);
    }
    
    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(
      String username, Injector userInjector,
            String presentationPolicyFilename, VerifierParameters verifierParameters) throws Exception {
        return this.createPresentationToken(username, userInjector,
                presentationPolicyFilename, null, verifierParameters);
    }
    
    @Deprecated
    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(String username,
      Injector userInjector, String presentationPolicyFilename,
      RevocationInformation revocationInformation) throws Exception {
      return createPresentationToken(username, userInjector, presentationPolicyFilename, revocationInformation, null);
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createSpecificPresentationToken(String username,
    	      Injector userInjector, String presentationPolicyFilename, URI chosenCredential,
    	      RevocationInformation revocationInformation, VerifierParameters verifierParameters) throws Exception {

    	assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = this.loadPresentationPolicy(presentationPolicyFilename);
        if(verifierParameters != null) {
            presentationPolicyAlternatives.setVerifierParameters(verifierParameters);
          } 
        if (revocationInformation != null) {
            for (PresentationPolicy pp : presentationPolicyAlternatives
                    .getPresentationPolicy()) {
                for (CredentialInPolicy cred : pp.getCredential()) {
                    IssuerAlternatives ia = cred.getIssuerAlternatives();
                    for (IssuerParametersUID ipUid : ia
                            .getIssuerParametersUID()) {

                        URI revInfoUid = revocationInformation.getRevocationInformationUID();
                        ipUid.setRevocationInformationUID(revInfoUid);
                    }
                }
            }
        }

        assertNotNull(presentationPolicyAlternatives);

        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);
        
        UiPresentationArguments choices = userEngine
        		.createPresentationToken(username, presentationPolicyAlternatives);
        if(choices == null){
        	throw new RuntimeException("Cannot generate presentationToken");
        }
        
        UiPresentationReturn choiceMade = new UiPresentationReturn(choices);

        choiceMade.chosenPolicy = -1;
        choiceMade.chosenPresentationToken = -1;

        for(TokenCandidatePerPolicy candidate :choices.tokenCandidatesPerPolicy) {
        	for(TokenCandidate tokenCandidate: candidate.tokenCandidates){
        		for(CredentialInUi cre: tokenCandidate.credentials){
        			if(chosenCredential.toString().equals(cre.uri)){
        				choiceMade.chosenPolicy = 
        					choices.tokenCandidatesPerPolicy.indexOf(candidate);
        				choiceMade.chosenPresentationToken = 
        					candidate.tokenCandidates.indexOf(tokenCandidate);
        			}
        		}
        	}
        }
        
        if(choiceMade.chosenPresentationToken == -1){
            throw new RuntimeException("Cannot generate presentationToken with specified credential");
        }
   
        PresentationToken presentationToken = 
        		userEngine.createPresentationToken(username, choiceMade);
        
        if(presentationToken == null) {
            throw new RuntimeException("Cannot generate presentationToken");
        }
        assertNotNull(presentationToken);

        return new Pair<PresentationToken, PresentationPolicyAlternatives>(
                presentationToken, presentationPolicyAlternatives);
    }
    
    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(String username,
            Injector userInjector, String presentationPolicyFilename,
            RevocationInformation revocationInformation,
            VerifierParameters verifierParameters) throws Exception {
        assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = this.loadPresentationPolicy(presentationPolicyFilename);
        if(verifierParameters != null) {
          presentationPolicyAlternatives.setVerifierParameters(verifierParameters);
        }
        
        if (revocationInformation != null) {
            for (PresentationPolicy pp : presentationPolicyAlternatives
                    .getPresentationPolicy()) {
                for (CredentialInPolicy cred : pp.getCredential()) {
                    IssuerAlternatives ia = cred.getIssuerAlternatives();
                    for (IssuerParametersUID ipUid : ia
                            .getIssuerParametersUID()) {

                        URI revInfoUid = revocationInformation.getRevocationInformationUID();
                        ipUid.setRevocationInformationUID(revInfoUid);
                    }
                }
            }
        }

        assertNotNull(presentationPolicyAlternatives);

        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        PresentationToken presentationToken = userEngine
                .createPresentationTokenFirstChoice(username, presentationPolicyAlternatives);
        if(presentationToken == null) {
            throw new RuntimeException("Cannot generate presentationToken");
        }
        assertNotNull(presentationToken);

        return new Pair<PresentationToken, PresentationPolicyAlternatives>(
                presentationToken, presentationPolicyAlternatives);
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken_NotSatisfied(
      String username, Injector userInjector, String presentationPolicyFilename) throws Exception {
        assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = this.loadPresentationPolicy(presentationPolicyFilename);
        assertNotNull(presentationPolicyAlternatives);

        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        PresentationToken presentationToken = userEngine
                .createPresentationTokenFirstChoice(username, presentationPolicyAlternatives);

        assertNull(presentationToken);

        return null;
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken_NotSatisfied(
      String username, Injector userInjector, RevocationInformation revocationInformation,
            String presentationPolicyFilename) throws Exception {
        assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = this.loadPresentationPolicy(presentationPolicyFilename);
        assertNotNull(presentationPolicyAlternatives);

        if (revocationInformation != null) {
            for (PresentationPolicy pp : presentationPolicyAlternatives
                    .getPresentationPolicy()) {
                for (CredentialInPolicy cred : pp.getCredential()) {
                    IssuerAlternatives ia = cred.getIssuerAlternatives();
                    for (IssuerParametersUID ipUid : ia
                            .getIssuerParametersUID()) {

                        URI revInfoUid = revocationInformation.getRevocationInformationUID();
                        ipUid.setRevocationInformationUID(revInfoUid);
                    }
                }
            }
        }

        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        PresentationToken presentationToken = userEngine
                .createPresentationTokenFirstChoice(username, presentationPolicyAlternatives);

        assertNull(presentationToken);

        return null;
    }

    private PresentationPolicyAlternatives loadPresentationPolicy(
            String presentationPolicyFilename) throws JAXBException,
            UnsupportedEncodingException, SAXException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream(
                presentationPolicyFilename);
        assertNotNull(resourceAsStream);
        try {
            PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                    .getObjectFromXML(
                            resourceAsStream, true);
            return presentationPolicyAlternatives;
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public PresentationToken verify(Injector verifierInjector,
            String presentationPolicyAlternatives,
            PresentationToken presentationToken) throws Exception {
    	PresentationPolicyAlternatives ppa = loadPresentationPolicy(presentationPolicyAlternatives);
    	return verify(verifierInjector, ppa, presentationToken);
    }
    
    public PresentationToken verify(Injector verifierInjector,
            PresentationPolicyAlternatives presentationPolicyAlternatives,
            PresentationToken presentationToken) throws Exception {
        VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);

        assertNotNull(presentationPolicyAlternatives);
        assertNotNull(presentationToken);

        PresentationTokenDescription pte = verifierEngine
                .verifyTokenAgainstPolicy(presentationPolicyAlternatives,
                        presentationToken, true);
        assertNotNull(pte);

        return presentationToken;
    }

    public Credential loadCredential(String credentialFilename)
            throws Exception {
        Credential credential = (Credential) XmlUtils.getObjectFromXML(this
                .getClass().getResourceAsStream(credentialFilename), true);
        return credential;
    }

    public void revokeCredential(Injector revocationInjector, URI revParamsUid,
            Attribute revocationHandleAttribute) throws CryptoEngineException {
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        List<Attribute> attributes = new LinkedList<Attribute>();
        attributes.add(revocationHandleAttribute);
        revocationEngine.revoke(revParamsUid, attributes);
    }
    
    public RevocationInformation getRevocationInformation(Injector revocationInjector,
          URI raParametersUID) throws CryptoEngineException {
        RevocationAbcEngine revocationEngine =
            revocationInjector.getInstance(RevocationAbcEngine.class);
        return revocationEngine.updateRevocationInformation(raParametersUID);
    }
    
    public BigInt getAccumulatorValue(Injector revocationInjector, URI raParametersUID)
        throws CryptoEngineException, ConfigurationException {
      RevocationInformation revocationInformation =
          getRevocationInformation(revocationInjector, raParametersUID);
      RevocationInformationFacade revocationInformationFacade =
          new RevocationInformationFacade(revocationInformation);
      ClRevocationStateWrapper revocationStateWrapper =
          new ClRevocationStateWrapper(revocationInformationFacade.getRevocationState());
      return revocationStateWrapper.getAccumulatorValue();
    }

}

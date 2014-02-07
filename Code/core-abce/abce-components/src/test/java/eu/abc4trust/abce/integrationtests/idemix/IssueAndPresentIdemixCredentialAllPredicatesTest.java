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

package eu.abc4trust.abce.integrationtests.idemix;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Test for simple issuance of idmx credential
 * and then presenting it (prove different predicates)
 * @author mdu
 */
public class IssueAndPresentIdemixCredentialAllPredicatesTest {

    private static boolean DEBUG = true;
    private UserAbcEngine userEngine;
    private IssuerAbcEngine issuerEngine;
    private VerifierAbcEngine verifierEngine;

    @Before
    public void Setup() throws Exception{

        //---------------------------------------------------
        //Setup all instances
        //---------------------------------------------------


        ObjectFactory of = new ObjectFactory();
        Module userModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector userInjector = Guice.createInjector(userModule);
        userEngine = userInjector.getInstance(UserAbcEngine.class);

        Module issuerModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector issuerInjector = Guice.createInjector(issuerModule);
        issuerEngine = userInjector.getInstance(IssuerAbcEngine.class);

        Module verifierModule = IdemixIntegrationModuleFactory.newModule(new Random(1231));
        Injector verifierInjector = Guice.createInjector(verifierModule);
        verifierEngine = verifierInjector.getInstance(VerifierAbcEngine.class);


        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager verifierKeyManager = verifierInjector.getInstance(KeyManager.class);

        //---------------------------------------------------
        //Issue a credential
        //---------------------------------------------------

        //read all objects


        CredentialSpecification creditCardSpec =
                (CredentialSpecification) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/credentialSpecificationCreditcardVisaAllPredicatesTest.xml"), true);
        
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createCredentialSpecification(creditCardSpec)));
        }

        //store cred spec
        if (!issuerKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (issuer)");
        }

        if (!userKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (user)");
        }

        if (!verifierKeyManager.storeCredentialSpecification(creditCardSpec.getSpecificationUID(), creditCardSpec)) {
            throw new RuntimeException("Cred spec was not stored (verifier)");
        }

        // load issuance policy
        IssuancePolicy ip =
                (IssuancePolicy) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                        "/eu/abc4trust/sampleXml/idemixIntegration/issuancePolicyCreditcardVisa.xml"), true);
        
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createIssuancePolicy(ip)));
        }

        // Load secret
        Secret secret = (Secret) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml"), true);
        userInjector.getInstance(CredentialManager.class).storeSecret(secret);

        List<Attribute> issuerAtts = new ArrayList<Attribute>();
        this.populateAttributes(issuerAtts);

        //just an additional test that cred spec was stored
        CredentialSpecification credSpec=null;
        try {
            credSpec = issuerKeyManager.getCredentialSpecification(creditCardSpec.getSpecificationUID());
            System.out.println(XmlUtils.toNormalizedXML(of.createCredentialSpecification(credSpec)));
        } catch (KeyManagerException e1) {
            e1.printStackTrace();
        }

        // create all URIs

        int keyLength = 1024; // TODO: define the security level & revocation
        URI cryptoMechanism = CryptoUriUtil.getIdemixMechanism();
        URI uid = ip.getCredentialTemplate().getIssuerParametersUID();
        URI hash = CryptoUriUtil.getHashSha256();
        URI revocationId = new URI("revocationUID");

        //step 1 - generate system parameters

        SystemParameters sysParams = issuerEngine.setupSystemParameters(keyLength, cryptoMechanism);
        
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createSystemParameters(sysParams)));
        }

        //step 2 - generate issuer parameters

        IssuerParameters issuerParameters = issuerEngine.setupIssuerParameters(creditCardSpec, sysParams, uid, hash, cryptoMechanism, revocationId, null);

        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createIssuerParameters(issuerParameters)));
        }
        
        // store parameters for all parties:
        issuerKeyManager.storeIssuerParameters(uid, issuerParameters);
        userKeyManager.storeIssuerParameters(uid, issuerParameters);
        verifierKeyManager.storeIssuerParameters(uid, issuerParameters);

        userKeyManager.storeSystemParameters(sysParams);
        
        // Issuer starts the issuance
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(ip, issuerAtts);
        assertFalse(issuerIm.isLastMessage());

        if (DEBUG) {
            System.out.println(XmlUtils.toXml((new ObjectFactory())
                    .createIssuanceMessage(issuerIm.getIssuanceMessage())));
        }

        // Reply from user
        IssuMsgOrCredDesc userIm = userEngine.issuanceProtocolStep(issuerIm
                .getIssuanceMessage());
        if (DEBUG) {
            System.out.println(XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
        }


        CredentialDescription cd = null;

        // Ping-pong until both user and issuer finish
        while (!issuerIm.isLastMessage()) {

            issuerIm = issuerEngine.issuanceProtocolStep(userIm.im);
            
            if (DEBUG) {
                System.out.println(XmlUtils.toXml(of.createIssuanceMessageAndBoolean(issuerIm)));
            }


            userIm = userEngine.issuanceProtocolStep(issuerIm
                    .getIssuanceMessage());
            

            boolean userLastMessage = (userIm.cd != null);
            
           
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
        }
        cd = userIm.cd;

        String cds = XmlUtils.toXml(of.createCredentialDescription(cd));
        System.out.println(cds);
    }


        //---------------------------------------------------
        //Present a credential
        //---------------------------------------------------
        
        @Test
        public void presentCredentialTest() throws Exception{

        runPresentation("/eu/abc4trust/sampleXml/idemixIntegration/presentationPolicyCreditCardIdmxTestAll.xml");        
        runPresentation("/eu/abc4trust/sampleXml/idemixIntegration/presentationPolicyCreditCardIdmxTestDisclosedOnly.xml");
        runPresentation("/eu/abc4trust/sampleXml/idemixIntegration/presentationPolicyCreditCardIdmxTestImplicitlyRevealed.xml");
        runPresentation("/eu/abc4trust/sampleXml/idemixIntegration/presentationPolicyCreditCardIdmxTestConstantPredicate.xml");
        runPresentation("/eu/abc4trust/sampleXml/idemixIntegration/presentationPolicyCreditCardIdmxTestRangeProof.xml");


    }
    
    private void runPresentation(String policyName) throws Exception{
      
      ObjectFactory of = new ObjectFactory();
      
      //Load the policy
      PresentationPolicyAlternatives ppa =
          (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(this.getClass().getResourceAsStream(policyName), true);
  
      if (DEBUG) {
        System.out.println(XmlUtils.toXml(of.createPresentationPolicyAlternatives(ppa)));
      }

      //Create PresentationToken
      PresentationToken presentationToken = userEngine.createPresentationToken(ppa);

      if (DEBUG) {
        String pts = XmlUtils.toXml(of.createPresentationToken(presentationToken));
        System.out.println(pts);
      }
      
      //Verify the generated PresentationToken
      PresentationTokenDescription ptd = verifierEngine.verifyTokenAgainstPolicy(ppa, presentationToken, false);
      String ptds = XmlUtils.toXml(of.createPresentationTokenDescription(ptd));
      System.out.println(ptds);
    }

    private void populateAttributes(List<Attribute> issuerAtts) throws Exception {
        ObjectFactory of = new ObjectFactory();

        Attribute status = of.createAttribute();
        status.setAttributeUID(new URI("Visa:Status"));
        status.setAttributeValue(new URI("Gold"));
        status.setAttributeDescription(of.createAttributeDescription());
        status.getAttributeDescription().setDataType(new URI("xs:anyURI"));
        status.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:anyUri:sha-256"));
        status.getAttributeDescription().setType(new URI("CardStatus"));
        issuerAtts.add(status);

        Attribute cardType = of.createAttribute();
        cardType.setAttributeUID(new URI("Visa:CardType"));
        cardType.setAttributeValue("SwissExpress");
        cardType.setAttributeDescription(of.createAttributeDescription());
        cardType.getAttributeDescription().setDataType(new URI("xs:string"));
        cardType.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        cardType.getAttributeDescription().setType(new URI("CardType"));
        issuerAtts.add(cardType);

        Attribute securityCode = of.createAttribute();
        securityCode.setAttributeUID(new URI("Visa:SecurityCode"));
        securityCode.setAttributeValue(42);
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);
        
/*        Attribute securityCode2 = of.createAttribute();
        securityCode2.setAttributeUID(new URI("security:code"));
        securityCode2.setAttributeValue(44);
        securityCode2.setAttributeDescription(of.createAttributeDescription());
        securityCode2.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode2.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode2.getAttributeDescription().setType(new URI("SecurityCode2"));
        issuerAtts.add(securityCode2);
        */
        
        Attribute expires = of.createAttribute();
        expires.setAttributeUID(new URI("Visa:Expires"));
        expires.setAttributeValue("2014-02-06Z");
        expires.setAttributeDescription(of.createAttributeDescription());
        expires.getAttributeDescription().setDataType(new URI("xs:date"));
        expires.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:date:unix:signed"));
        expires.getAttributeDescription().setType(new URI("Expires"));
        issuerAtts.add(expires);
        
        Attribute number = of.createAttribute();
        number.setAttributeUID(new URI("Visa:Number"));
        number.setAttributeValue("1234 5678 1234 4568 7456");
        number.setAttributeDescription(of.createAttributeDescription());
        number.getAttributeDescription().setDataType(new URI("xs:string"));
        number.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        number.getAttributeDescription().setType(new URI("CardNumber"));
        issuerAtts.add(number);
        
        Attribute owner = of.createAttribute();
        owner.setAttributeUID(new URI("Visa:Owner"));
        owner.setAttributeValue("Alice Davis");
        owner.setAttributeDescription(of.createAttributeDescription());
        owner.getAttributeDescription().setDataType(new URI("xs:string"));
        owner.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        owner.getAttributeDescription().setType(new URI("CardOwner"));
        issuerAtts.add(owner);
        
        Attribute owner2 = of.createAttribute();
        owner2.setAttributeUID(new URI("Visa:Owner2"));
        owner2.setAttributeValue("Alice Davis");
        owner2.setAttributeDescription(of.createAttributeDescription());
        owner2.getAttributeDescription().setDataType(new URI("xs:string"));
        owner2.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        owner2.getAttributeDescription().setType(new URI("CardOwner2"));
        issuerAtts.add(owner2);
        
        Attribute electron = of.createAttribute();
        electron.setAttributeUID(new URI("Visa:Electron"));
        electron.setAttributeValue(true);
        electron.setAttributeDescription(of.createAttributeDescription());
        electron.getAttributeDescription().setDataType(new URI("xs:boolean"));
        electron.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:boolean:unsigned"));
        electron.getAttributeDescription().setType(new URI("Electron"));
        issuerAtts.add(electron);
        
  /*      Attribute time = of.createAttribute();
        securityCode.setAttributeUID(new URI("security:time"));
        securityCode.setAttributeValue("");
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);
        */
    }




}

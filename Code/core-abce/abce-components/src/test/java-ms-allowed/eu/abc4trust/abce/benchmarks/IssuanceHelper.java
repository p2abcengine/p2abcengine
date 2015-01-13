//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.abce.benchmarks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.util.XmlUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class IssuanceHelper {
    private final Random random = new Random(43);
    private static String currentTest = "";
    
    private final String USERNAME = "username";

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

    public CredentialDescription issueCredential(Injector issuerInjector,
            Injector userInjector,
            String credentialSpecificationFilename,
            String issuancePolicyFilename, Map<String, Object> issuerAttsMap, String filename)
                    throws Exception {

    	IssuanceHelper.currentTest = filename;
        List<Attribute> issuerAtts = this.populateIssuerAttributes(
                issuerAttsMap, credentialSpecificationFilename);

        // Step 1. Load the credential specification into the keymanager.
        CredentialSpecification credentialSpecification = 
        		this.loadCredentialSpecification(credentialSpecificationFilename);

        KeyManager issuerKeyManager = issuerInjector.getInstance(KeyManager.class);
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
        if (this.isSimpleIssuance(ip)){
        	System.out.println("***************This is a simple issuance policy:  " + ip.getCredentialTemplate().getIssuerParametersUID());
        }else {
        	System.out.println("***************This is an advanced issuance policy:  " + ip.getCredentialTemplate().getIssuerParametersUID());
        }
        // Step 3. Issue credential.
        IssuerAbcEngine issuerEngine = issuerInjector.getInstance(IssuerAbcEngine.class);
        UserAbcEngine userEngine = userInjector.getInstance(UserAbcEngine.class);

        //For reload of tokens set these on the UProve reloader
        CredentialDescription cd = this.runIssuanceProtocol(
        		issuerEngine, userEngine, ip, issuerAtts);
        
        assertNotNull(cd);
        return cd;
    }

    public CredentialSpecification loadCredentialSpecification(
            String credentialSpecificationFilename) throws JAXBException,
            UnsupportedEncodingException, SAXException {
    	
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(this.getClass().getResourceAsStream(
                		credentialSpecificationFilename), true);
        return credentialSpecification;
    }
    
    public CredentialDescription runIssuanceProtocol(
            IssuerAbcEngine issuerEngine, UserAbcEngine userEngine,
            IssuancePolicy ip, List<Attribute> issuerAtts) throws Exception{
    	String specId = ip.getCredentialTemplate().getCredentialSpecUID().toString();

    	String filename = currentTest;
    	
    	File file;
    	if (filename.length() > 1){
    		file = new File(filename + "_COMMUNICATION_SIZES.txt");
    	} else { 
    		file = new File("RandomFileName.txt");
    	}
    	if (!file.exists()){ 
    		file.createNewFile();
    	}
    	System.out.println("Created the file: " + file.getName());
    	
    	FileWriter fw = new FileWriter(file.getAbsoluteFile());
    	BufferedWriter writer = new BufferedWriter(fw);

    	writer.write("Issuance policy id: " + specId + "\n");
        // Issuer starts the issuance.
    	
        IssuanceMessageAndBoolean issuerIm = issuerEngine.initIssuanceProtocol(
                ip, issuerAtts);
        IssuanceReturn userImOrCredDesc = userEngine.issuanceProtocolStep(USERNAME, issuerIm
                .getIssuanceMessage());
        ObjectFactory of = new ObjectFactory();
        JAXBElement<IssuanceMessage> userActualMsg = null;
        JAXBElement<IssuanceMessage>  issuerActualMsg = null;

        int round = 1;
        while (userImOrCredDesc.im != null) 
        {
        	issuerActualMsg = of.createIssuanceMessage(issuerIm.getIssuanceMessage());
        	userActualMsg = of.createIssuanceMessage(userImOrCredDesc.im  );
        	
        	writer.write("\nIssuer round: " + round + "\n");
            System.out.println("Issuance round: " + round);
            
            try{
            	//write the current messages in the file
                writer.write("\n\tIssuer's msg (" + 
    					sizeof(XmlUtils.toNormalizedXML(issuerActualMsg))+" Bytes):\n");

                writer.write("\n\tUser's msg (" 
                		+ sizeof(XmlUtils.toNormalizedXML(userActualMsg))+" Bytes):\n");                			
                assertNotNull(userImOrCredDesc.im);
            }catch(Exception e){
            	System.out.println(e.getMessage());
            }
            
            //fetch the new messages on both sides
            issuerIm = issuerEngine.issuanceProtocolStep(userImOrCredDesc.im);
            userImOrCredDesc = userEngine.issuanceProtocolStep(USERNAME, issuerIm.getIssuanceMessage());
            
            boolean userLastMessage = (userImOrCredDesc.cd != null);
            assertTrue(issuerIm.isLastMessage() == userLastMessage);
            round ++;
        }
        if (userImOrCredDesc.cd != null) {
        	 
        	 writer.write("\nLast issuance messsage from the Issuer -  ("
        			 + sizeof(XmlUtils.toNormalizedXML(
        					 of.createIssuanceMessage(issuerIm.getIssuanceMessage()) ) ) 
        			 + " Bytes):\n");	 
        }
        writer.write("\n\n END of the protocol.");
        
        assertNull(userImOrCredDesc.im);
        assertNotNull(userImOrCredDesc.cd);
        writer.close();
        return userImOrCredDesc.cd;
    }
    
    private boolean isSimpleIssuance(IssuancePolicy ip) {
    	PresentationPolicy pp = ip.getPresentationPolicy();

    	if (pp != null) {
    		if (pp.getMessage() != null) {
    			return false;
    		} else if (pp.getPseudonym().size() > 0) {
    			return false;
    		} else if (pp.getCredential().size() > 0) {
    			return false;
    		} else if (pp.getAttributePredicate().size() > 0) {
    			return false;
    		} else if (pp.getVerifierDrivenRevocation().size() > 0) {
    			return false;
    		}
    	}

    	CredentialTemplate ct = ip.getCredentialTemplate();
    	if (ct.getUnknownAttributes() != null) {
    		return false;
    	} else if (ct.getSameKeyBindingAs() != null) {
    		return false;
    	}

    	return true;
    }


    public static int sizeof(Object obj) throws IOException {

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteOutputStream.toByteArray().length;
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(
            Injector verfierInjector, Injector userInjector,
            String presentationPolicyFilename) throws Exception {

    	return this.createPresentationToken(verfierInjector, userInjector,
                presentationPolicyFilename, null);
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken(
            Injector verfierInjector, Injector userInjector,
            String presentationPolicyFilename,
            RevocationInformation revocationInformation) throws Exception {

    	assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = this.loadPresentationPolicy(presentationPolicyFilename);

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
                .createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);
        if(presentationToken == null) {
            throw new RuntimeException("Cannot generate presentationToken");
        }
        assertNotNull(presentationToken);

        return new Pair<PresentationToken, PresentationPolicyAlternatives>(
                presentationToken, presentationPolicyAlternatives);
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken_NotSatisfied(
            Injector verfierInjector, Injector userInjector,
            String presentationPolicyFilename) throws Exception {
        assertNotNull(presentationPolicyFilename);
        PresentationPolicyAlternatives presentationPolicyAlternatives = 
        		this.loadPresentationPolicy(presentationPolicyFilename);
        assertNotNull(presentationPolicyAlternatives);

        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        PresentationToken presentationToken = userEngine
                .createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);

        assertNull(presentationToken);

        return null;
    }

    public Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationToken_NotSatisfied(
            Injector verfierInjector, Injector userInjector, RevocationInformation revocationInformation,
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
                .createPresentationTokenFirstChoice(USERNAME, presentationPolicyAlternatives);

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

    public PresentationTokenDescription verify2(Injector verfierInjector,
            PresentationPolicyAlternatives presentationPolicyAlternatives,
            PresentationToken presentationToken) throws Exception {
        VerifierAbcEngine verifierEngine = verfierInjector
                .getInstance(VerifierAbcEngine.class);

        assertNotNull(presentationPolicyAlternatives);
        assertNotNull(presentationToken);

        PresentationTokenDescription pte = verifierEngine
                .verifyTokenAgainstPolicy(presentationPolicyAlternatives,
                        presentationToken, true);
        assertNotNull(pte);

        return pte;
    }
    
    public PresentationToken verify(Injector verfierInjector,
            PresentationPolicyAlternatives presentationPolicyAlternatives,
            PresentationToken presentationToken) throws Exception {
        VerifierAbcEngine verifierEngine = verfierInjector
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
}

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

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.BridgingModuleFactory;
import eu.abc4trust.abce.testharness.BridgingModuleFactory.IssuerCryptoEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * This test checks 3 things:
 * That a user can get issued a simple identity card with firstname, lastname, and birthday.
 * That the cryptoengines can handle inspectable attributes.
 * That the inspection engine can decrypt an inspectable attribute of a presentationtoken. 
 */
public class InspectionTest {

    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml";
    private static final String CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    private static final String CREDENTIAL_SPECIFICATION_STUDENT_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    private static final String ISSUANCE_POLICY_STUDENT_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicyStudentCard.xml";
    private static final String PRESENTATION_POLICY_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycardWithInspection.xml";
    private static final String PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_SAME_INSPECTOR = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyMultipleAttributesInspection.xml";
    private static final String PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_DIFFERENT_INSPECTORS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyMultipleAttributesDifferentInspectors.xml";
    private static final String PRESENTATION_POLICY_SAME_ATTRIBUTE_MULTIPLE_INSPECTORS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyMultipleInspectorsSingleAttribute.xml";
    private static final String PRESENTATION_POLICY_INSPECT_AND_REVOKABLE = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyInspectionAndRevocation.xml";
    private static final String PRESENTATION_POLICY_TWO_CREDS_SAME_ATTRIBUTE = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyInspectSameAttributeTwoCredentials.xml";
    
    private static final URI INSPECTOR_URI = URI.create("http://thebestbank.com/inspector/pub_key_v1");
    private static final URI SECOND_INSPECTOR_URI = URI.create("http://inspector.com/inspector/pub_key_v1");
    private static final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");
    private static final URI REVOCATION_PARAMETERS_UID2 = URI.create("revocationUID2");
    
  //  @Ignore
    @Test
    public void simpleInspectionIdemixTest() throws Exception {
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsSingle(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), issuanceHelper, PRESENTATION_POLICY_CREDENTIALS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        } 
    }

   // @Ignore
    @Test
    public void simpleInspectionUProveTest() throws Exception {
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsSingle(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), issuanceHelper, PRESENTATION_POLICY_CREDENTIALS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    @Ignore
    @Test
    public void multiInspectionUProveTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsMulti(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), issuanceHelper, PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_SAME_INSPECTOR, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }

    }
    
   // @Ignore
    @Test
    public void multiInspectionIdemixTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD,issuanceHelper);
        
        this.runTestsMulti(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), issuanceHelper, PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_SAME_INSPECTOR, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
   // @Ignore
    @Test
    public void revocationInspectionIdemixTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsRevocation(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(4), issuanceHelper, PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }

    @Ignore
    @Test
    public void revocationInspectionUProveTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsRevocation(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(4), issuanceHelper, PRESENTATION_POLICY_INSPECT_AND_REVOKABLE, CREDENTIAL_SPECIFICATION_REVOKABLE_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    @Test
    public void idemixTwoAttributesTwoInspectorsTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsDifferentInspectors(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_DIFFERENT_INSPECTORS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }

    @Ignore
    @Test
    public void uproveTwoAttributesTwoInspectorsTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsDifferentInspectors(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_MULTIPLE_ATTRIBUTES_DIFFERENT_INSPECTORS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    @Test
    public void idemixTwoCredentialSameAttributeNameTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsTwoCredSameAttribute(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_TWO_CREDS_SAME_ATTRIBUTE);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }

    @Ignore
    @Test
    public void uproveTwoCredentialSameAttributeNameTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsTwoCredSameAttribute(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_TWO_CREDS_SAME_ATTRIBUTE);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    
    @Ignore //this test only makes sense in relation to the uiselection
    @Test //need to change presentation policy to make the test work
    public void multiInspectionSameAttributeUProveTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper(); 
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.UPROVE, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD, issuanceHelper);
        
        this.runTestsMultiSame(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_SAME_ATTRIBUTE_MULTIPLE_INSPECTORS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    @Ignore //this test only makes sense in relation to the uiselection
    @Test
    public void multiInspectionSameAttributeIdemixTest() throws Exception{
        IssuanceHelper issuanceHelper = new IssuanceHelper();
    	List<Injector> injectors = this.setupEngines(IssuerCryptoEngine.IDEMIX, CREDENTIAL_SPECIFICATION_ID_CARD, CREDENTIAL_SPECIFICATION_STUDENT_CARD,issuanceHelper);
        
        this.runTestsMultiSame(injectors.get(0), injectors.get(1), injectors.get(2), injectors.get(3), injectors.get(5), issuanceHelper, PRESENTATION_POLICY_SAME_ATTRIBUTE_MULTIPLE_INSPECTORS, CREDENTIAL_SPECIFICATION_ID_CARD);
        int exitCode = 0;
        for(Injector i: injectors){
        	exitCode = i.getInstance(UProveBindingManager.class)
                    .stop();
            assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
        }
    }
    
    
    /**
     * 	I believe we can improve our confidence in the correctness of the design
		if the following unit tests were written:
    	
    	- Recover an attribute that is not an integer (e.g. UTF-8 string, or
		a date). For now, just make sure we recover the same encoded value;
		later we will want to recover the original value.
		*** DONE, checking a sha-256 string and utf-8 string in multitest
		
    	- Two inspectable attributes from the same credential (same inspector)
    	*** DONE
    	
    	- Two inspectable attributes from different credentials, but with
		the same attribute name (e.g., a token with 2 credit cards) (same
		inspector)
		*** DONE
		
    	- Two different attributes are inspected by different inspectors
    	*** DONE
    	
    	- The same attribute is inspected by different inspectors.
    	* 
    	
    	- An attribute is inspected during issuance
    	* ONLY MEANINGFUL FOR IDEMIX
    	
    	- Added: try with inspectable revocation formation. 
    	*** DONE
     */
    
    
    public List<Injector> setupEngines(IssuerCryptoEngine chosenEngine, String credentialSpecification, String secondCredSpec, IssuanceHelper issuanceHelper) throws Exception{

    	List<Injector> injectors = new ArrayList<Injector>();
        UProveUtils uproveUtils = new UProveUtils();

   /*     Injector revocationInjector = Guice
                .createInjector(new BridgingTestModule(new Random(1231),
                        CryptoEngine.IDEMIX,22999)); 
     */
        Injector revocationInjector = Guice
        			.createInjector(BridgingModuleFactory.newModule(new Random(1231),
        					uproveUtils.getIssuerServicePort()));
        
        RevocationProxyAuthority revocationProxyAuthority = revocationInjector
                .getInstance(RevocationProxyAuthority.class);

        //Construct issuers
        Injector governmentInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        chosenEngine, uproveUtils.getIssuerServicePort(), revocationProxyAuthority));
                
        //Construct user
        Injector userInjector = Guice.createInjector(BridgingModuleFactory.newModule(
                new Random(1987), uproveUtils.getUserServicePort(), revocationProxyAuthority));

        
        //Construct verifier
        Injector hotelInjector = Guice
                .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                        uproveUtils.getVerifierServicePort(), revocationProxyAuthority)); 

        //Construct inspector
        Injector inspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
                uproveUtils.getInspectorServicePort(), revocationProxyAuthority)); 

        //Construct inspector2
        Injector secondInspectorInjector = Guice.createInjector(BridgingModuleFactory.newModule(new Random(1231),
                uproveUtils.getInspectorServicePort(), revocationProxyAuthority)); 

        
        injectors.add(governmentInjector);
        injectors.add(userInjector);
        injectors.add(hotelInjector);
        injectors.add(inspectorInjector);
        injectors.add(revocationInjector);
        injectors.add(secondInspectorInjector);

        int keyLength = 1024;
        if(chosenEngine.equals(IssuerCryptoEngine.UPROVE)) keyLength = 2048;

        IssuerAbcEngine governmentEngine = governmentInjector
                .getInstance(IssuerAbcEngine.class);
        
        // Generate system parameters.
        SystemParameters systemParameters = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());
        } else {
            systemParameters = governmentEngine
                    .setupSystemParameters(keyLength, CryptoUriUtil.getUproveMechanism());

            Injector idemixInjector = Guice
                    .createInjector(BridgingModuleFactory.newModule(new Random(1231),
                            IssuerCryptoEngine.IDEMIX, 32123));
            IssuerAbcEngine idemixIssuer = idemixInjector.getInstance(IssuerAbcEngine.class);
            SystemParameters idemixSystemParameters = idemixIssuer
                    .setupSystemParameters(keyLength, CryptoUriUtil.getIdemixMechanism());

            systemParameters.getAny().addAll(idemixSystemParameters.getAny());
        }


        KeyManager inspectorKeyManager =inspectorInjector.getInstance(KeyManager.class);
        KeyManager secondInspectorKeyManager =secondInspectorInjector.getInstance(KeyManager.class);
        

        KeyManager governmentKeyManager = governmentInjector
                .getInstance(KeyManager.class);
        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        KeyManager hotelKeyManager = hotelInjector
                .getInstance(KeyManager.class);
        KeyManager revocationKeyManager = revocationInjector
                .getInstance(KeyManager.class);
        
        governmentKeyManager.storeSystemParameters(systemParameters);
        userKeyManager.storeSystemParameters(systemParameters);
        hotelKeyManager.storeSystemParameters(systemParameters);
        inspectorKeyManager.storeSystemParameters(systemParameters);
        secondInspectorKeyManager.storeSystemParameters(systemParameters);
        revocationKeyManager.storeSystemParameters(systemParameters);

        CryptoEngineInspector inspectorEngine = inspectorInjector.getInstance(CryptoEngineInspector.class);
        CryptoEngineInspector secondInspectorEngine = secondInspectorInjector.getInstance(CryptoEngineInspector.class);

        
        RevocationAbcEngine revocationEngine = revocationInjector
                .getInstance(RevocationAbcEngine.class);
        URI revParamsUid = REVOCATION_PARAMETERS_UID;
        Reference revocationInfoReference = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid,
                        revocationInfoReference,
                        nonRevocationEvidenceReference,
                        nonRrevocationUpdateReference);

        URI revParamsUid2 = REVOCATION_PARAMETERS_UID2;
        Reference revocationInfoReference2 = new Reference();
        revocationInfoReference.setReferenceType(URI.create("https"));
        revocationInfoReference.getReferences().add(URI.create("example.org"));
        Reference nonRevocationEvidenceReference2 = new Reference();
        nonRevocationEvidenceReference.setReferenceType(URI.create("https"));
        nonRevocationEvidenceReference.getReferences().add(URI.create("example.org"));
        Reference nonRrevocationUpdateReference2 = new Reference();
        nonRrevocationUpdateReference.setReferenceType(URI.create("https"));
        nonRrevocationUpdateReference.getReferences().add(URI.create("example.org"));
        RevocationAuthorityParameters revocationAuthorityParameters2 = revocationEngine
                .setupRevocationAuthorityParameters(keyLength,
                        CryptoUriUtil.getIdemixMechanism(), revParamsUid2,
                        revocationInfoReference2,
                        nonRevocationEvidenceReference2,
                        nonRrevocationUpdateReference2);
        
        // Store revocationauthority parameters
        governmentKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        hotelKeyManager.storeRevocationAuthorityParameters(revParamsUid,
                revocationAuthorityParameters);
        
        governmentKeyManager.storeRevocationAuthorityParameters(revParamsUid2,
                revocationAuthorityParameters2);
        userKeyManager.storeRevocationAuthorityParameters(revParamsUid2,
                revocationAuthorityParameters2);
        hotelKeyManager.storeRevocationAuthorityParameters(revParamsUid2,
                revocationAuthorityParameters2);        
        

        // Setup issuance policies.
        IssuancePolicy idcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_ID_CARD), true);
        URI idcardIssuancePolicyUid = idcardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();

        IssuancePolicy studentcardIssuancePolicy = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                ISSUANCE_POLICY_STUDENT_CARD), true);
        URI studentcardIssuancePolicyUid = studentcardIssuancePolicy
                .getCredentialTemplate().getIssuerParametersUID();
        

        // Load credential specifications.
        CredentialSpecification idcardCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                credentialSpecification), true);

        CredentialSpecification studentcardCredSpec = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                secondCredSpec), true);
        
        // Store credential specifications.
        governmentKeyManager.storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

        userKeyManager.storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

        hotelKeyManager.storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

        inspectorKeyManager.storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);
        
        secondInspectorKeyManager.storeCredentialSpecification(
                idcardCredSpec.getSpecificationUID(), idcardCredSpec);

        //store studentcard
        governmentKeyManager.storeCredentialSpecification(
                studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        userKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        hotelKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        inspectorKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        secondInspectorKeyManager.storeCredentialSpecification(
        		studentcardCredSpec.getSpecificationUID(), studentcardCredSpec);
        
        // Generate issuer parameters.
        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");
        URI engineType = null;
        if(chosenEngine==IssuerCryptoEngine.IDEMIX){
            engineType = URI.create("Idemix");
        } else {
            engineType = URI.create("Uprove");
        }

        URI revocationId = new URI("revocationUID1");
        IssuerParameters governmentIdcardIssuerParameters = governmentEngine
                .setupIssuerParameters(idcardCredSpec, systemParameters,
                        idcardIssuancePolicyUid, hash, engineType, revocationId, null);


        // store issuance parameters for government and user.
        governmentKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                governmentIdcardIssuerParameters);
        userKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                governmentIdcardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(idcardIssuancePolicyUid,
                governmentIdcardIssuerParameters);

        URI revocationId2 = new URI("revocationUID2");
        IssuerParameters governmentStudentcardIssuerParameters = governmentEngine
                .setupIssuerParameters(studentcardCredSpec, systemParameters,
                        studentcardIssuancePolicyUid, hash, engineType, revocationId2, null);


        // store issuance parameters for government and user.
        governmentKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                governmentStudentcardIssuerParameters);
        userKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                governmentStudentcardIssuerParameters);
        hotelKeyManager.storeIssuerParameters(studentcardIssuancePolicyUid,
                governmentStudentcardIssuerParameters);

        
        InspectorPublicKey inspectorPubKey = inspectorEngine.setupInspectorPublicKey(2048,
                            CryptoUriUtil.getIdemixMechanism(),
                            InspectionTest.INSPECTOR_URI);
        inspectorKeyManager.storeInspectorPublicKey(InspectionTest.INSPECTOR_URI, inspectorPubKey);
        userKeyManager.storeInspectorPublicKey(InspectionTest.INSPECTOR_URI, inspectorPubKey);
        hotelKeyManager.storeInspectorPublicKey(InspectionTest.INSPECTOR_URI, inspectorPubKey);
            
        InspectorPublicKey secondInspectorPubKey = secondInspectorEngine.setupInspectorPublicKey(2048,
                    CryptoUriUtil.getIdemixMechanism(),
                    InspectionTest.SECOND_INSPECTOR_URI);
        inspectorKeyManager.storeInspectorPublicKey(InspectionTest.SECOND_INSPECTOR_URI, secondInspectorPubKey);
        userKeyManager.storeInspectorPublicKey(InspectionTest.SECOND_INSPECTOR_URI, secondInspectorPubKey);
        hotelKeyManager.storeInspectorPublicKey(InspectionTest.SECOND_INSPECTOR_URI, secondInspectorPubKey);    
            

        return injectors;
    }

       private void runTestsSingle(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, IssuanceHelper issuanceHelper, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, credSpec);


           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationToken(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy);

           // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
           System.out.println(">> Inspect.");
           this.inspectSingle(pt, inspectorInjector);
       }
       
       private void runTestsRevocation(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, Injector revocationInjector, IssuanceHelper issuanceHelper, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, credSpec);


           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationTokenWithRevocation(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy, REVOCATION_PARAMETERS_UID);

           // Step 3. Inspect the presentationtoken to reveal the revocation handle.
           System.out.println(">> Inspect.");
           Attribute revocationHandleAttribute = this.inspectRevocation(pt, inspectorInjector);
           
           // Step 4. Revoke the credential.
           this.revokeCredential(revocationInjector,
                   issuanceHelper, REVOCATION_PARAMETERS_UID, revocationHandleAttribute);

           RevocationAbcEngine revocationEngine = revocationInjector.getInstance(RevocationAbcEngine.class);
           
           RevocationInformation revocationInformation = revocationEngine
                   .updateRevocationInformation(REVOCATION_PARAMETERS_UID);

           // Step 5. Verify revoked credential is revoked.
           this.revokedCredentialsShouldNotBeAllowed(
                   userInjector,
                   hotelInjector, issuanceHelper, revocationInformation, 0);
           
           
           
       }

       private void runTestsMulti(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, IssuanceHelper issuanceHelper, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, credSpec);

           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationToken(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy);

           // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
           System.out.println(">> Inspect.");
           this.inspectFirstAndLastName(pt, inspectorInjector);
       }
       
       private void runTestsMultiSame(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, Injector inspectorInjector2, IssuanceHelper issuanceHelper, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, credSpec);

           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationToken(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy);

           // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
           System.out.println(">> Inspect.");
           this.inspectLastNameTwice(pt, inspectorInjector, inspectorInjector2);
       }
       
       
       private void runTestsDifferentInspectors(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, Injector secondInspectorInjector, IssuanceHelper issuanceHelper, String presentationPolicy, String credSpec) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, credSpec);

           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationToken(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy);

           // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
           System.out.println(">> Inspect.");
           this.inspectDifferentInspectors(pt, inspectorInjector, secondInspectorInjector);
       }

       private void runTestsTwoCredSameAttribute(Injector governmentInjector, Injector userInjector,
               Injector hotelInjector, Injector inspectorInjector, IssuanceHelper issuanceHelper, String presentationPolicy) throws Exception{

           // Step 1. Get an idcard.
           System.out.println(">> Get idcard.");
           this.issueAndStoreIdcard(governmentInjector, userInjector,
                   issuanceHelper, CREDENTIAL_SPECIFICATION_ID_CARD);

           System.out.println(">> Get studentcard.");
           this.issueAndStoreStudentCard(governmentInjector, userInjector,
                   issuanceHelper, CREDENTIAL_SPECIFICATION_STUDENT_CARD);

           // Step 2. Use the idcard to create (and verify) a presentationtoken.
           System.out.println(">> Verify.");
        
           PresentationToken pt = this.createPresentationTokenWithRevocation(
                   issuanceHelper, hotelInjector, userInjector, 0, presentationPolicy, REVOCATION_PARAMETERS_UID2);

           // Step 3. Inspect the presentationtoken to reveal the name on the idcard.
           System.out.println(">> Inspect.");
           this.inspectIdenticalAttributeInDifferentCredentials(pt, inspectorInjector);
       }
       
       private void issueAndStoreIdcard(Injector governmentInjector,
               Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                       throws Exception {
           Map<String, Object> passportAtts = this.populateIdcardAttributes();
           issuanceHelper.issueCredential(governmentInjector, userInjector,
                   credSpec, ISSUANCE_POLICY_ID_CARD,
                   passportAtts);
       }

       private void issueAndStoreStudentCard(Injector governmentInjector,
               Injector userInjector, IssuanceHelper issuanceHelper, String credSpec)
                       throws Exception {
           Map<String, Object> studentAtts = this.populateStudentcardAttributes();
           issuanceHelper.issueCredential(governmentInjector, userInjector,
                   credSpec, ISSUANCE_POLICY_STUDENT_CARD,
                   studentAtts);
       }
       
       private Map<String, Object> populateIdcardAttributes() {
           Map<String, Object> att = new HashMap<String, Object>();
           att.put("FirstName", NAME);
           att.put("LastName", LASTNAME);
           att.put("Birthday", "1990-02-06Z");
           return att;
       }

       private Map<String, Object> populateStudentcardAttributes() {
           Map<String, Object> att = new HashMap<String, Object>();
           att.put("Name", NAME);
           att.put("LastName", LASTNAME);
           att.put("StudentNumber", 1000);
           att.put("Issued", "2012-11-11Z");
           att.put("Expires", "2015-11-11Z");
           att.put("IssuedBy", "University of X");
           return att;
       }
       
       private PresentationToken createPresentationTokenWithRevocation(IssuanceHelper issuanceHelper,
               Injector hotelInjector, Injector userInjector,
               int pseudonymChoice, String presentationPolicy, URI revParamsUid)
                       throws Exception {
    	   int presentationTokenChoice = 0;
           List<URI> chosenInspectors = new LinkedList<URI>();
           
           InputStream resourceAsStream = this.getClass().getResourceAsStream(
                   presentationPolicy);
           PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
           
           for(PresentationPolicy pp: presentationPolicyAlternatives.getPresentationPolicy()){
        	   for(CredentialInPolicy cip: pp.getCredential()){
        		   for(AttributeInPolicy aip: cip.getDisclosedAttribute()){
        			   chosenInspectors.add(aip.getInspectorAlternatives().getInspectorPublicKeyUID().get(0));  
        		   }        	   
        	   }
        	   
           }
           

/*           chosenInspectors.add(URI
                   .create("http://thebestbank.com/inspector/pub_key_v1"));
           chosenInspectors.add(URI
                   .create("http://thebestbank.com/inspector/pub_key_v1")); */
           
           VerifierAbcEngine verifierEngine = hotelInjector
                   .getInstance(VerifierAbcEngine.class);
           RevocationInformation revocationInformation = verifierEngine
                   .getLatestRevocationInformation(revParamsUid);
           PolicySelector polSelect = new PolicySelector(presentationTokenChoice, chosenInspectors,pseudonymChoice);
           
           
           Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                   .createPresentationToken(userInjector, userInjector,
                		   presentationPolicy, revocationInformation, polSelect);

           // Store all required cred specs in the verifier key manager.
           KeyManager hotelKeyManager = hotelInjector.getInstance(KeyManager.class);
           KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

           PresentationToken pt = p.first();
           assertNotNull(pt);
           for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
               hotelKeyManager.storeCredentialSpecification(
                       cit.getCredentialSpecUID(),
                       userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
           }

           return issuanceHelper.verify(hotelInjector, p.second(), p.first());
       }
       
       
       private PresentationToken createPresentationToken(IssuanceHelper issuanceHelper,
               Injector hotelInjector, Injector userInjector,
               int pseudonymChoice, String presentationPolicy)
                       throws Exception {
    	   int presentationTokenChoice = 0;
           List<URI> chosenInspectors = new LinkedList<URI>();
           
           
           InputStream resourceAsStream = this.getClass().getResourceAsStream(
                   presentationPolicy);
           PresentationPolicyAlternatives presentationPolicyAlternatives = (PresentationPolicyAlternatives) XmlUtils
                       .getObjectFromXML(
                               resourceAsStream, true);
           
           for(AttributeInPolicy aip: presentationPolicyAlternatives.getPresentationPolicy().get(0).getCredential().get(0).getDisclosedAttribute()){
        	  chosenInspectors.add(aip.getInspectorAlternatives().getInspectorPublicKeyUID().get(0)); 
           }

           /*
           chosenInspectors.add(URI
                   .create("http://thebestbank.com/inspector/pub_key_v1"));
           chosenInspectors.add(URI
                   .create("http://thebestbank.com/inspector/pub_key_v1"));
           chosenInspectors = null;*/
           PolicySelector polSelect = new PolicySelector(presentationTokenChoice, chosenInspectors,pseudonymChoice);
           
           
           Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                   .createPresentationToken(userInjector, userInjector,
                		   presentationPolicy, polSelect);

           // Store all required cred specs in the verifier key manager.
           KeyManager hotelKeyManager = hotelInjector.getInstance(KeyManager.class);
           KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);

           PresentationToken pt = p.first();
           assertNotNull(pt);
           for (CredentialInToken cit: pt.getPresentationTokenDescription().getCredential()){
               hotelKeyManager.storeCredentialSpecification(
                       cit.getCredentialSpecUID(),
                       userKeyManager.getCredentialSpecification(cit.getCredentialSpecUID()));
           }

           return issuanceHelper.verify(hotelInjector, p.second(), p.first());
       }
       
       private void inspectFirstAndLastName(PresentationToken pt, Injector inspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           try {
        	 //  ObjectFactory of = new ObjectFactory();
        	 //  System.out.println("inspecting token:\n"+XmlUtils.toXml(of.createPresentationToken(pt)));
			List<Attribute> inspectedAttributes = engine.inspect(pt);

			assertEquals(inspectedAttributes.size(), 2);

			Attribute inspectedFirstName = inspectedAttributes.get(1);
			System.out.println("setting Firstname: "+inspectedFirstName.getAttributeDescription().getType());
			Attribute inspectedLastName = inspectedAttributes.get(0);
			MyAttributeValue firstname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:utf-8"), NAME, null);
			MyAttributeValue lastname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
			
			System.out.println("inspected 1: "+inspectedFirstName.getAttributeValue()+"/"+firstname.getIntegerValueOrNull());
			
			System.out.println("inspected 2: "+inspectedLastName.getAttributeValue()+"/"+lastname.getIntegerValueOrNull());
			assertEquals(inspectedFirstName.getAttributeValue(), firstname.getIntegerValueOrNull());
			assertEquals(inspectedLastName.getAttributeValue(), lastname.getIntegerValueOrNull());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Test should not fail here!", e);
		}
           // Serialize PresentationToken - and re-inspect!
           try {
             System.out.println("Serialize Jaxb to String and Back");
             String ptXml = XmlUtils.toXml(new ObjectFactory().createPresentationToken(pt));
             PresentationToken ptJaxb = (PresentationToken) XmlUtils.getObjectFromXML(new ByteArrayInputStream(ptXml.getBytes()), true);
            List<Attribute> inspectedAttributes = engine.inspect(ptJaxb);

            assertNotNull("inspectedAttributes must not be null!", inspectedAttributes);
            assertEquals(inspectedAttributes.size(), 2);
            
            Attribute inspectedAttr = inspectedAttributes.get(0);
            MyAttributeValue val = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
            assertEquals(inspectedAttr.getAttributeValue(), val.getIntegerValueOrNull());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Test should not fail here!", e);
        }
       }
       
       private void inspectSingle(PresentationToken pt, Injector inspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           try {
        	   List<Attribute> inspectedAttributes = engine.inspect(pt);
        	   assertEquals(inspectedAttributes.size(), 1);

        	   Attribute inspectedAttr = inspectedAttributes.get(0);
        	   MyAttributeValue originalValue = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
        	   
        	   assertEquals(inspectedAttr.getAttributeValue(), originalValue.getIntegerValueOrNull());
           } catch (Exception e) {
        	   e.printStackTrace();
           }
       }
       
       private void inspectIdenticalAttributeInDifferentCredentials(PresentationToken pt, Injector inspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           try {
        	   List<Attribute> inspectedAttributes = engine.inspect(pt);
        	   assertEquals(inspectedAttributes.size(), 2);

        	   Attribute inspectedAttr = inspectedAttributes.get(0);
        	   Attribute inspectedAttr2 = inspectedAttributes.get(1);
        	   MyAttributeValue originalValue = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
        	   
        	   assertEquals(inspectedAttr.getAttributeValue(), originalValue.getIntegerValueOrNull());
        	   assertEquals(inspectedAttr2.getAttributeValue(), originalValue.getIntegerValueOrNull());
           } catch (Exception e) {
        	   e.printStackTrace();
           }
       }

       private Attribute inspectRevocation(PresentationToken pt, Injector inspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           Attribute revokedHandle = null;
           try {
        	   List<Attribute> inspectedAttributes = engine.inspect(pt);
        	   assertEquals(inspectedAttributes.size(), 2);

        	   Attribute inspectedAttr1 = inspectedAttributes.get(0);
        	   MyAttributeValue lastnameValue = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);
        	   
        	   revokedHandle = inspectedAttributes.get(1);
        	   
        	   assertEquals(inspectedAttr1.getAttributeValue(), lastnameValue.getIntegerValueOrNull());
        	   System.out.println("returning revocationhandle: "+(BigInteger)MyAttributeEncodingFactory.recoverValueFromBigInteger(revokedHandle.getAttributeDescription().getEncoding(), (BigInteger)revokedHandle.getAttributeValue(), null).getValueAsObject());
           } catch (Exception e) {
        	   e.printStackTrace();
           }
           assertNotNull("Failed to inspect revocation handle!", revokedHandle);
           return revokedHandle;
       }

       private void inspectDifferentInspectors(PresentationToken pt, Injector inspectorInjector, Injector secondInspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           CryptoEngineInspector secondInspector = secondInspectorInjector.getInstance(CryptoEngineInspector.class);
           try{
        	   MyAttributeValue firstname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:utf-8"), NAME, null);
   				MyAttributeValue lastname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);

        	 //  ObjectFactory of = new ObjectFactory();
        	 //  System.out.println("inspecting token:\n"+XmlUtils.toXml(of.createPresentationToken(pt)));
   				List<Attribute> inspectedAttributes = engine.inspect(pt);

   				assertEquals(inspectedAttributes.size(),1 );

   				Attribute inspectedFirstName = inspectedAttributes.get(0);
   				System.out.println("inspected 1: "+inspectedFirstName.getAttributeValue()+"/"+firstname.getIntegerValueOrNull());
   				assertEquals(inspectedFirstName.getAttributeValue(), firstname.getIntegerValueOrNull());
   				
   				inspectedAttributes = secondInspector.inspect(pt);

   				assertEquals(inspectedAttributes.size(),1 );

   				Attribute inspectedLastName = inspectedAttributes.get(0);
			
   				System.out.println("inspected 2: "+inspectedLastName.getAttributeValue()+"/"+lastname.getIntegerValueOrNull());
   				
   				assertEquals(inspectedLastName.getAttributeValue(), lastname.getIntegerValueOrNull());
           }catch(Exception e){
        	   throw new RuntimeException(e);
           }
       }
       
       private void inspectLastNameTwice(PresentationToken pt, Injector inspectorInjector, Injector secondInspectorInjector){
           CryptoEngineInspector engine = inspectorInjector.getInstance(CryptoEngineInspector.class);
           CryptoEngineInspector secondInspector = secondInspectorInjector.getInstance(CryptoEngineInspector.class);
           try{
   				MyAttributeValue lastname = MyAttributeEncodingFactory.parseValueFromEncoding(URI.create("urn:abc4trust:1.0:encoding:string:sha-256"), LASTNAME, null);

        	 //  ObjectFactory of = new ObjectFactory();
        	 //  System.out.println("inspecting token:\n"+XmlUtils.toXml(of.createPresentationToken(pt)));
   				List<Attribute> inspectedAttributes = engine.inspect(pt);

   				assertEquals(inspectedAttributes.size(),1 );

   				Attribute inspectedLastName = inspectedAttributes.get(0);
   				assertEquals(inspectedLastName.getAttributeValue(), lastname.getIntegerValueOrNull());
   				
   				inspectedAttributes = secondInspector.inspect(pt);

   				assertEquals(inspectedAttributes.size(),1 );

   				inspectedLastName = inspectedAttributes.get(0);
			
//   				System.out.println("inspected 2: "+inspectedLastName.getAttributeValue()+"/"+lastname.getIntegerValueOrNull());
   				
   				assertEquals(inspectedLastName.getAttributeValue(), lastname.getIntegerValueOrNull());
           }catch(Exception e){
        	   throw new RuntimeException(e);
           }
       }
       
       
       private void revokeCredential(Injector revocationInjector,
               IssuanceHelper issuanceHelper, URI revParamsUid,
               Attribute revocationHandleAttribute) throws CryptoEngineException {
           issuanceHelper.revokeCredential(revocationInjector, revParamsUid,
                   revocationHandleAttribute);
       }
       
       private void revokedCredentialsShouldNotBeAllowed(Injector userInjector,
               Injector verifierInjector, IssuanceHelper issuanceHelper,
               RevocationInformation revocationInformation,
               int chosenPresentationToken) throws Exception {
           try {
               this.loginToAccount(userInjector, verifierInjector,
                       issuanceHelper, REVOCATION_PARAMETERS_UID,
                       revocationInformation, chosenPresentationToken);
               fail("We should not be allowed to log in with a revoked credential");
           } catch (TokenVerificationException ex) {
               // StringWriter sw = new StringWriter();
               // PrintWriter pw = new PrintWriter(sw);
               // ex.printStackTrace(pw);
               // assertTrue(sw.toString().contains("Incorrect T-value at position"));
               assertTrue(
                       "We expect the verification to fail due to a revoked credential",
                       ex.getMessage()
                       .startsWith(
                               "The crypto evidence in the presentation token is not valid"));
           } catch (RuntimeException ex) {
             assertTrue(
               "We expect presentation token generation to fail",
               ex.getMessage()
               .startsWith(
                       "Cannot generate presentationToken"));
           }
       }
       
       private PresentationToken loginToAccount(Injector userInjector,
               Injector verifierInjector, IssuanceHelper issuanceHelper,
               URI revParamsUid, RevocationInformation revocationInformation,
               int chosenPresentationToken)
                       throws Exception {
           int pseudonymChoice = 0;
           Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                   .createPresentationToken(userInjector, userInjector,
                           PRESENTATION_POLICY_CREDENTIALS, revocationInformation,
                           new PolicySelector(true, chosenPresentationToken, //debug enabled
                                   pseudonymChoice));
           return issuanceHelper.verify(verifierInjector, p.second(), p.first());
       }
       
}
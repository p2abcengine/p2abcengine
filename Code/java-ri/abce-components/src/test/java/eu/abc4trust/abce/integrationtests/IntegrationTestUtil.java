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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.util.XmlUtils;

public class IntegrationTestUtil {
    static public final String NAME = "John";
    static public final String LASTNAME = "Dow";
    static public final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    static public final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    static public final String PRESENTATION_POLICY_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyRevocation.xml";
    static public final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");
	
    static public Secret getUProveSecret() throws JAXBException,
    UnsupportedEncodingException, SAXException {
        Secret secret = (Secret) XmlUtils.getObjectFromXML(
        		IntegrationTestUtil.class.getResourceAsStream(
                        "/eu/abc4trust/sampleXml/patras/uprove-secret.xml"),
                        true);
        return secret;
    }
    
    static public CredentialDescription issueAndStoreIdCard(
            Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper,
            String birthday) throws Exception {
        Map<String, Object> passportAtts = IntegrationTestUtil.populateIdCard(birthday);
        return issuanceHelper.issueCredential(governmentInjector, userInjector,
        		IntegrationTestUtil.CREDENTIAL_SPECIFICATION_ID_CARD, IntegrationTestUtil.ISSUANCE_POLICY_ID_CARD,
                passportAtts);
    }
    
    static public Map<String, Object> populateIdCard(String birthday) {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put("FirstName", IntegrationTestUtil.NAME);
        atts.put("LastName", IntegrationTestUtil.LASTNAME);
        atts.put("Birthday", birthday);
        return atts;
    }

    static public PresentationToken createAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid) throws Exception {

        VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);
        RevocationInformation revocationInformation = verifierEngine
                .getLatestRevocationInformation(revParamsUid);
        int selectedPolicy = 0;
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS,
                        revocationInformation, new PolicySelector(
                                selectedPolicy, pseudonymChoice));
       
        return issuanceHelper.verify(verifierInjector, p.second(), p.first());
    }

    static public PresentationToken loginToAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid, RevocationInformation revocationInformation,
            URI chosenCredential)
                    throws Exception {
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS, revocationInformation,
                        new PolicySelector(false, chosenCredential, 
                                pseudonymChoice));
        return issuanceHelper.verify(verifierInjector, p.second(), p.first());
    }
    
    static public PresentationToken loginToAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid, RevocationInformation revocationInformation,
            int chosenPresentationToken)
                    throws Exception {
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS, revocationInformation,
                        new PolicySelector(true, chosenPresentationToken, //debug enabled
                                pseudonymChoice));
        return issuanceHelper.verify(verifierInjector, p.second(), p.first());
    }

}

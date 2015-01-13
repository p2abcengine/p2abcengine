//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.abce.integrationtests;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Injector;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationInformation;

public class IntegrationTestUtil {
  private static final String USERNAME = "defaultUser";
    static public final String NAME = "John";
    static public final String LASTNAME = "Dow";
    static public final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocationSimpleIdentitycard.xml";
    static public final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml";
    static public final String PRESENTATION_POLICY_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyRevocation.xml";
    static public final URI REVOCATION_PARAMETERS_UID = URI.create("revocationUID1");
	
    static public CredentialDescription issueAndStoreIdCard(
            Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper,
            String birthday) throws Exception {
        Map<String, Object> passportAtts = IntegrationTestUtil.populateIdCard(birthday);
        return issuanceHelper.issueCredential(USERNAME, governmentInjector, userInjector,
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

    static public PresentationToken loginWithIdCard(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid) throws Exception {

        VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);
        RevocationInformation revocationInformation = verifierEngine
                .getLatestRevocationInformation(revParamsUid);
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(USERNAME, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS,
                        revocationInformation, null);
       
        return issuanceHelper.verify(verifierInjector, p.second, p.first);
    }
    
    static public PresentationToken loginToAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid, RevocationInformation revocationInformation)
                    throws Exception {
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(USERNAME, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS, revocationInformation, null);
        return issuanceHelper.verify(verifierInjector, p.second, p.first);
    }

    static public PresentationToken loginToAccount(Injector userInjector,
            Injector verifierInjector, IssuanceHelper issuanceHelper,
            URI revParamsUid, URI chosenCredential, 
            RevocationInformation revocationInformation)
                    throws Exception {
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createSpecificPresentationToken(USERNAME, userInjector,
                		IntegrationTestUtil.PRESENTATION_POLICY_CREDENTIALS, 
                			chosenCredential, revocationInformation, null);
        return issuanceHelper.verify(verifierInjector, p.second, p.first);
    }
    
    
}

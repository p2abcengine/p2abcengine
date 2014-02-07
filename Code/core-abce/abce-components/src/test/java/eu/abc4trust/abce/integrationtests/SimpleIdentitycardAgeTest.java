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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
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
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

/**
 * This test checks that a user can get issued a simple identity card with
 * firstname, lastname, and birthday.
 */
public class SimpleIdentitycardAgeTest {
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentityCard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentityCard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";
    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String CREDENTIAL_ID_CARD = "/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCard.xml";

    /**
     * Validates that the user is 18 years or older.
     * 
     * @throws Exception
     */
    @Test()
    @Ignore
    public void SimpleIdentitycardAgeOver18Test() throws Exception {
        Injector verifierInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1985),
                        CryptoEngine.MOCK, UProveUtils.UPROVE_COMMON_PORT));
        VerifierAbcEngine verifierEngine = verifierInjector
                .getInstance(VerifierAbcEngine.class);

        Pair<PresentationToken, PresentationPolicyAlternatives> pair = this
                .runTestCase("1985-05-05Z");
        verifierEngine.verifyTokenAgainstPolicy(pair.second(), pair.first(),
                true);
    }

    /**
     * Tries to validate that the user is 18 years or older, but fails.
     * 
     * @throws Exception
     */
    @Test() @Ignore
    public void SimpleIdentitycardAgeUnder18Test() throws Exception {
        Pair<PresentationToken, PresentationPolicyAlternatives> pair = this
                .runTestCase("1995-05-05Z");
        assertNull(pair.first());
    }

    /**
     * Timings test
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void TimingsTest() throws Exception {
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1985), CryptoEngine.MOCK, UProveUtils.UPROVE_COMMON_PORT));

        CredentialManager cd = userInjector
                .getInstance(CredentialManager.class);

        IssuanceHelper issuanceHelper = new IssuanceHelper();
        Credential c = issuanceHelper.loadCredential(CREDENTIAL_ID_CARD);

        int lowerInspectionPoint = 250; // 250 credentials.
        int baseInspectionPoint = 250; // Total 250 + 250 = 500 credentials.
        int upperInspectionPoint = 250; // Total 250 + 250 + 250 = 750
        // credentials.

        PresentationTokenExecutionTiming
        .loadCredentialStoreWithNumberOfCredentials(issuanceHelper, cd,
                lowerInspectionPoint, c);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        PresentationTokenExecutionTiming.warmup(issuanceHelper, userInjector);
        long x = PresentationTokenExecutionTiming
                .howLongDoesItTakeToCreateAPresentationToken(userInjector,
                        issuanceHelper);

        long y = this.addCredentialsAndRunPresentation(userInjector, cd, issuanceHelper, c,
                baseInspectionPoint);

        long z = this.addCredentialsAndRunPresentation(userInjector, cd, issuanceHelper, c,
                upperInspectionPoint);

        // System.out.println("250: " + x);
        // System.out.println("500: " + y);
        // System.out.println("750: " + z);
        this.failIfExecutionTimeGetsVerse(x, lowerInspectionPoint, y,
                lowerInspectionPoint + baseInspectionPoint);

        this.failIfExecutionTimeImproves(y, lowerInspectionPoint
                + baseInspectionPoint, z,
                lowerInspectionPoint + baseInspectionPoint
                + upperInspectionPoint);
    }

    private long addCredentialsAndRunPresentation(Injector userInjector, CredentialManager cd,
            IssuanceHelper issuanceHelper, Credential c,
            int upperInspectionPoint) throws Exception {
        PresentationTokenExecutionTiming
        .loadCredentialStoreWithNumberOfCredentials(issuanceHelper, cd,
                upperInspectionPoint, c);
        long z = PresentationTokenExecutionTiming
                .howLongDoesItTakeToCreateAPresentationToken(
                        userInjector, issuanceHelper);
        return z;
    }

    private void failIfExecutionTimeGetsVerse(long x, int credsX, long y,
            int credsY) throws Exception {
        // k is the constant difference in the y timing compared to Janus's
        // machine.
        double k = y - ((0.0032 * 500 * 500) + (0.209 * 500) + 457.47);
        // e is the expected value without constant contributions on the 250
        // credentials point on Janus's machine.
        double e = (0.0032 * 250 * 250) + (0.209 * 250);
        // m is the measured value without constant contributions.
        double m = x - k;
        boolean v = m < (4 * e);
        // System.out.println("k: " + k);
        // System.out.println("e: " + e);
        // System.out.println("m: " + m);
        assertTrue("Execution time has deteriated. Going from " + credsX
                + " credentials to " + credsY
                + " credentials increased the execution time from: " + x
                + " to " + y + " ms.", v);
    }

    private void failIfExecutionTimeImproves(long y, int credsY, long z,
            int credsZ) throws Exception {
        // k is the constant difference in the y timing compared to Janus's
        // machine.
        double k = y - ((0.0032 * 500 * 500) + (0.209 * 500) + 457.47);
        // e is the expected value without constant contributions on the 750
        // credentials point on Janus's machine.
        double e = (0.0032 * 750 * 750) + (0.209 * 750);
        // m is the measured value without constant contributions.
        double m = z - k;
        boolean v = m > e;
        // System.out.println("k: " + k);
        // System.out.println("e: " + e);
        // System.out.println("m: " + m);
        assertTrue("Execution time has improved. Going from " + credsY
                + " credentials to " + credsZ
                + " credentials increased the execution time from: " + y
                + " to " + z + " ms.", v);
    }

    private Pair<PresentationToken, PresentationPolicyAlternatives> runTestCase(
            String birthday) throws Exception {
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1985), CryptoEngine.MOCK, UProveUtils.UPROVE_COMMON_PORT));
        return this.runTestCase(birthday, userInjector);
    }

    private Pair<PresentationToken, PresentationPolicyAlternatives> runTestCase(
            String birthday, Injector userInjector) throws Exception {
        // Step 1.
        Injector issuerInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.MOCK, UProveUtils.UPROVE_COMMON_PORT));

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Step 2. Get identity card.
        this.issueAndStoreIdCard(issuerInjector, userInjector, issuanceHelper,
                birthday);

        // Step 3. Create presentation token.
        Pair<PresentationToken, PresentationPolicyAlternatives> pt = this
                .createPresentationTokenUsingCreditcard(
                        issuanceHelper, userInjector);

        return pt;
    }

    private Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationTokenUsingCreditcard(
            IssuanceHelper issuanceHelper, Injector userInjector)
                    throws Exception {
        int presentationTokenChoice = 0;
        List<URI> chosenInspectors = new LinkedList<URI>();
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_ID_CARD, new PolicySelector(
                                presentationTokenChoice, chosenInspectors,
                                pseudonymChoice));
        return p;
    }

    private void issueAndStoreIdCard(Injector issuerInjector,
            Injector userInjector, IssuanceHelper issuanceHelper,
            String birthday) throws Exception {
        Map<String, Object> passportAtts = this
                .populateIssuerAttributes(birthday);
        issuanceHelper.issueCredential(issuerInjector, userInjector,
                CREDENTIAL_SPECIFICATION_ID_CARD, ISSUANCE_POLICY_ID_CARD,
                passportAtts);
    }

    private Map<String, Object> populateIssuerAttributes(String birthday) {
        Map<String, Object> att = new HashMap<String, Object>();
        att.put("Firstname", NAME);
        att.put("Lastname", LASTNAME);
        att.put("Birthday", birthday);
        return att;
    }
}
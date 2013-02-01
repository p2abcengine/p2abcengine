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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test checks that a user can get issued a simple identity card with
 * firstname, lastname, and birthday.
 */
public class SimpleAccessCaseTest {

    private static final String NAME = "John";
    private static final String LASTNAME = "Dow";
    private static final String CREDENTIAL_SPECIFICATION_ID_CARD = "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentityCard.xml";
    private static final String ISSUANCE_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentityCard.xml";
    private static final String PRESENTATION_POLICY_PSEUDONYM_OR_CREDENTIALS = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyPseudonymOrCredentials.xml";
    private static final String CREDENTIAL_CREDITCARD = "/eu/abc4trust/sampleXml/credentials/credentialCreditcard.xml";
    private static final String CREDENTIAL_CREDITCARD_AMEX = "/eu/abc4trust/sampleXml/credentials/credentialCreditcardRevocableAmex.xml";


    /**
     * Validates that the user is 18 years or older.
     * 
     * @throws Exception
     */
    @Test  @Ignore
    public void loginForYoungstersTest() throws Exception {
        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.MOCK));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), CryptoEngine.MOCK));
        Injector serviceInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1988),
                        CryptoEngine.MOCK));

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Step 1. Get identity card.
        this.issueAndStoreIdCard(governmentInjector, userInjector,
                issuanceHelper, "1995-05-05Z");

        // Step 2. Get an account (pseudonym) using the identity card.
        this.createAccount(userInjector, serviceInjector, issuanceHelper, 0);

        // Step 3. Login using pseudonym.
        this.loginToAccount(userInjector, serviceInjector, issuanceHelper);
    }

    private void issueAndStoreIdCard(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper,
            String birthday) throws Exception {
        Map<String, Object> passportAtts = this.populateIdCard(birthday);
        issuanceHelper.issueCredential(governmentInjector, userInjector,
                CREDENTIAL_SPECIFICATION_ID_CARD, ISSUANCE_POLICY_ID_CARD,
                passportAtts);
    }

    private Map<String, Object> populateIdCard(String birthday) {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put("Firstname", NAME);
        atts.put("Lastname", LASTNAME);
        atts.put("Birthday", birthday);
        return atts;
    }

    private void createAccount(Injector userInjector, Injector serviceInjector,
            IssuanceHelper issuanceHelper, int selectedPolicy) throws Exception {
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_PSEUDONYM_OR_CREDENTIALS,
                        new PolicySelector(selectedPolicy, pseudonymChoice));
        issuanceHelper.verify(serviceInjector, p.second(), p.first());
    }

    private void loginToAccount(Injector userInjector,
            Injector serviceInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        int selectedPolicyNumber = 0;
        int pseudonymChoice = 0;
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_PSEUDONYM_OR_CREDENTIALS,
                        new PolicySelector(selectedPolicyNumber,
                                pseudonymChoice));
        issuanceHelper.verify(serviceInjector, p.second(), p.first());
    }

    /**
     * Tries to validate that the user is 18 years or older, but fails.
     * 
     * @throws Exception
     */
    @Test() @Ignore
    public void loginForAdultsTest() throws Exception {
        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.MOCK));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), CryptoEngine.MOCK));
        Injector serviceInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1988),
                        CryptoEngine.MOCK));

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Step 1. Get identity card.
        this.issueAndStoreIdCard(governmentInjector, userInjector,
                issuanceHelper, "1985-05-05Z");

        // Step 2. Get credit card.
        this.loadAndStoreCreditCard(governmentInjector, userInjector,
                issuanceHelper, CREDENTIAL_CREDITCARD);

        // Step 3. Get an account (pseudonym) using the identity card and credit
        // card.
        this.createAccount(userInjector, serviceInjector, issuanceHelper, 0);

        // Step 4. Login using pseudonym.
        this.loginToAccount(userInjector, serviceInjector, issuanceHelper);
    }

    /**
     * Login witd idcard + two creditcards
     * 
     * @throws Exception
     */
    @Test() @Ignore
    public void loginForAdultsTestTwoCreditcards() throws Exception {
        Injector governmentInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.MOCK));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1987), CryptoEngine.MOCK));
        Injector serviceInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1988),
                        CryptoEngine.MOCK));

        IssuanceHelper issuanceHelper = new IssuanceHelper();

        // Step 1. Get identity card.
        this.issueAndStoreIdCard(governmentInjector, userInjector,
                issuanceHelper, "1985-05-05Z");

        // Step 2a. Get credit card - Visa.
        this.loadAndStoreCreditCard(governmentInjector, userInjector,
                issuanceHelper, CREDENTIAL_CREDITCARD);

        // Step 2b. Get credit card - Amex.
        this.loadAndStoreCreditCard(governmentInjector, userInjector,
                issuanceHelper, CREDENTIAL_CREDITCARD_AMEX);

        // Step 3. Get an account (pseudonym) using the identity card and credit
        // card.
        this.createAccount(userInjector, serviceInjector, issuanceHelper, 0);

        // Step 4. Login using pseudonym.
        this.loginToAccount(userInjector, serviceInjector, issuanceHelper);
    }

    private void loadAndStoreCreditCard(Injector governmentInjector,
            Injector userInjector, IssuanceHelper issuanceHelper, String credentialResource)
                    throws Exception {
        Credential credentialCreditcard = (Credential) XmlUtils
                .getObjectFromXML(
                        this.getClass().getResourceAsStream(
                                credentialResource), true);
        CredentialManager credentialManager = userInjector
                .getInstance(CredentialManager.class);
        credentialManager.storeCredential(credentialCreditcard);
    }
}
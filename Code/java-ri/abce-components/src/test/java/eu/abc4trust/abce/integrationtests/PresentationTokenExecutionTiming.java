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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.rice.cs.plt.tuple.Pair;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.abce.testharness.PolicySelector;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;

public class PresentationTokenExecutionTiming {
    private static final String CREDENTIAL_ID_CARD = "/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCard.xml";
    private static final String PRESENTATION_POLICY_ID_CARD = "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1985), CryptoEngine.MOCK, UProveUtils.UPROVE_COMMON_PORT));

        CredentialManager cd = userInjector
                .getInstance(CredentialManager.class);

        IssuanceHelper issuanceHelper = new IssuanceHelper();
        Credential c = issuanceHelper.loadCredential(CREDENTIAL_ID_CARD);

        FileOutputStream fos = new FileOutputStream(
                "/Users/fagidiot/temp/test.txt");
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        out.flush();
        long deltaTime = 0;
        int upperBoundPresentationTokenExecutionTime = 1000;
        int upperBound = 1000;
        cd.storeCredential(c);
        warmup(issuanceHelper, userInjector);
        int inx = 2;
        while ((deltaTime < upperBoundPresentationTokenExecutionTime)
                && (inx < upperBound)) {
            cd.storeCredential(c);
            deltaTime = howLongDoesItTakeToCreateAPresentationToken(
                    userInjector, issuanceHelper);
            out.write("Time: " + deltaTime + ", credentials: " + inx + "\n");
            if ((inx % 50) == 0) {
                out.flush();
            }
            inx++;
        }
        out.close();
    }

    public static long howLongDoesItTakeToCreateAPresentationToken(
            Injector userInjector, IssuanceHelper issuanceHelper)
                    throws Exception {
        long deltaTime;
        long start = System.currentTimeMillis();
        createPresentationTokenUsingCreditcard(issuanceHelper, userInjector);
        long now = System.currentTimeMillis();
        deltaTime = now - start;
        return deltaTime;
    }

    public static void warmup(IssuanceHelper issuanceHelper,
            Injector userInjector) throws Exception {
        createPresentationTokenUsingCreditcard(issuanceHelper, userInjector);
    }

    public static Pair<PresentationToken, PresentationPolicyAlternatives> createPresentationTokenUsingCreditcard(
            IssuanceHelper issuanceHelper, Injector userInjector)
                    throws Exception {
        int presentationTokenChoice = 0;
        int pseudonymChoice = 0;
        List<URI> chosenInspectors = new LinkedList<URI>();
        Pair<PresentationToken, PresentationPolicyAlternatives> p = issuanceHelper
                .createPresentationToken(userInjector, userInjector,
                        PRESENTATION_POLICY_ID_CARD, new PolicySelector(
                                presentationTokenChoice, chosenInspectors,
                                pseudonymChoice));
        return p;
    }

    public static int loadCredentialStoreWithNumberOfCredentials(
            IssuanceHelper issuanceHelper, CredentialManager cd,
            int lowerBound, Credential c) throws Exception {
        int bound = 1;
        for (; bound < lowerBound; bound++) {
            cd.storeCredential(c);
        }
        return bound;
    }
}

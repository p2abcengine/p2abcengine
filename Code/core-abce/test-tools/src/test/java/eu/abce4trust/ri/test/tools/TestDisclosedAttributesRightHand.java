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

package eu.abce4trust.ri.test.tools;

import junit.framework.TestCase;

import org.junit.Test;

public class TestDisclosedAttributesRightHand extends TestCase {

    @Test
    public void testDisclosedAttributesRightHand() throws Exception {

        String presentationPolicyFile = "src/test/resources/disclosedAttributesRightHand/presentationPolicy.xml";
        String presentationTokenFile = "src/test/resources/disclosedAttributesRightHand/presentationToken.xml";
        String debuggerResourceFolder = "src/test/resources/disclosedAttributesRightHand/";

        boolean result = VerificationDebugger.verifyTokenAgainstPolicy(
                presentationPolicyFile, presentationTokenFile,
                debuggerResourceFolder);

        assertTrue(result);
    }
}

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

package eu.abc4trust.abce.internal.issuer.tokenManagerIssuer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.Test;

import eu.abc4trust.xml.AttributeInLogEntry;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;

public class TokenManagerIssuerTest {

    private static final String EXTENSION = ".file";
    private static final String TOKENS_FILE = "tokens";
    private static final String PSEUDONYMS_FILE = "pseudonyms";
    private static final String LOG_FILE = "log";

    @SuppressWarnings("deprecation")
    @Test
    public void testPersistentStoreAndGetToken() throws Exception {

        // Init the persistent storage and the TokenManager
        File temp1 = File.createTempFile(TOKENS_FILE, EXTENSION);
        temp1.deleteOnExit();

        File temp2 = File.createTempFile(PSEUDONYMS_FILE, EXTENSION);
        temp2.deleteOnExit();

        File temp3 = File.createTempFile(LOG_FILE, EXTENSION);
        temp3.deleteOnExit();

        PersistentFileTokenStorageIssuer persistentStorage = new PersistentFileTokenStorageIssuer(temp1, temp2, temp3);
        TokenManagerIssuer tokenManager = new TokenManagerIssuerImpl(persistentStorage);

        // Create a IssuanceToken for test

        PresentationTokenDescription pTokenDesc = new PresentationTokenDescription();
        final byte[] originalPseudonymValue = new byte[] {4,2,5};
        PseudonymInToken originalPseudonym = new PseudonymInToken();
        originalPseudonym.setPseudonymValue(originalPseudonymValue);
        pTokenDesc.getPseudonym().add(originalPseudonym);
        IssuanceToken iToken = new IssuanceToken();
        IssuanceTokenDescription iDesc = new IssuanceTokenDescription();
        iDesc.setPresentationTokenDescription(pTokenDesc);
        iToken.setIssuanceTokenDescription(iDesc);
        iToken.setVersion("1.0");

        // Create another IssuanceToken for test
        PresentationTokenDescription pTokenDesc2 = new PresentationTokenDescription();
        final byte[] originalPseudonymValue2 = new byte[] {3,2,5};
        PseudonymInToken originalPseudonym2 = new PseudonymInToken();
        originalPseudonym2.setPseudonymValue(originalPseudonymValue2);
        pTokenDesc2.getPseudonym().add(originalPseudonym2);
        IssuanceToken iToken2 = new IssuanceToken();
        IssuanceTokenDescription iDesc2 = new IssuanceTokenDescription();
        iDesc2.setPresentationTokenDescription(pTokenDesc2);
        iToken2.setIssuanceTokenDescription(iDesc2);
        iToken2.setVersion("2.0");

        // Store the first token
        URI uid = tokenManager.storeToken(iToken);

        // Retrieve the saved IssuanceToken
        IssuanceToken savedIssuanceToken = tokenManager.getToken(uid);

        // Get pseudonym from the saved IssuanceToken
        PseudonymInToken savedPseudonym = savedIssuanceToken.getIssuanceTokenDescription().getPresentationTokenDescription().getPseudonym().get(0);

        // Test if the original PseudonymValue bytewise equals the one that has been saved.
        assertArrayEquals(originalPseudonymValue, savedPseudonym.getPseudonymValue());

        // Test version
        assertEquals("1.0", savedIssuanceToken.getVersion());

        // Test isEstablishedPseudonym with the first original pseudonym
        assertTrue(tokenManager.isEstablishedPseudonym(originalPseudonym));

        // Test isEstablishedPseudonym with a false / nonexistant pseudonym
        byte[] falsePseudonymValue = new byte[] {41,23,51};
        PseudonymInToken falsePseudonym = new PseudonymInToken();
        falsePseudonym.setPseudonymValue(falsePseudonymValue);
        assertFalse(tokenManager.isEstablishedPseudonym(falsePseudonym));

        // Store the second token
        URI uid2 = tokenManager.storeToken(iToken2);

        // Retrieve the saved IssuanceToken
        IssuanceToken savedIssuanceToken2 = tokenManager.getToken(uid2);

        // Get pseudonym from the second saved PresentationToken
        PseudonymInToken savedPseudonym2 = savedIssuanceToken2.getIssuanceTokenDescription().getPresentationTokenDescription().getPseudonym().get(0);

        // Test if the original second PseudonymValue bytewise equals the one that has been saved.
        assertArrayEquals(originalPseudonymValue2, savedPseudonym2.getPseudonymValue());

        // Test version
        assertEquals("2.0", savedIssuanceToken2.getVersion());

        // Test isEstablishedPseudonym with second pseudonym
        assertTrue(tokenManager.isEstablishedPseudonym(originalPseudonym2));

        // Test isEstablishedPseudonym with a false / nonexistant pseudonym
        byte[] falsePseudonymValue2 = new byte[] {52,37,55};
        PseudonymInToken falsePseudonym2 = new PseudonymInToken();
        falsePseudonym2.setPseudonymValue(falsePseudonymValue2);
        assertFalse(tokenManager.isEstablishedPseudonym(falsePseudonym2));

        // Retrieve the first saved IssuanceToken again to make sure it has not been overwritten
        savedIssuanceToken = tokenManager.getToken(uid);

        // Get pseudonym from the saved IssuanceToken again to make sure it has not been overwritten
        savedPseudonym = savedIssuanceToken.getIssuanceTokenDescription().getPresentationTokenDescription().getPseudonym().get(0);

        // Test if the originalPseudonymValue bytewise equals the one that has been saved first again to make sure it has not been overwritten.
        assertArrayEquals(originalPseudonymValue, savedPseudonym.getPseudonymValue());

        // Test version again to make sure it has not been overwritten
        assertEquals("1.0", savedIssuanceToken.getVersion());

        // Test isEstablishedPseudonym with the first original pseudonym again to make sure it has not been overwritten
        assertTrue(tokenManager.isEstablishedPseudonym(originalPseudonym));

        // Test delete token
        assertTrue(tokenManager.deleteToken(uid));

        // Test isEstablishedPseudonym with the first original pseudonym to make sure it has been removed
        assertFalse(tokenManager.isEstablishedPseudonym(originalPseudonym));

        // Retrieve the first saved IssuanceToken again to make sure it has been removed
        savedIssuanceToken = tokenManager.getToken(uid);
        assertEquals(null, savedIssuanceToken);

        IssuanceLogEntry logEntry = new IssuanceLogEntry();
        logEntry.setIssuanceLogEntryUID(URI.create("logEntryUID"));
        logEntry.setIssuerParametersUID(URI.create("issuerParamsUID"));
        logEntry.setIssuanceToken(iToken);
        AttributeInLogEntry attributeInLogEntry = new AttributeInLogEntry();
        attributeInLogEntry.setAttributeType(URI.create("nullAttribute"));
        attributeInLogEntry.setAttributeValue(null);
        logEntry.getIssuerAttributes().add(attributeInLogEntry);

        URI storedUID = tokenManager.storeIssuanceLogEntry(logEntry);
        IssuanceLogEntry storedEntry = tokenManager.getIssuanceLogEntry(storedUID);
        assertEquals(logEntry.getIssuanceLogEntryUID(), storedEntry.getIssuanceLogEntryUID());
        assertEquals(logEntry.getIssuerParametersUID(), storedEntry.getIssuerParametersUID());
        assertEquals(logEntry.getIssuerAttributes().get(0).getAttributeType(),
                storedEntry.getIssuerAttributes().get(0).getAttributeType());
        assertEquals(logEntry.getIssuerAttributes().get(0).getAttributeValue(),
                storedEntry.getIssuerAttributes().get(0).getAttributeValue());
        assertEquals(logEntry.getIssuanceToken().getIssuanceTokenDescription().getPresentationTokenDescription().getPolicyUID(), storedEntry.getIssuanceToken().getIssuanceTokenDescription().getPresentationTokenDescription().getPolicyUID());



    }
}

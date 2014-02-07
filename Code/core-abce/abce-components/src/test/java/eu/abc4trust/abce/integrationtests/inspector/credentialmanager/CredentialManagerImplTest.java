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

package eu.abc4trust.abce.integrationtests.inspector.credentialmanager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerImpl;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialStorage;
import eu.abc4trust.abce.internal.inspector.credentialManager.PersistentCredentialStorage;
import eu.abc4trust.util.TemporaryFileFactory;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.SecretKey;

public class CredentialManagerImplTest {
    private static final URI EXPECTED_UUID = URI.create(UUID.randomUUID().toString());
    private static final CredentialStorage credStore =
            new PersistentCredentialStorage(TemporaryFileFactory.createTemporaryFile());
    private static final CredentialManagerImpl credMng = new CredentialManagerImpl(credStore);

    @Test
    public void storeKey() throws Exception {
        SecretKey inspectorSecretKey = new SecretKey();
        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().add("TestString1");
        inspectorSecretKey.setCryptoParams(cryptoParams);
        credMng.storeInspectorSecretKey(EXPECTED_UUID, inspectorSecretKey);

        SecretKey storedInspectorSecretKey = credMng
                .getInspectorSecretKey(EXPECTED_UUID);
        assertEquals(inspectorSecretKey.getCryptoParams().getAny().get(0),
                storedInspectorSecretKey.getCryptoParams().getAny().get(0));
    }

    @Test
    public void updateKey() throws Exception {
        SecretKey inspectorSecretKey = new SecretKey();
        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().add("TestString1");
        inspectorSecretKey.setCryptoParams(cryptoParams);
        credMng.storeInspectorSecretKey(EXPECTED_UUID, inspectorSecretKey);

        SecretKey storedInspectorSecretKey = credMng
                .getInspectorSecretKey(EXPECTED_UUID);
        assertEquals(inspectorSecretKey.getCryptoParams().getAny().get(0),
                storedInspectorSecretKey.getCryptoParams().getAny().get(0));

        SecretKey inspectorSecretKey_new = new SecretKey();
        cryptoParams = new CryptoParams();
        cryptoParams.getAny().add("TestString1");
        inspectorSecretKey_new.setCryptoParams(cryptoParams);
        credMng.storeInspectorSecretKey(EXPECTED_UUID, inspectorSecretKey_new);
        SecretKey storedInspectorSecretKey_new = credMng
                .getInspectorSecretKey(EXPECTED_UUID);
        assertEquals(inspectorSecretKey_new.getCryptoParams().getAny().get(0),
                storedInspectorSecretKey_new.getCryptoParams().getAny().get(0));
    }

    @Test (expected=CredentialManagerException.class)
    public void getUnknownKey() throws CredentialManagerException {
        @SuppressWarnings("unused")
        SecretKey storedInspectorSecretKey = credMng.getInspectorSecretKey(URI
                .create(UUID.randomUUID().toString()));
        // We should not get here.
        assertFalse(true);
    }

    @Test
    public void getListOfKeys() throws Exception {
        // We need a fresh instance of the store for this test.
        CredentialStorage credStore1 =
                new PersistentCredentialStorage(TemporaryFileFactory.createTemporaryFile());
        CredentialManagerImpl credMng1 = new CredentialManagerImpl(credStore1);
        Map<URI, SecretKey> keys = new HashMap<URI, SecretKey>();
        for (int i = 0 ; i != 10 ; ++i) {
            SecretKey inspectorSecretKey = new SecretKey();
            CryptoParams cryptoParams = new CryptoParams();
            cryptoParams.getAny().add("TestString1");
            inspectorSecretKey.setCryptoParams(cryptoParams);
            keys.put(URI.create(UUID.randomUUID().toString()), inspectorSecretKey);
        }

        for (Map.Entry<URI, SecretKey> k : keys.entrySet()) {
            credMng1.storeInspectorSecretKey(k.getKey(), k.getValue());
        }

        Object[] keysInStore = credMng1.listInspectorSecretKeys().toArray();
        Object[] keysCreated = keys.keySet().toArray();
        // We need to ensure same order.
        Arrays.sort(keysInStore);
        Arrays.sort(keysCreated);
        assertArrayEquals(keysInStore, keysCreated);
    }
}

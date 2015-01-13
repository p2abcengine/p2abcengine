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

package eu.abc4trust.abce.integrationtests.issuer.credentialmanager;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManagerImpl;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialStorage;
import eu.abc4trust.abce.internal.issuer.credentialManager.PersistentCredentialStorage;
import eu.abc4trust.util.TemporaryFileFactory;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.SecretKey;


public class CredentialManagerImplTest {

    private static final URI EXPECTED_UUID = URI.create("ba419d35-0dfe-4af7-aee7-bbe10c45c028");

    @Test
    public void getIssuerKey() throws Exception {
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile());

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore);

        SecretKey issuerSecretKey = new SecretKey();
        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getContent().add("TestString1");
        issuerSecretKey.setCryptoParams(cryptoParams);
        credMng.storeIssuerSecretKey(EXPECTED_UUID,
                issuerSecretKey);

        SecretKey storedIssuerSecretKey = credMng
                .getIssuerSecretKey(EXPECTED_UUID);
        assertEquals(issuerSecretKey.getCryptoParams().getContent()
                .get(0),
                storedIssuerSecretKey.getCryptoParams().getContent()
                .get(0));
    }

    @Test
    public void listCredentials() throws Exception {
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile());

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore);

        SecretKey[] issuerSecretKeys = new SecretKey[3];
        issuerSecretKeys[0] = new SecretKey();
        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getContent().add("TestString1");
        issuerSecretKeys[0].setCryptoParams(cryptoParams);

        issuerSecretKeys[1] = new SecretKey();
        cryptoParams = new CryptoParams();
        cryptoParams.getContent().add("TestString2");
        issuerSecretKeys[1].setCryptoParams(cryptoParams);

        issuerSecretKeys[2] = new SecretKey();
        cryptoParams = new CryptoParams();
        cryptoParams.getContent().add("TestString2");
        issuerSecretKeys[2].setCryptoParams(cryptoParams);

        credMng.storeIssuerSecretKey(URI.create("1"), issuerSecretKeys[0]);
        credMng.storeIssuerSecretKey(URI.create("2"), issuerSecretKeys[1]);
        credMng.storeIssuerSecretKey(URI.create("3"), issuerSecretKeys[2]);

        List<URI> storedURIs = credMng.listIssuerSecretKeys();
        for (int inx = 0; inx < issuerSecretKeys.length; inx++) {
            SecretKey issuerSecretKey = credMng
                    .getIssuerSecretKey(storedURIs
                            .get(inx));
            assertEquals(issuerSecretKeys[inx].getCryptoParams().getContent()
                    .get(0), issuerSecretKey.getCryptoParams().getContent().get(0));
        }
    }
}

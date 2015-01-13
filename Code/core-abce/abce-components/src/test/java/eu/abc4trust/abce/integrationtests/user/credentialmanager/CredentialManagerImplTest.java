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

package eu.abc4trust.abce.integrationtests.user.credentialmanager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.easymock.EasyMock;
import org.junit.Test;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerImpl;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialNotInStorageException;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCache;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheImpl;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentCredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentSecretStorage;
import eu.abc4trust.abce.internal.user.credentialManager.SecretStorage;
import eu.abc4trust.abce.testharness.ImagePathBuilder;
import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.user.CredentialSerializerGzipXml;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.InMemoryKeyStorage;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerImpl;
import eu.abc4trust.keyManager.PersistenceStrategy;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.util.ImageTestUtil;
import eu.abc4trust.util.TemporaryFileFactory;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;


public class CredentialManagerImplTest {    

    private static final URI EXPECTED_UUID = URI.create("ba419d35-0dfe-4af7-aee7-bbe10c45c028");

    private static CardStorage cardStorage = new CardStorage();
    private static final String USERNAME = "defaultUser";

    @Test
    public void attachMetadataToPseudonym() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Pseudonym p = new Pseudonym();
        p.setPseudonymUID(EXPECTED_UUID);
        p.setPseudonymValue(new byte[] { 1, 2, 3 });
        PseudonymMetadata md = new PseudonymMetadata();
        credMng.attachMetadataToPseudonym(USERNAME, p, md);
        PseudonymWithMetadata pmd = credMng.getPseudonym(USERNAME, p.getPseudonymUID());
        assertNotNull(pmd);
    }

    @Test
    public void getCredential() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        cred.setCredentialDescription(credDesc);
        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        Credential storedCred = credMng.getCredential(USERNAME, credUri);
        assertEquals(EXPECTED_UUID,
                storedCred.getCredentialDescription().getCredentialUID());
    }

    @Test
    public void getCredentialDescriptions() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential[] creds = new Credential[3];
        creds[0] = new Credential();
        creds[1] = new Credential();
        creds[2] = new Credential();

        URI[] issuerUris = { new URI("abc4trust:issuer1"),
                new URI("abc4trust:issuer2"), new URI("abc4trust:issuer3") };
        URI[] credSpecUris = { new URI("abc4trust:credspec1"),
                new URI("abc4trust:credspec2"), new URI("abc4trust:credspec3") };
        for (int inx = 0; inx < creds.length; inx++) {
            Credential cred = creds[inx];
            CredentialDescription credDesc = new CredentialDescription();
            credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
            credDesc.setIssuerParametersUID(issuerUris[inx]);
            credDesc.setCredentialSpecificationUID(credSpecUris[inx]);
            cred.setCredentialDescription(credDesc);
            URI credUid = new URI("Cred-Uri");
            credDesc.setCredentialUID(credUid);
            cred.setCredentialDescription(credDesc);
            URI credUri = credMng.storeCredential(USERNAME, cred);
            assertNotNull(credUri);
        }

        List<URI> issuers = new LinkedList<URI>();
        issuers.add(issuerUris[0]);
        issuers.add(issuerUris[2]);
        List<URI> credspecs = new LinkedList<URI>();
        credspecs.add(credSpecUris[2]);
        List<CredentialDescription> storedCredSpecs = credMng
                .getCredentialDescription(USERNAME, issuers,
                        credspecs);
        for (int inx = 0; inx < storedCredSpecs.size(); inx++) {
            assertEquals(issuerUris[2], storedCredSpecs.get(inx)
                    .getIssuerParametersUID());
        }
    }

    @Test
    public void getCredentialDescription() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        cred.setCredentialDescription(credDesc);
        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        CredentialDescription storedCredDesc = credMng
                .getCredentialDescription(USERNAME, credUri);
        assertEquals(EXPECTED_UUID, storedCredDesc.getCredentialUID());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasBeenRevokedCurrentFalse() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        URI expectedValue = new URI("abc4trust");
        credDesc.setCredentialUID(expectedValue);
        cred.setCredentialDescription(credDesc);

        CryptoEngineUser mockEngine = EasyMock
                .createMock(CryptoEngineUser.class);
        URI raparsUid = new URI("sample-uri");
        List<URI> revokedAttrs = new LinkedList<URI>();
        EasyMock.expect(
                mockEngine.updateNonRevocationEvidence(EasyMock.eq(USERNAME), 
                        EasyMock.isA(Credential.class),
                        EasyMock.isA(URI.class), EasyMock.isA(List.class)))
                        .andThrow(new CredentialWasRevokedException());
        EasyMock.replay(mockEngine);

        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, mockEngine, random, cardStorage, new CredentialSerializerGzipXml());

        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        assertTrue(credMng.hasBeenRevoked(USERNAME, credUri, raparsUid, revokedAttrs));

        EasyMock.verify(mockEngine);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasBeenRevokedCurrentTrue() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        URI expectedValue = new URI("abc4trust");
        credDesc.setCredentialUID(expectedValue);
        cred.setCredentialDescription(credDesc);

        CryptoEngineUser mockEngine = EasyMock
                .createMock(CryptoEngineUser.class);
        URI raparsUid = new URI("sample-uri");
        List<URI> revokedAttrs = new LinkedList<URI>();
        EasyMock.expect(
                mockEngine.updateNonRevocationEvidence(EasyMock.eq(USERNAME), 
                        EasyMock.isA(Credential.class),
                        EasyMock.isA(URI.class), EasyMock.isA(List.class)))
                        .andReturn(cred);
        EasyMock.replay(mockEngine);

        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, mockEngine, random, cardStorage, new CredentialSerializerGzipXml());

        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        assertFalse(credMng.hasBeenRevoked(USERNAME, credUri, raparsUid, revokedAttrs));

        EasyMock.verify(mockEngine);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasBeenRevokedFalse() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        URI expectedValue = new URI("abc4trust");
        credDesc.setCredentialUID(expectedValue);
        cred.setCredentialDescription(credDesc);

        CryptoEngineUser mockEngine = EasyMock
                .createMock(CryptoEngineUser.class);
        URI raparsUid = new URI("sample-uri");
        List<URI> revokedAttrs = new LinkedList<URI>();
        URI revinfouid = new URI("sample-revinfo-uid");
        EasyMock.expect(
                mockEngine.updateNonRevocationEvidence(EasyMock.eq(USERNAME), 
                        EasyMock.isA(Credential.class),
                        EasyMock.isA(URI.class), EasyMock.isA(List.class),
                        EasyMock.eq(revinfouid))).andThrow(
                                new CredentialWasRevokedException());
        EasyMock.replay(mockEngine);

        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, mockEngine, random, cardStorage, new CredentialSerializerGzipXml());

        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        assertTrue(credMng.hasBeenRevoked(USERNAME, credUri, raparsUid, revokedAttrs,
                revinfouid));

        EasyMock.verify(mockEngine);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasBeenRevokedTrue() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        URI expectedValue = new URI("abc4trust");
        credDesc.setCredentialUID(expectedValue);
        cred.setCredentialDescription(credDesc);

        CryptoEngineUser mockEngine = EasyMock
                .createMock(CryptoEngineUser.class);
        URI raparsUid = new URI("sample-uri");
        List<URI> revokedAttrs = new LinkedList<URI>();
        URI revinfouid = new URI("sample-revinfo-uid");
        EasyMock.expect(
                mockEngine.updateNonRevocationEvidence(EasyMock.eq(USERNAME), 
                        EasyMock.isA(Credential.class),
                        EasyMock.isA(URI.class), EasyMock.isA(List.class),
                        EasyMock.eq(revinfouid))).andReturn(cred);
        EasyMock.replay(mockEngine);

        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, mockEngine, random, cardStorage, new CredentialSerializerGzipXml());

        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        assertFalse(credMng.hasBeenRevoked(USERNAME, credUri, raparsUid, revokedAttrs,
                revinfouid));

        EasyMock.verify(mockEngine);
    }


    @Test
    public void listCredentials() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        random = new Random(42);
        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential[] creds = new Credential[3];
        creds[0] = new Credential();
        creds[1] = new Credential();
        creds[2] = new Credential();

        for (Credential cred : creds) {
            CredentialDescription credDesc = new CredentialDescription();
            credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
            cred.setCredentialDescription(credDesc);
            URI expectedValue = credMng.storeCredential(USERNAME, cred);
            assertNotNull(expectedValue);
        }

        URI[] expectedValues = new URI[3];
        expectedValues[0] = URI.create("ba419d35-0dfe-4af7-aee7-bbe10c45c028");
        expectedValues[1] = URI.create("4f083ce3-f12b-4b4b-86ee-9d82b52c856d");
        expectedValues[2] = URI.create("aa616abe-1761-4c9a-a743-67bd738597dc");
        List<URI> storedCredURIs = credMng.listCredentials(USERNAME);
        for (int inx = 0; inx < creds.length; inx++) {
            Credential credential = credMng.getCredential(USERNAME, storedCredURIs
                    .get(inx));
            assertEquals(expectedValues[inx], credential
                    .getCredentialDescription()
                    .getCredentialUID());
        }
    }

    @Test
    public void storeCredential() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        URI credUid = new URI("Cred-Uri");
        credDesc.setCredentialUID(credUid);
        cred.setCredentialDescription(credDesc);
        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
    }

    @Test
    public void storeAndGetPseudonymWithMetaData() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Pseudonym pseudonym = new Pseudonym();
        final byte[] pseudonymValue = new byte[] { 3, 2, 5 };
        pseudonym.setPseudonymValue(pseudonymValue);
        pseudonym.setPseudonymUID(EXPECTED_UUID);
        PseudonymWithMetadata pseudonymWithMetadata = new PseudonymWithMetadata();
        pseudonymWithMetadata.setPseudonym(pseudonym);
        credMng.storePseudonym(USERNAME, pseudonymWithMetadata);

        PseudonymWithMetadata storedPseudonymWithMetaData = credMng
                .getPseudonym(USERNAME, pseudonym.getPseudonymUID());

        assertArrayEquals(pseudonymWithMetadata.getPseudonym()
                .getPseudonymValue(), storedPseudonymWithMetaData
                .getPseudonym().getPseudonymValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateNonRevocationEvidence() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);

        URI credUid = new URI("abc4trust:orignal-credential");

        Credential orginalCred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        credDesc.setCredentialUID(credUid);
        URI originalIssuerParamsUid = new URI("abc4trust:issuer");
        credDesc.setIssuerParametersUID(originalIssuerParamsUid);
        URI credSpecUid = URI.create("credSpec");
        credDesc.setCredentialSpecificationUID(credSpecUid);
        orginalCred.setCredentialDescription(credDesc);

        CredentialSpecification credentialSpecification = new CredentialSpecification();
        credentialSpecification.setSpecificationUID(credSpecUid);
        credentialSpecification.setRevocable(true);

        keyManager.storeCredentialSpecification(credSpecUid,
                credentialSpecification);

        IssuerParameters issuerParameters = new IssuerParameters();
        URI revokationParametersUid = new URI("");
        issuerParameters.setRevocationParametersUID(revokationParametersUid);
        keyManager.storeIssuerParameters(originalIssuerParamsUid,
                issuerParameters);

        Credential updatedCred = new Credential();
        credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        credDesc.setCredentialUID(credUid);
        //        URI updatedCredUid = new URI("abc4trust:updated-credential");
        //        credDesc.setCredentialUID(updatedCredUid);
        updatedCred.setCredentialDescription(credDesc);

        CryptoEngineUser mockEngine = EasyMock
                .createMock(CryptoEngineUser.class);
        List<URI> revokedAttrs = new LinkedList<URI>();
        revokedAttrs.add(new URI(
                "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        EasyMock.expect(
                mockEngine.updateNonRevocationEvidence(EasyMock.eq(USERNAME), 
                        EasyMock.isA(Credential.class),
                        EasyMock.isA(URI.class), EasyMock.isA(List.class)))
                        .andReturn(updatedCred);
        EasyMock.replay(mockEngine);

        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, mockEngine, random, cardStorage, new CredentialSerializerGzipXml());

        URI credUri = credMng.storeCredential(USERNAME, orginalCred);
        assertNotNull(credUri);
        //This command updates the old credential. Does not create a new.
        credMng.updateNonRevocationEvidence(USERNAME);

        Credential testCredential = credMng.getCredential(USERNAME, updatedCred
                .getCredentialDescription().getCredentialUID());
        assertNotNull(testCredential);
        assertEquals(orginalCred.getCredentialDescription().getCredentialUID(),
                testCredential.getCredentialDescription().getCredentialUID());
        EasyMock.verify(mockEngine);
    }

    @Test
    public void getPseudonym() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        URI pseudonymUid = URI.create("foo-bar-pseudonym-uid");
        String scope = "http://universitypatras.gr/issuer";
        PseudonymWithMetadata pwm = this.createPseudonym(pseudonymUid, scope,
                true);

        credMng.storePseudonym(USERNAME, pwm);

        PseudonymWithMetadata storedPwm = credMng.getPseudonym(USERNAME, pseudonymUid);
        assertNotNull(storedPwm);

        assertEquals(pwm.getPseudonym().getScope(), storedPwm.getPseudonym()
                .getScope());
    }

    @Test(expected = CredentialManagerException.class)
    public void deletePseudonym() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        URI pseudonymUid = URI.create("foo-bar-pseudonym-uid");
        String scope = "http://universitypatras.gr/issuer";
        PseudonymWithMetadata pwm = this.createPseudonym(pseudonymUid, scope,
                true);

        credMng.storePseudonym(USERNAME, pwm);

        PseudonymWithMetadata storedPwm = credMng.getPseudonym(USERNAME, pseudonymUid);
        assertNotNull(storedPwm);

        assertEquals(pwm.getPseudonym().getScope(), storedPwm.getPseudonym()
                .getScope());

        credMng.deletePseudonym(USERNAME, pseudonymUid);

        storedPwm = credMng.getPseudonym(USERNAME, pseudonymUid);
    }

    private PseudonymWithMetadata createPseudonym(URI pseudonymUid,
            String scope, Boolean exclusive) {
        Pseudonym pseudonym = new Pseudonym();
        byte[] pv = new byte[3];
        pv[0] = 42;
        pv[1] = 84;
        pv[2] = 117;
        pseudonym.setExclusive(exclusive);
        pseudonym.setPseudonymUID(pseudonymUid);
        pseudonym.setPseudonymValue(pv);
        pseudonym.setScope(scope);

        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("");
        fd.setValue("");
        Metadata md = new Metadata();
        PseudonymMetadata pmd = new PseudonymMetadata();
        pmd.getFriendlyPseudonymDescription().add(fd);
        pmd.setMetadata(md);
        PseudonymWithMetadata pwm = new PseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
        pwm.setPseudonymMetadata(pmd);
        return pwm;
    }

    @Test
    public void listPseudonyms() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());


        PseudonymWithMetadata[] pwms = new PseudonymWithMetadata[3];
        URI p1Uid = URI.create("1");
        URI p2Uid = URI.create("2");
        URI p3Uid = URI.create("3");
        String p1Scope = "scope1";
        String p2Scope = "scope2";
        String p3Scope = "scope1";
        pwms[0] = this.createPseudonym(p1Uid, p1Scope, false);
        pwms[1] = this.createPseudonym(p2Uid, p2Scope, false);
        pwms[2] = this.createPseudonym(p3Uid, p3Scope, true);

        for (PseudonymWithMetadata pwm : pwms) {
            credMng.storePseudonym(USERNAME, pwm);
        }

        Pseudonym storedPseudonym = this.getPseudonymFromList(credMng, p2Scope,
                false);
        this.comparePseudonyms(p2Uid, p2Scope, false, storedPseudonym);

        storedPseudonym = this.getPseudonymFromList(credMng, p1Scope, true);
        this.comparePseudonyms(p3Uid, p3Scope, true, storedPseudonym);
    }

    private Pseudonym getPseudonymFromList(CredentialManagerImpl credMng,
            String p2Scope, boolean exlusive) throws CredentialManagerException {
        List<PseudonymWithMetadata> storedPwms = credMng.listPseudonyms(USERNAME, 
                p2Scope, exlusive);
        assertEquals(1, storedPwms.size());
        PseudonymWithMetadata storedPwm = storedPwms.get(0);
        Pseudonym storedPseudonym = storedPwm.getPseudonym();
        return storedPseudonym;
    }

    private void comparePseudonyms(URI uid, String scope,
            boolean exclusive, Pseudonym pseudonym) {
        assertEquals(uid, pseudonym.getPseudonymUID());
        assertEquals(scope, pseudonym.getScope());
        assertEquals(exclusive, pseudonym.isExclusive());
    }

    @Test
    public void storeSecret() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Secret sec = new Secret();
        SecretDescription secDesc = new SecretDescription();
        URI secuid = new URI("Sec-Uri");
        secDesc.setSecretUID(secuid);
        sec.setSecretDescription(secDesc);
        credMng.storeSecret(USERNAME, sec);
        Secret res = credMng.getSecret(USERNAME, secuid);
        assertNotNull(res);
    }

    @Test(expected = CredentialManagerException.class)
    public void deleteSecret() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());

        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());

        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        URI secUid = URI.create("foo-bar-secret-uid");

        Secret sec = new Secret();
        SecretDescription secDesc = new SecretDescription();

        secDesc.setSecretUID(secUid);
        sec.setSecretDescription(secDesc);

        credMng.storeSecret(USERNAME, sec);

        Secret res = credMng.getSecret(USERNAME, secUid);
        assertNotNull(res);


        credMng.deleteSecret(USERNAME, secUid);

        res = credMng.getSecret(USERNAME, secUid);
    }

    @Test
    public void storeImageTest() throws Exception {
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl credMng = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());

        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        cred.setCredentialDescription(credDesc);
        URI credUri = credMng.storeCredential(USERNAME, cred);
        assertNotNull(credUri);
        CredentialDescription storedCredDesc = credMng
                .getCredentialDescription(USERNAME, credUri);
        assertTrue(ImageTestUtil.compareImages(new File(ImagePathBuilder.TEST_IMAGE_JPG_STRING),
                new File(storedCredDesc.getImageReference())));
    }

    @Test
    public void deleteCredentialTest() throws Exception{
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                new InMemoryKeyStorage()), null);
        CryptoEngineUser engine = null;
        CredentialStorage credStore = new PersistentCredentialStorage(
                TemporaryFileFactory.createTemporaryFile(),
                TemporaryFileFactory.createTemporaryFile());
        SecretStorage secStore = new PersistentSecretStorage(
                TemporaryFileFactory.createTemporaryFile());
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        CredentialManagerImpl cm = new CredentialManagerImpl(credStore,
                secStore, keyManager, imCache, engine, random, cardStorage, new CredentialSerializerGzipXml());
        Credential cred = new Credential();
        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        cred.setCredentialDescription(credDesc);
        URI credUri = cm.storeCredential(USERNAME, cred);

        cm.deleteCredential(USERNAME, credUri);
        Credential nullCred = null;
        try{
            nullCred = cm.getCredential(USERNAME, credUri);
        }catch(CredentialNotInStorageException e){
            //Good - it should not be found!
        }
        assertTrue(nullCred == null);
    }
}

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

package eu.abc4trust.abce.keyManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.abc4trust.keyManager.InMemoryKeyStorage;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerImpl;
import eu.abc4trust.keyManager.KeyStorage;
import eu.abc4trust.keyManager.PersistenceStrategy;
import eu.abc4trust.keyManager.PersistentKeyStorage;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.SystemParameters;

public class KeyManagerTest {

    private static final String SPEC_VERSION = "1.0";

    @Test
    public void testStoreInspectorPublicKeyInMemoryKeyStorage()
            throws Exception {
        final InMemoryKeyStorage storage = new InMemoryKeyStorage();

        this.testStoreInspectorPublicKey(storage);
    }

    @Test
    public void testStoreInspectorPublicKeyPersistentKeyStorage()
            throws Exception {
        File temp = File.createTempFile("persistentKeyStorage", ".key");
        temp.deleteOnExit();
        final PersistentKeyStorage storage = new PersistentKeyStorage(temp);

        this.testStoreInspectorPublicKey(storage);
    }

    private void testStoreInspectorPublicKey(final KeyStorage storage)
            throws URISyntaxException, Exception, JAXBException,
            UnsupportedEncodingException, SAXException, IOException {
        InspectorPublicKey key = this.getInpectorPublicKey();
        URI uri = new URI("sample-uri");
        PersistenceStrategy persistensStrategy = new PersistenceStrategy(
                storage);
        KeyManager keyManager = new KeyManagerImpl(persistensStrategy, null);
        keyManager.storeInspectorPublicKey(uri, key);

        InspectorPublicKey storedKey = (InspectorPublicKey) persistensStrategy
                .loadObject(uri);
        this.compareInspectorPublicKeys(key, storedKey);
    }

    @Test
    public void testStoreAndGetInspectorPublicKey() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        InspectorPublicKey key = this.getInpectorPublicKey();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), null);
        keyManager.storeInspectorPublicKey(uri, key);

        InspectorPublicKey storedKey = keyManager.getInspectorPublicKey(uri);
        this.compareInspectorPublicKeys(key, storedKey);
    }

    private InspectorPublicKey getInpectorPublicKey() throws URISyntaxException {
        InspectorPublicKey key = new ObjectFactory().createInspectorPublicKey();
        key.setAlgorithmID(new URI("sample-algorithm"));
        key.setCryptoParams(null);
        key.setPublicKeyUID(new URI("sample-key"));
        key.setVersion(SPEC_VERSION);
        return key;
    }

    private void compareInspectorPublicKeys(InspectorPublicKey key,
            InspectorPublicKey storedKey) {
        assertEquals(key.getAlgorithmID(), storedKey.getAlgorithmID());
        assertEquals(key.getCryptoParams(), storedKey.getCryptoParams());
        assertEquals(key.getPublicKeyUID(), storedKey.getPublicKeyUID());
        assertEquals(key.getVersion(), storedKey.getVersion());
    }

    @Test
    public void testStoreAndGetIssuerParameters() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        IssuerParameters params = this.getIssuerParameters();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), null);
        keyManager.storeIssuerParameters(uri, params);

        IssuerParameters storedKey = keyManager.getIssuerParameters(uri);
        this.compareIssuerParameters(params, storedKey);
    }

    private void compareIssuerParameters(IssuerParameters params,
            IssuerParameters storedKey) {
        assertEquals(params.getAlgorithmID(), storedKey.getAlgorithmID());
        assertEquals(params.getCryptoParams(), storedKey.getCryptoParams());
        assertEquals(params.getCredentialSpecUID(),
                storedKey.getCredentialSpecUID());
        assertEquals(params.getKeyBindingInfo(),
                storedKey.getKeyBindingInfo());
        assertEquals(params.getHashAlgorithm(), storedKey.getHashAlgorithm());
        assertEquals(params.getParametersUID(), storedKey.getParametersUID());
        assertEquals(params.getRevocationParametersUID(),
                storedKey.getRevocationParametersUID());
        assertEquals(params.getSystemParameters(),
                storedKey.getSystemParameters());
        assertEquals(params.getVersion(), storedKey.getVersion());

    }

    private IssuerParameters getIssuerParameters() throws URISyntaxException {
        IssuerParameters params = new ObjectFactory().createIssuerParameters();
        params.setAlgorithmID(new URI("sample-uid"));
        params.setCredentialSpecUID(null);
        params.setCryptoParams(null);
        params.setKeyBindingInfo(null);
        params.setHashAlgorithm(new URI("sample-hash-algorithm"));
        params.setParametersUID(new URI("parms-uid"));
        params.setRevocationParametersUID(null);
        params.setSystemParameters(null);
        params.setVersion(SPEC_VERSION);
        return params;
    }

    @Test
    public void testStoreAndGetRevocationAuthorityParameters() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        RevocationAuthorityParameters params = this
                .getRevocationAuthorityParameters();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), null);
        keyManager.storeRevocationAuthorityParameters(uri, params);

        RevocationAuthorityParameters storedKey = keyManager
                .getRevocationAuthorityParameters(uri);
        this.compareRevocationAuthorityParameters(params, storedKey);
    }

    private void compareRevocationAuthorityParameters(
            RevocationAuthorityParameters params,
            RevocationAuthorityParameters storedKey) {
        assertEquals(params.getCryptoParams(), storedKey.getCryptoParams());
        assertEquals(params.getNonRevocationEvidenceReference(),
                storedKey.getNonRevocationEvidenceReference());
        assertEquals(params.getNonRevocationEvidenceUpdateReference(),
                storedKey.getNonRevocationEvidenceUpdateReference());
        assertEquals(params.getParametersUID(), storedKey.getParametersUID());
        assertEquals(params.getRevocationInfoReference(),
                storedKey.getRevocationInfoReference());
        assertEquals(params.getRevocationMechanism(),
                storedKey.getRevocationMechanism());
        assertEquals(params.getVersion(), storedKey.getVersion());
    }

    private RevocationAuthorityParameters getRevocationAuthorityParameters()
            throws URISyntaxException {
        RevocationAuthorityParameters params = new ObjectFactory()
        .createRevocationAuthorityParameters();
        params.setCryptoParams(null);
        params.setNonRevocationEvidenceReference(null);
        params.setNonRevocationEvidenceUpdateReference(null);
        params.setParametersUID(new URI("parms-uid"));
        params.setRevocationInfoReference(null);
        params.setRevocationMechanism(new URI("sample-mechanism"));
        params.setVersion(SPEC_VERSION);
        return params;
    }

    @Test
    public void testGetCurrentRevocationInformation() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        RevocationAuthorityParameters params = this
                .getRevocationAuthorityParameters();
        params.setRevocationInfoReference(new Reference());
        params.getRevocationInfoReference().getReferences()
        .add(new URI("my2.revocationproxy.abc4trust.eu"));
        URI uri = new URI("sample-uri");

        RevocationInformation revInfo = this.getRevocationInformation(42);
        RevocationMessage revMsg = new RevocationMessage();
        revMsg.setCryptoParams(new CryptoParams());
        revMsg.getCryptoParams().getAny().add(new ObjectFactory().createRevocationInformation(revInfo));

        RevocationProxy revocationProxy = EasyMock.createMock(RevocationProxy.class);
        EasyMock.expect(
                revocationProxy.processRevocationMessage(
                        EasyMock.anyObject(RevocationMessage.class),
                        EasyMock.anyObject(RevocationAuthorityParameters.class)))
                        .andReturn(revMsg);
        EasyMock.expect(
                revocationProxy.processRevocationMessage(
                        EasyMock.anyObject(RevocationMessage.class),
                        EasyMock.anyObject(RevocationAuthorityParameters.class)))
                        .andReturn(revMsg);
        EasyMock.replay(revocationProxy);
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage),
                revocationProxy);

        boolean b = keyManager.storeRevocationAuthorityParameters(uri, params);
        assertTrue(b);

        RevocationInformation ri = keyManager
        .getCurrentRevocationInformation(uri);
        assertNotNull(ri);

        this.compareRevocationInformation(revInfo, ri);

        byte[] value = inMemoryKeyStorage.getValue(new URI(
                KeyManagerImpl.CURRENT_REVOCATION_UID_STR));
        assertTrue((null != value) && (value.length != 0));

        RevocationInformation storedRi = keyManager.getCurrentRevocationInformation(uri);
        this.compareRevocationInformation(ri, storedRi);
    }

    private RevocationInformation getRevocationInformation(int i)
            throws URISyntaxException {
        RevocationInformation revInfo = new RevocationInformation();
        revInfo.setCreated(new GregorianCalendar(2011, 11, 22));
        revInfo.setExpires(new GregorianCalendar(2012, 11, 22));
        revInfo.setInformationUID(new URI("revocation-information:" + i));
        return revInfo;
    }

    private void compareRevocationInformation(RevocationInformation ri1,
            RevocationInformation ri2) {
        assertEquals(ri1.getCreated(), ri2.getCreated());
        assertEquals(ri1.getExpires(), ri2.getExpires());
        assertEquals(ri1.getInformationUID(), ri2.getInformationUID());
        assertEquals(ri1.getRevocationAuthorityParameters(),
                ri2.getRevocationAuthorityParameters());
        assertEquals(ri1.getVersion(), ri2.getVersion());
    }

    @Ignore
    @Test
    public void testGetRevocationInformation() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        RevocationAuthorityParameters params = this
                .getRevocationAuthorityParameters();
        params.setRevocationInfoReference(new Reference());
        params.getRevocationInfoReference().getReferences()
        .add(new URI("my2.revocationproxy.abc4trust.eu"));
        URI uri = new URI("sample-uri");

        RevocationInformation revInfo = this.getRevocationInformation(42);

        RevocationMessage revMsg = new RevocationMessage();
        revMsg.setCryptoParams(new CryptoParams());
        revMsg.getCryptoParams().getAny().add(revInfo);

        RevocationProxy revocationProxy = EasyMock
                .createMock(RevocationProxy.class);
        EasyMock.expect(
                revocationProxy.processRevocationMessage(
                        EasyMock.anyObject(RevocationMessage.class),
                        EasyMock.anyObject(RevocationAuthorityParameters.class)))
                        .andReturn(revMsg);
        EasyMock.replay(revocationProxy);

        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), revocationProxy);

        RevocationInformation ri = keyManager.getRevocationInformation(uri,
                revInfo.getInformationUID());
        assertNull(ri);

        boolean b = keyManager.storeRevocationAuthorityParameters(uri, params);
        assertTrue(b);

        ri = keyManager.getRevocationInformation(uri,
                revInfo.getInformationUID());
        assertNotNull(ri);

        this.compareRevocationInformation(revInfo, ri);

        byte[] value = inMemoryKeyStorage.getValue(revInfo.getInformationUID());
        assertTrue((null != value) && (value.length != 0));

        RevocationInformation storedRi = keyManager.getRevocationInformation(
                uri, revInfo.getInformationUID());
        this.compareRevocationInformation(ri, storedRi);
    }

    @Ignore
    @Test
    public void testGetCredentialSpecification() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        CredentialSpecification spec = new CredentialSpecification();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), null);
        keyManager.storeCredentialSpecification(uri, spec);

        CredentialSpecification storedKey = keyManager
                .getCredentialSpecification(uri);
        this.compareCredentialSpecification(spec, storedKey);
    }

    private void compareCredentialSpecification(CredentialSpecification spec,
            CredentialSpecification storedKey) {
        assertEquals(spec.getSpecificationUID(),
                storedKey.getSpecificationUID());
    }

    @Test
    public void testStoreAndGetSystemParameters() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        SystemParameters params = this.getSystemParameters();
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(
                inMemoryKeyStorage), null);
        keyManager.storeSystemParameters(params);

        SystemParameters storedKey = keyManager.getSystemParameters();
        assertEquals(params.getVersion(), storedKey.getVersion());
    }

    private SystemParameters getSystemParameters() {
        SystemParameters params = new ObjectFactory().createSystemParameters();
        params.setVersion(SPEC_VERSION);
        return params;
    }
}

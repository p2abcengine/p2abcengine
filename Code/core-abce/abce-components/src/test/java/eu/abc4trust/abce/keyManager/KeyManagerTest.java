//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

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
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
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
    public void testStoreInspectorPublicKeyInMemoryKeyStorage() throws Exception {
        final InMemoryKeyStorage storage = new InMemoryKeyStorage();

        this.testStoreInspectorPublicKey(storage);
    }

    @Test
    public void testStoreInspectorPublicKeyPersistentKeyStorage() throws Exception {
        File temp = File.createTempFile("persistentKeyStorage", ".key");
        temp.deleteOnExit();
        final PersistentKeyStorage storage = new PersistentKeyStorage(temp);

        this.testStoreInspectorPublicKey(storage);
    }

    private void testStoreInspectorPublicKey(final KeyStorage storage) throws URISyntaxException,
    Exception, JAXBException, UnsupportedEncodingException, SAXException, IOException {
        InspectorPublicKey key = this.getInpectorPublicKey();
        URI uri = new URI("sample-uri");
        PersistenceStrategy persistensStrategy = new PersistenceStrategy(storage);
        KeyManager keyManager = new KeyManagerImpl(persistensStrategy, null);
        keyManager.storeInspectorPublicKey(uri, key);

        InspectorPublicKey storedKey = (InspectorPublicKey) persistensStrategy.loadObject(uri);
        this.compareInspectorPublicKeys(key, storedKey);
    }

    @Test
    public void testStoreAndGetInspectorPublicKey() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        InspectorPublicKey key = this.getInpectorPublicKey();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), null);
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

    private void compareInspectorPublicKeys(InspectorPublicKey key, InspectorPublicKey storedKey) {
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
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), null);
        keyManager.storeIssuerParameters(uri, params);

        IssuerParameters storedKey = keyManager.getIssuerParameters(uri);
        this.compareIssuerParameters(params, storedKey);
    }

    private void compareIssuerParameters(IssuerParameters params, IssuerParameters storedKey) {
        assertEquals(params.getAlgorithmID(), storedKey.getAlgorithmID());
        assertEquals(params.getCryptoParams(), storedKey.getCryptoParams());
        assertEquals(params.getMaximalNumberOfAttributes(), storedKey.getMaximalNumberOfAttributes());
        assertEquals(params.getHashAlgorithm(), storedKey.getHashAlgorithm());
        assertEquals(params.getParametersUID(), storedKey.getParametersUID());
        assertEquals(params.getRevocationParametersUID(), storedKey.getRevocationParametersUID());
        assertEquals(params.getSystemParametersUID(), storedKey.getSystemParametersUID());
        assertEquals(params.getVersion(), storedKey.getVersion());

    }

    private IssuerParameters getIssuerParameters() throws URISyntaxException {
        IssuerParameters params = new ObjectFactory().createIssuerParameters();
        params.setAlgorithmID(new URI("sample-uid"));
        params.setMaximalNumberOfAttributes(0);
        params.setCryptoParams(null);
        params.setHashAlgorithm(new URI("sample-hash-algorithm"));
        params.setParametersUID(new URI("parms-uid"));
        params.setRevocationParametersUID(null);
        params.setSystemParametersUID(null);
        params.setVersion(SPEC_VERSION);
        return params;
    }

    @Test
    public void testStoreAndGetRevocationAuthorityParameters() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        URI raParametersUID = new URI("sample-uri");
        RevocationAuthorityParameters raParameters =
                this.getRevocationAuthorityParameters(raParametersUID);

        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), null);
        keyManager.storeRevocationAuthorityParameters(raParametersUID, raParameters);

        RevocationAuthorityParameters storedKey =
                keyManager.getRevocationAuthorityParameters(raParametersUID);
        this.compareRevocationAuthorityParameters(raParameters, storedKey);
    }

    private void compareRevocationAuthorityParameters(RevocationAuthorityParameters params,
            RevocationAuthorityParameters storedKey) {
        assertEquals(params.getCryptoParams(), storedKey.getCryptoParams());
        assertEquals(params.getNonRevocationEvidenceReference(),
                storedKey.getNonRevocationEvidenceReference());
        assertEquals(params.getNonRevocationEvidenceUpdateReference(),
                storedKey.getNonRevocationEvidenceUpdateReference());
        assertEquals(params.getParametersUID(), storedKey.getParametersUID());
        assertEquals(params.getRevocationInfoReference(), storedKey.getRevocationInfoReference());
        assertEquals(params.getRevocationMechanism(), storedKey.getRevocationMechanism());
        assertEquals(params.getVersion(), storedKey.getVersion());
    }

    private RevocationAuthorityParameters getRevocationAuthorityParameters(URI raParametersUID)
            throws URISyntaxException {
        RevocationAuthorityParameters params =
                new ObjectFactory().createRevocationAuthorityParameters();
        params.setCryptoParams(null);
        params.setNonRevocationEvidenceReference(null);
        params.setNonRevocationEvidenceUpdateReference(null);
        params.setParametersUID(raParametersUID);
        params.setRevocationInfoReference(null);
        params.setRevocationMechanism(new URI("sample-mechanism"));
        params.setVersion(SPEC_VERSION);
        return params;
    }

    @Test
    public void testGetCurrentRevocationInformation() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        URI raParametersUID = new URI("sample-uri");
        RevocationAuthorityParameters params = this.getRevocationAuthorityParameters(raParametersUID);

        RevocationInformation revInfo_expired = this.getRevocationInformation(42, true);
        RevocationInformation revInfo_valid = this.getRevocationInformation(43, false);

        RevocationProxy revocationProxy = this.setupRevocationProxyMock(revInfo_valid, params, 2);

        KeyManager keyManager =
                new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), revocationProxy);

        boolean b = keyManager.storeRevocationAuthorityParameters(raParametersUID, params);
        assertTrue(b);
        keyManager.storeRevocationInformation(revInfo_expired.getRevocationInformationUID(),
                revInfo_expired);

        RevocationInformation ri = keyManager
                .getCurrentRevocationInformation(revInfo_expired
                        .getRevocationInformationUID());
        assertNull(ri);
        ri = keyManager.getLatestRevocationInformation(raParametersUID);
        assertNotNull(ri);

        this.compareRevocationInformation(revInfo_valid, ri);

        byte[] value = inMemoryKeyStorage.getValue(new URI(raParametersUID.toString() + KeyManagerImpl.CURRENT_REVOCATION_UID_STR));
        assertTrue((null != value) && (value.length != 0));

        RevocationInformation storedRi = keyManager.getCurrentRevocationInformation(raParametersUID);
        this.compareRevocationInformation(ri, storedRi);
    }

    private RevocationInformation getRevocationInformation(int i, boolean expired)
            throws URISyntaxException {
        RevocationInformation revInfo = new RevocationInformation();

        Calendar created = new GregorianCalendar();
        created.roll(Calendar.MONTH, false);
        Calendar expires = null;
        if (expired) {
            expires = created;
        } else {
            expires = new GregorianCalendar();
        }
        expires.roll(Calendar.DAY_OF_MONTH, true);
        revInfo.setCreated(created);
        revInfo.setExpires(expires);

        revInfo.setRevocationInformationUID(new URI("revocation-information:" + i));
        return revInfo;
    }

    private void compareRevocationInformation(RevocationInformation ri1, RevocationInformation ri2) {
        assertEquals(ri1.getCreated(), ri2.getCreated());
        assertEquals(ri1.getExpires(), ri2.getExpires());
        assertEquals(ri1.getRevocationInformationUID(), ri2.getRevocationInformationUID());
        assertEquals(ri1.getRevocationAuthorityParametersUID(),
                ri2.getRevocationAuthorityParametersUID());
        assertEquals(ri1.getVersion(), ri2.getVersion());
    }

    private RevocationProxy setupRevocationProxyMock(RevocationInformation revocationInformation,
            RevocationAuthorityParameters raParameters, int numberOfRevocationMessages) throws Exception {

        raParameters.setRevocationInfoReference(new Reference());
        raParameters.getRevocationInfoReference().getReferences()
        .add(new URI("my2.revocationproxy.abc4trust.eu"));

        RevocationMessage revMsg = new RevocationMessage();
        revMsg.setCryptoParams(new CryptoParams());
        revMsg.getCryptoParams().getContent()
        .add(new ObjectFactory().createRevocationInformation(revocationInformation));

        RevocationProxy revocationProxy = EasyMock.createMock(RevocationProxy.class);
        for (int i = 0; i < numberOfRevocationMessages; i++) {
            EasyMock.expect(
                    revocationProxy.processRevocationMessage(EasyMock.anyObject(RevocationMessage.class),
                            EasyMock.anyObject(RevocationAuthorityParameters.class))).andReturn(revMsg);
        }
        EasyMock.replay(revocationProxy);

        return revocationProxy;
    }

    @Test
    public void testGetRevocationInformation() throws Exception {

        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        URI raParametersUID = new URI("sample-uri");
        RevocationAuthorityParameters raParameters =
                this.getRevocationAuthorityParameters(raParametersUID);

        RevocationInformation revInfo_expired = this.getRevocationInformation(42, true);
        RevocationInformation revInfo_valid = this.getRevocationInformation(43, false);

        RevocationProxy revocationProxy = this.setupRevocationProxyMock(revInfo_valid, raParameters, 1);

        KeyManager keyManager =
                new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), revocationProxy);


        // NOTE (pbi) the method getRevocationInformation tries to fetch the given revocation
        // information from the key store - if this fails is contacts the revocation authority. For that
        // reason this test cannot be performed as it used to be. If a different behaviour is desired,
        // the methods getLatestRevocationInformation (getting the RI from the RA) and
        // getCurrentRevocationInformation (only trying to retrieve it locally) are alternatives.
        //
        //
        // RevocationInformation ri = keyManager.getRevocationInformation(raParametersUID,
        // revInfo.getRevocationInformationUID());
        // assertNull(ri);

        boolean b = keyManager.storeRevocationAuthorityParameters(raParametersUID, raParameters);
        assertTrue(b);
        keyManager.storeRevocationInformation(
                revInfo_expired.getRevocationInformationUID(),
                revInfo_expired);

        RevocationInformation ri = keyManager.getRevocationInformation(
                URI.create(""),
                revInfo_expired.getRevocationInformationUID());
        assertNotNull(ri);

        this.compareRevocationInformation(revInfo_expired, ri);

        byte[] value = inMemoryKeyStorage.getValue(revInfo_expired.getRevocationInformationUID());
        assertTrue((null != value) && (value.length != 0));

        RevocationInformation storedRi = keyManager.getRevocationInformation(
                URI.create(""),
                revInfo_expired.getRevocationInformationUID());
        this.compareRevocationInformation(ri, storedRi);

        // allow the key manager to retrieve a valid revocation information from the revocation proxy
        RevocationInformation validRI = keyManager.getLatestRevocationInformation(raParametersUID);
        this.compareRevocationInformation(revInfo_valid, validRI);

    }

    @Test
    public void testGetCredentialSpecification() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        CredentialSpecification spec = new CredentialSpecification();
        URI uri = new URI("sample-uri");
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), null);
        keyManager.storeCredentialSpecification(uri, spec);

        CredentialSpecification storedKey = keyManager.getCredentialSpecification(uri);
        this.compareCredentialSpecification(spec, storedKey);
    }

    private void compareCredentialSpecification(CredentialSpecification spec,
            CredentialSpecification storedKey) {
        assertEquals(spec.getSpecificationUID(), storedKey.getSpecificationUID());
    }

    @Test
    public void testStoreAndGetSystemParameters() throws Exception {
        final InMemoryKeyStorage inMemoryKeyStorage = new InMemoryKeyStorage();

        SystemParameters params = this.getSystemParameters();
        KeyManager keyManager = new KeyManagerImpl(new PersistenceStrategy(inMemoryKeyStorage), null);
        keyManager.storeSystemParameters(params);

        SystemParameters storedKey = keyManager.getSystemParameters();
        assertEquals(params.getVersion(), storedKey.getVersion());
    }

    private SystemParameters getSystemParameters() {
        SystemParameters params = new ObjectFactory().createSystemParameters();
        params.setCryptoParams(new ObjectFactory().createCryptoParams());
        params.setVersion(SPEC_VERSION);
        return params;
    }
}

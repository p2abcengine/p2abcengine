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

package eu.abc4trust.abce.external.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.inspector.InspectorSystemParameterException;
import eu.abc4trust.cryptoEngine.inspector.UnknownInspectorPrivateKey;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class InspectorTest {
    private static final CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
    private static final URI cryptoMech = CryptoUriUtil.getIdemixMechanism();
    private static final Random random = new Random(1234);
    private static final URI sampleURI = URI.create("sample-key");
    private static final Injector injector =
            Guice.createInjector(IntegrationModuleFactory.newModule(InspectorTest.random,
                    InspectorTest.cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
    private InputSource getResource(String filename) {
        return new InputSource(this.getClass().getResourceAsStream("/eu/abc4trust/sampleXml/" + filename));
    }

    private void setupSystemParameters() {
        com.ibm.zurich.idmx.utils.SystemParameters sp =
                (com.ibm.zurich.idmx.utils.SystemParameters) Parser.getInstance().parse(
                        this.getResource("idemix/sp.xml"));
        StructureStore.getInstance().add(IdemixConstants.systemParameterId, sp);
        KeyManager keyManager = InspectorTest.injector.getInstance(KeyManager.class);
        try {
            // First see if we have created this.
            if (keyManager.hasSystemParameters()) {
                return;
            }
            SystemParameters params = new ObjectFactory().createSystemParameters();
            params.setVersion("1.0");
            params.getAny().add(sp);
            keyManager.storeSystemParameters(params);
        } catch (KeyManagerException e) {
            // ignore for now.
        }
    }

    @Test (expected=IllegalArgumentException.class)
    public void testSupportedCryptoMech() throws URISyntaxException, Exception {
        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);
        @SuppressWarnings("unused")
        InspectorPublicKey key = engine.setupInspectorPublicKey(1024,
                URI.create("not used"),
                InspectorTest.sampleURI);
        // We should not get here.
        assertFalse(true);

    }

    @Ignore
    @Test (expected=InspectorSystemParameterException.class)
    public void verifySystemParameters() throws URISyntaxException, Exception {
        com.ibm.zurich.idmx.utils.SystemParameters sp =
                (com.ibm.zurich.idmx.utils.SystemParameters) Parser.getInstance().parse(
                        this.getResource("idemix/sp.xml"));
        StructureStore.getInstance().add(IdemixConstants.systemParameterId, sp);
        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);
        // First we ensure that we create a new public key.
        @SuppressWarnings("unused")
        InspectorPublicKey key = engine.setupInspectorPublicKey(1024,
                InspectorTest.cryptoMech,
                InspectorTest.sampleURI);
        // We should not get here.
        assertFalse(true);
    }

    @Test
    public void getInspectorPublicKey() throws URISyntaxException, Exception {
        this.setupSystemParameters();
        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);
        // First we ensure that we create a new public key.
        InspectorPublicKey key = engine.setupInspectorPublicKey(1024,
                InspectorTest.cryptoMech,
                InspectorTest.sampleURI);
        // Ensure we get the same key if we call again
        InspectorPublicKey key1 = engine.setupInspectorPublicKey(1024,
                InspectorTest.cryptoMech,
                InspectorTest.sampleURI);
        // test that we get the keys and they are not null
        assertNotNull("New created key failed", key);
        assertNotNull("Get the new key failed", key1);
        // test that we get same algo ID
        assertEquals(key.getAlgorithmID(), key1.getAlgorithmID());
        // test that the URI is the same
        assertEquals(key.getPublicKeyUID(), key1.getPublicKeyUID());
    }

    @Test
    public void testContentOfInspectorPublicKey() throws URISyntaxException, Exception {
        this.setupSystemParameters();
        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);
        // First we ensure that we create a new public key.
        InspectorPublicKey key = engine.setupInspectorPublicKey(1024,
                InspectorTest.cryptoMech,
                InspectorTest.sampleURI);
        assertNotNull("Key key failed", key);
        VEPublicKey vePubKey = (VEPublicKey)Parser.getInstance().parse((org.w3c.dom.Element)key.getCryptoParams().getAny().get(0));
        assertNotNull("VEPublic key was null", vePubKey);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testNullPresentationToken() throws Exception {
        this.setupSystemParameters();
        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);

        @SuppressWarnings("unused")
        List<Attribute> attributes = engine.inspect(null);
        // Should not get to here.
        assertFalse(true);

    }
    private URI getInspectorPkUIDFromToken(PresentationToken t) {
        List<CredentialInToken> credTokenList = t.getPresentationTokenDescription().getCredential();
        for (CredentialInToken credToken : credTokenList) {
            List<AttributeInToken> attrTokenList = credToken.getDisclosedAttribute();
            for (AttributeInToken attrToken : attrTokenList) {
                if (attrToken.getInspectorPublicKeyUID() != null) {
                    return attrToken.getInspectorPublicKeyUID();
                }
            }
        }
        throw new IllegalArgumentException("No inspector public key found in PresentationToken");
    }

    @Ignore // the inspector engine might be told to inspect a pt with multiple inspectable attrs
    //and should therefore handle missing keys nicely
    @Test (expected=UnknownInspectorPrivateKey.class)
    public void testUnknownPrivateKeyFromInspect() throws Exception {
        this.setupSystemParameters();
        String xml_resource = "/eu/abc4trust/sampleXml/presentationTokens/presentationTokenHotelOption1.xml";
        InputStream is = this.getClass().getResourceAsStream(xml_resource);
        PresentationToken pt = (PresentationToken) XmlUtils.getObjectFromXML(is, false);

        CryptoEngineInspector engine = InspectorTest.injector.getInstance(CryptoEngineInspector.class);

        @SuppressWarnings("unused")
        List<Attribute> attributes = engine.inspect(pt);
        // Should not get to here
        assertFalse(true);
    }

}

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

package eu.abc4trust.abce.external.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.SystemParameters;

public class InspectorTest {
    private static final CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
    private static final URI cryptoMech = CryptoUriUtil.getIdemixMechanism();
    private static final Random random = new Random(1234);
    private static final URI sampleURI = URI.create("sample-key");
    private static final Injector injector =
            Guice.createInjector(IntegrationModuleFactory.newModule(InspectorTest.random,
                    InspectorTest.cryptoEngine));

    private SystemParameters setupSystemParameters() throws KeyManagerException {
        KeyManager keyManager = InspectorTest.injector.getInstance(KeyManager.class);
        try {
          // First see if we have created this.
          if (keyManager.hasSystemParameters()) {
            return keyManager.getSystemParameters();
          }
          SystemParameters sp = SystemParametersUtil.getDefaultSystemParameters_1024();
          keyManager.storeSystemParameters(sp);
          return sp;
        } catch (KeyManagerException e) {
          // ignore for now.
      	throw e;
        }
      }

      @Test (expected=IllegalArgumentException.class, timeout=TestConfiguration.TEST_TIMEOUT)
      public void testSupportedCryptoMech() throws URISyntaxException, Exception {
          InspectorAbcEngine engine = InspectorTest.injector.getInstance(InspectorAbcEngine.class);
          SystemParameters sp = setupSystemParameters();
          List<FriendlyDescription> friendlyDescription = Collections.emptyList();
          @SuppressWarnings("unused")
          InspectorPublicKey key = engine.setupInspectorPublicKey(sp,
                  URI.create("not used"),
                  InspectorTest.sampleURI,
                  friendlyDescription);
          // We should not get here.
          assertFalse(true);

      }


      @Test(timeout=TestConfiguration.TEST_TIMEOUT)
      public void getInspectorPublicKey() throws URISyntaxException, Exception {
          SystemParameters sp = setupSystemParameters();
          InspectorAbcEngine engine = InspectorTest.injector.getInstance(InspectorAbcEngine.class);
          List<FriendlyDescription> friendlyDescription = Collections.emptyList();
          // First we ensure that we create a new public key.
          InspectorPublicKey key = engine.setupInspectorPublicKey(sp,
                  InspectorTest.cryptoMech,
                  InspectorTest.sampleURI, friendlyDescription);
          // Ensure we get the same key if we call again
          InspectorPublicKey key1 = engine.setupInspectorPublicKey(sp,
                  InspectorTest.cryptoMech,
                  InspectorTest.sampleURI,friendlyDescription);
          // test that we get the keys and they are not null
          assertNotNull("New created key failed", key);
          assertNotNull("Get the new key failed", key1);
          // test that we get same algo ID
          assertEquals(key.getAlgorithmID(), key1.getAlgorithmID());
          // test that the URI is the same
          assertEquals(key.getPublicKeyUID(), key1.getPublicKeyUID());
      }

      @Test(timeout=TestConfiguration.TEST_TIMEOUT)
      public void testContentOfInspectorPublicKey() throws URISyntaxException, Exception {
          SystemParameters sp = setupSystemParameters();
          InspectorAbcEngine engine = InspectorTest.injector.getInstance(InspectorAbcEngine.class);
          List<FriendlyDescription> friendlyDescription = Collections.emptyList();
          // First we ensure that we create a new public key.
          InspectorPublicKey key = engine.setupInspectorPublicKey(sp,
                  InspectorTest.cryptoMech,
                  InspectorTest.sampleURI, friendlyDescription);
          assertNotNull("Key key failed", key);
      }
  }

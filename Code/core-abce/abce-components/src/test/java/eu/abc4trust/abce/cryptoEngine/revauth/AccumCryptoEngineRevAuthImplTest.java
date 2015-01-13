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

package eu.abc4trust.abce.cryptoEngine.revauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.integrationtests.Helper;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;

public class AccumCryptoEngineRevAuthImplTest {

    private URI REV_MECH;
    private static final int keyLength = 1024;
    private static final URI uid = URI.create("urn:abc4trust:1.0:uid");;

    @Before
    public void setup() throws ConfigurationException{
    	REV_MECH = Helper.getRevocationTechnologyURI("cl");    	
    }
    
    @Test
    public void testSetupRevocationAuthorityParameters()
            throws CryptoEngineException, KeyManagerException {
        Injector injector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.IDEMIX));
        KeyManager keyManager = injector.getInstance(KeyManager.class);
        keyManager.storeSystemParameters(SystemParametersUtil.getDefaultSystemParameters_1024());
        RevocationAbcEngine revAuth = injector
                .getInstance(RevocationAbcEngine.class);
        Reference infoRef = new Reference();
        infoRef.setReferenceType(URI.create("https"));
        infoRef.getReferences().add(URI.create("https://example.org"));
        Reference evidenceRef = new Reference();
        evidenceRef.setReferenceType(URI.create("https"));
        evidenceRef.getReferences().add(URI.create("https://example.org"));
        Reference updateRef = new Reference();
        updateRef.setReferenceType(URI.create("https"));
        updateRef.getReferences().add(URI.create("https://example.org"));
        RevocationAuthorityParameters revAuthParams = revAuth
                .setupRevocationAuthorityParameters(
                        keyLength, REV_MECH, uid, infoRef, evidenceRef,
                        updateRef);
        
        assertNotNull(revAuthParams.getParametersUID());
        assertNotNull(revAuthParams.getCryptoParams());
        assertNotNull(revAuthParams.getNonRevocationEvidenceReference());
        assertNotNull(revAuthParams.getNonRevocationEvidenceUpdateReference());
        assertNotNull(revAuthParams.getRevocationInfoReference());
        assertNotNull(revAuthParams.getRevocationMechanism());

        assertEquals(REV_MECH, revAuthParams.getRevocationMechanism());
    }

    @Ignore
    @Test
    public void testGenerateNonRevocationEvidence() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testUpdateRevocationInformation() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testUpdateNonRevocationEvidence() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testRevoke() {
        fail("Not yet implemented");
    }
}

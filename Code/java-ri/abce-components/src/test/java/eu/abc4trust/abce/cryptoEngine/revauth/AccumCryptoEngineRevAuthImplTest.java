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

package eu.abc4trust.abce.cryptoEngine.revauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.revauth.AccumCryptoEngineRevAuthImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

// TODO(enr): You need to load the correct system parameters
@Ignore
public class AccumCryptoEngineRevAuthImplTest {

    private static final URI REV_MECH = URI
            .create("urn:abc4trust:1.0:algorithm:accum:cl");
    private static final String VERSION = "1.0";
    private static final int keyLength = 0;
    private static final URI uid = URI.create("urn:abc4trust:1.0:uid");;

    @Test
    public void testSetupRevocationAuthorityParameters()
            throws CryptoEngineException {
        Injector injector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        CryptoEngine.IDEMIX, UProveUtils.UPROVE_COMMON_PORT));
        RevocationAbcEngine revAuth = injector
                .getInstance(RevocationAbcEngine.class);
        Reference infoRef = new Reference();
        infoRef.setReferenceType(URI.create("https"));
        infoRef.getReferences().add(URI.create("example.org"));
        Reference evidenceRef = new Reference();
        evidenceRef.setReferenceType(URI.create("https"));
        evidenceRef.getReferences().add(URI.create("example.org"));
        Reference updateRef = new Reference();
        updateRef.setReferenceType(URI.create("https"));
        updateRef.getReferences().add(URI.create("example.org"));
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
        assertNotNull(revAuthParams.getVersion());

        assertEquals(REV_MECH, revAuthParams.getRevocationMechanism());
        assertEquals(VERSION, revAuthParams.getVersion());
    }

    @Ignore
    @Test
    public void testGenerateNonRevocationEvidence() {
        fail("Not yet implemented");
    }

    @Test
    public void testGenerateRevocationInformation()
            throws CryptoEngineException {
        Injector injector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1984), CryptoEngine.IDEMIX, null));
        AccumCryptoEngineRevAuthImpl revAuth = injector
                .getInstance(AccumCryptoEngineRevAuthImpl.class);
        Reference infoRef = new Reference();
        infoRef.setReferenceType(URI.create("https"));
        infoRef.getReferences().add(URI.create("example.org"));
        Reference evidenceRef = new Reference();
        evidenceRef.setReferenceType(URI.create("https"));
        evidenceRef.getReferences().add(URI.create("example.org"));
        Reference updateRef = new Reference();
        updateRef.setReferenceType(URI.create("https"));
        updateRef.getReferences().add(URI.create("example.org"));
        RevocationAuthorityParameters revAuthParams = revAuth
                .setupRevocationAuthorityParameters(keyLength, REV_MECH, uid,
                        infoRef, evidenceRef, updateRef);

        RevocationInformation revocationInformation = revAuth
                .generateRevocationInformation(revAuthParams.getParametersUID());
        assertNotNull(revocationInformation.getInformationUID());
        assertNotNull(revocationInformation.getCryptoParams());
        assertNotNull(revocationInformation.getCreated());
        assertNotNull(revocationInformation.getExpires());
        assertNotNull(revocationInformation.getRevocationAuthorityParameters());
        assertNotNull(revocationInformation.getVersion());

        assertEquals(uid,
                revocationInformation.getRevocationAuthorityParameters());
        assertEquals(VERSION, revocationInformation.getVersion());
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

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

package eu.abc4trust.smartcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import eu.abc4trust.abce.testharness.ImagePathBuilder;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.CredentialSerializerGzipXml;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializerGzipXml;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymWithMetadata;

public class SoftwareSmartcardTest {

    private static final int pin = 1234;
    private static int puk = 12345678;
    private static BigInteger challenge;
    private static final URI deviceURI = URI.create("ImbaDeviceNo42");
    private static final Random rand = new Random(42);
    private static final CredentialSerializer serializer = new CredentialSerializerGzipXml();

    public static SoftwareSmartcard setupSmartcard() {
    	//init the structure store
    	//StructureStore.getInstance().add(IdemixConstants.groupParameterId, StaticGroupParameters.getGroupParameters());
    	
        short deviceID = 1;
        SystemParameters sp = getSystemParameters();
        // deviceSecret = 86127401088496880082801127003646828744375250417716244414750855766766755168176
        SoftwareSmartcard s = new SoftwareSmartcard(new Random(42));
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
        puk = s.init(pin, sp, key, deviceID);
        SmartcardBlob deviceUriBlob = new SmartcardBlob();
		try {
			deviceUriBlob.blob = deviceURI.toASCIIString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("US-ASCII not supported exception.", e);
		}
        s.storeBlob(pin, Smartcard.device_name, deviceUriBlob);
        
        //Setup challenge to new random
        challenge = new BigInteger(32, rand);
        return s;
    }

    public static SmartcardParameters getCredentialBases() {
        BigInteger p = new BigInteger("13983014021825029330432041492790970071621084464773656969911606241061807342940154950776220801109453405514836004504505950014655952003125355018122289242812307");
        BigInteger q = new BigInteger("4263165099819514102133474591208918364260622516330751115416512365230148267352138796296838231373257204547230954382685845694047366165171100272638554618056287");
        // n = 59611897368131366507364942276670668747995323268474975289271614234218937009935107719366762946802866847720072176517579036188074246487493444834020695796207762776438347095237876273178780516676456685032464805596922499374627605877256498945463430940126396361032161179291118883563104866211893115282075236693902324109
        BigInteger n = p.multiply(q);
        BigInteger R0 = BigInteger.valueOf(4);
        BigInteger S = BigInteger.valueOf(9);
        return SmartcardParameters.forTwoBaseCl(n, R0, S);
    }

    private static SystemParameters getSystemParameters() {
        SystemParameters sp = new SystemParameters();
        // A 257.9-bit safe prime
        sp.p = new BigInteger("434669094696085191199122398486087525063835578234110975424949095034621531078563");
        sp.g = BigInteger.valueOf(3);
        sp.subgroupOrder = sp.p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        sp.deviceSecretSizeBytes = 256 / 8;
        sp.signatureNonceLengthBytes = 80 / 8;
        sp.zkChallengeSizeBytes = 256;
        sp.zkStatisticalHidingSizeBytes = 80 / 8;
        return sp;
    }

    @Test
    public void testNonInitialized() {
        SoftwareSmartcard s = new SoftwareSmartcard(rand);
        URI someUri = URI.create("Hello");
        int pin = 1234;
        String password ="";
        RSAKeyPair rs = new RSAKeyPair(BigInteger.ZERO, BigInteger.ZERO);
        RSAVerificationKey rv = new RSAVerificationKey();
        rv.n = BigInteger.ZERO;
        SmartcardParameters cb = SmartcardParameters.forTwoBaseCl(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        assertEquals(s.addIssuerParameters(rs, someUri, cb), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.addIssuerParametersWithAttendanceCheck(rs, someUri, 0, cb, rv, 0), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.allocateCredential(0, someUri, someUri), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.backupAttendanceData(0, password), null);
        assertEquals(s.changePin(0, 0), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.computeCredentialFragment(0, null), null);
        assertEquals(s.computeDevicePublicKey(0), null);
        assertEquals(s.computeScopeExclusivePseudonym(0, null), null);
        assertEquals(s.deleteBlob(0, null), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.deleteCredential(0, null), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.deleteIssuer(pin, someUri, null), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.finalizeZkProof(0, null, null, null), null);
        assertEquals(s.getBlob(0, null), null);
        assertEquals(s.getBlobs(0), null);
        assertEquals(s.getBlobUris(0), null);
        assertEquals(s.getCourse(0, null), null);
        assertEquals(s.getDeviceURI(0), null);
        assertEquals(s.getIssuerParameters(0, null), null);
        assertEquals(s.getIssuerParametersList(0), null);
        assertEquals(s.getIssuerParametersOfCredential(0, null), null);
        assertEquals(s.getNewNonceForSignature(), null);
        assertEquals(s.getSystemParameters(0), null);
        assertEquals(s.incrementCourseCounter(0, null, null, 0), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.listCourses(0), null);
        assertEquals(s.listCredentialsUris(0), null);
        assertEquals(s.pinTrialsLeft(), 0);
        assertEquals(s.prepareZkProof(0, null, null, false), null);
        assertEquals(s.pukTrialsLeft(), 0);
        assertEquals(s.resetPinWithPuk(0, 0), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.smartcardPresent(), true);
        assertEquals(s.storeBlob(0, URI.create(""), null), SmartcardStatusCode.NOT_INITIALIZED);
        assertEquals(s.wasInit(), false);
    }

    @Test
    public void testBackup(){
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
    	SoftwareSmartcard s = setupSmartcard();
    	
        String password = "87654321";
        //no credentials
        SmartcardBackup backup = s.backupAttendanceData(pin, password);
        assertNotNull(backup);
        s = setupSmartcard();
        assertEquals(s.restoreAttendanceData(pin, password, backup), SmartcardStatusCode.OK);

        //now with issuer and credentials
        s = setupSmartcard();
        URI issuerURI = URI.create("issuer1");
        URI credentialUri = URI.create("cred1");
        SmartcardParameters credBases = getCredentialBases();
        s.getNewNonceForSignature();
        assertEquals(s.addIssuerParametersWithAttendanceCheck(key, issuerURI, 1, credBases, RSASignatureSystem.getVerificationKey(key), 2), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credentialUri, issuerURI), SmartcardStatusCode.OK);

        assertEquals(s.listCredentialsUris(pin).size(), 1);
        assertEquals(s.listCourses(pin).size(), 1);

        SmartcardBlob blob = new SmartcardBlob();
        blob.blob = issuerURI.toASCIIString().getBytes();
        assertEquals(s.storeBlob(pin, issuerURI, blob), SmartcardStatusCode.OK);        
        
        Set<URI> credentialIds = new HashSet<URI>();
        credentialIds.add(credentialUri);
        s.prepareZkProof(pin, credentialIds, new HashSet<URI>(), false); //just to activate the course.
        for(int i = 1; i <= 2; i++){
        	s.getNewNonceForSignature();
        	assertEquals(s.incrementCourseCounter(pin, key, issuerURI, i), SmartcardStatusCode.OK);
        }
        assertEquals(2, s.getCounterValue(pin, issuerURI));        
        
        backup = s.backupAttendanceData(pin, password);
        assertNotNull(backup);
        
        s = setupSmartcard();
        s.getNewNonceForSignature();
        assertEquals(s.addIssuerParametersWithAttendanceCheck(key, issuerURI, 1, credBases, RSASignatureSystem.getVerificationKey(key), 2), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credentialUri, issuerURI), SmartcardStatusCode.OK);

        assertEquals(s.listCredentialsUris(pin).size(), 1);
        assertEquals(s.listCourses(pin).size(), 1);

        blob = new SmartcardBlob();
        blob.blob = issuerURI.toASCIIString().getBytes();
        assertEquals(s.storeBlob(pin, issuerURI, blob), SmartcardStatusCode.OK);
        
        assertEquals(0, s.getCounterValue(pin, issuerURI));
        
        assertEquals(s.restoreAttendanceData(pin, password, backup), SmartcardStatusCode.OK);

        assertEquals(2, s.getCounterValue(pin, issuerURI));  
        
        assertEquals(s.listCredentialsUris(pin).size(), 1);
        assertEquals(s.listCourses(pin).size(), 1);
        assertEquals(s.getBlobUris(pin).size(), 2); //2 since the deviceUri is also in the blob store

        File f = new File("softwareSCBackup.bac");
        backup.serialize(f);
    }

    @Test
    public void testRestore(){
        SoftwareSmartcard s = setupSmartcard();
        URI issuerURI = URI.create("issuer1");
        URI credentialUri = URI.create("cred1");
        SmartcardParameters credBases = getCredentialBases();
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
        s.getNewNonceForSignature();
        assertEquals(s.addIssuerParametersWithAttendanceCheck(key, issuerURI, 1, credBases, RSASignatureSystem.getVerificationKey(key), 2), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credentialUri, issuerURI), SmartcardStatusCode.OK);

        assertEquals(s.listCredentialsUris(pin).size(), 1);
        assertEquals(s.listCourses(pin).size(), 1);

        SmartcardBlob blob = new SmartcardBlob();
        blob.blob = issuerURI.toASCIIString().getBytes();
        assertEquals(s.storeBlob(pin, issuerURI, blob), SmartcardStatusCode.OK);        
        
        File f = new File("softwareSCBackup.bac");
        SmartcardBackup backup = SmartcardBackup.deserialize(f);
        String password = "87654321";

        assertEquals(s.restoreAttendanceData(pin, password, backup), SmartcardStatusCode.OK);

        assertEquals(s.listCredentialsUris(pin).size(), 1);
        assertEquals(s.listCourses(pin).size(), 1);
        assertEquals(s.getBlobUris(pin).size(), 2); //2 since deviceUri is also in the blobstore
        assertEquals(deviceURI, s.getDeviceURI(pin));

        assertEquals(2, s.getCounterValue(pin, issuerURI));        
        
        assertTrue(Arrays.equals(s.getBlob(pin, issuerURI).blob, issuerURI.toASCIIString().getBytes()));
        f.delete();
    }

    @Test
    public void testStoreCredential(){
    	SoftwareSmartcard s = setupSmartcard();
    	
    	Random r = new Random(1234);
    	
    	URI longURI1 = URI.create(new BigInteger(2048*3, r).toString());
    	URI longURI2 = URI.create(new BigInteger(2048*2, r).toString());
    	URI longURI3 = URI.create(new BigInteger(2048*2, r).toString());
    	System.out.println("credUID: " + longURI3);
    	
    	byte[] loongByteArr = new byte[2048*2+256];
    	
    	Credential cred = new Credential();
    	CryptoParams cryptoParam = new CryptoParams();
    	
    	CredentialDescription credDesc = new CredentialDescription();
    	credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        credDesc.setIssuerParametersUID(longURI2);
        credDesc.setCredentialSpecificationUID(longURI2);
    	credDesc.setCredentialUID(longURI3);
    	cred.setCredentialDescription(credDesc);
    	cred.setCryptoParams(cryptoParam);
    	URI credentialId =  URI.create("credUri");
    	s.storeCredential(pin, credentialId, cred, serializer);
    	
    	Credential credPrime = s.getCredential(pin, credentialId, serializer);
    	CredentialDescription credDescriptionPrime = credPrime.getCredentialDescription();
    	System.out.println("from read credential - credUID: "+credDescriptionPrime.getCredentialUID().toString());
    	assertEquals(credDesc.getCredentialUID(), credDescriptionPrime.getCredentialUID());
    	
    	byte[] origCredSerialized = serializer.serializeCredential(cred);
    	byte[] newCredSerialized = serializer.serializeCredential(credPrime);
    	System.out.println("orig: " + origCredSerialized.length+", new: " + newCredSerialized.length);
    	assertTrue(Arrays.equals(origCredSerialized, newCredSerialized));
    	
    	
    	//Now test pseudonym storage
    	PseudonymWithMetadata pseudonym = new PseudonymWithMetadata();
    	Pseudonym pseu = new Pseudonym();
    	pseu.setExclusive(true);
    	URI pseudonymUID = URI.create("pseudonymUri");
    	pseu.setPseudonymUID(pseudonymUID);
    	pseu.setPseudonymValue(loongByteArr);
    	pseu.setScope("testScope");
    	pseu.setSecretReference(deviceURI);
    	pseudonym.setPseudonym(pseu);    	
    	pseudonym.setCryptoParams(cryptoParam);
    	    	
    	CardStorage cardStorage = new CardStorage();
    	cardStorage.addSmartcard(s, pin);    	
    	PseudonymSerializer pseudonymSerializer = new PseudonymSerializerGzipXml(cardStorage);
    	System.out.println("After this line");
    	s.storePseudonym(pin, pseudonymUID, pseudonym, pseudonymSerializer);
    	
    	PseudonymWithMetadata pseudonymPrime = s.getPseudonym(pin, pseu.getPseudonymUID(), pseudonymSerializer);

    	byte[] first = pseudonymSerializer.serializePseudonym(pseudonym);
    	byte[] second = pseudonymSerializer.serializePseudonym(pseudonymPrime);
    	assertTrue(Arrays.equals(first, second));
    }
    
    @Test
    public void testPin() {
        SoftwareSmartcard s = new SoftwareSmartcard();
        SystemParameters sp = new SystemParameters();
        sp.deviceSecretSizeBytes = 128 / 8;
        short deviceID = 1;
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
        SoftwareSmartcardTest.puk = s.init(pin, sp, key, deviceID);

        assertEquals(3, s.pinTrialsLeft());
        assertEquals(SmartcardStatusCode.OK, s.changePin(pin, pin));
        assertEquals(3, s.pinTrialsLeft());
        assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.changePin(pin-1, pin));
        assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.changePin(pin-1, pin));
        assertEquals(1, s.pinTrialsLeft());
        assertEquals(SmartcardStatusCode.OK, s.changePin(pin, pin));
        assertEquals(3, s.pinTrialsLeft());
        assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.changePin(pin-1, pin));
        assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.changePin(pin-1, pin));
        assertEquals(SmartcardStatusCode.FORBIDDEN, s.changePin(pin-1, pin));
        assertEquals(SmartcardStatusCode.FORBIDDEN, s.changePin(pin, pin));
        assertEquals(0, s.pinTrialsLeft());
        assertEquals(SmartcardStatusCode.OK, s.resetPinWithPuk(puk, pin));
        assertEquals(3, s.pinTrialsLeft());
        assertEquals(10, s.pukTrialsLeft());
        for(int i=9; i>=1; --i) {
            assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.resetPinWithPuk(puk-1, pin));
            assertEquals(i, s.pukTrialsLeft());
        }
        assertEquals(SmartcardStatusCode.OK, s.resetPinWithPuk(puk, pin));
        assertEquals(10, s.pukTrialsLeft());
        for(int i=9;i>=1;--i) {
            assertEquals(SmartcardStatusCode.UNAUTHORIZED, s.resetPinWithPuk(puk-1, pin));
            assertEquals(i, s.pukTrialsLeft());
        }
        assertEquals(SmartcardStatusCode.FORBIDDEN, s.resetPinWithPuk(puk-1, pin));
        assertEquals(0, s.pukTrialsLeft());
        assertEquals(SmartcardStatusCode.FORBIDDEN, s.resetPinWithPuk(puk, pin));
        assertEquals(0, s.pukTrialsLeft());
    }

    @Test
    public void testEmptyProof() {
        SoftwareSmartcard s = setupSmartcard();
        
        ZkProofCommitment com = s.prepareZkProof(pin, new HashSet<URI>(), new HashSet<URI>(), false);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, new HashSet<URI>(), new HashSet<URI>());
        assertTrue(ZkProofSystem.checkProof(com, res, challenge));
        // There are no witnesses, so the challenge doesn't actually matter
        assertTrue(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
    }

    @Test
    public void testNormalPseudonym() {
        SoftwareSmartcard s = setupSmartcard();

        // Normal pseudonym
        BigInteger publicKey = s.computeDevicePublicKey(pin);
        BigInteger deviceSecret = new BigInteger("81947287742789796125923186813596954448492990520405884824178747062075036638517");
        SystemParameters params = getSystemParameters();
        BigInteger devicePk = params.g.modPow(deviceSecret, params.p);
        // TODO (ms) Check with Kasper if this is ok  
        // assertEquals(devicePk, publicKey);

        ZkProofCommitment com = s.prepareZkProof(pin, new HashSet<URI>(), new HashSet<URI>(), true);
        //    ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
        //    List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
        //    ncol.add(nco);
        //byte[] nonceCommitment = Utils.hashConcat(nonceCom);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, new HashSet<URI>(), new HashSet<URI>());
        assertTrue(ZkProofSystem.checkProof(com, res, challenge));
        assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
    }

    @Test
    public void testScopeExclusivePseudonym() {
        SoftwareSmartcard s = setupSmartcard();

        // Scope exclusive pseudonym
        BigInteger base = new BigInteger("427356031130966532512025830811427122551813236122182141128982542568625228427612");
        BigInteger deviceSecret = new BigInteger("81947287742789796125923186813596954448492990520405884824178747062075036638517");
        // scope-exclusive pseudonym = base^deviceSecret
        URI scope = URI.create("Hello");
        // TODO (ms) Check with Kasper if this is ok
        // assertEquals(s.computeScopeExclusivePseudonym(pin, scope),base.modPow(deviceSecret, getSystemParameters().p));
        Set<URI> scopeList = new HashSet<URI>();
        scopeList.add(scope);

        ZkProofCommitment com = s.prepareZkProof(pin, new HashSet<URI>(), scopeList, false);
        //    List<byte[]> nonceCom = new ArrayList<byte[]>();
        //    nonceCom.add(com.nonceCommitment);
        //    ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
        //    List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
        //    ncol.add(nco);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, new HashSet<URI>(), new HashSet<URI>());
        assertTrue(ZkProofSystem.checkProof(com, res, challenge));
        assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
    }

    @Test
    public void testSimpleCredential() {
        SoftwareSmartcard s = setupSmartcard();
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
        URI issuerUri = URI.create("issuer");
        URI credUri = URI.create("credential");
        SmartcardParameters cb = getCredentialBases();

        s.getNewNonceForSignature();
        assertEquals(s.addIssuerParameters(key, issuerUri, cb), SmartcardStatusCode.OK);

        assertEquals(s.allocateCredential(pin, credUri, issuerUri), SmartcardStatusCode.OK);

        Set<URI> credList = new HashSet<URI>();
        credList.add(credUri);
        // First proof
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, new HashSet<URI>(), false);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, new HashSet<URI>());
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
        }
        // Second proof should work, since no attendance checks are done
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, new HashSet<URI>(), false);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, new HashSet<URI>());
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res,BigInteger.ZERO));
        }
    }

    @Test
    public void testCourseCredential() {
        SoftwareSmartcard s = setupSmartcard();
        RSAKeyPair rootKey = RSASignatureSystemTest.getSigningKeyForTest();
        RSAKeyPair key2 = RSASignatureSystemTest.getAnotherSigningKeyForTest();
        RSAVerificationKey cvk = RSASignatureSystem.getVerificationKey(key2);
        int minAttendance = 2;
        URI issuerUri = URI.create("issuer");
        URI credUri = URI.create("credential");
        SmartcardParameters cb = getCredentialBases();

        {
            s.getNewNonceForSignature();
            assertEquals(s.addIssuerParametersWithAttendanceCheck(rootKey, issuerUri, 1, cb, cvk, minAttendance), SmartcardStatusCode.OK);
        }

        assertEquals(s.allocateCredential(pin, credUri, issuerUri), SmartcardStatusCode.OK);

        // Course counter should be disabled before issuance
        {
            int lectureId = 42;
            s.getNewNonceForSignature();
            assertEquals(s.incrementCourseCounter(pin, key2, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        Set<URI> credList = new HashSet<URI>();
        credList.add(credUri);

        // Initial ZK proof = issuance
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, new HashSet<URI>(), false);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, new HashSet<URI>());
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
        }

        // Once the credential is issued, no more proofs until attendance check
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Increment course counter
        {
            int lectureId = 42;
            s.getNewNonceForSignature();
            assertEquals(s.incrementCourseCounter(pin, key2, issuerUri, lectureId), SmartcardStatusCode.OK);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Replay attack protection
        {
            int lectureId = 42;
            s.getNewNonceForSignature();
            assertEquals(s.incrementCourseCounter(pin, key2, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Replay attack protection
        {
            int lectureId = 39;
            s.getNewNonceForSignature();
            assertEquals(s.incrementCourseCounter(pin, key2, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Increment counter
        {
            int lectureId = 43;
            s.getNewNonceForSignature();
            assertEquals(s.incrementCourseCounter(pin, key2, issuerUri, lectureId), SmartcardStatusCode.OK);
        }

        // Now the proof works
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, new HashSet<URI>(), false);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, new HashSet<URI>());
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
        }
    }

    @Test
    public void testMultipleCredentialsAndPseudonyms() {
        SoftwareSmartcard s = setupSmartcard();
        RSAKeyPair rootKey = RSASignatureSystemTest.getSigningKeyForTest();
        URI issuerUriA = URI.create("issuerA");
        URI issuerUriB = URI.create("issuerB");
        URI credUriA1 = URI.create("credential1");
        URI credUriA2 = URI.create("credential2");
        URI credUriA3 = URI.create("credential3");
        URI credUriB1 = URI.create("credential4");
        URI credUriB2 = URI.create("credential5");
        SmartcardParameters cb = getCredentialBases();

        {
            s.getNewNonceForSignature();
            assertEquals(s.addIssuerParameters(rootKey, issuerUriA, cb), SmartcardStatusCode.OK);
        }
        {
            RSAKeyPair key = RSASignatureSystemTest.getAnotherSigningKeyForTest();
            RSAVerificationKey cvk = RSASignatureSystem.getVerificationKey(key);
            int minAttendance = 10;
            s.getNewNonceForSignature();
            assertEquals(s.addIssuerParametersWithAttendanceCheck(rootKey, issuerUriB, 1, cb, cvk, minAttendance), SmartcardStatusCode.OK);
        }

        assertEquals(s.allocateCredential(pin, credUriA1, issuerUriA), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credUriA2, issuerUriA), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credUriA3, issuerUriA), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credUriB1, issuerUriB), SmartcardStatusCode.OK);
        assertEquals(s.allocateCredential(pin, credUriB2, issuerUriB), SmartcardStatusCode.OK);

        Set<URI> credList = new HashSet<URI>();
        credList.add(credUriA1);
        credList.add(credUriA2);
        credList.add(credUriA3);
        credList.add(credUriB1);
        // without B2
        Set<URI> pseuList = new HashSet<URI>();
        pseuList.add(URI.create("scope1"));
        pseuList.add(URI.create("scope2"));
        pseuList.add(URI.create("scope3"));
        // Initial ZK proof = issuance
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, pseuList, true);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, pseuList);
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
        }

        credList.remove(credUriB1);
        credList.add(credUriB2);
        // B2 is issued here, (B1 not in the proof), other credentials don't have attendance check
        {
            ZkProofCommitment com = s.prepareZkProof(pin, credList, pseuList, true);
            assertNotNull(com);
            //      List<byte[]> nonceCom = new ArrayList<byte[]>();
            //      nonceCom.add(com.nonceCommitment);
            //      ZkNonceCommitmentOpening nco = s.zkNonceOpen(pin, nonceCom);
            //      List<ZkNonceCommitmentOpening> ncol = new ArrayList<ZkNonceCommitmentOpening>();
            //      ncol.add(nco);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, pseuList);
            assertTrue(ZkProofSystem.checkProof(com, res, challenge));
            assertFalse(ZkProofSystem.checkProof(com, res, BigInteger.ZERO));
        }

        // Now the attendance check kicks in
        assertNull(s.prepareZkProof(pin, credList, pseuList, true));
    }

}

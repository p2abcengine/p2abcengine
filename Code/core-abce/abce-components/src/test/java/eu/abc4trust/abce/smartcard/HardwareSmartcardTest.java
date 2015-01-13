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

package eu.abc4trust.abce.smartcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.testharness.ImagePathBuilder;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.CredentialSerializerObjectGzip;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignature;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSASignatureSystemTest;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBackup;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardCrypto;
import eu.abc4trust.smartcard.SmartcardParameters;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SystemParameters;
import eu.abc4trust.smartcard.ZkProofCommitment;
import eu.abc4trust.smartcard.ZkProofResponse;
import eu.abc4trust.smartcard.ZkProofSystem;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CryptoParams;

public class HardwareSmartcardTest {

    private static final int pin = 1234;
    @SuppressWarnings("unused")
    private static final int puk = 17751674;
    private static final byte[] accesscode = new byte[]{(byte) 0xDD, (byte) 0xE8, (byte) 0x90, (byte) 0x96, 0x3E, (byte) 0xF8, 0x09, 0x0E};
    private static BigInteger challenge;
    private static Random rand = new Random(42);
    //private static final PseudonymSerializer pseudonymSerializer = new PseudonymSerializerGzipXml();

    //private static final int keyIDForFirstSignKey = 1;
    private static final int keyIDForSecondSignKey = 1;

    private static final URI deviceURI = URI.create("ImbaCardNo42");
    private static final URI IssuerURINo1 = URI.create("urn:patras:issuer:credUniv:idemix");
    private static final URI IssuerURINo2 = URI.create("urn:patras:issuer:credCourse:uprove");

    private static RSAKeyPair rootKey = new RSAKeyPair(
            new BigInteger("13188739541922660896646064269210831728935525607028122743662847644630716074667792017963587041964414609957176615536380624327700918306145189040520169692459457"),
            new BigInteger("12965542865437711645486684206961737786619579267694394955648937336793338179891217408971833767983283274280247590175826238504374133935543138269003896636545659"));
    /*
    private static RSAKeyPair rootKey = new RSAKeyPair(
    									new BigInteger("98123248929977781234033599438430872512413464343146397351387389379354368678144441573871246352773205104826862682926853362525445766087875638152522866999171082979077110521492402490396873693935392516032428981301612806156847276776118279635414146466050412914757988580508268698346492883186329475125347579894931495911"),
    									new BigInteger("153675233447601346431048868855343067422415662106390990153196543740996536617679216226026465835429401901713210990800796200382364371445046479284855834231353845317728582606162246819027954438959188793527020710865829941771890543954962333617286117444708446256701507879424546361300317931079057381850093323907216844089"));
     */
    public static HardwareSmartcard getSmartcard() {
        HardwareSmartcard smartcard = null;
        try {
            List<CardTerminal> terminals = TerminalFactory.getDefault()
                    .terminals().list();

            for (CardTerminal terminal : terminals) {
                if (!terminal.isCardPresent()) {
                    continue;
                }

                Card card = terminal.connect("*");
                smartcard = new HardwareSmartcard(terminal, card, rand);
                //smartcard.initializeNoOfIssuersAndCreds(pin);
            }
        } catch (CardException ex) {
            throw new RuntimeException(ex);
        }
        HardwareSmartcard.printInput = true;        
        
        challenge = new BigInteger(32, rand); 
        return smartcard;
    }

    public static SmartcardParameters getCredentialBases() {
        /*
        BigInteger p = new BigInteger(
                "12626253643225658788054945243310161001518279478947653549052908831079155905872956458802477139613130936776937607840700839905021318088742281050346423374661061");
        BigInteger q = new BigInteger(
                "10703344939754407345467437772082699346739860491968578743102602332992358121160532991562371831707432661485771211677230593441997251930402068779708696552944203");
         */
        // n =
        // 59611897368131366507364942276670668747995323268474975289271614234218937009935107719366762946802866847720072176517579036188074246487493444834020695796207762776438347095237876273178780516676456685032464805596922499374627605877256498945463430940126396361032161179291118883563104866211893115282075236693902324109
        BigInteger n = new BigInteger(new byte[]{-81, -56, 2, 34, 56, 127, -112, 69, -5, 88, 116, -58, -58, -84, 70, -59, -110, -32, 2, -107, -114, -75, 26, 96, 53, -44, -19, -5, -66, -68, 3, 18, 36, -123, -26, 55, -126, 15, -69, 39, -4, -1, -117, 22, 109, 36, 73, 123, 37, -112, 14, 104, 24, 38, 14, 102, -88, -15, 118, -61, 111, -90, -118, -20, 33, 81, -20, -88, -75, -80, -11, -32, 55, 22, 78, -32, -76, 110, -32, -112, 37, 115, 4, 20, 70, -120, 27, 72, -74, -37, -49, -16, -21, 91, -73, -27, 6, -80, 84, -101, 62, 60, 86, -17, 59, 45, -73, -3, 63, -15, 82, -67, 12, 81, -108, -65, -68, 50, 52, 98, 13, 122, 108, 71, 45, 40, 48, 91, 72, 67, -86, -90, -114, 108, 92, -39, 94, -55, 103, 53, 92, 102, -112, -17, -107, -76, 57, 113, -76, -29, 2, -42, -114, -52, 80, -30, -84, -80, 21, 51, -97, -57, 0, 103, 14, -47, -78, 38, -9, -108, 58, -81, 113, 101, -115, 50, 30, 116, 26, 46, -66, -71, 35, -38, -5, -11, -26, 101, 82, -79, 101, 7});
        BigInteger R0 = new BigInteger(new byte[]{72, -108, 10, 99, 97, 120, 77, 42, -113, -17, 25, 77, 126, 63, 96, 52, 12, 99, -22, 111, 39, -80, -23, 88, -54, -49, -35, -79, 68, 29, 54, -102, 98, -59, 61, 121, 122, 73, 82, 8, 22, 107, 57, 33, 60, -127, 88, -40, -86, 57, -81, -26, 3, -16, -95, 83, 21, 58, 46, -105, 125, 71, -61, -109, 97, -83, -23, -32, 86, -18, -2, 124, -119, -2, -16, -94, 75, 71, 88, -34, 49, -77, -7, 83, -115, 109, 34, -66, -77, -3, -27, -8, 78, -116, -107, -26, -91, 93, 56, 38, -85, -118, -108, -82, 106, 7, 68, 18, -56, 74, 118, 90, 15, 87, -11, -43, 33, 92, 92, 54, -74, 21, -78, -67, 41, -44, 118, -81, 114, 92, -22, 3, 42, -38, -33, -6, 6, 19, -80, 52, -9, -126, 61, 76, -107, -58, 52, -98, -57, -18, 40, 99, 125, 47, 113, -1, 56, -20, 81, 1, -65, -68, -66, -115, 55, -120, -74, -12, 69, 82, -46, -58, -66, -34, -101, 121, -108, 68, 41, 59, 100, -24, -103, -112, -41, -54, -105, 65, -16, 90, 26, 13});
        BigInteger S = new BigInteger(new byte[]{-123, 124, 48, -77, 123, 68, -29, -30, -12, -34, -70, -12, -123, -19, -7, 27, -101, -111, 30, -98, -67, 71, 16, -39, 38, 124, -100, 65, -106, -97, -68, -13, -37, -109, -54, 105, -15, 26, 48, 52, 37, 105, -120, -120, -126, 52, -26, -60, -121, -54, -75, 27, -61, -23, 99, 21, 78, -128, -88, -112, -46, 49, -27, 9, 65, -19, -96, -49, 40, -27, -77, 23, -36, -100, 45, 61, 112, -113, -108, 32, 86, 72, 13, 16, -25, -36, 46, 50, -97, -54, 12, 72, 51, -14, -36, 11, 76, -6, -73, 37, -57, 59, 52, -43, 95, 77, 21, -14, -97, 68, -116, 66, -90, -119, 52, -44, -85, -25, -39, 86, 85, 106, -16, 88, 14, 50, -103, -96, -76, -68, -98, 34, -87, -18, 14, -122, 1, 84, -106, 119, -101, -43, -67, -128, 75, 41, -27, -91, 91, 19, -119, 36, -1, -126, -10, -26, -91, 84, 127, -37, -22, -4, -90, 95, 7, 19, -126, -14, 9, -53, 69, -127, 22, -8, 13, -18, -48, 28, 40, 70, 76, 11, -106, 21, 16, -96, 121, 58, -103, -94, 108, -61});
        return SmartcardParameters.forTwoBaseCl(n, R0, S);
    }

    public static SystemParameters getSystemParameters(){
    	eu.abc4trust.xml.SystemParameters sp = SystemParametersUtil.getDefaultSystemParameters_2048();
    	EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sp);        
        BigInteger p, g, subgroupOrder;
                try {
                  p = spw.getDHModulus().getValue();
                  g = spw.getDHGenerator1().getValue();
                  subgroupOrder = spw.getDHSubgroupOrder().getValue();
                } catch (ConfigurationException e1) {
                  throw new RuntimeException(e1);
                }
        SystemParameters system = new SystemParameters();
        system.p = p;
        system.g = g;
        system.subgroupOrder = subgroupOrder;
        return system;
    }

    @Ignore
    @Test
    public void computeRSAKey(){
        RSAKeyPair key = RSASignatureSystem.generateSigningKey(1024/8);
        System.out.println("p: "+ key.getP());
        System.out.println("q: "+ key.getQ());
    }

    @Test
    public void testBackupAESKey() throws Exception{
    	String original ="I'm a string..."; 
    	byte[] toEnc = original.getBytes("UTF-8");
    	byte[] IV = new byte[16];
    	rand.nextBytes(IV);
    	String salt = "very salty!";
    	String password = "password";
    	Cipher encCipher = HardwareSmartcard.getAESKey(salt, password, IV, true);
    	byte[] enc = encCipher.doFinal(toEnc);
    	
    	Cipher decCipher = HardwareSmartcard.getAESKey(salt, password, IV, false);
    	byte[] dec = decCipher.doFinal(enc);
    	String s = new String(dec, "UTF-8");
    	assertEquals(original, s);
    }
    
    @Ignore
    @Test
    public void InitializeCard() throws UnsupportedEncodingException{
        HardwareSmartcard s = getSmartcard();

        int mode = s.getMode();
        System.out.println("mode: " + mode);
        if(mode == 0){
            System.out.println("Set card into root-mode");
            assertEquals(s.setRootMode(accesscode), SmartcardStatusCode.OK);
        }

        SystemParameters system = getSystemParameters();
        short deviceID = 1234;
        s.init(pin, system, rootKey, deviceID);

        assertEquals(s.addIssuerParameters(rootKey, IssuerURINo1, getCredentialBases()), SmartcardStatusCode.OK);

        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();
        RSAVerificationKey cvk = RSASignatureSystem.getVerificationKey(key);
        int minAttendance = 2;
        assertEquals(s.addIssuerParametersWithAttendanceCheck(rootKey, IssuerURINo2, keyIDForSecondSignKey, getCredentialBases(), cvk, minAttendance), SmartcardStatusCode.OK);

        s.setWorkingMode();

        //now that we have a pin on the card, and are in working more, we store the deviceURI and issuerUris
        SmartcardBlob blob = new SmartcardBlob();
        blob.blob = deviceURI.toASCIIString().getBytes("US-ASCII");
        s.storeBlob(pin, Smartcard.device_name, blob);

        //s.storeIssuerUriAndID(pin, IssuerURINo1, staticMap.getIssuerIDFromUri(IssuerURINo1));
        //s.storeIssuerUriAndID(pin, IssuerURINo2, staticMap.getIssuerIDFromUri(IssuerURINo2));

    }

    @Ignore
    @Test
    public void testBlobStore(){
        HardwareSmartcard s = getSmartcard();
        URI uri = URI.create("This_is_a_test_URI");
        SmartcardBlob blob = new SmartcardBlob();
        String toBlob = "Jeg er en blob!";
        try {
            blob.blob = toBlob.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        s.storeBlob(HardwareSmartcardTest.pin, uri, blob);
        SmartcardBlob blob2 = s.getBlob(pin, uri);
        assertTrue(Arrays.equals(blob2.blob, blob.blob));

        Map<URI, SmartcardBlob> blobs = s.getBlobs(pin);
        for(URI u : blobs.keySet()){
            SmartcardBlob smartcardBlob = blobs.get(u);
            try {
                String blobString = new String(smartcardBlob.blob, "US-ASCII");
                System.out.println("<"+u+", "+blobString+">");
                assertTrue(blobString.equals(toBlob));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Ignore
    @Test
    public void testCryptoAuth(){
        byte[] challenge = new byte[]{-69, 107, -66, 1, 28, -57, 84, -68, -26, 25, -51, 76, -62, 107, 117, -114, -60, 15, -112, 39, -97, 2, 54, 111, 15, -64, 67, -41, -43, 44, -44, 107};
        byte[] data = new byte[257];
        RSASignature sig = SmartcardCrypto.generateSignature(data, challenge, rootKey, rand);
        RSAVerificationKey verKey = new RSAVerificationKey();
        verKey.n = rootKey.getN();
        byte[] extracted = SmartcardCrypto.extraction(verKey, sig, challenge);
        assertTrue(Arrays.equals(data, extracted));
    }

    @Ignore
    @Test
    public void testStoreCredential(){
        HardwareSmartcard s = getSmartcard();

        s.getBlobs(pin);

        URI credentialId =  URI.create("credUri");

        Random r = new Random(1234);
    	
    	URI longURI1 = URI.create(new BigInteger(2048*3, r).toString());
    	URI longURI2 = URI.create(new BigInteger(2048*2, r).toString());
    	URI longURI3 = URI.create(new BigInteger(2048*2, r).toString());

        CredentialSerializer serializer = new CredentialSerializerObjectGzip();

        Credential cred = new Credential();
        CryptoParams cryptoParam = new CryptoParams();

        CredentialDescription credDesc = new CredentialDescription();
        credDesc.setImageReference(ImagePathBuilder.TEST_IMAGE_JPG);
        credDesc.setIssuerParametersUID(longURI1);
        credDesc.setCredentialSpecificationUID(longURI2);
        credDesc.setCredentialUID(longURI3);
        cred.setCredentialDescription(credDesc);
        cred.setCryptoParams(cryptoParam);
        assertEquals(s.storeCredential(pin, credentialId, cred, serializer), SmartcardStatusCode.OK);

        Credential credPrime = s.getCredential(pin, credentialId, serializer);
        assertNotNull(credPrime);
        CredentialDescription credDescriptionPrime = credPrime.getCredentialDescription();
        System.out.println("from read credential - credUID: "+credDescriptionPrime.getCredentialUID().toString());
        assertEquals(credDesc.getCredentialUID(), credDescriptionPrime.getCredentialUID());


        byte[] origCredSerialized = serializer.serializeCredential(cred);
        byte[] newCredSerialized = serializer.serializeCredential(credPrime);
        System.out.println("orig: " + origCredSerialized.length+", new: " + newCredSerialized.length);
        assertTrue(Arrays.equals(origCredSerialized, newCredSerialized));

    }


    @Ignore
    @Test
    public void testBackup() throws Exception{

        HardwareSmartcard s = getSmartcard();

        //s.addIssuerParametersWithAttendanceCheck(pin, rootKey, IssuerURINo1, keyIDForFirstSignKey, getCredentialBases(), RSASignatureSystem.getVerificationKey(RSASignatureSystemTest.getSigningKeyForTest()), 2);
        SmartcardBlob blob = new SmartcardBlob();
        blob.blob = deviceURI.toASCIIString().getBytes("US-ASCII");
        s.storeBlob(pin, Smartcard.device_name, blob);
        String password = "12345678";
        SmartcardBackup backup = s.backupAttendanceData(pin, password);
        assertNotNull(backup);
        System.out.println("backup device: " + Arrays.toString(backup.macDevice));
        //System.out.println("backup counters: " + Arrays.toString(backup.macCounters));
        for(Byte credID : backup.macCredentials.keySet()){
            System.out.println("backup credential with ID: "+ credID+": "+ Arrays.toString(backup.macCredentials.get(credID)));
        }
        File f = new File("testBackup.bac");
        backup.serialize(f);
    }

    @Ignore
    @Test
    public void testRestore(){
        HardwareSmartcard s = getSmartcard();
        if(s.getMode() == 0){
            System.out.println("Set card into root-mode");
            s.setRootMode(accesscode);
        }

        String password = "12345678";
        File f = new File("testBackup.bac");
        SmartcardBackup backup = SmartcardBackup.deserialize(f);
        assertEquals(s.restoreAttendanceData(pin, password, backup), SmartcardStatusCode.OK);

        assertEquals(new String(s.getBlob(pin, Smartcard.device_name).blob), deviceURI.toASCIIString());
    }

    @Ignore
    @Test
    public void testEmptyProof() {
        HardwareSmartcard s = getSmartcard();

        Set<URI> credentialIDs = new HashSet<URI>();
        Set<URI> scopeExclusivePseudonyms = new HashSet<URI>();
        ZkProofCommitment comm = s.prepareZkProof(pin, credentialIDs, scopeExclusivePseudonyms, false);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, credentialIDs, scopeExclusivePseudonyms);
        assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
        // There are no witnesses, so the challenge doesn't actually matter
        assertTrue(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
    }

    @Ignore
    @Test
    public void testNormalPseudonym() {
        HardwareSmartcard s = getSmartcard();

        Set<URI> credentialIDs = new HashSet<URI>();
        Set<URI> scopeExclusivePseudonyms = new HashSet<URI>();
        ZkProofCommitment comm = s.prepareZkProof(pin, credentialIDs, scopeExclusivePseudonyms, true);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, credentialIDs, scopeExclusivePseudonyms);
        assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
        assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
    }

    @Ignore
    @Test
    public void fixPin(){
    	HardwareSmartcard s = getSmartcard();
    	int oldPin = 5678;
    	int puk = 70952389;
    	System.out.println(s.resetPinWithPuk(puk, pin));
    	//System.out.println(s.changePin(oldPin, pin));
    }
    
    @Ignore
    @Test
    public void testScopeExclusivePseudonym() {
        HardwareSmartcard s = getSmartcard();

        // Scope exclusive pseudonym
        URI scope = URI.create("urn:patras:registration");
        Set<URI> scopeList = new HashSet<URI>();
        scopeList.add(scope);

        Set<URI> credentialIDs = new HashSet<URI>();
        ZkProofCommitment comm = s.prepareZkProof(pin, credentialIDs, scopeList, false);
        ZkProofResponse res = s.finalizeZkProof(pin, challenge, credentialIDs, scopeList);
        assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
        assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
    }

    @Ignore
    @Test
    public void testSimpleCredential() {
        HardwareSmartcard s = getSmartcard();
        URI issuerUri = IssuerURINo1;
        URI credUri = URI.create("credential");
        assertEquals(s.allocateCredential(pin, credUri, issuerUri), SmartcardStatusCode.OK);
        Set<URI> credList = new HashSet<URI>();
        credList.add(credUri);
        // First proof
        {
            Set<URI> scopeList = new HashSet<URI>();
            ZkProofCommitment comm = s.prepareZkProof(pin, credList, scopeList, false);
            assertNotNull(comm);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, scopeList);
            assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
            assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
        }
        // Second proof should work, since no attendance checks are done
        //        {
        //            Set<URI> scopeList = new HashSet<URI>();
        //            ZkProofCommitment comm = s.prepareZkProof(pin, credList, scopeList, false);
        //            assertNotNull(comm);
        //            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, scopeList, comm.nonceCommitment);
        //            assertTrue(ZkProofSystem.checkProof(comm, res, challenge, comm.nonceCommitment));
        //            assertFalse(ZkProofSystem.checkProof(comm, res, new byte[1]));
        //        }
        s.deleteCredential(pin, credUri);
    }

    @Ignore
    @Test
    public void testCourseCredential() {
        HardwareSmartcard s = getSmartcard();
        URI issuerUri = IssuerURINo2;
        URI credUri = URI.create("courseCred");
        RSAKeyPair key = RSASignatureSystemTest.getSigningKeyForTest();

        assertEquals(s.allocateCredential(pin, credUri, issuerUri), SmartcardStatusCode.OK);

        // Course counter should be disabled before issuance
        {
            int lectureId = 42;
            assertEquals(s.incrementCourseCounter(pin, key, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        Set<URI> credList = new HashSet<URI>();
        credList.add(credUri);

        // Initial ZK proof = issuance
        {
            Set<URI> scopeList = new HashSet<URI>();
            ZkProofCommitment comm = s.prepareZkProof(pin, credList, scopeList, false);
            assertNotNull(comm);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, scopeList);
            assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
            //assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
        }

        // Once the credential is issued, no more proofs until attendance check
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Increment course counter
        {
            int lectureId = 42;
            assertEquals(s.incrementCourseCounter(pin, key, issuerUri, lectureId), SmartcardStatusCode.OK);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Replay attack protection
        {
            int lectureId = 42;
            assertEquals(s.incrementCourseCounter(pin, key, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Replay attack protection
        {
            int lectureId = 39;
            assertEquals(s.incrementCourseCounter(pin, key, issuerUri, lectureId), SmartcardStatusCode.NOT_MODIFIED);
        }

        // Not enough attendance
        assertNull(s.prepareZkProof(pin, credList, new HashSet<URI>(), false));

        // Increment counter
        {
            int lectureId = 43;
            assertEquals(s.incrementCourseCounter(pin, key, issuerUri, lectureId), SmartcardStatusCode.OK);
        }
        
        {
        	for(int lectureId = 44; lectureId < 50; lectureId++){
        		assertEquals(SmartcardStatusCode.OK, s.incrementCourseCounter(pin, key, issuerUri, lectureId));
        	}
        }

        // Now the proof works
        {
            Set<URI> scopeList = new HashSet<URI>();
            ZkProofCommitment comm = s.prepareZkProof(pin, credList, scopeList, false);
            assertNotNull(comm);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, scopeList);
            assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
            assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
        }

        s.deleteCredential(pin, credUri);
    }

    @Ignore
    @Test
    public void testMultipleCredentialsAndPseudonyms() {
        HardwareSmartcard s = getSmartcard();
        URI issuerUriA = IssuerURINo1;
        URI issuerUriB = IssuerURINo2;
        URI credUriA1 = URI.create("credential1");
        URI credUriA2 = URI.create("credential2");
        URI credUriA3 = URI.create("credential3");
        URI credUriB1 = URI.create("credential4");
        URI credUriB2 = URI.create("credential5");

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
            ZkProofCommitment comm = s.prepareZkProof(pin, credList, pseuList, true);
            assertNotNull(comm);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, pseuList);
            assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
            assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
        }

        credList.remove(credUriA1);
        credList.remove(credUriA2);
        credList.remove(credUriA3);
        credList.remove(credUriB1);
        credList.add(credUriB2);
        // B2 is issued here, (B1 not in the proof), other credentials don't have attendance check
        {
            ZkProofCommitment comm = s.prepareZkProof(pin, credList, pseuList, true);
            assertNotNull(comm);
            ZkProofResponse res = s.finalizeZkProof(pin, challenge, credList, pseuList);
            assertTrue(ZkProofSystem.checkProof(comm, res, challenge));
            assertFalse(ZkProofSystem.checkProof(comm, res, BigInteger.ZERO));
        }

        // Now the attendance check kicks in
        assertNull(s.prepareZkProof(pin, credList, pseuList, true));

        s.deleteCredential(pin, credUriA1);
        s.deleteCredential(pin, credUriA2);
        s.deleteCredential(pin, credUriA3);
        s.deleteCredential(pin, credUriB1);
        s.deleteCredential(pin, credUriB2);
    }

}

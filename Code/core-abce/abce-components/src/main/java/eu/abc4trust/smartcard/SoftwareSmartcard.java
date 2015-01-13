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

package eu.abc4trust.smartcard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PseudonymWithMetadata;

public class SoftwareSmartcard implements Smartcard, Serializable {
    static Logger log = Logger.getLogger(SoftwareSmartcard.class.getName());
    

    private static final long serialVersionUID = 1L;

    private static final int MAX_CREDENTIALS = 8;
    private static final int MAX_ISSUERS = 6;
    private static final int MAX_BLOBS = 50;
    private static final int MAX_URI_LEN_BYTES = 64;
    private static final int MAX_BLOB_LEN_BYTES = 512;
    private static final int MAX_PIN_TRIALS = 3;
    private static final int MAX_PUK_TRIALS = 10;
    private static final String URI_ENCODING = "UTF-8";
    @SuppressWarnings("unused")
    private static final String MAC_ALGORITHM = "HmacSHA256";

    // Token to distinguish different types of signatures
    @SuppressWarnings("unused")
    private static final int BACKUP_COURSE_TOKEN = 3;

    // State of smartcard
    static transient private Random rand = new SecureRandom();
    private boolean factoryInit;
    private int pin;
    private int puk;
    private int pinTrialsLeft;
    private int pukTrialsLeft;
    private final Map<URI, SmartcardBlob> blobstore;
    private SystemParameters params;
    private BigInteger deviceSecret;
    private byte[] currentNonce;
    private RSAVerificationKey schoolKey;
    private final Map<URI, TrustedIssuerParameters> issuerParameters;
    private final Map<URI, Integer> issuerIDs;
    private final Map<Integer, URI> issuerUris;
    private final Map<URI, CredentialOnSmartcard> credentials;
    private final Map<Integer, RSAVerificationKey> courseKeys;
    private short deviceID;
    private final byte[] macKey = new byte[16]; //hardcoded 16 zeros
    private transient ZkProofState zkProofState;

    public SoftwareSmartcard() {
        this(new SecureRandom());
    }

    public SoftwareSmartcard(Random random) {
        this.factoryInit = false;
        rand = random;
        this.blobstore = new HashMap<URI, SmartcardBlob>();
        this.issuerParameters = new HashMap<URI, TrustedIssuerParameters>();
        this.issuerIDs = new HashMap<URI, Integer>();
        this.issuerUris = new HashMap<Integer, URI>();
        this.credentials = new HashMap<URI, CredentialOnSmartcard>();
        this.courseKeys = new HashMap<Integer, RSAVerificationKey>();
    }

    /**
     * Return the ID from the mapping. If no mapping exists, we create a new entry
     * and return the ID from that mapping.
     * @param parametersUri
     * @return
     */
    private int getIssuerIDFromUri(URI parametersUri) {
        if(!this.issuerIDs.containsKey(parametersUri)){
            int id = 1;
            while(true){
                if(id >= 10){
                    throw new RuntimeException("More than 10 issuers not allowed!");
                }
                if(this.issuerIDs.values().contains(id)){
                    id++;
                }else{
                    this.issuerIDs.put(parametersUri, id);
                    this.issuerUris.put(id, parametersUri);
                    return id;
                }
            }
        }else{
            return this.issuerIDs.get(parametersUri);
        }
    }

    private URI getIssuerUriFromID(int ID){
        return this.issuerUris.get(ID);
    }

    private SmartcardStatusCode validatePassword(String password){
        char[] chars = password.toCharArray();
        if(chars.length != 8){
            System.err.println("Password does not convert to 8 bytes. Please enter a new password");
            return SmartcardStatusCode.BAD_REQUEST;
        }
        return SmartcardStatusCode.OK;
    }

    private SmartcardStatusCode authenticateWithPin(int pin) {
        if (!this.factoryInit) {
            return SmartcardStatusCode.NOT_INITIALIZED;
        }
        if (this.pinTrialsLeft <= 0) {
            return SmartcardStatusCode.FORBIDDEN;
        }
        if (this.pin != pin) {
            this.pinTrialsLeft--;
            if (this.pinTrialsLeft <= 0) {
                return SmartcardStatusCode.FORBIDDEN;
            } else {
                return SmartcardStatusCode.UNAUTHORIZED;
            }
        }
        this.pinTrialsLeft = MAX_PIN_TRIALS;
        return SmartcardStatusCode.OK;
    }

    private SmartcardStatusCode authenticateWithPuk(int puk) {
        if (!this.factoryInit) {
            return SmartcardStatusCode.NOT_INITIALIZED;
        }
        if (this.pukTrialsLeft <= 0) {
            return SmartcardStatusCode.FORBIDDEN;
        }
        if (this.puk != puk) {
            this.pukTrialsLeft--;
            if (this.pukTrialsLeft <= 0) {
                return SmartcardStatusCode.FORBIDDEN;
            } else {
                return SmartcardStatusCode.UNAUTHORIZED;
            }
        }
        this.pukTrialsLeft = MAX_PUK_TRIALS;
        return SmartcardStatusCode.OK;
    }

    private SmartcardStatusCode authenticateWithSignature(RSAKeyPair secretKey,
            RSAVerificationKey publicKey, ByteArrayOutputStream toSign) {
        if (!this.factoryInit) {
            return SmartcardStatusCode.NOT_INITIALIZED;
        }

        if (this.currentNonce == null) {
            return SmartcardStatusCode.STALE_NONCE;
        }
        if ((secretKey == null) || (publicKey == null)) {
            throw new RuntimeException("key was null");
        }

        RSASignature sig = SmartcardCrypto.generateSignature(toSign.toByteArray(), this.currentNonce, secretKey, rand);

        if (RSASignatureSystem.checkSignature(sig, publicKey, toSign.toByteArray(), this.currentNonce)) {
            this.currentNonce = null;
            return SmartcardStatusCode.OK;
        } else {
            this.currentNonce = null;
            return SmartcardStatusCode.UNAUTHORIZED;
        }
    }

    @Override
    public SmartcardStatusCode changePin(int pin, int newPin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return pinstatus;
        }
        this.pin = newPin;
        return SmartcardStatusCode.OK;
    }

    @Override
    public SmartcardStatusCode deleteBlob(int pin, URI uri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return pinstatus;
        }

        if (!this.blobstore.containsKey(uri)) {
            return SmartcardStatusCode.NOT_FOUND;
        }
        this.blobstore.remove(uri);
        return SmartcardStatusCode.OK;
    }

    @Override
    public SmartcardStatusCode deleteCredential(int pin, URI credentialUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return pinstatus;
        }
        log.info("======= Delete credential "+ credentialUri);

        if (! this.credentials.containsKey(credentialUri)) {
            return SmartcardStatusCode.NOT_FOUND;
        }
        this.removeCredentialUri(pin, credentialUri);
        this.credentials.remove(credentialUri);
        return SmartcardStatusCode.OK;
    }

    @Override
    public void removeCredentialUri(int pin, URI uri){
        if(uri.toString().startsWith(HardwareSmartcard.CREDENTIAL_PREFIX)){
            URI reloadURI = URI.create(uri.toString()+HardwareSmartcard.UPROVE_RELOAD_URI_POSTFIX);
            if(reloadURI.toString().contains(":") && !reloadURI.toString().contains("_")){
                reloadURI = URI.create(reloadURI.toString().replaceAll(":", "_")); //change all ':' to '_'
            }
            this.deleteBlob(pin, reloadURI);
            log.info("deleted the reload blob of the credential: " + reloadURI);
        }
        int i = 1;
        while(true){
            URI tmpUri = URI.create(uri.toString()+"_"+i++);
            if(this.deleteBlob(pin, tmpUri) != SmartcardStatusCode.OK){
                if(i == 1){
                    //Actual error - we should be able to remove at least 1 blob
                    throw new RuntimeException("Could not delete blob: " + tmpUri);
                }
                return;
            }
        }
    }

    @Override
    public SmartcardBlob getBlob(int pin, URI uri) {
        uri = uri == null ? uri : URI.create(uri.toString().replaceAll(":", "_"));
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        // Returns null if the uri is not in the store
        return this.blobstore.get(uri);
    }

    @Override
    public Map<URI, SmartcardBlob> getBlobs(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return this.blobstore;
    }

    @Override
    public Set<URI> getBlobUris(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return this.blobstore.keySet();
    }

    @Override
    public Course getCourse(int pin, URI issuerUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }

        TrustedIssuerParameters tip = this.getIssuerParameters(pin, issuerUri);
        if(tip==null) {
            return null;
        }
        return tip.course;
    }

    @Override
    public byte[] getNewNonceForSignature() {
        if (!this.factoryInit) {
            return null;
        }

        this.currentNonce = new byte[this.params.signatureNonceLengthBytes];
        rand.nextBytes(this.currentNonce);
        return this.currentNonce;
    }

    @Override
    public SmartcardStatusCode incrementCourseCounter(int pin, RSAKeyPair key,
            URI issuerUri, int lectureId) {
        if (!this.factoryInit) {
            return SmartcardStatusCode.NOT_INITIALIZED;
        }
        if (!this.uriLengthOk(issuerUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }

        RSAVerificationKey vkey = this.schoolKey;
        Course course = this.getCourse(this.pin, issuerUri);
        if (course != null) {
            vkey = this.courseKeys.get(course.getKeyID());
        }
        // Note: we don't return immediately if course == NULL, else the card would leak
        // which courses the owner attends.

        this.getNewNonceForSignature();
        ByteArrayOutputStream toSign = new ByteArrayOutputStream();
        Utils.addToStream(toSign, Utils.INC_COURSE_TOKEN);
        Utils.addToStream(toSign, issuerUri);
        Utils.addToStream(toSign, lectureId);
        SmartcardStatusCode sigstatus = this.authenticateWithSignature(key, vkey, toSign);
        if (sigstatus != SmartcardStatusCode.OK) {
            return sigstatus;
        }
        if (course == null) {
            return SmartcardStatusCode.NOT_FOUND;
        }
        // Counter will not be incremented if course credential was not issued yet
        if (course.updateLectureId(lectureId)) {
            return SmartcardStatusCode.OK;
        } else {

            return SmartcardStatusCode.NOT_MODIFIED;
        }
    }

    @Override
    public Set<Course> listCourses(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        Set<Course> ret = new HashSet<Course>();
        for(TrustedIssuerParameters tip: this.issuerParameters.values()) {
            if(tip.enforceAttendanceCheck) {
                ret.add(tip.course);
            }
        }
        return ret;
    }

    @Override
    public int pinTrialsLeft() {
        if (!this.factoryInit) {
            return 0;
        }
        return this.pinTrialsLeft;
    }

    @Override
    public int pukTrialsLeft() {
        if (!this.factoryInit) {
            return 0;
        }
        return this.pukTrialsLeft;
    }

    @Override
    public SmartcardStatusCode resetPinWithPuk(int puk, int newPin) {
        SmartcardStatusCode pukstatus = this.authenticateWithPuk(puk);
        if (pukstatus != SmartcardStatusCode.OK) {
            return pukstatus;
        }
        this.pin = newPin;
        this.pinTrialsLeft = MAX_PIN_TRIALS;
        return SmartcardStatusCode.OK;
    }

    @Override
    public boolean smartcardPresent() {
        return true;
    }

    @Override
    public SmartcardStatusCode storeBlob(int pin, URI uri, SmartcardBlob blob) {
        String[] forbiddenChars = new String[]{"\u0167", ":", "*", "?", "<", ">", " ", "|"};
        if(uri.toString().contains(":") && !uri.toString().contains("_")){
            uri = URI.create(uri.toString().replaceAll(":", "_")); //change all ':' to '_'
        }else{
            for (String forbiddenChar : forbiddenChars) {
                if(uri.toString().contains(forbiddenChar)){
                    throw new RuntimeException("Cannot store a blob under a URI containing the following char: " +forbiddenChar);
                }
            }
        }

        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return pinstatus;
        }

        if (! this.uriLengthOk(uri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }

        if (blob.getLength() > MAX_BLOB_LEN_BYTES) {
            return SmartcardStatusCode.REQUEST_ENTITY_TOO_LARGE;
        }

        int aftersize = this.blobstore.size();
        if (!this.blobstore.containsKey(uri)) {
            aftersize++;
        }
        if (aftersize > MAX_BLOBS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }

        this.blobstore.put(uri, blob);

        if (aftersize != this.blobstore.size()) {
            throw new RuntimeException("Assertion failed: aftersize != blobstore.size()");
        }

        return SmartcardStatusCode.OK;
    }

    @Override
    public boolean wasInit() {
        return this.factoryInit;
    }

    @Override
    public int init(int newPin, SystemParameters sysParams,
            RSAKeyPair rootKey, short deviceID) {
        if (this.factoryInit) {
            return -1;
        }
        this.pin = newPin;
        this.pinTrialsLeft = MAX_PIN_TRIALS;
        //Do NOT use the random generator. Causes tests to crash!
        //this.puk = rand.nextInt(100000000);
        this.puk = 12345678;
        this.pukTrialsLeft = MAX_PUK_TRIALS;
        this.blobstore.clear();
        this.params = sysParams;
        this.deviceSecret = new BigInteger(sysParams.deviceSecretSizeBytes * 8, rand);
        this.currentNonce = null;
        this.schoolKey = new RSAVerificationKey();
        this.schoolKey.n = rootKey.getN();
        this.issuerParameters.clear();
        this.credentials.clear();
        this.deviceID = deviceID;
        this.factoryInit = true;
        //Do NOT use the random generator. Causes tests to crash!
        //rand.nextBytes(macKeyForBackup);
        this.zkProofState = null;
        return this.puk;
    }

    private boolean uriLengthOk(URI uri) {
        try {
            return uri.toString().getBytes(URI_ENCODING).length <= MAX_URI_LEN_BYTES;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Problem with URI encoding");
        }
    }

    @Override
    public URI getDeviceURI(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return URI.create(new String(this.getBlob(pin, Smartcard.device_name).blob));
    }

    @Override
    public short getDeviceID(int pin){
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return -1;
        }
        return this.deviceID;
    }

    @Override
    public RSAVerificationKey readAuthenticationKey(int pin, int keyID) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return this.courseKeys.get(keyID);
    }

    @Override
    public SystemParameters getSystemParameters(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return this.params;
    }

    @Override
    public BigInteger computeScopeExclusivePseudonym(int pin, URI scope) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        // hash(scope) ^ deviceSecret (mod p)
        BigInteger base = Utils.baseForScopeExclusivePseudonym(scope, this.params.p, this.params.subgroupOrder);
        return base.modPow(this.deviceSecret, this.params.p);
    }


    @Override
    public BigInteger computeDevicePublicKey(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        // g^deviceSecret (mod p)
        return this.params.g.modPow(this.deviceSecret, this.params.p);
    }

    @Override
    public ZkProofCommitment prepareZkProof(int pin, Set<URI> credentialIds,
            Set<URI> scopeExclusivePseudonyms, boolean includeDevicePublicKeyProof) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }

        for(URI credentialId: credentialIds) {
            if (! this.credentials.containsKey(credentialId)) {
                return null;
            }
            // Check if attendance for each course is high enough
            // (or if we can do the proof without attendance check (for issuance))
            TrustedIssuerParameters tip = this.issuerParameters.get(this.credentials.get(credentialId).parametersUri);
            if (tip == null) {
                throw new RuntimeException("Could not find issuer.");
            }
            if (tip.enforceAttendanceCheck) {
                if (! tip.course.sufficientAttendance() && this.credentials.get(credentialId).issued) {
                    return null;
                }
            }
        }

        for(URI sep: scopeExclusivePseudonyms) {
            if (! this.uriLengthOk(sep)) {
                return null;
            }
        }

        ZkProofSpecification spec = this.getZkProofSpec(pin, credentialIds, scopeExclusivePseudonyms,
                includeDevicePublicKeyProof);
        if (spec == null) {
            return null;
        }
        ZkProofWitness wit = this.getZkProofWitness(credentialIds, includeDevicePublicKeyProof);
        this.zkProofState = ZkProofSystem.firstMove(spec, wit, rand);

        for(URI credentialId: credentialIds) {
            // Mark all credentials as being issued
            this.credentials.get(credentialId).issued = true;
            // Enable course counter
            TrustedIssuerParameters tip = this.issuerParameters.get(this.credentials.get(credentialId).parametersUri);
            if(tip.enforceAttendanceCheck) {
                tip.course.activate();
            }
        }

        return this.zkProofState.commitment;
    }

    @Override
    public ZkProofResponse finalizeZkProof(int pin, BigInteger challenge, Set<URI> credentialIds,
            Set<URI> scopeExclusivePseudonyms) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }

        if(this.zkProofState == null) {
            return null;
        }
        
        ZkProofResponse response =
                ZkProofSystem.secondMove(this.zkProofState, challenge, rand);

        this.zkProofState = null;
        return response;
    }

    @Override
    public SmartcardBackup backupAttendanceData(int pin, String password) {
        //As we should emulate the hardware-smartcard, we have to encrypt
        //stuff in 3 different rounds. One for device-info, one for counters and
        //one for credentials. Everything else (e.g. blob-store) must be stored manually, and the
        //credential information kept is only credentialID || issuerID || status || prescount || v
        //where v is the secret key of the credential.
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        if( this.validatePassword(password) != SmartcardStatusCode.OK){
            return null;
        }

        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        SmartcardBackup backup = new SmartcardBackup();


        sha256.update(this.macKey);
        sha256.update(Utils.passwordToByteArr(password));
        byte[] tmp = sha256.digest(new byte[]{2}); //for counter info label is 2
        byte[] key = new byte[16];
        System.arraycopy(tmp, 0, key, 0, 16);

        //Then the counters
        int noOfCourses = this.listCourses(pin).size();
        ByteBuffer coursesInfo = ByteBuffer.allocate(3*4*noOfCourses); //3 ints saved per course
        for(Course c : this.listCourses(pin)){
            int courseID = c.getCourseID();
            int lectureCount = c.getLectureCount();
            int lastLectureID = c.getLastLectureId();
            coursesInfo.putInt(courseID);
            coursesInfo.putInt(lectureCount);
            coursesInfo.putInt(lastLectureID);
        }
        byte[] toBackup = coursesInfo.array();
        backup.macCounters = SmartcardCrypto.backup(toBackup, key, this.deviceID, rand);

        return backup;
    }

    private byte[] pinToByteArr(int pin) {
        String s = String.valueOf(pin);
        if(s.length() != 4){
            int l = s.length();
            int diff = 4-l;
            String tmp = s;
            s = "";
            for(int i  =0; i < diff; i++){
                s += "0";
            }
            s+=tmp;
        }
        byte[] res = new byte[4];
        for(int i = 0; i < 4; i++){
            res[i] = (byte)s.charAt(i);
        }
        return res;
    }

    @Override
    public SmartcardStatusCode restoreAttendanceData(int pin, String password,
            SmartcardBackup backup) {

        if( this.validatePassword(password) != SmartcardStatusCode.OK){
            return null;
        }

        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //First, we create the issuers again by looking at the data from the counters
        sha256.update(this.macKey);
        sha256.update(Utils.passwordToByteArr(password));
        byte[] tmp = sha256.digest(new byte[]{2}); //for counters, the label is 2
        byte[] key = new byte[16];
        System.arraycopy(tmp, 0, key, 0, 16);
        byte[][] counterInfo = SmartcardCrypto.restore(backup.macCounters, key);
        byte[] deviceID_bytes = counterInfo[0];
        if(ByteBuffer.wrap(deviceID_bytes).getShort() != this.deviceID){
            return SmartcardStatusCode.BAD_REQUEST;
        }
        ByteBuffer counterData = ByteBuffer.wrap(counterInfo[1]);
        int noOfCourses = counterData.remaining()/12;
        for(int i = 0; i < noOfCourses; i++){
            int courseID = counterData.getInt();
            int lectureCount = counterData.getInt();
            int lastLectureID = counterData.getInt();
            log.info("counter id: " + courseID + " issuer: "+this.getIssuerUriFromID(courseID));
            this.issuerParameters.get(this.getIssuerUriFromID(courseID)).course.applyBackup(courseID, lectureCount, lastLectureID);
        }

        return SmartcardStatusCode.OK;
    }

    private byte[] pukToByteArr(int puk) {
        String s = String.valueOf(puk);
        if(s.length() != 8){
            return null;
        }
        byte[] res = new byte[8];
        for(int i = 0; i < 8; i++){
            res[i] = (byte)(s.charAt(i) & 0xFF);
        }
        return res;
    }

    @Override
    public BigInteger computeCredentialFragment(int pin, URI credentialUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        CredentialOnSmartcard cos = this.credentials.get(credentialUri);
        if(cos == null) {
            return null;
        }
        TrustedIssuerParameters tos = this.issuerParameters.get(cos.parametersUri);
        SmartcardParameters sp = tos.groupParams;
        BigInteger base1 = sp.getBaseForDeviceSecret();
        BigInteger base2 = sp.getBaseForCredentialSecretOrNull();
        BigInteger p = sp.getModulus();
        if (base2 != null) {
          BigInteger v = cos.v;
          return base1.modPow(this.deviceSecret, p).multiply(base2.modPow(v, p)).mod(p);
        } else {
          return base1.modPow(this.deviceSecret, p);
        }
    }

    private ZkProofSpecification getZkProofSpec(int pin, Set<URI> courseIds,
            Set<URI> scopeExclusivePseudonyms, boolean includeDevicePublicKeyProof) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }

        ZkProofSpecification zkps = new ZkProofSpecification(this.params);

        zkps.parametersForPseudonyms = this.params;
        zkps.credentialBases = new HashMap<URI, SmartcardParameters>();
        zkps.credFragment = new HashMap<URI, BigInteger>();
        for(URI courseId: courseIds) {
            if (! this.credentials.containsKey(courseId)) {
                return null;
            }
            CredentialOnSmartcard cos = this.credentials.get(courseId);
            TrustedIssuerParameters tos = this.issuerParameters.get(cos.parametersUri);
            zkps.credentialBases.put(courseId, tos.groupParams);
            BigInteger credFragment = this.computeCredentialFragment(pin, courseId);
            zkps.credFragment.put(courseId, credFragment);
        }
        zkps.scopeExclusivePseudonymValues = new HashMap<URI, BigInteger>();
        for (URI scope: scopeExclusivePseudonyms) {
            BigInteger psValue = this.computeScopeExclusivePseudonym(pin, scope);
            zkps.scopeExclusivePseudonymValues.put(scope, psValue);
        }
        if (includeDevicePublicKeyProof) {
            zkps.devicePublicKey = this.computeDevicePublicKey(pin);
        } else {
            zkps.devicePublicKey = null;
        }
        return zkps;
    }

    private ZkProofWitness getZkProofWitness(Set<URI> credentialUris,
            boolean includeDevicePublicKeyProof) {

        ZkProofWitness wit = new ZkProofWitness();
        wit.deviceSecret = this.deviceSecret;
        wit.courseRandomizer = new HashMap<URI, BigInteger>();
        for(URI credId: credentialUris) {
            if (! this.credentials.containsKey(credId)) {
                return null;
            }
            CredentialOnSmartcard cos = this.credentials.get(credId);
            wit.courseRandomizer.put(credId, cos.v);
        }
        return wit;
    }

    @Override
    public Set<URI> listCredentialsUris(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        Set<URI> ret = new HashSet<URI>(this.credentials.keySet());
        return ret;
    }

    @Override
    public TrustedIssuerParameters getIssuerParametersOfCredential(int pin, URI credentialUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        if (! this.credentials.containsKey(credentialUri)) {
            return null;
        }
        URI parametersUri = this.credentials.get(credentialUri).parametersUri;
        if (! this.issuerParameters.containsKey(parametersUri)) {
            throw new RuntimeException("Could not find issuer parameters");
        }
        return this.issuerParameters.get(parametersUri);
    }


    @Override
    public SmartcardStatusCode storeCredential(int pin, URI credentialId, Credential cred, CredentialSerializer serializer){
        byte[] credBytes = serializer.serializeCredential(cred);
        log.fine("CredBytes length: " + credBytes.length);
        int nextCredBlobUri = 1;
        SmartcardStatusCode returnCode = SmartcardStatusCode.OK;

        int bytesLeft = credBytes.length;
        int i = 0;
        boolean done = false;
        while(!done){
            SmartcardBlob blob = new SmartcardBlob();
            if(bytesLeft > MAX_BLOB_LEN_BYTES){
                blob.blob = new byte[MAX_BLOB_LEN_BYTES];
                bytesLeft -= MAX_BLOB_LEN_BYTES;
                System.arraycopy(credBytes, i*MAX_BLOB_LEN_BYTES, blob.blob, 0, MAX_BLOB_LEN_BYTES);
            }else{
                blob.blob = new byte[bytesLeft];
                System.arraycopy(credBytes, i*MAX_BLOB_LEN_BYTES, blob.blob, 0, bytesLeft);
                done = true; //We know we are done as we put the last bytes in the blob.
            }
            URI credUri = URI.create(credentialId.toASCIIString()+"_"+nextCredBlobUri++);
            log.fine("storing a blob of size: " + blob.blob.length + " with uri: " + credUri.toASCIIString());
            returnCode = this.storeBlob(pin, credUri, blob);
            log.fine("Return from storeBlob: " + returnCode);
            if(returnCode != SmartcardStatusCode.OK){
                return returnCode;
            }
            i++;
        }
        return returnCode;
    }

    @Override
    public SmartcardStatusCode storePseudonym(int pin, URI pseudonymId, PseudonymWithMetadata pseudo, PseudonymSerializer serializer){
        byte[] pseudoBytes = serializer.serializePseudonym(pseudo);
        log.fine("PseudoBytes length: " + pseudoBytes.length);
        int nextPseudoBlobUri = 1;
        SmartcardStatusCode returnCode = SmartcardStatusCode.OK;

        int bytesLeft = pseudoBytes.length;
        int i = 0;
        boolean done = false;
        while(!done){
            SmartcardBlob blob = new SmartcardBlob();
            if(bytesLeft > MAX_BLOB_LEN_BYTES){
                blob.blob = new byte[MAX_BLOB_LEN_BYTES];
                bytesLeft -= MAX_BLOB_LEN_BYTES;
                System.arraycopy(pseudoBytes, i*MAX_BLOB_LEN_BYTES, blob.blob, 0, MAX_BLOB_LEN_BYTES);
            }else{
                blob.blob = new byte[bytesLeft];
                System.arraycopy(pseudoBytes, i*MAX_BLOB_LEN_BYTES, blob.blob, 0, bytesLeft);
                done = true; //We know we are done as we put the last bytes in the blob.
            }
            URI credUri = URI.create(pseudonymId.toASCIIString()+"_"+nextPseudoBlobUri++);
            log.fine("storing a blob of size: " + blob.blob.length + " with uri: " + credUri.toASCIIString());
            returnCode = this.storeBlob(pin, credUri, blob);
            if(returnCode != SmartcardStatusCode.OK){
                return returnCode;
            }
            i++;
        }
        return returnCode;
    }

    @Override
    public PseudonymWithMetadata getPseudonym(int pin, URI pseudonymId, PseudonymSerializer serializer){
        ByteArrayOutputStream accumulatedPseuBytes = new ByteArrayOutputStream();
        return this.getPseudonym(pin, pseudonymId, 1, accumulatedPseuBytes, serializer);
    }

    private PseudonymWithMetadata getPseudonym(int pin, URI pseudonymId, int nextPseuBlobUriId,
            ByteArrayOutputStream accumulatedPseuBytes, PseudonymSerializer serializer){
        URI nextPseuBlobUri = URI.create(pseudonymId.toASCIIString()+"_"+nextPseuBlobUriId);
        log.fine("getting this uri: " + nextPseuBlobUri.toASCIIString());
        SmartcardBlob scBlob = this.getBlob(pin, nextPseuBlobUri);
        if(scBlob == null){
            return serializer.unserializePseudonym(accumulatedPseuBytes.toByteArray(), pseudonymId);
        }
        byte[] blob = scBlob.blob;
        accumulatedPseuBytes.write(blob, 0, blob.length);
        log.fine("Accumulated this many bytes: "  + accumulatedPseuBytes.size());
        if(blob.length < MAX_BLOB_LEN_BYTES){
            return serializer.unserializePseudonym(accumulatedPseuBytes.toByteArray(), pseudonymId);
        }else{
            //next round
            return this.getPseudonym(pin, pseudonymId, nextPseuBlobUriId+1, accumulatedPseuBytes, serializer);
        }
    }

    @Override
    public SmartcardStatusCode deletePseudonym(int pin, URI pseudonymUri){
        int i = 1;
        while(true){
            pseudonymUri = URI.create(pseudonymUri.toString()+"_"+i++);
            SmartcardStatusCode code = this.deleteBlob(pin, pseudonymUri);
            if(code != SmartcardStatusCode.OK){
                return code;
            }
        }
    }

    @Override
    public Credential getCredential(int pin, URI credentialId, CredentialSerializer serializer){
        ByteArrayOutputStream accumulatedCredBytes = new ByteArrayOutputStream();
        return this.getCredential(pin, credentialId, 1, accumulatedCredBytes, serializer);
    }

    private Credential getCredential(int pin, URI credentialId, int nextCredBlobUriId,
            ByteArrayOutputStream accumulatedCredBytes, CredentialSerializer serializer){
        URI nextCredBlobUri = URI.create(credentialId.toASCIIString()+"_"+nextCredBlobUriId);
        log.info("getting this uri: " + nextCredBlobUri.toASCIIString());
        SmartcardBlob scBlob = this.getBlob(pin, nextCredBlobUri);
        if(scBlob == null){
            return serializer.unserializeCredential(accumulatedCredBytes.toByteArray(), credentialId, this.getDeviceURI(pin));
        }
        byte[] blob = scBlob.blob;
        accumulatedCredBytes.write(blob, 0, blob.length);
        log.fine("Accumulated this many bytes: "  + accumulatedCredBytes.size());
        if(blob.length < MAX_BLOB_LEN_BYTES){
            //return new CredentialSerializerGzipXml().unserializeCredential(accumulatedCredBytes.toByteArray());
            return serializer.unserializeCredential(accumulatedCredBytes.toByteArray(), credentialId, this.getDeviceURI(pin));
        }else{
            //next round
            return this.getCredential(pin, credentialId, nextCredBlobUriId+1, accumulatedCredBytes, serializer);
        }
    }

    @Override
    public SmartcardStatusCode allocateCredential(int pin, URI credentialUri, URI issuerParameters) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return pinstatus;
        }
        TrustedIssuerParameters iparams = this.getIssuerParameters(pin, issuerParameters);
        if (iparams == null) {
            log.warning("IssuerParameters not found : " + issuerParameters + " - installed " + this.issuerParameters.keySet());
            return SmartcardStatusCode.NOT_FOUND;
        }
        if (! this.uriLengthOk(credentialUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        if (this.credentials.containsKey(credentialUri)) {
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        if ((this.credentials.size() + 1) > MAX_CREDENTIALS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }

        CredentialOnSmartcard cos = new CredentialOnSmartcard(credentialUri, issuerParameters,
                rand, iparams.groupParams.getModulus().bitLength(), this.params.zkStatisticalHidingSizeBytes);
        log.fine("Puts a cred into SC cred store: " + credentialUri);
        this.credentials.put(credentialUri, cos);

        return SmartcardStatusCode.OK;
    }

    @Override
    public SmartcardStatusCode addIssuerParametersWithAttendanceCheck(RSAKeyPair rootKey,
            URI parametersUri, int keyIDForCounter, SmartcardParameters credBases,
            RSAVerificationKey courseKey, int minimumAttendance) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.addToStream(baos, Utils.NEW_ISSUER_WITH_ATTENDANCE);
        Utils.addToStream(baos, parametersUri);
        Utils.addToStream(baos, credBases);
        Utils.addToStream(baos, courseKey);
        Utils.addToStream(baos, minimumAttendance);
        SmartcardStatusCode sigstatus = this.authenticateWithSignature(rootKey, this.schoolKey, baos);
        if (sigstatus != SmartcardStatusCode.OK) {
            return sigstatus;
        }

        if (! this.uriLengthOk(parametersUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        if (this.issuerParameters.containsKey(parametersUri)) {
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        if ((this.issuerParameters.size() + 1) > MAX_ISSUERS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }
        this.courseKeys.put(keyIDForCounter, courseKey);
        byte issuerID = (byte) this.getIssuerIDFromUri(parametersUri);
        TrustedIssuerParameters param = new TrustedIssuerParameters(issuerID, parametersUri, credBases,
                minimumAttendance, keyIDForCounter);
        this.issuerParameters.put(parametersUri, param);
        return SmartcardStatusCode.OK;
    }
/*
    @Override
    public SmartcardStatusCode addUProveIssuerParametersWithAttendanceCheck(RSAKeyPair rootKey,
            URI parametersUri, int keyIDForCounter, UProveParams uProveParams,
            RSAVerificationKey courseKey, int minimumAttendance) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.addToStream(baos, Utils.NEW_ISSUER_WITH_ATTENDANCE);
        Utils.addToStream(baos, parametersUri);
        Utils.addToStream(baos, uProveParams);
        Utils.addToStream(baos, courseKey);
        Utils.addToStream(baos, minimumAttendance);
        SmartcardStatusCode sigstatus = this.authenticateWithSignature(rootKey, this.schoolKey, baos);
        if (sigstatus != SmartcardStatusCode.OK) {
            return sigstatus;
        }

        if (! this.uriLengthOk(parametersUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        if (this.issuerParameters.containsKey(parametersUri)) {
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        if ((this.issuerParameters.size() + 1) > MAX_ISSUERS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }
        this.courseKeys.put(keyIDForCounter, courseKey);
        byte issuerID = (byte) this.getIssuerIDFromUri(parametersUri);
        TrustedIssuerParameters param = new TrustedIssuerParameters(issuerID, parametersUri, uProveParams,
                minimumAttendance, keyIDForCounter);
        this.issuerParameters.put(parametersUri, param);
        return SmartcardStatusCode.OK;
    }
*/
    @Override
    public SmartcardStatusCode addIssuerParameters(RSAKeyPair rootKey,
            URI parametersUri, SmartcardParameters credBases) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.addToStream(baos, Utils.NEW_ISSUER_SIMPLE);
        Utils.addToStream(baos, parametersUri);
        Utils.addToStream(baos, credBases);
        SmartcardStatusCode sigstatus = this.authenticateWithSignature(rootKey, this.schoolKey, baos);
        if (sigstatus != SmartcardStatusCode.OK) {
            return sigstatus;
        }
        if (! this.uriLengthOk(parametersUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        if (this.issuerParameters.containsKey(parametersUri)) {
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        if ((this.issuerParameters.size() + 1) > MAX_ISSUERS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }

        TrustedIssuerParameters param = new TrustedIssuerParameters(parametersUri, credBases);
        this.issuerParameters.put(parametersUri, param);
        return SmartcardStatusCode.OK;
    }
/*
    @Override
    public SmartcardStatusCode addUProveIssuerParameters(RSAKeyPair rootKey,
            URI parametersUri, UProveParams uProveParams) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.addToStream(baos, Utils.NEW_ISSUER_SIMPLE);
        Utils.addToStream(baos, parametersUri);
        Utils.addToStream(baos, uProveParams);
        SmartcardStatusCode sigstatus = this.authenticateWithSignature(rootKey, this.schoolKey, baos);
        if (sigstatus != SmartcardStatusCode.OK) {
            return sigstatus;
        }
        if (! this.uriLengthOk(parametersUri)) {
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        if (this.issuerParameters.containsKey(parametersUri)) {
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        if ((this.issuerParameters.size() + 1) > MAX_ISSUERS) {
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }

        TrustedIssuerParameters param = new TrustedIssuerParameters(parametersUri, uProveParams);
        this.issuerParameters.put(parametersUri, param);
        return SmartcardStatusCode.OK;
    }
    */

    @Override
    public TrustedIssuerParameters getIssuerParameters(int pin, URI paramsUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        if (! this.issuerParameters.containsKey(paramsUri)) {
            return null;
        }
        return this.issuerParameters.get(paramsUri);
    }

    @Override
    public Set<TrustedIssuerParameters> getIssuerParametersList(int pin) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return null;
        }
        return new HashSet<TrustedIssuerParameters>(this.issuerParameters.values());
    }

    @Override
    public SmartcardStatusCode deleteIssuer(int pin, URI paramUri, RSAKeyPair rootKey) {
        if(!this.factoryInit){
            return SmartcardStatusCode.NOT_INITIALIZED;
        }
        if(!this.issuerParameters.containsKey(paramUri)){
            return SmartcardStatusCode.NOT_MODIFIED;
        }
        ByteArrayOutputStream toSign = new ByteArrayOutputStream();
        Utils.addToStream(toSign, paramUri);
        this.authenticateWithSignature(rootKey, this.schoolKey, toSign);

        if (! this.issuerParameters.containsKey(paramUri)) {
            return SmartcardStatusCode.NOT_FOUND;
        }
        for(CredentialOnSmartcard cos: this.credentials.values()) {
            if (cos.parametersUri.equals(paramUri)) {
                return SmartcardStatusCode.NOT_MODIFIED;
            }
        }
        this.issuerParameters.remove(paramUri);
        return SmartcardStatusCode.OK;
    }

    @Override
    public boolean credentialExists(int pin, URI credentialUri) {
        SmartcardStatusCode pinstatus = this.authenticateWithPin(pin);
        if (pinstatus != SmartcardStatusCode.OK) {
            return false;
        }
        log.info("Trying to fetch "+credentialUri.toString());

        log.fine("\n");
        CredentialOnSmartcard cos = this.credentials.get(credentialUri);
        for(URI cred : this.credentials.keySet()){
            log.fine("Credentials on smartcard available: " + cred);
        }
        log.fine("\n");
        return cos != null;
    }

    @Override
    public int getCounterValue(int pin, URI issuerId) {
        Course course = this.getCourse(pin, issuerId);
        return course.getLectureCount();
    }
    public String getHashOfDeviceSecret() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(this.deviceSecret.toByteArray());
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100,
                        16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getHashOfSystemParameters() {
        if (this.params != null) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(this.params);
                    byte[] yourBytes = bos.toByteArray();
                    md.update(yourBytes);
                    byte[] mdbytes = md.digest();

                    // convert the byte to hex format method 1
                    StringBuffer sb = new StringBuffer();
                    for (byte mdbyte : mdbytes) {
                        sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                                .substring(1));
                    }

                    out.close();
                    bos.close();

                    return sb.toString();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    try {
                        out.close();
                        bos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
        return "No smart system params";
    }

    public String getHashOfIssuerParameters() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            for (TrustedIssuerParameters tip : this.issuerParameters.values()) {
                this.updateDigest(md, tip);
            }
            byte[] mdbytes = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getHashOfCredentialKeys() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            for (CredentialOnSmartcard cos : this.credentials.values()) {

                this.updateDigest(md, cos);
            }
            byte[] mdbytes = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updateDigest(MessageDigest md, Serializable cos) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(cos);
            byte[] yourBytes = bos.toByteArray();
            md.update(yourBytes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

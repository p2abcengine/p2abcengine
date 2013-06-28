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

package eu.abc4trust.smartcard;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.lang.NotImplementedException;

import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadStorageManager;
import eu.abc4trust.cryptoEngine.uprove.user.UProveCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PseudonymWithMetadata;

/**
 * A few things that are assumed to be true:
 * 	 - IssuerID's are static and can be found by a lookup in a static map from ID's to URI's.
 *   - Credential IDs are stored in blobs with the first byte
 *     of the blob being the ID of the credential.
 *   - A root-key with keyID=0 is always present when in working mode.
 * 	 - IssuerID = CounterID = groupID (but probably only for the pilot)
 * @author Kasper damgaard
 */
public class HardwareSmartcard implements Smartcard {

	private Card card;
    private CardChannel channel;
    private CardTerminal terminal;

    @SuppressWarnings("unused")
    private final byte
    getMode = 0x02, //works in any mode. returns 1 byte data
    setRootMode = 0x04, //8 byte accesscode required
    setWorkingMode = 0x06, //only from root. nothing else req.
    setVirginMode = 0x08, //16byte mac req.
    pinTrialsLeft = 0x0A, //returns single byte
    pukTrialsLeft = 0x0C, //returns single byte
    changePin = 0x0E, //old-pin and new-pin - 8 bytes total
    resetPin = 0x10, //8 byte PUK and 4 byte new pin
    initializeDevice = 0x12, //2 byte id and 2 byte size - only root. Gives back a ciphertext
    getDeviceID = 0x14, //pin, gives back a 2 byte deviceID
    getVersion = 0x16, //gives back a 64 byte version number
    getMemorySpace = 0x18, //pin, 2 byte back
    putData = 0x1A, //variable length input > 0
    getChallenge = 0x1C, //input challenge size (0 is 256), get back challenge of that size.
    authenticateData = 0x1E, //single keyID byte  - prior: PUT DATA
    setAuthenticationKey = 0x20, //single keyID byte - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    listAuthenticationKeys = 0x22, //pin, get back keyID||size(key) over all keys
    readAuthenticationKey = 0x24, //pin, keyID , get back Auth. key
    removeAuthenticationKey = 0x26, //single byte keyID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    setGroupComponent = 0x28, //groupID, compType [0,2] - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    setGenerator = 0x2A, //groupID, genID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    listGroups = 0x2C, //pin, get back concat of all groupID's
    readGroup = 0x2E, //pin, groupID, get back description of the group
    readGroupComponent = 0x30, //pin, groupID, comptype [0:modulus, 1:group order, 2: cofactor, 3:# of generators]
    readGenerator = 0x32, //pin, groupID, genID, get back the group generator
    removeGroup = 0x34, //groupID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    setCounter = 0x36, //counterID, keyID, index, threshold, 4 byte cursor - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    incrementCounter = 0x38, //keyID, byte[] sig
    listCounters = 0x3A, //pin, get back concat of all counterID's.
    readCounter = 0x3C, //pin, counterID, get back description of counter (see setCounter)
    removeCounter = 0x3E, //counterID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    setIssuer = 0x40, //issuerID, groupID, genID1, genID2, numpres, counterID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    listIssuers = 0x42, //pin, get back concat of issuerID's
    readIssuer = 0x44, //pin, issuerID, get back description of Issuer
    removeIssuer = 0x46, //issuerID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    setProver = 0x48, //proverID, 2 byte ksize, 2 byte csize, byte[] credIDs - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    readProver = 0x4A, //pin, proverID, get back description of prover
    removeProver = 0x4C, //proverID - prior to: GET CHALLENGE, PUT DATA, AUTHENTICATE DATA
    startCommitments = 0x4E, //pin, proverID, get back 16 byte proofsession
    startResponses = 0x50, //pin, proverID, byte[] input (at most 2043 bytes)
    setCredential = 0x52, //pin, credentialID, issuerID
    listCredentials = 0x54, //pin, get back concat of all credentialID's
    readCredential = 0x56, //pin, credentialID, 7 byte description of credential
    removeCredential = 0x58, //pin, credentialID
    getCredentialPublicKey = 0x5A, //pin, credentialID, get back byte[] public key
    getIssuanceCommitment = 0x5C, //pin, credentialID, get back byte[] C (issuance commitment)
    getIssuanceResponse = 0x5E, //pin, credentialID, get back byte[] R
    getPresentationCommitment = 0x60, //pin, credentialID, get back byte[] C (presentiation commitment)
    getPresentationResponse = 0x62, //pin, credentialID, get back byte[] R (presentation response)
    getDevicePublicKey = 0x64, //pin, get back byte[] device Pk
    getDeviceCommitment = 0x66, //pin, get back byte[] C (device commitment)
    getDeviceResponse = 0x68,	//pin, get back byte[] R (device response)
    getScopeExclusivePseudonym = 0x6A, //pin, byte[] scope (max 2044 bytes), get back byte[] h(scope)^deviceKey mod m
    getScopeExclusiveCommitment = 0x6C, //pin, byte[] scope, get back byte[] C (scope-exclusive commitment)
    getScopeExclusiveResponse = 0x6E, //pin, byte[] scope, get back byte[] R (scope-exclusive response)
    storeBlob = 0x70, //pin, byte[] uri (1-200 bytes)
    listBlobs = 0x72, //pin, nread (number of URI's already read), get back URI's in LV1 format + updated nread + nunread URI's
    readBlob = 0x74, //pin, byte[] uri (1-200 bytes), get back contents of the URI
    removeBlob = 0x76, //pin, byte[] uri
    backupDevice = 0x78, //pin, password(8 bytes) - (extended)
    restoreDevice = 0x7A, //pin, password (same as the one used in backupDevice) - NOT extended
    backupCounters = 0x7C, //pin, password (8 bytes) - NOT extended
    restoreCounters = 0x7E; // pin, password (same as the one used in
                                    // backupCounters) - NOT extended

    private final int
    backupCredential = 0x80, // pin, password (8 bytes), credentialID - Exteded
    restoreCredential = 0x82; //pin, password (8 bytes) - NOT Exteded.
    
    private final int ABC4TRUSTCMD = 0xBC,
            STATUS_OK = 0x90,
            STATUS_FORBIDDEN = 0x9A,
            STATUS_TOO_LITTLE_DATA = 0x9B,
            STATUS_NOT_EXACT_DATA_SIZE = 0x9C,
            STATUS_TOO_SMALL_CHALLENGE = 0x9D,
            STATUS_RFU = 0x9E,
            STATUS_ERROR = 0x9F;
    
    private final int STATUS_BAD_PIN = 0x03,
    		STATUS_CARD_LOCKED = 0x04,
    		STATUS_BAD_PUK = 0x05,
    		STATUS_CARD_DEAD = 0x06;

    private static final int MAX_CREDENTIALS = 8;
    public static final int MAX_BLOB_BYTES = 512;

    private final Random rand; 
	private static final StaticUriToIDMap staticMap = StaticUriToIDMap.getInstance();
	public static boolean printInput = false;

    /**
     * 
     * @param terminal
     * @param card
     * @param file A file describing the credential URI to ID mapping. If null, it is assumed that no mapping is done yet.
     */
    public HardwareSmartcard(CardTerminal terminal, Card card, Random rand) {
        this.terminal = terminal;
        this.channel = card.getBasicChannel();
        this.card = card;
        this.rand = rand;                
    }    
    
    private ResponseAPDU transmitCommand(CommandAPDU cmd) throws CardException{
    	int count = 0;
    	while(count < 4){
    		try{
				card.beginExclusive();
				break;
			}catch(CardException e){
				System.err.println("Could not obtain exclusive lock on the card!");
				count++;
				if(count == 4){
					throw e;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				card.disconnect(false);
				card = terminal.connect("*");
				this.channel = card.getBasicChannel();
			}
    	}
    	
		ResponseAPDU response = channel.transmit(cmd);
		card.endExclusive();    		
		return response;
    }
    
    @SuppressWarnings("unused")
    private void resetCard(){
    	try {
    		this.channel = null;
    		card.disconnect(true); //reset after disconnect
    		card = null;
    	    this.card = this.terminal.connect("*");
			this.channel = this.card.getBasicChannel();	
		} catch (CardException e) {
			throw new RuntimeException(e);
		}
    }
    
    /*
    private void detectMaxBlobSize(int pin){
    	if(MAX_BLOB_BYTES == 0){
    		if(this.readIssuer(pin, StaticUriToIDMap.credUnivUProveIssuer) == null){
    			MAX_BLOB_BYTES = 1900; //Idemix
    		}else{
    			MAX_BLOB_BYTES = 2046; //UProve
    		}
    	}
    }
    */
	

    private SmartcardStatusCode evaluateStatus(ResponseAPDU response){
        switch(response.getSW1()){
        case STATUS_OK:
            return SmartcardStatusCode.OK;
        case STATUS_FORBIDDEN:
            return SmartcardStatusCode.FORBIDDEN;
        case STATUS_TOO_LITTLE_DATA:
        case STATUS_NOT_EXACT_DATA_SIZE:
        case STATUS_TOO_SMALL_CHALLENGE:
        case STATUS_RFU:
            return SmartcardStatusCode.BAD_REQUEST;
        case STATUS_ERROR:
        	switch(response.getSW2()){
        	case STATUS_BAD_PIN:
        	case STATUS_BAD_PUK:
        		return SmartcardStatusCode.UNAUTHORIZED;
        	case STATUS_CARD_LOCKED:
        	case STATUS_CARD_DEAD:
        		return SmartcardStatusCode.FORBIDDEN;
        	
        	}
            return SmartcardStatusCode.BAD_REQUEST;
        default:
            return SmartcardStatusCode.BAD_REQUEST;
        }
    }

    /**
     * 
     * @param length
     * @return a byte array of length 2 containing the length in bytes
     */
    private byte[] intLengthToShortByteArr(int length){
        return ByteBuffer.allocate(2).putShort((short)length).array();
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

    private byte[] pukToByteArr(int puk){
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

    private byte[] uriToByteArr(URI uri){
        String s = uri.toASCIIString();
        byte[] res = new byte[s.length()];
        for(int i = 0; i < s.length(); i++){
            res[i] = (byte)s.charAt(i);
        }
        return res;
    }

    private URI byteArrToUri(byte[] b){
        try {
            String s = new String(b, "US-ASCII");
            System.out.println("s: " +s);
            return URI.create(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte getIssuerIDFromUri(int pin, URI uri){
    	return staticMap.getIssuerIDFromUri(uri);
    }

    private URI getIssuerUriFromID(int pin, byte ID, CryptoEngine engine){
    	return staticMap.getIssuerUriFromID(ID, engine);
    }

    public byte getCredentialIDFromUri(int pin, URI uri){
    	return this.getBlob(pin, uri).blob[0];
    }

    private URI getCredentialUriFromID(int pin, byte ID){    	
    	Map<URI, SmartcardBlob> blobs = getBlobs(pin);
    	for(URI uri : blobs.keySet()){
    		if(blobs.get(uri).blob[0] == ID && blobs.get(uri).blob.length == 1){
    			return uri;
    		}
    	}
        return null;
    }

    private byte getNewCredentialID(int pin){    	
        int maxNoOfCredentials = 8; //hardcoded in the card as well.
        Map<URI, SmartcardBlob> blobs = getBlobs(pin);
        for(int credID = 1; credID <= maxNoOfCredentials ; credID++){
        	boolean foundCredID = false;
        	for(URI uri : blobs.keySet()){
        		if(blobs.get(uri).blob[0] == credID && blobs.get(uri).blob.length == 1){
        			URI possibleCredURI = URI.create(uri.toString()+"_1");
        			if(blobs.containsKey(possibleCredURI)){
        				//there really is a credential with this ID
        				foundCredID = true;
            			continue;
        			}else{
        				//at some point in an issuance, something went wrong, and we only have the "URI to ID blob".
        				//Delete the URI to ID blob and return the credID
        				this.deleteBlob(pin, uri);
        				return (byte)credID;
        			}
        		}
        	}            
        	if(foundCredID){
        		continue;
        	}
        	return (byte)credID;
        }
        throw new RuntimeException("No more than "+maxNoOfCredentials+" credentials can be stored. remove one and try again.");
    }
    
    private byte getNewIssuerID(URI issuerUri){
    	return staticMap.getIssuerIDFromUri(issuerUri);    	
    }
    
    /**
     * Stores the ID at position 2 in the blob in order to differentiate between stored credentials and stored issuers
     * @param pin
     * @param uri
     * @param ID
     */
    public SmartcardStatusCode storeIssuerUriAndID(int pin, URI uri, byte ID){
    	SmartcardBlob blob = new SmartcardBlob();
    	blob.blob = new byte[2];
    	blob.blob[1] = ID;
        return this.storeBlob(pin, uri, blob);
    }

    private SmartcardStatusCode storeCredentialUriAndID(int pin, URI uri, byte ID){
    	SmartcardBlob blob = new SmartcardBlob();
    	blob.blob = new byte[1];
    	blob.blob[0] = ID;
        return this.storeBlob(pin, uri, blob);
    }

    @Override
    public void removeCredentialUri(int pin, URI uri){    	
    	int i = 1;
    	while(true){
    		URI tmpUri = URI.create(uri.toString()+"_"+i++);    		
    		if(this.deleteBlob(pin, tmpUri) != SmartcardStatusCode.OK){
    			if(i == 1){
    				//Actual error - we should be able to remove at least 1 blob
    				throw new RuntimeException("Could not delete blob: " + tmpUri);
    			}
    			return;
    		}else{
    			System.out.println("intermediate step, removed credential blob: "+ tmpUri);
    		}
    	}
    }

    public int getMode(){
        try {
        	ByteBuffer buf = ByteBuffer.allocate(5);
        	buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getMode, 0, 0, 1});
        	buf.position(0);
        	System.out.println("Input to GetMode: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Reponse from getMode: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData()[0];
            }
        } catch (CardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public String getVersion(){
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.getVersion, 0, 0, 64));
            System.out.println("Response from getVersion: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                String res = "";
                byte[] data = response.getData();
                for(int i = 0; i < 64; i++){
                    res += (char)(data[i] & 0xFF);
                }
                return res;
            }
        } catch (CardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public SmartcardStatusCode setVirginMode(byte[] mac){
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.setVirginMode, 0, 0, mac));
            System.out.println("response from setVirginMode: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            System.err.println("Failed to setVirginMode : " + e);
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    public SmartcardStatusCode setRootMode(byte[] accesscode){
        try {
        	ByteBuffer buf = ByteBuffer.allocate(13);
        	buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setRootMode, 0, 0, 8});
        	buf.put(accesscode);
        	buf.position(0);
        	System.out.println("Input to setRootMode: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("response from setRootMode: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    public SmartcardStatusCode setWorkingMode(){
    	ByteBuffer buf = ByteBuffer.allocate(4);
    	buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setWorkingMode, 0, 0});
    	buf.position(0);
        try {
        	System.out.println("Input for setWorkingMode: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("response from setWorkingMode: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    private void putData(byte[] data){
        try {
        	ByteBuffer buf = ByteBuffer.allocate(7 + data.length);
        	buf.put((byte) this.ABC4TRUSTCMD);
        	buf.put(this.putData);
        	buf.put(new byte[]{0,0,0});
        	buf.put(this.intLengthToShortByteArr(data.length));
        	buf.put(data);
        	buf.position(0);
        	System.out.println("Input to PutData: "+ Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from putData: " + response);
        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RSAVerificationKey readAuthenticationKey(int pin, int keyID){
        byte[] data = new byte[5];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = (byte)keyID;
        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.put(new byte[]{(byte) this.ABC4TRUSTCMD, this.readAuthenticationKey, 0, 0, 0, 0, 5});
        buffer.put(data);
        buffer.put(new byte[]{0, 0});
        buffer.position(0);
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buffer));
            System.out.println("Response from readAuthenticationKey: "+ response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                RSAVerificationKey vkey = new RSAVerificationKey();
                vkey.n = new BigInteger(1, response.getData());
                return vkey;
            }
            return null;
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private byte[] removeSignBit(byte[] positiveNumber){
    	if(positiveNumber[0] == 0){
        	byte[] tmp = new byte[positiveNumber.length-1];
        	System.arraycopy(positiveNumber, 1, tmp, 0, tmp.length);
        	return tmp;
        }else{
        	return positiveNumber;
        }
    }

    public SmartcardStatusCode setAuthenticationKey(BigInteger pk, int keyID, RSAKeyPair rootKey){
        byte[] pk_bytes = pk.toByteArray();
        pk_bytes = removeSignBit(pk_bytes);
        ResponseAPDU response;
        try {
            int mode = this.getMode();
            if(mode == 1){
                this.putData(pk_bytes);
            }else if(mode == 2){
                System.out.println("Can only use setAuthenticationKey in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;
            }
            System.out.println("Input for setAuthKey: " + Arrays.toString(new byte[]{(byte)this.ABC4TRUSTCMD, this.setAuthenticationKey, 0, 0, 1, (byte)keyID}));
            response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.setAuthenticationKey, 0, 0, new byte[]{(byte)keyID}));
            System.out.println("response from setAuthKey: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    /**
     * 
     * @param mode
     * @param component
     * @param groupID 
     * @param compType (0: mod, 1: order, 2: co-factor)
     * @param rootKey
     * @return
     * @throws CardException
     */
    private SmartcardStatusCode setGroupComponent(int mode, byte[] component, int groupID, int compType,
            RSAKeyPair rootKey) throws CardException{
    	component = removeSignBit(component);
    	
        byte[] data = new byte[2];
        data[0] = (byte)groupID;
        data[1] = (byte)compType;
        if(mode == 1){
            this.putData(component);
        }else{
        	System.out.println("Can only use setGroupComponent in root mode");
            return SmartcardStatusCode.UNAUTHORIZED;
        }
        ByteBuffer buf = ByteBuffer.allocate(7);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setGroupComponent, 0, 0, 2, (byte)groupID, (byte)compType});
        buf.position(0);
        System.out.println("Input for set Group Component: " + Arrays.toString(buf.array()));
        ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
        System.out.println("Response from setGroupComponent: " + response);
        return this.evaluateStatus(response);
    }

    private SmartcardStatusCode setGenerator(int mode, byte[] g, int groupID, int genID, RSAKeyPair rootKey)  throws CardException{
    	g = this.removeSignBit(g);
    	
        byte[] data = new byte[2];
        data[0] = (byte)groupID;
        data[1] = (byte)genID;
        if(mode == 1){
            this.putData(g);
        }else{
        	System.out.println("Can only use setGenerator in root mode");
            return SmartcardStatusCode.UNAUTHORIZED;
        }
        ByteBuffer buf = ByteBuffer.allocate(7);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setGenerator, 0, 0, 2, (byte)groupID, (byte)genID});
        buf.position(0);
        System.out.println("Input for set Generator: " + Arrays.toString(buf.array()));
        ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
        System.out.println("Response from setGenerator: " + response);
        return this.evaluateStatus(response);
    }

    private SmartcardStatusCode setCounter(int counterID, int keyID, int index, int threshold, byte[] cursor, RSAKeyPair rootKey){
        if(cursor.length != 4){
            throw new RuntimeException("Cursor should be of length 4");
        }
        byte[] data = new byte[8];
        data[0] = (byte)counterID;
        data[1] = (byte)keyID;
        data[2] = (byte)index;
        data[3] = (byte)threshold;
        System.arraycopy(cursor, 0, data, 4, 4);
        try {
            int mode = this.getMode();
            if(mode == 2){
            	System.out.println("Can only use setCounter in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;
            }
            ByteBuffer buf = ByteBuffer.allocate(13);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setCounter, 0, 0, 8});
            buf.put(data);
            buf.position(0);
            System.out.println("Input for setCounter: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setCounter: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    public byte[] getChallenge(int size){
        //TODO: Make this work for challenge sizes of 256 (or 0)
        if((size > 256) || (size < 1)){
            System.err.println("Argument 'size' for getChallenge should be in the range [1,256]");
            return null;
        }
        try {
            int realSize = size;
            if(realSize == 256){
                realSize = 0;
            }
            ByteBuffer buf = ByteBuffer.allocate(7);
            System.out.println("Le: " + (byte)size);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getChallenge, 0, 0, 1, (byte)realSize, 0});
            buf.position(0);
            System.out.println("Input for getChallenge: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getChallenge: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData();
            }else{
                return null;
            }
        } catch (CardException e) {
            return null;
        }
    }

    @Override
    public boolean wasInit() {
        int mode = this.getMode();
        if((mode == 0) || (mode == 1)){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public URI getDeviceURI(int pin) {
        try {
            SmartcardBlob blob = this.getBlob(pin, Smartcard.device_name);
            if(blob == null){
            	return null;
            }
            return URI.create(new String(blob.blob, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public short getDeviceID(int pin){
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.getDeviceID, 0, 0, this.pinToByteArr(pin), 2));
            System.out.println("Response from getdeviceID: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return ByteBuffer.wrap(response.getData()).getShort();
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public TrustedIssuerParameters getIssuerParametersOfCredential(int pin,
            URI credentialId) {
        byte credID = this.getCredentialIDFromUri(pin, credentialId);
        byte[] cred_info = this.readCredential(pin, credID);
        byte issuerID = cred_info[0];
        CryptoEngine engine = CryptoEngine.UPROVE;
        if(credentialId.toString().startsWith("IdmxCredential")){
        	engine = CryptoEngine.IDEMIX;
        }
        return this.getIssuerParameters(pin, this.getIssuerUriFromID(pin, issuerID, engine));
    }

    SystemParameters cachedSystemParams = null;
    @Override
    public SystemParameters getSystemParameters(int pin) {
        if(cachedSystemParams!=null) { 
            return cachedSystemParams;
        }
        //here we need to get the prime modulus p, the generator g and the subgroup order
        SystemParameters params = new SystemParameters();
        params.p = this.getGroupComponent(pin, 0, 0);
        params.subgroupOrder = this.getGroupComponent(pin, 0, 1);
        params.g = this.getGenerator(pin, 0, 1);
        System.out.println("Fetched System Parameters p, q and g: ");
        
        cachedSystemParams = params;
        return params;
    }

    Map<String, BigInteger> cachedGroupComponent = new HashMap<String, BigInteger>();
    /**
     * 
     * @param pin
     * @param groupID
     * @param compType 0: modulus, 1: group order 2: cofactor
     * @return
     */
    private BigInteger getGroupComponent(int pin, int groupID, int compType){
        if(cachedGroupComponent.containsKey(groupID + ":" + compType)) {
            BigInteger cached = cachedGroupComponent.get(groupID + ":" + compType);
            System.out.println("Cached readGroupComponent: " + groupID + " : " + compType + " : " + cached);
            return cached;
        }
        ByteBuffer buf = ByteBuffer.allocate(15);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readGroupComponent, 0, 0, 0, 0, 6});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{(byte)groupID, (byte)compType, 0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for readGroupComponent: " + groupID + " : " + compType + " : " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readGroupComponent: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                BigInteger groupComponent = new BigInteger(1, response.getData());
                System.out.println("GroupComponent - is : " + groupID + " : " + compType + " : " + groupComponent);

                cachedGroupComponent.put(groupID + ":" + compType, groupComponent);
                return groupComponent;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }
    Map<String, BigInteger> cachedGenerator = new HashMap<String, BigInteger>();

    private BigInteger getGenerator(int pin, int groupID, int genID){
        if(cachedGenerator.containsKey(groupID + ":" + genID)) {
            BigInteger cached = cachedGenerator.get(groupID + ":" + genID);
            System.out.println("Cached readGenerator: " + groupID + " : " + genID + " : " + cached);
            return cached;
        }

        ByteBuffer buf = ByteBuffer.allocate(15);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readGenerator, 0, 0, 0, 0, 6});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{(byte)groupID, (byte)genID, 0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for readGenerator: " + groupID + " : " + genID + " : " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readGenerator: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                BigInteger generator = new BigInteger(1, response.getData());
                System.out.println("Generator - is : " + groupID + " : " + genID + " : " + generator);
                cachedGenerator.put(groupID + ":" + genID, generator);
                return generator;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    Map<URI,BigInteger> cachedScopeExclusivePseudonym = new HashMap<URI, BigInteger>();
    @Override
    public BigInteger computeScopeExclusivePseudonym(int pin, URI scope) {
        if(cachedScopeExclusivePseudonym.containsKey(scope)) {
             BigInteger pv = cachedScopeExclusivePseudonym.get(scope);
             System.out.println("Cached from getScopeExclusivePseudonym: " + scope + " : " + pv);
             return pv;
        }
        try {
            byte[] scopeBytes = this.uriToByteArr(scope);
            if(scopeBytes.length > 2044){
                throw new RuntimeException("The inputted scope is too large.");
            }
            byte[] begin = new byte[]{(byte)this.ABC4TRUSTCMD, this.getScopeExclusivePseudonym, 0, 0, 0};
            ByteBuffer buf = ByteBuffer.allocate(9+4+scopeBytes.length);
            buf.put(begin);
            buf.put(this.intLengthToShortByteArr(4+scopeBytes.length));
            buf.put(this.pinToByteArr(pin));
            buf.put(scopeBytes);
            buf.put(new byte[]{0,0});
            buf.position(0);

            if(printInput)
        		System.out.println("Input for getScopeExclusivePseudonym: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getScopeExclusivePseudonym: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                BigInteger pv = new BigInteger(1, response.getData());
                cachedScopeExclusivePseudonym.put(scope, pv);
                return pv;
            }
            return null;
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BigInteger computeDevicePublicKey(int pin) {
        ByteBuffer buf = ByteBuffer.allocate(13);
        buf.put(new byte[]{(byte) this.ABC4TRUSTCMD, this.getDevicePublicKey, 0, 0, 0, 0, 4});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{0, 0});
        buf.position(0);
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getDevicePublicKey: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return new BigInteger(1, response.getData());
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public ZkProofCommitment prepareZkProof(int pin, Set<URI> credentialIds,
            Set<URI> scopeExclusivePseudonyms,
            boolean includeDevicePublicKeyProof) {
        ZkProofCommitment comm = new ZkProofCommitment();

        SystemParameters params = this.getSystemParameters(pin);
        comm.spec = new ZkProofSpecification(params);
        comm.spec.parametersForPseudonyms = params;
        comm.spec.credentialBases = new HashMap<URI, GroupParameters>();
        comm.spec.credFragment = new HashMap<URI, BigInteger>();
        for(URI courseId: credentialIds) {
            byte credID = this.getCredentialIDFromUri(pin, courseId);
            byte[] cred = this.readCredential(pin, credID);            
            byte issuerID = cred[0];
            GroupParameters groupParams = this.getGroupParameters(pin, issuerID); 
            comm.spec.credentialBases.put(courseId, groupParams);
            comm.spec.credFragment.put(courseId, this.computeCredentialFragment(pin, courseId));
        }
        comm.spec.scopeExclusivePseudonymValues = new HashMap<URI, BigInteger>();

        byte[] data = new byte[5];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = 1; //ProverID - TODO: hardcoded to 1 as of now. Assuming there can be only 1 for the pilot
        byte[] proofSession = null;
        ByteBuffer buf = ByteBuffer.allocate(11);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.startCommitments, 0, 0, 5});
        buf.put(data);
        buf.put((byte)16);
        buf.position(0);
        try {        	
        	if(printInput)
        		System.out.println("Input for startCommitments: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from startCommitments: "+response);
            System.out.println("And this is the output: " + Arrays.toString(response.getData()));
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return null;
            }
            proofSession = response.getData();
        } catch (CardException e) {
            throw new RuntimeException("PrepareZkProof crashed.", e);
        }
        //ProofStatus set to 1        
        comm.nonceCommitment = proofSession;

        if(includeDevicePublicKeyProof){
            comm.spec.devicePublicKey = this.computeDevicePublicKey(pin);
            comm.commitmentForDevicePublicKey = this.computeDevicePublicKeyCommitment(pin);
        }

        boolean notEnoughAttendance = false;
        for(URI uri : credentialIds){        	        	
            byte credID = this.getCredentialIDFromUri(pin, uri);
            byte[] credInfo = readCredential(pin, credID);            
            //byte issuerID = credInfo[0];
            //byte counterID = this.readIssuer(pin, issuerID)[4];
            byte status = credInfo[5];
            byte presentOrIssuance = this.getIssuanceCommitment;
            String command = "getIssuanceCommitment";
            //System.out.println("\nStatus of credential before commitments are made: " + status);
            if(status == 2){
            	//credential has already been issued. So we assume we want to present it.
            	command = "getPresentationCommitment";
            	presentOrIssuance = this.getPresentationCommitment;
            }
            /*
            if(counterID != 0){
            	//Counter active. We must know if the attendance is high enough.
	            byte[] counterInfo = readCounter(pin, counterID);
	            int index = counterInfo[1];
	            int threshold = counterInfo[2];
	            if(index < threshold && presentOrIssuance == this.getPresentationCommitment){
	            	//Not enough attendance. aborting at the end; Done because of timing attacks.
	            	notEnoughAttendance = true;
	            }
            } 
            */           
            
            buf = ByteBuffer.allocate(14);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, presentOrIssuance, 0, 0, 0, 0, 5});
            buf.put(this.pinToByteArr(pin));
            buf.put(credID);
            buf.put(new byte[]{0,0});
            buf.position(0);
            try {            	            	
            	if(printInput)
            		System.out.println("Input for "+command+": " +Arrays.toString(buf.array()));
                ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
                System.out.println("Response from "+command+": "+response);
                if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                    comm.commitmentForCreds.put(uri, new BigInteger(1, response.getData()));
                }else{
                    return null;
                }
            } catch (CardException e) {
                throw new RuntimeException("PrepareZkProof crashed.", e);
            }            
        }

        for(URI scope : scopeExclusivePseudonyms){
            BigInteger pseudonymCommitment = this.getScopeExclusiveCommitment(pin, scope);
            comm.commitmentForScopeExclusivePseudonyms.put(scope, pseudonymCommitment);
            comm.spec.scopeExclusivePseudonymValues.put(scope, this.computeScopeExclusivePseudonym(pin, scope));
        }
        if(notEnoughAttendance){
        	System.out.println("Because of not enough attendance?");
        	return null;
        }else{
        	return comm;
        }
    }

    private GroupParameters getGroupParameters(int pin, byte groupID) {
		BigInteger g1 = this.getGenerator(pin, groupID, 1);
		BigInteger g2 = this.getGenerator(pin, groupID, 2);
		BigInteger n = this.getGroupComponent(pin, groupID, 0);
		GroupParameters gp;
		if(g2 == null){
			//UPROVE
			BigInteger q = this.getGroupComponent(pin, groupID, 1);
			gp = new UProveParams(g1, n, q);
		}else{
			//IDEMIX
			gp = new CredentialBases(g1, g2, n);			
		}
		return gp;
	}

	/**
     * 
     * @param pin
     * @param credentialID
     * @return byte array containing: issuerID || size(v) [2 bytes] || size(kv) [2 bytes] || status || prescount
     */
    private byte[] readCredential(int pin, int credentialID){
        byte[] data = new byte[5];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = (byte)credentialID;
        ByteBuffer buf = ByteBuffer.allocate(11);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readCredential, 0, 0, 5});
        buf.put(data);
        buf.put((byte)7);
        buf.position(0);
        try {        	
        	if(printInput)
        		System.out.println("Input for readCredential: " + Arrays.toString(buf.array()));
        	System.out.println("Reading the on-board credential with ID="+credentialID);
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readCredential: " + response);
            System.out.println("With the data: " + Arrays.toString(response.getData()));            
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData();
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    Map<Integer, byte[]> cachedIssuerByteArray = new HashMap<Integer, byte[]>();
    /**
     * @param pin
     * @param issuerID
     * @return byte array containing: groupID || genID1 || genID2 || numpres || counterID
     */
    private byte[] readIssuer(int pin, int issuerID){
        if(cachedIssuerByteArray.containsKey(issuerID)) {
            byte[] cached = cachedIssuerByteArray.get(issuerID);
            System.out.println("ReadIssuer - use cached : " + (cached == null ? null : Arrays.toString(cached)));
            return cached;
        }
        
        byte[] data = new byte[5];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = (byte)issuerID;
        ByteBuffer buf = ByteBuffer.allocate(11);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readIssuer, 0, 0, 5});
        buf.put(data);
        buf.put((byte)5);
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for readIssuer: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readIssuer: " + response);
            System.out.println("With the data: "+Arrays.toString(response.getData()));
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                cachedIssuerByteArray.put(issuerID, response.getData());
                return response.getData();
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        cachedIssuerByteArray.put(issuerID, null);
        return null;
    }

    private BigInteger computeDevicePublicKeyCommitment(int pin) {
        ByteBuffer buf = ByteBuffer.allocate(13);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getDeviceCommitment, 0, 0, 0, 0, 4});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for getDeviceCommitment: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getDeviceCommitment: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
            	System.out.println("And this is the output: " + Arrays.toString(response.getData()));
            	System.out.println("Or this bigInt: " + new BigInteger(1, response.getData()));
                return new BigInteger(1, response.getData());
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BigInteger getScopeExclusiveCommitment(int pin, URI scope){
        byte[] uri = this.uriToByteArr(scope);
        ByteBuffer buf = ByteBuffer.allocate(13+uri.length);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getScopeExclusiveCommitment, 0, 0, 0});
        buf.put(this.intLengthToShortByteArr(4+uri.length));
        buf.put(this.pinToByteArr(pin));
        buf.put(uri);
        buf.put(new byte[]{0,0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for getScopeExclusiveCommitment: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getScopeExclusiveCommitment: "+response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return new BigInteger(1, response.getData());
            }else{
                throw new RuntimeException("Failed scope exclusive Commitment. Card answered: " + response);
            }
        } catch (CardException e) {
            throw new RuntimeException("getScopeExclusiveCommitment crashed.", e);
        }
    }   

    @Override
    public ZkProofResponse finalizeZkProof(int pin,
            byte[] challengeHashPreimage, Set<URI> credentialIDs,
            Set<URI> scopeExclusivePseudonyms, byte[] nonceCommitment) {
        byte[] data = new byte[4+1+1+16+challengeHashPreimage.length]; //pin, prooverID, d which is the number of proofs, proofsession and h
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = 1; //TODO: ProoverID - Hardcoded for now
        data[5] = 1; //number of proofs - hardcoded to 1 for pilot.
        System.out.println("nonce length: " + nonceCommitment.length);
        System.out.println("data length: " + data.length);
        System.arraycopy(nonceCommitment, 0, data, 6, 16);        
        System.arraycopy(challengeHashPreimage, 0, data, 4+1+1+16, challengeHashPreimage.length);
        
        ByteBuffer buf = ByteBuffer.allocate(7 + data.length);        
        buf.put(new byte[]{(byte) this.ABC4TRUSTCMD, this.startResponses, 0, 0, 0});
        buf.put(this.intLengthToShortByteArr(data.length));
        buf.put(data);
        buf.position(0);
        if(printInput)
    		System.out.println("Input for startResponses: " + Arrays.toString(buf.array()));
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from startResponses: "+response );
            System.out.println("And this is the output: " + Arrays.toString(response.getData()));
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return null;
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }

        ZkProofResponse zkpr = new ZkProofResponse();

        zkpr.responseForDeviceSecret = this.computeDevicePublicKeyResponse(pin);

        //For Get issuance response
        for(URI uri : credentialIDs){
            byte credID = this.getCredentialIDFromUri(pin, uri);
            byte[] credInfo = readCredential(pin, credID);
            byte status = credInfo[5];
            String command = "getIssuanceResponse";
            byte issueOrPresent = this.getIssuanceResponse;
            if(status >= 2){
            	System.out.println("Presentation. Status: " + status);
            	//credential has already been issued, so we want to present response.
            	command = "getPresentationResponse";
            	issueOrPresent = this.getPresentationResponse;
            }
            buf = ByteBuffer.allocate(14);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, issueOrPresent, 0, 0, 0 ,0, 5});
            buf.put(this.pinToByteArr(pin));
            buf.put(credID);
            buf.put(new byte[]{0, 0});
            buf.position(0);
            try {
            	if(printInput)
            		System.out.println("Input for "+command+": " + Arrays.toString(buf.array()));
                ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
                System.out.println("Response from "+command+": " + response);
                if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                    return null;
                }
                System.out.println("data returned: size: "+response.getData().length+" value: " + Arrays.toString(response.getData()));
                byte[] zx = new byte[response.getNr()/2];
                byte[] zv = new byte[response.getNr()/2];
                System.arraycopy(response.getData(), 0, zx, 0, zx.length);
                System.arraycopy(response.getData(), zx.length, zv, 0, zv.length);
                System.out.println("zx: " + Arrays.toString(zx));
                System.out.println("zv: " + Arrays.toString(zv));
                zkpr.responseForCourses.put(uri, new BigInteger(1, zv));
                zkpr.responseForDeviceSecret = new BigInteger(1, zx);
            } catch (CardException e) {
                e.printStackTrace();
                return null;
            }
        }

        return zkpr;
    }

    private BigInteger computeDevicePublicKeyResponse(int pin) {
        ByteBuffer buf = ByteBuffer.allocate(13);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getDeviceResponse, 0, 0, 0, 0, 4});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for getDeviceResponse: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getDeviceResponse: " + response);
            System.out.println("And this is the output: " + Arrays.toString(response.getData()));
            System.out.println("which gives this BigInteger: " + new BigInteger(1, response.getData()));
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return new BigInteger(1, response.getData());
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    Map<URI, BigInteger> cachedCredentialFragment = new HashMap<URI, BigInteger>();
    @Override
    public BigInteger computeCredentialFragment(int pin, URI credentialId) {
        //fragment is equal to the public key of a credential
        if(cachedCredentialFragment.containsKey(credentialId)) {
            BigInteger cached = cachedCredentialFragment.get(credentialId);
            System.out.println("Cached getCredentialPublicKey: " + credentialId + " - " + cached);
            return cached;
        }
        int credID = this.getCredentialIDFromUri(pin, credentialId);
        ByteBuffer buf = ByteBuffer.allocate(14);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.getCredentialPublicKey, 0, 0, 0, 0, 5});
        buf.put(this.pinToByteArr(pin));
        buf.put((byte)credID);
        buf.put(new byte[]{0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for getCredentialPublicKey: " + credentialId + " : " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from getCredentialPublicKey (fragment): " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
            	System.out.println("And this is the output: " + Arrays.toString(response.getData()));
            	BigInteger credentialFragment = new BigInteger(1, response.getData());
            	System.out.println("which gives this BigInteger:  " + credentialFragment);
            	cachedCredentialFragment.put(credentialId, credentialFragment);
                return credentialFragment;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean credentialExists(int pin, URI credentialUri) {
    	byte credentialID;
    	try{
    		credentialID = this.getCredentialIDFromUri(pin, credentialUri);
    	}catch(Exception e){
    		return false;
    	}
    	byte[] credInfo = this.readCredential(pin, credentialID);
    	return credInfo != null;    	
    }
    
    @Override
	public SmartcardStatusCode storeCredential(int pin, URI credentialId, Credential cred, CredentialSerializer serializer){    	
    	//this.detectMaxBlobSize(pin);
    	byte[] credBytes = serializer.serializeCredential(cred);    	    
    	System.out.println("CredBytes length: " + credBytes.length);
    	int nextCredBlobUri = 1;
    	SmartcardStatusCode returnCode = SmartcardStatusCode.OK;

		int bytesLeft = credBytes.length;    
		int i = 0;
		boolean done = false;
		while(!done){
			SmartcardBlob blob = new SmartcardBlob();	
			if(bytesLeft > MAX_BLOB_BYTES){
				blob.blob = new byte[MAX_BLOB_BYTES];
				bytesLeft -= MAX_BLOB_BYTES;
				System.arraycopy(credBytes, i*MAX_BLOB_BYTES, blob.blob, 0, MAX_BLOB_BYTES);
			}else{
				blob.blob = new byte[bytesLeft];
				System.arraycopy(credBytes, i*MAX_BLOB_BYTES, blob.blob, 0, bytesLeft);
				done = true; //We know we are done as we put the last bytes in the blob.
			}
			URI credUri = URI.create(credentialId.toASCIIString()+"_"+nextCredBlobUri++);  
			System.out.println("storing a blob of size: " + blob.blob.length + " with uri: " + credUri.toASCIIString()); 
			returnCode = storeBlob(pin, credUri, blob);
			if(returnCode != SmartcardStatusCode.OK){
				return returnCode;
			}
			i++;
		}    		
    	return returnCode;
    }
    
    @Override
	public SmartcardStatusCode storePseudonym(int pin, URI pseudonymId, PseudonymWithMetadata pseudo, PseudonymSerializer serializer){
    	//this.detectMaxBlobSize(pin);
    	byte[] pseudoBytes = serializer.serializePseudonym(pseudo);
    	System.out.println("PseudoBytes length: " + pseudoBytes.length);
    	int nextPseudoBlobUri = 1;
    	SmartcardStatusCode returnCode = SmartcardStatusCode.OK;

		int bytesLeft = pseudoBytes.length;    
		int i = 0;
		boolean done = false;
		while(!done){
			SmartcardBlob blob = new SmartcardBlob();	
			if(bytesLeft > MAX_BLOB_BYTES){
				blob.blob = new byte[MAX_BLOB_BYTES];
				bytesLeft -= MAX_BLOB_BYTES;
				System.arraycopy(pseudoBytes, i*MAX_BLOB_BYTES, blob.blob, 0, MAX_BLOB_BYTES);
			}else{
				blob.blob = new byte[bytesLeft];
				System.arraycopy(pseudoBytes, i*MAX_BLOB_BYTES, blob.blob, 0, bytesLeft);
				done = true; //We know we are done as we put the last bytes in the blob.
			}
			URI credUri = URI.create(pseudonymId.toASCIIString()+"_"+nextPseudoBlobUri++);  
			System.out.println("storing a blob of size: " + blob.blob.length + " with uri: " + credUri.toASCIIString()); 
			returnCode = storeBlob(pin, credUri, blob);
			if(returnCode != SmartcardStatusCode.OK){
				return returnCode;
			}
			i++;
		}    		
    	return returnCode;
    }
	
    @Override
	public PseudonymWithMetadata getPseudonym(int pin, URI pseudonymUID, PseudonymSerializer serializer){
		ByteArrayOutputStream accumulatedPseuBytes = new ByteArrayOutputStream();
		return getPseudonym(pin, pseudonymUID, 1, accumulatedPseuBytes, serializer);
	}
	
	private PseudonymWithMetadata getPseudonym(int pin, URI pseudonymUID, int nextPseuBlobUriId, 
			ByteArrayOutputStream accumulatedPseuBytes, PseudonymSerializer serializer){
		System.out.println("Accumulated this many bytes: "  + accumulatedPseuBytes.size());
    	URI nextPseuBlobUri = URI.create(pseudonymUID.toASCIIString()+"_"+nextPseuBlobUriId);
    	System.out.println("getting this uri: " + nextPseuBlobUri.toASCIIString());
    	SmartcardBlob scBlob = this.getBlob(pin, nextPseuBlobUri);
    	if(scBlob == null){
    		return serializer.unserializePseudonym(accumulatedPseuBytes.toByteArray(), pseudonymUID);
    	}
    	byte[] blob = scBlob.blob;
    	accumulatedPseuBytes.write(blob, 0, blob.length);    	
    	if(blob.length < MAX_BLOB_BYTES){
    		return serializer.unserializePseudonym(accumulatedPseuBytes.toByteArray(), pseudonymUID);
    	}else{
    		//next round
    		return getPseudonym(pin, pseudonymUID, nextPseuBlobUriId+1, accumulatedPseuBytes, serializer);
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
		//this.detectMaxBlobSize(pin);
    	ByteArrayOutputStream accumulatedCredBytes = new ByteArrayOutputStream();
    	return getCredential(pin, credentialId, 1, accumulatedCredBytes, serializer);
    }
    
    private Credential getCredential(int pin, URI credentialId, int nextCredBlobUriId, 
    		ByteArrayOutputStream accumulatedCredBytes, CredentialSerializer serializer){
    	System.out.println("Accumulated this many bytes: "  + accumulatedCredBytes.size());
    	URI nextCredBlobUri = URI.create(credentialId.toASCIIString()+"_"+nextCredBlobUriId);
    	System.out.println("getting this uri: " + nextCredBlobUri.toASCIIString());
    	SmartcardBlob scBlob = this.getBlob(pin, nextCredBlobUri);
    	if(scBlob == null){    		
    		return serializer.unserializeCredential(accumulatedCredBytes.toByteArray(), credentialId, this.getDeviceURI(pin));
    	}
    	byte[] blob = scBlob.blob;
    	accumulatedCredBytes.write(blob, 0, blob.length);
    	if(blob.length < MAX_BLOB_BYTES){
    		//return new CredentialSerializerGzipXml().unserializeCredential(accumulatedCredBytes.toByteArray());
    		return serializer.unserializeCredential(accumulatedCredBytes.toByteArray(), credentialId, this.getDeviceURI(pin));
    	}else{
    		//next round
    		return getCredential(pin, credentialId, nextCredBlobUriId+1, accumulatedCredBytes, serializer);
    	}
    }

    @Override
    public SmartcardStatusCode allocateCredential(int pin, URI credentialId, URI issuerParameters) {
        byte[] credIdBytes = null;
        credIdBytes = this.uriToByteArr(credentialId);
        if(credIdBytes.length > 199){
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }

        byte issuerID = this.getIssuerIDFromUri(pin, issuerParameters);        
        byte newCredentialID = this.getNewCredentialID(pin);
        if(newCredentialID == (byte)-1){
            return SmartcardStatusCode.INSUFFICIENT_STORAGE;
        }
        ByteBuffer buf = ByteBuffer.allocate(11);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setCredential, 0, 0, 6});
        buf.put(this.pinToByteArr(pin));
        buf.put(newCredentialID);
        buf.put(issuerID);
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for setCredential: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setCredential: " + response);
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return this.evaluateStatus(response);
            }
        } catch (CardException e) {
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }

        //Then store the mapping from credentialURI to credentialID:
        SmartcardStatusCode code = this.storeCredentialUriAndID(pin, credentialId, newCredentialID);
        if(code != SmartcardStatusCode.OK){
        	System.err.println("Credential stored correctly on card, but storing the Uri/ID failed with code: " + code);
        	return code;
        }

        return SmartcardStatusCode.OK;
    }

    @Override
    public SmartcardStatusCode deleteCredential(int pin, URI credentialId) {
        byte credID = this.getCredentialIDFromUri(pin, credentialId);
        ByteBuffer buf = ByteBuffer.allocate(10);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.removeCredential, 0, 0, 5});
        buf.put(this.pinToByteArr(pin));
        buf.put(credID);
        buf.position(0);        
        try {
        	System.out.println("Removing credential with uri: " + credentialId);
        	this.deleteBlob(pin, credentialId);
        	if(credentialId.toString().startsWith(UProveCryptoEngineUserImpl.UProveCredential)){
        		URI reloadURI = URI.create(credentialId.toString()+ReloadStorageManager.URI_POSTFIX);
        		if(reloadURI.toString().contains(":") && !reloadURI.toString().contains("_")){
        			reloadURI = URI.create(reloadURI.toString().replaceAll(":", "_")); //change all ':' to '_'
                }
        		this.deleteBlob(pin, reloadURI);
        		System.out.println("deleted the reload blob of the credential: " + reloadURI);
        	}
            this.removeCredentialUri(pin, credentialId);
            if(printInput)
        		System.out.println("Input for removeCredential: " + Arrays.toString(buf.array()));
            System.out.println("Trying to remove on-board credential with ID="+credID);
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("response from RemoveCredential: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    @Override
    public boolean smartcardPresent() {
        try {
            return this.terminal.isCardPresent();
        } catch (CardException ex) {
            return false;
        }
    }

    @Override
    public int init(int newPin, SystemParameters pseuParams,
            RSAKeyPair rootKey, short deviceId) {
        if(this.wasInit()){
            return -1;
        }
        try {

            byte[] deviceID = ByteBuffer.allocate(2).putShort(deviceId).array();
            this.setAuthenticationKey(rootKey.getN(), 0, null);
            byte[] deviceKeySize = this.intLengthToShortByteArr(pseuParams.deviceSecretSizeBytes);
            byte[] idAndDeviceKeySize = new byte[]{deviceID[0], deviceID[1], deviceKeySize[0], deviceKeySize[1]};
            ByteBuffer buf = ByteBuffer.allocate(13);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.initializeDevice, 0, 0, 0, 0, 4});
            buf.put(idAndDeviceKeySize);
            buf.put(new byte[]{0,0});
            buf.position(0);
            if(printInput)
        		System.out.println("Input to initialize device: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return -1;
            }
            byte[] pinAndPuk = SmartcardCrypto.decrypt(response.getData(), rootKey);
            byte[] pin = new byte[4];
            byte[] puk = new byte[8];
            System.arraycopy(pinAndPuk, 0, pin, 0, 4);
            System.arraycopy(pinAndPuk, 4, puk, 0, 8);
            String ipin = "", ipuk = "";
            for(int i = 0; i < 4; i++){
                ipin += (char)(pin[i] & 0xFF);
            }
            for(int i = 0; i < 8; i++){
                ipuk += (char)(puk[i] & 0xFF);
            }
            if(this.changePin(Integer.parseInt(ipin), newPin) != SmartcardStatusCode.OK){
                System.out.println("Could not change pin.");
                return -1;
            }            

            System.out.println("Now initializing group stuff");
            int mode = this.getMode();

            if(this.setGroupComponent(mode, pseuParams.p.toByteArray(), 0, 0, null) != SmartcardStatusCode.OK){
                return -1;
            }
            if(this.setGroupComponent(mode, pseuParams.subgroupOrder.toByteArray(), 0, 1, null) != SmartcardStatusCode.OK){
                return -1;
            }
            BigInteger f = pseuParams.p.subtract(BigInteger.ONE).divide(pseuParams.subgroupOrder); //cofactor
            this.setGroupComponent(mode, f.toByteArray(), 0, 2, null);

            //then add a generator of the subgroup q
            if(this.setGenerator(mode, pseuParams.g.toByteArray(), 0, 1, null) != SmartcardStatusCode.OK){
                return -1;
            }

            //set prover
            byte[] data = new byte[5+MAX_CREDENTIALS+1];
            data[0] = 1; //id 1
            int ksize = pseuParams.zkChallengeSizeBytes*2+pseuParams.zkStatisticalHidingSizeBytes;
            byte[] ksize_bytes = this.intLengthToShortByteArr(ksize);
            data[1] = ksize_bytes[0];
            data[2] = ksize_bytes[1]; // as large as the subgroup order is -1 to prevent overflow.
            int csize = pseuParams.zkChallengeSizeBytes;
            byte[] csize_bytes = this.intLengthToShortByteArr(csize);
            data[3] = csize_bytes[0];
            data[4] = csize_bytes[1]; // challenge size: 256 bit = 32 bytes (as per default in SystemParameters)
            for(int i = 0; i <= MAX_CREDENTIALS; i++){
            	//0 means it accepts both credentials and scope-exclusive stuff.
                //1,2,3,... means it accepts credentials with id 1,2,3,...
            	data[i+5] = (byte)i;            	
            }            
            buf = ByteBuffer.allocate(5+data.length);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setProver, 0, 0, (byte)data.length});
            buf.put(data);
            buf.position(0);
            System.out.println("Input to prover: " + Arrays.toString(buf.array()));
            response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setProver: " + response);
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return -1;
            }

            //After init, one should call setIssuer which creates a group and counter.
            return Integer.parseInt(ipuk);
        } catch (CardException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int pinTrialsLeft() {
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.pinTrialsLeft, 0, 0, 1));
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData()[0];
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int pukTrialsLeft() {
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.pukTrialsLeft, 0, 0, 1));
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData()[0];
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public SmartcardStatusCode resetPinWithPuk(int puk, int newPin) {
        byte[] data = new byte[8+4];
        System.arraycopy(this.pukToByteArr(puk), 0, data, 0, 8);
        System.arraycopy(this.pinToByteArr(newPin), 0, data, 8, 4);
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.resetPin, 0, 0, data));
            System.out.println("response from resetPinWithPuk: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }


    Map<URI, SmartcardBlob> blobCache = new HashMap<URI, SmartcardBlob>();
    @Override
    public SmartcardStatusCode storeBlob(int pin, URI uri, SmartcardBlob blob) {
    	//this.resetCard();
    	
        String[] forbiddenChars = new String[]{"\u0167", ":", "*", "?", "<", ">", " ", "|"};
        if(uri.toString().contains(":") && !uri.toString().contains("_")){
        	uri = URI.create(uri.toString().replaceAll(":", "_")); //change all ':' to '_'
        }else{
	        for(int i = 0; i < forbiddenChars.length; i++){        	
	        	if(uri.toString().contains(forbiddenChars[i])){        		
	        		throw new RuntimeException("Cannot store a blob under a URI containing the following char: " +forbiddenChars[i]);
	        	}
	        }
        }
        byte[] uriBytes = null;
        uriBytes = this.uriToByteArr(uri);
        if(uriBytes.length > 199){
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        
        // BLOB CACHE!
        blobCache.put(uri, blob);
        blobUrisCache.add(uri);
        
        //first put data from blob followed by the STORE BLOB command
        this.putData(blob.blob);

        byte[] data = new byte[4+uriBytes.length];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        System.arraycopy(uriBytes, 0, data, 4, uriBytes.length);
        ByteBuffer buf = ByteBuffer.allocate(9+uriBytes.length);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.storeBlob, 0, 0, (byte)data.length});
        buf.put(data);
        buf.position(0);
        try {        	
        	if(printInput)
        		System.out.println("Input for storeBlob: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from storeBlob: " + response);
            if((response.getSW1() != STATUS_OK) && (response.getSW1() != STATUS_BAD_PIN)){
            	throw new InsufficientStorageException("Could not store blob. Response from card: " + response);
            }
            return this.evaluateStatus(response);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SmartcardStatusCode deleteBlob(int pin, URI uri) {
        byte[] uriBytes = null;
        uriBytes = this.uriToByteArr(uri);
        if(uriBytes.length > 199){
            return SmartcardStatusCode.REQUEST_URI_TOO_LONG;
        }
        // BLOB CACHE!
        blobCache.remove(uri);
        blobUrisCache.remove(uri);
        
        byte[] data = new byte[4+uriBytes.length];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        System.arraycopy(uriBytes, 0, data, 4, uriBytes.length);
        ByteBuffer buf = ByteBuffer.allocate(9+uriBytes.length);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.removeBlob, 0, 0, (byte)data.length});
        buf.put(data);
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for removeBlob: "+Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from removeBlob: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Map<URI, SmartcardBlob> getBlobs(int pin) {
    	Set<URI> uris = this.getBlobUris(pin);
        Map<URI, SmartcardBlob> result = new HashMap<URI, SmartcardBlob>();
        for(URI uri : uris){
            result.put(uri, this.getBlob(pin, uri));
        }
        return result;
    }

    private boolean loadedBlobUris = false;
    private Set<URI> blobUrisCache = new HashSet<URI>();
    @Override
    public Set<URI> getBlobUris(int pin) {
        //TODO: Works only if the total length of URIs is less than 2048-#URIs-2
        Set<URI> uris = new HashSet<URI>();
    	if(loadedBlobUris){
    		System.out.println("Returning the cached blob uris: "+blobUrisCache);
    		return blobUrisCache;
    	}
        byte nread = 0;
        int eternalLoopPreventer = 0;
        while(true){
        	byte[] readInfo = this.getBlobUrisHelper(pin, uris, nread);
        	nread = readInfo[0];
        	if(readInfo[1] == 0){
        		loadedBlobUris = true;
        		blobUrisCache = uris;
        		return uris;
        	}
        	eternalLoopPreventer++;
        	if(eternalLoopPreventer > 1000){ //meaning if the card can store more than 1MB, we're fucked, but it cant...
        		return null;
        	}
        }
    }

    /**
     * Returns the number of uris read, no of uris remaining to be read.
     */
    private byte[] getBlobUrisHelper(int pin, Set<URI> uris, byte nread){
        ByteBuffer buf = ByteBuffer.allocate(14);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.listBlobs, 0, 0, 0, 0, 5});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{nread, 0, 0}); //first arg is how many URIs we read so far.
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for listBlobs: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from listBlobs: " + response);
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return null;
            }
            byte[] data = response.getData();
            System.out.println("data: " + Arrays.toString(data));
            int index = 0;
            while(true){
                if((index+2) == data.length){
                    //at the end, so the last two bytes is the updated number of read URIs and the number of unread URIs
                    //					System.out.println("data.length: " + data.length);
                    //					System.out.println("index: " + index);
                	nread = data[index];
                	byte unread = data[index+1];
                	System.out.println("nread: " + nread);
                	System.out.println("unread: " + unread);
                    return new byte[]{nread, unread};                	                    
                }else{
                    byte uriSize = data[index];
                    byte[] uri = new byte[uriSize];
                    System.arraycopy(data, index+1, uri, 0, uriSize);
                    uris.add(this.byteArrToUri(uri));
                    index += uriSize+1;
                }
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SmartcardBlob getBlob(int pin, URI uri) {
    	//this.resetCard();
    	
    	uri = URI.create(uri.toString().replaceAll(":", "_"));
        byte[] uriBytes = this.uriToByteArr(uri);
        if(uriBytes.length > 199){
            throw new RuntimeException("URI is too long. Cannot have been stored on smartcard.");
        }
        
        // BLOB CACHE!
        if(blobCache.containsKey(uri)) {
            SmartcardBlob cached = blobCache.get(uri);
            System.out.println("Cached readBlob: " + uri + " : " + cached.blob.length); // Arrays.toString(cached.blob));
            return cached;
        }
        ByteBuffer buf = ByteBuffer.allocate(9+4+uriBytes.length);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readBlob, 0, 0, 0});
        buf.put(this.intLengthToShortByteArr(uriBytes.length+4));
        buf.put(this.pinToByteArr(pin));
        buf.put(uriBytes);
        buf.put(new byte[]{0, 0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for readBlob: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readBlob: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                SmartcardBlob blob = new SmartcardBlob();
                blob.blob = response.getData();
                
                // BLOB CACHE!
                blobCache.put(uri, blob);
                return blob;
            }else{
                return null;
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SmartcardStatusCode changePin(int pin, int newPin) {
        byte[] data = new byte[8];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        System.arraycopy(this.pinToByteArr(newPin), 0, data, 4, 4);
        try {
        	ByteBuffer buf = ByteBuffer.allocate(13);
        	buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.changePin, 0, 0, 8});
        	buf.put(data);
        	buf.position(0);
        	if(printInput)
        		System.out.println("Input for changePin: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from changePin: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    @Override
    public Set<URI> listCredentialsUris(int pin) {
    	Set<URI> credUris = new HashSet<URI>();
        Set<URI> blobs = this.getBlobUris(pin);
        for(URI uri: blobs){
        	String uriString = uri.toString();
        	if((uriString.startsWith(IdemixCryptoEngineUserImpl.IdmxCredential) || 
        			uriString.startsWith(UProveCryptoEngineUserImpl.UProveCredential)) &&
        			uriString.endsWith("_1")){
        		URI credURI = URI.create(uri.toString().substring(0, uri.toString().length()-2));
        		System.out.println("listCredentialUris - added a cred uri: " + credURI);
        		credUris.add(credURI);
        	}
        }                
        return credUris;
    }

    @SuppressWarnings("unused")
	private List<Byte> listCounters(int pin) {
        ByteBuffer buf = ByteBuffer.allocate(10);
        buf.put(new byte[]{(byte) this.ABC4TRUSTCMD, this.listCounters, 0, 0, 4});
        buf.put(this.pinToByteArr(pin));
        buf.put(new byte[]{0});
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for listCounters: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from listCounters: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                List<Byte> counters = new ArrayList<Byte>();
                byte[] counterIDs = response.getData();
                for (byte counterID : counterIDs) {
                    counters.add(counterID);
                }
                return counters;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Course> listCourses(int pin) {
        Set<TrustedIssuerParameters> issuers = this.getIssuerParametersList(pin);
        Set<Course> courses = new HashSet<Course>();
        for(TrustedIssuerParameters params: issuers){
            courses.add(params.course);
        }
        return courses;
    }

    @Override
    public Course getCourse(int pin, URI issuerUri) {
        byte issuerID = this.getIssuerIDFromUri(pin, issuerUri);
        byte[] issuerInfo = this.readIssuer(pin, issuerID);
        byte counterID = issuerInfo[4];
        if (counterID == 0) {
        	return null;
        }
        byte[] counterInfo = this.readCounter(pin, counterID);
        if(counterInfo == null){
            return null;
        }
        byte threshold = counterInfo[2];
        byte keyID = counterInfo[0];
        return new Course(counterID, issuerUri, threshold, keyID);
    }

    @Override
    public TrustedIssuerParameters getIssuerParameters(int pin, URI paramsUri) {
    	CryptoEngine engine;
    	if(paramsUri.toString().endsWith("uprove")){
    		engine = CryptoEngine.UPROVE;
    	}else{
    		engine = CryptoEngine.IDEMIX;
    	}
        int issuerID = this.getIssuerIDFromUri(pin, paramsUri);
        byte[] issuerData = this.readIssuer(pin, issuerID);
        byte groupID = issuerData[0];
        byte genID1 = issuerData[1];
        byte genID2 = issuerData[2];
        byte counterID = issuerData[4];
        GroupParameters groupParams;
        BigInteger p = this.getGroupComponent(pin, groupID, 0);
        if(engine == CryptoEngine.IDEMIX){
        	BigInteger R0 = this.getGenerator(pin, groupID, genID1);
        	BigInteger S = this.getGenerator(pin, groupID, genID2);
        	groupParams = new CredentialBases(R0, S, p);
        }else{
        	BigInteger g = this.getGenerator(pin, groupID, genID1);
        	BigInteger q = this.getGroupComponent(pin, groupID, 1);
        	groupParams = new UProveParams(g, p, q);
        }

        if (counterID == 0) {
        	return new TrustedIssuerParameters(paramsUri, groupParams);
        }
        byte[] counterInfo = this.readCounter(pin, counterID);
        if(counterInfo == null){
            return new TrustedIssuerParameters(paramsUri, groupParams);
        }else{
            int keyID = counterInfo[0];
            int index = counterInfo[1];
            int threshold = counterInfo[2];
            byte[] cursor = new byte[4];
            System.arraycopy(counterInfo, 3, cursor, 0, 4);
            int lectureID = ByteBuffer.wrap(cursor).getInt();
            TrustedIssuerParameters tip = new TrustedIssuerParameters(counterID, paramsUri, groupParams, threshold, keyID);
            if(index > 0) tip.course.activate();
            for(int i = 1; i < index; i++){
            	tip.course.updateLectureId(i); //update courses lecture count by all but one
            }
            tip.course.updateLectureId(lectureID); //update to the real lecture ID
            return tip;
        }
    }

    /**
     * 
     * @param pin
     * @param counterID
     * @return the 7-byte stream keyID || index || threshold || cursor(4 bytes).
     */
    private byte[] readCounter(int pin, int counterID){
        byte[] data = new byte[5];
        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
        data[4] = (byte)counterID;
        ByteBuffer buf = ByteBuffer.allocate(11);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.readCounter, 0, 0, 5});
        buf.put(data);
        buf.put((byte)7);
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for readCounter: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from readCounter: " + response);
            System.out.println("With data: " + Arrays.toString(response.getData()));
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                return response.getData();
            }else{
                return null;
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public int getCounterValue(int pin, URI issuerId){
    	TrustedIssuerParameters tip = this.getIssuerParameters(pin, issuerId);
    	return tip.course.getLectureCount();
    }

    @Override
    public Set<TrustedIssuerParameters> getIssuerParametersList(int pin) {
//        ByteBuffer buf = ByteBuffer.allocate(10);
//        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.listIssuers, 0, 0, 4});
//        buf.put(this.pinToByteArr(pin));
//        buf.put((byte)0);
//        buf.position(0);
//        try {
//            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
//            System.out.println("Response from listIssuers: " + response);
//            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
//                return null;
//            }
//            byte[] issuerIDs = response.getData();
//            Set<TrustedIssuerParameters> issuers = new HashSet<TrustedIssuerParameters>();
//            for(int i = 0; i < response.getNr(); i++){
//                issuers.add(this.getIssuerParameters(pin, this.getIssuerUriFromID(pin, issuerIDs[i])));
//            }
//            return issuers;
//        } catch (CardException e) {
//            e.printStackTrace();
//            return null;
//        }
    	throw new NotImplementedException("This method is unused so far, and needs a few changes to the interface before it works. If needed, it can be fixed");
    }

    @Override
    public SmartcardBackup backupAttendanceData(int pin, String password) {
        SmartcardBackup backup = new SmartcardBackup();

        ByteBuffer buf = ByteBuffer.allocate(21);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.backupDevice, 0, 0, 0, 0, 0x0C});
        buf.put(this.pinToByteArr(pin));
        byte[] password_bytes = Utils.passwordToByteArr(password);
        if(password_bytes == null){
            return null;
        }
        buf.put(password_bytes);
        buf.put(new byte[]{0, 0});
        buf.position(0);

        try {
            //First we backup the device-specific stuff. pin, puk and deviceSecret is encrypted and
            //deviceID and deviceURI is stored in plain text
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from backupDevice: " + response);
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return null;
            }
            backup.macDevice = response.getData();
            backup.deviceID = this.getDeviceID(pin);
            backup.deviceUri = this.getDeviceURI(pin);

            //Then we backup the counters. counterID, index and cursor is encrypted, but
            //the threshold and keyID is hidden. Thus we need to save those along with the counterID
            //in cleartext. We assume that the key is put on the card in the initialization phase.
            buf = ByteBuffer.allocate(18);
            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.backupCounters, 0, 0, 0x0C});
            buf.put(this.pinToByteArr(pin));
            buf.put(password_bytes);
            buf.put((byte)0);
            buf.position(0);
            response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from backupCounters: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
            	backup.macCounters = response.getData();
            }                        

            List<Byte> credentials = this.listCredentialIDs(pin);
            for(Byte credID : credentials){
            	byte[] credInfo = this.readCredential(pin, credID);
            	byte status = credInfo[5];
            	System.out.println("backing up credential: "+this.getCredentialUriFromID(pin, credID)+" with status: " + status);
            	if(status != 2){
            		//Credential is either just created, and thus not backed up, OR ready for presenting, thus not backupable.
            		continue;
            	}
                buf = ByteBuffer.allocate(22);
                buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, (byte) this.backupCredential, 0, 0, 0, 0, 0x0D});
                buf.put(this.pinToByteArr(pin));
                buf.put(password_bytes);
                buf.put(credID);
                buf.put(new byte[]{0, 0});
                buf.position(0);
                response = this.transmitCommand(new CommandAPDU(buf));
                System.out.println("Response from backupCredentials: " + response);
                if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                    return null;
                }
                backup.macCredentials.put(credID, response.getData());
            }

            //finally we backup the blobstore
            backup.blobstore = this.getBlobs(pin);

        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }

        return backup;
    }

    @Override
    public SmartcardStatusCode restoreAttendanceData(int pin, String password, SmartcardBackup backup) {
        //restore device URI
        SmartcardBlob deviceUriBlob = new SmartcardBlob();
        try {
			deviceUriBlob.blob = backup.deviceUri.toASCIIString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			return SmartcardStatusCode.BAD_REQUEST;
		}
        this.storeBlob(pin, Smartcard.device_name, deviceUriBlob);
           
        try {        	
        	ByteBuffer buf;
            if(backup.macCounters.length != 0){
            	buf = ByteBuffer.allocate(17);
	            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.restoreCounters, 0, 0, 0x0C});
	            buf.put(this.pinToByteArr(pin));
	            buf.put(Utils.passwordToByteArr(password));
	            buf.position(0);
	            this.putData(backup.macCounters); //put the encrypted data in the buffer
	            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
	            System.out.println("Response from restoreCounters: " + response);
	            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
	                return this.evaluateStatus(response);
	            }
            }

            for(byte credID : backup.macCredentials.keySet()){
                buf = ByteBuffer.allocate(17);
                buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, (byte) this.restoreCredential, 0, 0, 0x0C});
                buf.put(this.pinToByteArr(pin));
                buf.put(Utils.passwordToByteArr(password));
                buf.position(0);
                if(printInput)
            		System.out.println("Input for for restoreCredential: " + Arrays.toString(buf.array()));
                this.putData(backup.macCredentials.get(credID));//put the encrypted data in the buffer
                ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
                System.out.println("Response from restoreCredential: " + response);
                if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                    return this.evaluateStatus(response);
                }
            }

            //restoring the blob-store, but remove the current blob-store first (in particular remove the pseudonym that no longer works.)
            Set<URI> blobs = this.getBlobUris(pin);
            for(URI uri : blobs){
            	this.deleteBlob(pin, uri);
            }
            
            for(URI uri : backup.blobstore.keySet()){
                this.storeBlob(pin, uri, backup.blobstore.get(uri));
            }

            //finally restore the device - this means also restoring the pin, so using the provided pin no longer works. 
            buf = ByteBuffer.allocate(17);
	        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.restoreDevice, 0, 0, 0x0C});
	        buf.put(this.pinToByteArr(pin));
	        buf.put(Utils.passwordToByteArr(password));
	        buf.position(0);        
            this.putData(backup.macDevice); //put the encrypted data in the buffer
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from restoreDevice: " + response);
            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                return this.evaluateStatus(response);
            }
            
        } catch (CardException e) {
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
        return SmartcardStatusCode.OK;
    }

    public List<Byte> listCredentialIDs(int pin) {
        ByteBuffer buf = ByteBuffer.allocate(10);
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.listCredentials, 0, 0, 4});
        buf.put(this.pinToByteArr(pin));
        buf.put((byte)0);
        buf.position(0);
        try {
        	if(printInput)
        		System.out.println("Input for listCredentials: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from listCredentials: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                List<Byte> credentialIDs = new ArrayList<Byte>();
                byte[] creds = response.getData();
                for (byte cred : creds) {
                    credentialIDs.add(cred);
                }
                return credentialIDs;
            }
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public SmartcardStatusCode deleteIssuer(int pin, URI issuerParameters, RSAKeyPair rootKey) {
    	if(this.getMode() != 1){
    		System.out.println("Can only use deleteIssuer in root mode");
            return SmartcardStatusCode.UNAUTHORIZED;
    	}
        byte issuerID = this.getIssuerIDFromUri(pin, issuerParameters);        
        try {
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(this.ABC4TRUSTCMD, this.removeIssuer, 0, 0, new byte[]{issuerID}));
            return this.evaluateStatus(response);
        } catch (CardException e) {
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    @Override
    public byte[] getNewNonceForSignature() {
        //Not used - unless we rename getChallenge to this method name, but that would be wrong I think.
        return this.getChallenge(16);
    }

    @Override
    public SmartcardStatusCode addIssuerParametersWithAttendanceCheck(RSAKeyPair rootKey, 
    		URI parametersUri, int keyIDForCounter, CredentialBases credBases,
            RSAVerificationKey courseKey, int minimumAttendance) {
        byte issuerID = this.getNewIssuerID(parametersUri);
        byte groupID = issuerID;
        byte genID1 = 1;
        byte genID2 = 2;
        byte numPres = 0; //unlimited presentations - limit not used in the pilot
        byte counterID = issuerID;
        ByteBuffer buf = ByteBuffer.allocate(11);
        //SET ISSUER(BYTE issuerID, groupID, genID1, genID2, numpres, counterID)
        byte[] data = new byte[]{issuerID, groupID, genID1, genID2, numPres, counterID};
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setIssuer, 0, 0, 6});
        buf.put(data);
        buf.position(0);

        try {
            //Before setting the issuer, we must create a group, generators as well as a counter
            int mode = this.getMode();
            this.setGroupComponent(mode, credBases.n.toByteArray(), groupID, 0, rootKey);
            this.setGenerator(mode, credBases.R0.toByteArray(), groupID, genID1, rootKey);
            this.setGenerator(mode, credBases.S.toByteArray(), groupID, genID2, rootKey);
            byte[] cursor = this.getNewCursor(0);

            //Create a new key with keyID that counter can use.
            this.setAuthenticationKey(courseKey.n, keyIDForCounter, rootKey);
            this.setCounter(counterID, keyIDForCounter, 0, minimumAttendance, cursor, rootKey);

            //prior to the actual command,if we are in working mode,
            //we have to authenticate the input data first.
            if(mode == 2){
            	System.out.println("Can only use addIssuerParameters in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;
            }
            System.out.println("Input to setIssuer: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setIssuer: " + response);
            if(evaluateStatus(response) == SmartcardStatusCode.OK){
//            	SmartcardStatusCode code = this.storeIssuerUriAndID(pin, parametersUri, issuerID);
//            	if(code != SmartcardStatusCode.OK){
//            		System.err.println("Could not store the issuerURI and ID on the card, but the issuer itself is still stored on the card. Returned code: " + code);
//            		return code;
//            	}
            }
            return this.evaluateStatus(response);
        } catch (CardException e) {
            //TODO: Error handling. Remove stuff again if something fails.
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }
    
    public SmartcardStatusCode addUProveIssuerParametersWithAttendanceCheck(RSAKeyPair rootKey, 
            URI parametersUri, int keyIDForCounter, UProveParams uProveParams,
            RSAVerificationKey courseKey, int minimumAttendance) {
        byte issuerID = this.getNewIssuerID(parametersUri);
        byte groupID = issuerID;
        byte genID1 = 1;
        byte genID2 = 0; //Not used in UProve, thus set to 0.
        byte numPres = 0; //unlimited presentations - limit not used in the pilot
        byte counterID = issuerID;
        ByteBuffer buf = ByteBuffer.allocate(11);
        //SET ISSUER(BYTE issuerID, groupID, genID1, genID2, numpres, counterID)
        byte[] data = new byte[]{issuerID, groupID, genID1, genID2, numPres, counterID};
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setIssuer, 0, 0, 6});
        buf.put(data);
        buf.position(0);

        try {
            //Before setting the issuer, we must create a group, generators as well as a counter
            int mode = this.getMode();
            this.setGroupComponent(mode, uProveParams.p.toByteArray(), groupID, 0, rootKey);
            this.setGroupComponent(mode, uProveParams.q.toByteArray(), groupID, 1, rootKey);
            this.setGroupComponent(mode, uProveParams.f.toByteArray(), groupID, 2, rootKey);
            System.out.println("p: " + uProveParams.p);
            System.out.println("q: " + uProveParams.q);
            System.out.println("g: " + uProveParams.g);
            System.out.println("f: " + uProveParams.f);
            this.setGenerator(mode, uProveParams.g.toByteArray(), groupID, genID1, rootKey);
            
            byte[] cursor = this.getNewCursor(0);

            //Create a new key with keyID that counter can use.
            this.setAuthenticationKey(courseKey.n, keyIDForCounter, rootKey);
            this.setCounter(counterID, keyIDForCounter, 0, minimumAttendance, cursor, rootKey);

            //prior to the actual command,if we are in working mode,
            //we have to authenticate the input data first.
            if(mode == 2){
            	System.out.println("Can only use addIssuerParameters in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;            }

            System.out.println("Input for setIssuer: " +Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setIssuer: " + response);
            if(evaluateStatus(response) == SmartcardStatusCode.OK){
//            	SmartcardStatusCode code = this.storeIssuerUriAndID(pin, parametersUri, issuerID);
//            	if(code != SmartcardStatusCode.OK){
//            		System.err.println("Could not store the issuerURI and ID on the card, but the issuer itself is still stored on the card. Returned code: " + code);
//            		return code;
//            	}
            }
            return this.evaluateStatus(response);
        } catch (CardException e) {
            //TODO: Error handling. Remove stuff again if something fails.
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    @Override
    public SmartcardStatusCode addIssuerParameters(RSAKeyPair rootKey,
            URI parametersUri, CredentialBases credBases) {
    	byte issuerID = this.getNewIssuerID(parametersUri);
        byte groupID = issuerID;
        byte genID1 = 1;//R0
        byte genID2 = 2;//S
        byte numPres = 0; //unlimited presentations - limit not used in the pilot
        byte counterID = 0; //no counter present
        ByteBuffer buf = ByteBuffer.allocate(11);
        //SET ISSUER(BYTE issuerID, groupID, genID1, genID2, numpres, counterID)
        byte[] data = new byte[]{issuerID, groupID, genID1, genID2, numPres, counterID};
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setIssuer, 0, 0, 6});
        buf.put(data);
        buf.position(0);

        try {
            //Before setting the issuer, we must create a group with generators. Idemix uses unknown order.
            int mode = this.getMode();
            SmartcardStatusCode status;
            status = this.setGroupComponent(mode, credBases.n.toByteArray(), groupID, 0, rootKey);
            if(status != SmartcardStatusCode.OK){
            	return status;
            }
            status = this.setGenerator(mode, credBases.R0.toByteArray(), groupID, genID1, rootKey);
            if(status != SmartcardStatusCode.OK){
            	return status;
            }
            status = this.setGenerator(mode, credBases.S.toByteArray(), groupID, genID2, rootKey);
            if(status != SmartcardStatusCode.OK){
            	return status;
            }

            //prior to the actual command, if we are in working mode,
            //we have to authenticate the input data first.
            if(mode == 2){
            	System.out.println("Can only use addIssuerParameters in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;
            }

            System.out.println("Input for set Issuer: " +Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setIssuer: " + response);            
            return evaluateStatus(response);
        } catch (CardException e) {
            //TODO: Error handling. Remove stuff again if something fails.
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }
    
    @Override
    public SmartcardStatusCode addUProveIssuerParameters(RSAKeyPair rootKey, 
    		URI parametersUri, UProveParams uProveParams){
    	byte issuerID = this.getNewIssuerID(parametersUri);
        byte groupID = issuerID;
        byte genID1 = 1;
        byte genID2 = 0;
        byte numPres = 0; //unlimited presentations - limit not used in the pilot
        byte counterID = 0; //no counter present
        ByteBuffer buf = ByteBuffer.allocate(11);
        //SET ISSUER(BYTE issuerID, groupID, genID1, genID2, numpres, counterID)
        byte[] data = new byte[]{issuerID, groupID, genID1, genID2, numPres, counterID};
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.setIssuer, 0, 0, 6});
        buf.put(data);
        buf.position(0);
        
        try {
            //Before setting the issuer, we must create a group, generators as well as a counter
            int mode = this.getMode();
            this.setGroupComponent(mode, uProveParams.p.toByteArray(), groupID, 0, rootKey);
            this.setGroupComponent(mode, uProveParams.q.toByteArray(), groupID, 1, rootKey);
            this.setGroupComponent(mode, uProveParams.f.toByteArray(), groupID, 2, rootKey);
            this.setGenerator(mode, uProveParams.g.toByteArray(), groupID, genID1, rootKey);

            //prior to the actual command, if we are in working mode,
            //we have to authenticate the input data first.
            if(mode == 2){
            	System.out.println("Can only use addIssuerParameters in root mode");
                return SmartcardStatusCode.UNAUTHORIZED;            }

            System.out.println("Input for setIssuer: " +Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from setIssuer: " + response);
            return this.evaluateStatus(response);
        } catch (CardException e) {
            //TODO: Error handling. Remove stuff again if something fails.
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    @Override
    public SmartcardStatusCode incrementCourseCounter(int pin, RSAKeyPair key,
            URI issuerId, int lectureId) {
    	//First check if the counter is enabled. //TODO!
//    	TrustedIssuerParameters tip = this.getIssuerParameters(pin, issuerId);
//    	if(!tip.course.isActivated()){
//    		if(!tip.course.updateLectureId(lectureId)){
//    			//Course not yet issued!
//    			return SmartcardStatusCode.NOT_MODIFIED;
//    		}
//    	}
    	
        //auth data should be counterID||cursor  , with cursor having the updated value.
        byte counterID = this.getIssuerIDFromUri(pin, issuerId); //IssuerID is the same as CounterID if the counter exists.
        byte keyID = this.readCounter(pin, counterID)[0];
        byte[] data = new byte[5];
        data[0] = counterID;
        System.arraycopy(this.getNewCursor(lectureId), 0, data, 1, 4);
        byte[] challenge = this.getNewNonceForSignature();
        byte[] sig = SmartcardCrypto.generateSignature(data, challenge, key, this.rand).sig;
        //sig = this.removeSignBit(sig);
        ByteBuffer buf = ByteBuffer.allocate(7+1+sig.length);
        byte[] bufferLength = ByteBuffer.allocate(2).putShort((short)(sig.length+1)).array();
        buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.incrementCounter, 0, 0, 0});
        buf.put(bufferLength);
        buf.put(keyID);
        buf.put(sig);
        buf.position(0);

        try {
        	byte[] counterInfo = this.readCounter(pin, counterID);
            byte index = counterInfo[1];
            if(printInput)
            	System.out.println("Input for incrementCounter: " + Arrays.toString(buf.array()));
            ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
            System.out.println("Response from incrementCounter: " + response);
            if(this.evaluateStatus(response) == SmartcardStatusCode.OK){
                //ensure that counter was increased or return not modified
            	byte[] newCounterInfo = this.readCounter(pin, counterID);
                int newIndex = newCounterInfo[1];                
                if(index == newIndex){
                    return SmartcardStatusCode.NOT_MODIFIED;
                }else{
                    return SmartcardStatusCode.OK;
                }
            }
            return this.evaluateStatus(response);
        } catch (CardException e) {
            e.printStackTrace();
            return SmartcardStatusCode.NOT_FOUND;
        }
    }

    /**
     * @param lectureID an ID for the lecture that is supposed to increase for each lecture.
     * @return 4 bytes describing the lectureID
     */
    private byte[] getNewCursor(int lectureID){
        return ByteBuffer.allocate(4).putInt(lectureID).array();
    }


    public SmartcardStatusCode issueCredentialOnSmartcard(int pin, byte credID){
    	byte[] credInfo = this.readCredential(pin, credID);
    	byte status = credInfo[5];
    	if(status == 0){    		
    		try {            	 
    			//Start commitments
    			byte[] data = new byte[5];
    			System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
    			data[4] = 1; //ProverID - TODO: hardcoded to 1 as of now. Assuming there can be only 1 for the pilot
    			ByteBuffer buf = ByteBuffer.allocate(11);
    			buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, this.startCommitments, 0, 0, 5});
    			buf.put(data);
    			buf.put((byte)16);
    			buf.position(0);     	
    			if(printInput)
    				System.out.println("Input for startCommitments: " + Arrays.toString(buf.array()));
    			ResponseAPDU response = this.transmitCommand(new CommandAPDU(buf));
    			System.out.println("Response from startCommitments: "+response);
    			System.out.println("And this is the output: " + Arrays.toString(response.getData()));
    			byte[] proofSession = response.getData();
    			if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
    				return this.evaluateStatus(response);
    			}

    			//Issue the credential
    			buf = ByteBuffer.allocate(14);
    			buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, getIssuanceCommitment, 0, 0, 0, 0, 5});
    			buf.put(this.pinToByteArr(pin));
    			buf.put(credID);
    			buf.put(new byte[]{0,0});
    			buf.position(0);

    			if(printInput)
    				System.out.println("Input for getIssuanceCommitment: " +Arrays.toString(buf.array()));
    			response = this.transmitCommand(new CommandAPDU(buf));
    			System.out.println("Response from getIssuanceCommitment: "+response);
    			if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
    				return this.evaluateStatus(response);
    			}
    			
    			//Start responses
    			data = new byte[4+1+1+16+1]; //pin, prooverID, d which is the number of proofs, proofsession and h
    	        System.arraycopy(this.pinToByteArr(pin), 0, data, 0, 4);
    	        data[4] = 1; //TODO: ProoverID - Hardcoded for now
    	        data[5] = 1; //number of proofs - hardcoded to 1 for pilot.
    	        System.arraycopy(proofSession, 0, data, 6, 16);        

    	        buf = ByteBuffer.allocate(7+data.length);        
    	        buf.put(new byte[]{(byte) this.ABC4TRUSTCMD, this.startResponses, 0, 0, 0});
    	        buf.put(this.intLengthToShortByteArr(data.length));
    	        buf.put(data);
    	        buf.position(0);
    	        if(printInput)
    				System.out.println("Input for startResponses: " + Arrays.toString(buf.array()));
	            response = this.transmitCommand(new CommandAPDU(buf));
	            System.out.println("Response from startResponses: "+response );    	            
	            if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
	            	return this.evaluateStatus(response);
	            }
    			
	            //Set status of cred to 2 - issued finalized
	            buf = ByteBuffer.allocate(14);
	            buf.put(new byte[]{(byte)this.ABC4TRUSTCMD, getIssuanceResponse, 0, 0, 0 ,0, 5});
	            buf.put(this.pinToByteArr(pin));
	            buf.put(credID);
	            buf.put(new byte[]{0, 0});
	            buf.position(0);
	            if(printInput)
    				System.out.println("Input for getIssuanceResponse: " + Arrays.toString(buf.array()));
                response = this.transmitCommand(new CommandAPDU(buf));
                System.out.println("Response from getIssuanceResponse: " + response);
                if(this.evaluateStatus(response) != SmartcardStatusCode.OK){
                	return this.evaluateStatus(response);
                }                
                credInfo = this.readCredential(pin, credID);
            	status = credInfo[5];
            	System.out.println("After issuing the credential with ID "+credID+", it now has status: " + status);
    		} catch (CardException e) {
    			throw new RuntimeException("issueCred on smartcard failed.", e);
    		}    
    	}else{
    		System.out.println("Warn: Credential on sc attempted issued, but was already issued");    		
    	}
    	return SmartcardStatusCode.OK;
    }
}

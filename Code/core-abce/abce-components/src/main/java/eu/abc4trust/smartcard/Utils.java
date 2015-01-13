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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;


/**
 * Lots of useful functions.
 * @author enr
 *
 */
public class Utils {
  
  private static final String ENCODING = "UTF-8";
  private static final String HASH_FUNCTION = "SHA-256";
  private static final int HASH_LENGTH_BITS = 256;
  
  // Token to distinguish different types of signatures
  public static final int NEW_ISSUER_SIMPLE = 0;
  public static final int NEW_ISSUER_WITH_ATTENDANCE = 1;
  public static final int INC_COURSE_TOKEN = 2;
  
  /**
   * Takes a password which has to be representable by 8 bytes, each which is a char.
   * @param password 
   * @return 
   */
  public static byte[] passwordToByteArr(String password){
	  char[] chars = password.toCharArray();
	  if(chars.length != 8){		  
		  return null;
	  }
	  byte[] res = new byte[8];
	  for(int i = 0; i < 8; i++){
		  res[i] = (byte) chars[i];
	  }			
	  return res;	
  }
  
  public static void addToStream(ByteArrayOutputStream baos, SmartcardParameters params){
	  // Order for Idemix is N, R0, S
      // Order for UProve is P, Q, G, F
      // We settle for order:  modulus, (order), base1, (base2), (cofactor)
      addToStream(baos, params.getModulus());
      if(params.getOrderOrNull() != null) {
	    addToStream(baos, params.getOrderOrNull());
      }
	  addToStream(baos, params.getBaseForDeviceSecret());
	  if(params.getBaseForCredentialSecretOrNull() != null) {
	    addToStream(baos, params.getBaseForCredentialSecretOrNull());
	  }
	  if(params.getCofactorOrNull() != null) {
	    addToStream(baos, params.getCofactorOrNull());
	  }
  }
  
  /**
   * Serialize the integer n into the stream.
   * This function will first write the length of the representation of n in bytes
   * (as a 4-byte big-endian integer) followed by the 2s-complement big-endian representation
   * of n.
   * @param baos
   * @param n
   */
  public static void addToStream(ByteArrayOutputStream baos, BigInteger n) {
    try {
      byte[] bytes = n.toByteArray();
      addRawIntToStream(baos, bytes.length);
      baos.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Problem with writing bigInteger", e);
    } 
  }
  
  /**
   * Serialize the string s into the stream.
   * This function will first write the length of the UTF-8 encoded string in bytes
   * (as a 4-byte big-endian integer) followed by the UTF-8 encoded string.
   * @param baos
   * @param s
   */
  public static void addToStream(ByteArrayOutputStream baos, String s) {
    try {
      byte[] bytes = s.getBytes(ENCODING);
      addRawIntToStream(baos, bytes.length);
      baos.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Problem with writing string", e);
    } 
  }
  
  public static void addToStream(ByteArrayOutputStream baos, URI s) {
    addToStream(baos, s.toString());
  }
  
  /**
   * Serialize the byte array b into the stream.
   * This function will first write the length of b in bytes
   * (as a 4-byte big-endian integer) followed by b.
   * @param baos
   * @param s
   */
  public static void addToStream(ByteArrayOutputStream baos, byte[] b) {
    try {
      addRawIntToStream(baos, b.length);
      baos.write(b);
    } catch (IOException e) {
      throw new RuntimeException("Problem with writing bytes[]", e);
    } 
  }
  
  /**
   * Serialize the integer n into the stream.
   * This function will first write 4 (as a 4-byte big-endian integer) followed by the
   * 2s-complement big-endian representation of n.
   * This should be compatible with the corresponding function that writes BigIntegers.
   * @param baos
   * @param n
   */
  public static void addToStream(ByteArrayOutputStream baos, int n) {
    try {
      byte[] bytes = ByteBuffer.allocate(4).putInt(n).array();
      addRawIntToStream(baos, bytes.length);
      baos.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Problem with writing integer", e);
    } 
  }  
  
  // At runtime Java makes no distinction between Map<URI, BigInteger> and
  // Map<URI, SmartcardBlob>.
  public static void addToStream(ByteArrayOutputStream baos, Map<URI, ? extends Object> objs) {
    addToStream(baos, objs.size());
    for(Entry<URI, ? extends Object> entry: objs.entrySet()) {
      addToStream(baos, entry.getKey());
      if (entry.getValue() instanceof BigInteger) {
        addToStream(baos, (BigInteger) entry.getValue());
      } else if (entry.getValue() instanceof SmartcardBlob) {
        addToStream(baos, (SmartcardBlob) entry.getValue());
      } else {
        throw new RuntimeException("Trying to add unsupported object to stream.");
      }
    }
  }
  
  public static void addToStream(ByteArrayOutputStream baos, SmartcardBlob blob) {
    addToStream(baos, blob.blob);
  }
  
  public static void addToStream(ByteArrayOutputStream baos, RSAVerificationKey vk) {
    addToStream(baos, vk.n);
    addToStream(baos, vk.sizeModulusBytes);
  }
  
  private static void addRawIntToStream(ByteArrayOutputStream baos, int k) throws IOException {
    baos.write(ByteBuffer.allocate(4).putInt(k).array());
  }
  
  /**
   * Hashes the first argument to an integer of size bytes*8 bits. The SHA-256 hash
   * function is used to generate the bytes of the returned integer as follows:
   * resbytes = SHA256(0x00 || toHash) || SHA256( 0x01 || toHash) || SHA256( 0x02 || toHash) || ...
   * resbytes is then trimmed to bytes
   * resbytes is then parsed as an integer represented in big-endian byte order.
   * 
   * @param toSign
   * @return
   */
  public static BigInteger hashToBigIntegerWithSize(byte[] toHash, int bytes) {
	  return hashToBigIntegerWithSize_new(toHash, bytes);
	  //return hashToBigIntegerWithSize_original(toHash, bytes);
  }
  
  private static BigInteger hashToBigIntegerWithSize_new(byte[] toHash, int bytes){
	  try{
		  MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
		  byte[] digest = md.digest(toHash);
		  
		  if(digest.length <= bytes){
			  return new BigInteger(1, digest);
		  }else{
			  //need to truncate to bytes
			  byte[] truncated = new byte[bytes];
			  System.arraycopy(digest, 0, truncated, 0, bytes);
			  return new BigInteger(1, truncated);
		  }
	  } catch(NoSuchAlgorithmException e) {
	      throw new RuntimeException("Unknown hash algorithm", e);
	  } 
  }
  
  private static BigInteger hashToBigIntegerWithSize_original(byte[] toHash, int bytes){
	  try {
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      
	      int numHashes = (bytes * 8 + HASH_LENGTH_BITS - 1) / HASH_LENGTH_BITS;
	      if (numHashes > 127) {
	        throw new RuntimeException("Size too large");
	      }

	      for (int i = 0; i < numHashes; ++i) {
	        MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
	        md.update((byte) i);
	        md.update(toHash);
	        byte[] digest = md.digest();
	        baos.write(digest);
	      }

	      byte[] resbytes = new byte[bytes];
	      System.arraycopy(baos.toByteArray(), 0, resbytes, 0, bytes);
	      BigInteger ret = new BigInteger(1, resbytes);

	      if (ret.compareTo(BigInteger.ZERO) < 0) {
	        throw new RuntimeException("expected a positive number");
	      }

	      return ret;

	    } catch (NoSuchAlgorithmException e) {
	      throw new RuntimeException("Unknown hash algorithm", e);
	    } catch (IOException e) {
	      throw new RuntimeException("IOException", e);
	    }
  }
  
  public static BigInteger hashToBigIntegerWithSize(ByteArrayOutputStream baos, int bytes) {
    return hashToBigIntegerWithSize(baos.toByteArray(), bytes);
  }
  
  public static BigInteger baseForScopeExclusivePseudonym(URI scope, BigInteger modulus,
                                                          BigInteger order) {
    ByteArrayOutputStream encodedScope = new ByteArrayOutputStream();
    //addToStream(encodedScope, scope);
    try {
		encodedScope.write(scope.toString().getBytes(ENCODING));
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
    int byteLengthOfBase = (modulus.bitLength() + 7) / 8;
    byteLengthOfBase--;
    // Compute the cofactor of the order of the subgroup:
    BigInteger cofactor = modulus.subtract(BigInteger.ONE).divide(order);
    // Since modulus is a safe prime, the square of the hash will have order exactly (modulus-1)/2
    return hashToBigIntegerWithSize(encodedScope, byteLengthOfBase).modPow(cofactor, modulus);
  }

  public static RSASignature generateSignatureToAddIssuer(RSAKeyPair sk, URI parametersUri,
        SmartcardParameters credBases, byte[] nonce, Random rand) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Utils.addToStream(baos, Utils.NEW_ISSUER_SIMPLE);
    Utils.addToStream(baos, parametersUri);
    Utils.addToStream(baos, credBases);
    return RSASignatureSystem.generateSignature(sk, baos.toByteArray(), nonce, rand);
  }

  public static RSASignature generateSignatureToAddIssuer(RSAKeyPair sk, URI parametersUri,
        SmartcardParameters credBases, RSAVerificationKey courseKey, int minAttendance, byte[] nonce, Random rand) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Utils.addToStream(baos, Utils.NEW_ISSUER_WITH_ATTENDANCE);
    Utils.addToStream(baos, parametersUri);
    Utils.addToStream(baos, credBases);
    Utils.addToStream(baos, courseKey);
    Utils.addToStream(baos, minAttendance);
    return RSASignatureSystem.generateSignature(sk, baos.toByteArray(), nonce, rand);
  }

  public static RSASignature generateSignatureForAttendance(RSAKeyPair csk, URI credUri,
      int lectureId, byte[] nonce, Random rand) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Utils.addToStream(baos, Utils.INC_COURSE_TOKEN);
    Utils.addToStream(baos, credUri);
    Utils.addToStream(baos, lectureId);
    return RSASignatureSystem.generateSignature(csk, baos.toByteArray(), nonce, rand);
  }
  
  public static ZkNonceCommitmentOpening newNonceCommitment(int nonceSizeBytes, int openSizeBytes,
                                                            Random r) {
    ZkNonceCommitmentOpening nco = new ZkNonceCommitmentOpening();
    nco.nonce = new byte[nonceSizeBytes];
    nco.opening = new byte[openSizeBytes];
    r.nextBytes(nco.nonce);
    r.nextBytes(nco.opening);
    return nco;
  }
  
  public static byte[] computeCommitment(ZkNonceCommitmentOpening nco) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Utils.addToStream(baos, nco.nonce);
      Utils.addToStream(baos, nco.opening);
    
      MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
      md.update(baos.toByteArray());
      byte[] digest = md.digest();
      return digest;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static byte[] hashConcat(List<byte[]> list) {
//    int len = list.get(0).length;
//    byte[] ret = new byte[len];
//    
//    for(byte[] a: list) {
//      // Assert all byte arrays are the same length
//      if (a.length != len) {
//        System.err.println("Invalid length of nonce expected: " + len + " actual: " + a.length);
//        return null;
//      }
//      
//      for (int i=0;i<len;++i) {
//        ret[i] = (byte) (ret[i] ^ a[i]);
//      }
//    }
//    
//    return ret;
	  
	  try {
		MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
		for(byte[] a : list){
			md.update(a);
		}
		return md.digest();
	} catch (NoSuchAlgorithmException e) {
		return null;
	}
	  
  }
  
  @Deprecated
  public static byte[] checkCommitmentsAndGetNonce(List<byte[]> commitments,
                                                   List<ZkNonceCommitmentOpening> openings,
                                                   byte[] myCommitment) {
    boolean hasMine = false;
    List<byte[]> nonces = new ArrayList<byte[]>();
    
    if(commitments.size() != openings.size()) {
      System.err.println("Commitments and opening sizes different");
      return null;
    }
    Iterator<ZkNonceCommitmentOpening> openingIter = openings.iterator();
    for(byte[] expected: commitments) {
      ZkNonceCommitmentOpening op = openingIter.next();
      
      byte[] actual = computeCommitment(op);
      if (! Arrays.equals(expected, actual)) {
        System.err.println("Invalid opening");
        return null;
      }
      if (!hasMine && Arrays.equals(expected, myCommitment)) {
        hasMine = true;
      }
      
      nonces.add(op.nonce);
    }
    
    if (!hasMine) {
      System.err.println("Commitment list does not contain commitment of this smartcard");
      return null;
    }
    
    return hashConcat(nonces);
  }

}

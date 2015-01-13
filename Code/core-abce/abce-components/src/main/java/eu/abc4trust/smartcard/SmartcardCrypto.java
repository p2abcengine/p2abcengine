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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SmartcardCrypto {

    private static final String AES_MODE = "AES/ECB/NoPadding";


    public static byte[] decrypt(byte[] cipher, RSAKeyPair rootKey){
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        //first, calculate cipher^(1/3) mod n
        //this is equivalent to computing cipher^d mod n where d is : d*3 = 1 mod phi(n)
        BigInteger d = new BigInteger("3").modInverse(rootKey.getPhi());
        BigInteger c = new BigInteger(1, cipher);
        BigInteger plain = c.modPow(d, rootKey.getN());

        //Then, parse as pad||h, where size(h)=32
        //		System.out.println("plainBytes: " + Arrays.toString(plain.toByteArray()));
        //		System.out.println("plainBytes length: " + plain.toByteArray().length);
        //		System.out.println("plainBytes length fixed: " + removeSignBit(plain.toByteArray()).length);
        //		System.out.println("n native length: " + rootKey.getN().toByteArray().length);
        //		System.out.println("n fixed length: " + removeSignBit(rootKey.getN().toByteArray()).length);
        byte[] plainBytes = plain.toByteArray();
        byte[] plainBytesWithZeros = new byte[removeSignBit(rootKey.getN().toByteArray()).length];
        System.arraycopy(plainBytes, 0, plainBytesWithZeros, plainBytesWithZeros.length - plainBytes.length, plainBytes.length);
        byte[] pad = new byte[plainBytesWithZeros.length-32];
        byte[] h = new byte[32];
        System.arraycopy(plainBytesWithZeros, 0, pad, 0, pad.length);
        System.arraycopy(plainBytesWithZeros, pad.length, h, 0, 32);

        //Now, compute sha-256 of pad and check if it equals h
        byte[] h_prime = sha256.digest(pad);
        if(!Arrays.equals(h_prime, h)){
            throw new RuntimeException("HMACs where not equal!");
        }
        if(pad[0] != 0x00){
            throw new RuntimeException("Decryption failure");
        }
        int l = rootKey.getN().toByteArray().length; //l is the byteLength of n

        byte[] L = new byte[2]; //only 2 bytes for the length of data since data can be no longer than l which will never be much more than 512
        System.arraycopy(pad, 1, L, 0, 2);
        int Lsize = ByteBuffer.wrap(L).getShort();
        if((Lsize < 1) || (Lsize > (l-43))){
            throw new RuntimeException("Decryption failure");
        }
        byte[] data = new byte[Lsize];
        System.arraycopy(pad, 3, data, 0, data.length);
        return data;
    }

    public static RSASignature generateSignature(byte[] data, byte[] challenge, RSAKeyPair key, Random rand){
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] nBytes = removeSignBit(key.getN().toByteArray());

        int L = data.length;
        int l = nBytes.length;
        byte[] sigma_bytes = null;
        byte[] m2 = null;
        byte[] L_bytes = ByteBuffer.allocate(2).putShort((short)L).array();
        if(L <= (l-35)){
            byte[] z = new byte[l-35-L];
            rand.nextBytes(z);
            byte[] pad = new byte[1+2+L+z.length];
            pad[0] = 0x00;
            System.arraycopy(L_bytes, 0, pad, 1, 2);
            System.arraycopy(data, 0, pad, 3, L);
            System.arraycopy(z, 0, pad, 1+2+L, z.length);

            sha256.update(challenge);
            byte[] h = sha256.digest(pad);

            sigma_bytes = new byte[pad.length+h.length];
            System.arraycopy(pad, 0, sigma_bytes, 0, pad.length);
            System.arraycopy(h, 0, sigma_bytes, pad.length, h.length);

        }else{
            byte[] m1 = new byte[l-35];
            m2 = new byte[L-(l-35)];
            System.arraycopy(data, 0, m1, 0, m1.length);
            System.arraycopy(data, m1.length, m2, 0, m2.length);
            byte[] pad = new byte[1+2+m1.length];
            pad[0] = 0x00;
            System.arraycopy(L_bytes, 0, pad, 1, 2);
            System.arraycopy(m1, 0, pad, 3, m1.length);
            sha256.update(challenge);
            sha256.update(pad);
            byte[] h = sha256.digest(m2);

            sigma_bytes = new byte[l];
            System.arraycopy(pad, 0, sigma_bytes, 0, pad.length);
            System.arraycopy(h, 0, sigma_bytes, pad.length, h.length);
        }

        //step 4:
        BigInteger sigma = new BigInteger(1, sigma_bytes);
        BigInteger d = BigInteger.valueOf(3).modInverse(key.getPhi());
        sigma = sigma.modPow(d, key.getN());
        byte[] signature = null;
        if(L <= (l-35)){
            signature = sigma.toByteArray();
            if(signature.length > l){
                signature = removeSignBit(signature);
            }
        }else{
            byte[] sigmaBytes = removeSignBit(sigma.toByteArray());
            signature = new byte[l + m2.length];
            System.arraycopy(sigmaBytes, 0, signature, l-sigmaBytes.length, sigmaBytes.length); //in case it is a lower number
            System.arraycopy(m2, 0, signature, l, m2.length);
        }
        RSASignature sig = new RSASignature();
        sig.sig = signature;
        return sig;
    }

    public static byte[] extraction(RSAVerificationKey vk, RSASignature sig, byte[] challenge){
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            sha256 = null;
            e.printStackTrace();
        }

        int l;
        byte[] nBytes = removeSignBit(vk.n.toByteArray());
        l = nBytes.length;
        int S;
        byte[] sigBytes = sig.sig;
        S = sigBytes.length;
        if(S == l){
            BigInteger signature = new BigInteger(1, sig.sig);
            BigInteger data = signature.modPow(BigInteger.valueOf(3), vk.n);
            //byte[] data_bytes = removeSignBit(data.toByteArray());
            byte[] data_bytes = data.toByteArray();
            byte[] plainBytesWithZeros = new byte[l];
            System.arraycopy(data_bytes, 0, plainBytesWithZeros, plainBytesWithZeros.length - data_bytes.length, data_bytes.length);

            byte[] h = new byte[32];
            byte[] pad = new byte[l-32];
            System.arraycopy(plainBytesWithZeros, 0, pad, 0, l-32);
            System.arraycopy(plainBytesWithZeros, pad.length, h, 0, 32);

            sha256.update(challenge);
            byte[] h_prime = sha256.digest(pad);
            if(!Arrays.equals(h, h_prime)){
                //return null;
                throw new RuntimeException("h's failed to be equal");
            }
            if(pad[0] != 0){
                //return null;
                throw new RuntimeException("pad[0] != 0");
            }
            byte[] L_bytes = new byte[2];
            System.arraycopy(pad, 1, L_bytes, 0, 2);
            int L = ByteBuffer.wrap(L_bytes).getShort();
            if(L > (l-35)){
                //return null;
                throw new RuntimeException("L larger than l-35: " +L);
            }
            //all went well, so output:
            byte[] output = new byte[L];
            System.arraycopy(pad, 3, output, 0, L);
            return output;

        }else if(S > l){
            //parse sig as sigma||m2
            byte[] sigma = new byte[l];
            byte[] m2 = new byte[S-l];
            byte[] sig_bytes = sig.sig;
            System.arraycopy(sig_bytes, 0, sigma, 0, l);
            System.arraycopy(sig_bytes, l, m2, 0, m2.length);
            BigInteger signature = new BigInteger(1, sigma);
            BigInteger data = signature.modPow(BigInteger.valueOf(3), vk.n);
            byte[] data_bytes = data.toByteArray();
            byte[] plainBytesWithZeros = new byte[l];
            System.arraycopy(data_bytes, 0, plainBytesWithZeros, plainBytesWithZeros.length - data_bytes.length, data_bytes.length);

            byte[] h = new byte[32];
            byte[] pad = new byte[l-32];
            System.arraycopy(plainBytesWithZeros, 0, pad, 0, l-32);
            System.arraycopy(plainBytesWithZeros, pad.length, h, 0, 32);

            sha256.update(challenge);
            sha256.update(pad);
            byte[] h_prime = sha256.digest(m2);
            if(!Arrays.equals(h, h_prime)){
                //				System.out.println("\n\n Test.. trying again with sig length -1\n\n");
                //				RSASignature sign = new RSASignature();
                //				sign.sig = removeSignBit(sig.sig);
                //				return extraction(vk, sign, challenge);
                throw new RuntimeException("h's failed to be equal");
            }
            if(pad[0] != 0){
                throw new RuntimeException("pad 0 not 0");
            }
            byte[] L_bytes = new byte[2];
            System.arraycopy(pad, 1, L_bytes, 0, 2);
            int L = ByteBuffer.wrap(L_bytes).getShort();
            if(L != ((l-35)+m2.length)){
                throw new RuntimeException("L not correct size " +L);
            }

            byte[] output = new byte[L];
            System.arraycopy(pad, 3, output, 0, (l-35));
            System.arraycopy(m2, 0, output, (l-35), m2.length);
            return output;
        }else{
            throw new RuntimeException("Extraction error: S lower than l. S:" +S+ ", l: " + l);
        }
    }

    public static byte[] backup(byte[] toBackup, byte[] key, short deviceID, Random rand){
        try {
            MessageDigest sha256;
            sha256 = MessageDigest.getInstance("SHA-256");

            byte[] z = new byte[8];
            rand.nextBytes(z);
            ByteBuffer buf = ByteBuffer.allocate(4+2+2+8);
            buf.put(new byte[]{0,0,0,0});
            buf.put(ByteBuffer.allocate(2).putShort((short)toBackup.length).array());
            buf.put(ByteBuffer.allocate(2).putShort(deviceID).array());
            buf.put(z);
            byte[] pad = buf.array();

            Cipher cipher = Cipher.getInstance(AES_MODE);
            SecretKeySpec sk = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            byte[] t = cipher.doFinal(pad);

            sha256.update(key);
            byte[] digest = sha256.digest(pad);
            byte[] k = new byte[16];
            byte[] c = new byte[16];
            System.arraycopy(digest, 0, k, 0, 16);
            System.arraycopy(digest, 16, c, 0, 16);

            int L = toBackup.length;
            byte[] data = null;
            if((L%16) == 0){
                data = toBackup;
            }else{
                L = (L + 16)-(L%16);
                data = new byte[L];
                System.arraycopy(toBackup, 0, data, 0, toBackup.length);
            }

            int n = data.length/16;
            ByteBuffer archive = ByteBuffer.allocate(L+16);
            archive.put(t);
            sk = new SecretKeySpec(k, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            for(int i = 0; i < n; i++){
                byte[] data_i = new byte[16];
                System.arraycopy(data, 16*i, data_i, 0, 16);
                byte[] toEnc = xor(data_i, c);
                c = cipher.doFinal(toEnc);
                archive.put(c);
            }
            return archive.array();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[][] restore(byte[] archive, byte[] backupKey){
        try {
            MessageDigest sha256;
            sha256 = MessageDigest.getInstance("SHA-256");

            int n = (archive.length/16) - 1;
            byte[] t = new byte[16];
            System.arraycopy(archive, 0, t, 0, 16);
            Cipher cipher = Cipher.getInstance(AES_MODE);

            SecretKeySpec sk = new SecretKeySpec(backupKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, sk);
            byte[] pad = cipher.doFinal(t);
            ByteBuffer buf = ByteBuffer.wrap(pad);
            int zero = buf.getInt();
            short L = buf.getShort();
            byte[] deviceID = new byte[2];
            deviceID[0] = buf.get();
            deviceID[1] = buf.get();
            byte[] z = new byte[8];
            buf.get(z);
            if((L < ((16*(n-1))+1)) || (L > (n*16))){
                throw new RuntimeException("restore failed");
            }
            if(zero != 0){
                throw new RuntimeException("restore failed - first 4 bytes of pad was not 0");
            }

            sha256.update(backupKey);
            ByteBuffer digest = ByteBuffer.wrap(sha256.digest(pad));
            byte[] k = new byte[16];
            byte[] c = new byte[16];
            digest.get(k);
            digest.get(c);

            ByteBuffer dataBuf = ByteBuffer.allocate(L);
            for(int i = 1; i < (n+1); i++){
                sk = new SecretKeySpec(k, "AES");
                cipher.init(Cipher.DECRYPT_MODE, sk);
                byte[] c_i = new byte[16];
                System.arraycopy(archive, i*16, c_i, 0, 16);
                byte[] data_i = cipher.doFinal(c_i);
                data_i = xor(data_i, c);
                c = c_i;
                if(i == n){
                    int noZeros = (16*n)-L;
                    byte[] zeros = new byte[noZeros];
                    byte[] wouldBeZeros = new byte[noZeros];
                    System.arraycopy(data_i, 16-noZeros, wouldBeZeros, 0, noZeros);
                    if(!Arrays.equals(zeros, wouldBeZeros)){
                        throw new RuntimeException("Decrypted data does not end with zeros.");
                    }
                    //Now truncate the zeros at the end.
                    byte[] d = new byte[dataBuf.remaining()];
                    System.arraycopy(data_i, 0, d, 0, d.length);
                    dataBuf.put(d);
                }else{
                    dataBuf.put(data_i);
                }
            }
            return new byte[][]{deviceID, dataBuf.array()};

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] xor(byte[] a, byte[] b){
        byte[] res = new byte[a.length];
        for(int j = 0; j < res.length; j++){
            res[j] = (byte) (a[j] ^ b[j]);
        }
        return res;
    }

    private static byte[] removeSignBit(byte[] positiveNumber){
        if(positiveNumber[0] == 0){
            byte[] tmp = new byte[positiveNumber.length-1];
            System.arraycopy(positiveNumber, 1, tmp, 0, tmp.length);
            return tmp;
        }else{
            return positiveNumber;
        }
    }

}

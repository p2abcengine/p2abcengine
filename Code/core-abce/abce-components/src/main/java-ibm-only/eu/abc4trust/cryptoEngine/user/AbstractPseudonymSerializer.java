//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.cryptoEngine.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerImpl;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.SecretBasedSmartcard;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;

public abstract class AbstractPseudonymSerializer implements PseudonymSerializer{   
    
    public static final String ENCODING = "UTF-8";
    
    public abstract CardStorage getCardStorage();
    
    public byte[] serializeExclusivePseudonym(PseudonymWithMetadata pwm){
        Pseudonym pseudonym = pwm.getPseudonym();
        if(!pseudonym.isExclusive()){
            throw new RuntimeException("Pseudonym not exclusive");
        }
        try{        
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String scope = pseudonym.getScope();
            byte[] scope_bytes = scope.getBytes(ENCODING);
            if (scope_bytes.length > 255) {
              throw new RuntimeException("Scope is too long. Cannot serialize pseudonym.");
            }
            out.write(magicHeaderForScopeExclusive());
            out.write(scope_bytes.length);
            out.write(scope_bytes);
            
            byte[] secretReference = pseudonym.getSecretReference().toString().getBytes(ENCODING);
            if (secretReference.length > 255) {
              throw new RuntimeException("Secret Reference is too long. Cannot serialize pseudonym.");
            }
            out.write(secretReference.length);
            out.write(secretReference);
            
            byte[] pseudonymUID = pseudonym.getPseudonymUID().toString().getBytes(ENCODING);
            if (pseudonymUID.length > 255) {
              throw new RuntimeException("pseudonym UID is too long. Cannot serialize pseudonym.");
            }
            out.write(pseudonymUID.length);
            out.write(pseudonymUID);
            
            return out.toByteArray();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public PseudonymWithMetadata unserializeExclusivePseudonym(byte[] data, URI pseudonymUID){
        URI scopeFromStream;
        URI secretReferenceFromStream;
        URI pseudonymUIDFromStream;
        try {           
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            int header = in.read();
            if(header != magicHeaderForScopeExclusive()) {
              return null;
            }
            scopeFromStream = URI.create(getNextString(in));
            secretReferenceFromStream = URI.create(getNextString(in));
            pseudonymUIDFromStream = URI.create(getNextString(in));
        } catch (IOException e) {
            return null;
        }
        
        ObjectFactory of = new ObjectFactory();
        Pseudonym pseudonym = of.createPseudonym();
        Smartcard sc = (Smartcard)this.getCardStorage().getSmartcard(this.getSmartcardUri());
        int pin = this.getCardStorage().getPin(this.getSmartcardUri());
        URI secretReference = sc.getDeviceURI(pin);
        if(!secretReference.equals(secretReferenceFromStream)){
            throw new RuntimeException("Secret refenreces don't match: " + pseudonym.getSecretReference()+" vs. "+ secretReferenceFromStream);
        }       
        URI prefixedPseudonymUID = URI.create(CredentialManagerImpl.PSEUDONYM_PREFIX+pseudonymUIDFromStream.toASCIIString().replaceAll(":", "_"));
        if(!pseudonymUID.equals(prefixedPseudonymUID)){
            // TODO: how shuld test be done...
            if(pseudonymUID.equals(pseudonymUIDFromStream)) {
              //throw new RuntimeException("PseudonymUIDs is not prefixed with " + CredentialManagerImpl.PSEUDONYM_PREFIX); 
            } else {
              throw new RuntimeException("PseudonymUIDs don't match prefixed card UID : "+pseudonymUID +" vs. "+prefixedPseudonymUID);
            }
        }
        pseudonym.setSecretReference(secretReference);
        pseudonym.setExclusive(true);       
        pseudonym.setPseudonymUID(pseudonymUIDFromStream);      
        BigInteger pseudonymValue = sc.computeScopeExclusivePseudonym(pin, scopeFromStream);

        pseudonym.setPseudonymValue(pseudonymValue.toByteArray());
        pseudonym.setScope(scopeFromStream.toString());

        Metadata md = of.createMetadata();
        PseudonymMetadata pmd = of.createPseudonymMetadata();
        pmd.getFriendlyPseudonymDescription().add(generateFriendlyDescription(pseudonym.getScope()));
        pmd.setMetadata(md);
        PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
        pwm.setPseudonymMetadata(pmd);
        CryptoParams cryptoEvidence = of.createCryptoParams();
        URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");

        URI secretUID = sc.getDeviceURI(pin);
        PseudonymCryptoFacade pcf = new PseudonymCryptoFacade();
        pcf.setScopeExclusivePseudonym(scopeFromStream, secretUID, pseudonym.getPseudonymValue());
        pwm.setCryptoParams(pcf.getCryptoParams());
        
        return pwm;
    }
    
    private String getNextString(ByteArrayInputStream in) throws IOException{
        int size = in.read();
        byte[] bytes = new byte[size];
        int read = in.read(bytes);
        if(size != read){
            throw new IOException("Amount of bytes read is not the expected amount");
        }
        return new String(bytes, ENCODING);
    }
    
    private URI getSmartcardUri(){
        Map<URI, BasicSmartcard> scs = this.getCardStorage().getSmartcards();
        for(URI uri : scs.keySet()){
            if(!(scs.get(uri) instanceof SecretBasedSmartcard)){                
                return uri;
            }
        }
        return null;
    }
    
    /**
     * Generate a friendly description heuristically from the given pseudonym scope.
     * The friendly description will have language "en" (English).
     * The scope will be divided into tokens according to the colon character ":".
     * We take the last of these tokens, do a URL-decoding (in the UTF-8 character set),
     * and put the result as the value of the friendly description. On error, the whole
     * scope is put as value of the friendly description.
     * @param scope
     * @return
     */
    public static FriendlyDescription generateFriendlyDescription(String scope) {
      FriendlyDescription fd = new FriendlyDescription();
      fd.setLang("en");

      
      String[] tokens = scope.split(":");
      String lastToken = tokens[tokens.length-1];
      try {
        String decoded = java.net.URLDecoder.decode(lastToken, "UTF-8");
        fd.setValue(decoded);
      } catch (Exception e) {
        fd.setValue(scope);
      }

      return fd;
    }
    
    public int magicHeaderForScopeExclusive() {
      return 70;
    }
}

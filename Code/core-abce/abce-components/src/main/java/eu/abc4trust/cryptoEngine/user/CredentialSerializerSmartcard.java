//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.cryptoEngine.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure.DataType;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.SystemParameters;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixUtils;
import eu.abc4trust.cryptoEngine.uprove.util.UProveKeyAndToken;
import eu.abc4trust.cryptoEngine.uprove.util.UProveToken;
import eu.abc4trust.cryptoEngine.user.credCompressor.CompressorUtils;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.MyCredentialDescription;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This class serializes credentials by looking up information from external sources.
 * 
 * @author enr
 */
public class CredentialSerializerSmartcard implements CredentialSerializer {
  private final KeyManager keyManager;
  private final List<String> listOfPrefixes;
  private final Map<String, Integer> invertedListOfPrefixes;
  
  // If enabled, prints out debugging info (size of serialization)
  public static boolean PRINT_SIZE_DEBUG_INFO = false;
  
  // If disabled, the attribute uid will not be written out
  public static boolean WRITE_ATTRIBUTE_UID = false;
  // If disabled, use the default image
  public static boolean WRITE_IMAGE_REFERENCE = false;
  // If disabled, use the default friendly credential description
  public static boolean WRITE_FRIENDLY_CREDENTIAL_DESC = false;
  // If disabled, don't write the credential Uid
  public static boolean WRITE_CREDENTIAL_UID = false;
  // If disabled, don't write the smartcard Uid
  public static boolean WRITE_SMARTCARD_UID = false;
  
  private static final int AS_XML = 0;
  private static final int IDEMIX_CREDENTIAL = 1;
  private static final int NON_REVOCATION_EVIDENCE_AS_JAXB = 2;
  private static final int NON_REVOCATION_EVIDENCE = 3;
  private static final int AS_JAVA_OBJECT = 4;
  private static final int UPROVE_KEY_AND_TOKEN = 5;
  
  @Inject
  public CredentialSerializerSmartcard(KeyManager keyManager,
                                       @Named("listOfPrefixesForCompressor") List<String> listOfPrefixes) {
    this.keyManager = keyManager;
    this.listOfPrefixes = listOfPrefixes;
    this.invertedListOfPrefixes = CompressorUtils.invertList(listOfPrefixes); 
  }
  
  @Override
  public byte[] serializeCredential(Credential cred) {
    try {
      IdemixCryptoEngineUserImpl.loadIdemixSystemParameters(keyManager.getSystemParameters());
      
      ByteArrayOutputStream ser = new ByteArrayOutputStream();
      debug(ser, "Start");
      
      ser.write(magicHeader());
      ser.write(flags());
            
      MyCredentialDescription mycd = serializeCredentialDescription(ser, cred.getCredentialDescription());
      
      debug(ser, "Cred Desc");
      CompressorUtils.writeLength(ser, cred.getNonRevocationEvidenceUID().size());
      for(URI uri: cred.getNonRevocationEvidenceUID()) {
        CompressorUtils.writeStringSmart(ser, uri.toString(), invertedListOfPrefixes);
      }
      debug(ser, "NRE uid");
      CompressorUtils.writeLength(ser, cred.getCryptoParams().getAny().size());
      for(Object o: cred.getCryptoParams().getAny()) {
        serializeCryptoParams(ser, o, mycd);
      }
      debug(ser, "Crypto params");


      
      return ser.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private int flags() {
    int flag = 0;
    if(WRITE_ATTRIBUTE_UID) {
      flag++;
    }
    if(WRITE_IMAGE_REFERENCE) {
      flag+=2;
    }
    if(WRITE_FRIENDLY_CREDENTIAL_DESC) {
      flag+=4;
    }
    if(WRITE_CREDENTIAL_UID) {
      flag+=8;
    }
    if(WRITE_SMARTCARD_UID) {
      flag+=16;
    }
    return flag;
  }

  private void debug(ByteArrayOutputStream ser, String desc) {
    if (PRINT_SIZE_DEBUG_INFO) {
      System.err.println(desc + " - " + ser.toByteArray().length);
    }
  }
  
  private MyCredentialDescription serializeCredentialDescription(ByteArrayOutputStream ser,
      CredentialDescription cd) throws KeyManagerException {
    debug(ser, "= Start");
    if(WRITE_CREDENTIAL_UID) {
      CompressorUtils.writeStringSmart(ser, cd.getCredentialUID().toString(), invertedListOfPrefixes);
    }
    debug(ser, "= Cred uid");
    if(WRITE_FRIENDLY_CREDENTIAL_DESC) {
      CompressorUtils.writeLength(ser, cd.getFriendlyCredentialName().size());
      for(FriendlyDescription fd: cd.getFriendlyCredentialName()) {
        CompressorUtils.writeStringSmart(ser, fd.getLang(), invertedListOfPrefixes);
        CompressorUtils.writeStringSmart(ser, fd.getValue(), invertedListOfPrefixes);
      }
    }
    debug(ser, "= Friendly desc");
    
    if(WRITE_IMAGE_REFERENCE) {
      CompressorUtils.writeStringSmart(ser, cd.getImageReference().toString(), invertedListOfPrefixes);
    }
    debug(ser, "= Image reference");
    
    CompressorUtils.writeStringSmart(ser, cd.getCredentialSpecificationUID().toString(), invertedListOfPrefixes);
    
    debug(ser, "= Cred spec");
    
    CompressorUtils.writeStringSmart(ser, cd.getIssuerParametersUID().toString(), invertedListOfPrefixes);
    
    debug(ser, "= Issuer uid");
    
    if(WRITE_SMARTCARD_UID) {
      CompressorUtils.writeStringSmart(ser, cd.getSecretReference().toString(), invertedListOfPrefixes);
    }
    
    debug(ser, "= Smartcard uid");
    
    CredentialSpecification credSpec = keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());
    if(credSpec == null) {
      throw new RuntimeException("Could not find credential specification " + cd.getCredentialSpecificationUID());
    }
    if(cd.getAttribute().size() != credSpec.getAttributeDescriptions().getAttributeDescription().size()) {
      throw new RuntimeException("Credential does not match credential specification");
    }
    IssuerParameters ip = keyManager.getIssuerParameters(cd.getIssuerParametersUID());
    if(ip == null) {
      throw new RuntimeException("Could not find issuer parameters " + cd.getIssuerParametersUID());
    }

    MyCredentialDescription mycd = new MyCredentialDescription(cd, credSpec, ip, keyManager);
    for(AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()) {
      Attribute a = mycd.getAttribute(ad.getType()).getXmlAttribute();
      if(ad.getDataType().toString().equals("xs:integer")) {
        try {
        CompressorUtils.writeBigInteger(ser, new BigInteger(a.getAttributeValue().toString()));
        debug(ser, "  - Int");
        } catch(NumberFormatException e) {
          throw e;
        }
      } else if(ad.getDataType().toString().equals("xs:boolean")) {
        debug(ser, "  - Boolean");
        CompressorUtils.writeBoolean(ser, a.getAttributeValue());
      } else {
        CompressorUtils.writeString(ser, a.getAttributeValue().toString());
        debug(ser, "  - String");
      }
      if(WRITE_ATTRIBUTE_UID) {
        CompressorUtils.writeStringSmart(ser, a.getAttributeUID().toString(), invertedListOfPrefixes);
      }
    }
    debug(ser, "= All attributes");
    
    return mycd;
  }
  

  private URI toUri(String s) {
    if(s==null)  {
      return null;
    } else {
      return URI.create(s);
    }
  }
  
  private MyCredentialDescription unserializeCredentialDescription(ByteArrayInputStream ser, URI credentialUri, URI smartcardUri)
      throws KeyManagerException {
    CredentialDescription cd = new CredentialDescription();
    
    if(WRITE_CREDENTIAL_UID) {
      cd.setCredentialUID(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
    } else {
      cd.setCredentialUID(credentialUri);
      if(credentialUri == null) {
        throw new RuntimeException("Cannot recover credential URI");
      }
    }
    
    int len;
    if(WRITE_FRIENDLY_CREDENTIAL_DESC) {
      len = CompressorUtils.getLength(ser);
      for(int i=0;i<len;++i) {
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang(CompressorUtils.readStringSmart(ser, listOfPrefixes));
        fd.setValue(CompressorUtils.readStringSmart(ser, listOfPrefixes));
        cd.getFriendlyCredentialName().add(fd);
      }
    }
    
    if (WRITE_IMAGE_REFERENCE) {
      cd.setImageReference(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
    }
    cd.setCredentialSpecificationUID(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
    cd.setIssuerParametersUID(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
    
    if(WRITE_SMARTCARD_UID) {
      cd.setSecretReference(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
    } else {
      cd.setSecretReference(smartcardUri);
      if(smartcardUri == null) {
        throw new RuntimeException("Cannot recover smartcard URI");
      }
    }
    
    CredentialSpecification credSpec =
        keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());
    if (credSpec == null) {
      throw new RuntimeException("Could not find credential specification "
          + cd.getCredentialSpecificationUID());
    }
    IssuerParameters ip = keyManager.getIssuerParameters(cd.getIssuerParametersUID());
    if(ip == null) {
      throw new RuntimeException("Could not find issuer parameters " + cd.getIssuerParametersUID());
    }
    
    if(!WRITE_IMAGE_REFERENCE) {
      cd.setImageReference(credSpec.getDefaultImageReference());
    }
    if(!WRITE_FRIENDLY_CREDENTIAL_DESC) {
      for(FriendlyDescription desc: credSpec.getFriendlyCredentialName()) {
        cd.getFriendlyCredentialName().add(desc);
      }
    }
    
    for(AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()) {
      Attribute a = new Attribute();
      a.setAttributeDescription(ad);
      if (ad.getDataType().toString().equals("xs:integer")) {
        a.setAttributeValue(CompressorUtils.readBigInteger(ser));
      } else if(ad.getDataType().toString().equals("xs:boolean")) {
        a.setAttributeValue(CompressorUtils.readBoolean(ser));
      } else {
        a.setAttributeValue(CompressorUtils.readString(ser));
      }
      if(WRITE_ATTRIBUTE_UID) {
        a.setAttributeUID(toUri(CompressorUtils.readStringSmart(ser, listOfPrefixes)));
      } else {
        a.setAttributeUID(toUri(cd.getCredentialUID().toString() + ":" + a.getAttributeDescription().getType().toString()));
      }
      cd.getAttribute().add(a);
    }
    return new MyCredentialDescription(cd, credSpec, ip, keyManager);
  }

  @Override
  public Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri) {
    
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      int header = bais.read();
      if (header != magicHeader()) {
        throw new RuntimeException("Cannot unserialize this credential: header was " + header
            + " expected header " + magicHeader());
      }
      int flags = bais.read();
      if(flags != flags()) {
        throw new RuntimeException("Incompatible flags for reading credential - got : " + flags + " - expected : " + flags());
      }
      
      Credential ret = new Credential();
      
      MyCredentialDescription mycd = unserializeCredentialDescription(bais, credentialUri, smartcardUri);
      ret.setCredentialDescription(mycd.getCredentialDesc());      
      
      int len = CompressorUtils.getLength(bais);
      for(int i=0;i<len;++i) {
        String s = CompressorUtils.readStringSmart(bais, listOfPrefixes);
        ret.getNonRevocationEvidenceUID().add(toUri(s));
      }
      
      ret.setCryptoParams(new CryptoParams());
      len = CompressorUtils.getLength(bais);
      for(int i=0;i<len;++i) {
         Object cp = unserializeCryptoParams(bais, mycd, ret.getNonRevocationEvidenceUID());
         ret.getCryptoParams().getAny().add(cp);
      }
      return ret;
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Credential unserializeCredential(byte[] data) {
    return unserializeCredential(data, null, null);
  }

  @SuppressWarnings("rawtypes")
  private void serializeCryptoParams(ByteArrayOutputStream ser, Object o, MyCredentialDescription mycd) throws KeyManagerException, IOException {
    if (o instanceof Element) {
      Element el = (Element) o;
      String rootNameWithNs = el.getNodeName();
      String rootName = rootNameWithNs.replaceAll("^.*:", "");
      if(rootName.equalsIgnoreCase("Credential")) {   
        serializeIdemixCredential(ser, el, mycd);
        debug(ser, "= Idmx");
      } else {
        serializeAsXml(ser, o);
        debug(ser, "= Xml");
      }
    } else if(o instanceof JAXBElement && ((JAXBElement)o).getValue().getClass().equals(NonRevocationEvidence.class)) {
      serializeNre(ser, (NonRevocationEvidence)XmlUtils.unwrap(o, NonRevocationEvidence.class), mycd, true);
      debug(ser, "= Nre");
    }else if(o instanceof NonRevocationEvidence){
    	serializeNre(ser, (NonRevocationEvidence)o, mycd, false);
    	debug(ser, "=Nre");
    } else if(o instanceof ArrayList<?>){
    	ArrayList<?> arr = (ArrayList<?>)o;
    	if(arr.size() > 0 && arr.get(0) instanceof UProveKeyAndToken){
    		CompressorUtils.writeLength(ser, UPROVE_KEY_AND_TOKEN);
    		debug(ser, "= Uprove key and token");
    		CompressorUtils.writeLength(ser, arr.size());
    		debug(ser, "= Length of array");
    		for(int i = 0; i < arr.size(); i++){
    			serializeUProveKeyAndToken(ser, (UProveKeyAndToken)arr.get(i));
    			debug(ser, "= UProveKeyAndToken");
    		}
    	}else{
    		if(arr.size() == 0){
    			//TODO: Serializing empty array list. Must be able to do this smarter somehow using a few bytes.
    			CompressorUtils.writeLength(ser, UPROVE_KEY_AND_TOKEN);
    			CompressorUtils.writeLength(ser, arr.size());
    			//serializeAsJavaObject(ser, o);
    		}else{
	    		//Should not happen, as UProve should not contain arraylists of other types than UProveKeyAndToken!
	    		System.err.println("\n\n Cred compression - Ensure that this does not happen. Serializing arraylist of unknown type \n\n");
	    		serializeAsJavaObject(ser, o);
    		}
    	}	    	
    } else {
      serializeAsJavaObject(ser, o);
      debug(ser, "= Obj");
    }
  }
  
  private void serializeUProveKeyAndToken(ByteArrayOutputStream ser, UProveKeyAndToken kt) throws IOException{
	  //No compression as this would increase the size for the random data it contains.
	  CompressorUtils.writeData(ser, kt.getPrivateKey());
	  UProveToken token = kt.getToken();	
	  CompressorUtils.writeData(ser, token.getH());
	  ser.write(token.isDeviceProtected() ? 1 : 0);
	  CompressorUtils.writeData(ser, token.getPi());
	  CompressorUtils.writeData(ser, token.getSigmaCPrime());
	  CompressorUtils.writeData(ser, token.getSigmaRPrime());
	  CompressorUtils.writeData(ser, token.getSigmaZPrime());
	  CompressorUtils.writeData(ser, token.getTi());
	  CompressorUtils.writeData(ser, token.getUidp());
	  CompressorUtils.writeLength(ser, token.getPseudonyms().size());
	  for(URI pseudonym : token.getPseudonyms()){
		  CompressorUtils.writeStringSmart(ser, pseudonym.toString(), invertedListOfPrefixes);
	  }
  }

  private ArrayList<UProveKeyAndToken> unserializeUProveKeyAndTokenArray(ByteArrayInputStream bais) throws IOException{
	  int amount = CompressorUtils.getLength(bais);
	  ArrayList<UProveKeyAndToken> list = new ArrayList<UProveKeyAndToken>();	  
	  for(int i = 0; i < amount; i++){
		  list.add(unserializeUProveKeyAndToken(bais));
	  }
	  return list;
  }

  private UProveKeyAndToken unserializeUProveKeyAndToken(ByteArrayInputStream bais) throws IOException{
	  UProveKeyAndToken kt = new UProveKeyAndToken();
	  kt.setPrivateKey(CompressorUtils.readData(bais));
	  UProveToken token = new UProveToken();
	  token.setH(CompressorUtils.readData(bais));
	  token.setDeviceProtected((bais.read()==1 ? true : false));
	  token.setPi(CompressorUtils.readData(bais));
	  token.setSigmaCPrime(CompressorUtils.readData(bais));
	  token.setSigmaRPrime(CompressorUtils.readData(bais));
	  token.setSigmaZPrime(CompressorUtils.readData(bais));
	  token.setTi(CompressorUtils.readData(bais));
	  token.setUidp(CompressorUtils.readData(bais));
	  int pseudonymAmount = CompressorUtils.getLength(bais);
	  Set<URI> pseudonyms = token.getPseudonyms();
	  for(int i = 0; i < pseudonymAmount; i++){
		 pseudonyms.add(URI.create(CompressorUtils.readStringSmart(bais, listOfPrefixes))); 
	  }
	  kt.setToken(token);
	  return kt;
  }

  
  private void serializeNre(ByteArrayOutputStream ser, NonRevocationEvidence nre, MyCredentialDescription mycd, boolean asJAXB) throws KeyManagerException {
    
    if(asJAXB){
    	CompressorUtils.writeLength(ser, NON_REVOCATION_EVIDENCE_AS_JAXB);
    }else{
    	CompressorUtils.writeLength(ser, NON_REVOCATION_EVIDENCE);
    }
    debug(ser, "-    Header NRE");
    // Skip UID
    CompressorUtils.writeStringSmart(ser, nre.getRevocationAuthorityParametersUID().toString(), invertedListOfPrefixes);
    debug(ser, "-    RA param uid");
    // Skip credential uid
    CompressorUtils.writeBigInteger(ser, BigInteger.valueOf(nre.getCreated().getTimeInMillis()));
    debug(ser, "-    created");
    CompressorUtils.writeBigInteger(ser, BigInteger.valueOf(nre.getExpires().getTimeInMillis()));
    debug(ser, "-    expires");
    CompressorUtils.writeBigInteger(ser, BigInteger.valueOf(nre.getEpoch()));
    debug(ser, "-    epoch");
    // Skip attribute
    
    RevocationAuthorityParameters rap = keyManager.getRevocationAuthorityParameters(nre.getRevocationAuthorityParametersUID());
    AccumulatorPublicKey apk = (AccumulatorPublicKey)Parser.getInstance().parse((Element)rap.getCryptoParams().getAny().get(0));

    StructureStore.getInstance().add(apk.getUri().toString(), apk);

    AccumulatorWitness wit = (AccumulatorWitness) Parser.getInstance().parse((Element)nre.getCryptoParams().getAny().get(0));
    CompressorUtils.writeBigInteger(ser, wit.getWitness());
    debug(ser, "-    wit");
    CompressorUtils.writeBigInteger(ser, wit.getState().getAccumulatorValue());
    debug(ser, "-    acc");
    CompressorUtils.writeBigInteger(ser, wit.getValue());
    debug(ser, "-    acc value");
  }
  
  
  private Object unserializeNre(ByteArrayInputStream bais, MyCredentialDescription cd, URI nreuri, boolean asJAXB) {
    try {
      BigInteger rh = cd.getAttributeValue(URI.create(MyCredentialSpecification.REVOCATION_HANDLE)).getIntegerValueOrNull();
      
      String rapid = CompressorUtils.readStringSmart(bais, listOfPrefixes);
      BigInteger created = CompressorUtils.readBigInteger(bais);
      BigInteger expires = CompressorUtils.readBigInteger(bais);
      int epoch = CompressorUtils.readBigInteger(bais).intValue();
      BigInteger witness = CompressorUtils.readBigInteger(bais);
      BigInteger accValue = CompressorUtils.readBigInteger(bais);
      BigInteger accValue2 = CompressorUtils.readBigInteger(bais);
      
      RevocationAuthorityParameters rap = keyManager.getRevocationAuthorityParameters(URI.create(rapid));
      AccumulatorPublicKey apk = (AccumulatorPublicKey)Parser.getInstance().parse((Element)rap.getCryptoParams().getAny().get(0));
      StructureStore.getInstance().add(apk.getUri().toString(), apk);
      
      TimeZone tz = TimeZone.getTimeZone("GMT");
      GregorianCalendar gc1 = new GregorianCalendar(tz);
      gc1.setTimeInMillis(created.longValue());
      GregorianCalendar gc2 = new GregorianCalendar(tz);
      gc2.setTimeInMillis(expires.longValue());
      XMLGregorianCalendar lastChange = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc1);
      AccumulatorWitness aw = new AccumulatorWitness(new AccumulatorState(apk, epoch, accValue, lastChange), accValue2, witness);      
      Element awel = XMLSerializer.getInstance().serializeAsElement(aw);
      
      NonRevocationEvidence nre = new NonRevocationEvidence();
      CryptoParams cp = new CryptoParams();
      cp.getAny().add(awel);
      
      nre.setCreated(gc1);
      nre.setCredentialUID(cd.getCredentialDescription().getCredentialUID());
      nre.setCryptoParams(cp);
      nre.setEpoch(epoch);
      nre.setExpires(gc2);
      nre.setNonRevocationEvidenceUID(nreuri);
      nre.setRevocationAuthorityParametersUID(URI.create(rapid));
      nre.getAttribute().add(cd.getAttribute(URI.create(MyCredentialSpecification.REVOCATION_HANDLE)).getXmlAttribute());
      if(asJAXB){
    	  return new ObjectFactory().createNonRevocationEvidence(nre);
      }else{
    	  return nre;
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void serializeAsJavaObject(ByteArrayOutputStream ser, Object o) throws IOException {
    CompressorUtils.writeLength(ser, AS_JAVA_OBJECT);
    
    ByteArrayOutputStream binaryObj = new ByteArrayOutputStream();
    ObjectOutput output = new ObjectOutputStream( binaryObj );
    output.writeObject(o);
    output.close();
    
    CompressorUtils.writeCompressedData(ser, binaryObj.toByteArray());
  }

  private SystemParameters loadParametersForIdemix(MyCredentialDescription mycd) throws KeyManagerException {
    CredentialStructure credStruct =
        IdemixUtils.createIdemixCredentialStructure(null, mycd.getCredSpec(), null, null,
          mycd.getCredSpec().isKeyBinding());
    StructureStore.getInstance().add(mycd.getCredSpec().getSpecificationUID().toString(), credStruct);
    
    IssuerParameters ip = keyManager.getIssuerParameters(mycd.getCredentialDesc().getIssuerParametersUID());
    SystemParameters sp = null;
    boolean ok=false;
    for(Object o: ip.getCryptoParams().getAny()) {
      if(o instanceof Element) {
        Element oel = (Element) o;
        String rootNameWithNs = oel.getNodeName();
        String rootName = rootNameWithNs.replaceAll("^.*:", "");
        if(rootName.equalsIgnoreCase("IssuerPublicKey")) {
          IssuerPublicKey ipk = (IssuerPublicKey)Parser.getInstance().parse(oel);
          StructureStore.getInstance().add(ip.getParametersUID().toString(), ipk);
          ok=true;
          sp = ipk.getGroupParams().getSystemParams();
        }
      }
    }
    if(!ok || sp == null) {
      throw new RuntimeException("Cannot find idemix issuer parameters for credential");
    }
    return sp;
  }
  private void serializeIdemixCredential(ByteArrayOutputStream ser,
      Element el, MyCredentialDescription mycd) throws KeyManagerException {
    
    SystemParameters sp = loadParametersForIdemix(mycd);
    
    com.ibm.zurich.idmx.dm.Credential cred = (com.ibm.zurich.idmx.dm.Credential) Parser.getInstance().parse(el);
    
    BigInteger e_compress = cred.getE().subtract(BigInteger.ONE.shiftLeft(sp.getL_e()-1));
    CompressorUtils.writeLength(ser, IDEMIX_CREDENTIAL);
    debug(ser, "-    Header");
    CompressorUtils.writeBigInteger(ser, cred.getCapA());
    debug(ser, "-    A");
    CompressorUtils.writeBigInteger(ser, e_compress);
    debug(ser, "-    E");
    CompressorUtils.writeBigInteger(ser, cred.getV());
    debug(ser, "-    V");
  }

  private Object unserializeAsIdemixCredential(ByteArrayInputStream bais, MyCredentialDescription mycd) throws KeyManagerException {
    
    SystemParameters sp = loadParametersForIdemix(mycd);
    CredentialStructure cs = (CredentialStructure)StructureStore.getInstance().get(mycd.getCredentialDescription().getCredentialSpecificationUID());
    
    BigInteger a = CompressorUtils.readBigInteger(bais);
    BigInteger e_compress = CompressorUtils.readBigInteger(bais);
    BigInteger v = CompressorUtils.readBigInteger(bais);
    
    BigInteger e = e_compress.add(BigInteger.ONE.shiftLeft(sp.getL_e()-1));
    
    Vector<com.ibm.zurich.idmx.dm.Attribute> atts = new Vector<com.ibm.zurich.idmx.dm.Attribute>();
    for(Attribute att: mycd.getCredentialDescription().getAttribute()) {
      URI name = att.getAttributeDescription().getType();
      BigInteger value = mycd.getAttributeValue(name).getIntegerValueOrNull();
      
      AttributeStructure as = cs.getAttributeStructure(name.toString());
      
      com.ibm.zurich.idmx.dm.Attribute ia;
      if(as.getDataType() == DataType.ENUM) {
        ia = new com.ibm.zurich.idmx.dm.Attribute(as, value, null);
      } else {
        ia = new com.ibm.zurich.idmx.dm.Attribute(as, value);
      }
      atts.add(ia);
    }
    
    com.ibm.zurich.idmx.dm.Credential cred = new com.ibm.zurich.idmx.dm.Credential(
      mycd.getCredentialDescription().getIssuerParametersUID(),
      mycd.getCredentialDescription().getCredentialSpecificationUID(),
      a, e, v, atts, null,
      mycd.getUid(),
      mycd.getSecretReference());
    return XMLSerializer.getInstance().serializeAsElement(cred);
  }

  private void serializeAsXml(ByteArrayOutputStream ser, Object o) {
    try {
      // TODO: Compress Idemix credentials here
      ObjectFactory of = new ObjectFactory();
      CryptoParams cp = new CryptoParams();
      cp.getAny().add(o);   
      String xml = XmlUtils.toXml(of.createCryptoParams(cp));
      CompressorUtils.writeLength(ser, AS_XML);
      CompressorUtils.writeStringCompressed(ser, xml);
    } catch (Exception e) {
      throw new RuntimeException("Could not serialize crypto param. ", e);
    }
  }
  
  private Object unserializeFromXml(ByteArrayInputStream bais) {
    String xml = CompressorUtils.readStringCompressed(bais);
    try {
      // TODO: Compress Idemix credentials here
      Object o = XmlUtils.getObjectFromXML(xml, false);
      if(o instanceof CryptoParams) {
        return ((CryptoParams)o).getAny().get(0);
      } else {
        return o;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private Object unserializeCryptoParams(ByteArrayInputStream bais, MyCredentialDescription cd, List<URI> list) throws KeyManagerException, IOException, ClassNotFoundException {
    Iterator<URI> listIter = list.iterator();
    
    switch(CompressorUtils.getLength(bais)) {
      case AS_XML:
        return unserializeFromXml(bais);
      case IDEMIX_CREDENTIAL:
        return unserializeAsIdemixCredential(bais, cd);
      case NON_REVOCATION_EVIDENCE_AS_JAXB:
    	  return unserializeNre(bais, cd, listIter.next(), true);
      case NON_REVOCATION_EVIDENCE:
        return unserializeNre(bais, cd, listIter.next(), false);      
      case UPROVE_KEY_AND_TOKEN:
    	return unserializeUProveKeyAndTokenArray(bais);
      case AS_JAVA_OBJECT:
        return unserializeJavaObject(bais);
      default:
        throw new RuntimeException("Cannot unserialize crypto parameters");
    }
  }

  private Object unserializeJavaObject(ByteArrayInputStream bais) throws IOException, ClassNotFoundException {
    
    byte[] data = CompressorUtils.readCompressedData(bais);
    
    
    ByteArrayInputStream binaryObj = new ByteArrayInputStream(data);
    ObjectInput output = new ObjectInputStream( binaryObj );
    Object ret = output.readObject();
    output.close();
    
    return ret;
  }

  @Override
  public int magicHeader() {
    return 0x05;
  }

}

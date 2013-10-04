//* Licensed Materials - Property of IBM, Miracle A/S,                *
//* Alexandra Instituttet A/S, and Microsoft                          *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* (C) Copyright Microsoft Corp. 2012. All Rights Reserved.          *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.uprove.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.datacontract.schemas._2004._07.abc4trust_uprove.ArrayOfUProveKeyAndTokenComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SecondIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SubgroupGroupDescriptionComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.ThirdIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveKeyAndTokenComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import abc4trust_uprove.service1.IService1;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfbase64Binary;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Utilities for converting UProve interop specialized objects to and from serializable objects
 * 
 * @author Raphael Dobers
 */

public class UProveUtils {

    public URI getAlias(int inx, PseudonymInToken pseudonym) {
        URI nymAlias = pseudonym.getAlias();
        if (nymAlias == null) {
            nymAlias = URI.create("abc4trust.eu/pseudonym/" + inx);
        }
        return nymAlias;
    }

    /**
     * Map message to U-Prove message string.
     * 
     * @param ptd
     * @param of
     * @return
     */
    public String normalizeApplicationMessage(PresentationTokenDescription ptd) {
        ObjectFactory of = new ObjectFactory();
        String applicationMessage = "Invalid message";
        try {
            applicationMessage = XmlUtils.toNormalizedXML(of.createMessage(ptd
                    .getMessage()));
        } catch (Exception e1) {
            throw new RuntimeException("failed to normalize XML message", e1);
        }
        return applicationMessage;
    }

    public ArrayList<UProveKeyAndToken> convertArrayOfUProveKeyAndTokenComposite(ArrayOfUProveKeyAndTokenComposite compositeTokens) {
        ArrayList<UProveKeyAndToken> retVal = new ArrayList<UProveKeyAndToken>();
        for(UProveKeyAndTokenComposite uktc:compositeTokens.getUProveKeyAndTokenComposite()) {
            byte[] privateKey = uktc.getPrivateKey().getValue();
            UProveTokenComposite utc = uktc.getToken().getValue();

            UProveToken token = new UProveToken();
            token.setH(utc.getH().getValue());
            token.setDeviceProtected(new Boolean(utc.isIsDeviceProtected()));
            token.setPi(utc.getPI().getValue());
            token.setSigmaCPrime(utc.getSigmaCPrime().getValue());
            token.setSigmaRPrime(utc.getSigmaRPrime().getValue());
            token.setSigmaZPrime(utc.getSigmaZPrime().getValue());
            token.setTi(utc.getTI().getValue());
            token.setUidp(utc.getUidp().getValue());

  //          token.setScope(""); // Used for Scope exclusive tokens

            UProveKeyAndToken ukt = new UProveKeyAndToken();
            ukt.setPrivateKey(privateKey);
            ukt.setToken(token);

            retVal.add(ukt);
        }

        return retVal;
    }
    
    public String getSessionKey(UProveBindingManager binding, int keyLength) {
       String sessionKey = binding.login(true, "1234", 1, 0, 1, keyLength);
		return sessionKey;		
	}
    
    /**
     * Only returns a hardware smartcard. NO SOFTWARESMARTCARDS!
     * @param cs
     * @return ONLY HardwareSmartcards
     */
    public static URI getSmartcardUri(CardStorage cs){
    	Map<URI, BasicSmartcard> scs = cs.getSmartcards();
    	for(URI uri : scs.keySet()){
    		if((scs.get(uri) instanceof HardwareSmartcard)){    			
    			return uri;
    		}
    	}
    	return null;
    }
    
    public String getSessionKey(UProveBindingManager binding, CardStorage cardStorage, int credIDOnSC, int keyLength) {
    	URI scUri = getSmartcardUri(cardStorage);
    	String sessionKey;
    	//For testing
    	if(scUri == null || cardStorage.getSmartcard(scUri) instanceof SoftwareSmartcard){
    		System.out.println("scUri null? "+scUri +", if not, was a software SC.");
    		sessionKey = binding.login(true, "1234", credIDOnSC, 0, 1, keyLength);
    	}else{ //only hardware smartcards
    		System.out.println("\n\n\n\n CALLING THE HARDWARE SMARTCARD BINDING LOGIN\n\n\n\n");
    	    String pinString = String.format("%04d", cardStorage.getPin(scUri));
    	    sessionKey = binding.login(false, pinString, credIDOnSC, -1, 1, keyLength);
    	}
		return sessionKey;		
	}
    
    public String getSessionKey(IService1 binding, int keyLength) {
    	String sessionKey = binding.login(true, "1234", 1, 0, 1, keyLength);
		return sessionKey;		
	}

    public List<UProveTokenComposite> convertUProveKeyAndToken(List<UProveKeyAndToken> keysAndTokens) {
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();

        List<UProveTokenComposite> list = new ArrayList<UProveTokenComposite>();

        for(UProveKeyAndToken ukt:keysAndTokens) {
            UProveTokenComposite utc = new UProveTokenComposite();
            UProveKeyAndTokenComposite uktc = new UProveKeyAndTokenComposite();

            UProveToken token = ukt.getToken();

            utc.setH(ofup.createUProveTokenCompositeH(token.getH()));
            utc.setIsDeviceProtected(token.isDeviceProtected());
            utc.setPI(ofup.createUProveTokenCompositePI(token.getPi()));
            utc.setSigmaCPrime(ofup.createUProveTokenCompositeSigmaCPrime(token.getSigmaCPrime()));
            utc.setSigmaRPrime(ofup.createUProveTokenCompositeSigmaRPrime(token.getSigmaRPrime()));
            utc.setSigmaZPrime(ofup.createUProveTokenCompositeSigmaZPrime(token.getSigmaZPrime()));
            utc.setTI(ofup.createUProveTokenCompositeTI(token.getTi()));
            utc.setUidp(ofup.createUProveTokenCompositeUidp(token.getUidp()));

            uktc.setPrivateKey(ofup.createUProveKeyAndTokenCompositePrivateKey(ukt.getPrivateKey()));
            list.add(utc);
        }

        return list;
    }

    public List<byte[]> getUProveTokenPrivateKeys(List<UProveKeyAndToken> keysAndTokens) {
        List<byte[]> list = new ArrayList<byte[]>();

        for(UProveKeyAndToken ukt:keysAndTokens) {
            list.add(ukt.getPrivateKey());
        }
        return list;
    }

    public IssuerParameters convertIssuerParametersComposite(IssuerParametersComposite ipc, SystemParameters syspars) {
        ObjectFactory of = new ObjectFactory();
        IssuerParameters ip = of.createIssuerParameters();

        // abc:IssuerParameters:abc:SystemParameters <-- ipc.Gq (group description)
        UProveSystemParameters uproveSyspars = new UProveSystemParameters(syspars);
        if (uproveSyspars.getGroupOID() == null) {
            syspars.getAny().add(new UProveSerializer().createGroupOIDElement(ipc.getGroupName().getValue()));
        }
        ip.setSystemParameters(syspars);

        // abc:IssuerParameters:abc:HashAlgorithm (In CryptoParams blob)  <-- ipc.HashFunctionOID
        try {
            ip.setHashAlgorithm(new URI(ipc.getHashFunctionOID().getValue()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        
        CryptoParams cryptoParams = of.createCryptoParams();

        cryptoParams.getAny().add(ipc.getE().getValue());
        cryptoParams.getAny().add(ipc.getG().getValue().getBase64Binary());
        cryptoParams.getAny().add(ipc.getGd().getValue());
        cryptoParams.getAny().add(new Boolean(ipc.isIsDeviceSupported()));
        cryptoParams.getAny().add(ipc.getUidH().getValue());
        cryptoParams.getAny().add(ipc.getUidP().getValue());
        cryptoParams.getAny().add(new Boolean(ipc.isUsesRecommendedParameters()));
        cryptoParams.getAny().add(new UProveSerializer().createSubgroupGroupDescriptionCompositeElement(ipc.getGq().getValue()));
        ip.setCryptoParams(cryptoParams);

        return ip;
    }

    public IssuerParametersComposite convertIssuerParameters(IssuerParameters ip) {
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
        com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory ofup2 = new com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory();

        IssuerParametersComposite ipc = new IssuerParametersComposite();

        //SystemParameters systemParameters = ip.getSystemParameters();
        //UProveSystemParameters uproveSystemParameters = new UProveSystemParameters(systemParameters);
        
        //ipc.setGq(ofup.createIssuerParametersCompositeGq(uproveSystemParameters.getGroupDesc()));
        ipc.setHashFunctionOID(ofup.createIssuerParametersCompositeHashFunctionOID(ip.getHashAlgorithm().toString()));

        CryptoParams cryptoEvidence = ip.getCryptoParams();
        ipc.setE(ofup.createIssuerParametersCompositeE((byte[])cryptoEvidence.getAny().get(0)));
        ArrayOfbase64Binary aob = ofup2.createArrayOfbase64Binary();
        @SuppressWarnings("unchecked")
        // TODO XXX this is so broken. we should not just depend on the order of how we put the values into 
        // cryptoEvidence. This should be order into different types that we can match on.
        List<byte[]> byteArrayList = (List<byte[]>)cryptoEvidence.getAny().get(1);
        aob.getBase64Binary().addAll(byteArrayList);
        ipc.setG(ofup.createIssuerParametersCompositeG(aob));
        ipc.setGd(ofup.createIssuerParametersCompositeE((byte[])cryptoEvidence.getAny().get(2)));
        ipc.setIsDeviceSupported((Boolean)cryptoEvidence.getAny().get(3));
        //ipc.setS(ofup.createIssuerParametersCompositeE((byte[])cryptoEvidence.getAny().get(4)));
        ipc.setUidH(ofup.createIssuerParametersCompositeUidH((String)cryptoEvidence.getAny().get(4)));
        ipc.setUidP(ofup.createIssuerParametersCompositeUidP((byte[])cryptoEvidence.getAny().get(5)));
        ipc.setUsesRecommendedParameters((Boolean)cryptoEvidence.getAny().get(6));
        Element element = (Element) cryptoEvidence.getAny().get(7);
        SubgroupGroupDescriptionComposite foo = new UProveSerializer().getSubGroup("Gq", element);
        ipc.setGq(ofup.createIssuerParametersCompositeGq(foo));
        return ipc;
    }

    public FirstIssuanceMessage convertFirstIssuanceMessageComposite(FirstIssuanceMessageComposite fic) {
        FirstIssuanceMessage fi = new FirstIssuanceMessage();
        fi.setSessionKey(fic.getSessionKey().getValue());
        fi.setSigmaA(fic.getSigmaA().getValue().getBase64Binary());
        fi.setSigmaB(fic.getSigmaB().getValue().getBase64Binary());
        fi.setSigmaZ(fic.getSigmaZ().getValue());

        return fi;
    }

    public FirstIssuanceMessageComposite convertFirstIssuanceMessage(FirstIssuanceMessage fi) {
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
        com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory ofup2 = new com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory();

        FirstIssuanceMessageComposite fic = new FirstIssuanceMessageComposite();
        fic.setSessionKey(ofup.createFirstIssuanceMessageCompositeSessionKey(fi.getSessionKey()));

        ArrayOfbase64Binary aob = ofup2.createArrayOfbase64Binary();
        List<byte[]> byteArrayList = fi.getSigmaA();
        aob.getBase64Binary().addAll(byteArrayList);

        ArrayOfbase64Binary aob2 = ofup2.createArrayOfbase64Binary();
        List<byte[]> byteArrayList2 = fi.getSigmaB();
        aob2.getBase64Binary().addAll(byteArrayList2);

        fic.setSigmaA(ofup.createFirstIssuanceMessageCompositeSigmaA(aob));
        fic.setSigmaB(ofup.createFirstIssuanceMessageCompositeSigmaB(aob2));
        fic.setSigmaZ(ofup.createFirstIssuanceMessageCompositeSigmaZ(fi.getSigmaZ()));
        return fic;
    }

    public SecondIssuanceMessage convertSecondIssuanceMessageComposite(SecondIssuanceMessageComposite sic) {
        SecondIssuanceMessage si = new SecondIssuanceMessage();
        si.setSessionKey(sic.getSessionKey().getValue());
        si.setSigmaC(sic.getSigmaC().getValue().getBase64Binary());
        return si;
    }

    public SecondIssuanceMessageComposite convertSecondIssuanceMessage(SecondIssuanceMessage si) {
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
        com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory ofup2 = new com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory();

        SecondIssuanceMessageComposite sic = new SecondIssuanceMessageComposite();
        sic.setSessionKey(ofup.createSecondIssuanceMessageCompositeSessionKey(si.getSessionKey()));

        ArrayOfbase64Binary aob = ofup2.createArrayOfbase64Binary();
        List<byte[]> byteArrayList = si.getSigmaC();
        aob.getBase64Binary().addAll(byteArrayList);

        sic.setSigmaC(ofup.createSecondIssuanceMessageCompositeSigmaC(aob));

        return sic;
    }

    public ThirdIssuanceMessage convertThirdIssuanceMessageComposite(ThirdIssuanceMessageComposite tic) {
        ThirdIssuanceMessage ti = new ThirdIssuanceMessage();
        ti.setSessionKey(tic.getSessionKey().getValue());
        ti.setSigmaR(tic.getSigmaR().getValue().getBase64Binary());

        return ti;
    }

    public ThirdIssuanceMessageComposite convertThirdIssuanceMessage(ThirdIssuanceMessage ti) {
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
        com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory ofup2 = new com.microsoft.schemas._2003._10.serialization.arrays.ObjectFactory();

        ThirdIssuanceMessageComposite tic = new ThirdIssuanceMessageComposite();
        tic.setSessionKey(ofup.createThirdIssuanceMessageCompositeSessionKey(ti.getSessionKey()));

        ArrayOfbase64Binary aob = ofup2.createArrayOfbase64Binary();
        List<byte[]> byteArrayList = ti.getSigmaR();
        aob.getBase64Binary().addAll(byteArrayList);
        tic.setSigmaR(ofup.createThirdIssuanceMessageCompositeSigmaR(aob));

        return tic;
    }

    public static BigInteger getModulus(SubgroupGroupDescriptionComposite OID){
    	BigInteger foo = new BigInteger(1, OID.getP().getValue()); 
    	return foo;
    }
    
    

    private static String STRING_SHA_256 = "urn:abc4trust:1.0:encoding:string:sha-256";
    private static String ANYURI_SHA_256 = "urn:abc4trust:1.0:encoding:anyUri:sha-256";

    public byte getAttributeEncoding(URI encoding) {
        //		From the schema:
        //		      <xs:simpleType name="AttributeEncoding">
        //    			  <xs:restriction base="xs:anyURI">
        //      			  <xs:enumeration value="urn:abc4trust:1.0:encoding:string:sha-256" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:string:utf-8" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:anyUri:sha-256" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:anyUri:utf-8" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:dateTime:unix:signed" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:dateTime:unix:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:date:unix:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:date:unix:signed" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:date:since1870:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:date:since2010:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:boolean:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:integer:unsigned" />
        //			      <xs:enumeration value="urn:abc4trust:1.0:encoding:integer:signed" />
        //			    </xs:restriction>
        //			  </xs:simpleType>
        //
        // the "hashing" encodings map to 0x01, all the others map to 0x00
        // (encoded directly).
        // but it also depends on the size of the attribute value...
        String encodingString = encoding.toString();
        if (STRING_SHA_256.equalsIgnoreCase(encodingString) || ANYURI_SHA_256.equalsIgnoreCase(encodingString)) {
            return 1;
        } else {
            return 0;
        }
    }

    public URI getAlias(int inx, CredentialInToken credInToken) {
        URI credAlias = credInToken.getAlias();
        if (credAlias == null) {
            credAlias = URI.create("abc4trust.eu/credential/" + inx);
        }
        return credAlias;
    }

    //TODO Are we sure this method functions correct if the credential assignments are not
    // ordered the same way as the credentials in the ptd?
    // To me it seems we iterate over the credentials in the ptd and
    // put them in the map without verifying the corresponding credential assignment
    // is for the same type of credential

    public Map<URI, Credential> fetchCredentialsFromPresentationToken(
            PresentationTokenDescription tokenDesc,
            List<URI> credentialAssignment, CredentialManager credManager) {
        Map<URI, Credential> ret = new HashMap<URI, Credential>();

        Iterator<URI> credIterator = credentialAssignment.iterator();
        List<CredentialInToken> credentials = tokenDesc.getCredential();
        System.out.println("UProveUtils - got this list of creds from token: " + Arrays.toString(credentials.toArray()));
        for (int credIndex = 0; credIndex < credentials.size(); credIndex++) {
            CredentialInToken cd = credentials.get(credIndex);
            URI credentialAlias = this.getAlias(credIndex, cd);
            URI credentialUri = credIterator.next();
            // shouldn't we be iterating over all credentialAssignments until
            // credentialUri equals cd.getCredentialSpecUid() ?
            Credential c = null;
            try {
            	System.out.println("Trying to fetch the credential: " +credentialUri);
                c = credManager.getCredential(credentialUri);
            } catch (CredentialManagerException e) {
                e.printStackTrace();
            }
            ret.put(credentialAlias, c);
        }
        return ret;
    }

    public File getPathToUProveExe() {
        String pathToUProveExe = System.getProperty("PathToUProveExe", null);
        if((pathToUProveExe!=null) && (pathToUProveExe.length() > 0)) {
            System.out.println("Using PathToUProveExe from System Properties : " + pathToUProveExe);
            File exe = new File(pathToUProveExe);
            if(! exe.exists()) {
                System.err.println("Illegal PathToUProveExe from System Properties : " + pathToUProveExe + " - file : " + exe.getAbsolutePath());
            }
            return exe;
        } else {
            File f = new File(".");
            String[] strings = new String[] {"..", "uprove", "UProveWSDLService", "ABC4Trust-UProve", "bin","Release" };
            
            for (String s : strings) {
                f = new File(f, s);
            }
            if(! f.exists()) {
                System.err.println("Illegal Default path for UPPathToUProveExe - file : " + f.getAbsolutePath());
            }

            return f;
        }
    }
    
    public static final int UPROVE_COMMON_PORT;
    static {
        String overRideUProveServicePort = System.getProperty("UProveServicePort", null);
        if(overRideUProveServicePort != null && overRideUProveServicePort.length()>0) {
            UPROVE_COMMON_PORT = Integer.parseInt(overRideUProveServicePort);
        } else {
            UPROVE_COMMON_PORT = 32123;
        }
    }
    public int getIssuerServicePort() {
        return UPROVE_COMMON_PORT;
    }
    public int getUserServicePort() {
        return UPROVE_COMMON_PORT;
    }
    public int getVerifierServicePort() {
        return UPROVE_COMMON_PORT;
    }
    public int getInspectorServicePort() {
        return UPROVE_COMMON_PORT;
    }
    public int getReIssuerServicePort() {
    	return UPROVE_COMMON_PORT;
	}    
    public boolean startUProveServicIfNotRunning() {
        return false;
    }
    
    public static String toXml(JAXBElement<?> jaxb) throws JAXBException, SAXException {
        Object value = jaxb.getValue();
        Class<? extends Object> classValue = value.getClass();
        String name = classValue.getPackage().getName();
        JAXBContext jaxbcontext = JAXBContext.newInstance(name);
        Marshaller marshaller = jaxbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //      marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new AbcNamespace());
        //      if (validate) {
        //        marshaller.setSchema(getSchema());
        //      }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(jaxb, byteArrayOutputStream);
        return byteArrayOutputStream.toString();
    }

    public Element convertJAXBToW3DOMElement(JAXBElement<?> jaxb) {
        try {
            String jaxbString = toXml(jaxb);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder parser = domFactory.newDocumentBuilder();
            Document document = parser.parse(new ByteArrayInputStream(jaxbString.getBytes("UTF-8")));
            return document.getDocumentElement();
        } catch(Exception e) {
            throw new IllegalStateException("Failed to marshall jaxb object to W3Dom Element: " + jaxb, e);
        }
    }
    
    public String convertW3DomElementToString(Element node) throws TransformerFactoryConfigurationError, TransformerException {
    	Source source = new DOMSource(node);
    	StringWriter stringWriter = new StringWriter();
    	Result result = new StreamResult(stringWriter);
    	TransformerFactory factory = TransformerFactory.newInstance();
    	Transformer transformer = factory.newTransformer();
    	transformer.transform(source, result);
    	String xml = stringWriter.getBuffer().toString();

    	return xml;
    }
    
    public <T> T convertXmlStringToJAXB(Class<T> clazz, String xml) throws JAXBException {
    	JAXBContext jaxb = JAXBContext.newInstance(clazz.getPackage().getName());
    	javax.xml.bind.Unmarshaller unmarshaller = jaxb.createUnmarshaller();
    	JAXBElement<T> element = unmarshaller.unmarshal(new StreamSource(
    			new StringReader(xml)), clazz);

    	return element.getValue();    	
    }

    public <T> T convertW3DomElementToJAXB(Class<T> clazz, Element node) {
    	// TODO : Why is convert to string needed
    	try {
    		String xml = convertW3DomElementToString(node);
    		return convertXmlStringToJAXB(clazz, xml);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	// TODO : not working - Why not just :
    	try {
    		JAXBContext jaxb = JAXBContext.newInstance(clazz.getPackage().getName());
    		Unmarshaller unmarshaller = jaxb.createUnmarshaller();

    		JAXBElement<T> element = unmarshaller.unmarshal(new DOMSource(node), clazz);

    		return element.getValue();
    	} catch (Exception e) {
    		throw new IllegalStateException("Failed to marshall incoming W3DOM Element to type : " + clazz, e);
    	}
    }

    public Element convertIntegerParamToW3DOMElement(String elementName, Integer numberOfTokensParam) {
        Element notpElement;
        try {
            notpElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement(elementName);
            notpElement.setTextContent("" + numberOfTokensParam);
            return notpElement;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create W3DOM NumberOfTokensParam", e);
        }
    }

    public Integer convertW3DOMElementToIntegerParam(String elementName, Element element) {
        if(! element.getNodeName().equalsIgnoreCase(elementName)) {
            throw new IllegalStateException("Element with name " + elementName + " - got : " + element.getNodeName());
        }
        String tmp = element.getTextContent();
        return new Integer(tmp);
    }

    @Deprecated
    public ArrayOfstring convertAttributesToUProveAttributes(
                           IssuerParametersComposite ipc, List<Attribute> attrList, boolean dummy) {
      List<MyAttribute> list = new ArrayList<MyAttribute>();
      for(Attribute a: attrList) {
        list.add(new MyAttribute(a));
      }
      return convertAttributesToUProveAttributes(ipc, list);
    }
    
    public ArrayOfstring convertAttributesToUProveAttributes(
            IssuerParametersComposite ipc, List<MyAttribute> attrList) {

        byte[] E = ipc.getE().getValue();

        ArrayOfstring arrayOfStringAttributesParam = new ArrayOfstring();
        List<String> strings = arrayOfStringAttributesParam.getString();
        for (int inx = 0; inx < attrList.size(); inx++) {
            MyAttribute a = attrList.get(inx);
            Object attributeValue = a.getAttributeValue();
            String string = attributeValue.toString();
            byte[] bytes = string.getBytes(Charset.forName("UTF8"));
            // If the size of the attribute value is larger than Zp then we
            // should hash it into the Zp space. However, we would like to avoid
            // the hashing of smaller values for performance reasons.
            // TODO(jdn): This is a conservative estimate of Zp used in U-Prove.
            // We should create a mechanism to propagate the correct information
            // here.
            if (bytes.length >= 32) {
                E[inx] = 1;
            }
            strings.add(string);
        }
        return arrayOfStringAttributesParam;
    }	
}

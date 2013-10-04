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

package eu.abc4trust.cryptoEngine.idemix.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure.DataType;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure.IssuanceMode;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.dm.structure.PrimeEncodingFactor;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPrivateKey;
import com.ibm.zurich.idmx.showproof.Identifier.ProofMode;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.Serializer;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CarriedOverAttribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.JointlyRandomAttribute;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.UnknownAttributes;

public class IdemixUtils {
	
	private static String ENCODING_STRING_PRIME = "urn:abc4trust:1.0:encoding:string:prime";
	
    public static IssuerKeyPair convertAbceIssuerKeysToIdemixKeyPair(
            SecretKey abceSecretKey) {

        String idemixPrivateKeyAsString = (String) abceSecretKey
                .getCryptoParams().getAny().get(0);
        IssuerKeyPair issuerKeyPair = (IssuerKeyPair) Parser.getInstance().parse(idemixPrivateKeyAsString);
        return issuerKeyPair;
    }

    public static SecretKey convertIdemixIssuerPrivateKeyToAbceKey(
            IssuerPrivateKey issuerPrivateKey) {
        SecretKey secretKey = new SecretKey();
        String issuerPrivateKeyAsString = XMLSerializer.getInstance().serialize(issuerPrivateKey);
        CryptoParams cryptoParams = new CryptoParams();
        secretKey.setCryptoParams(cryptoParams);
        cryptoParams.getAny().add(issuerPrivateKeyAsString);
        return secretKey;
    }

    /**
     * Helper for converting list of attributes to idemix values (only integers!!)
     * @param sp
     * @param attrs
     * @return
     */

    public static Values createIdemixValues(com.ibm.zurich.idmx.utils.SystemParameters sp, List<MyAttribute> attrs)
    {                                                   
    	
        Values valIdmxCred = new Values(sp);
        if (attrs != null){
            Iterator<MyAttribute> it=attrs.iterator();
            while(it.hasNext())
            {
                MyAttribute myatt = it.next();
                MyAttributeValue myatval = myatt.getValue();
                if (myatval.isEnumeration())
                {           	
                	List<String> valueList = myatval.getAllowedValues().getAllowedValues();
                	HashSet<String> allowedValues = new HashSet<String>(valueList);
                	valIdmxCred.add(myatt.getType().toString(), myatval.getIntegerValueOrNull(), allowedValues);
                } else {
                	valIdmxCred.add(myatt.getType().toString(), myatval.getIntegerValueOrNull());
                }
                
            }
        }
        return valIdmxCred;
    }

    public static String serializeObjToString(final String fs,  Object obj){
        String ret = new String("");
        Serializer.serialize(fs, obj);
        DataInputStream in = null;

        try {
            File f = new File(fs);
            byte[] buffer = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            in.readFully(buffer);
            ret = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException("IO problem in fileToString", e);
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) { /* ignore it */
            }
        }

        return ret;
    }

    public static Object deserializeStrToObj(String str, final Class<? extends Object> cls){
        try{
            // Create file
            FileWriter fstream = new FileWriter("out.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(str);
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return Serializer.deserialize("out.txt", cls);
    }

    public static byte[] toByteArray(Object obj) throws IOException {
        ObjectOutputStream os = null;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
        os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
        os.flush();
        os.writeObject(obj);
        os.flush();
        byte[] sendBuf = byteStream.toByteArray();
        os.close();
        return sendBuf;

    }

    /**
     * Helper for converting ABCE Credential Template + Spec to Idemix
     * Credential Structure for Issuance
     * 
     * @param credTempl
     * @param credSpec
     * @param issuerOnlyAtts
     * @param userOnlyAtts
     * @param isOnSmartCard
     * @return
     */
    public static CredentialStructure createIdemixCredentialStructure(
            CredentialTemplate credTempl, CredentialSpecification credSpec,
            List<MyAttribute> issuerOnlyAtts, List<MyAttribute> userOnlyAtts,
            boolean isOnSmartCard) {

        Vector<AttributeStructure> theAttStructures = new Vector<AttributeStructure>();
        HashMap<String, String> featureInformation = new HashMap<String, String>();

        List<URI> issuerAtts = new ArrayList<URI>();
        List<URI> userAtts = new ArrayList<URI>();

        if (credTempl!=null){
            UnknownAttributes uas = credTempl.getUnknownAttributes();
            //identify attributes unknown to the issuer
            if (uas != null) {
                //carried over attributes: -> committed attributes
                List<CarriedOverAttribute> coas = uas.getCarriedOverAttribute();
                for (CarriedOverAttribute coa: coas){
                    userAtts.add(coa.getTargetAttributeType());
                }

                //jointly random attributes: -> not supported by Idemix but can reuse code for updating credentials
                List<JointlyRandomAttribute> jras = uas.getJointlyRandomAttribute();
                for (JointlyRandomAttribute jra : jras) {
                    //TODO: handle jointly random attributes
                    jra.getTargetAttributeType();
                }
            }
        }

        //identify attributes known to the issuer only
        if(issuerOnlyAtts !=null){
            for (MyAttribute a: issuerOnlyAtts){
                issuerAtts.add(a.getType());
            }
        }
        //identify attributes known to the user only
        if(userOnlyAtts !=null){
            for (MyAttribute a: userOnlyAtts){
                userAtts.add(a.getType());
            }
        }

        List<AttributeDescription> ads = credSpec.getAttributeDescriptions().getAttributeDescription();

        //if spec is not empty
        if (ads != null){
            AttributeStructure as = null;
            //go through spec and create attr structure for all attributes
            //not using Iterator here because we have to use index for the attribute anyway
            for (int i = 0; i< ads.size(); i++){
                AttributeDescription ad = ads.get(i);
                if (userOnlyAtts == null){
                    if(issuerAtts.contains(ad.getType())){
                    	if (ad.getEncoding().equals(URI.create(ENCODING_STRING_PRIME))){
                             as = createEnumAttributeStructure(ad, IssuanceMode.ISSUER, i);
                    	} else {
                    		as = new AttributeStructure(ad.getType().toString(), i+1, IssuanceMode.ISSUER, DataType.INT);
                    	}
                    } else {
                    	if (ad.getEncoding().equals(URI.create(ENCODING_STRING_PRIME))){
                            as = createEnumAttributeStructure(ad, IssuanceMode.HIDDEN, i);
                    	} else {
                    		as = new AttributeStructure(ad.getType().toString(), i+1, IssuanceMode.HIDDEN, DataType.INT);
                    	}
                    }
                } else if (issuerOnlyAtts == null){
                    if(userAtts.contains(ad.getType())){
                    	if (ad.getEncoding().equals(URI.create(ENCODING_STRING_PRIME))){
                            as = createEnumAttributeStructure(ad, IssuanceMode.HIDDEN, i);
                    	} else {
                    		as = new AttributeStructure(ad.getType().toString(), i+1, IssuanceMode.HIDDEN, DataType.INT);
                    	}
                    } else {
                    	if (ad.getEncoding().equals(URI.create(ENCODING_STRING_PRIME))){
                            as = createEnumAttributeStructure(ad, IssuanceMode.ISSUER, i);
                    	} else {
                    		as = new AttributeStructure(ad.getType().toString(), i+1, IssuanceMode.ISSUER, DataType.INT);
                    	}
                    }
                }
                //TODO: carried over but known to the issuer?
                theAttStructures.add(as);
            }
        }

        CredentialStructure credStruct = new CredentialStructure(
                theAttStructures, featureInformation, isOnSmartCard);
        return credStruct;
    }

    /**
     * Helper for converting ABCE Credential Description to Idemix Credential Structure for Presentation
     * @param credSpec
     * @return
     * @throws Exception 
     */
    public static CredentialStructure createIdemixCredentialStructure(CredentialSpecification credSpec) throws Exception{

        Vector<AttributeStructure> theAttStructures = new Vector<AttributeStructure>();
        HashMap<String, String> featureInformation = new HashMap<String, String>();

        List<AttributeDescription> ads = credSpec.getAttributeDescriptions().getAttributeDescription();
        //if spec is not empty
        if (ads != null){
            //go through spec and create attr structure for all attributes
            //not using Iterator here because we have to use index for the attribute anyway
            for (int i = 0; i< ads.size(); i++) { 
                AttributeDescription ad = ads.get(i);
                if (ad.getEncoding().equals(URI.create(ENCODING_STRING_PRIME))){
                    AttributeStructure as = createEnumAttributeStructure(ad, IssuanceMode.KNOWN, i);
                    theAttStructures.add(as);
                } else {
                	AttributeStructure as = new AttributeStructure(ad.getType().toString(), i+1, IssuanceMode.KNOWN, DataType.INT);
                    theAttStructures.add(as);
                }
            }
        } //else throw new Exception("The are no attributes defined in the spec");

        boolean isOnSmartcard = credSpec.isKeyBinding();

        CredentialStructure credStruct = new CredentialStructure(
                theAttStructures, featureInformation, isOnSmartcard);
        return credStruct;
    }
    
    public static AttributeStructure createEnumAttributeStructure(AttributeDescription ad, IssuanceMode im, int index){
    	AttributeStructure ret = new AttributeStructure(ad.getType().toString(), index+1, im, DataType.ENUM);
        HashMap<String, PrimeEncodingFactor> hashMapAllowedValues = new HashMap<String, PrimeEncodingFactor>();
        int numValues = 1; //we encode only one prime into the attribute value
        EnumAllowedValues eav = new EnumAllowedValues(ad);
        Map<String,BigInteger> mapPrimes = MyAttributeEncodingFactory.getEncodingForEachAllowedValue(URI.create(ENCODING_STRING_PRIME), eav);
        for (String allowedValue:eav.getAllowedValues()){
        	PrimeEncodingFactor pef = new PrimeEncodingFactor(ad.getType().toString(), allowedValue);
            pef.setPrimeFactor(mapPrimes.get(allowedValue));
        	hashMapAllowedValues.put(ad.getType().toString()+";"+allowedValue, pef);                   	
        }
        ret.setPrimeEncodedFactors(hashMapAllowedValues, numValues);
        
        return ret;
   	
    }


    public static ProofMode castIssuanceToProofMode(IssuanceMode isMode){
        switch (isMode){
        case KNOWN: return ProofMode.REVEALED;
        case HIDDEN: return ProofMode.UNREVEALED;
        case ISSUER: return ProofMode.REVEALED;
        default: return ProofMode.UNREVEALED;
        }
    }

    public static String generateIdemixCredentialStructureID(
            URI specificationUID, URI context) {
        return specificationUID.toString()+context.toString();
    }

    public static URI getNewCredentialName(URI context) {
        return URI.create(IdemixConstants.nameOfNewCredential+context.toString());
    }


}

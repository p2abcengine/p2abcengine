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

package eu.abc4trust.cryptoEngine.inspector;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.key.VEPrivateKey;
import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;
import com.ibm.zurich.idmx.ve.Decryption;
import com.ibm.zurich.idmx.ve.VerifiableEncryption;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

public class CryptoEngineInspectorImpl implements CryptoEngineInspector {
    private final KeyManager keyManager;
    private final Logger logger = Logger.getLogger(CryptoEngineInspector.class.getName());
    private final Set<URI> supportedMech = new HashSet<URI>();
    private SystemParameters systemParameters = null;
    private CredentialManager credentialManager = null;

    //
    private Map<URI, SecretKey> inspectorPrivateKeyMap = null;
    private Map<URI, CredentialSpecification> credentialSpecificationMap = null;

    /**
     * Constructor used when running on normal storage based inspector
     * @param keyManager
     * @param credentialManager
     */
    @Inject
    public CryptoEngineInspectorImpl(KeyManager keyManager,
            CredentialManager credentialManager) {
        this.keyManager = keyManager;
        this.credentialManager = credentialManager;
        // build supported list of crypto mechanisms
        this.supportedMech.add(CryptoUriUtil.getIdemixMechanism());
        try {
            this.systemParameters = this.keyManager.getSystemParameters();
        } catch (KeyManagerException e) {
            this.logger.warning("SystemParameter is not set in KeyManager");
        }
    }
    
    
    /**
     * NOTE : Raw Utility constructor which can be used in client where Inspector Keys are stored on smart card
     * 
     * @param inspectorPrivateKeyMap
     * @param credentialSpecificationMap
     * @param systemParameters
     */
    public CryptoEngineInspectorImpl(Map<URI, SecretKey> inspectorPrivateKeyMap,
                                     Map<URI, CredentialSpecification> credentialSpecificationMap) {
        this.inspectorPrivateKeyMap = inspectorPrivateKeyMap;
        this.credentialSpecificationMap = credentialSpecificationMap;

        // build supported list of crypto mechanisms
        this.supportedMech.add(CryptoUriUtil.getIdemixMechanism());
        
        // not used
        this.keyManager = null;
    }


    private VEPrivateKey createVEPrivateKey(URI pkiUri) throws URISyntaxException, InspectorSystemParameterException {
        VEPrivateKey sk = null;
        try {
            sk = new VEPrivateKey(URI.create(IdemixConstants.systemParameterId), pkiUri);
        } catch (Exception ex) {
            this.logger.warning("Idemix sustemParameterId is not found.");
            throw new InspectorSystemParameterException("Idemix systemparamenter ID not found");
        }
        return sk;
    }

    private InspectorPublicKey createNewKey(URI pkuid, VEPublicKey publicKey) {
        InspectorPublicKey key = new ObjectFactory().createInspectorPublicKey();
        try {
            key.setAlgorithmID(CryptoUriUtil.getIdemixMechanism());
            key.setPublicKeyUID(pkuid);
            CryptoParams params = new ObjectFactory().createCryptoParams();
            params.getAny().add(XMLSerializer.getInstance().serializeAsElement(publicKey));
            key.setCryptoParams(params);
            key.setVersion("1.0");
            FriendlyDescription desc = new ObjectFactory().createFriendlyDescription();
            // TODO fix description
            desc.setLang("en_US");
            desc.setValue("Inspector public key");
            key.getFriendlyInspectorDescription().add(desc);
        } catch (Exception ex) {
            this.logger.warning("Could not create new public inspector key.");
        }
        return key;
    }

    @Override
    public InspectorPublicKey setupInspectorPublicKey(int keylength,
            URI mechanism,
            URI uid) throws Exception {
        if (!this.supportedMech.contains(mechanism)) {
            throw new IllegalArgumentException("Unsupported crypto mechanishm.");
        }
        // check the KeyManager for existing key
        try {
            InspectorPublicKey insKey = this.keyManager.getInspectorPublicKey(uid);
            if (insKey != null) {
                return insKey;
            }
        } catch (KeyManagerException ex) {
            // none found. We log the issue and create a new key
            this.logger.info("Key (" + uid.toString() + ") not found in KeyManager");
        }

        // We first need to check if systemParams is set otherwise fail.
        if (this.systemParameters == null) {
            // we retry to get it via KeyManager. If this also fails we will throw exception
            this.systemParameters = this.keyManager.getSystemParameters();
            if (this.systemParameters == null) {
                throw new InspectorSystemParameterException("System Parameters not set");
            }
        }

        VEPrivateKey privateKey = this.createVEPrivateKey(uid);
        VEPublicKey publicKey = privateKey.getPublicKey();
        URI publicKeyURI = privateKey.getPublicKeyLocation();
        StructureStore.getInstance().add(uid.toString(), publicKey);

        //Create InspectorPublicKey and add params.
        InspectorPublicKey inspectorPublicKey = this.createNewKey(publicKeyURI, publicKey);

        //Create InspectorPrivateKey and store it as a byte array
        SecretKey insSecretKey = new SecretKey();
        String inspectorPrivateKeyAsString = XMLSerializer.getInstance().serialize(privateKey);
        CryptoParams cryptoParams = new CryptoParams();
        insSecretKey.setCryptoParams(cryptoParams);
        cryptoParams.getAny().add(inspectorPrivateKeyAsString);

        // Store the private key in the credentialManager
        try {
            this.credentialManager.storeInspectorSecretKey(uid, insSecretKey);
        } catch (CredentialManagerException e) {
            this.logger.warning("Could not store key (" + uid.toString() + ") in CredentialManager");
            throw e;
        }

        // Store public part in keyManager.
        try {
            this.keyManager.storeInspectorPublicKey(uid, inspectorPublicKey);
        } catch (Exception e) {
            this.logger.warning("Could not store key (" + uid.toString() + ") in KeyManager");
            throw e;
        }
        return inspectorPublicKey;
    }

    @Override
    public List<Attribute> inspect(PresentationToken t) throws Exception {
        //System.out.println("inspecting");
        if (t == null) {
            throw new IllegalArgumentException("PresentationToken was null");
        }
        //ObjectFactory of = new ObjectFactory();
       // System.out.println(XmlUtils.toXml(of.createPresentationToken(t)));
        
        List<Attribute> attr = new ArrayList<Attribute>();
        int count = 0;
        
        for(CredentialInToken cit: t.getPresentationTokenDescription().getCredential()){
            List<AttributeInToken> inspectableAttributes = getInspectableAttributes(cit);

            for(AttributeInToken da: inspectableAttributes){
                SecretKey inspectorPrivateKey = null;
                if(inspectorPrivateKeyMap!=null) {
                    inspectorPrivateKey = inspectorPrivateKeyMap.get(da.getInspectorPublicKeyUID());
                } else {
                    try{
                        inspectorPrivateKey = this.credentialManager.getInspectorSecretKey(da.getInspectorPublicKeyUID());
                    } catch (CredentialManagerException e){
                        throw new UnknownInspectorPrivateKey(e);
                    }
                }
                
                if(inspectorPrivateKey != null){ 
                    String idemixPrivateKeyAsString = (String) inspectorPrivateKey
                            .getCryptoParams().getAny().get(0);
                    VEPrivateKey vePrivKey = (VEPrivateKey) Parser.getInstance().parse(idemixPrivateKeyAsString);
                    CredentialSpecification credSpec;
                    if(credentialSpecificationMap!=null) {
                        credSpec = credentialSpecificationMap.get(cit.getCredentialSpecUID());
                    } else {
                        credSpec = keyManager.getCredentialSpecification(cit.getCredentialSpecUID());
                    }

                    AttributeDescription attributeDescription = null;
                    for(AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()){
                        if(ad.getType().equals(da.getAttributeType())) {
                            attributeDescription = ad;
                            break;
                        }
                    }

                    //get attribute type and decrypt.
                    BigInteger attributeValue = this.decryptAttribute(vePrivKey, t,
                            cit.getAlias()+da.getAttributeType().toString());

                    if(attributeValue == null) throw new RuntimeException("Failed to decrypt verifiable encryption for: "+cit.getAlias().toString()+da.getAttributeType().toString());
                    
                    Attribute newAttr = new Attribute();
                    newAttr.setAttributeDescription(attributeDescription);
                    newAttr.setAttributeUID(URI
                            .create("urn:abc4trust:inspector:revealed:attribute:"
                                    + count));
                    count++;
                    newAttr.setAttributeValue(attributeValue);
                    attr.add(newAttr);

                }else {
                    System.out.println("WARN: Could not inspect attribute: "+da.getAttributeType()+" in credential: "+cit.getCredentialSpecUID()+", inspector key not in credential manager");
                }

                
            }
        }
        return attr;
    }

    private List<AttributeInToken> getInspectableAttributes(CredentialInToken cit){
        List<AttributeInToken> ret  = new ArrayList<AttributeInToken>();
        for(AttributeInToken ait: cit.getDisclosedAttribute()){
            if(inspectorPrivateKeyMap!=null && inspectorPrivateKeyMap.containsKey(ait.getInspectorPublicKeyUID())) {
                ret.add(ait);
                continue;
            }
            try{
                if(credentialManager.getInspectorSecretKey(ait.getInspectorPublicKeyUID()) != null){
                    ret.add(ait);
                }
            }catch(Exception e){
                System.out.println("WARN: Inspector tried to inspect attribute without having the appropriate private key");
            }
        }
        
        return ret;
    }
    


    private BigInteger decryptAttribute(VEPrivateKey insPrivateKey,
            PresentationToken t, String key) throws CryptoEngineException {
        // Get the proof and use it to decrypt the attribute.
        CryptoParams cryptEnv = t.getCryptoEvidence();
        List<Object> any = cryptEnv.getAny();
        for(Object o: any){
            if(o instanceof Element){
                Element e = (Element)o;
                String elementName = e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
                if(elementName.equals("IdmxProof")){
                    Element vEncryptionsElement = ((Element)e.getElementsByTagName("VerifiableEncryptions").item(0));
                    NodeList vEncryptions =vEncryptionsElement.getChildNodes(); 
                    for(int i = 0; i< vEncryptions.getLength(); i++){
                        Element ve = (Element)vEncryptions.item(i);
                        if(key.equals(ve.getAttribute("key"))){
                            VerifiableEncryption verEncrypt = (VerifiableEncryption) Parser
                                    .getInstance().parse(ve);
                            return Decryption.decrypt(insPrivateKey, verEncrypt);
                        }
                    }
                }
            }
        }
        return null;
    }

}

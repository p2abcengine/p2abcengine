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

package eu.abc4trust.cryptoEngine.uprove.issuer;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerKeyAndParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SecondIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.ThirdIssuanceMessageComposite;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ibm.zurich.credsystem.utils.Locations;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.XMLSerializer;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.revocation.IssuerRevocation;
import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.bridging.StaticGroupParameters;
import eu.abc4trust.cryptoEngine.idemix.user.CryptoEngineUtil;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.IssuerParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * CryptoEngineIssuer implementation that uses UProve through WebServices interop. Depends on the .NET/Mono based server part running on localhost
 * 
 * @author Raphael Dobers
 */

public class UProveCryptoEngineIssuerImpl implements CryptoEngineIssuer {

    private static final URI SHA_256 = CryptoUriUtil.getHashSha256();
    private final KeyManager keyManager;
    private final CredentialManager credManager;
    @SuppressWarnings("unused")
    private final RevocationProxy revocationProxy;

    private final Map<URI, List<MyAttribute>> attributeCache;
    private final Map<URI, IssuancePolicy> issuancePolicyCache;
    private final UProveUtils utils;
    private final TokenManagerIssuer tokenManager;
    private final Map<URI, String> sessionKeyCache;
    private final UProveBindingManager binding;

    private final Map<URI, NonRevocationEvidence> nonrevocationEvidence;
    private final IssuerRevocation issuerRevocation;

    private final int numberOfCredentialsToGenerate;

    @Inject
    /**
     * A good value for numberOfCredentialsToGenerate is less 50.
     * @param keyManager
     * @param credManager
     * @param revocationProxy
     * @param tokenManager
     * @param bindingManager
     * @param numberOfCredentialsToGenerate
     */
    public UProveCryptoEngineIssuerImpl(KeyManager keyManager, CredentialManager credManager, RevocationProxy revocationProxy,
            TokenManagerIssuer tokenManager,
            UProveBindingManager bindingManager,
            @Named("NumberOfCredentialsToGenerate") int numberOfCredentialsToGenerate,
            IssuerRevocation issuerRevocation) {
        this.keyManager = keyManager;
        this.credManager = credManager;
        this.revocationProxy = revocationProxy;
        this.attributeCache = new HashMap<URI, List<MyAttribute>>();
        this.issuancePolicyCache = new HashMap<URI, IssuancePolicy>();
        this.sessionKeyCache = new HashMap<URI, String>();
        this.tokenManager = tokenManager;

        this.utils = new UProveUtils();
        // Setup WebService connection to .NET based U-Prove.
        this.binding = bindingManager;
        this.binding.setupBiding("Issuer");

        this.numberOfCredentialsToGenerate = numberOfCredentialsToGenerate;
        this.nonrevocationEvidence = new HashMap<URI, NonRevocationEvidence>();
        this.issuerRevocation = issuerRevocation;

        System.out.println("Hello from UProveCryptoEngineIssuerImpl()");
    }

    private boolean isRevocationHandlePresent(List<Attribute> attributes){
        System.out.println("Inside isRevocationHandlePresent. ");
        for(Attribute att: attributes){
            URI uid = att.getAttributeDescription().getType();
            System.out.println("Comparing: " + uid + " to: "+RevocationConstants.REVOCATION_HANDLE);
            if(uid.equals(RevocationConstants.REVOCATION_HANDLE)){
                return true;
            }
        }
        return false;
    }

    @Override
    public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip, List<Attribute> atts, URI ctxt) {
        try {
            SystemParameters syspars;
            try {
                syspars = this.keyManager.getSystemParameters();
            } catch (KeyManagerException ex) {
                throw new RuntimeException(ex);
            }

            int keyLength = new UProveSystemParameters(syspars).getKeyLength();
            // TODO set the correct groupId, pin etc etc based on URI.
            String sessionKey = this.utils.getSessionKey(this.binding, keyLength);
            // add session id to cache
            this.sessionKeyCache.put(ctxt, sessionKey);

            URI credentialSpecUID = ip.getCredentialTemplate()
                    .getCredentialSpecUID();
            CredentialSpecification cd;
            try {
                cd = this.keyManager
                        .getCredentialSpecification(credentialSpecUID);
            } catch (KeyManagerException ex) {
                throw new CryptoEngineException(ex);
            }

            if (cd.isRevocable()) {
                NonRevocationEvidence nre;
                if(!this.isRevocationHandlePresent(atts)){
                    nre = this.issuerRevocation
                            .addRevocationHandleAttribute(atts, ip, ctxt);
                }else{
                    //If the revocation handle is already present, we do not need the non-revocation evidence,
                    //so create an empty xml.
                    nre = new ObjectFactory().createNonRevocationEvidence();
                }
                this.nonrevocationEvidence.put(ctxt, nre);
            }

            // Get the secret key from CredentialManager...
            SecretKey secretKey = null;
            URI issuerParametersUid = ip.getCredentialTemplate().getIssuerParametersUID();
            try {
                secretKey = this.credManager.getIssuerSecretKey(issuerParametersUid);
            } catch (CredentialManagerException e) {
                e.printStackTrace();
                throw new IllegalStateException("no secret key for issuer parameters: " + issuerParametersUid.toString());
            }

            IssuerParameters issuerParameters;
            issuerParameters = this.keyManager
                    .getIssuerParameters(issuerParametersUid);
            //Remove Idemix issuer PK
            if(issuerParameters.getCryptoParams().getAny().size() > 0){
            	issuerParameters.getCryptoParams().getAny().remove(issuerParameters.getCryptoParams().getAny().size()-1);
            }

            IssuerParametersComposite ipc = this.utils
                    .convertIssuerParameters(issuerParameters);
            this.binding.verifyIssuerParameters(ipc, sessionKey);

            // Set the IssuerPrivateKey on the UProve Issuer and retrieve associated sessionKey for this Issuance session
            if(secretKey != null) {
                Element e = (Element) secretKey
                        .getCryptoParams().getAny().get(0);
                UProveSerializer serializer = new UProveSerializer();
                byte[] issuerPrivateKey = serializer.getBytes(e);
                this.binding.setIssuerPrivateKey(issuerPrivateKey, sessionKey);
            }

            this.fillInAttributeCache(ctxt, atts, cd);
            this.issuancePolicyCache.put(ctxt, ip);
            ObjectFactory of = new ObjectFactory();
            IssuanceMessage ret = of.createIssuanceMessage();

            ret.getAny().add(of.createIssuancePolicy(ip));
            ret.setContext(ctxt);

            IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();
            imab.setLastMessage(false);
            imab.setIssuanceMessage(ret);
            return imab;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void fillInAttributeCache(URI ctxt, List<Attribute> atts, CredentialSpecification credSpec) {
        List<AttributeDescription> attributeList = credSpec.getAttributeDescriptions().getAttributeDescription();
        List<MyAttribute> list = new ArrayList<MyAttribute>();
        try{
            for(int i = 0; i < attributeList.size(); i++){
                AttributeDescription desc1 = attributeList.get(i);
                boolean foundEqual = false;
                int index = 0;
                while(!foundEqual){
                    Attribute at = atts.get(index++);
                    AttributeDescription desc2 = at.getAttributeDescription();
                    if(this.isAttributeDescriptionEqual(desc1, desc2)){
                        MyAttribute myAtt = new MyAttribute(at);
                        if(at.getAttributeDescription().getType().equals(RevocationConstants.REVOCATION_HANDLE)){
                            list.add(0, myAtt);
                        }else{
                            list.add(myAtt);
                        }
                        foundEqual = true;
                    }
                }
            }
        }catch(Exception e){
            //TODO: Is the extra attribute maybe the revocation handle? If so, we can check for that and not cast a warning.
            //Allthough I think the tests are doing strange stuff they should not do.
            boolean revocationHandleFound = false;
            list.clear();
            for(Attribute at: atts){
                MyAttribute myAtt = new MyAttribute(at);
                if(at.getAttributeDescription().getType().equals(RevocationConstants.REVOCATION_HANDLE)
                        && (atts.size() == (attributeList.size()+1))){
                    revocationHandleFound = true;
                    list.add(0, myAtt); //rev. handle always at first index
                }else{
                    list.add(myAtt);
                }
            }
            if(!revocationHandleFound){
                System.err.println("WARNING: Not all attributes was present in the credential specification. Still adding them.");
            }
        }
        this.attributeCache.put(ctxt, list);
    }

    private boolean isAttributeDescriptionEqual(AttributeDescription desc1, AttributeDescription desc2){
        if(!desc1.getAllowedValue().equals(desc2.getAllowedValue())){
            return false;
        }
        if(!desc1.getDataType().equals(desc2.getDataType())){
            return false;
        }
        if(!desc1.getEncoding().equals(desc2.getEncoding())){
            return false;
        }
        if(!desc1.getType().equals(desc2.getType())){
            return false;
        }
        List<FriendlyDescription> f1 = desc1.getFriendlyAttributeName();
        List<FriendlyDescription> f2 = desc2.getFriendlyAttributeName();
        if(f1.size() != f2.size()){
            return false;
        }
        for(int i = 0; i < f1.size(); i++){
            if(!f1.get(i).getLang().equals(f2.get(i).getLang()) ||
                    !f1.get(i).getValue().equals(f2.get(i).getValue())){
                return false;
            }
        }
        return true;
    }

    @Override
    public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m) {
        ObjectFactory of = new ObjectFactory();
        org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory uproveOf = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();

        URI context = m.getContext();
        String sessionKey = this.sessionKeyCache.get(context);

        IssuancePolicy ip = this.issuancePolicyCache.get(context);
        IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();

        Object o = null;

        if (m != null) {
            o = m.getAny().get(0);
        }


        // This is for the first step in the UProve issuance protocol, the issuer is asked for a FirstIssuanceMessage that will be passed to the user side
        // SecondIssuanceMessage is marshalled to W3Dom Element
        if(!(o instanceof Element)) {
            IssuerParameters issuerParameters = null;

            // For the ABC4Trust advanced Issuance setting, we fetch an IssuanceToken from the IssuanceMessage. For now it is not supported in UProve, and hence discarded.
            IssuanceToken it = null;

            try {
                it = (IssuanceToken) XmlUtils.unwrap(o, IssuanceToken.class);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if(it != null) {
                // TODO: Do the advanced setting

                // Check that token satisfies policy
                // TODO: This part should actually be handled by the issuance manager
                MyPresentationPolicy mypp = new MyPresentationPolicy(ip.getPresentationPolicy());
                IssuanceTokenDescription issuanceTokenDescription = it.getIssuanceTokenDescription();
                PresentationTokenDescription presentationTokenDescription = issuanceTokenDescription.getPresentationTokenDescription();
                if (!mypp.isSatisfiedBy(presentationTokenDescription, this.tokenManager, this.keyManager)) {
                    throw new RuntimeException("Issuance Token not satisfied");
                }
                this.tokenManager.storeToken(it);
            }

            try {
                issuerParameters = this.keyManager.getIssuerParameters(ip.getCredentialTemplate().getIssuerParametersUID());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Convert IssuerParameters to IssuerParametersComposite for UProve Webservice interop compatibility
            IssuerParametersComposite ipc = this.utils.convertIssuerParameters(issuerParameters);

            List<MyAttribute> attrList = this.attributeCache.get(context);
            ArrayOfstring arrayOfStringAttributesParam = this.utils
                    .convertAttributesToUProveAttributes(ipc, attrList);

            // ipc = this.utils.updateIssuerParametersBasedOnAttributtes(ipc,
            // attrList);
            this.binding.verifyIssuerParameters(ipc, sessionKey);

            Integer numberOfTokensParam = new Integer(
                    this.numberOfCredentialsToGenerate); // get as many as
            // possible without
            // noticeable perf
            // impact

            // Prover side asks issuer for first message
            System.out.println("Prover asks issuer for the first message...");
            FirstIssuanceMessageComposite firstMessage;

            if (sessionKey != null) {
                this.binding.verifyIssuerParameters(ipc, sessionKey);
                byte[] hd = null;
                if(it != null){
                    org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
                    try {
                        System.out.println(XmlUtils.toXml(of.createIssuanceToken(it)));
                    } catch (JAXBException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SAXException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (!it.getCryptoEvidence().getAny().isEmpty()) {
                        hd = DatatypeConverter.parseBase64Binary(((Element)it.getCryptoEvidence().getAny().get(0)).getTextContent());
                    }
                }

                byte[] virtualCardSecretArray = new byte[] {42};
                BigInteger virtualCardSecret = StaticGroupParameters.Gd.modPow(new BigInteger(1, virtualCardSecretArray), StaticGroupParameters.p);

                firstMessage = this.binding.getFirstMessage(
                        arrayOfStringAttributesParam, ipc, numberOfTokensParam,
                        sessionKey, hd != null ? hd : virtualCardSecret.toByteArray());
            }
            else {
                throw new RuntimeException("SessionKey has not been set properly in initIssuanceProtocol().");
            }

            // Map firstMessage to abc:IssuanceMessage new xml elements along with the attributes, numberOfTokens and the IssuancePolicy (For Prover side)
            IssuanceMessage ret = of.createIssuanceMessage();
            // uprove jaxb - convert to W3Dom
            ret.getAny().add(this.utils.convertJAXBToW3DOMElement(uproveOf.createFirstIssuanceMessageComposite(firstMessage)));

            // abce jaxb - use directly
            ret.getAny().add(of.createIssuancePolicy(ip));

            // create w3dom element for integer
            ret.getAny().add(this.utils.convertIntegerParamToW3DOMElement("NumberOfTokensParam", numberOfTokensParam));

            // add attributes individually
            List<MyAttribute> attributeList = this.attributeCache.get(context);
            for(MyAttribute a: attributeList) {
                ret.getAny().add(of.createAttribute(a.getXmlAttribute()));
            }
            ret.setContext(context);

            try{System.out.println("First message from issuer to user: " + XmlUtils.toXml(of.createIssuanceMessage(ret)));}catch(Exception e){e.printStackTrace();}

            imab.setLastMessage(false);
            imab.setIssuanceMessage(ret);
        }
        // This is for the second step in the UProve issuance protocol for the issuer side, the user side has returned a SecondIssuanceMessage object
        else if(o instanceof Element) {
            Element element = (Element) o;
            String elementName = element.getLocalName() != null ? element.getLocalName() : element.getNodeName();
            if(elementName.equalsIgnoreCase("SecondIssuanceMessageComposite")) {
                SecondIssuanceMessageComposite secondIssuanceMessageComposite = this.utils.convertW3DomElementToJAXB(SecondIssuanceMessageComposite.class, element);

                // Prover side asks issuer for third message based on the second
                // message
                System.out
                .println("Prover asks issuer for the third message based on the second message...");

                IssuerParameters issuerParameters = null;
                CredentialSpecification credSpec = null;

                List<MyAttribute> attributeList = null;

                attributeList = this.attributeCache.get(context);

                CredentialTemplate credentialTemplate;
                try {
                    credentialTemplate = ip.getCredentialTemplate();
                    issuerParameters = this.keyManager
                            .getIssuerParameters(credentialTemplate
                                    .getIssuerParametersUID());
                    credSpec = this.keyManager
                            .getCredentialSpecification(issuerParameters
                                    .getCredentialSpecUID());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "failed to get issuer params from keyManager", e);
                }

                MyCredentialSpecification myCredSpec = new MyCredentialSpecification(
                        credSpec);

                CredentialDescription cd = of.createCredentialDescription();

                CryptoEngineUtil.setupCredentialDescription(credSpec,
                        attributeList, credentialTemplate, myCredSpec, cd);

                IssuerParametersComposite ipc = this.utils
                        .convertIssuerParameters(issuerParameters);
                this.binding.verifyIssuerParameters(ipc, sessionKey);

                ThirdIssuanceMessageComposite thirdMessage = this.binding.getThirdMessage(secondIssuanceMessageComposite, sessionKey);

                IssuanceMessage ret = of.createIssuanceMessage();
                ret.getAny().add(this.utils.convertJAXBToW3DOMElement(uproveOf.createThirdIssuanceMessageComposite(thirdMessage)));
                ret.getAny().add(of.createCredentialDescription(cd));
                ret.setContext(context);

                // Add serialized non-revocation evidence
                NonRevocationEvidence nre = this.nonrevocationEvidence
                        .get(context);
                if (nre != null) {
                    ret.getAny().add(of.createNonRevocationEvidence(nre));
                }
                imab.setLastMessage(true);
                imab.setIssuanceMessage(ret);
            } else {
                throw new IllegalStateException("IssuanceMessage from User could not be handled : unknown Element !" + o.getClass() + " : "+ element + ":" +elementName);
            }
        } else {
            throw new IllegalStateException("Unrecognized type as 1st object in IssuanceMessage : " + o.getClass());
        }
        this.binding.printDebugOutputFromUProve();

        return imab;
    }

    static int count = 0;
    
    @Override
    public IssuerParametersAndSecretKey setupIssuerParameters(CredentialSpecification credspec,
            SystemParameters syspars, URI uid, URI hash, URI revParsUid, List<FriendlyDescription> friendlyDescriptions) {

        // Setup issuer parameters

        if (hash.compareTo(SHA_256) == 0) {
            hash = URI.create("SHA-256");
        } else {
            throw new RuntimeException("Unsupported hash algorithm: \"" + hash
                    + "\"");
        }

        String uniqueIdentifier = uid.toString();
        // TODO FIX find the correct URI id or session.



        //String sessionKey = this.sessionKeyCache.get(uid);

        Object[] adArray = credspec.getAttributeDescriptions().getAttributeDescription().toArray();
        byte[] attributeEncoding = new byte[0];
        if (adArray != null) {
            attributeEncoding = new byte[adArray.length];
            for (int i = 0; i < adArray.length; i++) {
                AttributeDescription attributeDescription = (AttributeDescription) adArray[i];
                URI encoding = attributeDescription.getEncoding();
                attributeEncoding[i] = this.utils.getAttributeEncoding(encoding);
            }
        }

        String sessionKey = null;
        if (!this.sessionKeyCache.containsKey(uid)) {
            int keyLength = new UProveSystemParameters(syspars).getKeyLength();
            sessionKey = this.utils.getSessionKey(this.binding, keyLength);
            this.sessionKeyCache.put(uid, sessionKey);
        } else {
            sessionKey = this.sessionKeyCache.get(uid);
        }
        IssuerKeyAndParametersComposite issuerKeyAndParametersComposite = this.binding
                .setupIssuerParameters(uniqueIdentifier, attributeEncoding,
                        hash.toString(), sessionKey);

        if (issuerKeyAndParametersComposite == null) {
            throw new RuntimeException(
                    "U-Prove Binding failed to generate issuer parameters.");
        }

        // Fetch the issuer parameters...
        IssuerParametersComposite ipc = issuerKeyAndParametersComposite.getIssuerParameters().getValue();

        // System.out
        // .println(">>>>>>>  " + Arrays.toString(ipc.getE().getValue()));

        // Convert IssuerParametersComposite to serializable version
        IssuerParameters ip = this.utils.convertIssuerParametersComposite(ipc, syspars);
        if(friendlyDescriptions != null){
            for(FriendlyDescription fd : friendlyDescriptions){
                ip.getFriendlyIssuerDescription().add(fd);
            }
        }

        ip.setAlgorithmID(CryptoUriUtil.getUproveMechanism());
        ip.setCredentialSpecUID(credspec.getSpecificationUID());
        ip.setKeyBindingInfo(null);
        ip.setParametersUID(uid);
        ip.setRevocationParametersUID(revParsUid);
        ip.setVersion("UPROVE:" + IssuerParametersUtil.generateVersionNumber()); // "1.0");

        // get an Idemix public key for handling predicates
        IssuerPublicKey ipk = this.getIdemixPublicKey(syspars, uid, adArray.length);
        CryptoParams cp = ip.getCryptoParams();
        cp.getAny().add(XMLSerializer.getInstance().serializeAsElement(ipk));
        ip.setCryptoParams(cp);
        

        // continue with normal UProve key generation
        IssuerParametersAndSecretKey ret = new IssuerParametersAndSecretKey();
        byte[] issuerPrivateKey = issuerKeyAndParametersComposite.getPrivateKey().getValue();
        SecretKey secretKey = new SecretKey();
        secretKey.setCryptoParams(new CryptoParams());

        UProveSerializer serializer = new UProveSerializer();
        Node issuerPrivateKeyElement = serializer.createByteElement(
                "IssuerPrivatKey", issuerPrivateKey);
        secretKey.getCryptoParams().getAny().add(issuerPrivateKeyElement);

        // Save the secret key in CredentialManager...
        try {
            this.credManager.storeIssuerSecretKey(uid, secretKey);
        } catch (CredentialManagerException e) {
            e.printStackTrace();
        }

        ret.issuerSecretKey = secretKey;
        ret.issuerParameters = ip;

        return ret;
    }

    private IssuerPublicKey getIdemixPublicKey(SystemParameters syspars, URI uid, int numberOfAttributes){
        IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(syspars);
        com.ibm.zurich.idmx.utils.SystemParameters sp = idemixSystemParameters.getSystemParameters();
        GroupParameters gp = idemixSystemParameters.getGroupParameters();

        // Load system, group and issuer parameters to Idemix StructureStore
        Locations.init(IdemixConstants.systemParameterId, sp);
        Locations.init(IdemixConstants.groupParameterId, gp);

        IssuerKeyPair issuerKey =
                new IssuerKeyPair(uid, URI.create(IdemixConstants.groupParameterId), numberOfAttributes,
                        IdemixConstants.EPOCH_LENGTH);
        return issuerKey.getPublicKey();
     }
    
    @Override
    public SystemParameters setupSystemParameters(int keyLength, URI cryptographicMechanism) {
        ObjectFactory of = new ObjectFactory();

        String cryptographicMechanismStr = cryptographicMechanism.toString();
        if(!cryptographicMechanismStr.equals(CryptoUriUtil.getUproveString())) {
            throw new UnsupportedOperationException(cryptographicMechanismStr + " crypto mechanism not supported for UProve cryptoengine");
        }

        SystemParameters ret = of.createSystemParameters();
        ret.setVersion("1.0");
        UProveSerializer serializer = new UProveSerializer();
        Node uprove = serializer.createKeyLengthElement(keyLength);
        ret.getAny().add(uprove);

        return ret;
    }

    @Override
    public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid)
            throws Exception {
        return this.tokenManager.getIssuanceLogEntry(issuanceEntryUid);
    }
}

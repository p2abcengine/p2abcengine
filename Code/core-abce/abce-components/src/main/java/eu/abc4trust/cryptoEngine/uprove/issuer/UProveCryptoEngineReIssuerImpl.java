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

package eu.abc4trust.cryptoEngine.uprove.issuer;

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE;
import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_DATA_TYPE;
import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_ENCODING;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SecondIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.ThirdIssuanceMessageComposite;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.verifier.policyTokenMatcher.PolicyTokenMatcherVerifierImpl;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.bridging.StaticGroupParameters;
import eu.abc4trust.cryptoEngine.idemix.user.CryptoEngineUtil;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineReIssuer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class UProveCryptoEngineReIssuerImpl implements CryptoEngineReIssuer{

    //private final UProveCryptoEngineIssuerImpl issuer;
    private final PolicyTokenMatcherVerifierImpl verifier;
    private final KeyManager keyManager;
    private final CredentialManager credManager;
    private final Random rand;
    private final UProveUtils utils;
    private final Map<URI, String> sessionKeyCache;
    private final Map<URI, IssuancePolicy> issuancePolicyCache;
    private final Map<URI, List<MyAttribute>> attributeCache;
    private final UProveBindingManager binding;
    private final int numberOfCredentialsToGenerate;

    private final ObjectFactory of = new ObjectFactory();

    @Inject
    public UProveCryptoEngineReIssuerImpl(PolicyTokenMatcherVerifierImpl verifier,
            UProveBindingManager binding,
            CredentialManager credManager,
            @Named("NumberOfCredentialsToGenerate") int numberOfCredentialsToGenerate,
            KeyManager keyManager){
        System.out.println("Hello from UProveCryptoEngineReIssuerImpl");
        this.verifier = verifier;
        this.keyManager = keyManager;
        this.credManager = credManager;
        this.rand = new SecureRandom();
        this.numberOfCredentialsToGenerate = numberOfCredentialsToGenerate;
        this.utils = new UProveUtils();
        this.binding = binding;
        this.binding.setupBiding("Issuer");

        this.sessionKeyCache = new HashMap<URI, String>();
        this.issuancePolicyCache = new HashMap<URI, IssuancePolicy>();
        this.attributeCache = new HashMap<URI, List<MyAttribute>>();
    }

    @Override
    public IssuanceMessageAndBoolean initReIssuanceProtocol(
            IssuancePolicy issuancePolicy, URI context) throws CryptoEngineException{
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
        this.sessionKeyCache.put(context, sessionKey);

        this.issuancePolicyCache.put(context, issuancePolicy);

        // Get the secret key from CredentialManager...
        SecretKey secretKey = null;
        URI issuerParametersUid = issuancePolicy.getCredentialTemplate().getIssuerParametersUID();
        try {
            secretKey = this.credManager.getIssuerSecretKey(issuerParametersUid);
        } catch (CredentialManagerException e) {
            e.printStackTrace();
            throw new IllegalStateException("no secret key for issuer parameters: " + issuerParametersUid.toString());
        }

        IssuerParameters issuerParameters;
        try {
            issuerParameters = this.keyManager
                    .getIssuerParameters(issuerParametersUid);
        } catch (KeyManagerException e1) {
            e1.printStackTrace();
            throw new CryptoEngineException(e1);
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


        IssuanceMessage ret = this.of.createIssuanceMessage();
        ret.getAny().add(this.of.createIssuancePolicy(issuancePolicy));
        ret.setContext(context);

        IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();
        imab.setLastMessage(false);
        imab.setIssuanceMessage(ret);
        return imab;
    }

    @Override
    public IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m) throws CryptoEngineException {
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

            List<MyAttribute> attrList = null;

            if(it != null) {
                // Check that token satisfies policy
                // TODO: This part should actually be handled by the issuance manager
                MyPresentationPolicy mypp = new MyPresentationPolicy(ip.getPresentationPolicy());
                IssuanceTokenDescription issuanceTokenDescription = it.getIssuanceTokenDescription();
                PresentationTokenDescription presentationTokenDescription = issuanceTokenDescription.getPresentationTokenDescription();
                URI credSpecUID = issuanceTokenDescription.getCredentialTemplate().getCredentialSpecUID();
                CredentialSpecification credSpec;
                try {
                    credSpec = this.keyManager.getCredentialSpecification(credSpecUID);
                } catch (KeyManagerException e) {
                    throw new CryptoEngineException(e);
                }
                //                if (!mypp.isSatisfiedBy(presentationTokenDescription, this.tokenManager)) {
                //                    throw new RuntimeException("Issuance Token not satisfied");
                //                }
                //                this.tokenManager.storeToken(it);

                //                PresentationPolicyAlternatives ppa = ip.getPresentationPolicy().getPolicyUID()
                //                PresentationTokenDescription ptd;
                //    			try {
                //    				ptd = verifier.verifyTokenAgainstPolicy(ppa, pt, false);
                //    			} catch (TokenVerificationException e) {
                //    				throw new CryptoEngineException(e);
                //    			}
                //    			this.printXML(ptd);
                attrList = new ArrayList<MyAttribute>();
                System.out.println("filling up attribute list");
                this.fillInAttributes(presentationTokenDescription, credSpec, attrList);
                System.out.println("Attributes found: " + attrList.size());
                this.attributeCache.put(context, attrList);
            }else{
                System.err.println("IssuanceToken was null!");
            }

            try {
                issuerParameters = this.keyManager.getIssuerParameters(ip.getCredentialTemplate().getIssuerParametersUID());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Convert IssuerParameters to IssuerParametersComposite for UProve Webservice interop compatibility
            IssuerParametersComposite ipc = this.utils.convertIssuerParameters(issuerParameters);

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
                        System.out.println(XmlUtils.toXml(this.of.createIssuanceToken(it)));
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
            IssuanceMessage ret = this.of.createIssuanceMessage();
            // uprove jaxb - convert to W3Dom
            ret.getAny().add(this.utils.convertJAXBToW3DOMElement(uproveOf.createFirstIssuanceMessageComposite(firstMessage)));

            // abce jaxb - use directly
            ret.getAny().add(this.of.createIssuancePolicy(ip));

            // create w3dom element for integer
            ret.getAny().add(this.utils.convertIntegerParamToW3DOMElement("NumberOfTokensParam", numberOfTokensParam));

            // add attributes individually
            List<MyAttribute> attributeList = this.attributeCache.get(context);
            for(MyAttribute a: attributeList) {
                ret.getAny().add(this.of.createAttribute(a.getXmlAttribute()));
            }
            ret.setContext(context);

            try{System.out.println("First Uprove message from issuer to user: \n" + XmlUtils.toXml(this.of.createIssuanceMessage(ret)));}
            catch(Exception e){e.printStackTrace();}

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

                CredentialDescription cd = this.of.createCredentialDescription();

                CryptoEngineUtil.setupCredentialDescription(credSpec,
                        attributeList, credentialTemplate, myCredSpec, cd);

                IssuerParametersComposite ipc = this.utils
                        .convertIssuerParameters(issuerParameters);
                this.binding.verifyIssuerParameters(ipc, sessionKey);

                ThirdIssuanceMessageComposite thirdMessage = this.binding.getThirdMessage(secondIssuanceMessageComposite, sessionKey);

                IssuanceMessage ret = this.of.createIssuanceMessage();
                ret.getAny().add(this.utils.convertJAXBToW3DOMElement(uproveOf.createThirdIssuanceMessageComposite(thirdMessage)));
                ret.getAny().add(this.of.createCredentialDescription(cd));
                ret.setContext(context);

                // Add serialized non-revocation evidence
                //We should just be able to add an empty one here, as the evidence will not change in the new credential
                NonRevocationEvidence nre_ = new NonRevocationEvidence();
                NonRevocationEvidence nre = this.of.createNonRevocationEvidence(nre_).getValue();
                //ret.getAny().add(nre);

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

    private void fillInAttributes(PresentationTokenDescription ptd, CredentialSpecification credSpec,
            List<MyAttribute> atts) {
        List<AttributeDescription> attributeList = credSpec.getAttributeDescriptions().getAttributeDescription();
        for(CredentialInToken cit : ptd.getCredential()){
            URI credSpecUidInToken = cit.getCredentialSpecUID();
            if(credSpecUidInToken.equals(credSpec.getSpecificationUID())){
                //Happy as the attribute credSpec and credSpec matches.
                for(AttributeInToken ait : cit.getDisclosedAttribute()){
                    URI attType = ait.getAttributeType();
                    boolean foundCommonType = false;
                    for(AttributeDescription attDesc : attributeList){
                        URI type = attDesc.getType();
                        if(type.equals(attType)){
                            foundCommonType = true;
                            Attribute att = this.of.createAttribute();
                            att.setAttributeUID(URI.create("" + this.rand.nextLong()));
                            att.setAttributeDescription(attDesc);
                            att.setAttributeValue(ait.getAttributeValue());
                            if(this.isAttributeRevocationHandle(att)){
                                //Always put revocation handles at position 0.
                                atts.add(0, new MyAttribute(att));
                            }else{
                                atts.add(new MyAttribute(att));
                            }
                        }
                    }
                    if(!foundCommonType){
                        throw new RuntimeException("Re-Issuance: Did not find any common type amongst attributes. Aborting!");
                    }
                }
            }
        }
    }

    private boolean isAttributeRevocationHandle(Attribute att){
        boolean found = false;
        if (att.getAttributeDescription().getType()
                .compareTo(REVOCATION_HANDLE) == 0) {
            found = true;
            URI dataType = att.getAttributeDescription().getDataType();
            if (dataType.compareTo(REVOCATION_HANDLE_DATA_TYPE) != 0) {
                throw new RuntimeException("Revocation Handle Datatype is incorrect: \""
                        + dataType + "\"");
            }
            URI encoding = att.getAttributeDescription().getEncoding();
            if (encoding.compareTo(REVOCATION_HANDLE_ENCODING) != 0) {
                throw new RuntimeException("Revocation Handle Encoding is incorrect: \""
                        + encoding + "\"");
            }
        }
        return found;
    }

    private void printXML(Object o){
        try {
            System.out.println("===================\n");
            if(o instanceof IssuancePolicy){
                System.out.println("InitReIssuanceProtocol - issuancePolicy: " + XmlUtils.toXml(this.of.createIssuancePolicy((IssuancePolicy)o)));
            }else if(o instanceof PresentationPolicyAlternatives){
                System.out.println("InitReIssuanceProtocol - ppa: " + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives((PresentationPolicyAlternatives)o)));
            }else if(o instanceof PresentationToken){
                System.out.println("InitReIssuanceProtocol - presentationToken: " + XmlUtils.toXml(this.of.createPresentationToken(((PresentationToken)o))));
            }else if(o instanceof CredentialSpecification){
                System.out.println("InitReIssuanceProtocol - credSpec: " + XmlUtils.toXml(this.of.createCredentialSpecification(((CredentialSpecification)o))));
            }else if(o instanceof PresentationTokenDescription){
                System.out.println("InitReIssuanceProtocol - presentationTokenDescription: " + XmlUtils.toXml(this.of.createPresentationTokenDescription(((PresentationTokenDescription)o))));
            }else if(o instanceof Attribute){
                System.out.println("InitReIssuanceProtocol - attribute: " + XmlUtils.toXml(this.of.createAttribute(((Attribute)o))));
            }else if(o instanceof MyAttribute){
                System.out.println("InitReIssuanceProtocol - attribute: " + XmlUtils.toXml(this.of.createAttribute(((MyAttribute)o).getXmlAttribute())));
            }else if(o instanceof IssuanceMessageAndBoolean){
                System.out
                .println("InitReIssuanceProtocol - IssuanceMessageAndBoolean: "
                        + XmlUtils.toXml(this.of
                                .createIssuanceMessage(((IssuanceMessageAndBoolean) o)
                                        .getIssuanceMessage())));
            }
        } catch (JAXBException e) {
            System.err.println("WARNING: Could not print XML for object: " + o);
        } catch (SAXException e) {
            System.err.println("WARNING: Could not print XML for object: " + o);
        } finally {
            System.out.println("\n===================");
        }
    }

    @Override
    public SystemParameters setupSystemParameters(int keyLength,
            URI cryptographicMechanism) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IssuerParametersAndSecretKey setupIssuerParameters(
            CredentialSpecification credspec, SystemParameters syspars,
            URI uid, URI hash, URI revParsUid) {
        // TODO Auto-generated method stub
        return null;
    }

}

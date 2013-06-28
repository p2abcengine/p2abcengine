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

package eu.abc4trust.cryptoEngine.idemix.issuer;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.ibm.zurich.credsystem.utils.Locations;
import com.ibm.zurich.idmx.dm.MessageToSign;
import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.issuance.AdvancedIssuanceSpec;
import com.ibm.zurich.idmx.issuance.AdvancedIssuer;
import com.ibm.zurich.idmx.issuance.Message;
import com.ibm.zurich.idmx.issuance.Message.IssuanceProtocolValues;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPrivateKey;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.VerifierInput;
import com.ibm.zurich.idmx.showproof.sval.SValue;
import com.ibm.zurich.idmx.smartcard.IdemixSmartcardHelper;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.revocation.IssuerRevocation;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.bridging.StaticGroupParameters;
import eu.abc4trust.cryptoEngine.idemix.user.CryptoEngineUtil;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixClaim;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixProofSpecGenerator;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixUtils;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.util.IssuerParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.smartcardManager.AbcSmartcardHelper;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.util.PolicyTranslator;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInLogEntry;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
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
 * CryptoEngineIssuer implementation that uses Idemix library for credential handling.
 * 
 * @author mdu
 * 
 */

public class IdemixCryptoEngineIssuerImpl implements CryptoEngineIssuer {


    private final Map<URI, List<MyAttribute>> attributeCache;
    private final Map<URI, IssuancePolicy> issuancePolicyCache;
    private final Map<URI, AdvancedIssuer> issuerCache;

    private final KeyManager keyManager;
    private final TokenManagerIssuer tokenManager;
    private final IdemixSmartcardHelper smartcardHelper;

    private final CredentialManager credentialManager;
    private final Map<URI, NonRevocationEvidence> nonrevocationEvidence;
    private final IssuerRevocation issuerRevocation;
    private final ContextGenerator contextGen;


    @Inject
    public IdemixCryptoEngineIssuerImpl(KeyManager keyManager,
            RevocationProxy revocationProxy, TokenManagerIssuer tokenManager,
            CredentialManager credentialManager,
            IssuerRevocation issuerRevocation, ContextGenerator contextGen) {
        this.keyManager = keyManager;
        this.attributeCache = new HashMap<URI, List<MyAttribute>>();
        this.issuancePolicyCache = new HashMap<URI, IssuancePolicy>();
        this.issuerCache = new HashMap<URI, AdvancedIssuer>();
        this.smartcardHelper = new AbcSmartcardHelper();
        this.tokenManager = tokenManager;
        this.credentialManager = credentialManager;
        this.nonrevocationEvidence = new HashMap<URI, NonRevocationEvidence>();
        this.issuerRevocation = issuerRevocation;
        this.contextGen = contextGen;
    }

    // ///////////////////////////////////////////////////////////////////
    // /override methods
    // ///////////////////////////////////////////////////////////////////

    @Override
    public SystemParameters setupSystemParameters(int keyLength, URI cryptographicMechanism) {

        // load initial idemix system parameters from resources (the idemix-xml),
        // then generate abce-specific system parameters

        String cryptographicMechanismStr = cryptographicMechanism.toString();
        if(!cryptographicMechanismStr.equals(CryptoUriUtil.getIdeMixString())) {
            throw new UnsupportedOperationException(cryptographicMechanismStr + " crypto mechanism not supported for Idemix cryptoengine");
        }

        ObjectFactory of = new ObjectFactory();
        SystemParameters ret = of.createSystemParameters();
        ret.setVersion("1.0");

        // generate system parameters

        com.ibm.zurich.idmx.utils.SystemParameters.SET_L_PHI_80 = true;
        com.ibm.zurich.idmx.utils.SystemParameters sp = com.ibm.zurich.idmx.utils.SystemParameters.generateSystemParametersFromRsaModulusSize(keyLength);
        // com.ibm.zurich.idmx.utils.SystemParameters sp =
        //         (com.ibm.zurich.idmx.utils.SystemParameters) Parser.getInstance().parse(
        //                 this.getResource("sp.xml"));


        // load system parameters into idemix
        StructureStore.getInstance().add(IdemixConstants.systemParameterId, sp);
        // load group parameters from file for now
        //TODO: add proper carry over from UProve
        GroupParameters gp = (GroupParameters) Parser.getInstance().parse(this.getResource("/eu/abc4trust/systemparameters/bridged-groupParameters.xml"));

        if (gp == null) {
            //gp = GroupParameters.generateGroupParams(URI.create(IdemixConstants.groupParameterId));
            gp = StaticGroupParameters.getGroupParameters();
            Locations.init(IdemixConstants.groupParameterId, gp);
        }
        if (gp.getSystemParams() == null) {
            throw new RuntimeException("System parameters are not correctly set up");
        }
        ret.getAny().add(sp);
        ret.getAny().add(gp);

        return ret;
    }

    @Override
    public IssuerParametersAndSecretKey setupIssuerParameters(CredentialSpecification credspec,
            SystemParameters syspars, URI uid, URI hash, URI revParsUid, List<FriendlyDescription> friendlyDescriptions) {

        ObjectFactory of = new ObjectFactory();

        int numberOfAttributes = credspec.getAttributeDescriptions().getAttributeDescription().size();

        IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(syspars);
        com.ibm.zurich.idmx.utils.SystemParameters sp = idemixSystemParameters.getSystemParameters();
        GroupParameters gp = idemixSystemParameters.getGroupParameters();

        // Load system, group and issuer parameters to Idemix StructureStore
        Locations.init(IdemixConstants.systemParameterId, sp);
        Locations.init(IdemixConstants.groupParameterId, gp);

        IssuerKeyPair issuerKey =
                new IssuerKeyPair(uid, URI.create(IdemixConstants.groupParameterId), numberOfAttributes,
                        IdemixConstants.EPOCH_LENGTH);
        IssuerPrivateKey issuerPrivateKey = issuerKey.getPrivateKey();

        // save public key as crypto params of the issuer parameters
        CryptoParams cp = of.createCryptoParams();
        cp.getAny().add(XMLSerializer.getInstance().serializeAsElement(issuerKey.getPublicKey()));

        IssuerParameters ip = of.createIssuerParameters();
        if(friendlyDescriptions != null){
            for(FriendlyDescription fd : friendlyDescriptions){
                ip.getFriendlyIssuerDescription().add(fd);
            }
        }
        ip.setAlgorithmID(CryptoUriUtil.getIdemixMechanism());
        ip.setCredentialSpecUID(credspec.getSpecificationUID());
        ip.setKeyBindingInfo(null);
        ip.setParametersUID(uid);
        ip.setRevocationParametersUID(revParsUid);
        ip.setVersion("IDEMIX:" + IssuerParametersUtil.generateVersionNumber()); // "1.0");
        ip.setHashAlgorithm(hash);
        ip.setSystemParameters(syspars);
        ip.setCryptoParams(cp);

        IssuerParametersAndSecretKey ret = new IssuerParametersAndSecretKey();
        SecretKey secretKey =
                IdemixUtils.convertIdemixIssuerPrivateKeyToAbceKey(issuerPrivateKey);

        try {
            this.credentialManager.storeIssuerSecretKey(uid, secretKey);
        } catch (CredentialManagerException ex) {
            throw new RuntimeException(ex);
        }

        ret.issuerSecretKey = secretKey;
        ret.issuerParameters = ip;

        return ret;
    }

    @Override
    public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
            List<Attribute> atts, URI ctxt) throws CryptoEngineException {


        URI credentialSpecUID = ip.getCredentialTemplate()
                .getCredentialSpecUID();
        CredentialSpecification cd;
        try {
            cd = this.keyManager.getCredentialSpecification(credentialSpecUID);
            if (cd == null) {
                throw new CryptoEngineException(
                        "Could not find credential description with UID: \""
                                + credentialSpecUID + "\"");
            }
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }


        if (cd.isRevocable()) {
            NonRevocationEvidence nre = this.issuerRevocation
                    .addRevocationHandleAttribute(atts, ip, ctxt);

            this.nonrevocationEvidence.put(ctxt, nre);
        }

        this.fillInAttributeCache(ctxt, atts);
        this.issuancePolicyCache.put(ctxt, ip);

        ObjectFactory of = new ObjectFactory();
        IssuanceMessage ret = of.createIssuanceMessage();

        IssuerParameters issuerParameters = null;
        SystemParameters systemParameters = null;

        // Get system and issuer parameters from key manager
        try {
            issuerParameters =
                    this.keyManager.getIssuerParameters(ip.getCredentialTemplate().getIssuerParametersUID());
            systemParameters = issuerParameters.getSystemParameters();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        SecretKey secretKey = null;
        try {
            secretKey = this.credentialManager.getIssuerSecretKey(issuerParameters.getParametersUID());
        } catch (CredentialManagerException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot extract Issuer Secret Key" + e);
        }

        // load parameters
        IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(systemParameters);
        com.ibm.zurich.idmx.utils.SystemParameters sp = idemixSystemParameters.getSystemParameters();
        GroupParameters gp = idemixSystemParameters.getGroupParameters();


        // Load system, group and issuer parameters to Idemix StructureStore
        Locations.init(IdemixConstants.systemParameterId, sp);
        Locations.init(IdemixConstants.groupParameterId, gp);

        IssuerPublicKey idemixPublicKey =
                (IssuerPublicKey) Parser.getInstance().parse(
                        (Element) issuerParameters.getCryptoParams().getAny().get(0));
        Locations.init(issuerParameters.getParametersUID().toString(), idemixPublicKey);


        IssuerKeyPair isKP = IdemixUtils.convertAbceIssuerKeysToIdemixKeyPair(secretKey);

        // create advanced issuer, add it to cache
        AdvancedIssuer issuer = new AdvancedIssuer(isKP);
        this.issuerCache.put(ctxt, issuer);

        Message msgToRecipient = null;
        eu.abc4trust.xml.Message msg = null;

        // create first idmx msg to recipient
        msgToRecipient = issuer.round0();

        // set idmx nonce to the policy
        if (ip.getPresentationPolicy().getMessage() == null) {
            msg = of.createMessage();
        } else {
            msg = ip.getPresentationPolicy().getMessage();
        }

        BigInteger nonce = msgToRecipient.getIssuanceElement(IssuanceProtocolValues.nonce);
        msg.setNonce(nonce.toByteArray());
        ip.getPresentationPolicy().setMessage(msg);

        ret.getAny().add(of.createIssuancePolicy(ip));
        ret.setContext(ctxt);

        // create a return value
        IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();
        imab.setLastMessage(false);
        imab.setIssuanceMessage(ret);
        return imab;
    }

    private void fillInAttributeCache(URI ctxt, List<Attribute> atts) {
        List<MyAttribute> list = new ArrayList<MyAttribute>();
        for(Attribute at: atts) {
            MyAttribute myAtt = new MyAttribute(at);
            list.add(myAtt);
        }
        this.attributeCache.put(ctxt, list);
    }

    @Override
    public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {

        // message format: (0):IssuanceToken|(1):IdmxMsg_1

        IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean(); // return value
        ObjectFactory of = new ObjectFactory();
        IssuanceMessage ret = of.createIssuanceMessage(); // part of return value
        CredentialDescription cd = of.createCredentialDescription();

        URI context = m.getContext();
        IssuancePolicy ip = this.issuancePolicyCache.get(context);

        ProofSpec proofSpec = null;
        IssuanceToken isToken = null;
        Message msgToRecipient = null;
        URI issLogEntryURI = null;

        // Parse message
        Object message = null;
        Message parsedIdmxMsg = null;
        if (m != null) {
            message = m.getAny().get(0);
        }
        // parse token & idemix message from the token:
        try {

            isToken = (IssuanceToken) XmlUtils.unwrap(message, IssuanceToken.class);

            // TODO: This part should actually be done by the issuance manager
            // Check that token satifies policy
            MyPresentationPolicy mypp = new MyPresentationPolicy(ip.getPresentationPolicy());
            if (!mypp.isSatisfiedBy(isToken.getIssuanceTokenDescription()
                    .getPresentationTokenDescription(), this.tokenManager, this.keyManager)) {
                throw new RuntimeException("Issuance Token not satisfied");
            }
            this.tokenManager.storeToken(isToken);

            CryptoParams cryptoEvidence = isToken.getCryptoEvidence();
            // parse idemix proof from the token
            parsedIdmxMsg =
                    (Message) Parser.getInstance().parse((Element) cryptoEvidence.getAny().get(0));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (isToken != null) {
            // extract issuance token description
            IssuanceTokenDescription itd = isToken.getIssuanceTokenDescription();
            // extract the credential template
            CredentialTemplate credTempl = itd.getCredentialTemplate();

            // create a list of credspecs
            Map<String, CredentialSpecification> aliasCredSpecsMap =
                    this.getCredSpecList(isToken.getIssuanceTokenDescription().getPresentationTokenDescription(),
                            credTempl);

            CredentialSpecification credSpec =
                    aliasCredSpecsMap.get(IdemixConstants.tempNameOfNewCredential);
            // create my cred spec object - helper
            MyCredentialSpecification myCredSpec = new MyCredentialSpecification(credSpec);

            // load parameters
            List<URI> issuerParamsURIList = new ArrayList<URI>();
            List<CredentialSpecification> credSpecsList = new ArrayList<CredentialSpecification>();
            for (CredentialSpecification cs : aliasCredSpecsMap.values()) {
                if (!credSpecsList.contains(cs)) {
                    credSpecsList.add(cs);
                }
            }
            for (CredentialInToken credInToken : isToken.getIssuanceTokenDescription()
                    .getPresentationTokenDescription().getCredential()) {
                issuerParamsURIList.add(credInToken.getIssuerParametersUID());
            }
            issuerParamsURIList.add(credTempl.getIssuerParametersUID());

            // prepare all parameters
            com.ibm.zurich.idmx.utils.SystemParameters sysPar =
                    this.loadIdemixSystemAndIssuerParameters(issuerParamsURIList);
            CredentialStructure credStruct =
                    this.loadIdemixCredentialStructures(credSpecsList, credTempl, context);

            CryptoEngineUtil.setupCredentialDescription(credSpec,
                    this.attributeCache.get(m.getContext()), credTempl, myCredSpec, cd);

            // convert attribute values to idemix-type ones
            List<MyAttribute> attrs = this.attributeCache.get(context);
            Values idmxValues = IdemixUtils.createIdemixValues(sysPar, attrs);

            URI temporarySecretName = this.getSmartcardSecretUid(parsedIdmxMsg);

            // parse token description to re-create a proof spec and verify the token
            PolicyTranslator polTransl =
                    new PolicyTranslator(itd.getPresentationTokenDescription(), credTempl, aliasCredSpecsMap,
                            temporarySecretName);
            IdemixClaim idmxClaim = new IdemixClaim(polTransl, this.keyManager,
                    this.contextGen, null);
            try {
                proofSpec =
                        IdemixProofSpecGenerator.generateProofSpecForIssuance(idmxClaim,
                                IdemixConstants.groupParameterId, credStruct,
                                this.keyManager, this.contextGen);
                System.out.println("------------------\n Proof spec for Issuer: "+proofSpec.toStringPretty()+"\n------------------");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            AdvancedIssuanceSpec issuanceSpec =
                    new AdvancedIssuanceSpec(credTempl.getIssuerParametersUID(),
                            credSpec.getSpecificationUID(), IdemixConstants.tempNameOfNewCredential, proofSpec);

            if (parsedIdmxMsg.getCounter() == 1) {

                // Add all issuer attributes to the token:
                // this.tokenManager
                // .storeIssuerAttributes(issuerAttrs, issLogEntryURI);

                // messages to verify:
                VerifierInput input = new VerifierInput();
                input.smartcardHelper = this.smartcardHelper;
                if (idmxClaim.getMessage() != null) {
                    MessageToSign msg = new MessageToSign(
                            idmxClaim.getMessage());
                    input.messages.put(IdemixConstants.messageName, msg);
                }
                msgToRecipient = this.issuerCache.get(context).round2(
                        parsedIdmxMsg, issuanceSpec, idmxValues, input);
                imab.setLastMessage(true);

            }
            if (msgToRecipient == null) {
                System.out.println("error in composing the 2 msg!");
            }

            // TODO(enr): Check that this is correct...

            issLogEntryURI = this.tokenManager.storeToken(isToken);
            IssuanceLogEntry issLogEntry = new IssuanceLogEntry();
            issLogEntry.setIssuanceLogEntryUID(issLogEntryURI);
            issLogEntry.setIssuerParametersUID(credTempl.getIssuerParametersUID());
            issLogEntry.setIssuanceToken(isToken);

            for (MyAttribute isAtr: this.attributeCache.get(context)){
                AttributeInLogEntry isAttr = new AttributeInLogEntry();
                isAttr.setAttributeType(isAtr.getType());
                isAttr.setAttributeValue(isAtr.getAttributeValue());

                issLogEntry.getIssuerAttributes().add(isAttr);
            }
            this.tokenManager.storeIssuanceLogEntry(issLogEntry);
        }
        // compose the return value
        ret.getAny().add(of.createCredentialDescription(cd));
        ret.getAny().add(XMLSerializer.getInstance().serializeAsElement(msgToRecipient));
        ret.setContext(m.getContext());

        // Add serialized non-revocation evidence
        NonRevocationEvidence nre = this.nonrevocationEvidence.get(context);
        if (nre != null) {
            ret.getAny().add(of.createNonRevocationEvidence(nre));
        }

        imab.setIssuanceMessage(ret);
        imab.setIssuanceLogEntryURI(issLogEntryURI);

        return imab;
    }

    private URI getSmartcardSecretUid(Message parsedIdmxMsg) {
        Map<String, SValue> values = parsedIdmxMsg.getProof().getSValues();
        URI smartcardSecretUid = null;
        for (String s : values.keySet()) {
            if (s.startsWith("smartcard_secret;")) {
                int inx = s.indexOf(";") + 1;
                String smartcardSecret = s.substring(inx);
                if (!smartcardSecret.equals("ms.xml")) {
                    smartcardSecretUid = URI.create(smartcardSecret);
                }
            }
        }
        return smartcardSecretUid;
    }

    // ///////////////////////////////////////////////////////////////////
    // /helpers
    // ///////////////////////////////////////////////////////////////////


    private com.ibm.zurich.idmx.utils.SystemParameters loadIdemixSystemAndIssuerParameters(
            List<URI> issuerParamsURIList) {
        // here we assume system parameters are consistent
        // TODO: throw an exception if they are not
        IssuerPublicKey isPK = null;
        SystemParameters systemParameters = null;
        IssuerParameters issuerParameters = null;
        com.ibm.zurich.idmx.utils.SystemParameters sysPar = null;

        for (URI issuerURI : issuerParamsURIList) {
            // Get system parameters from key manager
            try {
                issuerParameters = this.keyManager.getIssuerParameters(issuerURI);
                CryptoParams cp = issuerParameters.getCryptoParams();
                isPK = (IssuerPublicKey) Parser.getInstance().parse((Element) cp.getAny().get(0));
                StructureStore.getInstance().add(issuerParameters.getParametersUID().toString(), isPK);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if (systemParameters == null) {
                systemParameters = issuerParameters.getSystemParameters();

                IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(systemParameters);
                sysPar = idemixSystemParameters.getSystemParameters();
                GroupParameters grPar = idemixSystemParameters.getGroupParameters();

                // Load system, group and issuer parameters to Idemix StructureStore
                StructureStore.getInstance().add(IdemixConstants.systemParameterId, sysPar);
                StructureStore.getInstance().add(IdemixConstants.groupParameterId, grPar);
            }
        }
        return sysPar;
    }



    private CredentialStructure loadIdemixCredentialStructures(
            List<CredentialSpecification> credSpecsList, CredentialTemplate ct, URI context) {
        CredentialStructure ret = null;
        for (CredentialSpecification credSpec : credSpecsList) {
            CredentialStructure credStruct = null;
            // create credential structure for Idemix call and load it to Idemix Structure Store
            if ((ct != null) && (credSpec.getSpecificationUID().equals(ct.getCredentialSpecUID()))) {

                credStruct =
                        IdemixUtils.createIdemixCredentialStructure(ct, credSpec,
                                this.attributeCache.get(context), null, credSpec.isKeyBinding());
                ret = credStruct;
            } else {
                credStruct =
                        IdemixUtils.createIdemixCredentialStructure(null, credSpec, null, null,
                                credSpec.isKeyBinding());
            }
            StructureStore.getInstance().add(credSpec.getSpecificationUID().toString(), credStruct);
        }
        return ret;
    }



    private Map<String, CredentialSpecification> getCredSpecList(PresentationTokenDescription ptd,
            CredentialTemplate ct) {

        Map<String, CredentialSpecification> aliasCredSpecs =
                new HashMap<String, CredentialSpecification>();
        CredentialSpecification credSpec = null;
        for (CredentialInToken credInToken : ptd.getCredential()) {

            try {
                credSpec = this.keyManager.getCredentialSpecification(credInToken.getCredentialSpecUID());
            } catch (KeyManagerException e) {
                throw new RuntimeException("no cred spec" + credInToken.getCredentialSpecUID() + "stored:"
                        + e);
            }
            aliasCredSpecs.put(credInToken.getAlias().toString(), credSpec);
        }
        try {
            credSpec = this.keyManager.getCredentialSpecification(ct.getCredentialSpecUID());
        } catch (KeyManagerException e) {
            throw new RuntimeException("no cred spec" + ct.getCredentialSpecUID() + "stored:" + e);
        }
        aliasCredSpecs.put(IdemixConstants.tempNameOfNewCredential, credSpec);
        return aliasCredSpecs;
    }

    private InputSource getResource(String filename) {
        String resource;
        if(filename.startsWith("/")) {
            resource = filename;
        } else {
            resource = "/eu/abc4trust/sampleXml/idemix/" + filename;
        }
        return new InputSource(this.getClass().getResourceAsStream(resource));
    }

    @Override
    public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid)
            throws Exception {

        return this.tokenManager.getIssuanceLogEntry(issuanceEntryUid);
    }

}

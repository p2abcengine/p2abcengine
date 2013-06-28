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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerKeyAndParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;

import eu.abc4trust.abce.internal.revocation.RevocationProof;
import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.abce.internal.revocation.UserRevocation;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.user.CryptoEngineUtil;
import eu.abc4trust.cryptoEngine.idemix.util.RevocationProofData;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.util.CommitmentStripper;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CarriedOverAttribute;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PresentationTokenWithCommitments;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.UnknownAttributes;

/**
 * CryptoEngineUser implementation that uses UProve through WebServices interop. Depends on the .NET/Mono based server part running on localhost
 * 
 * @author Raphael Dobers
 */
public class UProveCryptoEngineUserImpl implements CryptoEngineUser {

    public static final String UProveCredential = "UProveCredential";
	public static boolean RELOADING_TOKENS = false;
    private final KeyManager keyManager;
    private final CredentialManager credManager;
    private final ContextGenerator contextGen;

    private final Map<URI, IssuanceTokenDescription> tokenCache;
    private final Map<URI, List<URI>> credCache;

    private final UProveUtils utils;
    private final UProveIssuanceHandling issuanceHandling;
    private final CryptoEngineUtil cryptoEngineUtil;
    private final AbcSmartcardManager smartcardManager;
    private final UserRevocation userRevocation;
    private final RevocationProof revocationProof;
    private final CardStorage cardStorage;
    private final ReloadTokensCommunicationStrategy reloadTokens;
    private final CryptoEngineContext ctxt;

    @Inject
    public UProveCryptoEngineUserImpl(KeyManager keyManager, CredentialManager credManager,
            AbcSmartcardManager smartcardManager, ContextGenerator contextGen,
            CryptoEngineContext ctxt, UserRevocation userRevocation,
            RevocationProof revocationProof, CardStorage cardStorage, ReloadTokensCommunicationStrategy reloadTokens,
            UProveIssuanceHandling issuanceHandling) {
        // WARNING: Due to circular dependencies you MUST NOT dereference credManager
        // in this constructor.
        // (Guice does some magic to support circular dependencies).

        this.keyManager = keyManager;
        this.credManager = credManager;
        this.contextGen = contextGen;
        this.tokenCache = new HashMap<URI, IssuanceTokenDescription>();
        this.credCache = new HashMap<URI, List<URI>>();
        this.cryptoEngineUtil = new CryptoEngineUtil(this.credManager,
                this.contextGen);
        this.smartcardManager = smartcardManager;
        this.userRevocation = userRevocation;
        this.revocationProof = revocationProof;
        this.cardStorage = cardStorage;
        this.reloadTokens= reloadTokens;
        this.ctxt = ctxt;
        this.issuanceHandling = issuanceHandling;

        this.utils = new UProveUtils();

        System.out.println("Hello from UProveCryptoEngineUserImpl()");
    }

    @Override
    public IssuanceToken createIssuanceToken(IssuanceTokenDescription itd,
            List<URI> creduids, List<Attribute> atts, List<URI> pseudonyms,
            URI context) {
        // UProve does not support the ABC4Trust advanced Issuance setting yet.
        // Hence, the returned IssuanceToken is simply discarded later..

        this.fillInAttributeCache(context, atts);
        this.tokenCache.put(context, itd);
        this.credCache.put(context, creduids);

        Map<URI, Credential> aliasCreds = null;
        Map<URI, PseudonymWithMetadata> aliasNyms = null;
        try {
            aliasCreds = this.cryptoEngineUtil
                    .fetchCredentialsFromIssuanceToken(itd, creduids);
            aliasNyms = this.cryptoEngineUtil.fetchPseudonymsFromIssuanceToken(
                    itd, pseudonyms);
        } catch (CredentialManagerException e) {
            e.printStackTrace();
        }

//        ReloadInformation info = new ReloadInformation(itd,creduids, pseudonyms);
//        this.reloadTokens.setCredentialInformation(context, info);

        ObjectFactory of = new ObjectFactory();
        IssuanceToken ret = of.createIssuanceToken();

        ret.setVersion("1.0");
        ret.setIssuanceTokenDescription(itd);
        CryptoParams cryptoEvidence = this
                .generateIssuanceCryptoEvidenceUProve(itd, aliasCreds,
                        aliasNyms, atts, context);
        ret.setCryptoEvidence(cryptoEvidence);

        return ret;
    }

    private void fillInAttributeCache(URI ctxtUri, List<Attribute> atts) {
        List<MyAttribute> list = new ArrayList<MyAttribute>();
        for(Attribute at: atts) {
            MyAttribute myAtt = new MyAttribute(at);
            list.add(myAtt);
        }
        this.ctxt.attributeCache.put(ctxtUri, list);
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {
        return this.userRevocation.updateNonRevocationEvidence(cred, raparsuid,
                revokedatts);
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException, CredentialWasRevokedException {
        return this.userRevocation.updateNonRevocationEvidence(cred, raparsuid,
                revokedatts, revinfouid);
    }

    private CryptoParams generateIssuanceCryptoEvidenceUProve(
            IssuanceTokenDescription itd, Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms, List<Attribute> atts,
            URI context) {
        ObjectFactory of = new ObjectFactory();

        CredentialTemplate ct = itd.getCredentialTemplate();

        IssuerParameters issuerParameters = null;

        // Get system and issuer parameters from key manager
        try {
            issuerParameters = this.keyManager.getIssuerParameters(ct
                    .getIssuerParametersUID());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // create UID of newly issued credential, allocate it on a smart-card
        URI uidOfNewCredential = this.contextGen.getUniqueContext(URI
                .create(UProveCredential));
        this.ctxt.uidOfIssuedCredentialCache.put(context, uidOfNewCredential);

        // Find the URI of the secret.
        URI secretUid = null;
        URI sameKeyBindingAsUri = ct.getSameKeyBindingAs();
        secretUid = this.cryptoEngineUtil
                .getSmartcardUidFromPseudonymOrCredentialUri(
                        aliasCreds, aliasNyms, sameKeyBindingAsUri);

        this.ctxt.secretCache.put(context, secretUid);

        // boolean isOnSmartcard = false;
        if(!RELOADING_TOKENS){
        	this.smartcardManager.allocateCredential(secretUid,
        			uidOfNewCredential, issuerParameters.getParametersUID(), false);
        }

        // add carry over attribute values to the user values:
        List<Attribute> userValues = atts;
        // add get carry over values:
        UnknownAttributes unknownAttributes = ct.getUnknownAttributes();
        if (unknownAttributes != null) {
            for (CarriedOverAttribute coa : unknownAttributes
                    .getCarriedOverAttribute()) {
                Credential c = aliasCreds.get(coa.getSourceCredentialInfo()
                        .getAlias());
                for (Attribute at : c.getCredentialDescription().getAttribute()) {
                    if (at.getAttributeDescription()
                            .getType()
                            .equals(coa.getSourceCredentialInfo()
                                    .getAttributeType())) {
                        at.getAttributeDescription().setType(
                                coa.getTargetAttributeType());
                        userValues.add(at);
                    }
                }
            }
        }

        CryptoParams cryptoEvidence = of.createCryptoParams();
        try {
            if(this.keyManager.getCredentialSpecification(itd.getCredentialTemplate().getCredentialSpecUID()).isKeyBinding()){
                URI scUri = UProveUtils.getSmartcardUri(this.cardStorage);
                if(scUri != null){
                    HardwareSmartcard sc = (HardwareSmartcard) this.cardStorage.getSmartcard(scUri);
                    BigInteger hd = sc.computeDevicePublicKey(this.cardStorage.getPin(scUri));
                    com.microsoft.schemas._2003._10.serialization.ObjectFactory fact = new com.microsoft.schemas._2003._10.serialization.ObjectFactory();
                    JAXBElement<byte[]> hdB = fact.createBase64Binary(hd.toByteArray());
                    cryptoEvidence.getAny().add(hdB);

                    //CryptoParams innerCryptoEvidence = of.createCryptoParams();
                    //innerCryptoEvidence.getAny().add(hdB);
                    //PresentationToken token = of.createPresentationToken();
                    //token.setCryptoEvidence(innerCryptoEvidence);
                    //cryptoEvidence.getAny().add(token);
                    //					String xml = UProveUtils.toXml();
                    //					System.out.println("\n\n"+xml+"\n\n");
                }
            }
        } catch (KeyManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return cryptoEvidence;
    }


    public PresentationTokenWithCommitments createPresentationTokenWithCommitments(
            PresentationTokenDescriptionWithCommitments ptdwc,
            List<URI> creds,
            List<URI> pseudonyms) throws Exception {
        // TODO This method should be refactored to work together with
        // createPresentationToken(...) to avoid dublicate code. See the
        // following (old) description:
        // modify createPresentationTokenInternal to handle both presentation with and without commitments. Details are in svn:
        // abc4trust\wp2\Architecture_Doc\version 1.1\Crypto Orchestration.doc
        // for each attribute to commit to, add the attribute index to createPresentationTokenInternal's committedAttributes array.
        // Right now it's difficult to do without refactoring some of the serialization code. It would be easier if the ...WithCommitment
        // classes would derive from the other ones.

        Map<URI, Credential> aliasCreds = this.utils
                .fetchCredentialsFromPresentationToken(CommitmentStripper.stripPresentationTokenDescription(ptdwc), creds,
                        this.credManager);
        Map<URI, PseudonymWithMetadata> aliasNyms = this.cryptoEngineUtil
                .fetchPseudonymsFromPresentationToken(CommitmentStripper.stripPresentationTokenDescription(ptdwc), pseudonyms);

        ObjectFactory of = new ObjectFactory();
        PresentationTokenWithCommitments ret = of.createPresentationTokenWithCommitments();
        ret.setPresentationTokenDescriptionWithCommitments(ptdwc);
        ret.setVersion("1.0");
        CryptoParams cryptoEvidence = of.createCryptoParams();


        // Handle revocation:
        Collection<RevocationProofData> revocationProofData = new LinkedList<RevocationProofData>();
        Map<URI, RevocationInformation> revInfoUidToRevInfo = new HashMap<URI, RevocationInformation>();

        for (Credential cred : aliasCreds.values()) {
            List<Object> any = cred.getCryptoParams().getAny();
            URI credSpecUid = cred.getCredentialDescription()
                    .getCredentialSpecificationUID();
            CredentialSpecification credentialSpecification = this.keyManager
                    .getCredentialSpecification(credSpecUid);

            CredentialInTokenWithCommitments credInToken = this.revocationProof
                    .getCredentialInToken(credSpecUid, ptdwc.getCredential());

            boolean isUProve = this.revocationProof.isUProve(cred); // TODO this is not needed, idemix credentials are filered away in the delegator

            if (isUProve && credentialSpecification.isRevocable()) {
                NonRevocationEvidence nre = (NonRevocationEvidence) any.get(1);
                URI revParamsUid = nre.getRevocationAuthorityParametersUID();

                URI revInfoUid = credInToken.getRevocationInformationUID();
                RevocationInformation revInfo = this.keyManager
                        .getRevocationInformation(revParamsUid, revInfoUid);
                RevocationUtility.updateNonRevocationEvidence(nre, revInfo);

                AccumulatorWitness w1 = this.revocationProof
                        .extractWitness(nre);
                int epoch = nre.getEpoch();
                RevocationProofData revocationProofDatum = this.revocationProof
                        .revocationProofDatum(epoch, credSpecUid, revParamsUid,
                                w1);

                revocationProofData.add(revocationProofDatum);
                revInfoUidToRevInfo.put(revInfoUid, revInfo);
            }
        }

        try {

            UProveEvidenceGenerator generator = new UProveEvidenceGenerator(
                    this.credManager, this.keyManager, this.cryptoEngineUtil,
                    this.ctxt.binding, this.cardStorage, this.reloadTokens);

            List<Object> presentationEvidence = generator.getPresentationEvidence(ptdwc, aliasCreds, aliasNyms);
            if (presentationEvidence == null) {
                // We have run out of U-Prove tokens.
                return null;
            }

            JAXBElement<byte[]> serializedProofData = null;
            try{
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                com.microsoft.schemas._2003._10.serialization.ObjectFactory fact = new com.microsoft.schemas._2003._10.serialization.ObjectFactory();
                try {
                    out = new ObjectOutputStream(bos);
                    out.writeObject(revocationProofData);
                    out.writeObject(revInfoUidToRevInfo);
                    out.flush();
                    byte[] 	bytes = bos.toByteArray();
                    serializedProofData = fact.createBase64Binary(bytes);
                } finally {
                    out.close();
                    bos.close();
                }
            }catch(Exception e){
                System.err.println("Failed to serialize revocation proof data");
                throw new RuntimeException(e);
            }


            // Iterate over the newly created proof to find any committed
            // values and place that data into the committedattributes in
            // the credentials in the presentationtokendescription
            for(Object o: presentationEvidence){
                Element elm = (Element)o;
                if(elm.getNodeName().equals("UProveCredentialAndPseudonym")){
                    Element proof = (Element)elm.getElementsByTagName("Proof").item(0);
                    String credAlias = ((Element)proof.getElementsByTagName("CredentialAlias").item(0)).getTextContent();
                    for(CredentialInTokenWithCommitments c: ptdwc.getCredential()){
                        try {
                            if(c.getAlias().equals(new URI(credAlias))){
                                int count = 0;
                                Element tildeO = (Element)proof.getElementsByTagName("TildeO").item(0);
                                NodeList indices = ((Element)proof.getElementsByTagName("CommittedAttributesIndices").item(0)).getChildNodes();
                                for(int i =0; i< indices.getLength(); i++){
                                    int index = (Integer.parseInt(indices.item(i).getTextContent()))-1;  // UProve indices used by UProve start by 1. ABC4Trust indices start by 0
                                    // First check if the committed index is for a committed attribute
                                    URI committedIndexType = this.keyManager.getCredentialSpecification(c.getCredentialSpecUID()).getAttributeDescriptions().getAttributeDescription().get(index).getType();
                                    for(CommittedAttribute ca: c.getCommittedAttribute()){
                                        if(ca.getAttributeType().equals(committedIndexType)){
                                            // Set opening information = TildeO
                                            CryptoParams opening = of.createCryptoParams();
                                            opening.getAny().add(tildeO.getChildNodes().item(count));
                                            ca.setOpeningInformation(opening);

                                            // Set commitment = TildeC = offset 0 in the tildevalues
                                            CryptoParams commitment = of.createCryptoParams();
                                            Element tildes = (Element)proof.getElementsByTagName("TildeValues").item(0);
                                            Node newTildes = tildes.cloneNode(false);
                                            newTildes.appendChild(tildes.getChildNodes().item((count*3)).cloneNode(true));
                                            commitment.getAny().add(newTildes);
                                            ca.setCommitment(commitment);

                                            CryptoParams committedValue = of.createCryptoParams();

                                            // Set committed value = x, can be found in credential
                                            Credential credential = aliasCreds.get(c.getAlias());
                                            for(Attribute a: credential.getCredentialDescription().getAttribute()){
                                                if(a.getAttributeDescription().getType().equals(ca.getAttributeType())){
                                                    MyAttributeValue mav = MyAttributeEncodingFactory.parseValueFromEncoding(a.getAttributeDescription().getEncoding(), a.getAttributeValue(), new EnumAllowedValues(a.getAttributeDescription()));
                                                    BigInteger x = mav.getIntegerValueOrNull();
                                                    if(x == null)
                                                    {
                                                        throw new RuntimeException("Could not find committed attribute");
                                                    }
                                                    com.microsoft.schemas._2003._10.serialization.ObjectFactory fact = new com.microsoft.schemas._2003._10.serialization.ObjectFactory();
                                                    committedValue.getAny().add(fact.createBase64Binary(x.toByteArray()));
                                                }
                                            }
                                            ca.setCommittedValue(committedValue);
                                            count++;
                                        }
                                    }
                                }
                            }
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        } catch (KeyManagerException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            cryptoEvidence.getAny().addAll(presentationEvidence);
            cryptoEvidence.getAny().add(serializedProofData);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        ret.setCryptoEvidence(cryptoEvidence);
        /*    try {
            System.out.println("\n\ncreatePresentationTokenWithCommitments output :\n"
                    + XmlUtils.toXml(of.createPresentationTokenWithCommitments(ret)));
        }catch(Exception e){} */
        return ret;
    }

    // Returns null if the ABC4Trust credential has run out of UProveTokens - the calling code is responsible for initiating new issuance.
    @Override
    public PresentationToken createPresentationToken(PresentationTokenDescription ptd, List<URI> creds, List<URI> pseudonyms) {
        Map<URI, Credential> aliasCreds = this.utils
                .fetchCredentialsFromPresentationToken(ptd, creds,
                        this.credManager);
        Map<URI, PseudonymWithMetadata> aliasNyms = this.cryptoEngineUtil
                .fetchPseudonymsFromPresentationToken(ptd, pseudonyms);

        ObjectFactory of = new ObjectFactory();
        PresentationToken ret = of.createPresentationToken();
        ret.setPresentationTokenDescription(ptd);
        ret.setVersion("1.0");
        CryptoParams cryptoEvidence = of.createCryptoParams();
        try {
            UProveEvidenceGenerator generator = new UProveEvidenceGenerator(
                    this.credManager, this.keyManager, this.cryptoEngineUtil,
                    this.ctxt.binding, this.cardStorage, this.reloadTokens);
            List<Object> presentationEvidence = generator.getPresentationEvidence(ptd, aliasCreds, aliasNyms);
            if (presentationEvidence == null) {
                // We have run out of U-Prove tokens.
                throw new RuntimeException("We have run out of U-Prove tokens");
            }
            cryptoEvidence.getAny().addAll(presentationEvidence);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        ret.setCryptoEvidence(cryptoEvidence);
        return ret;
    }


    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {
    	return this.issuanceHandling.issuanceProtocolStep(m, null);
    }

    @Override
    public PseudonymWithMetadata createPseudonym(URI pseudonymUri,
            String scope, boolean exclusive, URI secretReference) {
        PseudonymWithMetadata pwm = new PseudonymWithMetadata();
        Pseudonym p = new Pseudonym();
        p.setExclusive(exclusive);
        p.setScope(scope);
        p.setSecretReference(secretReference);
        p.setPseudonymUID(pseudonymUri);
        pwm.setPseudonym(p);

        URI scURI = UProveUtils.getSmartcardUri(this.cardStorage);
        String sessionKey = null;
        SystemParameters syspars;
        try {
            syspars = this.keyManager.getSystemParameters();
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }

        int keyLength = new UProveSystemParameters(syspars).getKeyLength();

        if(scURI != null){
            int credID = -1;
            sessionKey = this.utils.getSessionKey(this.ctxt.binding, this.cardStorage, credID, keyLength);
        }else{
            sessionKey = this.utils.getSessionKey(this.ctxt.binding, keyLength);
        }

        try {
            // this.binding.setSecret(secret.getSecretKey().toByteArray());
            UProveSerializer serializer = new UProveSerializer();

            IssuerParametersComposite ipc = this
                    .createIssuerParametersForPseudonym();

            this.ctxt.binding.verifyIssuerParameters(ipc, sessionKey);
            PseudonymComposite pc = this.ctxt.binding.presentPseudonym(scope, exclusive ? scope : null, sessionKey);
            p.setPseudonymValue(pc.getP().getValue());
            ObjectFactory of = new ObjectFactory();
            CryptoParams cryptoEvidence = of.createCryptoParams();

            Document document = this.getW3CDocument();
            byte[] value = pc.getA().getValue();
            Element wrapper = this.getCryptoEvidenceWrapper(pc, document, "A",
                    value);
            cryptoEvidence.getAny().add(wrapper);

            value = pc.getR().getValue();
            wrapper = this.getCryptoEvidenceWrapper(pc, document, "R", value);

            Node issuerParametersCompositeElement = serializer
                    .createIssuerParametersCompositeElement(ipc);

            cryptoEvidence.getAny().add(wrapper);
            cryptoEvidence.getAny().add(issuerParametersCompositeElement);
            pwm.setCryptoParams(cryptoEvidence);
            return pwm;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private Element getCryptoEvidenceWrapper(PseudonymComposite pc,
            Document document, String elementName, byte[] value) {
        Element wrapper = document.createElement(elementName);
        if (value != null) {
            wrapper.setTextContent(DatatypeConverter.printBase64Binary(value));
        }
        return wrapper;
    }

    private Document getW3CDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document;
    }

    @Override
    // FIXME: we discussed that on the WP4 mailing list that this method should take the
    // security level and/or the system parameters since the keyManager might support more
    // than one, and therefore the user would need at least one secret for each combination.
    public Secret createSecret() {
        // create a user-bound secret

        URI newSdUri = this.contextGen.getUniqueContext(URI.create("abc4trust://secret"));
        SecretDescription newSd = new SecretDescription();
        newSd.setDeviceBoundSecret(false);
        newSd.setSecretUID(newSdUri);
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("New user secret " + newSdUri);
        newSd.getFriendlySecretDescription().add(fd);
        List<FriendlyDescription> friendlySecretDescriptions = newSd
                .getFriendlySecretDescription();
        FriendlyDescription friendlyDescription = new FriendlyDescription();
        friendlyDescription.setLang("en");
        friendlyDescription.setValue("New secret");
        friendlySecretDescriptions.add(friendlyDescription);
        friendlyDescription = new FriendlyDescription();
        friendlyDescription.setLang("da");
        friendlyDescription.setValue("Ny hemmelighed");
        friendlySecretDescriptions.add(friendlyDescription);
        newSd.setMetadata(null);
        Secret s = new Secret();
        s.setSecretKey(this.contextGen.getRandomNumber(256)); // TODO: don't
        // hardcode.
        s.setSecretDescription(newSd);
        // TODO(jdn): Set the smartscard system parameters?

        SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();
        scSysParams.setPrimeModulus(BigInteger.ZERO);
        scSysParams.setGenerator(BigInteger.ZERO);
        scSysParams.setSubgroupOrder(BigInteger.ZERO);
        scSysParams.setZkChallengeSizeBytes(0);
        scSysParams.setZkStatisticalHidingSizeBytes(0);
        scSysParams.setDeviceSecretSizeBytes(0);
        scSysParams.setSignatureNonceLengthBytes(0);
        scSysParams.setZkNonceSizeBytes(0);
        scSysParams.setZkNonceOpeningSizeBytes(0);

        s.setSystemParameters(scSysParams);

        return s;
    }

    private IssuerParametersComposite createIssuerParametersForPseudonym() {
        URI hash = URI.create("SHA-256");
        String uniqueIdentifier = this.contextGen.getUniqueContext(
                URI.create("abce4trust://U-ProveSecretIpc")).toString();
        URI uriId = URI.create(uniqueIdentifier);
        String sessionKey = null;
        SystemParameters syspars;
        try {
            syspars = this.keyManager.getSystemParameters();
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }

        int keyLength = new UProveSystemParameters(syspars).getKeyLength();

        if (!this.ctxt.sessionKeyCache.containsKey(uriId)) {
            sessionKey = this.utils.getSessionKey(this.ctxt.binding, keyLength);
            this.ctxt.sessionKeyCache.put(uriId, sessionKey);
        } else {
            sessionKey = this.ctxt.sessionKeyCache.get(uriId);
        }

        byte[] attributeEncoding = new byte[0];

        IssuerKeyAndParametersComposite issuerKeyAndParametersComposite = this.ctxt.binding
                .setupIssuerParameters(uniqueIdentifier, attributeEncoding,
                        hash.toString(), sessionKey);

        if (issuerKeyAndParametersComposite == null) {
            throw new RuntimeException(
                    "U-Prove Binding failed to generate issuer parameters.");
        }

        // Fetch the issuer parameters...
        IssuerParametersComposite ipc = issuerKeyAndParametersComposite
                .getIssuerParameters().getValue();
        return ipc;
    }

    @Override
    public boolean isRevoked(Credential cred) throws CryptoEngineException {
        return this.userRevocation.isRevoked(cred);
    }

}

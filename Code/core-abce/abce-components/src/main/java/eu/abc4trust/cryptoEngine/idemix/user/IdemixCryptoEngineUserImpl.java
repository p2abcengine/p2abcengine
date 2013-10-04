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

package eu.abc4trust.cryptoEngine.idemix.user;


import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.dm.StoredPseudonym;
import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.issuance.AdvancedIssuanceSpec;
import com.ibm.zurich.idmx.issuance.AdvancedRecipient;
import com.ibm.zurich.idmx.issuance.Message;
import com.ibm.zurich.idmx.issuance.Message.IssuanceProtocolValues;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.ProverInput;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.abce.internal.revocation.RevocationProof;
import eu.abc4trust.abce.internal.revocation.UserRevocation;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixClaim;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixProofSpecGenerator;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixUtils;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.MyCredentialDescription;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.util.PolicyTranslator;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CarriedOverAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
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
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.UnknownAttributes;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * CryptoEngineUser implementation that uses Idemix library for credential handling.
 * 
 * @author mdu
 * 
 */

public class IdemixCryptoEngineUserImpl implements CryptoEngineUser {

    private final boolean DEBUG = false;

    public static final String IdmxCredential = "IdmxCredential";

    private final KeyManager keyManager;
    public final CredentialManager credManager;
    private final ContextGenerator contextGen;
    private final Logger logger;

    private final Map<URI, List<MyAttribute>> attributeCache;
    private final Map<URI, AdvancedRecipient> recipientCache;
    private final Map<URI, URI> uidOfIssuedCredentialCache;
    private final HashMap<URI, URI> secretCache;

    private final AbcSmartcardManager smartcardManager;
    private final CryptoEngineUtil cryptoEngineUtil;
    private final UserRevocation userRevocation;
    private final RevocationProof revocationProof;

    @Inject
    public IdemixCryptoEngineUserImpl(KeyManager keyManager, CredentialManager credManager,
            AbcSmartcardManager smartcardManager, ContextGenerator contGen,
            Logger logger, UserRevocation userRevocation,
            RevocationProof revocationProof) {
        // WARNING: Due to circular dependencies you MUST NOT dereference credManager
        // in this constructor.
        // (Guice does some magic to support circular dependencies).

        this.keyManager = keyManager;
        this.credManager = credManager;
        this.contextGen = contGen;
        this.smartcardManager = smartcardManager;
        this.logger = logger;
        this.cryptoEngineUtil = new CryptoEngineUtil(this.credManager, this.contextGen);
        this.userRevocation = userRevocation;
        this.revocationProof = revocationProof;

        this.attributeCache = new HashMap<URI, List<MyAttribute>>();
        this.recipientCache = new HashMap<URI, AdvancedRecipient>();
        this.secretCache = new HashMap<URI, URI>();
        this.uidOfIssuedCredentialCache = new HashMap<URI, URI>();
    }

    public PresentationTokenWithCommitments createPresentationTokenWithCommitments(
            PresentationTokenDescriptionWithCommitments ptd, List<URI> creds, List<URI> pseudonyms) throws CryptoEngineException{
        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createPresentationTokenWithCommitments("+ptd.getPolicyUID()+")", true);

        LinkedHashMap<URI, Credential> aliasCreds =
                this.cryptoEngineUtil.fetchCredentialsFromPresentationTokenWithCommitments(ptd, creds);
        LinkedHashMap<URI, PseudonymWithMetadata> aliasNyms =
                this.cryptoEngineUtil.fetchPseudonymsFromPresentationTokenWithCommitments(ptd, pseudonyms);
        ObjectFactory of = new ObjectFactory();
        PresentationTokenWithCommitments ret = of.createPresentationTokenWithCommitments();
        ret.setCryptoEvidence(this.generatePresentationCryptoEvidenceWithCommitmentsIdemix(ptd,
                aliasCreds, aliasNyms));
        ret.setVersion("1.0");
        ret.setPresentationTokenDescriptionWithCommitments(ptd);

        /*
        try {
			System.out.println("idemix userengine - prestoken!:\n"+XmlUtils.toXml(of.createPresentationTokenWithCommitments(ret)));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         */

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createPresentationTokenWithCommitments("+ptd.getPolicyUID()+")", false);

        return ret;
    }


    // ///////////////////////////////////////////////////////////////////
    // /override methods
    // ///////////////////////////////////////////////////////////////////
    @Override
    public IssuanceToken createIssuanceToken(IssuanceTokenDescription itd, List<URI> creduids,
            List<Attribute> atts, List<URI> pseudonyms, URI ctxt) {

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createIssuanceToken(" + itd.getCredentialTemplate().getCredentialSpecUID() + ")", true);

        // put attributes to the attribute cache
        this.fillInAttributeCache(ctxt, atts);

        LinkedHashMap<URI, Credential> aliasCreds = null;
        LinkedHashMap<URI, PseudonymWithMetadata> aliasNyms = null;
        try {
            aliasCreds = this.cryptoEngineUtil.fetchCredentialsFromIssuanceToken(itd, creduids);
            aliasNyms = this.cryptoEngineUtil.fetchPseudonymsFromIssuanceToken(itd, pseudonyms);
        } catch (CredentialManagerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        ObjectFactory of = new ObjectFactory();
        IssuanceToken ret = of.createIssuanceToken();
        // create a reply to the first message of the advanced issuer
        ret.setVersion("1.0");
        ret.setIssuanceTokenDescription(itd);
        CryptoParams cryptoEvidenceIdemix;
        try {
            cryptoEvidenceIdemix = this.generateIssuanceCryptoEvidenceIdemix(
                    itd, aliasCreds, aliasNyms, atts, ctxt);
        } catch (CryptoEngineException ex) {
            throw new RuntimeException(ex);
        }
        ret.setCryptoEvidence(cryptoEvidenceIdemix);
        /* try {
            XmlUtils.toNormalizedXML(of.createIssuanceToken(ret));
        } catch (Exception e) {
            String errorMessage = "Could not serialize Application data: " + e.getMessage();
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
         */
        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createIssuanceToken(" + itd.getCredentialTemplate().getCredentialSpecUID() + ")", false);
        return ret;
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
    public PresentationToken createPresentationToken(
            PresentationTokenDescription ptd, List<URI> creds,
            List<URI> pseudonyms) throws CryptoEngineException {

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createPresentationToken", true);

        LinkedHashMap<URI, Credential> aliasCreds =
                this.cryptoEngineUtil.fetchCredentialsFromPresentationToken(ptd, creds);
        LinkedHashMap<URI, PseudonymWithMetadata> aliasNyms =
                this.cryptoEngineUtil.fetchPseudonymsFromPresentationToken(ptd, pseudonyms);
        ObjectFactory of = new ObjectFactory();
        PresentationToken ret = of.createPresentationToken();
        CryptoParams cp = this.generatePresentationCryptoEvidenceIdemix(ptd, aliasCreds, aliasNyms);
        ret.setCryptoEvidence(cp);
        ret.setVersion("1.0");
        ret.setPresentationTokenDescription(ptd);

        /*
        try {
			System.out.println("userengine - prestoken!: "+XmlUtils.toXml(of.createPresentationToken(ret)));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         */

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.createPresentationToken", false);
        return ret;
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.issuanceProtocolStep", true);

        Message idmxMessage = null; // original msg from the issuer
        IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc(); // return object
        URI context = m.getContext(); // get the context

        Object message = null;
        if (m != null) {
            message = m.getAny().get(1); // parse msg
            idmxMessage = (Message) Parser.getInstance().parse((Element) message);
        }

        if (idmxMessage.getCounter() == 2) { // extract and store the credential

            ObjectFactory of = new ObjectFactory();
            Credential cred = of.createCredential();


            CredentialDescription credDesc = (CredentialDescription) XmlUtils.unwrap(
                    m.getAny().get(0), CredentialDescription.class);
            CredentialSpecification credSpec;
            MyCredentialDescription myCredDesc;
            try {
                credSpec = this.keyManager
                        .getCredentialSpecification(credDesc.getCredentialSpecificationUID());
                myCredDesc = new MyCredentialDescription(credDesc , credSpec, this.keyManager);
            } catch (KeyManagerException ex) {
                throw new CryptoEngineException(ex);
            }

            // Fill in missing values in credential description:
            // - Secret references
            // - User-specific attributes

            URI secretUid = this.secretCache.get(context);

            // get credential
            AdvancedRecipient advancedRecipient = this.recipientCache.get(context);
            //TODO add idemix values coming from the issuer

            TimingsLogger.logTiming("round3", true);
            com.ibm.zurich.idmx.dm.Credential idmxCred =
                    advancedRecipient.round3(idmxMessage, secretUid,
                            this.uidOfIssuedCredentialCache.get(context));
            if (idmxCred == null) {
                throw new CryptoEngineException(
                        "Could not create credential for round three based on the context: \""
                                + context + "\" and secret UID: \"" + secretUid
                                + "\"");
            }
            TimingsLogger.logTiming("round3", false);
            // System.out.println("Issuance: " + idmxCred.toStringPretty());

            MyCredentialSpecification myCredSpec = new MyCredentialSpecification(credSpec);

            NonRevocationEvidence nre = null;

            if (credSpec.isRevocable()) {
                try{
                    IssuerParameters ip = this.keyManager.getIssuerParameters(credDesc.getIssuerParametersUID());
                    URI revParUid = ip.getRevocationParametersUID();
                    boolean pkInStorage = false;
                    try{
                        pkInStorage = (StructureStore.getInstance().get(revParUid) != null);
                    }catch(RuntimeException e){}
                    if(!pkInStorage){
                        RevocationAuthorityParameters revParams = this.keyManager.getRevocationAuthorityParameters(revParUid);
                        if (revParams == null) {
                            throw new CryptoEngineException(
                                    "Could not find revocation autority parameters with UID: "
                                            + revParUid + " in key manager");
                        }
                        List<Object> any = revParams.getCryptoParams().getAny();
                        Element publicKeyStr = (Element) any.get(0);
                        Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

                        AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;
                        StructureStore.getInstance().add(revParUid.toString(), publicKey);
                    }
                }catch(KeyManagerException e){
                    throw new CryptoEngineException(e);
                }

                nre = (NonRevocationEvidence) XmlUtils.unwrap(
                        m.getAny().get(2), NonRevocationEvidence.class);

                com.ibm.zurich.idmx.dm.Attribute revocationAttr = idmxCred
                        .getAttribute(RevocationConstants.REVOCATION_HANDLE_STR);
                BigInteger revocationAttrValue = revocationAttr.getValue();
                this.userRevocation
                .compareRevocationHandleToNonRevocationEvidence(
                        revocationAttrValue, nre);
            }

            // add user's secret attributes to the credential description.
            if (idmxCred != null) {
                if (myCredDesc.getCredentialDescription().getAttribute().size() != this.attributeCache.get(
                        context).size()) {
                    for (MyAttribute attr : this.attributeCache.get(context)) {
                        if (!myCredDesc.getCredentialDescription().getAttribute().contains(attr)) {
                            // first add the friendly description from the spec
                            List<FriendlyDescription> frienlyAttrDescFromSpec =
                                    myCredSpec.getFriendlyDescryptionsForAttributeType(attr.getType());
                            if (attr.getFriendlyAttributeName() == null) {
                                attr.getFriendlyAttributeName()
                                .addAll(frienlyAttrDescFromSpec);
                            }
                            myCredDesc.addAttribute(attr, true);
                        }
                    }
                }

                CredentialDescription credentialDesc = myCredDesc
                        .getCredentialDesc();
                credentialDesc.setSecretReference(secretUid);
                cred.setCredentialDescription(credentialDesc);

                cred.getCredentialDescription().setCredentialUID(
                        this.uidOfIssuedCredentialCache.get(context));

                // Finalize creating and store the credential.

                CryptoParams cp = of.createCryptoParams();
                cp.getAny().add(XMLSerializer.getInstance().serializeAsElement(idmxCred));

                // Set NonRevocationEvidenceUID.
                if (credSpec.isRevocable()) {
                    nre.setCredentialUID(myCredDesc.getUid());
                    cred.getNonRevocationEvidenceUID().add(
                            nre.getNonRevocationEvidenceUID());
                    cp.getAny().add(new ObjectFactory().createNonRevocationEvidence(nre));
                }

                cred.setCryptoParams(cp);
                try {
                    TimingsLogger.logTiming("CredentialManager.storeCredential", true);
                    URI newUri = this.credManager.storeCredential(cred);
                    TimingsLogger.logTiming("CredentialManager.storeCredential", false);
                    cred.getCredentialDescription().setCredentialUID(newUri);
                } catch (CredentialManagerException ex) {
                    throw new CryptoEngineException(ex);
                }
                ret.cd = cred.getCredentialDescription();
            } else {
                ret = null;
            }
        }

        TimingsLogger.logTiming("IdemixCryptoEngineUserImpl.issuanceProtocolStep", false);
        return ret;
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred, URI raparsuid,
            List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {
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

    // ///////////////////////////////////////////////////////////////////
    // /generating presentation and issuance evidences
    // ///////////////////////////////////////////////////////////////////

    public CryptoParams generateIssuanceCryptoEvidenceIdemix(
            IssuanceTokenDescription itd, Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms, List<Attribute> atts,
            URI context) throws CryptoEngineException {

        PresentationTokenDescription ptd = itd.getPresentationTokenDescription();
        CredentialTemplate ct = itd.getCredentialTemplate();

        ProverInput proverInput = null;
        ProofSpec proofSpec = null;
        List<URI> issuerParamsURIList = new ArrayList<URI>();

        // create a list of credspecs
        Map<String, CredentialSpecification> aliasCredSpecsMap = this.getCredSpecList(aliasCreds, ct);

        List<CredentialSpecification> credSpecsList = new ArrayList<CredentialSpecification>();
        for (CredentialSpecification cs : aliasCredSpecsMap.values()) {
            if (!credSpecsList.contains(cs)) {
                credSpecsList.add(cs);
            }
        }

        for (CredentialInToken credInToken : ptd.getCredential()) {
            issuerParamsURIList.add(credInToken.getIssuerParametersUID());
        }
        issuerParamsURIList.add(ct.getIssuerParametersUID());

        // prepare all parameters.
        this.loadIdemixIssuerParameters(issuerParamsURIList);
        com.ibm.zurich.idmx.utils.SystemParameters sysPar = this
                .loadIdemixSystemParameters().getSystemParameters();

        // create and load credential structures.
        CredentialStructure credStruct =
                this.loadIdemixCredentialStructuresForIssuance(credSpecsList, ct, context);

        // create UID of newly issued credential, allocate it on a smart-card.

        // Patras-specific code for course URIs.
        URI uidOfNewCredential = this.contextGen.getUniqueContext(URI.create(IdmxCredential));
        this.uidOfIssuedCredentialCache.put(context, uidOfNewCredential);

        // Find the UID of the secret.
        URI secretUid = null;
        URI sameKeyBindingAsUri = ct.getSameKeyBindingAs();
        secretUid =
                this.cryptoEngineUtil.getSmartcardUidFromPseudonymOrCredentialUri(aliasCreds, aliasNyms,
                        sameKeyBindingAsUri);

        this.secretCache.put(context, secretUid);

        // boolean isOnSmartcard = false;
        //TODO(Kasper): Should we not somehow save a credential on the card here? i.e replace null with a credential.
        this.smartcardManager.allocateCredential(secretUid, uidOfNewCredential,
                ct.getIssuerParametersUID(), false);

        // add carry over attribute values to the user values:
        // get user values that came from abce layer:
        List<MyAttribute> userValues = this.attributeCache.get(context);
        // add get carry over values:

        UnknownAttributes unknownAttributes = ct.getUnknownAttributes();
        if (unknownAttributes != null) {
            for (CarriedOverAttribute coa : unknownAttributes.getCarriedOverAttribute()) {
                Credential c = aliasCreds.get(coa.getSourceCredentialInfo().getAlias());
                for (Attribute at : c.getCredentialDescription().getAttribute()) {
                    if (at.getAttributeDescription().getType()
                            .equals(coa.getSourceCredentialInfo().getAttributeType())) {
                        at.getAttributeDescription().setType(coa.getTargetAttributeType());
                        userValues.add(new MyAttribute(at));
                    }
                }
            }
        }

        // convert abce attribute values to the idemix values
        Values idmxValues = IdemixUtils.createIdemixValues(sysPar, userValues);

        // start generating idemix evidence
        IdemixClaimGenerator idmxGenerator = new IdemixClaimGenerator(
                this.smartcardManager, this.keyManager, this.credManager,
                this.revocationProof, this.contextGen);

        // smartcardSecretUid
        URI annoymousNameOfSmarcardSecretUid =
                this.contextGen.getUniqueContext(URI.create("abc4trust://secretname/"));
        // parse issuance policy
        PolicyTranslator pt =
                new PolicyTranslator(ptd, ct, aliasCredSpecsMap, annoymousNameOfSmarcardSecretUid);
        // create idemix claim
        IdemixClaim idmxClaim = new IdemixClaim(pt, this.keyManager,
                this.contextGen, null);

        // create prover and proof spec for the advanced issuance
        try {
            proverInput =
                    idmxGenerator.generateProverInputForAdvancedIssuance(idmxClaim, aliasCreds, aliasNyms,
                            idmxValues, ct.getCredentialSpecUID(), ct.getIssuerParametersUID(),
                            uidOfNewCredential, secretUid);
            proofSpec = IdemixProofSpecGenerator.generateProofSpecForIssuance(
                    idmxClaim, IdemixConstants.groupParameterId, credStruct,
                    this.keyManager, this.contextGen);
            // System.out.println(proofSpec.toStringPretty());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // create advanced issuance spec from proof spec and credential structure
        AdvancedIssuanceSpec issuanceSpec =
                new AdvancedIssuanceSpec(ct.getIssuerParametersUID(), ct.getCredentialSpecUID(),
                        IdemixConstants.tempNameOfNewCredential, proofSpec);


        // create idmx advanced recipient and put it to the recipients cache
        AdvancedRecipient recipient = new AdvancedRecipient(issuanceSpec, proverInput, idmxValues);
        this.recipientCache.put(context, recipient);

        // generate the first idmx message from the issuer only here to use as an input to the recipient
        // since the first msg contains only nonce that was transfered in abce message

        HashMap<IssuanceProtocolValues, BigInteger> issuanceProtocolValues =
                new HashMap<Message.IssuanceProtocolValues, BigInteger>();
        issuanceProtocolValues.put(IssuanceProtocolValues.nonce, new BigInteger(ptd.getMessage()
                .getNonce()));
        Message msgToRecipient1 = new Message(issuanceProtocolValues, null, null);

        // generate message to the issuer
        Message msgToIssuer1 = recipient.round1(msgToRecipient1);

        // create a return object
        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        cryptoEvidence.getAny().add(XMLSerializer.getInstance().serializeAsElement(msgToIssuer1));

        return cryptoEvidence;

    }

    public CryptoParams generatePresentationCryptoEvidenceWithCommitmentsIdemix(
            PresentationTokenDescriptionWithCommitments ptd, LinkedHashMap<URI, Credential> aliasCreds,
            LinkedHashMap<URI, PseudonymWithMetadata> aliasNyms) throws CryptoEngineException{
        IdemixClaimGenerator idmxGenerator = new IdemixClaimGenerator(
                this.smartcardManager, this.keyManager, this.credManager,
                this.revocationProof, this.contextGen);
        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        //new

        List<URI> issuerParamsURIList = new ArrayList<URI>();

        try{
            // 	prepare all parameters
            for(CredentialInTokenWithCommitments credInToken: ptd.getCredential()){

                this.revocationProof.addCredInToken(
                        credInToken.getCredentialSpecUID(), credInToken);

                if (!issuerParamsURIList.contains(credInToken.getIssuerParametersUID()) && this.keyManager.getIssuerParameters(credInToken.getIssuerParametersUID()).getAlgorithmID().equals(CryptoUriUtil.getIdemixMechanism())){
                    issuerParamsURIList.add(credInToken.getIssuerParametersUID());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        this.loadIdemixIssuerParameters(issuerParamsURIList);
        this.loadIdemixSystemParameters();

        //create a list of credspecs
        LinkedHashMap<String, CredentialSpecification> aliasCredSpecsMap = this.getCredSpecList(aliasCreds, null);

        List<CredentialSpecification> credSpecsList = new ArrayList<CredentialSpecification>();
        for(CredentialSpecification cs: aliasCredSpecsMap.values()){
            if (!credSpecsList.contains(cs)){
                credSpecsList.add(cs);
            }
        }
        this.loadIdemixCredentialStructuresForPresentation(credSpecsList);

        try {
            cryptoEvidence.getAny().add(idmxGenerator.getPresentationEvidenceWithCommitment(ptd,
                    aliasCreds, aliasCredSpecsMap, aliasNyms, this.keyManager));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        /*        try{
        	cryptoEvidence.getAny().add(idmxGenerator.getVerifiableEncryptions());
        }catch(Exception e){
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
        // Inspection.
        for (CredentialInTokenWithCommitments cred : ptd.getCredential()) {
            CredentialSpecification credSpec = aliasCredSpecsMap.get(cred
                    .getAlias().toString());
            for (AttributeInToken attrInToken : cred.getDisclosedAttribute()) {
                AttributeDescription attrDesc = this.getAttributeFromType(
                        attrInToken.getAttributeType(), credSpec);

                Credential realCred = aliasCreds.get(cred.getAlias());

                Attribute attr = this.getAttribute(realCred, attrInToken);

                Element encryptAttributeIfNecessary = this
                        .encryptAttributeIfNecessary(attrDesc, attr,
                                attrInToken.getInspectorPublicKeyUID());
                if (encryptAttributeIfNecessary != null) {
                    cryptoEvidence.getAny().add(encryptAttributeIfNecessary);
                }
            }
        }*/



        return cryptoEvidence;
    }


    private IdemixSystemParameters loadIdemixSystemParameters()
            throws CryptoEngineException {
        SystemParameters systemParameters = null;
        try {
            systemParameters = this.keyManager.getSystemParameters();
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
        return IdemixCryptoEngineUserImpl.loadIdemixSystemParameters(systemParameters);
    }

    public CryptoParams generatePresentationCryptoEvidenceIdemix(PresentationTokenDescription ptd,
            LinkedHashMap<URI, Credential> aliasCreds,
            LinkedHashMap<URI, PseudonymWithMetadata> aliasNyms)
                    throws CryptoEngineException {
        IdemixClaimGenerator idmxGenerator = new IdemixClaimGenerator(
                this.smartcardManager, this.keyManager, this.credManager,
                this.revocationProof, this.contextGen);
        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        List<URI> issuerParamsURIList = new ArrayList<URI>();

        // prepare all parameters
        for (CredentialInToken credInToken : ptd.getCredential()) {
            this.revocationProof.addCredInToken(
                    credInToken.getCredentialSpecUID(), credInToken);
            if (!issuerParamsURIList.contains(credInToken.getIssuerParametersUID())) {
                issuerParamsURIList.add(credInToken.getIssuerParametersUID());
            }
        }

        this.loadIdemixIssuerParameters(issuerParamsURIList);

        // create a list of credspecs
        LinkedHashMap<String, CredentialSpecification> aliasCredSpecsMap = this.getCredSpecList(aliasCreds, null);

        List<CredentialSpecification> credSpecsList = new ArrayList<CredentialSpecification>();
        for (CredentialSpecification cs : aliasCredSpecsMap.values()) {
            if (!credSpecsList.contains(cs)) {
                credSpecsList.add(cs);
            }
        }
        this.loadIdemixCredentialStructuresForPresentation(credSpecsList);

        try {
            Element presentationEvidence = idmxGenerator.getPresentationEvidence(ptd, aliasCreds, aliasCredSpecsMap, aliasNyms);
            cryptoEvidence.getAny().add(presentationEvidence);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        // Inspection is handled in claim generator/proofspecgenerator

        /*try{
        	cryptoEvidence.getAny().add(idmxGenerator.getVerifiableEncryptions());
        }catch(Exception e){
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }*/
        return cryptoEvidence;
    }

    private Attribute getAttribute(Credential realCred,
            AttributeInToken attrInToken) {
        List<Attribute> realAttributes = realCred
                .getCredentialDescription()
                .getAttribute();

        for (Attribute attr : realAttributes) {
            if (attrInToken.getAttributeType().compareTo(
                    attr.getAttributeDescription().getType()) == 0) {
                return attr;
            }
        }
        return null;
    }



    // ///////////////////////////////////////////////////////////////////
    // /helpers
    // ///////////////////////////////////////////////////////////////////

    private AttributeDescription getAttributeFromType(URI attributeType,
            CredentialSpecification credSpec) {
        for (AttributeDescription ad : credSpec.getAttributeDescriptions()
                .getAttributeDescription()) {
            if (ad.getType().compareTo(attributeType) == 0) {
                return ad;
            }
        }
        return null;
    }



    private void loadIdemixIssuerParameters(
            List<URI> issuerParamsURIList) {
        IssuerPublicKey isPK = null;
        IssuerParameters issuerParameters = null;

        for (URI issuerURI : issuerParamsURIList) {
            // Add issuer public key to Idemix structure store.
            try {
                issuerParameters = this.keyManager.getIssuerParameters(issuerURI);
                CryptoParams cp = issuerParameters.getCryptoParams();
                isPK = (IssuerPublicKey) Parser.getInstance().parse((Element) cp.getAny().get(0));
                String issuerParametersAsStr = issuerParameters.getParametersUID().toString();
                this.logger.info("IssuerPublicKey" + issuerParametersAsStr
                        + ": " + isPK.getCapZ());
                StructureStore.getInstance().add(issuerParametersAsStr, isPK);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static IdemixSystemParameters loadIdemixSystemParameters(
            SystemParameters systemParameters)
                    throws CryptoEngineException {
        IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(systemParameters);
        com.ibm.zurich.idmx.utils.SystemParameters sysPar = idemixSystemParameters.getSystemParameters();
        GroupParameters grPar = idemixSystemParameters.getGroupParameters();

        // Load system, group and issuer parameters to Idemix StructureStore
        StructureStore.getInstance().add(IdemixConstants.systemParameterId, sysPar);
        StructureStore.getInstance().add(IdemixConstants.groupParameterId, grPar);
        return idemixSystemParameters;
    }



    private void loadIdemixCredentialStructuresForPresentation(
            List<CredentialSpecification> credSpecsList) {
        this.loadIdemixCredentialStructuresForIssuance(credSpecsList, null, null);
    }

    private CredentialStructure loadIdemixCredentialStructuresForIssuance(
            List<CredentialSpecification> credSpecsList, CredentialTemplate ct, URI context) {

        CredentialStructure ret = null;
        for (CredentialSpecification credSpec : credSpecsList) {
            CredentialStructure credStruct = null;
            // create credential structure for Idemix call and load it to Idemix Structure Store

            if (credSpec != null) {
                if ((ct != null) && (credSpec.getSpecificationUID().equals(ct.getCredentialSpecUID()))) {
                    credStruct =
                            IdemixUtils.createIdemixCredentialStructure(ct, credSpec, null,
                                    this.attributeCache.get(context), credSpec.isKeyBinding());
                    ret = credStruct;
                } else {
                    credStruct =
                            IdemixUtils.createIdemixCredentialStructure(null, credSpec, null, null,
                                    credSpec.isKeyBinding());
                }
                StructureStore.getInstance().add(credSpec.getSpecificationUID().toString(), credStruct);
            } else {
                throw new RuntimeException("cannot extract cred spec");
            }
        }
        return ret;
    }

    private LinkedHashMap<String, CredentialSpecification> getCredSpecList(Map<URI, Credential> aliasCreds,
            CredentialTemplate ct) {

        LinkedHashMap<String, CredentialSpecification> aliasCredSpecs = new LinkedHashMap<String, CredentialSpecification>();
        CredentialSpecification credSpec = null;
        for (URI credAlias : aliasCreds.keySet()) {
            try {
                Credential credential = aliasCreds.get(credAlias);
                CredentialDescription credentialDescription = credential
                        .getCredentialDescription();
                URI credentialSpecificationUID = credentialDescription
                        .getCredentialSpecificationUID();
                credSpec = this.keyManager
                        .getCredentialSpecification(credentialSpecificationUID);
                if (credSpec == null) {
                    throw new RuntimeException(
                            "Could not find credential specification with UID: \""
                                    + credentialSpecificationUID + "\"");
                }
            } catch (KeyManagerException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            aliasCredSpecs.put(credAlias.toString(), credSpec);
        }
        if (ct != null) {
            try {
                credSpec = this.keyManager.getCredentialSpecification(ct.getCredentialSpecUID());
                aliasCredSpecs.put(IdemixConstants.tempNameOfNewCredential, credSpec);
            } catch (KeyManagerException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return aliasCredSpecs;
    }


    @Override
    public PseudonymWithMetadata createPseudonym(URI pseudonymUri, String scope, boolean exclusive,
            URI secretReference) {
        PseudonymWithMetadata pwm = new PseudonymWithMetadata();
        Pseudonym p = new Pseudonym();
        pwm.setPseudonym(p);
        p.setExclusive(exclusive);
        p.setScope(scope.toString());
        p.setSecretReference(secretReference);
        p.setPseudonymUID(pseudonymUri);

        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        secretReference = this.cryptoEngineUtil.getSmartcardUri(secretReference);
        byte[] pseudonymValue = null;
        URI groupParameterId = URI.create(IdemixConstants.groupParameterId);
        if (exclusive) {
            BigInteger value =
                    this.smartcardManager.computeScopeExclusivePseudonym(secretReference, URI.create(scope));
            pseudonymValue = value.toByteArray();
            this.createNewScopeExclusivePseudonym(scope, secretReference, cryptoEvidence,
                    groupParameterId);
        } else {
            try {
                SystemParameters sysParams = this.keyManager.getSystemParameters();
                BigInteger h = this.getGroupParametersH(sysParams);
                BigInteger randomizer = this.getRandomizer(secretReference);
                BigInteger value = this.smartcardManager.computePseudonym(secretReference, h, randomizer);
                pseudonymValue = value.toByteArray();

                this.createNewPseudonym(secretReference, cryptoEvidence, groupParameterId, randomizer);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        // cryptoEvidence.getAny().add(cryptoParams);
        p.setPseudonymValue(pseudonymValue);
        pwm.setCryptoParams(cryptoEvidence);
        return pwm;
    }

    private BigInteger getGroupParametersH(SystemParameters sysParams) {
        IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(sysParams);

        // this will throw illegal state exception if not found!
        GroupParameters gp = idemixSystemParameters.getGroupParameters();

        BigInteger gpH = gp.getH();
        return gpH;
    }

    private BigInteger getRandomizer(URI secretReference) {
        BigInteger p = this.smartcardManager.getPseudonymModulusOfCard(secretReference);

        int randomizerLengthBits =
                p.bitLength() + (this.getSystemParameters().getZkStatisticalHidingSizeBytes() * 8);

        BigInteger randomizer = this.contextGen.getRandomNumber(randomizerLengthBits);
        return randomizer;
    }

    private void createNewScopeExclusivePseudonym(String scope, URI secretReference,
            CryptoParams cryptoParams, URI groupParameterId) {
        URI scopeUri = URI.create(scope);
        StoredDomainPseudonym dp =
                new StoredDomainPseudonym(scopeUri, secretReference, groupParameterId);
        cryptoParams.getAny().add(XMLSerializer.getInstance().serializeAsElement(dp));
    }

    private void createNewPseudonym(URI secretReference, CryptoParams cryptoParams, URI gpLocation,
            BigInteger randomizer) {
        com.ibm.zurich.idmx.dm.StoredPseudonym pseudonym =
                new StoredPseudonym(secretReference, gpLocation, randomizer);
        cryptoParams.getAny().add(XMLSerializer.getInstance().serializeAsElement(pseudonym));
    }

    @Override
    public Secret createSecret() {
        URI newSdUri = this.contextGen.getUniqueContext(URI.create("abc4trust://secret"));
        SecretDescription newSd = new SecretDescription();
        newSd.setDeviceBoundSecret(false);
        newSd.setSecretUID(newSdUri);
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("New computer-based secret " + newSdUri);
        newSd.getFriendlySecretDescription().add(fd);
        newSd.setMetadata(null);
        this.logger.info("Creating a new secret " + newSdUri);
        Secret s = new Secret();
        SmartcardSystemParameters smartCardSysParams = this.getSystemParameters();
        s.setSystemParameters(smartCardSysParams);
        int deviceSecretSizeBytes = s.getSystemParameters().getDeviceSecretSizeBytes();
        s.setSecretKey(this.contextGen.getRandomNumber(deviceSecretSizeBytes * 8));
        s.setSecretDescription(newSd);
        return s;
    }

    private SmartcardSystemParameters getSystemParameters() {
        SmartcardSystemParameters smartCardSysParams;
        try {
            smartCardSysParams = SystemParametersUtil
                    .createSmartcardSystemParameters(this.keyManager
                            .getSystemParameters());
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }
        return smartCardSysParams;
    }

    @Override
    public boolean isRevoked(Credential cred) throws CryptoEngineException {
        return this.userRevocation.isRevoked(cred);
    }
}

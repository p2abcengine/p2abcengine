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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PresentationProofComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;
import org.w3c.dom.Element;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.user.CryptoEngineUtil;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveKeyAndToken;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSerializer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.util.CommitmentStripper;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.xml.SystemParameters;

public class UProveEvidenceGenerator {
    private static final int CRYPTO_PARAMS_TOKEN_INDEX = 0;
    private static final int CRYPTO_PARAMS_ISSUER_PARAMETER = 2;
	public static boolean DELETE_TOKEN = true;

    private final KeyManager keyManager;
    private final UProveBindingManager binding;
    private final UProveUtils utils;
    private final CredentialManager credManager;
    @SuppressWarnings("unused")
    private final CryptoEngineUtil cryptoEngineUtil;
    private final CardStorage cardStorage;
    private final ReloadTokensCommunicationStrategy reloadTokens;

    public UProveEvidenceGenerator(CredentialManager credManager,
            KeyManager keyManager, CryptoEngineUtil cryptoEngineUtil,
            UProveBindingManager binding, CardStorage cardStorage, ReloadTokensCommunicationStrategy reloadTokens) {
        this.credManager = credManager;
        this.binding = binding;
        this.keyManager = keyManager;
        this.cryptoEngineUtil = cryptoEngineUtil;
        this.utils = new UProveUtils();
        this.cardStorage = cardStorage;
        this.reloadTokens= reloadTokens;
    }

    public List<Object> getPresentationEvidence(
            PresentationTokenDescriptionWithCommitments ptdwc,
            Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms) throws CredentialManagerException{
        List<Object> cryptoEvidence = new LinkedList<Object>();

        PresentationTokenDescription ptd = CommitmentStripper.stripPresentationTokenDescription(ptdwc);
        // Map message to U-Prove message string.
        String applicationMessage = this.utils.normalizeApplicationMessage(ptd);


        // First do the pseudonyms.
        List<PseudonymInToken> pseudonyms = ptd.getPseudonym();
        cryptoEvidence.addAll(this.presentationEvidenceForPseudonyms(
                aliasCreds, pseudonyms, aliasNyms, applicationMessage));
        Map<String, CredentialSpecification> aliasCredSpecsMap = this
                .getCredSpecList(aliasCreds, null);

        // Then do the credentials.
        Collection<? extends Object> presentationEvidenceForCredentials = this
                .presentationEvidenceForCredentials(ptdwc, aliasCreds, aliasNyms,
                        applicationMessage, aliasCredSpecsMap);
        if (presentationEvidenceForCredentials == null) {
            // We have run out of U-Prove tokens.
            return null;
        }

        cryptoEvidence.addAll(presentationEvidenceForCredentials);

        return cryptoEvidence;
    }

    public List<Object> getPresentationEvidence(
            PresentationTokenDescription ptd,
            Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms)
                    throws CredentialManagerException {
        List<Object> cryptoEvidence = new LinkedList<Object>();

        // Map message to U-Prove message string.
        String applicationMessage = this.utils.normalizeApplicationMessage(ptd);

        // First do the pseudonyms.
        List<PseudonymInToken> pseudonyms = ptd.getPseudonym();
        cryptoEvidence.addAll(this.presentationEvidenceForPseudonyms(
                aliasCreds, pseudonyms, aliasNyms, applicationMessage));
        Map<String, CredentialSpecification> aliasCredSpecsMap = this
                .getCredSpecList(aliasCreds, null);

        // Then do the credentials.
        Collection<? extends Object> presentationEvidenceForCredentials = this
                .presentationEvidenceForCredentials(ptd, aliasCreds, aliasNyms,
                        applicationMessage, aliasCredSpecsMap);
        if (presentationEvidenceForCredentials == null) {
            // We have run out of U-Prove tokens.
            return null;
        }
        cryptoEvidence.addAll(presentationEvidenceForCredentials);

        return cryptoEvidence;
    }

    /**
     * Prover generates proof for each pseudonym.
     * 
     * @param aliasCreds
     * @param pseudonyms
     * @param aliasNyms
     * @param applicationMessage
     * @return
     * @throws CredentialManagerException
     */
    private Collection<? extends Object> presentationEvidenceForPseudonyms(
            Map<URI, Credential> aliasCreds,
            List<PseudonymInToken> pseudonyms,
            Map<URI, PseudonymWithMetadata> aliasNyms, String applicationMessage)
                    throws CredentialManagerException {

        URI scURI = UProveUtils.getSmartcardUri(this.cardStorage);
        SystemParameters syspars;
        try {
            syspars = this.keyManager.getSystemParameters();
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }

        int keyLength = new UProveSystemParameters(syspars).getKeyLength();

        String sessionKey = null;
        if(scURI != null){
            int credID = -1;
            sessionKey = this.utils.getSessionKey(this.binding, this.cardStorage, credID, keyLength);
        }else{
          sessionKey = this.utils.getSessionKey(this.binding, keyLength);
        }

        Map<BigInteger, PseudonymWithMetadata> valueNyms = new HashMap<BigInteger, PseudonymWithMetadata>();
        for (PseudonymWithMetadata p: aliasNyms.values()) {
            byte[] pseudonymValue = p.getPseudonym().getPseudonymValue();
            BigInteger n = new BigInteger(pseudonymValue);
            valueNyms.put(n, p);
        }

        List<Object> evidence = new LinkedList<Object>();
        for (int inx = 0; inx < pseudonyms.size(); inx++) {
            PseudonymInToken pseudonym = pseudonyms.get(inx);

            UProveSerializer serializer = new UProveSerializer();

            byte[] pseudonymValue = pseudonym.getPseudonymValue();
            BigInteger n = new BigInteger(pseudonymValue);
            PseudonymWithMetadata pwm = valueNyms.get(n);

            List<Object> any = pwm.getCryptoParams().getAny();
            Element issuerParameterElement = (Element) any.get(CRYPTO_PARAMS_ISSUER_PARAMETER);
            // TODO: this fails if there are only 2 elements in any (which is the case when cred is non-revocable see UProveCryptoEngineUserImpl)
            IssuerParametersComposite ipc = serializer
                    .deserializeToIssuerParametersComposite(issuerParameterElement);
            this.binding.verifyIssuerParameters(ipc, sessionKey);

            String scope = "null";
            if (pseudonym.isExclusive()) {
                scope = pseudonym.getScope();
            }
            PseudonymComposite proof = this.binding.presentPseudonym(
                    applicationMessage, scope, sessionKey);
            URI nymAlias = this.utils.getAlias(inx, pseudonym);

            evidence.add(serializer.serialize(proof, nymAlias, ipc));

            // pseudonym.setPseudonymValue(proof.getP().getValue());
        }
        this.binding.logout(sessionKey);
        return evidence;
    }

    private Collection<? extends Object> presentationEvidenceForCredentials(
            PresentationTokenDescriptionWithCommitments ptdwc, Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms,
            String applicationMessage,
            Map<String, CredentialSpecification> aliasCredSpecs){

        List<Object> evidence = new LinkedList<Object>();

        // Call U-Prove for each credential and get associated Presentation
        // token. Wrap those into the cryptoEvidence of the PresentationToken
        // that is to be returned.
        List<CredentialInTokenWithCommitments> credentials = ptdwc.getCredential();
        for (int credIndex = 0; credIndex < credentials.size(); credIndex++) {
            CredentialInTokenWithCommitments credInToken = credentials.get(credIndex);

            URI credAlias = this.utils.getAlias(credIndex, CommitmentStripper.stripCredentialInToken(credInToken));

            Credential cred = aliasCreds.get(credAlias);
            //We need to get a token from the credential. getProofToken may contact the issuer and re-issue the credential
            //if there are not enough tokens. Thus we need to reload the credential from credManager just to make sure.
            ArrayList<UProveKeyAndToken> currentToken = new ArrayList<UProveKeyAndToken>(); 
            currentToken.add(this.getProofToken(aliasNyms.keySet(), cred));
            try {
				cred = credManager.getCredential(cred.getCredentialDescription().getCredentialUID());
			} catch (CredentialManagerException e) {
				throw new ReloadTokensCommunicationStrategy.ReloadException("Failed to reload credential after update");
			}
            
            UProveTokenComposite compositeToken = this.utils.convertUProveKeyAndToken(currentToken).get(0);
            byte[] privateKey = this.utils.getUProveTokenPrivateKeys(currentToken).get(0);

            //Login session using credID
            URI scURI = UProveUtils.getSmartcardUri(this.cardStorage);
            SystemParameters syspars;
            try {
              syspars = this.keyManager.getSystemParameters();
            } catch (KeyManagerException ex) {
              throw new RuntimeException(ex);
            }

            int keyLength = new UProveSystemParameters(syspars).getKeyLength();

            String sessionKey = null;
            if(scURI != null){
                HardwareSmartcard sc = (HardwareSmartcard)this.cardStorage.getSmartcard(scURI);
                int credID = sc.getCredentialIDFromUri(this.cardStorage.getPin(scURI), cred.getCredentialDescription().getCredentialUID());
                sessionKey = this.utils.getSessionKey(this.binding, this.cardStorage, credID, keyLength);
            }else{
              sessionKey = this.utils.getSessionKey(this.binding, keyLength);
            }

            // Get the relevant IssuerParameters.
            IssuerParametersComposite ipc = this.convertIssuerParameters(cred);

            // Map disclosed attributes to U-Prove integer indexes.
            UProveDisclosedAttributes disclosedAttributes = this.getExplicitlyDisclosedAttributes(ipc,
                    CommitmentStripper.stripPresentationTokenDescription(ptdwc), aliasCredSpecs, cred);

            //Map committed attributes to U-Prove integer indexes.
            //Note we also commit to inspectable attributes
            ArrayOfint committedAttributes = this.getCommittedAttributes(ptdwc, aliasCredSpecs, cred);

            //Add revocation handle to list of committed attributes if necessary
            this.addRevocationHandleToCommittedAttributes(ptdwc, aliasCredSpecs, cred, committedAttributes);

            if(committedAttributes.getInt().size() == 0) {
                committedAttributes = null;
            }
            // A credential is not a pseudonym so set the verifierScopeParam to
            // null.
            String verifierScopeParam = "null";
            // TODO(jdn): Remove the scope parameter (null)?

            if(committedAttributes != null)
                java.util.Collections.sort(committedAttributes.getInt());

            // Prover generates proof for the above token
            PresentationProofComposite proof = this.binding.proveToken(
                    disclosedAttributes.getArrayOfStringAttributesParam(),
                    disclosedAttributes.getArrayOfIntDisclosedParam(),
                    committedAttributes, applicationMessage,
                    verifierScopeParam, ipc, compositeToken, privateKey, sessionKey);

            //String sessionKeyFoo = utils.getSessionKey(binding);
            Boolean verified = this.binding.verifyTokenProof(proof, disclosedAttributes.getArrayOfIntDisclosedParam(),
                    committedAttributes, applicationMessage,
                    verifierScopeParam, ipc, compositeToken, sessionKey);

            System.out.println("\n\n\n PROOF VERIFIED? ::: "+verified+" \n\n\n");
            
            UProveSerializer serializer = new UProveSerializer();
            evidence.add(serializer.serialize(proof, credAlias,
                    disclosedAttributes.getArrayOfIntDisclosedParam(),
                    committedAttributes, compositeToken, ipc));
        }
        //        String sessionKey = utils.getSessionKey(binding);
        //        binding.logout(sessionKey);
        return evidence;

    }

    private void addRevocationHandleToCommittedAttributes(
            PresentationTokenDescriptionWithCommitments ptdwc,
            Map<String, CredentialSpecification> aliasCredSpecs,
            Credential cred, ArrayOfint committedAttributes) {
        try {
            CredentialSpecification credSpec = this.keyManager.getCredentialSpecification(cred.getCredentialDescription().getCredentialSpecificationUID());
            if(credSpec.isRevocable()){
                List<AttributeDescription> descriptions =credSpec.getAttributeDescriptions().getAttributeDescription();

                for(int counter = 1; counter<= descriptions.size(); counter++){
                    AttributeDescription desc = descriptions.get(counter-1);
                    if(desc.getType().equals(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"))){
                        if(!committedAttributes.getInt().contains(new Integer(counter))) {
                            committedAttributes.getInt().add(new Integer(counter));
                        }
                    }
                }
            }

        } catch (KeyManagerException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private Collection<? extends Object> presentationEvidenceForCredentials(
            PresentationTokenDescription ptd, Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms,
            String applicationMessage,
            Map<String, CredentialSpecification> aliasCredSpecs){

        ArrayOfint committedAttributes = null;
        List<Object> evidence = new LinkedList<Object>();
        // Call U-Prove for each credential and get associated Presentation
        // token. Wrap those into the cryptoEvidence of the PresentationToken
        // that is to be returned.

        List<CredentialInToken> credentials = ptd.getCredential();
        for (int credIndex = 0; credIndex < credentials.size(); credIndex++) {
            CredentialInToken credInToken = credentials.get(credIndex);

            URI credAlias = this.utils.getAlias(credIndex, credInToken);

            Credential cred = aliasCreds.get(credAlias);
            //We need to get a token from the credential. getProofToken may contact the issuer and re-issue the credential
            //if there are not enough tokens. Thus we need to reload the credential from credManager just to make sure.
            ArrayList<UProveKeyAndToken> currentToken = new ArrayList<UProveKeyAndToken>(); 
            currentToken.add(this.getProofToken(aliasNyms.keySet(), cred));
            try {
				cred = credManager.getCredential(cred.getCredentialDescription().getCredentialUID());
			} catch (CredentialManagerException e) {
				throw new ReloadTokensCommunicationStrategy.ReloadException("Failed to reload credential after update");
			}

            UProveTokenComposite compositeToken = this.utils.convertUProveKeyAndToken(currentToken).get(0);
            byte[] privateKey = this.utils.getUProveTokenPrivateKeys(currentToken).get(0);

            //Login session using credID
            URI scURI = UProveUtils.getSmartcardUri(this.cardStorage);
            SystemParameters syspars;
            try {
              syspars = this.keyManager.getSystemParameters();
            } catch (KeyManagerException ex) {
              throw new RuntimeException(ex);
            }

            int keyLength = new UProveSystemParameters(syspars).getKeyLength();

            String sessionKey = null;
            if(scURI != null){
                HardwareSmartcard sc = (HardwareSmartcard)this.cardStorage.getSmartcard(scURI);
                int credID = sc.getCredentialIDFromUri(this.cardStorage.getPin(scURI), cred.getCredentialDescription().getCredentialUID());
                sessionKey = this.utils.getSessionKey(this.binding, this.cardStorage, credID, keyLength);
            }else{
              sessionKey = this.utils.getSessionKey(this.binding, keyLength);
            }

            // Get the relevant IssuerParameters.
            IssuerParametersComposite ipc = this.convertIssuerParameters(cred);

            // Map disclosed attributes to U-Prove integer indexes.
            UProveDisclosedAttributes disclosedAttributes = this
                    .getExplicitlyDisclosedAttributes(ipc,
                            ptd, aliasCredSpecs, cred);

            // A credential is not a pseudonym so set the verifierScopeParam to
            // null.
            String verifierScopeParam = "null";
            // TODO(jdn): Remove the scope parameter (null)?

            // Prover generates proof for the above token
            PresentationProofComposite proof = this.binding.proveToken(
                    disclosedAttributes.getArrayOfStringAttributesParam(),
                    disclosedAttributes.getArrayOfIntDisclosedParam(),
                    committedAttributes, applicationMessage,
                    verifierScopeParam, ipc, compositeToken, privateKey, sessionKey);


            UProveSerializer serializer = new UProveSerializer();
            evidence.add(serializer.serialize(proof, credAlias,
                    disclosedAttributes.getArrayOfIntDisclosedParam(),
                    committedAttributes, compositeToken, ipc));

        }
        //        String sessionKey = utils.getSessionKey(binding);
        //        binding.logout(sessionKey);
        return evidence;


    }

    /*
     * private void setEvidence(PresentationTokenDescription ptd,
     * UProveDisclosedAttributes disclosedAttributes, CryptoParams
     * cryptoEvidence, UProveTokenComposite compositeToken,
     * PresentationProofComposite proof) {
     * Save the PresentationProofComposite to items in the PresentationToken according
     * to the following rules:
     * proof.disclosedAttributes -> presentationtoken:disclosedAttribute:attributevalue
     * proof.ps -> presentationtoken:pseudonym:pseudonymvalue
     * _rest_ of the proof -> cryptoevidence into new xml elements
     * also, disclosedParam and uproveToken needs to be added to the cryptoevidence
     * 
     * CryptoParams cryptoEvidenceTemp = of.createCryptoParams();
     * 
     * // Add relevant PresentationProofComposite parts to // cryptoevidence
     * byte[] valueA = proof.getA().getValue(); byte[] bs = proof.getAp() ==
     * null ? null : proof.getAp() .getValue(); ArrayOfbase64Binary valueR =
     * proof.getR().getValue();
     * 
     * cryptoEvidenceTemp.getAny().add(valueA);
     * cryptoEvidenceTemp.getAny().add(bs); // optional, check for null
     * cryptoEvidenceTemp.getAny().add(valueR.getBase64Binary());
     * 
     * // Add disclosedParam and UProveToken to cryptoevidence for // usage in
     * the Verifier Engine. cryptoEvidenceTemp.getAny().add(
     * disclosedAttributes.getArrayOfIntDisclosedParam()); // Add committed
     * values.
     * 
     * // cryptoEvidenceTemp.getAny().add(committedAttributes); //
     * cryptoEvidenceTemp.getAny().add(proof.getTildeValues() == // null ? null
     * : // proof.getTildeValues().getValue().getBase64Binary());
     * 
     * cryptoEvidenceTemp.getAny().add(compositeToken); PresentationToken pToken
     * = of.createPresentationToken();
     * pToken.setPresentationTokenDescription(ptd);
     * 
     * // TODO: the commitment values must be saved into the //
     * PresentationTokenWithCommitment: // for each attribute identified in the
     * committedAttributes // index array // unpack the proof.getTildeValues and
     * tildaO into the // Commitment, CommittedValue, and OpeningInformation
     * elements.
     * 
     * // Add relevant PresentationProofComposite parts to // PresentationToken
     * PresentationTokenDescription presentationTokenDescription = this.pToken
     * .getPresentationTokenDescription(); if
     * (presentationTokenDescription.getPseudonym().size() > 0) {
     * JAXBElement<byte[]> ps = proof.getPs(); JAXBElement<byte[]> tokenID =
     * proof.getTokenID(); byte[] value = exclusive ? ps.getValue() : tokenID
     * .getValue(); PseudonymInToken pseudonymInToken =
     * this.presentationTokenDescription .getPseudonym().get(0);
     * pseudonymInToken.setPseudonymValue(value); // optional, // check for
     * null. } List<CredentialInToken> credential =
     * this.presentationTokenDescription .getCredential(); CredentialInToken
     * credentialInToken = this.credential.get(0); List<AttributeInToken>
     * disclosedAttribute = this.credentialInToken .getDisclosedAttribute(); if
     * (disclosedAttribute.size() > 0) { AttributeInToken attributeInToken =
     * this.disclosedAttribute .get(0); JAXBElement<ArrayOfbase64Binary>
     * disclosedAttributes = proof .getDisclosedAttributes();
     * ArrayOfbase64Binary value = disclosedAttributes.getValue(); List<byte[]>
     * attributeValueBase64Binary = value .getBase64Binary(); attributeInToken
     * .setAttributeValue(attributeValueBase64Binary); }
     * pToken.setVersion("1.0"); pToken.setCryptoEvidence(cryptoEvidenceTemp);
     * 
     * // Add the presentationToken for this credential to the // cryptoevidence
     * for the PresentationToken that is to be // returned
     * cryptoEvidence.getAny().add(pToken); }
     */

    /**
     * Map committed attributes to U-Prove integer indexes.
     * 
     * @param ptdwc
     * @param aliasCredSpecs
     * @param cred
     * @return
     */
    private ArrayOfint getCommittedAttributes(
            PresentationTokenDescriptionWithCommitments ptdwc,
            Map<String, CredentialSpecification> aliasCredSpecs, Credential cred){

        ArrayOfint ret = new ArrayOfint();
        List<CredentialInTokenWithCommitments> credsInToken = ptdwc.getCredential();
        CredentialSpecification credSpec = null;

        try{
            credSpec = this.keyManager.getCredentialSpecification(cred.getCredentialDescription().getCredentialSpecificationUID());
        }catch(KeyManagerException e){
            throw new RuntimeException(e);
        }

        List<AttributeDescription> attributeDescriptions = credSpec.getAttributeDescriptions().getAttributeDescription();
        for (CredentialInTokenWithCommitments citwc: credsInToken) {
            if(!citwc.getCredentialSpecUID().equals(credSpec.getSpecificationUID())){
                continue;
            }

            for(int inx = 1; inx <= attributeDescriptions.size(); inx++){
                AttributeDescription ad = attributeDescriptions.get(inx-1);
                //First we check if ad is in the committed values
                for(int i = 0; i<citwc.getCommittedAttribute().size(); i++){
                    CommittedAttribute ca = citwc.getCommittedAttribute().get(i);
                    if(ad.getType().equals(ca.getAttributeType())){
                    	if(!ret.getInt().contains(new Integer(inx))){
                    		ret.getInt().add(new Integer(inx));
                    	}
                    }
                }

                //Then we check if ad is in the inspected values
                for(int i = 0; i<citwc.getDisclosedAttribute().size(); i++){
                    AttributeInToken ait = citwc.getDisclosedAttribute().get(i);
                    if(ad.getType().equals(ait.getAttributeType()) && (ait.getInspectorPublicKeyUID()!=null)){
                        if(!ret.getInt().contains(new Integer(inx))) {
                            ret.getInt().add(new Integer(inx));
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Map disclosed attributes to U-Prove integer indexes.
     * 
     * @param ipc
     * 
     * @param ptd
     * @param aliasCredSpecs
     * @param cred
     * @return
     */
    //TODO indeces start in 1 instead of 0, might be buggy!
    //Note that we only add a disclosed attribute, if it is not marked for inspection
    private UProveDisclosedAttributes getExplicitlyDisclosedAttributes(
            IssuerParametersComposite ipc, PresentationTokenDescription ptd,
            Map<String, CredentialSpecification> aliasCredSpecs, Credential cred) {
        List<CredentialInToken> credsintok = null;
        int numberOfAttributesInCredential = 0;
        credsintok = ptd.getCredential();
        numberOfAttributesInCredential = credsintok.size();

        CredentialDescription credentialDescription = cred
                .getCredentialDescription();

        @SuppressWarnings("deprecation")
        ArrayOfstring arrayOfStringAttributesParam = this.utils
        .convertAttributesToUProveAttributes(ipc,
                credentialDescription.getAttribute(), /*dummy*/ false);

        int numberOfAttributesInDescription = credentialDescription
                .getAttribute()
                .size();
        ArrayOfint arrayOfIntDisclosedParam = new ArrayOfint();

        for (int inx = 0; inx < numberOfAttributesInCredential; inx++) {
            CredentialInToken credintok = null;
            List<AttributeInToken> attrsintok = null;
            credintok = credsintok.get(inx);

            attrsintok = credintok.getDisclosedAttribute();
            for (AttributeInToken attrintok : attrsintok) {
                if(attrintok.getInspectorPublicKeyUID()!=null) {
                    continue;
                }
                URI disclosedAttrType = attrintok.getAttributeType();
                // Find indexes in credential attributes list
                for (int jnx = 0; jnx < numberOfAttributesInDescription; jnx++) {
                    Attribute att = credentialDescription.getAttribute()
                            .get(jnx);
                    if (att.getAttributeDescription().getType().toString()
                            .equals(disclosedAttrType.toString())) {
                        arrayOfIntDisclosedParam.getInt().add(
                                new Integer(jnx+1));  // Indices in Uprove start at 1 instead of 0
                    }
                }
            }
        }
        UProveDisclosedAttributes disclosedAttributes = new UProveDisclosedAttributes(
                arrayOfStringAttributesParam, arrayOfIntDisclosedParam);
        return disclosedAttributes;
    }

    /**
     * Convert IssuerParameters to IssuerParametersComposite for U-Prove
     * Webservice interop compatibility.
     * 
     * @param cred
     * @return
     */
    private IssuerParametersComposite convertIssuerParameters(Credential cred) {
        IssuerParameters issuerParameters = null;
        URI issuerParametersUid = cred.getCredentialDescription()
                .getIssuerParametersUID();
        try {
            issuerParameters = this.keyManager
                    .getIssuerParameters(issuerParametersUid); // TODO: cache
            // the data for perf.
        } catch (Exception e) {
            throw new RuntimeException(new CryptoEngineException(e));
        }
        IssuerParametersComposite ipc = null;
        if(issuerParameters.getAlgorithmID().equals(CryptoUriUtil.getUproveMechanism())) {
            ipc = this.utils
                    .convertIssuerParameters(issuerParameters);
        }

        return ipc;
    }

    private Map<String, CredentialSpecification> getCredSpecList(
            Map<URI, Credential> aliasCreds, CredentialTemplate ct) {

        Map<String, CredentialSpecification> aliasCredSpecs = new HashMap<String, CredentialSpecification>();
        CredentialSpecification credSpec = null;
        for (URI credAlias : aliasCreds.keySet()) {

            try {
                credSpec = this.keyManager
                        .getCredentialSpecification((aliasCreds.get(credAlias)
                                .getCredentialDescription()
                                .getCredentialSpecificationUID()));
            } catch (KeyManagerException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            aliasCredSpecs.put(credAlias.toString(), credSpec);
        }
        if (ct != null) {
            try {
                credSpec = this.keyManager.getCredentialSpecification(ct
                        .getCredentialSpecUID());
            } catch (KeyManagerException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            aliasCredSpecs.put(IdemixConstants.tempNameOfNewCredential,
                    credSpec);
        }
        return aliasCredSpecs;
    }

    /*
     * Private utility method for retrieving a token andstoring/deleting a UProve 
     * Token according to the following rules: If the
     * presentation policy contains a pseudonym element, then if an available
     * token has been saved for the scope, use it, otherwise, pick an unused
     * UProve token and save it for the scope. else (if pseudonym xml element is
     * not present in policy), pick the first available UProve token and delete
     * after use 
     * 
     * Returns new token if ok, else throws a ReloadException if we have run 
     * out of UProve tokens and failed to reload new ones
     */
    private UProveKeyAndToken getProofToken(Set<URI> pseudonyms, Credential cred) {
        // Fetch the UProveTokens and private keys ArrayLists
        // ArrayList<UProveKeyAndToken> == one ABC4Trust Credential with
        // array size amount of usage before the credential must be renewed
        // (Minus Tokens saved for a scope))
    	ArrayList<UProveKeyAndToken> keysAndTokens;
    	URI credUid = cred.getCredentialDescription().getCredentialUID();
    	if(UProveIssuanceHandlingImpl.STORE_TOKENS_ON_SMARTCARD){
	        @SuppressWarnings("unchecked")
	        ArrayList<UProveKeyAndToken> tmp = (ArrayList<UProveKeyAndToken>) cred.getCryptoParams().getAny().get(CRYPTO_PARAMS_TOKEN_INDEX);
            keysAndTokens = tmp;
    	}else{    		    	
	    	keysAndTokens = this.getUProveTokensInStorage(credUid);
    	}

        int tokenIndex = -1;
        int savedTokenIndex = -1;
        int unUsedIndex = -1;
        int unUsedCount = 0;
        UProveKeyAndToken res = null;
        
        if (pseudonyms.isEmpty()) {
            pseudonyms=null; //the logic below expects this
        }

        for (int i = 0; i < keysAndTokens.size(); i++) {
            UProveKeyAndToken token = keysAndTokens.get(i);
            // Register an unused token if found
            if (token.getToken().getPseudonyms().size() ==0) {
                unUsedIndex = i;
            }
            // The presentation policy contains a pseudonym element and we
            // search for an available token that has been saved before..
            if ((token.getToken().getPseudonyms()==null) || token.getToken().getPseudonyms().isEmpty()) {
                unUsedCount++;
            }

            if (pseudonyms != null) {
                for(URI u: pseudonyms){
                    if (token.getToken().getPseudonyms().contains(u)) {
                        savedTokenIndex = i;
                        System.out.println("Setting savedTokenIndex to " + i);
                    }
                }
            }
        }
        //assert( (unUsedIndex!=-1) == (unUsedCount>0) )
        if ((unUsedIndex!=-1) != (unUsedCount>0))
        	throw new ReloadTokensCommunicationStrategy.ReloadException("Reload tokens failed. Logic error");
        
        if ((unUsedCount==0) && (savedTokenIndex==-1)) {
            //0 tokens left and no previous saved token found. Reload!
            try {
            	System.out.println("\n\nRELOADING TOKENS!!!! \n\n");
                cred = this.reloadTokens.reloadTokens(cred);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ReloadTokensCommunicationStrategy.ReloadException("failed to load new tokens: "+ e.getMessage());
            }
          
            //refresh list since it should now be reflected in the storage, be it smartcard or disc
            if(UProveIssuanceHandlingImpl.STORE_TOKENS_ON_SMARTCARD){
	            //use tmp to allow for @SuppressWarnings
	            @SuppressWarnings("unchecked")
	            ArrayList<UProveKeyAndToken> tmp = (ArrayList<UProveKeyAndToken>) cred.getCryptoParams().getAny().get(CRYPTO_PARAMS_TOKEN_INDEX);
	            keysAndTokens = tmp;
            }else{            	            
	            keysAndTokens = this.getUProveTokensInStorage(credUid);
	            if(keysAndTokens.size() == 0){
	            	throw new ReloadTokensCommunicationStrategy.ReloadException("Reloaded tokens, but they where not stored correctly in persistent memory.");
	            }
            }

            tokenIndex=0;
        } else {
        	//either we have a saved token or we have a new unused token index    		
        	if (pseudonyms != null) {
        		//presentation policy contains a pseudonym element
        		if (savedTokenIndex != -1) {
        			// A saved token was found
        			tokenIndex = savedTokenIndex;
        		} else {
        			// no saved token was found but an unused one was found
        			//assert ( unUsedIndex != -1 )
        			tokenIndex = unUsedIndex;
        		} 
            	UProveKeyAndToken token = keysAndTokens.get(tokenIndex);
            	for(URI u: pseudonyms){
            		if(!token.getToken().getPseudonyms().contains(u)){
            			token.getToken().getPseudonyms().add(u);
            		}
            	}
            	keysAndTokens.set(tokenIndex, token);
            	System.out.println("Saving a token...");
        	} else {
                // The presentation policy does not contain a pseudonym element
           		tokenIndex = unUsedIndex;
            }                
        }
        
        // We have not run out of UProve tokens
        // assert( tokenIndex != -1)
      
        res = keysAndTokens.get(tokenIndex);
        
        if (pseudonyms == null) {
        	//non pseudonym token, only use once, delete from storage
        	if(DELETE_TOKEN){
        		keysAndTokens.remove(tokenIndex);
        	}
        }
        
        if(UProveIssuanceHandlingImpl.STORE_TOKENS_ON_SMARTCARD){
	        //update crypto params in credential (in storage) 
	        ObjectFactory of = new ObjectFactory();
	        CryptoParams cryptoEvidenceNew = of.createCryptoParams();
	
	        // We need only want to change the keysAndTokens of the credential
	        // other cryptoparams (eg. revocation information) need to remain
	        List<Object> any = cred.getCryptoParams().getAny();
	        for (int i = 0;i< any.size();i++) {
	        	Object o = any.get(i);
	        	if (i!=CRYPTO_PARAMS_TOKEN_INDEX) {
	        		cryptoEvidenceNew.getAny().add(o);
	        	} else {
	        		cryptoEvidenceNew.getAny().add(keysAndTokens);
	        	}
	        }
	        cred.setCryptoParams(cryptoEvidenceNew);
	
	        try {
	        	System.out.println("Updating credential...");
	        	this.credManager.updateCredential(cred);
	        } catch (CredentialManagerException e) {
	        	throw new RuntimeException(new CryptoEngineException(e));
	        }
        }else{
	        //update token array in persistent storage
	        try {
				this.keyManager.storeCredentialTokens(credUid, keysAndTokens);
			} catch (KeyManagerException e) {
				throw new RuntimeException(e);
			}
        }
        return res;
    }

	@SuppressWarnings("unchecked")
	private ArrayList<UProveKeyAndToken> getUProveTokensInStorage(URI uid) {
		ArrayList<?> tokens = this.keyManager.getCredentialTokens(uid);
		if(tokens == null){
			System.out.println("WARNING: No Uprove token file where found! Returning an empty one to UProveEvidenceGenerator");
			return new ArrayList<UProveKeyAndToken>();
		}
		System.out.println("Found this amount of tokens: " + tokens.size());
		return (ArrayList<UProveKeyAndToken>) tokens;
	}
}

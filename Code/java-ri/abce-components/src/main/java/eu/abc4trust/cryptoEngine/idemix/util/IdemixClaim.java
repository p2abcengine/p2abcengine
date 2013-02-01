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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.zurich.idmx.dm.MessageToSign;
import com.ibm.zurich.idmx.dm.Representation;
import com.ibm.zurich.idmx.dm.RepresentationOpening;
import com.ibm.zurich.idmx.showproof.Proof;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.Verifier;
import com.ibm.zurich.idmx.showproof.VerifierInput;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.showproof.predicates.DomainNymPredicate;
import com.ibm.zurich.idmx.showproof.predicates.Predicate;
import com.ibm.zurich.idmx.showproof.predicates.Predicate.PredicateType;
import com.ibm.zurich.idmx.showproof.predicates.PseudonymPredicate;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.revocation.RevocationProof;
import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.abce.internal.revocation.VerifierRevocation;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcardManager.AbcSmartcardHelper;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyAttributeReference;
import eu.abc4trust.util.MyPredicate;
import eu.abc4trust.util.PolicyTranslator;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.CommittedKey;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.RevocationInformation;


public class IdemixClaim implements Claim, VerifiableClaim{

    //	private static final long serialVersionUID = 1L;

    private final URI newCredIssuerParametersUID;
    private final String newCredSecretName;
    private final URI newCredSpecUID;
    private final Map<URI,MyAttributeReference> newCredAttrRefs;

    private transient Proof proof = null;

    private final List<PseudonymInToken> nymsInToken;

    private final String messageToSign;

    private final List<MyAttributeReference> inspectableAttributes;
    private final List<MyAttributeReference> committedAttributes;
    private final List<MyAttributeReference> oneOfAttributes;
    private final List<Representation> representations;
    private final Map<String, RepresentationOpening> representationOpenings;

    private final Map<MyAttributeReference, MyAttributeValue> allDisclosedAttributesAndValues;
    private final Map<MyAttributeReference, URI> inspectorKeyMap;
    private final Map<MyAttributeReference, BigInteger> inspectableAttributeValues;

    private final Map<String, List<MyAttributeReference>> credentialAttributeRefsList;
    private final BigInteger nonce;

    private final Set<Set<MyAttributeReference>> attrRefEqivClasses;
    private final Set<Set<MyAttributeReference>> revealedAttrRefEqivClasses;

    private final Map<MyPredicate, List<CredentialInToken>> predicateCredentialList;

    private final Map<String, CredentialSpecification> aliasCredSpecs;
    private final Map<String, CredentialInToken> aliasCredsInToken;

    private final Map<String, CommittedKey> aliasCommittedKeys;
    private final Map<String, List<CommittedAttribute>> aliasCommittedAttributes;
    private final List<CommittedValue> committedRevocationHandles;
    private final List<CommittedValue> committedInspectableValues;

    private final Map<String, URI> commitmentToIssuer;
    private final KeyManager keyManager;
    // private final Map<URI, RevocationInformation> revInfoUidToRevInfo;
    private final Collection<RevocationProofData> revocationProofData;
    private final ContextGenerator contextGen;
    private final VerifierRevocation verifierRevocation;

    public IdemixClaim(PolicyTranslator polTransl, KeyManager keyManager,
            ContextGenerator contextGen, VerifierRevocation verifierRevocation) {
        this.newCredAttrRefs = polTransl.getNewCredAttrRefs();
        this.newCredIssuerParametersUID = polTransl.getNewCredIssuerParametersUID();
        this.newCredSecretName = polTransl.getNewCredSecretName();
        this.newCredSpecUID = polTransl.getNewCredSpecUID();
        this.oneOfAttributes = polTransl.getOneOfAttrs();
        this.inspectorKeyMap = polTransl.getAttributeToInspectorKeyMap();
        this.inspectableAttributeValues = polTransl.getInspectableAttrValues();
        this.committedInspectableValues = polTransl.getCommittedInspectableValues();

        this.allDisclosedAttributesAndValues = polTransl.getAllDisclosedAttributesAndValues();

        this.inspectableAttributes = polTransl.getInspectableAttrs();

        this.messageToSign = polTransl.getMessageToSign();

        this.nymsInToken = polTransl.getPseudonyms();

        this.representations = new ArrayList<Representation>();
        this.representationOpenings = new HashMap<String, RepresentationOpening>();

        this.commitmentToIssuer = polTransl.getCommitmentToIssuerMap();

        this.predicateCredentialList = polTransl.getPredicateCredsInTokenMap();

        this.credentialAttributeRefsList = polTransl.getCredentialsAttributesList();

        this.aliasCredsInToken = polTransl.getCredentialList(); //this one cares

        this.aliasCredSpecs = polTransl.getCredSpecList();

        this.attrRefEqivClasses = polTransl.getAttrRefEqivClasses();
        this.revealedAttrRefEqivClasses = polTransl.getRevealedAttrRefEqivClasses();

        if (polTransl.getNonce()!=null){
            this.nonce = polTransl.getNonce();
        } else {
        	//TODO: specify truly random nonce or throw exception
            this.nonce = new BigInteger("123123123");
        }

        this.committedAttributes =  polTransl.getCommittedAttributes();
        this.committedRevocationHandles = polTransl.getCommittedRevocationHandles();

        if(polTransl.containsCommitments()){
            this.aliasCommittedKeys = polTransl.getAliasCommittedKeys();
            this.aliasCommittedAttributes = polTransl.getAliasCommittedAttributes();
        } else {
            this.aliasCommittedAttributes = null;
            this.aliasCommittedKeys = null;
        }

        this.keyManager = keyManager;

        this.revocationProofData = polTransl.getRevocationProofData();

        this.contextGen = contextGen;
        this.verifierRevocation = verifierRevocation;
    }

    @Override
    public List<PseudonymInToken> getPseudonyms() {
        return this.nymsInToken;
    }

    @Override
    public Map<MyPredicate, List<CredentialInToken>> getPredicateCredentialList() {
        return this.predicateCredentialList;
    }
    @Override
    public Map<MyAttributeReference, MyAttributeValue> getAllDisclosedAttributesAndValues(){
        return this.allDisclosedAttributesAndValues;
    }

    @Override
    public List<MyAttributeReference> getInspectableAttributes() {
        return this.inspectableAttributes;
    }


    @Override
    public String getEvidenceAsString() {
        // TODO If this method is used wrt. bridging/commitments
        // representations needs to be added to the evidence as in getEvidenceAsElement()

        return XMLSerializer.getInstance().serialize(this.proof);
    }

    @Override
    public Element getEvidenceAsElement() {
        Element elm = XMLSerializer.getInstance().serializeAsElement(this.proof);
        Element representations = elm.getOwnerDocument().createElement("Representations");
        Document doc = elm.getOwnerDocument();
        boolean representationsAdded = false;
        for(Representation ro : this.representations){
            Node ser = doc.importNode(XMLSerializer.getInstance().serializeAsElement(ro),true);
            representations.appendChild(ser);
            representationsAdded = true;
        }
        if(representationsAdded) {
            elm.appendChild(representations);
        }
        return elm;
    }

    /**
     * Sets the evidence of this Identity Mixer claim in the form of a Proof object.
     * @param proof The evidence of this claim.
     */
    @Override
    public void setEvidence(Proof proof) {
        this.proof = proof;
    }

    @Override
    public Proof getEvidence() {
        return this.proof;
    }

    public List<Representation> getRepresentations(){
        return this.representations;
    }

    public Map<String, RepresentationOpening> getRepresentationOpenings(){
        return this.representationOpenings;
    }

    @Override
    public boolean isValid() {
        if (this.proof==null) {
            return false;
        }

        //////////////////////////////////////////////////////////////
        // Validate cryptographic proof.
        //////////////////////////////////////////////////////////////
        try {
            Map<URI, RevocationInformation> revInfoUidToRevInfo = this.verifierRevocation
                    .getRevInfoUidToRevInfo();
            int inx = 0;
            // for (CredentialInToken credInToken : this.aliasCredsInToken
            // .values()) {
            for (CredentialInToken credInToken : this.verifierRevocation
                    .getCredentialInToken()) {
                URI credentialSpecificationUID = credInToken
                        .getCredentialSpecUID();
                CredentialSpecification credentialSpecification = this.keyManager
                        .getCredentialSpecification(credentialSpecificationUID);
                if (credentialSpecification.isRevocable()) {
                    String tempName = RevocationProof.ABC4TRUST_REVOCATION_TEMP_NAME
                            + inx;

                    URI revocationInformationUid = credInToken
                            .getRevocationInformationUID();
                    RevocationInformation revocationInformation = revInfoUidToRevInfo
                            .get(revocationInformationUid);

                    AccumulatorState state = RevocationUtility
                            .getState(revocationInformation);
                    int epoch = state.getEpoch();

                    URI revParamsUid = revocationInformation
                            .getRevocationAuthorityParameters();

                    URI credSpecUid = credentialSpecificationUID;
                    RevocationProofData revocationProofDatum = new RevocationProofData(
                            credSpecUid, revParamsUid, epoch, tempName, null);
                    this.revocationProofData.add(revocationProofDatum);
                    inx++;
                }
            }

            ProofSpec proofSpec = IdemixProofSpecGenerator
                    .generateProofSpecForPresentation(this,
                            IdemixConstants.groupParameterId, this.keyManager,
                            this.contextGen);
            VerifierInput verifierInput = new VerifierInput();

            verifierInput.smartcardHelper = new AbcSmartcardHelper();

            //messages to verify:
            if (this.messageToSign!=null) {
                MessageToSign msg = new MessageToSign(this.messageToSign);
                verifierInput.messages.put(IdemixConstants.messageName, msg);
            }

            // Verify that pseudonym value in proof is the same as in
            // presentation token description.
            boolean result = this.verifyPseudonymValues(proofSpec);
            if (!result) {
                return false;
            }

            for(Representation r: this.representations){
                verifierInput.representations.put(r.getName(), r);
            }


            // Verify revocation accumulators.
            this.verifierRevocation.addAccumulators(verifierInput);

            Verifier verifier = new Verifier(proofSpec, this.proof, this.nonce,
                    verifierInput);

            if (! verifier.verify()) {
                return false;
            }

            Map<String, CredentialInToken>  credentialList = this.getCredentialList();


            //////////////////////////////////////////////////////////////
            // Validate disclosed attributes.
            //////////////////////////////////////////////////////////////
            Map<MyAttributeReference, MyAttributeValue> allDisclosedAttributesAndValues2 = this.getAllDisclosedAttributesAndValues();
            for (MyAttributeReference ar : allDisclosedAttributesAndValues2.keySet()) {
                // Build the map's key: CREDSTRUCTNAME ; NAME_OF_CLPREDICATE ; ATTRIBUTENAME (where ; is the Constants.DELIMITER)
                StringBuilder key = new StringBuilder();
                key.append(credentialList.get(ar.getCredentialAlias()).getCredentialSpecUID());	// CredentialStructureID = credSpecUID
                key.append(com.ibm.zurich.idmx.utils.Constants.DELIMITER);
                key.append(ar.getCredentialAlias());								// the name of the credential reference was used as name of the CL Predicate
                key.append(com.ibm.zurich.idmx.utils.Constants.DELIMITER);
                key.append(ar.getAttributeType());

                HashMap<String, BigInteger> revealedValues = verifier.getRevealedValues();
                BigInteger encodedValueByProof = revealedValues.get(key.toString());
                MyAttributeValue myAttributeValue = allDisclosedAttributesAndValues2.get(ar);
                BigInteger encodedValueByClaim = myAttributeValue.getIntegerValueOrNull();

                if (! encodedValueByClaim.equals(encodedValueByProof)) {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    private boolean verifyPseudonymValues(ProofSpec proofSpec) {
        Iterator<Predicate> predicates = proofSpec.getPredicates()
                .iterator();
        Set<BigInteger> pseudonymValuesInProof = new HashSet<BigInteger>();
        while (predicates.hasNext()) {
            Predicate predicate = predicates.next();
            PredicateType predicateType = predicate.getPredicateType();
            String name = null;
            if (predicateType.equals(PredicateType.PSEUDONYM)) {
                PseudonymPredicate pred = (PseudonymPredicate) predicate;
                name = pred.getName();
            }
            if (predicateType.equals(PredicateType.DOMAINNYM)) {
                DomainNymPredicate pred = (DomainNymPredicate) predicate;
                name = pred.getTempName();
            }
            if (name != null) {
                BigInteger proofPseudonymValue = this.proof.getCommonValue(name);
                pseudonymValuesInProof.add(proofPseudonymValue);
            }
        }

        for (PseudonymInToken pit : this.nymsInToken) {
            byte[] pseudonymValue = pit.getPseudonymValue();
            BigInteger pseudonymValueAsInteger = new BigInteger(pseudonymValue);
            if (!pseudonymValuesInProof.contains(pseudonymValueAsInteger)) {
                return false;
            }
        }
        return true;
    }

    public Set<Set<MyAttributeReference>> getAttrRefEqivClasses(){
        return this.attrRefEqivClasses;
    }

    public Set<Set<MyAttributeReference>> getRevealedAttrRefEqivClasses(){
        return this.revealedAttrRefEqivClasses;
    }

    public Map<String, List<MyAttributeReference>> getCredAttributeRefs() {
        return this.credentialAttributeRefsList;
    }

    public Map<String, CredentialInToken> getCredentialList() {
        return this.aliasCredsInToken;
    }

    public Map<String, CredentialSpecification> getCredSpecList() {
        return this.aliasCredSpecs;
    }

    public String getMessage() {
        return this.messageToSign;
    }


    public BigInteger getNonce() {
        return this.nonce;
    }

    public Map<String, URI> getCommitmentToIssuerMap(){
        return this.commitmentToIssuer;
    }


    public Map<URI,MyAttributeReference> getNewCredentialAttrRefs() {
        return this.newCredAttrRefs;
    }

    public URI getNewCredSpecUID() {
        return this.newCredSpecUID;
    }

    public String getNewCredSecretName() {
        return this.newCredSecretName;
    }

    public URI getNewCredIssuerParametersUID() {
        return this.newCredIssuerParametersUID;
    }

    public Map<String, CommittedKey> getAliasCommittedKeys(){
        return this.aliasCommittedKeys;
    }

    public Map<String, List<CommittedAttribute>> getAliasCommittedAttributes(){
        return this.aliasCommittedAttributes;
    }

    public List<MyAttributeReference> getCommittedAttributes(){
        return this.committedAttributes;
    }

    public List<MyAttributeReference> getOneOfAttributes(){
        return this.oneOfAttributes;
    }

    public Collection<RevocationProofData> getRevocationProofData() {
        return this.revocationProofData;
    }

	public List<CommittedValue> getCommittedRevocationHandles() {
		return committedRevocationHandles;
	}

	public Map<MyAttributeReference, URI> getInspectorKeyMap() {
		return inspectorKeyMap;
	}
	
	public Map<MyAttributeReference, BigInteger> getInspectableAttributeValues() {
		return inspectableAttributeValues;
	}

	public List<CommittedValue> getCommittedInspectableValues() {
		return committedInspectableValues;
	}
}

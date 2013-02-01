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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibm.zurich.idmx.dm.CredentialCommitment;
import com.ibm.zurich.idmx.dm.MessageToSign;
import com.ibm.zurich.idmx.dm.RepresentationOpening;
import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.showproof.Proof;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.Prover;
import com.ibm.zurich.idmx.showproof.ProverInput;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Constants;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.ve.VerifiableEncryption;
import com.ibm.zurich.idmx.ve.VerifiableEncryptionOpening;

import eu.abc4trust.abce.internal.revocation.RevocationProof;
import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.idemix.util.Claim;
import eu.abc4trust.cryptoEngine.idemix.util.CommittedValue;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixClaim;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixProofSpecGenerator;
import eu.abc4trust.cryptoEngine.idemix.util.RevocationProofData;
import eu.abc4trust.cryptoEngine.idemix.util.VerifiableClaim;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyAttributeReference;
import eu.abc4trust.util.PolicyTranslator;
import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.util.XmlUtils;



/**
 * Generates Idemix Claim (extended mechanism spesific token description) containing the
 * presentation proof and an Idemix Prover for the advanced issuance
 */
public class IdemixClaimGenerator {

    AbcSmartcardManager smartcardManager;
    private final KeyManager keyManager;
    @SuppressWarnings("unused")
    private final CredentialManager credManager;
    private final RevocationProof revocationProof;
    private final ContextGenerator generator;

    public IdemixClaimGenerator(AbcSmartcardManager scManager,
            KeyManager keyManager, CredentialManager credManager,
            RevocationProof revocationProof, ContextGenerator generator) {
        this.smartcardManager = scManager;
        this.keyManager = keyManager;
        this.credManager = credManager;
        this.revocationProof = revocationProof;
        this.generator = generator;
    };

    public Element getPresentationEvidenceInternal(IdemixClaim idmxClaim,
            Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms) {

        VerifiableClaim verClaim = null;
        try {
            verClaim = this.generatePresentationEvidence(idmxClaim, aliasCreds,
                    aliasNyms);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return verClaim.getEvidenceAsElement();
    }

    public Element getPresentationEvidenceWithCommitment(PresentationTokenDescriptionWithCommitments ptd,
            Map<URI, Credential> aliasCreds,
            Map<String, CredentialSpecification> aliasCredSpecs,
            Map<URI, PseudonymWithMetadata> aliasNyms, KeyManager keyManager) {
        PolicyTranslator pt = new PolicyTranslator(ptd, aliasCredSpecs, keyManager);

        IdemixClaim idmxClaim = new IdemixClaim(pt, this.keyManager,
                this.generator, null);

        return this.getPresentationEvidenceInternal(idmxClaim, aliasCreds,
                aliasNyms);
    }

    public Element getPresentationEvidence(PresentationTokenDescription ptd,
            Map<URI, Credential> aliasCreds, Map<String, CredentialSpecification> aliasCredSpecs,
            Map<URI, PseudonymWithMetadata> aliasNyms) {
        PolicyTranslator pt = new PolicyTranslator(ptd, aliasCredSpecs);
        IdemixClaim idmxClaim = new IdemixClaim(pt, this.keyManager,
                this.generator, null);
        return this.getPresentationEvidenceInternal(idmxClaim, aliasCreds, aliasNyms);
    }

    @SuppressWarnings({ "rawtypes" })
    public VerifiableClaim generatePresentationEvidence(final Claim claim,
            final Map<URI, Credential> credAssignment,
            final Map<URI, PseudonymWithMetadata> nymAssignment)
                    throws Exception {
        IdemixClaim ic = (IdemixClaim) claim;

        ProverInput input = new ProverInput();
        input.smartcardManager = this.smartcardManager;
        // parse the Idemix Credentials for the assignment
        for (URI credVarName : credAssignment.keySet()) {
            try{
                com.ibm.zurich.idmx.dm.Credential c = null;
                Credential abceCred = credAssignment.get(credVarName);
                CryptoParams cp = abceCred.getCryptoParams();
                c =
                        (com.ibm.zurich.idmx.dm.Credential) Parser.getInstance().parse(
                                (Element) cp.getAny().get(0));
                // 	key format: credentialStructureURIasString;NameAsUsedInProofSpecForCLPredicate
                String key =
                        c.getCredStructId().toString().concat(Constants.DELIMITER).concat(credVarName.toString()); // Note
                // constructor
                input.credentials.put(key, c);
            }catch(ClassCastException e){
                System.out.println("IdemixClaimGenerator failed to insert credential "+credVarName+" into prover input. Most likely it's a UPROVE cred and should be ignored.");
            }
        }


        // parse the Idemix Simple&Domain Pseudonyms for the assignment
        for (URI nymVarName : nymAssignment.keySet()) {
            com.ibm.zurich.idmx.dm.StoredPseudonym p = null;
            com.ibm.zurich.idmx.dm.StoredDomainPseudonym dp = null;
            PseudonymWithMetadata abceNym = nymAssignment.get(nymVarName);

            CryptoParams cp = abceNym.getCryptoParams();

            if (abceNym.getPseudonym().isExclusive()) {
                dp =
                        (com.ibm.zurich.idmx.dm.StoredDomainPseudonym) Parser.getInstance().parse(
                                (Element) cp.getAny().get(0));
                // key format: NameAsUsedInProofSpecForDomNymPredicate
                String key = nymVarName.toString();
                input.domainPseudonyms.put(key, dp);
            } else {
                p =
                        (com.ibm.zurich.idmx.dm.StoredPseudonym) Parser.getInstance().parse(
                                (Element) cp.getAny().get(0));
                // 	key format: NameAsUsedInProofSpecForNymPredicate
                String key = nymVarName.toString();
                // Note that the value of nymVarName was used nym predicate
                input.pseudonyms.put(key, p);
            }
        }
        
        // Handle UProve revocation handles
        if(ic.getCommittedRevocationHandles() !=null){
        	for(CommittedValue crh: ic.getCommittedRevocationHandles()){
  
        		String name = crh.getAlias()+"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle";
        		RepresentationOpening ro = new RepresentationOpening(crh.getBases(), crh.getExponents(), crh.getModulus(),name);

        		ic.getRepresentationOpenings().put(name,ro);
        		ic.getRepresentations().add(ro.getRepresentationObject());
        		input.representationOpenings.put(name, ro);
        	}
        }
        
        // Handle UProve inspectable values
        if(ic.getCommittedInspectableValues() != null){
        	for(CommittedValue cv : ic.getCommittedInspectableValues()){
        		String name = cv.getAlias()+cv.getAttributeType();
        		RepresentationOpening ro = new RepresentationOpening(cv.getBases(), cv.getExponents(), cv.getModulus(),name);

        		ic.getRepresentationOpenings().put(name,ro);
        		ic.getRepresentations().add(ro.getRepresentationObject());
        		input.representationOpenings.put(name, ro);
        	}
        }
        
        // Handle inspection
        if(ic.getInspectableAttributes().size() > 0){
        	ObjectFactory of = new ObjectFactory();
        	for(MyAttributeReference mar: ic.getInspectableAttributes()){
            	CommittedAttribute comAttr = of.createCommittedAttribute();
        		URI ipkuid = ic.getInspectorKeyMap().get(mar);
        		InspectorPublicKey inspectorPublicKey = keyManager.getInspectorPublicKey(ipkuid);
        		
            	Element pkElement = (Element) inspectorPublicKey.getCryptoParams()
                        .getAny().get(0);
                VEPublicKey pk = (VEPublicKey) Parser.getInstance()
                        .parse(pkElement);
                BigInteger r1 = pk.getRandom();
                BigInteger message = null;
           
                Credential c = credAssignment.get(URI.create(mar.getCredentialAlias()));
                
                for(Attribute a: c.getCredentialDescription().getAttribute()){
                	if(a.getAttributeDescription().getType().equals(URI.create(mar.getAttributeType()))){
                		MyAttributeValue mav = MyAttributeEncodingFactory.parseValueFromEncoding(a.getAttributeDescription().getEncoding(), a.getAttributeValue(), new EnumAllowedValues(a.getAttributeDescription()));
                        message = mav.getIntegerValueOrNull();
                        comAttr.setAttributeType(a.getAttributeDescription().getType());
                		break;                		
                	}
                }
                
                URI vepkId = inspectorPublicKey.getPublicKeyUID();
                String label = c.getCredentialDescription().getCredentialSpecificationUID().toString()+"/"+mar.getAttributeType();
                VerifiableEncryptionOpening pEnc1 = new VerifiableEncryptionOpening(
                        message, r1, vepkId, label);
                VerifiableEncryption vEnc1 = pEnc1.getEncryption();
            	input.verifiableEncryptions.put(mar.getAttributeReference(), pEnc1);      
        	}
        }
        
        
        
        // Add opening information for commitments
        if(ic.getAliasCommittedAttributes() != null){
            for(String s: ic.getAliasCommittedAttributes().keySet()) {
                for(CommittedAttribute ca: ic.getAliasCommittedAttributes().get(s)){
                    // Find the bases in the (uprove)issuer parameters
                    IssuerParametersComposite ipc = null;
                    try{
                        ipc = (IssuerParametersComposite)StructureStore.getInstance().get(ic.getCommitmentToIssuerMap().get(s+ca.getAttributeType()));
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                    Vector<BigInteger> bases = new Vector<BigInteger>();
                    byte[] b = ipc.getG().getValue().getBase64Binary().get(0);
                    byte[] unsigned = new byte[b.length+1];
                    System.arraycopy(b, 0, unsigned, 1, b.length);
                    bases.add(new BigInteger(unsigned));
                    b = ipc.getG().getValue().getBase64Binary().get(1);
                    unsigned = new byte[b.length+1];
                    System.arraycopy(b, 0, unsigned, 1, b.length);
                    bases.add(new BigInteger(unsigned));

                    // 	Find the exponents ie. tildeO and the committedvalue
                    b = DatatypeConverter.parseBase64Binary(((Element)ca.getOpeningInformation().getAny().get(0)).getTextContent());
                    unsigned = new byte[b.length+1];
                    System.arraycopy(b, 0, unsigned, 1, b.length);
                    BigInteger tildeO =new BigInteger(unsigned);
                    b = (byte[])((JAXBElement)ca.getCommittedValue().getAny().get(0)).getValue();
                    unsigned = new byte[b.length+1];
                    System.arraycopy(b, 0, unsigned, 1, b.length);
                    BigInteger x =new BigInteger(unsigned);

                    Vector<BigInteger> exponents = new Vector<BigInteger>();
                    exponents.add(x);
                    exponents.add(tildeO);

                    BigInteger modulus = UProveUtils.getModulus(ipc.getGq().getValue());

                    RepresentationOpening ro = new RepresentationOpening(bases, exponents, modulus,s+ca.getAttributeType().toString());

                    ic.getRepresentationOpenings().put(s+ca.getAttributeType().toString(),ro);
                    ic.getRepresentations().add(ro.getRepresentationObject());
                    input.representationOpenings.put(s+ca.getAttributeType().toString(), ro);
                }
            }
        }

        // messages to sign:
        if (ic.getMessage() != null) {
            MessageToSign msg = new MessageToSign(ic.getMessage());
            input.messages.put(IdemixConstants.messageName, msg);
        }

        
        // Handle revocation.
        Collection<RevocationProofData> revocationProofData = new LinkedList<RevocationProofData>();
        // Map<URI, RevocationInformation> revInfoUidToRevInfo = new
        // HashMap<URI, RevocationInformation>();
        for (Credential cred : credAssignment.values()) {
            List<Object> any = cred.getCryptoParams().getAny();
            URI credSpecUid = cred.getCredentialDescription()
                    .getCredentialSpecificationUID();
            CredentialSpecification credentialSpecification = this.keyManager
                    .getCredentialSpecification(credSpecUid);

            boolean isIdemix = this.revocationProof.isIdemix(cred);
            if (isIdemix && credentialSpecification.isRevocable()) {
                
                URI revInfoUid = this.revocationProof
                        .getCredentialRevocationInformationUid(credSpecUid);

                NonRevocationEvidence nre = (NonRevocationEvidence) XmlUtils.unwrap(any.get(1), NonRevocationEvidence.class);
                URI revParamsUid = nre.getRevocationAuthorityParametersUID();

                RevocationInformation revInfo = this.keyManager
                        .getRevocationInformation(revParamsUid, revInfoUid);
                RevocationUtility.updateNonRevocationEvidence(nre, revInfo);

                int epoch = nre.getEpoch();

                AccumulatorWitness w1 = this.revocationProof
                        .extractWitness(nre);

                RevocationProofData revocationProofDatum = this.revocationProof
                        .revocationProofDatum(epoch, credSpecUid,
                                revParamsUid, w1);

                revocationProofData.add(revocationProofDatum);
                // revInfoUidToRevInfo.put(revInfoUid, revInfo);

                // this.credManager.deleteCredential(cred
                // .getCredentialDescription().getCredentialUID());
                // this.credManager.storeCredential(cred);

            }
        }

        // ic.getRevocationInformation().putAll(revInfoUidToRevInfo);
        ic.getRevocationProofData().addAll(revocationProofData);

        
        int inx = 0;
        for (RevocationProofData revocationProofDatum : ic
                .getRevocationProofData()) {
            String tempName = revocationProofDatum.getTempName(inx);
            AccumulatorWitness witness = revocationProofDatum.getWitness();
            input.accumulatorWitnesses.put(tempName, witness);
            inx++;
        }
        

        // Generate Idemix Proof Specification
        ProofSpec proofSpec =
                IdemixProofSpecGenerator.generateProofSpecForPresentation(ic,
                        IdemixConstants.groupParameterId, this.keyManager,
                        this.generator);


        // Generate Idemix Prover and build the proof
        Proof proof = new Prover(input, proofSpec, ic.getNonce()).buildProof();
        ic.setEvidence(proof);

        return ic;
    }

    public ProverInput generateProverInputForAdvancedIssuance(final Claim claim,
            final Map<URI, Credential> credAssignment,
            final Map<URI, PseudonymWithMetadata> nymAssignment, final Values idmxValues,
            final URI credStructureID, final URI issuerParamsID, final URI credNameOnSmartCard,
            final URI nameOfSmartCard) throws Exception {

        IdemixClaim ic = (IdemixClaim) claim;
        ProverInput input = new ProverInput();
        input.smartcardManager = this.smartcardManager;

        // parse the Idemix Credentials for the assignment
        for (URI credVarName : credAssignment.keySet()) {
            com.ibm.zurich.idmx.dm.Credential c = null;
            Credential abceCred = credAssignment.get(credVarName);

            CryptoParams cp = abceCred.getCryptoParams();
            c =
                    (com.ibm.zurich.idmx.dm.Credential) Parser.getInstance().parse(
                            (Element) cp.getAny().get(0));

            // key format: credentialStructureURIasString;NameAsUsedInProofSpecForCLPredicate
            String key = c.getFullTemporaryNameForProof(credVarName.toString()); // Note that the value of
            // credVarName was used
            // in the CLPredicate
            // constructor
            input.credentials.put(key, c);
        }

        // parse the Idemix Simple&Domain Pseudonyms for the assignment

        for (URI nymVarName : nymAssignment.keySet()) {
            com.ibm.zurich.idmx.dm.StoredPseudonym p = null;
            com.ibm.zurich.idmx.dm.StoredDomainPseudonym dp = null;
            PseudonymWithMetadata abceNym = nymAssignment.get(nymVarName);

            // TODO: either parse the Idemix-format pseudonyms from Crypto Params (same as for
            // credentials)
            // or create a method in IdemixUtils to convert PseudonymWithMethadata to
            // Stored(Domain)Pseudonym
            CryptoParams cp = abceNym.getCryptoParams();
            if (abceNym.getPseudonym().getScope() != null) {
                dp =
                        (com.ibm.zurich.idmx.dm.StoredDomainPseudonym) Parser.getInstance().parse(
                                (Element) cp.getAny().get(0));
                // dp =
                // (com.ibm.zurich.idmx.dm.StoredDomainPseudonym)Parser.getInstance().parse(getResource("DomNym_Visa.xml"));
                // key format: NameAsUsedInProofSpecForDomNymPredicate
                String key = nymVarName.toString();
                input.domainPseudonyms.put(key, dp);
            } else {
                p =
                        (com.ibm.zurich.idmx.dm.StoredPseudonym) Parser.getInstance().parse(
                                (Element) cp.getAny().get(0));
                // key format: NameAsUsedInProofSpecForNymPredicate
                String key = nymVarName.toString();
                input.pseudonyms.put(key, p);
            }
        }

        // messages to sign:
        if (ic.getMessage() != null) {
            MessageToSign msg = new MessageToSign(ic.getMessage());
            input.messages.put(IdemixConstants.messageName, msg);
        }

        CredentialCommitment credComm =
                new CredentialCommitment(issuerParamsID, credStructureID, idmxValues, this.smartcardManager,
                        credNameOnSmartCard, nameOfSmartCard);
        input.credentialCommitments.put(IdemixConstants.tempNameOfNewCredential, credComm);

        // Generate Idemix ProverInput to use as an input to the Reciepient

        return input;
    }

    @SuppressWarnings("unused")
    private InputSource getResource(String filename) {
        return new InputSource(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/idemix/" + filename));
    }


}

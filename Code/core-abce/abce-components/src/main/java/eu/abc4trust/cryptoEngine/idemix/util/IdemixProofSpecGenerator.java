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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBElement;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;

import com.ibm.zurich.idmx.dm.RepresentationOpening;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure.DataType;
import com.ibm.zurich.idmx.dm.structure.AttributeStructure.IssuanceMode;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.showproof.Identifier;
import com.ibm.zurich.idmx.showproof.Identifier.ProofMode;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.showproof.predicates.AccumulatorPredicate;
import com.ibm.zurich.idmx.showproof.predicates.CLComPredicate;
import com.ibm.zurich.idmx.showproof.predicates.CLPredicate;
import com.ibm.zurich.idmx.showproof.predicates.DomainNymPredicate;
import com.ibm.zurich.idmx.showproof.predicates.InequalityPredicate;
import com.ibm.zurich.idmx.showproof.predicates.InequalityPredicate.InequalityOperator;
import com.ibm.zurich.idmx.showproof.predicates.MessagePredicate;
import com.ibm.zurich.idmx.showproof.predicates.NotEqualPredicate;
import com.ibm.zurich.idmx.showproof.predicates.Predicate;
import com.ibm.zurich.idmx.showproof.predicates.Predicate.PredicateType;
import com.ibm.zurich.idmx.showproof.predicates.PrimeEncodePredicate;
import com.ibm.zurich.idmx.showproof.predicates.PrimeEncodePredicate.PrimeEncodeOp;
import com.ibm.zurich.idmx.showproof.predicates.PseudonymPredicate;
import com.ibm.zurich.idmx.showproof.predicates.RepresentationPredicate;
import com.ibm.zurich.idmx.showproof.predicates.VerEncPredicate;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.internal.revocation.RevocationConstants;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.Constants.OperationType;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyAttributeReference;
import eu.abc4trust.util.MyPredicate;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.PseudonymInToken;


/**
 * Generates an Idemix Proof Specification for a given advanced issuance or presentation claim.
 */
public class IdemixProofSpecGenerator {

    public static ProofSpec generateProofSpecForPresentation(
            final IdemixClaim claim, final String groupParametersUID,
            KeyManager keyManager, ContextGenerator generator)
                    throws Exception {
        return generateProofSpec(claim, groupParametersUID, false, null,
                keyManager, generator);
    }

    public static ProofSpec generateProofSpecForIssuance(
            final IdemixClaim claim, final String groupParametersUID,
            final CredentialStructure credStruct, KeyManager keyManager,
            ContextGenerator generator)
                    throws Exception {
        return generateProofSpec(claim, groupParametersUID, true, credStruct,
                keyManager, generator);
    }

    /**
     * Generates an Idemix Proof Specification for a given Idemix claim. <br/>
     * A validity test is performed on whether all implicitly revealed
     * attributes (via transitive equals-equality relations) are indeed part of
     * the claim's (explicitly) disclosed attributes. If this is not the case,
     * an exception is thrown.
     * 
     * @param claim
     *            The claim that is transformed to an Idemix Proof
     *            Specification. The claim's statement MUST be a conjunction of
     *            predicates (this is because Identity Mixer is currently not
     *            able to proof disjunctions).
     * @param revocationPredicateData
     * @param revAuthParamsUid
     * @param epoch
     * @return the Idemix Proof Specification for the given Idemix claim.
     * @throws URISyntaxException
     * @throws Exception
     *             in case a problem occurs during the generation.
     */
    ////////////////////////////////////////////////////////
    // Handle a) Proofs of ownership: create a CL-Predicate (proofs of ownership) for all credential references in
    //           the claim's credential declarations.
    //        b) Equality expressions in the claim's formula: the CL-Predicate constructor takes a mapping from attribute referenes
    //           to Idemix Identifiers -> let those attribute references that are in equals-equality relationship refer to
    //           the same Idemix Identifiers.
    //        c) Disclosure requests: mark the Idemix Identifiers of all attribute references that are disclosed as 'revealed'.
    // Requirements on the proof specification's Idemix Identifiers:
    //   a) In general, for each attribute that is contained (according to the credential type) in a credential whose ownership is proven (via a CLPredicate), has an own identifier in the Identifier section.
    //   b) Those attributes that are in an equals-equality relationship according to the formula, refer to the same identifier.
    // We need three mappings for the Identifiers:
    //   1) an identifierID-to-Identifier mapping: 					needed by the ProofSpec constructor (that is called once).
    //   2) an attributeName-to-Identifier mapping per credential:	needed by the CLPredicate constructor (that is called for each credential whose ownership is proved).
    //   3) an MyAttributeReference-to-Identifier mapping:			needed, e.g., to know which identifier to use in the InequalityPredicate constructors.
    ////////////////////////////////////////////////////////


    @SuppressWarnings("rawtypes")
    private static ProofSpec generateProofSpec(final IdemixClaim claim,
            final String groupParametersUID, final boolean issuanceMode,
            final CredentialStructure credStruct, KeyManager keyManager,
            ContextGenerator generator)
                    throws Exception {
        if (claim==null) {
            throw new Exception("Illegal argument. The given claim must not be null.");
        }

        ////////////////////////////////////////////////////////
        // Handle a) Proofs of ownership: create a CL-Predicate (proofs of ownership) for all credential references in
        //           the claim's credential declarations.
        //        b) Equality expressions in the claim's formula: the CL-Predicate constructor takes a mapping from attribute referenes
        //           to Idemix Identifiers -> let those attribute references that are in equals-equality relationship refer to
        //           the same Idemix Identifiers.
        //        c) Disclosure requests: mark the Idemix Identifiers of all attribute references that are disclosed as 'revealed'.
        // Requirements on the proof specification's Idemix Identifiers:
        //   a) In general, for each attribute that is contained (according to the credential type) in a credential whose ownership is proven (via a CLPredicate), has an own identifier in the Identifier section.
        //   b) Those attributes that are in an equals-equality relationship according to the formula, refer to the same identifier.
        // We need three mappings for the Identifiers:
        //   1) an identifierID-to-Identifier mapping: 					needed by the ProofSpec constructor (that is called once).
        //   2) an attributeName-to-Identifier mapping per credential:	needed by the CLPredicate constructor (that is called for each credential whose ownership is proved).
        //   3) an MyAttributeReference-to-Identifier mapping:			needed, e.g., to know which identifier to use in the InequalityPredicate constructors.
        ////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Create Idemix Identifiers for equality att references (first we get equality identifiers and then all the rest)
        // The key of the returned map is the attribute Reference
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Map<MyAttributeReference, Identifier> equalityIdentifiers = createEqualityIdemixIdentifiers(claim.getAttrRefEqivClasses(), claim.getRevealedAttrRefEqivClasses());
        int countId = equalityIdentifiers.size(); // starting the counter in this way makes sure there are no number clashes
        
        

        // Create Identifiers for Committed Attributes
        Map<MyAttributeReference, Identifier> committedIdentifiers = createCommittedIdemixIdentifiers(claim.getCommittedAttributes(), equalityIdentifiers, countId);
        countId += committedIdentifiers.size();

        Map<MyAttributeReference, Identifier> inspectableIdentifiers = createInspectableIdemixIdentifiers(claim.getInspectableAttributes(), equalityIdentifiers, committedIdentifiers, countId, claim);
        countId += inspectableIdentifiers.size();


        //Retrieve a list of OneOf attribute references to create special Identifiers:
        List<MyAttributeReference> oneOfAttrRefs = claim.getOneOfAttributes();

        ///////////////////////////////////////////
        //handle all the rest
        //////////////////////////////////////////
        HashMap<String, Identifier> identifierIDToIdentifier = new HashMap<String, Identifier>();
        Map<MyAttributeReference, Identifier> attrRefToIdentifier = new HashMap<MyAttributeReference, Identifier>();
        Map<MyAttributeReference, URI> attrRefToCredSpecUid = new HashMap<MyAttributeReference, URI>();
        
        //idemix predicates:
        List<Predicate> idemixPredicates = new Vector<Predicate>();

        ///////////////////////////////////
        //signing messages:
        ///////////////////////////////////
        if ((claim.getMessage()!=null)){
            idemixPredicates.add(new MessagePredicate(IdemixConstants.messageName, claim.getMessage()));
        }


        /////////////////////////////////////////////////////////////////////
        //handle all pseudonyms
        //retrieve nyms from the claim and generate the according predicates
        /////////////////////////////////////////////////////////////////////

        if (claim.getPseudonyms()!=null){
            int nymIndex = 0;
            for (PseudonymInToken nym: claim.getPseudonyms()){
                URI nymAlias = nym.getAlias();
                if (nymAlias == null) {
                    nymAlias = URI.create("abc4trust.eu/pseudonym/"+ nymIndex);
                }
                nymIndex ++;
                if (nym.isExclusive()) {
                    idemixPredicates.add(new DomainNymPredicate(nymAlias.toString(), URI.create(nym.getScope()), IdemixConstants.masterSecretName));
                } else {
                    idemixPredicates.add(new PseudonymPredicate(nymAlias.toString(), IdemixConstants.masterSecretName));
                }
            }
        }

        /////////////////////////////////////////////////////////////
        //get all credentials used from policy translator
        // assign idemix identifiers to credential attributes used to
        //create CL predicates
        /////////////////////////////////////////////////////////////


        Map<MyPredicate, List<CredentialInToken>> statement = claim.getPredicateCredentialList();

        Map<String, List<MyAttributeReference>> creds = claim.getCredAttributeRefs();

        Map<URI, Identifier> credSpecRevocationIdentifiers = new HashMap<URI, Identifier>();

        String crAlias = null;
        int credIndex = 0;
        for (CredentialInToken c: claim.getCredentialList().values()){
            Map<String, Identifier> attributeNameToIdentifier = new HashMap<String, Identifier>();
            String alias = c.getAlias().toString();
            for(MyAttributeReference currentAttRef: creds.get(alias)){
                Identifier i = null;
                if (equalityIdentifiers.containsKey(currentAttRef)) {
                    // Reuse existing Identifier (that was previously created for the equality expressions)
                    i = equalityIdentifiers.get(currentAttRef);

                    /////////////////////////////////////////
                    // Optional validity test whether there exist implicitly revealed attributes: check whether all revealed attributes
                    // (no matter if directly or indirectly (via equals-equality relationship) implicitly via the formula or explicitly
                    // via the disclosed attributes) are indeed part of the claim's explicitly disclosed attributes.
                    if (i.isRevealed() && !claim.getAllDisclosedAttributesAndValues().containsKey(currentAttRef)) {
                        throw new Exception("There are attributes that are implicitly revealed by the formula but that are not part of the claim's explicitly revealed attributes.");
                    }
                    /////////////////////////////////////////
                    // consider the case of EQUAL ONE OF attribute - special type of the identifier
                } else if ((oneOfAttrRefs != null)
                        && oneOfAttrRefs.contains(currentAttRef)) {
                    // Create new Identifier
                    i = new Identifier("id"+(++countId), DataType.ENUM, ProofMode.UNREVEALED);
                    i.setAttributeName(c.getIssuerParametersUID().toString(), c.getCredentialSpecUID().toString(), currentAttRef.getAttributeType());
	            } else if(committedIdentifiers.containsKey(currentAttRef)){
	                	i = committedIdentifiers.get(currentAttRef);
				} else if(inspectableIdentifiers.containsKey(currentAttRef)){
	                	i = inspectableIdentifiers.get(currentAttRef);
	                // If the attribute was disclosed 
            	} else if(claim.getAllDisclosedAttributesAndValues().containsKey(currentAttRef)){
                    // Create new Identifier
                    i = new Identifier("id"+(++countId), DataType.INT, ProofMode.REVEALED);
                } else{          	
                    // Create new Identifier
                    i = new Identifier("id"+(++countId), DataType.INT, ProofMode.UNREVEALED);
                }
                identifierIDToIdentifier.put(i.getName(), i);
                attributeNameToIdentifier.put(currentAttRef.getAttributeType(), i);

                attrRefToIdentifier.put(currentAttRef, i);
                if (crAlias == null) {
                    crAlias = currentAttRef.getCredentialAlias();
                }
                
                
                // Add inspectable idemix credspec uids to map 
                if(claim.getInspectableAttributes().contains(currentAttRef)){
                	attrRefToCredSpecUid.put(currentAttRef, c.getCredentialSpecUID());
                }

            }

            // ////////////////////////////////////////
            // Find revocation handle.
            // ////////////////////////////////////////
            URI credSpecUid = c.getCredentialSpecUID();
            CredentialSpecification credSpec = keyManager.getCredentialSpecification(credSpecUid);
            if (credSpec.isRevocable()) {
                Identifier revocationIdentifier = attributeNameToIdentifier
                        .get(RevocationConstants.REVOCATION_HANDLE_STR);

                if (revocationIdentifier == null) {
                    throw new CryptoEngineException(
                            "Revocation handle identifier is null");
                }
                credSpecRevocationIdentifiers.put(credSpecUid,
                        revocationIdentifier);
            }

            URI credentialAlias = c.getAlias();
            if (credentialAlias == null) {
                credentialAlias = URI.create("abc4trust.eu/credential/"+ credIndex);
            }
            credIndex ++;

			Predicate predicate =new CLPredicate(c.getIssuerParametersUID(),
                    c.getCredentialSpecUID(),
                    credentialAlias.toString(),
                    IdemixConstants.masterSecretName,
                    (HashMap<String, Identifier>) attributeNameToIdentifier);
            idemixPredicates.add(predicate);
        }
        Vector<Predicate> tempPreds = new Vector<Predicate>();
        tempPreds.addAll(idemixPredicates);
        ///////////////////////////////////
        // To enable token with commitments:
        ///////////////////////////////////
        //  parse commitment in the policy translator, pass it to the claim
        //  get commitment data from the claim and
        //	idemixPredicates.add(new RepresentationPredicate(...));
        if(claim.getAliasCommittedAttributes() != null){
            for(String s: claim.getAliasCommittedAttributes().keySet()){
                
                List<CommittedAttribute> attributes = claim.getAliasCommittedAttributes().get(s);
                for(CommittedAttribute ca: attributes){
                    // This will only work if the issuer parameters are uprove
                    IssuerParametersComposite ipc = null;
                    try{
                        ipc = (IssuerParametersComposite)StructureStore.getInstance().get(claim.getCommitmentToIssuerMap().get(s+ca.getAttributeType()));
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


                    Vector<Identifier> ids = new Vector<Identifier>();

                    boolean attributeUsedInEquality = false;
                    
                    Set<MyAttributeReference> identifiers = new HashSet<MyAttributeReference>();
                    identifiers.addAll(equalityIdentifiers.keySet());
                    identifiers.addAll(committedIdentifiers.keySet());
                    for(MyAttributeReference mar: identifiers){
                        Identifier i = equalityIdentifiers.get(mar);
                        if(i == null) {
                            i = committedIdentifiers.get(mar);
                        }
                        if(mar.getAttributeReference().equals(s+ca.getAttributeType().toString())){
                            ids.add(i);
                            // if we are working with 2 non idemix credentials, ie. comparing 2 committed attributes
                            // we need to make sure the identifiers are in the identifier list
                            if(!identifierIDToIdentifier.containsValue(i)){

                                if(ca.getCommittedValue().getAny().size() > 0){
                                    b = (byte[])((JAXBElement)ca.getCommittedValue().getAny().get(0)).getValue();
                                    i.setValue(new BigInteger(b));
                                }
                                identifierIDToIdentifier.put(i.getName(), i);
                            }
                            if(attrRefToIdentifier.get(mar) == null){
                            	attrRefToIdentifier.put(mar, i);
                            }
                            attributeUsedInEquality = true;
                        }
                    }
                    if(!attributeUsedInEquality){
                        for(MyAttributeReference mar: committedIdentifiers.keySet()){
                            Identifier i = committedIdentifiers.get(mar);
                            if(mar.getAttributeReference().equals((s+ca.getAttributeType().toString()))){
                                identifierIDToIdentifier.put(i.getName(), i);
                                attrRefToIdentifier.put(mar, i);
                            }
                        }
                    }
                    
                    Identifier id1 = new Identifier("id"+(++countId), DataType.INT, ProofMode.UNREVEALED);
                    
                    RepresentationOpening ro = claim.getRepresentationOpenings().get(s+ca.getAttributeType().toString());
                    if(ro != null){
                        id1.setValue(ro.getExponent(1));
                    }
                    ids.add(id1);
                    identifierIDToIdentifier.put(id1.getName(), id1);
                    
                    RepresentationPredicate rp = new RepresentationPredicate(s+ca.getAttributeType().toString(), ids, bases);
                    idemixPredicates.add(rp);
                }
            }
        }

        /////////////////////////////////////////////////////////////////////////////
        //if it is issuance - create a credentialCommitment predicate
        //to carry over attributes from other credentials or bind to the same secret
        /////////////////////////////////////////////////////////////////////////////
        if(issuanceMode){
            HashMap<String, Identifier> newCredIdentifierIdToIdentifier = new HashMap<String, Identifier>();
            Map<URI,MyAttributeReference> newCredAttributeReferences = claim.getNewCredentialAttrRefs();
            for(URI newCredentialAttributeType: newCredAttributeReferences.keySet()){
                Identifier i = null;
                MyAttributeReference newCredentialAttribute = newCredAttributeReferences.get(newCredentialAttributeType);
                AttributeStructure currentAttStruct = credStruct.getAttributeStructure(newCredentialAttributeType.toString());
                if (attrRefToIdentifier.containsKey(newCredentialAttribute)){
                    i = attrRefToIdentifier.get(newCredentialAttribute);
                    newCredIdentifierIdToIdentifier.put(currentAttStruct.getName(), i);
                } else if (currentAttStruct.getIssuanceMode()!=IssuanceMode.ISSUER){
                    i = new Identifier("id"+(++countId), DataType.INT, IdemixUtils.castIssuanceToProofMode(currentAttStruct.getIssuanceMode()));
                    newCredIdentifierIdToIdentifier.put(currentAttStruct.getName(), i);
                }
            }
            idemixPredicates.add(new CLComPredicate
                    (claim.getNewCredIssuerParametersUID(), claim.getNewCredSpecUID(), IdemixConstants.tempNameOfNewCredential, claim.getNewCredSecretName(), newCredIdentifierIdToIdentifier));
        }

        ///////////////////////////////////////////////////////////
        //handle all non-equality and other predicates
        //(equals-equality expressions are already taken care of)
        //////////////////////////////////////////////////////////

        for (MyPredicate predicate: statement.keySet()){
            List<CredentialInToken> cds = statement.get(predicate);
            URI pk = null;//URI.create("http://www.admin.edu/studentid/issuancekey_v1.0_dummy");
            
            if(predicate.getLeftRef() != null){
            	String s= 	predicate.getLeftRef().getAttributeReference();
            	
            	if(claim.getCommitmentToIssuerMap() != null && claim.getCommitmentToIssuerMap().get(s) != null){
            		pk = claim.getCommitmentToIssuerMap().get(s);
            		pk = URI.create(pk.toString()+"_dummy");
            	}
            }else if(predicate.getRightRef() != null){
            	String s= 	predicate.getRightRef().getAttributeReference();
            	if(claim.getCommitmentToIssuerMap() != null && claim.getCommitmentToIssuerMap().get(s) != null){
            		pk = claim.getCommitmentToIssuerMap().get(s);
            		pk = URI.create(pk.toString()+"_dummy");
            	}
            }
            handlePredicate(predicate, cds, attrRefToIdentifier, idemixPredicates, pk);

        }

        //////////////////////////////////////////////////
        // Handle revocation - Add accumulator predicates.
        //////////////////////////////////////////////////
        int revIdCounter = 0;
        for (RevocationProofData revProofDatum : claim.getRevocationProofData()) {
            Identifier identifier = credSpecRevocationIdentifiers
                    .get(revProofDatum.getCredSpecUid());
            if(identifier == null){
            	// Check if it is a committed handle
            	for(CommittedValue cv: claim.getCommittedRevocationHandles()){
            		if(cv.getCredSpecUID().equals(revProofDatum.getCredSpecUid())){
            			MyAttributeReference mar = new MyAttributeReference(URI.create(cv.getAlias()),URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
            			identifier = committedIdentifiers.get(mar);
            			if(identifier == null){
            				identifier = equalityIdentifiers.get(mar);	
            			}
            			if(identifier != null) {
                            continue;
                        }
            		}
            	}
            }
            
            
            if (identifier == null) {
                identifier = new Identifier("id" + (revIdCounter++),
                        DataType.INT, ProofMode.UNREVEALED);
                AccumulatorWitness witness = revProofDatum.getWitness();
                if (witness != null) {
                    identifier.setValue(witness.getValue());
                    // BigInteger r = generator.getRandomNumber(128);
                    // identifier.setRandom(r);
                }
                identifierIDToIdentifier.put(identifier.getName(), identifier);
                credSpecRevocationIdentifiers.put(revProofDatum.getCredSpecUid(), identifier); // This alows reuse in the case of committed handle
            }
            String tempName = revProofDatum.getTempName();
            URI keyUid = revProofDatum.getKeyUid();
            int epoc = revProofDatum.getEpoch();
            AccumulatorPredicate accumulatorPredicate = new AccumulatorPredicate(
                    tempName, keyUid, epoc, identifier);


            idemixPredicates.add(accumulatorPredicate);
        }
        
        ///////////////////////////////////
        // Handle Committed revocation handles:
        ///////////////////////////////////
        
        if((claim.getCommittedRevocationHandles() != null) && (claim.getCommittedRevocationHandles().size() != 0)){
        	for(CommittedValue cv : claim.getCommittedRevocationHandles()){
        		MyAttributeReference mar = new MyAttributeReference(URI.create(cv.getAlias()),URI.create(cv.getAttributeType()));
        		Identifier id = equalityIdentifiers.get(mar);
        		if(id == null){
        			id = committedIdentifiers.get(mar);
        		}
        		id.setValue(cv.getCommitmentValue());
        	}
        }
        /*
        		String name = cv.getAlias()+cv.getAttributeType();
        		name = cv.getAlias()+"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle";
        		Vector<Identifier> ids = new Vector<Identifier>();
        		// Since we already created accumulator predicates, we must already have an id for this handle
        		
                Identifier identifier = credSpecRevocationIdentifiers
                        .get(cv.getCredSpecUID());
                ids.add(identifier);
                
                Identifier id1 = new Identifier("id"+(++countId), DataType.INT, ProofMode.UNREVEALED);
                RepresentationOpening ro = claim.getRepresentationOpenings().get(name);
                if(ro != null){
                    id1.setValue(ro.getExponent(1));
                }
                ids.add(id1);
           //     identifierIDToIdentifier.put(id1.getName(), id1);
             System.out.println("in old times this would add a representation predicate for "+name);   
           //     RepresentationPredicate rp = new RepresentationPredicate(name, ids, cv.getBases());
           //     idemixPredicates.add(rp);
        	}
        }*/



        ///////////////////////////////////
        // Handle Committed inspectable values:
        ///////////////////////////////////
        if(claim.getCommittedInspectableValues() != null){
        	for(CommittedValue cv : claim.getCommittedInspectableValues()){
        		MyAttributeReference mar = new MyAttributeReference(URI.create(cv.getAlias()),URI.create(cv.getAttributeType()));
        		attrRefToCredSpecUid.put(mar,  cv.getCredSpecUID());
        	}
        }

        
        ///////////////////////////////////
        // Create Verifiable encryption predicates for all inspectable values:
        ///////////////////////////////////
        if(claim.getInspectableAttributes()!= null){
        	for(MyAttributeReference mar: claim.getInspectableAttributes()){
            	String label = attrRefToCredSpecUid.get(mar).toString()+"/"+mar.getAttributeType();
            	
   				Identifier identifier = equalityIdentifiers.get(mar);
   				if(identifier == null) {
                    identifier = committedIdentifiers.get(mar);
                }
   				if(identifier == null) {
                    identifier = inspectableIdentifiers.get(mar);
                } 

            	Predicate vePred = new VerEncPredicate(mar.getAttributeReference(), identifier, claim.getInspectorKeyMap().get(mar), label);
            	idemixPredicates.add(vePred);
        	}
        }
        
        
        ////////////////////////////////////////////////////////
        // Create and return proof specification
        ////////////////////////////////////////////////////////

        Vector<Predicate> idemixPredicatesVector = (Vector<Predicate>) idemixPredicates;
        ProofSpec proofSpec = new ProofSpec(identifierIDToIdentifier,
                idemixPredicatesVector, groupParametersUID);
         //System.out.println(" ============= ProofSpecGenerator ============= ");
         //System.out.println(proofSpec.toStringPretty());

        return proofSpec;
    }

	/**
     * Method creates Idemix Identifiers for the equality expressions
     * @param attrRefEqivClasses
     * @param revealedAttrRefEqivClasses
     * @return
     */
    private static Map<MyAttributeReference, Identifier> createEqualityIdemixIdentifiers(Set<Set<MyAttributeReference>> attrRefEqivClasses, Set<Set<MyAttributeReference>> revealedAttrRefEqivClasses){

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Create one Idemix Identifier per equivalence class and let all attribute references refer to this Identifier
        Map<MyAttributeReference, Identifier> identifiers = new HashMap<MyAttributeReference, Identifier>();
        int countId = 0;
        for (Set<MyAttributeReference> equivClass : attrRefEqivClasses) {

            ProofMode proofMode = revealedAttrRefEqivClasses.contains(equivClass) ? ProofMode.REVEALED : ProofMode.UNREVEALED;
            Identifier i = new Identifier("id"+(++countId), DataType.INT, proofMode);
            for (MyAttributeReference attrRef : equivClass) {
                identifiers.put(attrRef, i);
            }
        }
        return identifiers;
    }

    private static Map<MyAttributeReference, Identifier> createCommittedIdemixIdentifiers(List<MyAttributeReference> attrRef, Map<MyAttributeReference, Identifier> equalityIdentifiers, int idCount){
        Map<MyAttributeReference, Identifier> identifiers = new HashMap<MyAttributeReference, Identifier>();
        for(MyAttributeReference mar: attrRef){
            if(!equalityIdentifiers.containsKey(mar)){
                Identifier i = new Identifier("id"+(++idCount), DataType.INT, ProofMode.UNREVEALED);
                identifiers.put(mar,  i);
            }
        }
        return identifiers;
    }
    
    private static Map<MyAttributeReference, Identifier> createInspectableIdemixIdentifiers(List<MyAttributeReference> attrRef, Map<MyAttributeReference, Identifier> equalityIdentifiers, Map<MyAttributeReference, Identifier> committedIdentifiers, int idCount, IdemixClaim claim){
        Map<MyAttributeReference, Identifier> identifiers = new HashMap<MyAttributeReference, Identifier>();
        for(MyAttributeReference mar: attrRef){
            if(!equalityIdentifiers.containsKey(mar) && !committedIdentifiers.containsKey(mar)){
                Identifier i = new Identifier("id"+(++idCount), DataType.INT, ProofMode.UNREVEALED);
                identifiers.put(mar,  i);
            }
        }
        return identifiers;
    }
    
    /**
     * Handles the following predicates:
     * <ul>
     * <li>Constant expression: Nothing to prove.
     * <li>{@link EqualityExpression} (EQUALS already handled in {@link #createEqualityIdemixIdentifiers another method})
     * <li>{@link InequalityExpression}
     * <li>{@link MyAttributeReference} (already handled in {@link #createEqualityIdemixIdentifiers another method})
     * <li>{@link FunctionCall}: Not supported.
     * <li>{@link GroupedExpression}
     * </ul>
     * As names of the Idemix Inequality Predicates the string representations of the corresponding predicates are used.
     * 
     * @param myPredicate
     * @param attrRefToIdentifier A mapping from attribute references to Identity Mixer identifiers.
     *                            The mapping has to contain an entry for each attribute reference occurring in given predicate.
     *                            The identifiers are used to determine whether the corresponding attribute is revealed (no matter if
     *                            explicitly or implicitly).
     * @param predicates The list of Identity Mixer Predicates that is extended with a predicate corresponding to the given literal
     * @param publicKeyID URI of the IssuerPublicKey (Used to handle UProve credentials)
     * @throws Exception in case the given literal has a form that is not supported by this generator.
     */
    private static void handlePredicate(final MyPredicate myPredicate, final List<CredentialInToken> credInTokenInPredicate, final Map<MyAttributeReference, Identifier> attrRefToIdentifier, List<Predicate> predicates, final URI publicKeyID) throws Exception {

        OperationType function = myPredicate.getFunction();

        ////////////////////////////////////////////////////////////////////////////////////////////
        if (function.equals(OperationType.EQUAL)) {
            // Nothing to do here: equality expressions with the EQUAL operator were already handled earlier.
            // if one of operands is revealed - both are revealed,
            // if they are both not revealed - they just have the same idmx identifiers and CL predicate takes care of it
            ///////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////
        }else if (function.equals(OperationType.NOTEQUAL)){
            //////////////////////////////////////////////////////////////////////////////////////////////
            // PS(a != b) => only if a is disclosed
            //////////////////////////////////////////////////////////////////////////////////////////////
          
        	 MyAttributeReference l = myPredicate.getLeftRef();
             MyAttributeReference r = myPredicate.getRightRef();
             boolean lIsConstant = l.isConstant();
             boolean rIsConstant = r.isConstant();
             
             if (!lIsConstant && !rIsConstant) {

                 //////////////////////////////////////////////////////////////////////////////////////////////
                 // PS(a.b != c.d) => Create Not Equal Predicate. NOTE: Currently NOT supported by Idemix!
                 //////////////////////////////////////////////////////////////////////////////////////////////
            	 
            	 throw new Exception("Generation of an Idemix Proof Specification for expression '"+myPredicate.getPredicateAsString()+"' is currently not supported.");

                 // Once this is supported by Idemix one can use the commented code below:
            	 // Get the Idemix Identifiers corresponding to the left and right attributes.
                 /* Identifier leftIdentifier = attrRefToIdentifier.get(l);
                 Identifier rightIdentifier = attrRefToIdentifier.get(r);

                 // Create the predicate with all the gathered info.
                 predicates.add(new NotEqualPredicate(leftIdentifier, rightIdentifier)); */
                
             } else if (!lIsConstant && rIsConstant) {
                 //////////////////////////////////////////////////////////////////////////////////////////////
                 // PS(a.b != constExp) => Create Not Equal Predicate.
                 //////////////////////////////////////////////////////////////////////////////////////////////

                 // Determine the value of the constant expression.
                 //now use the same encoding as for the attribute from this predicate
                 MyAttributeValue mav = myPredicate.getRightVal();
                 BigInteger value = mav.getIntegerValueUnderEncoding(myPredicate.getEncoding());

                 String expValueAsString;

                 if (value!=null) {
                     expValueAsString = value.toString();
                 } else {
                     throw new Exception("Generation of an Idemix Proof Specification for expression '"+myPredicate.getPredicateAsString()+"' is currently not supported.");
                 }

                 // Get the Idemix Identifier corresponding to the attribute.
                 Identifier identifier = attrRefToIdentifier.get(l);

                 // Create the predicate with all the gathered info.
                 predicates.add(new NotEqualPredicate(identifier, value));

             } else if (lIsConstant && !rIsConstant) {
                 //////////////////////////////////////////////////////////////////////////////////////////////
                 // PS(constExp != a.b)  => PS(a.b != constExp)
                 //////////////////////////////////////////////////////////////////////////////////////////////
                 MyPredicate invertedPredicate = myPredicate.invertPredicate();

                 handlePredicate(invertedPredicate, credInTokenInPredicate, attrRefToIdentifier, predicates, publicKeyID);

             } else if (lIsConstant && rIsConstant) {
                 //handle a predicate with 2 constants
                 //evaluateFunction from MyAttrValFAct
                 if (!myPredicate.evaluateConstantExpression()){
                     throw new Exception("The predicate statement is not true");
                 }
             } else {
                 //////////////////////////////////////////////////////////////////////////////////////////////
                 // PS(constNonAttrExp != nonConstNonAttrExp) 	=> Not supported.
                 //////////////////////////////////////////////////////////////////////////////////////////////
                 throw new Exception("Generation of an Idemix Proof Specification for expression '"+myPredicate.getPredicateAsString()+"' is currently not supported.");
             }
        ////////////////////////////////////////////////////////////////////////////////////////////
        } else if (function.equals(OperationType.EQUALONEOF)) {
            Vector<String> theAttributes = new Vector<String>();
            MyAttributeReference refAttr = null;
            //TODO: null checks?
            int count = 0;
            String type = "";
            for (Map<MyAttributeReference,MyAttributeValue> argumentMap: myPredicate.getArguments()){
                Set<MyAttributeReference> refsSet = argumentMap.keySet();
                Iterator<MyAttributeReference> iter = refsSet.iterator();
                if (count == 0){
                    //get attr ref of the first argument in the predicate
                	refAttr = iter.next();
                    type = refAttr.getAttributeType();
                    count ++;
                } else{
                    MyAttributeReference ref = iter.next();
                    MyAttributeValue val = argumentMap.get(ref);
                    //TODO:check the encoding???
                    theAttributes.add(type+";"+val.toString());
                    //TODO: add if not in the right order
                }
            }
            // Get the Idemix Identifiers corresponding to the left attribute.
            Identifier theIdentifier = attrRefToIdentifier.get(refAttr);

            //create ONEOF predicate:
            predicates.add(new PrimeEncodePredicate(myPredicate.getPredicateAsString(), theIdentifier, theAttributes, PrimeEncodeOp.OR));

        } else if ((function.equals(OperationType.GREATER))||
                (function.equals(OperationType.LESS))||
                (function.equals(OperationType.GREATEREQ))||
                (function.equals(OperationType.LESSEQ))){
            ////////////////////////////////////////////////////////////////////////////////////////////
            MyAttributeReference l = myPredicate.getLeftRef();
            MyAttributeReference r = myPredicate.getRightRef();
            boolean lIsConstant = l.isConstant();
            boolean rIsConstant = r.isConstant();

            InequalityOperator idmxOperator;
            switch (function) {
            case GREATER:
                idmxOperator = InequalityOperator.GT; break;
            case GREATEREQ:
                idmxOperator = InequalityOperator.GEQ; break;
            case LESS:
                idmxOperator = InequalityOperator.LT; break;
            case LESSEQ:
                idmxOperator = InequalityOperator.LEQ; break;
            default:
                throw new Exception("Unknown operator: " + function.toString());
            }

            if (!lIsConstant && !rIsConstant) {

                //////////////////////////////////////////////////////////////////////////////////////////////
                // PS(a.b inequalityOperator c.d) => Create Inequality Predicate. NOTE: Currently only supported by Idemix if c.d is revealed!
                //////////////////////////////////////////////////////////////////////////////////////////////

                // Determine the Idemix Public Key identifier of the issuer of any of the involved credentials. Here we take the left one.
                // For Identity Mixer, the issuer name is at the same time the identifier of the public key.
            	URI publicKeyIdLeft = null;
            	if(credInTokenInPredicate.get(0) != null){
            		publicKeyIdLeft = credInTokenInPredicate.get(0).getIssuerParametersUID(); // Declarations in claims have exactly one issuer.
            	} else{
            		publicKeyIdLeft = publicKeyID;
            	}

                // Get the Idemix Identifiers corresponding to the left and right attributes.
                Identifier leftIdentifier = attrRefToIdentifier.get(l);
                Identifier rightIdentifier = attrRefToIdentifier.get(r);
                
                // Determine a unique string representation for this predicate.
                StringBuilder predicateAsString = new StringBuilder();
                predicateAsString.append(l.getAttributeReference());
                predicateAsString.append(function.toString());
                predicateAsString.append(r.getAttributeReference());

                // Create the predicate with all the gathered info.
                predicates.add(new InequalityPredicate(predicateAsString.toString(), publicKeyIdLeft, leftIdentifier, idmxOperator, rightIdentifier));

            } else if (!lIsConstant && rIsConstant) {
                //////////////////////////////////////////////////////////////////////////////////////////////
                // PS(a.b inequalityOperator constExp) where a.b is Integer or Date     	=> Create Inequality Predicate.
                // PS(a.b inequalityOperator constExp) where a.b is NOT Integer or Date 	=> Not supported.
                //////////////////////////////////////////////////////////////////////////////////////////////

                // Determine the value of the constant expression.
                //now use the same encoding as for the attribute from this predicate
                MyAttributeValue mav = myPredicate.getRightVal();
                BigInteger value = mav.getIntegerValueUnderEncoding(myPredicate.getEncoding());

                String expValueAsString;

                if (value!=null) {
                    expValueAsString = value.toString();
                } else {
                    throw new Exception("Generation of an Idemix Proof Specification for expression '"+myPredicate.getPredicateAsString()+"' is currently not supported.");
                }

                // Determine the Idemix Public Key identifier of the corresponding credential's issuer.
                URI publicKeyId = null;
                if(credInTokenInPredicate.get(0) != null) {
                	publicKeyId = credInTokenInPredicate.get(0).getIssuerParametersUID(); // Declarations in claims have exactly one issuer. // For Identity Mixer, the issuer name is at the same time the ID of the public key.
                } else {	
                	publicKeyId = publicKeyID;
                }

                // Get the Idemix Identifier corresponding to the attribute.
                Identifier identifier = attrRefToIdentifier.get(l);

                // Determine a unique string representation for this predicate.
                StringBuilder predicateAsString = new StringBuilder();
                predicateAsString.append(l.getAttributeReference());
                predicateAsString.append(function.toString());
                predicateAsString.append(expValueAsString);

                // Create the predicate with all the gathered info.
                predicates.add(new InequalityPredicate(predicateAsString.toString(), publicKeyId, identifier, idmxOperator, value));

            } else if (lIsConstant && !rIsConstant) {
                //////////////////////////////////////////////////////////////////////////////////////////////
                // PS(constExp < a.b)  => PS(a.b > constExp)
                // PS(constExp <= a.b) => PS(a.b >= constExp)
                // PS(constExp > a.b)  => PS(a.b < constExp)
                // PS(constExp >= a.b) => PS(a.b <= constExp)
                //////////////////////////////////////////////////////////////////////////////////////////////
                MyPredicate invertedPredicate = myPredicate.invertPredicate();

                handlePredicate(invertedPredicate, credInTokenInPredicate, attrRefToIdentifier, predicates, publicKeyID);

            } else if (lIsConstant && rIsConstant) {
                //handle a predicate with 2 constants
                //evaluateFunction from MyAttrValFAct
                if (!myPredicate.evaluateConstantExpression()){
                    throw new Exception("The predicate statement is not true");
                }
            } else {
                //////////////////////////////////////////////////////////////////////////////////////////////
                // PS(constNonAttrExp inequalityOperator nonConstNonAttrExp) 	=> Not supported.
                // PS(nonConstNonAttrExp inequalityOperator constNonAttrExp) 	=> Not supported.
                // PS(nonConstNonAttrExp inequalityOperator nonConstNonAttrExp) => Not supported.
                //////////////////////////////////////////////////////////////////////////////////////////////
                throw new Exception("Generation of an Idemix Proof Specification for expression '"+myPredicate.getPredicateAsString()+"' is currently not supported.");
            }
            ////////////////////////////////////////////////////////////////////////////////////////////
        } else {
            ////////////////////////////////////////////////////////////////////////////////////////////
            throw new Exception("The given form of predicate is currently not supported by this generator.");
        }
    }


}

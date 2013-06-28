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

package eu.abc4trust.cryptoEngine.bridging.user;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.user.UProveCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.CommittedAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PresentationTokenWithCommitments;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.VerifierDrivenRevocationInToken;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * CryptoEngineUser implementation that handles crypto orchestration and delegates
 * requests to the appropriate crypto engine (Idemix or UProve).
 * 
 * @author michael.stausholm@alexandra.dk
 *
 */

public class CryptoEngineDelegatorUser implements CryptoEngineUser {


    private final IdemixCryptoEngineUserImpl idemixEngine;
    private final UProveCryptoEngineUserImpl uproveEngine;
    private final KeyManager keyManager;
    private final CredentialManager credentialManager;
    private final Map<URI, CryptoEngineUser> contextMapping;

    private final Logger logger;

    @Inject
    public CryptoEngineDelegatorUser(IdemixCryptoEngineUserImpl idemix,
            UProveCryptoEngineUserImpl uprove, KeyManager keyManager,
            CredentialManager credManager, Logger logger) {
        this.idemixEngine = idemix;
        this.uproveEngine = uprove;
        this.keyManager = keyManager;
        this.credentialManager = credManager;
        this.contextMapping = new HashMap<URI, CryptoEngineUser>();
        this.logger = logger;
    }

    @Override
    public PresentationToken createPresentationToken(
            PresentationTokenDescription ptd, List<URI> creds,
            List<URI> pseudonyms) throws CryptoEngineException {

        System.out.println("Starting bridging");

        ObjectFactory of = new ObjectFactory();
        List<URI> uproveCreds = new ArrayList<URI>();
        List<URI> uprovePseudos = new ArrayList<URI>();

        Map<String, String> pseudonymEngineMap = this.mapPseudonyms(pseudonyms, ptd);
        try{

            for(URI c: creds){
                URI issuerUID = this.credentialManager.getCredential(c).getCredentialDescription().getIssuerParametersUID();
                IssuerParameters issuerParameters = this.keyManager.getIssuerParameters(issuerUID);
                URI algorithmID = issuerParameters.getAlgorithmID();
                URI uproveMechanism = CryptoUriUtil.getUproveMechanism();
                if(algorithmID.equals(uproveMechanism)){
                    uproveCreds.add(c);
                }
            }

            //	System.out.println("PTD input to Bridging: \n"+XmlUtils.toXml(of.createPresentationTokenDescription(ptd), false)+"\n\n");
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        PresentationTokenDescriptionWithCommitments ptdwc1 = of.createPresentationTokenDescriptionWithCommitments();
        ptdwc1.setCryptoEvidence(of.createCryptoParams());
        ptdwc1.setMessage(ptd.getMessage());
        ptdwc1.setPolicyUID(ptd.getPolicyUID());
        for(PseudonymInToken pit: ptd.getPseudonym()){
            if("UPROVE".equals(pseudonymEngineMap.get(pit.getScope()))){
                ptdwc1.getPseudonym().add(pit);
                uprovePseudos.add(pit.getAlias());
            }

        }
        for(VerifierDrivenRevocationInToken dvrit: ptd.getVerifierDrivenRevocation()){
            ptdwc1.getVerifierDrivenRevocation().add(dvrit);
        }
        ptdwc1.setTokenUID(ptd.getTokenUID());

        for(CredentialInToken cit: ptd.getCredential()){
            CredentialSpecification credSpec = null;
            try{
                credSpec = this.keyManager.getCredentialSpecification(cit.getCredentialSpecUID());
                if(! this.keyManager.getIssuerParameters(cit.getIssuerParametersUID()).getAlgorithmID().equals(new URI("urn:abc4trust:1.0:algorithm:uprove"))) {
                    continue;
                }
            }catch(Exception e){
                throw new RuntimeException(e);
            }

            //Add issuer parameters to Idemix Structure store
            try{
                this.storeUProveIssuerParameters(cit.getIssuerParametersUID());
            }catch(Exception e){
                System.out.println("Failed to store UProve issuer parameters in Idemix structure store");
                throw new RuntimeException(e);
            }

            CredentialInTokenWithCommitments citwc = of.createCredentialInTokenWithCommitments();
            URI alias = cit.getAlias();
            if(alias ==null){
                try {
                    alias = new URI(cit.toString());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            citwc.setAlias(alias);

            citwc.setCredentialSpecUID(cit.getCredentialSpecUID());
            citwc.setIssuerParametersUID(cit.getIssuerParametersUID());
            citwc.setRevocationInformationUID(cit.getRevocationInformationUID());
            citwc.setSameKeyBindingAs(cit.getSameKeyBindingAs());
            for(AttributePredicate ap: ptd.getAttributePredicate()){
                for(Object obj: ap.getAttributeOrConstantValue()){
                    if(obj instanceof eu.abc4trust.xml.AttributePredicate.Attribute) {
                        AttributePredicate.Attribute attribute = (AttributePredicate.Attribute)obj;
                        if(attribute.getCredentialAlias().equals(alias)) { // This attribute is to be added to citwc as a committedvalue
                            CommittedAttribute ca = of.createCommittedAttribute();
                            ca.setAttributeType(attribute.getAttributeType());
                            citwc.getCommittedAttribute().add(ca);
                        }
                    }
                }
            }
            for(AttributeInToken ait: cit.getDisclosedAttribute()){
                citwc.getDisclosedAttribute().add(ait);

                //test code to add commitments to inspectable attributes
                if(ait.getInspectorPublicKeyUID()!= null){
                    boolean alreadyOnList = false;
                    for(CommittedAttribute ca: citwc.getCommittedAttribute()){
                        if(ca.getAttributeType().equals(ait.getAttributeType())) {
                            alreadyOnList = true;
                        }
                    }
                    if(!alreadyOnList){
                        CommittedAttribute ca = of.createCommittedAttribute();
                        ca.setAttributeType(ait.getAttributeType());
                        citwc.getCommittedAttribute().add(ca);
                    }
                }
                //end test code
            }
            if(credSpec.isRevocable()){
                boolean alreadyOnList = false;
                for(CommittedAttribute ca: citwc.getCommittedAttribute()){
                    if(ca.getAttributeType().toString().equals("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle")) {
                        alreadyOnList = true;
                    }
                }
                if(!alreadyOnList){
                    CommittedAttribute ca = of.createCommittedAttribute();
                    ca.setAttributeType(URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
                    citwc.getCommittedAttribute().add(ca);
                }
            }
            ptdwc1.getCredential().add(citwc);
        }

        // A presentation token description for all Uprove credentials has
        // been constructed and we can now call the uprove engine for a token
        PresentationTokenWithCommitments ptwc1 = of.createPresentationTokenWithCommitments();
        ptwc1.setCryptoEvidence(of.createCryptoParams());
        if((uproveCreds.size()!=0) || (uprovePseudos.size() !=0)) {
            System.out.println("Userdelegator calling UProve engine for token");
            /*try{
        		xml = XmlUtils.toXml(of.createPresentationTokenDescriptionWithCommitments(ptdwc1), false);
        		System.out.println(xml+"\n\n");

        	}catch(Exception e){
        		e.printStackTrace();
        	} /* */

            try {
                ptwc1 = this.uproveEngine.createPresentationTokenWithCommitments(
                        ptdwc1, uproveCreds, pseudonyms);
            } catch (Exception ex) {
                throw new CryptoEngineException(ex);
            }


            try{
                System.out.println("PRINTING UPROVE PTWC (first part of token): ");
                //	Object[] tmp = new Object[2];
                //	int i = 0;
                //	for(Object o: ptwc1.getCryptoEvidence().getAny()){
                //		System.out.println("evidence: "+o.getClass().getName()+" "+o);
                //		if(!(o instanceof ElementImpl)) {
                //			tmp[i] = o;
                //		}
                //	}
                //	ptwc1.getCryptoEvidence().getAny().remove(2);
                //	ptwc1.getCryptoEvidence().getAny().remove(1);
                //xml = XmlUtils.toXml(of.createPresentationTokenWithCommitments(ptwc1), false);
                //System.out.println(xml+"\n\n");
                //			ptwc1.getCryptoEvidence().getAny().add(1, tmp[0]);
                //		ptwc1.getCryptoEvidence().getAny().add(2, tmp[1]);
            }catch (Exception e){  System.err.println("EXCEPTION :"); e.printStackTrace();	} /* */
        }

        PresentationTokenDescriptionWithCommitments ptdwc2 = of.createPresentationTokenDescriptionWithCommitments();
        ptdwc2.setMessage(ptd.getMessage());
        ptdwc2.setPolicyUID(ptd.getPolicyUID());
        ptdwc2.getAttributePredicate().addAll(ptd.getAttributePredicate());

        for(PseudonymInToken pit: ptd.getPseudonym()){
            if("IDEMIX".equals(pseudonymEngineMap.get(pit.getScope()))){
                ptdwc2.getPseudonym().add(pit);
            }
        }
        ptdwc2.setTokenUID(ptd.getTokenUID());
        for(VerifierDrivenRevocationInToken dvrit: ptd.getVerifierDrivenRevocation()){
            ptdwc2.getVerifierDrivenRevocation().add(dvrit);
        }

        // First add the credentials from the original presentationtokendescription:
        for(CredentialInToken cit: ptd.getCredential()){
            try{
                if(! this.keyManager.getIssuerParameters(cit.getIssuerParametersUID()).getAlgorithmID().equals(new URI("urn:abc4trust:1.0:algorithm:idemix"))) {
                    continue;
                }
            }catch(Exception e){
                throw new RuntimeException(e);
            }
            CredentialInTokenWithCommitments citwc = of.createCredentialInTokenWithCommitments();
            citwc.setAlias(cit.getAlias());
            for(AttributeInToken ait: cit.getDisclosedAttribute()){
                citwc.getDisclosedAttribute().add(ait);
            }
            citwc.setCredentialSpecUID(cit.getCredentialSpecUID());
            citwc.setIssuerParametersUID(cit.getIssuerParametersUID());
            citwc.setRevocationInformationUID(cit.getRevocationInformationUID());
            citwc.setSameKeyBindingAs(cit.getSameKeyBindingAs());
            ptdwc2.getCredential().add(citwc);
        }

        // Then add the credentials from the uprove presentationtoken:
        if(uproveCreds.size()!=0){
            for(CredentialInTokenWithCommitments cit: ptwc1.getPresentationTokenDescriptionWithCommitments().getCredential()){
                CredentialInTokenWithCommitments citwc = of.createCredentialInTokenWithCommitments();
                citwc.setAlias(cit.getAlias());
                citwc.setSameKeyBindingAs(cit.getSameKeyBindingAs());
                citwc.setIssuerParametersUID(cit.getIssuerParametersUID());
                citwc.setRevocationInformationUID(cit.getRevocationInformationUID());
                citwc.setCredentialSpecUID(cit.getCredentialSpecUID());
                // 	We don't copy the disclosed attributes! - if it is an inspectable attribute, we do
                for(AttributeInToken a: cit.getDisclosedAttribute()){
                    if(a.getInspectorPublicKeyUID()!= null) {
                        citwc.getDisclosedAttribute().add(a);
                    }
                }
                for(CommittedAttribute ca: cit.getCommittedAttribute()){
                    citwc.getCommittedAttribute().add(ca);
                }
                ptdwc2.getCredential().add(citwc);
            }
        }
        /* try{
    		System.out.println("printing idemix input!\n");
			xml = XmlUtils.toXml(of.createPresentationTokenDescriptionWithCommitments(ptdwc2), false);
			System.out.println(xml+"\n\n");
		}catch (Exception e){ System.out.println("Exception happend");e.printStackTrace();}/* */



        // We now have constructed a PTD usable by the Idemix engine
        System.out.println("Userdelegator calling Idemix engine for token");

        // Revocation. Remove crypto params.
        List<Object> any = ptwc1.getCryptoEvidence().getAny();

        ptdwc2.setCryptoEvidence(of.createCryptoParams());
        ptdwc2.getCryptoEvidence().getAny().addAll(any);

        // We can now feed ptdwc2 to the idemix engine
        PresentationTokenWithCommitments ptwc2 = this.idemixEngine.createPresentationTokenWithCommitments(ptdwc2, creds, pseudonyms);


        /*try{
    		System.out.println("PRINTING idemix PTWC2: ");

    		xml = XmlUtils.toXml(of.createPresentationTokenWithCommitments(ptwc2), false);
    		System.out.println(xml+"\n\n");

    	}catch (Exception e){  System.out.println("more bad happened :<"); e.printStackTrace();	} */

        System.out.println("Userdelegator combining presentation tokens");

        PresentationToken pt = of.createPresentationToken();
        // TODO What should the version of the combined token be?  // We can either use the version of the Idemix or UProve token
        // HGK : For now - set to 1.0 - hardcoded every where else...
        pt.setVersion("1.0");


        // Create the presentationtokendescription
        pt.setPresentationTokenDescription(ptd);

        // Combine and set crypto evidence
        CryptoParams cp = of.createCryptoParams();
        for(Object o: any){
            if(o instanceof Element) {
                cp.getAny().add(o);
            } else {
                System.err.println("Cannot add non serializable object to CryptoParams : " + o.getClass());
            }
        }
        if((ptwc2.getCryptoEvidence().getAny()!= null) && (ptwc2.getCryptoEvidence().getAny().size()> 0)){
            for(Object o: ptwc2.getCryptoEvidence().getAny()){

                if(o instanceof Element) {
                    cp.getAny().add(o);
                } else {
                    System.err.println("Cannot add non serializable object to CryptoParams : " + o.getClass());
                }
            }
        }
        // TODO Create a tag containing the credentials.
        for(CredentialInTokenWithCommitments c: ptwc2.getPresentationTokenDescriptionWithCommitments().getCredential()){
            if(c.getCommittedAttribute().size() > 0){
                for(CommittedAttribute ca: c.getCommittedAttribute()){
                    // Remove the committedvalue before sending the token
                    // otherwise we will be revealing the value!
                    ca.getCommittedValue().getAny().clear();
                    ca.getOpeningInformation().getAny().clear();
                }
                try {
                    String cred = XmlUtils.toXml(of.createCredentialInTokenWithCommitments(c));
                    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                    InputStream credIn = new ByteArrayInputStream(cred.getBytes("UTF-8"));
                    Document doc = docBuilder.parse(credIn);
                    Node newNode = (doc.getFirstChild()).cloneNode(true);
                    cp.getAny().add(newNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        pt.setCryptoEvidence(cp);
        List<Object> toBeRemoved = new ArrayList<Object>();
        //TODO STRIP RevocationInformation from the final presentation token!
        for(Object o: pt.getCryptoEvidence().getAny()){
            try{
                Element elm = (Element)o;
                if(elm.getNodeName().equals("RevocationInformation") || elm.getNodeName().equals("InspectableInformation")){
                    toBeRemoved.add(elm);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        for(Object o:toBeRemoved){
            pt.getCryptoEvidence().getAny().remove(o);
        }

        /*try{
    		System.out.println("\n\nprinting final (combined)token: ");
    		xml = XmlUtils.toXml(of.createPresentationToken(pt), false);
    		System.out.println(xml+"\n\n");

    	}catch (Exception e){  e.printStackTrace();	}/* */

        System.out.println("Userdelegator done combining tokens");
        return pt;
    }

    @Override
    public IssuanceToken createIssuanceToken(IssuanceTokenDescription itd, List<URI> creduids,
            List<Attribute> atts, List<URI> pseudonyms, URI ctxt) {
        try{
            URI issuerParametersUID = itd.getCredentialTemplate()
                    .getIssuerParametersUID();
            IssuerParameters issuerParameters = this.keyManager
                    .getIssuerParameters(issuerParametersUID);
            if (issuerParameters == null) {
                throw new CryptoEngineException(
                        "Could not find issuer parameters with UID: \""
                                + issuerParametersUID + "\"");
            }
            if(issuerParameters.getAlgorithmID().equals(new URI("urn:abc4trust:1.0:algorithm:uprove"))){
                return this.uproveEngine.createIssuanceToken(itd, creduids, atts, pseudonyms, ctxt);
            } else {
                return this.idemixEngine.createIssuanceToken(itd, creduids, atts, pseudonyms, ctxt);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {

        // Maintain a mapping of contexts to the appropriate cryptoengine.
        if(this.contextMapping.containsKey(m.getContext())){
            return this.contextMapping.get(m.getContext()).issuanceProtocolStep(m);

        }else{
            for(Object obj: m.getAny()){
                // uprove's Jaxb firstIssuanceMessageComposite is marshalled to w3dom Element
                if(obj instanceof Element) {
                    Element element = (Element)obj;
                    String elementName = element.getLocalName() != null ? element.getLocalName() : element.getNodeName();

                    if(elementName.equalsIgnoreCase("firstIssuanceMessageComposite")) {
                        this.contextMapping.put(m.getContext(), this.uproveEngine);
                        return this.uproveEngine.issuanceProtocolStep(m);
                    }
                }
                if( obj instanceof JAXBElement){
                    JAXBElement jobj = (JAXBElement)obj;
                    Object child = jobj.getValue();
                    if((child instanceof CredentialDescription) || (child instanceof CredentialTemplate)){
                        URI algorithm = null;
                        URI uproveuri = null;
                        try{
                            uproveuri = new URI("urn:abc4trust:1.0:algorithm:uprove");
                            if(child instanceof CredentialDescription){
                                CredentialDescription cd = (CredentialDescription)child;
                                algorithm = this.keyManager.getIssuerParameters(cd.getIssuerParametersUID()).getAlgorithmID();
                            } else {
                                CredentialTemplate ct = (CredentialTemplate)child;
                                algorithm = this.keyManager.getIssuerParameters(ct.getIssuerParametersUID()).getAlgorithmID();
                            }
                        } catch (NullPointerException ex) {
                            throw new CryptoEngineException(
                                    "Could not find issuer in keymanager" + ex);
                        } catch (Exception ex) {
                            throw new CryptoEngineException(ex);
                        }

                        if(algorithm.equals(uproveuri)){
                            this.contextMapping.put(m.getContext(), this.uproveEngine);
                            return this.uproveEngine.issuanceProtocolStep(m);
                        } else{
                            this.contextMapping.put(m.getContext(), this.idemixEngine);
                            return this.idemixEngine.issuanceProtocolStep(m);
                        }
                    }
                }
            }
        }
        throw new CryptoEngineException(
                "Could not map IssuanceMessage to an appropriate cryptoengine");
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {
        URI issParams = cred.getCredentialDescription().getIssuerParametersUID();
        URI algorithm = null;
        try{
            algorithm = this.keyManager.getIssuerParameters(issParams).getAlgorithmID();
        }catch(KeyManagerException e){
            throw new RuntimeException(e);
        }
        if(algorithm.equals(CryptoUriUtil.getIdemixMechanism())) {
            return this.idemixEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts);
        }
        return this.uproveEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts);
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException, CredentialWasRevokedException {
        URI issParams = cred.getCredentialDescription().getIssuerParametersUID();
        URI algorithm = null;
        try{
            algorithm = this.keyManager.getIssuerParameters(issParams).getAlgorithmID();
        }catch(KeyManagerException e){
            throw new RuntimeException(e);
        }
        if(algorithm.equals(CryptoUriUtil.getIdemixMechanism())) {
            return this.idemixEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts, revinfouid);
        }
        return this.uproveEngine.updateNonRevocationEvidence(cred, raparsuid, revokedatts, revinfouid);
    }

    @Override
    public PseudonymWithMetadata createPseudonym(URI pseudonymUri, String scope, boolean exclusive,
            URI secretReference) {
        //    	System.out.println("\nCreate Pseudonym with parameters:");
        //    	System.out.println("PseudonymURI: "+pseudonymUri);
        //    	System.out.println("scope: "+scope);
        //    	System.out.println("exclusive: "+exclusive);
        //    	System.out.println("secretReference: "+secretReference);
        //    	if(secretReference.toString().contains("Idemix")){
        //    		return this.idemixEngine.createPseudonym(pseudonymUri, scope, exclusive, secretReference);
        //    	}else{
        //    		return this.uproveEngine.createPseudonym(pseudonymUri, scope, exclusive, secretReference);
        //    	}
        try{
            return this.idemixEngine.createPseudonym(pseudonymUri, scope, exclusive, secretReference);
        }catch(Exception e){
            //	e.printStackTrace();
        }
        return this.uproveEngine.createPseudonym(pseudonymUri, scope, exclusive, secretReference);
    }

    /**
     * TODO Apparently this only generates some randomness + a random UID. Nothing is stored locally.
     * For now we just return the secret we get from the idemix engine, but check with
     * Maria and Christian if that is the proper thing to do
     * @return
     */
    @Override
    public Secret createSecret() {
        try{
            return this.idemixEngine.createSecret();
        } catch (Exception e) {
            this.logger.info(e.getMessage());
            return this.uproveEngine.createSecret();
        }
    }

    private Map<String, String> mapPseudonyms(List<URI> pseudonyms, PresentationTokenDescription ptd){
        Map<String, String> ret = new HashMap<String, String>();
        System.out.println("mapping pseudonyms to engine");
        for(URI p: pseudonyms){
            System.out.println("pseudonym: "+p);
            try {
                PseudonymWithMetadata pseudo = this.credentialManager.getPseudonym(p);
                System.out.println("is: "+pseudo);
                try{
                    @SuppressWarnings("unused")
                    com.ibm.zurich.idmx.dm.StoredPseudonym dp = (com.ibm.zurich.idmx.dm.StoredPseudonym) Parser.getInstance().parse(
                            (Element) pseudo.getCryptoParams().getAny().get(0));
                    System.out.println("could be cast to idemix");
                    ret.put(pseudo.getPseudonym().getScope(), "IDEMIX");
                    continue;
                }catch(Exception e){}
                try{
                    @SuppressWarnings("unused")
                    com.ibm.zurich.idmx.dm.StoredDomainPseudonym dp = (com.ibm.zurich.idmx.dm.StoredDomainPseudonym) Parser.getInstance().parse(
                            (Element) pseudo.getCryptoParams().getAny().get(0));
                    System.out.println("could be cast to idemix2");
                    ret.put(pseudo.getPseudonym().getScope(), "IDEMIX");
                    continue;
                }catch(Exception e){}
                try{
                    if(((Element)pseudo.getCryptoParams().getAny().get(0)).getNodeName().equals("A")){
                        System.out.println("could be cast to uprove");
                        ret.put(pseudo.getPseudonym().getScope(), "UPROVE");
                        continue;
                    }
                }catch(Exception e){}
                System.out.println("could not be cast :<");
                ret.put(pseudo.getPseudonym().getScope(), null);

            } catch (CredentialManagerException e) {
                throw new RuntimeException(e);
            }
        }

        return ret;
    }


    private void storeUProveIssuerParameters(URI issuerParamsUID) throws Exception{
        IssuerParametersComposite ipc = null;
        IssuerParameters params = this.keyManager.getIssuerParameters(issuerParamsUID);
        UProveUtils utils = new UProveUtils();
        ipc = utils.convertIssuerParameters(params);
        StructureStore.getInstance().add(issuerParamsUID.toString(), ipc);
    }

    @Override
    public boolean isRevoked(Credential cred) throws CryptoEngineException {
        return this.idemixEngine.isRevoked(cred);
    }

}
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

package eu.abc4trust.cryptoEngine.idemix.verifier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.dm.Representation;
import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
import com.ibm.zurich.idmx.key.IssuerPublicKey;
import com.ibm.zurich.idmx.showproof.Proof;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.internal.revocation.VerifierRevocation;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixClaim;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixUtils;
import eu.abc4trust.cryptoEngine.idemix.util.VerifiableClaim;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.util.PolicyTranslator;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PresentationTokenWithCommitments;
import eu.abc4trust.xml.SystemParameters;


/**
 * CryptoEngineVerifier implementation that uses Idemix library for credential handling.
 * 
 * @author mdu
 *
 */

public class IdemixCryptoEngineVerifierImpl implements CryptoEngineVerifier {
	
	private boolean DEBUG = false;

    private final KeyManager keyManager;
    private final Logger logger;
    private final ContextGenerator contextGen;
    private final VerifierRevocation verifierRevocation;

    @Inject
    public IdemixCryptoEngineVerifierImpl(KeyManager keyManager, Logger logger,
            ContextGenerator contextGen, VerifierRevocation verifierRevocation) {
        this.keyManager = keyManager;
        this.logger = logger;
        this.contextGen = contextGen;
        this.verifierRevocation = verifierRevocation;
    }

    /////////////////////////////////////////////////////////////////////
    ///override methods
    /////////////////////////////////////////////////////////////////////



    // @Override
    public boolean verifyTokenWithCommitments(PresentationTokenWithCommitments t)
            throws TokenVerificationException, CryptoEngineException {
        // Since we need to get the committed value into the claim somehow, we have to use this duplicated method
        //unwrap presentation token and crypto params

        VerifiableClaim verIdmxClaim = null;

        try{
            //eu.abc4trust.xml.ObjectFactory objF = new eu.abc4trust.xml.ObjectFactory();
            //System.out.println("idemix verifier engine - prestoken!:\n"+eu.abc4trust.xml.util.XmlUtils.toXml(objF.createPresentationTokenWithCommitments(t), false));
            CryptoParams cryptoEvidence = t.getCryptoEvidence();

            List<Representation> representations= new ArrayList<Representation>();

            // parse idemix proof from the token
            // We first have to remove any Representations
            Element proofElement = null;
            Element revocationInformation = null;
            List<Element> inspectionElements = new ArrayList<Element>();
            for(Object temp : cryptoEvidence.getAny()){
                Element element = (Element) temp;
                String elementName = element.getLocalName() != null ? element.getLocalName() : element.getNodeName();
            	if(elementName.equals("IdmxProof")) {
            		proofElement = element;
            	}
            	if(elementName.equals("RevocationInformation")) {
            		revocationInformation = element;
            	}
                if(elementName.equals("InspectableInformation")) {
                	inspectionElements.add(element);
                }
            	
            }
            NodeList children = proofElement.getChildNodes();
            for(int i =0 ; i< children.getLength(); i++){
                if(children.item(i) instanceof Element) {
                    Element child = (Element)children.item(i);
                    if(child.getTagName().equals("Representations")){
                        NodeList representationElements = child.getChildNodes();
                        for(int j = 0; j<representationElements.getLength(); j++){
                        	try{
                        		Representation rep = (Representation) Parser.getInstance().parse((Element)representationElements.item(j));
                        		representations.add(rep);
                        	}catch(ClassCastException e){
                        		//Do nothing as the element is most likely a TextElement which is empty due to some malformed xml containing \r or \n.
                        	}
                        }
                        proofElement.removeChild(children.item(i));
                        break;
                    }
                } else if(children.item(i) instanceof org.w3c.dom.Text) {
                    // Text Node ?? skip
                    continue;
                } else {
                    // TODO WARN...
                    System.err.println("WARNING : Unknow XML Node " + children.item(i).getClass());
                }

            }
//            Proof	proof = (Proof) Parser.getInstance().parse((Element) cryptoEvidence.getAny().get(0));
            Proof	proof = (Proof) Parser.getInstance().parse(proofElement);


            List<URI> issuerParamsURIList = new ArrayList<URI>();
            List<URI> credSpecsURIList = new ArrayList<URI>();
            for (CredentialInTokenWithCommitments credInToken: t.getPresentationTokenDescriptionWithCommitments().getCredential()){
                if(this.keyManager.getIssuerParameters(credInToken.getIssuerParametersUID()).getAlgorithmID().equals(CryptoUriUtil.getIdemixMechanism())){
                    credSpecsURIList.add(credInToken.getCredentialSpecUID());
                    if (!issuerParamsURIList.contains(credInToken.getIssuerParametersUID())){
                        issuerParamsURIList.add(credInToken.getIssuerParametersUID());
                    }
                }
            }

            this.loadIdemixSystemAndIssuerParameters(issuerParamsURIList);
            this.loadIdemixCredentialStructures(credSpecsURIList);

            //create Idemix Claim from the token
            PresentationTokenDescriptionWithCommitments ptd = t.getPresentationTokenDescriptionWithCommitments();
            ObjectFactory of = new ObjectFactory();
            CryptoParams cp = of.createCryptoParams();
            ptd.setCryptoEvidence(cp);
            ptd.getCryptoEvidence().getAny().add(revocationInformation);
            for(Element e: inspectionElements){
            	ptd.getCryptoEvidence().getAny().add(e);
            }
            PolicyTranslator polTransl = new PolicyTranslator(ptd, this.getCredSpecList(ptd), this.keyManager);

            IdemixClaim idmxClaim = new IdemixClaim(polTransl, this.keyManager,
                    this.contextGen, this.verifierRevocation);

            //insert the proof
            idmxClaim.setEvidence(proof);

            for(Representation r: representations){
                idmxClaim.getRepresentations().add(r);
            }
            //verify the claim
            verIdmxClaim = idmxClaim;


        } catch (Exception e) {
        	e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            this.logger.info(sw.toString());
            throw new TokenVerificationException(e);
        }

        return verIdmxClaim.isValid();
    }

    @Override
    public boolean verifyToken(PresentationToken t)
            throws TokenVerificationException, CryptoEngineException {
    	
         //unwrap presentation token and crypto params

        VerifiableClaim verIdmxClaim = null;


        try{
            CryptoParams cryptoEvidence = t.getCryptoEvidence();

            //parse idemix proof from the token
            Proof	proof = (Proof) Parser.getInstance().parse((Element) cryptoEvidence.getAny().get(0));

            this.verifierRevocation.clearCredentialInToken();

            //load issuer parameters and credential structures to Idemix
            List<URI> issuerParamsURIList = new ArrayList<URI>();
            List<URI> credSpecsURIList = new ArrayList<URI>();
            for (CredentialInToken credInToken: t.getPresentationTokenDescription().getCredential()){

                this.verifierRevocation.addCredentialInToken(credInToken);

                credSpecsURIList.add(credInToken.getCredentialSpecUID());
                if (!issuerParamsURIList.contains(credInToken.getIssuerParametersUID())){
                    issuerParamsURIList.add(credInToken.getIssuerParametersUID());
                }
            }

            this.loadIdemixSystemAndIssuerParameters(issuerParamsURIList);
            this.loadIdemixCredentialStructures(credSpecsURIList);

            //create Idemix Claim from the token
            PresentationTokenDescription ptd = t.getPresentationTokenDescription();
            PolicyTranslator polTransl = new PolicyTranslator(ptd, this.getCredSpecList(ptd));

            IdemixClaim idmxClaim = new IdemixClaim(polTransl, this.keyManager,
                    this.contextGen, this.verifierRevocation);

            //insert the proof
            idmxClaim.setEvidence(proof);

            //verify the claim
            verIdmxClaim = idmxClaim;

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            this.logger.info(sw.toString());
            throw new TokenVerificationException(e);
        }
        
        return verIdmxClaim.isValid();
    }

    /////////////////////////////////////////////////////////////////////
    ///helpers
    /////////////////////////////////////////////////////////////////////

    private Map<String, CredentialSpecification> getCredSpecList(PresentationTokenDescription ptd){

        Map<String, CredentialSpecification> aliasCredSpecs = new HashMap<String, CredentialSpecification>();

        for (CredentialInToken credInToken: ptd.getCredential()){
            CredentialSpecification credSpec = null;
            try {
                credSpec = this.keyManager.getCredentialSpecification(credInToken.getCredentialSpecUID());
            } catch (KeyManagerException e) {
                throw new RuntimeException("no cred spec"+credInToken.getCredentialSpecUID()+"stored:"+e);
            }
            if (credSpec!=null){
                aliasCredSpecs.put(credInToken.getAlias().toString(), credSpec);
            } else {
                throw new RuntimeException("cannot retrieve a cred spec: "+credInToken.getCredentialSpecUID());
            }
        }
        return aliasCredSpecs;
    }

    private Map<String, CredentialSpecification> getCredSpecList(PresentationTokenDescriptionWithCommitments ptd){

        Map<String, CredentialSpecification> aliasCredSpecs = new HashMap<String, CredentialSpecification>();

        for (CredentialInTokenWithCommitments credInToken: ptd.getCredential()){
            CredentialSpecification credSpec = null;
            try {
                credSpec = this.keyManager.getCredentialSpecification(credInToken.getCredentialSpecUID());
            } catch (KeyManagerException e) {
                throw new RuntimeException("no cred spec"+credInToken.getCredentialSpecUID()+"stored:"+e);
            }
            if (credSpec!=null){
                aliasCredSpecs.put(credInToken.getAlias().toString(), credSpec);
            } else {
                throw new RuntimeException("cannot retrieve a cred spec: "+credInToken.getCredentialSpecUID());
            }
        }
        return aliasCredSpecs;
    }

    private void loadIdemixSystemAndIssuerParameters(List<URI> issuerParamsURIList){

        IssuerPublicKey isPK = null;
        SystemParameters systemParameters = null;
        IssuerParameters issuerParameters = null;
        com.ibm.zurich.idmx.utils.SystemParameters sysPar = null;

        for(URI issuerURI: issuerParamsURIList){
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

            if (systemParameters == null){
                systemParameters = issuerParameters.getSystemParameters();

                IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(systemParameters);
                sysPar = idemixSystemParameters.getSystemParameters();
                GroupParameters grPar = idemixSystemParameters.getGroupParameters();

                //Load system, group and issuer parameters to Idemix StructureStore
                StructureStore.getInstance().add(IdemixConstants.systemParameterId, sysPar);
                StructureStore.getInstance().add(IdemixConstants.groupParameterId, grPar);
            }
        }
    }


    private void loadIdemixCredentialStructures(List<URI> credSpecsURIList){

        for (URI credSpecURI: credSpecsURIList){
            CredentialStructure credStruct = null;
            CredentialSpecification credSpec = null;
            try {
                credSpec = this.keyManager.getCredentialSpecification(credSpecURI);
            } catch (KeyManagerException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            //create credential structure for Idemix call and load it to Idemix Structure Store

            if (credSpec != null) {
                credStruct = IdemixUtils.createIdemixCredentialStructure(null,
                        credSpec, null, null, credSpec.isKeyBinding());
                StructureStore.getInstance().add(credSpecURI.toString(), credStruct);
            } else {
                throw new RuntimeException("cannot extract cred spec");
            }
        }
    }

}


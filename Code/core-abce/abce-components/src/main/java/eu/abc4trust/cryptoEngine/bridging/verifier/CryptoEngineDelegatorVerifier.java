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

package eu.abc4trust.cryptoEngine.bridging.verifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.internal.revocation.VerifierRevocation;
import eu.abc4trust.cryptoEngine.idemix.verifier.IdemixCryptoEngineVerifierImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.uprove.verifier.UProveCryptoEngineVerifierImpl;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInRevocation;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
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
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.VerifierDrivenRevocationInToken;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * CryptoEngineVerifier implementation that handles crypto orchestration and delegates
 * requests to the appropriate crypto engine (Idemix or UProve).
 * 
 * @author Michael Stausholm
 */

public class CryptoEngineDelegatorVerifier implements CryptoEngineVerifier {
    private final IdemixCryptoEngineVerifierImpl idemixEngine;

    private final UProveCryptoEngineVerifierImpl uproveEngine;
    private final KeyManager keyManager;

    private final VerifierRevocation verifierRevocation;

    @Inject
    public CryptoEngineDelegatorVerifier(KeyManager keyManager,
            IdemixCryptoEngineVerifierImpl idemix,
            UProveCryptoEngineVerifierImpl uprove,
            VerifierRevocation verifierRevocation) {
        this.idemixEngine = idemix;
        this.uproveEngine = uprove;
        this.keyManager = keyManager;
        this.verifierRevocation = verifierRevocation;
    }

    @Override
    public boolean verifyToken(PresentationToken t) throws TokenVerificationException {

        ObjectFactory of = new ObjectFactory();
       // System.out.println("Bridging verify token: "+t);
        try{
            //            String xml = XmlUtils.toXml(of.createPresentationToken(t), false);
            //            System.out.println(xml+"\n\n");


            String[] pseudonymEngines = this.getPseudonymEngines(t);

            boolean tokenContainsUProve = false;
            PresentationTokenWithCommitments idemixToken = of.createPresentationTokenWithCommitments();
            PresentationToken uproveToken = of.createPresentationToken();
            if(t.getVersion() == null){
                uproveToken.setVersion("1.0");
                idemixToken.setVersion("1.0");
            } else {
                idemixToken.setVersion(t.getVersion());
                uproveToken.setVersion(t.getVersion());
            }
            PresentationTokenDescription desc = t.getPresentationTokenDescription();
            PresentationTokenDescription iDesc = of.createPresentationTokenDescription();
            PresentationTokenDescription uDesc = of.createPresentationTokenDescription();
            iDesc.setMessage(desc.getMessage());
            uDesc.setMessage(desc.getMessage());
            iDesc.setPolicyUID(desc.getPolicyUID());
            uDesc.setPolicyUID(desc.getPolicyUID());
            iDesc.setTokenUID(desc.getTokenUID());
            uDesc.setTokenUID(desc.getTokenUID());


            for(int i = 0; i< pseudonymEngines.length; i++){
                PseudonymInToken pit =  t.getPresentationTokenDescription().getPseudonym().get(i);
                if("UPROVE".equals(pseudonymEngines[i])){
                    uDesc.getPseudonym().add(pit);
                }else {
                    iDesc.getPseudonym().add(pit);
                }
            }

            this.verifierRevocation.clearCredentialInToken();

            Set<URI> idemixCredentialAliases = new HashSet<URI>();
            Set<URI> uproveCredentialAliases = new HashSet<URI>();
            for(CredentialInToken cit: desc.getCredential()){

                this.verifierRevocation.addCredentialInToken(cit);

                /*
                try{
                	System.out.println("original: "+XmlUtils.toXml(of.createCredentialInToken(cit)));
                }catch(Exception e){System.out.println("failed to print original cit");};
                 */
                if((this.keyManager.getIssuerParameters(cit.getIssuerParametersUID())).getAlgorithmID().equals(CryptoUriUtil.getIdemixMechanism())){
                    idemixCredentialAliases.add(cit.getAlias());
                    iDesc.getCredential().add(cit);
                } else {
                    uproveCredentialAliases.add(cit.getAlias());
                    uDesc.getCredential().add(cit);
                    tokenContainsUProve = true;

                    //Add issuer parameters to Idemix Structure store
                    try{
                        this.storeUProveIssuerParameters(cit.getIssuerParametersUID());
                    }catch(Exception e){
                        System.out.println("Failed to store UProve issuer parameters in Idemix structure store");
                        throw new RuntimeException(e);
                    }


                }


            }



            for(AttributePredicate ap: desc.getAttributePredicate()){
                for(Object o: ap.getAttributeOrConstantValue()){
                    if(o instanceof AttributePredicate.Attribute) {
                        iDesc.getAttributePredicate().add(ap);
                        break;
                    }
                }
            }


            //TODO This might require some changes to work with revocation
            for(VerifierDrivenRevocationInToken vdr: desc.getVerifierDrivenRevocation()){
                for(AttributeInRevocation air: vdr.getAttribute()){
                    if(idemixCredentialAliases.contains(air.getCredentialAlias())) {
                        iDesc.getVerifierDrivenRevocation().add(vdr);
                        break;
                    }else {
                        uDesc.getVerifierDrivenRevocation().add(vdr);
                    }
                }
            }

            //TODO Look at the uprove crypto evidence, if there are committed indices,
            // check with cred spec if they are for revocation handles
            // if so, do some stuff that allows poltransl and the other idemix parts to work

            idemixToken.setPresentationTokenDescriptionWithCommitments(this.addCommitments(iDesc));
            uproveToken.setPresentationTokenDescription(uDesc);

            boolean verifies = false;
            CryptoParams tcp = t.getCryptoEvidence();
            CryptoParams icp = of.createCryptoParams();
            boolean cryptoEvidenceContainsIdemix = false;
            for(Object o: tcp.getAny()){
                try{
                    if(o instanceof JAXBElement) {
                        JAXBElement<?> jaxb = (JAXBElement<?>) o;
                        Object wrapped = jaxb.getValue();
                        if(wrapped instanceof CredentialInTokenWithCommitments) {
                            cryptoEvidenceContainsIdemix = true;
                            CredentialInTokenWithCommitments citwc = (CredentialInTokenWithCommitments) wrapped;
                            idemixToken.getPresentationTokenDescriptionWithCommitments().getCredential().add(citwc); // (CredentialInTokenWithCommitments)XmlUtils.getObjectFromXML(credIn, false));
                        } else {
                            System.err.println("Element is JaxB : " + jaxb.getValue() + " : " + jaxb.getDeclaredType() + " : " + jaxb.getScope() + " : " + jaxb.getName());
                        }
                        continue;
                    }
                    Element e = (Element)o;
                    String elementName = e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
                    if("UProveCredentialAndPseudonym".equals(elementName)){
                        String credAlias = ((Element)e.getElementsByTagName("Proof").item(0)).getElementsByTagName("CredentialAlias").item(0).getTextContent();
                        CredentialSpecification credSpec = null;
                        CredentialInToken c = null;

                        for(CredentialInToken cit: t.getPresentationTokenDescription().getCredential()){
                            if(cit.getAlias().toString().equals(credAlias)){
                                credSpec = this.keyManager.getCredentialSpecification(cit.getCredentialSpecUID());
                                c = cit;
                                break;
                            }
                        }

                        if (credSpec == null) {
                            throw new RuntimeException(
                                    "Could not find credential specification.");
                        }

                        List<Integer> inspectableAttributes = new ArrayList<Integer>();
                        Map<Integer, URI> indexToInspectorKey = new HashMap<Integer, URI>();
                        Map<Integer, URI> indexToAttributeType = new HashMap<Integer, URI>();
                        // TODO here we have to also look at any inspectable attributes.
                        // If there are any, we need to create an inspectable information element etc.
                        int revocationindex = -1;
                        for(int i = 0; i< credSpec.getAttributeDescriptions().getAttributeDescription().size(); i++){
                            AttributeDescription ad = credSpec.getAttributeDescriptions().getAttributeDescription().get(i);

                            if(ad.getType().toString().equals("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle")){
                                revocationindex = i;
                            }
                            for(AttributeInToken da: c.getDisclosedAttribute()){
                                if(da.getAttributeType().equals(ad.getType()) && (da.getInspectorPublicKeyUID()!=null)){
                                    inspectableAttributes.add(i);
                                    indexToInspectorKey.put(i,da.getInspectorPublicKeyUID());
                                    indexToAttributeType.put(i,da.getAttributeType());
                                }
                            }
                        }
                        Set<Integer> committedValues = new HashSet<Integer>();
                        committedValues.addAll(inspectableAttributes);
                        if(!committedValues.contains(revocationindex) && (revocationindex != -1)) {
                            committedValues.add(revocationindex);
                        }
                        Map<Integer, Integer> indexToIndexInProof = this.mapIndices(committedValues);

                        if(e.getElementsByTagName("CommittedAttributesIndices").getLength() > 0){
                            Element cai = (Element)e.getElementsByTagName("CommittedAttributesIndices").item(0);
                            NodeList nl = cai.getChildNodes();
                            for(int j = 0; j<nl.getLength(); j++){
                                Element ind = null;
                                try{
                                    ind = (Element)nl.item(j);
                                }catch(ClassCastException ex){
                                    //If we end up here, it is most likely because we got a TextElement which is empty due
                                    //to malformed xml (such as \r or \n)
                                    continue;
                                }
                                int committedIndex =Integer.parseInt(ind.getTextContent())-1;
                                Element root = null;
                                //make addToRoot method, to allow for inspectable revocationinformation
                                if (committedIndex == revocationindex){
                                    root = ind.getOwnerDocument().createElement("RevocationInformation");
                                }
                                else if(inspectableAttributes.contains(committedIndex)){
                                    root = ind.getOwnerDocument().createElement("InspectableInformation");
                                    root.setAttribute("InspectorPublicKey", indexToInspectorKey.get(committedIndex).toString());
                                    root.setAttribute("AttributeType", indexToAttributeType.get(committedIndex).toString());
                                }else{
                                    //TODO: @Michael - explain this code a little :)
                                    continue;
                                }
                                root.setAttribute("CredentialAlias", c.getAlias().toString());
                                root.setAttribute("CredentialSpecUID", credSpec.getSpecificationUID().toString());
                                root.setAttribute("IssuerParamsUID", c.getIssuerParametersUID().toString());

                                Element tilde = root.getOwnerDocument().createElement("TildeO");
                                root.appendChild(tilde);
                                NodeList tildes = e.getElementsByTagName("TildeO").item(0).getChildNodes();
                                //
                               // System.out.println("got the tildeO nodes: "+tildes.getLength());

                                tilde.appendChild(tildes.item(indexToIndexInProof.get(committedIndex)).cloneNode(true));
                                tilde = root.getOwnerDocument().createElement("TildeValues");
                                root.appendChild(tilde);
                                tildes = e.getElementsByTagName("TildeValues").item(0).getChildNodes();
                                tilde.appendChild(tildes.item((indexToIndexInProof.get(committedIndex)*3)).cloneNode(true));
                                icp.getAny().add(root);
                            }
                        }
                    }


                    if("IdmxProof".equals(elementName)) {
                        cryptoEvidenceContainsIdemix = true;
                        icp.getAny().add(o);
                    }
                    if("CredentialInTokenWithCommitments".equals(elementName) || "abc:CredentialInTokenWithCommitments".equals(elementName)) {
                        cryptoEvidenceContainsIdemix = true;
                        //magic code adding the CrednetialInToken
                        TransformerFactory transfac = TransformerFactory.newInstance();
                        Transformer trans = transfac.newTransformer();
                        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        trans.setOutputProperty(OutputKeys.INDENT, "yes");

                        //create string from xml tree
                        StringWriter sw = new StringWriter();
                        StreamResult result = new StreamResult(sw);
                        DOMSource source = new DOMSource(e.getOwnerDocument());
                        trans.transform(source, result);
                        String xmlString = sw.toString();

                        InputStream credIn = new ByteArrayInputStream(xmlString.getBytes());
                        idemixToken.getPresentationTokenDescriptionWithCommitments().getCredential().add((CredentialInTokenWithCommitments)XmlUtils.getObjectFromXML(credIn, false));
                    }
                    if("VerifiableEncryption".equals(elementName)) {
                        icp.getAny().add(o);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    //TODO @jdn, I think your revocation code causes some some serialization problems /Michael
                    //throw new RuntimeException(e);
                }
            }


            if(cryptoEvidenceContainsIdemix) {
                idemixToken.setCryptoEvidence(icp);

                //    System.out.println("\n\nNow verifying idmtoken!!");
                //	System.out.println(XmlUtils.toXml(of.createPresentationTokenWithCommitments(idemixToken), false)+"\n\n");


                //System.out.println("about to call idmix engine for verification");
                //System.out.println("we have "+idemixToken.getCryptoEvidence().getAny().size()+" crypto evidence");
                if(idemixToken.getCryptoEvidence().getAny().size() == 0) {
                    verifies = true;
                } else {
                    verifies = this.idemixEngine.verifyTokenWithCommitments(idemixToken);
                }
                System.out.println("idemix verifies: "+verifies);
            }



            if(tokenContainsUProve){
                //                CryptoParams tcp = t.getCryptoEvidence();
                tcp = t.getCryptoEvidence();
                CryptoParams ucp = of.createCryptoParams();
                for(Object o: tcp.getAny()){
                    try{
                        Element e = (Element)o;
                        if(e.getNodeName().startsWith("UProve")){
                            ucp.getAny().add(o);
                        }
                    }catch(Exception e){
                    }
                }
                uproveToken.setCryptoEvidence(ucp);
                //System.out.println("Now verifying uprove");

                //xml = XmlUtils.toXml(of.createPresentationToken(uproveToken), false);
                //System.out.println(xml+"\n\n");

                boolean ver = this.uproveEngine.verifyToken(uproveToken);
                //System.out.println("uprove verifies: "+ver);
                if(cryptoEvidenceContainsIdemix) {
                    verifies = verifies && ver;
                } else {
                    verifies = ver;
                }
            }

            //System.out.println("DELEGATION VERIFIER.verifies:::: "+verifies);
            return verifies;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private Map<Integer, Integer> mapIndices(Set<Integer> inspectableAttributes) {
        Map<Integer, Integer> ret = new HashMap<Integer,Integer>();
        int count = inspectableAttributes.size();
        for(int i = 0; i< count; i++){
            int key = this.getSmallest(inspectableAttributes);
            ret.put(key, i);
            inspectableAttributes.remove(key);
        }
        return ret;
    }

    private Integer getSmallest(Set<Integer> list){
        int ret = -1;
        for(int i: list){
            if(ret <= 0) {
                ret = i;
            } else if(i<ret) {
                ret = i;
            }
        }
        return ret;
    }


    /**
     * We need to map the pseudonyms to specific engines.
     * This is done by iterating over the pseudonyms and for each finding
     * the cryptoevidence matching either the alias or the index of the pseudonym
     * 
     * @param ptd
     * @return
     */
    //TODO Need to test presentationtokens containing multiple pseudonyms
    private String[] getPseudonymEngines(PresentationToken pt){
        String[] ret = new String[pt.getPresentationTokenDescription().getPseudonym().size()];
        for(int i = 0; i< ret.length; i++){
            PseudonymInToken pit = pt.getPresentationTokenDescription().getPseudonym().get(i);
            String pseudonymAlias = "";
            if(pit.getAlias() != null){
                pseudonymAlias = pit.getAlias().toString();
            }
            ret[i] = "IDEMIX"; // We assume idemix pseudonyms by default and change if we find out it is UProve
            for(Object o: pt.getCryptoEvidence().getAny()){
                try{
                    Element elm = (Element)o;
                    if(elm.getNodeName().equals("UProvePseudonym") || elm.getNodeName().equals("UProveCredentialAndPseudonym")){
                        // This will cause a nullpointer exception if

                        if(elm.getElementsByTagName("PseudonymAlias").getLength()==0) {
                            break;
                        }
                        String uproveAlias = elm.getElementsByTagName("PseudonymAlias").item(0).getTextContent();
                        if(uproveAlias.equals(pseudonymAlias)){
                            ret[i] = "UPROVE";
                            break; //breaks innerloop over objects in cryptoevidence
                        } else {
                            int index = Integer.parseInt(uproveAlias.substring(uproveAlias.lastIndexOf("/")+1));
                            if(index == i){
                                ret[i] = "UPROVE";
                                break;
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }



    private PresentationTokenDescriptionWithCommitments addCommitments(PresentationTokenDescription ptd){
        ObjectFactory of = new ObjectFactory();
        PresentationTokenDescriptionWithCommitments ptdwc = of.createPresentationTokenDescriptionWithCommitments();
        ptdwc.setMessage(ptd.getMessage());
        ptdwc.setPolicyUID(ptd.getPolicyUID());
        ptdwc.setTokenUID(ptd.getTokenUID());
        ptdwc.getAttributePredicate().addAll(ptd.getAttributePredicate());
        ptdwc.getPseudonym().addAll(ptd.getPseudonym());
        ptdwc.getVerifierDrivenRevocation().addAll(ptd.getVerifierDrivenRevocation());
        for(CredentialInToken cit: ptd.getCredential()){
            CredentialInTokenWithCommitments citwc = of.createCredentialInTokenWithCommitments();
            citwc.setAlias(cit.getAlias());
            citwc.setSameKeyBindingAs(cit.getSameKeyBindingAs());
            citwc.setIssuerParametersUID(cit.getIssuerParametersUID());
            citwc.setRevocationInformationUID(cit.getRevocationInformationUID());
            citwc.setCredentialSpecUID(cit.getCredentialSpecUID());
            citwc.getDisclosedAttribute().addAll(cit.getDisclosedAttribute());

            ptdwc.getCredential().add(citwc);

        }
        return ptdwc;
    }

    private void storeUProveIssuerParameters(URI issuerParamsUID) throws Exception{
        IssuerParametersComposite ipc = null;
        IssuerParameters params = this.keyManager.getIssuerParameters(issuerParamsUID);
        UProveUtils utils = new UProveUtils();
        ipc = utils.convertIssuerParameters(params);
        StructureStore.getInstance().add(issuerParamsUID.toString(), ipc);
    }

}

//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.uprove.util;

import java.net.URI;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PresentationProofComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SubgroupGroupDescriptionComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfbase64Binary;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;

public class UProveSerializer {

	private static final String SYSTEM_PARAMETERS_NS = "urn:eu:abc4trust:systemparameters:uprove:1.0";
	private static final String KEY_LENGTH = "KeyLength";
	private static final String GROUP_OID = "GroupOID";
    private static final String NUMBER_OF_TOKENS = "NumberOfTokens";

	private static final String PROOF = "Proof";
	private static final String G_NAME = "G";
	private static final String G_NAME_SUB = "SubGroupG";
	private static final String ISSUER_PARAMETERS = "IssuerParameters";
	private static final String USES_RECOMMENDED_PARAMETERS = "UsesRecommendedParameters";
	private static final String UID_P = "UidP";
	private static final String UID_H = "UidH";
	private static final String S = "S";
	private static final String IS_DEVICE_SUPPORTED = "IsDeviceSupported";
	private static final String HASH_FUNCTION_OID = "HashFunctionOID";
	private static final String GQ = "Gq";
	private static final String GD = "Gd";
	private static final String E_NAME = "E";
	private static final String P_NAME = "P";
	private static final String P_NAME_SUB = "SubGroupP";
	private static final String GD_NAME_SUB = "SubGroupGD";
	private static final String Q_NAME = "Q";
	private static final String Q_NAME_SUB = "SubGroupQ";
	private static final String COMMITTED_ATTRIBUTES_INDICES = "CommittedAttributesIndices";
	private static final String DISCLOSED_PARAM_INDICES = "DisclosedParamIndices";
	private static final String TOKEN_ID = "TokenId";
	private static final String MESSAGE_D = "MessageD";
	private static final String TILDE_VALUES = "TildeValues";
	private static final String TILDE_O = "TildeO";
	private static final String R_NAME = "R";
	private static final String PS_NAME = "Ps";
	private static final String DISCLOSED_ATTRIBUTES = "DisclosedAttributes";
	private static final String AP_NAME = "Ap";
	private static final String A_NAME = "A";
	private static final String BYTE_ARRAY = "ByteArray";
	private static final String INT = "Int";
	private static final String IS_DEVICE_PROTECTED = "IsDeviceProtected";
	private static final String UIDP = "Uidp";
	private static final String TI = "TI";
	private static final String SIGMA_Z_PRIME = "SigmaZPrime";
	private static final String SIGMA_R_PRIME = "SigmaRPrime";
	private static final String SIGMA_C_PRIME = "SigmaCPrime";
	private static final String PI = "PI";
	private static final String H = "H";
	private static final String U_PROVE_TOKEN_ELEMENT = "UProveTokenElement";
	public static final String CREDENTIAL_ALIAS = "CredentialAlias";
	public static final String U_PROVE_CREDENTIAL_AND_PSEUDONYM = "UProveCredentialAndPseudonym";
	public static final String U_PROVE_PSEUDONYM = "UProvePseudonym";
	public static final String PSEUDONYM_ALIAS = "PseudonymAlias";

	private Document wrapper;

	public UProveSerializer() {
		try {
			this.wrapper = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns an XML serialized UProve pseudonym.
	 * 
	 * @param proof
	 * @param nymAlias
	 * @param issuerParameterElement
	 * @return
	 */
	public Node serialize(PseudonymComposite proof, URI nymAlias,
			IssuerParametersComposite ipc) {
		Element uprovePseudonymElement = null;
		uprovePseudonymElement = this.wrapper.createElement(U_PROVE_PSEUDONYM);

		Element proofElement = this.wrapper.createElement(PROOF);
		uprovePseudonymElement.appendChild(proofElement);

		byte[] A = proof.getA().getValue();
		byte[] P = proof.getP().getValue();
		byte[] R = proof.getR().getValue();

		proofElement.appendChild(this.createByteElement(A_NAME, A));
		proofElement.appendChild(this.createByteElement(P_NAME, P));
		proofElement.appendChild(this.createByteElement(R_NAME, R));
		proofElement.appendChild(this
				.createIssuerParametersCompositeElement(ipc));

		uprovePseudonymElement.appendChild(this.createURIElement(
				PSEUDONYM_ALIAS, nymAlias));
		return uprovePseudonymElement;
	}

	/**
	 * Returns an XML serialized UProve Credential and pseudonym.
	 * 
	 * @param proof
	 * @param credAlias
	 * @param disclosedAttributes
	 * @param committedAttributes
	 * @param compositeToken
	 * @param ipc
	 * @return
	 */
	public Element serialize(PresentationProofComposite proof, URI credAlias,
			ArrayOfint disclosedAttributes, ArrayOfint committedAttributes,
			UProveTokenComposite compositeToken, IssuerParametersComposite ipc) {
		Element uproveCredentialAndPseudonymElement = null;

		uproveCredentialAndPseudonymElement = this.wrapper
				.createElement(U_PROVE_CREDENTIAL_AND_PSEUDONYM);

		Element proofElement = this.wrapper.createElement(PROOF);
		uproveCredentialAndPseudonymElement.appendChild(proofElement);

		byte[] A = proof.getA().getValue();
		byte[] Ap = proof.getAp().getValue();
		byte[] Ps = proof.getPs().getValue();
		byte[] tokenId = proof.getTokenID().getValue();
		byte[] messageD = proof.getMessageD().getValue();

		proofElement.appendChild(this.createByteElement(A_NAME, A));
		proofElement.appendChild(this.createByteElement(AP_NAME, Ap));
		proofElement.appendChild(this.createByteElement(PS_NAME, Ps));
		proofElement.appendChild(this.createByteElement(TOKEN_ID, tokenId));
		proofElement.appendChild(this.createByteElement(MESSAGE_D, messageD));

		proofElement.appendChild(this
				.createArrayOfbase64BinaryElement(DISCLOSED_ATTRIBUTES, proof
						.getDisclosedAttributes().getValue()));
		proofElement.appendChild(this.createArrayOfbase64BinaryElement(R_NAME,
				proof.getR().getValue()));
		proofElement.appendChild(this.createArrayOfbase64BinaryElement(TILDE_O,
				proof.getTildeO().getValue()));
		proofElement.appendChild(this.createArrayOfbase64BinaryElement(
				TILDE_VALUES, proof.getTildeValues().getValue()));

		proofElement.appendChild(this.createIntElementArray(
				DISCLOSED_PARAM_INDICES, disclosedAttributes));
		proofElement.appendChild(this.createIntElementArray(
				COMMITTED_ATTRIBUTES_INDICES, committedAttributes));
		proofElement.appendChild(this.createUProveTokenElement(compositeToken));
		proofElement.appendChild(this
				.createIssuerParametersCompositeElement(ipc));

		proofElement.appendChild(this.createURIElement(CREDENTIAL_ALIAS,
				credAlias));

		return uproveCredentialAndPseudonymElement;
	}

	public Node createKeyLengthElement(int keyLength) {
		Element k = this.wrapper.createElementNS(SYSTEM_PARAMETERS_NS,
				KEY_LENGTH);
		k.setPrefix("uprove");
		k.setTextContent("" + keyLength);
		return k;
	}

	public Node createGroupOIDElement(String groupName) {
		Element e = this.wrapper.createElementNS(SYSTEM_PARAMETERS_NS,
				GROUP_OID);
		e.setPrefix("uprove");
		e.setTextContent(groupName);
		return e;
	}

	public Node createNumberOfTokensElement(int numberOfTokens) {
	    Element e = this.wrapper.createElementNS(SYSTEM_PARAMETERS_NS,
	      NUMBER_OF_TOKENS);
        e.setPrefix("uprove");
        e.setTextContent("" + numberOfTokens);
        return e;
	}
	
	public Node createSubgroupGroupDescriptionCompositeElement(
			SubgroupGroupDescriptionComposite sGroup) {
		Element e = this.wrapper.createElement(GQ);
		e.appendChild(this.createByteElement(P_NAME_SUB,
				this.getValueAndCheckForNull(sGroup.getP())));
		e.appendChild(this.createByteElement(Q_NAME_SUB,
				this.getValueAndCheckForNull(sGroup.getQ())));
		e.appendChild(this.createByteElement(G_NAME_SUB,
				this.getValueAndCheckForNull(sGroup.getG())));
		e.appendChild(this.createByteElement(GD_NAME_SUB,
				this.getValueAndCheckForNull(sGroup.getGd())));

		return e;
	}

	public Node createIssuerParametersCompositeElement(
			IssuerParametersComposite ipc) {
		Element e = this.wrapper
				.createElement(U_PROVE_CREDENTIAL_AND_PSEUDONYM);

		Element issuerParametersElement = this.wrapper
				.createElement(ISSUER_PARAMETERS);
		e.appendChild(issuerParametersElement);

		issuerParametersElement.appendChild(this.createByteElement(E_NAME,
				this.getValueAndCheckForNull(ipc.getE())));
		issuerParametersElement
				.appendChild(this.createArrayOfbase64BinaryElement(G_NAME, ipc
						.getG().getValue()));
		issuerParametersElement.appendChild(this.createByteElement(GD,
				this.getValueAndCheckForNull(ipc.getGd())));
		issuerParametersElement.appendChild(this
				.createSubgroupGroupDescriptionCompositeElement(ipc.getGq()
						.getValue()));
		issuerParametersElement.appendChild(this.createStringElement(
				HASH_FUNCTION_OID, ipc.getHashFunctionOID().getValue()));
		issuerParametersElement.appendChild(this.createByteElement(S,
				this.getValueAndCheckForNull(ipc.getS())));
		issuerParametersElement.appendChild(this.createStringElement(UID_H, ipc
				.getUidH().getValue()));
		issuerParametersElement.appendChild(this.createByteElement(UID_P,
				this.getValueAndCheckForNull(ipc.getUidP())));

		issuerParametersElement
				.appendChild(this.createBooleanElement(
						USES_RECOMMENDED_PARAMETERS,
						ipc.isUsesRecommendedParameters()));
		issuerParametersElement.appendChild(this.createBooleanElement(
				IS_DEVICE_SUPPORTED, ipc.isIsDeviceSupported()));

		return issuerParametersElement;
	}

	private byte[] getValueAndCheckForNull(JAXBElement<byte[]> s) {
		if (s != null) {
			return s.getValue();
		}
		return null;
	}

	private Node createBooleanElement(String name, boolean b) {
		Node element = this.wrapper.createElement(name);
		element.setTextContent("" + b);
		return element;
	}

	private Node createStringElement(String name, String value) {
		Node element = this.wrapper.createElement(name);
		if (value == null) {
			return element;
		}
		element.setTextContent(value);
		return element;
	}

	private Node createUProveTokenElement(UProveTokenComposite compositeToken) {
		Element e = this.wrapper
				.createElement(U_PROVE_CREDENTIAL_AND_PSEUDONYM);

		Element uproveTokenElement = this.wrapper
				.createElement(U_PROVE_TOKEN_ELEMENT);
		e.appendChild(uproveTokenElement);

		uproveTokenElement.appendChild(this.createByteElement(H, compositeToken
				.getH().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(PI,
				compositeToken.getPI().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(SIGMA_C_PRIME,
				compositeToken.getSigmaCPrime().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(SIGMA_R_PRIME,
				compositeToken.getSigmaRPrime().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(SIGMA_Z_PRIME,
				compositeToken.getSigmaZPrime().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(TI,
				compositeToken.getTI().getValue()));
		uproveTokenElement.appendChild(this.createByteElement(UIDP,
				compositeToken.getUidp().getValue()));
		uproveTokenElement.appendChild(this.createBooleanElement(
				IS_DEVICE_PROTECTED, compositeToken.isIsDeviceProtected()));
		return uproveTokenElement;
	}

	private Node createIntElementArray(String name, ArrayOfint arrayOfInt) {
		Node element = this.wrapper.createElement(name);
		if (arrayOfInt == null) {
			return element;
		}
		for (int i : arrayOfInt.getInt()) {
			Node intElement = this.createIntElement(i);
			element.appendChild(intElement);
		}
		return element;
	}

	public Node createIntElement(int i) {
		Node intElement = this.wrapper.createElement(INT);
		intElement.setTextContent("" + i);
		return intElement;
	}

	private Node createURIElement(String name, URI credAlias) {
		Node element = this.wrapper.createElement(name);
		if (credAlias == null) {
			return element;
		}
		element.setTextContent(credAlias.toString());
		return element;
	}

	private Node createArrayOfbase64BinaryElement(String name,
			ArrayOfbase64Binary arrayOfBase64Binary) {
		Node element = this.wrapper.createElement(name);
		if (arrayOfBase64Binary == null) {
			return element;
		}
		List<byte[]> base64Binary = arrayOfBase64Binary.getBase64Binary();

		for (byte[] bytes : base64Binary) {
			Node byteArray = this.wrapper.createElement(BYTE_ARRAY);
			byteArray
					.setTextContent(DatatypeConverter.printBase64Binary(bytes));
			element.appendChild(byteArray);
		}
		return element;
	}

	public Node createByteElement(String name, byte[] bytes) {
		Node element = this.wrapper.createElement(name);
		if (bytes == null) {
			return element;
		}
		element.setTextContent(DatatypeConverter.printBase64Binary(bytes));
		return element;
	}

	public PresentationProofComposite deserializeToPresentationProofComposite(
			Element e) {
		org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
		PresentationProofComposite pc = new PresentationProofComposite();
		byte[] bytes = this.getBytes(A_NAME, e);
		pc.setA(ofup.createPresentationProofCompositeA(bytes));
		bytes = this.getBytes(AP_NAME, e);
		pc.setAp(ofup.createPresentationProofCompositeAp(bytes));
		ArrayOfbase64Binary base64Binary = this.getBase64Binary(
				DISCLOSED_ATTRIBUTES, e);
		pc.setDisclosedAttributes(ofup
				.createPresentationProofCompositeDisclosedAttributes(base64Binary));
		bytes = this.getBytes(PS_NAME, e);
		pc.setPs(ofup.createPresentationProofCompositePs(bytes));
		base64Binary = this.getBase64Binary(R_NAME, e);
		pc.setR(ofup.createPresentationProofCompositeR(base64Binary));
		base64Binary = this.getBase64Binary(TILDE_O, e);
		pc.setTildeO(ofup.createPresentationProofCompositeTildeO(base64Binary));
		base64Binary = this.getBase64Binary(TILDE_VALUES, e);
		pc.setTildeValues(ofup
				.createPresentationProofCompositeTildeValues(base64Binary));
		bytes = this.getBytes(TOKEN_ID, e);
		pc.setTokenID(ofup.createPresentationProofCompositeTokenID(bytes));
		bytes = this.getBytes(MESSAGE_D, e);
		pc.setMessageD(ofup.createPresentationProofCompositeMessageD(bytes));

		return pc;
	}

	public ArrayOfint deserializeToDisclosedIndices(Element e) {
		String name = DISCLOSED_PARAM_INDICES;
		return this.getArrayOfint(e, name);
	}

	public ArrayOfint deserializeToCommittedIndices(Element e) {
		String name = COMMITTED_ATTRIBUTES_INDICES;
		return this.getArrayOfint(e, name);
	}

	private ArrayOfint getArrayOfint(Element e, String name) {
		Element t = this.getElementByName(name, e);
		ArrayOfint res = new ArrayOfint();
		NodeList nodes = t.getChildNodes();
		for (int inx = 0; inx < nodes.getLength(); inx++) {
			Node n = nodes.item(inx);
			if (n.getNodeType() != 1) {
				if (n.getNodeType() == 3) {
					System.out
							.println("DEBUG: node type is of the type 'Text' - just whitespace ? ["
									+ n.getTextContent().trim() + "]");
				} else {
					System.out
							.println("WARN: node type is not of the type WC3Dom Element - node type # : "
									+ n.getNodeType() + " : " + n);
				}
				continue;
			}
			res.getInt().add(this.getInt((Element) n));
		}
		return res;
	}

	private Integer getInt(Element n) {
		String str = n.getTextContent();
		int i = Integer.parseInt(str);
		return i;
	}

	public UProveTokenComposite deserializeToCompositeToken(Element e) {
		org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
		UProveTokenComposite tc = new UProveTokenComposite();
		byte[] bytes = this.getBytes(H, e);
		tc.setH(ofup.createUProveTokenCompositeH(bytes));
		boolean b = this.getBoolean(IS_DEVICE_PROTECTED, e);
		tc.setIsDeviceProtected(b);
		bytes = this.getBytes(PI, e);
		tc.setPI(ofup.createUProveTokenCompositePI(bytes));
		bytes = this.getBytes(SIGMA_C_PRIME, e);
		tc.setSigmaCPrime(ofup.createUProveTokenCompositeSigmaCPrime(bytes));
		bytes = this.getBytes(SIGMA_R_PRIME, e);
		tc.setSigmaRPrime(ofup.createUProveTokenCompositeSigmaRPrime(bytes));
		bytes = this.getBytes(SIGMA_Z_PRIME, e);
		tc.setSigmaZPrime(ofup.createUProveTokenCompositeSigmaZPrime(bytes));
		bytes = this.getBytes(TI, e);
		tc.setTI(ofup.createUProveTokenCompositeTI(bytes));
		bytes = this.getBytes(UIDP, e);
		tc.setUidp(ofup.createUProveTokenCompositeUidp(bytes));
		return tc;
	}

	public IssuerParametersComposite deserializeToIssuerParametersComposite(
			Element e) {
		org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
		IssuerParametersComposite ipc = new IssuerParametersComposite();
		byte[] bytes = this.getBytes(E_NAME, e);
		ipc.setE(ofup.createIssuerParametersCompositeE(bytes));
		ArrayOfbase64Binary base64Binary = this.getBase64Binary(G_NAME, e);
		ipc.setG(ofup.createIssuerParametersCompositeG(base64Binary));
		bytes = this.getBytes(GD, e);
		ipc.setGd(ofup.createIssuerParametersCompositeGd(bytes));
		SubgroupGroupDescriptionComposite subGroup = this.getSubGroup(GQ, e);
		ipc.setGq(ofup.createIssuerParametersCompositeGq(subGroup));
		String string = this.getString(HASH_FUNCTION_OID, e);
		ipc.setHashFunctionOID(ofup
				.createIssuerParametersCompositeHashFunctionOID(string));
		boolean b = this.getBoolean(IS_DEVICE_SUPPORTED, e);
		ipc.setIsDeviceSupported(b);
		bytes = this.getBytes(S, e);
		ipc.setS(ofup.createIssuerParametersCompositeS(bytes));
		string = this.getString(UID_H, e);
		ipc.setUidH(ofup.createIssuerParametersCompositeUidH(string));
		bytes = this.getBytes(UID_P, e);
		ipc.setUidP(ofup.createIssuerParametersCompositeUidP(bytes));
		b = this.getBoolean(USES_RECOMMENDED_PARAMETERS, e);
		ipc.setUsesRecommendedParameters(b);
		return ipc;
	}

	public SubgroupGroupDescriptionComposite getSubGroup(String name, Element e) {
		org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
		SubgroupGroupDescriptionComposite sGroup = new SubgroupGroupDescriptionComposite();

		byte[] p = this.getBytes(P_NAME_SUB, e);
		sGroup.setP(ofup.createSubgroupGroupDescriptionCompositeP(p));
		byte[] q = this.getBytes(Q_NAME_SUB, e);
		sGroup.setQ(ofup.createSubgroupGroupDescriptionCompositeQ(q));
		byte[] g = this.getBytes(G_NAME_SUB, e);
		sGroup.setG(ofup.createSubgroupGroupDescriptionCompositeG(g));
		byte[] gd = this.getBytes(GD_NAME_SUB, e);
		sGroup.setGd(ofup.createSubgroupGroupDescriptionCompositeGd(gd));
		return sGroup;
	}

	private boolean getBoolean(String name, Element e) {
		Element t = this.getElementByName(name, e);
		boolean b = Boolean.parseBoolean(t.getTextContent());
		return b;
	}

	private String getString(String name, Element e) {
		Element t = this.getElementByName(name, e);
		String str = t.getTextContent();
		return str;
	}

	private ArrayOfbase64Binary getBase64Binary(String name, Element e) {
		Element t = this.getElementByName(name, e);
		ArrayOfbase64Binary res = new ArrayOfbase64Binary();
		NodeList nodes = t.getChildNodes();
		for (int inx = 0; inx < nodes.getLength(); inx++) {
			Node n = nodes.item(inx);
			if (n instanceof Element) {
				res.getBase64Binary().add(this.getBytes((Element) n));
			}
		}
		return res;
	}

	public PseudonymComposite deserializeToPseudonymComposite(Element e) {
		org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory ofup = new org.datacontract.schemas._2004._07.abc4trust_uprove.ObjectFactory();
		PseudonymComposite pc = new PseudonymComposite();
		byte[] bytes = this.getBytes(A_NAME, e);
		pc.setA(ofup.createPseudonymCompositeA(bytes));
		bytes = this.getBytes(P_NAME, e);
		pc.setP(ofup.createPseudonymCompositeP(bytes));
		bytes = this.getBytes(R_NAME, e);
		pc.setR(ofup.createPseudonymCompositeR(bytes));
		return pc;
	}

	public IssuerParametersComposite deserializePseudonymEvidenceToIssuerParametersComposite(
			Element uproveEvidence) {
		return this.deserializeToIssuerParametersComposite(this
				.getElementByName(ISSUER_PARAMETERS, uproveEvidence));
	}

	private byte[] getBytes(String name, Element e) {
		Element t = this.getElementByName(name, e);
		return this.getBytes(t);
	}

	public byte[] getBytes(Element t) {
		String str = t.getTextContent();
		byte[] bytes = DatatypeConverter.parseBase64Binary(str);
		return bytes;
	}

	private Element getElementByName(String name, Element e) {
		NodeList elementsByTagName = e.getElementsByTagName(name);
		if (elementsByTagName.getLength() != 1) {
			throw new RuntimeException("Element had: "
					+ elementsByTagName.getLength() + " " + name
					+ " elements, but expected 1");
		}
		Node node = elementsByTagName.item(0);
		return (Element) node;
	}

	public int deserializeToInt(Element keyLengthElement) {
		return this.getInt(keyLengthElement);
	}

}

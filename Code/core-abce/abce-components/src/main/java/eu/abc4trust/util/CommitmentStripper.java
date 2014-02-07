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

package eu.abc4trust.util;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PresentationTokenWithCommitments;

public class CommitmentStripper {

	public static PresentationTokenDescription stripPresentationTokenDescription(PresentationTokenDescriptionWithCommitments ptdwc){
		ObjectFactory of = new ObjectFactory();
		PresentationTokenDescription ret = of.createPresentationTokenDescription();
		ret.setMessage(ptdwc.getMessage());
		ret.setPolicyUID(ptdwc.getPolicyUID());
		ret.setTokenUID(ptdwc.getTokenUID());
		ret.getPseudonym().addAll(ptdwc.getPseudonym());
		ret.getVerifierDrivenRevocation().addAll(ptdwc.getVerifierDrivenRevocation());
		ret.getAttributePredicate().addAll(ptdwc.getAttributePredicate());
		for(CredentialInTokenWithCommitments citwc:ptdwc.getCredential()){
			CredentialInToken cit = of.createCredentialInToken();
			cit.setAlias(citwc.getAlias());
			cit.setCredentialSpecUID(citwc.getCredentialSpecUID());
			cit.setIssuerParametersUID(citwc.getIssuerParametersUID());
			cit.setRevocationInformationUID(citwc.getRevocationInformationUID());
			cit.setSameKeyBindingAs(citwc.getSameKeyBindingAs());
			cit.getDisclosedAttribute().addAll(citwc.getDisclosedAttribute());
			ret.getCredential().add(cit);
		}
		return ret;
	}
	
	public static CredentialInToken stripCredentialInToken(CredentialInTokenWithCommitments citwc){
		ObjectFactory of = new ObjectFactory();
		CredentialInToken cit = of.createCredentialInToken();
		cit.setAlias(citwc.getAlias());
		cit.setCredentialSpecUID(citwc.getCredentialSpecUID());
		cit.setIssuerParametersUID(citwc.getIssuerParametersUID());
		cit.setRevocationInformationUID(citwc.getRevocationInformationUID());
		cit.setSameKeyBindingAs(citwc.getSameKeyBindingAs());
		cit.getDisclosedAttribute().addAll(citwc.getDisclosedAttribute());
		return cit;
	}
	
	public static PresentationTokenDescription stripCommitmentsAndUProveFromPTD(PresentationTokenDescriptionWithCommitments ptdwc, KeyManager keyManager){
		ObjectFactory of = new ObjectFactory();
		PresentationTokenDescription ret = of.createPresentationTokenDescription();
		ret.setMessage(ptdwc.getMessage());
		ret.setPolicyUID(ptdwc.getPolicyUID());
		ret.setTokenUID(ptdwc.getTokenUID());
		ret.getPseudonym().addAll(ptdwc.getPseudonym());
		ret.getVerifierDrivenRevocation().addAll(ptdwc.getVerifierDrivenRevocation());
		ret.getAttributePredicate().addAll(ptdwc.getAttributePredicate());
		for(CredentialInTokenWithCommitments citwc:ptdwc.getCredential()){
			try {
				IssuerParameters issuerParameters = keyManager.getIssuerParameters(citwc.getIssuerParametersUID());
                if(issuerParameters.getAlgorithmID().equals(CryptoUriUtil.getUproveMechanism())) {
					continue;
				}
			} catch (KeyManagerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			CredentialInToken cit = of.createCredentialInToken();
			cit.setAlias(citwc.getAlias());
			cit.setCredentialSpecUID(citwc.getCredentialSpecUID());
			cit.setIssuerParametersUID(citwc.getIssuerParametersUID());
			cit.setRevocationInformationUID(citwc.getRevocationInformationUID());
			cit.setSameKeyBindingAs(citwc.getSameKeyBindingAs());
			cit.getDisclosedAttribute().addAll(citwc.getDisclosedAttribute());
			ret.getCredential().add(cit);
		}
		return ret;
	}
	
	
	public static PresentationToken stripPresentationToken(PresentationTokenWithCommitments ptwc){
		ObjectFactory of = new ObjectFactory();
		PresentationToken pt = of.createPresentationToken();
		pt.setCryptoEvidence(ptwc.getCryptoEvidence());
		pt.setVersion(ptwc.getVersion());
		pt.setPresentationTokenDescription(stripPresentationTokenDescription(ptwc.getPresentationTokenDescriptionWithCommitments()));
		return pt;
	}
}

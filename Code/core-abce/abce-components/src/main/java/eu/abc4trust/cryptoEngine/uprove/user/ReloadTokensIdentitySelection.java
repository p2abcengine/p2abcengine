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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.returnTypes.ui.InspectorInUi;
import eu.abc4trust.returnTypes.ui.PseudonymInUi;
import eu.abc4trust.returnTypes.ui.PseudonymListCandidate;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PseudonymMetadata;

public class ReloadTokensIdentitySelection implements IdentitySelectionUi {
	private final ReloadInformation info;
	private URI EMPTY_URI = null;
	public ReloadTokensIdentitySelection(Credential cred, ReloadInformation info ) {
		this.info = info;
		try {
			EMPTY_URI = new URI("");  //stupid compiler. This will never fail
		} catch (URISyntaxException e) {
		}
	}

	@Override
	public UiPresentationReturn selectPresentationTokenDescription(
			UiPresentationArguments args) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public UiIssuanceReturn selectIssuanceTokenDescription(
			UiIssuanceArguments args) {
		int chosenIssuanceToken = -1;
		int chosenPseudonymList = -1;
		List<String> chosenInspectors = new ArrayList<String>();
		
		//TODO: we should verify cred-spec-uid (just for safe measure)
		
		boolean isTokenCandidateFound = false; //we stop when we have found the right one
		System.out.println("");
		for (TokenCandidate candidate : args.tokenCandidates) {
			chosenIssuanceToken++; //update chosenIssuanceToken to point at current candidate  
			isTokenCandidateFound = true; //assume this is the right one, then verify with tests below.
			//exit this for loop when we find a match or try next candidate if not
			
			//first validate referenced/used credentials
			//since UProve only supports issuance from scratch this list may always be empty?
			List<URI> list = new LinkedList<URI>(info.creduids);
			for (CredentialInUi referencedCredential : candidate.credentials) {
				if (!list.remove(getURI(referencedCredential.uri))) {
					isTokenCandidateFound = false;
					break;
				}
			}
			if (!isTokenCandidateFound || !list.isEmpty())
				continue; //this is not the right tokenCandidate, try next one
			
			//second validate pseudonym list
			chosenPseudonymList=-1;
			for (PseudonymListCandidate pCandidate : candidate.pseudonymCandidates) {
				chosenPseudonymList++;
				list = new LinkedList<URI>(info.pseudonyms);
				for (PseudonymInUi pseudonym : pCandidate.pseudonyms) {
					if (!list.remove(getURI(pseudonym.uri))) {
						isTokenCandidateFound = false;
						break;						
					}
				}
			}
			if (!isTokenCandidateFound || !list.isEmpty())
				continue; //this is not the right tokenCandidate, try next one
			
			//final verify inspectors
			chosenInspectors.clear(); //chosenInspectors should contain the uri (as string) for all selected  
			//inspectors for inspectable attributes. So now we clear previous attempt
			ListIterator<URI> it = info.inspectors.listIterator();
			for (InspectableAttribute attribute : candidate.inspectableAttributes) {
				if (!it.hasNext()) { //there are more inspectable attributes than we have inspectors for. Abort
					isTokenCandidateFound=false;
					break;
				}
				URI expectedInspectorURI = it.next();
				isTokenCandidateFound=false;
				for (InspectorInUi inspector : attribute.inspectorAlternatives) {
					if (getURI(inspector.uri).equals(expectedInspectorURI)) {
						isTokenCandidateFound=true;
						chosenInspectors.add(inspector.uri);
						break;
					}
				}
				if (!isTokenCandidateFound)
					break; //could not find the inspector for this attribute. Abort so we can try the next token candidate				
			}
			if (!isTokenCandidateFound)
				continue; //this is not the right tokenCandidate, try next one
		}
		
		UiIssuanceReturn ret = null;
		
		if (isTokenCandidateFound)
			ret = new UiIssuanceReturn(args.uiContext, chosenIssuanceToken, 
				new HashMap<String, PseudonymMetadata>(), chosenPseudonymList, chosenInspectors);
		
		return ret;
	}
	
	private URI getURI(String uriString) {
		try {
			return new URI(uriString);
		} catch (URISyntaxException e) {
		}
		return EMPTY_URI;
	}
	
}

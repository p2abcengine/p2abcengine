//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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
package eu.abc4trust.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.xml.PseudonymMetadata;

@Path("/identity")
public class IdentitySelectionService {
	
	@Path("/presentation")
	@POST()
	@Produces({MediaType.TEXT_XML})
	public JAXBElement<UiPresentationReturn> selectPresentationTokenDescription(JAXBElement<UiPresentationArguments> presentationArguments) {
		UiPresentationArguments arg = presentationArguments.getValue();
		
		// Choose always the first choice among all the alternatives
		int chosenPolicy = 0;
		int chosenPresentationToken = 0;
		int chosenPseudonymList = 0;
		Map<String, PseudonymMetadata> metadataToChange = new HashMap<String, PseudonymMetadata>();

		List<String> chosenInspectors = new ArrayList<String>();
		TokenCandidatePerPolicy tokenCandidates = arg.tokenCandidatesPerPolicy.get(chosenPolicy); 
		for (InspectableAttribute is : tokenCandidates.tokenCandidates.get(chosenPresentationToken).inspectableAttributes) {
			chosenInspectors.add(is.inspectorAlternatives.get(0).uri);
		}
		UiPresentationReturn presentationReturn = 
				new UiPresentationReturn(arg.uiContext, chosenPolicy, chosenPresentationToken, 
						metadataToChange, chosenPseudonymList, chosenInspectors);
		return ObjectFactoryReturnTypes.wrap(presentationReturn);
	}


	@Path("/issuance")
	@POST()
	@Produces({MediaType.TEXT_XML})
	public JAXBElement<UiIssuanceReturn> selectIssuanceTokenDescription(JAXBElement<IssuanceReturn> issuanceArguments) {
		UiIssuanceArguments arg = issuanceArguments.getValue().uia;
		
		// Choose always the first choice among all the alternatives
		int chosenPresentationToken = 0;
		int chosenPseudonymList = 0;
		Map<String, PseudonymMetadata> metadataToChange = new HashMap<String, PseudonymMetadata>();

		List<String> chosenInspectors = new ArrayList<String>();
		for (InspectableAttribute is : arg.tokenCandidates.get(chosenPresentationToken).inspectableAttributes) {
			chosenInspectors.add(is.inspectorAlternatives.get(0).uri);
		}

		UiIssuanceReturn ret = new UiIssuanceReturn(arg.uiContext, chosenPresentationToken, metadataToChange, chosenPseudonymList,
				chosenInspectors);
		
		return ObjectFactoryReturnTypes.wrap(ret);
	}
}

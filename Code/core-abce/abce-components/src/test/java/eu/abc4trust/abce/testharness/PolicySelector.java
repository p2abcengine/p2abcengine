//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.abce.testharness;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymMetadata;

public class PolicySelector implements IdentitySelection {

    private final int chosenPresentationToken;
    List<URI> chosenInspectors;
    private final int selectedPseudonymNumber;
    private final boolean debug;
    private URI selectedCredential = null;

    public PolicySelector(boolean debug, int selectedPolicyNumber,
            int selectedPseudonymNumber) {
        this.debug = debug;
        this.chosenPresentationToken = selectedPolicyNumber;
        this.chosenInspectors = new LinkedList<URI>();
        this.selectedPseudonymNumber = selectedPseudonymNumber;
    }
    
    public PolicySelector(boolean debug, int selectedPolicyNumber,
                          int selectedPseudonymNumber, URI selectedCredential) {
                      this.debug = debug;
                      this.selectedCredential = selectedCredential;
                      this.chosenPresentationToken = selectedPolicyNumber;
                      this.chosenInspectors = new LinkedList<URI>();
                      this.selectedPseudonymNumber = selectedPseudonymNumber;
                  }

    public PolicySelector(boolean debug, URI selectedCredential,
            int selectedPseudonymNumber) {
        this.debug = debug;
        this.selectedCredential = selectedCredential;
        this.chosenPresentationToken = 0;
        this.chosenInspectors = new LinkedList<URI>();
        this.selectedPseudonymNumber = selectedPseudonymNumber;
    }
    
    public PolicySelector(int selectedPolicyNumber, int selectedPseudonymNumber) {
        this.debug = false;
        this.chosenPresentationToken = selectedPolicyNumber;
        this.chosenInspectors = new LinkedList<URI>();
        this.selectedPseudonymNumber = selectedPseudonymNumber;
    }

    public PolicySelector(int selectedPolicyNumber, List<URI> chosenInspectors,
            int selectedPseudonymNumber) {
        this.debug = false;
        this.chosenPresentationToken = selectedPolicyNumber;
        this.chosenInspectors = chosenInspectors;
        this.selectedPseudonymNumber = selectedPseudonymNumber;
    }
    
    public PolicySelector(int selectedPolicyNumber, List<URI> chosenInspectors,
                          int selectedPseudonymNumber, URI selectedCredential) {
      this.debug = false;
      this.chosenPresentationToken = selectedPolicyNumber;
      this.chosenInspectors = chosenInspectors;
      this.selectedCredential = selectedCredential;
      this.selectedPseudonymNumber = selectedPseudonymNumber;
    }

    public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
            Map<URI, CredentialDescription> credentialDescriptions,
            Map<URI, PseudonymDescription> pseudonyms,
            Map<URI, InspectorDescription> inspectors,
            List<PresentationTokenDescription> tokens,
            List<List<URI>> credentialUids,
            List<Set<List<URI>>> pseudonymChoice,
            List<List<Set<URI>>> inspectorChoice) {

        if (this.debug) {
            System.out.println("### selectPresentationTokenDescription");
            System.out.println("*** - policies               : " + policies.size());
            System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
            System.out.println("*** - pseudonyms             : " + pseudonyms.size());
            System.out.println("*** - inspectors             : " + inspectors.size());
            System.out.println("### - tokens                 : " + tokens.size());
            System.out.println("*** - credentialUids         : " + credentialUids.size());
            System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
            System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());
        }

        Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI, PseudonymMetadata>();

        List<URI> chosenPseudonyms = null;
        Set<List<URI>> pseudonymChoices = pseudonymChoice
                .get(this.chosenPresentationToken);
        for (int inx = 0; inx < (this.selectedPseudonymNumber + 1); inx++) {
            chosenPseudonyms = pseudonymChoices.iterator().next();
        }

        if (this.chosenInspectors.isEmpty()) {
            for (List<Set<URI>> uris : inspectorChoice) {
                for (Set<URI> uriset : uris) {
                    this.chosenInspectors.addAll(uriset);
                }
            }
        }
        // chosenInspectors.addAll(inspectorChoice.get(0).get(0).iterator());

		if(this.selectedCredential != null){
			for(int i = 0; i< credentialUids.size(); i++){
				if(this.selectedCredential.equals(credentialUids.get(i).get(0))) return new SptdReturn(i,
		                metaDataToChange, chosenPseudonyms, this.chosenInspectors);
			}
			throw new RuntimeException("Cannot choose credential, URI does not exist!");
		}
        SptdReturn r = new SptdReturn(this.chosenPresentationToken,
                metaDataToChange, chosenPseudonyms, this.chosenInspectors);
        return r;
    }

    @Override
    public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
            Map<URI, CredentialDescription> credentialDescriptions,
            Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
            List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
            List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
            List<List<Set<URI>>> inspectorChoice) {
        throw new UnsupportedOperationException();
    }

}

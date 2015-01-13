//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.ui.idSelection;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

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
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SelectIssuanceTokenDescription;
import eu.abc4trust.xml.SelectPresentationTokenDescription;


public class IdentitySelectionWrapper implements IdentitySelection {

    public boolean done;

    public SelectIssuanceTokenDescription selectIssuanceTokenDescription;
    public SelectPresentationTokenDescription selectPresentationTokenDescription;


    private SitdReturn issToken;
    public List<Attribute> selfClaimedAttributes;
    public List<IssuanceTokenDescription> issuancetokens;
    public boolean hasIssuanceChoices;
    private boolean issuanceTokenSelected;


    private SptdReturn presToken;
    public Map<URI, CredentialDescription> credentialDescriptions;
    public Map<URI, PseudonymWithMetadata> pseudonyms;
    public List<PresentationTokenDescription> presentationtokens;
    public List<List<URI>> credentialUids;
    public List<List<Set<URI>>> pseudonymChoice;
    public List<List<Set<URI>>> inspectorChoice;
    private boolean hasPresentationChoices;
    private boolean presentationTokenSelected;

    private Exception e;


    @Inject
    public IdentitySelectionWrapper() {
        this.done = false;
        this.presToken  = null;
        this.issToken  = null;
        this.hasPresentationChoices = false;
        this.presentationTokenSelected = false;
        System.out.println("idSelectionWrapper created");
    }


    public void setException(Exception e){
        this.e = e;
    }

    public Exception getException(){
        return this.e;
    }

    public boolean hasPresentationChoices(){
        return this.hasPresentationChoices;
    }

    @Override
    public SptdReturn selectPresentationTokenDescription(
            Map<URI, PolicyDescription> policies,
            Map<URI, CredentialDescription> credentialDescriptions,
            Map<URI, PseudonymDescription> pseudonyms,
            Map<URI, InspectorDescription> inspectors,
            List<PresentationTokenDescription> tokens,
            List<List<URI>> credentialUids, List<Set<List<URI>>> pseudonymChoice,
            List<List<Set<URI>>> inspectorChoice) {
        //  public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
                //      Map<URI, CredentialDescription> credentialDescriptions,
                //      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
        //      List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids,
        //      List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice) {
        System.out.println("selectPresentationTokenDescription called!!!!!!");
        this.credentialDescriptions = credentialDescriptions;
        // TODO(enr): Fix pseudonyms, inspectors and policies
        // this.pseudonyms = pseudonyms;
        this.presentationtokens = tokens;
        this.credentialUids = credentialUids;
        // TODO(enr): Fix this
        //this.pseudonymChoice = pseudonymChoice;
        this.inspectorChoice = inspectorChoice;
        this.hasPresentationChoices = true;


        // Choose always the first choice among all the alternatives
        System.out.println("### selectPresentationTokenDescription");
        System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
        System.out.println("*** - pseudonyms             : " + pseudonyms.size());
        System.out.println("*** - tokens                 : " + tokens.size());
        System.out.println("*** - credentialUids         : " + credentialUids.size());
        if(credentialDescriptions.size()<=4) {
            try {
                this.selectPresentationTokenDescription = new SelectPresentationTokenDescription();
                //	    		selectPresentationTokenDescription.credentialDescriptions = credentialDescriptions;
                //	    		// selectPresentationTokenDescription.pseudonyms = pseudonyms;
                //	    		int ix = 0;
                //	    		for(PresentationTokenDescription token : tokens) {
                //	    			CandidatePresentationToken candidatePresentationToken = new CandidatePresentationToken();
                //	    			candidatePresentationToken.token = token;
                ////	    			candidatePresentationToken.credentialUids.credentialUids = credentialUids.get(ix);
                //	    			// TODO(enr): Fix this
                //	    			//candidatePresentationToken.pseudonymChoice.pseudonymChoice = convertToHashSet(pseudonymChoice.get(ix));
                ////	    			candidatePresentationToken.inspectorChoice.inspectorChoice = convertToHashSet(inspectorChoice.get(ix));
                //
                //	    			selectPresentationTokenDescription.candidatePresentationTokenList.list.add(candidatePresentationToken);
                //	    			ix ++;
                //	    		}
                //
                //	    		String xml = XmlUtils.toXml(new ObjectFactory().createSelectPresentationTokenDescription(selectPresentationTokenDescription), false);
                //	    		System.out.println("selectPresentationTokenDescription : XML : " + xml);

            } catch(Exception e) {
                System.err.println("FAILED TO CREAT JSON");
                e.printStackTrace();
            }
        }

        try{// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been made
            while(!this.presentationTokenSelected) {
                Thread.sleep(200);
            }
        }catch(InterruptedException e){
            System.out.println("idSelection got interrupted");
        }
        // The idSelectionWrapper has got a choice it can return
        this.done = true;
        return this.presToken;
    }

    public void selectPresentationToken(int chosenPresentationToken, Map<URI, PseudonymMetadata> metadataToChange, List<URI> chosenPseudonyms, List<URI> chosenInspectors){
        // The UI gave the idSelectionWrapper some input, time to wake up
        this.presToken = new SptdReturn(chosenPresentationToken, metadataToChange, chosenPseudonyms, chosenInspectors);
        this.presentationTokenSelected = true;
    }


    public boolean hasIssuanceChoices(){
        return this.hasIssuanceChoices;
    }

    @Override
    public SitdReturn selectIssuanceTokenDescription(
            Map<URI, PolicyDescription> policies,
            Map<URI, CredentialDescription> credentialDescriptions,
            Map<URI, PseudonymDescription> pseudonyms,
            Map<URI, InspectorDescription> inspectors,
            List<IssuanceTokenDescription> tokens,
            List<List<URI>> credentialUids,
            List<Attribute> selfClaimedAttributes,
            List<Set<List<URI>>> pseudonymChoice,
            List<List<Set<URI>>> inspectorChoice) {
        //  public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
                //      Map<URI, CredentialDescription> credentialDescriptions,
                //      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
                //      List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
        //      List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
        //      List<List<Set<URI>>> inspectorChoice) {

        System.out.println("ABC engine called selectIssuanceTokenDescription in idSelectionWrapper");
        this.credentialDescriptions = credentialDescriptions;
        // TODO(enr): Fix pseudonyms, inspectors and policies
        // this.pseudonyms = pseudonyms;
        this.issuancetokens = tokens;
        this.credentialUids = credentialUids;
        this.selfClaimedAttributes = selfClaimedAttributes;
        // TODO(enr): Fix this
        //this.pseudonymChoice = pseudonymChoice;
        this.inspectorChoice = inspectorChoice;
        this.hasIssuanceChoices = true;

        if(credentialDescriptions.size()<3) { //why 3?
            try {
                this.selectIssuanceTokenDescription = new SelectIssuanceTokenDescription();
                //  				selectIssuanceTokenDescription.credentialDescriptions = credentialDescriptions;
                //  				// selectIssuanceTokenDescription.pseudonyms = pseudonyms;
                //  				selectIssuanceTokenDescription.selfClaimedAttributes.attributes = selfClaimedAttributes;
                //  				int ix = 0;
                //  				for(IssuanceTokenDescription token : tokens) {
                //  					CandidateIssuanceToken candidateIssuanceToken = new CandidateIssuanceToken();
                //  					candidateIssuanceToken.token = token;
                //  					candidateIssuanceToken.credentialUids.credentialUids = credentialUids.get(ix);
                //  					// TODO(enr): fix this
                //  					//candidateIssuanceToken.pseudonymChoice.pseudonymChoice = convertToHashSet(pseudonymChoice.get(ix));
                //  					//candidateIssuanceToken.inspectorChoice.inspectorChoice = convertToHashSet(inspectorChoice.get(ix));
                //
                //  					selectIssuanceTokenDescription.candidateIssuanceTokenList.list.add(candidateIssuanceToken);
                //  					ix ++;
                //  				}
            } catch(Exception e){
                System.out.println("idSelectionWrapper failed to generate selectIssuanceTokenDescription");
                e.printStackTrace();
            }
        }

        try{// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been made
            while(!this.issuanceTokenSelected) {
                Thread.sleep(200);
            }
        }catch(InterruptedException e){
            System.out.println("idSelection got interrupted");
        }
        // The idSelectionWrapper has got a choice it can return
        this.done = true;
        return this.issToken;
    }

    public void selectIssuanceToken(int chosenIssuanceToken, Map<URI, PseudonymMetadata> metadataToChange, List<URI> chosenPseudonyms, List<URI> chosenInspectors, List<Object> chosenAttributeValues){
        // The UI gave the idSelectionWrapper some input, time to wake up
        this.issToken = new SitdReturn(chosenIssuanceToken, metadataToChange, chosenPseudonyms, chosenInspectors, chosenAttributeValues);
        this.issuanceTokenSelected = true;
    }

    // TODO: remove dead code?
    @SuppressWarnings("unused")
    private List<HashSet<URI>> convertToHashSet(List<Set<URI>> list) {
        System.out.println("convertToHashSet " + list);
        if(list == null) {
            System.out.println("- null");
            return null;
        }
        List<HashSet<URI>> copiedList = new ArrayList<HashSet<URI>>();
        for(Set<URI> set : list) {
            HashSet<URI> new_set = new HashSet<URI>(set);
            copiedList.add(new_set);
        }
        return copiedList;
    }
}
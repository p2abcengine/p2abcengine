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

package eu.abc4trust.ri.servicehelper.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CandidatePresentationToken;
import eu.abc4trust.xml.CandidatePresentationTokenList;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialDescriptions;
import eu.abc4trust.xml.CredentialDescriptionsEntry;
import eu.abc4trust.xml.CredentialUidList;
import eu.abc4trust.xml.InspectorChoiceList;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.InspectorDescriptions;
import eu.abc4trust.xml.InspectorDescriptionsEntry;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PolicyDescriptions;
import eu.abc4trust.xml.PolicyDescriptionsEntry;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymChoiceList;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymDescriptionValue;
import eu.abc4trust.xml.PseudonymDescriptions;
import eu.abc4trust.xml.PseudonymDescriptionsEntry;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.SelectIssuanceTokenDescription;
import eu.abc4trust.xml.SelectPresentationTokenDescription;
import eu.abc4trust.xml.Token;
import eu.abc4trust.xml.URISet;
import eu.abc4trust.xml.util.XmlUtils;

public class JSonIdentitySelection implements IdentitySelection {

    public SelectPresentationTokenDescription selectPresentationTokenDescription;
    //  public SelectPresentationTokenDescriptionReturn selectPresentationTokenDescriptionReturn;

    public SelectIssuanceTokenDescription selectIssuanceTokenDescription;
    //  public SelectIssuanceTokenDescriptionReturn selectIssuanceTokenDescriptionReturn;

    @Inject
    public JSonIdentitySelection() {
        // TODO Auto-generated method stub
        System.out.println("*** JSON identity selection *** DO NOT USE IN PRODUCTION ***");
    }

    private PolicyDescriptions createPolicyDescriptions(Map<URI, PolicyDescription> policies) {
        PolicyDescriptions pd = new PolicyDescriptions();
        for(URI key : policies.keySet()) {
            PolicyDescriptionsEntry entry = new PolicyDescriptionsEntry();
            entry.setKey(key);
            entry.setValue(policies.get(key));
            pd.getEntry().add(entry);
        }
        return pd;
    }
    private CredentialDescriptions createCredentialDescriptions(Map<URI, CredentialDescription> policies) {
        CredentialDescriptions cd = new CredentialDescriptions();
        for(URI key : policies.keySet()) {
            CredentialDescriptionsEntry entry = new CredentialDescriptionsEntry();
            entry.setKey(key);
            entry.setValue(policies.get(key));
            cd.getEntry().add(entry);
        }
        return cd;
    }
    private PseudonymDescriptions createPseudonymDescriptions(Map<URI, PseudonymDescription> policies) {
        PseudonymDescriptions pd = new PseudonymDescriptions();
        for(URI key : policies.keySet()) {
            PseudonymDescriptionValue value = new PseudonymDescriptionValue();
            value.setPseudonymDescription(policies.get(key));
            PseudonymDescriptionsEntry entry = new PseudonymDescriptionsEntry();
            entry.setKey(key);
            entry.setValue(value);
            pd.getEntry().add(entry);
        }
        return pd;
    }
    private InspectorDescriptions createInspectorDescriptions(Map<URI, InspectorDescription> policies) {
        InspectorDescriptions id = new InspectorDescriptions();
        for(URI key : policies.keySet()) {
            InspectorDescriptionsEntry entry = new InspectorDescriptionsEntry();
            entry.setKey(key);
            entry.setValue(policies.get(key));
            id.getEntry().add(entry);
        }
        return id;
    }

    @Override
    public SptdReturn selectPresentationTokenDescription(
            Map<URI, PolicyDescription> policies,
            Map<URI, CredentialDescription> credentialDescriptions,
            Map<URI, PseudonymDescription> pseudonyms,
            Map<URI, InspectorDescription> inspectors,
            List<PresentationTokenDescription> tokens,
            List<List<URI>> credentialUids,
            List<Set<List<URI>>> pseudonymChoice,
            List<List<Set<URI>>> inspectorChoice) {


        System.out.println("### selectPresentationTokenDescription");
        System.out.println("*** - policies               : " + policies.size());
        System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
        System.out.println("*** - pseudonyms             : " + pseudonyms.size());
        System.out.println("*** - inspectors             : " + inspectors.size());
        System.out.println("### - tokens                 : " + tokens.size());
        System.out.println("*** - credentialUids         : " + credentialUids.size());
        System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
        System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());

        try {
            this.selectPresentationTokenDescription = new SelectPresentationTokenDescription();

            //      selectPresentationTokenDescription.setPolicyDescriptions(createPolicyDescriptions(policies));
            //      selectPresentationTokenDescription.setCredentialDescriptions(createCredentialDescriptions(credentialDescriptions));
            //      selectPresentationTokenDescription.setPseudonymDescriptions(createPseudonymDescriptions(pseudonyms));
            //      selectPresentationTokenDescription.setInspectorDescriptions(createInspectorDescriptions(inspectors));
            this.selectPresentationTokenDescription.setPolicyDescriptions(this.createPolicyDescriptions(policies));
            this.selectPresentationTokenDescription.setCredentialDescriptions(this.createCredentialDescriptions(credentialDescriptions));
            this.selectPresentationTokenDescription.setPseudonymDescriptions(this.createPseudonymDescriptions(pseudonyms));
            this.selectPresentationTokenDescription.setInspectorDescriptions(this.createInspectorDescriptions(inspectors));

            CandidatePresentationTokenList cptl = new CandidatePresentationTokenList();
            this.selectPresentationTokenDescription.setCandidatePresentationTokenList(cptl);
            int ix = 0;
            for (PresentationTokenDescription token : tokens) {
                CandidatePresentationToken candidatePresentationToken = new CandidatePresentationToken();

                Token td = new Token();
                td.setPolicyUID(token.getPolicyUID());
                td.setTokenUID(token.getTokenUID());
                candidatePresentationToken.setToken(td );

                PolicyDescription policy = policies.get(token.getPolicyUID());
                candidatePresentationToken.getFriendlyTokenDescription().addAll(policy.getMessage().getFriendlyPolicyDescription());

                CredentialUidList cul = new CredentialUidList();
                cul.getCredentialUid().addAll(credentialUids.get(ix));
                candidatePresentationToken.setCredentialUidList(cul);

                candidatePresentationToken.setPseudonymChoiceList(this.createPseudonymChoiceList(pseudonymChoice.get(ix)));
                candidatePresentationToken.setInspectorChoiceList(this.createInspectorChoiceList(inspectorChoice.get(ix)));

                cptl.getCandidatePresentationToken().add(candidatePresentationToken);
                ix++;
            }
        } catch (Exception e) {
            System.err.println("FAILED TO CREAT JSON");
            e.printStackTrace();
        }


        int chosenPolicyNumber = 0;
        //    Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
        List<URI> chosenPseudonyms = new ArrayList<URI>();
        //    TreeSet<List<URI>> orderedSet = new TreeSet<List<URI>>();
        Set<List<URI>> chosenPseudonym = pseudonymChoice.get(chosenPolicyNumber);
        // orderedSet.addAll(chosenPseudonym);
        if(chosenPseudonym.iterator().hasNext()) {
            chosenPseudonyms = chosenPseudonym.iterator().next(); // orderedSet.first();
        }

        System.out.println("### : chosen pseudonyms " + chosenPseudonyms);
        List<URI> chosenInspectors = new ArrayList<URI>();
        if (chosenInspectors.isEmpty()) {
            for (List<Set<URI>> uris : inspectorChoice) {
                for (Set<URI> uriset : uris) {
                    //          System.out.println("adding: " + uriset);
                    chosenInspectors.addAll(uriset);
                }
            }
        }

        // From Janus' PolicySelector
        Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
        //   List<URI> chosenPseudonyms = new LinkedList<URI>();
        //
        for (Set<List<URI>> urisetlist : pseudonymChoice) {
            for(List<URI> urilist : urisetlist) {
                for(URI pseudonymUri : urilist) {
                    PseudonymDescription pseudonymDescription = pseudonyms.get(pseudonymUri);
                    PseudonymMetadata pseudonymMetadata = pseudonymDescription.getPseudonymMetadata();

                    //            PseudonymMetadata pseudonymMetadata = new PseudonymMetadata();
                    pseudonymMetadata.setHumanReadableData("My HumanReadableData - Will Be Removed (according to comment in schema)");
                    Metadata metadata = new Metadata();
                    pseudonymMetadata.setMetadata(metadata);

                    metadataToChange.put(pseudonymUri, pseudonymMetadata);
                }
            }
        }

        //    selectPresentationTokenDescriptionReturn = new SelectPresentationTokenDescriptionReturn();
        //    selectPresentationTokenDescriptionReturn.chosenPresentationToken = chosenPresentationToken;
        //    selectPresentationTokenDescriptionReturn.metadataToChange = metadataToChange;
        //    selectPresentationTokenDescriptionReturn.chosenPseudonyms = chosenPseudonyms;
        //    selectPresentationTokenDescriptionReturn.chosenInspectors = chosenInspectors;

        return new SptdReturn(chosenPolicyNumber, metadataToChange, chosenPseudonyms,
                chosenInspectors);
    }


    private InspectorChoiceList createInspectorChoiceList(List<Set<URI>> list) {
        InspectorChoiceList icl = new InspectorChoiceList();
        for(Set<URI> set : list) {
            URISet uriSet = new URISet();
            uriSet.getURI().addAll(Arrays.asList(set.toArray(new URI[0])));
            icl.getURISet().add(uriSet);
        }
        return icl;
    }

    private PseudonymChoiceList createPseudonymChoiceList(Set<List<URI>> set) {
        PseudonymChoiceList pcl = new PseudonymChoiceList();

        Iterator<List<URI>> iterator = set.iterator();
        while(iterator.hasNext()) {
            URISet uriSet = new URISet();
            uriSet.getURI().addAll(iterator.next());
            pcl.getURISet().add(uriSet);
        }
        return pcl;
    }

    // TODO: remove dead code?
    @SuppressWarnings("unused")
    private List<HashSet<URI>> convertToHashSet(List<Set<URI>> list) {
        System.out.println("convertToHashSet " + list);
        if (list == null) {
            System.out.println("- null");
            return null;
        }
        List<HashSet<URI>> copiedList = new ArrayList<HashSet<URI>>();
        for (Set<URI> set : list) {
            HashSet<URI> new_set = new HashSet<URI>(set);
            copiedList.add(new_set);
        }
        return copiedList;
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
        System.out.println("=== selectIssuanceTokenDescription");
        System.out.println("*** - policies               : " + policies.size());
        System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
        System.out.println("*** - pseudonyms             : " + pseudonyms.size());
        System.out.println("*** - inspectors             : " + inspectors.size());
        System.out.println("### - tokens                 : " + tokens.size());
        System.out.println("*** - credentialUids         : " + credentialUids.size());
        System.out.println("*** - selfClaimedAttributes  : " + selfClaimedAttributes.size());
        System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
        System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());

        try {
            this.selectIssuanceTokenDescription = new SelectIssuanceTokenDescription();

            this.selectIssuanceTokenDescription.setPolicyDescriptions(this.createPolicyDescriptions(policies));
            this.selectIssuanceTokenDescription.setCredentialDescriptions(this.createCredentialDescriptions(credentialDescriptions));
            this.selectIssuanceTokenDescription.setPseudonymDescriptions(this.createPseudonymDescriptions(pseudonyms));
            this.selectIssuanceTokenDescription.setInspectorDescriptions(this.createInspectorDescriptions(inspectors));

            //      selectIssuanceTokenDescription.credentialDescriptions = credentialDescriptions;
            //      // TODO(enr): Fix pseudonyms, inspectors and policies
            //      // selectIssuanceTokenDescription.pseudonyms = pseudonyms;
            //      selectIssuanceTokenDescription.selfClaimedAttributes.attributes = selfClaimedAttributes;
            //      int ix = 0;
            //      for (IssuanceTokenDescription token : tokens) {
            //        CandidateIssuanceToken candidateIssuanceToken = new CandidateIssuanceToken();
            //        candidateIssuanceToken.token = token;
            //        candidateIssuanceToken.credentialUids.credentialUids = credentialUids.get(ix);
            //        // TODO(enr): Fix this
            //        // candidateIssuanceToken.pseudonymChoice.pseudonymChoice =
            //        // convertToHashSet(pseudonymChoice.get(ix));
            //        // candidateIssuanceToken.inspectorChoice.inspectorChoice =
            //        // convertToHashSet(inspectorChoice.get(ix));
            //
            //        selectIssuanceTokenDescription.candidateIssuanceTokenList.list
            //            .add(candidateIssuanceToken);
            //        ix++;
            //      }
            String xml =
                    XmlUtils.toXml(new ObjectFactory()
                    .createSelectIssuanceTokenDescription(this.selectIssuanceTokenDescription), false);
            System.out.println("selectIssuanceTokenDescription : XML : " + xml);

        } catch (Exception e) {
            System.err.println("FAILED TO CREAT JSON");
            e.printStackTrace();
        }



        // Choose always the first choice among all the alternatives
        int chosenIssuanceToken = 0;
        Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
        List<URI> chosenPseudonyms = new ArrayList<URI>();
        // TODO(enr): Fix this
        /*
         * for (Set<URI> ps : pseudonymChoice.get(chosenIssuanceToken)) { // Note: the code below
         * guarantees that we chose the first pseudonym alphabetically // for repeatability (HashSet
         * don't guarantee any order). TreeSet<URI> orderedSet = new TreeSet<URI>();
         * orderedSet.addAll(ps); URI first = orderedSet.first(); chosenPseudonyms.add(first);
         * metadataToChange.put(first, new PseudonymMetadata()); }
         */
        List<URI> chosenInspectors = new ArrayList<URI>();
        for (Set<URI> is : inspectorChoice.get(chosenIssuanceToken)) {
            // Note: the code below guarantees that we chose the first inspector alphabetically
            // for repeatability (HashSet don't guarantee any order).
            TreeSet<URI> orderedSet = new TreeSet<URI>();
            orderedSet.addAll(is);
            chosenInspectors.add(orderedSet.first());
        }
        List<Object> chosenAttributeValues = new ArrayList<Object>();
        for (Attribute a : selfClaimedAttributes) {
            chosenAttributeValues.add(a.getAttributeValue());
        }

        //    selectIssuanceTokenDescriptionReturn = new SelectIssuanceTokenDescriptionReturn();
        //    selectIssuanceTokenDescriptionReturn.chosenIssuanceToken = chosenIssuanceToken;
        //    selectIssuanceTokenDescriptionReturn.metadataToChange = metadataToChange;
        //    selectIssuanceTokenDescriptionReturn.chosenPseudonyms = chosenPseudonyms;
        //    selectIssuanceTokenDescriptionReturn.chosenInspectors = chosenInspectors;
        //    selectIssuanceTokenDescriptionReturn.chosenAttributeValues = chosenAttributeValues;

        return new SitdReturn(chosenIssuanceToken, metadataToChange, chosenPseudonyms,
                chosenInspectors, chosenAttributeValues);
    }


}

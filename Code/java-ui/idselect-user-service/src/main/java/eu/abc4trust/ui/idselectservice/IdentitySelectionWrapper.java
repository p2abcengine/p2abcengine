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

package eu.abc4trust.ui.idselectservice;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CandidateIssuanceToken;
import eu.abc4trust.xml.CandidateIssuanceTokenList;
import eu.abc4trust.xml.CandidatePresentationToken;
import eu.abc4trust.xml.CandidatePresentationTokenList;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialDescriptions;
import eu.abc4trust.xml.CredentialDescriptionsEntry;
import eu.abc4trust.xml.CredentialUidList;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorChoiceList;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.InspectorDescriptions;
import eu.abc4trust.xml.InspectorDescriptionsEntry;
import eu.abc4trust.xml.IssuanceTokenDescription;
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

public class IdentitySelectionWrapper implements IdentitySelection {

    public boolean done;
    public boolean canceled = false;

    public SelectIssuanceTokenDescription selectIssuanceTokenDescription;
    public SelectPresentationTokenDescription selectPresentationTokenDescription;


    private SitdReturn issToken;
    public List<Attribute> selfClaimedAttributes;
    public List<IssuanceTokenDescription> issuancetokens;
    public boolean hasIssuanceChoices;
    private boolean issuanceTokenSelected;


    private SptdReturn presToken;
    public Map<URI, PolicyDescription> policies;
    public Map<URI, CredentialDescription> credentialDescriptions;
    public Map<URI, PseudonymDescription> pseudonyms;
    public Map<URI, InspectorDescription> inspectors;
    //    public List<IssuanceTokenDescription> tokens;
    public List<PresentationTokenDescription> presentationtokens;
    public List<List<URI>> credentialUids;
    public List<Set<List<URI>>> pseudonymChoice;
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

    public boolean hasIssuanceChoices(){
        return this.hasIssuanceChoices;
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

        System.out.println("selectPresentationTokenDescription called!!!!!!");

        this.policies = policies;
        this.credentialDescriptions = credentialDescriptions;
        // TODO(enr): Fix pseudonyms, inspectors and policies
        this.pseudonyms = pseudonyms;
        this.inspectors = inspectors;
        this.presentationtokens = tokens;
        this.credentialUids = credentialUids;
        // TODO(enr): Fix this
        this.pseudonymChoice = pseudonymChoice;
        this.inspectorChoice = inspectorChoice;
        this.hasPresentationChoices = true;

        try {
            this.selectPresentationTokenDescription = new SelectPresentationTokenDescription();

            this.selectPresentationTokenDescription.setPolicyDescriptions(this.createPolicyDescriptions(policies));
            this.selectPresentationTokenDescription.setCredentialDescriptions(this.createCredentialDescriptions(credentialDescriptions));
            this.selectPresentationTokenDescription.setPseudonymDescriptions(this.createPseudonymDescriptions(pseudonyms));
            this.selectPresentationTokenDescription.setInspectorDescriptions(this.createInspectorDescriptions(inspectors));
            this.selectPresentationTokenDescription.setCandidatePresentationTokenList(this.createCandidatePresentationTokenList(tokens, credentialUids, pseudonymChoice, inspectorChoice, credentialDescriptions));

        } catch(Exception e) {
            System.err.println("FAILED TO CREAT JSON");
            e.printStackTrace();
        }

        try{// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been made
            System.out.println(">> idSelectionwrapper going to sleep, waiting for a choice made by the user");
            while(!this.presentationTokenSelected) {
                Thread.sleep(200);
            }
        }catch(InterruptedException e){
            System.out.println("idSelection got interrupted");
        }

        if(this.canceled) {
            // TODO: Throw Exception
        }
        System.out.println(">> idSelectionwrapper got a choice from the user and is now done");
        // The idSelectionWrapper has got a choice it can return
        // NOT HERE - mark done in UserService!
        // done = true;
        return this.presToken;
    }

    public void selectPresentationToken(int chosenPresentationToken, Map<URI, PseudonymMetadata> metadataToChange, List<URI> chosenPseudonyms, List<URI> chosenInspectors){
        // The UI gave the idSelectionWrapper some input, time to wake up
        this.presToken = new SptdReturn(chosenPresentationToken, metadataToChange, chosenPseudonyms, chosenInspectors);
        this.presentationTokenSelected = true;
        if(chosenPresentationToken==-1) {
            this.canceled = true;
        }
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
            List<List<Set<URI>>> inspectorChoice){

        System.out.println("ABC engine called selectIssuanceTokenDescription in idSelectionWrapper");
/*        
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
*/        
        
        this.policies = policies;

        this.credentialDescriptions = credentialDescriptions;
        this.pseudonyms = pseudonyms;
        this.inspectors = inspectors;
        this.issuancetokens = tokens;
        this.credentialUids = credentialUids;
        this.selfClaimedAttributes = selfClaimedAttributes;

        this.pseudonymChoice = pseudonymChoice;
        this.inspectorChoice = inspectorChoice;
        this.hasIssuanceChoices = true;


        try {
            this.selectIssuanceTokenDescription = new SelectIssuanceTokenDescription();
            this.selectIssuanceTokenDescription.setPolicyDescriptions(this.createPolicyDescriptions(policies));
            this.selectIssuanceTokenDescription.setCredentialDescriptions(this.createCredentialDescriptions(credentialDescriptions));
            this.selectIssuanceTokenDescription.setPseudonymDescriptions(this.createPseudonymDescriptions(pseudonyms));
            this.selectIssuanceTokenDescription.setInspectorDescriptions(this.createInspectorDescriptions(inspectors));
            this.selectIssuanceTokenDescription.setCandidateIssuanceTokenList(this.createCandidateIssuanceTokenList(tokens, credentialUids, pseudonymChoice, inspectorChoice, credentialDescriptions));
//            this.selectPresentationTokenDescription.setCandidatePresentationTokenList(this.createCandidatePresentationTokenList(tokens, credentialUids, pseudonymChoice, inspectorChoice, credentialDescriptions));

        } catch(Exception e){
            System.out.println("idSelectionWrapper failed to generate selectIssuanceTokenDescription");
            e.printStackTrace();
        }
        try{// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been made
            System.out.println("idselectionwrapper/issuance going to sleep");
            while(!this.issuanceTokenSelected) {
                Thread.sleep(200);
            }
        }catch(InterruptedException e){
            System.out.println("idSelection/issuance got interrupted");
        }
        System.out.println("idselectionwrapper/issuance waking up!");
        if(this.canceled) {
            // TODO: Throw Exception
        }
        // The idSelectionWrapper has got a choice it can return
        // NOT HERE - mark done in UserService!
        // done = true;
        return this.issToken;
    }

    public void selectIssuanceToken(int chosenIssuanceToken, Map<URI, PseudonymMetadata> metadataToChange, List<URI> chosenPseudonyms, List<URI> chosenInspectors, List<Object> chosenAttributeValues){
        // The UI gave the idSelectionWrapper some input, time to wake up
        this.issToken = new SitdReturn(chosenIssuanceToken, metadataToChange, chosenPseudonyms, chosenInspectors, chosenAttributeValues);
        this.issuanceTokenSelected = true;
        if(chosenIssuanceToken==-1) {
            this.canceled = true;
        }

    }

    @SuppressWarnings("unused")
    private InspectorChoiceList createInspectorChoiceList(List<Set<URI>> list) {
        InspectorChoiceList icl = new InspectorChoiceList();
        for(Set<URI> set : list) {
            URISet uriSet = new URISet();
            uriSet.getURI().addAll(Arrays.asList(set.toArray(new URI[0])));
            icl.getURISet().add(uriSet);
        }
        return icl;
    }

    @SuppressWarnings("unused")
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

    private PolicyDescriptions createPolicyDescriptions(Map<URI, PolicyDescription> policies) {
        System.out.println("setting policydescriptions # of policies: "+policies.size());
        PolicyDescriptions pd = new PolicyDescriptions();
        for(URI key : policies.keySet()) {
            PolicyDescriptionsEntry entry = new PolicyDescriptionsEntry();
            entry.setKey(key);
            //	      FriendlyDescription description = new FriendlyDescription();
            //	      description.setValue("friendly descNAME value");
            //	      description.setLang("en");
            //	      policies.get(key).getMessage().getFriendlyPolicyName().add(description);
            //	      description.setValue("friendly descDESCRIPTION value");
            //	      description.setLang("en");
            //	      policies.get(key).getMessage().getFriendlyPolicyDescription().add(description);

            entry.setValue(policies.get(key));
            pd.getEntry().add(entry);

            // TODO Demo purpose only, to ensure atleast 2 policydescriptions
            /*      entry = new PolicyDescriptionsEntry();
	      entry.setKey(key);
	      entry.setValue(policies.get(key));
	      pd.getEntry().add(entry);
             */
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

    private CandidatePresentationTokenList createCandidatePresentationTokenList(List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids, List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice, Map<URI, CredentialDescription> credentialDescriptions){
        CandidatePresentationTokenList cptl = new CandidatePresentationTokenList();
        for(int i= 0; i< tokens.size(); i++){

            PresentationTokenDescription desc = tokens.get(i);
            List<URI> credentialUidsList = credentialUids.get(i);
            Set<List<URI>> pseudonymChoices = pseudonymChoice.get(i);
            List<Set<URI>> inspectorChoices = inspectorChoice.get(i);

            CandidatePresentationToken cpt = new CandidatePresentationToken();
            Token token = new Token();
            token.setPolicyUID(desc.getPolicyUID());
            token.setTokenUID(desc.getTokenUID());
            cpt.setToken(token);

            CredentialUidList cul = new CredentialUidList();
            for(URI credURI: credentialUidsList){
                cul.getCredentialUid().add(credURI);
                //			  for(FriendlyDescription fd: credentialDescriptions.get(credURI).getFriendlyCredentialName()){
                //				  System.out.println("adding friendly description: "+fd);
                //				cpt.getFriendlyTokenDescription().add(fd);
                //			  }
            }
            cpt.setCredentialUidList(cul);
            /*System.out.println("checking the friendly token names:S "+desc.getMessage().getFriendlyPolicyName());
		  cpt.getFriendlyTokenDescription().add(null);
		  for(FriendlyDescription d: desc.getMessage().getFriendlyPolicyDescription()){
			  System.out.println("d: "+d);
			  cpt.getFriendlyTokenDescription().add(d);
		  }
             */

            //		   Create and add a mock FriendlyDescription
            FriendlyDescription fDesc_en = new FriendlyDescription();
            fDesc_en.setValue("Mock FriendlyDescription");
            fDesc_en.setLang("en");
            cpt.getFriendlyTokenDescription().add(fDesc_en);

            FriendlyDescription fDesc_dk = new FriendlyDescription();
            fDesc_dk.setValue("Mock Venlig Beskrivelse");
            fDesc_dk.setLang("da");
            cpt.getFriendlyTokenDescription().add(fDesc_dk);

            PseudonymChoiceList pcl = new PseudonymChoiceList();
            for(List<URI> uriset: pseudonymChoices){
                URISet uris = new URISet();
                for(URI uri: uriset){
                    uris.getURI().add(uri);
                }
                pcl.getURISet().add(uris);
            }
            cpt.setPseudonymChoiceList(pcl);

            InspectorChoiceList icl = new InspectorChoiceList();
            for(Set<URI> inspectors: inspectorChoices){
                URISet uriset = new URISet();
                for(URI ins: inspectors){
                    uriset.getURI().add(ins);
                }
                icl.getURISet().add(uriset);
            }
            cpt.setInspectorChoiceList(icl);

            cptl.getCandidatePresentationToken().add(cpt);
        }
        return cptl;
    }


    private CandidateIssuanceTokenList createCandidateIssuanceTokenList(List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids, List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice, Map<URI, CredentialDescription> credentialDescriptions){
      CandidateIssuanceTokenList citl = new CandidateIssuanceTokenList();
      for(int i= 0; i< tokens.size(); i++){

          IssuanceTokenDescription issuance_desc = tokens.get(i);
//          try {
//            System.out.println("XML IssuanceTokenDescription : " + XmlUtils.toXml(new ObjectFactory().createIssuanceTokenDescriptioncreateIssuanceTokenDescription(issuance_desc)));
//          } catch(Exception e) {
//            
//          }
          PresentationTokenDescription presentation_desc = issuance_desc.getPresentationTokenDescription();
          List<URI> credentialUidsList = credentialUids.get(i);
          Set<List<URI>> pseudonymChoices = pseudonymChoice.get(i);
          List<Set<URI>> inspectorChoices = inspectorChoice.get(i);

          // TODO : handle
          // issuance_desc.getCredentialTemplate()

          CandidateIssuanceToken cit = new CandidateIssuanceToken();
          Token token = new Token();
          token.setPolicyUID(presentation_desc.getPolicyUID());
          token.setTokenUID(presentation_desc.getTokenUID());
          cit.setToken(token);

          CredentialUidList cul = new CredentialUidList();
          for(URI credURI: credentialUidsList){
              cul.getCredentialUid().add(credURI);
              //            for(FriendlyDescription fd: credentialDescriptions.get(credURI).getFriendlyCredentialName()){
              //                System.out.println("adding friendly description: "+fd);
              //              cpt.getFriendlyTokenDescription().add(fd);
              //            }
          }
          cit.setCredentialUidList(cul);
          /*System.out.println("checking the friendly token names:S "+desc.getMessage().getFriendlyPolicyName());
        cpt.getFriendlyTokenDescription().add(null);
        for(FriendlyDescription d: desc.getMessage().getFriendlyPolicyDescription()){
            System.out.println("d: "+d);
            cpt.getFriendlyTokenDescription().add(d);
        }
           */

          //TODO : Take description from presentation policy...
          
          //         Create and add a mock FriendlyDescription
          FriendlyDescription fDesc_en = new FriendlyDescription();
          fDesc_en.setValue("Mock FriendlyDescription CandidateIssuanceToken");
          fDesc_en.setLang("en");
          cit.getFriendlyTokenDescription().add(fDesc_en);

          FriendlyDescription fDesc_dk = new FriendlyDescription();
          fDesc_dk.setValue("Mock Venlig Beskrivelse CandidateIssuanceToken");
          fDesc_dk.setLang("da");
          cit.getFriendlyTokenDescription().add(fDesc_dk);

          PseudonymChoiceList pcl = new PseudonymChoiceList();
          for(List<URI> uriset: pseudonymChoices){
              URISet uris = new URISet();
              for(URI uri: uriset){
                  uris.getURI().add(uri);
              }
              pcl.getURISet().add(uris);
          }
          cit.setPseudonymChoiceList(pcl);

          InspectorChoiceList icl = new InspectorChoiceList();
          for(Set<URI> inspectors: inspectorChoices){
              URISet uriset = new URISet();
              for(URI ins: inspectors){
                  uriset.getURI().add(ins);
              }
              icl.getURISet().add(uriset);
          }
          cit.setInspectorChoiceList(icl);

          citl.getCandidateIssuanceToken().add(cit);
      }
      return citl;
  }

/*    
    //TODO: broken atm - uses some mock values
    @SuppressWarnings("unused")
    private CandidateIssuanceTokenList createCandidateIssuanceTokenList(List<IssuanceTokenDescription> tokens){
        CandidateIssuanceTokenList citl = new CandidateIssuanceTokenList();
        for(IssuanceTokenDescription desc: tokens){
            CandidateIssuanceToken token = new CandidateIssuanceToken();
            //		  token.set
            citl.getCandidateIssuanceToken().add(token);
        }
        return citl;
    }
*/
}
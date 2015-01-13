//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.ui.idSelection;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
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
import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymMetadata;

public class IdentitySelectionUiConverter implements IdentitySelectionUi {

  private final IdentitySelection is;
  
  @Inject
  public IdentitySelectionUiConverter(IdentitySelection is) {
    this.is = is;
  }
  
  @Override
  public UiPresentationReturn selectPresentationTokenDescription(UiPresentationArguments args) {
    
    SptdReturn ret;
    {
      Map<URI, PolicyDescription> policies = new HashMap<URI, PolicyDescription>();
      for(TokenCandidatePerPolicy p: args.tokenCandidatesPerPolicy) {
        URI key = p.policy.getPolicyUID();
        PolicyDescription pd = new PolicyDescription();
        pd.setPolicyUID(key);
        pd.setMessage(p.policy.getMessage());
        policies.put(key, pd);
      }
      
      Map<URI, CredentialDescription> credentialDescriptions = new HashMap<URI, CredentialDescription>();
      for(CredentialInUi cd: args.data.credentials) {
        URI key = cd.desc.getCredentialUID();
        credentialDescriptions.put(key, cd.desc);
      }
      
      Map<URI, PseudonymDescription> pseudonyms = new HashMap<URI, PseudonymDescription>();
      for(PseudonymInUi p: args.data.pseudonyms) {
        URI key = p.pseudonym.getPseudonymUID();
        PseudonymDescription psd = new PseudonymDescription();
        psd.setExclusive(p.pseudonym.isExclusive());
        psd.setPseudonymMetadata(p.metadata);
        psd.setPseudonymUID(key);
        psd.setScope(p.pseudonym.getScope());
        pseudonyms.put(key, psd);
      }
      
      Map<URI, InspectorDescription> inspectors = new HashMap<URI, InspectorDescription>();
      for(InspectorInUi i: args.data.inspectors) {
        URI key = URI.create(i.uri);
        InspectorDescription id = new InspectorDescription();
        id.setInspectorUID(key);
        for(FriendlyDescription fd: i.description) {
          id.getFriendlyInspectorDescription().add(fd);
        }
        inspectors.put(key, id);
      }
      
      List<PresentationTokenDescription> tokens = new ArrayList<PresentationTokenDescription>();
      List<List<URI>> credentialUids = new ArrayList<List<URI>>();
      List<Set<List<URI>>> pseudonymChoice = new ArrayList<Set<List<URI>>>();
      List<List<Set<URI>>> inspectorChoice = new ArrayList<List<Set<URI>>>();
      for(TokenCandidatePerPolicy p: args.tokenCandidatesPerPolicy) {
        for(TokenCandidate c: p.tokenCandidates) {

          List<URI> credentialList = new ArrayList<URI>();
          Set<List<URI> > pseudonymSet = new HashSet<List<URI>>();
          List<Set<URI> > inspectorList = new ArrayList<Set<URI>>();
          
          for(CredentialInUi ciu: c.credentials) {
            credentialList.add(ciu.desc.getCredentialUID());
          }
          
          for(PseudonymListCandidate plc: c.pseudonymCandidates) {
            List<URI> psList = new ArrayList<URI>();
            for(PseudonymInUi piu: plc.pseudonyms) {
              psList.add(piu.pseudonym.getPseudonymUID());
            }
            pseudonymSet.add(psList);
          }
          
          for(InspectableAttribute ia: c.inspectableAttributes) {
            Set<URI> ins = new HashSet<URI>();
            for(InspectorInUi iiu: ia.inspectorAlternatives) {
              ins.add(URI.create(iiu.uri));
            }
            inspectorList.add(ins);
          }
          
          tokens.add(c.tokenDescription);
          credentialUids.add(credentialList);
          pseudonymChoice.add(pseudonymSet);
          inspectorChoice.add(inspectorList);
        }
      }
      
      ret = is.selectPresentationTokenDescription(policies, credentialDescriptions,
        pseudonyms, inspectors, tokens, credentialUids, pseudonymChoice, inspectorChoice);
    }
    {
      int i = ret.chosenPresentationToken;
      int policy = 0;
      int token = 0;
      for(TokenCandidatePerPolicy tc: args.tokenCandidatesPerPolicy) {
        if(i >= tc.tokenCandidates.size()) {
          i -= tc.tokenCandidates.size();
          policy++;
        } else {
          token = i;
          break;
        }
      }
      int chosenPsNymList = 0;
      
      for(PseudonymListCandidate plc: args.tokenCandidatesPerPolicy.get(policy).tokenCandidates.get(token).pseudonymCandidates) {
        if(isEqual(plc, ret.chosenPseudonyms)) {
          chosenPsNymList = plc.candidateId;
          break;
        }
      }
      Map<String, PseudonymMetadata> mtc = new HashMap<String, PseudonymMetadata>();
      for(URI key: ret.metadataToChange.keySet()) {
        String s = key.toString();
        mtc.put(s, ret.metadataToChange.get(key));
      }
      List<String> ci = new ArrayList<String>();
      for(URI key: ret.chosenInspectors) {
        ci.add(key.toString());
      }
      
      return new UiPresentationReturn(args.uiContext, policy, token, mtc, chosenPsNymList, ci);
    }
  }

  private boolean isEqual(PseudonymListCandidate plc, List<URI> chosenPseudonyms) { 
    Iterator<PseudonymInUi> plci = plc.pseudonyms.iterator();
    for(URI u: chosenPseudonyms) {
      String s = plci.next().uri;
      if(! u.toString().equals(s)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public UiIssuanceReturn selectIssuanceTokenDescription(UiIssuanceArguments args) {
    
    //TODO(enr): fill out
    SitdReturn ret;
    {
      //List<Attribute> selfClaimedAttributes = args.selfClaimedAttributes;
      List<Attribute> selfClaimedAttributes = new ArrayList<Attribute>();
      
      Map<URI, PolicyDescription> policies = new HashMap<URI, PolicyDescription>();
      {
        URI key = args.policy.getPresentationPolicy().getPolicyUID();
        PolicyDescription pd = new PolicyDescription();
        pd.setPolicyUID(key);
        pd.setMessage(args.policy.getPresentationPolicy().getMessage());
        policies.put(key, pd);
      }
      
      Map<URI, CredentialDescription> credentialDescriptions = new HashMap<URI, CredentialDescription>();
      for(CredentialInUi cd: args.data.credentials) {
        URI key = cd.desc.getCredentialUID();
        credentialDescriptions.put(key, cd.desc);
      }
      
      Map<URI, PseudonymDescription> pseudonyms = new HashMap<URI, PseudonymDescription>();
      for(PseudonymInUi p: args.data.pseudonyms) {
        URI key = p.pseudonym.getPseudonymUID();
        PseudonymDescription psd = new PseudonymDescription();
        psd.setExclusive(p.pseudonym.isExclusive());
        psd.setPseudonymMetadata(p.metadata);
        psd.setPseudonymUID(key);
        psd.setScope(p.pseudonym.getScope());
        pseudonyms.put(key, psd);
      }
      
      Map<URI, InspectorDescription> inspectors = new HashMap<URI, InspectorDescription>();
      for(InspectorInUi i: args.data.inspectors) {
        URI key = URI.create(i.uri);
        InspectorDescription id = new InspectorDescription();
        id.setInspectorUID(key);
        for(FriendlyDescription fd: i.description) {
          id.getFriendlyInspectorDescription().add(fd);
        }
        inspectors.put(key, id);
      }
      
      List<IssuanceTokenDescription> tokens = new ArrayList<IssuanceTokenDescription>();
      List<List<URI>> credentialUids = new ArrayList<List<URI>>();
      List<Set<List<URI>>> pseudonymChoice = new ArrayList<Set<List<URI>>>();
      List<List<Set<URI>>> inspectorChoice = new ArrayList<List<Set<URI>>>();
      {
        for(TokenCandidate c: args.tokenCandidates) {

          List<URI> credentialList = new ArrayList<URI>();
          Set<List<URI> > pseudonymSet = new HashSet<List<URI>>();
          List<Set<URI> > inspectorList = new ArrayList<Set<URI>>();
          
          for(CredentialInUi ciu: c.credentials) {
            credentialList.add(ciu.desc.getCredentialUID());
          }
          
          for(PseudonymListCandidate plc: c.pseudonymCandidates) {
            List<URI> psList = new ArrayList<URI>();
            for(PseudonymInUi piu: plc.pseudonyms) {
              psList.add(piu.pseudonym.getPseudonymUID());
            }
            pseudonymSet.add(psList);
          }
          
          for(InspectableAttribute ia: c.inspectableAttributes) {
            Set<URI> ins = new HashSet<URI>();
            for(InspectorInUi iiu: ia.inspectorAlternatives) {
              ins.add(URI.create(iiu.uri));
            }
            inspectorList.add(ins);
          }
          
          IssuanceTokenDescription itd = new IssuanceTokenDescription();
          itd.setCredentialTemplate(args.policy.getCredentialTemplate());
          itd.setPresentationTokenDescription(c.tokenDescription);
          tokens.add(itd);
          credentialUids.add(credentialList);
          pseudonymChoice.add(pseudonymSet);
          inspectorChoice.add(inspectorList);
        }
      }   
      
      ret = is.selectIssuanceTokenDescription(policies, credentialDescriptions, pseudonyms,
        inspectors, tokens, credentialUids, selfClaimedAttributes, pseudonymChoice, inspectorChoice);
    }
    {
      int token = ret.chosenIssuanceToken;
      int chosenPsNymList = 0;
      
      for(PseudonymListCandidate plc: args.tokenCandidates.get(token).pseudonymCandidates) {
        if(isEqual(plc, ret.chosenPseudonyms)) {
          chosenPsNymList = plc.candidateId;
          break;
        }
      }
      
      Map<String, PseudonymMetadata> mtc = new HashMap<String, PseudonymMetadata>();
      for(URI key: ret.metadataToChange.keySet()) {
        String s = key.toString();
        mtc.put(s, ret.metadataToChange.get(key));
      }
      List<String> ci = new ArrayList<String>();
      for(URI key: ret.chosenInspectors) {
        ci.add(key.toString());
      }
      
      return new UiIssuanceReturn(args.uiContext, token, mtc, chosenPsNymList, ci/*, ret.chosenAttributeValues*/);
    }
  }

}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.xml.PseudonymMetadata;

public class MockIdentitySelectionUi implements IdentitySelectionUi {

  public MockIdentitySelectionUi() {
    System.out.println("*** Using mock identity selection *** DO NOT USE IN PRODUCTION ***");
  }
  
  @Override
  public UiPresentationReturn selectPresentationTokenDescription(UiPresentationArguments arg) {
    // Choose always the first choice among all the alternatives
    int chosenPolicy = 0;
    int chosenPresentationToken = 0;
    int chosenPseudonymList = 0;
    Map<String, PseudonymMetadata> metadataToChange = new HashMap<String, PseudonymMetadata>();

    List<String> chosenInspectors = new ArrayList<String>();
    for (InspectableAttribute is : arg.tokenCandidatesPerPolicy.get(chosenPolicy).tokenCandidates.get(chosenPresentationToken).inspectableAttributes) {
      chosenInspectors.add(is.inspectorAlternatives.get(0).uri);
    }
    return new UiPresentationReturn(arg.uiContext, chosenPolicy, chosenPresentationToken, metadataToChange, chosenPseudonymList, chosenInspectors);
  }

  @Override
  public UiIssuanceReturn selectIssuanceTokenDescription(UiIssuanceArguments arg) {
    // Choose always the first choice among all the alternatives
    int chosenPresentationToken = 0;
    int chosenPseudonymList = 0;
    Map<String, PseudonymMetadata> metadataToChange = new HashMap<String, PseudonymMetadata>();

    List<String> chosenInspectors = new ArrayList<String>();
    for (InspectableAttribute is : arg.tokenCandidates.get(chosenPresentationToken).inspectableAttributes) {
      chosenInspectors.add(is.inspectorAlternatives.get(0).uri);
    }
    
    /*List<Object> chosenAttributeValues = new ArrayList<Object>();
    for (Attribute a : arg.selfClaimedAttributes) {
      chosenAttributeValues.add(a.getAttributeValue());
    }*/
    return new UiIssuanceReturn(arg.uiContext, chosenPresentationToken, metadataToChange, chosenPseudonymList,
        chosenInspectors/*, chosenAttributeValues*/);
  }
  
  /*
   *   @Inject
  public MockIdentitySelectionXml() {
    // TODO Auto-generated method stub
    System.out.println("*** Using mock identity selection *** DO NOT USE IN PRODUCTION ***");
  }

  @Override
  public SptdReturn selectPresentationTokenDescription(SptdArguments arg) {
    // Choose always the first choice among all the alternatives
    int chosenPresentationToken = 0;
    Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
    List<URI> chosenPseudonyms;
    {
      TreeSet<List<URI>> orderedSet = new TreeSet<List<URI>>();
      orderedSet.addAll(arg.pseudonymChoice.get(chosenPresentationToken));
      chosenPseudonyms = orderedSet.first();
    }

    List<URI> chosenInspectors = new ArrayList<URI>();
    for (Set<URI> is : arg.inspectorChoice.get(chosenPresentationToken)) {
      // Note: the code below guarantees that we chose the first inspector alphabetically
      // for repeatability (HashSet don't guarantee any order).
      TreeSet<URI> orderedSet = new TreeSet<URI>();
      orderedSet.addAll(is);
      chosenInspectors.add(orderedSet.first());
    }
    return new SptdReturn(chosenPresentationToken, metadataToChange, chosenPseudonyms,
        chosenInspectors);
  }

  @Override
  public SitdReturn selectIssuanceTokenDescription(SitdArguments arg) {
    // Choose always the first choice among all the alternatives
    int chosenPresentationToken = 0;
    Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
    List<URI> chosenPseudonyms;
    {
      TreeSet<List<URI>> orderedSet = new TreeSet<List<URI>>();
      orderedSet.addAll(arg.pseudonymChoice.get(chosenPresentationToken));
      chosenPseudonyms = orderedSet.first();
    }
    List<URI> chosenInspectors = new ArrayList<URI>();
    for (Set<URI> is : arg.inspectorChoice.get(chosenPresentationToken)) {
      // Note: the code below guarantees that we chose the first inspector alphabetically
      // for repeatability (HashSet don't guarantee any order).
      TreeSet<URI> orderedSet = new TreeSet<URI>();
      orderedSet.addAll(is);
      chosenInspectors.add(orderedSet.first());
    }
    List<Object> chosenAttributeValues = new ArrayList<Object>();
    for (Attribute a : arg.selfClaimedAttributes) {
      chosenAttributeValues.add(a.getAttributeValue());
    }
    return new SitdReturn(chosenPresentationToken, metadataToChange, chosenPseudonyms,
        chosenInspectors, chosenAttributeValues);
  }
   */

}

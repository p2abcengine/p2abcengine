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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.SitdArguments;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdArguments;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.PseudonymMetadata;

public class MockIdentitySelectionXml implements IdentitySelectionXml {

  @Inject
  public MockIdentitySelectionXml() {
    // TODO Auto-generated method stub
    System.out.println("*** Using mock identity selection *** DO NOT USE IN PRODUCTION ***");
  }

  @Override
  public SptdReturn selectPresentationTokenDescription(SptdArguments arg) {
    // Choose always the first choice among all the alternatives
    int chosenPresentationToken = 0;
    Map<URI, PseudonymMetadata> metadataToChange = new HashMap<URI, PseudonymMetadata>();
    
    // Note: here we take the first option when the lists are ordered by hash code,
    // the behavior may be inconsistent from one run to the next.
    // We do this, since List<URI> is not comparable.
    List<URI> chosenPseudonyms = arg.pseudonymChoice.get(chosenPresentationToken).iterator().next();

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
    
    // Note: here we take the first option when the lists are ordered by hash code,
    // the behavior may be inconsistent from one run to the next.
    // We do this, since List<URI> is not comparable.
    List<URI> chosenPseudonyms = arg.pseudonymChoice.get(chosenPresentationToken).iterator().next();

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

}

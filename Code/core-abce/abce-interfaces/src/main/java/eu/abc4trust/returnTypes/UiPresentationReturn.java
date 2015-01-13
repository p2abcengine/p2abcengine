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

package eu.abc4trust.returnTypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.xml.PseudonymMetadata;

@XmlRootElement(name="UiPresentationReturn", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
@XmlAccessorType(XmlAccessType.FIELD)
public class UiPresentationReturn {
  /**
   * The context of the corresponding UiPresentationArguments.
   */
  public URI uiContext;
  /**
   * Index of the chosen policy
   */
  public int chosenPolicy;
  
  /**
   * The index of the chosen presentation token.
   */
  public int chosenPresentationToken;
  
  /**
   * User-defined metadata for each newly created pseudonym, and for each metadata the user changed
   * through the GUI (the User can add free notes or descriptions to the pseudonym to remind her
   * later when re-using the pseudonym; this metadata will be stored with the corresponding
   * pseudonyms). The metadata given here will replace the old metadata.
   */
  public Map<String, PseudonymMetadata> metadataToChange;
  /**
   * Chosen pseudonym list.
   * If there are no pseudonyms to choose, put NULL here.
   */
  public Integer chosenPseudonymList;
  /**
   * URI of the chosen inspectors.
   */
  public List<String> chosenInspectors;

  public UiPresentationReturn(URI context, int chosenPolicy, int chosenPresentationToken, Map<String, PseudonymMetadata> metadataToChange,
      int chosenPseudonymList, List<String> chosenInspectors) {
    this.uiContext = context;
    this.chosenPolicy = chosenPolicy;
    this.chosenPresentationToken = chosenPresentationToken;
    this.metadataToChange = metadataToChange;
    this.chosenPseudonymList = chosenPseudonymList;
    // shouldn't/cannot be null - create empty
    if(chosenInspectors==null) {
      this.chosenInspectors = new ArrayList<String>();
    } else {
      this.chosenInspectors = chosenInspectors;
    }
  }
  
  public UiPresentationReturn() {
    this.uiContext = null;
    this.chosenPolicy = 0;
    this.chosenPresentationToken = 0;
    this.metadataToChange = null;
    this.chosenPseudonymList = 0;
    // when serializing as XML - this chosenInspectors could be null
    this.chosenInspectors = new ArrayList<String>();
  }

  public UiPresentationReturn(UiPresentationArguments arg) {
    this.uiContext = arg.uiContext;
    this.chosenPolicy = 0;
    this.chosenPresentationToken = 0;
    this.metadataToChange = Collections.emptyMap();
    this.chosenPseudonymList = 0;
    this.chosenInspectors = new ArrayList<String>();
    for(InspectableAttribute ia: arg.tokenCandidatesPerPolicy.get(0).tokenCandidates.get(0).inspectableAttributes) {
      chosenInspectors.add(ia.inspectorAlternatives.get(0).uri);
    }
  }

  @Override
  public String toString() {
    return "UiPresentationReturn [uiContext=" + uiContext + ", chosenPolicy=" + chosenPolicy
        + ", chosenPresentationToken=" + chosenPresentationToken + ", metadataToChange="
        + metadataToChange + ", chosenPseudonymList=" + chosenPseudonymList + ", chosenInspectors="
        + chosenInspectors + "]";
  }
}

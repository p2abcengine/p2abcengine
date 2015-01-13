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
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.xml.PseudonymMetadata;

// eu.abc4trust.ui.idSelection.IdentitySelection.selectIssuanceTokenDescription
@XmlRootElement(name="SitdReturn", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
@XmlAccessorType(XmlAccessType.FIELD)
public class SitdReturn {

  /**
   * The index of the chosen issuance token.
   */
  public int chosenIssuanceToken;
  /**
   * User-defined metadata for each newly created pseudonym, and for each metadata the user changed
   * through the GUI (the User can add free notes or descriptions to the pseudonym to remind her
   * later when re-using the pseudonym; this metadata will be stored with the corresponding
   * pseudonyms).
   */
  public Map<URI, PseudonymMetadata> metadataToChange;
  /**
   * Chosen pseudonym UIDs: where
   * pseudonymChoice[chosenIssuanceToken][j].contains(chosenPseudonyms[j])
   */
  public List<URI> chosenPseudonyms;
  /**
   * Chosen inspectors: where inspectorChoice[chosenIssuanceToken][j].contains(chosenInspectors[j])
   */
  public List<URI> chosenInspectors;
  /**
   * List of AttributeValues for the each of the self-claimed attributes.
   */
  public List<Object> chosenAttributeValues;

  public SitdReturn(int chosenIssuanceToken, Map<URI, PseudonymMetadata> metadataToChange,
      List<URI> chosenPseudonyms, List<URI> chosenInspectors, List<Object> chosenAttributeValues) {
    this.chosenIssuanceToken = chosenIssuanceToken;
    this.metadataToChange = metadataToChange;
    this.chosenPseudonyms = chosenPseudonyms;
    this.chosenInspectors = chosenInspectors;
    this.chosenAttributeValues = chosenAttributeValues;
  }
  
  public SitdReturn() {
    this.chosenIssuanceToken = 0;
    this.metadataToChange = null;
    this.chosenPseudonyms = null;
    this.chosenInspectors = null;
    this.chosenAttributeValues = null;
  }
  
  @Override
  public String toString() {
    return "SitdReturn [chosenIssuanceToken=" + chosenIssuanceToken + ", metadataToChange="
        + metadataToChange + ", chosenPseudonyms=" + chosenPseudonyms + ", chosenInspectors="
        + chosenInspectors + ", chosenAttributeValues=" + chosenAttributeValues + "]";
  }
}

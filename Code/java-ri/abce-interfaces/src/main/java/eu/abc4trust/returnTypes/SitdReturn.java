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

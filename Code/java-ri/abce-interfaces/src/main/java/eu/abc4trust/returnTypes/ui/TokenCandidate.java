//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.CredentialAdapter;
import eu.abc4trust.xml.PresentationTokenDescription;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class TokenCandidate {

  @XmlAttribute
  public int candidateId;
  
  @XmlElement
  public PresentationTokenDescription tokenDescription;
  
  @XmlElementWrapper
  @XmlElement(name="credential")
  @XmlJavaTypeAdapter(CredentialAdapter.class)
  public List<CredentialInUi> credentials;
  
  @XmlElementWrapper
  @XmlElement(name="pseudonymCandidate")
  public List<PseudonymListCandidate> pseudonymCandidates;
  
  @XmlElementWrapper
  @XmlElement(name="revealedFact")
  public List<RevealedFact> revealedFacts;
  
  @XmlElementWrapper
  @XmlElement(name="revealedAttributeValue")
  public List<RevealedAttributeValue> revealedAttributeValues;
  
  @XmlElementWrapper
  @XmlElement(name="inspectableAttribute")
  public List<InspectableAttribute> inspectableAttributes;
  
  
  public TokenCandidate() {
    this.credentials = new ArrayList<CredentialInUi>();
    this.pseudonymCandidates = new ArrayList<PseudonymListCandidate>();
    this.revealedFacts = new ArrayList<RevealedFact>();
    this.revealedAttributeValues = new ArrayList<RevealedAttributeValue>();
    this.inspectableAttributes = new ArrayList<InspectableAttribute>();
  }
  
  public void addPseudonymCandidate(PseudonymListCandidate plc) {
    plc.candidateId = pseudonymCandidates.size();
    this.pseudonymCandidates.add(plc);
  }

  @Override
  public String toString() {
    return "TokenCandidate [candidateId=" + candidateId + ", tokenDescription=" + tokenDescription
        + ", credentials=" + credentials + ", pseudonymCandidates=" + pseudonymCandidates
        + ", revealedFacts=" + revealedFacts + ", revealedAttributeValues="
        + revealedAttributeValues + ", inspectableAttributes=" + inspectableAttributes + "]";
  }
}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.returnTypes.ui.AddTokenCandidate;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.returnTypes.ui.UiCommonArguments;
import eu.abc4trust.xml.IssuancePolicy;

@XmlRootElement(name="UiPresentationArguments", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
public class UiIssuanceArguments implements AddTokenCandidate {
  
  @XmlElement
  public UiCommonArguments data;
  
  @XmlElementWrapper
  @XmlElement(name = "tokenCandidate")
  public List<TokenCandidate> tokenCandidates;
  
  @XmlElement
  public IssuancePolicy policy;
  
  /*@XmlElementWrapper
  @XmlElement(name = "selfClaimedAttribute")
  public List<Attribute> selfClaimedAttributes;*/
  
  
  public UiIssuanceArguments() {
    this.data = new UiCommonArguments();
    this.tokenCandidates = new ArrayList<TokenCandidate>();
    this.policy = new IssuancePolicy();
    //this.selfClaimedAttributes = new ArrayList<Attribute>();
  }
  
  @Override
  public void addTokenCandidate(TokenCandidate can) {
    can.candidateId = tokenCandidates.size();
    tokenCandidates.add(can);
  }

  @Override
  public String toString() {
    return "UiIssuanceArguments [data=" + data + ", tokenCandidates=" + tokenCandidates
        + ", policy=" + policy + "]";
  }
}

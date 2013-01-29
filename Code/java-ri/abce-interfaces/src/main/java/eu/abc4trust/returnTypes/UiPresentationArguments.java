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

import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.returnTypes.ui.UiCommonArguments;

@XmlRootElement(name="UiPresentationArguments", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
public class UiPresentationArguments {
  
  @XmlElement
  public UiCommonArguments data;

  @XmlElementWrapper
  @XmlElement(name = "tokenCandidatePerPolicy")
  public List<TokenCandidatePerPolicy> tokenCandidatesPerPolicy;
  
  
  public UiPresentationArguments() {
    this.data = new UiCommonArguments();
    this.tokenCandidatesPerPolicy = new ArrayList<TokenCandidatePerPolicy>();
  }
  
  public void addTokenCandidate(TokenCandidatePerPolicy can) {
    can.policyId = tokenCandidatesPerPolicy.size();
    tokenCandidatesPerPolicy.add(can);
  }
  
  @Override
  public String toString() {
    return "UiPresentationArguments [data=" + data + ", tokenCandidatesPerPolicy="
        + tokenCandidatesPerPolicy + "]";
  }
}

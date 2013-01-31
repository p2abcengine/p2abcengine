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

import eu.abc4trust.xml.PresentationPolicy;


@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class TokenCandidatePerPolicy implements AddTokenCandidate {

  @XmlAttribute
  public int policyId;
  
  @XmlElement
  public PresentationPolicy policy;

  @XmlElementWrapper
  @XmlElement(name = "tokenCandidate")
  public List<TokenCandidate> tokenCandidates;

  
  public TokenCandidatePerPolicy() {
    this.tokenCandidates = new ArrayList<TokenCandidate>();
  }

  public TokenCandidatePerPolicy(PresentationPolicy policy,
                                  List<TokenCandidate> candidates) {
    this.policy = policy;
    this.tokenCandidates = candidates;
  }
  
  @Override
  public void addTokenCandidate(TokenCandidate can) {
    can.candidateId = tokenCandidates.size();
    tokenCandidates.add(can);
  }

  @Override
  public String toString() {
    return "TokenCandidatePerPolicy [policyId=" + policyId + ", policy=" + policy
        + ", tokenCandidates=" + tokenCandidates + "]";
  }
}

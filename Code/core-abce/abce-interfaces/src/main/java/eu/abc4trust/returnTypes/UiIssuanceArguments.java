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

package eu.abc4trust.returnTypes;

import java.net.URI;
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
  
  /**
   * A unique identifier for this object.
   */
  public URI uiContext;
  
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
    this.uiContext = null;
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
    return "UiIssuanceArguments [uiContext=" + uiContext + ", data=" + data + ", tokenCandidates="
        + tokenCandidates + ", policy=" + policy + "]";
  }
}

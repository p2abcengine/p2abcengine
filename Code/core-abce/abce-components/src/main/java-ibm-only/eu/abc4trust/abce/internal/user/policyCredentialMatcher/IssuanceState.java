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

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import java.util.List;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.util.MyCandidateToken;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.VerifierParameters;

public class IssuanceState { 
  public final UiIssuanceArguments arg;
  public final List<MyCandidateToken> candidateTokens;
  public final IssuanceMessage issuanceMessage;
  
  public IssuanceState(UiIssuanceArguments arg, List<MyCandidateToken> candidateTokens, IssuanceMessage issuanceMessage) {
    super();
    this.arg = arg;
    this.candidateTokens = candidateTokens;
    this.issuanceMessage = issuanceMessage;
  }
}

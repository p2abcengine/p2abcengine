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

package eu.abc4trust.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.PseudonymInUi;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.xml.PseudonymMetadata;

public class MyUiPresentationReturn {

  public final UiPresentationReturn ret;
  public final List<URI> chosenCredentials;
  public final List<URI> chosenPseudonyms;
  public final List<URI> chosenInspectors;
  public final Map<URI, PseudonymMetadata> metadataToChange;
  public final MyCandidateToken token;

  public MyUiPresentationReturn(UiPresentationArguments arg, UiPresentationReturn ret, List<MyCandidateToken> candidateTokens) {
    this.ret = ret;

    TokenCandidate tc =
        arg.tokenCandidatesPerPolicy.get(ret.chosenPolicy).tokenCandidates
            .get(ret.chosenPresentationToken);

    this.chosenCredentials = new ArrayList<URI>();
    for (CredentialInUi o : tc.credentials) {
      this.chosenCredentials.add(o.desc.getCredentialUID());
    }

    this.chosenPseudonyms = new ArrayList<URI>();
    if (tc.pseudonymCandidates.size() > 0) {
      for (PseudonymInUi o : tc.pseudonymCandidates.get(ret.chosenPseudonymList).pseudonyms) {
        this.chosenPseudonyms.add(o.pseudonym.getPseudonymUID());
      }
    }

    this.chosenInspectors = new ArrayList<URI>();
    for (String ins : ret.chosenInspectors) {
      this.chosenInspectors.add(URI.create(ins));
    }

    this.metadataToChange = new HashMap<URI, PseudonymMetadata>();
    for (String key : ret.metadataToChange.keySet()) {
      this.metadataToChange.put(URI.create(key), ret.metadataToChange.get(key));
    }
    
    int tokenNumber=ret.chosenPresentationToken;
    for(int i=0;i<ret.chosenPolicy;++i) {
      tokenNumber += arg.tokenCandidatesPerPolicy.get(i).tokenCandidates.size();
    }
    this.token = candidateTokens.get(tokenNumber);
  }
}

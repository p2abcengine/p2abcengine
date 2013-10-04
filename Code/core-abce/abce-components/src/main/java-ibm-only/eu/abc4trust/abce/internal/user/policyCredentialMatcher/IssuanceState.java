//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import java.net.URI;
import java.util.List;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.util.MyCandidateToken;

public class IssuanceState { 
  public final UiIssuanceArguments arg;
  public final List<MyCandidateToken> candidateTokens;
  public final URI issuanceContext;
  
  public IssuanceState(UiIssuanceArguments arg, List<MyCandidateToken> candidateTokens, URI issuanceContext) {
    super();
    this.arg = arg;
    this.candidateTokens = candidateTokens;
    this.issuanceContext = issuanceContext;
  }
}

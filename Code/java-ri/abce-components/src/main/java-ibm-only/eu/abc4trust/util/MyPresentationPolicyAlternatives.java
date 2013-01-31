//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.net.URI;

import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;

public class MyPresentationPolicyAlternatives {
  private final PresentationPolicyAlternatives policyAlternatives;
  
  public MyPresentationPolicyAlternatives(PresentationPolicyAlternatives ppa) {
    this.policyAlternatives = ppa;
  }

  public MyPresentationPolicy findOrThrow(URI policyUri) throws TokenVerificationException {
    for(PresentationPolicy pp: policyAlternatives.getPresentationPolicy()) {
      if (pp.getPolicyUID().equals(policyUri)) {
        return new MyPresentationPolicy(pp);
      }
    }
    String errorMessage = "Cannot find presentation policy with URI '" + policyUri + "' among the alternatives";
    TokenVerificationException ex = new TokenVerificationException();
    ex.errorMessages.add(errorMessage);
    throw ex;
  }

}

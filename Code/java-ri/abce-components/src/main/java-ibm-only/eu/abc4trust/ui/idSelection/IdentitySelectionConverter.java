//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ui.idSelection;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import eu.abc4trust.returnTypes.SitdArguments;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdArguments;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;

public class IdentitySelectionConverter implements IdentitySelection {

  private final IdentitySelectionXml isx;
  
  @Inject
  public IdentitySelectionConverter(IdentitySelectionXml isx) {
    this.isx = isx;
  }

  @Override
  public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
      List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids,
      List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice) {
    return isx.selectPresentationTokenDescription(
      new SptdArguments(policies, credentialDescriptions, pseudonyms, inspectors, tokens,
        credentialUids, pseudonymChoice, inspectorChoice));
  }

  @Override
  public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
      List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
      List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
      List<List<Set<URI>>> inspectorChoice) {
    return isx.selectIssuanceTokenDescription(new SitdArguments(policies, credentialDescriptions,
      pseudonyms, inspectors, tokens, credentialUids, selfClaimedAttributes,
      pseudonymChoice, inspectorChoice));
  }

}

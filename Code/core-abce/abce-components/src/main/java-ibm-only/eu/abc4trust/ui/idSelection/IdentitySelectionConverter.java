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

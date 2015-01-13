//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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

import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;



public interface IdentitySelection {
  /**
   * This method performs the identity selection, possibly presented by a graphical user interface,
   * allowing the User to choose which combination of credentials and/or pseudonyms, all satisfying
   * the policy she prefers to use.
   * 
   * @param credentialDescriptions A map that associates the credential UID with the corresponding
   *        credential descriptions of all credentials that are used in any of the candidate tokens.
   * @param pseudonyms A map that associates a pseudonym UID with the corresponding
   *        pseudonyms+metadata of all pseudonyms that may be used in any of the candidate tokens.
   *        pseudonyms that can be newly created will have no metadata and no
   *        PseudonymValue.
   * @param tokens The list of candidate presentation tokens.
   * @param credentialUids A two-dimensional list specifying for each candidate presentation token
   *        which credentials would be used to generate it. Meaning, creduid[i] is the list of
   *        credential identifiers that are used to generate token[i]. The list of credential
   *        identifiers is sorted according to the order that they appear in the presentation token.
   * @param pseudonymChoice A three-dimensional list specifying a candidate list of pseudonyms
   *        for each candidate presentation token. One of the elements of the set
   *        pseudonymChoice[i] must be chosen. The kth element of the chosen set
   *        is the identifier (PseudonymMetadata->PseudonymUID) for the kth pseudonym in token[i].
   *        This list also contains fresh UID for pseudonyms that can be newly created.
   * @param inspectorChoice A three-dimensional list specifying for each revealed attribute
   *        containing an alternative of inspectors, in each candidate presentation token, which
   *        inspectors can be chosen when generating the presentation token. Meaning,
   *        inspectorChoice[i][j] is the list of inspectors for the jth revealed attribute
   *        (containing inspectors) in token[i].
   * @return The method returns the index of the chosen presentation token. It also returns
   *         user-defined metadata for each newly created pseudonym, and for each metadata the user
   *         changed through the GUI (the User can add free notes or descriptions to the pseudonym
   *         to remind her later when re-using the pseudonym; this metadata will be stored with the
   *         corresponding pseudonyms). It also returns a list of chosen pseudonym UIDs: where
   *         pseudonymChoice[chosenPresentationToken][j].contains(chosenPseudonyms[j]). And finally
   *         it returns a list of chosen inspectors: where
   *         inspectorChoice[chosenPresentationToken][j].contains(chosenInspectors[j]).
   */
  public SptdReturn selectPresentationTokenDescription(
      Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms,
      Map<URI, InspectorDescription> inspectors,
      List<PresentationTokenDescription> tokens,
      List<List<URI>> credentialUids,
      List<Set<List<URI>>> pseudonymChoice,
      List<List<Set<URI>>> inspectorChoice);

  /**
   * This method is an "enhanced" version of the selectPresentationTokenDescription() method above.
   * This method presents again an identity selection, possibly being a graphical user interface
   * allowing the User to choose which combination of credentials and/or pseudonyms she prefers to
   * satisfy the policy and which self-claimed attributes she wants to embed in the new credential
   * to be issued.
   * 
   * @param credentialDescriptions A map that associates the credential UID with the corresponding
   *        credential descriptions of all credentials that are used in any of the candidate tokens.
   * @param pseudonyms A map that associates a pseudonym UID with the corresponding
   *        pseudonyms+metadata of all pseudonyms that may be used in any of the candidate tokens.
   *        pseudonyms that can be newly created will have no metadata and no
   *        PseudonymValue.
   * @param tokens The list of candidate issuance tokens. Each token also contains the credential
   *        template that describes which attributes from which credentials will be carried over to
   *        the newly issued credential.
   * @param credentialUids A two-dimensional list specifying for each candidate presentation token
   *        which credentials would be used to generate it. Meaning, creduid[i] is the list of
   *        credential identifiers that are used to generate token[i]. The list of credential
   *        identifiers is sorted according to the order that they appear in the presentation token.
   * @param selfClaimedAttributes The list of self-claimed attributes, possibly with or possibly
   *        without attributesValues. In any case, the user may change the proposed attributeValue.
   * @param pseudonymChoice A three-dimensional list specifying a candidate list of pseudonyms
   *        for each candidate presentation token. One of the elements of the set
   *        pseudonymChoice[i] must be chosen. The kth element of the chosen set
   *        is the identifier (PseudonymMetadata->PseudonymUID) for the kth pseudonym in token[i].
   *        This list also contains fresh UID for pseudonyms that can be newly created.
   * @param inspectorChoice A three-dimensional list specifying for each revealed attribute
   *        containing an alternative of inspectors, in each candidate presentation token, which
   *        inspectors can be chosen when generating the presentation token. Meaning,
   *        inspectorChoice[i][j] is the list of inspectors for the jth revealed attribute
   *        (containing inspectors) in token[i].
   * @return The method returns the index of the chosen issuance token. It also returns user-defined
   *         metadata for each newly created pseudonym, and for each metadata the user changed
   *         through the GUI (the User can add free notes or descriptions to the pseudonym to remind
   *         her later when re-using the pseudonym; this metadata will be stored with the
   *         corresponding pseudonyms). It also returns a list of chosen pseudonym UIDs: where
   *         pseudonymChoice[chosenIssuanceToken][j].contains(chosenPseudonyms[j]). It also
   *         returns a list of chosen inspectors: where
   *         inspectorChoice[chosenIssuanceToken][j].contains(chosenInspectors[j]). And finally
   *         it contains a list of AttributeValues for the each of the self-claimed attributes.
   */
  public SitdReturn selectIssuanceTokenDescription(
      Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms,
      Map<URI, InspectorDescription> inspectors,
      List<IssuanceTokenDescription> tokens,
      List<List<URI>> credentialUids,
      List<Attribute> selfClaimedAttributes,
      List<Set<List<URI>>> pseudonymChoice,
      List<List<Set<URI>>> inspectorChoice);
}

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.abc4trust.abce.internal.TokenManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestration;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.AbstractPseudonymSerializer;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.util.attributeTypes.MyAttributeValueFactory;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.AttributeInRevocation;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.VerifierDrivenRevocationInPolicy;
import eu.abc4trust.xml.VerifierDrivenRevocationInToken;
import eu.abc4trust.xml.VerifierIdentity;
import eu.abc4trust.xml.util.XmlUtils;

public class MyPresentationPolicy {
  private final PresentationPolicy policy;

  private final static Logger logger = Logger.getLogger(MyPresentationPolicy.class.getName());

  public MyPresentationPolicy(PresentationPolicy policy) {
    this.policy = policy;
  }
  
  public PresentationPolicy getPolicy() {
    return policy;
  }

  public boolean isSatisfiedBy(PresentationTokenDescription ptd, TokenManager tk, KeyManager km) {
    if (!ptd.getPolicyUID().equals(policy.getPolicyUID())) {
      logger.warning("Different policyUIDs.");
      return false;
    }
    if (!messageEquals(ptd.getMessage(), policy.getMessage())) {
      logger.warning("Messages don't match.");
      return false;
    }
    if (!pseudonymListEquals(ptd.getPseudonym(), policy.getPseudonym())) {
      logger.warning("Pseudonyms don't match");
      return false;
    }
    if (!credentialsEquals(ptd.getCredential(), policy.getCredential(), km)) {
      logger.warning("Credentials don't match");
      return false;
    }
    if (!predicatesEquals(ptd.getAttributePredicate(), policy.getAttributePredicate())) {
      logger.warning("Predicates don't match.");
      return false;
    }
    if (!verifierDrivenRevocationListEquals(ptd.getVerifierDrivenRevocation(),
        policy.getVerifierDrivenRevocation())) {
      logger.warning("Verifier Driven Revocation don't match");
      return false;
    }
    if(policy.isAllowSimpleProof() == false && ptd.isUsesSimpleProof()){
      logger.warning("Policy does not allow simple proofs.");
      return false;
    }
    if(ptd.isUsesSimpleProof() && ! checkIfSimpleProofAdmissible()) {
      logger.warning("Policy is too complex for simple proof flag.");
    }
    
    if (tk != null) {
      return checkEstablishedPseudonyms(ptd, tk);
    } else {
      return true;
    }
  }

  private boolean pseudonymListEquals(List<PseudonymInToken> lhs, List<PseudonymInPolicy> rhs) {
    if (lhs.size() != rhs.size()) {
      logger.warning("Incorrect number of predicates");
      return false;
    }

    Iterator<PseudonymInPolicy> rhsIter = rhs.iterator();
    for (PseudonymInToken lhsPred : lhs) {
      PseudonymInPolicy rhsPred = rhsIter.next();
      if (!pseudonymEquals(lhsPred, rhsPred)) {
        logger.warning("List of pseudonyms are not equal.");
        return false;
      }
    }
    return true;
  }

  private boolean myequals(Object lhs, Object rhs) {
    return (lhs == rhs) || ((lhs != null) && lhs.equals(rhs));
  }

  private boolean myArrayEquals(byte[] lhs, byte[] rhs) {
    if (lhs == null && rhs == null) {
      return true;
    } else if (lhs != null && rhs == null) {
      return false;
    } else if (lhs == null && rhs != null) {
      return false;
    } else {
      return Arrays.equals(lhs, rhs);
    }
  }

  private boolean predicatesEquals(List<AttributePredicate> lhs, List<AttributePredicate> rhs) {
    if (lhs.size() != rhs.size()) {
      logger.warning("Incorrect number of predicates");
      return false;
    }

    Iterator<AttributePredicate> rhsIter = rhs.iterator();
    for (AttributePredicate lhsPred : lhs) {
      AttributePredicate rhsPred = rhsIter.next();
      if (!predicateEquals(lhsPred, rhsPred)) {
        logger.warning("List of predicates are not equal.");
        return false;
      }
    }
    return true;
  }

  private boolean predicateEquals(AttributePredicate lhs, AttributePredicate rhs) {

    if (!myequals(lhs.getFunction(), rhs.getFunction())) {
      logger.warning("Different function");
      return false;
    }
    if (lhs.getAttributeOrConstantValue().size() != rhs.getAttributeOrConstantValue().size()) {
      logger.warning("Different number of arguments");
      return false;
    }
    Iterator<Object> lhsList = lhs.getAttributeOrConstantValue().iterator();
    for (Object rhsObj : rhs.getAttributeOrConstantValue()) {
      Object lhsObj = lhsList.next();

      if (rhsObj instanceof AttributePredicate.Attribute) {
        if (!(lhsObj instanceof AttributePredicate.Attribute)) {
          logger.warning("Different classes of arguments.");
          return false;
        }
        if (!attributeInPredicateEquals((AttributePredicate.Attribute) rhsObj,
            (AttributePredicate.Attribute) lhsObj)) {
          logger.warning("Different attribute predicates.");
          return false;
        }
      } else {
        if (!constantValueEquals(rhsObj, lhsObj, lhs.getFunction())) {
          logger.warning("Different constant value.");
          return false;
        }
      }
    }
    return true;
  }

  private boolean constantValueEquals(Object rhsObj, Object lhsObj, URI function) {
    MyAttributeValue rhs = MyAttributeValueFactory.parseValueFromFunction(function, rhsObj);
    MyAttributeValue lhs = MyAttributeValueFactory.parseValueFromFunction(function, lhsObj);
    return rhs.isCompatibleAndEquals(lhs);
  }

  private boolean attributeInPredicateEquals(AttributePredicate.Attribute rhs,
      AttributePredicate.Attribute lhs) {
    if (!rhs.getAttributeType().equals(lhs.getAttributeType())) {
      logger.warning("Different attribute type");
      return false;
    }
    if (!rhs.getCredentialAlias().equals(lhs.getCredentialAlias())) {
      logger.warning("Different credential alias");
      return false;
    }
    if (!myequals(rhs.getDataHandlingPolicy(), lhs.getDataHandlingPolicy())) {
      logger.warning("Different data handling policy");
      return false;
    }
    return true;
  }

  private boolean credentialsEquals(List<CredentialInToken> lhs, List<CredentialInPolicy> rhs,
                                    KeyManager keyManager) {

    if (lhs.size() != rhs.size()) {
      logger.warning("Different number of credentials");
      return false;
    }

    // Credentials must be in the same order
    Iterator<CredentialInToken> lhsIter = lhs.iterator();
    Iterator<CredentialInPolicy> rhsIter = rhs.iterator();

    while (lhsIter.hasNext() && rhsIter.hasNext()) {
      CredentialInToken lhsCred = lhsIter.next();
      CredentialInPolicy rhsCred = rhsIter.next();
      if (!credentialEquals(lhsCred, rhsCred, keyManager)) {
        logger.warning("Different credential");
        return false;
      }
    }

    return true;
  }

  private boolean credentialEquals(CredentialInToken lhs, CredentialInPolicy rhs, KeyManager keyManager) {

    if (!myequals(lhs.getAlias(), rhs.getAlias())) {
      logger.warning("Alias not the same");
      return false;
    }
    if (!rhs.getCredentialSpecAlternatives().getCredentialSpecUID()
        .contains(lhs.getCredentialSpecUID())) {
      logger.warning("CredentialSpecification not among the alternatives");
      logger.warning("Value was: " + lhs.getCredentialSpecUID());
      return false;
    }
    if (!issuerAmongAlternatives(rhs.getIssuerAlternatives().getIssuerParametersUID(),
        lhs.getIssuerParametersUID(), lhs.getRevocationInformationUID())) {
      logger.warning("IssuerParameters and RevocationInformation not among alterntives");
      logger.warning("Values were: " + lhs.getIssuerParametersUID() + " - "
          + lhs.getRevocationInformationUID());
      return false;
    }
    if (!attributesEquals(lhs.getDisclosedAttribute(), rhs.getDisclosedAttribute())) {
      logger.warning("Disclosed Attributes don't match");
      return false;
    }
    if (!myequals(lhs.getSameKeyBindingAs(), rhs.getSameKeyBindingAs())) {
      logger.warning("Different SameKeyBindingAs");
      return false;
    }
    if(keyManager != null) {
      if(!checkIssuerParameterCredentialSpecConsistency(lhs.getIssuerParametersUID(), 
          lhs.getCredentialSpecUID(), keyManager)) {
        logger.warning("Incompatible issuer parameters and credential specification");
        return false;
      }
    } else {
      logger.warning("Did not check if issuer parameters are for the correct credential spec.");
    }

    return true;
  }

  private boolean checkIssuerParameterCredentialSpecConsistency(URI issuerParametersUID,
      URI credentialSpecUID, KeyManager keyManager) {
    try {
      IssuerParameters ip = keyManager.getIssuerParameters(issuerParametersUID);
      CredentialSpecification spec = keyManager.getCredentialSpecification(credentialSpecUID);
      return ip.getMaximalNumberOfAttributes() >= spec.getAttributeDescriptions().getAttributeDescription().size();
    } catch (KeyManagerException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean verifierDrivenRevocationListEquals(List<VerifierDrivenRevocationInToken> lhs,
      List<VerifierDrivenRevocationInPolicy> rhs) {
    if (lhs.size() != rhs.size()) {
      logger.warning("Incorrect size of verifier driven revocation list");
      return false;
    }

    Iterator<VerifierDrivenRevocationInPolicy> rhsIter = rhs.iterator();
    for (VerifierDrivenRevocationInToken lhsPred : lhs) {
      VerifierDrivenRevocationInPolicy rhsPred = rhsIter.next();
      if (!verifierDrivenRevocationEquals(lhsPred, rhsPred)) {
        logger.warning("List of verifier driven revocation are not equal.");
        return false;
      }
    }
    return true;
  }

  private boolean verifierDrivenRevocationEquals(VerifierDrivenRevocationInToken lhs,
      VerifierDrivenRevocationInPolicy rhs) {

    // We don't compare the revocation parameters
    if (!attributesInRevocationEquals(lhs.getAttribute(), rhs.getAttribute())) {
      logger.warning("List of attributes in verifier driven revocation are not equal.");
      return false;
    }

    return true;
  }

  private boolean attributesInRevocationEquals(List<AttributeInRevocation> lhs,
      List<AttributeInRevocation> rhs) {
    if (lhs.size() != rhs.size()) {
      logger.warning("Incorrect size of verifier driven revocation attribute list");
      return false;
    }

    Iterator<AttributeInRevocation> rhsIter = rhs.iterator();
    for (AttributeInRevocation lhsPred : lhs) {
      AttributeInRevocation rhsPred = rhsIter.next();
      if (!attributeInRevocationEquals(lhsPred, rhsPred)) {
        logger.warning("Attribute in verifier driven revocation are not equal.");
        return false;
      }
    }
    return true;
  }

  private boolean attributeInRevocationEquals(AttributeInRevocation lhs, AttributeInRevocation rhs) {
    if (!lhs.getAttributeType().equals(rhs.getAttributeType())) {
      logger.warning("AttributeType are not equal.");
      return false;
    }
    if (!lhs.getCredentialAlias().equals(rhs.getCredentialAlias())) {
      logger.warning("CredentialAlias are not equal.");
      return false;
    }
    return true;
  }

  private boolean attributesEquals(List<AttributeInToken> lhs, List<AttributeInPolicy> rhs) {
    if (lhs.size() != rhs.size()) {
      logger.warning("Incorrect number of attributes.");
      return false;
    }

    // Attributes must be in the same order
    Iterator<AttributeInToken> lhsIter = lhs.iterator();
    Iterator<AttributeInPolicy> rhsIter = rhs.iterator();

    while (lhsIter.hasNext() && rhsIter.hasNext()) {
      AttributeInToken lhsAtt = lhsIter.next();
      AttributeInPolicy rhsAtt = rhsIter.next();
      if (!attributeEquals(lhsAtt, rhsAtt)) {
        logger.warning("Different attribute.");
        return false;
      }
    }

    return true;
  }

  private boolean attributeEquals(AttributeInToken lhs, AttributeInPolicy rhs) {
    if (!lhs.getAttributeType().equals(rhs.getAttributeType())) {
      logger.warning("Different attribute type.");
      return false;
    }
    if (!myequals(lhs.getDataHandlingPolicy(), rhs.getDataHandlingPolicy())) {
      logger.warning("Different data handling policy.");
      return false;
    }
    if (!myequals(lhs.getInspectionGrounds(), rhs.getInspectionGrounds())) {
      logger.warning("Different Inspection grounds.");
      return false;
    }

    if (rhs.getInspectorAlternatives() != null) {
      if (!rhs.getInspectorAlternatives().getInspectorPublicKeyUID().contains(lhs.getInspectorPublicKeyUID())) {
        logger.warning("Inspector not among alternatives.");
        return false;
      }
    } else {
      // Expect that the attribute is revealed
      if (lhs.getAttributeValue() == null) {
        logger.warning("Attribute value must be revealed");
        return false;
      }
    }

    return true;
  }

  private boolean issuerAmongAlternatives(List<IssuerParametersUID> lhsList, URI rhsIssuerParams,
      URI rhsRevocationInfo) {

    for (IssuerParametersUID lhsElement : lhsList) {
      if (lhsElement.getValue().equals(rhsIssuerParams)
          && myequals(lhsElement.getRevocationInformationUID(), rhsRevocationInfo)) {
        return true;
      }
    }

    return false;
  }

  private boolean pseudonymEquals(PseudonymInToken lhs, PseudonymInPolicy pseudonymInPolicy) {
    if (lhs == null && pseudonymInPolicy == null) {
      return true;
    } else if (lhs == null && pseudonymInPolicy != null) {
      logger.warning("Missing pseudonym.");
      return false;
    } else if (lhs != null && pseudonymInPolicy == null) {
      logger.warning("Should not contain pseudonym.");
      return false;
    } else if (!lhs.getScope().equals(pseudonymInPolicy.getScope())) {
      logger.warning("Different scopes");
      return false;
    } else if(pseudonymInPolicy.getPseudonymValue() != null && !Arrays.equals(pseudonymInPolicy.getPseudonymValue(), lhs.getPseudonymValue())) {
      // No check if pseudonymInPolicy does not specify a value
      logger.warning("Incorrect pseudonym value");
      return false;
    }
    // Skipping isEstablished, since the pseudonym in PresentationToken doesn't set this value
    else if (lhs.isExclusive() != pseudonymInPolicy.isExclusive()) {
      logger.warning("Different isExclusive");
      return false;
    } else if (!myequals(lhs.getAlias(), pseudonymInPolicy.getAlias())) {
      logger.warning("Different Alias");
      return false;
    } else if (!myequals(lhs.getSameKeyBindingAs(), pseudonymInPolicy.getSameKeyBindingAs())) {
      logger.warning("Different SameKeyBindingAs");
      return false;
    } else {
      return true;
    }
  }

  private boolean messageEquals(Message lhs, Message rhs) {
    if (lhs == null && rhs == null) {
      return true;
    } else if (lhs == null && rhs != null) {
      logger.warning("Missing message.");
      return false;
    } else if (lhs != null && rhs == null) {
      logger.warning("Should not contain message.");
      return false;
    } else if (!myArrayEquals(lhs.getNonce(), rhs.getNonce())) {
      logger.warning("Nonces are different: " + Arrays.toString(lhs.getNonce()) + ":" + Arrays.toString(rhs.getNonce()));
      return false;
    } else if (! friendlyEquals(lhs.getFriendlyPolicyName(), rhs.getFriendlyPolicyName())) {
      logger.warning("Friendly policy name are different");
      return false;
    } else if (! friendlyEquals(lhs.getFriendlyPolicyDescription(), rhs.getFriendlyPolicyDescription())) {
      logger.warning("Friendly policy description are different");
      return false;
    } else if (!verifierIdentityEquals(lhs.getVerifierIdentity(), rhs.getVerifierIdentity())) {
      logger.warning("Verifier identity is different");
      return false;
    } else if (!applicationDataEquals(lhs.getApplicationData(), rhs.getApplicationData())) {
      logger.warning("Application data is different");
      return false;
    } else {
      return true;
    }
  }

  private boolean friendlyEquals(List<FriendlyDescription> lhs,
      List<FriendlyDescription> rhs) {
    if (lhs == null && rhs == null) {
      return true;
    } else if (lhs == null || rhs == null) {
      logger.warning("List not present in both lhs and rhs");
      return false;
    } else if (lhs.size() != rhs.size()) {
      logger.warning("List size different");
      return false;
    }
    
    Iterator<FriendlyDescription> li = lhs.iterator();
    for(FriendlyDescription r: rhs) {
      FriendlyDescription l = li.next();
      if (!friendlyEquals(l, r)) {
        return false;
      }
    }
    return true;
  }

  private boolean friendlyEquals(FriendlyDescription l, FriendlyDescription r) {
    if (! l.getLang().equals(r.getLang())) {
      logger.warning("Incorrect language: l=" + l.getLang() + "  r=" + r.getLang());
      return false;
    }
    if( ! l.getValue().equals(r.getValue())) {
      logger.warning("Incorrect value: l=" + l.getValue() + " r=" + r.getValue());
      return false;
    }
    return true;
  }

  private boolean applicationDataEquals(ApplicationData lhs, ApplicationData rhs) {
    if (lhs == null && rhs == null) {
      return true;
    }
    if (lhs == null && rhs != null) {
      logger.warning("Missing application data.");
      return false;
    }
    if (lhs != null && rhs == null) {
      logger.warning("Should not contain application data.");
      return false;
    }

    // TODO(enr): Currently the only way to compare a sequence of xs:any
    ObjectFactory of = new ObjectFactory();
    String lhsXml, rhsXml;
    try {
      lhsXml = XmlUtils.toNormalizedXML(of.createApplicationData(lhs));
      rhsXml = XmlUtils.toNormalizedXML(of.createApplicationData(rhs));
    } catch (Exception e) {
      String errorMessage = "Could not serialize Application data: " + e.getMessage();
      logger.severe(errorMessage);
      e.printStackTrace();
      throw new RuntimeException(errorMessage);
    }

    if (!lhsXml.equals(rhsXml)) {
      logger.warning("Application data are not equal.");
      return false;
    }

    return true;
  }
  
  private boolean verifierIdentityEquals(VerifierIdentity lhs, VerifierIdentity rhs) {
    if (lhs == null && rhs == null) {
      return true;
    }
    if (lhs == null && rhs != null) {
      logger.warning("Missing verifier identity.");
      return false;
    }
    if (lhs != null && rhs == null) {
      logger.warning("Should not contain verifier identity.");
      return false;
    }

    // TODO(enr): Currently the only way to compare a sequence of xs:any
    ObjectFactory of = new ObjectFactory();
    String lhsXml, rhsXml;
    try {
      lhsXml = XmlUtils.toNormalizedXML(of.createVerifierIdentity(lhs));
      rhsXml = XmlUtils.toNormalizedXML(of.createVerifierIdentity(rhs));
    } catch (Exception e) {
      String errorMessage = "Could not serialize Verifier identity: " + e.getMessage();
      logger.severe(errorMessage);
      e.printStackTrace();
      throw new RuntimeException(errorMessage);
    }

    if (!lhsXml.equals(rhsXml)) {
      logger.warning("Verifier identities are not equal.");
      return false;
    }

    return true;
  }

  public URI getPolicyUri() {
    return policy.getPolicyUID();
  }

  public List<ArrayList<MyCredentialDescription>> findCredentialAssignment(String username, 
      CredentialManager credentialManager, KeyManager km)
          throws CredentialManagerException, KeyManagerException {
    List<List<MyCredentialDescription>> credentialsForPolicy =
        new ArrayList<List<MyCredentialDescription>>();

    for (CredentialInPolicy c : policy.getCredential()) {
      credentialsForPolicy.add(getCredentialsMatchingSpec(username, credentialManager, km, c));
    }

    LinkedList<ArrayList<MyCredentialDescription>> credentialAssignments = null;
    try {
      credentialAssignments =
          new LinkedList<ArrayList<MyCredentialDescription>>(
              CartesianProduct.cartesianProduct(credentialsForPolicy));
    } catch (Exception e) {
      logger.severe("Cannot create credentialAssignments: " + e.getMessage());
      return null;
    }

    Map<URI, Integer> credentialAliasMap = reverseLookupTable(getCredentialAliasList());
    filterPredicates(credentialAssignments, credentialAliasMap, policy.getAttributePredicate());

    return credentialAssignments;
  }
  
  public List<ArrayList<PseudonymWithMetadata>> findPseudonymAssignment(
    List<List<PseudonymWithMetadata>> pseudonymChoice,
    List<MyCredentialDescription> credAssign) {

    List<ArrayList<PseudonymWithMetadata>> pseudonymAssignment = null;
    try {
      pseudonymAssignment = new LinkedList<ArrayList<PseudonymWithMetadata>>(
          CartesianProduct.cartesianProduct(pseudonymChoice));
    } catch (Exception e) {
      logger.severe("Cannot create credentialAssignments: " + e.getMessage());
      return null;
    }

    return pseudonymAssignment;
  }

  public boolean filterSecrets(List<ArrayList<PseudonymWithMetadata>> pseudonymAssignment,
      List<MyCredentialDescription> credAssign) {
    
    // Create map associating credential alias with secret URI
    Map<URI, URI> secretAssignmentCred = new HashMap<URI, URI>();
    {
      Iterator<CredentialInPolicy> cipi = policy.getCredential().iterator();
      for(MyCredentialDescription mycd: credAssign) {
        CredentialInPolicy cip = cipi.next();
        if (cip.getAlias() != null) {
          if (secretAssignmentCred.containsKey(cip.getAlias())) {
            throw new RuntimeException("Duplicate alias in Policy (cred) " + cip.getAlias());
          }
          secretAssignmentCred.put(cip.getAlias(), mycd.getSecretReference());
        }
      }
    }
    
    Iterator<ArrayList<PseudonymWithMetadata>> pwmli = pseudonymAssignment.iterator();
    pseudonymloop:
    while(pwmli.hasNext()) {
      List<PseudonymWithMetadata> pwml = pwmli.next();
      // Create map associating cred+nym alias with secret URI
      Map<URI, URI> secretAssignment = new HashMap<URI, URI>(secretAssignmentCred);
      {
        Iterator<PseudonymInPolicy> psipi = policy.getPseudonym().iterator();
        for(PseudonymWithMetadata pwm: pwml) {
          PseudonymInPolicy pip = psipi.next();
          if (pip.getAlias() != null) {
            if (secretAssignment.containsKey(pip.getAlias())) {
              throw new RuntimeException("Duplicate alias in Policy (nym) " + pip.getAlias());
            }
            secretAssignment.put(pip.getAlias(), pwm.getPseudonym().getSecretReference());
          }
        }
      }
      
      // Check if credentials has same secrets as...
      {
        Iterator<CredentialInPolicy> cipi = policy.getCredential().iterator();
        for(MyCredentialDescription mycd: credAssign) {
          CredentialInPolicy cip = cipi.next();
          if (cip.getSameKeyBindingAs() != null) {
            URI secretOfOther = secretAssignment.get(cip.getSameKeyBindingAs());
            if (secretOfOther == null) {
              throw new RuntimeException("Alias referenced by SameKeyBindingAs (cred) not found");
            }
            if (! secretOfOther.equals(mycd.getSecretReference())) {
              pwmli.remove();
              continue pseudonymloop;
            }
          }
        }
      }
      // Check if credentials has same secrets as...
      {
        Iterator<PseudonymInPolicy> psipi = policy.getPseudonym().iterator();
        for(PseudonymWithMetadata pwm: pwml) {
          PseudonymInPolicy pip = psipi.next();
          if (pip.getSameKeyBindingAs() != null) {
            URI secretOfOther = secretAssignment.get(pip.getSameKeyBindingAs());
            if (secretOfOther == null) {
              throw new RuntimeException("Alias referenced by SameKeyBindingAs (nym) not found");
            }
            if (! secretOfOther.equals(pwm.getPseudonym().getSecretReference())) {
              pwmli.remove();
              continue pseudonymloop;
            }
          }
        }
      }
    }
    
    return pseudonymAssignment.size() > 0;
  }

  private void filterPredicates(
      LinkedList<ArrayList<MyCredentialDescription>> credentialAssignments,
      Map<URI, Integer> credentialAliasList, List<AttributePredicate> attributePredicates) {

    Iterator<ArrayList<MyCredentialDescription>> iter = credentialAssignments.iterator();
    while (iter.hasNext()) {
      ArrayList<MyCredentialDescription> candidateAssignment = iter.next();

      if (!satisfiesPredicate(candidateAssignment, credentialAliasList, attributePredicates)) {
        iter.remove();
//        logger.info("Removed " + candidateAssignment + " (predicate)");
      }
    }
  }

  private boolean satisfiesPredicate(ArrayList<MyCredentialDescription> candidateAssignment,
      Map<URI, Integer> credentialAliasList, List<AttributePredicate> attributePredicates) {
    for (AttributePredicate predicate : attributePredicates) {
      URI function = predicate.getFunction();
      List<MyAttributeValue> arguments = new ArrayList<MyAttributeValue>();
      for (Object param : predicate.getAttributeOrConstantValue()) {
        if (param instanceof AttributePredicate.Attribute) {
          AttributePredicate.Attribute attParam = (AttributePredicate.Attribute) param;
          URI credentialAlias = attParam.getCredentialAlias();
          Integer credentialAliasIndex = credentialAliasList.get(credentialAlias);
          if (credentialAliasIndex == null) {
            logger.severe("Unknown credential alias: '" + credentialAlias + "'");
            return false;
          }
          MyCredentialDescription credentialContainingAttribute =
              candidateAssignment.get(credentialAliasIndex);
          URI attributeType = attParam.getAttributeType();
          MyAttributeValue value = credentialContainingAttribute.getAttributeValue(attributeType);
          if (value == null) {
            logger.severe("Unknown attribute type: '" + attributeType + "' for credential alias: '"
            + credentialAlias + "'" );
            return false;
          }
          arguments.add(value);
        } else {
          MyAttributeValue constant =
              MyAttributeValueFactory.parseValueFromFunction(function, param);
          arguments.add(constant);
        }
      }
      if (!MyAttributeValueFactory.evaulateFunction(function, arguments)) {
        return false;
      }
    }
    return true;
  }

  private LinkedList<MyCredentialDescription> getCredentialsMatchingSpec(String username, 
      CredentialManager credentialManager, KeyManager km, CredentialInPolicy c)
          throws CredentialManagerException, KeyManagerException {
    List<URI> credentialSpecs = c.getCredentialSpecAlternatives().getCredentialSpecUID();
    List<URI> issuerParameters = new ArrayList<URI>();
    for (IssuerParametersUID ipu : c.getIssuerAlternatives().getIssuerParametersUID()) {
      issuerParameters.add(ipu.getValue());
    }

    List<CredentialDescription> credDescMatchingIssuer =
        credentialManager.getCredentialDescription(username, issuerParameters, credentialSpecs);

    LinkedList<MyCredentialDescription> candidateCredentials =
        new LinkedList<MyCredentialDescription>();
    for (CredentialDescription cd : credDescMatchingIssuer) {
      if(!cd.isRevokedByIssuer()) {
        candidateCredentials.add(new MyCredentialDescription(cd, km));
      }
    }

    return candidateCredentials;
  }

  private <T> Map<T, Integer> reverseLookupTable(List<T> list) {
    Map<T, Integer> reverseLookupMap = new HashMap<T, Integer>();
    Integer i = Integer.valueOf(0);
    for (T element : list) {
      reverseLookupMap.put(element, i);
      i++;
    }
    return reverseLookupMap;
  }

  public PresentationTokenDescription generateTokenDescription(
      ArrayList<MyCredentialDescription> assignment, ContextGenerator contextGenerator) {

    List<URI> credentialAliasList = getCredentialAliasList();

    ObjectFactory of = new ObjectFactory();

    PresentationTokenDescription ptd = of.createPresentationTokenDescription();
    for (AttributePredicate pred : policy.getAttributePredicate()) {
      ptd.getAttributePredicate().add(pred);
    }
    populateVerifierDrivenRevocation(ptd);
    populateCredentialsInTokenDescription(ptd, assignment, credentialAliasList);
    ptd.setMessage(policy.getMessage());
    ptd.setPolicyUID(policy.getPolicyUID());
    ptd.setUsesSimpleProof(policy.isAllowSimpleProof() && checkIfSimpleProofAdmissible());
    for (PseudonymInPolicy policyPseudonym : policy.getPseudonym()) {
      PseudonymInToken pseudonym = of.createPseudonymInToken();
      ptd.getPseudonym().add(pseudonym);
      pseudonym.setAlias(policyPseudonym.getAlias());
      pseudonym.setExclusive(policyPseudonym.isExclusive());
      pseudonym.setScope(policyPseudonym.getScope());
      pseudonym.setSameKeyBindingAs(policyPseudonym.getSameKeyBindingAs());
      // Pseudonym value will be filled out later
      pseudonym.setPseudonymValue(null);
    }

    ptd.setTokenUID(contextGenerator.getUniqueContext(URI.create("abc4t://token")));
    return ptd;
  }

  static final Set<URI> admissiblePredicatesForSimpleProof = new HashSet<URI>();
  static {
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:date-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:date-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:time-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"));
    admissiblePredicatesForSimpleProof.add(URI.create("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"));
  }
  private boolean checkIfSimpleProofAdmissible() {
    if(policy.getPseudonym().size() != 0) {
      logger.warning("Policy has pseudonyms: simple proofs not admissible.");
      return false;
    }
    if(policy.getCredential().size() != 1) {
      logger.warning("Policy has not exactly 1 credential: simple proofs not admissible.");
      return false;
    }
    for(AttributeInPolicy a: policy.getCredential().get(0).getDisclosedAttribute()) {
      if(a.getInspectorAlternatives() != null) {
        logger.warning("Policy has inspectors: simple proofs not admissible.");
        return false;
      }
    }
    if(policy.getVerifierDrivenRevocation().size() != 0) {
      logger.warning("Policy has verifier driven revocation: simple proofs not admissible.");
      return false;
    }
    for(AttributePredicate a: policy.getAttributePredicate()) {
      if(! admissiblePredicatesForSimpleProof.contains(a.getFunction())) {
        logger.warning("Policy has attribute predicate not admissible for simple proofs.");
        return false;
      }
    }

    return true;
  }

  private void populateVerifierDrivenRevocation(PresentationTokenDescription ptd) {
    ObjectFactory of = new ObjectFactory();
    for (VerifierDrivenRevocationInPolicy vdrp : policy.getVerifierDrivenRevocation()) {
      VerifierDrivenRevocationInToken vdrt = of.createVerifierDrivenRevocationInToken();
      ptd.getVerifierDrivenRevocation().add(vdrt);
      // TODO(enr): Where do we get that revocation information UID from?
      vdrt.setRevocationInformationUID(URI.create("no:revocation:information:yet"));
      vdrt.getAttribute().addAll(vdrp.getAttribute());
    }
  }

  private List<URI> getCredentialAliasList() {
    List<URI> credentialAliasList = new ArrayList<URI>();
    for (CredentialInPolicy c : policy.getCredential()) {
      credentialAliasList.add(c.getAlias());
    }
    return credentialAliasList;
  }

  private void populateCredentialsInTokenDescription(PresentationTokenDescription output,
      ArrayList<MyCredentialDescription> assignments, List<URI> credentialAliasList) {

    ObjectFactory of = new ObjectFactory();

    Iterator<URI> aliases = credentialAliasList.iterator();
    Iterator<CredentialInPolicy> credsInPolicy = policy.getCredential().iterator();
    for (MyCredentialDescription myCredDesc : assignments) {
      CredentialInPolicy credInPolicy = credsInPolicy.next();
      URI alias = aliases.next();

      CredentialInToken c = of.createCredentialInToken();
      CredentialDescription credDesc = myCredDesc.getCredentialDescription();

      c.setAlias(alias);
      myCredDesc.populateDisclosedAttributes(c, credInPolicy);
      c.setCredentialSpecUID(credDesc.getCredentialSpecificationUID());
      c.setIssuerParametersUID(credDesc.getIssuerParametersUID());
      IssuerParametersUID ipu =
          locateIssuerParameterOrThrow(credInPolicy, credDesc.getIssuerParametersUID());
      c.setRevocationInformationUID(ipu.getRevocationInformationUID());
      c.setSameKeyBindingAs(credInPolicy.getSameKeyBindingAs());

      output.getCredential().add(c);
    }
  }

  private IssuerParametersUID locateIssuerParameterOrThrow(CredentialInPolicy credInPolicy, URI rhs) {
    // TODO(enr): Linear search... surely we can do better
    for (IssuerParametersUID ipu : credInPolicy.getIssuerAlternatives().getIssuerParametersUID()) {
      if (ipu.getValue().equals(rhs)) {
        return ipu;
      }
    }
    String errorMessage = "Cannot find IssuerParameters '" + rhs + "' in CredentialInPolicy.";
    logger.severe(errorMessage);
    throw new RuntimeException(errorMessage);
  }

  public List<List<MyInspectableAttribute>> computeInspectorChoice(KeyManager keyManager) {
    List<List<MyInspectableAttribute>> ret = new ArrayList<List<MyInspectableAttribute>>();
    int credentialId = -1;
    for (CredentialInPolicy cip : policy.getCredential()) {
      credentialId++;
      for (AttributeInPolicy aip : cip.getDisclosedAttribute()) {
       if (aip.getInspectorAlternatives()!=null) {
         List<MyInspectableAttribute> inspectorAlternative = new ArrayList<MyInspectableAttribute>();
          for (URI inspectorUri: aip.getInspectorAlternatives().getInspectorPublicKeyUID()) {
            InspectorPublicKey ipk = null;
            try {
              ipk = keyManager.getInspectorPublicKey(inspectorUri);
            } catch(Exception e) {
              throw new RuntimeException(e);
            } 
            if (ipk != null) {
              inspectorAlternative.add(new MyInspectableAttribute(ipk, credentialId, aip.getAttributeType(), aip.getDataHandlingPolicy(), aip.getInspectionGrounds()));
            } else {
              logger.severe("Could not find inspector with public key " + inspectorUri +
                ". Removing him from alternatives.");
            }
          }
          if (inspectorAlternative.size() == 0) {
            logger.severe("Could not find any inspectors for disclosed attribute " +
                          aip.getAttributeType() + ". Removing policy alternative.");
            return null;
          }
          ret.add(inspectorAlternative);
        }
      }
    }
    return ret;
  }

  public List<List<PseudonymWithMetadata>> computePseudonymChoice(String username, 
      CredentialManager credentialManager, ContextGenerator contextGenerator,
      EvidenceGenerationOrchestration evidenceOrchestration) throws CryptoEngineException {
    try {
      List<SecretDescription> secrets = credentialManager.listSecrets(username);
      List<List<PseudonymWithMetadata>> ret = new ArrayList<List<PseudonymWithMetadata>>();
      for (PseudonymInPolicy pseudonym : policy.getPseudonym()) {
        String scope = pseudonym.getScope();
        boolean canCreateNew = !pseudonym.isEstablished();
        
        if(!pseudonym.isEstablished() && !pseudonym.isExclusive() && pseudonym.getPseudonymValue() != null) {
          throw new RuntimeException("Cannot specify a value for pseudonym if it is not established and not exclusive");
        }

        List<PseudonymWithMetadata> list =
            credentialManager.listPseudonyms(username, scope, pseudonym.isExclusive());
        
        list = filterPseudonymsByValue(list, pseudonym.getPseudonymValue());
        
        if (list.size() > 0 && pseudonym.isExclusive()) {
          // Can't create a new pseudonym if we are asked a scope exclusive one, and we have one
          // already
          canCreateNew = false;
        }

        if (canCreateNew) {
          for (SecretDescription sd: secrets) {
            ObjectFactory of = new ObjectFactory();
            PseudonymWithMetadata newPseudonym = of.createPseudonymWithMetadata();
            newPseudonym.setPseudonym(of.createPseudonym());
            
            // Secret reference
            newPseudonym.getPseudonym().setSecretReference(sd.getSecretUID());
            // Will be filled out by crypto engine
            newPseudonym.setCryptoParams(null);
            // Metadata example
            if(! pseudonym.isExclusive()) {
              Map<String, String> trans = new HashMap<String, String>();
              //trans.put("en", "New pseudonym with scope %s and secret %s.");
              //trans.put("el", "\u039d\u03ad\u03bf \u03c8\u03b5\u03c5\u03b4\u03ce\u03bd\u03c5\u03bc\u03bf \u03bc\u03b5 \u03c4\u03bf\u03bd \u03c4\u03bf\u03bc\u03ad\u03b1 %s \u03ba\u03b1\u03b9 \u03bc\u03c5\u03c3\u03c4\u03b9\u03ba\u03ae %s.");
              //trans.put("sv", "Ny pseudonym med dom\u00e4n %s och hemliga %s.");
              trans.put("en", "New Pseudonym %s");
              trans.put("el", "\u039d\u03ad\u03bf \u03a8\u03b5\u03c5\u03b4\u03ce\u03bd\u03c5\u03bc\u03bf %s");
              trans.put("sv", "Ny Pseudonym %s");
              
              String now = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
              
              newPseudonym.setPseudonymMetadata(of.createPseudonymMetadata());
              for(String lang: trans.keySet()) {
                FriendlyDescription desc = of.createFriendlyDescription();
                desc.setLang(lang);
                desc.setValue(String.format(trans.get(lang), now));
//                System.err.println(desc.getValue());
                newPseudonym.getPseudonymMetadata().getFriendlyPseudonymDescription().add(desc);
              }
            } else {
              newPseudonym.setPseudonymMetadata(of.createPseudonymMetadata());
              FriendlyDescription fd = AbstractPseudonymSerializer.generateFriendlyDescription(scope);
              newPseudonym.getPseudonymMetadata().getFriendlyPseudonymDescription().add(fd);
            }
  
            newPseudonym.getPseudonym().setScope(scope);
            URI prefix = URI.create("abc4t://nym");
            if (pseudonym.isExclusive()) {
              prefix = URI.create("abc4t://dnym");
            }
            newPseudonym.getPseudonym().setPseudonymUID(
                contextGenerator.getUniqueContext(prefix));
            newPseudonym.getPseudonym().setExclusive(pseudonym.isExclusive());
            if(pseudonym.getPseudonymValue() != null && pseudonym.isExclusive()) {
              // Ignore scope exclusive pseudonyms where the pseudonym value doesn't match
              PseudonymWithMetadata newpwm = evidenceOrchestration.createPseudonym(username, newPseudonym.getPseudonym().getPseudonymUID(), pseudonym.getScope(), pseudonym.isExclusive(), sd.getSecretUID());
              if(!Arrays.equals(newpwm.getPseudonym().getPseudonymValue(), pseudonym.getPseudonymValue())) {
                continue;
              }
            }
            // Will be filled out by Crypto Engine
            newPseudonym.getPseudonym().setPseudonymValue(null);
  
            list.add(newPseudonym);
          }
        }
        ret.add(new ArrayList<PseudonymWithMetadata>(list));
      }
      return ret;
    } catch (CredentialManagerException e) {
      throw new RuntimeException(e);
    }
  }
  
  private List<PseudonymWithMetadata> filterPseudonymsByValue(
    List<PseudonymWithMetadata> list, byte[] pseudonymValue) {
    if(pseudonymValue == null) {
      return list;
    }
    List<PseudonymWithMetadata> ret = new ArrayList<PseudonymWithMetadata>();
    for(PseudonymWithMetadata pwm: list) {
      if(Arrays.equals(pwm.getPseudonym().getPseudonymValue(), pseudonymValue)) {
        ret.add(pwm);
      }
    }
    return ret;
  }

  public Message getMessage() {
    return this.policy.getMessage();
  }

  private boolean checkEstablishedPseudonyms(PresentationTokenDescription td,
      TokenManager tk) {
    Iterator<PseudonymInToken> piti = td.getPseudonym().iterator();
    for(PseudonymInPolicy pip: policy.getPseudonym()) {
      PseudonymInToken pit = piti.next();
      if (pip.isEstablished()) {
        if (! tk.isEstablishedPseudonym(pit)) {
          logger.severe("The pseudonym " + pip.getAlias() + " is not established.");
          return false;
        }
      }
    }
    return true;
  }

  public void updateIssuerToRevocationInformationUidMap(Map<URI, URI> toUpdate) {
    for(CredentialInPolicy cip: policy.getCredential()) {
      for(IssuerParametersUID ipu: cip.getIssuerAlternatives().getIssuerParametersUID()) {
        URI issuer = ipu.getValue();
        URI riu = ipu.getRevocationInformationUID();
        if (riu != null) {
          URI oldRiu = toUpdate.get(issuer);
          if(oldRiu == null || oldRiu.equals(riu)) {
            toUpdate.put(issuer, riu);
          } else {
            throw new RuntimeException("There are two different revocationInformationUids for the same issuer."); 
          }
        }
      }
    }
  }
}

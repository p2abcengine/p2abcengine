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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PresentationState;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.AddTokenCandidate;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.CredentialSpecInUi;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.returnTypes.ui.InspectorInUi;
import eu.abc4trust.returnTypes.ui.IssuerInUi;
import eu.abc4trust.returnTypes.ui.PseudonymInUi;
import eu.abc4trust.returnTypes.ui.PseudonymListCandidate;
import eu.abc4trust.returnTypes.ui.RevealedFactsAndAttributeValues;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.returnTypes.ui.UiCommonArguments;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;

public class MyCandidateToken {
  private final MyPresentationPolicy pp;
  private final PresentationTokenDescription ptd;
  private final IssuanceTokenDescription itd;
  private final List<MyCredentialDescription> creds;
  private final List<ArrayList<PseudonymWithMetadata>> pseudonyms;
  private final List<List<MyInspectableAttribute>> inspectors;

  public MyCandidateToken(MyPresentationPolicy pp, PresentationTokenDescription ptd,
      CredentialTemplate ct,
      List<MyCredentialDescription> creds, List<ArrayList<PseudonymWithMetadata>> pseudonyms,
      List<List<MyInspectableAttribute>> inspectors) {
    this.pp = pp;
    this.ptd = ptd;
    if (ct != null) {
      this.itd = generateIssuanceToken(ct);
    } else {
      this.itd = null;
    }
    this.creds = creds;
    this.pseudonyms = pseudonyms;
    this.inspectors = inspectors;
  }
  
  public IssuanceTokenDescription generateIssuanceToken(CredentialTemplate ct) {
    IssuanceTokenDescription issToken = new IssuanceTokenDescription();
    issToken.setCredentialTemplate(ct);
    issToken.setPresentationTokenDescription(ptd);
    return issToken;
  }

  public void populateIssuance(Map<URI, PolicyDescription> mpo,
                               Map<URI, CredentialDescription> mcd,
                               Map<URI, PseudonymDescription> mps,
                               Map<URI, InspectorDescription> mid,
                               List<IssuanceTokenDescription> litd,
                               List<List<URI>> credUri,
                               List<Set<List<URI>>> pseudonyms,
                               List<List<Set<URI>>> inspectors) {
    if (itd == null) {
      throw new RuntimeException("No issuance token description");
    }
    populate(mpo, mcd, mps, mid, credUri, pseudonyms, inspectors);
    litd.add(itd);
  }
  
  public void populatePresentation(Map<URI, PolicyDescription> mpo,
                                   Map<URI, CredentialDescription> mcd,
                                   Map<URI, PseudonymDescription> mps,
                                   Map<URI, InspectorDescription> mid,
                                   List<PresentationTokenDescription> lptd,
                                   List<List<URI>> credUri,
                                   List<Set<List<URI>>> pseudonyms,
                                   List<List<Set<URI>>> inspectors) {
    populate(mpo, mcd, mps, mid, credUri, pseudonyms, inspectors);
    lptd.add(ptd);
  }
  
  private void populate(Map<URI, PolicyDescription> mpo,
                        Map<URI, CredentialDescription> mcd,
                        Map<URI, PseudonymDescription> mps,
                        Map<URI, InspectorDescription> mid,
                        List<List<URI>> credUri,
                        List<Set<List<URI>>> lslp,
                        List<List<Set<URI>>> llsi) {
    {
      PolicyDescription pd = new PolicyDescription();
      pd.setPolicyUID(pp.getPolicyUri());
      pd.setMessage(pp.getMessage());
      mpo.put(pp.getPolicyUri(), pd);
    }
    for (MyCredentialDescription cd: creds) {
      mcd.put(cd.getUid(), cd.getCredentialDesc());
    }
    for (List<PseudonymWithMetadata> lpwm: pseudonyms) {
      for(PseudonymWithMetadata pwm: lpwm) {
        URI psUri = pwm.getPseudonym().getPseudonymUID();
        PseudonymDescription pd = new PseudonymDescription();
        pd.setExclusive(pwm.getPseudonym().isExclusive());
        pd.setPseudonymMetadata(pwm.getPseudonymMetadata());
        pd.setPseudonymUID(psUri);
        pd.setScope(pwm.getPseudonym().getScope());
        mps.put(psUri, pd);
      }
    }
    for (List<MyInspectableAttribute> sipk: inspectors) {
      for(MyInspectableAttribute ipk: sipk) {
        URI iUri = ipk.ipk.getPublicKeyUID();
        InspectorDescription id = new InspectorDescription();
        if (ipk.ipk.getFriendlyInspectorDescription().size() > 0) {
        	if (ipk.ipk.getFriendlyInspectorDescription().size() > 0) {
                 for (FriendlyDescription fid: ipk.ipk.getFriendlyInspectorDescription()){
                	 id.getFriendlyInspectorDescription().add(fid);
                 }
        	}
        }
        id.setInspectorUID(ipk.ipk.getPublicKeyUID());
        mid.put(iUri, id);
      }
    }
    {
      credUri.add(getCredentialUriList());
    }
    {
      Set<List<URI>> psChoice = new HashSet<List<URI>>();
      for (List<PseudonymWithMetadata> lpwm: pseudonyms) {
        List<URI> entry = new ArrayList<URI>();
        for(PseudonymWithMetadata pwm: lpwm) {
          URI psUri = pwm.getPseudonym().getPseudonymUID();
          entry.add(psUri);
        }
        psChoice.add(entry);
      }
      lslp.add(psChoice);
    }
    {
      List<Set<URI>> iChoice = new ArrayList<Set<URI>>();
      for (List<MyInspectableAttribute> sipk: inspectors) {
        Set<URI> entry = new HashSet<URI>();
        for(MyInspectableAttribute ipk: sipk) {
          URI psUri = ipk.ipk.getPublicKeyUID();
          entry.add(psUri);
        }
        iChoice.add(entry);
      }
      llsi.add(iChoice);
    }
  }
  
  public static SitdReturn callIssuanceUi(IdentitySelection ui, List<MyCandidateToken> tokens) {
    Map<URI, PolicyDescription> mpo = new HashMap<URI, PolicyDescription>();
    Map<URI, CredentialDescription> mcd = new HashMap<URI, CredentialDescription>();
    Map<URI, PseudonymDescription> mps = new HashMap<URI, PseudonymDescription>();
    Map<URI, InspectorDescription> mid = new HashMap<URI, InspectorDescription>();
    List<IssuanceTokenDescription> litd = new ArrayList<IssuanceTokenDescription>();
    List<List<URI>> credUri = new ArrayList<List<URI>>();
    List<Set<List<URI>>> pseudonyms = new ArrayList<Set<List<URI>>>();
    List<List<Set<URI>>> inspectors = new ArrayList<List<Set<URI>>>();
    for(MyCandidateToken token: tokens) {
      token.populateIssuance(mpo, mcd, mps, mid, litd, credUri, pseudonyms, inspectors);
    }
    // TODO(enr): Remove empty list of attributes
    List<Attribute> atts = new ArrayList<Attribute>();
    return ui.selectIssuanceTokenDescription(mpo, mcd, mps, mid,
                                             litd, credUri, atts, pseudonyms, inspectors);
  }
  
  public static SptdReturn callPresentationUi(IdentitySelection ui, List<MyCandidateToken> tokens) {
    Map<URI, PolicyDescription> mpo = new HashMap<URI, PolicyDescription>();
    Map<URI, CredentialDescription> mcd = new HashMap<URI, CredentialDescription>();
    Map<URI, PseudonymDescription> mps = new HashMap<URI, PseudonymDescription>();
    Map<URI, InspectorDescription> mid = new HashMap<URI, InspectorDescription>();
    List<PresentationTokenDescription> lptd = new ArrayList<PresentationTokenDescription>();
    List<List<URI>> credUri = new ArrayList<List<URI>>();
    List<Set<List<URI>>> pseudonyms = new ArrayList<Set<List<URI>>>();
    List<List<Set<URI>>> inspectors = new ArrayList<List<Set<URI>>>();
    for(MyCandidateToken token: tokens) {
      token.populatePresentation(mpo, mcd, mps, mid, lptd, credUri, pseudonyms, inspectors);
    }
    return ui.selectPresentationTokenDescription(mpo, mcd, mps, mid,
                                                 lptd, credUri, pseudonyms, inspectors);
  }
  
  public List<URI> getCredentialUriList() {
    List<URI> credsInToken = new ArrayList<URI>();
    for (MyCredentialDescription cd: creds) {
      credsInToken.add(cd.getUid());
    }
    return credsInToken;
  }

  public PresentationTokenDescription getPresentationToken() {
    return ptd; 
  }
  
  public void populatePseudonymsInMap(Map<URI, PseudonymWithMetadata> mpwm) {
    for (List<PseudonymWithMetadata> lpwm: pseudonyms) {
      for(PseudonymWithMetadata pwm: lpwm) {
        URI psUri = pwm.getPseudonym().getPseudonymUID();
        mpwm.put(psUri, pwm);
      }
    }
  }

  public IssuanceTokenDescription getIssuanceToken() {
    return itd; 
  }
  
  public void updateIssuerToRevocationInformationUidMap(Map<URI, URI> toUpdate) {
    pp.updateIssuerToRevocationInformationUidMap(toUpdate);
  }

  public static UiPresentationArguments prepareUiPresentationArguments(
      List<MyCandidateToken> candidateTokens, KeyManager km, ContextGenerator contextGenerator) {
    UiPresentationArguments arg = new UiPresentationArguments();
    
    TokenCandidatePerPolicy tcpp = null;
    URI policyId = null;
    for(MyCandidateToken token: candidateTokens) {
      if(!token.ptd.getPolicyUID().equals(policyId)) {
        if(tcpp!=null) {
          arg.addTokenCandidate(tcpp);
        }
        tcpp = new TokenCandidatePerPolicy();
        policyId = token.ptd.getPolicyUID();
        tcpp.policy = token.pp.getPolicy();
        populatePolicy(arg.data, tcpp.policy, km);
      }
      token.populate(arg.data, tcpp);
    }
    if(tcpp!=null) {
      arg.addTokenCandidate(tcpp);
    }
    arg.uiContext = contextGenerator.getUniqueContext(URI.create("ui-context-p/"));
    
    return arg;
  }

  private static void populatePolicy(UiCommonArguments data, PresentationPolicy policy, KeyManager km) {
    List<URI> neededIssuers = new ArrayList<URI>();
    for(CredentialInPolicy cip: policy.getCredential()) {
      for(IssuerParametersUID ipu: cip.getIssuerAlternatives().getIssuerParametersUID()) {
        neededIssuers.add(ipu.getValue());
      }
    }
    List<URI> neededCredSpecs = new ArrayList<URI>();
    for(CredentialInPolicy cip: policy.getCredential()) {
      for(URI csid: cip.getCredentialSpecAlternatives().getCredentialSpecUID()) {
        neededCredSpecs.add(csid);
      }
    }
    for(URI u: neededIssuers) {
      try {
        if(u == null) {
          continue;
        }
        IssuerParameters ip = km.getIssuerParameters(u);
        if(ip == null) {
          continue;
        }
        data.addIssuer(new IssuerInUi(ip));
      } catch(KeyManagerException kme) {
        //Ignore
        continue;
      }
    }
    for(URI credUri: neededCredSpecs) {
      try {
        if(credUri == null) {
          continue;
        }
        CredentialSpecification spec = km.getCredentialSpecification(credUri);
        if(spec == null) {
          continue;
        }
        data.addCredentialSpec(new CredentialSpecInUi(spec));
      } catch(KeyManagerException kme) {
        //Ignore
        continue;
      }
    }
  }
  
  private static void populatePolicy(UiCommonArguments data, IssuancePolicy policy, KeyManager km) {
    populatePolicy(data, policy.getPresentationPolicy(), km);
    try {
      URI issuri = policy.getCredentialTemplate().getIssuerParametersUID();
      if(issuri == null) {
        return;
      }
      IssuerParameters ip = km.getIssuerParameters(issuri);
      if(ip == null) {
        return;
      }
      data.addIssuer(new IssuerInUi(ip));
    } catch(KeyManagerException kme) {
      // Ignore
      return;
    }
    try {
      URI credSpec = policy.getCredentialTemplate().getCredentialSpecUID();
      if(credSpec == null) {
        return;
      }
      CredentialSpecification spec = km.getCredentialSpecification(credSpec);
      if(spec == null) {
        return;
      }
      data.addCredentialSpec(new CredentialSpecInUi(spec));
    } catch(KeyManagerException kme) {
      // Ignore
      return;
    }
  }

  private void populate(UiCommonArguments data, AddTokenCandidate atc) {
    
    TokenCandidate tc = new TokenCandidate();
    Map<URI,CredentialSpecification> uriCredspecs = new HashMap<URI, CredentialSpecification>();
    
    // Token description
    tc.tokenDescription = ptd;
    
    // Credentials
    for (MyCredentialDescription cd: creds) {
      CredentialInUi ciu = new CredentialInUi(cd.getCredentialDescription(), cd.getIssuerParameters(), cd.getCredSpec(), cd.getRevocationAuthorityParameters());
      //addFriendlyDescriptions(ciu);
      uriCredspecs.put(cd.getCredSpec().getSpecificationUID(), cd.getCredSpec());
      data.addCredential(ciu);
      tc.credentials.add(ciu);
    }
    
    // Pseudonym candidates
    for (List<PseudonymWithMetadata> lpwm: pseudonyms) {
      PseudonymListCandidate plc = new PseudonymListCandidate();
      for(PseudonymWithMetadata pwm: lpwm) {
        PseudonymInUi piu = new PseudonymInUi(pwm);
        data.addPseudonym(piu);
        plc.pseudonyms.add(piu);
      }
      tc.addPseudonymCandidate(plc);
    }
    
    // Get revealed facts and attribute values    
    RevealedFactsAndAttributeValues rfats = RevealedAttrsAndFactsdDescrGenerator.generateFriendlyDesciptions(ptd, uriCredspecs);
    
    // Revealed facts
    tc.revealedAttributeValues = rfats.revealedAttributeValues;
    
    // Revealed attribute values
    tc.revealedFacts = rfats.revealedFacts;
    
    // Inspectable attributes
    for(List<MyInspectableAttribute> lmia: inspectors) {
      InspectableAttribute insa = new InspectableAttribute();
      if(lmia.size() != 0) {
        insa.credential = tc.credentials.get(lmia.get(0).credential);
        insa.attributeType = lmia.get(0).attributeType;
        insa.inspectionGrounds = lmia.get(0).inspectionGrounds;
        insa.dataHandlingPolicy = lmia.get(0).dataHandlingPolicy;
        for(MyInspectableAttribute mia: lmia) {
          InspectorInUi iiu = new InspectorInUi(mia.ipk);
          insa.inspectorAlternatives.add(iiu);
          data.addInspector(iiu);
        }
      } else {
        throw new RuntimeException("Inspectable attributes empty");
      }
      tc.inspectableAttributes.add(insa);
    }
    
    atc.addTokenCandidate(tc);
  }
/*
  private void addFriendlyDescriptions(CredentialInUi ciu) {
    
    final String DEFAULT_LANG = "en";
    
    Map<String, String> ownershipInfo = new HashMap<String, String>();
    ownershipInfo.put("en", "You reveal that you own a credential of type '%s' issued by '%s'.");
    ownershipInfo.put("sv", "Du visar att du \u00e4ger en referens av typen '%s' som utf\u00e4rdas av '%s'.");
    ownershipInfo.put("el", "\u0398\u03b1 \u03b1\u03c0\u03bf\u03ba\u03b1\u03bb\u03cd\u03c8\u03b5\u03b9 \u03cc\u03c4\u03b9 \u03ad\u03c7\u03b5\u03c4\u03b5 \u03c3\u03c4\u03b7\u03bd \u03ba\u03b1\u03c4\u03bf\u03c7\u03ae \u03c3\u03b1\u03c2 \u03bc\u03b9\u03b1 \u03c0\u03b9\u03c3\u03c4\u03bf\u03c0\u03bf\u03af\u03b7\u03c3\u03b7 \u03c4\u03bf\u03c5 \u03c4\u03cd\u03c0\u03bf\u03c5 '%s' \u03c0\u03bf\u03c5 \u03b5\u03ba\u03b4\u03af\u03b4\u03b5\u03c4\u03b1\u03b9 \u03b1\u03c0\u03cc \u03c4\u03b7\u03bd '%s'.");
    
    Map<String, String> validityInfo = new HashMap<String, String>();
    validityInfo.put("en", "You reveal that your credential has not been revoked by '%s'.");
    validityInfo.put("sv", "Du visar att din referens inte har \u00e5terkallats av '%s'.");
    validityInfo.put("el", "\u039c\u03c0\u03bf\u03c1\u03b5\u03af\u03c4\u03b5 \u03b1\u03c0\u03bf\u03ba\u03b1\u03bb\u03cd\u03c0\u03c4\u03bf\u03c5\u03bd \u03cc\u03c4\u03b9 \u03c4\u03b1 \u03b4\u03b9\u03b1\u03c0\u03b9\u03c3\u03c4\u03b5\u03c5\u03c4\u03ae\u03c1\u03b9\u03ac \u03c3\u03b1\u03c2 \u03b4\u03b5\u03bd \u03ad\u03c7\u03b5\u03b9 \u03b1\u03bd\u03b1\u03ba\u03bb\u03b7\u03b8\u03b5\u03af \u03b1\u03c0\u03cc \u03c4\u03bf '%s'.");
    
    Map<String, String> typeName = new HashMap<String, String>();
    Map<String, String> issuerName = new HashMap<String, String>();
    Map<String, String> raName = new HashMap<String, String>();
    
    for(FriendlyDescription fd: ciu.issuer.description) {
      issuerName.put(fd.getLang(), fd.getValue());
    }
    if(ciu.revocationAuthority != null) {
      for(FriendlyDescription fd: ciu.revocationAuthority.description) {
        raName.put(fd.getLang(), fd.getValue());
      }
    }
    for(FriendlyDescription fd: ciu.spec.spec.getFriendlyCredentialName()) {
      typeName.put(fd.getLang(), fd.getValue());
    }
    
    for(String lang: ownershipInfo.keySet()) {
      FriendlyDescription fd = new FriendlyDescription();
      fd.setLang(lang);
      String type = typeName.get(lang);
      String issuer = issuerName.get(lang);
      if(type == null) {
        type = typeName.get(DEFAULT_LANG);
        if(type == null) {
          type = ciu.spec.uri;
        }
      }
      if(issuer == null) {
        issuer = typeName.get(DEFAULT_LANG);
        if(issuer == null) {
          type = ciu.issuer.uri;
        }
      }
      String formatString = ownershipInfo.get(lang);
      fd.setLang(lang);
      fd.setValue(String.format(formatString, type, issuer));
      ciu.ownershipInfos.add(fd);
    }
    
    if(ciu.spec.spec.isRevocable()) {
      for(String lang: validityInfo.keySet()) {
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang(lang);
        String ra = raName.get(lang);
        if(ra == null) {
          ra = raName.get(DEFAULT_LANG);
          if(ra == null) {
            if(ciu.revocationAuthority != null) {
              ra = ciu.revocationAuthority.uri;
            } else if (ciu.issuer.revocationAuthorityUri != null) {
              ra = ciu.issuer.revocationAuthorityUri.toString();
            } else {
              ra = "(???)";
            }
          }
        }
        String formatString = validityInfo.get(lang);
        fd.setLang(lang);
        fd.setValue(String.format(formatString, ra));
        ciu.validityInfos.add(fd);
      }
    }
  }
  */

  public static UiIssuanceArguments prepareUiIssuanceArguments(List<MyCandidateToken> candidateTokens,
      KeyManager km, ContextGenerator contextGenerator) {
    UiIssuanceArguments arg = new UiIssuanceArguments();
    for(MyCandidateToken token: candidateTokens) {
      token.populate(arg.data, arg);
    }
    if (candidateTokens.size() != 0) {
      arg.policy.setCredentialTemplate(candidateTokens.get(0).itd.getCredentialTemplate());
      arg.policy.setPresentationPolicy(candidateTokens.get(0).pp.getPolicy());
      arg.policy.setVersion("1.0");
      populatePolicy(arg.data, arg.policy, km);
    } else {
      throw new RuntimeException("Cannot satisfy policy (no candidate tokens).");
    }
    arg.uiContext = contextGenerator.getUniqueContext(URI.create("ui-context-i/"));
    
    return arg;
  }
}

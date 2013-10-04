//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;

@XmlRootElement(name="SptdArguments", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
@XmlAccessorType(XmlAccessType.FIELD)
public class SptdArguments {
  public Map<URI, PolicyDescription> policies;
  public Map<URI, CredentialDescription> credentialDescriptions;
  public Map<URI, PseudonymDescription> pseudonyms;
  public Map<URI, InspectorDescription> inspectors;
  public List<PresentationTokenDescription> tokens;
  public List<ArrayList<URI>> credentialUids;
  public List<HashSet<ArrayList<URI>>> pseudonymChoice;
  public List<ArrayList<HashSet<URI>>> inspectorChoice;
  
  public SptdArguments() { 
  }
  
  public SptdArguments(Map<URI, PolicyDescription> policies,
                       Map<URI, CredentialDescription> credentialDescriptions,
                       Map<URI, PseudonymDescription> pseudonyms,
                       Map<URI, InspectorDescription> inspectors,
                       List<PresentationTokenDescription> tokens,
                       List<List<URI>> credentialUids, List<Set<List<URI>>> pseudonymChoice,
                       List<List<Set<URI>>> inspectorChoice) {
    this.policies = policies;
    this.credentialDescriptions = credentialDescriptions;
    this.pseudonyms = pseudonyms;
    this.inspectors = inspectors;
    this.tokens = tokens;
    this.credentialUids = new ArrayList<ArrayList<URI>>();
    for (List<URI> lu: credentialUids) {
      this.credentialUids.add(new ArrayList<URI>(lu));
    }
    this.pseudonymChoice = new ArrayList<HashSet<ArrayList<URI>>>();
    for(Set<List<URI>> slu: pseudonymChoice) {
      HashSet<ArrayList<URI>> res = new HashSet<ArrayList<URI>>();
      for(List<URI> lu: slu) {
        res.add(new ArrayList<URI>(lu));
      }
      this.pseudonymChoice.add(res);
    }
    this.inspectorChoice = new ArrayList<ArrayList<HashSet<URI>>>();
    for(List<Set<URI>> slu: inspectorChoice) {
      ArrayList<HashSet<URI>> res = new ArrayList<HashSet<URI>>();
      for(Set<URI> lu: slu) {
        res.add(new HashSet<URI>(lu));
      }
      this.inspectorChoice.add(res);
    }
  }
}

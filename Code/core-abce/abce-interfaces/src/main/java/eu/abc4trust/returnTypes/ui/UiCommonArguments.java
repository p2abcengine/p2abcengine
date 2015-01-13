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

package eu.abc4trust.returnTypes.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class UiCommonArguments {
  @XmlElementWrapper
  @XmlElement(name = "credentialSpecification")
  public List<CredentialSpecInUi> credentialSpecifications;
  
  @XmlElementWrapper
  @XmlElement(name = "issuer")
  public List<IssuerInUi> issuers;
  
  @XmlElementWrapper
  @XmlElement(name = "revocationAuthority")
  public List<RevocationAuthorityInUi> revocationAuthorities;
  
  @XmlElementWrapper
  @XmlElement(name = "credential")
  public List<CredentialInUi> credentials;
  
  @XmlElementWrapper
  @XmlElement(name = "pseudonym")
  public List<PseudonymInUi> pseudonyms;
  
  @XmlElementWrapper
  @XmlElement(name = "inspector")
  public List<InspectorInUi> inspectors;
  
  private transient Set<String> credentialUris;
  private transient Set<String> issuerUris;
  private transient Set<String> revocationUris;
  private transient Set<String> credentialSpecUris;
  private transient Set<String> pseudonymUris;
  private transient Set<String> inspectorUris;
  
  public UiCommonArguments() {
    this.credentialSpecifications = new ArrayList<CredentialSpecInUi>();
    this.issuers = new ArrayList<IssuerInUi>();
    this.revocationAuthorities = new ArrayList<RevocationAuthorityInUi>();
    this.credentials = new ArrayList<CredentialInUi>();
    this.pseudonyms = new ArrayList<PseudonymInUi>();
    this.inspectors = new ArrayList<InspectorInUi>();
    
    this.credentialUris = new HashSet<String>();
    this.issuerUris = new HashSet<String>();
    this.revocationUris = new HashSet<String>();
    this.credentialSpecUris = new HashSet<String>();
    this.pseudonymUris = new HashSet<String>();
    this.inspectorUris = new HashSet<String>();
  }
  
  public void addCredential(CredentialInUi cred) {
    if(credentialUris.add(cred.uri)) {
      credentials.add(cred);
      addIssuer(cred.issuer);
      addCredentialSpec(cred.spec);
      if(cred.revocationAuthority != null) {
        addRevocationAuthority(cred.revocationAuthority);
      }
    }
  }
  public void addIssuer(IssuerInUi iss) {
    if(issuerUris.add(iss.uri)) {
      issuers.add(iss);
    }
  }
  
  public void addCredentialSpec(CredentialSpecInUi cs) {
    if(credentialSpecUris.add(cs.uri)) {
      credentialSpecifications.add(cs);
    }
  }
  
  public void addRevocationAuthority(RevocationAuthorityInUi ra) {
    if(revocationUris.add(ra.uri)) {
      revocationAuthorities.add(ra);
    }
  }
  
  public void addPseudonym(PseudonymInUi ps) {
    if(pseudonymUris.add(ps.uri)) {
      pseudonyms.add(ps);
    }
  }
  
  public void addInspector(InspectorInUi ins) {
    if(inspectorUris.add(ins.uri)) {
      inspectors.add(ins);
    }
  }

  @Override
  public String toString() {
    return "UiCommonArguments [credentialSpecifications=" + credentialSpecifications + ", issuers="
        + issuers + ", revocationAuthorities=" + revocationAuthorities + ", credentials="
        + credentials + ", pseudonyms=" + pseudonyms + ", inspectors=" + inspectors + "]";
  }
}

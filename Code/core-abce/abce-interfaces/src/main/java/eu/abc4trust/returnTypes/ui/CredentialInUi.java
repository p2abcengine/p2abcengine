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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.CredentialSpecAdapter;
import eu.abc4trust.returnTypes.ui.adapters.IssuerAdapter;
import eu.abc4trust.returnTypes.ui.adapters.RevocationAuthorityAdapter;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class CredentialInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElement
  public CredentialDescription desc;
  
  /*@XmlElementWrapper
  @XmlElement(name = "ownershipInfo")
  public List<FriendlyDescription> ownershipInfos;
  
  @XmlElementWrapper
  @XmlElement(name = "validityInfo")
  public List<FriendlyDescription> validityInfos;*/
  
  @XmlElement
  @XmlJavaTypeAdapter(RevocationAuthorityAdapter.class)
  public RevocationAuthorityInUi revocationAuthority;
  
  @XmlElement
  @XmlJavaTypeAdapter(CredentialSpecAdapter.class)
  public CredentialSpecInUi spec;
  
  @XmlElement
  @XmlJavaTypeAdapter(IssuerAdapter.class)
  public IssuerInUi issuer;
  
  
  public CredentialInUi() {
    //this.ownershipInfos = new ArrayList<FriendlyDescription>();
    //this.validityInfos = new ArrayList<FriendlyDescription>();
  }
  
  public CredentialInUi(CredentialDescription desc, IssuerParameters ip, CredentialSpecification spec, RevocationAuthorityParameters rap) {
    this.uri = desc.getCredentialUID().toString();
    this.desc = desc;
    //this.ownershipInfos = new ArrayList<FriendlyDescription>();
    //this.validityInfos = new ArrayList<FriendlyDescription>();
    this.issuer = new IssuerInUi(ip);
    if(rap != null) {
      this.revocationAuthority = new RevocationAuthorityInUi(rap);
    }
    this.spec = new CredentialSpecInUi(spec);
  }

  @Override
  public String toString() {
    return "CredentialInUi [uri=" + uri + ", desc=" + desc + /*", ownershipInfos=" + ownershipInfos
        + ", validityInfos=" + validityInfos +*/ ", revocationAuthority=" + revocationAuthority
        + ", spec=" + spec + ", issuer=" + issuer + "]";
  }
}

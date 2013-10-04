//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.CredentialSpecAdapter;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class IssuerInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElement(required=false)
  public URI revocationAuthorityUri;
  
  @XmlElementWrapper
  @XmlElement(name = "description")
  public List<FriendlyDescription> description;
  
  @XmlElement
  @XmlJavaTypeAdapter(CredentialSpecAdapter.class)
  public CredentialSpecInUi spec;
  
  
  public IssuerInUi() {
    this.description = new ArrayList<FriendlyDescription>();
  }
  
  public IssuerInUi(IssuerParameters ip, CredentialSpecification spec) {
    this.uri = ip.getParametersUID().toString();
    this.description = ip.getFriendlyIssuerDescription();
    this.spec = new CredentialSpecInUi(spec);
    this.revocationAuthorityUri = ip.getRevocationParametersUID();
  }

  @Override
  public String toString() {
    return "IssuerInUi [uri=" + uri + ", description=" + description + ", spec=" + spec + "]";
  }
}

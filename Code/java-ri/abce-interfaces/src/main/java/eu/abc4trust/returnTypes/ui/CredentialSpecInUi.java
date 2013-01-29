//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import eu.abc4trust.xml.CredentialSpecification;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class CredentialSpecInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElement
  public CredentialSpecification spec;
  
  
  public CredentialSpecInUi() {
  }
  
  public CredentialSpecInUi(CredentialSpecification spec) {
    this.uri = spec.getSpecificationUID().toString();
    this.spec = spec;
  }

  @Override
  public String toString() {
    return "CredentialSpecInUi [uri=" + uri + ", spec=" + spec + "]";
  }
}

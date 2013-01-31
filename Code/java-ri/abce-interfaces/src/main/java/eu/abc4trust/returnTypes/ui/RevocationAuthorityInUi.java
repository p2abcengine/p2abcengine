//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.RevocationAuthorityParameters;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class RevocationAuthorityInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElementWrapper
  @XmlElement(name = "description")
  public List<FriendlyDescription> description;
  
  public RevocationAuthorityInUi() {
    this.description = new ArrayList<FriendlyDescription>();
  }
  
  
  public RevocationAuthorityInUi(RevocationAuthorityParameters rap) {
    this.uri = rap.getParametersUID().toString();
    // TODO(enr): Add description to RevocationAuthorityParameters instead of providing dummy one
    description = new ArrayList<FriendlyDescription>();
    FriendlyDescription dummyDescription = new FriendlyDescription();
    dummyDescription.setLang("en");
    dummyDescription.setValue("RevAuth-" + uri);
    description.add(dummyDescription);
  }


  @Override
  public String toString() {
    return "RevocationAuthorityInUi [uri=" + uri + ", description=" + description + "]";
  }
}

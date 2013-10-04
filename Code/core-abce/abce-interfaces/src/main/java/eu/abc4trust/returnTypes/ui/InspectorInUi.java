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
import eu.abc4trust.xml.InspectorPublicKey;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class InspectorInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElementWrapper
  @XmlElement(name = "description")
  public List<FriendlyDescription> description;
  
  
  public InspectorInUi() {
    this.description = new ArrayList<FriendlyDescription>();
  }
  
  public InspectorInUi(InspectorPublicKey ipk) {
    this.uri = ipk.getPublicKeyUID().toString();
    this.description = ipk.getFriendlyInspectorDescription();
  }

  @Override
  public String toString() {
    return "InspectorInUi [uri=" + uri + ", description=" + description + "]";
  }
}

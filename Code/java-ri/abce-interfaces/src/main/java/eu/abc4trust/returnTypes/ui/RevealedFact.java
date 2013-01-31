//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import eu.abc4trust.xml.FriendlyDescription;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class RevealedFact {

  @XmlElementWrapper
  @XmlElement(name = "description")
  public List<FriendlyDescription> descriptions;
  
  
  public RevealedFact() {
    this.descriptions = new ArrayList<FriendlyDescription>();
  }


  @Override
  public String toString() {
    return "RevealedFact [descriptions=" + descriptions + "]";
  }
}

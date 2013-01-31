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

import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class PseudonymInUi {
  @XmlID
  @XmlAttribute
  public String uri;
  
  @XmlElement
  public Pseudonym pseudonym;
  
  @XmlElement
  public PseudonymMetadata metadata;
  
  
  public PseudonymInUi() {
  }
  
  public PseudonymInUi(PseudonymWithMetadata pwm) {
    this.uri = pwm.getPseudonym().getPseudonymUID().toString();
    this.pseudonym = pwm.getPseudonym();
    this.metadata = pwm.getPseudonymMetadata();
  }

  @Override
  public String toString() {
    return "PseudonymInUi [uri=" + uri + ", pseudonym=" + pseudonym + ", metadata=" + metadata
        + "]";
  }
}

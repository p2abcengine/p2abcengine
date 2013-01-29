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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.PseudonymAdapter;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class PseudonymListCandidate {

  @XmlAttribute
  public int candidateId;
  
  @XmlElementWrapper
  @XmlElement(name="pseudonym")
  @XmlJavaTypeAdapter(PseudonymAdapter.class)
  public List<PseudonymInUi> pseudonyms;
  
  
  public PseudonymListCandidate() {
    this.pseudonyms = new ArrayList<PseudonymInUi>();
  }


  @Override
  public String toString() {
    return "PseudonymListCandidate [candidateId=" + candidateId + ", pseudonyms=" + pseudonyms
        + "]";
  }
}

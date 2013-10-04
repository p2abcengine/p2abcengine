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

@XmlType
public class RevealedFactsAndAttributeValues {

  
  @XmlElementWrapper
  @XmlElement(name="revealedFact")
  public List<RevealedFact> revealedFacts;
  
  @XmlElementWrapper
  @XmlElement(name="revealedAttributeValue")
  public List<RevealedAttributeValue> revealedAttributeValues;
  
  
  public RevealedFactsAndAttributeValues() {
    this.revealedFacts = new ArrayList<RevealedFact>();
    this.revealedAttributeValues = new ArrayList<RevealedAttributeValue>();
  }
  
}

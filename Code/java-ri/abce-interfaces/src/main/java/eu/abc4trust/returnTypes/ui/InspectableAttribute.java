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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.abc4trust.returnTypes.ui.adapters.CredentialAdapter;
import eu.abc4trust.returnTypes.ui.adapters.InspectorAdapter;

@XmlType(namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class InspectableAttribute {

  @XmlElement
  @XmlJavaTypeAdapter(CredentialAdapter.class)
  public CredentialInUi credential;
  
  @XmlElement
  public URI attributeType;
  
  @XmlElement
  public URI dataHandlingPolicy;
  
  @XmlElement
  public String inspectionGrounds;
  
  @XmlElementWrapper
  @XmlElement(name="inspectorAlternative")
  @XmlJavaTypeAdapter(InspectorAdapter.class)
  public List<InspectorInUi> inspectorAlternatives;
  
  
  public InspectableAttribute() {
    this.inspectorAlternatives = new ArrayList<InspectorInUi>();
  }


  @Override
  public String toString() {
    return "InspectableAttribute [credential=" + credential + ", attributeType=" + attributeType
        + ", dataHandlingPolicy=" + dataHandlingPolicy + ", inspectionGrounds=" + inspectionGrounds
        + ", inspectorAlternatives=" + inspectorAlternatives + "]";
  }
}

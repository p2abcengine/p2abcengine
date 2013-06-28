//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.returnTypes.ui.UiCommonArguments;

@XmlRootElement(name="UiManageCredentialData", namespace = "http://abc4trust.eu/wp2/abcschemav1.0") 
public class UiManageCredentialData {
  
  @XmlElement
  public UiCommonArguments data;

  public Map<URI, Boolean> revokedCredentials;
  
  public UiManageCredentialData() {
    this.data = new UiCommonArguments();
    this.revokedCredentials = new HashMap<URI, Boolean>();
  }
  
  @Override
  public String toString() {
    return "UiManageCredentialData [data=" + data + ", revokedCredentials=" + revokedCredentials + "]";
  }
}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.net.URI;

import eu.abc4trust.xml.InspectorPublicKey;

public class MyInspectableAttribute {
  public final InspectorPublicKey ipk;
  public final int credential;
  public final URI attributeType;
  public final URI dataHandlingPolicy;
  public final String inspectionGrounds;
  
  public MyInspectableAttribute(InspectorPublicKey ipk, int credential, URI attributeType,
                                URI dataHandlingPolicy, String inspectionGrounds) {
    this.ipk = ipk;
    this.credential = credential;
    this.attributeType = attributeType;
    this.dataHandlingPolicy = dataHandlingPolicy;
    this.inspectionGrounds = inspectionGrounds;
  }
}

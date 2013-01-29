//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.InspectorInUi;
import eu.abc4trust.returnTypes.ui.ref.InspectorRef;

public class InspectorAdapter extends XmlAdapter<InspectorRef, InspectorInUi> {

  @Override
  public InspectorRef marshal(InspectorInUi arg0) throws Exception {
    return new InspectorRef(arg0);
  }

  @Override
  public InspectorInUi unmarshal(InspectorRef arg0) throws Exception {
    return arg0.inspector;
  }

}

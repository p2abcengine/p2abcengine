//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.PseudonymInUi;
import eu.abc4trust.returnTypes.ui.ref.PseudonymRef;

public class PseudonymAdapter extends XmlAdapter<PseudonymRef, PseudonymInUi> {

  @Override
  public PseudonymRef marshal(PseudonymInUi arg0) throws Exception {
    return new PseudonymRef(arg0);
  }

  @Override
  public PseudonymInUi unmarshal(PseudonymRef arg0) throws Exception {
    return arg0.pseudonym;
  }

}

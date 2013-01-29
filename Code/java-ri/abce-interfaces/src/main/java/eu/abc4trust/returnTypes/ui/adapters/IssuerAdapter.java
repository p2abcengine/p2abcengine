//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.IssuerInUi;
import eu.abc4trust.returnTypes.ui.ref.IssuerRef;

public class IssuerAdapter extends XmlAdapter<IssuerRef, IssuerInUi> {

  @Override
  public IssuerRef marshal(IssuerInUi arg0) throws Exception {
    return new IssuerRef(arg0);
  }

  @Override
  public IssuerInUi unmarshal(IssuerRef arg0) throws Exception {
    return arg0.issuer;
  }

}

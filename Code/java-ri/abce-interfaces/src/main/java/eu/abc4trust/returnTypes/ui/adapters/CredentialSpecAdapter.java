//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.CredentialSpecInUi;
import eu.abc4trust.returnTypes.ui.ref.CredentialSpecRef;

public class CredentialSpecAdapter extends XmlAdapter<CredentialSpecRef, CredentialSpecInUi>{

  @Override
  public CredentialSpecRef marshal(CredentialSpecInUi arg0) throws Exception {
    return new CredentialSpecRef(arg0);
  }

  @Override
  public CredentialSpecInUi unmarshal(CredentialSpecRef arg0) throws Exception {
    return arg0.credSpec;
  }

}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.ref.CredentialRef;

public class CredentialAdapter extends XmlAdapter<CredentialRef, CredentialInUi>{

  @Override
  public CredentialRef marshal(CredentialInUi arg0) throws Exception {
    return new CredentialRef(arg0);
  }

  @Override
  public CredentialInUi unmarshal(CredentialRef arg0) throws Exception {
    return arg0.credDesc;
  }

}

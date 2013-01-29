//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.returnTypes.ui.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.abc4trust.returnTypes.ui.RevocationAuthorityInUi;
import eu.abc4trust.returnTypes.ui.ref.RevocationAuthorityRef;

public class RevocationAuthorityAdapter extends XmlAdapter<RevocationAuthorityRef, RevocationAuthorityInUi>{

  @Override
  public RevocationAuthorityRef marshal(RevocationAuthorityInUi arg0) throws Exception {
    return new RevocationAuthorityRef(arg0);
  }

  @Override
  public RevocationAuthorityInUi unmarshal(RevocationAuthorityRef arg0) throws Exception {
    return arg0.revocationAuthority;
  }

}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ui.idSelection;

import eu.abc4trust.returnTypes.SitdArguments;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdArguments;
import eu.abc4trust.returnTypes.SptdReturn;

public interface IdentitySelectionXml {
  public SptdReturn selectPresentationTokenDescription(SptdArguments args);
  public SitdReturn selectIssuanceTokenDescription(SitdArguments args);
}

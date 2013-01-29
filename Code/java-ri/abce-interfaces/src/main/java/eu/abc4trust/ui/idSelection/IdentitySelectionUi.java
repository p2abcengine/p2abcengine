//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ui.idSelection;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;

public interface IdentitySelectionUi {
  public UiPresentationReturn selectPresentationTokenDescription(UiPresentationArguments args);
  public UiIssuanceReturn selectIssuanceTokenDescription(UiIssuanceArguments args);
}

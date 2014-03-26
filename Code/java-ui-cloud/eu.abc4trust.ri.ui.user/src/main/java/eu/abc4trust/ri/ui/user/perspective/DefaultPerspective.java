//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.14                                           *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import eu.abc4trust.ri.ui.user.view.CredentialAttributesView;
import eu.abc4trust.ri.ui.user.view.CredentialListView;
import eu.abc4trust.ri.ui.user.view.IdentitySelectionView;
import eu.abc4trust.ri.ui.user.view.RedirectView;

/**
 * Configures the perspective layout. This class is contributed through the
 * plugin.xml.
 */
public class DefaultPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		layout.addView(IdentitySelectionView.ID, IPageLayout.LEFT, 1.0f, layout.getEditorArea());
		layout.addView(CredentialListView.ID, IPageLayout.BOTTOM, 0.35f, IdentitySelectionView.ID);
		layout.addView(CredentialAttributesView.ID, IPageLayout.BOTTOM, 0.35f, CredentialListView.ID);
		layout.addView(RedirectView.ID, IPageLayout.RIGHT, 1.0f, layout.getEditorArea());
		
		layout.getViewLayout(IdentitySelectionView.ID).setCloseable(false);
		layout.getViewLayout(CredentialListView.ID).setCloseable(false);
		layout.getViewLayout(CredentialAttributesView.ID).setCloseable(false);
		layout.getViewLayout(RedirectView.ID).setCloseable(false);
	}
}

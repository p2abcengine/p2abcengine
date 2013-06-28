//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import eu.abc4trust.ri.ui.user.view.CredentialAttributesView;
import eu.abc4trust.ri.ui.user.view.CredentialListView;
import eu.abc4trust.ri.ui.user.view.IdentitySelectionView;

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
		
		layout.getViewLayout(IdentitySelectionView.ID).setCloseable(false);
		layout.getViewLayout(CredentialListView.ID).setCloseable(false);
		layout.getViewLayout(CredentialAttributesView.ID).setCloseable(false);
	}
}

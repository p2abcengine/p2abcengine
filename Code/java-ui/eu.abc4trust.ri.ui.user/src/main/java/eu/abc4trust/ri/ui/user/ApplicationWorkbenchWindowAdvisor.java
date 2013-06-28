//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.UIMode;
import eu.abc4trust.ri.ui.user.view.CredentialAttributesView;
import eu.abc4trust.ri.ui.user.view.CredentialListView;
import eu.abc4trust.ri.ui.user.view.IdentitySelectionView;

/**
 * Configures the initial size and appearance of a workbench window.
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		configurer.setTitle(Messages.get().ApplicationWorkbenchWindowAdvisor_applicationTitle);
		configurer.setShellStyle(SWT.TITLE); // SWT.RESIZE | SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.MIN // SWT.NOTRIM
		configurer.setShowMenuBar(false);
	}
	
	@Override
	public void postWindowCreate() {
		Shell shell = getWindowConfigurer().getWindow().getShell();
		shell.setMaximized(true);
		
		IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();
		ApplicationParameters ap = ApplicationParameters.getSessionSingletonInstance();
		
		if (ap.getUIMode().equals(UIMode.MANAGEMENT)) {
		    page.hideView(page.findViewReference(IdentitySelectionView.ID));
		    //page.setPartState(page.findViewReference(IdentitySelectionView.ID), IWorkbenchPage.STATE_MINIMIZED);
		    
		} else {
		    page.hideView(page.findViewReference(CredentialListView.ID));
		    //page.setPartState(page.findViewReference(CredentialListView.ID), IWorkbenchPage.STATE_MINIMIZED);
		    page.hideView(page.findViewReference(CredentialAttributesView.ID));
		    //page.setPartState(page.findViewReference(CredentialAttributesView.ID), IWorkbenchPage.STATE_MINIMIZED);
		}
	}
	
	@Override
	public void postWindowClose() {
		super.postWindowClose();
		//System.out.println("Window closed.");
	}
}

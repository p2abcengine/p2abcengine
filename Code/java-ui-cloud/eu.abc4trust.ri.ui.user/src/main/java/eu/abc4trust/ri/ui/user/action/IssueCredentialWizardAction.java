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

package eu.abc4trust.ri.ui.user.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import eu.abc4trust.ri.ui.user.utils.ResourceRegistryStore;
import eu.abc4trust.ri.ui.user.wizard.IssueWizard;
import eu.abc4trust.ri.ui.user.wizard.IssueWizardDialog;

public class IssueCredentialWizardAction extends Action implements
		IWorkbenchAction {
	private static final long serialVersionUID = -6683414625835170929L;

	private static final String ID = "IssueCredentialWizardAction";
	private final TableViewer viewer;
	private final Shell shell;

	public IssueCredentialWizardAction(Shell shell, TableViewer viewer) {
		this.shell = shell;
		this.viewer = viewer;
		setId(ID);
		setImageDescriptor(ResourceRegistryStore
				.getImageDescriptor(ResourceRegistryStore.IMG_CREDENTIAL_ISSUE_16x16));
		setText("Issue credential");
		setToolTipText("Issue credential");
	}

	public void run() {
		WizardDialog dialog = new IssueWizardDialog(shell, new IssueWizard());

		if (dialog.open() == Window.OK) {
			System.out.println("Ok pressed");
		} else {
			System.out.println("Cancel pressed");
		}

		if (viewer != null) {
			viewer.refresh();
		}
	}

	@Override
	public void dispose() {
	}

}
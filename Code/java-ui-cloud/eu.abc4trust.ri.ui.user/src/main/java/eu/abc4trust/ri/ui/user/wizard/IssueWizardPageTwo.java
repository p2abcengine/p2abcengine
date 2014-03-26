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

package eu.abc4trust.ri.ui.user.wizard;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class IssueWizardPageTwo extends WizardPage {
	private static final long serialVersionUID = -8854045957561327767L;
	
	private Composite container;
	private Label label1;
	private Label label2;

	public IssueWizardPageTwo() {
		super("Getting credential");
		setTitle("Getting credential");
		setDescription("Credential wizard, second page");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 2;

		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		URL url = FileLocator
				.find(bundle, new Path("icons/load.gif"), null);
		Image image = ImageDescriptor.createFromURL(url).createImage();

		label1 = new Label(container, SWT.NONE);
		label1.setImage(image);

		label2 = new Label(container, SWT.NONE);
		label2.setText("Getting crendential, please wait...");

		setControl(container);
		setPageComplete(false);
	}

	public void refreshPage() {
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 1;

		label1.setVisible(false);

		label2.setVisible(false);

		label2 = new Label(container, SWT.NONE);
		label2.setText("Thank you very much");

		setControl(container);
		setPageComplete(true);
	}
}
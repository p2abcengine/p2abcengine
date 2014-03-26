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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class IssueWizardPageOne extends WizardPage {
	private static final long serialVersionUID = 8915571431729627714L;
	
	private Text issuer;
	private Composite container;
	private Text credential;
	private Text username;
	private Text password;
	private IssueWizard wizard;

	public IssueWizardPageOne(IssueWizard wizard) {
		super("Get credential");
		setTitle("Get credential");
		setDescription("Credential wizard, first page");
		this.wizard = wizard;
	}
	
	public String getIssuerRoot() {
		return issuer.getText();
	}
	
	public String getCredentialUid() {
		return credential.getText();
	}
	
	public String getUsername() {
		return username.getText();
	}
	
	public String getPassword() {
		return password.getText();
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Issuer");
		issuer = new Text(container, SWT.BORDER | SWT.SINGLE);
		issuer.setText("http://idmlab06.extranet.nokiasiemensnetworks.com:443/issuer_aas/issuer/external/");

		Label label2 = new Label(container, SWT.NONE);
		label2.setText("Credential");
		credential = new Text(container, SWT.BORDER | SWT.SINGLE);
		credential.setText("urn:fiware:credspec:credIdm");

		Label label3 = new Label(container, SWT.NONE);
		label3.setText("Username at issuer");
		username = new Text(container, SWT.BORDER | SWT.SINGLE);
		username.setText("");

		Label label4 = new Label(container, SWT.NONE);
		label4.setText("Password at issuer");
		password = new Text(container, SWT.BORDER | SWT.SINGLE);
		password.setText("");
		password.setEchoChar('*');

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		issuer.setLayoutData(gd);
		credential.setLayoutData(gd);
		username.setLayoutData(gd);
		password.setLayoutData(gd);
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

		issuer.addKeyListener(new MyKeyListener());
		credential.addKeyListener(new MyKeyListener());
		username.addKeyListener(new MyKeyListener());
		password.addKeyListener(new MyKeyListener());

//		setPageComplete(true);
	}

	private class MyKeyListener implements KeyListener {
		private static final long serialVersionUID = -7144096771922290020L;

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == 13) {
				IssueWizardDialog.ProtocolHandler.doProtocol(wizard);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getSource() instanceof Text) {
				if (!((Text) e.getSource()).getText().isEmpty()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		}
	};

}
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

package eu.abc4trust.ri.ui.user.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Login dialog, which prompts for the user's account info, and has Login and
 * Cancel buttons.
 */
public class LoginDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private CCombo userNameCombo;
	private Text passwordText;
	private Button loginButton;
	
	private static final String DEFAULT_USER = "AliceDoe";
	private static final String DEFAULT_PASSWORD = "alicedoepassword";
	
//    public static URI masterSecretLocation = null;
//    
//    public static URI getMasterSecretLocation() {
//        return masterSecretLocation;
//    }

	public LoginDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.TITLE);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Credential Repository Login");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label accountLabel = new Label(composite, SWT.NONE);
		accountLabel.setText("\nPlease authenticate to access your credential store.\n\n");
		accountLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&User name:");
		userIdLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

		userNameCombo = new CCombo(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		userNameCombo.setLayoutData(gridData);
		userNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				passwordText.setText(DEFAULT_PASSWORD);
			}
		});

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		passwordLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));

		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		// adjust color of combo
		userNameCombo.setBackground(passwordText.getBackground());
		initializeUsers(parent.getDisplay());
		
		return composite;
	}

	private void initializeUsers(Display display) {                   
	    System.out.println("Showing user selection.");                        
	    
	    // reset master secret location
//	    masterSecretLocation = null;
	    
	    userNameCombo.removeAll();
	    userNameCombo.add(DEFAULT_USER);
	        
	    userNameCombo.setText(DEFAULT_USER);
	    passwordText.setText(DEFAULT_PASSWORD);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		//createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.get().CANCEL_LABEL, false);
		loginButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.get().OK_LABEL, true);
		if (loginButton!=null && !loginButton.isDisposed()) {
			loginButton.setFocus();
		}
	}

	protected void okPressed() {
		if (DEFAULT_USER.equals(userNameCombo.getText()) && DEFAULT_PASSWORD.equals(passwordText.getText())) {
			super.okPressed();
		} else {
			MessageDialog.openError(getShell(), "Invalid login", "Please user testuser/testpassword");
			return;
		}
	}
}
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import eu.abc4trust.ri.ui.user.Container;
import eu.abc4trust.ri.ui.user.PresentationContainer;
import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.UIMode;
import eu.abc4trust.ri.ui.user.utils.UIUtil;

public class RedirectView extends ViewPart {

	public static final String ID = "eu.abc4trust.ui.user.view.redirect"; //$NON-NLS-1$
	private Control focusElement;
	
	@Override
	public void createPartControl(Composite parent) {
		if (!ApplicationParameters.getSessionSingletonInstance().getUIMode()
				.equals(UIMode.AUTO_REDIRECT)) {
			focusElement = UIUtil
					.createMessageContent(
							parent,
							"For maintainance reasons, this view is inactive when the UI mode is not '"+UIMode.AUTO_REDIRECT+"'.");
			return;
		}
		
		Composite content = new Composite(parent, SWT.NONE);
        FormLayout formLayout =  new FormLayout();
        formLayout.marginHeight = 5;
        formLayout.marginWidth = 5;
        content.setLayout(formLayout);
        
		PresentationContainer pc = Container.INSTANCE.getAndRemovePresentation(ApplicationParameters.getSessionSingletonInstance().getUserNonce());
        Label label = new Label(content, SWT.NONE);
        if ("not satisfiable".equals(pc.getStringToken())) {
        	label.setText("Sorry, your credentials are not sufficient to satisfy the policy."
        			+ "<br /><br /><a href='" + pc.getFailureUrl() + "' target='_blank'>Return to drop box.</a>");
        	label.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        } else {
        	label.setText("Please wait, you are being redirected...");
        
	        FormData formData = new FormData();
	        formData.left = new FormAttachment(0);
	        formData.right = new FormAttachment(100);
	        formData.top = new FormAttachment(0);
	        label.setLayoutData(formData);
	        
			
			Browser browser = new Browser(content, SWT.NONE);
			//http://localhost:9300/verification/verifyTokenAgainstPolicy/
			//http://idmlab07.extranet.nokiasiemensnetworks.com:443/ExampleDropBox/StoreToken
			try {
				System.out.println("Outgoing user nonce:" + URLDecoder.decode(ApplicationParameters.getSessionSingletonInstance().getUserNonce(), "UTF-8"));
				
				browser.setText("<html>" +
								"<body onload='document.redirectform.submit();'>" +
								"<form name='redirectform' action='" + pc.getSuccessUrl() + "' target='_parent' method='POST'>" +
								"<input type='hidden' name='resource' value='" + pc.getResource() + "'/>" +
								"<input type='hidden' name='policy' value='" + pc.getPolicyXml() + "'/>" +
								"<input type='hidden' name='user-nonce' value='" + URLDecoder.decode(ApplicationParameters.getSessionSingletonInstance().getUserNonce(), "UTF-8") + "'/>" +
								"<input type='hidden' name='token' value='" + pc.getStringToken() + "'/>" +
								"<input type='submit' style='display:none' value='Redirect'/>" +
								"</form>" +
								"</body>" +
					        	"</html>");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			
			formData = new FormData();
			formData.top = new FormAttachment(label);
			formData.left = new FormAttachment(0);
			browser.setLayoutData(formData);
			
			focusElement = label;
			parent.layout(); // causes the browser to be rendered and thus the form in the browser to be submitted
        }
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFocus() {
	    if (focusElement != null) {
            focusElement.setFocus();
        }
	}
}

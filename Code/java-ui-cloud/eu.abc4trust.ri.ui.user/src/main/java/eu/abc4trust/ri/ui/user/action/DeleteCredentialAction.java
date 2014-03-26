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

import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.UiManageCredentialData;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.ri.ui.user.Application;
import eu.abc4trust.ri.ui.user.Container;
import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.ResourceRegistryStore;
import eu.abc4trust.ri.ui.user.utils.UIProperties;
import eu.abc4trust.ri.ui.user.utils.UIUtil;

public class DeleteCredentialAction extends Action implements ISelectionListener, IWorkbenchAction {
    private static final long serialVersionUID = 2938313737070797079L;

    public static final String ID = "eu.abc4trust.ui.user.action.deletecredential";
	
	private final IWorkbenchWindow window;
	private final TableViewer viewer;
	private CredentialInUi selectedCredential;
	private UiManageCredentialData uimd;

	public DeleteCredentialAction(IWorkbenchWindow window, TableViewer viewer, UiManageCredentialData uimd) {
		this.window = window;
		this.viewer = viewer;
		this.uimd = uimd;
		setId(ID);
		setText(Messages.get().DeleteCredentialAction_execute);
		setToolTipText(Messages.get().DeleteCredentialAction_execute);
		setImageDescriptor(ResourceRegistryStore.getImageDescriptor(ResourceRegistryStore.IMG_CREDENTIAL_DELETE_16x16));
		setEnabled(false);
		window.getSelectionService().addSelectionListener(this);
	}
	
	@Override
	public void run() {
	    String hrCredentialName = UIUtil.getHumanReadable(selectedCredential.desc.getFriendlyCredentialName());
	    
	    MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_QUESTION | SWT.NO | SWT.YES);
        messageBox.setText(Messages.get().DeleteCredentialAction_execute);
        messageBox.setMessage(MessageFormat.format(Messages.get().DeleteCredentialAction_safetyQuestion, hrCredentialName));
        int response = messageBox.open();
        if (response == SWT.YES) {
	    
    	    if ( ! ApplicationParameters.getSessionSingletonInstance().isDemo()) {
    	        try {
    	            Client client = Client.create();
    	            Builder deleteCredential =
                        client.resource(Application.userAbceEngineServiceBaseUrl + "/deleteCredential") //$NON-NLS-1$
                        .type(MediaType.TEXT_PLAIN);
    	            deleteCredential.post(selectedCredential.uri);
    	            
    	        } catch (RuntimeException e) {
    	            e.printStackTrace();
                    MessageBox mbox = new MessageBox(window.getShell(), SWT.ICON_ERROR | SWT.OK);
                    mbox.setText(Messages.get().DeleteCredentialAction_caption_errorMessage);
                    mbox.setMessage(Messages.get().DeleteCredentialAction_error_couldNotDeleteCredential);
                    mbox.open();
    	            return;
    	        }
            }
    
    	    if (uimd != null)
    	    	uimd.data.credentials.remove(selectedCredential);
    	    Container.INSTANCE.removeCredential(selectedCredential);
    	    viewer.refresh();
        }
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object o = selection.getFirstElement();
				if (o instanceof CredentialInUi) {
					selectedCredential = (CredentialInUi) o;
					
					if (selectedCredential.desc.isRevokedByIssuer() || UIProperties.getSessionSingletonInstance().allowDeletingNonRevokedCredential()) {
	                    setEnabled(true);
	                    return;
					}
				}
			}
		}
		setEnabled(false);
	}
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
}
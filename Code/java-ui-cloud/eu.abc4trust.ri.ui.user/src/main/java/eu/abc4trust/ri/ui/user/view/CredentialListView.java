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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.UiManageCredentialData;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.ri.ui.user.Application;
import eu.abc4trust.ri.ui.user.Container;
import eu.abc4trust.ri.ui.user.action.DeleteCredentialAction;
import eu.abc4trust.ri.ui.user.action.IssueCredentialWizardAction;
import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.ResourceRegistryStore;
import eu.abc4trust.ri.ui.user.utils.UIMode;
import eu.abc4trust.ri.ui.user.utils.UIUtil;

public class CredentialListView extends ViewPart {
    
	public static final String ID = "eu.abc4trust.ui.user.view.credentiallist"; //$NON-NLS-1$
	
	private UiManageCredentialData uimd = null;
	private TableViewer viewer;
	private Control focusElement;

	private class ViewContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 8695744470921596896L;

		public Object[] getElements(Object parent) {
//			List<CredentialInUi> list = uimd.data.credentials;
//			if (cdui != null)
//				list.add(cdui);
//			for (CredentialInUi cdui : list) {
//				System.out.println(cdui.desc.getFriendlyCredentialName().get(0).getValue());
//			}
//		    return list.toArray();
			return Container.INSTANCE.listCredentials().toArray();
		}
		
		public void dispose() { /* Nothing to dispose. */ }
		public void inputChanged(Viewer v, Object oldInput, Object newInput) { /* Nothing to change. */ }
	}

	private class ViewLabelProvider extends LabelProvider { // implements ITableLabelProvider {
		private static final long serialVersionUID = -6115312984354416184L;
		
//		public String getColumnText(Object obj, int index) {
//			return getText(obj);
//		}
//
//		public Image getColumnImage(Object obj, int index) {
//			return getImage(obj);
//		}

		public Image getImage(Object obj) {
			URL imgURL;
			try {
			    imgURL = ((CredentialInUi) obj).desc.getImageReference().toURL();
				Image thumbnail = ResourceRegistryStore.getThumbnail(imgURL, 32);
				
				return thumbnail;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
		}
		
		@Override
		public String getText(Object element) {
		    String hrn = UIUtil.getHumanReadable(((CredentialInUi) element).desc.getFriendlyCredentialName());
			
			if (hrn!=null && !hrn.isEmpty()) {
				return hrn;
			} else {
				return super.getText(element);
			}
		}
	}

	public void createPartControl(Composite parent) {
	    if ( ! ApplicationParameters.getSessionSingletonInstance().getUIMode().equals(UIMode.MANAGEMENT)) {
	        // TODO Re-activate view as soon as loading of illegal/unreachable images is solved
            focusElement = UIUtil.createMessageContent(parent, "For maintainance reasons, this view is inactive when the UI mode is not 'management'."); 
            return;
	    }
	    
        try {
            uimd = getCredentialManagementData();
         } catch (Exception e) {
             e.printStackTrace();
             focusElement = UIUtil.createMessageContent(parent, MessageFormat.format(Messages.get().IdentitySelectionView_error_invalidInput, e.getMessage()));
             return;
         }
	    
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());        
		viewer.setInput(getViewSite());
		
		makeActions();
		focusElement = viewer.getControl();
		getSite().setSelectionProvider(viewer);
	}
	
    private UiManageCredentialData getCredentialManagementData() throws Exception {
        String MGMGDATA_EXAMPLE = "/xml/management/UiManageCredentialData_Example.xml"; //$NON-NLS-1$
        ApplicationParameters ap = ApplicationParameters.getSessionSingletonInstance();
                        
        if (ap.isDemo()) {
//            uimd = (UiManageCredentialData) XmlUtils.getObjectFromXML(Application.class.getResourceAsStream(MGMGDATA_EXAMPLE), false);
            
        } else {
            // Obtain data from webservice method
            Client client = Client.create();
            Builder getUIData =
                    client.resource(Application.userAbceEngineServiceBaseUrl + "/getUiManageCredentialData/" + ap.getSessionID()) //$NON-NLS-1$
                    .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            uimd = getUIData.get(UiManageCredentialData.class);
        }
        
        return uimd;
    }

	public void setFocus() {
	    if (focusElement != null) {
            focusElement.setFocus();
        }
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
   private void makeActions() {
     IssueCredentialWizardAction issueCredentialWizardAction = new IssueCredentialWizardAction(getSite().getShell(), viewer); 
       DeleteCredentialAction deleteCredentialAction = new DeleteCredentialAction(getSite().getWorkbenchWindow(), viewer, uimd);
       //////////////////////
       // Actions in DropDown
       //////////////////////
       IActionBars actionBars = getViewSite().getActionBars();
       IToolBarManager toolBarManager = actionBars.getToolBarManager();
       toolBarManager.add(issueCredentialWizardAction);
       toolBarManager.add(deleteCredentialAction);
    }
}
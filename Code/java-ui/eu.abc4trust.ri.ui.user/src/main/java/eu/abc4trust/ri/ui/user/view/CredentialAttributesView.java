//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.view;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.ResourceRegistryStore;
import eu.abc4trust.ri.ui.user.utils.UIUtil;
import eu.abc4trust.xml.Attribute;

public class CredentialAttributesView extends ViewPart implements ISelectionListener {
	
	public static final String ID = "eu.abc4trust.ui.user.view.credentialattributes"; //$NON-NLS-1$
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	
	private CredentialInUi currentCredential;
	private Set<Image> imagesToDispose = new HashSet<Image>();
	
	private Composite partControlParent;
	private Composite viewContent;
	private Label defaultLabel;
	private Control focusElement;

	public CredentialAttributesView() {
		super();
		partControlParent = null;
		viewContent = null;
		currentCredential = null;
	}
	
	public void createPartControl(Composite parent) {
		//setPartName("Credential Attributes");
		partControlParent = parent;
		viewContent = createDefaultContent(partControlParent);
		
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}
	
    public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
    	if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object o = selection.getFirstElement();
				
				if (o instanceof CredentialInUi) {
					currentCredential = (CredentialInUi) o;
					updateOutlineContent();
					return;
				}
			}
			
			if (selection.size() == 0) {
				currentCredential = null;
				updateOutlineContent();
				return;
			}
		}
	}
    
    public void setFocus() {
    	if (focusElement!=null) { // should never be null (as long as the current credential has attributes)
    		focusElement.setFocus();
    	}
    }
	
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		super.dispose();
		for (Image i : imagesToDispose) {
			i.dispose();
		}
	}
    
    public void updateOutlineContent() {
    	viewContent.dispose();
    	if (currentCredential!=null) {
    		viewContent = createContent(partControlParent);
    	} else {
    		viewContent = createDefaultContent(partControlParent);
    	}
    	partControlParent.layout(true, true);
    }
	
	private Composite createDefaultContent(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		FormLayout formLayout =  new FormLayout();
		formLayout.marginHeight = 3;
		formLayout.marginWidth = 3;
		content.setLayout(formLayout);
		
		defaultLabel = new Label(content, SWT.NONE);
		defaultLabel.setText(Messages.get().CredentialAttributesView_msg_credentialSelectionPrompt);
		
		FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		defaultLabel.setLayoutData(formData);
		
		focusElement = defaultLabel;
		return content;
	}
	
	private Composite createContent(Composite parent) {
	    int firstColumWidth = 300; // TODO calculate dynamically by the widest column
	    
		ScrolledComposite scrolledContent = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		
		Composite content = new Composite(scrolledContent, SWT.NONE);
		FormLayout formLayout =  new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		formLayout.spacing = 5;
		content.setLayout(formLayout);
		
		scrolledContent.setContent(content);
		scrolledContent.setExpandVertical(false);
		scrolledContent.setExpandHorizontal(false);
		
		Label imageLabel = new Label(content, SWT.NONE);
        Image image = null;
        try {
            // image = ResourceRegistryStore.getThumbnail(currentCredential.desc.getImageReference().toURL(), 200);
            image = ImageDescriptor.createFromURL(currentCredential.desc.getImageReference().toURL()).createImage();
            if(image!=null && image.getBounds().width > 150) {
                ImageData scaledImage = image.internalImage.getImageData().scaledTo(150, 150);
                Device device = parent.getDisplay();
    			image = new Image(device, scaledImage);
            }
            imagesToDispose.add(image);
            imageLabel.setImage(image);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        FormData formData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(0);
        imageLabel.setLayoutData(formData);
		Text typeLabel = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
		typeLabel.setText(Messages.get().CredentialAttributesView_credentialType+":"); //$NON-NLS-1$
		Text typeText = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
		String credSpecUID = currentCredential.desc.getCredentialSpecificationUID().toString();
		String humanReadableCredSpecName = UIUtil.getHumanReadable(currentCredential.spec.spec.getFriendlyCredentialName());
		typeText.setText(humanReadableCredSpecName + " ("+credSpecUID+")");
		
		formData = new FormData(firstColumWidth, SWT.DEFAULT); // width: 180
		formData.left = new FormAttachment(0);
		formData.top = new FormAttachment(typeText, 0, SWT.CENTER);
		typeLabel.setLayoutData(formData);
		formData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
		formData.left = new FormAttachment(typeLabel);
		formData.top = new FormAttachment(imageLabel);
		typeText.setLayoutData(formData);
		typeText.setBackground(typeLabel.getBackground());
		
		Text issuerLabel = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
		issuerLabel.setText(Messages.get().CredentialAttributesView_issuer+":"); //$NON-NLS-1$
		Text issuerText = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
		String issuerUID = currentCredential.desc.getIssuerParametersUID().toString();
        String humanReadableIssuerName = UIUtil.getHumanReadable(currentCredential.issuer.description);
        issuerText.setText(humanReadableIssuerName + " ("+issuerUID+")");
		issuerText.setBackground(issuerLabel.getBackground());
		
		formData = new FormData(firstColumWidth, SWT.DEFAULT); // width: 180
		formData.left = new FormAttachment(0);
		formData.top = new FormAttachment(issuerText, 0, SWT.CENTER);
		issuerLabel.setLayoutData(formData);
		formData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
		formData.left = new FormAttachment(issuerLabel);
		formData.top = new FormAttachment(typeText);
		issuerText.setLayoutData(formData);
		
		Text revocationLabel = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
		revocationLabel.setText(Messages.get().CredentialAttributesView_revokedByIssuer+":"); //$NON-NLS-1$
		Label revocationCheckboxOrText = new Label(content, SWT.NONE);
        if (currentCredential.spec.spec.isRevocable()) {
            // Show checkbox if the credential is revocable
            final boolean state = currentCredential.desc.isRevokedByIssuer();
            revocationCheckboxOrText.setImage(ResourceRegistryStore.getImage(state?ResourceRegistryStore.IMG_CHECKBOX_SELECTED:ResourceRegistryStore.IMG_CHECKBOX_UNSELECTED)); // This is a workaround as disabled checkboxes are very light grey and almost invisible.
        } else {
            // Inform the user that the credential is not revocable
            revocationCheckboxOrText.setText(Messages.get().CredentialAttributesView_msg_notRevocable);
        }
        formData = new FormData(firstColumWidth, SWT.DEFAULT); // width: 180
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(revocationCheckboxOrText, 0, SWT.CENTER);
        revocationLabel.setLayoutData(formData);
        formData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
        formData.left = new FormAttachment(revocationLabel, 9);
        formData.top = new FormAttachment(issuerText);
        revocationCheckboxOrText.setLayoutData(formData);

		
		
		// Create a label and appropriate further controls (text field, date element,
		// checkbox, etc.) for every attribute of the current credential
		List<Control> valueControls = new ArrayList<Control>();
		for (Attribute attribute : currentCredential.desc.getAttribute()) {
			Control valueControl = null; // Create the control already now for layouting the label correctly
			
			//////////////////////////////////////////////
			// Label showing the attribute name
			String attributeType = attribute.getAttributeDescription().getType().toString();
			String fallback;
			if (attributeType.equals("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle")) { //$NON-NLS-1$
			    fallback = Messages.get().CredentialAttributesView_revocationHandle;
			} else {
			    fallback = attributeType;
			}
			Text label = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
			label.setText(UIUtil.getHumanReadable(attribute.getAttributeDescription().getFriendlyAttributeName(), fallback) + ":"); //$NON-NLS-1$
			
			//////////////////////////////////////////////
			// Control (text field, date element, checkbox, etc.) showing the attribute value
			// TODO: properly support data types with different control types			
//			URI dataType = attribute.getAttributeDescription().getDataType();
//			if (dataType.equals(Date.class)) {
//				// Attribute is a date
//				Date d = (Date) currentCredential.getAttributeValue(attributeName);
//				Calendar c = new GregorianCalendar();
//				try {
//					Date origin = Translator.DATE_FORMAT.parse(Constants.DATE_ORIGIN);
//					c.setTime(origin);
//					c.add(Calendar.YEAR, 1);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//				
//				valueControl = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
//				if(c.getTime().after(d)) {
//					((Text)valueControl).setText("None");
//				} else {
//					SimpleDateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy 'at' HH:mm z");
//					((Text)valueControl).setText(df.format(d));
//				}
//				
//				valueControl.setBackground(label.getBackground());
//				
//			} else if (dataType.equals(Boolean.class)) {
//				// Attribute is a boolean value
//				final boolean state = ((Boolean)currentCredential.getAttributeValue(attributeName)).booleanValue();
//				valueControl = new Button(content, SWT.CHECK);
//				valueControl.setBackground(label.getBackground());
//				((Button)valueControl).setSelection(state);
//				((Button)valueControl).addSelectionListener(new SelectionAdapter() {
//					@Override
//					// This is a workaround as "valueControl.setEnabled(false)" results
//					// in a very light grey checkbox that is almost invisible.
//					public void widgetSelected(SelectionEvent e) {
//						// e.doit = false; // does not work...
//						Button checkBox = ((Button) e.widget);
//						checkBox.setSelection(state);
//					}
//				});
//				
//			} else {
				// Attribute value is represented as text.
				valueControl = new Text(content, SWT.SINGLE | SWT.READ_ONLY);
				
				String textValue;				
				String attributeDataType = attribute.getAttributeDescription().getDataType().toString().substring(3);
				if (attributeDataType.equals(DatatypeConstants.DATE.getLocalPart())) {
				    Calendar c = DatatypeConverter.parseDate(attribute.getAttributeValue().toString());
				    textValue = new SimpleDateFormat(DATE_FORMAT).format(c.getTime());
				} else {
				    textValue = attribute.getAttributeValue().toString();
				}
				
				((Text)valueControl).setText(textValue);
				valueControl.setBackground(label.getBackground());
//			}
		
			//////////////////////////////////////////////
			// Lay out the controls
			formData = new FormData(firstColumWidth, SWT.DEFAULT); // width: 180
			formData.left = new FormAttachment(0);
			formData.top = new FormAttachment(valueControl, 0, SWT.CENTER);
			label.setLayoutData(formData);
			
			formData = new FormData();
			formData.left = new FormAttachment(label);
			if (valueControls.isEmpty()) {
				formData.top = new FormAttachment(revocationLabel);
			} else {
				// Attach the top of the valueControl to the bottom of the one above
				formData.top = new FormAttachment(valueControls.get(valueControls.size()-1));
			}
			valueControl.setLayoutData(formData);
			
			// Remember the control to lay out subsequent ones right below it
			valueControls.add(valueControl);
		}

		///////////////////////////////////////////////////////////////////////
		// Make the scrollbars work
		Point size = scrolledContent.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledContent.getContent().setSize(size);
		scrolledContent.setMinSize(size);
		//scrolledContent.layout(true, true);
		
		focusElement = typeText;
		return scrolledContent;
	}
}
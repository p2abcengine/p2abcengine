//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.view;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.CredentialSpecInUi;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.returnTypes.ui.InspectorInUi;
import eu.abc4trust.returnTypes.ui.PseudonymInUi;
import eu.abc4trust.returnTypes.ui.PseudonymListCandidate;
import eu.abc4trust.returnTypes.ui.RevealedAttributeValue;
import eu.abc4trust.returnTypes.ui.RevealedFact;
import eu.abc4trust.returnTypes.ui.TokenCandidate;
import eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy;
import eu.abc4trust.ri.ui.user.Application;
import eu.abc4trust.ri.ui.user.XmlUtils;
import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.ResourceRegistryStore;
import eu.abc4trust.ri.ui.user.utils.UIProperties;
import eu.abc4trust.ri.ui.user.utils.UIUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.PseudonymMetadata;

public class IdentitySelectionView extends ViewPart {
	
	public static final String ID = "eu.abc4trust.ui.user.view.identityselection"; //$NON-NLS-1$
	
    private ApplicationParameters sessionParams;
	private UiPresentationArguments uipa;
	private UiIssuanceArguments uiia;
	private String defaultStatusMessage;
	
	private PresentationPolicy selectedPolicy = null;
	private Composite content;
	private ScrolledComposite scrolledContent;
	private Label arrowLabel = null;
	private Group revealedInfoSummaryGroup;
	private Button submitButton;
	private Control focusElement;
	
	private Map<PresentationPolicy, List<TokenCandidate>> policyToTokenCandidates = new LinkedHashMap<PresentationPolicy, List<TokenCandidate>>();
	private Map<PresentationPolicy, Integer> policyToIdentifier = new LinkedHashMap<PresentationPolicy, Integer>();
	private Map<PresentationPolicy, Composite> policyToComposite = new LinkedHashMap<PresentationPolicy, Composite>();
	private Map<PresentationPolicy, Map<CredentialInPolicy, List<Button>>> policyToCredVarToButtons = new LinkedHashMap<PresentationPolicy, Map<CredentialInPolicy, List<Button>>>();
	private Map<PresentationPolicy, Map<PseudonymInPolicy, Control>> policyToNymVarToControl = new LinkedHashMap<PresentationPolicy, Map<PseudonymInPolicy, Control>>();
	private Map<Control, PresentationPolicy> selectableControlToPolicy = new LinkedHashMap<Control, PresentationPolicy>();
	private Map<URI, CredentialSpecification> credentialSpecifications = new LinkedHashMap<URI, CredentialSpecification>();
	
	public void createPartControl(Composite parent) {
	    
	    sessionParams = ApplicationParameters.getSessionSingletonInstance();
	    
		// Populate application status message
		defaultStatusMessage = MessageFormat.format(Messages.get().IdentitySelectionView_msg_defaultStatus,
                sessionParams.getUIMode(),
                sessionParams.getUserAcceptedLocales().toString(),
                sessionParams.getSessionID())
           + (sessionParams.isDemo()?" "+Messages.get().IdentitySelectionView_demoMode+".":""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		switch (sessionParams.getUIMode()) {
		
            case MANAGEMENT:
                focusElement = UIUtil.createMessageContent(parent, Messages.get().IdentitySelectionView_msg_applicationInMgmtMode);
                return;
		
			case PRESENTATION:
			    setStatusMessage(defaultStatusMessage);
				try {
					uipa = getUIPresentationArguments();
				} catch (Exception e) {
					e.printStackTrace();
					focusElement = UIUtil.createMessageContent(parent, MessageFormat.format(Messages.get().IdentitySelectionView_error_invalidInput, e.getMessage()));
					return;
				}
				
				for (TokenCandidatePerPolicy tcpp : uipa.tokenCandidatesPerPolicy) {
					policyToTokenCandidates.put(tcpp.policy, tcpp.tokenCandidates);
					policyToIdentifier.put(tcpp.policy, tcpp.policyId);
				}
				for (CredentialSpecInUi credSpec : uipa.data.credentialSpecifications) {
				    credentialSpecifications.put(credSpec.spec.getSpecificationUID(), credSpec.spec);
				}
				break;
				
			case ISSUANCE:
			    setStatusMessage(defaultStatusMessage);
				try {
					uiia = getUiIssuanceArguments();
				} catch (Exception e) {
					e.printStackTrace();
					focusElement = UIUtil.createMessageContent(parent, MessageFormat.format(Messages.get().IdentitySelectionView_error_invalidInput, e.getMessage()));
					return;
				}
				
				policyToTokenCandidates.put(uiia.policy.getPresentationPolicy(), uiia.tokenCandidates);
                for (CredentialSpecInUi credSpec : uiia.data.credentialSpecifications) {
                    credentialSpecifications.put(credSpec.spec.getSpecificationUID(), credSpec.spec);
                }
				break;
		}
		
		// Check for malformed policy
		for (PresentationPolicy policy : policyToTokenCandidates.keySet()) {
			boolean policyRequestsNoCredentials = policy.getCredential().isEmpty();
			boolean policyRequestsNoPseudonyms = policy.getPseudonym().isEmpty();
			if (policyRequestsNoPseudonyms && policyRequestsNoCredentials) {
				focusElement = UIUtil.createMessageContent(parent, MessageFormat.format(Messages.get().IdentitySelectionView_error_malformedPolicy, policy.getPolicyUID()));
				return;
			}
		}	
		
		// Create UI content for the given policy/policies
		createContentForPolicies(parent);
		
		// PreSelect the first token and pseudonym candidate
		preSelectFirstClaim();
	}
	
    private UiPresentationArguments getUIPresentationArguments() throws Exception {
//      String INPUTDATA                                = "/xml/ui-pres-1.xml"; //$NON-NLS-1$
//      String INPUTDATA2                               = "/xml/ui-pres-2.xml"; //$NON-NLS-1$
        String HOTELBOOKING_PRESENTATION                = "/xml/hotelbooking/ids-p-175207525-q.xml"; //$NON-NLS-1$
        
        if (ApplicationParameters.getSessionSingletonInstance().isDemo()) {
            if (uipa == null) {
                uipa = (UiPresentationArguments) XmlUtils.getObjectFromXML(Application.class.getResourceAsStream(HOTELBOOKING_PRESENTATION), false);
            }
        } else {
            // Obtain data from webservice method
            Client client = Client.create();
            Builder getUIArguments =
                client.resource(Application.userAbceEngineServiceBaseUrl + "/getUiPresentationArguments/" + sessionParams.getSessionID()) //$NON-NLS-1$
                .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            uipa = getUIArguments.get(UiPresentationArguments.class);
        }
        
        return uipa;
    }
    
    private UiIssuanceArguments getUiIssuanceArguments() throws Exception {
        String HOTELBOOKING_ISSUING                         = "/xml/hotelbooking/ids-i-132727255-q.xml"; //$NON-NLS-1$
        //String HOTELBOOKING_ISSUING                         = "/xml/hotelbooking/ids-i-370430331-q.xml"; //$NON-NLS-1$
        //String HOTELBOOKING_ISSUING                         = "/xml/hotelbooking/ids-i-977961710-q.xml"; //$NON-NLS-1$
        
        if (ApplicationParameters.getSessionSingletonInstance().isDemo()) {
            if (uiia == null) {
                uiia = (UiIssuanceArguments) XmlUtils.getObjectFromXML(Application.class.getResourceAsStream(HOTELBOOKING_ISSUING), false);
            }
        } else {
            Client client = Client.create();
            Builder getUIArguments =
                client.resource(Application.userAbceEngineServiceBaseUrl + "/getUiIssuanceArguments/" + sessionParams.getSessionID()) //$NON-NLS-1$
                .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
            uiia = getUIArguments.get(UiIssuanceArguments.class);
        }
        return uiia;
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
	
//	private Composite createDefaultContent(Composite parent) {
//		Composite content = new Composite(parent, SWT.NONE);
//		FormLayout formLayout =  new FormLayout();
//		formLayout.marginHeight = 3;
//		formLayout.marginWidth = 3;
//		content.setLayout(formLayout);
//		
//		Label defaultLabel = new Label(content, SWT.NONE);
//		defaultLabel.setText(Messages.IdentitySelectionView_error_loading_input);
//		
//		FormData formData = new FormData();
//		formData.left = new FormAttachment(0);
//		formData.right = new FormAttachment(100);
//		formData.top = new FormAttachment(0);
//		formData.bottom = new FormAttachment(100);
//		defaultLabel.setLayoutData(formData);
//		
//		focusElement = defaultLabel;
//		return content;
//	}
	
	private void createContentForPolicies(Composite parent) {
		scrolledContent = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		
		content = new Composite(scrolledContent, SWT.NONE);
		FormLayout formLayout =  new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		formLayout.spacing = 5;
		content.setLayout(formLayout);
		
		scrolledContent.setContent(content);
		scrolledContent.setExpandVertical(false);
		scrolledContent.setExpandHorizontal(false);
		
		List<Label> policySeparationlabels = new ArrayList<Label>();

		Iterator<PresentationPolicy> policyIterator = policyToTokenCandidates.keySet().iterator();
		while (policyIterator.hasNext()) {
			PresentationPolicy policy = policyIterator.next();
			List<TokenCandidate> tokenCandidates = policyToTokenCandidates.get(policy);
			
			Composite policyComposite = createContentForPolicy(policy, tokenCandidates);
			policyToComposite.put(policy, policyComposite);
			
			// Layout of the policyComposite
			FormData formData = new FormData();
			formData.left = new FormAttachment(0);
			if (policySeparationlabels.isEmpty()) {
				formData.top = new FormAttachment(0);
			} else {
				// Attach the top of the group to the bottom of the one above
				formData.top = new FormAttachment(policySeparationlabels.get(policySeparationlabels.size()-1));
			}
			policyComposite.setLayoutData(formData);
			
			// Add an OR-Label if this was not the last policy
			if (policyIterator.hasNext()) {
				Label policySeparationlabel = new Label(content, SWT.NONE);
				//policySeparationlabel.setText("...or this policy...");
				FontData[] fontData = policySeparationlabel.getFont().getFontData();
				for(int i = 0; i < fontData.length; ++i) {
					fontData[i].setHeight(fontData[i].getHeight()+1);
					fontData[i].setStyle(SWT.BOLD);
				}
				final Font biggerBoldLabelFont = new Font(Display.getCurrent(), fontData);
				policySeparationlabel.setFont(biggerBoldLabelFont);
				policySeparationlabel.addDisposeListener(new DisposeListener() {
					private static final long serialVersionUID = 1L;
					@Override
					public void widgetDisposed(DisposeEvent event) {
						biggerBoldLabelFont.dispose();
					}
				});
				policySeparationlabels.add(policySeparationlabel);
				
				FormData formDataLabel = new FormData();
				formDataLabel.left = new FormAttachment(policyComposite, 0, SWT.LEFT);
				formDataLabel.top = new FormAttachment(policyComposite);
				policySeparationlabel.setLayoutData(formDataLabel);
			}
		}
		
		///////////////////////////////////////////////////////////////////////
		// Make the scrollbars work
		updateScrolledContentMinSize();
	}
	
	private Composite createContentForPolicy(PresentationPolicy policy, List<TokenCandidate> tokenCandidates) {
		Composite credRefGroupsComposite = new Composite(content, SWT.NONE);
		credRefGroupsComposite.setLayout(new FormLayout());
		
		Group policyGroup = new Group(credRefGroupsComposite, SWT.NONE);
		policyGroup.setText(UIUtil.getHumanReadable(policy.getMessage()!=null ? policy.getMessage().getFriendlyPolicyName() : null,
		                                            policy.getPolicyUID().toString()));
//		policyGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE002));
		// Layout for the group's children
		FormLayout formLayout =  new FormLayout();
		formLayout.marginHeight = 0;
		formLayout.marginWidth = 0;
		formLayout.marginBottom = 0;
		formLayout.spacing = 0;
		policyGroup.setLayout(formLayout);
		
		Text policyDescriptionText = new Text(policyGroup, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		policyDescriptionText.setText(UIUtil.getHumanReadable(policy.getMessage()!=null ? policy.getMessage().getFriendlyPolicyDescription() : null, "")); //$NON-NLS-1$
		
		// Layout of the policy description
        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(0);
        formData.width = Math.min(policyDescriptionText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 400);
        policyDescriptionText.setLayoutData(formData);
		
		List<Group> policyContentGroups = new ArrayList<Group>();
		/////////////////////////////////////////////////////////////////////
		// Create an SWT group for the PSEUDONYMS if there are some to select
		/////////////////////////////////////////////////////////////////////
		if (! policy.getPseudonym().isEmpty()) {
			final Group pseudonymsGroup = new Group(policyGroup, SWT.NONE);
			pseudonymsGroup.setText(Messages.get().IdentitySelectionView_heading_pseudonymGroup);
			policyContentGroups.add(pseudonymsGroup); // Remember the group for proper alignment of possible further groups
			
			// Layout of the group
			formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(policyDescriptionText, -5);
			pseudonymsGroup.setLayoutData(formData);
			
			// Layout for the group's children
			formLayout =  new FormLayout();
			formLayout.marginHeight = 0;
			formLayout.marginWidth = 0;
			formLayout.spacing = 3;
			pseudonymsGroup.setLayout(formLayout);
			
			List<Control> pseudonymControls = new ArrayList<Control>();
			int pseudonymControlWidth = 250;
			int pseudonymControlHeight = 20;
			Map<PseudonymInPolicy, Control> nymToControl = new LinkedHashMap<PseudonymInPolicy, Control>();
			// Fill the group with combo boxes (or text fields) which represent pseudonyms
			for (int pseudonymIndex = 0; pseudonymIndex < policy.getPseudonym().size(); pseudonymIndex++) {
				
				PseudonymInPolicy pseudonymInPolicy = policy.getPseudonym().get(pseudonymIndex);
				Set<PseudonymInUi> allPseudonymsForIndex = getAllPseudonymsForPolicyByIndex(tokenCandidates, pseudonymIndex);
				Control pseudonymControl;
				
				if (containsOnlyNewPseudonym(allPseudonymsForIndex)) {
				    // If there is only a new pseudonym to choose, create a text input field
					PseudonymInUi pui = allPseudonymsForIndex.iterator().next(); // TODO: ensure that there is only one: the current
                                                                                 // test data has two -> according to Robert, multiple new pseudonyms
                                                                                 // are allowed and appear in cases where the new pseudonyms are bound
                                                                                 // to different secrets. Add an additional selection mechanism 
					Text newPseudonymText;
					if ((pseudonymInPolicy.isExclusive()) && (! UIProperties.getSessionSingletonInstance().allowEditingScopeExclusivePseudonymAlias())) {
					    // scope-exclusive pseudonym, whose alias must not be modified
					    newPseudonymText = new Text(pseudonymsGroup, SWT.BORDER | SWT.READ_ONLY);
					} else {
					    newPseudonymText = new Text(pseudonymsGroup, SWT.BORDER);
					}					
					newPseudonymText.setText(UIUtil.getHumanReadable(pui.metadata!=null?pui.metadata.getFriendlyPseudonymDescription():null, pui.uri));
					newPseudonymText.setData(pui);
					pseudonymControl = newPseudonymText;
					selectableControlToPolicy.put(newPseudonymText, policy);
					pseudonymControls.add(newPseudonymText);
					
					// Layout for the text field
					formData = new FormData();
					formData.left = new FormAttachment(0);
					formData.top = new FormAttachment(0);
					formData.width = pseudonymControlWidth;
					formData.height = pseudonymControlHeight;
					newPseudonymText.setLayoutData(formData);
										
				} else {
					// Otherwise create a combo box with all the pseudonym options
					final Combo pseudonymCombo;
					// If list contains a NEW pseudonym, make the combo box editable, otherwise read-only
					PseudonymInUi newNym = getFirstNewPseudonym(allPseudonymsForIndex);
					if (newNym != null) {
						pseudonymCombo = new Combo(pseudonymsGroup, SWT.BORDER | SWT.DROP_DOWN);
						pseudonymCombo.setText(UIUtil.getHumanReadable(newNym.metadata!=null ? newNym.metadata.getFriendlyPseudonymDescription() : null, newNym.uri));
					} else {
						pseudonymCombo = new Combo(pseudonymsGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
					}
					// Populate combo box with human readable pseudonym names
					List<PseudonymInUi> allPseudonymsForIndexList = new ArrayList<PseudonymInUi>(allPseudonymsForIndex);
					Map<Integer, PseudonymInUi> indexToPseudonym = new HashMap<Integer, PseudonymInUi>();
					for (PseudonymInUi piui : allPseudonymsForIndexList) {
						if (isNewPseudonym(piui.pseudonym)) {
							indexToPseudonym.put(-1, piui); // -1 for new pseudonym. Assumption: at most one new pseudonym // TODO: this assumption does not hold any longer according to Robert: fix
						} else {
							indexToPseudonym.put(pseudonymCombo.getItemCount(), piui); // Remember position per pseudonym (with index origin 0)
							pseudonymCombo.add(UIUtil.getHumanReadable(piui.metadata!=null ? piui.metadata.getFriendlyPseudonymDescription() : null, piui.uri)); // TODO prevent users from creating pseudonyms with the same name, otherwise combo selection not unambiguous any more!
						}
					}	
					pseudonymCombo.setData(indexToPseudonym);
					pseudonymControl = pseudonymCombo;
					selectableControlToPolicy.put(pseudonymControl, policy);
					pseudonymControls.add(pseudonymCombo);
					
					// If list does not permit for new pseudonyms (=combo box is read only), pre-select the first entry
					if (newNym == null) pseudonymCombo.select(0);
				}
				
				// Layout for the control
                formData = new FormData();
                formData.left = new FormAttachment(0);
                if (pseudonymControls.size()==1) {
                    formData.top = new FormAttachment(0);
                } else {
                    // Attach the top of the combo to the bottom of the one above
                    formData.top = new FormAttachment(pseudonymControls.get(pseudonymControls.size()-2));
                }
                formData.width = pseudonymControlWidth;
                formData.height = pseudonymControlHeight;
                pseudonymControl.setLayoutData(formData);
				
				// Place scope label next to text field/combo box for scope-exclusive pseudonyms
				if (pseudonymInPolicy.isExclusive()) {
					Label pseudonymLabel = new Label(pseudonymsGroup, SWT.NONE);
					pseudonymLabel.setText(Messages.get().IdentitySelectionView_scope+": "+(pseudonymInPolicy.getScope() != null ? pseudonymInPolicy.getScope(): Messages.get().IdentitySelectionView_error_missingScope));  //$NON-NLS-1$
					FontData[] fontData = pseudonymLabel.getFont().getFontData();
					for(int i = 0; i < fontData.length; ++i) {
						fontData[i].setHeight(fontData[i].getHeight()-3);
						//fontData[i].setStyle(SWT.BOLD);
					}
					final Font biggerBoldLabelFont = new Font(Display.getCurrent(), fontData);
					pseudonymLabel.setFont(biggerBoldLabelFont);
					pseudonymLabel.addDisposeListener(new DisposeListener() {
						private static final long serialVersionUID = 1L;
						@Override
						public void widgetDisposed(DisposeEvent event) {
							biggerBoldLabelFont.dispose();
						}
					});
					// Layout for the label
					formData = new FormData();
					formData.left = new FormAttachment(pseudonymControl);
					formData.top = new FormAttachment(pseudonymControl, 0, SWT.CENTER);
					pseudonymLabel.setLayoutData(formData);
				}
				
				// Add focus listener to pseudonym control
				pseudonymControl.addFocusListener(new FocusListener() {
					private static final long serialVersionUID = 9017229362495716206L;

					@Override
					public void focusLost(FocusEvent arg0) { }
					
					@Override
					public void focusGained(FocusEvent arg0) {
						selectedPolicy = selectableControlToPolicy.get(arg0.widget);
						deselectButtonsOfOtherPolicies(selectedPolicy);	
						updateRevealedInfoContent();
					}
				});
				// Add modify listener to pseudonym control
				pseudonymControl.addListener(SWT.Modify, new Listener() {
					private static final long serialVersionUID = 5575728658673001364L;

					@Override
					public void handleEvent(Event arg0) {
						selectedPolicy = selectableControlToPolicy.get(arg0.widget);
						deselectButtonsOfOtherPolicies(selectedPolicy);	
						updateRevealedInfoContent();
					}
				});
			
				// Remember which control belongs to which pseudonymVariable in the policy and to which policy
				nymToControl.put(pseudonymInPolicy, pseudonymControl);
			}
			
			policyToNymVarToControl.put(policy, nymToControl);
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Create as many SWT groups as there are credentials required by the policy (=number of credential variables)
		// and fill the i-th group with SWT buttons corresponding to credentials that appear in the i-th position
		// of any CredentialUidList corresponding to a CandidatePresentationToken associated with the current policy
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<CredentialInPolicy, List<Button>> credVarToButtons = new LinkedHashMap<CredentialInPolicy, List<Button>>();
		
		for (int credVarIndex = 0; credVarIndex < policy.getCredential().size(); credVarIndex++) {
			CredentialInPolicy cip = policy.getCredential().get(credVarIndex);
			
			// Determine the _set_ of credentials (i.e., every credential only once) that are assigned 
			// to this credential reference ("credential variable") in any of the claim's assignments.
			Set<CredentialInUi> allCredPossibilitiesForCredVar = getAllCredentialsForPolicyByIndex(tokenCandidates, credVarIndex);
			
			//////////////////////////
			// Create the SWT group...
			final Group credRefGroup = new Group(policyGroup, SWT.NONE);
			// Set caption of the group as disjunction of all allowed human readable names of the allowed credential specifications.
			StringBuilder caption = new StringBuilder();
			for (URI credSpecAlternativeURI : cip.getCredentialSpecAlternatives().getCredentialSpecUID()) {
			    CredentialSpecification credSpec = credentialSpecifications.get(credSpecAlternativeURI);
			    String credSpecName = UIUtil.getHumanReadable(credSpec!=null ? credSpec.getFriendlyCredentialName() : null, credSpecAlternativeURI.toString());
			    
			    if (caption.length() > 0) {
			        caption.append(" "+Messages.get().IdentitySelectionView_or+" "); //$NON-NLS-1$ //$NON-NLS-2$
			    }
			    caption.append(credSpecName);
			}
			credRefGroup.setText(caption.toString());
			
			policyContentGroups.add(credRefGroup); // Remember the group for proper alignment of possible further groups
			
			// Layout of the group
			formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			if (policyContentGroups.size()==1) {
				formData.top = new FormAttachment(0);
			} else {
				// Attach the top of the group to the bottom of the one above
				formData.top = new FormAttachment(policyContentGroups.get(policyContentGroups.size()-2));
			}
			credRefGroup.setLayoutData(formData);
			
			// Layout for the group's children
			formLayout =  new FormLayout();
			formLayout.marginHeight = 0;
			formLayout.marginWidth = 0;
			formLayout.spacing = 3;
			credRefGroup.setLayout(formLayout);

			///////////////////////////////////////////
			// ...and add a button for every credential
			List<Button> credButtons = new ArrayList<Button>();
			for (CredentialInUi cred : allCredPossibilitiesForCredVar) {
				final Button credButton = new Button(credRefGroup, SWT.RADIO);
				
				if (cred.desc.getImageReference()!=null) {
					try {
						credButton.setImage(ResourceRegistryStore.getThumbnail(cred.desc.getImageReference().toURL(), 128));
					} catch (MalformedURLException e) { 
					    credButton.setImage(ResourceRegistryStore.getImage(ResourceRegistryStore.IMG_MISSING));
						e.printStackTrace();
					}
				} else {
					credButton.setImage(ResourceRegistryStore.getImage(ResourceRegistryStore.IMG_MISSING));
				}
				credButton.setData(cred);
				credButtons.add(credButton); // Remember the button for proper alignment of further buttons.
				
				// Layout for the button
				formData = new FormData();
				if (credButtons.size()>1) {
					formData.left = new FormAttachment(credButtons.get(credButtons.size()-2));
				}
				credButton.setLayoutData(formData);
				
				// Add selection listener
				credButton.addSelectionListener(new SelectionAdapter() {
					private static final long serialVersionUID = -8447158820993892379L;

					@Override
					public void widgetSelected(SelectionEvent e) {
						Button selectedButton = ((Button) e.widget);
						
						// As the buttons are SWT.RADIO buttons, the widgetSelected is also called for buttons
						// that lose the selection. Thus, perform the intended behavior only for the button
						// that is actually selected (i.e., where getSelection is true).
						if (selectedButton.getSelection()) {
							selectedPolicy = selectableControlToPolicy.get(selectedButton);
							deselectButtonsOfOtherPolicies(selectedPolicy);
							updateRevealedInfoContent();
						}
					}
				});
				
				selectableControlToPolicy.put(credButton, policy);
			}
			
			// Remember the buttons for this reference
//			policyToButtons.put(tcpp, credButtons);
			credVarToButtons.put(cip, credButtons);
		}
		policyToCredVarToButtons.put(policy, credVarToButtons);
		
		return credRefGroupsComposite;
	}
	
	/**
	 * Called whenever the selection changes.
	 * Subsequently determines the token description for the current selection and shows the corresponding information that would be revealed.
	 */
	private void updateRevealedInfoContent() {
		// Dispose the previous disclosure info
		if (arrowLabel!=null && !arrowLabel.isDisposed()) arrowLabel.dispose(); 
		if (revealedInfoSummaryGroup!=null && !revealedInfoSummaryGroup.isDisposed()) revealedInfoSummaryGroup.dispose();
		if (submitButton!=null && !submitButton.isDisposed()) submitButton.dispose();
		
		// Determine whether for the the current selection the token/pseudonym candidates are unambiguous 
		final CandidatePair selectedCandidates = getCandidatesFromCurrentSelection();
		if (selectedCandidates == null) {
			// It is ambiguous which tokens and/or pseudonyms shall be returned
			String statusMsg = Messages.get().IdentitySelectionView_msg_noCandidateAssociatedWithSelection;
			setStatusMessage(statusMsg);
			return; // Nothing more to do.
			
		} else {
			setStatusMessage(defaultStatusMessage); 
		}
		
		Composite selectedPolicyComposite = policyToComposite.get(selectedPolicy);
		arrowLabel = new Label(content, SWT.NONE);
		arrowLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
		arrowLabel.setImage(ResourceRegistryStore.getImage(ResourceRegistryStore.IMG_ARROW));
		FormData formData = new FormData();
		formData.left = new FormAttachment(selectedPolicyComposite);
		formData.top = new FormAttachment(selectedPolicyComposite, 0, SWT.CENTER);
		arrowLabel.setLayoutData(formData);
		
		revealedInfoSummaryGroup = new Group(content, SWT.NONE);
		revealedInfoSummaryGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
//		revealedInfoSummaryGroup.setText("Information that you are about to reveal");
		// Layout of the group
		formData = new FormData();
		formData.left = new FormAttachment(arrowLabel);
		formData.top = new FormAttachment(selectedPolicyComposite, 0, SWT.TOP); // , 0, SWT.CENTER
		revealedInfoSummaryGroup.setLayoutData(formData);
		// Layout for the group's children
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 0;
		formLayout.marginWidth = 0;
		formLayout.spacing = 0;
		revealedInfoSummaryGroup.setLayout(formLayout);
		
		Label adaptedCardLabel = new Label(revealedInfoSummaryGroup, SWT.NONE);
		adaptedCardLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
		adaptedCardLabel.setText(Messages.get().IdentitySelectionView_heading_adaptedCard);
		FontData[] fontData = adaptedCardLabel.getFont().getFontData();
		for(int i = 0; i < fontData.length; ++i) {
			fontData[i].setHeight(fontData[i].getHeight()+5);
			fontData[i].setStyle(SWT.BOLD);
		}
		final Font biggerBoldLabelFont = new Font(Display.getCurrent(), fontData);
		adaptedCardLabel.setFont(biggerBoldLabelFont);
		adaptedCardLabel.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = -2937275480456180279L;

			@Override
			public void widgetDisposed(DisposeEvent event) {
				biggerBoldLabelFont.dispose();
			}
		});
		
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		// Ownership notes ///////////////////////////////////////////
		Group proofOfOwnershipGroup = new Group(revealedInfoSummaryGroup, SWT.NONE);
		proofOfOwnershipGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
		proofOfOwnershipGroup.setText(Messages.get().IdentitySelectionView_heading_ownershipGroup);
//		fontData = proofOfOwnershipGroup.getFont().getFontData();
//		for(int i = 0; i < fontData.length; ++i) {
//			fontData[i].setStyle(SWT.BOLD);
//		}
//		final Font biggerBoldOwnershipGFont = new Font(Display.getCurrent(), fontData);
//		proofOfOwnershipGroup.setFont(biggerBoldOwnershipGFont);
//		proofOfOwnershipGroup.addDisposeListener(new DisposeListener() {
//			@Override
//			public void widgetDisposed(DisposeEvent event) {
//				biggerBoldOwnershipGFont.dispose();
//			}
//		});
		// Layout of the group
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(adaptedCardLabel);
		proofOfOwnershipGroup.setLayoutData(formData);
		// Layout for the group's children
		formLayout = new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		formLayout.spacing = 5;
		proofOfOwnershipGroup.setLayout(formLayout);
		
		formData = new FormData();
		formData.left = new FormAttachment(proofOfOwnershipGroup, 0, SWT.CENTER);
		formData.top = new FormAttachment(0);
		adaptedCardLabel.setLayoutData(formData);
		
		List<Label> revealedInfoLabels = new ArrayList<Label>();
		// Ownership notes for pseudonyms
		for (int i=0; i < selectedPolicy.getPseudonym().size(); i++) {
			PseudonymInPolicy pip = selectedPolicy.getPseudonym().get(i);
			Control c = policyToNymVarToControl.get(selectedPolicy).get(pip);
			PseudonymInUi piui = selectedCandidates.getPseudonymCandidate().pseudonyms.get(i);
			
			Label revealedInfoLabel = new Label(proofOfOwnershipGroup, SWT.NONE);
			revealedInfoLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
			String pseudonymName = c instanceof Text ? ((Text) c).getText() : ((Combo) c).getText();
			if (isNewPseudonym(piui.pseudonym)) {
				revealedInfoLabel.setText("\u2022 " + MessageFormat.format(Messages.get().IdentitySelectionView_msg_newPseudonym, pseudonymName)); //$NON-NLS-1$
			} else {
				revealedInfoLabel.setText("\u2022 " + MessageFormat.format(Messages.get().IdentitySelectionView_msg_existingPseudonym, pseudonymName)); //$NON-NLS-1$
			}
			revealedInfoLabels.add(revealedInfoLabel);
			
			// Layout for the label
			formData = new FormData();
			if (revealedInfoLabels.size()>1) {
				formData.top = new FormAttachment(revealedInfoLabels.get(revealedInfoLabels.size()-2));
			}
			revealedInfoLabel.setLayoutData(formData);
		}
		// Ownership notes for credentials
		for (int i=0; i < selectedPolicy.getCredential().size(); i++) {
			CredentialInUi ciui = selectedCandidates.getTokenCandidate().credentials.get(i);
			String credSpecName = UIUtil.getHumanReadable(ciui.spec.spec.getFriendlyCredentialName(), ciui.spec.spec.getSpecificationUID().toString());
			String credIssuer = UIUtil.getHumanReadable(ciui.issuer.description, ciui.desc.getIssuerParametersUID().toString());
			
			Label revealedInfoLabel = new Label(proofOfOwnershipGroup, SWT.NONE);
			revealedInfoLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
			revealedInfoLabel.setText("\u2022 " + MessageFormat.format(Messages.get().IdentitySelectionView_msg_ownership, credSpecName, credIssuer));  //$NON-NLS-1$
			revealedInfoLabels.add(revealedInfoLabel);
			
			// Layout for the label
			formData = new FormData();
			if (revealedInfoLabels.size()>1) {
				formData.top = new FormAttachment(revealedInfoLabels.get(revealedInfoLabels.size()-2));
			}
			revealedInfoLabel.setLayoutData(formData);
		}
		Group previousGroupInUi = proofOfOwnershipGroup;
		
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		// Explicitly revealed attributes ////////////////////////////
//		Group revealedValuesGroup = null;
		if ( ! selectedCandidates.tokenCandidate.revealedAttributeValues.isEmpty()) {
			final Group revealedValuesGroup = new Group(revealedInfoSummaryGroup, SWT.NONE);
			revealedValuesGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
			revealedValuesGroup.setText(Messages.get().IdentitySelectionView_heading_attributesGroup);
//			fontData = revealedValuesGroup.getFont().getFontData();
//			for(int i = 0; i < fontData.length; ++i) {
//				fontData[i].setStyle(SWT.BOLD);
//			}
//			final Font biggerBoldRevealedValuesGFont = new Font(Display.getCurrent(), fontData);
//			revealedValuesGroup.setFont(biggerBoldRevealedValuesGFont);
//			revealedValuesGroup.addDisposeListener(new DisposeListener() {
//				@Override
//				public void widgetDisposed(DisposeEvent event) {
//					biggerBoldRevealedValuesGFont.dispose();
//				}
//			});
			// Layout of the group
			formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(previousGroupInUi);
			revealedValuesGroup.setLayoutData(formData);
			// Layout for the group's children
			formLayout = new FormLayout();
			formLayout.marginHeight = 5;
			formLayout.marginWidth = 5;
			formLayout.spacing = 5;
			revealedValuesGroup.setLayout(formLayout);
			
			revealedInfoLabels = new ArrayList<Label>();
			for (RevealedAttributeValue revealedAttr :  selectedCandidates.tokenCandidate.revealedAttributeValues) {
				Label revealedInfoLabel = new Label(revealedValuesGroup, SWT.NONE);
				revealedInfoLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
				revealedInfoLabel.setText("\u2022 "+UIUtil.getHumanReadable(revealedAttr.descriptions)+". "); //$NON-NLS-1$ //$NON-NLS-2$
				revealedInfoLabels.add(revealedInfoLabel);
				
				// Layout for the button
				formData = new FormData();
				if (revealedInfoLabels.size()>1) {
					formData.top = new FormAttachment(revealedInfoLabels.get(revealedInfoLabels.size()-2));
				}
				revealedInfoLabel.setLayoutData(formData);
			}
			previousGroupInUi = revealedValuesGroup;
		}
		
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		// Predicates ////////////////////////////////////////////////
		if ( ! selectedCandidates.tokenCandidate.revealedFacts.isEmpty()) {
			final Group revealedPredicatesGroup = new Group(revealedInfoSummaryGroup, SWT.NONE);
			revealedPredicatesGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
			revealedPredicatesGroup.setText(Messages.get().IdentitySelectionView_heading_factsGroup);
//			fontData = revealedPredicatesGroup.getFont().getFontData();
//			for(int i = 0; i < fontData.length; ++i) {
//				fontData[i].setStyle(SWT.BOLD);
//			}
//			final Font biggerBoldRevealedPredicatesGFont = new Font(Display.getCurrent(), fontData);
//			revealedPredicatesGroup.setFont(biggerBoldRevealedPredicatesGFont);
//			revealedPredicatesGroup.addDisposeListener(new DisposeListener() {
//				@Override
//				public void widgetDisposed(DisposeEvent event) {
//					biggerBoldRevealedPredicatesGFont.dispose();
//				}
//			});
			// Layout of the group
			formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(previousGroupInUi);
			revealedPredicatesGroup.setLayoutData(formData);
			revealedPredicatesGroup.setLayout(new FormLayout());
			// Layout for the group's children
			formLayout = new FormLayout();
			formLayout.marginHeight = 5;
			formLayout.marginWidth = 5;
			formLayout.spacing = 5;
			revealedPredicatesGroup.setLayout(formLayout);
			
			revealedInfoLabels = new ArrayList<Label>();
			for (RevealedFact revealedFact :  selectedCandidates.tokenCandidate.revealedFacts) {
				Label revealedInfoLabel = new Label(revealedPredicatesGroup, SWT.NONE);
				revealedInfoLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
				revealedInfoLabel.setText("\u2022 "+UIUtil.getHumanReadable(revealedFact.descriptions)+". "); //$NON-NLS-1$ //$NON-NLS-2$
				revealedInfoLabels.add(revealedInfoLabel);
				
				// Layout for the label
				formData = new FormData();
				if (revealedInfoLabels.size()>1) {
					formData.top = new FormAttachment(revealedInfoLabels.get(revealedInfoLabels.size()-2));
				}
				revealedInfoLabel.setLayoutData(formData);
			}
			previousGroupInUi = revealedPredicatesGroup;
		}
		
		
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		// Inspectable Attributes ////////////////////////////////////
		final List<Combo> inspectorCombos = new ArrayList<Combo>();
		if ( ! selectedCandidates.tokenCandidate.inspectableAttributes.isEmpty()) {
			final Group inspectableAttributesGroup = new Group(revealedInfoSummaryGroup, SWT.NONE);
			inspectableAttributesGroup.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
			inspectableAttributesGroup.setText(Messages.get().IdentitySelectionView_heading_inspectionGroup);
//			fontData = revealedPredicatesGroup.getFont().getFontData();
//			for(int i = 0; i < fontData.length; ++i) {
//				fontData[i].setStyle(SWT.BOLD);
//			}
//			final Font biggerBoldRevealedPredicatesGFont = new Font(Display.getCurrent(), fontData);
//			revealedPredicatesGroup.setFont(biggerBoldRevealedPredicatesGFont);
//			revealedPredicatesGroup.addDisposeListener(new DisposeListener() {
//				@Override
//				public void widgetDisposed(DisposeEvent event) {
//					biggerBoldRevealedPredicatesGFont.dispose();
//				}
//			});
			// Layout of the group
			formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			formData.top = new FormAttachment(previousGroupInUi);
			inspectableAttributesGroup.setLayoutData(formData);
			inspectableAttributesGroup.setLayout(new FormLayout());
			// Layout for the group's children
			formLayout = new FormLayout();
			formLayout.marginHeight = 5;
			formLayout.marginWidth = 5;
			formLayout.spacing = 5;
			inspectableAttributesGroup.setLayout(formLayout);
			
			List<CLabel> inspectableAttributesInfoCLabels = new ArrayList<CLabel>();
			for (InspectableAttribute inspectableAttribute : selectedCandidates.tokenCandidate.inspectableAttributes) {
				// Determine human readable attribute type
				String humanReadableAttrType = inspectableAttribute.attributeType.toString();
				for (AttributeDescription ad : inspectableAttribute.credential.spec.spec.getAttributeDescriptions().getAttributeDescription()) {
					if (ad.getType().equals(inspectableAttribute.attributeType)) {
						humanReadableAttrType = UIUtil.getHumanReadable(ad.getFriendlyAttributeName(), inspectableAttribute.attributeType.toString());
						break;
					}
				}
				// Determine human readable credential name
				String humanReadableCredSpecName = UIUtil.getHumanReadable(inspectableAttribute.credential.spec.spec.getFriendlyCredentialName(), inspectableAttribute.credential.uri);
				// Determine attribute value
				String attrValue = null;
				for (Attribute attribute : inspectableAttribute.credential.desc.getAttribute()) {
					if (attribute.getAttributeDescription().getType().equals(inspectableAttribute.attributeType)) {
						attrValue = attribute.getAttributeValue().toString(); // TODO obtain prepared by maria to avoid formatting issues
						break;
					}
				}
				
				CLabel revealedInfoLabel = new CLabel(inspectableAttributesGroup, SWT.NONE);
				revealedInfoLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
				revealedInfoLabel.setText("\u2022 " + MessageFormat.format(Messages.get().IdentitySelectionView_msg_inspectableAttribute, humanReadableAttrType, humanReadableCredSpecName, attrValue)); //$NON-NLS-1$
				revealedInfoLabel.setImage(ResourceRegistryStore.getImage(ResourceRegistryStore.IMG_LOCK_SMALL));
				inspectableAttributesInfoCLabels.add(revealedInfoLabel);
				// Layout for the label
				formData = new FormData();
				if (inspectableAttributesInfoCLabels.size()>1) {
					formData.top = new FormAttachment(inspectableAttributesInfoCLabels.get(inspectableAttributesInfoCLabels.size()-2));
				}
				revealedInfoLabel.setLayoutData(formData);
				
				CLabel inspectionGroundsCLabel = new CLabel(inspectableAttributesGroup, SWT.NONE);
				inspectionGroundsCLabel.setBackground(ResourceRegistryStore.getColor(ResourceRegistryStore.COL_BLUE001));
				inspectionGroundsCLabel.setText("         " + MessageFormat.format(Messages.get().IdentitySelectionView_msg_inspectionGrounds, inspectableAttribute.inspectionGrounds)); //$NON-NLS-1$
				inspectableAttributesInfoCLabels.add(inspectionGroundsCLabel);
				// Layout for the label
				formData = new FormData();
				if (inspectableAttributesInfoCLabels.size()>1) {
					formData.top = new FormAttachment(inspectableAttributesInfoCLabels.get(inspectableAttributesInfoCLabels.size()-2), -10);
				}
				inspectionGroundsCLabel.setLayoutData(formData);
				
				// Combo box to allow the user to select the inspector for the inspectable attribute
				Combo inspectorCombo = new Combo(inspectableAttributesGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
				for (InspectorInUi inspector : inspectableAttribute.inspectorAlternatives) {
					inspectorCombo.setData(Integer.toString(inspectorCombo.getItemCount()), inspector);
					if (inspector!=null) {
						inspectorCombo.add(UIUtil.getHumanReadable(inspector.description, inspector.uri)); 
					} else {
						inspectorCombo.add("<"+Messages.get().IdentitySelectionView_error_missingInspectorInfo+">"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				inspectorCombo.select(0);
				inspectorCombos.add(inspectorCombo);
				
				// Layout for the combo
				formData = new FormData();
				formData.left = new FormAttachment(revealedInfoLabel);
				formData.top = new FormAttachment(revealedInfoLabel, 0, SWT.CENTER);
				formData.width = 250;
				formData.height = 20;
				inspectorCombo.setLayoutData(formData);
			}
			previousGroupInUi = inspectableAttributesGroup;
		}
		
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		// Submit Button /////////////////////////////////////////////
		submitButton = new Button(content, SWT.PUSH);
		submitButton.setSize(new Point(100, 100));
		submitButton.setText(Messages.get().IdentitySelectionView_caption_submitButton);
		fontData = submitButton.getFont().getFontData();
		for(int i = 0; i < fontData.length; ++i) {
			fontData[i].setHeight(fontData[i].getHeight()+5);
			fontData[i].setStyle(SWT.BOLD);
		}
		final Font biggerBoldButtonFont = new Font(Display.getCurrent(), fontData);
		submitButton.setFont(biggerBoldButtonFont);
		submitButton.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = -3288660714545962234L;

			@Override
			public void widgetDisposed(DisposeEvent event) {
				biggerBoldButtonFont.dispose();
			}
		});
		
		formData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
		formData.top = new FormAttachment(revealedInfoSummaryGroup);
		formData.left = new FormAttachment(revealedInfoSummaryGroup, 0, SWT.CENTER);
		submitButton.setLayoutData(formData);
		
		submitButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -8687765617282676685L;
			
			@SuppressWarnings("incomplete-switch")
            @Override
			public void widgetSelected(SelectionEvent e) {
				if (sessionParams.isDemo()) {
					MessageBox mbox = new MessageBox(submitButton.getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mbox.setText(Messages.get().IdentitySelectionView_demoMode);
					mbox.setMessage(Messages.get().IdentitySelectionView_msg_demoModeSubmit);
					mbox.open();
					return;
				}
				
				int chosenPresentationToken = selectedCandidates.getTokenCandidate().candidateId;
				int chosenPseudonymList = selectedCandidates.getPseudonymCandidate()!=null ? selectedCandidates.getPseudonymCandidate().candidateId : -1; //-1 means that policy does not ask for pseudonym

				// chosenInspectors
				List<String> chosenInspectors = new ArrayList<String>();
				for (Combo inspectorCombo : inspectorCombos) {
					String selectionIndexAsString = Integer.toString(inspectorCombo.getSelectionIndex());
					InspectorInUi inspector = (InspectorInUi) inspectorCombo.getData(selectionIndexAsString);
					
					if (inspector!=null) {
						chosenInspectors.add(inspector.uri);
					} else {
						MessageBox mbox = new MessageBox(revealedInfoSummaryGroup.getShell(), SWT.ICON_ERROR | SWT.OK);
						mbox.setText(Messages.get().IdentitySelectionView_error_missingInspectorDataCaption);
						mbox.setMessage(Messages.get().IdentitySelectionView_error_missingInspectorData);
						mbox.open();
						return;
					}
				}
				
				// metadataToChange
				Map<String, PseudonymMetadata> metadataToChange = new HashMap<String, PseudonymMetadata>();
				for (int i=0; i < selectedPolicy.getPseudonym().size(); i++) {
					PseudonymInUi piui = selectedCandidates.getPseudonymCandidate().pseudonyms.get(i);
					
					PseudonymInPolicy pip = selectedPolicy.getPseudonym().get(i);
					Control c = policyToNymVarToControl.get(selectedPolicy).get(pip);
					String pseudonymNameSetByUser = c instanceof Text ? ((Text) c).getText() : ((Combo) c).getText();
					
					if (isNewPseudonym(piui.pseudonym)) {
						// The selected pseudonym is a newly generated one
						String humanReadable = UIUtil.getHumanReadable(piui.metadata.getFriendlyPseudonymDescription(), piui.uri.toString());
						
						if (! pseudonymNameSetByUser.equals(humanReadable)) {
							// User changed the name via the UI
							
							PseudonymMetadata oldPseudonymMetadata = piui.metadata;
							PseudonymMetadata newPseudonymMetadata = new PseudonymMetadata(); // PseudonymMetadata does not have setFriendlyPseudonymDescription() method, so new PseudonymMetadata object is used which contains an empty list 
							
							newPseudonymMetadata.setMetadata(oldPseudonymMetadata.getMetadata());
							// Create a friendly description with entries for only _one_ language to avoid
							// confusion about which language the user defined pseudonym is in.
							FriendlyDescription newFriendlyDescription = new FriendlyDescription();
							newFriendlyDescription.setLang(sessionParams.getUserAcceptedLocales().get(0).getLanguage());
							newFriendlyDescription.setValue(pseudonymNameSetByUser);
							List<FriendlyDescription> newFriendlyDescriptions = newPseudonymMetadata.getFriendlyPseudonymDescription();
							newFriendlyDescriptions.add(newFriendlyDescription);
							
							metadataToChange.put(piui.uri, newPseudonymMetadata);
						}
					} // Nothing to do otherwise: metadataToChange map only populated for newly created pseudonyms
				} 

				switch (sessionParams.getUIMode()) {

					case PRESENTATION:
						UiPresentationReturn uipr = new UiPresentationReturn();
						uipr.chosenPolicy = policyToIdentifier.get(selectedPolicy);
						uipr.chosenPresentationToken = chosenPresentationToken;
						uipr.chosenPseudonymList = chosenPseudonymList;
						uipr.chosenInspectors = chosenInspectors;
						uipr.metadataToChange = metadataToChange;
						
						try {
						    submitUiReturn(uipr);
						} catch(Exception ex) {
						    ex.printStackTrace();
						}
						break;
						
					case ISSUANCE:
						UiIssuanceReturn uiir = new UiIssuanceReturn();
						uiir.chosenIssuanceToken = chosenPresentationToken;
						uiir.chosenPseudonymList = chosenPseudonymList;
						uiir.chosenInspectors = chosenInspectors;
						uiir.metadataToChange = metadataToChange;
						
                        try {
                            submitUiReturn(uiir);
                        } catch(Exception ex) {
                          ex.printStackTrace();
                        }
						break;
				}
				// close windows...
				Application.closeApplication();
			}
		});
		
		updateScrolledContentMinSize();
		scrolledContent.layout(true, true);
	}
	
    private void submitUiReturn(UiIssuanceReturn uiir) {
        Client client = Client.create();
        Builder setUIArguments =
            client.resource(Application.userAbceEngineServiceBaseUrl + "/setUiIssuanceReturn/" + sessionParams.getSessionID()) //$NON-NLS-1$
            .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
        setUIArguments.post(uiir);
    }

    private void submitUiReturn(UiPresentationReturn uipr) {
        Client client = Client.create();
        Builder setUIArguments =
            client.resource(Application.userAbceEngineServiceBaseUrl + "/setUiPresentationReturn/" + sessionParams.getSessionID()) //$NON-NLS-1$
            .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
        setUIArguments.post(uipr);
    }
	
	private void setStatusMessage(String statusMessage) {
		getViewSite().getActionBars().getStatusLineManager().setMessage(statusMessage);
	}

	private boolean isNewPseudonym(Pseudonym pseudonym) {
		if (pseudonym.getPseudonymValue()==null || pseudonym.getPseudonymValue().length==0)
			return true;
		else
			return false;
	}
	
	private boolean containsOnlyNewPseudonym(Set<PseudonymInUi> pseudonyms) { 
		for (PseudonymInUi p : pseudonyms) {
			if (! isNewPseudonym(p.pseudonym)) {
				return false;
			}
		}
		return true;
	}
	
	private PseudonymInUi getFirstNewPseudonym(Set<PseudonymInUi> pseudonyms) { // TODO according to robert, there can be multiple new pseudonyms -> issue. fix it.
		for (PseudonymInUi p : pseudonyms) {
			if (isNewPseudonym(p.pseudonym)) { 
				return p;
			}
		}
		return null;
	}

	private Set<PseudonymInUi> getAllPseudonymsForPolicyByIndex(List<TokenCandidate> tokenCandidates, int pseudonymIndex) {
		Set<PseudonymInUi> allPseudonymsFromTokenCandidatesByIndex = new HashSet<PseudonymInUi>();

		for (TokenCandidate tc : tokenCandidates) {
			for (PseudonymListCandidate plc : tc.pseudonymCandidates) {
				allPseudonymsFromTokenCandidatesByIndex.add(plc.pseudonyms.get(pseudonymIndex));
			}
		}
		return allPseudonymsFromTokenCandidatesByIndex;
	}
	
	private Set<CredentialInUi> getAllCredentialsForPolicyByIndex(List<TokenCandidate> tokenCandidates, int credVarIndex) {
		Set<CredentialInUi> allCredentialsForPolicyByIndex = new HashSet<CredentialInUi>();
		
		for (TokenCandidate tc : tokenCandidates) {
			allCredentialsForPolicyByIndex.add(tc.credentials.get(credVarIndex));
		}
		return allCredentialsForPolicyByIndex;
	}
	
	private void updateScrolledContentMinSize() {
		Point point = scrolledContent.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 75; // To solve some bug in RAP (or, this class?) where some content on the right is always clipped when the UI is shown for the first(!) time.
		scrolledContent.getContent().setSize(point);
		scrolledContent.setMinSize(point);
	}
	
	private void deselectButtonsOfOtherPolicies(PresentationPolicy selectedPolicy) {
		// RadioButton-Behavior across different policies: deSelect all
		// buttons that belong to _other_ polices
		for (PresentationPolicy policy : policyToTokenCandidates.keySet()) {
			if (policy != selectedPolicy) {
				for (List<Button> buttonsForCredVar : policyToCredVarToButtons.get(policy).values()) {
					for (Button buttonForCredVar : buttonsForCredVar) {
						buttonForCredVar.setSelection(false);
					}
				}
			}
		}
	}
	
	private class CandidatePair {
		   private final TokenCandidate tokenCandidate;
		   private final PseudonymListCandidate pseudonymCandidate;
		   
		   public CandidatePair(TokenCandidate tokenCandidate, PseudonymListCandidate pseudonymCandidate) {
			   this.tokenCandidate = tokenCandidate;
			   this.pseudonymCandidate = pseudonymCandidate;
		   }
		   
		   public TokenCandidate getTokenCandidate() {
			   return tokenCandidate;
	       }
		   
		   public PseudonymListCandidate getPseudonymCandidate() {
			   return pseudonymCandidate;
		   }
		}
	
	/**
	 * @return a {@link CandidatePair} object if by means of the current user-selection it is
	 *         unambiguous which token candidate and/or which pseudonym candidate are chosen.
	 *         Null otherwise.
	 */
	private CandidatePair getCandidatesFromCurrentSelection() {
		boolean policyRequestsCredentials = selectedPolicy.getCredential().size() > 0;
		boolean policyRequestsPseudonyms = selectedPolicy.getPseudonym().size() > 0;
		
		if (policyRequestsPseudonyms && !policyRequestsCredentials) {
			// policy asks ONLY for pseudonyms -> then there is only one token candidate with multiple pseudonym candidates
			TokenCandidate tc = policyToTokenCandidates.get(selectedPolicy).get(0);
			PseudonymListCandidate pc = getPseudonymCandidateFromCurrentPseudonymSelection(tc);
			if (pc != null) return new CandidatePair(tc, pc);
			else return null;
			
		} else if (!policyRequestsPseudonyms && policyRequestsCredentials) {
			// policy asks ONLY for credentials
			TokenCandidate tc = getTokenCandidateFromCurrentCredentialSelection();
			if (tc != null) return new CandidatePair(tc, null);
			else return null;
			
		} else if (policyRequestsCredentials && policyRequestsPseudonyms) {
			// policy asks for credentials AND pseudonyms
			TokenCandidate tc = getTokenCandidateFromCurrentCredentialSelection();
			if (tc != null) {
				PseudonymListCandidate pc = getPseudonymCandidateFromCurrentPseudonymSelection(tc);
				if (pc != null) return new CandidatePair(tc, pc);
				else return null;
			} else {
				return null;
			}
		} else {
			// Neither pseudonyms nor credentials are requested -> this case should never happen (sanity check at UI beginning assures this)			
			return null;
		}
	}
	
	private TokenCandidate getTokenCandidateFromCurrentCredentialSelection() {
		List<CredentialInUi> currentCredAssignment = getCurrentCredentialSelectionAsAssignment();
		if (currentCredAssignment==null) return null;
		
		for (TokenCandidate tc : policyToTokenCandidates.get(selectedPolicy)) {
			if (tc.credentials.equals(currentCredAssignment)) {
				return tc;
			}
		}
		return null;
	}
	
    private PseudonymListCandidate getPseudonymCandidateFromCurrentPseudonymSelection(TokenCandidate tokenCandidate) {
        List<PseudonymInUi> currentNymAssignment = getCurrentPseudonymSelectionAsAssignment();
        if (currentNymAssignment==null) return null;
        
        // Assumption: pseudonym candidate lists are unique within a token candidate
        for (PseudonymListCandidate plc : tokenCandidate.pseudonymCandidates) {
            if (plc.pseudonyms.equals(currentNymAssignment)) {
                return plc;
            }
        }
        return null;
    }
	
	private List<PseudonymInUi> getCurrentPseudonymSelectionAsAssignment() {
		List<PseudonymInUi> selectionAsAssignment = new ArrayList<PseudonymInUi>();
		
		for (PseudonymInPolicy pip : selectedPolicy.getPseudonym()) {
			Control c = policyToNymVarToControl.get(selectedPolicy).get(pip);
			
			if (c instanceof Text) {
				selectionAsAssignment.add((PseudonymInUi) c.getData());
				
			} else {
				Combo combo = (Combo) c;
				@SuppressWarnings("unchecked")
				Map<Integer, PseudonymInUi> indexToPseudonym = (Map<Integer, PseudonymInUi>) combo.getData();
				PseudonymInUi piui = indexToPseudonym.get(Integer.valueOf(combo.getSelectionIndex())); // -1 for new pseudonym //TODO fix issue that there may be multiple _new_ pseudonyms now, that is, probably consider also a second selection box.
				selectionAsAssignment.add(piui);
			}
		}
		
		if (selectionAsAssignment.isEmpty()) return null;
		else return selectionAsAssignment;
	}
	
	private List<CredentialInUi> getCurrentCredentialSelectionAsAssignment() {
		List<CredentialInUi> selectionAsAssignment = new ArrayList<CredentialInUi>();
		
		for (CredentialInPolicy cip : selectedPolicy.getCredential()) {
			for (Button b : policyToCredVarToButtons.get(selectedPolicy).get(cip)) {
				if (b.getSelection()) {
					selectionAsAssignment.add((CredentialInUi) b.getData());
					break; // Assumes that only one button per credential reference can be selected.
				}
			}
		}
		
		if (selectionAsAssignment.isEmpty()) return null;
		else return selectionAsAssignment;
	}
	
	private void preSelectFirstClaim() {
		// Select first policy
		selectedPolicy = policyToTokenCandidates.keySet().iterator().next();
		
		// Select first button of each credential variable in selected policy
		for (CredentialInPolicy cip : selectedPolicy.getCredential()) {
			// Take first button of each credential variable in policy...
			Button b = policyToCredVarToButtons.get(selectedPolicy).get(cip).get(0);
			// ...and select it
			b.setSelection(true);
		}
		
		// Nothing to do for pseudonyms:
		//    Where new pseudonyms are possible, text field or non-read-only combo box has new pseudonym pre-selected
		//    Where only existing pseudonyms are possible, combo box has the first entry pre-selected
		
		updateRevealedInfoContent();
	}
	
	
}

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

package eu.abc4trust.ri.ui.user.utils;

import org.eclipse.rwt.RWT;

public class Messages {
    
    // public static final Locale SWEDISH = new Locale("sv");
    
    public static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
    public static final String BULLET      = "\u2022 ";  //$NON-NLS-1$
    
    ///////////////////////////////////////////////
    public String ApplicationWorkbenchWindowAdvisor_applicationTitle;
    
    ///////////////////////////////////////////////
    public String UIMode_issuance;
    public String UIMode_presentation;
    public String UIMode_management;
     
    ///////////////////////////////////////////////
    public String IdentitySelectionView_demoMode;
    public String IdentitySelectionView_scope;
    public String IdentitySelectionView_or;
    

    public String IdentitySelectionView_heading_presentationDescription;
    
    public String IdentitySelectionView_heading_adaptedCard;
    public String IdentitySelectionView_heading_pseudonymGroup;
    public String IdentitySelectionView_heading_ownershipGroup;
    public String IdentitySelectionView_heading_attributesGroup;
    public String IdentitySelectionView_heading_factsGroup;
    public String IdentitySelectionView_heading_inspectionGroup;
    public String IdentitySelectionView_heading_propertiesOfNewCredential;
    public String IdentitySelectionView_heading_unknownAttributes;

    public String IdentitySelectionView_msg_applicationInMgmtMode;
    public String IdentitySelectionView_msg_defaultStatus;
    public String IdentitySelectionView_msg_newPseudonym;
    public String IdentitySelectionView_msg_existingPseudonym;
    public String IdentitySelectionView_msg_ownership;
    public String IdentitySelectionView_msg_inspectableAttribute;
    public String IdentitySelectionView_msg_inspectionGrounds;
    public String IdentitySelectionView_msg_issuerAndTypeOfNewCredential;
    public String IdentitySelectionView_msg_keyBindingOfNewCredentialToCredential;
    public String IdentitySelectionView_msg_keyBindingOfNewCredentialToPseudonym;
    public String IdentitySelectionView_msg_carryOverDirect;
    public String IdentitySelectionView_msg_carryOverIndirect;
    public String IdentitySelectionView_msg_jointlyRandomGenerated;
    public String IdentitySelectionView_msg_noCandidateAssociatedWithSelection;
    public String IdentitySelectionView_msg_demoModeSubmit;

    public String IdentitySelectionView_caption_submitButtonPresentation;
    public String IdentitySelectionView_caption_submitButtonBasicIssuance;
    public String IdentitySelectionView_caption_submitButtonAdvancedIssuance;
    
    public String IdentitySelectionView_error_invalidInput;
    public String IdentitySelectionView_error_malformedPolicy;
    public String IdentitySelectionView_error_missingInspectorInfo;
    public String IdentitySelectionView_error_missingInspectorDataCaption;
    public String IdentitySelectionView_error_missingInspectorData;
    public String IdentitySelectionView_error_missingScope;
    
    ///////////////////////////////////////////////
    public String UIUtil_error_noHumanReadableInfo;
    
    ///////////////////////////////////////////////
    public String CredentialAttributesView_issuer;
    public String CredentialAttributesView_credentialType;
    public String CredentialAttributesView_revokedByIssuer;
    public String CredentialAttributesView_revocationHandle;
    public String CredentialAttributesView_msg_revocable;
    public String CredentialAttributesView_msg_notRevocable;
    public String CredentialAttributesView_msg_credentialSelectionPrompt;
    public String CredentialAttributesView_msg_policyWithoutDescription;
    
    ///////////////////////////////////////////////
    public String DeleteCredentialAction_execute;
    public String DeleteCredentialAction_safetyQuestion;
    public String DeleteCredentialAction_caption_errorMessage;
    public String DeleteCredentialAction_error_couldNotDeleteCredential;
    
    
    private Messages() {}
    
    public static Messages get() {
        return (Messages) RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, Messages.class);
    }
}
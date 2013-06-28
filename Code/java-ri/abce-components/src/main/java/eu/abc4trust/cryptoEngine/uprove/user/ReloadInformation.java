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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescription;

public class ReloadInformation {
	public String issuanceUrl = null; //set by ReloadStorageManager
	public String issuanceStepUrl = null; //set by ReloadStorageManager
	
	public final List<URI> creduids;
	public final List<URI> pseudonyms;
	public final List<URI> inspectors;
	
	public ReloadInformation() {
		creduids = new ArrayList<URI>();
		pseudonyms = new ArrayList<URI>();
		inspectors = new ArrayList<URI>();
	}
	
	public ReloadInformation(List<URI> creduids, List<URI> pseudonyms) {
		this.creduids = new ArrayList<URI>(creduids);
		this.pseudonyms = new ArrayList<URI>(pseudonyms);
		this.inspectors = new ArrayList<URI>();
		
//		PresentationTokenDescription presentationTokenDescription = itd.getPresentationTokenDescription();
//        for (CredentialInToken cit : presentationTokenDescription.getCredential()) {
//            for (AttributeInToken ait : cit.getDisclosedAttribute()) {
//                if (ait.getInspectionGrounds() != null) {
//                	inspectors.add(ait.getInspectorPublicKeyUID());
//                }
//            }
//        }
	}
}

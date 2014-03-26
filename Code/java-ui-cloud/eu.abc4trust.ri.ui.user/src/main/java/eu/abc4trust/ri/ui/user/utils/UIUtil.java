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

package eu.abc4trust.ri.ui.user.utils;

import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.abc4trust.ri.ui.user.Application;
import eu.abc4trust.xml.FriendlyDescription;

public class UIUtil {
	
	@Deprecated
	public static String getDefaultLanguageHumanReadable(List<FriendlyDescription> humanReadables) {
		String defaultLanguageISO = Locale.getDefault().getLanguage();
		
		for (FriendlyDescription humanReadable : humanReadables) {
			if (humanReadable.getLang().equals(defaultLanguageISO)) {
				return humanReadable.getValue();
			}
		}
		
		// return the first one as default in case no match is found
		return humanReadables.get(0).getValue()+" <no match found for language '"+defaultLanguageISO+"'>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Matches the ordered list of languages that are acceptable to the user with the languages of the given human readable names and returns the first match.</br>
	 * The ordered list of languages that are acceptable to the user are determined by the user's browser settings.</br>
	 * </br> 
	 * If there is no match, the system tries to find a match for a fallback language.</br>
	 * If there is also no match for the fallback language, the given fallback value is returned.
	 * 
	 * @param humanReadables The human readable names in all available languages.
	 * @param fallback The fallback value that is returned if no language-match can be found for both the user-accepted languages and the fallback language.
	 * @return the human readable name in the most suitable language.
	 */
	public static String getHumanReadable(List<FriendlyDescription> humanReadables, String fallback) {
		if (humanReadables==null || humanReadables.isEmpty()) {
			return fallback;
		}
		
		// Find match between the ordered list of languages that are acceptable to the user and the languages of the given human readable names
		for (Locale l : ApplicationParameters.getSessionSingletonInstance().getUserAcceptedLocales()) {
		    for (FriendlyDescription humanReadable : humanReadables) {
	            if (humanReadable.getLang().equals(l.getLanguage())) {
	                return humanReadable.getValue();
	            }
	        }
		}
		
		// No match for the user-accepted languages: try with fallback language
		for (FriendlyDescription humanReadable : humanReadables) {
			if (humanReadable.getLang().equals(Application.LOCALE_FALLBACK.getLanguage())) {
				return humanReadable.getValue();
			}
		}
		
		//  No match for the user-accepted languages or fallback language: use fallback value
		return fallback;
	}
	
	public static String getHumanReadable(List<FriendlyDescription> humanReadables) {
		String fallback = "<"+Messages.get().UIUtil_error_noHumanReadableInfo+">"; //$NON-NLS-1$ //$NON-NLS-2$
		return getHumanReadable(humanReadables, fallback);
	}
	
	public static Label createMessageContent(Composite parent, String message) {
		Composite content = new Composite(parent, SWT.NONE);
		FormLayout formLayout =  new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		content.setLayout(formLayout);
		
		Label label = new Label(content, SWT.NONE);
		label.setText(message);
		
		FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		label.setLayoutData(formData);
		
		return label;
	}
}
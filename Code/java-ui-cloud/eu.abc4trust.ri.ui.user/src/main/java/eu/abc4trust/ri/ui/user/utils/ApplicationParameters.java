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

import org.eclipse.rwt.SessionSingletonBase;

public class ApplicationParameters {

	private UIMode uiMode;
	private String sessionID;
	private boolean demoMode;
	private String userNonce;

	public String getUserNonce() {
		return userNonce;
	}

	public void setUserNonce(String userNonce) {
		this.userNonce = userNonce;
	}

	private List<Locale> userAcceptedLocales;

	private ApplicationParameters() {
		// prevent instantiation from outside
	}

	public static ApplicationParameters getSessionSingletonInstance() {
		return SessionSingletonBase.getInstance(ApplicationParameters.class);
	}

	public void setUiMode(UIMode uiMode) {
		this.uiMode = uiMode;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
	}

	public void setUserAcceptedLocales(List<Locale> locales) {
		this.userAcceptedLocales = locales;
	}

	public boolean isDemo() {
		return demoMode;
	}

	public UIMode getUIMode() {
		return uiMode;
	}

	public String getSessionID() {
		return sessionID;
	}

	public List<Locale> getUserAcceptedLocales() {
		return userAcceptedLocales;
	}
}
//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.utils;

import java.util.List;
import java.util.Locale;

import org.eclipse.rwt.SessionSingletonBase;

public class ApplicationParameters {
    
    private UIMode uiMode;
    private String sessionID;
    private boolean demoMode;
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
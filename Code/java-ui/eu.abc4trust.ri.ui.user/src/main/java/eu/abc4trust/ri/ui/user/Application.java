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

package eu.abc4trust.ri.ui.user;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.widgets.JSExecutor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.UIMode;
import eu.abc4trust.ri.ui.user.utils.UIProperties;

/**
 * This class controls all aspects of the application's execution
 * and is contributed through the plugin.xml.
 */
public class Application implements IApplication {
	
    public static final String userAbceEngineServiceBaseUrl                 = "http://localhost:9300/idselect-user-service/user"; //$NON-NLS-1$
	public static final String REQUESTPARAMNAME_UIMODE                      = "mode";         //$NON-NLS-1$
	public static final String REQUESTPARAMNAME_DEMO                        = "demo";         //$NON-NLS-1$
    public static final String REQUESTPARAMNAME_SESSIONID                   = "sessionid";    //$NON-NLS-1$
    public static final String REQUESTPARAM_ISSUANCEMODE                    = "issuance";     //$NON-NLS-1$
    public static final String REQUESTPARAM_PRESENTATIONMODE                = "presentation"; //$NON-NLS-1$
    public static final String REQUESTPARAM_MANAGEMENTMODE                  = "management";   //$NON-NLS-1$
	public static final Locale LOCALE_FALLBACK                              = UIProperties.getSessionSingletonInstance().uiFallbackLocale(); //Messages.SWEDISH; // Locale.ENGLISH;
	
	public Object start(IApplicationContext context) throws Exception {
	    
	    ApplicationParameters p = ApplicationParameters.getSessionSingletonInstance();
		
		// Obtain parameters from HTTP request
		String param_demoMode = RWT.getRequest().getParameter(REQUESTPARAMNAME_DEMO);
		if (param_demoMode != null) p.setDemoMode(new Boolean(param_demoMode).booleanValue());
		else p.setDemoMode(true);
		
		String param_uiMode = RWT.getRequest().getParameter(REQUESTPARAMNAME_UIMODE);
		if (param_uiMode != null && param_uiMode.equalsIgnoreCase(REQUESTPARAM_ISSUANCEMODE)) p.setUiMode(UIMode.ISSUANCE);
		else if (param_uiMode != null && param_uiMode.equalsIgnoreCase(REQUESTPARAM_PRESENTATIONMODE)) p.setUiMode(UIMode.PRESENTATION);
		else p.setUiMode(UIMode.MANAGEMENT);

		String param_sessionID = RWT.getRequest().getParameter(REQUESTPARAMNAME_SESSIONID);
		p.setSessionID(param_sessionID);
		
	    List<Locale> userAcceptedLocales = Collections.list(RWT.getRequest().getLocales());
	    System.out.println("userAcceptedLocales : " + userAcceptedLocales);
	    if(!userAcceptedLocales.contains(LOCALE_FALLBACK)) {
	    	// add fallback to list
	    	userAcceptedLocales.add(LOCALE_FALLBACK);
	    }
	    p.setUserAcceptedLocales(userAcceptedLocales);
	    
	    System.out.println("Handling UI request: " + //$NON-NLS-1$
	            MessageFormat.format(Messages.get().IdentitySelectionView_msg_defaultStatus, p.getUIMode().toString(), p.getUserAcceptedLocales().toString(), p.getSessionID()) +
	            (p.isDemo()?" "+Messages.get().IdentitySelectionView_demoMode+".":"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$);
		
		// Create the Application
		Display display = PlatformUI.createDisplay();
		WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
		context.applicationRunning(); // to bring down a splash screen if it exists
		return PlatformUI.createAndRunWorkbench(display, advisor);
	}

    public void stop() {
		// Do nothing
	}
  
    public static void closeApplication() {
    	System.out.println("UI CLOSE APPLICATION!! ");
        Display.getCurrent().disposeExec(new Runnable() {
            @Override
            public void run() {
            	// OLD mehtod for closing - but did not work for IE
              // http://www.eclipse.org/forums/index.php/m/635947/
//              JSExecutor.executeJS("window.close(); "); //$NON-NLS-1$

            	try {
            		String script = "";
//		            	System.out.println("Excecute Javascript on app close! " + script);
            		// lock ui - might not be needed..
                	script += 
            			"var blurDiv = document.createElement(\"div\");" +
		        		"blurDiv.id = \"blurDiv\";" + 
		        		"blurDiv.style.cssText = \"position:absolute; top:0; right:0; width:\" + screen.width + \"px; height:\" + screen.height + \"px; background-color: #000000; opacity:0.5;\";" +
		            	"" +
		        		"document.getElementsByTagName(\"body\")[0].appendChild(blurDiv);";
            		// add 'idselectDone'..
                	script += 
            			"var done = document.createElement(\"div\"); " + 
		            	"done.id = \"idselectDone\"; " +
		            	"done.style.cssText=\"display: none;\";" + 
		        		"document.getElementsByTagName(\"head\")[0].appendChild(done);";
		            	
//		            	System.out.println("Excecute Javascript on app close! " + script);
		            	JSExecutor.executeJS(script); //$NON-NLS-1$
                } catch(Exception e) {
                	System.err.println("Excecute Javascript on app close! Failed!");
                	e.printStackTrace();
                }
            }
        });
        Display.getCurrent().dispose();
    }
}

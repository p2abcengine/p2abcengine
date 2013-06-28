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

package eu.abc4trust.ui.idselectservice;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * InitUserABCEServletContext tries to init UserABCE service - so that its ready 
 */
public class InitUserABCEServletContext implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
		System.out.println("## InitUserABCEServletContext contextInitialized");
		boolean b = UserService.touchThisBooleanToForceStaticInit;
    }
    public void contextDestroyed(ServletContextEvent event) {
      // Do Nothing
    }
}

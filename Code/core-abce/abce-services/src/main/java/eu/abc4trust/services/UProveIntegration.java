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

package eu.abc4trust.services;

import java.util.logging.Logger;

public class UProveIntegration {

    @SuppressWarnings("unused")
    private boolean useUprove;

    Logger log = Logger.getLogger(UProveIntegration.class.getName());

    public void verify() {

        String pathToUProveExe = "PathToUProveExe";
        String uprovePath = System.getProperty(pathToUProveExe, null);

        if ((uprovePath == null) || (uprovePath.equals(""))) {
            this.useUprove = false;
            this.log.info("U-Prove support disabled - The system property \""
                    + pathToUProveExe + "\" was not set");
        } else {
            this.useUprove = true;
        }
    }

}

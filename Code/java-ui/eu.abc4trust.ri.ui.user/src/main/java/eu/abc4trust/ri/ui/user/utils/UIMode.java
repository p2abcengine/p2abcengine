//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.utils;

public enum UIMode {
    
    ISSUANCE,
    PRESENTATION,
    MANAGEMENT;
    
    @Override
    public String toString() {
        
        switch (this) {
            case MANAGEMENT: return Messages.get().UIMode_management;
            case PRESENTATION: return Messages.get().UIMode_presentation;
            case ISSUANCE: return Messages.get().UIMode_issuance;
            default: return "unknown"; //$NON-NLS-1$
        }
    }
}
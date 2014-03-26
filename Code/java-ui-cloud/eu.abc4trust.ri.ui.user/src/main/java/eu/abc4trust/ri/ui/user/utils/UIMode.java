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

public enum UIMode {
    
    ISSUANCE,
    PRESENTATION,
    MANAGEMENT,
    AUTO_REDIRECT;
    
    @Override
    public String toString() {
        
        switch (this) {
            case MANAGEMENT: return Messages.get().UIMode_management;
            case PRESENTATION: return Messages.get().UIMode_presentation;
            case ISSUANCE: return Messages.get().UIMode_issuance;
            case AUTO_REDIRECT: return "Auto-redirect";
            default: return "unknown"; //$NON-NLS-1$
        }
    }
}
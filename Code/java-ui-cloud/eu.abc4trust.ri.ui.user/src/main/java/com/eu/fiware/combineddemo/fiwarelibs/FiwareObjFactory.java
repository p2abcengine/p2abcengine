//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Nokia                                                             *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2015. All Rights Reserved.                *
//* (C) Copyright Nokia. 2015. All Rights Reserved.                   *
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

package com.eu.fiware.combineddemo.fiwarelibs;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class FiwareObjFactory {

    public FiwareObjFactory() {
    }

    public JAXBElement<PolicyRequest> createPolicyRequest(PolicyRequest pr) {
        JAXBElement<PolicyRequest> jaxbElement;
        jaxbElement = new JAXBElement(new QName(PolicyRequest.class.getSimpleName()), PolicyRequest.class, null);
        jaxbElement.setValue(pr);
        return jaxbElement;
    }

    public JAXBElement<IssuanceRequest> createIssuanceRequest(IssuanceRequest ir) {
        JAXBElement<IssuanceRequest> jaxbElement;
        jaxbElement = new JAXBElement(new QName(IssuanceRequest.class.getSimpleName()), IssuanceRequest.class, null);
        jaxbElement.setValue(ir);
        return jaxbElement;
    }
    
      public JAXBElement<VerificationRequest> createVerificationRequest(VerificationRequest tcr) {
        JAXBElement<VerificationRequest> jaxbElement;
        jaxbElement = new JAXBElement(new QName(VerificationRequest.class.getSimpleName()), VerificationRequest.class, null);
        jaxbElement.setValue(tcr);
        return jaxbElement;
    }
}

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

import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "VerificationRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerificationRequest", propOrder = {
    "verifierName",
    "verifierPassword",
    "ppa",
    "token"
})
public class VerificationRequest implements Serializable {

    private static final long serialVersionUID = -8392589399445581157L;
    
    @XmlElement(name = "VerifierName")
    protected String verifierName;
    
    @XmlElement(name = "VerifierPassword")
    protected String verifierPassword;
    
    @XmlElement(name = "PresentationPolicyAlternatives")
    protected PresentationPolicyAlternatives ppa;
    
    @XmlElement(name = "PresentationToken")
    protected PresentationToken token;

    /**
     * Gets the value of the Username property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getVerifername() {
        return this.verifierName;
    }

    /**
     * Sets the value of the Username property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setVerifiername(String value) {
        this.verifierName = value;
    }

    /**
     * Gets the value of the Password property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getVerifierPassword() {
        return this.verifierPassword;
    }

    /**
     * Sets the value of the Password property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setVerifierPassword(String value) {
        this.verifierPassword = value;
    }

    /**
     * Gets the value of the PPAproperty.
     *
     * @return possible object is {@link String }
     *
     */
    public PresentationPolicyAlternatives getPpa() {
        return this.ppa;
    }

    /**
     * Sets the value of the PPAproperty.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPpa(PresentationPolicyAlternatives value) {
        this.ppa = value;
    }

    /**
     * Gets the value of the Token property.
     *
     * @return possible object is {@link String }
     *
     */
    public PresentationToken getToken() {
        return this.token;
    }

    /**
     * Sets the value of the Token property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setToken(PresentationToken value) {
        this.token = value;
    }
}

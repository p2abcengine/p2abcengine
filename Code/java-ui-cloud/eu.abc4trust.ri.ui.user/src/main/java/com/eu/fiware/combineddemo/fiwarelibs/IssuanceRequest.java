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

package com.eu.fiware.combineddemo.fiwarelibs;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "IssuanceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IssuanceRequest", propOrder = {
    "userName",
    "password",
    "credentialSpecificationUID"
})
public class IssuanceRequest implements Serializable {

    private static final long serialVersionUID = -5744984022839227360L;
    
    @XmlElement(name = "UserName")
    protected String userName;
    
    @XmlElement(name = "Password")
    protected String password;
    
    @XmlElement(name = "CredentialSpecificationUID")
    protected String credentialSpecificationUID;

    /**
     * Gets the value of the Username property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets the value of the Username property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setUsername(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the Password property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the value of the Password property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the CredentialSpecUID property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getCredentialSpecificationUID() {
        return this.credentialSpecificationUID;
    }

    /**
     * Sets the value of the CredentialSpecUID property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setCredentialSpecificationUID(String value) {
        this.credentialSpecificationUID = value;
    }
}

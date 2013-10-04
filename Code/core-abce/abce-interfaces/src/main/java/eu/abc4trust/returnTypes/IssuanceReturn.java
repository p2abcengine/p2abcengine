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

package eu.abc4trust.returnTypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;

@XmlRootElement(name = "IssuanceReturn", namespace = "http://abc4trust.eu/wp2/abcschemav1.0")
public class IssuanceReturn {

    @XmlElement
    public IssuanceMessage im;

    @XmlElement
    public CredentialDescription cd;

    @XmlElement
    public UiIssuanceArguments uia;

    public IssuanceReturn(){
        this.im = null;
        this.cd = null;
        this.uia = null;
    }

    public IssuanceReturn(UiIssuanceArguments uia) {
        this.im = null;
        this.cd = null;
        this.uia = uia;
    }

    public IssuanceReturn(IssuMsgOrCredDesc imcd) {
        this.im = imcd.im;
        this.cd = imcd.cd;
        this.uia = null;
    }
}

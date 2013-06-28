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

package eu.abc4trust.ri.servicehelper.issuer;

/**
 * Helper class. Holds response From IssuerABC - marshaled to String
 */
public class IssuanceResponse_String {
    public final String xml;
    public final boolean lastmessage;
    public final String context;

    public IssuanceResponse_String(String xml, boolean lastmessage) {
        this.xml = xml;
        this.lastmessage = lastmessage;
        this.context = null;
    }

    public IssuanceResponse_String(String xml, boolean lastmessage, String context) {
        this.xml = xml;
        this.lastmessage = lastmessage;
        this.context = context;
    }

    public IssuanceResponse_String(String xml, String context) {
        this.xml = xml;
        this.lastmessage = false;
        this.context = context;
    }
}

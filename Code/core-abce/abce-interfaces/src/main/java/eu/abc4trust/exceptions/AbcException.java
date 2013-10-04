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

package eu.abc4trust.exceptions;

import java.util.ArrayList;
import java.util.List;

public class AbcException extends Exception {
    private static final long serialVersionUID = 7845400460719024892L;
    public final List<String> errorMessages;

    public AbcException() {
        this.errorMessages = new ArrayList<String>();
    }

    public AbcException(Exception e) {
        super(e);
        this.errorMessages = new ArrayList<String>();
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (String line : this.errorMessages) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }
}

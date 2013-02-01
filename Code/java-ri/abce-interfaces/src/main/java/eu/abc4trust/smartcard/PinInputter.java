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

package eu.abc4trust.smartcard;

import java.util.List;

public interface PinInputter {

    /**
     * This method performs input of pin code, possibly presented by a graphical
     * user interface, allowing the User to input a pin code for each of the
     * given smartcards.
     * 
     * @param smartcards
     *            The list of smartcards.
     * @return The list of pin codes in the same order as the corresponding
     *         smartcard appears in the smartcards list.
     */
    public List<Integer> getPin(List<Smartcard> smartcards);
}

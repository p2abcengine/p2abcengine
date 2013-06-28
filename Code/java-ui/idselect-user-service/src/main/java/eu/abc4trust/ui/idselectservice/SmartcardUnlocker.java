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

package eu.abc4trust.ui.idselectservice;

import java.util.LinkedList;
import java.util.List;

import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;

public class SmartcardUnlocker {

    public boolean unlock(String pinsStr, CardStorage cardStorage) {
        String[] as = pinsStr.split(",");
        List<Integer> pins = new LinkedList<Integer>();
        for (String s : as) {
            pins.add(Integer.parseInt(s.trim()));
        }

        List<BasicSmartcard> closedSmartcards = cardStorage.getClosedSmartcards();

        boolean b = cardStorage.unlockClosedSmartcards(closedSmartcards,
                pins);
        return b;
    }

}

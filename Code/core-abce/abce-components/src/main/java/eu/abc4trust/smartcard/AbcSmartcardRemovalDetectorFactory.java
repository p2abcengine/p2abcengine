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

import java.util.concurrent.ConcurrentHashMap;

import javax.smartcardio.CardTerminal;

public class AbcSmartcardRemovalDetectorFactory {

    private final CardStorage cardStorage;

    public AbcSmartcardRemovalDetectorFactory(CardStorage cardStorage) {
        super();
        this.cardStorage = cardStorage;
    }

    /**
     * 
     * @param terminal
     * @param smartcard
     * @param terminalNameToSmartcardRemovalDetector
     * @param timeout
     *            - 3000 is three seconds.
     * @return
     */
    public AbcSmartcardRemovalDetector create(
            CardTerminal terminal,
            Smartcard smartcard,
            ConcurrentHashMap<String, AbcSmartcardRemovalDetector> terminalNameToSmartcardRemovalDetector,
            Long timeout) {
        return new AbcSmartcardRemovalDetector(terminal, smartcard,
                this.cardStorage, terminalNameToSmartcardRemovalDetector,
                timeout);
    }

}

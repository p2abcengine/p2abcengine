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

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * 
 * AbcSmartcardRemovalDetector
 * 
 * This runnable stops execution if the executing thread is interrupted.
 * 
 * @author Janus Dam Nielsen
 */
public class AbcSmartcardRemovalDetector implements Runnable {

    private final CardTerminal terminal;
    private final Smartcard smartcard;
    private final CardStorage cardStorage;
    private final ConcurrentHashMap<String, AbcSmartcardRemovalDetector> scs;
    private final long timeout;

    public AbcSmartcardRemovalDetector(CardTerminal terminal,
            Smartcard smartcard, CardStorage cardStorage,
            ConcurrentHashMap<String, AbcSmartcardRemovalDetector> scs,
            long timeout) {
        this.terminal = terminal;
        this.smartcard = smartcard;
        this.cardStorage = cardStorage;
        this.scs = scs;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        boolean done = Thread.currentThread().isInterrupted();
        while (!done) {
            try {
                boolean cardIsAbsent = this.terminal
                        .waitForCardAbsent(this.timeout);
                done = Thread.currentThread().isInterrupted();
                if (cardIsAbsent) {
                    this.removeSmartcard();
                    done = true;
                }
            } catch (CardException ex) {
                this.removeSmartcard();
                done = true;
            }
        }

    }

    private void removeSmartcard() {
        this.cardStorage.removeSmartcard(this.smartcard);
        this.scs.remove(this.terminal.getName());
    }

}

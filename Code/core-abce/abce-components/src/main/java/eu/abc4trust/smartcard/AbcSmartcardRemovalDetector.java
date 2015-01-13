//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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

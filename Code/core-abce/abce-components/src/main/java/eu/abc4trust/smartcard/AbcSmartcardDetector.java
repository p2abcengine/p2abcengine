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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;


/**
 * AbcSmartcardDetector
 * 
 * This runnable stops execution if the executing thread is interrupted.
 * 
 * 
 * @author Janus Dam Nielsen
 */
public class AbcSmartcardDetector implements Runnable {

    private final AbcTerminalFactory factory;
    private final ConcurrentHashMap<String, AbcSmartcardRemovalDetector> terminalNameToSmartcardRemovalDetector;
    private final List<Thread> threads;
    private final CardStorage cardStorage;
    private final AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactory;
    private final long timeout;
    private boolean alive;
    private final Logger logger;
    private final Random rand;

    public AbcSmartcardDetector(AbcTerminalFactory terminalFactory,
            CardStorage cardStorage,
            AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactory,
            long timeout, Random rand) {
        super();
        this.factory = terminalFactory;
        this.terminalNameToSmartcardRemovalDetector = new ConcurrentHashMap<String, AbcSmartcardRemovalDetector>();
        this.threads = new LinkedList<Thread>();
        this.cardStorage = cardStorage;
        this.abcSmartcardRemovalDetectorFactory = abcSmartcardRemovalDetectorFactory;
        this.timeout = timeout;
        this.logger = Logger.getLogger(AbcSmartcardDetector.class
                .getCanonicalName());
        this.rand = rand;
    }

    @Override
    public void run() {
        this.alive = true;
        boolean done = Thread.currentThread().isInterrupted();
        while (!done) {
            try {
                List<Smartcard> availableSmartcards = this
                        .getAvailableSmartcards();
                if(availableSmartcards.size() != 0){
                    this.logger.log(Level.INFO, "Newly available smartcards: "
                            + availableSmartcards);
                }
                this.cardStorage.addClosedSmartcards(availableSmartcards);

                Thread.sleep(this.timeout);
                done = Thread.currentThread().isInterrupted();
            } catch (InterruptedException ex) {
                done = true;
            }
        }
        // Stop the threads.
        for (Thread t : this.threads) {
            t.interrupt();
        }
        this.alive = false;
    }


    private List<Smartcard> getAvailableSmartcards() {
        List<Smartcard> availableSmartcards = new LinkedList<Smartcard>();
        try {
            List<CardTerminal> terminals = this.factory.terminals().list();

            for (CardTerminal terminal : terminals) {
                if (!this.terminalNameToSmartcardRemovalDetector.containsKey(terminal.getName())) {
                    String name = terminal.getName();
                    if (!terminal.isCardPresent()) {
                        continue;
                    }

                    Card card = terminal.connect("*");
                    Smartcard smartcard = new HardwareSmartcard(terminal, card, this.rand);

                    availableSmartcards.add(smartcard);
                    AbcSmartcardRemovalDetector removalDetector = this.abcSmartcardRemovalDetectorFactory
                            .create(terminal, smartcard,
                                    this.terminalNameToSmartcardRemovalDetector,
                                    this.timeout);
                    Thread thread = new Thread(removalDetector,
                            "RemovalDetector");
                    this.threads.add(thread);
                    this.terminalNameToSmartcardRemovalDetector.putIfAbsent(
                            name, removalDetector);
                    thread.start();
                }
            }
        } catch (Exception ex) {
            this.logger.log(Level.INFO, ex.getMessage());
        }
        return availableSmartcards;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void stop() {
        Thread.currentThread().interrupt();
        this.alive = false;
    }

}

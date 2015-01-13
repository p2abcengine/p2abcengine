//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

/**
 * CardStorage
 * 
 * Thread safety policy: Smartcards are created in either the thread running the
 * ABCE (the ABCE thread) or in the smart-card detector. And only used in the
 * ABCE thread. Smart-cards are added to the card storage by both threads thus
 * we guard the collection holding the smart-cards with a lock. But the
 * smart-card objects are not thread safe because they are only manipulated by
 * the ABCE thread.
 * 
 * The CardStorage does not support client locking, because the lock is a
 * private internal lock object.
 * 
 * @author Janus Dam Nielsen
 */
public class CardStorage {

    private final Map<URI, BasicSmartcard> cards;
    private final Map<URI, Integer> pins;
    private final Object lock;
    private final List<BasicSmartcard> closedCards;
    private final Map<BasicSmartcard, URI> smartcardToURI;

    @Inject
    public CardStorage() {
        super();
        this.cards = new HashMap<URI, BasicSmartcard>();
        this.pins = new HashMap<URI, Integer>();
        this.lock = new Object();
        this.closedCards = new LinkedList<BasicSmartcard>();
        this.smartcardToURI = new HashMap<BasicSmartcard, URI>();
    }

    public boolean addSmartcard(BasicSmartcard smartcard, int pin) {
        synchronized (this.lock) {
            URI cardUri = smartcard.getDeviceURI(pin);
            if (null == cardUri) {
                return false;
            }
            this.cards.put(cardUri, smartcard);
            this.pins.put(cardUri, pin);
//            this.smartcardToPin.put(smartcard, pin);
            this.smartcardToURI.put(smartcard, cardUri);
        }
        return true;
    }

    public boolean removeSmartcard(BasicSmartcard smartcard) {
        synchronized (this.lock) {
            if (this.smartcardToURI.containsKey(smartcard)) {
                return this.localRemoveSmartcard(smartcard);
            }

            if (this.closedCards.contains(smartcard)) {
                this.closedCards.remove(smartcard);
                return true;
            }
            return false;
        }
    }

    private boolean localRemoveSmartcard(BasicSmartcard smartcard/*, URI cardUri*/) {
        synchronized (this.lock) {
            URI cardUri = this.smartcardToURI.remove(smartcard);
            if(cardUri!=null) {
                this.cards.remove(cardUri);
                this.pins.remove(cardUri);
            }
        }
        return true;
    }

    public BasicSmartcard getSmartcard(URI smartcardUri) {
        synchronized (this.lock) {
            return this.cards.get(smartcardUri);
        }
    }

    public int getPin(URI smartcardUri) {
        synchronized (this.lock) {
            return this.pins.get(smartcardUri);
        }
    }

    public Map<URI, BasicSmartcard> getSmartcards() {
        synchronized (this.lock) {
        	return Collections.unmodifiableMap(new HashMap<URI, BasicSmartcard>(this.cards));
        }
    }

    public boolean addSmartcards(List<BasicSmartcard> smartcards, List<Integer> pins) {
        synchronized (this.lock) {
            boolean b = true;
            for (int inx = 0; inx < smartcards.size(); inx++) {
                BasicSmartcard sc = smartcards.get(inx);
                int pin = pins.get(inx);
                boolean addSmartcard = this.addSmartcard(sc, pin);
                if (addSmartcard) {
                    if (this.closedCards.contains(sc)) {
                        this.closedCards.remove(sc);
                    }
                }
                b = b && addSmartcard;
            }
            return b;
        }
    }

    public void removeSmartcards(List<BasicSmartcard> smartcards, List<Integer> pins) {
        synchronized (this.lock) {
            for (int inx = 0; inx < smartcards.size(); inx++) {
                BasicSmartcard sc = smartcards.get(inx);
                this.localRemoveSmartcard(sc/*, pin*/);
            }
        }
    }

    public List<BasicSmartcard> getClosedSmartcards() {
        synchronized (this.lock) {
            return this.closedCards;
        }
    }

    public void addClosedSmartcards(List<Smartcard> closedCards) {
        synchronized (this.lock) {
            this.closedCards.addAll(closedCards);
        }
    }

    public boolean unlockClosedSmartcards(List<BasicSmartcard> closedCards,
            List<Integer> pins) {
        synchronized (this.lock) {
            return this.addSmartcards(closedCards, pins);
        }
    }

}

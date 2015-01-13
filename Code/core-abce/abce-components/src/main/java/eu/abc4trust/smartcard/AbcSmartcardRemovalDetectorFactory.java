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

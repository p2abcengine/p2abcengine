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

package eu.abc4trust.abce.utils;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import eu.abc4trust.smartcard.HardwareSmartcard;

public class SmartcardVirginMode {

    private static Random rand = new Random(42);

    /**
     * @param args
     */
    public static void main(String[] args) {
        HardwareSmartcard s = getSmartcard();
        byte[] challenge = s.getChallenge(16);
        String c = challengeToHexString(challenge);
        System.out.println("CryptoExpert Rescue Homepage : http://www.cryptoexperts.com/abc4trustlite/");
        System.out.println("User : abc4trust, Password : xxxx");
        System.out.println("Challenge: " + c);

        KeepConnectionAlive kca = new SmartcardVirginMode().new KeepConnectionAlive(s);
        Thread t = new Thread(kca, "Keep connection alive");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String HEXES = "0123456789ABCDEF";
    public static String challengeToHexString(byte[] challenge){
        if ( challenge == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * challenge.length );
        for ( final byte b : challenge ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
            .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static HardwareSmartcard getSmartcard() {
        HardwareSmartcard smartcard = null;
        try {
            List<CardTerminal> terminals = TerminalFactory.getDefault()
                    .terminals().list();

            for (CardTerminal terminal : terminals) {
                if (!terminal.isCardPresent()) {
                    continue;
                }

                Card card = terminal.connect("*");
                smartcard = new HardwareSmartcard(terminal, card, rand);
                //smartcard.initializeNoOfIssuersAndCreds(pin);
            }
        } catch (CardException ex) {
            throw new RuntimeException(ex);
        }
        return smartcard;
    }

    private class KeepConnectionAlive implements Runnable{
        private final HardwareSmartcard s;

        public KeepConnectionAlive(HardwareSmartcard s){
            this.s = s;
        }

        @Override
        public void run() {
            Scanner scan = new Scanner(System.in);
            String macString = scan.nextLine();
            System.out.println("Read this line: " + macString);
            byte[] mac = new byte[16];
            int index = 0;
            for(int i = 0; i < 16; i++){
                String hex = macString.substring(index, index+2);
                index += 2;
                System.out.println("Parsed this hex: " + hex);
                mac[i] = (byte)Integer.parseInt(hex, 16);
            }
            System.out.println("Did we suceed?: " +this.s.setVirginMode(mac));
        }

    }
}

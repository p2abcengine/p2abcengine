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

package eu.abc4trust.abce.utils;

import java.security.SecureRandom;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.smartcard.AbcSmartcardDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetectorFactory;
import eu.abc4trust.smartcard.AbcTerminalFactory;
import eu.abc4trust.smartcard.AbcTerminalFactoryImpl;
import eu.abc4trust.smartcard.CardStorage;

public class SmartcardTester {

    private static final long TIMEOUT = 3000;

    public static void main(String[] args) throws InterruptedException {
        Random random = new SecureRandom();
        AbcTerminalFactory terminalFactory = new AbcTerminalFactoryImpl();
        Injector injector = Guice.createInjector(ProductionModuleFactory.newModule());
        CardStorage cardStorage = injector.getInstance(CardStorage.class);
        AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactory = new AbcSmartcardRemovalDetectorFactory(
                cardStorage);
        AbcSmartcardDetector detector = new AbcSmartcardDetector(
                terminalFactory, cardStorage,
                abcSmartcardRemovalDetectorFactory, TIMEOUT, random);
        Thread thread = new Thread(detector, "Smart card detector");
        thread.start();
        thread.join();
    }
}

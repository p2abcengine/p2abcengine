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
        Thread thread = new Thread(detector);
        thread.start();
        thread.join();
    }
}

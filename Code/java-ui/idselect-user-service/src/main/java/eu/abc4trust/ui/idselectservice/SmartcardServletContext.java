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

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import eu.abc4trust.smartcard.AbcSmartcardDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetectorFactory;
import eu.abc4trust.smartcard.AbcTerminalFactory;
import eu.abc4trust.smartcard.AbcTerminalFactoryImpl;
import eu.abc4trust.smartcard.CardStorage;

public class SmartcardServletContext implements ServletContextListener {
    private static final int TIMEOUT = 3000;
//    private static AbcSmartcardDetector detector;
    private static Thread thread;
    private static Random random = new SecureRandom();
    public static AtomicReference<CardStorage> cardStorageReference;

    public void contextInitialized(ServletContextEvent event) {
        cardStorageReference = new AtomicReference<CardStorage>();
    }

//    public static boolean startDetector() {
//        CardStorage cardStorage = cardStorageReference.get();
//        if ((detector == null)
//                || (!detector.isAlive() && (cardStorage != null))) {
//            AbcTerminalFactory terminalFactory = new AbcTerminalFactoryImpl();
//
//            AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactory = new AbcSmartcardRemovalDetectorFactory(
//                    cardStorage);
//            detector = new AbcSmartcardDetector(terminalFactory,
//                    cardStorage,
//                    abcSmartcardRemovalDetectorFactory,
//                    TIMEOUT, random);
//            thread = new Thread(detector);
//            thread.start();
//            return true;
//        }
//        return false;
//    }    

    public void contextDestroyed(ServletContextEvent event) {
//        if (detector != null) {
//            detector.stop();
//        }
    }
}

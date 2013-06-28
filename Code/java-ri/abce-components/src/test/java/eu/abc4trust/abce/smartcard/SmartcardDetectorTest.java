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

package eu.abc4trust.abce.smartcard;

import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import eu.abc4trust.smartcard.AbcSmartcardDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetectorFactory;
import eu.abc4trust.smartcard.AbcTerminalFactory;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.Smartcard;

public class SmartcardDetectorTest {

    private static final Long TIMEOUT = 3000l;

    @SuppressWarnings("unchecked")
    @Test
    public void smartcardDetectorTest() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        Card cardMock = EasyMock.createMock(Card.class);
        CardChannel mockChannel = EasyMock.createMock(CardChannel.class);
        EasyMock.expect(cardMock.getBasicChannel()).andReturn(mockChannel);
        EasyMock.replay(cardMock);


        CardTerminal cardTerminalMock = EasyMock.createMock(CardTerminal.class);
        EasyMock.expect(cardTerminalMock.isCardPresent()).andReturn(true);
        EasyMock.expect(cardTerminalMock.connect("*")).andReturn(cardMock);
        EasyMock.expect(cardTerminalMock.getName()).andReturn("TestTerminal");
        EasyMock.expect(cardTerminalMock.getName()).andReturn("TestTerminal");
        EasyMock.replay(cardTerminalMock);
        List<CardTerminal> terminalsMock = new LinkedList<CardTerminal>();
        terminalsMock.add(cardTerminalMock);

        CardTerminals cardTerminalsMock = EasyMock
                .createMock(CardTerminals.class);
        EasyMock.expect(cardTerminalsMock.list()).andReturn(terminalsMock)
        .atLeastOnce();

        EasyMock.replay(cardTerminalsMock);

        AbcTerminalFactory terminalFactory = EasyMock
                .createMock(AbcTerminalFactory.class);
        EasyMock.expect(terminalFactory.terminals())
        .andReturn(cardTerminalsMock).atLeastOnce();
        EasyMock.replay(terminalFactory);

        CardStorage cardStorageMock = EasyMock.createMock(CardStorage.class);
        cardStorageMock.addClosedSmartcards(isA(List.class));
        EasyMock.expectLastCall().andAnswer(
                new IAnswer<Void>() {

                    @Override
                    public Void answer()
                            throws Throwable {
                        latch.countDown();
                        return null;
                    }

                });

        EasyMock.replay(cardStorageMock);

        AbcSmartcardRemovalDetector abcSmartcardRemovalDetectorMock = EasyMock
                .createMock(AbcSmartcardRemovalDetector.class);
        abcSmartcardRemovalDetectorMock.run();
        EasyMock.replay(abcSmartcardRemovalDetectorMock);

        AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactoryMock = EasyMock
                .createMock(AbcSmartcardRemovalDetectorFactory.class);
        EasyMock.expect(
                abcSmartcardRemovalDetectorFactoryMock.create(
                        isA(CardTerminal.class), isA(Smartcard.class),
                        isA(ConcurrentHashMap.class), isA(Long.class)))
                        .andReturn(abcSmartcardRemovalDetectorMock);
        EasyMock.replay(abcSmartcardRemovalDetectorFactoryMock);

        Random random = new SecureRandom();
        AbcSmartcardDetector detector = new AbcSmartcardDetector(
                terminalFactory, cardStorageMock,
                abcSmartcardRemovalDetectorFactoryMock, TIMEOUT, random);
        Thread t = new Thread(detector);
        t.start();

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void smartcardRemovalDetectorTest() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch removalDectorLatch = new CountDownLatch(1);

        Card cardMock = EasyMock.createMock(Card.class);
        CardChannel mockChannel = EasyMock.createMock(CardChannel.class);
        EasyMock.expect(cardMock.getBasicChannel()).andReturn(mockChannel);
        EasyMock.replay(cardMock);

        CardTerminal cardTerminalMock = EasyMock.createMock(CardTerminal.class);
        EasyMock.expect(cardTerminalMock.isCardPresent()).andReturn(true);
        EasyMock.expect(cardTerminalMock.connect("*")).andReturn(cardMock);

        EasyMock.expect(cardTerminalMock.getName()).andReturn("TestTerminal")
                .atLeastOnce();
        EasyMock.expect(cardTerminalMock.waitForCardAbsent(TIMEOUT)).andAnswer(
                new IAnswer<Boolean>() {

                    @Override
                    public Boolean answer() throws Throwable {
                        removalDectorLatch
                        .await(TIMEOUT, TimeUnit.MILLISECONDS);
                        return true;
                    }
                });
        EasyMock.replay(cardTerminalMock);
        List<CardTerminal> terminalsMock = new LinkedList<CardTerminal>();
        terminalsMock.add(cardTerminalMock);

        CardTerminals cardTerminalsMock = EasyMock
                .createMock(CardTerminals.class);
        EasyMock.expect(cardTerminalsMock.list()).andReturn(terminalsMock)
        .atLeastOnce();
        EasyMock.replay(cardTerminalsMock);

        AbcTerminalFactory terminalFactory = EasyMock
                .createMock(AbcTerminalFactory.class);
        EasyMock.expect(terminalFactory.terminals()).andReturn(
                cardTerminalsMock).atLeastOnce();
        EasyMock.replay(terminalFactory);

        CardStorage cardStorageMock = EasyMock.createMock(CardStorage.class);
        cardStorageMock.addClosedSmartcards(isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                removalDectorLatch.countDown();
                return null;
            }

        });
        EasyMock.expectLastCall().atLeastOnce();

        cardStorageMock.removeSmartcard(isA(Smartcard.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {

            @Override
            public Void answer() throws Throwable {
                latch.countDown();
                return null;
            }

        });

        EasyMock.replay(cardStorageMock);

        AbcSmartcardRemovalDetector abcSmartcardRemovalDetectorMock = EasyMock
                .createMock(AbcSmartcardRemovalDetector.class);
        abcSmartcardRemovalDetectorMock.run();
        EasyMock.replay(abcSmartcardRemovalDetectorMock);

        AbcSmartcardRemovalDetectorFactory abcSmartcardRemovalDetectorFactory = new AbcSmartcardRemovalDetectorFactory(
                cardStorageMock);

        Random random = new SecureRandom();
        AbcSmartcardDetector detector = new AbcSmartcardDetector(
                terminalFactory, cardStorageMock,
                abcSmartcardRemovalDetectorFactory, TIMEOUT, random);
        Thread t = new Thread(detector);
        t.start();

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }
}

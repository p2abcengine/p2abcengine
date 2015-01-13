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

package eu.abc4trust.abce.smartcard;

import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
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
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.smartcard.AbcSmartcardDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetector;
import eu.abc4trust.smartcard.AbcSmartcardRemovalDetectorFactory;
import eu.abc4trust.smartcard.AbcTerminalFactory;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.Smartcard;

public class SmartcardDetectorTest {

    private static final Long TIMEOUT = 3000L;

    @SuppressWarnings("unchecked")
    @Test
    public void smartcardDetectorTest() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        Card cardMock = EasyMock.createMock(Card.class);
        CardChannel mockChannel = EasyMock.createMock(CardChannel.class);
        EasyMock.expect(cardMock.getBasicChannel()).andReturn(mockChannel);
        
        cardMock.beginExclusive();
        cardMock.endExclusive();
        
        EasyMock.replay(cardMock);


        CardTerminal cardTerminalMock = EasyMock.createMock(CardTerminal.class);
        EasyMock.expect(cardTerminalMock.isCardPresent()).andReturn(true);
        EasyMock.expect(cardTerminalMock.connect("*")).andReturn(cardMock);
        EasyMock.expect(cardTerminalMock.getName()).andReturn("TestTerminal").atLeastOnce();
        EasyMock.replay(cardTerminalMock);
        List<CardTerminal> terminalsMock = new LinkedList<CardTerminal>();
        terminalsMock.add(cardTerminalMock);

        //TODO: Add beginExclusive and whatever else is found in HardwareSmartcard transmit command thing for the Card

        
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
        Thread t = new Thread(detector, "Smart card detector");
        t.start();

        boolean v = latch.await(10, TimeUnit.SECONDS);
        assertTrue(v);
        assertEquals(0, latch.getCount());
    }

    @SuppressWarnings("unchecked")
    @Test @Ignore("Smartcards not working")
    public void smartcardRemovalDetectorTest() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch removalDectorLatch = new CountDownLatch(1);

        Card cardMock = EasyMock.createMock(Card.class);
        CardChannel mockChannel = EasyMock.createMock(CardChannel.class);
        
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put(new byte[]{-68, (byte) -114, 0, 0, 0});
        buf.position(0);
        ResponseAPDU resp = new ResponseAPDU(new byte[]{90,00});
        EasyMock.expect(mockChannel.transmit(new CommandAPDU(buf))).andReturn(resp).atLeastOnce();
        
        EasyMock.replay(mockChannel);
        
        EasyMock.expect(cardMock.getBasicChannel()).andReturn(mockChannel).atLeastOnce();
        
        cardMock.beginExclusive();
        EasyMock.expectLastCall().atLeastOnce();
        cardMock.endExclusive();
        EasyMock.expectLastCall().atLeastOnce();        
        
        EasyMock.replay(cardMock);

        CardTerminal cardTerminalMock = EasyMock.createMock(CardTerminal.class);
        EasyMock.expect(cardTerminalMock.isCardPresent()).andReturn(true).atLeastOnce();
        EasyMock.expect(cardTerminalMock.connect("*")).andReturn(cardMock).atLeastOnce();

        EasyMock.expect(cardTerminalMock.getName()).andReturn("TestTerminal")
        .atLeastOnce();
        EasyMock.expect(cardTerminalMock.waitForCardAbsent(TIMEOUT)).andAnswer(
                new IAnswer<Boolean>() {

                    @Override
                    public Boolean answer() throws Throwable {
                        boolean v = removalDectorLatch
                                .await(TIMEOUT, TimeUnit.MILLISECONDS);
                        return v;
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
        Thread t = new Thread(detector, "Smart card detector");
        t.start();

        boolean v = latch.await(10, TimeUnit.SECONDS);
        assertTrue(v);
        assertEquals(0, latch.getCount());
    }
}

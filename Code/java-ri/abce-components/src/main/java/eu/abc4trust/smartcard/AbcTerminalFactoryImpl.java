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

package eu.abc4trust.smartcard;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class AbcTerminalFactoryImpl implements AbcTerminalFactory {

    private final TerminalFactory factory;

    public AbcTerminalFactoryImpl() {
        this(TerminalFactory.getDefault());
    }

    public AbcTerminalFactoryImpl(TerminalFactory factory) {
        super();
        this.factory = factory;
    }

    @Override
    public CardTerminals terminals() {
        return this.factory.terminals();
    }

}

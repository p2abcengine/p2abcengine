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

package eu.abc4trust.cryptoEngine.revauth;

import java.io.Serializable;

import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorHistory;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.XMLSerializer;

public class RevocationAuthorityState implements Serializable {

    private static final long serialVersionUID = -650191843836654832L;

    private final int epoch;
    private final String history;
    private final String state;

    public RevocationAuthorityState(int epoch,
            AccumulatorHistory history, AccumulatorState state) {
        super();
        this.epoch = epoch;
        XMLSerializer xmlSerializer = XMLSerializer.getInstance();
        this.history = xmlSerializer.serialize(history);
        this.state = xmlSerializer.serialize(state);
    }

    public AccumulatorState getState() {
        return (AccumulatorState) Parser.getInstance().parse(this.state);
    }

    public AccumulatorHistory getHistory() {
        return (AccumulatorHistory) Parser.getInstance().parse(this.history);
    }

    public int getEpoch() {
        return this.epoch;
    }
}

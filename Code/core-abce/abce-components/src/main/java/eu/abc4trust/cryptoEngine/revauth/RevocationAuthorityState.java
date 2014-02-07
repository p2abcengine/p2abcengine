//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

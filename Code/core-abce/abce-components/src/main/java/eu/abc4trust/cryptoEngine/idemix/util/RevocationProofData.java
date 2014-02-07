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

package eu.abc4trust.cryptoEngine.idemix.util;

import java.io.Serializable;
import java.net.URI;

import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;

public class RevocationProofData implements Serializable{

	private static final long serialVersionUID = 8664597021986269994L;
	
	private final URI credSpecUid;
    private String tempName;
    private final URI keyUid;
    private final int epoch;
    private final AccumulatorWitness witness;

    public RevocationProofData(URI credSpecUid, URI keyUid, int epoch,
            String tempName, AccumulatorWitness witness) {
        this.credSpecUid = credSpecUid;
        this.keyUid = keyUid;
        this.epoch = epoch;
        this.tempName = tempName;
        this.witness = witness;
    }

    public URI getCredSpecUid() {
        return this.credSpecUid;
    }

    public String getTempName(int inx) {
        this.tempName = this.tempName + inx;
        return this.tempName;
    }

    public URI getKeyUid() {
        return this.keyUid;
    }

    public int getEpoch() {
        return this.epoch;
    }

    public AccumulatorWitness getWitness() {
        return this.witness;
    }

    public String getTempName() {
        return this.tempName;
    }

}

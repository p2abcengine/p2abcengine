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

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

import org.w3c.dom.Element;

import com.ibm.zurich.idmx.showproof.Proof;

/**
 * A verifiable claim is a claim whose validity can be verified.
 * The validity is verified with respect to the technology-specific evidence.
 */
public interface VerifiableClaim extends Claim {

    /**
     * Verifies whether this claim is valid.
     * 
     */
    public boolean isValid();

    /**
     * Returns the evidence of this claim as String.
     * @return the evidence of this claim.
     */
    public String getEvidenceAsString();

    public void setEvidence(Proof proof);

    public Element getEvidenceAsElement();

}

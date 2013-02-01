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

package eu.abc4trust.abce.internal.revocation;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.util.XmlUtils;

public class UserRevocation {

    private final KeyManager keyManager;

    @Inject
    public UserRevocation(KeyManager keyManager) {
        super();
        this.keyManager = keyManager;
    }

    public void compareRevocationHandleToNonRevocationEvidence(
            BigInteger revocationAttrValue, NonRevocationEvidence nre)
                    throws CryptoEngineException {
        Parser xmlParser = Parser.getInstance();
        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness w1 = (AccumulatorWitness) xmlParser
                .parse(witnessElement);

        BigInteger witness = w1.getValue();
        if (revocationAttrValue.compareTo(witness) != 0) {
            throw new CryptoEngineException(
                    "Revocation handle does not correspond to witness in non-revocation evidence");
        }
    }

    private Credential updateNonRevocationEvidence(Credential cred,
            RevocationInformation revInfo) {

        NonRevocationEvidence nre = (NonRevocationEvidence) XmlUtils.unwrap(cred
          .getCryptoParams().getAny().get(1), NonRevocationEvidence.class);

        RevocationUtility.updateNonRevocationEvidence(nre, revInfo);
        return cred;
    }

    private RevocationInformation getCurrentRevocationInformation(URI raparsuid)
            throws CryptoEngineException {
        try {
            return this.keyManager.getCurrentRevocationInformation(raparsuid);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
    }

    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts) throws CryptoEngineException {

        // TODO(jdn): This might only be working if the existing NRE is the
        // original NRE.
        RevocationInformation revInfo = null;
        revInfo = this.getCurrentRevocationInformation(raparsuid);

        return this.updateNonRevocationEvidence(cred, revInfo);
    }

    public Credential updateNonRevocationEvidence(Credential cred,
            URI raparsuid, List<URI> revokedatts, URI revinfouid)
                    throws CryptoEngineException {
        RevocationInformation revInfo = null;
        try {
            revInfo = this.keyManager.getRevocationInformation(raparsuid,
                    revinfouid);
            return this.updateNonRevocationEvidence(cred, revInfo);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }

    }

}

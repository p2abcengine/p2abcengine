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

import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE;
import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_DATA_TYPE;
import static eu.abc4trust.abce.internal.revocation.RevocationConstants.REVOCATION_HANDLE_ENCODING;

import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.RevocationMessageType;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationMessage;

public class IssuerRevocation {

    private final ContextGenerator contextGenerator;
    private final KeyManager keyManager;
    private final RevocationProxy revocationProxy;

    @Inject
    public IssuerRevocation(KeyManager keyManager,
            RevocationProxy revocationProxy, ContextGenerator contextGenerator) {
        super();
        this.contextGenerator = contextGenerator;
        this.keyManager = keyManager;
        this.revocationProxy = revocationProxy;
    }

    /**
     * Make sure the credential is revocable before calling this method.
     * 
     * @param attrs
     * @param ip
     * @param context
     * @return
     * @throws CryptoEngineException
     */
    public NonRevocationEvidence addRevocationHandleAttribute(
            List<Attribute> attrs, IssuancePolicy ip,
            URI context) throws CryptoEngineException {

        Attribute revocationHandleAttribute = null;
        boolean found = false;
        for (Attribute att : attrs) {
            if (att.getAttributeDescription().getType()
                    .compareTo(REVOCATION_HANDLE) == 0) {
                found = true;
                revocationHandleAttribute = att;
                URI dataType = att.getAttributeDescription().getDataType();
                if (dataType.compareTo(REVOCATION_HANDLE_DATA_TYPE) != 0) {
                    throw new RuntimeException("Datatype is incorrect: \""
                            + dataType + "\"");
                }
                URI encoding = att.getAttributeDescription().getEncoding();
                if (encoding.compareTo(REVOCATION_HANDLE_ENCODING) != 0) {
                    throw new RuntimeException("Encoding is incorrect: \""
                            + encoding + "\"");
                }
            }
        }

        if (!found) {
            revocationHandleAttribute = new Attribute();
            revocationHandleAttribute.setAttributeUID(URI.create(""
                    + this.contextGenerator.getUniqueContext(URI
                            .create("urn:abc4trust:1.0:attribute"))));
            URI type = REVOCATION_HANDLE;
            URI dataType = REVOCATION_HANDLE_DATA_TYPE;
            URI encoding = REVOCATION_HANDLE_ENCODING;
            AttributeDescription attd = new AttributeDescription();
            attd.setDataType(dataType);
            attd.setEncoding(encoding);
            attd.setType(type);
            revocationHandleAttribute.setAttributeDescription(attd);
            attrs.add(revocationHandleAttribute);
        }

        // if the user replied already
        // Compute non-revocation evidence if the credential is
        // revocable.

        NonRevocationEvidence nre = this
                .addRevocationWitnessToRevocationHandle(
                        revocationHandleAttribute, ip, context);
        return nre;
    }

    private NonRevocationEvidence addRevocationWitnessToRevocationHandle(
            Attribute revocationHandleAttribute, IssuancePolicy ip, URI context)
                    throws CryptoEngineException {

        NonRevocationEvidence nre = null;
        IssuerParameters issuerParameters = null;

        // Get system and issuer parameters from key manager
        try {
            issuerParameters = this.keyManager.getIssuerParameters(ip
                    .getCredentialTemplate().getIssuerParametersUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        URI revParamsUid = issuerParameters.getRevocationParametersUID();
        try {

            nre = this.getNextNonRevocationEvidence(revParamsUid, context,
                    revocationHandleAttribute);
        } catch (Exception ex) {
            throw new CryptoEngineException(ex);
        }

        // Get revocation handle from non-revocation evidence and
        // add it to the issuer attributes
        Element witnessElement = (Element) nre.getCryptoParams().getAny()
                .get(0);
        AccumulatorWitness witness = (AccumulatorWitness) Parser.getInstance()
                .parse(witnessElement);

        revocationHandleAttribute.setAttributeValue(witness.getValue());
        return nre;
    }

    private NonRevocationEvidence getNextNonRevocationEvidence(
            URI revParamsUid, URI context, Attribute revocationHandleAttribute)
                    throws Exception {
        // Call RA for the next NRE through the Revocation Proxy:
        // get RA parameters using UID from the issuer parameters.

        // Wrap the request into the Revocation message.
        RevocationMessage revmsg = new RevocationMessage();
        revmsg.setContext(context);
        revmsg.setRevocationAuthorityParametersUID(revParamsUid);
        revmsg.setCryptoParams(new CryptoParams());

        revmsg.getCryptoParams().getAny()
          .add(RevocationUtility.serializeRevocationMessageType(RevocationMessageType.REQUEST_REVOCATION_HANDLE));
        revmsg.getCryptoParams().getAny().add(new ObjectFactory().createAttribute(revocationHandleAttribute));

        RevocationAuthorityParameters revParams = this.keyManager
                .getRevocationAuthorityParameters(revParamsUid);

        RevocationMessage rm = this.revocationProxy.processRevocationMessage(
                revmsg, revParams);

        // Unwrap NRE from RevocationMessage
        JAXBElement<NonRevocationEvidence> jaxbNonRevocationEvidence = (JAXBElement<NonRevocationEvidence>) rm.getCryptoParams().getAny().get(0);
        return jaxbNonRevocationEvidence.getValue();
    }
}

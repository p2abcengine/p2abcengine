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

import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorEvent;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorHistory;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorSecretKey;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorState;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorWitness;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInLogEntry;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

public class AccumCryptoEngineRevAuthImpl implements CryptoEngineRevocation {

    private static final int EXPIRES_NUMBER_OF_DAYS_AFTER_NOW = 1;

    private final Logger logger = Logger.getLogger(CryptoEngineRevocation.class
            .getName());

    private static final String VERSION = "1.0";
    private final CredentialManager credentialManager;
    private final Set<URI> supportedMech = new HashSet<URI>();
    private final KeyManager keyManager;
    private final ContextGenerator contextGenerator;

    private final RevocationAuthorityStorage revAuthStorage;

    @Inject
    public AccumCryptoEngineRevAuthImpl(KeyManager keyManager,
            CredentialManager credentialManager,
            ContextGenerator contextGenerator,
            RevocationAuthorityStorage revocationAuthorityStorage) {
        this.keyManager = keyManager;
        this.credentialManager = credentialManager;
        this.contextGenerator = contextGenerator;
        this.revAuthStorage = revocationAuthorityStorage;
        // build supported list of crypto mechanisms
        this.supportedMech.add(CryptoUriUtil.getIdemixMechanism());
    }

    private com.ibm.zurich.idmx.utils.SystemParameters loadIdemixSystemParameters() {

        try {
            SystemParameters kmsp = this.keyManager.getSystemParameters();
            IdemixSystemParameters isp = new IdemixSystemParameters(kmsp);
            com.ibm.zurich.idmx.utils.SystemParameters sp = isp.getSystemParameters();
            com.ibm.zurich.idmx.utils.GroupParameters gp = isp.getGroupParameters();

            if (gp == null) {
                throw new RuntimeException("Group parameters are not correctly set up");
            }
            if (gp.getSystemParams() == null) {
                throw new RuntimeException("System parameters are not correctly set up");
            }

            return sp;

        } catch (KeyManagerException e) {
            throw new RuntimeException(e);
        }
    }

    public RevocationInformation revoke(URI revAuthParamsUid,
            List<Attribute> attributes) throws CryptoEngineException {
        // The attributes contain the revocation handles as IssuerAttribute
        // objects.
        if (attributes.size() != 1) {
            throw new IllegalArgumentException(
                    "Attributes does not contain the expected number of revocation handles which is 1");
        }

        Attribute revocationHandleAttribute = attributes.get(0);

        // Load system parameters.

        this.loadIdemixSystemParameters();

        // Retrieve public key.

        AccumulatorPublicKey publicKey = this.getPublicKey(revAuthParamsUid);

        StructureStore.getInstance().add(publicKey.getUri().toString(),
                publicKey);

        // Retrieve SecretKey.

        AccumulatorSecretKey accumSecretKey = this
                .getSecretKey(revAuthParamsUid);

        // Retrieve the state and history, create if empty.

        RevocationAuthorityState revAuthState = this
                .getRevocationAuthorityState(revAuthParamsUid);
        if (revAuthState == null) {
            AccumulatorHistory history = new AccumulatorHistory();
            AccumulatorState state = AccumulatorState
                    .getEmptyAccumulator(publicKey);

            revAuthState = new RevocationAuthorityState(0, history, state);
        }

        AccumulatorState state = revAuthState.getState();
        AccumulatorHistory history = revAuthState.getHistory();



        BigInteger prime = (BigInteger) revocationHandleAttribute
                .getAttributeValue();

        XMLGregorianCalendar now = null;
        AccumulatorEvent e = AccumulatorEvent.removePrime(state, prime, now,
                accumSecretKey);

        boolean check = true;
        state = AccumulatorState.applyEvent(state, e, check);

        history.addEvent(e);

        URI logEntryUid = this.contextGenerator.getUniqueContext(URI
                .create("urn:abc4trust:1.0:revocation:log:entry"));

        CryptoParams revLogEntryCryptoParams = new CryptoParams();
        XMLSerializer xmlSerializer = XMLSerializer.getInstance();
        revLogEntryCryptoParams.getAny().add(xmlSerializer.serialize(e));

        AttributeInLogEntry rHandleInLog = new AttributeInLogEntry();
        rHandleInLog.setAttributeType(revocationHandleAttribute.getAttributeDescription().getType());
        rHandleInLog.setAttributeValue(revocationHandleAttribute.getAttributeValue());

        RevocationLogEntry revLogEntry = new RevocationLogEntry();
        revLogEntry.setCryptoParameters(revLogEntryCryptoParams);
        revLogEntry.setDateCreated(AccumCryptoEngineRevAuthImpl.getNow());
        revLogEntry.getRevocableAttribute().add(rHandleInLog);
        revLogEntry.setRevocationLogEntryUID(logEntryUid);
        revLogEntry.setRevoked(true);
        try {
            this.credentialManager.addRevocationLogEntry(logEntryUid,
                    revLogEntry);
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        revAuthState = new RevocationAuthorityState(e.getNewEpoch(), history,
                state);
        this.storeRevocationAuthorityState(revAuthParamsUid, revAuthState);

        return this.updateRevocationInformation(revAuthParamsUid);
    }

    public RevocationInformation generateRevocationInformation(
            URI revAuthParamsUid) throws CryptoEngineException {
        AccumulatorPublicKey publicKey = this.getPublicKey(revAuthParamsUid);
        AccumulatorState state = AccumulatorState
                .getEmptyAccumulator(publicKey);

        AccumulatorHistory history = new AccumulatorHistory();

        try {
            RevocationAuthorityState revocationAuthorityState = new RevocationAuthorityState(
                    0, history, state);
            this.revAuthStorage.store(revAuthParamsUid,
                    revocationAuthorityState);
        } catch (Exception ex) {
            throw new CryptoEngineException(ex);
        }

        RevocationInformation revocationInformation = new RevocationInformation();
        revocationInformation.setVersion(VERSION);
        revocationInformation.setRevocationAuthorityParameters(revAuthParamsUid);
        revocationInformation.setInformationUID(this.getInformationUid());
        GregorianCalendar now = AccumCryptoEngineRevAuthImpl.getNow();
        GregorianCalendar expirationDate = AccumCryptoEngineRevAuthImpl
                .getExpirationDate();
        revocationInformation.setExpires(expirationDate);
        revocationInformation.setCreated(now);
        CryptoParams cryptoParams = new CryptoParams();
        XMLSerializer xmlSerializer = XMLSerializer.getInstance();
        cryptoParams.getAny().add(
                xmlSerializer.serializeAsElement(state));
        cryptoParams.getAny().add(xmlSerializer.serializeAsElement(history));
        revocationInformation.setCryptoParams(cryptoParams);

        try {
            this.keyManager.storeRevocationInformation(
                    revocationInformation.getInformationUID(),
                    revocationInformation);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        return revocationInformation;
    }

    private URI getInformationUid() {
        return this.contextGenerator.getUniqueContext(URI
                .create("urn:abc4trust:1.0:revocation:information"));
    }

    public static GregorianCalendar getExpirationDate() {
        GregorianCalendar expirationDate = getNow();
        expirationDate.add(Calendar.DATE, EXPIRES_NUMBER_OF_DAYS_AFTER_NOW);
        return expirationDate;
    }

    private AccumulatorPublicKey getPublicKey(URI revAuthParamsUid)
            throws CryptoEngineException {
        RevocationAuthorityParameters revParams = null;

        try {
            revParams = this.keyManager.getRevocationAuthorityParameters(revAuthParamsUid);
        } catch (KeyManagerException ex) {
            this.logger.warning("Could retrive revocation authority parameters (" + revAuthParamsUid.toString()
                    + ") from KeyManager");
            throw new CryptoEngineException(ex);
        }

        List<Object> any = revParams.getCryptoParams().getAny();
        Element publicKeyStr = (Element) any.get(0);
        Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

        AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;
        return publicKey;
    }

    @Override
    public RevocationInformation updateRevocationInformation(
            URI revAuthParamsUid) throws CryptoEngineException {

        // Retrieve the state and history, create if empty.

        RevocationAuthorityState revAuthState = this
                .getRevocationAuthorityState(revAuthParamsUid);

        AccumulatorState state = null;
        AccumulatorHistory history = null;

        // If the current state exists:
        if (revAuthState != null) {
            // then use it.
            state = revAuthState.getState();
            history = revAuthState.getHistory();
        } else {
            // else recreate it from scratch.
            RevocationInformation revInfo = this
                    .generateRevocationInformation(revAuthParamsUid);
            List<Object> cryptoEvidence = revInfo.getCryptoParams().getAny();
            Element object = (Element) cryptoEvidence.get(0);
            Parser xmlParser = Parser.getInstance();
            state = (AccumulatorState) xmlParser.parse(object);
            Element historyElement = (Element) cryptoEvidence.get(1);
            history = (AccumulatorHistory) xmlParser.parse(historyElement);
        }

        XMLSerializer xmlSerializer = XMLSerializer.getInstance();

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().add(xmlSerializer.serializeAsElement(state));
        cryptoParams.getAny().add(xmlSerializer.serializeAsElement(history));

        GregorianCalendar now = AccumCryptoEngineRevAuthImpl.getNow();

        RevocationInformation revocationInformation = new RevocationInformation();
        revocationInformation.setCreated(now);
        revocationInformation.setExpires(AccumCryptoEngineRevAuthImpl
                .getExpirationDate());
        revocationInformation.setCryptoParams(cryptoParams);
        revocationInformation.setInformationUID(this.getInformationUid());
        revocationInformation
        .setRevocationAuthorityParameters(revAuthParamsUid);
        revocationInformation.setVersion(VERSION);

        try {
            this.keyManager.storeRevocationInformation(
                    revocationInformation.getInformationUID(),
                    revocationInformation);
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
        return revocationInformation;
    }

    @Override
    public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(
            URI revAuthParamsUid, int epoch) throws CryptoEngineException {
        AccumulatorHistory events = this.getHistorySinceEpoch(revAuthParamsUid,
                epoch);

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().add(events);

        GregorianCalendar now = AccumCryptoEngineRevAuthImpl.getNow();
        GregorianCalendar expirationDate = AccumCryptoEngineRevAuthImpl
                .getExpirationDate();

        NonRevocationEvidenceUpdate nonRevocationEvidenceUpdate = new NonRevocationEvidenceUpdate();
        nonRevocationEvidenceUpdate.setCreated(now);
        nonRevocationEvidenceUpdate.setExpires(expirationDate);
        nonRevocationEvidenceUpdate.setCryptoParams(cryptoParams);
        nonRevocationEvidenceUpdate.setNonRevocationEvidenceUID(null);
        nonRevocationEvidenceUpdate
        .setRevocationAuthorityParametersUID(revAuthParamsUid);
        nonRevocationEvidenceUpdate.setNonRevocationEvidenceUpdateUID(this
                .getNonRevocationEvidenceUpdateUid());

        return nonRevocationEvidenceUpdate;
    }

    private URI getNonRevocationEvidenceUpdateUid() {
        return this.contextGenerator
                .getUniqueContext(URI
                        .create("urn:abc4trust:1.0:revocation:nonrevocationevidenceupdate"));
    }

    @SuppressWarnings("unused")
    private AccumulatorHistory getHistorySinceEpoch(URI revAuthParamsUid,
            int epoch) throws CryptoEngineException {
        // Retrieve the state and history, create if empty.

        RevocationAuthorityState revAuthState = this
                .getRevocationAuthorityState(revAuthParamsUid);

        AccumulatorState state = revAuthState.getState();
        AccumulatorHistory history = revAuthState.getHistory();

        AccumulatorHistory accumulatorHistory = new AccumulatorHistory();

        for (AccumulatorEvent event : history) {
            // if (state.get)
            // event.
        }

        return accumulatorHistory;
    }

    @Override
    public NonRevocationEvidence generateNonRevocationEvidence(
            URI revAuthParamsUid, List<Attribute> attributes)
                    throws CryptoEngineException {
        // The attributes contain the revocation handles as IssuerAttribute
        // objects.
        if (attributes.size() != 1) {
            throw new IllegalArgumentException(
                    "Attributes does not contain the expected number of revocation handles which is 1");
        }

        URI revocationHandleAttributeUid = attributes.get(0).getAttributeUID();
        Attribute revocationHandleAttribute = attributes.get(0);

        // Load system parameters.

        this.loadIdemixSystemParameters();

        // Retrieve public key.

        AccumulatorPublicKey publicKey = this.getPublicKey(revAuthParamsUid);

        StructureStore.getInstance().add(publicKey.getUri().toString(),
                publicKey);

        // Retrieve SecretKey.

        AccumulatorSecretKey accumSecretKey = this
                .getSecretKey(revAuthParamsUid);


        // Retrieve the state and history, create if empty.

        RevocationAuthorityState revAuthState = this
                .getRevocationAuthorityState(revAuthParamsUid);
        if (revAuthState == null) {
            AccumulatorHistory history = new AccumulatorHistory();
            AccumulatorState state = AccumulatorState
                    .getEmptyAccumulator(publicKey);

            revAuthState = new RevocationAuthorityState(0,
                    history, state);
            this.storeRevocationAuthorityState(revAuthParamsUid, revAuthState);
        }

        int epoch = revAuthState.getEpoch();
        AccumulatorState state = revAuthState.getState();
        BigInteger lastPrime = publicKey.getRandomPrime();

        // Compute non-revocation evidence (NRE).

        AccumulatorWitness w1 = AccumulatorWitness.calculateWitness(state,
                lastPrime, accumSecretKey);

        if (!w1.isConsistent()) {
            throw new CryptoEngineException("Witness is inconsistent.");
        }

        revocationHandleAttribute.setAttributeValue(w1.getValue());

        // Create serializer.

        XMLSerializer xmlSerializer = XMLSerializer.getInstance();


        // Create crypto params.

        CryptoParams cryptoParams = new CryptoParams();
        cryptoParams.getAny().add(xmlSerializer.serializeAsElement(w1));

        // Create NRE xml.

        GregorianCalendar expirationDate = AccumCryptoEngineRevAuthImpl
                .getExpirationDate();

        URI nonRevocationEvidenceUid = this.contextGenerator
                .getUniqueContext(URI
                        .create("urn:abc4trust:1.0:nonrevocation:evidence"));

        ObjectFactory of = new ObjectFactory();
        NonRevocationEvidence nre = of.createNonRevocationEvidence();
        nre.setCreated(AccumCryptoEngineRevAuthImpl.getNow());
        nre.setCredentialUID(URI.create("urn:abc4trust:1.0:tobesetbyuser"));
        nre.setCryptoParams(cryptoParams);
        nre.setExpires(expirationDate);
        nre.setNonRevocationEvidenceUID(nonRevocationEvidenceUid);
        nre.setRevocationAuthorityParametersUID(revAuthParamsUid);
        nre.getAttribute().addAll(attributes);
        nre.setEpoch(epoch);

        // Save NRE in the ABCE-layer history in the credential manager.

        try {
            this.credentialManager.storeNonRevocationEvidence(nre);
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        // Save RevocationLogEntry in the ABCE-layer history in the credential
        // manager.

        try {
            URI revocationLogEntryUid = this.contextGenerator
                    .getUniqueContext(URI
                            .create("urn:abc4trust:1.0:revocation:log:entry"));

            CryptoParams revLogEntryCryptoParams = new CryptoParams();

            AttributeInLogEntry rHandleInLog = new AttributeInLogEntry();
            rHandleInLog.setAttributeType(revocationHandleAttribute.getAttributeDescription().getType());
            rHandleInLog.setAttributeValue(revocationHandleAttribute.getAttributeValue());


            RevocationLogEntry revLogEntry = new RevocationLogEntry();
            revLogEntry.setCryptoParameters(revLogEntryCryptoParams);
            revLogEntry.setDateCreated(AccumCryptoEngineRevAuthImpl.getNow());
            revLogEntry.getRevocableAttribute().add(rHandleInLog);
            revLogEntry.setRevocationLogEntryUID(revocationLogEntryUid);
            revLogEntry.setRevoked(false);
            this.credentialManager.addRevocationLogEntry(
                    revocationHandleAttributeUid,
                    revLogEntry);
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        return nre;
    }

    private void storeRevocationAuthorityState(URI revAuthParamsUid,
            RevocationAuthorityState revAuthState) throws CryptoEngineException {
        try {
            this.revAuthStorage.store(revAuthParamsUid, revAuthState);
        } catch (Exception ex) {
            throw new CryptoEngineException(ex);
        }
    }

    public static GregorianCalendar getNow() {
        return new GregorianCalendar();
    }

    private RevocationAuthorityState getRevocationAuthorityState(
            URI revAuthParamsUid) {
        RevocationAuthorityState revAuthState = null;
        try {
            revAuthState = this.revAuthStorage.get(revAuthParamsUid);
        } catch (Exception ex) {
            this.logger.warning("Could get state ("
                    + revAuthParamsUid.toString() + ") in storage");
            return null;
        }
        return revAuthState;
    }

    private AccumulatorSecretKey getSecretKey(URI revAuthParamsUid)
            throws CryptoEngineException {
        SecretKey secretKey = null;
        try {
            secretKey = this.credentialManager.getSecretKey(revAuthParamsUid);
        } catch (Exception ex) {
            this.logger.warning("Could not store key ("
                    + revAuthParamsUid.toString() + ") in CredentialManager");
            throw new CryptoEngineException(ex);
        }

        String secretKeyStr = (String)
                secretKey.getCryptoParams().getAny().get(0);
        AccumulatorSecretKey accumSecretKey = (AccumulatorSecretKey) Parser
                .getInstance().parse(secretKeyStr);
        return accumSecretKey;
    }

    @Override
    public RevocationAuthorityParameters setupRevocationAuthorityParameters(
            int keyLength,
            URI cryptographicMechanism,
            URI revAuthParamsUid,
            Reference revocationInfoReference,
            Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference) throws CryptoEngineException {

        // Load system parameters.

        this.loadIdemixSystemParameters();

        // Generate secret key.

        URI systemParametersUri = URI.create(IdemixConstants.systemParameterId);

        AccumulatorSecretKey secretKey =
                AccumulatorSecretKey
                .generatePrivateKey(systemParametersUri, revAuthParamsUid);

        // Generate public key.

        AccumulatorPublicKey publicKey = secretKey.getPublicKey();


        // Create output.

        ObjectFactory of = new ObjectFactory();

        SecretKey revAuthSecretKey = of.createSecretKey();
        revAuthSecretKey.setSecretKeyUID(revAuthParamsUid);

        CryptoParams secretKeyCryptoParams = of.createCryptoParams();
        revAuthSecretKey.setCryptoParams(secretKeyCryptoParams);
        XMLSerializer xmlSerializer = XMLSerializer.getInstance();
        secretKeyCryptoParams.getAny().add(
                xmlSerializer.serialize(secretKey));

        CryptoParams cryptoParams = of.createCryptoParams();
        cryptoParams.getAny().add(xmlSerializer.serializeAsElement(publicKey));

        RevocationAuthorityParameters revAuthParams = of
                .createRevocationAuthorityParameters();
        revAuthParams.setCryptoParams(cryptoParams);
        revAuthParams
        .setNonRevocationEvidenceReference(nonRevocationEvidenceReference);
        revAuthParams
        .setNonRevocationEvidenceUpdateReference(nonRevocationUpdateReference);
        revAuthParams.setParametersUID(revAuthParamsUid);
        revAuthParams.setRevocationInfoReference(revocationInfoReference);
        revAuthParams.setRevocationMechanism(cryptographicMechanism);
        revAuthParams.setVersion(VERSION);

        // Store secret key in the credentialManager.

        try {
            this.credentialManager.storeSecretKey(
                    revAuthSecretKey.getSecretKeyUID(), revAuthSecretKey);
        } catch (Exception ex) {
            this.logger.warning("Could not store key ("
                    + revAuthParamsUid.toString()
                    + ") in CredentialManager");
            throw new CryptoEngineException(ex);
        }


        // Store public part in keyManager.

        try {
            this.keyManager.storeRevocationAuthorityParameters(
                    revAuthParamsUid,
                    revAuthParams);
        } catch (Exception ex) {
            this.logger
            .warning("Could not store revocation authority [arameters ("
                    + revAuthParamsUid.toString()
                    + ") in KeyManager");
            throw new CryptoEngineException(ex);
        }
        return revAuthParams;
    }

    private InputSource getResource(String filename) {
        return new InputSource(this.getClass().getResourceAsStream(
                "/eu/abc4trust/sampleXml/idemix/" + filename));
    }

    @Override
    public RevocationInformation getRevocationInformation(URI revParamsUid,
            URI revInfoUid) throws CryptoEngineException {
        try {
            RevocationInformation revInfo = this.keyManager
                    .getRevocationInformation(revParamsUid, revInfoUid);
            return revInfo;
        } catch (KeyManagerException ex) {
            throw new CryptoEngineException(ex);
        }
    }

}

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

package eu.abc4trust.keyManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.cryptoEngine.revauth.AccumCryptoEngineRevAuthImpl;
import eu.abc4trust.revocationProxy.RevocationMessageType;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.SystemParameters;

public class KeyManagerImpl implements KeyManager {

    private final Logger log = Logger.getLogger(KeyManagerImpl.class.getName());

    private static final URI UPROVE_TOKENS_UID = URI.create("abc4trust:uprove_keys_and_tokens_uid");
    public static final String CURRENT_REVOCATION_UID_STR = "abc4trust:current_revocation_uid";
    private static final URI SYSTEM_PARAMETERS_UID = URI
            .create("abc4trust:system_parameters_uid");
    private final PersistenceStrategy persistensStrategy;

    private final RevocationProxy revocationProxy;

    @Inject
    public KeyManagerImpl(
            PersistenceStrategy persistensStrategy,
            RevocationProxy revocationProxy) {
        this.persistensStrategy = persistensStrategy;
        this.revocationProxy = revocationProxy;
    }

    @Override
    public RevocationInformation getCurrentRevocationInformation(URI rapuid)
            throws KeyManagerException {
        RevocationInformation revocationInformation = this
                .loadCurrentRevocationInformation();

        // If revocation information is not expired.
        if ((revocationInformation != null)
                && (revocationInformation.getExpires().compareTo(
                        AccumCryptoEngineRevAuthImpl.getNow()) > 0)) {
            return revocationInformation;
        }

        if (rapuid == null) {
            throw new KeyManagerException(
                    "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
        }

        return this.getLatestRevocationInformation(rapuid);
    }

    @Override
    public RevocationInformation getLatestRevocationInformation(URI rapuid)
            throws KeyManagerException {
        try {
            // Revocation information is either non-existing or expired.

            if (rapuid == null) {
                throw new KeyManagerException(
                        "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
            }

            RevocationAuthorityParameters revocationAuthorityParameters = this
                    .getRevocationAuthorityParameters(rapuid);
            if (revocationAuthorityParameters == null) {
                throw new KeyManagerException(
                        "Unkown revocation authority parameters : " + rapuid);
            }

            RevocationInformation revocationInformation = this
                    .getCurrentRevocationInformationFromRevocationAuthority(revocationAuthorityParameters);

            try {
                this.storeRevocationInformation(
                        revocationInformation.getInformationUID(),
                        revocationInformation);
            } catch (KeyManagerException ex) {
                return null;
            }

            try {
                this.storeRevocationInformation(new URI(
                        CURRENT_REVOCATION_UID_STR), revocationInformation);
            } catch (KeyManagerException ex) {
                return null;
            }

            return revocationInformation;
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    private RevocationInformation getCurrentRevocationInformationFromRevocationAuthority(
            RevocationAuthorityParameters revParams) throws KeyManagerException {
        try {
            this.log.info("getCurrentRevocationInformationFromRevocationAuthority");
            this.log.info("RevocationAuthorityParametersUID: "
                    + revParams.getParametersUID());
            if (revParams.getRevocationInfoReference() != null) {
                if (revParams.getRevocationInfoReference().getReferences() != null) {
                    if (!revParams.getRevocationInfoReference().getReferences()
                            .isEmpty()) {
                        this.log.info("RevocationInfoReference: "
                                + revParams.getRevocationInfoReference().getReferences()
                                .get(0));
                    }
                }
            }
            if (revParams.getNonRevocationEvidenceReference() != null) {
                if (revParams.getNonRevocationEvidenceReference()
                        .getReferences() != null) {
                    if (!revParams.getNonRevocationEvidenceReference()
                            .getReferences().isEmpty()) {
                        this.log.info("NonRevocationEvidenceReference: "
                                + revParams.getNonRevocationEvidenceReference()
                                .getReferences().get(0));
                    }
                }
            }
            if (revParams.getNonRevocationEvidenceUpdateReference() != null) {
                if (revParams.getNonRevocationEvidenceUpdateReference()
                        .getReferences() != null) {
                    if (!revParams.getNonRevocationEvidenceUpdateReference()
                            .getReferences().isEmpty()) {
                        this.log.info("NonRevocationEvidenceUpdateReference: "
                                + revParams.getNonRevocationEvidenceUpdateReference()
                                .getReferences().get(0));
                    }
                }
            }

            // Wrap the request into the Revocation message.
            RevocationMessage revmsg = new RevocationMessage();
            revmsg.setContext(URI.create("NO-CONTEXT"));
            revmsg.setRevocationAuthorityParametersUID(revParams
                    .getParametersUID());
            revmsg.setCryptoParams(new CryptoParams());
            revmsg.getCryptoParams().getAny()
            .add(RevocationUtility.serializeRevocationMessageType(RevocationMessageType.GET_CURRENT_REVOCATION_INFORMATION));

            this.log.info("Type of RevocationProxy: "
                    + this.revocationProxy.getClass());
            RevocationMessage rm = this.revocationProxy
                    .processRevocationMessage(revmsg, revParams);
            // Unwrap RevInfo from RevocationMessage.
            JAXBElement<RevocationInformation> jaxbRI = (JAXBElement<RevocationInformation>) rm.getCryptoParams().getAny().get(0);
            return jaxbRI.getValue();
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    private RevocationInformation loadCurrentRevocationInformation()
            throws KeyManagerException {
        URI uri;
        try {
            uri = new URI(KeyManagerImpl.CURRENT_REVOCATION_UID_STR);
            return (RevocationInformation) this.persistensStrategy
                    .loadObject(uri);
        } catch (URISyntaxException ex) {
            throw new KeyManagerException(ex);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public InspectorPublicKey getInspectorPublicKey(URI ipkuid)
            throws KeyManagerException {
        try {
            return (InspectorPublicKey) this.persistensStrategy
                    .loadObject(ipkuid);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean storeInspectorPublicKey(URI ipkuid,
            InspectorPublicKey inspectorPublicKey) throws KeyManagerException {
        try {
            return this.persistensStrategy.writeObjectAndOverwrite(ipkuid,
                    inspectorPublicKey);
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public IssuerParameters getIssuerParameters(URI issuid)
            throws KeyManagerException {
        try {
            return (IssuerParameters) this.persistensStrategy
                    .loadObject(issuid);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean storeIssuerParameters(URI issuid,
            IssuerParameters issuerParameters) throws KeyManagerException {
        try {
            return this.persistensStrategy.writeObjectAndOverwrite(issuid,
                    issuerParameters);
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public RevocationAuthorityParameters getRevocationAuthorityParameters(
            URI rapuid) throws KeyManagerException {
        try {
            return (RevocationAuthorityParameters) this.persistensStrategy
                    .loadObject(rapuid);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean storeRevocationAuthorityParameters(URI rapuid,
            RevocationAuthorityParameters revocationAuthorityParameters)
                    throws KeyManagerException {
        try {
            return this.persistensStrategy.writeObjectAndOverwrite(rapuid,
                    revocationAuthorityParameters);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public RevocationInformation getRevocationInformation(URI rapuid,
            URI revinfouid) throws KeyManagerException {
        // TODO(jdn): fix this method.
        try {
            RevocationInformation revocationInformation = (RevocationInformation) this.persistensStrategy
                    .loadObject(revinfouid);
            if (revocationInformation == null) {

                RevocationAuthorityParameters revocationAuthorityParameters = this
                        .getRevocationAuthorityParameters(rapuid);

                if (revocationAuthorityParameters == null) {
                    throw new KeyManagerException(
                            "Unkown revocation authority parameters");
                }

                revocationInformation = this
                        .getRevocationInformationFromRevocationAuthority(
                                revocationAuthorityParameters, revinfouid);

                if (revocationInformation == null) {
                    return null;
                }

                try {
                    this.storeRevocationInformation(
                            revocationInformation.getInformationUID(),
                            revocationInformation);
                } catch (KeyManagerException ex) {
                    return null;
                }
            }
            return revocationInformation;
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    private RevocationInformation getRevocationInformationFromRevocationAuthority(
            RevocationAuthorityParameters revParams, URI revinfouid)
                    throws Exception {
        try {
            // Wrap the request into the Revocation message.
            RevocationMessage revmsg = new RevocationMessage();
            revmsg.setContext(URI.create("NO-CONTEXT"));
            revmsg.setRevocationAuthorityParametersUID(revParams
                    .getParametersUID());
            revmsg.setCryptoParams(new CryptoParams());
            revmsg.getCryptoParams().getAny()
            .add(RevocationUtility.serializeRevocationMessageType(RevocationMessageType.REQUEST_REVOCATION_INFORMATION));

            revmsg.getCryptoParams().getAny().add(RevocationUtility.serializeRevocationInfoUid(revinfouid));

            RevocationMessage rm = this.revocationProxy
                    .processRevocationMessage(revmsg, revParams);

            // Unwrap RevInfo from RevocationMessage.
            JAXBElement<RevocationInformation> ri = (JAXBElement<RevocationInformation>) rm.getCryptoParams().getAny().get(0);
            return ri.getValue();
        } catch (Exception ex) {
            throw new KeyManagerException(ex);
        }
    }

    public void storeRevocationInformation(URI riuid,
            RevocationInformation revocationInformation)
                    throws KeyManagerException {
        try {
            boolean r = this.persistensStrategy.writeObjectAndOverwrite(riuid,
                    revocationInformation);
            if (!r) {
                throw new KeyManagerException(
                        "Something went wrong in the persistense.");
            }
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean storeCredentialSpecification(URI uid,
            CredentialSpecification credentialSpecification)
                    throws KeyManagerException {
        try {
            return this.persistensStrategy.writeObjectAndOverwrite(uid,
                    credentialSpecification);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public CredentialSpecification getCredentialSpecification(URI credspec)
            throws KeyManagerException {
        try {
            return (CredentialSpecification) this.persistensStrategy
                    .loadObject(credspec);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean storeSystemParameters(SystemParameters systemParameters)
            throws KeyManagerException {
        try {
            return this.persistensStrategy.writeObjectAndOverwrite(
                    SYSTEM_PARAMETERS_UID,
                    systemParameters);
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public SystemParameters getSystemParameters() throws KeyManagerException {
        try {
            SystemParameters sysParams = (SystemParameters) this.persistensStrategy
                    .loadObject(SYSTEM_PARAMETERS_UID);
            if (sysParams == null) {
                throw new KeyManagerException(
                        "Could not find the system paramters in the key manager");
            }
            return sysParams;
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public boolean hasSystemParameters() throws KeyManagerException {
        try {

            SystemParameters sysParams = (SystemParameters) this.persistensStrategy
                    .loadObject(SYSTEM_PARAMETERS_UID);
            return sysParams != null;
        } catch (PersistenceException ex) {
            throw new KeyManagerException(ex);
        }
    }

    @Override
    public ArrayList<?> getCredentialTokens(URI uid){
        try {
            URI uri = URI.create(uid.toString()+UPROVE_TOKENS_UID);
            return (ArrayList<?>)this.persistensStrategy.loadObject(uri);
        } catch (PersistenceException e) {
            return null;
        }
    }

    @Override
    public void storeCredentialTokens(URI uid, ArrayList<?> tokens)
            throws KeyManagerException {
        try {
            URI uri = URI.create(uid.toString()+UPROVE_TOKENS_UID);
            this.persistensStrategy.writeObjectAndOverwrite(uri, tokens);
        } catch (PersistenceException e) {
            throw new KeyManagerException(e);
        }
    }
}

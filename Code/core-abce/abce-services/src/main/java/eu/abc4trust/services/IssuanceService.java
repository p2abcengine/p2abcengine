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

package eu.abc4trust.services;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.IssuerParametersInput;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;

@Path("/issuer")
public class IssuanceService {

    private static final URI CRYPTOMECHANISM_URI_IDEMIX = URI
            .create("urn:abc4trust:1.0:algorithm:idemix");

    private static final URI CRYPTOMECHANISM_URI_UPROVE = URI
            .create("urn:abc4trust:1.0:algorithm:uprove");

    private static final URI CRYPTOMECHANISM_URI_BRIDGED = URI
            .create("urn:abc4trust:1.0:algorithm:bridged");

    private static final URI CRYPTOMECHANISM_URI_BRIDGED_WITH_IDEMIX_ISSUER = URI
            .create("urn:abc4trust:1.0:algorithm:bridged:idemix:issuer");

    private static final URI CRYPTOMECHANISM_URI_BRIDGED_WITH_UPROVE_ISSUER = URI
            .create("urn:abc4trust:1.0:algorithm:bridged:uprove:issuer");

    private final ObjectFactory of = new ObjectFactory();

    private final Logger log = Logger
            .getLogger(IssuanceService.class.getName());
    private final String issuerParamsPrefix = "issuer_resources/";
    private final String fileStoragePrefix = "issuer_storage/";

    public IssuanceService() {
    }

    private void initializeHelper(CryptoEngine cryptoEngine) {
        this.log.info("IssuanceService loading...");

        try {
            if (IssuanceHelper.isInit()) {
                this.log.info("IssuanceHelper is initialized");
                IssuanceHelper.verifyFiles(false, this.fileStoragePrefix,
                        cryptoEngine);
            } else {

                this.log.info("Initializing IssuanceHelper...");

                UProveIntegration uproveIntegration = new UProveIntegration();
                uproveIntegration.verify();

                IssuanceHelper.initInstanceForService(cryptoEngine,
                        this.issuerParamsPrefix, this.fileStoragePrefix);

                this.log.info("IssuanceHelper is initialized");
            }
        } catch (Exception e) {
            System.out.println("Create Domain FAILED " + e);
            e.printStackTrace();
        }
    }

    /**
     * This method generates a fresh set of system parameters for the given
     * security level, expressed as the bitlength of a symmetric key with
     * comparable security, and cryptographic mechanism. Issuers can generate
     * their own system parameters, but can also reuse system parameters
     * generated by a different entity. More typically, a central party (e.g., a
     * standardization body) will generate and publish system parameters for a
     * number of different key lengths that will be used by many Issuers.
     * Security levels 80 and 128 MUST be supported; other values MAY also be
     * supported.
     * 
     * Currently, the supported mechanism URIs are
     * urn:abc4trust:1.0:algorithm:idemix for Identity Mixer and
     * urn:abc4trust:1.0:algorithm:uprove for U-Prove.
     * 
     * This method will overwrite any existing system parameters.
     * 
     * @param securityLevel
     * @param cryptoMechanism
     * @return
     * @throws Exception
     */
    @POST()
    @Path("/setupSystemParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<SystemParameters> setupSystemParameters(
            @QueryParam("securityLevel") int securityLevel,
            @QueryParam("cryptoMechanism") URI cryptoMechanism)
                    throws Exception {

        this.log.info("IssuanceService - setupSystemParameters "
                + securityLevel + ", " + cryptoMechanism);

        CryptoEngine cryptoEngine = this.parseCryptoMechanism(cryptoMechanism);

        this.initializeHelper(cryptoEngine);

        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();

        int idemixKeylength = this.parseIdemixSecurityLevel(securityLevel);

        int uproveKeylength = this.parseUProveSecurityLevel(securityLevel);

        SystemParameters systemParameters = issuanceHelper
                .createNewSystemParametersWithIdemixSpecificKeylength(
                        idemixKeylength, uproveKeylength);

        SystemParameters serializeSp = SystemParametersUtil
                .serialize(systemParameters);

        return this.of.createSystemParameters(serializeSp);
    }

    private int parseIdemixSecurityLevel(int securityLevel) {
        if (securityLevel == 80) {
            return 1024;
        }
        return com.ibm.zurich.idmx.utils.SystemParameters
                .equivalentRsaLength(securityLevel);
    }

    private int parseUProveSecurityLevel(int securityLevel) {
        switch (securityLevel) {
        case 80:
            return 2048;
        case 128:
            return 3072;
        }
        throw new RuntimeException("Unsupported securitylevel: \""
                + securityLevel + "\"");
    }

    private CryptoEngine parseCryptoMechanism(URI cryptoMechanism) {
        if (cryptoMechanism == null) {
            throw new RuntimeException("No cryptographic mechanism specified");
        }
        if (cryptoMechanism.equals(CRYPTOMECHANISM_URI_IDEMIX)) {
            return CryptoEngine.IDEMIX;
        }
        if (cryptoMechanism.equals(CRYPTOMECHANISM_URI_UPROVE)) {
            return CryptoEngine.UPROVE;
        }
        if (cryptoMechanism.equals(CRYPTOMECHANISM_URI_BRIDGED_WITH_IDEMIX_ISSUER)) {
            return CryptoEngine.BRIDGED_WITH_IDEMIX_ISSUER;
        }
        if (cryptoMechanism.equals(CRYPTOMECHANISM_URI_BRIDGED_WITH_UPROVE_ISSUER)) {
            return CryptoEngine.BRIDGED_WITH_UPROVE_ISSUER;
        }
        if (cryptoMechanism.equals(CRYPTOMECHANISM_URI_BRIDGED)) {
            return CryptoEngine.BRIDGED;
        }
        throw new IllegalArgumentException("Unkown crypto mechanism: \""
                + cryptoMechanism + "\"");
    }

    // H2.1 Update(jdn): added crypto engine.
    /**
     * This method generates a fresh issuance key and the corresponding Issuer
     * parameters. The issuance key is stored in the Issuer’s key store, the
     * Issuer parameters are returned as output of the method. The input to this
     * method specify the credential specification credspec of the credentials
     * that will be issued with these parameters, the system parameters syspars,
     * the unique identifier uid of the generated parameters, the hash algorithm
     * identifier hash, and, optionally, the parameters identifier for any
     * Issuer-driven Revocation Authority.
     * 
     * Currently, the only supported hash algorithm is SHA-256 with identifier
     * urn:abc4trust:1.0:hashalgorithm:sha-256.
     * 
     * @return
     * @throws Exception
     */
    /*
     * curl --header "Content-Type:application/xml" -X POST -d @credSpecAndSysParams.xml http://localhost:9500/abce-services/issuer/setupIssuerParameters/?cryptoEngine=IDEMIX\&issuerParametersUid=urn%3A%2F%2Ftest%2Ffoobar\&hash=urn:abc4trust:1.0:hashalgorithm:sha-256
     */
    @POST()
    @Path("/setupIssuerParameters/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuerParameters> setupIssuerParameters(
            IssuerParametersInput issuerParametersInput)
                    throws Exception {

        this.log.info("IssuanceService - setupIssuerParameters ");

        CryptoEngine cryptoEngine = this
                .parseCryptoMechanism(issuerParametersInput.getAlgorithmID());

        this.initializeHelper(cryptoEngine);

        this.validateInput(issuerParametersInput);
        URI hashAlgorithm = issuerParametersInput.getHashAlgorithm();

        String systemAndIssuerParamsPrefix = Constants.ISSUER_RESOURCES_FOLDER
                + "/";

        IssuanceHelper instance = IssuanceHelper.getInstance();

        KeyManager keyManager = instance.keyManager;
        SystemParameters systemParameters = keyManager.getSystemParameters();

        URI credentialSpecUid = issuerParametersInput.getCredentialSpecUID();
        CredentialSpecification credspec = keyManager
                .getCredentialSpecification(credentialSpecUid);

        if (credspec == null) {
            throw new IllegalStateException(
                    "Could not find credential specification \""
                            + credentialSpecUid + "\"");
        }

        URI issuerParametersUid = issuerParametersInput.getParametersUID();
        URI hash = hashAlgorithm;
        URI revocationParametersUid = issuerParametersInput
                .getRevocationParametersUID();
        List<FriendlyDescription> friendlyDescriptions = issuerParametersInput
                .getFriendlyIssuerDescription();
        System.out.println("FriendlyIssuerDescription: "
                + friendlyDescriptions.size());
        IssuerParameters issuerParameters = instance.setupIssuerParameters(
                cryptoEngine, credspec, systemParameters,
                issuerParametersUid, hash, revocationParametersUid,
                systemAndIssuerParamsPrefix, friendlyDescriptions);

        this.log.info("IssuanceService - issuerParameters generated");

        SystemParameters serializeSp = SystemParametersUtil
                .serialize(systemParameters);

        issuerParameters.setSystemParameters(serializeSp);
        return this.of.createIssuerParameters(issuerParameters);
    }

    private void validateInput(IssuerParametersInput issuerParametersTemplate) {
        if (issuerParametersTemplate == null) {
            throw new IllegalArgumentException(
                    "issuer paramters input is required");
        }

        if (issuerParametersTemplate.getCredentialSpecUID() == null) {
            throw new IllegalArgumentException(
                    "Credential specifation UID is required");
        }

        if (issuerParametersTemplate.getParametersUID() == null) {
            throw new IllegalArgumentException(
                    "Issuer parameters UID is required");
        }

        if (issuerParametersTemplate.getAlgorithmID() == null) {
            throw new IllegalArgumentException(
                    "Crypto Algorithm ID is required");
        }

        if (issuerParametersTemplate.getHashAlgorithm() == null) {
            throw new IllegalArgumentException("Hash algorithm is required");
        }

        if (!issuerParametersTemplate.getHashAlgorithm().equals(
                CryptoUriUtil.getHashSha256())) {
            throw new IllegalArgumentException("Unknown hashing algorithm");
        }

    }

    /**
     * This method is invoked by the Issuer to initiate an issuance protocol
     * based on the given issuance policy ip and the list of attribute
     * type-value pairs atts to be embedded in the new credential. It returns an
     * IssuanceMessage that is to be sent to the User and fed to the
     * issuanceProtocolStep method on the User’s side. The IssuanceMessage
     * contains a Context attribute that will be the same for all message
     * exchanges in this issuance protocol, to facilitate linking the different
     * flows of the protocol.
     * 
     * In case of an issuance “from scratch”, i.e., for which the User does not
     * have to prove ownership of existing credentials or established
     * pseudonyms, the given issuance policy ip merely specifies the credential
     * specification and the issuer parameters for the credential to be issued.
     * In this case, the returned issuance message is the first message in the
     * actual cryptographic issuance protocol.
     * 
     * In case of an “advanced” issuance, i.e., where the User has to prove
     * ownership of existing credentials or pseudonyms to carry over attributes,
     * a user secret, or a device secret, the returned IssuanceMessage is simply
     * a wrapper around the issuance policy ip with a fresh Context attribute.
     * The returned boolean indicates whether this is the last flow of the
     * issuance protocol. If the IssuanceMessage is not the final one, the
     * Issuer will subsequently invoke its issuanceProtocolStep method on the
     * next incoming IssuanceMessage from the User. The issuer also returns the
     * uid of the stored issuance log entry that contains an issuance token
     * together with the attribute values provided by the issuer to keep track
     * of the issued credentials.
     */
    @POST()
    @Path("/initIssuanceProtocol/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessageAndBoolean> initIssuanceProtocol(
            IssuancePolicyAndAttributes issuancePolicyAndAttributes)
                    throws Exception {

        this.log.info("IssuanceService - initIssuanceProtocol ");

        IssuancePolicy ip = issuancePolicyAndAttributes.getIssuancePolicy();
        List<Attribute> attributes = issuancePolicyAndAttributes.getAttribute();

        URI issuerParametersUid = ip.getCredentialTemplate()
                .getIssuerParametersUID();

        CryptoEngine cryptoEngine = this.getCryptoEngine(issuerParametersUid);

        this.initializeHelper(cryptoEngine);

        this.initIssuanceProtocolValidateInput(issuancePolicyAndAttributes);

        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();

        this.loadCredentialSpecifications();

        this.loadIssuerParameters();

        IssuanceMessageAndBoolean issuanceMessageAndBoolean = issuanceHelper.initIssuanceProtocol(ip, attributes);

        return this.of
                .createIssuanceMessageAndBoolean(issuanceMessageAndBoolean);

    }

    private CryptoEngine getCryptoEngine(URI issuerParametersUid) {
        if (issuerParametersUid.toString().endsWith("idemix")) {
            return CryptoEngine.IDEMIX;
        }

        if (issuerParametersUid.toString().endsWith("uprove")) {
            return CryptoEngine.UPROVE;
        }
        throw new IllegalArgumentException(
                "Unkown crypto engine from issuer parameters uid: \""
                        + issuerParametersUid + "\"");
    }

    private void loadCredentialSpecifications()
            throws FileNotFoundException {
        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();
        ServiceResourceStorage serviceResourceStorage = ServiceResourceStorage
                .getInstance();
        String[] credSpecResourceList = serviceResourceStorage
                .loadCredentialSpecificationResourceList();
        issuanceHelper.addCredentialSpecifications(credSpecResourceList);
    }

    private void loadIssuerParameters() throws FileNotFoundException {
        IssuanceHelper issuanceHelper = IssuanceHelper.getInstance();
        ServiceResourceStorage serviceResourceStorage = ServiceResourceStorage
                .getInstance();
        String[] issuerParametersResourceList = serviceResourceStorage
                .loadIssuerParametersResourceList();
        issuanceHelper.addIssuerParameters(issuerParametersResourceList);
    }

    private void initIssuanceProtocolValidateInput(
            IssuancePolicyAndAttributes issuancePolicyAndAttributes) {
        if (issuancePolicyAndAttributes == null) {
            throw new IllegalArgumentException(
                    "\"issuancePolicyAndAttributes\" is required.");
        }

        if (issuancePolicyAndAttributes.getIssuancePolicy() == null) {
            throw new IllegalArgumentException(
                    "\"Issuance policy\" is required.");
        }

        if (issuancePolicyAndAttributes.getAttribute() == null) {
            throw new IllegalArgumentException("\"Attributes\" are required.");
        }
    }

    /**
     * This method performs one step in an interactive issuance protocol. On
     * input an incoming issuance message m received from the User, it returns
     * the outgoing issuance message that is to be sent back to the User, a
     * boolean indicating whether this is the last message in the protocol, and
     * the uid of the stored issuance log entry that contains an issuance token
     * together with the attribute values provided by the issuer to keep track
     * of the issued credentials. The Context attribute of the outgoing message
     * has the same value as that of the incoming message, allowing the Issuer
     * to link the different messages of this issuance protocol.
     */
    @POST()
    @Path("/issuanceProtocolStep")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessageAndBoolean> issuanceProtocolStep(
            final IssuanceMessage issuanceMessage) {

        this.log.info("IssuanceService - step - context : "
                + issuanceMessage.getContext());

        CryptoEngine engine = this.getCryptoEngine(issuanceMessage);

        this.initializeHelper(engine);

        IssuanceMessageAndBoolean response;
        try {
            response = IssuanceHelper.getInstance().issueStep(engine, issuanceMessage);
        } catch (Exception e) {
            this.log.info("- got Exception from IssuaceHelper/ABCE Engine - processing IssuanceMessage from user...");
            e.printStackTrace();
            throw new IllegalStateException("Failed to proces IssuanceMessage from user");
        }

        IssuanceMessage issuanceMessageFromResponce = response
                .getIssuanceMessage();
        if (response.isLastMessage()) {
            this.log.info(" - last message for context : "
                    + issuanceMessageFromResponce.getContext());
        } else {
            this.log.info(" - more steps context : "
                    + issuanceMessageFromResponce.getContext());
        }

        return this.of.createIssuanceMessageAndBoolean(response);
    }

    private CryptoEngine getCryptoEngine(final IssuanceMessage issuanceMessage) {
        CryptoEngine engine = CryptoEngine.IDEMIX;

        if (issuanceMessage.getAny().get(0) instanceof JAXBElement) {
            engine = CryptoEngine.IDEMIX;
        }
        if (issuanceMessage.getAny().get(0) instanceof Element) {
            engine = CryptoEngine.UPROVE;
        }
        return engine;
    }

    /**
     * This method looks up an issuance log entry of previously issued
     * credentials that contains a verified issuance token together with the
     * attribute values provided by the issuer. The issuance log entry
     * identifier issuanceEntryUid is the identifier that was included in the
     * issuance token description that was returned when the token was verified.
     */
    @GET()
    @Path("/getIssuanceLogEntry/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceLogEntry> getIssuanceLogEntry(
            @QueryParam("issuanceEntryUid") URI issuanceEntryUid)
                    throws Exception {

        this.log.info("IssuanceService - getIssuanceLogEntry: \""
                + issuanceEntryUid + "\"");

        CryptoEngine engine = CryptoEngine.IDEMIX;// this.getCryptoEngine(issuanceEntryUid);

        this.initializeHelper(engine);

        IssuanceLogEntry response;
        try {
            response = IssuanceHelper.getInstance().getIssuanceLogEntry(engine,
                    issuanceEntryUid);
        } catch (Exception e) {
            this.log.info("- got Exception from IssuaceHelper/ABCE Engine - processing IssuanceLogEntry from user...");
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed to proces IssuanceLogEntry from user");
        }

        return this.of.createIssuanceLogEntry(response);
    }

    @PUT()
    @Path("/storeCredentialSpecification/{credentialSpecifationUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeCredentialSpecification(
            @PathParam("credentialSpecifationUid") URI credentialSpecifationUid,
            CredentialSpecification credSpec) {
        this.log.info("IssuanceService - storeCredentialSpecification: \""
                + credentialSpecifationUid + "\"");

        try {
            CryptoEngine engine = CryptoEngine.IDEMIX;
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(IssuanceHelper.getFileStoragePrefix(
                            this.fileStoragePrefix, engine));

            boolean r1 = keyManager.storeCredentialSpecification(
                    credentialSpecifationUid, credSpec);

            engine = CryptoEngine.UPROVE;
            keyManager = UserStorageManager.getKeyManager(IssuanceHelper
                    .getFileStoragePrefix(this.fileStoragePrefix, engine));

            boolean r2 = keyManager.storeCredentialSpecification(
                    credentialSpecifationUid, credSpec);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r1 && r2);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT()
    @Path("/storeIssuerParameters/{issuerParametersUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeIssuerParameters(
            @PathParam("issuerParametersUid") URI issuerParametersUid,
            IssuerParameters issuerParameters) {
        this.log.info("IssuanceService - storeIssuerParameters ");

        this.log.info("IssuanceService - storeIssuerParameters - issuerParametersUid: "
                + issuerParametersUid
                + ", "
                + issuerParameters.getParametersUID());
        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);

            boolean r = keyManager.storeIssuerParameters(issuerParametersUid,
                    issuerParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            this.log.info("UserService - storeIssuerParameters - done ");

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @PUT()
    @Path("/storeRevocationAuthorityParameters/{revocationAuthorityParametersUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeRevocationAuthorityParameters(
            @PathParam("revocationAuthorityParametersUid") URI revocationAuthorityParametersUid,
            RevocationAuthorityParameters revocationAuthorityParameters) {
        this.log.info("IssuanceService - storeRevocationAuthorityParameters: \""
                + revocationAuthorityParameters + "\"");

        if (revocationAuthorityParameters.getParametersUID().compareTo(
                revocationAuthorityParametersUid) != 0) {
            throw new WebApplicationException(new RuntimeException(
                    "Detected inconsistency in the revocation parameters UID found in the revocation parameters and the revocationAuthorityParametersUid"),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        try {
            CryptoEngine engine = CryptoEngine.IDEMIX;
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(IssuanceHelper.getFileStoragePrefix(
                            this.fileStoragePrefix, engine));

            boolean r1 = keyManager.storeRevocationAuthorityParameters(
                    revocationAuthorityParametersUid,
                    revocationAuthorityParameters);

            this.storeRevocationParametersInStructorStore(keyManager,
                    revocationAuthorityParametersUid);

            engine = CryptoEngine.UPROVE;
            keyManager = UserStorageManager.getKeyManager(IssuanceHelper
                    .getFileStoragePrefix(this.fileStoragePrefix, engine));

            boolean r2 = keyManager.storeRevocationAuthorityParameters(
                    revocationAuthorityParametersUid,
                    revocationAuthorityParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r1 && r2);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void storeRevocationParametersInStructorStore(
            KeyManager keyManager, URI revocationAuthorityParametersUid)
                    throws KeyManagerException {
        boolean pkInStorage = false;
        try {
            pkInStorage = StructureStore.getInstance().get(
                    revocationAuthorityParametersUid) != null;
        } catch (RuntimeException e) {
        }
        if (!pkInStorage) {
            RevocationAuthorityParameters revParams = keyManager
                    .getRevocationAuthorityParameters(revocationAuthorityParametersUid);
            @SuppressWarnings("rawtypes")
            List any = revParams.getCryptoParams().getAny();
            Element publicKeyStr = (Element) any.get(0);
            Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

            AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;
            StructureStore.getInstance().add(
                    revocationAuthorityParametersUid.toString(), publicKey);
        }
    }

    @GET()
    @Path("/status")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response issuerStatus() {
        this.log.info("IssuanceService - status : running");
        return Response.ok().build();
    }

}

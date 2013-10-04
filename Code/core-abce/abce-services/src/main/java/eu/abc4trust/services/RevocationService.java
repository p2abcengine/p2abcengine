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

package eu.abc4trust.services;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.services.helpers.RevocationHelper;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationReferences;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


@Path("/revocation")
public class RevocationService {

    private static final String REVOCATION_STORAGE_FOLDER = "revocation_storage";

    static String fileStoragePrefix = REVOCATION_STORAGE_FOLDER + "/";

    private final ObjectFactory of = new ObjectFactory();

    private final Logger log = Logger.getLogger(RevocationService.class
            .getName());

    @POST
    @Path("/setupRevocationAuthorityParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RevocationAuthorityParameters> setupRevocationAuthorityParameters(
            eu.abc4trust.xml.RevocationReferences references,
            @QueryParam("keyLength") int keyLength,
            @QueryParam("cryptoMechanism") URI cryptographicMechanism,
            @QueryParam("uid") URI uid)
                    throws Exception{

        this.log.info("RevocationService - setupRevocationAuthorityParameters ");

        this.validateRevocationReferences(references);

        Reference revocationInfoReference = references
                .getRevocationInfoReference();
        Reference nonRevocationEvidenceReference = references
                .getNonRevocationEvidenceReference();
        Reference nonRevocationUpdateReference = references
                .getNonRevocationEvidenceUpdateReference();

        this.log.info("Setting up Revocation Authority ABCE with the following parameters:");
        this.log.info("keyLength: " + keyLength);
        this.log.info("crypto: " + cryptographicMechanism);
        this.log.info("uid: " + uid);
        this.log.info("info: " + revocationInfoReference);
        this.log.info("non-evidence: " + nonRevocationEvidenceReference);
        this.log.info("non-update: " + nonRevocationUpdateReference);


        this.initializeHelper();

        try {
            RevocationAuthorityParameters raParams = RevocationHelper.setupParameters(cryptographicMechanism, keyLength, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference, fileStoragePrefix);
            // this.log.info("now return output: \n"+XmlUtils.toXml(of.createRevocationAuthorityParameters(raParams)));
            return this.of.createRevocationAuthorityParameters(raParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * Validate the input. Raises an exception if the input is incorrect.
     * 
     * @param references
     */
    private void validateRevocationReferences(RevocationReferences references) {
        this.log.info("RevocationService - validateRevocationReferences");

        if (references.getNonRevocationEvidenceReference() == null) {
            throw new RuntimeException(
                    "No non-revocation evidence reference specified");
        }

        if (references.getNonRevocationEvidenceUpdateReference() == null) {
            throw new RuntimeException(
                    "No non-revocation evidence update reference specified");
        }

        if (references.getRevocationInfoReference() == null) {
            throw new RuntimeException(
                    "No revocation information reference specified");
        }

    }

    @POST()
    @Path("/generatenonrevocationevidence/{revParUid}")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NonRevocationEvidence> generateNonRevocationEvidence(@PathParam ("revParUid") final URI revParUid, JAXBElement<AttributeList> attributeList) throws Exception {
        this.log.info("RevocationService - generatenonrevocationevidence");

        this.validateRevocationParametersUid(revParUid);

        this.log.info("RevocationService - generatenonrevocationevidence - parameters are valid");

        this.initializeHelper();

        this.checkIfParamsInStructureStore(revParUid);

        this.log.info("RevocationService - generatenonrevocationevidence - all ready...");

        List<Attribute> attributes = attributeList.getValue().getAttributes();
        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        NonRevocationEvidence revInfo = engine
                .generateNonRevocationEvidence(revParUid, attributes);

        this.log.info("RevocationService - generatenonrevocationevidence - revinfo: "
                + revInfo);

        JAXBElement<NonRevocationEvidence> createdNonRevocationEvidence = this.of.createNonRevocationEvidence(revInfo);

        String xml = XmlUtils.toNormalizedXML(createdNonRevocationEvidence);

        this.log.info("RevocationService - generatenonrevocationevidence - returning: "
                + xml);

        return createdNonRevocationEvidence;
    }

    private void validateRevocationParametersUid(final URI revParUid)
            throws Exception {
        if (revParUid == null) {
            throw new Exception("Revocation Parameters UID is null!");
        }
    }

    @POST()
    @Path("/generatenonrevocationevidenceupdate/{revParUid}")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NonRevocationEvidenceUpdate> generateNonRevocationEvidenceUpdate(@PathParam ("revParUid") final URI revParUid, @QueryParam("epoch") final int epoch) throws Exception {
        this.log.info("RevocationService - generatenonrevocationevidenceupdate");

        this.validateRevocationParametersUid(revParUid);

        this.initializeHelper();

        this.checkIfParamsInStructureStore(revParUid);

        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        NonRevocationEvidenceUpdate revInfo  = engine.generateNonRevocationEvidenceUpdate(revParUid, epoch);

        return this.of.createNonRevocationEvidenceUpdate(revInfo);
    }

    @POST()
    @Path("/getrevocationinformation/{revParUid}")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RevocationInformation> getRevocationInformation(@PathParam ("revParUid") final URI revParUid) throws Exception {
        this.log.info("RevocationService - getrevocationinformation");

        this.validateRevocationParametersUid(revParUid);

        this.initializeHelper();

        this.checkIfParamsInStructureStore(revParUid);

        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation revInfo  = engine.updateRevocationInformation(revParUid);
        return this.of.createRevocationInformation(revInfo);
    }

    @POST()
    @Path("/revoke/{revParUid}")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RevocationInformation> revoke(@PathParam ("revParUid") final URI revParUid, final JAXBElement<AttributeList> in) throws Exception {
        this.log.info("RevocationService - revoke");

        this.validateRevocationParametersUid(revParUid);

        this.initializeHelper();

        this.checkIfParamsInStructureStore(revParUid);

        List<Attribute> attributes = in.getValue().getAttributes();
        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation ri = engine.revoke(revParUid, attributes);

        return this.of.createRevocationInformation(ri);
    }

    @GET()
    @Path("/updaterevocationinformation/{revParUid}")
    public JAXBElement<RevocationInformation> updateRevocationInformation(@PathParam ("revParUid") final URI revParUid) throws Exception {
        this.log.info("RevocationService - updaterevocationinformation");

        this.validateRevocationParametersUid(revParUid);

        this.initializeHelper();

        this.checkIfParamsInStructureStore(revParUid);

        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation revInfo = engine
                .updateRevocationInformation(revParUid);

        return this.of.createRevocationInformation(revInfo);
    }


    private void checkIfParamsInStructureStore(URI revParUid) throws KeyManagerException{
        boolean pkInStorage = false;
        try {
            pkInStorage = (StructureStore.getInstance().get(revParUid) != null);
        } catch (RuntimeException e) {
            pkInStorage = false;
        }

        if(!pkInStorage){
            this.log.info("Revocation parameters not found in structurestore");
            KeyManager keyManager = RevocationHelper.getInstance().keyManager;
            RevocationAuthorityParameters revParams = keyManager
                    .getRevocationAuthorityParameters(revParUid);

            if (revParams == null) {
                throw new RuntimeException(
                        "No revocation parameters UID matching: \"" + revParUid
                        + "\"");
            }

            AccumulatorPublicKey publicKey = this.getPublicKey(revParams);

            StructureStore.getInstance().add(revParUid.toString(), publicKey);
            this.log.info("stored as " + revParUid.toString());
        }
    }

    private AccumulatorPublicKey getPublicKey(
            RevocationAuthorityParameters revParams) {
        List<Object> any = revParams.getCryptoParams().getAny();
        Element publicKeyStr = (Element) any.get(0);
        Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);
        return (AccumulatorPublicKey) publicKeyObj;
    }

    @POST()
    @Path("/storeSystemParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeSystemParameters(
            SystemParameters systemParameters) {
        this.log.info("RevocationService - storeSystemParameters ");

        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(RevocationService.fileStoragePrefix);

            boolean r = keyManager.storeSystemParameters(systemParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            if (r) {
                this.initializeHelper();
            }

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST()
    @Path("/storeRevocationAuthorityParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeRevocationAuthorityParameters(
            RevocationAuthorityParameters revocationAuthorityParameters) {
        try{
            //  register key for IDEMIX!
            this.log.info("- Try to register Revocation public key in IDEMIX StructureStore : "
                    + revocationAuthorityParameters.getParametersUID());
            AccumulatorPublicKey publicKey = this
                    .getPublicKey(revocationAuthorityParameters);

            StructureStore.getInstance().add(publicKey.getUri().toString(),
                    publicKey);
            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(true);

            return this.of.createABCEBoolean(createABCEBoolean);

        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void initializeHelper() {
        this.log.info("RevocationService loading...");

        try {
            if (RevocationHelper.isInit()) {
                this.log.info("RevocationHelper is initialized");
                AbstractHelper.verifyFiles(false,
                        RevocationService.fileStoragePrefix);
            } else {
                this.log.info("Initializing RevocationHelper...");

                UProveIntegration uproveIntegration = new UProveIntegration();
                uproveIntegration.verify();

                RevocationHelper.initInstance(RevocationService.fileStoragePrefix);

                this.log.info("RevocationHelper is initialized");
            }
        } catch (Exception ex) {
            this.log.info("Create RevocationHelper FAILED " + ex);
            ex.printStackTrace();
        }
    }

}

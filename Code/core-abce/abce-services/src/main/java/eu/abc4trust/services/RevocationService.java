//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.helpers.RevocationHelper;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationEvent;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.RevocationReferences;
import eu.abc4trust.xml.RevocationState;
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
        this.log.info("uid: " + uid);
        this.log.info("info: " + revocationInfoReference);
        this.log.info("non-evidence: " + nonRevocationEvidenceReference);
        this.log.info("non-update: " + nonRevocationUpdateReference);


        this.initializeHelper();

        try {
            URI technology = URI.create("urn:idmx:3.0.0:block:revocation:cl");
            RevocationAuthorityParameters raParams = RevocationHelper.setupParameters(technology, keyLength, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference, fileStoragePrefix);

            String legacyRevocationAuthParameter = fileStoragePrefix + "revocation_authority_";
            if("urn".equals(uid.getScheme())) {
                legacyRevocationAuthParameter += uid.toASCIIString().replaceAll(":", "_");
            } else {
                legacyRevocationAuthParameter += uid.getHost().replace(".", "_") + uid.getPath().replace("/", "_");
            }
            FileSystem.storeObjectInFile(raParams, legacyRevocationAuthParameter);
            
            return this.of.createRevocationAuthorityParameters(raParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @POST()
    @Path("/storeSystemParameters")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeSystemParameters(
            JAXBElement<SystemParameters> systemParameters) {
      System.err.println("RevocationService - storeSystemParameters ");
        this.log.info("RevocationService - storeSystemParameters ");

        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(RevocationService.fileStoragePrefix);

            boolean r = keyManager.storeSystemParameters(systemParameters.getValue());

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

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(true);

            return this.of.createABCEBoolean(createABCEBoolean);

        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
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

        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation revInfo  = engine.updateRevocationInformation(revParUid);

        return this.of.createRevocationInformation(revInfo);
    }

    @GET()
    @Path("/updaterevocationinformation/{revParUid}")
    public JAXBElement<RevocationInformation> updateRevocationInformation(@PathParam ("revParUid") final URI revParUid) throws Exception {
        this.log.info("RevocationService - updaterevocationinformation");

        this.validateRevocationParametersUid(revParUid);

        this.initializeHelper();

        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation revInfo = engine.updateRevocationInformation(revParUid);
        
        return this.of.createRevocationInformation(revInfo);
    }

    @POST()
    @Path("/revoke/{revParUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RevocationInformation> revoke(@PathParam ("revParUid") final URI revParUid, final JAXBElement<AttributeList> in) throws Exception {
        this.log.severe("RevocationService - revoke");
  
        this.validateRevocationParametersUid(revParUid);
    
        this.initializeHelper();
  
        List<Attribute> attributes = in.getValue().getAttributes();
        RevocationAbcEngine engine = RevocationHelper.getInstance().engine;
        RevocationInformation ri = engine.revoke(revParUid, attributes);
        
        return this.of.createRevocationInformation(ri);
    }


    private void initializeHelper() {
        this.log.info("RevocationService loading...");

        try {
            if (RevocationHelper.isInit()) {
                this.log.info("RevocationHelper is initialized");
                AbstractHelper.verifyFiles(false,
                        RevocationService.fileStoragePrefix);
            } else {
                this.log.info("Initializing RevocationHelper : storage folder : " + RevocationService.fileStoragePrefix);

                RevocationHelper.initInstance(RevocationService.fileStoragePrefix);

                this.log.info("RevocationHelper is initialized");
            }
        } catch (Exception ex) {
            this.log.info("Create RevocationHelper FAILED " + ex);
            ex.printStackTrace();
        }
    }
    
    @GET()
    @Path("/isAlive")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isAlive() throws Exception {
        System.out.println("ALIVE!");
        return Response.ok().build();
    }
}

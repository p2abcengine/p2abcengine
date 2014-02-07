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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.xml.sax.SAXException;

import com.google.inject.Module;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationPolicyAlternativesAndPresentationToken;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


@Path("/verification")
public class VerificationService {

    private final Logger log = Logger.getLogger(VerificationService.class
            .getName());

    ObjectFactory of = new ObjectFactory();

    public VerificationService() throws Exception {
        System.out.println("VerificationService");

        if (VerificationHelper.isInit()) {
            System.out.println(" - Helper already init'ed");
        } else {
            String fileStoragePrefix = Constants.VERIFIER_STORAGE_FOLDER + "/";
            if(System.getProperty("PathToUProveExe", null) == null) {
                String uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
                System.setProperty("PathToUProveExe", uprovePath);
            }

            String[] credSpecResources = this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER,"credentialSpecification");
            String[] revAuthResourceList = this.getFilesFromDir(Constants.REVOCATION_STORAGE_FOLDER, "revocation_authority");
            String[] inspectorResourceList = this.getFilesFromDir(Constants.INSPECTOR_STORAGE_FOLDER, "inspector");

            String[] issuerParamsResourceList = this.getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_params");


            VerificationHelper.initInstance(CryptoEngine.BRIDGED,
                    issuerParamsResourceList, credSpecResources,
                    inspectorResourceList, revAuthResourceList,
                    fileStoragePrefix, new Module[0]);
        }
    }

    @Path("/verifyTokenAgainstPolicy")
    @POST()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<PresentationTokenDescription> verifyTokenAgainstPolicy(JAXBElement<PresentationPolicyAlternativesAndPresentationToken> ppaAndpt, @QueryParam("store") String storeString) throws TokenVerificationException, CryptoEngineException{
        boolean store = false;
        if ((storeString != null) && storeString.toUpperCase().equals("TRUE")) {
            store = true;
        }
        VerificationHelper verficationHelper = VerificationHelper.getInstance();
        URI uid = URI
                .create("http://ticketcompany/MyFavoriteSoccerTeam/issuance:idemix");
        try {
            IssuerParameters ip = verficationHelper.keyManager
                    .getIssuerParameters(uid);
            String s = XmlUtils.toXml(this.of.createIssuerParameters(ip));
            System.out.println(s);
        } catch (KeyManagerException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        PresentationPolicyAlternativesAndPresentationToken value = ppaAndpt.getValue();
        PresentationPolicyAlternatives presentationPolicyAlternatives = value.getPresentationPolicyAlternatives();
        PresentationToken presentationToken = value.getPresentationToken();
        PresentationTokenDescription ptd = verficationHelper.engine.verifyTokenAgainstPolicy(presentationPolicyAlternatives, presentationToken, store);
        return this.of.createPresentationTokenDescription(ptd);
    }

    @Path("/getToken")
    @GET()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<PresentationToken> getToken(@QueryParam("tokenUID") URI tokenUid){
        VerificationHelper verificationHelper = VerificationHelper.getInstance();
        PresentationToken pt = verificationHelper.engine.getToken(tokenUid);
        return this.of.createPresentationToken(pt);
    }

    @Path("/deleteToken")
    @POST()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<Boolean> deleteToken(@QueryParam("tokenUID") URI tokenUid){
        boolean result = VerificationHelper.getInstance().engine.deleteToken(tokenUid);
        JAXBElement<Boolean> jaxResult = new JAXBElement<Boolean>(new QName("deleteToken"), Boolean.TYPE, result);
        return jaxResult;
    }

    @POST()
    @Path("/storeSystemParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeSystemParameters(
            SystemParameters systemParameters) {
        this.log.info("VerificationService - storeSystemParameters ");

        try {
            VerificationHelper verificationHelper = VerificationHelper
                    .getInstance();
            KeyManager keyManager = verificationHelper.keyManager;

            boolean r = keyManager.storeSystemParameters(systemParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

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
        this.log.info("VerificationService - storeIssuerParameters ");

        this.log.info("VerificationService - storeIssuerParameters - issuerParametersUid: "
                + issuerParametersUid
                + ", "
                + issuerParameters.getParametersUID());
        try {
            VerificationHelper verificationHelper = VerificationHelper
                    .getInstance();
            KeyManager keyManager = verificationHelper.keyManager;

            boolean r = keyManager.storeIssuerParameters(issuerParametersUid,
                    issuerParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            try {
                IssuerParameters ip = keyManager
                        .getIssuerParameters(issuerParametersUid);
                String s = XmlUtils.toXml(this.of.createIssuerParameters(ip));
                System.out.println(s);
            } catch (KeyManagerException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (JAXBException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (SAXException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }

            this.log.info("VerificationService - storeIssuerParameters - done ");

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @GET()
    @Path("/createPresentationPolicy")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<PresentationPolicyAlternatives> createPresentationPolicy(
            @PathParam("applicationData") String applicationData,
            PresentationPolicyAlternatives presentationPolicy) {
        this.log.info("VerificationService - createPresentationPolicy ");

        try {
            VerificationHelper verificationHelper = VerificationHelper
                    .getInstance();

            Map<URI, URI> revocationInformationUids = new HashMap<URI, URI>();

            PresentationPolicyAlternatives modifiedPresentationPolicyAlternatives = verificationHelper
                    .createPresentationPolicy(presentationPolicy,
                            applicationData, revocationInformationUids);
            this.log.info("VerificationService - createPresentationPolicy - done ");

            return this.of
                    .createPresentationPolicyAlternatives(modifiedPresentationPolicyAlternatives);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT()
    @Path("/storeCredentialSpecification/{credentialSpecifationUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeCredentialSpecification(
            @PathParam("credentialSpecifationUid") URI credentialSpecifationUid,
            CredentialSpecification credSpec) {
        this.log.info("VerificationService - storeCredentialSpecification ");

        try {
            VerificationHelper verificationHelper = VerificationHelper
                    .getInstance();

            KeyManager keyManager = verificationHelper.keyManager;

            boolean r = keyManager.storeCredentialSpecification(
                    credentialSpecifationUid, credSpec);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

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
        this.log.info("VerificationService - storeRevocationAuthorityParameters: \""
                + revocationAuthorityParameters + "\"");

        try {
            VerificationHelper verificationHelper = VerificationHelper
                    .getInstance();

            KeyManager keyManager = verificationHelper.keyManager;

            boolean r = keyManager.storeRevocationAuthorityParameters(
                    revocationAuthorityParametersUid,
                    revocationAuthorityParameters);

            verificationHelper
            .registerRevocationPublicKeyForIdemix(revocationAuthorityParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String[] getFilesFromDir(String folderName, final String filter){
        String[] resourceList;
        URL url = AbstractHelper.class.getResource(folderName);
        File folder = null;
        if(url != null) {
            folder = new File(url.getFile());
        }else{
            folder = new File(folderName);
        }

        File[] fileList = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.indexOf(filter) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if(fileList == null){
            System.out.println("Folder "+folderName+" does not exist! \n Trying to continue without these resources");
            return new String[0];
        }


        resourceList = new String[fileList.length];
        for(int i=0; i<fileList.length; i++) {
            resourceList[i] = fileList[i].getAbsolutePath();
        }
        return resourceList;
    }

    private boolean checkIfFileExists(String fileName){
        URL url = AbstractHelper.class.getResource(fileName);
        File f = null;
        if(url != null){
            f = new File(url.getFile());
        }else{
            f = new File(fileName);
        }
        return f.exists();
    }

}

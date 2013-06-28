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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternativesAndPresentationToken;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;


@Path("/verification")
public class VerificationService {

    ObjectFactory of = new ObjectFactory();

    public VerificationService() throws Exception {
        System.out.println("VerificationService");

        if (VerificationHelper.isInit()) {
            System.out.println(" - Helper already init'ed");
        } else {
            String fileStoragePrefix = Constants.VERIFIER_STORAGE_FOLDER + "/";
            if(System.getProperty("PathToUProveExe", null) == null) {
                String uprovePath = "./../../../dotNet/ABC4Trust-UProve_dotNET_WebServiceServer/ABC4Trust-UProve/bin/Release";
                System.setProperty("PathToUProveExe", uprovePath);
            }

            String[] credSpecResources = this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER,"credentialSpecification");
            String[] revAuthResourceList = this.getFilesFromDir(Constants.REVOCATION_STORAGE_FOLDER, "revocation_authority");
            String[] inspectorResourceList = this.getFilesFromDir(Constants.INSPECTOR_STORAGE_FOLDER, "inspector");

            String[] issuerParamsResourceList = this.getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_params");


            VerificationHelper.initInstance(CryptoEngine.BRIDGED, issuerParamsResourceList,
                    credSpecResources, inspectorResourceList, revAuthResourceList ,fileStoragePrefix, new String[0]);
        }
    }

    @Path("/verifyTokenAgainstPolicy")
    @POST()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<PresentationTokenDescription> verifyTokenAgainstPolicy(JAXBElement<PresentationPolicyAlternativesAndPresentationToken> ppaAndpt, @QueryParam("store") String storeString) throws TokenVerificationException, CryptoEngineException{
        boolean store = false;
        try{
            store = storeString.toUpperCase().equals("TRUE");
        }catch(Exception e){}
        PresentationTokenDescription ptd = VerificationHelper.getInstance().engine.verifyTokenAgainstPolicy(ppaAndpt.getValue().getPresentationPolicyAlternatives(), ppaAndpt.getValue().getPresentationToken(), store);
        return this.of.createPresentationTokenDescription(ptd);
    }

    @Path("/getToken")
    @GET()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<PresentationToken> getToken(@QueryParam("tokenUID") URI tokenUid){
        PresentationToken pt = VerificationHelper.getInstance().engine.getToken(tokenUid);
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

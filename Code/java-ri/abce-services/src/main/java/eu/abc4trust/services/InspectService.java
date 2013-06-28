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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;

/**
 */
@Path("/inspector")
public class InspectService {


    ObjectFactory of = new ObjectFactory();

    public InspectService() throws Exception {
        System.out.println("InspectService");
        if(InspectorHelper.isInit()) {
            System.out.println("- Helper is already initialized");
        } else {
            System.out.println("- init Helper...");

            String fileStoragePrefix = Constants.INSPECTOR_STORAGE_FOLDER+"/";

            boolean existingStorage = this.checkIfFileExists(fileStoragePrefix+"inspectorSecrets");

            if(existingStorage){
                System.out.println("InspectionService -- Found existing secret storage file, trying to use them");
                String[] inspector_public_keys = this.getFilesFromDir(fileStoragePrefix, "inspector_publickey");
                String systemParametersResource = Constants.SYSTEM_PARAMETER_RESOURCE;

                String[] credSpecResourceList =
                        this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification");


                InspectorHelper.initInstance(fileStoragePrefix, systemParametersResource, credSpecResourceList, inspector_public_keys);
            }else{
                System.out.println("RevocationService - Did not find any storage files. Do nothing until setupRevocationAuthorityParameters() is invoked");
            }
        }
    }


    @Path("/inspect")
    @POST()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<AttributeList> inspect(JAXBElement<PresentationToken> pt) throws Exception{
        List<Attribute> attributes = InspectorHelper.getInstance().engine.inspect(pt.getValue());
        AttributeList al = this.of.createAttributeList();
        al.getAttributes().addAll(attributes);
        return this.of.createAttributeList(al);
    }

    @Path("/setupInspectorPublicKey")
    @POST()
    @Produces({MediaType.TEXT_XML})
    public JAXBElement<InspectorPublicKey> setupInspectorPublicKey(
            @QueryParam("keyLength") int keyLength,
            @QueryParam("cryptoMechanism") URI cryptoMechanism,
            @QueryParam("uid") URI uid)
                    throws Exception{
        System.out.println("Setting up Inspector public keywith the following parameters:");
        System.out.println("keyLength: "+keyLength);
        System.out.println("crypto: "+cryptoMechanism);
        System.out.println("uid: "+uid);

        String fileStoragePrefix = Constants.INSPECTOR_STORAGE_FOLDER+"/";

        if(!InspectorHelper.isInit()){
            System.out.println("InspectionService -- Found existing secret storage file, trying to use them");

            String[] credSpecResourceList = null;

            try{
                credSpecResourceList =
                        this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification");
            }catch(NullPointerException npe){
                throw new FileNotFoundException("Credential Specification resources folder does not exist!");
            }
            InspectorHelper.initInstance(fileStoragePrefix, Constants.SYSTEM_PARAMETER_RESOURCE, credSpecResourceList, new String[0]);
        }
        InspectorPublicKey ipk = InspectorHelper.setupPublicKey(cryptoMechanism, keyLength, uid, fileStoragePrefix);

        return this.of.createInspectorPublicKey(ipk);
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

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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
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

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

/**
 */
@Path("/inspector")
public class InspectService {

  ObjectFactory of = new ObjectFactory();
  private final Logger log = Logger
      .getLogger(InspectService.class.getName());

//  public InspectService() throws Exception {
 //   System.out.println("InspectService");
  //  initializeHelper();
 // }

  private void initializeHelper() {
    this.log.info("InspectionService loading...");
    try {        
      String fileStoragePrefix = Constants.INSPECTOR_STORAGE_FOLDER+"/";
      if (InspectorHelper.isInit()) {
        this.log.info("InspectorHelper is initialized");
        InspectorHelper.verifyFiles(false, fileStoragePrefix);
    //    loadSystemParameters();
    //    loadCredentialSpec();
     //   loadIssuerParameters();
      } else {
        this.log.info("Initializing InspectorHelper...");

    //    String[] credSpecResources =
    //        this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification", "xml");

        //String[] inspectorResourceList =
        //    this.getFilesFromDir(Constants.INSPECTOR_STORAGE_FOLDER, "inspector_public", "xml");
        
        InspectorHelper.initInstanceForService(fileStoragePrefix);//, Constants.SYSTEM_PARAMETER_RESOURCE, 
          //credSpecResources, inspectorResourceList);            

        this.log.info("InspectorHelper is initialized");
      }
    } catch (Exception e) {
      System.out.println("Create Domain FAILED " + e);
      e.printStackTrace();
    }
  }  
/*
  private void loadCredentialSpec() throws IOException, JAXBException, SAXException{
    String[] credSpecResources =
        this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification", "xml");
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResources);
    InspectorHelper.getInstance().addCredentialSpecifications(credSpecList);
  }
  
  private void loadSystemParameters() throws IOException, JAXBException, SAXException{
    SystemParameters syspar = FileSystem.loadXmlFromResource(Constants.SYSTEM_PARAMETER_RESOURCE);
    InspectorHelper.getInstance().setSystemParams(syspar);
  }
  
  private void loadIssuerParameters() 
      throws IOException, JAXBException, SAXException, KeyManagerException{
    String[] issuerParamsResourceList =
        this.getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_param", "xml");
    List<IssuerParameters> issuerParamsList =
        FileSystem.loadXmlListFromResources(issuerParamsResourceList);
    InspectorHelper helper = InspectorHelper.getInstance();
    helper.addIssuerParameters(issuerParamsList);
  } */

  @Path("/inspect")
  @POST()
  @Produces({MediaType.TEXT_XML})
  public JAXBElement<AttributeList> inspect(JAXBElement<PresentationToken> pt) throws Exception{
    
    initializeHelper();
    
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
    String fileStoragePrefix = Constants.INSPECTOR_STORAGE_FOLDER+"/";

    initializeHelper();

    SystemParameters systemParameters = InspectorHelper.getInstance().getSystemParameters();
    
    InspectorPublicKey ipk = InspectorHelper.setupPublicKey(systemParameters, cryptoMechanism, keyLength, uid, fileStoragePrefix);
    InspectorHelper.getInstance().addInspectorPublicKey(uid, ipk);
    return this.of.createInspectorPublicKey(ipk);
  }

  @POST()
  @Path("/storeSystemParameters")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<ABCEBoolean> storeSystemParameters(
          JAXBElement<SystemParameters> systemParameters) {
    System.err.println("InspectService - storeSystemParameters ");
      this.log.info("InspectService - storeSystemParameters ");

      try {
    	  
          KeyManager keyManager = UserStorageManager
                  .getKeyManager(Constants.INSPECTOR_STORAGE_FOLDER+"/");

          boolean r = keyManager.storeSystemParameters(systemParameters.getValue());

       //   InspectorHelper.getInstance().setSystemParams(systemParameters.getValue());
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
  
  @PUT()
  @Path("/storeCredentialSpecification/{credentialSpecifationUid}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<ABCEBoolean> storeCredentialSpecification(
          @PathParam("credentialSpecifationUid") URI credentialSpecifationUid,
          CredentialSpecification credSpec) {
      this.log.info("InspectorService - storeCredentialSpecification ");

      try {
    	  KeyManager keyManager = UserStorageManager
                  .getKeyManager(Constants.INSPECTOR_STORAGE_FOLDER+"/");

          boolean r = keyManager.storeCredentialSpecification(
                  credentialSpecifationUid, credSpec);

          ABCEBoolean createABCEBoolean = this.of
                  .createABCEBoolean();
          createABCEBoolean.setValue(r);

          return this.of.createABCEBoolean(createABCEBoolean);
      } catch (Exception ex) {
          throw new WebApplicationException(ex,
                  Response.Status.INTERNAL_SERVER_ERROR);
      }
  }
  

  private String[] getFilesFromDir(String folderName, final String filter, final String extension){
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
        if (arg1.indexOf(filter) != -1 && arg1.endsWith(extension)) {
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

}

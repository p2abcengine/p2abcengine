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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.inspector.InspectorHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
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

  ObjectFactory of = new ObjectFactory();

  public VerificationService() throws Exception {
	    System.out.println("VerificationService");
	    initializeHelper();
  }
  
  private void initializeHelper() {
	  try {
		  if (VerificationHelper.isInit()) {
			  VerificationHelper.verifyFiles(false, Constants.VERIFIER_STORAGE_FOLDER + "/");
		  } else {
			  VerificationHelper.initInstance();
		  }
		  try{
			  loadSystemParameters();
		  }catch(Exception e){
			  System.out.println("Failed to load system parameters.");
		  }
		  try{
			  loadIssuerParameters();
		  }catch(Exception e){
			  System.out.println("Failed to load issuer parameters.");
		  }
		  try{
			  loadCredentialSpec();
		  }catch(Exception e){
			  System.out.println("Failed to load credential specifications.");
		  }
		  try{
			  loadRevocationAuthorityParameters();
		  }catch(Exception e){
			  System.out.println("Failed to load revocation authority parameters.");
		  }
	  } catch (Exception e) {
		  System.out.println("Create Domain FAILED " + e);
		  e.printStackTrace();
	  }
  }
  
  private void loadIssuerParameters() 
      throws IOException, JAXBException, SAXException, KeyManagerException{
    String[] issuerParamsResourceList =
        this.getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_param", "xml");
    List<IssuerParameters> issuerParamsList =
        FileSystem.loadXmlListFromResources(issuerParamsResourceList);
    VerificationHelper helper = VerificationHelper.getInstance();
    helper.addIssuerParameters(issuerParamsList);
  }
  
  private void loadCredentialSpec() throws IOException, JAXBException, SAXException{
    String[] credSpecResources =
        this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification", "xml");
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResources);
    VerificationHelper.getInstance().addCredentialSpecifications(credSpecList);
  }
  
  private void loadSystemParameters() throws IOException, JAXBException, SAXException {
    String systemParamsResource = Constants.SYSTEM_PARAMETER_RESOURCE;
    SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamsResource);
    this.storeSystemParameters(systemParams);
  }

  private void loadRevocationAuthorityParameters() throws IOException, JAXBException, SAXException {
	    String[] revAuthParamsResource = 
	    		this.getFilesFromDir(Constants.REVOCATION_STORAGE_FOLDER, "revocation", "xml");
	    List<RevocationAuthorityParameters> revAuthParams = 
	    		FileSystem.loadXmlListFromResources(revAuthParamsResource);
	    for(RevocationAuthorityParameters revAuthParameter: revAuthParams){
	    	this.storeRevocationAuthorityParameters(revAuthParameter.getParametersUID(), revAuthParameter);
	    }
	  }
  
  
  @Path("/verifyTokenAgainstPolicy")
  @POST()
  @Produces({MediaType.TEXT_XML})
  public JAXBElement<PresentationTokenDescription> verifyTokenAgainstPolicy(
      JAXBElement<PresentationPolicyAlternativesAndPresentationToken> ppaAndpt,
      @QueryParam("store") String storeString) throws TokenVerificationException,
      CryptoEngineException{
    this.initializeHelper();
    
    boolean store = false;
    try {
      store = storeString.toUpperCase().equals("TRUE");
    } catch (Exception e) {}    
    
    PresentationTokenDescription ptd =
        VerificationHelper.getInstance().engine
            .verifyTokenAgainstPolicy(ppaAndpt.getValue().getPresentationPolicyAlternatives(),
                ppaAndpt.getValue().getPresentationToken(), store);
    return this.of.createPresentationTokenDescription(ptd);
  }

  @Path("/getToken")
  @GET()
  @Produces({MediaType.TEXT_XML})
  public JAXBElement<PresentationToken> getToken(@QueryParam("tokenUID") URI tokenUid) {
    PresentationToken pt = VerificationHelper.getInstance().engine.getToken(tokenUid);
    return this.of.createPresentationToken(pt);
  }

  @Path("/deleteToken")
  @POST()
  @Produces({MediaType.TEXT_XML})
  public JAXBElement<Boolean> deleteToken(@QueryParam("tokenUID") URI tokenUid) {
    boolean result = VerificationHelper.getInstance().engine.deleteToken(tokenUid);
    JAXBElement<Boolean> jaxResult =
        new JAXBElement<Boolean>(new QName("deleteToken"), Boolean.TYPE, result);
    return jaxResult;
  }

  private String[] getFilesFromDir(String folderName, final String filter, final String extension) {
    String[] resourceList;
    URL url = AbstractHelper.class.getResource(folderName);
    File folder = null;
    if (url != null) {
      folder = new File(url.getFile());
    } else {
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
    if (fileList == null) {
      System.out.println("Folder " + folderName
          + " does not exist! \n Trying to continue without these resources");
      return new String[0];
    }


    resourceList = new String[fileList.length];
    for (int i = 0; i < fileList.length; i++) {
      resourceList[i] = fileList[i].getAbsolutePath();
    }
    return resourceList;
  }

  @POST()
  @Path("/storeSystemParameters/")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<ABCEBoolean> storeSystemParameters(
		  SystemParameters systemParameters) {

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
		  JAXBElement<PresentationPolicyAlternatives> rawPresentationPolicy) {

	  try {
		  PresentationPolicyAlternatives presentationPolicy = rawPresentationPolicy.getValue();
		  VerificationHelper verificationHelper = VerificationHelper
				  .getInstance();

		  Map<URI, URI> revocationInformationUids = new HashMap<URI, URI>();

		  //TODO Michael: Verifiy that this is correct
		  PresentationPolicyAlternatives modifiedPresentationPolicyAlternatives = verificationHelper
				  //.createPresentationPolicy(presentationPolicy,
				  .modifyPresentationPolicy(presentationPolicy, verificationHelper.generateNonce(),
						  applicationData, revocationInformationUids);

		  return this.of
				  .createPresentationPolicyAlternatives(modifiedPresentationPolicyAlternatives);
	  } catch (Exception ex) {
		  ex.printStackTrace();
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
	  try {
		  VerificationHelper verificationHelper = VerificationHelper
				  .getInstance();

		  KeyManager keyManager = verificationHelper.keyManager;

		  boolean r = keyManager.storeRevocationAuthorityParameters(
				  revocationAuthorityParametersUid,
				  revocationAuthorityParameters);

		  //TODO Michael: Not sure if this step i required with new CA?
		  //verificationHelper
		  //.registerRevocationPublicKeyForIdemix(revocationAuthorityParameters);

		  ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
		  createABCEBoolean.setValue(r);

		  return this.of.createABCEBoolean(createABCEBoolean);
	  } catch (Exception ex) {
		  throw new WebApplicationException(ex,
				  Response.Status.INTERNAL_SERVER_ERROR);
	  }
  }

  @PUT()
  @Path("/storeInspectorPublicKey/{inspectorPublicKeyUid}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
  @Produces(MediaType.TEXT_XML)
  public JAXBElement<ABCEBoolean> storeInspectorPublicKey(
          @PathParam("inspectorPublicKeyUid") URI inspectorPublicKeyUid,
          InspectorPublicKey inspectorPublicKey) {
      try {
    	  VerificationHelper verificationHelper = VerificationHelper.getInstance();
          KeyManager keyManager = verificationHelper.keyManager;

          boolean r = keyManager.storeInspectorPublicKey(inspectorPublicKeyUid, inspectorPublicKey);

          ABCEBoolean createABCEBoolean = this.of
                  .createABCEBoolean();
          createABCEBoolean.setValue(r);

          return this.of.createABCEBoolean(createABCEBoolean);
      } catch (Exception ex) {
          throw new WebApplicationException(ex,
                  Response.Status.INTERNAL_SERVER_ERROR);
      }
  }
  
}

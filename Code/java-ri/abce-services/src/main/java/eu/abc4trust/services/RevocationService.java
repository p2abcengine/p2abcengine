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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.revocation.RevocationHelper;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;


/** class RevocationService
 *  This is a demo implementation. This particular service will either accept or fail all
 *  RevocationMessage requests in a number of rounds.
 *  It is expected that 3d party providers implement this interface and do what needs to be done
 */

@Path("/revocation")
public class RevocationService {

    private final ObjectFactory of = new ObjectFactory();
    
    public RevocationService() throws Exception {
        if(RevocationHelper.isInit()) {
            System.out.println("RevocationHelper - already setup..");
        } else {
        	try{
                String fileStoragePrefix = Constants.REVOCATION_STORAGE_FOLDER
                        +"/";
                
                boolean existingStorage = checkIfFileExists(fileStoragePrefix+"revocationAuthoritySecrets");
                existingStorage = existingStorage && checkIfFileExists(fileStoragePrefix+"revocationAuthorityStorage");
                
                if(existingStorage){
                	System.out.println("RevocationService -- Found existing storage files, trying to use them");
                	String[] rev_auth = getFilesFromDir(fileStoragePrefix, "revocation_authority");
                    String systemParametersResource = Constants.SYSTEM_PARAMETER_RESOURCE;

                    String[] issuerParamsResourceList = getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_params");

                    String[] credSpecResourceList =
                    		this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification");

                	RevocationHelper.initInstance(fileStoragePrefix, issuerParamsResourceList, credSpecResourceList, systemParametersResource, rev_auth);
                }else{
                	System.out.println("RevocationService - Did not find any storage files. Do nothing until setupRevocationAuthorityParameters() is invoked");
                }
        		
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
    }

    @POST
    @Path("/setupRevocationAuthorityParameters/")
    @Consumes(MediaType.APPLICATION_XML)
    public JAXBElement<RevocationAuthorityParameters> setupRevocationAuthorityParameters(
    		JAXBElement<eu.abc4trust.xml.RevocationReferences> references,
            @QueryParam("keyLength") int keyLength,
            @QueryParam("cryptoMechanism") URI cryptographicMechanism,
            @QueryParam("uid") URI uid) 
            throws Exception{
    	
    	eu.abc4trust.xml.RevocationReferences refs = references.getValue();
    	Reference revocationInfoReference = refs.getRevocationInfoReference();
        Reference nonRevocationEvidenceReference = refs.getNonRevocationEvidenceReference();
        Reference nonRevocationUpdateReference = refs.getNonRevocationEvidenceUpdateReference();
    	
    	System.out.println("Setting up Revocation Authority ABCE with the following parameters:");
    	System.out.println("keyLength: "+keyLength);
    	System.out.println("crypto: "+cryptographicMechanism);
    	System.out.println("uid: "+uid);
    	System.out.println("info: "+revocationInfoReference);
    	System.out.println("non-evidence: "+nonRevocationEvidenceReference);
    	System.out.println("non-update: "+nonRevocationUpdateReference);
    	
               
        String fileStoragePrefix = Constants.REVOCATION_STORAGE_FOLDER
                + "/";

        String systemParametersResource = Constants.SYSTEM_PARAMETER_RESOURCE;
        String[] issuerParamsResourceList = null;
        String[] credSpecResourceList = null;
        
        try{
			issuerParamsResourceList = getFilesFromDir(Constants.ISSUER_RESOURCES_FOLDER, "issuer_params");
		}catch(NullPointerException npe){
			throw new FileNotFoundException("Issuer resources folder does not exist!");
		}
        try{
        	credSpecResourceList =
            		this.getFilesFromDir(Constants.CREDENTIAL_SPECIFICATION_FOLDER, "credentialSpecification");
        }catch(NullPointerException npe){
			throw new FileNotFoundException("Credential Specification resources folder does not exist!");
		}
		

        RevocationHelper.resetInstance();
        
        try {
        	
			RevocationHelper.initInstance(fileStoragePrefix, issuerParamsResourceList, credSpecResourceList, systemParametersResource, new String[0]);
	    	RevocationAuthorityParameters raParams = RevocationHelper.setupParameters(cryptographicMechanism, keyLength, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference, fileStoragePrefix);
	    	//System.out.println("now return output: \n"+XmlUtils.toXml(of.createRevocationAuthorityParameters(raParams)));
	    	return of.createRevocationAuthorityParameters(raParams);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    
    @POST()
    @Path("/generatenonrevocationevidence/{revParUid}")
    public JAXBElement<NonRevocationEvidence> generateNonRevocationEvidence(@PathParam ("revParUid") final URI revParUid, JAXBElement<AttributeList> attributeList) throws Exception {
    	if(revParUid==null) {
    		throw new Exception("Revocation Parameters UID is null!");
    	}
    	checkIfParamsInStructureStore(revParUid);
    	List<Attribute> attributes = attributeList.getValue().getAttributes();
    	NonRevocationEvidence revInfo  = RevocationHelper.getInstance().engine.generateNonRevocationEvidence(revParUid, attributes);

    	return of.createNonRevocationEvidence(revInfo);
    	/* Wrap in RevocationMessage
    	RevocationMessage rm = of.createRevocationMessage();
    	rm.setRevocationAuthorityParametersUID(revInfo.getRevocationAuthorityParametersUID());
    	CryptoParams cp = of.createCryptoParams();
    	cp.getAny().add(of.createNonRevocationEvidence(revInfo));
    	rm.setCryptoParams(cp);
    	return of.createRevocationMessage(rm);
    	*/
    }
    
    @POST()
    @Path("/generatenonrevocationevidenceupdate/{revParUid}")
    public JAXBElement<NonRevocationEvidenceUpdate> generateNonRevocationEvidenceUpdate(@PathParam ("revParUid") final URI revParUid, @QueryParam("epoch") final int epoch) throws Exception {
    	if(revParUid==null) {
    		throw new Exception("Revocation Parameters UID is null!");
    	}
    	checkIfParamsInStructureStore(revParUid);
    	NonRevocationEvidenceUpdate revInfo  = RevocationHelper.getInstance().engine.generateNonRevocationEvidenceUpdate(revParUid, epoch);
    	return of.createNonRevocationEvidenceUpdate(revInfo);
    	/*RevocationMessage rm = of.createRevocationMessage();
    	rm.setRevocationAuthorityParametersUID(revInfo.getRevocationAuthorityParametersUID());
    	CryptoParams cp = of.createCryptoParams();
    	cp.getAny().add(of.createNonRevocationEvidenceUpdate(revInfo));
    	rm.setCryptoParams(cp);
    	return of.createRevocationMessage(rm);*/
    }

    @POST()
    @Path("/getrevocationinformation/{revParUid}")
    public JAXBElement<RevocationInformation> getRevocationInformation(@PathParam ("revParUid") final URI revParUid) throws Exception {
    	if(revParUid==null) {
    		throw new Exception("Revocation Parameters UID is null!");
    	}

    	checkIfParamsInStructureStore(revParUid);
 
    	RevocationInformation revInfo  = RevocationHelper.getInstance().engine.updateRevocationInformation(revParUid);
    	return of.createRevocationInformation(revInfo);
    	/*
    	RevocationMessage rm = of.createRevocationMessage();
    	rm.setRevocationAuthorityParametersUID(revInfo.getRevocationAuthorityParameters());
    	CryptoParams cp = of.createCryptoParams();
    	cp.getAny().add(of.createRevocationInformation(revInfo));
    	rm.setCryptoParams(cp);
    	return of.createRevocationMessage(rm); */
    }
    
    @POST()
    @Path("/revoke/{revParUid}")
    //  @Produces(MediaType.APPLICATION_XML)
    public JAXBElement<RevocationInformation> revoke(@PathParam ("revParUid") final URI revParUid, final JAXBElement<AttributeList> in) throws Exception {
        List<Attribute> attributes = in.getValue().getAttributes();
        RevocationInformation ri = RevocationHelper.getInstance().engine.revoke(revParUid, attributes);
        return this.of.createRevocationInformation(ri);
        /* The following code will wrap the revocation information into a RevocationMessage
        RevocationMessage rm = of.createRevocationMessage();
    	rm.setRevocationAuthorityParametersUID(ri.getRevocationAuthorityParameters());
    	CryptoParams cp = of.createCryptoParams();
    	cp.getAny().add(of.createRevocationInformation(ri));
    	rm.setCryptoParams(cp);
    	return of.createRevocationMessage(rm);
    	*/
    }

    @GET()
    @Path("/updaterevocationinformation/{revParUid}")
    public JAXBElement<RevocationInformation> updateRevocationInformation(@PathParam ("revParUid") final URI revParUid) throws Exception {
    	if(revParUid==null) {
    		throw new Exception("Revocation Parameters UID is null!");
    	}

    	RevocationInformation revInfo  = RevocationHelper.getInstance().engine.updateRevocationInformation(revParUid);
    	return of.createRevocationInformation(revInfo);
    	/*
    	RevocationMessage rm = of.createRevocationMessage();
    	rm.setRevocationAuthorityParametersUID(revInfo.getRevocationAuthorityParameters());
    	CryptoParams cp = of.createCryptoParams();
    	cp.getAny().add(of.createRevocationInformation(revInfo));
    	rm.setCryptoParams(cp);
    	return of.createRevocationMessage(rm);*/
    }


    private void checkIfParamsInStructureStore(URI revParUid) throws KeyManagerException{
    	boolean pkInStorage = false;
    	try{
    		pkInStorage = (StructureStore.getInstance().get(revParUid) != null);
    	}catch(RuntimeException e){};
    	if(!pkInStorage){
    		System.out.println("Revocation parameters not found in structurestore");
    		RevocationAuthorityParameters revParams = RevocationHelper.getInstance().keyManager.getRevocationAuthorityParameters(revParUid);
    	    List<Object> any = revParams.getCryptoParams().getAny();
    	    Element publicKeyStr = (Element) any.get(0);
    	    Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

    	    AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;    		
    		StructureStore.getInstance().add(revParUid.toString(), publicKey);
    		System.out.println("stored as "+revParUid.toString());
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

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

package eu.abc4trust.revocationProxy;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;

public class WebServiceCommunicationStrategy implements RevocationProxyCommunicationStrategy {

    private void checkSupportedReferenceType(Reference r) throws RevocationProxyException {
        URI type = r.getReferenceType();
        if(type!=null) {
            String s = type.toString();
            if("http".equals(s) || "https".equals(s) ) {
                return;
            }
        }
        throw new RevocationProxyException("Only 'http' and 'https' ReferenceTypes are supported in WebServiceCommunicationStrategy - was : " + type);
    }

    private URI getRevocationServiceURI(Reference r) throws RevocationProxyException {
        //List<Object> anyList = r.getAny();
    	List<URI> anyList = r.getReferences();
        if(anyList!=null && anyList.size()>0) {
            Object o = anyList.get(0);
            if(o instanceof URI) {
                return (URI) o;
            } else {
                throw new RevocationProxyException("Wrong type of reference to Revocation service - expected URI - was : " + o.getClass());
            }
        } else {
            throw new RevocationProxyException("URI containing Web address of Revocation not supplied in Reference.");
        }
    }
    
    private RevocationMessage getRevocationMessageFromServer(Reference reference, RevocationMessage m) throws RevocationProxyException {

      URI revocationServiceURI = getRevocationServiceURI(reference);
      System.out.println("WebServiceCommunicationStrategy.getRevocationMessageFromServer : " + reference.getReferenceType() + " : " + revocationServiceURI);

      // 
      ObjectFactory of = new ObjectFactory();

      Client client = Client.create();
//      client.setFollowRedirects(false);
      client.setConnectTimeout(15000);
      client.setReadTimeout(30000);

      WebResource revocationResource = client.resource(revocationServiceURI);

      JAXBElement<RevocationMessage> request =  of.createRevocationMessage(m);
      
      RevocationMessage resp = of.createRevocationMessage();
      resp.setContext(m.getContext());
      resp.setRevocationAuthorityParametersUID(m.getRevocationAuthorityParametersUID());
      
      ClientResponse cresp = revocationResource.post(ClientResponse.class, request);
      InputStream respIS = cresp.getEntityInputStream();
      RevocationMessage result = null; 
      
      try {
          // Unmarshall returned message
          JAXBContext jCtx = JAXBContext.newInstance(RevocationMessage.class, NonRevocationEvidence.class, NonRevocationEvidenceUpdate.class, RevocationInformation.class);
          Object entity = jCtx.createUnmarshaller().unmarshal(respIS);

          // If the response is an JAXBElement, look at what type it is and create a RevocationMessage
          // otherwise throw exception
          if(entity instanceof JAXBElement){
        	  JAXBElement ent = (JAXBElement)entity;
              Class declaredType = ent.getDeclaredType();
              if(declaredType == RevocationMessage.class){
            	  result = (RevocationMessage)ent.getValue();
              }else if(declaredType == NonRevocationEvidence.class ||declaredType == NonRevocationEvidenceUpdate.class ||declaredType == RevocationInformation.class){
            	  CryptoParams cp = of.createCryptoParams();
            	  cp.getAny().add(ent);
            	  result = of.createRevocationMessage();
            	  result.setContext(m.getContext());
            	  result.setRevocationAuthorityParametersUID(m.getRevocationAuthorityParametersUID());
            	  result.setCryptoParams(cp);
              }else {
            	  throw new RevocationProxyException("Unexpected JAXBElement received");
              }
          }else {
        	  throw new RevocationProxyException("Unknown datatype received");
          }
      } catch (JAXBException e) {
    	  throw new RevocationProxyException(e);
      }
      
      return result;
    }

    @Override
    public CryptoParams requestRevocationHandle(RevocationMessage m,
            Reference nonRevocationEvidenceReference)
                    throws RevocationProxyException {

        checkSupportedReferenceType(nonRevocationEvidenceReference);

        try {
            RevocationMessage response = getRevocationMessageFromServer(nonRevocationEvidenceReference, m);
            return response.getCryptoParams();
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();
            // is 'null' allowed ? - handling taken from old code...
            if(clientResponse.getStatus()== Status.NO_CONTENT.ordinal()) {
                return null;
            }
            throw new RevocationProxyException("Failed to get RevocationMessage from server : " + e);
        }
       
    }


    @Override
    public CryptoParams requestRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {

        checkSupportedReferenceType(revocationInfoReference);

        try {
            RevocationMessage response = getRevocationMessageFromServer(revocationInfoReference, m);
            return response.getCryptoParams();
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();
            // is 'null' allowed ? - handling taken from old code...
            if(clientResponse.getStatus()== Status.NO_CONTENT.ordinal()) {
              System.err.println("Response from RevocationServer should not be empty ?");
                throw new RevocationProxyException("Response from RevocationServer should not be empty ?");
            } else {
              System.err.println("Failed to get RevocationMessage from server : " + e);
                throw new RevocationProxyException("Failed to get RevocationMessage from server : " + e);
            }
        }
    }

    @Override
    public CryptoParams revocationEvidenceUpdate(RevocationMessage m,
            Reference nonRevocationEvidenceUpdateReference)
                    throws RevocationProxyException {
      
        checkSupportedReferenceType(nonRevocationEvidenceUpdateReference);

        try {
            RevocationMessage response = getRevocationMessageFromServer(nonRevocationEvidenceUpdateReference, m);
            return response.getCryptoParams();
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();
            // is 'null' allowed ? - handling taken from old code...
            if(clientResponse.getStatus()== Status.NO_CONTENT.ordinal()) {
                throw new RevocationProxyException("Response from RevocationServer should not be empty ?");
            } else {
                throw new RevocationProxyException("Failed to get RevocationMessage from server : " + e);
            }
        }
    }

    @Override
    public CryptoParams getCurrentRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {
      
        checkSupportedReferenceType(revocationInfoReference);

        try {
            RevocationMessage response = getRevocationMessageFromServer(revocationInfoReference, m);
            return response.getCryptoParams();
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();
            // is 'null' allowed ? - handling taken from old code...
            if(clientResponse.getStatus()== Status.NO_CONTENT.ordinal()) {
                throw new RevocationProxyException("Response from RevocationServer should not be empty ?");
            } else {
                throw new RevocationProxyException("Failed to get RevocationMessage from server : " + e);
            }
        }
    }

}

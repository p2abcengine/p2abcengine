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

package eu.abc4trust.revocationProxy;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.util.XmlUtils;

public class GenericWebServiceCommunicationStrategy implements RevocationProxyCommunicationStrategy {

    private final Logger log = Logger
            .getLogger(GenericWebServiceCommunicationStrategy.class.getName());

    private void checkSupportedReferenceType(Reference r) throws RevocationProxyException {
        URI type = r.getReferenceType();
        if(type!=null) {
            String s = type.toString();
            if("http".equals(s) || "https".equals(s) || "url".equals(s)) {
                return;
            }
        }
        throw new RevocationProxyException("Only 'url', 'http' and 'https' ReferenceTypes are supported in WebServiceCommunicationStrategy - was : " + type);
    }

    private URI getRevocationServiceURI(Reference r) throws RevocationProxyException {
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
    
    private RevocationMessage getRevocationMessageFromServer(URI uri, JAXBElement<?> payload, boolean post) throws RevocationProxyException {
    	
        ObjectFactory of = new ObjectFactory();

        Client client = Client.create();
//        client.setFollowRedirects(false);
        client.setConnectTimeout(15000);
        client.setReadTimeout(30000);
        //System.out.println("contacting "+uri);
        WebResource revocationResource = client.resource(uri);

        ClientResponse cresp = null;
        if(post) cresp = revocationResource.post(ClientResponse.class, payload);
        else
        	cresp = revocationResource.get(ClientResponse.class);
        	
        int status = cresp.getStatus();
        if(status != 200){
        	throw new RevocationProxyException("Revocation Authority seems to be unavailable. Came back with the http status: "+status);
        }        
        
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
              	  cp.getContent().add(ent);
              	  result = of.createRevocationMessage();
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
        	ObjectFactory of = new ObjectFactory();
        	AttributeList attributes = of.createAttributeList();
        	XmlUtils.fixNestedContent(m.getCryptoParams());
        	for(Object o:m.getCryptoParams().getContent()){
        		if(o instanceof Attribute) {
//        		if(o instanceof JAXBElement){
//        			JAXBElement jax = (JAXBElement)o;
//					Attribute a = (Attribute)jax.getValue();
        		    Attribute a = (Attribute)o;
					if(a.getAttributeValue() != null){
						attributes.getAttributes().add(a);
					} else {
						a.setAttributeValue(BigInteger.ZERO);
						attributes.getAttributes().add(a);
					}
        		}
        	}
        	JAXBElement<AttributeList> payload = of.createAttributeList(attributes);

        	URI revocationServiceURI = null;
			try {
                URI revocationAuthorityParametersUID = m.getRevocationAuthorityParametersUID();
                String urlEncodedRevocationAuthorityParametersUID = URLEncoder
                        .encode(revocationAuthorityParametersUID.toString(),
                                "UTF-8");
                revocationServiceURI = new URI(this.getRevocationServiceURI(
                        nonRevocationEvidenceReference).toString()
                        + "/" + urlEncodedRevocationAuthorityParametersUID);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				throw new RevocationProxyException("Failed to create revocationServiceURI: "+e);
			}
            RevocationMessage response = getRevocationMessageFromServer(revocationServiceURI, payload, true);
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
        	URI revocationServiceURI = null;
			try {
                URI revocationAuthorityParametersUID = m.getRevocationAuthorityParametersUID();
                String urlEncodedRevocationAuthorityParametersUID = URLEncoder
                        .encode(revocationAuthorityParametersUID.toString(),
                                "UTF-8");
                revocationServiceURI = new URI(this.getRevocationServiceURI(
                        revocationInfoReference).toString()
                        + "/" + urlEncodedRevocationAuthorityParametersUID);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				throw new RevocationProxyException("Failed to create revocationServiceURI: "+e);
			}
            RevocationMessage response = getRevocationMessageFromServer(revocationServiceURI, null, false);
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
      throw new RevocationProxyException("Method not implemented");
    }

    @Override
    public CryptoParams getCurrentRevocationInformation(RevocationMessage m,
            Reference revocationInfoReference) throws RevocationProxyException {
      
        checkSupportedReferenceType(revocationInfoReference);

        try {
        	URI revocationServiceURI = null;
			try {
                URI revocationAuthorityParametersUID = m.getRevocationAuthorityParametersUID();
                String urlEncodedRevocationAuthorityParametersUID = URLEncoder
                        .encode(revocationAuthorityParametersUID.toString(),
                                "UTF-8");
                revocationServiceURI = new URI(this.getRevocationServiceURI(
                        revocationInfoReference).toString()
                        + "/" + urlEncodedRevocationAuthorityParametersUID);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				throw new RevocationProxyException("Failed to create revocationServiceURI: "+e);
			}
            RevocationMessage response = getRevocationMessageFromServer(revocationServiceURI, null, false);
        	
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

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

public class GenericWebServiceCommunicationStrategy implements RevocationProxyCommunicationStrategy {

    private final Logger log = Logger
            .getLogger(GenericWebServiceCommunicationStrategy.class.getName());

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
        List<URI> anyList = r.getReferences();
        if((anyList!=null) && (anyList.size()>0)) {
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
        this.log.info("contacting " + uri);
        WebResource revocationResource = client.resource(uri);

        ClientResponse cresp = null;
        if (post) {
            cresp = revocationResource.post(ClientResponse.class, payload);
        } else {
            cresp = revocationResource.get(ClientResponse.class);
        }

        InputStream responceInputStream = cresp.getEntityInputStream();
        RevocationMessage result = null;

        // ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        //
        // int nRead;
        // byte[] data = new byte[16384];
        //
        // try {
        // while ((nRead = responceInputStream.read(data, 0, data.length)) !=
        // -1) {
        // buffer.write(data, 0, nRead);
        // }
        // } catch (IOException ex) {
        // throw new RevocationProxyException(ex);
        // }
        //
        // try {
        // buffer.flush();
        // } catch (IOException ex) {
        // throw new RevocationProxyException(ex);
        // }
        // String string = new String(buffer.toByteArray());
        // this.log.info(">>>>>>>>> " + string);

        try {
            // Unmarshall returned message
            JAXBContext jCtx = JAXBContext.newInstance(RevocationMessage.class,
                    NonRevocationEvidence.class, NonRevocationEvidenceUpdate.class,
                    RevocationInformation.class);
            Object entity =
                    jCtx.createUnmarshaller().unmarshal(responceInputStream);

            // If the response is an JAXBElement, look at what type it is and
            // create a RevocationMessage
            // otherwise throw exception
            if(entity instanceof JAXBElement){
                JAXBElement ent = (JAXBElement)entity;
                Class declaredType = ent.getDeclaredType();
                if(declaredType == RevocationMessage.class){
                    result = (RevocationMessage)ent.getValue();
                }else if((declaredType == NonRevocationEvidence.class)
                        ||(declaredType == NonRevocationEvidenceUpdate.class) ||(declaredType
                                == RevocationInformation.class)){
                    CryptoParams cp = of.createCryptoParams();
                    cp.getAny().add(ent);
                    result = of.createRevocationMessage();
                    result.setCryptoParams(cp);
                }else {
                    throw new
                    RevocationProxyException("Unexpected JAXBElement received");
                }
            }else {
                throw new RevocationProxyException("Unknown datatype received");
            }
        } catch (JAXBException e) {
            throw new RevocationProxyException(e);
        }

        return result;
        //        return null;
    }

    @Override
    public CryptoParams requestRevocationHandle(RevocationMessage m,
            Reference nonRevocationEvidenceReference)
                    throws RevocationProxyException {

        this.checkSupportedReferenceType(nonRevocationEvidenceReference);

        try {
            ObjectFactory of = new ObjectFactory();
            AttributeList attributes = of.createAttributeList();
            for(Object o:m.getCryptoParams().getAny()){
                if(o instanceof JAXBElement){
                    JAXBElement jax = (JAXBElement)o;
                    Attribute a = (Attribute)jax.getValue();
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
            } catch (URISyntaxException ex) {
                throw new RevocationProxyException(
                        "Failed to create revocationServiceURI: " + ex);
            } catch (UnsupportedEncodingException ex) {
                throw new RevocationProxyException(
                        "Failed to encode revocation paramters UID: " + ex);
            }
            RevocationMessage response = this.getRevocationMessageFromServer(revocationServiceURI, payload, true);
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

        this.checkSupportedReferenceType(revocationInfoReference);

        try {
            URI revocationAuthorityParametersUID = m
                    .getRevocationAuthorityParametersUID();
            String urlEncodedRevocationAuthorityParametersUID = URLEncoder
                    .encode(revocationAuthorityParametersUID.toString(),
                            "UTF-8");
            URI revocationServiceURI = this.getRevocationServiceURI(
                    revocationInfoReference);
            String revocationServiceUriStr = revocationServiceURI.toString();
            String uriStr = revocationServiceUriStr
                    + "/"
                    + urlEncodedRevocationAuthorityParametersUID;
            URI uri = URI.create(uriStr);
            RevocationMessage response = this.getRevocationMessageFromServer(
                    uri, null, false);
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
        } catch (UnsupportedEncodingException ex) {
            throw new RevocationProxyException(
                    "Failed to encode revocation paramters UID: " + ex);
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

        this.checkSupportedReferenceType(revocationInfoReference);

        try {
            URI revocationAuthorityParametersUID = m
                    .getRevocationAuthorityParametersUID();
            String urlEncodedRevocationAuthorityParametersUID = URLEncoder
                    .encode(revocationAuthorityParametersUID.toString(),
                            "UTF-8");

            String revocationInfoReferenceStr = this.getRevocationServiceURI(revocationInfoReference).toString();
            String uriStr = revocationInfoReferenceStr + "/"
                    + urlEncodedRevocationAuthorityParametersUID;
            URI uri = URI.create(uriStr);
            RevocationMessage response = this.getRevocationMessageFromServer(
                    uri, null, false);
            return response.getCryptoParams();
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();
            // is 'null' allowed ? - handling taken from old code...
            if(clientResponse.getStatus()== Status.NO_CONTENT.ordinal()) {
                throw new RevocationProxyException("Response from RevocationServer should not be empty ?");
            } else {
                throw new RevocationProxyException("Failed to get RevocationMessage from server : " + e);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RevocationProxyException(
                    "Failed to encode revocation paramters UID: " + ex);
        }
    }

}

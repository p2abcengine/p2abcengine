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

package eu.abc4trust.services.issuer;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.util.XmlUtils;

public abstract class AbstractTestFactory {

    protected Builder getHttpBuilder(String string, String baseUrl) {
        Client client = Client.create();
        Builder resource = client.resource(baseUrl + string)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);
        return resource;
    }

    public IssuancePolicyAndAttributes loadIssuancePolicyAndAttributes(
            String issuancePolicyAndAttributesFile) throws IOException,
            JAXBException, SAXException {
        InputStream is = FileSystem
                .getInputStream(issuancePolicyAndAttributesFile);
        IssuancePolicyAndAttributes issuancePolicyAndAttributes = (IssuancePolicyAndAttributes) XmlUtils
                .getObjectFromXML(is, false);
        return issuancePolicyAndAttributes;
    }
}

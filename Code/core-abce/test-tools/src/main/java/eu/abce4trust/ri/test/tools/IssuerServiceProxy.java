//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abce4trust.ri.test.tools;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.guice.ProductionModule.CryptoEngine;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

public class IssuerServiceProxy extends ServiceProxy {

    private final ObjectFactory of = new ObjectFactory();
    private final Client client = Client.create();
    private final String issuerUrl;

    public IssuerServiceProxy(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public IssuanceMessage issuanceStart(String serverMethod, String issuanceKey)
            throws Exception {
        String urlStr = this.issuerUrl + "/issue/" + serverMethod + "/"
                + issuanceKey;
        Builder issueStartResource = this.client.resource(urlStr)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

        IssuanceMessage server_im = null;
        server_im = this.postMessage(urlStr, issueStartResource,
                IssuanceMessage.class);

        System.out.println(" - initial message - server : " + server_im);
        System.out.println(" - initial message - server : \n"
                + XmlUtils.toXml(this.of.createIssuanceMessage(server_im)));
        return server_im;
    }

    public IssuanceMessage issuanceStep(IssuanceMessage user_im,
            CryptoEngine engine) throws Exception {
        String urlStr = this.issuerUrl + "/issue/step/?UserCryptoEngine="
                + engine.toString();
        Builder issueStepResource = this.client.resource(urlStr)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

        IssuanceMessage server_im = this.postMessage(urlStr, issueStepResource,
                IssuanceMessage.class, this.of.createIssuanceMessage(user_im));

        System.out.println(" - initial message - server : \n"
                + XmlUtils.toXml(this.of.createIssuanceMessage(server_im)));
        return server_im;
    }

}

//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.14                                           *
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

package eu.abc4trust.ri.ui.user.test;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.LoggingFilter;

import eu.abc4trust.returnTypes.UiManageCredentialData;


public class UiManageCredentialDataTest {
    
    private static final String userAbceEngineServiceBaseUrl = "http://localhost:9300/idselect-user-service/user"; //$NON-NLS-1$

    public static void main(String[] args) throws Exception {
      
        // get data
        String session = "TestUi" + System.currentTimeMillis();
        Client client = Client.create();
        client.addFilter(new LoggingFilter());

        Builder getUIData =
            client.resource(userAbceEngineServiceBaseUrl + "/getUiManageCredentialData/" + session) //$NON-NLS-1$
            .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
    
        UiManageCredentialData data = getUIData.get(UiManageCredentialData.class);
        System.out.println("Got data : " + data);
        System.out.println("Map<URI, Boolean> with revoke status of credential : " + data.revokedCredentials);

        // delete credential
        URI firstCredentialUID = data.revokedCredentials.keySet().iterator().next();

        Builder deleteCredential =
            client.resource(userAbceEngineServiceBaseUrl + "/deleteCredential") //$NON-NLS-1$
            .type(MediaType.TEXT_PLAIN);
        deleteCredential.post(firstCredentialUID.toString());
    }
}
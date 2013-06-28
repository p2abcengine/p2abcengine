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
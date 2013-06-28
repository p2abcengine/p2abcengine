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

package eu.abce4trust.ri.test.tools;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

public class UserServiceProxy extends ServiceProxy {

    private final ObjectFactory of = new ObjectFactory();
    private final String userUrl;
    private final Client client = Client.create();

    public UserServiceProxy(String userUrl) {
        this.userUrl = userUrl;
    }

    void unlockSmartcard(String sessionId) {
        String urlString = this.userUrl + "/unlockSmartcards/" + sessionId;
        Builder userserviceResource = this.client.resource(urlString)
                .type(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_PLAIN);

        this.postMessage(urlString, userserviceResource, "5678");
    }

    void issuanceProtocolStep(IssuanceMessage server_im, String sessionId) {
        String issuanceProtocolStepUrlString = this.userUrl
                + "/issuanceProtocolStep/" + sessionId
                + "?startRequest=fakeUrl&stepRequest=fakeUrl";
        Builder issuanceProtocolStepUserserviceResource = this
                .prepareIssuanceStep(issuanceProtocolStepUrlString);

        String returnStatus = this.postMessage(issuanceProtocolStepUrlString,
                issuanceProtocolStepUserserviceResource, String.class,
                this.of.createIssuanceMessage(server_im));

        if (returnStatus == "GO AHEAD CALL NEW UI FOR ISSUANCE") {
            this.exit("Wrong responce");
        }
    }

    IssuanceMessage issuanceProtocolStepIssuanceMessage(
            IssuanceMessage server_im,
            String sessionId) {
        String issuanceProtocolStepUrlString = this.userUrl
                + "/issuanceProtocolStep/" + sessionId
                + "?startRequest=fakeUrl&stepRequest=fakeUrl";
        Builder issuanceProtocolStepUserserviceResource = this
                .prepareIssuanceStep(issuanceProtocolStepUrlString);

        IssuanceMessage user_im = this.postMessage(
                issuanceProtocolStepUrlString,
                issuanceProtocolStepUserserviceResource, IssuanceMessage.class,
                this.of.createIssuanceMessage(server_im));
        return user_im;
    }

    UiIssuanceReturn getUiIssuanceArguments(String sessionId)
            throws IOException, JsonParseException, JsonMappingException {
        String urlString = this.userUrl + "/getUiIssuanceArguments/"
                + sessionId;
        Builder userserviceResource = this
                .prepareUIIssuanceArguments(urlString);

        UiIssuanceArguments uiSelectionString = this.getMessage(urlString,
                userserviceResource, UiIssuanceArguments.class);

        UiIssuanceReturn uiIssuanceReturn = this.uiSelection(uiSelectionString);
        return uiIssuanceReturn;
    }

    void setUiIssuanceArguments(String sessionId,
            UiIssuanceReturn uiIssuanceReturn) {
        String urlString;
        Builder userserviceResource;
        urlString = this.userUrl + "/setUiIssuanceReturn/" + sessionId;
        userserviceResource = this.prepareIssuanceStepSelect(urlString);

        ClientResponse r = this.postMessage(urlString, userserviceResource,
                ClientResponse.class, uiIssuanceReturn);

        int responceCode = 204; // 200

        if (r.getStatus() != responceCode) {
            this.exit("Recieved an error code from the user-service: "
                    + r.getStatus());
        }
    }

    private UiIssuanceReturn uiSelection(UiIssuanceArguments uiSelectionString)
            throws IOException, JsonParseException, JsonMappingException {
        System.out.println(uiSelectionString);

        UiIssuanceReturn uiIssuanceReturn = new UiIssuanceReturn();
        uiIssuanceReturn.chosenIssuanceToken = 0;
        uiIssuanceReturn.chosenPseudonymList = 0;

        return uiIssuanceReturn;
    }

    private Builder prepareIssuanceStep(String urlString) {
        Builder userserviceResource;
        userserviceResource = this.client.resource(urlString)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);
        return userserviceResource;
    }

    private Builder prepareUIIssuanceArguments(String urlString) {
        Builder userserviceResource;
        userserviceResource = this.client.resource(urlString)
                .type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML);
        return userserviceResource;
    }

    private Builder prepareIssuanceStepSelect(String urlString) {
        Builder userserviceResource;
        userserviceResource = this.client.resource(urlString)
                .type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML);
        return userserviceResource;
    }

    public IssuanceMessage issuanceProtocolStepSelect(
            IssuanceMessage server_im, String sessionId) throws Exception {
        String issuanceProtocolStepSelectUrlString = this.userUrl
                + "/issuanceProtocolStepSelect/" + sessionId
                + "?startRequest=fakeUrl&stepRequest=fakeUrl";
        Builder issuanceProtocolStepSelectUserserviceResource = this
                .prepareIssuanceProtocolStepSelect(issuanceProtocolStepSelectUrlString);

        IssuanceMessage user_im = this
                .postMessage(issuanceProtocolStepSelectUrlString,
                        issuanceProtocolStepSelectUserserviceResource,
                        IssuanceMessage.class);

        System.out.println(" - initial message - client - created ");
        System.out.println(" - initial message - client - created " + user_im);
        System.out
        .println(" - initial message - client : "
                + XmlUtils.toXml(
                        this.of.createIssuanceMessage(user_im),
                        false));
        return user_im;
    }

    private Builder prepareIssuanceProtocolStepSelect(
            String issuanceProtocolStepSelectUrlString) {
        Builder userserviceResource;
        userserviceResource = this.client.resource(issuanceProtocolStepSelectUrlString)
                .type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_XML);
        return userserviceResource;
    }
}

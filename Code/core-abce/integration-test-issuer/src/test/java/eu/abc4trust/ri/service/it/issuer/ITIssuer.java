//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.service.it.issuer;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.util.XmlUtils;

public class ITIssuer extends AbstractIT {

  private static final String USERNAME = "defaultUser";

    public ITIssuer() {
        System.out.println("ITIssuer");
    }

    URI scope = URI.create("urn:identitycard:registration");
    @Test
    public void testIssuanceIdcard_Alice_IdemixOnly() throws Exception {
        System.out.println("---- testIssuanceIdcard_Alice Idemix----");

        initHelper(CryptoTechnology.IDEMIX, "alice_idemix_", "identitycard", scope);
        this.runIssuance("start", "IDCARD_IDEMIX?user=alice");
    }

//    @Test
    public void testIssuanceIdcard_Alice_UProveOnly() throws Exception {
        System.out.println("---- testIssuanceIdcard_Alice UProve----");

        initHelper(CryptoTechnology.UPROVE, "alice_uprove_", "identitycard", scope);
        this.runIssuance("start", "IDCARD_UPROVE?user=alice");
    }


//    @Test
    public void testIssuanceIdcard_Stewart() throws Exception {
        System.out.println("---- testIssuanceIdcard_Stewart ----");

        initHelper(CryptoTechnology.IDEMIX, "stewart_idemix", "identitycard", scope);
        this.runIssuance("start", "idcard?user=stewart");
    }


    // @Test
    public void testHotelCredentials_NotStudent() throws Exception {
        System.out.println("---- testHotelCredentials_NotStudent ----");
        UserHelper.resetInstance();
//        initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "hotel_alice");

        // idcard
        this.runIssuance("start", "idcard?user=alice");
        // passport ch
        this.runIssuance("start", "passport?variant=ch&user=alice");
        // creditcards
        this.runIssuance("start", "creditcard?variant=visa&user=alice");
        this.runIssuance("start", "creditcard?variant=amex&user=alice");
    }

    // @Test
    public void testHotelCredentials_Student() throws Exception {
        System.out.println("---- testHotelCredentials_Student ----");
        UserHelper.resetInstance();
//        initHelper(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "hotel_stewart");

        // idcard
        this.runIssuance("start", "idcard?user=stewart");
        // passport ch
        this.runIssuance("start", "passport?variant=ch&user=stewart");
        // creditcards
        this.runIssuance("start", "creditcard?variant=visa&user=stewart");
        this.runIssuance("start", "creditcard?variant=amex&user=stewart");
        // studentcard
        this.runIssuance("start", "studentcard?user=stewart");
    }


    
    // TODO: User abstract method
    @SuppressWarnings("unused")
	private void runIssuance(String issuanceKey) throws Exception {

        System.out.println("- run issuance with key : " + issuanceKey);

        Client client = Client.create();
        // client.addFilter(new LoggingFilter());

        Builder issueStartResource =
                client.resource(baseUrl + "/issue/start/" + issuanceKey)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

        IssuanceMessage server_im = issueStartResource.post(IssuanceMessage.class);
        System.out.println(" - initial message - server : " + server_im);
        System.out.println(" - initial message - server : "
                + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));

        System.out.println("\nENGINE : " + UserHelper.getInstance().getEngine());
        
        IssuMsgOrCredDesc user_im =
                UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
        System.out.println(" - initial message - client - created");
        System.out.println(" - initial message - client : "
                + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), true));

        int stepCount = 1;
        boolean lastmessage = false;
        while (!lastmessage) {
            Builder issueStepResource =
                    client.resource(baseUrl + "/issue/step").type(MediaType.APPLICATION_XML)
                    .accept(MediaType.TEXT_XML);

            // send to server and receive new im
            System.out.println(" - contact server");
            server_im =
                    issueStepResource.post(IssuanceMessage.class, of.createIssuanceMessage(user_im.im));
            System.out.println(" - got response");
            System.out.println(" - step message - server : " + stepCount + " : "
                    + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));

            // process im
            user_im = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, server_im);
            System.out.println(" - step message - client :" + stepCount);

            lastmessage = (user_im.cd != null);
            if (!lastmessage) {
                System.out.println(" - initial message - step : " + stepCount + " : "
                        + XmlUtils.toXml(of.createIssuanceMessage(user_im.im), false));
            }
        }
        System.out.println(" - done...");
        System.out.println(" - done : credentialDescription : "
                + XmlUtils.toXml(of.createCredentialDescription(user_im.cd), false));

        System.out.println("Show Credential");
        System.out.println("Show Credential : credentialDescription UID : "
                + user_im.cd.getCredentialUID());

        Credential cred =
                UserHelper.getInstance().credentialManager.getCredential(USERNAME, user_im.cd.getCredentialUID());
        System.out.println("Show Credential " + cred);
        System.out.println("Show Credential " + cred.getCredentialDescription().getSecretReference());


        CredentialManager credentialManager = UserHelper.getInstance().credentialManager;
        System.out.println("Show Credential Manager : " + credentialManager);

    }

}

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

import java.util.Random;

import eu.abc4trust.guice.ProductionModule.CryptoEngine;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.xml.IssuanceMessage;

public class HeadlessUser {

    private final IssuerServiceProxy issuerService;
    private final UserServiceProxy userService;

    public HeadlessUser(String issuerUrl, String userUrl) {
        this.issuerService = new IssuerServiceProxy(issuerUrl);
        this.userService = new UserServiceProxy(userUrl);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String issuerUrl = "http://localhost:9500";
        String userserviceUrl = "http://localhost:9300/idselect-user-service/user";
        switch (args.length) {
        case 0:
            break;
        case 2:
            issuerUrl = args[0];
            userserviceUrl = args[1];
            break;
        default:
            printUsage();
            System.exit(-1);
        }

        HeadlessUser headlessUser = new HeadlessUser(issuerUrl, userserviceUrl);
        headlessUser
        .runIssuance(
                "startSchool",
                "issuanceKey?pilotUserNumber=1111&UserCryptoEngine=IDEMIX",
                CryptoEngine.IDEMIX);

    }


    public void runIssuance(String serverMethod, String issuanceKey,
            CryptoEngine userCryptoEngine)
                    throws Exception {


        System.out.println("- run issuance with key : " + issuanceKey);

        IssuanceMessage server_im = this.issuerService.issuanceStart(
                serverMethod, issuanceKey);

        Random random = new Random();
        String sessionId = "" + Math.abs(random.nextInt());

        this.userService.unlockSmartcard(sessionId);

        this.userService.issuanceProtocolStep(server_im, sessionId);

        UiIssuanceReturn uiIssuanceReturn = this.userService
                .getUiIssuanceArguments(sessionId);

        this.userService.setUiIssuanceArguments(sessionId,
                uiIssuanceReturn);

        IssuanceMessage user_im = this.userService
                .issuanceProtocolStepSelect(
                        server_im, sessionId);

        int stepCount = 1;
        boolean lastmessage = false;
        while (!lastmessage) {
            server_im = this.issuerService.issuanceStep(user_im,
                    userCryptoEngine);
            //            urlString = this.issuerUrl + "/issue/step?UserCryptoEngine="
            //                    + userCryptoEngine;
            //            Builder issueStepResource = client.resource(urlString)
            //                    .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);
            //
            //            // send to server and receive new im
            //            System.out.println(" - contact server");
            //            server_im = this.postMessage(urlString, issueStepResource,
            //                    IssuanceMessage.class, of.createIssuanceMessage(user_im));
            //            System.out.println(" - got response");
            //            System.out
            //            .println(" - step message - server : "
            //                    + stepCount
            //                    + " : "
            //                    + XmlUtils.toXml(
            //                            of.createIssuanceMessage(server_im), false));

            user_im = this.userService.issuanceProtocolStepIssuanceMessage(
                    server_im, sessionId);
            // process in
            // user_im = UserHelper.getInstance().getEngine()
            // .issuanceProtocolStep(server_im);
            System.out.println(" - step message - client :" + stepCount);
        }
        System.out.println(" - done...");
    }


    private static void printUsage() {
        // TODO Auto-generated method stub

    }
}

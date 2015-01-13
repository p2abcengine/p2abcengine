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

package eu.abc4trust.ri.service.it.issuer;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.SoftwareSmartcard;

public class ITPatrasPilot extends AbstractIT {

    public ITPatrasPilot() {
        System.out.println("ITIssuer");
    }
    private static final String USERNAME = "defaultUser";
    
    private SoftwareSmartcard setupCryptoEngines(CryptoTechnology clientTechnology,
            int matNumber) throws Exception {

        String storagePrefix = "student_" + clientTechnology.toString().toLowerCase() + "_" + matNumber;

        storagePrefix += "_" + clientTechnology.toString().toLowerCase() + "_";

        URI scope = new URI("urn:patras:registration");
        return initHelper(clientTechnology, storagePrefix, "patras", scope);
    }

    private void issuePatrasCredentials(CryptoTechnology clientTechnology, int matNumber) throws Exception {
        System.out.println("-- issuePatrasCredentials - clientTechnology : " + clientTechnology + " - matNumber : " + matNumber);

        String scope = "urn:patras:registration";
//        this.initPseudonym(softwareSmartcard, scope, matNumber);

        // issue university credential
        this.runIssuance("startPatras", "UNIVERSITY_" + clientTechnology + "?matriculationnumber=" + matNumber, scope);

        String courceScope = "urn:patras:evaluation";
        // issue course credential
        this.runIssuance("startPatras", "COURSE_" + clientTechnology + "?matriculationnumber=" + matNumber, courceScope);

        List<URI> list = UserHelper.getInstance().credentialManager.listCredentials(USERNAME);
        System.out.println("# of credentials : " + list.size() + " : " + list);

    }

    private void verifyPatrasCredentials(CryptoTechnology clientTechnology, int matNumber) throws Exception {
        System.out.println("-- verifyPatrasCredentials - clientTechnology : " + clientTechnology + " - matNumber : " + matNumber);

//        this.setupCryptoEngines(clientTechnology, matNumber);

        //
        String scope = "urn:patras:registration";
        this.runVerification(clientTechnology, "presentationPolicyPatrasUniversityLogin.xml", true, scope);

        //
        String courceScope = "urn:patras:evaluation";
        this.runVerification(clientTechnology, "presentationPolicyPatrasCourseEvaluation.xml", true, courceScope);

    }

//      @Test
      public void testStudent_42_idemix() throws Exception {
          setupCryptoEngines(CryptoTechnology.IDEMIX, 42);
          
          issuePatrasCredentials(CryptoTechnology.IDEMIX, 42);
          verifyPatrasCredentials(CryptoTechnology.IDEMIX, 42);
      }

//      @Test
      public void testStudent_42_uprove() throws Exception {
          setupCryptoEngines(CryptoTechnology.UPROVE, 42);
          issuePatrasCredentials(CryptoTechnology.UPROVE, 42);
          verifyPatrasCredentials(CryptoTechnology.UPROVE, 42);
      }

}

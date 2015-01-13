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

public class ITSoderhamnPilot extends AbstractIT {

    public ITSoderhamnPilot() {
        System.out.println("ITIssuer");
    }
    private static final String USERNAME = "defaultUser";

    private SoftwareSmartcard setupCryptoEngines(CryptoTechnology clientTechnology,
            String pupil) throws Exception {

      String storagePrefix = pupil.toLowerCase();
        storagePrefix += "_" + clientTechnology.toString().toLowerCase();
        URI scope = new URI("urn:soderhamn:registration");
        return initHelper(clientTechnology, storagePrefix, "soderhamn", scope);
    }


    private void issueSoederhamnCredentials(CryptoTechnology clientTechnology, String pupil) throws Exception {
        System.out.println("-- issueSoederhamnCredentials - clientTechnology : " + clientTechnology + " - pupil : " + pupil);
        SoftwareSmartcard smartcard = setupCryptoEngines(clientTechnology, pupil);

        String soderhamnScope = "urn:soderhamn:registration";

//        this.initPseudonym(clientTechnology, soderhamnScope, 42);

        // issue School credential
        this.runIssuance("startSoderhamn", "SCHOOL_" + clientTechnology + "?pupil=" + pupil, soderhamnScope);

        // issue Subject credential
        this.runIssuance("startSoderhamn", "SUBJECT_" + clientTechnology + "?pupil=" + pupil, soderhamnScope);

        List<URI> list = UserHelper.getInstance().credentialManager.listCredentials(USERNAME);
        System.out.println("# of credentials : " + list.size() + " : " + list);

    }

    private void verifySoederhamnCredentials(CryptoTechnology clientTechnology, String pupil) throws Exception {
        System.out.println("-- issueSoederhamnCredentials - clientTechnology : " + clientTechnology + " - pupil : " + pupil);
        this.setupCryptoEngines(clientTechnology, pupil);

        // School credential
        System.out.println("Present Soderhamn Smartcard Pseudonym!");
        String soderhamnScope = "urn:soderhamn:registration";
        this.runVerification(clientTechnology, "presentationPolicySoderhamnSchool.xml", true, soderhamnScope);

        // Subject credential
        String frenchScope = "urn:soderhamn:restrictedarea:french";
        System.out.println("Present Soderhamn Subject Credential - pseudonym being established!");
        this.runVerification(clientTechnology, "presentationPolicyRASubjectMustBeFrench.xml", true, frenchScope);

        // Subject pseudonym
        System.out.println("Present Soderhamn Subject Credential - pseudonym is used!");
        this.runVerification(clientTechnology, "presentationPolicyRASubjectMustBeFrench.xml", true, frenchScope);

        // Subject credential - english not satisfied
        String englishScope = "urn:soderhamn:restrictedarea:english";
        System.out.println("Present Soderhamn Subject Credential - Not Satisfied!!");
        this.runVerification(clientTechnology, "presentationPolicyRASubjectMustBeEnglish.xml", false, englishScope);

    }

//    @Test
    public void testPupil_Emil_Idemix() throws Exception {
        this.issueSoederhamnCredentials(CryptoTechnology.IDEMIX, "Emil");
        this.verifySoederhamnCredentials(CryptoTechnology.IDEMIX, "Emil");
    }

//    @Test
    public void testPupil_Emil_UProve() throws Exception {
        this.issueSoederhamnCredentials(CryptoTechnology.UPROVE, "Emil");
        this.verifySoederhamnCredentials(CryptoTechnology.UPROVE, "Emil");
    }

}

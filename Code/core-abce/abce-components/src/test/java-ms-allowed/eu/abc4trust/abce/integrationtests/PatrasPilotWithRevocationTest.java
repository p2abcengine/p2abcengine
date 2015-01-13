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

// * Licensed Materials - Property of IBM, Miracle A/S, *
// * Alexandra Instituttet A/S, and Microsoft *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * (C) Copyright Microsoft Corp. 2012. All Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.PresentationToken;

/**
 * Patras scenario.
 */
public class PatrasPilotWithRevocationTest {
  private static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUniversityLogin.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY_WITH_REVOCATION =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml";

  private static final String ISSUANCE_POLICY_PATRAS_UNIVERSITY =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourseWithRevocation.xml";

  private static final String ISSUANCE_POLICY_PATRAS_COURSE =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml";

  private static final String PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasCourseEvaluation.xml";

  private static final String PRESENTATION_POLICY_PATRAS_UNIVERSITY =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasUnivCredential.xml";

  @SuppressWarnings("unused")
  private static final String PRESENTATION_POLICY_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/presentationPolicyPatrasTombola.xml";

  private static final String ISSUANCE_POLICY_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasTombola.xml";

  private static final String CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA =
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasTombola.xml";



  // TODO: Backup and restore of attendance credentials.

  private static final String COURSE_UID = "23330E";
  @SuppressWarnings("unused")
  private static final String SHA256 = "urn:abc4trust:1.0:encoding:string:sha-256";
  private static final String NAME = "John";
  private static final String LASTNAME = "Doe";
  private static final String UNIVERSITYNAME = "Patras";
  private static final String DEPARTMENTNAME = "CS";
  private static final int MATRICULATIONNUMBER = 1235332;
  @SuppressWarnings("unused")
  private static final String ATTENDANCE_UID = "attendance";
  @SuppressWarnings("unused")
  private static final String LECTURE_UID = "lecture";


  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patrasPilotIdemixTest() throws Exception {
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");
    int keyLength = 1024;
    standardPatrasScenario(1, keyLength, cl_technology, cl_technology, cl_technology,
        revocationTechnology);
  }

  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patrasPilotUProveTest() throws Exception {
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");
    int keyLength = 1024;
    standardPatrasScenario(1, keyLength, uprove_technology, uprove_technology, uprove_technology,
        revocationTechnology);
  }

  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patrasPilotCombinedTechnologyTest() throws Exception {
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");

    int keyLength = 1024;
    standardPatrasScenario(1, keyLength, cl_technology, uprove_technology, cl_technology,
        revocationTechnology);
  }

  @Test(timeout = TestConfiguration.TEST_TIMEOUT)
  public void patrasPilotExtendedRevocationTest() throws Exception {
    URI cl_technology = Helper.getSignatureTechnologyURI("cl");
    URI uprove_technology = Helper.getSignatureTechnologyURI("brands");
    URI revocationTechnology = Helper.getRevocationTechnologyURI("cl");

    int keyLength = 1024;
    extendedPatrasScenario(2, keyLength, cl_technology, uprove_technology, cl_technology,
        revocationTechnology);
  }


  private RevocationHelper standardPatrasScenario(int numberOfUsers, int keyLength,
      URI firstCredentialTechnology, URI secondCredentialTechnology, URI thirdCredentialTechnology,
      URI revocationTechnology) throws Exception {

    RevocationHelper revocationHelper =
        new RevocationHelper(numberOfUsers, keyLength, revocationTechnology);

    revocationHelper.setupUsers();

    revocationHelper.setupIssuer(firstCredentialTechnology, ISSUANCE_POLICY_PATRAS_UNIVERSITY,
        CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY_WITH_REVOCATION);
    revocationHelper.setupIssuer(secondCredentialTechnology, ISSUANCE_POLICY_PATRAS_COURSE,
        CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION);
    // revocationHelper.setupIssuer(0, third, ...);

    CredentialDescription[] courseCredDesc = new CredentialDescription[numberOfUsers];
    CredentialDescription[] univCredDesc = new CredentialDescription[numberOfUsers];
    for (int i = 0; i < numberOfUsers; i++) {
      // Step 1. Login with pseudonym.
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      System.out.println(">> Login with pseudonym of user " + i + ".");
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      revocationHelper.loginWithPseudonym(i, PRESENTATION_POLICY_PATRAS_UNIVERSITY_LOGIN);

      // Step 2. Get university credential.
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      System.out.println(">> Get university credential for user " + i + ".");
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      univCredDesc[i] =
          revocationHelper.issueAndStoreCredential(i, populateUniveristyAttributes(),
              CREDENTIAL_SPECIFICATION_PATRAS_UNIVERSITY_WITH_REVOCATION,
              ISSUANCE_POLICY_PATRAS_UNIVERSITY);

      // Step 3. Get course credential.
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      System.out.println(">> Get course credential for user " + i + ".");
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      courseCredDesc[i] =
          revocationHelper
              .issueAndStoreCredential(i, populateCourseAttributes(),
                  CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION,
                  ISSUANCE_POLICY_PATRAS_COURSE);

      // Step 4. Verify against course evaluation using the course credential.
      PresentationToken pt =
          revocationHelper.logIntoCourseEvaluation(i, PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);
      assertNotNull(pt);

      pt = revocationHelper.logIntoCourseEvaluation(i, PRESENTATION_POLICY_PATRAS_UNIVERSITY);
      assertNotNull(pt);

    }


    // Do the tombola thing
    if (thirdCredentialTechnology != null) {
      // Create the issuer parameters and stores them to the appropriate locations (e.g.,
      // KeyManagers and user storage)
      revocationHelper.setupIssuer(thirdCredentialTechnology, ISSUANCE_POLICY_PATRAS_TOMBOLA,
          CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA);
    }


    for (int j = 0; j < numberOfUsers; j++) {

      int userToBeTested = j;

      // Revoke course credential
      revocationHelper.testRevocation(userToBeTested, courseCredDesc[userToBeTested],
          PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);

      // Check that we cannot use the credential
      try {
        revocationHelper.logIntoCourseEvaluation(userToBeTested,
            PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);
        Assert
            .fail("Should not have gone all the way - exception expected when logging in with revoked credential.");
      } catch (RuntimeException e) {
        // ok - expected to fail
        assertTrue(e.getMessage().contains("Cannot generate presentationToken"));
        System.out
            .println("Presentation token cannot be created (as the credential has been revoked.");
      }
      
      // Re-issue the credential
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      System.out.println(">> Issue course credential to user " + userToBeTested + ".");
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      courseCredDesc[userToBeTested] =
          revocationHelper
              .issueAndStoreCredential(userToBeTested, populateCourseAttributes(),
                  CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION,
                  ISSUANCE_POLICY_PATRAS_COURSE);

      // Check that we can once again use the credential
      try {
        revocationHelper.logIntoCourseEvaluation(userToBeTested,
            PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);        
      } catch (RuntimeException e) {
        // failure - did not expect to fail
        Assert.fail("Should not have gone all the way - exception expected when logging in with revoked credential.");
      }

      // Update the NRE of all users
      revocationHelper.updateNonRevocationEvidence(-1);

      // Issue the tombola credential
      if (thirdCredentialTechnology != null) {
        Map<String, Object> atts = new HashMap<String, Object>();
        revocationHelper.issueAndStoreCredential(userToBeTested, atts,
            CREDENTIAL_SPECIFICATION_PATRAS_TOMBOLA, ISSUANCE_POLICY_PATRAS_TOMBOLA);
      }


      // Revoke the newly issued credential
      revocationHelper.testRevocation(userToBeTested, courseCredDesc[userToBeTested],
          PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);

      try {
        revocationHelper.logIntoCourseEvaluation(userToBeTested,
            PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);
        Assert
            .fail("Should not have gone all the way - exception expected when logging in with revoked credential.");
      } catch (RuntimeException e) {
        // ok - expected to fail
        assertTrue(e.getMessage().contains("Cannot generate presentationToken"));
        System.out
            .println("Presentation token cannot be created (as the credential has been revoked.");
      }
    }


    return revocationHelper;
  }


  private void extendedPatrasScenario(int numberOfUsers, int keyLength,
      URI firstCredentialTechnology, URI secondCredentialTechnology, URI thirdCredentialTechnology,
      URI revocationTechnology) throws Exception {

    RevocationHelper revocationHelper =
        standardPatrasScenario(numberOfUsers, keyLength, firstCredentialTechnology,
            secondCredentialTechnology, thirdCredentialTechnology, revocationTechnology);

    // Update the NRE of all users
    revocationHelper.updateNonRevocationEvidence(-1);

    // Re-issue the credential
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(">> Issue course credential to user " + 1 + ".");
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    CredentialDescription courseCredDesc =
        revocationHelper.issueAndStoreCredential(1, populateCourseAttributes(),
            CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION, ISSUANCE_POLICY_PATRAS_COURSE);

    // Revoke credential
    revocationHelper
        .testRevocation(1, courseCredDesc, PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);

    // Login to the system
    try {
      revocationHelper.logIntoCourseEvaluation(1, PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);
      Assert
          .fail("Should not have gone all the way - exception expected when logging in with revoked credential.");
    } catch (RuntimeException e) {
      // ok - expected to fail
      assertTrue(e.getMessage().contains("Cannot generate presentationToken"));
      System.out
          .println("Presentation token cannot be created (as the credential has been revoked.");
    }


    // Re-issue the course credential for the first user
    courseCredDesc =
        revocationHelper.issueAndStoreCredential(0, populateCourseAttributes(),
            CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION, ISSUANCE_POLICY_PATRAS_COURSE);

    // Revoke credential AGAIN
    revocationHelper.testRevocation(0, courseCredDesc, null);

    // Re-issue the credential
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(">> Issue course credential to user " + 0 + ".");
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    courseCredDesc =
        revocationHelper.issueAndStoreCredential(0, populateCourseAttributes(),
            CREDENTIAL_SPECIFICATION_PATRAS_COURSE_WITH_REVOCATION, ISSUANCE_POLICY_PATRAS_COURSE);

    // Login using the new credential
    revocationHelper.logIntoCourseEvaluation(0, PRESENTATION_POLICY_PATRAS_COURSE_EVALUATION);

    // Update the NRE of all users
    revocationHelper.updateNonRevocationEvidence(-1);
  }



  private Map<String, Object> populateCourseAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
    // att.put(REVOCATION_HANDLE_STR,
    // URI.create("urn:patras:revocation:handle2"));
    return att;
  }


  private Map<String, Object> populateUniveristyAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credUniv:firstname", NAME);
    att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
    // att.put(REVOCATION_HANDLE_STR,
    // URI.create("urn:patras:revocation:handle1"));
    att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
    att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
    att.put("urn:patras:credspec:credUniv:matriculationnr", MATRICULATIONNUMBER);
    return att;
  }

}

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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.14 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// * *
// * This file is licensed under the Apache License, Version 2.0 (the *
// * "License"); you may not use this file except in compliance with *
// * the License. You may obtain a copy of the License at: *
// * http://www.apache.org/licenses/LICENSE-2.0 *
// * Unless required by applicable law or agreed to in writing, *
// * software distributed under the License is distributed on an *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY *
// * KIND, either express or implied. See the License for the *
// * specific language governing permissions and limitations *
// * under the License. *
// */**/****************************************************************

package eu.abc4trust.ri.servicehelper.revocation;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.revocation.RevocationHelper.RevocationReferences;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
public class RevocationHelperTest {
  @Before
  public void alwaysResetEngine() throws Exception {
    IssuanceHelper.resetInstance();
    RevocationHelper.resetInstance();
    UserHelper.resetInstance();
  }

  @Test(expected = IllegalStateException.class)
  public void test_notInit() throws Exception {
    IssuanceHelper.getInstance();
  }

  static ObjectFactory of = new ObjectFactory();

  public static final URI patrasRevocationAuthority = URI
      .create("urn:patras:revocationauthority:default");

  public static final String UNIVERSITY_IDEMIX = "UNIVERSITY_IDEMIX";
  public static final SpecAndPolicy universityIdemix = new SpecAndPolicy(UNIVERSITY_IDEMIX,
      CryptoTechnology.IDEMIX, null, 42, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml",
      patrasRevocationAuthority.toString());


  @Test
  public void testRevocationHelper() throws Exception {
    String fileStoragePrefix = null;
    if (new File("target").exists()) {
      fileStoragePrefix = "target/revocation-test/";
    } else {
      fileStoragePrefix = "service-helper/target/revocation-test/";
    }

    //
    IssuanceHelper.initInstance(1024, fileStoragePrefix, fileStoragePrefix,
        new SpecAndPolicy[] {universityIdemix}, new ArrayList<RevocationAuthorityParameters>());

    //
    URI revocationInfoReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/getrevocationinformation");
    URI nonRevocationEvidenceReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/generatenonrevocationevidence");
    URI nonRevocationUpdateReference =
        URI.create("http://localhost:9094/integration-test-revocation/revocation/generatenonrevocationevidenceupdate");;
    RevocationReferences revocationReferences =
        new RevocationReferences(patrasRevocationAuthority, revocationInfoReference,
            nonRevocationEvidenceReference, nonRevocationUpdateReference);


    String systemParametersResource = fileStoragePrefix + RevocationHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParameters = FileSystem.loadXmlFromResource(systemParametersResource);
    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResourcesInDir(fileStoragePrefix, "issuer_params");
    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversityWithRevocation.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    System.out.println("=============================================");

    RevocationHelper.initInstance(fileStoragePrefix, fileStoragePrefix, systemParameters,
        issuerParamsList, credSpecList, revocationReferences);

    String[] revocationAuthorityParameters_resources =
        FileSystem.findFilesInDir(fileStoragePrefix, "revocation_authority");

    System.out.println("revocationAuthorityParameters_resources : "
        + revocationAuthorityParameters_resources);
    System.out.println("revocationAuthorityParameters_resources : "
        + revocationAuthorityParameters_resources[0]);

    RevocationAuthorityParameters revocationAuthorityParameters =
        (RevocationAuthorityParameters) FileSystem
            .loadXmlFromResource(revocationAuthorityParameters_resources[0]);
    // Assert.assertNotNull("Version should be specified",
    // revocationAuthorityParameters.getVersion());
    // Assert.assertNotSame("Version cannot be empty", revocationAuthorityParameters.getVersion(),
    // "");
    Assert.assertNotNull("RevocationInfoReference - should have been assigned",
        revocationAuthorityParameters.getRevocationInfoReference());
    Assert.assertNotNull("NonRevocationEvidenceReference - should have been  assigned",
        revocationAuthorityParameters.getNonRevocationEvidenceReference());
    Assert.assertNotNull("RevocationEvidenceUpdateReference - should have been  assigned",
        revocationAuthorityParameters.getNonRevocationEvidenceUpdateReference());
    System.out.println("revocationAuthorityParameters_resources : "
        + XmlUtils.toXml(of.createRevocationAuthorityParameters(revocationAuthorityParameters),
            false));
    System.out.println("revocationAuthorityParameters_resources : "
        + XmlUtils.toXml(of.createRevocationAuthorityParameters(revocationAuthorityParameters),
            true));
  }


}

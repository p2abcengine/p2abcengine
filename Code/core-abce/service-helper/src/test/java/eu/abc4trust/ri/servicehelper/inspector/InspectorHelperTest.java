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

package eu.abc4trust.ri.servicehelper.inspector;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.revocation.RevocationHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;

/**
 * Unit test for simple App.
 */
public class InspectorHelperTest {

  @Before
  public void alwaysResetEngine() throws Exception {
    InspectorHelper.resetInstance();
  }
	
  @Test(expected = IllegalStateException.class)
  public void test_notInit() throws Exception {
    InspectorHelper.getInstance();
  }

  static ObjectFactory of = new ObjectFactory();

  private final URI[] inspectorPublicKeyUIDs = {URI.create("urn:soderhamn:inspectorpk")};

  private String getFolderName() {
    if (new File("target").exists()) {
      return "target";
    } else {
      return "service-helper/target";
    }
  }

  private String getCurrentIssuerPrefix(String name) {
    final String file_part_of_name;
    file_part_of_name = "inspector_ut_" + name + "_";
    return this.getFolderName() + "/" + file_part_of_name;
  }

  private void test_init(String test_name, boolean clearFiles, URI[] inspectorKeyUIDs)
      throws Exception {
    String fileStoragePrefix;

    String folderName = this.getFolderName();
    File folder = new File(folderName);
    fileStoragePrefix = this.getCurrentIssuerPrefix(test_name);

    System.out.println("FILESTORAGE PREFIX!!!" + fileStoragePrefix);
    if (clearFiles && folder.exists()) {
      File[] storage_file_list = folder.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          if (arg1.startsWith("inspector_ut_")) {
            return true;
          } else {
            return false;
          }
        }
      });
      System.out.println("DELETE FILES : " + storage_file_list);
      for (File f : storage_file_list) {
        System.out.println("Delete Files : " + f.getAbsolutePath());
        f.delete();
      }
    }

    String inspectorStorageAndResourcesPrefix = fileStoragePrefix;

    String systemParametersResource =
        inspectorStorageAndResourcesPrefix + "resources/" + InspectorHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParameters;
    try {
      systemParameters = FileSystem.loadXmlFromResource(systemParametersResource);
      System.out.println("SystemParameters exist from previous test.");
    } catch (IOException e) {
      File resourceFolder = new File(inspectorStorageAndResourcesPrefix + "resources/");
      if (!resourceFolder.exists()) {
        resourceFolder.mkdirs();
      }
      System.out.println("Create Default SystemParameters and store in file  : "
          + systemParametersResource);
      systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024(); // generateSystemParameters(userInjector,
                                                                            // 1024);
      FileSystem.storeObjectAsXMLInFile(of.createSystemParameters(systemParameters),
          systemParametersResource);
    }

    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"};

    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);
    InspectorHelper.initInstance(inspectorStorageAndResourcesPrefix + "storage/",
        inspectorStorageAndResourcesPrefix + "resources/", systemParameters, inspectorKeyUIDs,
        credSpecList);
  }

  @Test()
  public void testInitInspectorHelper() throws Exception {

    InspectorHelper.resetInstance();
    this.test_init("soderhamn", false, inspectorPublicKeyUIDs);
  }

  @Test()
  public void testInitInspectorHelperAgain() throws Exception {

    InspectorHelper.resetInstance();
    this.test_init("soderhamn", false, inspectorPublicKeyUIDs);
  }

}

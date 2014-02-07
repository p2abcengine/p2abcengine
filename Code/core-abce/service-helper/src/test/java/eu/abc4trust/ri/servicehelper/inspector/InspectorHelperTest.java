//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
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

package eu.abc4trust.ri.servicehelper.inspector;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;

import org.junit.Test;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
public class InspectorHelperTest {

    @Test(expected = IllegalStateException.class)
    public void test_notInit() throws Exception {
        InspectorHelper.getInstance();
    }

    static ObjectFactory of = new ObjectFactory();

    private final URI[] inspectorPublicKeyUIDs = { URI.create("urn:soderhamn:inspectorpk") };

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

    private void test_init(String test_name, boolean clearFiles, URI[] inspectorKeyUIDs) throws Exception {
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

        System.out.println("Create SystemParameters");
        SystemParameters systemParameters = SystemParametersUtil.generatePilotSystemParameters_WithIdemixSpecificKeySize(2048, 2048);
        
        String systemParametersResource = inspectorStorageAndResourcesPrefix + "resources/" + InspectorHelper.SYSTEM_PARAMS_NAME_BRIDGED;
        
        FileSystem.storeObjectInFile(systemParameters, systemParametersResource);
        String[] credSpecResourceList =
          { "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml"
          };
        
        InspectorHelper.initInstance(inspectorStorageAndResourcesPrefix + "storage/", inspectorStorageAndResourcesPrefix + "resources/", systemParametersResource, inspectorKeyUIDs, credSpecResourceList);
    }

    @Test()
    public void testInitInspectorHelper() throws Exception {

        InspectorHelper.resetInstance();
        this.test_init("soderhamn", false, inspectorPublicKeyUIDs);
    }

}

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

package eu.abc4trust.services;

import java.io.File;
import java.net.URI;

public class Constants {

    public static int KEY_SIZE = 1024;
  
    private static String BASE_FOLDER = "";
    static {
        if (new File("target").exists()) {
            BASE_FOLDER = "target/";
        } else {
            BASE_FOLDER = "abce-services/target/";
        }
    }
    public static final String SCOPE_STRING = "urn:soderhamn:registration";
    public static final URI SCOPE_URI = URI.create(SCOPE_STRING);

    public static final String ISSUER_RESOURCES_FOLDER = BASE_FOLDER + "issuer_resources";
    public static final String ISSUER_STORAGE_FOLDER = BASE_FOLDER + "issuer_storage";
    
    public static final String ISSUER_PARAMETERS = ISSUER_RESOURCES_FOLDER+"/issuer_parameters.xml";

    public static final String REVOCATION_STORAGE_FOLDER = BASE_FOLDER + "revocation_storage";

    public static final String CREDENTIAL_SPECIFICATION_FOLDER = BASE_FOLDER + "xml";

    public static final String INSPECTOR_STORAGE_FOLDER = BASE_FOLDER + "inspector_storage";

    public static final String USER_STORAGE_FOLDER = BASE_FOLDER + "user_storage";

    public static final String VERIFIER_STORAGE_FOLDER = BASE_FOLDER + "verifier_storage";

    public static final URI SODERHAMN_REVOCATION_AUTHORITY = URI.create("urn:soderhamn:revocationauthority:default");

    public static final URI[] INSPECTOR_PUBLIC_KEY_UIDs = {URI.create("urn:soderhamn:inspectorpk")};
    public static final String[] INSPECTOR_PUBLIC_KEY_RESOURCE_LIST = {
        Constants.ISSUER_RESOURCES_FOLDER + "/inspector_publickey_urn_soderhamn_inspectorpk"
    };

    public static final String INSPECTOR_PRIVATE_KEY = Constants.INSPECTOR_STORAGE_FOLDER + "/inspector_privatekey_urn_soderhamn_inspectorpk";

    public static final String SYSTEM_PARAMETER_RESOURCE = ISSUER_RESOURCES_FOLDER+"/system_parameters.xml";

}


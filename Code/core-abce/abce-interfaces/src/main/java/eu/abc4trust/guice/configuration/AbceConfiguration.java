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

package eu.abc4trust.guice.configuration;

import java.io.File;
import java.util.Random;

public interface AbceConfiguration {

    public File getSecretStorageFile();

    public File getKeyStorageFile();

    public File getTokensFile();

    public File getPseudonymsFile();

    public File getCredentialFile();

    public Random getPrng();

    public String getDefaultImagePath();

    public File getImageCacheDir();

    public File getIssuerSecretKeyFile();
    
    public File getIssuerLogFile();
    
    public File getInspectorSecretKeyFile();

    public Integer getUProveRetryTimeout();

    public Integer getUProveNumberOfCredentialsToGenerate();

    public File getRevocationAuthoritySecretKeyFile();

    public File getRevocationAuthorityStorageFile();

}

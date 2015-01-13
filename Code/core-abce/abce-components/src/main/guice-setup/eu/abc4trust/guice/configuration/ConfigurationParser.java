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

package eu.abc4trust.guice.configuration;

import java.io.File;

import org.apache.commons.configuration.Configuration;

public class ConfigurationParser {

    protected final String NOTSET = "NOTSET";

    private final Configuration config;

    public ConfigurationParser(Configuration configuration) {
        this.config = configuration;
    }

    public AbceConfiguration loadConfiguration(AbceConfigurationImpl configuration) {
        configuration.setKeyStorageFile(new File(this.getKeyStorageFile()));
        configuration.setCredentialFile(new File(this.getCredentialFile()));
        configuration.setIssuerLogFile(new File(this.getIssuerLogFile()));
        configuration.setPseudonymsFile(new File(this.getPseudonymsFile()));
        configuration.setTokensFile(new File(this.getTokensFile()));
        configuration.setDefaultImagePath(this.getDefaultImagePath());
        configuration.setImageCacheDir(new File(this.getImageCacheDir()));
        return configuration;
    }

    private String getImageCacheDir() {
        return this.config.getString("image_cache_dir", this.NOTSET);
    }

    private String getDefaultImagePath() {
        return this.config.getString("default_image_path", this.NOTSET);
    }

    private String getTokensFile() {
        return this.config.getString("tokenstorage_file", this.NOTSET);
    }

    private String getIssuerLogFile() {
        return this.config.getString("issuerlog_file", this.NOTSET);
    }
    
    private String getPseudonymsFile() {
        return this.config.getString("pseudonyms_file", this.NOTSET);
    }

    private String getCredentialFile() {
        return this.config.getString("credentialstorage_file", this.NOTSET);
    }

    private String getKeyStorageFile() {
        return this.config.getString("keystorage_file", this.NOTSET);
    }

}

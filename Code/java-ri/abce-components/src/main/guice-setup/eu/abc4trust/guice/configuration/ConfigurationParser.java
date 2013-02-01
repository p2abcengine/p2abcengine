//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

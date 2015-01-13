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

@Deprecated
public class AbceConfigurationImpl implements AbceConfiguration {

    private File keyStorageFile;
    private File secretStorageFile;
    private File tokensFile;
    private File pseudonymsFile;
    private File credentialFile;
    private Random prng;
    private String defaultImageFilename;
    private File imageCacheDir;
    private File issuerSecretKeyFile;
    private File issuerLogFile;
    private File inspectorSecretKeyFile;
    private Integer uproveRetryTimeout;
    private Integer uproveNumberOfCredentialsToGenerate;

    private File revocationAuthoritySecretKeyFile;
    private File revocationAuthorityStorageFile;

    public AbceConfigurationImpl() {
        super();
    }

    @Override
    public File getSecretStorageFile() {
        return this.secretStorageFile;
    }

    @Override
    public File getKeyStorageFile() {
        return this.keyStorageFile;
    }

    @Override
    public File getTokensFile() {
        return this.tokensFile;
    }

    @Override
    public File getPseudonymsFile() {
        return this.pseudonymsFile;
    }

    @Override
    public File getCredentialFile() {
        return this.credentialFile;
    }

    public void setSecretStorageFile(File keyStorageFile) {
        this.secretStorageFile = keyStorageFile;
    }

    public void setKeyStorageFile(File keyStorageFile) {
        this.keyStorageFile = keyStorageFile;
    }

    public void setTokensFile(File tokensFile) {
        this.tokensFile = tokensFile;
    }

    public void setPseudonymsFile(File pseudonymsFile) {
        this.pseudonymsFile = pseudonymsFile;
    }

    public void setCredentialFile(File credentialFile) {
        this.credentialFile = credentialFile;
    }

    @Override
    public Random getPrng() {
        return this.prng;
    }

    public void setPrng(Random prng) {
        this.prng = prng;
    }

    @Override
    public String getDefaultImagePath() {
        return this.defaultImageFilename;
    }

    public void setDefaultImagePath(String defaultImageFilename) {
        this.defaultImageFilename = defaultImageFilename;
    }

    @Override
    public File getImageCacheDir() {
        return this.imageCacheDir;
    }

    public void setImageCacheDir(File imageCacheDir) {
        this.imageCacheDir = imageCacheDir;
    }

    public void setIssuerSecretKeyFile(File file) {
        this.issuerSecretKeyFile = file;
    }

    @Override
    public File getIssuerSecretKeyFile() {
        return this.issuerSecretKeyFile;
    }

    public void setIssuerLogFile(File file) {
        this.issuerLogFile = file;
    }

    @Override
    public File getIssuerLogFile() {
        return this.issuerLogFile;
    }
    
    @Override
    public File getInspectorSecretKeyFile() {
        return this.inspectorSecretKeyFile;
    }

    public void setInspectorSecretKeyFile(File file) {
        this.inspectorSecretKeyFile = file;
    }

    @Override
    public Integer getUProveRetryTimeout() {
        return this.uproveRetryTimeout;
    }

    public void setUProveRetryTimeout(Integer uproveRetryTimeout) {
        this.uproveRetryTimeout = uproveRetryTimeout;
    }

    @Override
    public Integer getUProveNumberOfCredentialsToGenerate() {
      return uproveNumberOfCredentialsToGenerate;
    }

    public void setUProveNumberOfCredentialsToGenerate(Integer value) {
      this.uproveNumberOfCredentialsToGenerate = value;
    }
    
    @Override
    public File getRevocationAuthoritySecretKeyFile() {
        return this.revocationAuthoritySecretKeyFile;
    }

    public void setRevocationAuthoritySecretStorageFile(File keyStorageFile) {
        this.revocationAuthoritySecretKeyFile = keyStorageFile;
    }

    @Override
    public File getRevocationAuthorityStorageFile() {
        return this.revocationAuthorityStorageFile;
    }

    public void setRevocationAuthorityStorageFile(File storageFile) {
        this.revocationAuthorityStorageFile = storageFile;
    }

}

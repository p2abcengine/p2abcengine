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
import java.util.Random;

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
    private File uproveWorkingDirectory;
    private String uprovePathToExe;
    private Integer uprovePortnumber;
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
    public File getUProveWorkingDirectory() {
        return this.uproveWorkingDirectory;
    }

    @Override
    public String getUProvePathToExe() {
        return this.uprovePathToExe;
    }

    public void setUProveWorkingDirectory(File file) {
        this.uproveWorkingDirectory = file;
    }

    public void setUProvePathToExe(String pathToExe) {
        this.uprovePathToExe = pathToExe;
    }

    @Override
    public Integer getUProvePortNumber() {
        return this.uprovePortnumber;
    }

    public void setUProvePortNumber(Integer uprovePortnumber) {
        this.uprovePortnumber = uprovePortnumber;
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

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

    public File getUProveWorkingDirectory();

    public String getUProvePathToExe();

    public Integer getUProvePortNumber();

    public Integer getUProveNumberOfCredentialsToGenerate();

    public File getRevocationAuthoritySecretKeyFile();

    public File getRevocationAuthorityStorageFile();

}

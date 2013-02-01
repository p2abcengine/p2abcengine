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

package eu.abc4trust.abce.integrationtests.idemix;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.google.inject.Module;

import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.configuration.AbceConfiguration;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.util.TemporaryFileFactory;

public class IdemixIntegrationModuleFactory {

  public static Module newModule(Random random, File storageFolder, String storagePrefix) {
    AbceConfiguration config = generateConfiguration(random, storageFolder, storagePrefix);
    return ProductionModuleFactory.newModule(config, ProductionModuleFactory.CryptoEngine.IDEMIX);
  }

  public static Module newModule(Random random) {
    return newModule(random, null, null);
  }

  private static AbceConfiguration generateConfiguration(Random random, File storageFolder,
      String filePrefix) {
    AbceConfigurationImpl config = new AbceConfigurationImpl();

    config.setCredentialFile(getStorageFile(storageFolder, filePrefix, "credential"));
    config.setKeyStorageFile(getStorageFile(storageFolder, filePrefix, "keys"));
    config.setSecretStorageFile(getStorageFile(storageFolder, filePrefix, "secrets"));
    config.setPseudonymsFile(getStorageFile(storageFolder, filePrefix, "pseudonyms"));
    config.setTokensFile(getStorageFile(storageFolder, filePrefix, "tokens"));
    config.setIssuerSecretKeyFile(getStorageFile(storageFolder, filePrefix, "issuersecretfile"));
    config.setIssuerLogFile(getStorageFile(storageFolder, filePrefix, "issuerlogfile"));
    config.setInspectorSecretKeyFile(getStorageFile(storageFolder, filePrefix, "inspectorsecretfile"));
    config.setRevocationAuthoritySecretStorageFile(getStorageFile(storageFolder, filePrefix, "revocationauthoritysecretfile"));
    config.setRevocationAuthorityStorageFile(getStorageFile(storageFolder, filePrefix, "revocationauthoritystoragefile"));
    config.setDefaultImagePath("file://error");
    config.setImageCacheDir(TemporaryFileFactory.createTemporaryDir());
    config.setPrng(random);
    return config;
  }

  private static File getStorageFile(File storageFolder, String filePrefix, String storageName) {
    try {
      if (storageFolder == null) {
        return TemporaryFileFactory.createTemporaryFile();
      } else {

        File storageFile = new File(storageFolder, filePrefix + storageName);
        if (storageFile.exists()) {
          return storageFile;
        } else {
          boolean created = storageFile.createNewFile();
          if (!created) {
            throw new IOException("Could not create new file : " + storageFile.getName());
          }
          return storageFile;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

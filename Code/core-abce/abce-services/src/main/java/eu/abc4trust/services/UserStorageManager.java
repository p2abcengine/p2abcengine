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

package eu.abc4trust.services;

import java.security.SecureRandom;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.guice.configuration.AbceKeyManagerConfigurationModule;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.FileSystem;

public class UserStorageManager {

    private static final boolean WIPE_STOARAGE_FILES = false;

    public static KeyManager getKeyManager(String fileStoragePrefix)
            throws Exception {

        AbceConfigurationImpl configuration = setupStorageFilesForConfiguration(
                fileStoragePrefix, WIPE_STOARAGE_FILES);

        Module m = new AbceKeyManagerConfigurationModule(configuration);
        Injector injector = Guice.createInjector(m);
        return injector.getInstance(KeyManager.class);
    }

    protected static AbceConfigurationImpl setupStorageFilesForConfiguration(
            String fileStoragePrefix, boolean wipe_existing_storage)
                    throws Exception {
        AbceConfigurationImpl configuration = new AbceConfigurationImpl();
        configuration.setKeyStorageFile(FileSystem.getFile(fileStoragePrefix
                + "keystorage", wipe_existing_storage));
        Random random = new SecureRandom(); // new Random(1985)
        configuration.setPrng(random);

        return configuration;
    }

}

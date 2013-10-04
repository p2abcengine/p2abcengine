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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerImpl;
import eu.abc4trust.keyManager.KeyStorage;
import eu.abc4trust.keyManager.PersistentKeyStorage;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.RevocationProxyCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxyImpl;
import eu.abc4trust.revocationProxy.WebServiceCommunicationStrategy;

public class AbceKeyManagerConfigurationModule extends AbstractModule {

    private final AbceConfiguration configuration;

    public AbceKeyManagerConfigurationModule(AbceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        this.bind(File.class).annotatedWith(Names.named("KeyStorageFile"))
        .toInstance(this.configuration.getKeyStorageFile());
        this.bind(KeyManager.class).to(KeyManagerImpl.class)
        .in(Singleton.class);
        this.bind(KeyStorage.class).to(PersistentKeyStorage.class)
        .in(Singleton.class);
        this.bind(RevocationProxyCommunicationStrategy.class)
        .to(WebServiceCommunicationStrategy.class).in(Singleton.class);
        this.bind(RevocationProxy.class).to(RevocationProxyImpl.class)
                .in(Singleton.class);
    }
}

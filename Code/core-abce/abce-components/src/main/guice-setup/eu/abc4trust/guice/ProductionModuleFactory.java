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

package eu.abc4trust.guice;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import eu.abc4trust.db.DatabaseConnectionFactory;
import eu.abc4trust.db.JdbcPersistentStorage;
import eu.abc4trust.guice.abcEngine.FileBackedKeyManager;
import eu.abc4trust.guice.abcEngine.NewKeyManagerModule;
import eu.abc4trust.guice.abcEngine.RealAbcEngineModule;
import eu.abc4trust.guice.configuration.AbceConfiguration;
import eu.abc4trust.guice.configuration.AbceConfigurationModule;
import eu.abc4trust.guice.configuration.DefaultAbceConfigurationModule;
import eu.abc4trust.guice.configuration.StorageModule;
import eu.abc4trust.guice.cryptoEngine.Idemix3CryptoEngineModule;
import eu.abc4trust.guice.cryptoEngine.MockCryptoEngineModule;
import eu.abc4trust.guice.ui.MockUiModule;

public class ProductionModuleFactory {

  public enum CryptoEngine {
    IDEMIX(new Idemix3CryptoEngineModule()), UPROVE(new Idemix3CryptoEngineModule()), MOCK(
        new MockCryptoEngineModule());

    private final Module module;

    CryptoEngine(Module module) {
      this.module = module;
    }

    Module getModule() {
      return this.module;
    }
  }


  /**
   * Uses the old key manager (without multiuser support but with smartcard support).
   * 
   * @return
   */
  @Deprecated
  public static Module newModule(AbceConfiguration configuration, CryptoEngine ce) {
    return Modules.combine(new AbceConfigurationModule(configuration), new RealAbcEngineModule(),
        new FileBackedKeyManager(), new MockUiModule(), ce.getModule());
  }

  /**
   * Uses the old key manager (without multiuser support but with smartcard support).
   * 
   * @return
   */
  @Deprecated
  public static Module newModuleWithFilePersistence() {
    return Modules.combine(new DefaultAbceConfigurationModule(), new RealAbcEngineModule(),
        new FileBackedKeyManager(), new MockUiModule(), CryptoEngine.IDEMIX.getModule());
  }

  /**
   * Uses the new key manager using a database-backed storage. Revocation not yet ready & smartcards
   * are not supported
   * 
   * @return
   */
  public static Module newModuleWithPersistance() {
    return newModule(new StorageModule(true));
  }

  /**
   * Uses the new key manager using in-memory storage (which is not retained when the injector
   * exits). Revocation not yet ready & smartcards are not supported
   * 
   * @return
   */
  public static Module newModuleWithoutPersistance() {
    return newModule(new StorageModule(false));
  }

  /**
   * Uses the new key manager using in-memory storage (which is not retained when the injector
   * exits). Revocation not yet ready & smartcards are not supported
   * 
   * @return
   */
  public static Module newModule() {
    return newModuleWithoutPersistance();
  }

  /**
   * Uses the new key manager using a custom database-backed storage. Revocation not yet ready &
   * smartcards are not supported
   * 
   * @return
   */
  public static Module newModule(DatabaseConnectionFactory dbc) {
    return newModule(new StorageModule(new JdbcPersistentStorage(dbc)));
  }

  private static Module newModule(Module storageModule) {
    return Modules.combine(storageModule, new RealAbcEngineModule(), new NewKeyManagerModule(),
        new MockUiModule(), new Idemix3CryptoEngineModule());
  }

}

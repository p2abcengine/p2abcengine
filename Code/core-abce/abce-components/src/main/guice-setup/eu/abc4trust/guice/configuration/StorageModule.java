//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

import com.google.inject.AbstractModule;

import eu.abc4trust.db.LocalJdbcDatabase;
import eu.abc4trust.db.MockPersistentStorage;
import eu.abc4trust.db.JdbcPersistentStorage;
import eu.abc4trust.db.PersistentStorage;

public class StorageModule extends AbstractModule {

  private final PersistentStorage ps;

  public StorageModule(PersistentStorage ps) {
    this.ps = ps;
  }

  public StorageModule(boolean realDatabase) {
    if (realDatabase) {
      this.ps = new JdbcPersistentStorage(new LocalJdbcDatabase());
    } else {
      this.ps = new MockPersistentStorage();
    }
  }

  @Override
  protected void configure() {
    this.bind(PersistentStorage.class).toInstance(ps);
  }

}

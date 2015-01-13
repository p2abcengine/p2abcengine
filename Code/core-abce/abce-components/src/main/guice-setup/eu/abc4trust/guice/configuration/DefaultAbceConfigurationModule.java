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
import java.security.SecureRandom;
import java.util.Random;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import eu.abc4trust.util.TemporaryFileFactory;

public class DefaultAbceConfigurationModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(File.class).annotatedWith(Names.named("KeyStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("IssuerSecretKeyStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("InspectorSecretKeyStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("RevocationAuthoritySecretKeyStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("RevocationAuthorityStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("TokenStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("PseudonymsStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("TokenLogFile"))
    	.toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("CredentialStorageFile"))
        .toInstance(new File(""));
    this.bind(File.class).annotatedWith(Names.named("SecretStorageFile"))
        .toInstance(new File(""));
    this.bind(String.class).annotatedWith(Names.named("DefaultImagePath"))
        .toInstance("file://error");
    this.bind(File.class).annotatedWith(Names.named("ImageCacheBaseDir"))
        .toInstance(TemporaryFileFactory.createTemporaryDir());
    this.bind(Random.class).annotatedWith(Names.named("RandomNumberGenerator"))
        .toInstance(new SecureRandom());

    // Uprove extras
    this.bind(Integer.class).annotatedWith(Names.named("NumberOfCredentialsToGenerate"))
        .toInstance(Integer.valueOf(10));
      this.bind(File.class).annotatedWith(Names.named("UProveWorkingDirectory"))
          .toProvider(Providers.of((File) null));
    this.bind(String.class).annotatedWith(Names.named("PathToUProveExe"))
        .toInstance("");
    this.bind(Integer.class).annotatedWith(Names.named("UProveRetryTimeout"))
        .toInstance(0);
    this.bind(Integer.class).annotatedWith(Names.named("RetryTimeout"))
        .toInstance(0);
    this.bind(Integer.class).annotatedWith(Names.named("UProvePortNumber"))
        .toInstance(0);
  }

}

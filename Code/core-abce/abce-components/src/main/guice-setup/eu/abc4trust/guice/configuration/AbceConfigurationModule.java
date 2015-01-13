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
import java.util.Random;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

@Deprecated
public class AbceConfigurationModule extends AbstractModule {
  private final AbceConfiguration configuration;
  
  public AbceConfigurationModule(AbceConfiguration configuration) {
    this.configuration = configuration;  
  }

  @Override
  protected void configure() {
    this.bind(File.class).annotatedWith(Names.named("KeyStorageFile"))
        .toInstance(this.configuration.getKeyStorageFile());
    this.bind(File.class).annotatedWith(Names.named("IssuerSecretKeyStorageFile"))
        .toInstance(this.configuration.getIssuerSecretKeyFile());
    this.bind(File.class).annotatedWith(Names.named("InspectorSecretKeyStorageFile"))
        .toInstance(this.configuration.getInspectorSecretKeyFile());
    this.bind(File.class).annotatedWith(Names.named("RevocationAuthoritySecretKeyStorageFile"))
        .toInstance(this.configuration.getRevocationAuthoritySecretKeyFile());
    this.bind(File.class).annotatedWith(Names.named("RevocationAuthorityStorageFile"))
        .toInstance(this.configuration.getRevocationAuthorityStorageFile());
    this.bind(File.class).annotatedWith(Names.named("TokenStorageFile"))
        .toInstance(this.configuration.getTokensFile());
    this.bind(File.class).annotatedWith(Names.named("PseudonymsStorageFile"))
        .toInstance(this.configuration.getPseudonymsFile());
    this.bind(File.class).annotatedWith(Names.named("TokenLogFile"))
    	.toInstance(this.configuration.getIssuerLogFile());
    this.bind(File.class).annotatedWith(Names.named("CredentialStorageFile"))
        .toInstance(this.configuration.getCredentialFile());
    this.bind(File.class).annotatedWith(Names.named("SecretStorageFile"))
        .toInstance(this.configuration.getSecretStorageFile());
    this.bind(String.class).annotatedWith(Names.named("DefaultImagePath"))
        .toInstance(this.configuration.getDefaultImagePath());
    this.bind(File.class).annotatedWith(Names.named("ImageCacheBaseDir"))
        .toInstance(this.configuration.getImageCacheDir());
    this.bind(Random.class).annotatedWith(Names.named("RandomNumberGenerator"))
        .toInstance(this.configuration.getPrng());

    // Uprove extras
    Integer numberOfCredentialsToGenerate =
        this.configuration.getUProveNumberOfCredentialsToGenerate();
    if (numberOfCredentialsToGenerate == null) {
      numberOfCredentialsToGenerate = 10;
    }
    this.bind(Integer.class).annotatedWith(Names.named("NumberOfCredentialsToGenerate"))
        .toInstance(numberOfCredentialsToGenerate);

    // We bind to providers, since the variables might be null
    this.bind(Integer.class).annotatedWith(Names.named("UProveRetryTimeout"))
        .toProvider(Providers.of(this.configuration.getUProveRetryTimeout()));
    this.bind(Integer.class).annotatedWith(Names.named("RetryTimeout"))
        .toProvider(Providers.of(this.configuration.getUProveRetryTimeout()));
  }
}

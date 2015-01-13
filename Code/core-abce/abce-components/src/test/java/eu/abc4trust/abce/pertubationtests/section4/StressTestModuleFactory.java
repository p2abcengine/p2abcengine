//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
package eu.abc4trust.abce.pertubationtests.section4;

import java.util.Random;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfiguration;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.revocationProxy.InMemoryCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxyCommunicationStrategy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.TemporaryFileFactory;

public class StressTestModuleFactory {

	public enum IssuerCryptoEngine {
		IDEMIX(ProductionModuleFactory.CryptoEngine.IDEMIX), UPROVE(
				ProductionModuleFactory.CryptoEngine.UPROVE);

		private final ProductionModuleFactory.CryptoEngine ce;

		IssuerCryptoEngine(ProductionModuleFactory.CryptoEngine ce) {
			this.ce = ce;
		}

		ProductionModuleFactory.CryptoEngine getCryptoEngine() {
			return ce;
		}
	}
	
	  public static Module newModule(Random random, IssuerCryptoEngine issuerEngine) {
		    return newModule(random, issuerEngine.getCryptoEngine());
		  }

	public static Module newModule(Random random, CryptoEngine cryptoEngine) {
		AbceConfiguration config = generateDefaultConfiguration(random);

		Module m = ProductionModuleFactory.newModule(config, cryptoEngine);
		return Modules.override(m).with(new AbstractModule() {
			@Override
			protected void configure() {
				this.bind(RevocationProxyCommunicationStrategy.class)
				.to(InMemoryCommunicationStrategy.class).in(Singleton.class);
			}
		});
	}

	//public static Module newModule(Random random, CryptoEngine cryptoEngine) {
	//  Integer uprovePortNumber = null;
	//  return newModule(random, cryptoEngine, uprovePortNumber);
	//}

	public static Module newModule(Random random) {
		return newModule(random, CryptoEngine.UPROVE);
	}

	public static Module newModule(Random random,
			RevocationProxyAuthority revocationProxyAuthority) {
		return newModule(random, CryptoEngine.UPROVE, revocationProxyAuthority);
	}

	//public static Module newModule(Random random, CryptoEngine cryptoEngine,
	//                               final RevocationProxyAuthority revocationProxyAuthority) {
	//  return newModule(random, cryptoEngine, null, revocationProxyAuthority);
	//}

	  public static Module newModule(Random random, IssuerCryptoEngine issuerEngine,
			  final RevocationProxyAuthority revocationProxyAuthority) {
		    return newModule(random, issuerEngine.getCryptoEngine(), revocationProxyAuthority);
		  }
	
	public static Module newModule(Random random, CryptoEngine cryptoEngine,
			final RevocationProxyAuthority revocationProxyAuthority) {

		Module m = newModule(random, cryptoEngine);

		return Modules.override(m).with(new AbstractModule() {
			@Override
			protected void configure() {
				this.bind(RevocationProxyAuthority.class).toInstance(revocationProxyAuthority);
			}
		});
	}

	private static AbceConfiguration generateDefaultConfiguration(Random random) {
		AbceConfigurationImpl config = new AbceConfigurationImpl();
		config.setSecretStorageFile(TemporaryFileFactory.createTemporaryFile());
		config.setCredentialFile(TemporaryFileFactory.createTemporaryFile());
		config.setKeyStorageFile(TemporaryFileFactory.createTemporaryFile());
		config.setPseudonymsFile(TemporaryFileFactory.createTemporaryFile());
		config.setTokensFile(TemporaryFileFactory.createTemporaryFile());
		config.setImageCacheDir(TemporaryFileFactory.createTemporaryDir());
		config.setIssuerSecretKeyFile(TemporaryFileFactory.createTemporaryFile());
		config.setIssuerLogFile(TemporaryFileFactory.createTemporaryFile());
		config.setInspectorSecretKeyFile(TemporaryFileFactory.createTemporaryFile());
		config.setRevocationAuthoritySecretStorageFile(TemporaryFileFactory.createTemporaryFile());
		config.setRevocationAuthorityStorageFile(TemporaryFileFactory.createTemporaryFile());
		config.setPrng(random);
		config.setDefaultImagePath("file://error");
		config.setUProveRetryTimeout(10);
		config.setUProveNumberOfCredentialsToGenerate(1501);
		return config;
	}
}
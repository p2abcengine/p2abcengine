//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.servicehelper.user;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;

import eu.abc4trust.abce.external.user.SynchronizedUserAbcEngineImpl;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SystemParameters;

public class UserHelper extends AbstractHelper {

  private static final Logger log = Logger.getLogger(UserHelper.class.toString());

  public static boolean WIPE_STOARAGE_FILES = false;
  private static final String USERNAME = "defaultUser";

  static UserHelper instance;

  // with inspector and revocation
  public static synchronized UserHelper initInstance(SystemParameters systemParamsResource,
      List<IssuerParameters> issuerParamsList, String fileStoragePrefix,
      List<CredentialSpecification> credSpecList, List<InspectorPublicKey> inspectorPublicKeyList,
      List<RevocationAuthorityParameters> revocationAuthorityParametersList) throws URISyntaxException {

    initialializeInstanceField(fileStoragePrefix);
    instance.setSystemParams(systemParamsResource);

    instance.addCredentialSpecifications(credSpecList);

    instance.addIssuerParameters(issuerParamsList);

    try {
      for (SecretDescription sd : instance.credentialManager.listSecrets(USERNAME)) {
        instance.forceLoadSoftwareSmartcard(sd.getSecretUID());
      }
    } catch (Exception ex) {
      log.log(Level.SEVERE, "Could not load software smartcard :" + ex.getMessage());
    }
    instance.addInspectorPublicKeys(inspectorPublicKeyList);
    instance.addRevocationAuthorities(instance.keyManager, revocationAuthorityParametersList);

    log.info("UserHelper.initInstance : DONE");

    return instance;
  }

  public static synchronized UserHelper initInstanceForService(CryptoEngine cryptoEngine,
      String fileStoragePrefix) throws URISyntaxException {

    initialializeInstanceField(fileStoragePrefix);

    log.info("UserHelper.initInstance : DONE");

    return instance;
  }

  private static void initialializeInstanceField(String fileStoragePrefix)
      throws URISyntaxException {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("UserHelper.initInstance");
    instance = new UserHelper(fileStoragePrefix);
  }

  public static synchronized boolean isInit() {
    return instance != null;
  }

  public static synchronized UserHelper getInstance() {
    if (instance == null) {
      log.warning("initInstance not called before using UserHelper!");
      throw new IllegalStateException("initInstance not called before using UserHelper!");
    }
    return instance;
  }

  public static synchronized void resetInstance() {
    log.warning("WARNING UserHelper.resetInstance : " + instance);
    instance = null;
  }

  private UserAbcEngine engine;
  public CardStorage cardStorage;
  public CredentialManager credentialManager;
  public CryptoEngineUser cryptoEngineUser;
  private ExternalSecretsManager deviceManager;


  private UserHelper(String fileStoragePrefix) throws URISyntaxException {
    CryptoEngine cryptoEngine = CryptoEngine.IDEMIX;
    log.info("UserHelper : : create instance " + cryptoEngine + " : " + fileStoragePrefix);
    try {

      AbceConfigurationImpl configuration =
          this.setupStorageFilesForConfiguration(fileStoragePrefix, cryptoEngine,
              WIPE_STOARAGE_FILES);
      configuration
          .setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
      configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

      Module m = ProductionModuleFactory.newModule(configuration, cryptoEngine);
      Injector injector = Guice.createInjector(m);

      this.keyManager = injector.getInstance(KeyManager.class);
      this.credentialManager = injector.getInstance(CredentialManager.class);
      this.cardStorage = injector.getInstance(CardStorage.class);
      //
      UserAbcEngine e = injector.getInstance(UserAbcEngine.class);
      this.engine = new SynchronizedUserAbcEngineImpl(e);
      this.cryptoEngineUser = injector.getInstance(CryptoEngineUser.class);
      this.deviceManager = injector.getInstance(ExternalSecretsManager.class);

      //
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup user !", e);
    }
  }

  public UserAbcEngine getEngine() {
    return this.engine;
  }

  public void forceLoadSoftwareSmartcard(URI secretUID) {
    deviceManager.getPseudonymSubgroupOrder(USERNAME, secretUID);
  }

}

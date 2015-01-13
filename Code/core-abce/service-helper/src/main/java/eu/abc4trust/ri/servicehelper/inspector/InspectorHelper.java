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

package eu.abc4trust.ri.servicehelper.inspector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class InspectorHelper extends AbstractHelper {

  static Logger log = Logger.getLogger(InspectorHelper.class.getName());

  private static InspectorHelper instance;

  /**
   * @param ignoredCryptoEngine ignored as only Idemix inspection keys are used
   * @param inspectorStoragePrefix - private storage files (private keys) will be stored here
   * @param inspectorResourcesPrefix - public keys will be exported here
   * @param systemParametersResource - the system parameter
   * @param inspectorKeyUIDs - list of Inspector URIs
   * @param credSpecResourceList - supported credSpecs
   * @return
   * @throws Exception
   */
  public static synchronized InspectorHelper initInstanceForService(String inspectorStoragePrefix,
      String systemParametersResource, String[] credSpecResourceList,
      String[] inspectorPKResourceList) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("InspectorHelper.initInstance");

    instance = new InspectorHelper(inspectorStoragePrefix, null, null);
    instance.addCredentialSpecifications(credSpecResourceList);

    SystemParameters systemParameters = instance.setSystemParams(systemParametersResource);
    for (String s : inspectorPKResourceList) {
      InspectorPublicKey publicKey = FileSystem.loadObjectFromResource(s);
    }
    return instance;
  }

  
  public static synchronized InspectorHelper initInstanceForService(String inspectorStoragePrefix) throws Exception {
	    if (instance != null) {
	      throw new IllegalStateException("initInstance can only be called once!");
	    }
	    log.info("InspectorHelper.initInstance");

	    instance = new InspectorHelper(inspectorStoragePrefix, null, null);

	    return instance;
	  }


  /**
   * @param inspectorStoragePrefix - private storage files (private keys) will be stored here
   * @param inspectorResourcesPrefix - public keys will be exported here
   * @param systemParametersResource - the system parameter
   * @param inspectorKeyUIDs - list of Inspector URIs
   * @param credSpecResourceList - supported credSpecs
   * @return
   * @throws Exception
   */
  public static synchronized InspectorHelper initInstance(String inspectorStoragePrefix,
      String inspectorResourcesPrefix, SystemParameters systemParameters, URI[] inspectorKeyUIDs,
      List<CredentialSpecification> credSpecList) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("InspectorHelper.initInstance");

    instance =
        new InspectorHelper(inspectorStoragePrefix, inspectorResourcesPrefix, inspectorKeyUIDs);
    instance.addCredentialSpecifications(credSpecList);
    instance.setSystemParams(systemParameters);

    instance.initInspectorKeys(systemParameters, inspectorKeyUIDs, inspectorResourcesPrefix);
    //
    return instance;
  }

  /**
   * @return true if IssuanceHelper has been initialized
   */
  public static synchronized boolean isInit() {
    return instance != null;
  }

  /**
   * Only used in test - can reset static instance
   */
  public static synchronized void resetInstance() {
    System.err.println("WARNING InspectorHelper.resetInstance : " + instance);
    instance = null;
  }

  /**
   * @return initialized instance of IssuanceHelper
   */
  public static synchronized InspectorHelper getInstance() {
    log.info("InspectorHelper.getInstance : " + instance
        + (instance == null ? "" : " : " + instance));
    if (instance == null) {
      throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
    }
    return instance;
  }

  public InspectorAbcEngine engine;
  public CredentialManager credentialManager;

  /**
   * Private constructor
   * 
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @throws URISyntaxException
   */
  private InspectorHelper(String inspectorStoragePrefix, String inspectorResourcesPrefix,
      URI[] inspectorKeyUIDs) throws URISyntaxException {
    log.info("InspectorHelper : create instance - storage prefix : "
        + inspectorStoragePrefix + " - resouces prefix : " + inspectorResourcesPrefix);
    try {
      AbceConfigurationImpl configuration =
          this.setupStorageFilesForConfiguration(inspectorStoragePrefix, CryptoEngine.IDEMIX);

      Injector injector =
          Guice.createInjector(ProductionModuleFactory
              .newModule(configuration, CryptoEngine.IDEMIX));

      this.engine = injector.getInstance(InspectorAbcEngine.class);

      this.keyManager = injector.getInstance(KeyManager.class);
      this.credentialManager = injector.getInstance(CredentialManager.class);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Init Failed !", e);
    }
  }


  private void initInspectorKeys(SystemParameters sp, URI[] inspectorKeyUIDs,
      String inspectorResourcesPrefix) {
    try {
      for (URI uid : inspectorKeyUIDs) {
        InspectorPublicKey publicKey = this.keyManager.getInspectorPublicKey(uid);
        if (publicKey == null) {
          // key does not exist!
          URI mechanism = CryptoUriUtil.getIdemixMechanism();
          log.info("Generate Inspector Keys with UID : " + uid + " - with mechanism : " + mechanism);
          List<FriendlyDescription> friendlyDescription = Collections.emptyList();
          publicKey = this.engine.setupInspectorPublicKey(sp, mechanism, uid, friendlyDescription);

          keyManager.storeInspectorPublicKey(uid, publicKey);

          String inspector_publickey_resource = "inspector_publickey_";
          boolean urnScheme = "urn".equals(uid.getScheme());
          if (urnScheme) {
            inspector_publickey_resource += uid.toASCIIString().replaceAll(":", "_");
          } else {
            inspector_publickey_resource +=
                uid.getHost().replace(".", "_") + uid.getPath().replace("/", "_");
          }
          inspector_publickey_resource += ".xml";
          JAXBElement<InspectorPublicKey> publicKeyJaxb = new ObjectFactory().createInspectorPublicKey(publicKey);
          FileSystem.storeObjectAsXMLInFile(publicKeyJaxb, inspectorResourcesPrefix, inspector_publickey_resource);

        } else {
          log.info("We already have inspector keys with UID : " + uid);
        }
      }

    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not setup Inspector !", e);
      throw new IllegalStateException("Could not setup Inspector !", e);
    }

  }

  public List<Attribute> inspect(PresentationToken value) throws Exception {
    return this.engine.inspect(value);
  }

  public static synchronized InspectorPublicKey setupPublicKey(SystemParameters sp, URI mechanism,
      int keyLength, URI uid, String inspectorResourcesPrefix) throws Exception {
    if (instance == null) {
      throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
    }
    List<FriendlyDescription> friendlyDescription = Collections.emptyList();
    InspectorPublicKey ipk =
        instance.engine.setupInspectorPublicKey(sp, mechanism, uid, friendlyDescription);
    String inspector_publickey = "inspector_publickey_";
    boolean urnScheme = "urn".equals(uid.getScheme());
    if (urnScheme) {
      inspector_publickey += uid.toASCIIString().replaceAll(":", "_");
    } else {
      inspector_publickey += uid.getHost().replace(".", "_") + uid.getPath().replace("/", "_");
    }

    FileSystem.storeObjectInFile(ipk, inspectorResourcesPrefix, inspector_publickey);
    return ipk;
  }

  public SecretKey exportPrivateKey(URI inspectorPublicKeyUID) throws Exception {
    return this.credentialManager.getInspectorSecretKey(inspectorPublicKeyUID);
  }


  public SystemParameters getSystemParameters() throws KeyManagerException {
    return this.keyManager.getSystemParameters();
  }
  
  public void addInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey) 
      throws KeyManagerException{
    this.keyManager.storeInspectorPublicKey(ipkuid, inspectorPublicKey);
  }

  @Override
  public void addCredentialSpecifications(List<CredentialSpecification> credSpecs){
    super.addCredentialSpecifications(credSpecs);
  }
  
  @Override 
  public void addIssuerParameters(List<IssuerParameters> issuerParameters){
    super.addIssuerParameters(issuerParameters);
  }
  
  @Override
  public void setSystemParams(SystemParameters syspar){
    super.setSystemParams(syspar);
  }
}

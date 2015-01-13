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

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.SynchronizedIssuerAbcEngineImpl;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.guice.configuration.StorageFiles;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.SystemParametersHelper;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IntegerParameter;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class IssuanceHelper extends AbstractHelper {

  static Logger log = Logger.getLogger(IssuanceHelper.class.getName());

  private static IssuanceHelper instance;

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
    System.err.println("WARNING IssuanceHelper.resetInstance : " + instance);
    instance = null;
  }

  /**
   * @return initialized instance of IssuanceHelper
   */
  public static synchronized IssuanceHelper getInstance() {
    log.info("IssuanceHelper.getInstance : " + instance
        + (instance == null ? "" : " : " + instance));
    if (instance == null) {
      throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
    }
    return instance;
  }

  /**
   * Private constructor for initializing/generating SystemParameters
   * 
   * @param keyLength
   * 
   * @param publicResources this prefix will be prepended on public resources files generated by the
   *        IssuerAbcEnginge
   * @param privateStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @throws URISyntaxException
   * @throws FileSystemException 
   */
  private IssuanceHelper(String publicResourcesPrefix, String privateStoragePrefix)
      throws URISyntaxException, FileSystemException {

    IssuanceHelper.log.info("IssuanceHelper : create instance - for generating SystemParameters "
        + " - private storage : " + privateStoragePrefix + " - publicResources : "
        + publicResourcesPrefix);

    this.publicResourcesPrefix = publicResourcesPrefix;
    this.privateStoragePrefix = privateStoragePrefix;
    
    createFoldersIfNoneExist(publicResourcesPrefix, privateStoragePrefix);
    
    try {
      AbceConfigurationImpl configuration =
          setupStorageFilesForConfiguration(privateStoragePrefix, CryptoEngine.IDEMIX);
      configuration
          .setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
      configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

      Injector injector =
          Guice.createInjector(ProductionModuleFactory
              .newModule(configuration, CryptoEngine.IDEMIX));

      IssuerAbcEngine eng = injector.getInstance(IssuerAbcEngine.class);
      singleEngine = new SynchronizedIssuerAbcEngineImpl(eng);
      keyManager = injector.getInstance(KeyManager.class);
      credentialManager = injector.getInstance(CredentialManager.class);
      random = configuration.getPrng();
      issuerTokenStorage = injector.getInstance(TokenStorageIssuer.class);

    } catch (Exception e) {
      System.err.println("Init Failed");
      e.printStackTrace();
      throw new IllegalStateException("Could not setup issuer !", e);
    }
  }

  private void checkAndGenerateSystemParameters(int keyLength) throws Exception {
    try {
      generatedSystemParameters = keyManager.getSystemParameters();
      log.info("SystemParameters already exists!");
      return;
    } catch (KeyManagerException ignore) {
      log.fine("SystemParameters does not exist!");
    }
    // go on and create
    createSystemParameters(singleEngine, keyLength);
  }

  /**
   * Setup IssuanceHelper
   * 
   * @param keyLength
   * 
   * @param publicResources this prefix will be prepended on public resources files generated by the
   *        IssuerAbcEnginge
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @throws Exception
   */
  public static synchronized IssuanceHelper initInstance(int keyLength,
      String publicResourcesPrefix, String privateStoragePrefix, SpecAndPolicy[] specAndPolicyList,
      List<RevocationAuthorityParameters> revocationAuthorityParametersList) throws Exception {

    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("IssuanceHelper.initInstance(Array)");
    IssuanceHelper helper = new IssuanceHelper(publicResourcesPrefix, privateStoragePrefix);
    helper.checkAndGenerateSystemParameters(keyLength);
    helper.addRevocationAuthorities(helper.keyManager, revocationAuthorityParametersList);
    helper.initIssuerParemeterList(specAndPolicyList, helper.credentialManager);

    instance = helper;


    return instance;
  }

  public static synchronized IssuanceHelper initInstanceWithExitingSystemPareters(
      SystemParameters foreignSystemParameters, List<CredentialSpecification> foreignCredSpecList,
      List<IssuerParameters> foreignIssuerParamList, String publicResourcesPrefix,
      String privateStoragePrefix, SpecAndPolicy[] specAndPolicyList,
      List<RevocationAuthorityParameters> revocationAuthorityParametersList) throws Exception {

    IssuanceHelper helper = new IssuanceHelper(publicResourcesPrefix, privateStoragePrefix);
    // add 'foreign' issuer'
    try{
    	helper.keyManager.getSystemParameters();
    }catch(KeyManagerException e){
    	log.info("helper did not have system params in memory - storing the ones given: "+foreignSystemParameters);
        helper.keyManager.storeSystemParameters(foreignSystemParameters);
    }
    
    helper.addCredentialSpecifications(foreignCredSpecList);
    helper.addIssuerParameters(foreignIssuerParamList);

    helper.generatedSystemParameters = helper.keyManager.getSystemParameters();
    helper.addRevocationAuthorities(helper.keyManager, revocationAuthorityParametersList);
    helper.initIssuerParemeterList(specAndPolicyList, helper.credentialManager);

    instance = helper;

    return instance;
  }

  public static synchronized IssuanceHelper initInstanceForService(
      String systemAndIssuerParamsPrefix, String fileStoragePrefix) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("IssuanceHelper.initInstanceForService(Array)");

    instance = new IssuanceHelper(systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0]);

    return instance;
  }

  public static synchronized StorageFiles verifyFiles(boolean wipe_existing_storage,
      String fileStoragePrefix, CryptoEngine cryptoEngine) throws Exception {
    String v = getFileStoragePrefix(fileStoragePrefix, cryptoEngine);
    return AbstractHelper.verifyFiles(wipe_existing_storage, v);
  }

  /**
   * TODO : Verify that SystemParams generated for service are OK... Ends up using static
   * 'SystemParametersHelper.getLargeSystemParameters()'
   * 
   * Private constructor - used for Service
   * 
   * @param privateStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @throws Exception
   */
  private IssuanceHelper(String publicResourcesPrefix, String privateStoragePrefix,
      String[] revocationAuthorityParametersResourcesList) throws Exception {
    IssuanceHelper.log.info("IssuanceHelper : create instance for issuer service "
        + privateStoragePrefix);
    this.publicResourcesPrefix = publicResourcesPrefix;
    this.privateStoragePrefix = privateStoragePrefix;
    
    this.createFoldersIfNoneExist(publicResourcesPrefix, privateStoragePrefix);

    AbceConfigurationImpl configuration = this.setupConfiguration();

    Injector injector =
        Guice.createInjector(ProductionModuleFactory.newModule(configuration, CryptoEngine.IDEMIX));

    IssuerAbcEngine engine = injector.getInstance(IssuerAbcEngine.class);

    this.singleEngine = new SynchronizedIssuerAbcEngineImpl(engine);

    this.keyManager = injector.getInstance(KeyManager.class);

    String systemParametersResource = this.privateStoragePrefix + SYSTEM_PARAMS_XML_NAME;

    SystemParameters systemParameters =
        SystemParametersHelper.checkAndLoadSystemParametersIfAbsent(this.keyManager,
            systemParametersResource);

    if (systemParameters == null) {
      IssuanceHelper.log.info("No system parameters loaded");
    }

    this.issuerTokenStorage = injector.getInstance(TokenStorageIssuer.class);
    this.addRevocationAuthorities(this.keyManager, revocationAuthorityParametersResourcesList);

    this.random = configuration.getPrng(); // new SecureRandom(); // new Random(1985);
  }

  private void createFoldersIfNoneExist(String publicResources, String privateResources) 
      throws FileSystemException{
    File f = new File(publicResources);
    if(!f.exists()){
      if(!f.mkdirs()){
        throw new FileSystemException(publicResources);
      }
    }
    f = new File(privateResources);
    if(!f.exists()){
      if(!f.mkdirs()){
        throw new FileSystemException(privateResources);
      }
    }
  }
  
  private IssuerAbcEngine singleEngine = null;
  private TokenStorageIssuer issuerTokenStorage;
  private CredentialManager credentialManager;


  private final Map<String, SpecAndPolicy> specAndPolicyMap = new HashMap<String, SpecAndPolicy>();

  private Random random;

  private final ObjectFactory of = new ObjectFactory();

  private final String publicResourcesPrefix;
  private final String privateStoragePrefix;

  //
  private static final boolean STORE_RESOURCES_AS_SERIALIZED_JAVA_OBJECTS = false;

  // private final String systemParametersResource;


  private AbceConfigurationImpl setupConfiguration() throws Exception {

    AbceConfigurationImpl configuration =
        setupStorageFilesForConfiguration(privateStoragePrefix, CryptoEngine.IDEMIX);
    configuration
        .setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
    configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);
    return configuration;
  }

  public static String getFileStoragePrefix(String filePrefix, CryptoEngine cryptoEngine) {
    if ((filePrefix != null) && (filePrefix.length() > 0)) {
      if (filePrefix.endsWith("_")) {
        return filePrefix + ("" + cryptoEngine).toLowerCase() + "_";
      } else {
        if (filePrefix.endsWith("/") || filePrefix.endsWith("\\")) {
          // this is a folder...
          return filePrefix + ("" + cryptoEngine).toLowerCase() + "_";
        } else {
          return filePrefix + ("_" + cryptoEngine).toLowerCase() + "_";
        }
      }
    }
    return ("" + cryptoEngine).toLowerCase();
  }

  private SystemParameters generatedSystemParameters = null;

  // use from service
  public SystemParameters createNewSystemParametersKeylength(int keyLength) throws IOException,
      KeyManagerException, Exception {

    SystemParameters sp = singleEngine.setupSystemParameters(keyLength);
    keyManager.storeSystemParameters(sp);
    return sp;

  }

  private void createSystemParameters(IssuerAbcEngine engine, int keyLength) throws Exception {

    log.info("- create new system parameters with keysize: " + keyLength);
    // ok - we have to generate them from scratch...

    this.generatedSystemParameters = engine.setupSystemParameters(keyLength);
    storeAndPublishSystemParameters();
  }

  private SystemParameters storeAndPublishSystemParameters() throws Exception {

    log.info("- store and publish generated (or imported systemparameters)");

    // store in private storage folder
    if (STORE_RESOURCES_AS_SERIALIZED_JAVA_OBJECTS) {
      FileSystem.storeObjectInFile(this.generatedSystemParameters, this.privateStoragePrefix
          + SYSTEM_PARAMS_OBJ_NAME);
    }
    FileSystem.storeObjectAsXMLInFile(
        this.of.createSystemParameters(this.generatedSystemParameters), this.privateStoragePrefix,
        SYSTEM_PARAMS_XML_NAME);

    if (!this.privateStoragePrefix.equals(this.publicResourcesPrefix)) {
      // store in resource folder (for easier export to other ABCEs)
      // String systemParametersResource_resourcePrefix =
      // this.systemAndIssuerParamsPrefix + SYSTEM_PARAMS_NAME;
      FileSystem.storeObjectAsXMLInFile(
          this.of.createSystemParameters(this.generatedSystemParameters),
          this.publicResourcesPrefix, SYSTEM_PARAMS_XML_NAME);

      if (STORE_RESOURCES_AS_SERIALIZED_JAVA_OBJECTS) {
        FileSystem.storeObjectInFile(this.generatedSystemParameters, this.publicResourcesPrefix,
            SYSTEM_PARAMS_OBJ_NAME);
      }
    }

    // store in keyManager
    boolean success = keyManager.storeSystemParameters(this.generatedSystemParameters);
    if(!success){
    	log.info("Could not store system parameters in keymanager!");
    }else{
    	log.info("Successfully stored system parameters in keymanager!");
    }

    // this.storeSystemParametersAsXML(this.fileStoragePrefix, this.generatedSystemParameters,
    // SYSTEM_PARAMS_NAME);

    IssuanceHelper.log.info("- new SystemParameters.");

    return this.generatedSystemParameters;
  }

  // private void storeSystemParametersAsXML(String filePrefix, SystemParameters systemParameters,
  // String name) throws Exception {
  // SystemParameters serializedSystemParameters = SystemParametersUtil.serialize(systemParameters);
  // JAXBElement<SystemParameters> asXml =
  // this.of.createSystemParameters(serializedSystemParameters);
  // FileSystem.storeObjectAsXMLInFile(asXml, filePrefix, name);
  // }


  private void initIssuerParemeterList(SpecAndPolicy[] specAndPolicyList,
      CredentialManager credentialManager) throws Exception {
    IssuanceHelper.log.info("initIssuerParemeterList..:");

    if (!keyManager.hasSystemParameters() || (this.generatedSystemParameters == null)) {
      throw new IllegalAccessException("initSystemParameters - should have setup SystemParameters");
    }

    SystemParameters systemParameters = null;
    systemParameters = keyManager.getSystemParameters();

    for (SpecAndPolicy currentSap : specAndPolicyList) {

      this.initIssuerParemeters(// cryptoEngine, initEngine, keyManager,
          this.publicResourcesPrefix, systemParameters, currentSap, credentialManager);
    }

  }

  private void initIssuerParemeters(
      // CryptoEngine cryptoEngine,
      // IssuerAbcEngine initEngine, KeyManager keyManager,
      String systemAndIssuerParamsPrefix, SystemParameters systemParameters,
      SpecAndPolicy currentSap, CredentialManager credentialManager) throws Exception,
      KeyManagerException, URISyntaxException, IOException {
    SpecAndPolicy sap = this.initSpecAndPolicyFromResouces(currentSap);
    CredentialSpecification credSpec = sap.getCredentialSpecification();
    URI policyIssuerParametersUID =
        sap.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();

    IssuanceHelper.log.info("Check Credential Specification / Issuance Policy : "
        + credSpec.getSpecificationUID() + " : " + policyIssuerParametersUID);


    this.checkIfCredSpecIsInKeystoreAddIfAbsent(keyManager, credSpec);

    URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");

    URI issuerParamsUid;
    if (sap.issuerParamsUid_URI != null) {
      issuerParamsUid = new URI(sap.issuerParamsUid);
    } else {
      // use default!
      boolean urnScheme = "urn".equals(policyIssuerParametersUID.getScheme());
      issuerParamsUid =
          URI.create(policyIssuerParametersUID
              + ((urnScheme ? ":" : "/") + sap.cryptoTechnology.toString()).toLowerCase());
      sap.issuerParamsUid_URI = issuerParamsUid;
    }
    if (!sap.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID()
        .equals(issuerParamsUid)) {
      log.fine("PATCH : " + issuerParamsUid + " : "
          + sap.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID());
      sap.getIssuancePolicy().getCredentialTemplate().setIssuerParametersUID(issuerParamsUid);
    }

    URI revocationParamsUid;
    if (sap.revocationParamsUid != null) {
      revocationParamsUid = new URI(sap.revocationParamsUid);
    } else {
      revocationParamsUid = null; // new URI(credSpec.getSpecificationUID() + (urnScheme ? ":" :
                                  // "/") + "revocationUID");
    }
    sap.revocationParamsUid_URI = revocationParamsUid;

    IssuanceHelper.log.info("Check Issuance Parameters : " + issuerParamsUid + " : "
        + revocationParamsUid);

    IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParamsUid);
    if (issuerParameters != null) {
      IssuanceHelper.log.info(" - issuer params exists! " + issuerParamsUid + " - with UID : "
          + issuerParameters.getParametersUID() + " -  and version number : "
          + issuerParameters.getVersion());

      IssuanceHelper.log.info(" : " + issuerParameters.getSystemParametersUID());
    } else {
      this.setupAndStoreIssuerParameters(
          currentSap.cryptoTechnology,
          // initEngine,
          // keyManager,
          // credentialManager,
          systemAndIssuerParamsPrefix, systemParameters, credSpec, hash, issuerParamsUid,
          revocationParamsUid, sap.friendlyDescriptions, currentSap.maximalNumberOfAttributes,
          currentSap.numberOfTokens);
    }

    // Needed ??
    // sap.issuancePolicy.getCredentialTemplate().setIssuerParametersUID(
    // issuerParameters.getParametersUID());

    // sap.getIssuancePolicy().getCredentialTemplate().setIssuerParametersUID(issuerParamsUid);

    // String sapKey = sap.key + "::" + cryptoEngine;
    // IssuanceHelper.log.info(" - add spec/policy : " + sapKey + " : "
    // + sap.getCredentialSpecification().getSpecificationUID());


    this.specAndPolicyMap.put(sap.key, sap);
  }

  private IssuerParameters setupAndStoreIssuerParameters(CryptoTechnology cryptoTechnology,
      String systemAndIssuerParamsPrefix, SystemParameters systemParameters,
      CredentialSpecification credSpec, URI hash, URI issuerParamsUid, URI revocationParamsUid,
      List<FriendlyDescription> friendlyDescriptions, int maximalNumberOfAttributes,
      int numberOfTokens) throws Exception {
    IssuerParameters issuerParameters;
    IssuanceHelper.log.info(" - create Issuer Parameters : " + cryptoTechnology.getURI() + " : "
        + issuerParamsUid);

    issuerParameters =
        singleEngine.setupIssuerParameters(systemParameters, maximalNumberOfAttributes,
            cryptoTechnology.getURI(), issuerParamsUid, revocationParamsUid, friendlyDescriptions);


    IssuanceHelper.log.info(" - store Issuer Parameters! " + issuerParamsUid + " : "
        + issuerParameters + " - with version number : " + issuerParameters.getVersion());

    if (cryptoTechnology == CryptoTechnology.UPROVE) {
      // set number of attributes!
//      PublicKey pk =
//      ((JAXBElement<PublicKey>) issuerParameters.getCryptoParams().getContent().get(0)).getValue();
  	  XmlUtils.fixNestedContent(issuerParameters.getCryptoParams());

      PublicKey pk =
      (PublicKey) issuerParameters.getCryptoParams().getContent().get(0);
      String uproveTokensKey = "urn:idmx:3.0.0:issuer:publicKey:uprove:tokens";
      for (Parameter p : pk.getParameter()) {
        if (uproveTokensKey.equals(p.getName())) {
          IntegerParameter ip = (IntegerParameter) p;
          ip.setValue(numberOfTokens);
          break;
        }
      }
    }

    keyManager.storeIssuerParameters(issuerParamsUid, issuerParameters);

    IssuanceHelper.log.info(" - save in file - spec : " + credSpec.getSpecificationUID()
        + " - key : " + issuerParamsUid + " - filename : "
        + getParamsFilename("issuer_params_", ".xml", issuerParamsUid));

    FileSystem.storeObjectAsXMLInFile(this.of.createIssuerParameters(issuerParameters),
        this.publicResourcesPrefix, getParamsFilename("issuer_params_", ".xml", issuerParamsUid));
    if (STORE_RESOURCES_AS_SERIALIZED_JAVA_OBJECTS) {
      FileSystem.storeObjectInFile(issuerParameters, this.publicResourcesPrefix,
          getParamsFilename("issuer_params_", ".obj", issuerParamsUid));
    }
    FileSystem.storeObjectAsXMLInFile(this.of.createCredentialSpecification(credSpec),
        this.publicResourcesPrefix, getParamsFilename("cred_spec_", ".xml", issuerParamsUid));


    if (credentialManager != null) {
      SecretKey issuerPrivateKeyForIssuerParameters =
          credentialManager.getIssuerSecretKey(issuerParamsUid);

      if (issuerPrivateKeyForIssuerParameters != null) {

        FileSystem.storeObjectAsXMLInFile(
            this.of.createIssuerSecretKey(issuerPrivateKeyForIssuerParameters),
            this.privateStoragePrefix,
            this.getParamsFilename("issuer_private_key_", ".xml", issuerParamsUid));

        if (STORE_RESOURCES_AS_SERIALIZED_JAVA_OBJECTS) {
          FileSystem.storeObjectInFile(issuerPrivateKeyForIssuerParameters,
              this.privateStoragePrefix,
              this.getParamsFilename("issuer_private_key_", ".obj", issuerParamsUid));
        }
      }
    }

    IssuanceHelper.log.info(" - created issuerParameters with UID : "
        + issuerParameters.getParametersUID());

    return issuerParameters;
  }

  private String getParamsFilename(String prefix, String suffix, URI issuerParamsUid) {
    boolean urnScheme = "urn".equals(issuerParamsUid.getScheme());
    String issuer_params_filename = prefix;
    if (urnScheme) {
      issuer_params_filename += issuerParamsUid.toASCIIString().replaceAll(":", "_");
    } else {
      issuer_params_filename +=
          issuerParamsUid.getHost().replace(".", "_") + issuerParamsUid.getPath().replace("/", "_");
    }
    return issuer_params_filename + suffix;
  }

  private String getParamsFilename(String prefix, URI issuerParamsUid, boolean urnScheme) {
    String issuer_params_filename = prefix;
    if (urnScheme) {
      issuer_params_filename += issuerParamsUid.toASCIIString().replaceAll(":", "_");
    } else {
      issuer_params_filename +=
          issuerParamsUid.getHost().replace(".", "_") + issuerParamsUid.getPath().replace("/", "_");
    }
    return issuer_params_filename;
  }

  private String getCredSpecFilename(URI uid) {
    String filename = "cred_spec_";
    filename += uid.toASCIIString().replaceAll(":", "_");
    filename = filename.replaceAll(":", "_").replace("/", "_");

    return filename;
  }

  private void checkIfCredSpecIsInKeystoreAddIfAbsent(KeyManager keyManager,
      CredentialSpecification credSpec) throws KeyManagerException {
    CredentialSpecification credSpecInKeystore =
        keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
    if (credSpecInKeystore != null) {
      IssuanceHelper.log.info(" - credspec already in keystore : " + credSpec.getSpecificationUID()
          + " : " + credSpec);
      try {
        IssuanceHelper.log
            .info("credSpec: "
                + XmlUtils.toXml(new ObjectFactory()
                    .createCredentialSpecification(credSpecInKeystore)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      IssuanceHelper.log.info(" - store credspec in keystre : ");
      keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);
      try {
        IssuanceHelper.log.info("credSpec: "
            + XmlUtils.toXml(new ObjectFactory().createCredentialSpecification(credSpec)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // called for Service..
  public IssuerParameters setupIssuerParameters(CryptoEngine cryptoEngine,
      CredentialSpecification credSpec, SystemParameters systemParameters, URI issuerParamsUid,
      URI hash, URI revocationParamsUid, String systemAndIssuerParamsPrefix,
      List<FriendlyDescription> friendlyDescriptions) throws Exception {
    IssuerParameters issuerParameters = null;
    IssuanceHelper.log.info("cryptoEngine: " + cryptoEngine);

    CryptoTechnology ct;
    switch (cryptoEngine) {
      case IDEMIX:
        ct = CryptoTechnology.IDEMIX;
        break;
      case UPROVE:
        ct = CryptoTechnology.UPROVE;
        break;
      default:
        throw new IllegalStateException("The crypto engine: " + cryptoEngine
            + " is not supported use IDEMIX or UPROVE instead");
    }

    issuerParameters =
        this.setupAndStoreIssuerParameters(ct, systemAndIssuerParamsPrefix, systemParameters,
            credSpec, hash, issuerParamsUid, revocationParamsUid, friendlyDescriptions, 20, 10);

    return issuerParameters;
  }

  public void registerSmartcardScopeExclusivePseudonym(BigInteger pse) throws IOException {
    // TODO : VERIFY : Must match the way pseudonums are created...
    String primaryKey = DatatypeConverter.printBase64Binary(pse.toByteArray());

    if (!issuerTokenStorage.checkForPseudonym(primaryKey)) {
      log.info("registerSmartcardScopeExclusivePseudonym - register new pseudonym  - BigInteger: "
          + pse + " - PseudonymPrimaryKey : " + primaryKey);
      issuerTokenStorage.addPseudonymPrimaryKey(primaryKey);
    } else {
      log.info("registerSmartcardScopeExclusivePseudonym - already registered");
    }
  }

  /**
   * Creates a new instance of SpecAndPolicy
   * 
   * @param credspecAndPolicyKey
   * @return
   * @throws Exception
   */
  public SpecAndPolicy initSpecAndPolicy(String specAndPolicyKey) throws Exception {

    SpecAndPolicy cached = this.specAndPolicyMap.get(specAndPolicyKey);

    log.fine("initSpecAndPolicy - cached! \n- "
        + XmlUtils.toXml(this.of.createIssuancePolicy(cached.getIssuancePolicy())));

    SpecAndPolicy cloned = new SpecAndPolicy(cached);
    return cloned;
  }

  private void modifyPA(PresentationPolicy pp) throws Exception {
    log.info("modifyPA - sdd RevocationInformation UIDs to Policy.");

    // try to make sure that RevocationInformation is only fetch once per RevAuth
    Map<URI, RevocationInformation> revocationInformationMap =
        new HashMap<URI, RevocationInformation>();

    // REVOCATION!
    for (CredentialInPolicy cred : pp.getCredential()) {
      List<URI> credSpecURIList = cred.getCredentialSpecAlternatives().getCredentialSpecUID();
      boolean containsRevoceableCredential = false;
      CredentialSpecification credSpec = null;
      for (URI uri : credSpecURIList) {
        try {
          credSpec = this.keyManager.getCredentialSpecification(uri);
          if (credSpec.isRevocable()) {
            containsRevoceableCredential = true;
            break;
          }
        } catch (KeyManagerException ignore) {}
      }
      if (containsRevoceableCredential) {
        IssuerAlternatives ia = cred.getIssuerAlternatives();
        log.fine("WE HAVE REVOCEABLE CREDENTIAL : " + ia);
        for (IssuerParametersUID ipUid : ia.getIssuerParametersUID()) {
          IssuerParameters ip = this.keyManager.getIssuerParameters(ipUid.getValue());
          if ((ip != null) && (ip.getRevocationParametersUID() != null)) {
            // issuer params / credspec has revocation...
            RevocationInformation ri =
                revocationInformationMap.get(ip.getRevocationParametersUID());
            log.fine("RevocationInformation : " + ri);
            if (ri == null) {
              log.fine("Getting rev parameters uid information: " + ip.getRevocationParametersUID());
              log.fine("Getting rev parameters uid information: "
                  + keyManager.getRevocationAuthorityParameters(ip.getRevocationParametersUID()));

              ri = this.keyManager.getLatestRevocationInformation(ip.getRevocationParametersUID());
              revocationInformationMap.put(ip.getRevocationParametersUID(), ri);
            }
            log.fine("RevocationInformation : " + ri.getRevocationInformationUID());
            URI revInfoUid = ri.getRevocationInformationUID();
            ipUid.setRevocationInformationUID(revInfoUid);
          }
        }
      }
    }
    log.fine(" - presentationPolicy modified to include RevocationInformation");
  }


  private SpecAndPolicy initSpecAndPolicyFromResouces(SpecAndPolicy cloneThisSap) throws Exception {
    SpecAndPolicy sap = new SpecAndPolicy(cloneThisSap);

    IssuanceHelper.log.info("initSpecAndPolicyFromResouces : " + sap + " - " + sap.specResource
        + " : " + sap.policyResource + " : " + sap.issuerParamsUid);
    InputStream is;
    CredentialSpecification credSpec = this.initCredentialSpecificationFromResources(sap);

    is = FileSystem.getInputStream(sap.policyResource);
    if (is == null) {
      throw new IllegalStateException("Illegal resource name for IssuancePolicy : "
          + sap.policyResource);
    }
    IssuancePolicy issuancePolicy = (IssuancePolicy) XmlUtils.getObjectFromXML(is, true);

    if (!credSpec.getSpecificationUID().equals(
        issuancePolicy.getCredentialTemplate().getCredentialSpecUID())) {
      throw new IllegalStateException(
          "SpecificationUID must mactch for CredentialSpecification and IssuancePolicy : "
              + credSpec.getSpecificationUID() + " != "
              + issuancePolicy.getCredentialTemplate().getCredentialSpecUID());
    }
    sap.setCredentialSpecification(credSpec);
    sap.setIssuancePolicy(issuancePolicy);

    return sap;
  }

  private CredentialSpecification initCredentialSpecificationFromResources(SpecAndPolicy sap)
      throws IOException, JAXBException, UnsupportedEncodingException, SAXException {
    InputStream is = FileSystem.getInputStream(sap.specResource);
    if (is == null) {
      throw new IllegalStateException("Illegal resource name for CredSpec : " + sap.specResource);
    }
    CredentialSpecification credSpec =
        (CredentialSpecification) XmlUtils.getObjectFromXML(is, true);
    return credSpec;
  }


  /**
   * Process next step of issuance on IssuanceMessager
   * 
   * @param issuanceMessage IssuanceMessager as String
   * @return
   * @throws Exception
   */
  public IssuanceMessageAndBoolean issueStep(IssuanceMessage issuanceMessage) throws Exception {
    IssuanceHelper.log.info("IssuanceHelper - step_jaxb - marchalled object: " + issuanceMessage);

    if (this.singleEngine == null) {
      throw new IllegalStateException(
          "IssuanceHelper.issueStep called without specifying CryptoEngine!");
    }
    return this.issueStep(this.singleEngine, issuanceMessage);
  }

  private IssuanceMessageAndBoolean issueStep(IssuerAbcEngine useEngine,
      IssuanceMessage issuanceMessage) throws Exception {

    IssuanceMessageAndBoolean response;
    try {
      response = useEngine.issuanceProtocolStep(issuanceMessage);
    } catch (Exception e) {
      System.err
          .println("- IssuerABCE could not process Step IssuanceMessage from UserABCE : " + e);

      throw new Exception("Could not process next step on issuauce : ", e);
    }
    if (response.isLastMessage()) {
      IssuanceHelper.log.info(" - last step - on server");
    } else {
      IssuanceHelper.log.info(" - continue steps");
    }

    return response;
  }

  public IssuanceMessageAndBoolean reIssueStep(IssuanceMessage issuanceMessage) throws Exception {

    IssuanceMessageAndBoolean response;
    try {
      response = this.singleEngine.reIssuanceProtocolStep(issuanceMessage);
    } catch (Exception e) {
      System.err
          .println("- IssuerABCE could not process Step IssuanceMessage from UserABCE : " + e);

      throw new Exception("Could not process next step on issuauce : ", e);
    }
    if (response.isLastMessage()) {
      IssuanceHelper.log.info(" - last step - on server");
    } else {
      IssuanceHelper.log.info(" - continue steps");
    }
    return response;
  }

  public IssuanceMessage initReIssuance(String specAndPolicyKey) throws Exception {

    SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(specAndPolicyKey);
    if (specAndPolicy == null) {
      IssuanceHelper.log.info("IssuanceHelper - initReIssuance : " + specAndPolicyKey + " : "
          + this.specAndPolicyMap);
      throw new IllegalStateException("Unknown Spec And Policy Key " + specAndPolicyKey);
    }

    IssuanceMessageAndBoolean initReIssuanceProtocol =
        this.singleEngine.initReIssuanceProtocol(specAndPolicy.cloneIssuancePolicy());
    return initReIssuanceProtocol.getIssuanceMessage();
  }


  /**
   * Performs first step of issuance
   * 
   * @param credspecAndPolicyKey key identifying SpecAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceMessage initIssuance(String credspecAndPolicyKey,
      Map<String, Object> attributeValueMap) throws Exception {
    IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + credspecAndPolicyKey + " : "
        + specAndPolicyMap.keySet());
    if (this.singleEngine == null) {
      throw new IllegalStateException(
          "IssuanceHelper.initIssuance called without specifying CryptoEngine!");
    }

    SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(credspecAndPolicyKey);
    if (specAndPolicy == null) {
      IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + credspecAndPolicyKey + " : "
          + this.specAndPolicyMap);
      throw new IllegalStateException("Unknown Spec And Policy Key " + credspecAndPolicyKey);
    }
    return this.initIssuance(this.singleEngine, specAndPolicy, attributeValueMap);
  }

  /**
   * Performs first step of issuance
   * 
   * @param specAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceMessage initIssuance(SpecAndPolicy specAndPolicy,
      Map<String, Object> attributeValueMap) throws Exception {
    if (this.singleEngine == null) {
      throw new IllegalStateException(
          "IssuanceHelper.initIssuance called without specifying CryptoEngine!");
    }
    return this.initIssuance(this.singleEngine, specAndPolicy, attributeValueMap);
  }


  private IssuanceMessage initIssuance(IssuerAbcEngine useEngine, SpecAndPolicy specAndPolicy,
      Map<String, Object> attributeValueMap) throws Exception {

    List<Attribute> issuerAtts = new ArrayList<Attribute>();

    this.populateIssuerAttributes(specAndPolicy, issuerAtts, attributeValueMap);

    IssuancePolicy clonedIssuancePolicy = specAndPolicy.cloneIssuancePolicy();
    URI policyIssuerParametersUID = specAndPolicy.issuerParamsUid_URI;
    modifyPA(clonedIssuancePolicy.getPresentationPolicy());
    return this.initIssuanceProtocol(useEngine, issuerAtts, clonedIssuancePolicy,
        policyIssuerParametersUID).getIssuanceMessage();
  }

  private IssuanceMessageAndBoolean initIssuanceProtocol(IssuerAbcEngine useEngine,
      List<Attribute> issuerAtts, IssuancePolicy clonedIssuancePolicy, URI policyIssuerParametersUID)
      throws Exception {
    IssuanceMessageAndBoolean response = null;
    try {


      IssuanceHelper.log.info(" - call ABCE - policy : " + clonedIssuancePolicy + " : "
          + policyIssuerParametersUID + " - attributes : " + issuerAtts);
      response = useEngine.initIssuanceProtocol(clonedIssuancePolicy, issuerAtts);

    } catch (Exception e) {
      System.err.println("- got Exception from ABCE Engine - try to create sample XML");
      e.printStackTrace();
      throw new Exception("Failed to initIsuanceProtocol", e);
    }

    if (response.isLastMessage()) {
      // cannot be last message
      throw new IllegalStateException(
          "Internal error in IssuerABCEngine - lastmessage returned from initIssuanceProtocol");
    }

    return response;
  }

  private void populateIssuerAttributes(SpecAndPolicy specAndPolicy, List<Attribute> issuerAtts,
      Map<String, Object> attributeValueMap) {

    CredentialSpecification credSpec = specAndPolicy.getCredentialSpecification();

    // TODO - to make proper check - also check 'unknown' from IssuancePolicy
    // eg findUnknownAttributtes(ip);
    // and check that all attribues in credspecs are matched..
    if (credSpec.getAttributeDescriptions().getAttributeDescription().size() < attributeValueMap
        .size()) {
      throw new IllegalStateException("Wrong number of attributes ? - in credspec : "
          + credSpec.getAttributeDescriptions().getAttributeDescription().size() + " - in map "
          + attributeValueMap.size());
    }


    Map<String, AttributeDescription> adMap = new HashMap<String, AttributeDescription>();
    for (AttributeDescription ad : credSpec.getAttributeDescriptions().getAttributeDescription()) {
      adMap.put(ad.getType().toString(), ad);
    }

    Set<String> definedAttributes = attributeValueMap.keySet();
    for (String key : definedAttributes) {
      AttributeDescription ad = adMap.get(key);
      if (ad == null) {
        throw new IllegalStateException("No Attribute in Credspec with type : " + key);
      }

      Object value = attributeValueMap.get(key);

      Attribute attribute = this.of.createAttribute();
      // TODO : Howto create vaules ??
      attribute.setAttributeUID(URI.create("" + this.random.nextLong()));

      Object xmlValue =
          new AttributeValueConverter().convertValue(ad.getDataType().toString(), value);
      // this.log.info("- xml Value : " + xmlValue);
      attribute.setAttributeValue(xmlValue);
      attribute.setAttributeDescription(this.of.createAttributeDescription());
      attribute.getAttributeDescription().setDataType(URI.create(ad.getDataType().toString()));
      attribute.getAttributeDescription().setEncoding(URI.create(ad.getEncoding().toString()));
      attribute.getAttributeDescription().setType(URI.create(ad.getType().toString()));

      // TODO:SETTING Friendly's should be handled inside User Engine!!!
      attribute.getAttributeDescription().getFriendlyAttributeName()
          .addAll(ad.getFriendlyAttributeName());

      //
      issuerAtts.add(attribute);
    }
  }

  // for service
  public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy issuancePolicy,
      List<Attribute> attributes) throws Exception {
    IssuanceMessageAndBoolean issuanceMessageAndBoolean = null;

    URI issuerPolicyParametersUid = issuancePolicy.getCredentialTemplate().getIssuerParametersUID();

    issuanceMessageAndBoolean =
        this.initIssuanceProtocol(singleEngine, attributes, issuancePolicy,
            issuerPolicyParametersUid);
    return issuanceMessageAndBoolean;
  }

  @Override
  public void addCredentialSpecifications(String[] credSpecResourceList) {
    super.addCredentialSpecifications(credSpecResourceList);
  }

  @Override
  public void addIssuerParameters(String[] issuerParametersResourceList) {
    super.addIssuerParameters(issuerParametersResourceList);
  }

  public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid) throws Exception {
    return this.singleEngine.getIssuanceLogEntry(issuanceEntryUid);
  }
}

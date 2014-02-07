//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.SynchronizedIssuerAbcEngineImpl;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.cryptoEngine.idemix.user.IdemixCryptoEngineUserImpl;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModule;
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
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

@SuppressWarnings("deprecation")
public class IssuanceHelper extends AbstractHelper {

    static Logger log = Logger.getLogger(IssuanceHelper.class.getName());

    private static IssuanceHelper instance;

    /**
     * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
     *        IssuerAbcEnginge
     * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
     * @return
     * @throws URISyntaxException
     */
    @Deprecated
    public static synchronized IssuanceHelper initInstance(CryptoEngine cryptoEngine,
            String systemAndIssuerParamsPrefix, String fileStoragePrefix,
            ArrayList<SpecAndPolicy> specAndPolicyList) throws URISyntaxException {
        if (instance != null) {
            throw new IllegalStateException("initInstance can only be called once!");
        }
        log.info("IssuanceHelper.initInstance(ArrayList)");

        instance =
                new IssuanceHelper(cryptoEngine, null, systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0],
                        specAndPolicyList);

        return instance;
    }

    /**
     * @param fileStoragePrefix
     *            this prefix will be prepended on storage files needed by the
     *            IssuerAbcEnginge
     * @param specAndPolicyList
     *            list of CredentialSpecifications + IssuancePolices
     * @param createNewSystemParametersIfNoneExists
     * @return
     * @throws URISyntaxException
     */
    public static synchronized IssuanceHelper initInstance(
            CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix,
            String fileStoragePrefix, SpecAndPolicy... specAndPolicyList)
                    throws URISyntaxException {
        if (instance != null) {
            throw new IllegalStateException("initInstance can only be called once!");
        }
        log.info("IssuanceHelper.initInstance(Array)");

        ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
        for (SpecAndPolicy sap : specAndPolicyList) {
            list.add(sap);
        }
        instance = new IssuanceHelper(cryptoEngine, null,
                systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0],
                list);

        return instance;
    }

    public static synchronized IssuanceHelper initInstanceForService(
            CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix,
            String fileStoragePrefix, Module... modules) throws Exception {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        log.info("IssuanceHelper.initInstanceForService(Array)");

        instance = new IssuanceHelper(cryptoEngine,
                systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0],
                modules);

        return instance;
    }

    public static synchronized StorageFiles verifyFiles(
            boolean wipe_existing_storage, String fileStoragePrefix,
            CryptoEngine cryptoEngine) throws Exception {
        String v = getFileStoragePrefix(fileStoragePrefix, cryptoEngine);
        return AbstractHelper.verifyFiles(wipe_existing_storage, v);
    }

    /**
     * Private constructor
     * 
     * @param fileStoragePrefix
     *            this prefix will be prepended on storage files needed by the
     *            IssuerAbcEnginge
     * @throws Exception
     */
    private IssuanceHelper(CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix,
            String fileStoragePrefix,
            String[] revocationAuthorityParametersResourcesList,
            Module... modules)
                    throws Exception {
        IssuanceHelper.log
        .info("IssuanceHelper : create instance for issuer service "
                + cryptoEngine + " : " + fileStoragePrefix);
        this.cryptoEngine = cryptoEngine;
        this.systemAndIssuerParamsPrefix = systemAndIssuerParamsPrefix;
        this.fileStoragePrefix = fileStoragePrefix;
        this.systemParametersResource = this.fileStoragePrefix
                + SYSTEM_PARAMS_NAME_BRIDGED;

        UProveUtils uproveUtils = new UProveUtils();
        AbceConfigurationImpl configuration = this.setupSingleEngineForService(
                cryptoEngine, uproveUtils,
                revocationAuthorityParametersResourcesList, modules);

        this.random = configuration.getPrng(); // new SecureRandom(); // new Random(1985);
    }

    //    @Deprecated
    //    /**
    //     * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
    //     *        IssuerAbcEnginge
    //     * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
    //     * @return
    //     * @throws URISyntaxException
    //     */
    //    public static synchronized IssuanceHelper initInstance(
    //            ProductionModule.CryptoEngine cryptoEngine,
    //            String systemAndIssuerParamsPrefix, String fileStoragePrefix,
    //            SpecAndPolicy... specAndPolicyList) throws URISyntaxException {
    //        if (instance != null) {
    //            throw new IllegalStateException("initInstance can only be called once!");
    //        }
    //        log.info("IssuanceHelper.initInstance(Array)");
    //
    //        ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
    //        for (SpecAndPolicy sap : specAndPolicyList) {
    //            list.add(sap);
    //        }
    //
    //        instance = new IssuanceHelper(
    //                oldCryptoEngineToNewCryptoEngine(cryptoEngine),
    //                systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0],
    //                list);
    //
    //        return instance;
    //    }
    //
    //    @Deprecated
    //    public static synchronized IssuanceHelper initInstance(
    //            ProductionModule.CryptoEngine cryptoEngine,
    //            String systemAndIssuerParamsPrefix, String fileStoragePrefix,
    //            SpecAndPolicy[] specAndPolicyList,
    //            String[] revocationAuthorityParametersResourcesList)
    //                    throws URISyntaxException {
    //        return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine),
    //                systemAndIssuerParamsPrefix, fileStoragePrefix,
    //                specAndPolicyList, revocationAuthorityParametersResourcesList);
    //    }
    public static synchronized IssuanceHelper initInstanceBridged(int idemixKeyLength, int uproveKeyLength, int numberOfUProverCredentials, String systemAndIssuerParamsPrefix, String fileStoragePrefix,
            SpecAndPolicy[] specAndPolicyList,
            String[] revocationAuthorityParametersResourcesList)
                    throws URISyntaxException {
        IDEMIX_KEY_LENGTH = idemixKeyLength;
        UPROVE_KEY_LENGTH = uproveKeyLength;
        UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE = numberOfUProverCredentials;

        return initInstance(CryptoEngine.BRIDGED, systemAndIssuerParamsPrefix, fileStoragePrefix, specAndPolicyList, revocationAuthorityParametersResourcesList);
    }

    public static synchronized IssuanceHelper initInstance(CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix, String fileStoragePrefix,
            SpecAndPolicy[] specAndPolicyList,
            String[] revocationAuthorityParametersResourcesList)
                    throws URISyntaxException {
        // wit null's its still top level issuer...
        return initInstanceWithExitingSystemPareters(cryptoEngine, null, new String[0], new String[0], systemAndIssuerParamsPrefix, fileStoragePrefix,
                specAndPolicyList,
                revocationAuthorityParametersResourcesList);
    }
    public static synchronized IssuanceHelper initInstanceWithExitingSystemPareters(CryptoEngine cryptoEngine, String systemParametersResource, String[] foreignCredSpecResourceList, String[] foreignIssuerParamResourceList, String systemAndIssuerParamsPrefix, String fileStoragePrefix,
            SpecAndPolicy[] specAndPolicyList,
            String[] revocationAuthorityParametersResourcesList)
                    throws URISyntaxException {


        if (instance != null) {
            throw new IllegalStateException("initInstance can only be called once!");
        }
        log.info("IssuanceHelper.initInstance(Array)");

        ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
        for (SpecAndPolicy sap : specAndPolicyList) {
            list.add(sap);
        }

        instance =
                new IssuanceHelper(cryptoEngine, systemParametersResource,
                        systemAndIssuerParamsPrefix, fileStoragePrefix,
                        revocationAuthorityParametersResourcesList, list);

        if(cryptoEngine == CryptoEngine.BRIDGED) {
            instance.keyManager = instance.idemixKeyManager;
            instance.addCredentialSpecifications(foreignCredSpecResourceList);
            instance.addIssuerParameters(foreignIssuerParamResourceList);
            instance.keyManager = instance.uproveKeyManager;
            instance.addCredentialSpecifications(foreignCredSpecResourceList);
            instance.addIssuerParameters(foreignIssuerParamResourceList);
        } else if(cryptoEngine == CryptoEngine.IDEMIX) {
            instance.keyManager = instance.idemixKeyManager;
            instance.addCredentialSpecifications(foreignCredSpecResourceList);
            instance.addIssuerParameters(foreignIssuerParamResourceList);
        } else if(cryptoEngine == CryptoEngine.UPROVE) {
            instance.keyManager = instance.uproveKeyManager;
            instance.addCredentialSpecifications(foreignCredSpecResourceList);
            instance.addIssuerParameters(foreignIssuerParamResourceList);
        } else {
            instance.addIssuerParameters(foreignIssuerParamResourceList);
        }
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
        System.err.println("WARNING IssuanceHelper.resetInstance : " + instance);
        if((instance!=null) && (instance.uproveBindingManager != null) ) {
            try {
                // try to stop uprove engine/service if running...
                //                instance.uproveBindingManager.stop();
            } catch(Exception ignore) {
                System.err.println("Failed to stop UProve service : " + ignore);
            }

        }
        instance = null;
    }

    /**
     * @return initialized instance of IssuanceHelper
     */
    public static synchronized IssuanceHelper getInstance() {
        log.info("IssuanceHelper.getInstance : " + instance
                + (instance == null ? "" : " : " + instance.cryptoEngine));
        if (instance == null) {
            throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
        }
        return instance;
    }

    private IssuerAbcEngine singleEngine = null;
    private IssuerAbcEngine uproveEngine = null;
    private IssuerAbcEngine idemixEngine = null;

    // needed for 'reset'
    private UProveBindingManager uproveBindingManager = null;

    private final Map<String, SpecAndPolicy> specAndPolicyMap = new HashMap<String, SpecAndPolicy>();

    private Random random;

    private final ObjectFactory of = new ObjectFactory();

    private final List<TokenStorageIssuer> issuerStorageManagerList = new ArrayList<TokenStorageIssuer>();
    private KeyManager uproveKeyManager;
    private KeyManager idemixKeyManager;

    private final String systemAndIssuerParamsPrefix;
    private final String fileStoragePrefix;
    private final String systemParametersResource;

    private CredentialManager credentialManager;

    /**
     * Private constructor
     * 
     * @param fileStoragePrefix
     *            this prefix will be prepended on storage files needed by the
     *            IssuerAbcEnginge
     * @param specAndPolicyList
     *            list of CredentialSpecifications + IssuancePolices
     * @param createNewSystemParametersIfNoneExists
     * @throws URISyntaxException
     */
    private IssuanceHelper(CryptoEngine cryptoEngine, String systemParametersResource, String systemAndIssuerParamsPrefix,
            String fileStoragePrefix,
            String[] revocationAuthorityParametersResourcesList,
            ArrayList<SpecAndPolicy> specAndPolicyList)
                    throws URISyntaxException {
        IssuanceHelper.log.info("IssuanceHelper : create instance " + cryptoEngine
                + " : "
                + fileStoragePrefix + " : " + specAndPolicyList);

        this.cryptoEngine = cryptoEngine;
        this.systemAndIssuerParamsPrefix = systemAndIssuerParamsPrefix;
        this.fileStoragePrefix = fileStoragePrefix;
        if(systemParametersResource==null) {
            this.systemParametersResource = this.fileStoragePrefix + SYSTEM_PARAMS_NAME_BRIDGED;
        } else {
            this.systemParametersResource = systemParametersResource;
        }
        try {
            UProveUtils uproveUtils = new UProveUtils();

            switch (cryptoEngine) {
            case BRIDGED: {
                this.setupIdemixEngine(cryptoEngine,
                        revocationAuthorityParametersResourcesList,
                        specAndPolicyList, uproveUtils);
            }
            {
                AbceConfigurationImpl configuration = this
                        .setupUProveEngine(cryptoEngine,
                                revocationAuthorityParametersResourcesList,
                                specAndPolicyList, uproveUtils);

                this.random = configuration.getPrng(); // new
                // SecureRandom(); //
                // new Random(1985);
            }
            break;

            default:
                AbceConfigurationImpl configuration = this.setupSingleEngine(
                        cryptoEngine,
                        revocationAuthorityParametersResourcesList,
                        specAndPolicyList, uproveUtils);

                this.random = configuration.getPrng(); // new SecureRandom(); // new Random(1985);
                break;
            }


        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup issuer !", e);
        }
    }

    private AbceConfigurationImpl setupSingleEngineForService(
            CryptoEngine cryptoEngine, UProveUtils uproveUtils,
            String[] revocationAuthorityParametersResourcesList,
            Module... modules)
                    throws Exception {

        AbceConfigurationImpl configuration = this.setupConfiguration(
                cryptoEngine, uproveUtils, cryptoEngine);

        Module newModule = ProductionModuleFactory
                .newModule(configuration, cryptoEngine);
        Module combinedModule = Modules.override(newModule).with(modules);
        Injector injector = Guice.createInjector(combinedModule);

        IssuerAbcEngine engine = injector.getInstance(IssuerAbcEngine.class);

        this.singleEngine = new SynchronizedIssuerAbcEngineImpl(engine);

        if (cryptoEngine == CryptoEngine.UPROVE) {
            this.uproveBindingManager = injector
                    .getInstance(UProveBindingManager.class);
        }

        this.keyManager = injector.getInstance(KeyManager.class);

        String systemParametersResource = this.fileStoragePrefix
                + SYSTEM_PARAMS_NAME_BRIDGED;

        SystemParameters systemParameters = SystemParametersHelper
                .checkAndLoadSystemParametersIfAbsent(this.keyManager,
                        systemParametersResource);

        if (systemParameters == null) {
            IssuanceHelper.log.info("No system parameters loaded");
        }

        this.issuerStorageManagerList.add(injector
                .getInstance(TokenStorageIssuer.class));
        this.addRevocationAuthorities(this.keyManager,
                revocationAuthorityParametersResourcesList);

        return configuration;
    }

    private AbceConfigurationImpl setupSingleEngine(CryptoEngine cryptoEngine,
            String[] revocationAuthorityParametersResourcesList,
            ArrayList<SpecAndPolicy> specAndPolicyList,
            UProveUtils uproveUtils)
                    throws Exception {
        AbceConfigurationImpl configuration = this.setupConfiguration(
                cryptoEngine, uproveUtils,
                cryptoEngine);


        Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(configuration, cryptoEngine));
        IssuerAbcEngine eng = injector.getInstance(IssuerAbcEngine.class);
        this.singleEngine = new SynchronizedIssuerAbcEngineImpl(eng);
        if(cryptoEngine==CryptoEngine.UPROVE) {
            this.uproveBindingManager = injector.getInstance(UProveBindingManager.class);
        }

        this.keyManager = injector.getInstance(KeyManager.class);
        IssuerAbcEngine engine = this.singleEngine;

        this.credentialManager = injector
                .getInstance(CredentialManager.class);

        this.initSystemAndIssuerParams(this.keyManager,
                revocationAuthorityParametersResourcesList,
                specAndPolicyList,
                cryptoEngine, injector, engine, this.credentialManager);

        return configuration;
    }


    private void setupIdemixEngine(CryptoEngine cryptoEngine,
            String[] revocationAuthorityParametersResourcesList,
            ArrayList<SpecAndPolicy> specAndPolicyList,
            UProveUtils uproveUtils)
                    throws Exception {
        CryptoEngine specificCryptoEngine = CryptoEngine.IDEMIX;
        AbceConfigurationImpl configuration = this.setupConfiguration(
                cryptoEngine, uproveUtils,
                specificCryptoEngine);

        //
        Injector injector = Guice
                .createInjector(ProductionModuleFactory.newModule(
                        configuration, specificCryptoEngine));
        IssuerAbcEngine engine = injector
                .getInstance(IssuerAbcEngine.class);
        this.idemixEngine = new SynchronizedIssuerAbcEngineImpl(engine);

        this.credentialManager = injector
                .getInstance(CredentialManager.class);

        this.idemixKeyManager = injector.getInstance(KeyManager.class);
        engine = this.idemixEngine;
        this.initSystemAndIssuerParams(this.idemixKeyManager,
                revocationAuthorityParametersResourcesList, specAndPolicyList,
                specificCryptoEngine, injector, engine, this.credentialManager);
    }

    private AbceConfigurationImpl setupUProveEngine(CryptoEngine cryptoEngine,
            String[] revocationAuthorityParametersResourcesList,
            ArrayList<SpecAndPolicy> specAndPolicyList, UProveUtils uproveUtils)
                    throws Exception {
        CryptoEngine specificCryptoEngine = CryptoEngine.UPROVE;
        AbceConfigurationImpl configuration = this
                .setupConfiguration(cryptoEngine,
                        uproveUtils,
                        specificCryptoEngine);


        Injector injector = Guice
                .createInjector(ProductionModuleFactory.newModule(
                        configuration, specificCryptoEngine));
        IssuerAbcEngine engine = injector
                .getInstance(IssuerAbcEngine.class);
        this.uproveEngine = new SynchronizedIssuerAbcEngineImpl(
                engine);

        this.uproveBindingManager = injector
                .getInstance(UProveBindingManager.class);

        this.credentialManager = injector
                .getInstance(CredentialManager.class);

        this.uproveKeyManager = injector.getInstance(KeyManager.class);
        engine = this.uproveEngine;
        this.initSystemAndIssuerParams(this.uproveKeyManager,
                revocationAuthorityParametersResourcesList, specAndPolicyList,
                specificCryptoEngine, injector, engine, this.credentialManager);
        return configuration;
    }

    private void initSystemAndIssuerParams(KeyManager keyManager,
            String[] revocationAuthorityParametersResourcesList,
            ArrayList<SpecAndPolicy> specAndPolicyList,
            CryptoEngine cryptoEngine, Injector injector,
            IssuerAbcEngine engine, CredentialManager credentialManager)
                    throws Exception {

        this.initSystemParameters(keyManager);
        this.initParamsForEngine(cryptoEngine, engine, keyManager,
                specAndPolicyList, credentialManager);

        this.issuerStorageManagerList.add(injector
                .getInstance(TokenStorageIssuer.class));
        this.addRevocationAuthorities(keyManager,
                revocationAuthorityParametersResourcesList);
    }


    private AbceConfigurationImpl setupConfiguration(CryptoEngine cryptoEngine,
            UProveUtils uproveUtils, CryptoEngine specificCryptoEngine)
                    throws Exception {
        AbceConfigurationImpl configuration = this
                .setupStorageFilesForConfiguration(IssuanceHelper.getFileStoragePrefix(
                        this.fileStoragePrefix, specificCryptoEngine),
                        cryptoEngine);
        configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe()
                .getAbsolutePath());
        configuration.setUProvePortNumber(uproveUtils.getIssuerServicePort());
        configuration
        .setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
        configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);
        return configuration;
    }

    public static String getFileStoragePrefix(String filePrefix,
            CryptoEngine cryptoEngine) {
        if((filePrefix!=null) && (filePrefix.length()>0)) {
            if(filePrefix.endsWith("_")) {
                return filePrefix + ("" +cryptoEngine).toLowerCase() + "_";
            } else {
                if(filePrefix.endsWith("/") || filePrefix.endsWith("\\")) {
                    // this is a folder...
                    return filePrefix + ("" +cryptoEngine).toLowerCase() + "_";
                } else {
                    return filePrefix + ("_" +cryptoEngine).toLowerCase() + "_";
                }
            }
        }
        return ("" +cryptoEngine).toLowerCase();
    }

    private SystemParameters generatedSystemParameters = null;


    private void initSystemParameters(KeyManager keyManager) throws Exception {
        IssuanceHelper.log.info("initSystemParameters");
        String systemParametersResource;
        if(this.systemParametersResource!=null) {
            systemParametersResource = this.systemParametersResource;
            log.warning("### - sys param - has been set!!! " + systemParametersResource);
        } else {
            systemParametersResource = this.fileStoragePrefix + SYSTEM_PARAMS_NAME_BRIDGED;
            log.warning("### - fallback to default SystemParam resource name : " + systemParametersResource);
        }
        this.generatedSystemParameters = SystemParametersHelper
                .checkAndLoadSystemParametersIfAbsent(keyManager,
                        systemParametersResource);

        if (this.generatedSystemParameters != null) {
            return;
        }

        this.createNewSystemParametersWithIdemixSpecificKeylength(IDEMIX_KEY_LENGTH, UPROVE_KEY_LENGTH,
                keyManager);
    }

    public SystemParameters createNewSystemParametersWithIdemixSpecificKeylength(
            int idemixKeylength, int uproveKeylength) throws IOException,
            KeyManagerException, Exception {

        return this.createNewSystemParametersWithIdemixSpecificKeylength(
                idemixKeylength, uproveKeylength, this.keyManager);

    }

    private SystemParameters createNewSystemParametersWithIdemixSpecificKeylength(
            int idemixKeylength, int uproveKeylength, KeyManager keyManager)
                    throws IOException,
                    KeyManagerException, Exception {
        IssuanceHelper.log.info("- create new system parameters with keysize: "
                + idemixKeylength);
        // ok - we have to generate them from scratch...
        this.generatedSystemParameters = SystemParametersUtil
                .generatePilotSystemParameters_WithIdemixSpecificKeySize(
                        idemixKeylength, uproveKeylength, UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);

        // store in storage folder
        FileSystem.storeObjectInFile(this.generatedSystemParameters,
                this.systemParametersResource);

        if(! this.fileStoragePrefix.equals(this.systemAndIssuerParamsPrefix)) {
            // store in resource folder (for easier export to other ABCEs)
            String systemParametersResource_resourcePrefix = this.systemAndIssuerParamsPrefix + SYSTEM_PARAMS_NAME_BRIDGED;
            FileSystem.storeObjectInFile(this.generatedSystemParameters,
                    systemParametersResource_resourcePrefix);
        }

        // store in keyManager
        keyManager.storeSystemParameters(this.generatedSystemParameters);

        IdemixCryptoEngineUserImpl
        .loadIdemixSystemParameters(this.generatedSystemParameters);

        this.storeSystemParametersAsXML(this.fileStoragePrefix,
                this.generatedSystemParameters, SYSTEM_PARAMS_NAME_BRIDGED);

        IssuanceHelper.log.info("- new SystemParameters.");

        return this.generatedSystemParameters;
    }

    private void storeSystemParametersAsXML(String filePrefix,
            SystemParameters systemParameters, String name) throws Exception {
        SystemParameters serializedSystemParameters = SystemParametersUtil
                .serialize(systemParameters);
        JAXBElement<SystemParameters> asXml = this.of
                .createSystemParameters(serializedSystemParameters);
        FileSystem.storeObjectAsXMLInFile(asXml, filePrefix, name);
    }


    private void initParamsForEngine(CryptoEngine cryptoEngine,
            IssuerAbcEngine initEngine, KeyManager keyManager,
            ArrayList<SpecAndPolicy> specAndPolicyList,
            CredentialManager credentialManager) throws Exception {
        IssuanceHelper.log.info("initParamsForEngine : " + cryptoEngine + " : "
                + initEngine + " : " + keyManager);

        if (!keyManager.hasSystemParameters() || (this.generatedSystemParameters == null)) {
            throw new IllegalAccessException("initSystemParameters - should have setup SystemParameters");
        }

        SystemParameters systemParameters = null;
        systemParameters = keyManager.getSystemParameters();

        for (SpecAndPolicy currentSap : specAndPolicyList) {

            this.initParamForEngine(cryptoEngine, initEngine, keyManager,
                    this.systemAndIssuerParamsPrefix, systemParameters,
                    currentSap, credentialManager);
        }

    }

    private void initParamForEngine(CryptoEngine cryptoEngine,
            IssuerAbcEngine initEngine, KeyManager keyManager,
            String systemAndIssuerParamsPrefix,
            SystemParameters systemParameters, SpecAndPolicy currentSap,
            CredentialManager credentialManager)
                    throws Exception, KeyManagerException, URISyntaxException,
                    IOException {
        SpecAndPolicy sap = this.initSpecAndPolicyFromResouces(currentSap);
        CredentialSpecification credSpec = sap.getCredentialSpecification();
        URI policyIssuerParametersUID =
                sap.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();

        IssuanceHelper.log.info("Check Credential Specification / Issuance Policy : "
                + credSpec.getSpecificationUID() + " : " + policyIssuerParametersUID);


        this.checkIfCredSpecIsInKeystoreAddIfAbsent(keyManager, credSpec);


        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");


        URI issuerParamsUid;
        if (sap.issuerParamsUid != null) {
            issuerParamsUid = new URI(sap.issuerParamsUid);
        } else {
            // use default!
            issuerParamsUid = policyIssuerParametersUID;
        }
        boolean urnScheme = "urn".equals(issuerParamsUid.getScheme());
        issuerParamsUid =  URI.create(issuerParamsUid + ((urnScheme ? ":" : "/") + cryptoEngine).toLowerCase());

        sap.issuerParamsUid_URI = issuerParamsUid;

        URI revocationParamsUid;
        if ((sap.revocationParamsUid != null) && credSpec.isRevocable()) {
            revocationParamsUid = new URI(sap.revocationParamsUid);
        } else {
            revocationParamsUid = new URI(credSpec.getSpecificationUID() + (urnScheme ? ":" : "/") + "revocationUID");
        }
        sap.revocationParamsUid_URI = revocationParamsUid;

        IssuanceHelper.log.info("Check Issuance Parameters : " + issuerParamsUid + " : "
                + revocationParamsUid);

        IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParamsUid);
        if (issuerParameters != null) {
            IssuanceHelper.log.info(" - issuer params exists! " + issuerParamsUid
                    + " - with UID : "
                    + issuerParameters.getParametersUID() + " -  and version number : " + issuerParameters.getVersion());

            IssuanceHelper.log.info(" : " + issuerParameters.getSystemParameters());
        } else {
            this.setupAndStoreIssuerParameters(cryptoEngine, initEngine,
                    keyManager, credentialManager, systemAndIssuerParamsPrefix,
                    systemParameters,
                    credSpec, hash, issuerParamsUid, revocationParamsUid, sap.friendlyDescriptions);
        }

        // Needed ??
        // sap.issuancePolicy.getCredentialTemplate().setIssuerParametersUID(
        // issuerParameters.getParametersUID());

        sap.getIssuancePolicy().getCredentialTemplate().setIssuerParametersUID(issuerParamsUid);

        String sapKey = sap.key + "::" + cryptoEngine;
        IssuanceHelper.log.info(" - add spec/policy : " + sapKey + " : "
                + sap.getCredentialSpecification().getSpecificationUID());
        this.specAndPolicyMap.put(sapKey, sap);
    }

    private IssuerParameters setupAndStoreIssuerParameters(
            CryptoEngine cryptoEngine,
            IssuerAbcEngine initEngine,
            KeyManager keyManager, CredentialManager credentialManager,
            String systemAndIssuerParamsPrefix,
            SystemParameters systemParameters,
            CredentialSpecification credSpec, URI hash, URI issuerParamsUid,
            URI revocationParamsUid, List<FriendlyDescription> friendlyDescriptions) throws Exception {
        IssuerParameters issuerParameters;
        IssuanceHelper.log.info(" - create Issuer Parameters!");

        issuerParameters = this.setupIssuerParameters(cryptoEngine, initEngine,
                systemParameters, credSpec, hash, issuerParamsUid,
                revocationParamsUid, friendlyDescriptions);

        IssuanceHelper.log.info(" - store Issuer Parameters! "
                + issuerParamsUid + " : " + issuerParameters
                + " - with version number : " + issuerParameters.getVersion());

        keyManager.storeIssuerParameters(issuerParamsUid, issuerParameters);
        boolean urnScheme = "urn".equals(issuerParamsUid.getScheme());
        String issuer_params_filename = this.getIssuerParamsFilename(
                issuerParamsUid, urnScheme);

        String credSpec_filename = this.getCredSpecFilename(
                credSpec
                .getSpecificationUID());

        IssuanceHelper.log.info(" - save in file - spec : "
                + credSpec.getSpecificationUID() + " - key : "
                + issuerParamsUid + " - filename : " + issuer_params_filename);

        FileSystem.storeObjectInFile(issuerParameters,
                this.systemAndIssuerParamsPrefix,
                issuer_params_filename);

        FileSystem.storeObjectAsXMLInFile(
                this.of.createCredentialSpecification(credSpec),
                this.systemAndIssuerParamsPrefix,
                credSpec_filename);


        if (credentialManager != null) {
            SecretKey issuerPrivateKeyForIssuerParameters =
                    credentialManager.getIssuerSecretKey(issuerParamsUid);

            if (issuerPrivateKeyForIssuerParameters != null) {
                FileSystem.storeObjectInFile(
                        issuerPrivateKeyForIssuerParameters,
                        this.fileStoragePrefix,
                        "private_key_"
                                + issuer_params_filename);
            }

            // TODO(jdn): fix bug...
            // AbstractHelper.storeObjectAsXMLInFile(this.of
            // .createIssuerSecretKey(issuerPrivateKeyForIssuerParameters),
            // systemAndIssuerParamsPrefix,
            // issuer_params_filename+"_private_key");
        }

        IssuanceHelper.log.info(" - created issuerParameters with UID : "
                + issuerParameters.getParametersUID());

        return issuerParameters;
    }

    private String getIssuerParamsFilename(URI issuerParamsUid,
            boolean urnScheme) {
        String issuer_params_filename = "issuer_params_";
        if (urnScheme) {
            issuer_params_filename += issuerParamsUid.toASCIIString()
                    .replaceAll(":", "_");
        } else {
            issuer_params_filename += issuerParamsUid.getHost().replace(".",
                    "_")
                    + issuerParamsUid.getPath().replace("/", "_");
        }
        return issuer_params_filename;
    }

    private String getCredSpecFilename(URI uid) {
        String filename = "cred_spec_";
        filename += uid.toASCIIString().replaceAll(":", "_");
        filename = filename.replaceAll(":", "_")
                .replace("/", "_");

        return filename;
    }

    private void checkIfCredSpecIsInKeystoreAddIfAbsent(KeyManager keyManager,
            CredentialSpecification credSpec) throws KeyManagerException {
        CredentialSpecification credSpecInKeystore =
                keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
        if (credSpecInKeystore != null) {
            IssuanceHelper.log.info(" - credspec already in keystore : "
                    + credSpec.getSpecificationUID()
                    + " : " + credSpec);
            try{
                IssuanceHelper.log.info("credSpec: "
                        + XmlUtils.toXml(new ObjectFactory()
                        .createCredentialSpecification(credSpecInKeystore)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            IssuanceHelper.log.info(" - store credspec in keystre : ");
            keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);
            try{
                IssuanceHelper.log.info("credSpec: "
                        + XmlUtils.toXml(new ObjectFactory()
                        .createCredentialSpecification(credSpec)));
            }catch(Exception e){e.printStackTrace();}
        }
    }

    private IssuerParameters setupIssuerParameters(CryptoEngine cryptoEngine,
            IssuerAbcEngine initEngine, SystemParameters systemParameters,
            CredentialSpecification credSpec, URI hash, URI issuerParamsUid,
            URI revocationParamsUid, List<FriendlyDescription> friendlyDescriptions) {
        IssuerParameters issuerParameters;
        issuerParameters =
                initEngine.setupIssuerParameters(credSpec, systemParameters, issuerParamsUid, hash,
                        /*URI.create("Idemix")*/URI.create(this.cryptoEngine.toString()), revocationParamsUid, friendlyDescriptions);
        return issuerParameters;
    }

    public IssuerParameters setupIssuerParameters(CryptoEngine cryptoEngine,
            CredentialSpecification credSpec,
            SystemParameters systemParameters, URI issuerParamsUid, URI hash,
            URI revocationParamsUid, String systemAndIssuerParamsPrefix,
            List<FriendlyDescription> friendlyDescriptions) throws Exception {
        IssuerAbcEngine engine;
        IssuerParameters issuerParameters = null;
        IssuanceHelper.log.info("cryptoEngine: " + cryptoEngine);
        switch (cryptoEngine) {
        case IDEMIX:
            engine = this.idemixEngine;
            KeyManager akeyManager = this.idemixKeyManager;
            if (this.idemixEngine == null) {
                engine = this.singleEngine;
            }
            if (this.idemixKeyManager == null) {
                akeyManager = this.keyManager;
            }
            issuerParameters = this.setupAndStoreIssuerParameters(cryptoEngine,
                    engine, akeyManager, this.credentialManager,
                    systemAndIssuerParamsPrefix,
                    systemParameters, credSpec, hash, issuerParamsUid,
                    revocationParamsUid, friendlyDescriptions);
            break;
        case UPROVE:
            engine = this.uproveEngine;
            if (this.uproveEngine == null) {
                engine = this.singleEngine;
            }
            akeyManager = this.uproveKeyManager;
            if (this.idemixEngine == null) {
                engine = this.singleEngine;
            }
            if (this.uproveKeyManager == null) {
                akeyManager = this.keyManager;
            }
            issuerParameters = this.setupAndStoreIssuerParameters(cryptoEngine,
                    engine, akeyManager, this.credentialManager,
                    systemAndIssuerParamsPrefix,
                    systemParameters, credSpec, hash, issuerParamsUid,
                    revocationParamsUid, friendlyDescriptions);

            break;
        default:
            throw new IllegalStateException("The crypto engine: "
                    + cryptoEngine
                    + " is not supported use IDEMIX or UPROVE instead");
        }

        return issuerParameters;
    }

    public void registerSmartcardScopeExclusivePseudonym(BigInteger pse) throws IOException {
        // TODO : VERIFY : Must match the way pseudonums are created...
        String primaryKey = DatatypeConverter.printBase64Binary(pse.toByteArray());

        for(TokenStorageIssuer issuerStorageManager : this.issuerStorageManagerList) {
            if(! issuerStorageManager.checkForPseudonym(primaryKey)) {
                IssuanceHelper.log.info("registerSmartcardScopeExclusivePseudonym - register new pseudonym  - BigInteger: "
                        + pse + " - PseudonymPrimaryKey : " + primaryKey);
                issuerStorageManager.addPseudonymPrimaryKey(primaryKey);
            } else {
                IssuanceHelper.log.info("registerSmartcardScopeExclusivePseudonym - already registered");
            }
        }
    }

    /**
     * Creates a new instance of SpecAndPolicy
     * 
     * @param credspecAndPolicyKey
     * @return
     * @throws Exception
     */
    public SpecAndPolicy initSpecAndPolicy(CryptoEngine cryptoEngine, String specAndPolicyKey) throws Exception {

        String sapKey = specAndPolicyKey + "::" + cryptoEngine;
        SpecAndPolicy cached = this.specAndPolicyMap.get(sapKey);

        System.out.println("initSpecAndPolicy - cached! \n- " + XmlUtils.toXml(this.of.createIssuancePolicy(cached.getIssuancePolicy())));

        return new SpecAndPolicy(cached);
    }

    private SpecAndPolicy initSpecAndPolicyFromResouces(SpecAndPolicy cloneThisSap) throws Exception {
        SpecAndPolicy sap = new SpecAndPolicy(cloneThisSap);

        IssuanceHelper.log.info("initSpecAndPolicyFromResouces : " + sap.specResource
                + " : "
                + sap.policyResource);
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

        //        sap.setIssuancePolicyBytes(XmlUtils.toXml(this.of.createIssuancePolicy(issuancePolicy), true).getBytes());

        return sap;
    }

    private CredentialSpecification initCredentialSpecificationFromResources(
            SpecAndPolicy sap) throws IOException, JAXBException,
            UnsupportedEncodingException, SAXException {
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
     * @param issuanceMessageXML IssuanceMessager as String
     * @return
     * @throws Exception
     */
    public IssuanceResponse_String issueStep_String(String issuanceMessageXML) throws Exception {
        if(this.singleEngine==null) {
            throw new IllegalStateException("IssuanceHelper.issueStep called without specifying CryptoEngine!");
        }
        IssuanceMessage issuanceMessage =
                (IssuanceMessage) XmlUtils.getObjectFromXML(
                        new ByteArrayInputStream(issuanceMessageXML.getBytes()), false);
        IssuanceHelper.log.info("IssuanceHelper - step_string - marchalled object: "
                + issuanceMessage
                + " - xml : " + issuanceMessageXML);

        IssuanceMessageAndBoolean response = this.issueStep(issuanceMessage);

        IssuanceMessage im = response.getIssuanceMessage();
        String xml = XmlUtils.toXml(this.of.createIssuanceMessage(im), false);

        // this.log.info("IssuanceService - step - return  XML : " + xml);

        return new IssuanceResponse_String(xml, response.isLastMessage(),
                response.getIssuanceMessage().getContext()
                .toString());
    }


    /**
     * Process next step of issuance on IssuanceMessager
     * 
     * @param issuanceMessage IssuanceMessager as String
     * @return
     * @throws Exception
     */
    public IssuanceMessageAndBoolean issueStep(IssuanceMessage issuanceMessage) throws Exception {
        IssuanceHelper.log.info("IssuanceHelper - step_jaxb - marchalled object: "
                + issuanceMessage);

        if(this.singleEngine==null) {
            throw new IllegalStateException("IssuanceHelper.issueStep called without specifying CryptoEngine!");
        }
        return this.issueStep(this.singleEngine, issuanceMessage);
    }

    public IssuanceMessageAndBoolean issueStep(ProductionModule.CryptoEngine cryptoEngine, IssuanceMessage issuanceMessage) throws Exception {
        return this.issueStep(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuanceMessage);
    }
    /**
     * Process next step of issuance on IssuanceMessager
     * 
     * @param issuanceMessage IssuanceMessager as String
     * @return
     * @throws Exception
     */
    public IssuanceMessageAndBoolean issueStep(CryptoEngine cryptoEngine, IssuanceMessage issuanceMessage) throws Exception {
        IssuanceHelper.log.info("IssuanceHelper - step_jaxb - marchalled object: "
                + issuanceMessage);

        IssuerAbcEngine useEngine;
        if(this.singleEngine!=null) {
            if(this.cryptoEngine != cryptoEngine) {
                throw new IllegalStateException("IssuanceHelper.issueStep called specifying CryptoEngine - but not initialized using BRIDGED! - running " + this.cryptoEngine + " - requesting " + cryptoEngine);
            } else {
                useEngine = this.singleEngine;
            }
        } else {
            if(cryptoEngine == CryptoEngine.IDEMIX) {
                useEngine = this.idemixEngine;
            } else if(cryptoEngine == CryptoEngine.UPROVE) {
                useEngine = this.uproveEngine;
            } else {
                throw new IllegalStateException("IssuanceHelper.issueStep : idemix/uprove engine not initialized...");
            }
        }
        return this.issueStep(useEngine, issuanceMessage);
    }


    private IssuanceMessageAndBoolean issueStep(IssuerAbcEngine useEngine, IssuanceMessage issuanceMessage) throws Exception {

        IssuanceMessageAndBoolean response;
        try {
            response = useEngine.issuanceProtocolStep(issuanceMessage);
        } catch (Exception e) {
            System.err.println("- IssuerABCE could not process Step IssuanceMessage from UserABCE : " + e);

            throw new Exception("Could not process next step on issuauce : ", e);
        }
        if (response.isLastMessage()) {
            IssuanceHelper.log.info(" - last step - on server");
        } else {
            IssuanceHelper.log.info(" - continue steps");
        }

        // String xml = XmlUtils.toXml(of.createIssuanceMessage(response.im), false);
        // this.log.info("IssuanceService - step - return  XML : " + xml);

        return response;
    }

    public IssuanceMessageAndBoolean reIssueStep(IssuanceMessage issuanceMessage) throws Exception {

        IssuanceMessageAndBoolean response;
        try {
            response = this.uproveEngine.reIssuanceProtocolStep(issuanceMessage);
        } catch (Exception e) {
            System.err.println("- IssuerABCE could not process Step IssuanceMessage from UserABCE : " + e);

            throw new Exception("Could not process next step on issuauce : ", e);
        }
        if (response.isLastMessage()) {
            IssuanceHelper.log.info(" - last step - on server");
        } else {
            IssuanceHelper.log.info(" - continue steps");
        }
        return response;
    }

    public IssuanceMessage initReIssuance(String specAndPolicyString) throws Exception{
        if(this.uproveEngine == null){
            throw new IllegalStateException("IssuanceHelper.initReIssuance called without initializing the uprove engine!");
        }
        String sapKey = specAndPolicyString + "::" + CryptoEngine.UPROVE;

        SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(sapKey);
        if (specAndPolicy == null) {
            IssuanceHelper.log.info("IssuanceHelper - initReIssuance : " + sapKey + " : "
                    + this.specAndPolicyMap);
            throw new IllegalStateException("Unknown Spec And Policy Key " + sapKey);
        }

        IssuanceMessageAndBoolean initReIssuanceProtocol = this.uproveEngine.initReIssuanceProtocol(
                specAndPolicy.cloneIssuancePolicy());
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
    public IssuanceResponse_String initIssuance_String(String credspecAndPolicyKey,
            Map<String, Object> attributeValueMap) throws Exception {
        if(this.singleEngine==null) {
            throw new IllegalStateException("IssuanceHelper.initIssuance_String called without specifying CryptoEngine!");
        }

        IssuanceMessage jaxb = this.initIssuance(credspecAndPolicyKey, attributeValueMap);

        String xml = XmlUtils.toXml(this.of.createIssuanceMessage(jaxb), true);

        // this.log.info("IssuanceMessageAndBoolean XML : " + xml);

        return new IssuanceResponse_String(xml, jaxb.getContext().toString());
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
        IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + credspecAndPolicyKey);
        if(this.singleEngine==null) {
            throw new IllegalStateException("IssuanceHelper.initIssuance called without specifying CryptoEngine!");
        }

        String sapKey = credspecAndPolicyKey + "::" + this.cryptoEngine;

        SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(sapKey);
        if (specAndPolicy == null) {
            IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + sapKey + " : "
                    + this.specAndPolicyMap);
            throw new IllegalStateException("Unknown Spec And Policy Key " + sapKey);
        }
        return this.initIssuance(this.singleEngine, specAndPolicy, attributeValueMap);
    }

    public IssuanceMessage initIssuance(ProductionModule.CryptoEngine cryptoEngine, String credspecAndPolicyKey,
            Map<String, Object> attributeValueMap) throws Exception {
        return this.initIssuance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), credspecAndPolicyKey, attributeValueMap);
    }
    /**
     * Performs first step of issuance
     * 
     * @param credspecAndPolicyKey key identifying SpecAndPolicy
     * @param attributeValueMap attribute values defined by issuer
     * @return
     * @throws Exception
     */
    public IssuanceMessage initIssuance(CryptoEngine cryptoEngine, String credspecAndPolicyKey,
            Map<String, Object> attributeValueMap) throws Exception {
        IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + cryptoEngine + " : "
                + credspecAndPolicyKey);
        IssuerAbcEngine useEngine;
        if(this.singleEngine!=null) {
            if(this.cryptoEngine != cryptoEngine) {
                throw new IllegalStateException("IssuanceHelper.initIssuance called specifying CryptoEngine - but not initialized using BRIDGED! - initialzied to : " + this.cryptoEngine + " - wanted : " + cryptoEngine);
            } else {
                useEngine = this.singleEngine;
            }
        } else {
            if(cryptoEngine == CryptoEngine.IDEMIX) {
                useEngine = this.idemixEngine;
            } else if(cryptoEngine == CryptoEngine.UPROVE) {
                useEngine = this.uproveEngine;
            } else {
                throw new IllegalStateException("IssuanceHelper.initIssuance : idemix/uprove engine not specified : " + cryptoEngine);
            }
        }
        String sapKey = credspecAndPolicyKey + "::" + cryptoEngine;

        SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(sapKey);
        if (specAndPolicy == null) {
            IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + sapKey + " : "
                    + this.specAndPolicyMap);
            throw new IllegalStateException("Unknown Spec And Policy Key " + sapKey);
        }
        return this.initIssuance(useEngine, specAndPolicy, attributeValueMap);
    }

    /**
     * Performs first step of issuance
     * 
     * @param credspecAndPolicyKey key identifying SpecAndPolicy
     * @param attributeValueMap attribute values defined by issuer
     * @return
     * @throws Exception
     */
    public IssuanceMessage initIssuance(CryptoEngine cryptoEngine, SpecAndPolicy specAndPolicy,
            Map<String, Object> attributeValueMap) throws Exception {
        IssuanceHelper.log.info("IssuanceHelper - initIssuance : " + cryptoEngine + " : "
                + specAndPolicy);
        IssuerAbcEngine useEngine;
        if(this.singleEngine!=null) {
            if(this.cryptoEngine != cryptoEngine) {
                throw new IllegalStateException("IssuanceHelper.initIssuance called specifying CryptoEngine - but not initialized using BRIDGED! - initialzied to : " + this.cryptoEngine + " - wanted : " + cryptoEngine);
            } else {
                useEngine = this.singleEngine;
            }
        } else {
            if(cryptoEngine == CryptoEngine.IDEMIX) {
                useEngine = this.idemixEngine;
            } else if(cryptoEngine == CryptoEngine.UPROVE) {
                useEngine = this.uproveEngine;
            } else {
                throw new IllegalStateException("IssuanceHelper.initIssuance : idemix/uprove engine not specified : " + cryptoEngine);
            }
        }
        return this.initIssuance(useEngine, specAndPolicy, attributeValueMap);
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
        if(this.singleEngine==null) {
            throw new IllegalStateException("IssuanceHelper.initIssuance called without specifying CryptoEngine!");
        }
        return this.initIssuance(this.singleEngine, specAndPolicy, attributeValueMap);
    }


    private IssuanceMessage initIssuance(IssuerAbcEngine useEngine, SpecAndPolicy specAndPolicy,
            Map<String, Object> attributeValueMap) throws Exception {

        List<Attribute> issuerAtts = new ArrayList<Attribute>();

        this.populateIssuerAttributes(specAndPolicy, issuerAtts,
                attributeValueMap);

        IssuancePolicy clonedIssuancePolicy = specAndPolicy.cloneIssuancePolicy();
        URI policyIssuerParametersUID = specAndPolicy.issuerParamsUid_URI;
        return this.initIssuanceProtocol(useEngine, issuerAtts,
                clonedIssuancePolicy, policyIssuerParametersUID)
                .getIssuanceMessage();
    }

    private IssuanceMessageAndBoolean initIssuanceProtocol(
            IssuerAbcEngine useEngine,
            List<Attribute> issuerAtts, IssuancePolicy clonedIssuancePolicy,
            URI policyIssuerParametersUID) throws Exception {
        IssuanceMessageAndBoolean response = null;
        try {


            IssuanceHelper.log.info(" - call ABCE - policy : " + clonedIssuancePolicy
                    + " : " + policyIssuerParametersUID
                    + " - attributes : " + issuerAtts);
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


    private void populateIssuerAttributes(SpecAndPolicy specAndPolicy,
            List<Attribute> issuerAtts, Map<String, Object> attributeValueMap) {

        CredentialSpecification credSpec = specAndPolicy.getCredentialSpecification();
        IssuancePolicy ip = specAndPolicy.getIssuancePolicy();

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

            // this.log.info("ad - type " + ad.getType() + " : " +
            // ad.getDataType() + " - value : "
            // + value);

            Attribute attribute = this.of.createAttribute();
            // TODO : Howto create vaules ??
            attribute.setAttributeUID(URI.create("" + this.random.nextLong()));

            Object xmlValue = new AttributeValueConverter().convertValue(ad
                    .getDataType().toString(), value);
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

    public IssuanceMessageAndBoolean initIssuanceProtocol(
            IssuancePolicy issuancePolicy, List<Attribute> attributes)
                    throws Exception {
        IssuerAbcEngine engine;
        IssuanceMessageAndBoolean issuanceMessageAndBoolean = null;
        this.validateIssuancePolicy(issuancePolicy);

        URI issuerPolicyParametersUid = issuancePolicy.getCredentialTemplate()
                .getIssuerParametersUID();

        boolean urnScheme = "urn".equals(issuerPolicyParametersUid.getScheme());
        issuerPolicyParametersUid = URI.create(issuerPolicyParametersUid
                + ((urnScheme ? ":" : "/") + this.cryptoEngine).toLowerCase());

        switch (this.cryptoEngine) {
        case IDEMIX:
            engine = this.idemixEngine;
            if (this.idemixEngine == null) {
                engine = this.singleEngine;
            }
            issuanceMessageAndBoolean = this.initIssuanceProtocol(engine,
                    attributes, issuancePolicy, issuerPolicyParametersUid);
            break;
        case UPROVE:
            engine = this.idemixEngine;
            if (this.idemixEngine == null) {
                engine = this.singleEngine;
            }
            issuanceMessageAndBoolean = this.initIssuanceProtocol(engine,
                    attributes, issuancePolicy, issuerPolicyParametersUid);

            break;
        default:
            throw new IllegalStateException("The crypto engine: "
                    + this.cryptoEngine
                    + " is not supported use IDEMIX or UPROVE instead");
        }
        return issuanceMessageAndBoolean;
    }

    private void validateIssuancePolicy(IssuancePolicy issuancePolicy) {
        if (issuancePolicy.getCredentialTemplate() == null) {
            throw new RuntimeException("Credential template should be present");
        }

        if (issuancePolicy.getPresentationPolicy() == null) {
            throw new RuntimeException("Presentation policy should be present");
        }

        if (issuancePolicy.getPresentationPolicy().getMessage() == null) {
            throw new RuntimeException(
                    "Presentation policy message should be present");
        }

    }

    @Override
    public void addCredentialSpecifications(String[] credSpecResourceList) {
        super.addCredentialSpecifications(credSpecResourceList);
    }

    @Override
    public void addIssuerParameters(String[] issuerParametersResourceList) {
        super.addIssuerParameters(issuerParametersResourceList);
    }

    public IssuanceLogEntry getIssuanceLogEntry(CryptoEngine engine,
            URI issuanceEntryUid) throws Exception {
        return this.singleEngine.getIssuanceLogEntry(issuanceEntryUid);
    }
}

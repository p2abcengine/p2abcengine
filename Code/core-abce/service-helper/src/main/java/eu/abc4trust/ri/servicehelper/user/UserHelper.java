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

package eu.abc4trust.ri.servicehelper.user;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import eu.abc4trust.abce.external.user.SynchronizedUserAbcEngineImpl;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveSystemParameters;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.SystemParametersHelper;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
import eu.abc4trust.xml.SystemParameters;

public class UserHelper extends AbstractHelper {

    private static final Logger logger = Logger.getLogger(UserHelper.class
            .toString());

    public static boolean WIPE_STOARAGE_FILES = false;

    public ReloadTokensCommunicationStrategy reloadTokens = null;
    static UserHelper instance;

    // orig
    public static synchronized UserHelper initInstance(CryptoEngine cryptoEngine,
            /* String systemParamsResource, */String[] issuerParamsResourceList, String fileStoragePrefix,
            String[] credSpecResourceList) throws URISyntaxException {
        return initInstance(cryptoEngine, issuerParamsResourceList, fileStoragePrefix, credSpecResourceList, new String[0], new String[0]);
    }

    // with inspector
    public static synchronized UserHelper initInstance(CryptoEngine cryptoEngine,
            /* String systemParamsResource, */String[] issuerParamsResourceList, String fileStoragePrefix,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList) throws URISyntaxException {
        return initInstance(cryptoEngine, issuerParamsResourceList, fileStoragePrefix, credSpecResourceList, inspectorPublicKeyResourceList, new String[0]);
    }

    // with inspector and revocation
    public static synchronized UserHelper initInstance(CryptoEngine cryptoEngine,
            /* String systemParamsResource, */String[] issuerParamsResourceList, String fileStoragePrefix,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String[] revocationAuthorityParametersResourceList) throws URISyntaxException {

        initialializeInstanceField(cryptoEngine, fileStoragePrefix);

        // instance.setSystemParams(systemParamsResource);
        instance.addCredentialSpecifications(credSpecResourceList);
        instance.addIssuerParameters(issuerParamsResourceList);
        if((issuerParamsResourceList==null)||(issuerParamsResourceList.length==0)) {

            try {
                String systemParametersResource = fileStoragePrefix
                        + SYSTEM_PARAMS_NAME_BRIDGED;
                @SuppressWarnings("unused")
                SystemParameters systemParameters = SystemParametersHelper
                .checkAndLoadSystemParametersIfAbsent(
                        instance.keyManager, systemParametersResource);
                // SystemParameters systemParameters = (SystemParameters)
                // XmlUtils.getObjectFromXML(UserHelper.class.getResourceAsStream("/eu/abc4trust/systemparameters/bridged-systemParameters.xml"),
                // true);
                // instance.keyManager.storeSystemParameters(systemParameters);

                UProveSystemParameters uproveSystemParameters = new UProveSystemParameters(systemParameters);
                UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE = uproveSystemParameters.getNumberOfTokens();
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.info(ex.getMessage());
            }
        }

        instance.checkIfSystemParametersAreLoaded();
        instance.addInspectorPublicKeys(inspectorPublicKeyResourceList);
        instance.addRevocationAuthorities(instance.keyManager, revocationAuthorityParametersResourceList);

        System.out.println("UserHelper.initInstance : DONE");

        return instance;
    }

    public static synchronized UserHelper initInstanceForService(
            CryptoEngine cryptoEngine, String fileStoragePrefix,
            Module... modules)
                    throws URISyntaxException {

        initialializeInstanceField(cryptoEngine, fileStoragePrefix, modules);

        instance.checkIfSystemParametersAreLoaded();

        System.out.println("UserHelper.initInstance : DONE");

        return instance;
    }

    private static void initialializeInstanceField(CryptoEngine cryptoEngine,
            String fileStoragePrefix, Module... modules)
                    throws URISyntaxException {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        System.out.println("UserHelper.initInstance");
        instance = new UserHelper(cryptoEngine, fileStoragePrefix, modules);
    }

    public static synchronized boolean isInit() {
        return instance != null;
    }

    public static synchronized UserHelper getInstance() {
        // System.out.println("UserHelper.getInstance : " + instance);
        if (instance == null) {
            System.out.println("initInstance not called before using UserHelper!");
            throw new IllegalStateException("initInstance not called before using UserHelper!");
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        System.err.println("WARNING UserHelper.resetInstance : " + instance);
        if((instance!=null) && (instance.uproveBindingManager != null) ) {
            System.err.println("WARNING UserHelper.resetInstance : Stop UPROVE UserEngine");
            try {
                // try to stop uprove engine/service if running...
                //                @SuppressWarnings("unused")
                //                int exitCode = instance.uproveBindingManager.stop();
                //              System.out.println("exitCode : " + exitCode);
                //              Thread.sleep(2000);
            } catch(Exception ignore) {
                System.err.println("Failed to stop UProve service : " + ignore);
            }
        }
        instance = null;
    }

    private UserAbcEngine engine;
    public AbcSmartcardManager smartcardManager;
    public CardStorage cardStorage;
    public CredentialManager credentialManager;

    // needed for 'reset'
    private UProveBindingManager uproveBindingManager = null;


    private UserHelper(CryptoEngine cryptoEngine, String fileStoragePrefix,
            Module... modules) throws URISyntaxException {
        System.out
        .println("UserHelper : : create instance " + cryptoEngine + " : " + fileStoragePrefix);
        this.cryptoEngine = cryptoEngine;
        try {
            UProveUtils uproveUtils = new UProveUtils();

            AbceConfigurationImpl configuration = this
                    .setupStorageFilesForConfiguration(fileStoragePrefix,
                            cryptoEngine, WIPE_STOARAGE_FILES);
            configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe().getAbsolutePath());
            configuration.setUProvePortNumber(uproveUtils.getUserServicePort());
            configuration.setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
            configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

            Module m = ProductionModuleFactory.newModule(configuration, cryptoEngine);
            Module combinedModule = Modules.override(m).with(modules);
            Injector injector = Guice.createInjector(combinedModule);

            this.keyManager = injector.getInstance(KeyManager.class);
            this.credentialManager = injector.getInstance(CredentialManager.class);
            this.smartcardManager = injector.getInstance(AbcSmartcardManager.class);
            this.cardStorage = injector.getInstance(CardStorage.class);
            this.reloadTokens = injector.getInstance(ReloadTokensCommunicationStrategy.class);
            //
            UserAbcEngine e = injector.getInstance(UserAbcEngine.class);
            this.engine = new SynchronizedUserAbcEngineImpl(e);


            if((cryptoEngine == CryptoEngine.UPROVE) || (cryptoEngine == CryptoEngine.BRIDGED)) {
                this.uproveBindingManager = injector.getInstance(UProveBindingManager.class);
            }

            //
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup user !", e);
        }
    }

    public UserAbcEngine getEngine() {
        return this.engine;
    }

}

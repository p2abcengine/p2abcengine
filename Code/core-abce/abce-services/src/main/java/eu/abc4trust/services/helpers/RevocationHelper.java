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

package eu.abc4trust.services.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;

public class RevocationHelper extends AbstractHelper {

    private static final Logger logger = Logger
            .getLogger(RevocationHelper.class.toString());

    private static RevocationHelper instance;


    public static synchronized RevocationHelper initInstance(
            String revocationStoragePrefix, Module... modules)
                    throws Exception {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        logger.info("RevocationHelper.initInstance");

        instance = new RevocationHelper(
                ProductionModuleFactory.CryptoEngine.BRIDGED,
                revocationStoragePrefix);

        return instance;
    }

    @Override()
    protected void checkIfSystemParametersAreLoaded() {
        try {
            if (!instance.keyManager.hasSystemParameters()) {
                throw new IllegalStateException("No system parameters found");
            }
        } catch (KeyManagerException ex) {
            throw new IllegalStateException("No system parameters found");
        }
    }


    public static synchronized RevocationAuthorityParameters setupParameters(
            URI mechanism, int keyLength, URI uid, Reference revocationInfoReference, Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference, String revocationResourcesPrefix) throws CryptoEngineException {

        RevocationAuthorityParameters revocationAuthorityParameters = instance.engine.setupRevocationAuthorityParameters(keyLength, mechanism, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference);

        // Register key for IDEMIX.
        List<Object> any = revocationAuthorityParameters.getCryptoParams().getAny();
        Element publicKeyStr = (Element) any.get(0);
        Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);
        AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;
        String publicKeyUri = publicKey.getUri().toString();

        StructureStore.getInstance().add(publicKeyUri, publicKey);

        return revocationAuthorityParameters;
    }

    /**
     * @return true if IssuanceHelper has been initialized
     */
    public static synchronized boolean isInit() {
        return instance != null;
    }

    /**
     * @return initialized instance of IssuanceHelper
     */
    public static synchronized RevocationHelper getInstance() {
        logger.info("RevocationHelper.getInstance : " + instance
                + (instance == null ? "" : " : " + instance.cryptoEngine));
        if (instance == null) {
            throw new IllegalStateException(
                    "getInstance not called before using IssuanceHelper!");
        }

        return instance;
    }

    // public CredentialManager credentialManager;

    //
    public RevocationAbcEngine engine;
    public RevocationProxyAuthority revocationProxyAuthority;

    /**
     * Private constructor
     * 
     * @param revocationStoragePrefix
     *            this prefix will be prepended on storage files needed by the
     *            IssuerAbcEnginge
     * @throws URISyntaxException
     */
    private RevocationHelper(ProductionModuleFactory.CryptoEngine cryptoEngine,
            String revocationStoragePrefix, Module... modules)
                    throws URISyntaxException {
        RevocationHelper.logger
        .info("RevocationHelper : create instance - storage prefix : "
                + revocationStoragePrefix);
        try {
            this.cryptoEngine = cryptoEngine;

            AbceConfigurationImpl configuration = this
                    .setupStorageFilesForConfiguration(revocationStoragePrefix, this.cryptoEngine);


            Module newModule = ProductionModuleFactory.newModule(
                    configuration, this.cryptoEngine);

            Module combinedModule = Modules.override(newModule).with(modules);
            Injector injector = Guice.createInjector(combinedModule);


            this.engine = injector.getInstance(RevocationAbcEngine.class);

            this.revocationProxyAuthority = injector.getInstance(RevocationProxyAuthority.class);

            this.keyManager = injector.getInstance(KeyManager.class);

            RevocationHelper.logger.info("keymanager : " + this.keyManager);
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup issuer !", e);
        }
    }

}

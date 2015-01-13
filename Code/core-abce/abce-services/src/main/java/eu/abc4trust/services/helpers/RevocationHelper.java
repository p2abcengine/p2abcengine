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

package eu.abc4trust.services.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
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

        instance = new RevocationHelper(revocationStoragePrefix);

        return instance;
    }

    protected void setupIdemixEngine() {
        try {
            if (!instance.keyManager.hasSystemParameters()) {
                throw new IllegalStateException("No system parameters found");
            }
        } catch (KeyManagerException ex) {
            throw new IllegalStateException("No system parameters found");
        }
    }


    public static synchronized RevocationAuthorityParameters setupParameters(
            URI technology, int keyLength, URI uid, Reference revocationInfoReference, Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference, String revocationResourcesPrefix) throws CryptoEngineException {

        RevocationAuthorityParameters revocationAuthorityParameters = instance.engine.setupRevocationAuthorityParameters(keyLength, technology, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference);

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
                + (instance == null ? "" : " : " + instance));
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
    private RevocationHelper(String revocationStoragePrefix)
                    throws URISyntaxException {
        RevocationHelper.logger
        .info("RevocationHelper : create instance - storage prefix : "
                + revocationStoragePrefix);
        try {
            AbceConfigurationImpl configuration = this
                    .setupStorageFilesForConfiguration(revocationStoragePrefix, CryptoEngine.IDEMIX);


            Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(
                    configuration, CryptoEngine.IDEMIX));

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

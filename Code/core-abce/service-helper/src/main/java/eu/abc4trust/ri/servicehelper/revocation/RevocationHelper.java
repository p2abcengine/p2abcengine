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

package eu.abc4trust.ri.servicehelper.revocation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.w3c.dom.Element;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;

public class RevocationHelper extends AbstractHelper {

    public static class RevocationReferences {
        public final URI revocationAuthorityUID;
        public final URI revocationInfoServiceURI;
        public final URI nonRevocationEvidenceServiceURI;
        public final URI nonRevocationUpdateServiceURI;

        public RevocationReferences(final URI revocationAuthorityUID,
                final URI revocationInfoServiceURI,
                final URI nonRevocationEvidenceServiceURI,
                final URI nonRevocationUpdateServiceURI) {
            this.revocationAuthorityUID = revocationAuthorityUID;
            this.revocationInfoServiceURI = revocationInfoServiceURI;
            this.nonRevocationEvidenceServiceURI = nonRevocationEvidenceServiceURI;
            this.nonRevocationUpdateServiceURI = nonRevocationUpdateServiceURI;
        }

        public RevocationReferences(final String revocationAuthority,
                final String revocationInfoService,
                final String nonRevocationEvidenceService,
                final String nonRevocationUpdateService) {
            this.revocationAuthorityUID = URI.create(revocationAuthority);
            this.revocationInfoServiceURI = URI.create(revocationInfoService);
            this.nonRevocationEvidenceServiceURI = URI.create(nonRevocationEvidenceService);
            this.nonRevocationUpdateServiceURI = URI.create(nonRevocationUpdateService);
        }

        public Reference getRevocationInfoReference() {
            Reference revocationInfoReference = new Reference();
            revocationInfoReference.setReferenceType(URI.create(this.revocationInfoServiceURI.getScheme()));
            revocationInfoReference.getReferences().add(this.revocationInfoServiceURI);
            return revocationInfoReference;
        }
        public Reference getNonRevocationEvidenceReference() {
            Reference nonRevocationEvidenceReference = new Reference();
            nonRevocationEvidenceReference.setReferenceType(URI.create(this.nonRevocationEvidenceServiceURI.getScheme()));
            nonRevocationEvidenceReference.getReferences().add(this.nonRevocationEvidenceServiceURI);
            return nonRevocationEvidenceReference;
        }
        public Reference getNonRevocationUpdateReference() {
            Reference nonRevocationUpdateReference = new Reference();
            nonRevocationUpdateReference.setReferenceType(URI.create(this.nonRevocationUpdateServiceURI.getScheme()));
            nonRevocationUpdateReference.getReferences().add(this.nonRevocationUpdateServiceURI);
            return nonRevocationUpdateReference;
        }
    }
    private static RevocationHelper instance;

    // deprecated - but please keep until after pilot has been released...
    @Deprecated
    public static synchronized RevocationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine, String revocationStoragePrefix, String revocationResourcesPrefix, String systemParametersResource, String[] issuerParamsResourceList, String[] credSpecResourceList, RevocationReferences... revocationReferences) throws Exception {
        return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), revocationStoragePrefix, revocationResourcesPrefix, systemParametersResource, issuerParamsResourceList, credSpecResourceList, revocationReferences);
    }
    /**
     * @param cryptoEngine
     * @param revocationStoragePrefix - private storage files (private keys) will be stored here
     * @param revocationResourcesPrefix - public keys will be exported here
     * @param systemParametersResource - the system parameter
     * @param issuerParamsResourceList - issuer params
     * @param credSpecResourceList - supported credSpecs
     * @param revocationReferences ...
     * @return
     * @throws Exception
     */
    public static synchronized RevocationHelper initInstance(CryptoEngine cryptoEngine, String revocationStoragePrefix, String revocationResourcesPrefix, String systemParametersResource, String[] issuerParamsResourceList, String[] credSpecResourceList, RevocationReferences... revocationReferences)
            throws Exception {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        System.out.println("RevocationHelper.initInstance");

        instance = new RevocationHelper(cryptoEngine, revocationStoragePrefix, revocationResourcesPrefix);
        instance.addCredentialSpecifications(credSpecResourceList);
        instance.addIssuerParameters(issuerParamsResourceList);
        //
        SystemParameters systemParameters = null;
        // system params...
        if (!instance.keyManager.hasSystemParameters()) {

            // read systemparameters - both Idemix and UProve
            if(systemParametersResource!=null) {
                System.out.println("- load systemparameters from resource! " + systemParametersResource);
                systemParameters = FileSystem
                        .loadObjectFromResource(systemParametersResource);
                instance.keyManager.storeSystemParameters(systemParameters);
            } else {
                throw new IllegalStateException("systemparameters resource not specified!");
//                new SystemParametersUtil();
//                systemParameters = SystemParametersUtil
//                        .generatePilotSystemParameters_WithIdemixSpecificKeySize(
//                                IDEMIX_KEY_LENGTH, UPROVE_KEY_LENGTH);
//                instance.keyManager.storeSystemParameters(systemParameters);
//                System.out.println("WARN - using static SystemParameters");
            }
        } else {
            System.out.println(" - system params exists!");
            systemParameters = instance.keyManager.getSystemParameters();
            System.out.println("systemParameters : " + systemParameters);
        }
        // register Idemix system parameters
        instance.checkIfSystemParametersAreLoaded();

        instance.setupRevocationReferences(revocationReferences, revocationResourcesPrefix);
        //
        return instance;
    }

    public static synchronized RevocationHelper initInstance(String revocationStoragePrefix, String[] issuerParamsResourceList,
            String[] credSpecResourceList, String systemParametersResource, String[] revocationAuthorityResourceList)
                    throws Exception {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        System.out.println("RevocationHelper.initInstance");

        instance = new RevocationHelper(ProductionModuleFactory.CryptoEngine.BRIDGED, revocationStoragePrefix, revocationStoragePrefix);
        instance.addCredentialSpecifications(credSpecResourceList);
        instance.addIssuerParameters(issuerParamsResourceList);

        SystemParameters systemParameters = null;
        // system params...
        if (!instance.keyManager.hasSystemParameters()) {

            // read systemparameters - both Idemix and UProve
            if(systemParametersResource!=null) {
                System.out.println("- load systemparameters from resource! " + systemParametersResource);
                systemParameters = FileSystem
                        .loadObjectFromResource(systemParametersResource);
                instance.keyManager.storeSystemParameters(systemParameters);
            } else {
              throw new IllegalStateException("systemparameters resource not specified!");
//                new SystemParametersUtil();
//                systemParameters = SystemParametersUtil
//                        .generatePilotSystemParameters_WithIdemixSpecificKeySize(
//                                IDEMIX_KEY_LENGTH, UPROVE_KEY_LENGTH);
//                instance.keyManager.storeSystemParameters(systemParameters);
//                System.out.println("WARN - using static SystemParameters");
            }
        } else {
            System.out.println(" - system params exists!");
            systemParameters = instance.keyManager.getSystemParameters();
            System.out.println("systemParameters : " + systemParameters);
        }
        // register Idemix system parameters
        instance.checkIfSystemParametersAreLoaded();

        if(revocationAuthorityResourceList.length!=0){
            for(String resource: revocationAuthorityResourceList){
                try{
                    RevocationAuthorityParameters revocationAuthorityParameters = FileSystem
                            .loadObjectFromResource(resource);
                    // 	register key for IDEMIX!
                    System.out.println("- Try to register Revocation public key in IDEMIX StructureStore : " + revocationAuthorityParameters.getParametersUID());
                    List<Object> any = revocationAuthorityParameters.getCryptoParams().getAny();
                    Element publicKeyStr = (Element) any.get(0);
                    Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

                    AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;

                    StructureStore.getInstance().add(publicKey.getUri().toString(),
                            publicKey);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }


    public static synchronized RevocationAuthorityParameters setupParameters(
            URI mechanism, int keyLength, URI uid, Reference revocationInfoReference, Reference nonRevocationEvidenceReference,
            Reference nonRevocationUpdateReference, String revocationResourcesPrefix) throws CryptoEngineException {


        RevocationAuthorityParameters revocationAuthorityParameters = instance.engine.setupRevocationAuthorityParameters(keyLength, mechanism, uid, revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference);


        boolean urnScheme = "urn".equals(uid.getScheme());
        String revocation_authority_filename = "revocation_authority_";
        if (urnScheme) {
            revocation_authority_filename += uid.toASCIIString().replaceAll(":", "_");
        } else {
            revocation_authority_filename +=
                    uid.getHost().replace(".", "_")
                    + uid.getPath().replace("/", "_");
        }
        try {
            if(!new File(revocationResourcesPrefix+revocation_authority_filename).exists()){
                FileSystem.storeObjectInFile(revocationAuthorityParameters,
                        revocationResourcesPrefix,
                        revocation_authority_filename);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store RevocationAuthorityParameters : " + uid + " : " + revocationResourcesPrefix + revocation_authority_filename, e);
        }

        // register key for IDEMIX!
        System.out.println("- Try to register Revocation public key in IDEMIX StructureStore : " + uid);
        List<Object> any = revocationAuthorityParameters.getCryptoParams().getAny();
        Element publicKeyStr = (Element) any.get(0);
        Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

        AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;

        StructureStore.getInstance().add(publicKey.getUri().toString(),
                publicKey);



        return revocationAuthorityParameters;
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
        System.err.println("WARNING RevocationHelper.resetInstance : "
                + instance);
        instance = null;
    }

    /**
     * @return initialized instance of IssuanceHelper
     */
    public static synchronized RevocationHelper getInstance() {
        System.out.println("RevocationHelper.getInstance : " + instance
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
     * @param fileStoragePrefix
     *            this prefix will be prepended on storage files needed by the
     *            IssuerAbcEnginge
     * @param specAndPolicyList
     *            list of CredentialSpecifications + IssuancePolices
     * @throws URISyntaxException
     */
    private RevocationHelper(ProductionModuleFactory.CryptoEngine cryptoEngine, String revocationStoragePrefix, String revocationResourcesPrefix)
            throws URISyntaxException {
        System.out.println("RevocationHelper : create instance - storage prefix : "
                + revocationStoragePrefix + " - resouces prefix : " + revocationResourcesPrefix);
        try {
            this.cryptoEngine = cryptoEngine;

            AbceConfigurationImpl configuration = this
                    .setupStorageFilesForConfiguration(revocationStoragePrefix, this.cryptoEngine);


            Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(
                    configuration, this.cryptoEngine));

            this.engine = injector.getInstance(RevocationAbcEngine.class);

            this.revocationProxyAuthority = injector.getInstance(RevocationProxyAuthority.class);

            this.keyManager = injector.getInstance(KeyManager.class);
            //this.credentialManager = injector.getInstance(CredentialManager.class);
            System.out.println("keymanager : " + this.keyManager);
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup issuer !", e);
        }
    }


    private void setupRevocationReferences(RevocationReferences[] revocationReferences, String revocationResourcesPrefix) {

        for(RevocationReferences r : revocationReferences) {
            RevocationAuthorityParameters revocationAuthorityParameters;
            try {
                revocationAuthorityParameters = this.keyManager.getRevocationAuthorityParameters(r.revocationAuthorityUID);
            } catch (KeyManagerException e) {
                throw new IllegalStateException("Failed to get RevocationAuthorityParameters from KeyStore : " + r.revocationAuthorityUID, e);
            }
            if(revocationAuthorityParameters!=null) {
                System.out.println("RevocationAuthorityParameters already exists for : " + r.revocationAuthorityUID);
            } else {
                System.out.println("Initialize RevocationAuthorityParameters for : " + r.revocationAuthorityUID);
                URI cryptographicMechanism;
                switch (this.cryptoEngine) {
                case UPROVE:
                    cryptographicMechanism = URI.create("urn:abc4trust:1.0:algorithm:uprove");
                    break;
                case BRIDGED:
                    cryptographicMechanism = URI.create("urn:abc4trust:1.0:algorithm:bridging");
                    break;

                default:
                    cryptographicMechanism = URI.create("urn:abc4trust:1.0:algorithm:idemix");
                    break;
                }
                try {
                    revocationAuthorityParameters = this.engine.setupRevocationAuthorityParameters(REVOCATION_KEY_LENGTH, cryptographicMechanism, r.revocationAuthorityUID, r.getRevocationInfoReference(), r.getNonRevocationEvidenceReference(), r.getNonRevocationUpdateReference());
                } catch (CryptoEngineException e) {
                    throw new IllegalStateException("Failed to setup RevocationAuthorityParameters : " + r.revocationAuthorityUID, e);
                }
            }
            boolean urnScheme = "urn".equals(r.revocationAuthorityUID.getScheme());
            String revocation_authority_filename = "revocation_authority_";
            if (urnScheme) {
                revocation_authority_filename += r.revocationAuthorityUID.toASCIIString().replaceAll(":", "_");
            } else {
                revocation_authority_filename +=
                        r.revocationAuthorityUID.getHost().replace(".", "_")
                        + r.revocationAuthorityUID.getPath().replace("/", "_");
            }
            try {
                if(!new File(revocationResourcesPrefix+revocation_authority_filename).exists()){
                    FileSystem.storeObjectInFile(revocationAuthorityParameters,
                            revocationResourcesPrefix,
                            revocation_authority_filename);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to store RevocationAuthorityParameters : " + r.revocationAuthorityUID + " : " + revocationResourcesPrefix + revocation_authority_filename, e);
            }

            // register key for IDEMIX!
            System.out.println("- Try to register Revocation public key in IDEMIX StructureStore : " + r.revocationAuthorityUID);
            List<Object> any = revocationAuthorityParameters.getCryptoParams().getAny();
            Element publicKeyStr = (Element) any.get(0);
            Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

            AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;

            StructureStore.getInstance().add(publicKey.getUri().toString(),
                    publicKey);

        }
    }



}

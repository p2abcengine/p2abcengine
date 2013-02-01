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

package eu.abc4trust.ri.servicehelper.inspector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.util.CryptoUriUtil;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

public class InspectorHelper extends AbstractHelper {

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
    public static synchronized InspectorHelper initInstance(ProductionModule.CryptoEngine ignoredCryptoEngine, String inspectorStoragePrefix, String inspectorResourcesPrefix, String systemParametersResource, URI[] inspectorKeyUIDs, String[] credSpecResourceList)
        throws Exception {
      return initInstance(inspectorStoragePrefix, inspectorResourcesPrefix, systemParametersResource, inspectorKeyUIDs, credSpecResourceList);
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
    public static synchronized InspectorHelper initInstance(String inspectorStoragePrefix, String inspectorResourcesPrefix, String systemParametersResource, URI[] inspectorKeyUIDs, String[] credSpecResourceList)
                    throws Exception {
        if (instance != null) {
            throw new IllegalStateException(
                    "initInstance can only be called once!");
        }
        System.out.println("InspectorHelper.initInstance");

        instance = new InspectorHelper(inspectorStoragePrefix, inspectorResourcesPrefix, inspectorKeyUIDs);
        instance.addCredentialSpecifications(credSpecResourceList);
        //
        SystemParameters systemParameters = null;
        // system params...
        if (!instance.keyManager.hasSystemParameters()) {
          
          // read systemparameters - both Idemix and UProve
          if(systemParametersResource!=null) {
            System.out.println("- load systemparameters from resource! " + systemParametersResource);
            systemParameters= loadObjectFromResource(systemParametersResource);
            instance.keyManager.storeSystemParameters(systemParameters);
          } else {
              systemParameters = new SystemParametersUtil().generatePilotSystemParameters_WithIdemixSpecificKeySize(IDEMIX_KEY_LENGTH);
              instance.keyManager.storeSystemParameters(systemParameters);
              System.out.println("WARN - using static SystemParameters");
          }
      } else {
          System.out.println(" - system params exists!");
          systemParameters = instance.keyManager.getSystemParameters();
          System.out.println("systemParameters : " + systemParameters);
      }
      // register Idemix system parameters
      instance.setupIdemixEngine();        
      instance.initInspectorKeys(inspectorKeyUIDs, inspectorResourcesPrefix);
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
        System.err.println("WARNING InspectorHelper.resetInstance : "
                + instance);
        instance = null;
    }

    /**
     * @return initialized instance of IssuanceHelper
     */
    public static synchronized InspectorHelper getInstance() {
        System.out.println("InspectorHelper.getInstance : " + instance
                + (instance == null ? "" : " : " + instance.cryptoEngine));
        if (instance == null) {
            throw new IllegalStateException(
                    "getInstance not called before using IssuanceHelper!");
        }
        return instance;
    }

    private InspectorAbcEngine engine;
    public CredentialManager credentialManager;

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
    private InspectorHelper(String inspectorStoragePrefix, String inspectorResourcesPrefix, URI[] inspectorKeyUIDs)
            throws URISyntaxException {
        System.out.println("InspectorHelper : create instance - storage prefix : "
                + inspectorStoragePrefix + " - resouces prefix : " + inspectorResourcesPrefix);
        try {
            System.out.println("WARN : cryptoEngine fix to IDEMIX for now!");
            this.cryptoEngine = CryptoEngine.IDEMIX;

            AbceConfigurationImpl configuration = this
                    .setupStorageFilesForConfiguration(inspectorStoragePrefix, this.cryptoEngine);


            Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(
                    configuration, this.cryptoEngine));

            this.engine = injector.getInstance(InspectorAbcEngine.class);



            this.keyManager = injector.getInstance(KeyManager.class);
            this.credentialManager = injector.getInstance(CredentialManager.class);
            System.out.println("keymanager : " + this.keyManager);
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup Inspector !", e);
        }
    }        


    private void initInspectorKeys(URI[] inspectorKeyUIDs, String inspectorResourcesPrefix) {
        try {
            for(URI uid : inspectorKeyUIDs) {
                InspectorPublicKey publicKey = this.keyManager.getInspectorPublicKey(uid);
                if(publicKey == null) {
                      // key does not exist!
                    System.out.println("Generate Inspector Keys with UID : " + uid);
                    URI mechanism = CryptoUriUtil.getIdemixMechanism();
                    int inspectorKeySize = INSPECTOR_KEY_LENGTH;
                    publicKey = this.engine.setupInspectorPublicKey(inspectorKeySize, mechanism, uid);
                    
                    String inspector_publickey = "inspector_publickey_";
                    boolean urnScheme = "urn".equals(uid.getScheme());
                    if (urnScheme) {
                      inspector_publickey += uid.toASCIIString().replaceAll(":", "_");
                    } else {
                      inspector_publickey +=
                          uid.getHost().replace(".", "_")
                              + uid.getPath().replace("/", "_");
                    }
                    storeObjectInFile(publicKey, inspectorResourcesPrefix, inspector_publickey);
                    
                } else {
                     System.out.println("We already have inspector keys with UID : " + uid);
                }
                // TODO : is this needed on inspector side ?
                VEPublicKey vePubKey = (VEPublicKey)Parser.getInstance().parse((org.w3c.dom.Element)publicKey.getCryptoParams().getAny().get(0));
                StructureStore.getInstance().add(publicKey.getPublicKeyUID().toString(), vePubKey);

            }
            
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup Inspector !", e);
        }

    }

    public List<Attribute> inspect(PresentationToken value) throws Exception {
        return this.engine.inspect(value);
    }
    
    public SecretKey exportPrivateKey(URI inspectorPublicKeyUID) throws Exception {
        return credentialManager.getInspectorSecretKey(inspectorPublicKeyUID);
    }

}

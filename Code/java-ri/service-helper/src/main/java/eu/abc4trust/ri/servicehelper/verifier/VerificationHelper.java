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

package eu.abc4trust.ri.servicehelper.verifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.verifier.SynchronizedVerifierAbcEngineImpl;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenStorage;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * @author hgk
 * 
 */
public class VerificationHelper extends AbstractHelper {

    private static VerificationHelper instance;

    @Deprecated
    public static synchronized VerificationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String fileStoragePrefix,
            ArrayList<String> presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuerParamsResourceList, credSpecResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }

    /**
     * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
     *        VerifierAbcEnginge
     * @param presentationPolicyResourceList initialize helper with list of resources of
     *        PresentationPolicyAlternatives
     * @throws URISyntaxException
     */
    @Deprecated
    public static synchronized VerificationHelper initInstance(CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String fileStoragePrefix,
            ArrayList<String> presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(cryptoEngine, issuerParamsResourceList, credSpecResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }
    
    @Deprecated
    public static synchronized VerificationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String fileStoragePrefix,
            ArrayList<String> presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(cryptoEngine, issuerParamsResourceList, credSpecResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }    
    @Deprecated
    public static synchronized VerificationHelper initInstance(CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String fileStoragePrefix,
            ArrayList<String> presentationPolicyResourceList) throws URISyntaxException {
        if (instance != null) {
            throw new IllegalStateException("initInstance can only be called once!");
        }
        System.out.println("VerificationHelper.initInstance  (ArrayList)");
        instance = new VerificationHelper(cryptoEngine, fileStoragePrefix);
        //
        //    instance.setSystemParams(systemParamsResource);
        instance.addCredentialSpecifications(credSpecResourceList);
        instance.addIssuerParameters(issuerParamsResourceList);
        instance.setupIdemixEngine();
        instance.addInspectorPublicKeys(inspectorPublicKeyResourceList);

        //
        instance.addPresentationPolicy(presentationPolicyResourceList);

        System.out.println("VerificationHelper.initInstance : DONE");

        return instance;
    }

    public static synchronized VerificationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
       return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuerParamsResourceList, credSpecResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }
    public static synchronized VerificationHelper initInstance(CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(cryptoEngine, issuerParamsResourceList, credSpecResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }
    /**
     * @param credSpecResourceList
     * @param inspectorPublicKeyResourceList 
     * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
     *        VerifierAbcEnginge
     * @param presentationPolicyResourceList initialize helper with list of resources of
     *        PresentationPolicyAlternatives
     * @throws URISyntaxException
     */
    public static synchronized VerificationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, fileStoragePrefix, presentationPolicyResourceList);
    }
    
    public static synchronized VerificationHelper initInstance(CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
        return initInstance(cryptoEngine, issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, new String[0], fileStoragePrefix, presentationPolicyResourceList);
    }

    /**
     * @param credSpecResourceList
     * @param inspectorPublicKeyResourceList 
     * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
     *        VerifierAbcEnginge
     * @param presentationPolicyResourceList initialize helper with list of resources of
     *        PresentationPolicyAlternatives
     * @throws URISyntaxException
     */
    public static synchronized VerificationHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String[] revocationAuthorityParametersResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
      return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, revocationAuthorityParametersResourceList, fileStoragePrefix, presentationPolicyResourceList);
    }
    
    public static synchronized VerificationHelper initInstance(CryptoEngine cryptoEngine,
            /*String systemParamsResource, */String[] issuerParamsResourceList,
            String[] credSpecResourceList, String[] inspectorPublicKeyResourceList, String[] revocationAuthorityParametersResourceList, String fileStoragePrefix,
            String... presentationPolicyResourceList) throws URISyntaxException {
        if (instance != null) {
            throw new IllegalStateException("initInstance can only be called once!");
        }
        System.out.println("VerificationHelper.initInstance ([])");
        instance = new VerificationHelper(cryptoEngine, fileStoragePrefix);
        //
        //instance.setSystemParams(systemParamsResource);
        instance.addCredentialSpecifications(credSpecResourceList);
        instance.addIssuerParameters(issuerParamsResourceList);
        instance.setupIdemixEngine();
        instance.addInspectorPublicKeys(inspectorPublicKeyResourceList);
        //
        instance.addPresentationPolicy(presentationPolicyResourceList);
        //
        instance.addRevocationAuthorities(instance.keyManager, revocationAuthorityParametersResourceList);
        
        System.out.println("VerificationHelper.initInstance : DONE");

        return instance;
    }


    /**
     * @return true if VerificationHelper has been initialized
     */
    public static synchronized boolean isInit() {
        return instance != null;
    }

    /**
     * Only used in test - can reset static instance
     */
    public static synchronized void resetInstance() {
        System.err.println("WARNING VerificationHelper.resetInstance : " + instance);
        if((instance!=null) && (instance.uproveBindingManager != null) ) {
            System.err.println("WARNING VerificationHelper.resetInstance - Stop UProve Servicd");
            try {
                // try to stop uprove engine/service if running...
                @SuppressWarnings("unused")
                int exitCode = instance.uproveBindingManager.stop();
            } catch(Exception ignore) {
                System.err.println("Failed to stop UProve service : " + ignore);
            }
        }

        instance = null;
    }

    /**
     * @return initialized instance of VerificationHelper
     */
    public static synchronized VerificationHelper getInstance() {
        System.out.println("VerificationHelper.getInstance : " + instance);
        if (instance == null) {
            throw new IllegalStateException("initInstance not called before using VerificationHelper!");
        }
        return instance;
    }

    public VerifierAbcEngine engine;
    // public KeyManager verifierKeyManager;
    private Random random;
    private TokenStorage tokenStorage;

    
    // needed for 'reset'
    private UProveBindingManager uproveBindingManager = null;

    /**
     * holds map resources by filename (without path) and the bytes of resource
     */
    private final Map<String, byte[]> policyResouceMap = new HashMap<String, byte[]>();
    private final ObjectFactory of = new ObjectFactory();

    /**
     * Used when creating a presentationPolicy to be later fetched when verifying a presentation token.
     */
    Map<URI, RevocationInformation> revocationInformationStore = new HashMap<URI, RevocationInformation>();

    /**
     * Private constructor - initializes ABCE
     * 
     * @param fileStoragePrefix
     * @throws URISyntaxException
     */
    private VerificationHelper(CryptoEngine cryptoEngine, String fileStoragePrefix)
            throws URISyntaxException {
        System.out.println("VerificationHelper : create instance " + cryptoEngine + " : "
                + fileStoragePrefix);
        this.cryptoEngine = cryptoEngine;
        try {
            UProveUtils uproveUtils = new UProveUtils();

            AbceConfigurationImpl configuration = this.setupStorageFilesForConfiguration(fileStoragePrefix, cryptoEngine);
            configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe().getAbsolutePath());
            configuration.setUProvePortNumber(uproveUtils.getVerifierServicePort());
            configuration.setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
            configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

            Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(configuration, cryptoEngine));

            VerifierAbcEngine e = injector.getInstance(VerifierAbcEngine.class);

            this.engine = new SynchronizedVerifierAbcEngineImpl(e);
            this.keyManager = injector.getInstance(KeyManager.class);

            this.random = injector.getInstance(Random.class);
            
            this.tokenStorage = injector.getInstance(TokenStorage.class);

            if((cryptoEngine == CryptoEngine.UPROVE) || (cryptoEngine == CryptoEngine.BRIDGED)) {
                this.uproveBindingManager = injector.getInstance(UProveBindingManager.class);
            }

        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup Verifier !", e);
        }
    }


    /**
     * Adds extra policy resorces to VerificationHelper
     * 
     * @param presentationPolicyResourceList
     */
    public void addPresentationPolicy(String[] presentationPolicyResourceList) {
        System.out.println("VerificationHelper addPresentationPolicy from resoucres : "
                + presentationPolicyResourceList);
        ArrayList<String> list = new ArrayList<String>();
        for (String r : presentationPolicyResourceList) {
            list.add(r);
        }
        this.addPresentationPolicy(list);
    }

    /**
     * Adds extra policy resorces to VerificationHelper
     * 
     * @param presentationPolicyResourceList
     */
    public void addPresentationPolicy(ArrayList<String> presentationPolicyResourceList) {
        System.out.println("VerificationHelper addPresentationPolicy from resoucres : "
                + presentationPolicyResourceList);
        String current = null;
        try {

            for (String resource : presentationPolicyResourceList) {
                current = resource;
                int ix = resource.lastIndexOf("/");
                String key = resource;
                if (ix != -1) {
                    key = resource.substring(ix + 1);
                }
                System.out.println(" - add policy : " + key + " - resource : " + resource);

                byte[] b = new byte[1];
                InputStream is = getInputStream(resource);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                while (is.read(b) != -1) {
                    os.write(b);
                }
                byte[] policyBytes = os.toByteArray();
                @SuppressWarnings("unused")
                PresentationPolicyAlternatives presentationPolicy =
                (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(new ByteArrayInputStream(
                        policyBytes), false);
                // ok...
                this.policyResouceMap.put(key, policyBytes);
            }
        } catch (Exception e) {
            System.err.println("Init Failed - policy : " + current);
            e.printStackTrace();
            throw new IllegalStateException("Init Failed - policy ! : " + current, e);
        }
    }

    /**
     * @param policyName name of policy resource (without path)
     * @param applicationData if present - will be inserted on all presentation policies
     * @return PresentationPolicyAlternatives - patched with applicationData - marshaled to String
     * @throws Exception
     */
    public String createPresentationPolicy_String(String policyName, byte[] nonce,
            String applicationData) throws Exception {
        System.out.println("VerificationHelper - create policy String : " + policyName + " - data : "
                + applicationData);

        PresentationPolicyAlternatives pp_alternatives =
                this.createPresentationPolicy(policyName, nonce, applicationData, null);
        JAXBElement<PresentationPolicyAlternatives> result =
                this.of.createPresentationPolicyAlternatives(pp_alternatives);

        String xml = XmlUtils.toXml(result, false);

        System.out.println(" - createPolicy - return  XML");

        return xml;

    }
    
    public PresentationPolicyAlternatives modifyPresentationPolicy(PresentationPolicyAlternatives ppa, 
    		byte[] nonce, String applicationData, Map<URI, URI> revInfoUIDs) throws Exception{
    	modifyPPA(ppa, applicationData, nonce, revInfoUIDs);
    	return ppa;
    }

    /**
     * @param policyName name of policy resource (without path)
     * @param applicationData if present - will be inserted on all presentation policies
     * @param revInfoUIDs if present - will try to fetch revocation information based on the uids.
     * @return PresentationPolicyAlternatives - patched with applicationData
     * @throws Exception
     */
    public PresentationPolicyAlternatives createPresentationPolicy(String policyName, byte[] nonce,
            String applicationData, Map<URI, URI> revInfoUIDs) throws Exception {
        System.out.println("VerificationHelper - create policy : " + policyName + " - data : "
                + applicationData);

        PresentationPolicyAlternatives pp_alternatives;
        byte[] policyBytes = this.policyResouceMap.get(policyName);
        if (policyBytes == null) {
            System.err.println(" - policy not found : " + this.policyResouceMap);
            System.err.println(" - policy not found : " + this.policyResouceMap.keySet());
            throw new IllegalStateException("PresentationPolicy not found : " + policyName);
        }
        try {
            pp_alternatives =
                    (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(new ByteArrayInputStream(
                            policyBytes), false);
        } catch (Exception e) {
            System.err.println(" - could init sample XML - create default");
            e.printStackTrace();
            throw new IllegalStateException(
                    "Could not init PresentationPolicy - event though it should have been verifed...");
        }

        modifyPPA(pp_alternatives, applicationData, nonce, revInfoUIDs);

        return pp_alternatives;
    }
    
    private PresentationPolicyAlternatives modifyPPA(PresentationPolicyAlternatives pp_alternatives, String applicationData, 
    		byte[] nonce, Map<URI, URI> revInfoUIDs) throws Exception{

        // try to make sure that RevocationInformation is only fetch once per RevAuth
        Map<URI, RevocationInformation> revocationInformationMap = new HashMap<URI, RevocationInformation>();

    	for (PresentationPolicy pp : pp_alternatives.getPresentationPolicy()) {

            Message message = pp.getMessage();
            // set nonce
	        message.setNonce(nonce);


            // set application data
            if (applicationData != null) {

                System.out.println(" - SET APPLICATION DATA : " + applicationData);
                // message.setApplicationData(applicationData.getBytes());
                ApplicationData a = message.getApplicationData();
                a.getContent().clear();
                a.getContent().add(applicationData);
            }
                
            //  REVOCATION!
            for (CredentialInPolicy cred : pp.getCredential()) {
                List<URI> credSpecURIList = cred.getCredentialSpecAlternatives().getCredentialSpecUID();
                boolean containsRevoceableCredential = false;
                CredentialSpecification credSpec = null;
                for(URI uri : credSpecURIList) {
                    try {
                        credSpec = keyManager.getCredentialSpecification(uri);
                        if(credSpec.isRevocable()) {
                            containsRevoceableCredential = true;
                            break;
                        }
                    } catch(KeyManagerException ignore) {
                    }
                }
                if(containsRevoceableCredential) {
                    IssuerAlternatives ia = cred.getIssuerAlternatives();
                    System.out.println("WE HAVE REVOCEABLE CREDENTIAL : " + ia);
                    for (IssuerParametersUID ipUid : ia.getIssuerParametersUID()) {
                        IssuerParameters ip = keyManager.getIssuerParameters(ipUid.getValue());
                        if(ip!=null && ip.getRevocationParametersUID()!=null) {
                            // issuer params / credspec has revocation...
                        	RevocationInformation ri;
                        	System.out.println("revInfoUIDs: " + revInfoUIDs);
                        	if(revInfoUIDs != null){
                        		System.out.println("Trying to get revInfo under "+ credSpec.getSpecificationUID());
                        		URI revInformationUid = revInfoUIDs.get(credSpec.getSpecificationUID());
                        		ri = this.revocationInformationStore.get(revInformationUid);
                        		System.out.println("Got revInfo: " + ri.getInformationUID()+", which should be the same as: "+revInformationUid);
                        	}else{
                        		ri = revocationInformationMap.get(ip.getRevocationParametersUID());
                        	}
                            System.out.println("RevocationInformation : " + ri);
                            if(ri==null) {
                            	System.out.println("Getting rev parameters uid information: " + ip.getRevocationParametersUID());
                                ri = keyManager.getLatestRevocationInformation(ip.getRevocationParametersUID());
                                revocationInformationMap.put(ip.getRevocationParametersUID(), ri);
                                this.revocationInformationStore.put(ri.getInformationUID(), ri);
                            }
                            System.out.println("RevocationInformation : " + ri.getInformationUID());
                            URI revInfoUid = ri.getInformationUID();
                            ipUid.setRevocationInformationUID(revInfoUid);                            
                        }
                    }
                }
            }
        }
        System.out.println(" - presentationPolicy created");

        return pp_alternatives;
    }

    /**
     * TODO : Remove when ABCE-Components can properly compare XML Private Helper
     * 
     * @param orig XML as string
     * @return patched XML as JaxB
     * @throws Exception
     */
    private PresentationToken getPatchedPresetationToken(String orig) throws Exception {
        String patched =
                orig.replace("ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\"",
                        "ConstantValue");

        patched = patched.replace(" xmlns=\"\"", "");
        patched = patched.replace("xmlns:ns2=\"http://abc4trust.eu/wp2/abcschemav1.0\"", "");

        return (PresentationToken) XmlUtils.getObjectFromXML(
                new ByteArrayInputStream(patched.getBytes()), true);

    }

    /**
     * @param policyName name of policy resource (without path)
     * @param applicationData if present - will be inserted on all presentation policies - must match
     *        application data supplied when creating Policy
     * @param presentationTokenXml as String
     * @return
     * @throws Exception
     */
    public boolean verifyToken_String(String policyName, byte[] nonce, String applicationData,
            String presentationTokenXml) throws Exception {
        System.out.println("VerificationHelper - verify token String: " + policyName
                + " - applicationData : " + applicationData + " - token : " + presentationTokenXml);

        PresentationPolicyAlternatives pp =
                this.createPresentationPolicy(policyName, nonce, applicationData, null);
        return this.verifyToken(pp, this.getPatchedPresetationToken(presentationTokenXml));
    }

    /**
     * @param policyName name of policy resource (without path)
     * @param applicationData if present - will be inserted on all presentation policies - must match
     *        application data supplied when creating Policy
     * @param presentationToken
     * @return
     * @throws Exception
     */
    public boolean verifyToken(String policyName, byte[] nonce, String applicationData,
            PresentationToken presentationToken) throws Exception {
        System.out.println("VerificationHelper - verify token : " + policyName
                + " - applicationData : " + applicationData);

        String orig = XmlUtils.toXml(this.of.createPresentationToken(presentationToken));
        presentationToken = this.getPatchedPresetationToken(orig);

        Map<URI, URI> revInfoUIDs = this.extractRevInfoUIDs(presentationToken);        
        
        PresentationPolicyAlternatives pp =
                this.createPresentationPolicy(policyName, nonce, applicationData, revInfoUIDs);

        return this.verifyToken(pp, presentationToken);
    }    
    
    private Map<URI, URI> extractRevInfoUIDs(PresentationToken pt){
    	Map<URI, URI> revInfoUIDs = null;
    	for(CredentialInToken cred : pt.getPresentationTokenDescription().getCredential()){
            boolean containsRevoceableCredential = false;            
            try {
                CredentialSpecification credSpec = keyManager.getCredentialSpecification(cred.getCredentialSpecUID());
                if(credSpec.isRevocable()) {
                    containsRevoceableCredential = true;
                }
            } catch(KeyManagerException ignore) {
            }
            if(containsRevoceableCredential) {
            	if(revInfoUIDs == null){
            		revInfoUIDs = new HashMap<URI, URI>();
            	}
            	revInfoUIDs.put(cred.getCredentialSpecUID(), cred.getRevocationInformationUID());
            }
        }
    	return revInfoUIDs;
    }

    /**
     * @param ppXml PresentationPolicyAlternatives as String
     * @param presentationTokenXml as String
     * @return
     * @throws Exception
     */
    public boolean verifyToken_String(String ppaXml, String presentationTokenXml) throws Exception {

        PresentationPolicyAlternatives ppa =
                (PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(
                        new ByteArrayInputStream(ppaXml.getBytes()), true);

        return this.verifyToken(ppa, this.getPatchedPresetationToken(presentationTokenXml));
    }

    /**
     * @param ppa PresentationPolicyAlternatives
     * @param presentationToken
     * @return
     * @throws Exception
     */
    public boolean verifyToken(PresentationPolicyAlternatives ppa, PresentationToken presentationToken)
            throws Exception {
        try {
            // verify in ABCE
            this.engine.verifyTokenAgainstPolicy(ppa, presentationToken, true);
            System.out.println(" - OK!");
            return true;
        } catch (TokenVerificationException e) {
            System.err.println(" - TokenVerificationException : " + e);
            throw e;
        } catch (Exception e) {
            System.err.println(" - Failed verifying token : " + e);
            return false;
        }

    }

    public byte[] generateNonce() {
        // TODO : is 10 bytes correct ?
        byte[] nonceBytes = new byte[10];
        this.random.nextBytes(nonceBytes);
        return nonceBytes;
    }

    
    public void registerSmartcardScopeExclusivePseudonym(BigInteger pse) throws IOException {
    	String primaryKey = DatatypeConverter.printBase64Binary(pse.toByteArray());
    	registerSmartcardScopeExclusivePseudonym(primaryKey);
    }
    public void registerSmartcardScopeExclusivePseudonym(byte[] pseValueAsBytes) throws IOException {
    	String primaryKey = DatatypeConverter.printBase64Binary(pseValueAsBytes);
    	registerSmartcardScopeExclusivePseudonym(primaryKey);
    }
	public void registerSmartcardScopeExclusivePseudonym(String b64Encoded_pseudonymValue) throws IOException {
    	String primaryKey = b64Encoded_pseudonymValue;
        
        if(! tokenStorage.checkForPseudonym(primaryKey)) {
        	System.out.println("registerSmartcardScopeExclusivePseudonym - register new pseudonym  - PseudonymPrimaryKey : " + primaryKey);
        	tokenStorage.addPseudonymPrimaryKey(primaryKey);
        } else {
        	System.out.println("registerSmartcardScopeExclusivePseudonym - already registered");
        }
    }

}

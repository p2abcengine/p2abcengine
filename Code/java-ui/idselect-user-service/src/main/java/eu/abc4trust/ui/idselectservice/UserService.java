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

package eu.abc4trust.ui.idselectservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;

import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiManageCredentialData;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.CredentialSpecInUi;
import eu.abc4trust.returnTypes.ui.IssuerInUi;
import eu.abc4trust.returnTypes.ui.UiCommonArguments;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
//import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcard.InsufficientStorageException;
import eu.abc4trust.smartcard.SecretBasedSmartcard;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBackup;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.smartcard.StaticUriToIDMap;
import eu.abc4trust.smartcard.Utils;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialDescriptions;
import eu.abc4trust.xml.CredentialDescriptionsEntry;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

@Path("/")
public class UserService {

    public static final String USER_NAME = "standalone_demo";
  
    private final static Logger logger = Logger.getLogger(UserService.class.getName());
  
    public static boolean touchThisBooleanToForceStaticInit = true;
    private static final boolean useSemaphore = false;
    private static boolean DEBUG = false;
    private static String deploymentVersionId = "N/A";
    public static String userServiceVersionId = "NOT RESOLVED YET";
    
    private final ObjectFactory of = new ObjectFactory();
    private UserAbcEngine engine;
    private final CardStorage cardStorage;

    private static final URI SECRET_BASED_SMARTCARD_URI = URI.create("secret://secretbased-smartcard-1234");

    // Leftovers
    static HashMap<String, PresentationToken> presentationTokens;
    static HashMap<String, IssuMsgOrCredDesc> issuanceMessages;
    static HashMap<String, IdentitySelectionUIWrapper> identitySelections;
    private static final Map<String, URI> contextMap = new HashMap<String, URI>();
//    private static final Map<String, CryptoEngine> cryptoEngineMap = new HashMap<String, CryptoEngine>();
	private static boolean userServiceBusy = false;
    static IdentitySelectionUIWrapper currentIdentitySelections;

    @Context
    ServletContext context;


    String[] pilotRun_issuerParamsResourceList = null;
    String[] pilotRun_credSpecResourceList =  null;
    //    String pilotRun_filePrefix = "";
    private static String fileStoragePrefix = "";

    private static String softwareSmartcardResource = null;
    //    private static int softwareSmartcardPin = -1;
    private static BasicSmartcard softwareSmartcard = null;

    private long credentialDeleterSleepTime;
    private static Thread revokedCredentialDeleter;
    
	private static boolean deploymentSpecificPropertiesInitialized;

    private static StringBuilder debugInfo = new StringBuilder();
    private static void addDebugInfo(String msg) {
        debugInfo.append(msg);
        debugInfo.append("\n");
    }
    private static void addDebugInfo(String msg, Throwable t) {
        debugInfo.append(msg);
        debugInfo.append(" - exception : " + t);
        debugInfo.append("\n");
        for(StackTraceElement ste : t.getStackTrace()) {
            debugInfo.append("    ");
            debugInfo.append(ste.toString());
            debugInfo.append("\n");
        }
        debugInfo.append("\n");

    }
    static {
        logger.fine("UserService static init ! - file.encoding : " + System.getProperty("file.encoding", null));
        addDebugInfo("UserService static init ! - file.encoding : " + System.getProperty("file.encoding", null));
        // specify behaviour in PolicyCredentialMatcher
        
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = true;

        String resourceFolder;
        try {
            addDebugInfo("Init User ABCE");
            resourceFolder = initUserABCE();
            addDebugInfo("Init User ABCE DONE!");
        } catch (Exception e) {
            addDebugInfo("Could not init User ABCE", e);
            throw new IllegalStateException("Could not init User ABCE", e);
        }

//        // check for software smartcards
//        softwareSmartcardResource = System.getProperty("UserSoftwareSmartcard", null);
//        logger.warning("LOG W Try to use SoftwareSmartcard : " + softwareSmartcardResource);
//        System.out.println("Sys O Try to use SoftwareSmartcard : " + softwareSmartcardResource);
//        System.err.println("Sys E Try to use SoftwareSmartcard : " + softwareSmartcardResource);
//        
//        addDebugInfo("softwareSmartcardResource : " + softwareSmartcardResource);
//        //
//        if((softwareSmartcardResource!=null) && (softwareSmartcard == null)) {
//            logger.fine("Try to use SoftwareSmartcard : " + softwareSmartcardResource);
//            try {
//                softwareSmartcard = FileSystem.loadObjectFromResource(softwareSmartcardResource);
//                addDebugInfo("Try to use SoftwareSmartcard : " + softwareSmartcardResource);
//            } catch(Exception e) {
//                addDebugInfo("UserSoftwareSmartcard could not be loaded from : " + softwareSmartcardResource, e);
//                throw new IllegalStateException("UserSoftwareSmartcard could not be loaded from : " + softwareSmartcardResource, e);
//            }
//
//        } else {
//            addDebugInfo("UserSerivce Initialize ABCE could not initialize Software Smartcards" + softwareSmartcard);
//            logger.warning("UserSerivce Initialize ABCE could not initialize Software Smartcards");
//            throw new IllegalStateException("UserSerivce Initialize ABCE could not initialize Software Smartcards");
//        }
        if(softwareSmartcard != null) {
          System.out.println("INFO : SoftwareSmartcard - initialized as SecretBased Smartcard : " + softwareSmartcard);
        } else {
          System.err.println("ERROR : SoftwareSmartcard - not initialized as SecretBased Smartcard ??");
        }
        
        logMemoryUsage();
        
        addDebugInfo("UserSerivce Version Numbers - deployment Id / userservice Id : " + deploymentVersionId + " / " + userServiceVersionId);
        logger.fine("UserSerivce Version Numbers - deployment Id / userservice Id : " + deploymentVersionId + " / " + userServiceVersionId);
        
    }
    
    private static String logMemoryUsage() {
      long kb = 1024;
      long mb = 1024 *1204;
      long total = Runtime.getRuntime().totalMemory(); //  / mb;
      long free = Runtime.getRuntime().freeMemory(); // / mb;
      long max = Runtime.getRuntime().maxMemory(); // / mb;

      long used = (total - free); // / mb;
      
//      String memUsageString.format("Memory Usage : Used + Free = Total : Max : %,2d + %,2d = %,2d : %,2d",used, free, total, max);
      String memUsage = String.format("Memory Usage : Used + Free = Total : Max (Kilobytes) : %,2d + %,2d = %,2d : %,2d",used/kb, free/kb, total/kb, max/kb);
//      String memUsageString.format("Memory Usage : Used + Free = Total : Max (Megabytes) : %,2d + %,2d = %,2d : %,2d",used/mb, free/mb, total/mb, max/mb);
      logger.fine(memUsage);
      return memUsage;
    }
    
    private void saveSoftwareSmartcard() {
        if(softwareSmartcard != null) {
            logger.fine("saveSoftwareSmartcard to resource " + softwareSmartcardResource);
            try {
              if(softwareSmartcard instanceof SoftwareSmartcard) {
//                FileSystem.storeObjectInFile(softwareSmartcard, softwareSmartcardResource);
                System.out.println(" - skipped storing SoftwareSmartcard..." + softwareSmartcard);
              } else {
                System.out.println(" - do not store BasicSmartcard of type : " + softwareSmartcard);
              }
            } catch(Exception e) {
                System.err.println("WARN : Failed to store software smartcard to resource " + softwareSmartcardResource + " - error : " + e);
            }
        }
    }

    public UserService() throws Exception {
        // are these per session ?
        this.engine = UserHelper.getInstance().getEngine();
        this.cardStorage = UserHelper.getInstance().cardStorage;

        //
        if (UserService.revokedCredentialDeleter == null) {
            System.out
            .println("Launching thread for revoked credential deleter");
            this.engine = UserHelper.getInstance().getEngine();
            // Check every five minutes.
            this.credentialDeleterSleepTime = 60 * 60 * 1000;
            RevokedCredentialDeleter rcd = new RevokedCredentialDeleter(
                    this.engine, this.credentialDeleterSleepTime);
            UserService.revokedCredentialDeleter = new Thread(rcd);
            UserService.revokedCredentialDeleter.start();
            
            //Also start the detector for smart cards. 
//            SmartcardServletContext.cardStorageReference
//            .getAndSet(this.cardStorage);
//            SmartcardServletContext.startDetector();
            
            try{
            	Thread.sleep(500);
            }catch(Exception e){
            	//Do nothing other than note it happened, since we want to run anyways. 
            	logger.fine("Could not sleep - was interrupted!: "+e.getMessage());
            }
        }


    }

    private static String initUserABCE() throws Exception {
      
        String userServiceRunDir = System.getProperty("UserServiceRunDir");
        if(userServiceRunDir == null) {
          throw new IllegalStateException("UserServiceRunDir should have been set from executeable jar...");
        }
        
        String abc4Trust_LOCALAPPDATA = System.getProperty("ABC4TRUST_LOCALAPPDATA");
        if(abc4Trust_LOCALAPPDATA == null) {
          throw new IllegalStateException("ABC4TRUST_LOCALAPPDATA should have been set from executeable jar...");
        }
        
        addDebugInfo("userServiceRunDir == " + userServiceRunDir + " - abc4Trust_LOCALAPPDATA : " + abc4Trust_LOCALAPPDATA);

        String resourceFolder = userServiceRunDir + (userServiceRunDir.endsWith("/") || userServiceRunDir.endsWith("\\") ? "" : "/") + "resources";

        // TODO : FIND USERs HOME !!!
        fileStoragePrefix = abc4Trust_LOCALAPPDATA + (abc4Trust_LOCALAPPDATA.endsWith("/") || abc4Trust_LOCALAPPDATA.endsWith("\\")? "" : "/") + "user_storage/";
        addDebugInfo("fileStoragePrefix == " + fileStoragePrefix);
        logger.fine("fileStoragePrefix : " + fileStoragePrefix);

        if(UserHelper.isInit()) {
            logger.fine("UserService already init !");
        } else {
            logger.fine("UserService initiating !");
            
            // load all - should only be 1
            String systemParamsResource = resourceFolder + "/system_params.xml";
            SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamsResource); 
            
            try {
              List<IssuerParameters> issuerParamsList = new ArrayList<IssuerParameters>();

              // load all credspecs in folder
              List<CredentialSpecification> credSpecList = new ArrayList<CredentialSpecification>();

              // load all inspector public keys in folder
              List<InspectorPublicKey> inspectorPublicKeyList = new ArrayList<InspectorPublicKey>();

              List<RevocationAuthorityParameters> revocationAuthorityParametersList = new ArrayList<RevocationAuthorityParameters>();
              UserHelper.initInstance(systemParams, issuerParamsList, fileStoragePrefix, credSpecList, inspectorPublicKeyList, revocationAuthorityParametersList);
              SystemParameters check = UserHelper.getInstance().keyManager.getSystemParameters();
              System.out.println("RESOURCE SP : " + systemParams.getSystemParametersUID());
              System.out.println("STORAGE  SP : " + check.getSystemParametersUID());
              
            } catch(Exception e) {
              System.err.println("Failed to perform SystemParameter test ??");
              e.printStackTrace();
            } finally {
              UserHelper.resetInstance();
            }
            
            // load all issuer params in folder

            List<IssuerParameters> issuerParamsList = FileSystem.findAndLoadXmlResourcesInDir(resourceFolder, "issuer_params");

            // load all credspecs in folder
            List<CredentialSpecification> credSpecList = FileSystem.findAndLoadXmlResourcesInDir(resourceFolder, "cred_spec");

            // load all inspector public keys in folder
            List<InspectorPublicKey> inspectorPublicKeyList = FileSystem.findAndLoadXmlResourcesInDir(resourceFolder, "inspector_publickey");

            List<RevocationAuthorityParameters> revocationAuthorityParametersList = FileSystem.findAndLoadXmlResourcesInDir(resourceFolder, "revocation_authority");

            if (issuerParamsList.size() == 0) {
                throw new IllegalStateException(
                        "Did not find any issuer resources. Please look in: "
                                + resourceFolder);
            }

            for(IssuerParameters ip : issuerParamsList){
              logger.fine(" - ip : " + ip.getAlgorithmID() + " : " + ip.getVersion() + " - " + ip.getParametersUID());
              addDebugInfo(" - ip : " + ip.getAlgorithmID() + " : " + ip.getVersion() + " - " + ip.getParametersUID());
            }
            if((issuerParamsList.size() != credSpecList.size()) && ((issuerParamsList.size() / 2) != credSpecList.size())) {
                logger.fine("Warning : Mismatch between number of IssuerParmameter and number of credspecs - " + issuerParamsList.size() + " (and / 2 ) != " + credSpecList.size());
                addDebugInfo("Warning : Mismatch between number of IssuerParmameter and number of credspecs - " + issuerParamsList.size() + " (and / 2 ) != " + credSpecList.size());
            }
            
            readDeploymentSpecificProperties(resourceFolder);

            UserHelper.initInstance(systemParams, issuerParamsList, fileStoragePrefix, credSpecList, inspectorPublicKeyList, revocationAuthorityParametersList);

            // always overwrite Rev Aut info...
            for(RevocationAuthorityParameters rap : revocationAuthorityParametersList) {
              UserHelper.getInstance().keyManager.storeRevocationAuthorityParameters(rap.getParametersUID(), rap);
            }

            
            SecretBasedSmartcard secretBasedSmartcard = new SecretBasedSmartcard(UserService.USER_NAME, UserHelper.getInstance().credentialManager, UserHelper.getInstance().keyManager);
            softwareSmartcard  = secretBasedSmartcard;            
            try {
              Secret secret = UserHelper.getInstance().credentialManager.getSecret(UserService.USER_NAME, SECRET_BASED_SMARTCARD_URI); 
              System.out.println(" - Secret already generated!");
              secretBasedSmartcard.initFromSecret(secret);
            } catch(SecretNotInStorageException e) {
              System.out.println(" - Secret NOT in CredentialManager - try to create!");
              secretBasedSmartcard.initNew(systemParams, SECRET_BASED_SMARTCARD_URI);
              Secret secret = secretBasedSmartcard.getSecret();
              UserHelper.getInstance().credentialManager.storeSecret(UserService.USER_NAME, secret);
              System.out.println(" - Secret Generated! and stored in CredentialManager!");
            }

            presentationTokens =  new HashMap<String, PresentationToken>();
            issuanceMessages =  new HashMap<String, IssuMsgOrCredDesc>();
            identitySelections = new HashMap<String, IdentitySelectionUIWrapper>();
            
        }
        logger.fine("UserService init ! DONE");
        
        return resourceFolder;
    }

    private static String getResourceNames(String[] resourceList) {
        StringBuilder s = new StringBuilder();
        if(resourceList.length==0) {
            s.append("Length == 0");
        } else {
            boolean first = true;
            for(String r : resourceList) {
                if(first) {
                    first = false;
                } else {
                    s.append(", ");
                }
                s.append(r);
            }
        }
        return s.toString();
    }
    
    private static void readDeploymentSpecificProperties(String resourceFolder) {
    	if(deploymentSpecificPropertiesInitialized) {
    		return;
    	}
        InputStream is = null;
		try {
		    try {
	            is = FileSystem.getInputStream("/deploymentspecific.properties");
	            logger.fine("Reading deploymentspecific.properties from '/deploymentspecific.properties'");
	            addDebugInfo("Reading deploymentspecific.properties from '/deploymentspecific.properties'");
		    } catch(IOException ignore) {
		    }
			if(is==null) {
	            try {
	                is = FileSystem.getInputStream(resourceFolder + "/deploymentspecific.properties");
	                logger.fine("Reading deploymentspecific.properties from '" + resourceFolder + "/deploymentspecific.properties'");
                    addDebugInfo("Reading deploymentspecific.properties from '" + resourceFolder + "/deploymentspecific.properties'");
	            } catch(IOException ignore) {
	            }
			}
			if(is != null) {
			    try {
    				Properties props = new Properties();
    				props.load(is);
    				
    				DEBUG = Boolean.parseBoolean(props.getProperty("printDebugInfo", "false"));
    				logger.fine("Allow debug printing: "+DEBUG);
    				
    				deploymentVersionId = props.getProperty("deploymentVersionId", "N/A");
			    } catch(Exception e) {
	                logger.log(Level.WARNING, "Failed to load properties.", e);
			    }
			} else {
			    logger.warning("No deployment specific properties.");
			}
		} finally {
			deploymentSpecificPropertiesInitialized = true;
			if(is!=null) {
			  try {
			    is.close();
			  } catch(Exception ignore){};
			}
		}
    }
    
    @GET()
    @Path("/user/getUiPresentationArguments/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response getUiPresentationArguments(@PathParam ("SessionID") final String sessionId) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        logger.fine("--- getUiPresentationArguments - session ID : " + sessionId + " - wrapper : " + isw);
        if(isw==null) {
            String msg = "Internal Error ! - IdentitySelectionUIWrapper should be defined for session : " + sessionId;
            System.err.println(msg);
            throw new Exception(msg);
        }

        if(isw.getUiPresentationArguments()==null) {
            String msg = "Internal Error ! - uiPresentationArguments should have been defined set on IdentitySelectionUIWrapper";
            System.err.println(msg);
            throw new Exception(msg);
        }
        logger.fine("- uiPresentationArguments : " + isw.getUiPresentationArguments());
        logger.fine("- uiPresentationArguments : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(isw.getUiPresentationArguments()), false));
//        System.out.println("- uiPresentationArguments : " + isw.getUiPresentationArguments());
//        System.out.println("- uiPresentationArguments : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(isw.getUiPresentationArguments()), false));
//        logger.fine("- uiPresentationArguments : " + isw.getUiPresentationArguments().data.inspectors);
//        logger.fine("- uiPresentationArguments : " + isw.getUiPresentationArguments().data.issuers);
        return Response.ok(isw.getUiPresentationArguments()).build();
    }

    @POST()
    @Path("/user/setUiPresentationReturn/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response setUiPresentationReturn(@PathParam ("SessionID") final String sessionId, UiPresentationReturn uiPresentationReturn) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        logger.fine("--- setUiPresentationReturn - session ID : " + sessionId + " - wrapper : " + isw);
        logger.fine("- UiPresentationReturn : " + uiPresentationReturn);
        //      logger.fine("- UiPresentationReturn XML : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(uiPresentationReturn)));
        if(isw==null) {
            String msg = "Internal Error ! - IdentitySelectionUIWrapper should be defined for session : " + sessionId;
            System.err.println(msg);
            throw new Exception(msg);
        }
        // TODO : Should this be fixed in 'abce-components' ??
        if(uiPresentationReturn.chosenPseudonymList == -1) {
            uiPresentationReturn.chosenPseudonymList = 0;
        }

        isw.setUiPresentationReturn(uiPresentationReturn);
        return Response.noContent().build();
    }

    @GET()
    @Path("/user/getUiIssuanceArguments/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response getUiIssuanceArguments(@PathParam ("SessionID") final String sessionId) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        logger.fine("--- getUiIssuanceArguments - session ID : " + sessionId + " - wrapper : " + isw);
        if(isw==null) {
            String msg = "Internal Error ! - IdentitySelectionUIWrapper should be defined for session : " + sessionId;
            System.err.println(msg);
            throw new Exception(msg);
        }

        if(isw.getUiIssuanceArguments()==null) {
            String msg = "Internal Error ! - uiIssuanceArguments should have been defined set on IdentitySelectionUIWrapper";
            System.err.println(msg);
            throw new Exception(msg);
        }
        logger.fine("- uiIssuanceArguments : " + isw.getUiIssuanceArguments());
        // logger.fine("- uiIssuanceArguments : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(isw.getUiIssuanceArguments()), false));
        return Response.ok(isw.getUiIssuanceArguments()).build();
    }

    @POST()
    @Path("/user/setUiIssuanceReturn/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response setUiIssuanceReturn(@PathParam ("SessionID") final String sessionId, UiIssuanceReturn uiIssuanceReturn) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        logger.fine("--- setUiIssuanceReturn - session ID : " + sessionId + " - wrapper : " + isw);
        logger.fine("- uiIssuanceReturn - session ID : " + uiIssuanceReturn);
        //      logger.fine("- uiIssuanceReturn XML : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(uiIssuanceReturn)));
        if(isw==null) {
            String msg = "Internal Error ! - IdentitySelectionUIWrapper should be defined for session : " + sessionId;
            System.err.println(msg);
            throw new Exception(msg);
        }
        // TODO : Should this be fixed in 'abce-components' ??
        if(uiIssuanceReturn.chosenPseudonymList == -1) {
            uiIssuanceReturn.chosenPseudonymList = 0;
        }

        isw.setUiIssuanceReturn(uiIssuanceReturn);
        return Response.noContent().build();
    }
    
    private final Semaphore createPresentationTokenSemaphore = new Semaphore(1, true);
    private int presentationCallsCounter = 0;
    
    /**
     * First call to create presentation token. Takes a presentation policy as input
     * If the policy cannot be satisfied, return 422.
     * If the policy requires user involvement, returns 203 + JSON
     * If the policy can be fulfilled without user involvement, return 200 + XML
     * @param sessionId
     * @param presentationPolicy
     * @return 422 if policy can not be satisfied, 203+JSON if user choice is required and 200+xml when done
     */
    @POST()
    @Path("/user/createPresentationToken/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_XML, MediaType.TEXT_PLAIN})
    public Response createPresentationToken(@PathParam ("SessionID") final String sessionId,
            final JAXBElement<PresentationPolicyAlternatives> presentationPolicy_jaxb) {
        final PresentationPolicyAlternatives presentationPolicy = presentationPolicy_jaxb.getValue();
    	if(presentationCallsCounter > 0){
    		logger.warning("WARNING: CreatePresentationToken called again before the last one finished. presentationCallsCounter="+presentationCallsCounter);
    	}
    	presentationCallsCounter++;
    	if(useSemaphore){
    		try {
				createPresentationTokenSemaphore.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException("Could not aquire semaphore lock - was interrupted", e);
			}
    	}
    	
        logger.fine("--- createPresentationToken - session ID : " + sessionId);
        logMemoryUsage();

        try{
        	logger.fine("-- -- " + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(presentationPolicy)));

            if(!this.engine.canBeSatisfied(UserService.USER_NAME, presentationPolicy)){
                logger.fine("cannot satisfy policy, halting!");
                finishPresentationCount();
                return Response.status(422).build();
            }
            logger.fine("-- -- policy can be satisfied!");
        }catch(Exception e){
            logger.log(Level.WARNING, "engine.canBeSatisfied threw an exception:", e);
            finishPresentationCount();
            return Response.status(422).build();
        }catch(Throwable t) {
            logger.log(Level.SEVERE, "internal error calling : engine.canBeSatisfied", t);
            finishPresentationCount();
            return Response.status(422).build();
        }

        final IdentitySelectionUIWrapper isw = new IdentitySelectionUIWrapper();


        identitySelections.put(sessionId, isw);
        // TODO: REMOVE AFTER TEST!
        currentIdentitySelections = isw;

        Thread thread = new Thread(new Runnable(){
            @SuppressWarnings("deprecation")
			public void run(){
                try{
                    UiPresentationArguments uiPresentationArguments = UserService.this.engine.createPresentationToken(UserService.USER_NAME, presentationPolicy);
                    // this will sleep thread until selectiont is done...
                    isw.selectPresentationTokenDescription(uiPresentationArguments);
                    // - here we wait for return...
                    PresentationToken pt = UserService.this.engine.createPresentationToken(UserService.USER_NAME, isw.getUiPresentationReturn());
                    presentationTokens.put(sessionId, pt);
                } catch(Exception e){
                    logger.log(Level.WARNING, "internal err! :", e);

                    //TODO something to store the exception in isw to allow for error handling
                } finally {
                    // set done!
                    isw.done = true;
                }
            }
        });

        logger.fine("--- createpresentationToken starting thread and going to sleep");
        userServiceBusy = true;
        thread.start();

        try {
            while((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            logger.fine("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {
            	finishPresentationCount();
            	userServiceBusy = false;
                return Response.status(500).build();
            }
        }
        logger.fine("### --- createpresentationToken woke up : " + sessionId + " : "+presentationTokens.get(sessionId)+" "+isw.hasPresentationChoices()+" "+isw.done);
        if(isw.done || (presentationTokens.get(sessionId)!=null)) {
            logger.fine("### --- createPresentationToken finished without need for user interaction "  + isw.done + " : " + presentationTokens.get(sessionId));
            identitySelections.remove(sessionId);
            PresentationToken pt = presentationTokens.remove(sessionId);
            if((isw.getException() == null) && (pt != null)) {
                // !! saveSoftwareSmartcard
                this.saveSoftwareSmartcard();
                finishPresentationCount();
                userServiceBusy = false;
                return Response.ok(this.of.createPresentationToken(pt)).type(MediaType.TEXT_XML).build();
            }
        } else{
            logger.fine("### --- createPresentationToken has choices for ui selection");
            finishPresentationCount();
            userServiceBusy = false;
            return Response.status(203).entity("GO AHEAD CALL NEW UI FOR PRESENTATION").type(MediaType.TEXT_PLAIN).build();
        }
        logger.fine("### --- createpresentaitontoken - this will never be reached");
        finishPresentationCount();
        userServiceBusy = false;
        return Response.notAcceptable(null).build();
    }
    
    private void finishPresentationCount(){
    	presentationCallsCounter--;
    	if(useSemaphore){
    		createPresentationTokenSemaphore.release();
    	}
    }


    // new method for createPresentationTokenIdentitySelection
    // takes JSON as input, delivers it to ISWrapper and waits until there is something in presentationTokens
    @POST()
    @Path("/user/createPresentationTokenIdentitySelection/{SessionID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_XML)
    public Response createPresentationTokenIdentitySelection(@PathParam ("SessionID") final String sessionId,
            final String choice) throws Exception {

        logger.fine("-- createPresentationToken - got IdentitySelection - for Session ID : " + sessionId + " - JSon choice : [" + choice + "]");

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            logger.warning("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        logger.fine("- isw " + isw);
        logger.fine("- isw " + isw.hasPresentationChoices());
        logger.fine("- isw " + isw.getUiPresentationReturn());
        logger.fine("- isw done ? " + isw.done);

        // if null - user cancelled / closed window. This was not detected by UI - so we notify by setting 'null' UIIssuanceReturn!
        if(isw.getUiPresentationReturn()==null) {
            logger.fine("CreatePresentationToken called but ID Selection not set ? User has cancelled/closed windows... sessionID: "+sessionId+", ABORTING");
            isw.setUiPresentationReturn(null);
            //          identitySelections.remove(sessionId);
            //          return Response.status(422).build();
        }


        try{
        	userServiceBusy = true;
            while(!isw.done) {
                Thread.sleep(200);
            }
        } catch(InterruptedException e){
            if(!isw.done) {
                logger.fine("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            logger.warning("- interrupted ERROR 500");
            userServiceBusy = false;
            return Response.status(500).build();
        }
        userServiceBusy = false;
        //
        PresentationToken pt = presentationTokens.remove(sessionId);
        identitySelections.remove(sessionId);
        if(pt == null){
            logger.fine("- No PresentationToken : ERROR 422");

            logger.fine("Unknown PresentationToken for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        // !! saveSoftwareSmartcard
        this.saveSoftwareSmartcard();
        
        JAXBElement<PresentationToken> ptJaxB = this.of.createPresentationToken(pt);
//        String ptXml = XmlUtils.toXml(ptJaxB);
//        logger.fine("- PresentationToken XML : " + ptXml);
        logger.fine("- PresentationToken - A OK - return http : 200");

        return Response.ok(ptJaxB).build();
    }



    /**
     * Takes an IssuanceMessage and passes it on to the ABC engine.
     * If the ABC engine requires user interaction via the UI, a
     * JSON message is returned with status 203 otherwise an
     * IssuanceMessage (encoded as XML) is returned with status 200
     * or an empty message and status 204 is returned if the issuer
     * does not expect a reply.
     * 
     * All exceptions results in an empty message and status 500.
     * 
     * @param sessionId Current sessionId
     * @param mess IssuanceMessage as XML
     * @return XML with status 200, JSON with status 203 or empty message with status 204 or 500 or:
     * 501 which means that there is not space on the card.
     */
    @POST()
    @Path("/user/issuanceProtocolStep/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_XML, MediaType.TEXT_PLAIN})
    public Response issuanceProtocolStep(@PathParam ("SessionID") final String sessionId,
            @QueryParam("startRequest") String startIssuanceUrl,
            @QueryParam("stepRequest") String stepIssuanceUrl,
            final JAXBElement<IssuanceMessage> mess_jaxb) throws Exception {
        final IssuanceMessage mess = mess_jaxb.getValue();
        logger.fine("-- issuanceProtocolStep: "+sessionId);
        if(DEBUG){
        	logger.fine("-- issuanceMessage - incoming : "+XmlUtils.toXml(this.of.createIssuanceMessage(mess), false));
        }
        logMemoryUsage();
        
        if((startIssuanceUrl != null) && (stepIssuanceUrl != null)){
            startIssuanceUrl = URLDecoder.decode(startIssuanceUrl, "UTF-8");
            stepIssuanceUrl = URLDecoder.decode(stepIssuanceUrl, "UTF-8");
        }
        if(DEBUG){
        	logger.fine("-- issuanceProtocolStep - startRequest: "+startIssuanceUrl);
        	logger.fine("-- issuanceProtocolStep - stepRequest: "+stepIssuanceUrl);
        }

//        CryptoEngine cryptoEngine = cryptoEngineMap.get(sessionId);
//        if(cryptoEngine == null){
//            IssuancePolicy ip = null;
//            try{ip = (IssuancePolicy) XmlUtils.unwrap(mess.getAny(), IssuancePolicy.class);}
//            catch(Exception e){
//                try{ip = (IssuancePolicy) mess.getAny().get(1);}
//                catch(Exception ex){
//                    System.err.println("WARNING: Neither unwrapping worked!");
//                }
//            }
//            if(ip != null){
////                if(this.engine.listCredentials().size() > 6){
////        	        logger.fine("Cannot issue the 8'th credential - technical problems. Tell user to check revoked status");
////        	        return Response.status(501).build();
////                }
//            	
//                contextMap.put(sessionId, mess.getContext()); //for maybe calling some reload token code depending on cryptoEngine.
//                logger.fine("Mapping sessionId to this context: " + mess.getContext());
//                if(ip.getCredentialTemplate().getIssuerParametersUID().toString().endsWith("uprove")){
//                    logger.fine("From issuance message, it is assumed that we are working with UProve.");
//                    cryptoEngineMap.put(sessionId, CryptoEngine.UPROVE);
//                }else if(ip.getCredentialTemplate().getIssuerParametersUID().toString().endsWith("idemix")){
//                    logger.fine("From issuance message, it is assumed that we are working with Idemix.");
//                    cryptoEngineMap.put(sessionId, CryptoEngine.IDEMIX);
//                }else{
//                    System.err.println("Warning: issuer parameter contains no known cryptoEngine!");
//                }
//            }else{
//                System.err.println("Warning: Issuance Policy was null - this should not be the case!");
//            }
//        }

        if(identitySelections.get(sessionId)!= null) {
            logger.fine("-- Session identifier is already used");
            return Response.status(900).build();
        }
        final IdentitySelectionUIWrapper isw = new IdentitySelectionUIWrapper();



        identitySelections.put(sessionId, isw);

        Thread thread = new Thread(new Runnable(){
            public void run(){
                try {
                    logger.fine("Starting Thread for IssanceProtocol Selection");
                    // @SuppressWarnings("deprecation")
                    IssuMsgOrCredDesc imOrDesc = new IssuMsgOrCredDesc();;
                    IssuanceReturn issuanceReturn = UserService.this.engine.issuanceProtocolStep(UserService.USER_NAME, mess);
                    if(issuanceReturn.uia!=null) {
                      isw.selectIssuanceTokenDescription(issuanceReturn.uia);
                      // here we wait for return...
                      imOrDesc.im = UserService.this.engine.issuanceProtocolStep(UserService.USER_NAME, isw.getUiIssuanceReturn());
                    } else {
                      imOrDesc.im = issuanceReturn.im;
                      imOrDesc.cd = issuanceReturn.cd;
                    }
                    
//                    IssuMsgOrCredDesc imOrDesc = UserService.this.engine.issuanceProtocolStep(UserService.USER_NAME, mess, isw);
                    
                    logger.fine("UserABCE Creaded IssuanceMessage : " + imOrDesc);
                    issuanceMessages.put(sessionId,imOrDesc); //add to include IdentitySelectionWrapper
                    logger.fine("Stored IssuanceMessage for session : " + sessionId + " : " + imOrDesc);
                } catch(Exception e) {
                    logger.log(Level.WARNING, "internal err (Exception)", e);
                    isw.setException(e);
                    //put e in isw to allow for error handling
                } catch(Throwable e) {
                    logger.log(Level.WARNING, "internal err (Throwable)");
                    //put e in isw to allow for error handling
                } finally {
                    // set done!
                    isw.done = true;
                }
            }
        });
        logger.fine("-- issuanceProtooclStep: starting thread and going to sleep");
        userServiceBusy = true;
        thread.start();
        try {
            while((issuanceMessages.get(sessionId)==null) && !isw.hasIssuanceChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            logger.fine("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((issuanceMessages.get(sessionId)==null) &&!isw.hasIssuanceChoices() && !isw.done) {
            	userServiceBusy = false;
                return Response.status(500).build();
            }
        }
        logger.fine("-- issuanceProtooclStep: waking up: "+issuanceMessages.get(sessionId)+ "- "+isw.hasIssuanceChoices()+" - "+isw.done);
        logger.fine("-- done waiting for wrapper!");
        if(isw.done || (issuanceMessages.get(sessionId)!=null)) {
            logger.fine("-- wrapper is done! ");
            identitySelections.remove(sessionId);
            IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
            if (userIm == null){
                // this is an error case!
                logger.fine("-- Error running Issuance Protocol!");
                if(isw.getException() != null){
                	if(isw.getException() instanceof InsufficientStorageException){
                		userServiceBusy = false;
                		return Response.status(501).build();
                	}
                }
                userServiceBusy = false;
                return Response.status(500).build();
            } else if (userIm.cd != null){
// TODO : NOT IMPLEMENTED FOR PATRAS
//                //Save information for reloading tokens if we are running UProve
//                if(cryptoEngineMap.get(sessionId) == CryptoEngine.UPROVE){
//                    if(contextMap.get(sessionId) != null){
//                        logger.fine("=====================\n\n Adding reload token info - ProtocolStep! \n\n=======================");
//                        UserHelper.getInstance().reloadTokens.addCredentialIssuer(contextMap.get(sessionId), userIm.cd, startIssuanceUrl, stepIssuanceUrl);
//                    }else{
//                        System.err.println("======== \n Reload token info should have been added, but no context was found under the session id "+sessionId);
//                    }
//                }

                // !! saveSoftwareSmartcard
                this.saveSoftwareSmartcard();
                userServiceBusy = false;
                return Response.status(204).build();
            }else{
            	if(DEBUG){
            		logger.fine("-- issuanceMessage - send back to issuer (no select) : "+XmlUtils.toXml(this.of.createIssuanceMessage(mess), false));
            	}else{
            		logger.fine("-- issuanceMessage - send back to issuer (no select) : "+mess);
            	}

                return Response.ok(this.of.createIssuanceMessage(userIm.im)).type(MediaType.TEXT_XML).build();
            }
        }else {
            logger.fine("-- wrapper has choices! ");
            userServiceBusy = false;
            return Response.status(203).entity("GO AHEAD CALL NEW UI FOR ISSUANCE").type(MediaType.TEXT_PLAIN).build();
        }

    }


    //fun stuff part!
    @POST()
    @Path("/user/issuanceProtocolStepSelect/{SessionID}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_XML})
    public Response issuanceProtocolStepSelect(@PathParam ("SessionID") final String sessionId,
            @QueryParam("startRequest") String startIssuanceUrl,
            @QueryParam("stepRequest") String stepIssuanceUrl,
            final String choice) throws Exception {

        logger.fine("issuanceProtocolStepSelect : "+sessionId + " - JSon choice : [" + choice + "]");

        if((startIssuanceUrl != null) && (stepIssuanceUrl != null)){
            startIssuanceUrl = URLDecoder.decode(startIssuanceUrl, "UTF-8");
            stepIssuanceUrl = URLDecoder.decode(stepIssuanceUrl, "UTF-8");
        }
        if(DEBUG){
	        logger.fine("-- issuanceProtocolStepSelect - startRequest: "+startIssuanceUrl);
	        logger.fine("-- issuanceProtocolStepSelect - stepRequest: "+stepIssuanceUrl);
        }

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            logger.fine("Unknown IdentitySelectionWrapper(1) for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        // if null - user cancelled / closed window. This was not detected by UI - so we notify by setting 'null' UIIssuanceReturn!
        if(isw.getUiIssuanceReturn()==null) {
            isw.setUiIssuanceReturn(null);
            //
        }
        try{
        	userServiceBusy = true;
            while(!isw.done) {
                logger.fine("Waiting for ISW to finish! " + isw.done);                
                Thread.sleep(200);
            }
        } catch(InterruptedException e){
            if(!isw.done) {
                logger.fine("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            userServiceBusy = false;
            return Response.status(500).build();
        }
        userServiceBusy = false;
        IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
        identitySelections.remove(sessionId);
        if(userIm == null){
            logger.warning("Unknown IdentitySelectionWrapper(2) for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        if (userIm.cd != null){ //The ABC Engine returned a credential description, so the protocol is done
// TODO : NOT IMPLEMENTED FOR PATRAS
//            //Save information for reloading tokens if we are running UProve
//            if(cryptoEngineMap.get(sessionId) == CryptoEngine.UPROVE){
//                if(contextMap.get(sessionId) != null){
//                    logger.fine("=====================\n Adding reload token info - ProtocolStepSelect! \n=======================");
//                    UserHelper.getInstance().reloadTokens.addCredentialIssuer(contextMap.get(sessionId), userIm.cd, startIssuanceUrl, stepIssuanceUrl);
//                }else{
//                    System.err.println("======== \n Reload token info should have been added, but no context was found under the session id "+sessionId);
//                }
//            }
            // !! saveSoftwareSmartcard
            this.saveSoftwareSmartcard();
            return Response.status(204).build();
        }else{ //The ABC engine returned a issuancemessage that has to be sent to the issuer
        	if(DEBUG){
        		logger.fine("-- issuanceMessage - send back to issuer (After select) : "+XmlUtils.toXml(this.of.createIssuanceMessage(userIm.im), false));
        	}else{
        		logger.fine("-- issuanceMessage - send back to issuer (After select) : "+userIm.im);
        	}
            return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
        }
    }


    @POST()
    @Path("/user/updateNonRevocationEvidence")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response updateNonRevocationEvidence() {

        @SuppressWarnings("unused")
        ObjectFactory of = new ObjectFactory();

        logger.fine("updateNonRevocationEvidence");

        try {
        	userServiceBusy = true;
            this.engine.updateNonRevocationEvidence(UserService.USER_NAME);
            userServiceBusy = false;
            logger.fine(" - updateNonRevocationEvidence Done");
            return Response.ok().build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, " - updateNonRevocationEvidence Failed", e);
            return Response.serverError().build();
        }

    }
    
    @POST
    @Path("/user/checkRevocationStatus")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response checkRevocationStatus(){
    	logger.fine("checkRevocationStatus");
    	
    	try{
    		userServiceBusy = true;
	        for (URI credUri : this.engine.listCredentials(UserService.USER_NAME)) {
	            if (this.engine.isRevoked(UserService.USER_NAME, credUri)) {
	                logger.fine("Deleting revoked credential: " + credUri);
	                this.engine.deleteCredential(UserService.USER_NAME, credUri);
	                logger.fine("Deleted revoked credential: " + credUri);
	            } else {
	                logger.fine("Credential OK: " + credUri);
	            }
	        }
	        userServiceBusy = false;
	    	logger.fine("checkRevocationStatus - done");
	    	return Response.ok().build();
    	}catch(Exception e){
	    	logger.log(Level.SEVERE, "checkRevocationStatus failed : ", e);
    		return Response.serverError().build();
    	}
    }

    @POST()
    @Path("/user/listCredentials")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response listCredentials() {

        @SuppressWarnings("unused")
        ObjectFactory of = new ObjectFactory();

        logger.fine("listCredentials");

        try {
            // List<URI> resp = engine.listCredentials();
            List<URI> resp = new ArrayList<URI>();
            resp.add(new URI("http://asdf.gh/jkl"));

            logger.fine(" - resp " + resp);
            StringBuilder sb = new StringBuilder();
            if (resp != null) {
                for (URI uri : resp) {
                    sb.append(uri);
                    sb.append("\n");
                }
            }
            return Response.ok(sb.toString()).build();
        } catch (Exception e) {
            logger.fine(" - failed");
            e.printStackTrace();
            return Response.serverError().build();
        }

    }

    @POST()
    @Path("/user/getCredentialDescription")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_XML)
    public Response getCredentialDescription(final String creduid) {

        ObjectFactory of = new ObjectFactory();

        logger.fine("getCredentialDescription : " + creduid);

        URI uri;
        try {
            uri = new URI(creduid);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            CredentialDescription resp = this.engine.getCredentialDescription(UserService.USER_NAME, uri);

            return Response.ok(of.createCredentialDescription(resp)).build();
        } catch (Exception e) {
            logger.fine(" - failed");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @POST()
    @Path("/user/deleteCredential")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_XML)
    public Response deleteCredential(final String creduid) {

        @SuppressWarnings("unused")
        ObjectFactory of = new ObjectFactory();

        logger.fine("getCredentialDescription : " + creduid);

        URI uri;
        try {
            uri = new URI(creduid);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            boolean result = this.engine.deleteCredential(UserService.USER_NAME, uri);
            logger.fine(" - call ok - deleted : " + result);
            if (result) {
                return Response.ok().build();
            } else {
                return Response.status(Status.NO_CONTENT).build();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, " - failed", e);
            return Response.serverError().build();
        }
    }


    /**
     * returns whether or not the card is the same as last time we checked. 
     * Returns: 
     * 204 if it is the same card, or
     * 410 if no card is currently present, or 
     * 406 if it is not the same card, or if it is the first time it's called (thus we still need PIN).  
     */
    //static boolean smartcardAvailable = false;
    static String lastCardReference = null;
    @GET()
    @Path("/user/checkSmartcard/{SessionID}")
    public Response checkSmartcard(@PathParam ("SessionID") final String sessionId) throws Exception {
        boolean sameCard = this.isSameCardStorageReference();
        logger.fine("checkSmartcard - available and same card ? : " + sameCard); // smartcardAvailable);
        if(sameCard) { // smartcardAvailable) {

            return Response.noContent().build();
        } else {
            // if software - and not 'sameCard' - card not unlocked yet...
            if(softwareSmartcard!=null) {
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
            // real card - check if card in reader...
        	if(this.cardStorage.getClosedSmartcards().size() == 0 && this.cardStorage.getSmartcards().size() == 0) {
        		//No card in card-reader
        		return Response.status(Status.GONE).build();
        	}else{
        		return Response.status(Status.NOT_ACCEPTABLE).build();
        	}
        }
    }

    @GET()
    @Path("/user/isSameSmartcard/{SessionID}")
    public Response isSameSmartcard(@PathParam ("SessionID") final String sessionId) throws Exception {
        boolean sameCard = this.isSameCardStorageReference();
        logger.fine("isSameSmartcard - available and same card ? : " + sameCard); // smartcardAvailable);
        if(sameCard) {
            return Response.noContent().build();
        } else {
        	return Response.status(Status.GONE).build();
        }
    }

    private String getCurrentCardReference() {
        System.out.println("getCardStorageReference : " + this.cardStorage.getSmartcards() + " : " + this.cardStorage.getClosedSmartcards());
        if(this.cardStorage.getClosedSmartcards().size()>0) {
            // we hav closed cards ??
            logger.fine("we have closed cards ?? : " + this.cardStorage.getClosedSmartcards());
            return null;
        } else if(this.cardStorage.getSmartcards().size()>0) {
            System.out.println("xx : " + this.cardStorage.getSmartcards());
            String currentCardReference = "currentCard" + this.cardStorage.getSmartcards();
            System.out.println("currentCardReference : "+ currentCardReference);
            return currentCardReference;
        } else {
            return null;
        }
    }
    private void storeCardStorageReference() {
        lastCardReference = this.getCurrentCardReference();
        System.out.println("storeCardStorageReference - after authenticate! " + lastCardReference);
    }
    private boolean isSameCardStorageReference() {
        String current = this.getCurrentCardReference();
        System.out.println("isSameCardStoarageReference - last : " + lastCardReference + " - cur :  " + current);
        if((lastCardReference!=null) && lastCardReference.equals(current)) {
            logger.fine("- still same card! " + lastCardReference);
            return true;
        } else {
            logger.fine("- card updated!");
            return false;
        }
    }

    /**
     * The pins in pinsStr must match in order and number with the list of
     * smartcards returned by {@code this.cardStorage.getClosedSmartcards();}
     * 
     * @param pinsStr
     * @return
     * @throws Exception
     */
    @POST()
    @Path("/user/unlockSmartcards/{SessionID}")
    @Consumes({ MediaType.TEXT_PLAIN })
    public Response unlockSmartcards(@PathParam ("SessionID") final String sessionId, final String pinsStr) throws Exception {
        System.out.println("unlockSmartcards - called with: " + sessionId +" and pinstr: " + "xxxx - softwareSmart : " + softwareSmartcard ); // pinsStr);
        //
        if(softwareSmartcard!=null && lastCardReference==null) {
            // thismight be first we ust smartcard!
            System.out.println("is this first time we use smartcard ?? : " + softwareSmartcard + " : " + lastCardReference + " - storage size : " + this.cardStorage.getSmartcards().size());

            if(this.cardStorage.getSmartcard(softwareSmartcard.getDeviceURI(Integer.parseInt(pinsStr))) == null) {
              // not added!
              System.out.println("unlockSmartcards - add software smartcard : " + softwareSmartcard + " : " + this.cardStorage.getSmartcards());
              boolean status = this.cardStorage.addSmartcard(softwareSmartcard, Integer.parseInt(pinsStr));
  
              System.out.println("Unlock - add card status : " + status + " - current smartcars : " + this.cardStorage.getSmartcards() + " - closed cards : " + this.cardStorage.getClosedSmartcards() );
              if(!status) {
                System.err.println("Unlock - software smartcard could not be unlocked...");
                  return Response.status(Status.UNAUTHORIZED).build();
              }
            } else {
              System.out.println("unlockSmartcards - card already added! - but register it as last used!");
            }
            // save card
            this.storeCardStorageReference();

            return Response.noContent().build();
        } else {
          System.err.println("NO softwareSmartcard FOUND ?? : " + softwareSmartcard + " : " + this.cardStorage.getSmartcards().size());
        }

        if(this.cardStorage.getSmartcards().size() == 1){
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                try {
                    this.cardStorage.getSmartcards().get(uri).getDeviceID(Integer.parseInt(pinsStr));
                } catch(IllegalStateException e) {
                    // card has been removed!
                    logger.fine("Card has been removed ?? " + e);
                    break;
                }
                // - go ahead!
                if(this.cardStorage.getSmartcards().get(uri).getDeviceURI(Integer.parseInt(pinsStr)) == null){
                    return Response.status(Status.CONFLICT).build();
                }else{
                    return Response.noContent().build();
                }
            }
        }


        int pin = Integer.parseInt(pinsStr);

        int smartcards = this.cardStorage.getClosedSmartcards().size();
        logger.fine("Smartcardpins. Size of closed smartcards: "+smartcards);
        if(smartcards == 1){
            return this.checkPinAndAddSmartcardHelper(pin);
        }else if(smartcards > 1){
            logger.fine("More than one smartcard was found. Aborting!");
            return Response.status(Status.CONFLICT).build();
        }else{
            int waitSeconds = 0;
            int maxWaitSeconds = 60;
            //wait for a smartcard to appear
            while((smartcards == 0) && (waitSeconds <= maxWaitSeconds)){
                try{
                    Thread.sleep(2000);
                    waitSeconds += 2;
                    smartcards = this.cardStorage.getClosedSmartcards().size();
                    logger.fine("no. of smartcards: "+ smartcards + " - waited for : " + waitSeconds + " <= " + maxWaitSeconds);
                }catch(InterruptedException e){
                    logger.fine("Waiting for smartcard Thread got interrupted.");
                    if(smartcards == 1){
                        return this.checkPinAndAddSmartcardHelper(pin);
                    }else{
                        logger.fine("SmartcardPins: No cards after interrupt. Sending conflict ");
                        return Response.status(Status.CONFLICT).build();
                    }
                }
            }
            if(smartcards == 1){
                return this.checkPinAndAddSmartcardHelper(pin);
            }else{
                if(waitSeconds >= maxWaitSeconds) {
                    logger.fine("Timeout waiting for smartcard after # seconds : " + maxWaitSeconds);
                } else {
                    logger.fine("SmartcardPins: Strangely, the number of smartcards seem to now be: " + smartcards);
                }
                return Response.status(Status.CONFLICT).build();
            }
        }
        //        // ORIGINAL CODE !
        //
        //        boolean res = new SmartcardUnlocker().unlock(pinsStr, this.cardStorage);
        //        if (res) {
        //            return Response.ok().build();
        //        }
        //        return Response.status(Status.FORBIDDEN).build();
    }

    private Response checkPinAndAddSmartcardHelper(int pin){

        Smartcard sc = (Smartcard) this.cardStorage.getClosedSmartcards().get(0);
        SmartcardStatusCode code = sc.changePin(pin, pin);
        if(code == SmartcardStatusCode.UNAUTHORIZED){
            return Response.status(Status.UNAUTHORIZED).build();
        }else if(code == SmartcardStatusCode.FORBIDDEN){
            return Response.status(Status.FORBIDDEN).build();
        }
        System.out.println("checkPinAndAddSmartcardHelper : " + this.cardStorage.getSmartcards().size() );
        boolean added = this.cardStorage.addSmartcard(this.cardStorage.getClosedSmartcards().get(0), pin);
        if(added){
            this.cardStorage.getClosedSmartcards().remove(0);
        }
        this.storeCardStorageReference(); // smartcardAvailable = true;
        return Response.noContent().build();
    }

    @GET()
    @Path("/user/backupExists/{SessionID}")
    public Response backupExists(@PathParam ("SessionID") final String sessionID) throws Exception{
        for(URI uri : this.cardStorage.getSmartcards().keySet()){
            //We know there is only one card in this set
            Smartcard s = (Smartcard)this.cardStorage.getSmartcards().get(uri);
            int pin = this.cardStorage.getPin(uri);
            File f = new File(UserService.fileStoragePrefix+"smartcard_backup_"+s.getDeviceID(pin)+".bac");
            logger.fine("BackupExists? : " + f.exists());
            if(f.exists()){
                return Response.status(204).build();
            }else{
                return Response.ok().build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @POST()
    @Path("/user/backupSmartcard/{SessionID}")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response backupSmartcard(@PathParam ("SessionID") final String sessionId,
            final String password) throws Exception{
        logger.fine("backupSmartcard");
        if(Utils.passwordToByteArr(password) == null){
            //Password not valid
            System.err.println("Password not valid!");
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        if(!this.isSameCardStorageReference()) { // smartcardAvailable){
            System.err.println("Smartcard not found.");
            return Response.status(Status.NOT_FOUND).build();
        }
        if(this.cardStorage.getSmartcards().size() != 1){
            System.err.println("Too many smartcards found.");
            return Response.status(Status.CONFLICT).build();
        }

        try{
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                //We know there is only one card in this set
                Smartcard s = (Smartcard)this.cardStorage.getSmartcards().get(uri);
                int pin = this.cardStorage.getPin(uri);
                SmartcardBackup backup = s.backupAttendanceData(pin, password);
                if(backup == null){
                    return Response.status(Status.BAD_REQUEST).build();
                }
                File f = new File(UserService.fileStoragePrefix+"smartcard_backup_"+s.getDeviceID(pin)+".bac");
                backup.serialize(f);
            }
        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

    @POST()
    @Path("/user/restoreSmartcard/{SessionID}")
    @Consumes({MediaType.TEXT_PLAIN})
    public Response restoreSmartcard(@PathParam ("SessionID") final String sessionId,
            final String password) throws Exception{
        logger.fine("restoreSmartcard");
        if(Utils.passwordToByteArr(password) == null){
            //Password not valid
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }
        if(!this.isSameCardStorageReference()) { // smartcardAvailable){
            return Response.status(Status.NOT_FOUND).build();
        }
        if(this.cardStorage.getSmartcards().size() != 1){
            return Response.status(Status.CONFLICT).build();
        }
        URI scURI = null;
        try{
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                //We know there is only one card in this set
                Smartcard s = (Smartcard)this.cardStorage.getSmartcards().get(uri);
                int pin = this.cardStorage.getPin(uri);
                File f = new File(UserService.fileStoragePrefix+"smartcard_backup_"+s.getDeviceID(pin)+".bac");
                if(!f.exists()){
                    logger.fine("Backup file not found.. ");
                    return Response.status(Status.NOT_FOUND).build();
                }
                SmartcardBackup backup = SmartcardBackup.deserialize(f);
                SmartcardStatusCode code = s.restoreAttendanceData(pin, password, backup);
                if(code != SmartcardStatusCode.OK){
                    System.err.println("Restoration failed: " + code);
                    return Response.status(Status.BAD_REQUEST).build();
                }
                scURI = uri;
            }
        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        boolean removed = this.cardStorage.removeSmartcard(this.cardStorage.getSmartcards().get(scURI));
        if(removed)
        {
            this.storeCardStorageReference(); // smartcardAvailable = false;
        }
        return Response.ok().build();
    }

    @POST()
    @Path("/user/changePin")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response changePin(String pins) throws Exception{
        boolean sameCard = this.isSameCardStorageReference();
        Smartcard sc = null;
        boolean closedCard = false;
        if(sameCard){
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                sc = (Smartcard) this.cardStorage.getSmartcard(uri);
                break;
            }
        }else{
            sc = (Smartcard) this.cardStorage.getClosedSmartcards().get(0);
            closedCard = true;
        }
        String[] pinSplit = pins.split(" ");
        int oldPin_ = Integer.parseInt(pinSplit[0]);
        int newPin_ = Integer.parseInt(pinSplit[1]);
        SmartcardStatusCode code = sc.changePin(oldPin_, newPin_);
        // - save smartcard
        saveSoftwareSmartcard();
        if(code == SmartcardStatusCode.UNAUTHORIZED){
            return Response.status(401).build();
        }else if(code == SmartcardStatusCode.FORBIDDEN){
            return Response.status(402).build();
        }else{
        	if(closedCard){
        	    System.out.println("changePin - 1: " + this.cardStorage.getSmartcards().size() );
	        	boolean added = this.cardStorage.addSmartcard(sc, newPin_);
	            if(added){
	                this.cardStorage.getClosedSmartcards().remove(0);
	            }
	            this.storeCardStorageReference(); // smartcardAvailable = true;
        	}else{
        	    System.out.println("changePin 2 : " + this.cardStorage.getSmartcards().size() );
        		this.cardStorage.removeSmartcard(sc);
        		this.cardStorage.addSmartcard(sc, newPin_);        		
        	}        	
            return Response.ok().build();
        }
    }

    @POST()
    @Path("/user/unlockCard")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response unlockCard(String pukAndPin) throws Exception{
        boolean sameCard = this.isSameCardStorageReference();
        Smartcard sc = null;
        if(sameCard){
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                sc = (Smartcard) this.cardStorage.getSmartcard(uri);
            }
        }else{
            sc = (Smartcard) this.cardStorage.getClosedSmartcards().get(0);
        }
        String[] pinSplit = pukAndPin.split(" ");
        int puk = Integer.parseInt(pinSplit[0]);
        int pin = Integer.parseInt(pinSplit[1]);
        SmartcardStatusCode code = sc.resetPinWithPuk(puk, pin);
        // - save smartcard
        saveSoftwareSmartcard();
        if(code == SmartcardStatusCode.UNAUTHORIZED){
            //Wrong Puk
            return Response.status(401).build();
        }else if(code == SmartcardStatusCode.FORBIDDEN){
            //Wrong puk 10 times in a row.. card is dead.
            return Response.status(402).build();
        }else{
            return Response.ok().build();
        }
    }


//    private Response handleUserDataBlob(String storeValue) {
//
//        if(!this.isSameCardStorageReference()) { // smartcardAvailable){
//            System.err.println("Smartcard not found.");
//            return Response.status(Status.NOT_FOUND).build();
//        }
//        if(this.cardStorage.getSmartcards().size() != 1){
//            System.err.println("Too many smartcards found.");
//            return Response.status(Status.CONFLICT).build();
//        }
//
//        try{
//            Smartcard s = null;
//            int pin = -1;
//            for(URI uri : this.cardStorage.getSmartcards().keySet()){
//                //We know there is only one card in this set
//                s = (Smartcard)this.cardStorage.getSmartcards().get(uri);
//                pin = this.cardStorage.getPin(uri);
//                break;
//            }
//            if(s==null) {
//                System.err.println("Smartcards disappeared.");
//                return Response.status(Status.CONFLICT).build();
//            }
//
//            // hvordan vælger man uri
//            URI soderhamnDataStoreBlobURI = URI.create("urn:datablob");
//
//            //Set<URI> blobUris = s.getBlobUris(pin);
//            //logger.fine("blobUris " + blobUris);
//            // we now have card!
//            if(storeValue!=null) {
//                // we store
//                logger.fine("storeData - new value : [" + storeValue + "]");
//
//                // skal gammel blob slettes først ?
//                SmartcardBlob exits = s.getBlob(pin, soderhamnDataStoreBlobURI);
//                logger.fine("- blob exits " + exits);
//                SmartcardBlob replace = new SmartcardBlob();
//                replace.blob = storeValue.getBytes("UTF-8");
//                int maxAmountOfBytes = HardwareSmartcard.MAX_BLOB_BYTES;
//                if(replace.blob.length > maxAmountOfBytes){
//                	//We need to split the blob - rounding up 
//                	int amountOfBlobs = (replace.blob.length+maxAmountOfBytes-1) / maxAmountOfBytes;
//                	for(int i = 0; i < amountOfBlobs; i++){
//                		byte[] toStore = new byte[maxAmountOfBytes];
//                		int bytesLeft = replace.blob.length-(i*maxAmountOfBytes);
//                		int amountToCopy = (bytesLeft > maxAmountOfBytes) ? maxAmountOfBytes : bytesLeft; 
//                		System.arraycopy(replace.blob, i*maxAmountOfBytes, toStore, 0, amountToCopy);
//                		SmartcardBlob blobToStore = new SmartcardBlob();
//                		blobToStore.blob = toStore;
//                		URI uriToStoreUnder;
//                		if(i == 0){
//                			uriToStoreUnder = soderhamnDataStoreBlobURI;
//                		}else{
//                			uriToStoreUnder = URI.create(soderhamnDataStoreBlobURI.toString()+":"+i);
//                		}
//                		s.storeBlob(pin, uriToStoreUnder, blobToStore);
//                	}                	
//                }else{
//                	SmartcardStatusCode status = s.storeBlob(pin, soderhamnDataStoreBlobURI, replace);
//                	logger.fine("Status of storing!" + status + " : " + new String(replace.blob) + " : " + replace.getLength());
//                	int i = 1;
//                	while(status == SmartcardStatusCode.OK){
//                		status = s.deleteBlob(pin, URI.create(soderhamnDataStoreBlobURI.toString()+":"+i));                		
//                		logger.fine("(tried to) remove the intermediate blob: "+soderhamnDataStoreBlobURI.toString()+":"+i);
//                		i++;
//                	}                	
//                }
//                
//                //
//                this.saveSoftwareSmartcard();
//
//                //logger.fine("control : "+ s.getBlob(pin, soderhamnDataStoreBlobURI));
//                logger.fine("Done storing data");
//
//            } else {
//                // we load
//                logger.fine("loadData...");
//                
//                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//                SmartcardBlob blob = s.getBlob(pin, soderhamnDataStoreBlobURI);                
//                logger.fine("- blob : " + blob);
//                if(blob!=null) {
//                    logger.fine("- blob length : " + blob.getLength());
//                    logger.fine("- blob : " + new String(blob.blob));
//                    byteStream.write(blob.blob);
//                    int i = 1;
//                    SmartcardBlob tmpBlob = new SmartcardBlob();
//                    tmpBlob.blob = blob.blob;
//                    while(true){
//                    	if(tmpBlob.getLength() == HardwareSmartcard.MAX_BLOB_BYTES){
//                    		URI tmpBlobURI = URI.create(soderhamnDataStoreBlobURI.toString()+":"+i);
//                    		tmpBlob = s.getBlob(pin, tmpBlobURI);
//                    		if(tmpBlob == null){
//                    			break;
//                    		}
//                    		byteStream.write(tmpBlob.blob);
//                    		i++;
//                    	}else{
//                    		break;
//                    	}                    	
//                    }
//                }
//                
//                if((blob == null) || (blob.getLength()==0)) {
//                	logger.fine("Done loading data. - no data in blob");
//                    return Response.ok("").build();
//                } else {
//                	logger.fine("Done loading data. Result: "+new String(byteStream.toByteArray(), "UTF-8").trim());
//                    String dataStorageValue = new String(byteStream.toByteArray(), "UTF-8").trim();
//                    return Response.ok(dataStorageValue).build();
//                }
//
//            }
//            //
//
//        } catch(Exception e){
//            e.printStackTrace();
//            return Response.status(Status.BAD_REQUEST).build();
//        }
//        return Response.ok().build();
//
//    }
    
    /**
     * 
     */
    @POST()
    @Path("/user/storeData/{SessionID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response storeData(@PathParam ("SessionID") final String sessionId,
            final String value) throws Exception {
    	userServiceBusy = true;
    	// NO DATA SOTRING IN DEMO!
    	Response resp = Response.ok().build(); // this.handleUserDataBlob(value);
    	userServiceBusy = false;
        return resp;        
    }

    /**
     * 
     */
    @GET()
    @Path("/user/loadData/{SessionID}")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public Response loadData(@PathParam ("SessionID") final String sessionId) throws Exception {
    	userServiceBusy = true;
        // NO DATA SOTRING IN DEMO!
        Response resp = Response.ok().build(); // this.handleUserDataBlob(null);
        userServiceBusy = false;
        return resp;
    }



    @GET()
    @Path("/user/getCredentialDescriptionList/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getCredentialDescriptionList(@PathParam ("SessionID") final String sessionId) throws Exception {

        try {            
            ObjectFactory of = new ObjectFactory();

            logger.fine("getCredentialDescriptionList - " + sessionId);

            CredentialDescriptions credentialDescriptions = new CredentialDescriptions();
            List<URI> uriList = this.engine.listCredentials(UserService.USER_NAME);

            logger.fine(" # of credentials : " + (uriList == null ? " 'null' " : uriList.size()));

            for(URI uri: uriList) {
                logger.fine("- credential URI : " + uri);
                CredentialDescription cd = this.engine.getCredentialDescription(UserService.USER_NAME, uri);

                CredentialSpecification credSpec = UserHelper.getInstance().keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());
                // Add revocation status as attribute
                boolean isRevoked = credSpec.isRevocable() ? engine.isRevoked(UserService.USER_NAME, uri) : false;
                
                Attribute isRevokedAtt = of.createAttribute();
                AttributeDescription ad = of.createAttributeDescription();
                ad.setType(new URI("urn:abc4trust:gui:isRevoked"));
                ad.setEncoding(new URI("urn:abc4trust:1.0:encoding:boolean:unsigned"));
                ad.setDataType(new URI("xs:boolean"));
                
                isRevokedAtt.setAttributeDescription(ad);
                isRevokedAtt.setAttributeUID(new URI(cd.getCredentialUID().toString()+":gui:isRevoked"));
                isRevokedAtt.setAttributeValue(isRevoked);
                cd.getAttribute().add(isRevokedAtt);
                
                CredentialDescriptionsEntry entry = new CredentialDescriptionsEntry();
                entry.setKey(uri);
                entry.setValue(cd);
                credentialDescriptions.getEntry().add(entry);
            }

            // TODO : check if this is patras - and try to add counter - as a credential...
            //            try {
            //              logger.fine("JSON/XML for CredentialList : " + XmlUtils.toXml(this.of.createCredentialDescriptions(credentialDescriptions)));
            //            } catch(Exception e) {
            //              logger.fine("Failed logging JSON/XML for CredentialList : " + e);
            //            }

            return Response.ok(this.of.createCredentialDescriptions(credentialDescriptions)).build();
        } catch(Exception e) {
            logger.log(Level.SEVERE, " - failed", e);
            return Response.serverError().build();

        }
    }

    @GET()
    @Path("/user/getUiManageCredentialData/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getUiManageCredentialData(@PathParam ("SessionID") final String sessionId) throws Exception {

        try {
            logger.fine("getUiCredentialDescriptionList - " + sessionId);

            List<URI> uriList = this.engine.listCredentials(UserService.USER_NAME);

            logger.fine(" # of credentials : " + (uriList == null ? " 'null' " : uriList.size()));

            UiManageCredentialData uiManageCredentialData = new UiManageCredentialData();
            UiCommonArguments uiCredListInfo = uiManageCredentialData.data;
            
            for(URI uri: uriList) {
                logger.fine("- credential URI : " + uri);
                CredentialDescription cd = this.engine.getCredentialDescription(UserService.USER_NAME, uri);

                if(cd.getSecretReference()==null) {
                  System.out.println("- NO Secret Reference in CredentialDescription! - try to fix" + uri);
                  Credential c = UserHelper.getInstance().credentialManager.getCredential(UserService.USER_NAME, uri);
//                  c.getCredentialDescription().setSecretReference(SECRET_BASED_SMARTCARD_URI);
                  
                  UserHelper.getInstance().credentialManager.updateCredential(UserService.USER_NAME, c);

                  cd = c.getCredentialDescription();

                  
                  Credential c_constrol = UserHelper.getInstance().credentialManager.getCredential(UserService.USER_NAME, uri);
                  
                  System.out.println(" : " + c_constrol.getCredentialDescription().getSecretReference());
                } else {
                  System.out.println("- CredentialDescription has Secret Reference !! " + cd.getSecretReference());
                }
                
                CredentialSpecification credSpec = UserHelper.getInstance().keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());

                IssuerParameters ip = UserHelper.getInstance().keyManager.getIssuerParameters(cd.getIssuerParametersUID());

                IssuerInUi issInUi = new IssuerInUi(ip);
                uiCredListInfo.addIssuer(issInUi );
                
                boolean isRevoked = false;
                RevocationAuthorityParameters rap = null;
                if( credSpec.isRevocable() ) {
                    isRevoked = engine.isRevoked(UserService.USER_NAME, uri);
                    
                    URI revocationParametersUID = ip.getRevocationParametersUID();
                    rap = UserHelper.getInstance().keyManager.getRevocationAuthorityParameters(revocationParametersUID);

                    uiManageCredentialData.revokedCredentials.put(uri, isRevoked);
                }
                // set revoked - for UI...
                cd.setRevokedByIssuer(isRevoked);
                
                CredentialSpecInUi csInUi = new CredentialSpecInUi(credSpec);
                uiCredListInfo.addCredentialSpec(csInUi );

                CredentialInUi credInUi = new CredentialInUi(cd, ip , credSpec, rap);
                uiCredListInfo.addCredential(credInUi);

                logger.fine(" -- Added Credential : " + cd.getCredentialSpecificationUID() + " : " + ip.getVersion());
            }
            JAXBElement<UiManageCredentialData> jaxb = ObjectFactoryReturnTypes.wrap(uiManageCredentialData);

            return Response.ok(jaxb).build();
        } catch(Exception e) {
            System.err.println("FAILED : getUiCredentialDescriptionList - " + sessionId + " : " + e.getMessage());
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET()
    @Path("/user/getDebugInfo/{SessionID}")
    @Consumes({MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDebugInfo(@PathParam ("SessionID") final String sessionId) throws Exception {

        logger.fine("getDebugInfo - " + sessionId);
        try {
            StringBuilder sb = new StringBuilder(debugInfo);
            sb.append("\n");
            sb.append(logMemoryUsage());
            return Response.ok(sb.toString()).build();
        } catch(Exception e) {
            logger.log(Level.SEVERE, " - failed", e);
            return Response.serverError().build();

        }
    }

    @GET()
    @Path("/alive/isAlive")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isAlive() throws Exception {
        if(userServiceBusy){
        	return Response.status(201).build();
        }else{
        	return Response.ok().build();
        }
    }

    @GET()
    @Path("/info/deploymentVersionId")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deploymentVersionId() throws Exception {

        logger.fine("deploymentVersionId : " + deploymentVersionId);
        logger.fine("softwareCard used?: "+ softwareSmartcard);
        String toReturn = "";
        if(softwareSmartcard != null){
        	toReturn += "software ";
        }else{
        	toReturn += "hardware ";
        }
    	toReturn+=deploymentVersionId;
        return Response.ok(toReturn).build();
    }
    
    @GET()
    @Path("/info/userServiceVersionId")
    @Produces(MediaType.TEXT_PLAIN)
    public Response userServiceVersionId() throws Exception {

        logger.fine("userServiceVersionId : " + userServiceVersionId);
        logger.fine("softwareCard used?: "+ softwareSmartcard);
        String toReturn = "";
        if(softwareSmartcard != null){
        	toReturn += "software ";
        }else{
        	toReturn += "hardware ";
        }
    	toReturn+=userServiceVersionId;
        return Response.ok(toReturn).build();        
    }
    
    @GET()
    @Path("/debug/timingsOnOff")
    @Produces(MediaType.TEXT_PLAIN)
    public Response timingsOnOff() throws Exception {
    	//TODO: Make this actually change some static variable in abce-components.
    	boolean on = TimingsLogger.toogleLogger();
        logger.fine("timings are now on?: " + on);
        if(on){
        	return Response.ok("true").build();
        }else{
        	return Response.ok("false").build();
        }
    }
    
    @GET()
    @Path("/info/getAttendanceData")
    @Produces(MediaType.TEXT_PLAIN)
    public Response attendanceData() throws Exception {
    	if(!this.isSameCardStorageReference()) { // smartcardAvailable){
            System.err.println("Smartcard not found.");
            return Response.status(Status.NOT_FOUND).build();
        }
        if(this.cardStorage.getSmartcards().size() != 1){
            System.err.println("Too many smartcards found.");
            return Response.status(Status.CONFLICT).build();
        }

        try{
            Smartcard s = null;
            int pin = -1;
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                //We know there is only one card in this set
                s = (Smartcard)this.cardStorage.getSmartcards().get(uri);
                pin = this.cardStorage.getPin(uri);
                break;
            }
            if(s==null) {
                System.err.println("Smartcards disappeared.");
                return Response.status(Status.CONFLICT).build();
            }            
            //Ready to do work with the card
            int counterValue = -1;
            try{
            	counterValue = s.getCounterValue(pin, StaticUriToIDMap.courseIssuerUID);
            }catch(Exception e){
            	System.err.println("exception when getting counterValue. Sending -1 back. Exception was: ");
            	e.printStackTrace();
            }
            return Response.ok(""+counterValue).build();            
        }catch(Exception e){
        	logger.fine("getAttendanceData - failed");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
    
}

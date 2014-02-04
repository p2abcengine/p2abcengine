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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

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
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiManageCredentialData;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.returnTypes.ui.CredentialSpecInUi;
import eu.abc4trust.returnTypes.ui.IssuerInUi;
import eu.abc4trust.returnTypes.ui.UiCommonArguments;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcard.InsufficientStorageException;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBackup;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.smartcard.StaticUriToIDMap;
import eu.abc4trust.smartcard.Utils;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialDescriptions;
import eu.abc4trust.xml.CredentialDescriptionsEntry;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.util.XmlUtils;

@Path("/")
public class UserService {

    public static boolean touchThisBooleanToForceStaticInit = true;
    private static final boolean useSemaphore = false;
    private static boolean DEBUG = false;
    private static String deploymentVersionId = "N/A";
    public static String userServiceVersionId = "NOT RESOLVED YET";
    
    private final ObjectFactory of = new ObjectFactory();
    private UserAbcEngine engine;
    private final CardStorage cardStorage;


    // Leftovers
    static HashMap<String, PresentationToken> presentationTokens;
    static HashMap<String, IssuMsgOrCredDesc> issuanceMessages;
    static HashMap<String, IdentitySelectionUIWrapper> identitySelections;
    private static final Map<String, URI> contextMap = new HashMap<String, URI>();
    private static final Map<String, CryptoEngine> cryptoEngineMap = new HashMap<String, CryptoEngine>();
	private static boolean userServiceBusy = false;
    static IdentitySelectionUIWrapper currentIdentitySelections;

    @Context
    ServletContext context;


    private static String[] findResourcesInFolder(String folderName, final String filter) {
        System.out.println("Look for resources in folder : " + folderName);
        String[] resourceList;
        File folder = new File(folderName);
        File[] fileList = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.indexOf(filter) != -1) {
                    System.out.println("Test : " + arg1);
                    return true;
                } else {
                    return false;
                }
            }
        });

        resourceList = new String[fileList.length];
        for(int i=0; i<fileList.length; i++) {
            resourceList[i] = fileList[i].getAbsolutePath();
        }
        return resourceList;
    }

    String[] pilotRun_issuerParamsResourceList = null;
    String[] pilotRun_credSpecResourceList =  null;
    //    String pilotRun_filePrefix = "";
    private static String fileStoragePrefix = "";

    private static String softwareSmartcardResource = null;
    //    private static int softwareSmartcardPin = -1;
    private static SoftwareSmartcard softwareSmartcard = null;

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
        System.out.println("UserService static init ! - file.encoding : " + System.getProperty("file.encoding", null));
        addDebugInfo("UserService static init ! - file.encoding : " + System.getProperty("file.encoding", null));
        // specify behaviour in PolicyCredentialMatcher
        PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;

        try {
            addDebugInfo("Init User ABCE");
            initUserABCE();
            addDebugInfo("Init User ABCE DONE!");
        } catch (Exception e) {
            addDebugInfo("Could not init User ABCE", e);
            throw new IllegalStateException("Could not init User ABCE", e);
        }

        // check for software smartcards
        softwareSmartcardResource = System.getProperty("UserSoftwareSmartcard", null);
        //
        if((softwareSmartcardResource!=null) && (softwareSmartcard == null)) {
            System.out.println("Try to use SoftwareSmartcard : " + softwareSmartcardResource);
            try {
                softwareSmartcard = FileSystem.loadObjectFromResource(softwareSmartcardResource);
                addDebugInfo("Try to use SoftwareSmartcard : " + softwareSmartcardResource);
            } catch(Exception e) {
                addDebugInfo("UserSoftwareSmartcard could not be loaded from : " + softwareSmartcardResource, e);
                throw new IllegalStateException("UserSoftwareSmartcard could not be loaded from : " + softwareSmartcardResource, e);
            }

        } else {
            addDebugInfo("UserSerivce Initialize ABCE to use Hardware Smartcards");
            System.out.println("UserSerivce Initialize ABCE to use Hardware Smartcards");
        }
        logMemoryUsage();
        
        addDebugInfo("UserSerivce Version Numbers - deployment Id / userservice Id : " + deploymentVersionId + " / " + userServiceVersionId);
        System.out.println("UserSerivce Version Numbers - deployment Id / userservice Id : " + deploymentVersionId + " / " + userServiceVersionId);
        
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
      System.out.println(memUsage);
      return memUsage;
    }
    
    private void saveSoftwareSmartcard() {
        if(softwareSmartcard != null) {
            System.out.println("saveSoftwareSmartcard to resource " + softwareSmartcardResource);
            try {
              FileSystem.storeObjectInFile(softwareSmartcard, softwareSmartcardResource);
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
            SmartcardServletContext.cardStorageReference
            .getAndSet(this.cardStorage);
            SmartcardServletContext.startDetector();
            
            try{
            	Thread.sleep(500);
            }catch(Exception e){
            	//Do nothing other than note it happened, since we want to run anyways. 
            	System.out.println("Could not sleep - was interrupted!: "+e.getMessage());
            }
        }


    }

    private static void initUserABCE() throws Exception {
        String testcase = System.getProperty("testcase", null);
        if(testcase == null) {

            testcase = null; //"patras";
        }

        String testCaseFilePrefix;
        String installerEmbeddedResources = null;
        if("generic-installer".equals(testcase)) {
            // running from executable jar with embedded Jetty...
            testCaseFilePrefix = System.getProperty("UserServiceRunDir") +"/";
            installerEmbeddedResources = testCaseFilePrefix + "/resources";
            System.setProperty("PathToUProveExe", testCaseFilePrefix + "/uprove");
        } else if( testcase != null ) {
            // by convension look for resources in folder named 'testcase' + '_'
            testCaseFilePrefix = testcase + "_";
        } else {
            // run userservice with : -Dtestcase=patras / -Dtestcase=soderhamn
            throw new IllegalStateException("Unknown 'testcase' : " + testcase + "\n\nstart with : \n\nmvn jetty:run -Dtestcase=patras\nmvn jetty:run -Dtestcase=soderhamn");
        }

        fileStoragePrefix = testCaseFilePrefix + "user_storage/"; //  + pilotRun_filePrefix;
        System.out.println("fileStoragePrefix : " + fileStoragePrefix);

        if(UserHelper.isInit()) {
            System.out.println("UserService already init !");
        } else {
            System.out.println("UserService initiating !");
            if(System.getProperty("PathToUProveExe", null)==null){
                System.out.println("Set development path to UProve Exe");
                String uprovePath = "./../../../dotNet/releases/1.0.0";
                System.setProperty("PathToUProveExe", uprovePath);
            } else {
                System.out.println("PathToUProveExe - already defined!");
            }

            
            // if 'generic-install' resources are in a fixed place
            String issuerResources = installerEmbeddedResources != null ? installerEmbeddedResources : testCaseFilePrefix + "issuer_resources/";

            
            // load all - should only be 1
            String systemParamsResource = issuerResources + (issuerResources.endsWith("/") ? "" : "/") + "system_params";

            // load all issuer params in folder
            String[] issuerParamsResourceList = findResourcesInFolder(issuerResources, "issuer_params");

            // load all credspecs in folder
            String[] credSpecResourceList = findResourcesInFolder(issuerResources, "credentialSpecification");

            // load all inspector public keys in folder
            String[] inspectorPublicKeyResourceList = findResourcesInFolder(issuerResources, "inspector_publickey");

            String[] revocationAuthorityParametersResourceList = findResourcesInFolder(issuerResources, "revocation_authority");

            if (issuerParamsResourceList.length == 0) {
                throw new IllegalStateException(
                        "Did not find any issuer resources. Please look in: "
                                + issuerResources);
            }

            // patras or soderhamn
            if(issuerParamsResourceList[0].contains("patras")) {
                System.out.println("A6");
                System.out.println("Issuer Parameteres are for Patras Pilot");
                addDebugInfo("Issuer Parameteres are for Patras Pilot");
                StaticUriToIDMap.Patras = true;
            } else {
                System.out.println("A7");
                System.out.println("Issuer Parameteres are for Soderhamn Pilot");
                addDebugInfo("Issuer Parameteres are for Soderhamn Pilot");
                StaticUriToIDMap.Patras = false;
            }
            // wipe storage! on every startup
            // DONOT : deletes UPROVE TOKENS! UserHelper.WIPE_STOARAGE_FILES = true;

            System.out.println("issuer params : " + getResourceNames(issuerParamsResourceList));
            addDebugInfo("issuer params : " + getResourceNames(issuerParamsResourceList));
            for(String ipResource : issuerParamsResourceList){
                try {
                    IssuerParameters ip = FileSystem.loadObjectFromResource(ipResource);
                    System.out.println(" - ip : " + ip.getAlgorithmID() + " : " + ip.getVersion());
                    addDebugInfo(" - ip : " + ip.getAlgorithmID() + " : " + ip.getVersion());
                } catch (Exception e) {
                    System.out.println("Warning : IssuerParmameter resource seems to be illegal - " + ipResource + " : " + e);
                    addDebugInfo("Warning : IssuerParmameter resource seems to be illegal - " + ipResource, e);
                }
            }
            System.out.println("cred specs    : " + getResourceNames(credSpecResourceList));
            addDebugInfo("cred specs    : " + getResourceNames(credSpecResourceList));
            if((issuerParamsResourceList.length != credSpecResourceList.length) && ((issuerParamsResourceList.length / 2) != credSpecResourceList.length)) {
                System.out.println("Warning : Mismatch between number of IssuerParmameter and number of credspecs - " + issuerParamsResourceList.length + " (and / 2 ) != " + credSpecResourceList.length);
                addDebugInfo("Warning : Mismatch between number of IssuerParmameter and number of credspecs - " + issuerParamsResourceList.length + " (and / 2 ) != " + credSpecResourceList.length);
            }
            System.out.println("inspector keys    : " + getResourceNames(inspectorPublicKeyResourceList));
            addDebugInfo("inspector keys    : " + getResourceNames(inspectorPublicKeyResourceList));
            System.out.println("revauth params: "+getResourceNames(revocationAuthorityParametersResourceList));
            addDebugInfo("revauth params: "+getResourceNames(revocationAuthorityParametersResourceList));
            
            readDeploymentSpecificProperties();

            UserHelper.initInstance(systemParamsResource, issuerParamsResourceList, fileStoragePrefix, credSpecResourceList, inspectorPublicKeyResourceList, revocationAuthorityParametersResourceList);

            presentationTokens =  new HashMap<String, PresentationToken>();
            issuanceMessages =  new HashMap<String, IssuMsgOrCredDesc>();
            identitySelections = new HashMap<String, IdentitySelectionUIWrapper>();
        }
        System.out.println("UserService init ! DONE");
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
    
    private static void readDeploymentSpecificProperties() {
    	if(deploymentSpecificPropertiesInitialized) {
    		return;
    	}
		try {
			InputStream is = FileSystem.getInputStream("/deploymentspecific.properties");
			if(is==null) {
				// try from system classloader... no prepended
				is = ClassLoader.getSystemClassLoader().getResourceAsStream("deploymentspecific.properties");
			}
			if(is != null) {
				Properties props = new Properties();
				props.load(is);
				is.close();
				System.out.println("Found deployment specific properties : " + props);
				HardwareSmartcard.printInput = Boolean.parseBoolean(props.getProperty("allowPrintInputForHardwareSmartcard", "false"));
				
				System.out.println("Allow printing input for hardware smart card: "+HardwareSmartcard.printInput);
				
				DEBUG = Boolean.parseBoolean(props.getProperty("printDebugInfo", "false"));
				System.out.println("Allow debug printing: "+DEBUG);
				
				deploymentVersionId = props.getProperty("deploymentVersionId", "N/A");
			} else {
				System.out.println("No deployment specific properties.");
			}
		} catch(Exception e) {
			System.err.println("Failed to read DeploymentSpecificProperties in UserService..");
			e.printStackTrace();
		} finally {
			deploymentSpecificPropertiesInitialized = true;
		}
    }
    
    @GET()
    @Path("/user/getUiPresentationArguments/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response getUiPresentationArguments(@PathParam ("SessionID") final String sessionId) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        System.out.println("--- getUiPresentationArguments - session ID : " + sessionId + " - wrapper : " + isw);
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
//        System.out.println("- uiPresentationArguments : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(isw.getUiPresentationArguments()), false));
        System.out.println("- uiPresentationArguments : " + isw.getUiPresentationArguments());
//        System.out.println("- uiPresentationArguments : " + isw.getUiPresentationArguments().data.inspectors);
//        System.out.println("- uiPresentationArguments : " + isw.getUiPresentationArguments().data.issuers);
        return Response.ok(isw.getUiPresentationArguments()).build();
    }

    @POST()
    @Path("/user/setUiPresentationReturn/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response setUiPresentationReturn(@PathParam ("SessionID") final String sessionId, UiPresentationReturn uiPresentationReturn) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        System.out.println("--- setUiPresentationReturn - session ID : " + sessionId + " - wrapper : " + isw);
        System.out.println("- UiPresentationReturn : " + uiPresentationReturn);
        //      System.out.println("- UiPresentationReturn XML : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(uiPresentationReturn)));
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
        System.out.println("--- getUiIssuanceArguments - session ID : " + sessionId + " - wrapper : " + isw);
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
        System.out.println("- uiIssuanceArguments : " + isw.getUiIssuanceArguments());
        return Response.ok(isw.getUiIssuanceArguments()).build();
    }

    @POST()
    @Path("/user/setUiIssuanceReturn/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response setUiIssuanceReturn(@PathParam ("SessionID") final String sessionId, UiIssuanceReturn uiIssuanceReturn) throws Exception{

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        System.out.println("--- setUiIssuanceReturn - session ID : " + sessionId + " - wrapper : " + isw);
        System.out.println("- uiIssuanceReturn - session ID : " + uiIssuanceReturn);
        //      System.out.println("- uiIssuanceReturn XML : " + XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(uiIssuanceReturn)));
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
            final PresentationPolicyAlternatives presentationPolicy) {
    	if(presentationCallsCounter > 0){
    		System.out.println("WARNING: CreatePresentationToken called again before the last one finished. presentationCallsCounter="+presentationCallsCounter);
    	}
    	presentationCallsCounter++;
    	if(useSemaphore){
    		try {
				createPresentationTokenSemaphore.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException("Could not aquire semaphore lock - was interrupted", e);
			}
    	}
    	
        System.out.println("--- createPresentationToken - session ID : " + sessionId);
        logMemoryUsage();

        try{
        	System.out.println("-- -- " + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(presentationPolicy)));

            if(!this.engine.canBeSatisfied(presentationPolicy)){
                System.out.println("cannot satisfy policy, halting!");
                finishPresentationCount();
                return Response.status(422).build();
            }
            System.out.println("-- -- policy can be satisfied!");
        }catch(Exception e){
            System.out.println("engine.canBeSatisfied threw an exception:");
            e.printStackTrace();
            finishPresentationCount();
            return Response.status(422).build();
        }catch(Throwable t) {
            System.out.println("internal error calling : engine.canBeSatisfied");
            t.printStackTrace();
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
                    presentationTokens.put(sessionId,UserService.this.engine.createPresentationToken(presentationPolicy, isw));
                } catch(Exception e){
                    System.out.println("internal err");
                    e.printStackTrace();
                    //TODO something to store the exception in isw to allow for error handling
                } finally {
                    // set done!
                    isw.done = true;
                }
            }
        });

        System.out.println("--- createpresentationToken starting thread and going to sleep");
        userServiceBusy = true;
        thread.start();

        try {
            while((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            System.out.println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {
            	finishPresentationCount();
            	userServiceBusy = false;
                return Response.status(500).build();
            }
        }
        System.out.println("### --- createpresentationToken woke up : " + sessionId + " : "+presentationTokens.get(sessionId)+" "+isw.hasPresentationChoices()+" "+isw.done);
        if(isw.done || (presentationTokens.get(sessionId)!=null)) {
            System.out.println("### --- createPresentationToken finished without need for user interaction "  + isw.done + " : " + presentationTokens.get(sessionId));
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
            System.out.println("### --- createPresentationToken has choices for ui selection");
            finishPresentationCount();
            userServiceBusy = false;
            return Response.status(203).entity("GO AHEAD CALL NEW UI FOR PRESENTATION").type(MediaType.TEXT_PLAIN).build();
        }
        System.out.println("### --- createpresentaitontoken - this will never be reached");
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

        System.out.println("-- createPresentationToken - got IdentitySelection - for Session ID : " + sessionId + " - JSon choice : [" + choice + "]");

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            System.out.println("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        System.out.println("- isw " + isw);
        System.out.println("- isw " + isw.hasPresentationChoices());
        System.out.println("- isw " + isw.getUiPresentationReturn());
        System.out.println("- isw done ? " + isw.done);

        // if null - user cancelled / closed window. This was not detected by UI - so we notify by setting 'null' UIIssuanceReturn!
        if(isw.getUiPresentationReturn()==null) {
            System.out.println("CreatePresentationToken called but ID Selection not set ? User has cancelled/closed windows... sessionID: "+sessionId+", ABORTING");
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
                System.out.println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            System.out.println("- interrupted ERROR 500");
            userServiceBusy = false;
            return Response.status(500).build();
        }
        userServiceBusy = false;
        //
        PresentationToken pt = presentationTokens.remove(sessionId);
        identitySelections.remove(sessionId);
        if(pt == null){
            System.out.println("- No PresentationToken : ERROR 422");

            System.out.println("Unknown PresentationToken for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        // !! saveSoftwareSmartcard
        this.saveSoftwareSmartcard();
        
        JAXBElement<PresentationToken> ptJaxB = this.of.createPresentationToken(pt);
//        String ptXml = XmlUtils.toXml(ptJaxB);
//        System.out.println("- PresentationToken XML : " + ptXml);
        System.out.println("- PresentationToken - A OK - return http : 200");

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
            final IssuanceMessage mess) throws Exception {

        System.out.println("-- issuanceProtocolStep: "+sessionId);
        if(DEBUG){
        	System.out.println("-- issuanceMessage - incoming : "+XmlUtils.toXml(this.of.createIssuanceMessage(mess), false));
        }
        logMemoryUsage();
        
        if((startIssuanceUrl != null) && (stepIssuanceUrl != null)){
            startIssuanceUrl = URLDecoder.decode(startIssuanceUrl, "UTF-8");
            stepIssuanceUrl = URLDecoder.decode(stepIssuanceUrl, "UTF-8");
        }
        if(DEBUG){
        	System.out.println("-- issuanceProtocolStep - startRequest: "+startIssuanceUrl);
        	System.out.println("-- issuanceProtocolStep - stepRequest: "+stepIssuanceUrl);
        }

        CryptoEngine cryptoEngine = cryptoEngineMap.get(sessionId);
        if(cryptoEngine == null){
            IssuancePolicy ip = null;
            try{ip = (IssuancePolicy) XmlUtils.unwrap(mess.getAny(), IssuancePolicy.class);}
            catch(Exception e){
                try{ip = (IssuancePolicy) mess.getAny().get(1);}
                catch(Exception ex){
                    System.err.println("WARNING: Neither unwrapping worked!");
                }
            }
            if(ip != null){
                if(this.engine.listCredentials().size() > 6){
        	        System.out.println("Cannot issue the 8'th credential - technical problems. Tell user to check revoked status");
        	        return Response.status(501).build();
                }
            	
                contextMap.put(sessionId, mess.getContext()); //for maybe calling some reload token code depending on cryptoEngine.
                System.out.println("Mapping sessionId to this context: " + mess.getContext());
                if(ip.getCredentialTemplate().getIssuerParametersUID().toString().endsWith("uprove")){
                    System.out.println("From issuance message, it is assumed that we are working with UProve.");
                    cryptoEngineMap.put(sessionId, CryptoEngine.UPROVE);
                }else if(ip.getCredentialTemplate().getIssuerParametersUID().toString().endsWith("idemix")){
                    System.out.println("From issuance message, it is assumed that we are working with Idemix.");
                    cryptoEngineMap.put(sessionId, CryptoEngine.IDEMIX);
                }else{
                    System.err.println("Warning: issuer parameter contains no known cryptoEngine!");
                }
            }else{
                System.err.println("Warning: Issuance Policy was null - this should not be the case!");
            }
        }

        if(identitySelections.get(sessionId)!= null) {
            System.out.println("-- Session identifier is already used");
            return Response.status(900).build();
        }
        final IdentitySelectionUIWrapper isw = new IdentitySelectionUIWrapper();



        identitySelections.put(sessionId, isw);

        Thread thread = new Thread(new Runnable(){
            public void run(){
                try {
                    System.out.println("Starting Thread for IssanceProtocol Selection");
                    @SuppressWarnings("deprecation")
					IssuMsgOrCredDesc imOrDesc = UserService.this.engine.issuanceProtocolStep(mess, isw);
                    System.out.println("UserABCE Creaded IssuanceMessage : " + imOrDesc);
                    issuanceMessages.put(sessionId,imOrDesc); //add to include IdentitySelectionWrapper
                    System.out.println("Stored IssuanceMessage for session : " + sessionId + " : " + imOrDesc);
                } catch(Exception e) {
                    System.out.println("internal err (Exception)");
                    e.printStackTrace();
                    isw.setException(e);
                    //put e in isw to allow for error handling
                } catch(Throwable e) {
                    System.out.println("internal err (Throwable)");
                    e.printStackTrace();                    
                    //put e in isw to allow for error handling
                } finally {
                    // set done!
                    isw.done = true;
                }
            }
        });
        System.out.println("-- issuanceProtooclStep: starting thread and going to sleep");
        userServiceBusy = true;
        thread.start();
        try {
            while((issuanceMessages.get(sessionId)==null) && !isw.hasIssuanceChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            System.out.println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((issuanceMessages.get(sessionId)==null) &&!isw.hasIssuanceChoices() && !isw.done) {
            	userServiceBusy = false;
                return Response.status(500).build();
            }
        }
        System.out.println("-- issuanceProtooclStep: waking up: "+issuanceMessages.get(sessionId)+ "- "+isw.hasIssuanceChoices()+" - "+isw.done);
        System.out.println("-- done waiting for wrapper!");
        if(isw.done || (issuanceMessages.get(sessionId)!=null)) {
            System.out.println("-- wrapper is done! ");
            identitySelections.remove(sessionId);
            IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
            if (userIm == null){
                // this is an error case!
                System.out.println("-- Error running Issuance Protocol!");
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
//                        System.out.println("=====================\n\n Adding reload token info - ProtocolStep! \n\n=======================");
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
            		System.out.println("-- issuanceMessage - send back to issuer (no select) : "+XmlUtils.toXml(this.of.createIssuanceMessage(mess), false));
            	}else{
            		System.out.println("-- issuanceMessage - send back to issuer (no select) : "+mess);
            	}

                return Response.ok(this.of.createIssuanceMessage(userIm.im)).type(MediaType.TEXT_XML).build();
            }
        }else {
            System.out.println("-- wrapper has choices! ");
            userServiceBusy = false;
            return Response.status(203).entity("GO AHEAD CALL NEW UI FOR ISSUANCE").type(MediaType.TEXT_PLAIN).build();
        }

    }


    //fun stuff part!
    @POST()
    @Path("/user/issuanceProtocolStepSelect/{SessionID}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_XML})
    public Response issuanceProtocolStep(@PathParam ("SessionID") final String sessionId,
            @QueryParam("startRequest") String startIssuanceUrl,
            @QueryParam("stepRequest") String stepIssuanceUrl,
            final String choice) throws Exception {

        System.out.println("issuanceProtocolStepSelect : "+sessionId + " - JSon choice : [" + choice + "]");

        if((startIssuanceUrl != null) && (stepIssuanceUrl != null)){
            startIssuanceUrl = URLDecoder.decode(startIssuanceUrl, "UTF-8");
            stepIssuanceUrl = URLDecoder.decode(stepIssuanceUrl, "UTF-8");
        }
        if(DEBUG){
	        System.out.println("-- issuanceProtocolStepSelect - startRequest: "+startIssuanceUrl);
	        System.out.println("-- issuanceProtocolStepSelect - stepRequest: "+stepIssuanceUrl);
        }

        IdentitySelectionUIWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            System.out.println("Unknown IdentitySelectionWrapper(1) for sessionID: "+sessionId+", ABORTING");
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
                System.out.println("Waiting for ISW to finish! " + isw.done);                
                Thread.sleep(200);
            }
        } catch(InterruptedException e){
            if(!isw.done) {
                System.out.println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            userServiceBusy = false;
            return Response.status(500).build();
        }
        userServiceBusy = false;
        IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
        identitySelections.remove(sessionId);
        if(userIm == null){
            System.out.println("Unknown IdentitySelectionWrapper(2) for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        if (userIm.cd != null){ //The ABC Engine returned a credential description, so the protocol is done
// TODO : NOT IMPLEMENTED FOR PATRAS
//            //Save information for reloading tokens if we are running UProve
//            if(cryptoEngineMap.get(sessionId) == CryptoEngine.UPROVE){
//                if(contextMap.get(sessionId) != null){
//                    System.out.println("=====================\n Adding reload token info - ProtocolStepSelect! \n=======================");
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
        		System.out.println("-- issuanceMessage - send back to issuer (After select) : "+XmlUtils.toXml(this.of.createIssuanceMessage(userIm.im), false));
        	}else{
        		System.out.println("-- issuanceMessage - send back to issuer (After select) : "+userIm.im);
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

        System.out.println("updateNonRevocationEvidence");

        try {
        	userServiceBusy = true;
            this.engine.updateNonRevocationEvidence();
            userServiceBusy = false;
            System.out.println(" - updateNonRevocationEvidence Done");
            return Response.ok().build();
        } catch (Exception e) {
            System.out.println(" - updateNonRevocationEvidence Failed");
            e.printStackTrace();
            return Response.serverError().build();
        }

    }
    
    @POST
    @Path("/user/checkRevocationStatus")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response checkRevocationStatus(){
    	System.out.println("checkRevocationStatus");
    	
    	try{
    		userServiceBusy = true;
	        for (URI credUri : this.engine.listCredentials()) {
	            if (this.engine.isRevoked(credUri)) {
	                System.out.println("Deleting revoked credential: " + credUri);
	                this.engine.deleteCredential(credUri);
	                System.out.println("Deleted revoked credential: " + credUri);
	            } else {
	                System.out.println("Credential OK: " + credUri);
	            }
	        }
	        userServiceBusy = false;
	    	System.out.println("checkRevocationStatus - done");
	    	return Response.ok().build();
    	}catch(Exception e){
	    	System.out.println("checkRevocationStatus failed : " + e);
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

        System.out.println("listCredentials");

        try {
            // List<URI> resp = engine.listCredentials();
            List<URI> resp = new ArrayList<URI>();
            resp.add(new URI("http://asdf.gh/jkl"));

            System.out.println(" - resp " + resp);
            StringBuilder sb = new StringBuilder();
            if (resp != null) {
                for (URI uri : resp) {
                    sb.append(uri);
                    sb.append("\n");
                }
            }
            return Response.ok(sb.toString()).build();
        } catch (Exception e) {
            System.out.println(" - failed");
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

        System.out.println("getCredentialDescription : " + creduid);

        URI uri;
        try {
            uri = new URI(creduid);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            CredentialDescription resp = this.engine.getCredentialDescription(uri);

            return Response.ok(of.createCredentialDescription(resp)).build();
        } catch (Exception e) {
            System.out.println(" - failed");
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

        System.out.println("getCredentialDescription : " + creduid);

        URI uri;
        try {
            uri = new URI(creduid);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            boolean result = this.engine.deleteCredential(uri);
            System.out.println(" - call ok - deleted : " + result);
            if (result) {
                return Response.ok().build();
            } else {
                return Response.status(Status.NO_CONTENT).build();
            }
        } catch (Exception e) {
            System.out.println(" - failed");
            e.printStackTrace();
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
        System.out.println("checkSmartcard - available and same card ? : " + sameCard); // smartcardAvailable);
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
        System.out.println("isSameSmartcard - available and same card ? : " + sameCard); // smartcardAvailable);
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
            System.out.println("we have closed cards ?? : " + this.cardStorage.getClosedSmartcards());
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
            System.out.println("- still same card! " + lastCardReference);
            return true;
        } else {
            System.out.println("- card updated!");
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
        System.out.println("unlockSmartcards - called with: " + sessionId +" and pinstr: " + "xxxx" ); // pinsStr);
        //
        if((softwareSmartcard!=null) && (this.cardStorage.getSmartcards().size() == 0)) {
            // TODO :
            System.out.println("unlockSmartcards - add software smartcard : " + softwareSmartcard + " : " + this.cardStorage.getSmartcards());
            boolean status = this.cardStorage.addSmartcard(softwareSmartcard, Integer.parseInt(pinsStr));

            System.out.println("Unlock - add card status : " + status + " - current smartcars : " + this.cardStorage.getSmartcards() + " - closed cards : " + this.cardStorage.getClosedSmartcards() );
            if(!status) {
                System.out.println("Unlock - software smartcard could not be unlocked...");
                return Response.status(Status.UNAUTHORIZED).build();
            }

            // save card
            this.storeCardStorageReference();

            return Response.noContent().build();
        }

        if(this.cardStorage.getSmartcards().size() == 1){
            for(URI uri : this.cardStorage.getSmartcards().keySet()){
                try {
                    this.cardStorage.getSmartcards().get(uri).getDeviceID(Integer.parseInt(pinsStr));
                } catch(IllegalStateException e) {
                    // card has been removed!
                    System.out.println("Card has been removed ?? " + e);
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
        System.out.println("Smartcardpins. Size of closed smartcards: "+smartcards);
        if(smartcards == 1){
            return this.checkPinAndAddSmartcardHelper(pin);
        }else if(smartcards > 1){
            System.out.println("More than one smartcard was found. Aborting!");
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
                    System.out.println("no. of smartcards: "+ smartcards + " - waited for : " + waitSeconds + " <= " + maxWaitSeconds);
                }catch(InterruptedException e){
                    System.out.println("Waiting for smartcard Thread got interrupted.");
                    if(smartcards == 1){
                        return this.checkPinAndAddSmartcardHelper(pin);
                    }else{
                        System.out.println("SmartcardPins: No cards after interrupt. Sending conflict ");
                        return Response.status(Status.CONFLICT).build();
                    }
                }
            }
            if(smartcards == 1){
                return this.checkPinAndAddSmartcardHelper(pin);
            }else{
                if(waitSeconds >= maxWaitSeconds) {
                    System.out.println("Timeout waiting for smartcard after # seconds : " + maxWaitSeconds);
                } else {
                    System.out.println("SmartcardPins: Strangely, the number of smartcards seem to now be: " + smartcards);
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
            System.out.println("BackupExists? : " + f.exists());
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
        System.out.println("backupSmartcard");
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
        System.out.println("restoreSmartcard");
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
                    System.out.println("Backup file not found.. ");
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
	        	boolean added = this.cardStorage.addSmartcard(sc, newPin_);
	            if(added){
	                this.cardStorage.getClosedSmartcards().remove(0);
	            }
	            this.storeCardStorageReference(); // smartcardAvailable = true;
        	}else{
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


    private Response handleUserDataBlob(String storeValue) {

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

            // hvordan vælger man uri
            URI soderhamnDataStoreBlobURI = URI.create("urn:datablob");

            //Set<URI> blobUris = s.getBlobUris(pin);
            //System.out.println("blobUris " + blobUris);
            // we now have card!
            if(storeValue!=null) {
                // we store
                System.out.println("storeData - new value : [" + storeValue + "]");

                // skal gammel blob slettes først ?
                SmartcardBlob exits = s.getBlob(pin, soderhamnDataStoreBlobURI);
                System.out.println("- blob exits " + exits);
                SmartcardBlob replace = new SmartcardBlob();
                replace.blob = storeValue.getBytes("UTF-8");
                int maxAmountOfBytes = HardwareSmartcard.MAX_BLOB_BYTES;
                if(replace.blob.length > maxAmountOfBytes){
                	//We need to split the blob - rounding up 
                	int amountOfBlobs = (replace.blob.length+maxAmountOfBytes-1) / maxAmountOfBytes;
                	for(int i = 0; i < amountOfBlobs; i++){
                		byte[] toStore = new byte[maxAmountOfBytes];
                		int bytesLeft = replace.blob.length-(i*maxAmountOfBytes);
                		int amountToCopy = (bytesLeft > maxAmountOfBytes) ? maxAmountOfBytes : bytesLeft; 
                		System.arraycopy(replace.blob, i*maxAmountOfBytes, toStore, 0, amountToCopy);
                		SmartcardBlob blobToStore = new SmartcardBlob();
                		blobToStore.blob = toStore;
                		URI uriToStoreUnder;
                		if(i == 0){
                			uriToStoreUnder = soderhamnDataStoreBlobURI;
                		}else{
                			uriToStoreUnder = URI.create(soderhamnDataStoreBlobURI.toString()+":"+i);
                		}
                		s.storeBlob(pin, uriToStoreUnder, blobToStore);
                	}                	
                }else{
                	SmartcardStatusCode status = s.storeBlob(pin, soderhamnDataStoreBlobURI, replace);
                	System.out.println("Status of storing!" + status + " : " + new String(replace.blob) + " : " + replace.getLength());
                	int i = 1;
                	while(status == SmartcardStatusCode.OK){
                		status = s.deleteBlob(pin, URI.create(soderhamnDataStoreBlobURI.toString()+":"+i));                		
                		System.out.println("(tried to) remove the intermediate blob: "+soderhamnDataStoreBlobURI.toString()+":"+i);
                		i++;
                	}                	
                }
                
                //
                this.saveSoftwareSmartcard();

                //System.out.println("control : "+ s.getBlob(pin, soderhamnDataStoreBlobURI));
                System.out.println("Done storing data");

            } else {
                // we load
                System.out.println("loadData...");
                
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                SmartcardBlob blob = s.getBlob(pin, soderhamnDataStoreBlobURI);                
                System.out.println("- blob : " + blob);
                if(blob!=null) {
                    System.out.println("- blob length : " + blob.getLength());
                    System.out.println("- blob : " + new String(blob.blob));
                    byteStream.write(blob.blob);
                    int i = 1;
                    SmartcardBlob tmpBlob = new SmartcardBlob();
                    tmpBlob.blob = blob.blob;
                    while(true){
                    	if(tmpBlob.getLength() == HardwareSmartcard.MAX_BLOB_BYTES){
                    		URI tmpBlobURI = URI.create(soderhamnDataStoreBlobURI.toString()+":"+i);
                    		tmpBlob = s.getBlob(pin, tmpBlobURI);
                    		if(tmpBlob == null){
                    			break;
                    		}
                    		byteStream.write(tmpBlob.blob);
                    		i++;
                    	}else{
                    		break;
                    	}                    	
                    }
                }
                
                if((blob == null) || (blob.getLength()==0)) {
                	System.out.println("Done loading data. - no data in blob");
                    return Response.ok("").build();
                } else {
                	System.out.println("Done loading data. Result: "+new String(byteStream.toByteArray(), "UTF-8").trim());
                    String dataStorageValue = new String(byteStream.toByteArray(), "UTF-8").trim();
                    return Response.ok(dataStorageValue).build();
                }

            }
            //

        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Status.BAD_REQUEST).build();
        }
        return Response.ok().build();

    }
    /**
     * 
     */
    @POST()
    @Path("/user/storeData/{SessionID}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response storeData(@PathParam ("SessionID") final String sessionId,
            final String value) throws Exception {
    	userServiceBusy = true;
    	Response resp = this.handleUserDataBlob(value);
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
        Response resp = this.handleUserDataBlob(null);
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

            System.out.println("getCredentialDescriptionList - " + sessionId);

            CredentialDescriptions credentialDescriptions = new CredentialDescriptions();
            List<URI> uriList = this.engine.listCredentials();

            System.out.println(" # of credentials : " + (uriList == null ? " 'null' " : uriList.size()));

            for(URI uri: uriList) {
                System.out.println("- credential URI : " + uri);
                CredentialDescription cd = this.engine.getCredentialDescription(uri);

                CredentialSpecification credSpec = UserHelper.getInstance().keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());
                // Add revocation status as attribute
                boolean isRevoked = credSpec.isRevocable() ? engine.isRevoked(uri) : false;
                
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
            //              System.out.println("JSON/XML for CredentialList : " + XmlUtils.toXml(this.of.createCredentialDescriptions(credentialDescriptions)));
            //            } catch(Exception e) {
            //              System.out.println("Failed logging JSON/XML for CredentialList : " + e);
            //            }

            return Response.ok(this.of.createCredentialDescriptions(credentialDescriptions)).build();
        } catch(Exception e) {
            System.out.println(" - failed");
            e.printStackTrace();
            return Response.serverError().build();

        }
    }

    @GET()
    @Path("/user/getUiManageCredentialData/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response getUiManageCredentialData(@PathParam ("SessionID") final String sessionId) throws Exception {

        try {
            System.out.println("getUiCredentialDescriptionList - " + sessionId);

            List<URI> uriList = this.engine.listCredentials();

            System.out.println(" # of credentials : " + (uriList == null ? " 'null' " : uriList.size()));

            UiManageCredentialData uiManageCredentialData = new UiManageCredentialData();
            UiCommonArguments uiCredListInfo = uiManageCredentialData.data;
            
            for(URI uri: uriList) {
                System.out.println("- credential URI : " + uri);
                CredentialDescription cd = this.engine.getCredentialDescription(uri);

                CredentialSpecification credSpec = UserHelper.getInstance().keyManager.getCredentialSpecification(cd.getCredentialSpecificationUID());

                IssuerParameters ip = UserHelper.getInstance().keyManager.getIssuerParameters(cd.getIssuerParametersUID());

                IssuerInUi issInUi = new IssuerInUi(ip);
                uiCredListInfo.addIssuer(issInUi );
                
                boolean isRevoked = false;
                RevocationAuthorityParameters rap = null;
                if( credSpec.isRevocable() ) {
                    isRevoked = engine.isRevoked(uri);
                    
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

                System.out.println(" -- Added Credential : " + cd.getCredentialSpecificationUID() + " : " + ip.getVersion());
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

        System.out.println("getDebugInfo - " + sessionId);
        try {
            StringBuilder sb = new StringBuilder(debugInfo);
            sb.append("\n");
            sb.append(logMemoryUsage());
            return Response.ok(sb.toString()).build();
        } catch(Exception e) {
            System.out.println(" - failed");
            e.printStackTrace();
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

        System.out.println("deploymentVersionId : " + deploymentVersionId);
        System.out.println("softwareCard used?: "+ softwareSmartcard);
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

        System.out.println("userServiceVersionId : " + userServiceVersionId);
        System.out.println("softwareCard used?: "+ softwareSmartcard);
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
        System.out.println("timings are now on?: " + on);
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
        	System.out.println("getAttendanceData - failed");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
    
}

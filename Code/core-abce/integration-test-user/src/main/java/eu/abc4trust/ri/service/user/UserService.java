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

package eu.abc4trust.ri.service.user;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;

import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.zurich.idmx.dm.StoredDomainPseudonym;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;
import eu.abc4trust.ri.servicehelper.user.JSonIdentitySelection;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.ri.ui.idSelection.IdentitySelectionWrapper;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.Metadata;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SelectPresentationTokenDescription;
import eu.abc4trust.xml.util.XmlUtils;



@Deprecated
@Path("/")
public class UserService {

    // private UserAbcEngine engine;
    private final ObjectFactory of = new ObjectFactory();

    // For handling idSelection
    static HashMap<String, PresentationToken> presentationTokens;
    static HashMap<String, IssuMsgOrCredDesc> issuanceMessages;
    static HashMap<String, IdentitySelectionWrapper> identitySelections;


    public UserService() throws Exception {
        System.out.println("UserService ()!");
    }

    private static CryptoEngine clientEngine = null;
    public void initUserHelper(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String user) throws Exception {
        System.out.println("initUserHelper ! - cryptoEngine : " + cryptoEngine + " - clientEngine : " + clientEngine + " - run with user : " + user );

        //        if(cryptoEngine != CryptoEngine.IDEMIX || clientEngine != CryptoEngine.IDEMIX) {
        //          throw new IllegalStateException("Only IDEMIX SUPPORTED IN THES TEST FOR NOW!!!");
        //        }
        Logger.getLogger("eu.abc4trust").setLevel(Level.WARNING);
        UserHelper.resetInstance();
        // String user = "alice";
        //        String user = "stewart";
        //        String user = "hotel_alice";
        //        String user = "hotel_stewart";

        File folder;
        String uprovePath;
        String fileStoragePrefix;
        if( new File("target").exists()) {
            fileStoragePrefix = "target/" + user + "_";
            folder = new File("target");
            uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
        } else {
            fileStoragePrefix = "integration-test-user/target/" + user + "_";
            folder = new File("integration-test-user/target");
            uprovePath = "./../uprove/UProveWSDLService/ABC4Trust-UProve/bin/Release";
        }
        if(System.getProperty("PathToUProveExe",null) == null) {
            System.setProperty("PathToUProveExe", uprovePath);
        }

        presentationTokens =  new HashMap<String, PresentationToken>();
        issuanceMessages =  new HashMap<String, IssuMsgOrCredDesc>();
        identitySelections = new HashMap<String, IdentitySelectionWrapper>();


        System.out.println("UserService initialised !");
        String[] credSpecResourceList = {
                //                                          "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcard.xml",
                //                                          "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcardAmex.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcardAmex.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasCourse.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasUniversity.xml",
                //                    "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml",
                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
                "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
                "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml",
                "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
                "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"
        };

        //        File systemParamsFile = new File(folder, "issuer_system_params");
        //        String systemParamsResource = systemParamsFile.getAbsolutePath();
        File[] issuerParamsFileList = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.startsWith("issuer_issuer_params_")) {
                    //                    System.out.println("Test : " + arg1);
                    return true;
                } else {
                    return false;
                }
            }
        });
        System.out.println("issuerparams files : " + issuerParamsFileList + " : "
                + issuerParamsFileList.length);
        String[] issuerParamsResourceList = new String[issuerParamsFileList.length];

        for (int ix = 0; ix < issuerParamsFileList.length; ix++) {
            //            System.out.println(" - " + issuerParamsFileList[ix].getAbsolutePath());
            issuerParamsResourceList[ix] = issuerParamsFileList[ix].getAbsolutePath();
        }

        String[] inspectorPublicKeyResourceList = new String[0];

        // should be able to handle 'Bridged' in suer
        UserHelper.initInstance(cryptoEngine, issuerParamsResourceList, fileStoragePrefix, credSpecResourceList, inspectorPublicKeyResourceList);

        // TODO : verify this... why is systemparams not read from issuerparams
        Secret secret;
        if(clientEngine == CryptoEngine.IDEMIX) {
            System.out.println("- client engine is IDEMIX - load secret !");
            InputStream is = FileSystem
                    .getInputStream(
                    "/eu/abc4trust/sampleXml/smartcard/sampleSecret.xml");
            System.out.println("IS : " + is);

            secret = (Secret) XmlUtils.getObjectFromXML(is, true);
        } else if(clientEngine == CryptoEngine.UPROVE) {
            System.out.println("- client engine is UPROVE - load secret !");
            InputStream is = FileSystem
                    .getInputStream(
                    "/eu/abc4trust/sampleXml/patras/uprove-secret.xml");
            System.out.println("IS : " + is);
            secret = (Secret) XmlUtils.getObjectFromXML(is,true);

        } else {
            throw new IllegalStateException("CryptoEngine not supported : " + cryptoEngine);
        }

        URI secretUid = secret.getSecretDescription().getSecretUID();
        System.out.println("System - adding smart card secret : "
                + secret.getSecretDescription().getSecretUID());

        try {
            @SuppressWarnings("unused")
            Secret exists_secret = UserHelper.getInstance().credentialManager
            .getSecret(secretUid);
            System.out.println("Secret Already Exists!! " + secretUid);
        } catch (SecretNotInStorageException e) {
            System.out.println("Secret Not In Storage!");

            UserHelper.getInstance().credentialManager.storeSecret(secret);
        }
        // Issue a pseudonym to match the Patras case

        String soderhamnScope = "urn:soderhamn:registration";
        String patrasScope = "urn:patras:registration";
        String[] scopes = { patrasScope, soderhamnScope };
        if(clientEngine == CryptoEngine.IDEMIX) {

            /// pseudonym values calculated from static secret.xml
            BigInteger[] pseudonymValues = { IssuanceHelper.TEST_CONSTANTS.patrasPseudonymValue_Idemix, IssuanceHelper.TEST_CONSTANTS.soderhamnPseudonymValue_Idemix };
            for(int i=0; i<scopes.length; i++) {
                String scope = scopes[i];
                @SuppressWarnings("unused")
                URI scopeUri = URI.create(scope);
                BigInteger pseudonymValue = pseudonymValues[i];

                URI pseudonymUID = URI.create(scope + ":pseudonymuid:42");
                try {
                    @SuppressWarnings("unused")
                    PseudonymWithMetadata pseudo = UserHelper.getInstance().credentialManager.getPseudonym(pseudonymUID);
                    System.out.println(" - pseudo exists! " + scope);
                    //                    System.out.println(" - pseudo exists! : " + scope + " : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
                } catch (CredentialManagerException e) {

                    PseudonymWithMetadata pwm =
                            this.createPseudonym(secretUid, scope, pseudonymUID,
                                    pseudonymValue);

                    UserHelper.getInstance().credentialManager.storePseudonym(pwm);

                    System.out.println(" - " + scope + " : pseudo Created!");
                    // System.out.println(" - " + scope + " : pseudo Created! : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
                }
            }
        } else if(clientEngine == CryptoEngine.UPROVE) {
            //@SuppressWarnings("unused")
            // SystemParameters systemParameters = AbstractHelper.loadObjectFromResource(folder.getAbsolutePath() + "/issuer_system_params_uprove");
            // UserHelper.getInstance().keyManager.storeSystemParameters(systemParameters);

            String uprovePseudonymResourse = "/eu/abc4trust/sampleXml/patras/uprove-pseudonym.xml";
            for (String scope : scopes) {
                System.out.println("Create Pseudonym for scope : " + scope);
                URI pseudonymUID = URI.create(scope); //  + ":pseudonymuid:uprove:42");
                try {
                    @SuppressWarnings("unused")
                    PseudonymWithMetadata pseudo = UserHelper.getInstance().credentialManager.getPseudonym(pseudonymUID);
                    System.out.println(" - pseudo exists!");
                    //                  System.out.println(" - pseudo exists! : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
                } catch (CredentialManagerException e) {

                    PseudonymWithMetadata pwm =
                            (PseudonymWithMetadata) XmlUtils.getObjectFromXML(
                                    this.getClass().getResourceAsStream(uprovePseudonymResourse), true);
                    pwm.getPseudonym().setScope(scope);
                    pwm.getPseudonym().setPseudonymUID(pseudonymUID);

                    UserHelper.getInstance().credentialManager.storePseudonym(pwm);

                    // System.out.println(" - " + scope + " : pseudo Created! : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), true));
                    System.out.println(" - " + scope + " : pseudo Created!");
                }

            }

        }
        // if(false) {
        // // Step 1b. Load inspectors
        // URI inspector1 =
        // URI.create("http://thebestbank.com/inspector/pub_key_v1");
        // InspectorPublicKey ipk1 = new InspectorPublicKey();
        // ipk1.setPublicKeyUID(inspector1);
        // UserHelper.getInstance().keyManager.storeInspectorPublicKey(inspector1,
        // ipk1);
        //
        // URI inspector2 = URI.create("http://admin.ch/inspector/pub_key_v1");
        // InspectorPublicKey ipk2 = new InspectorPublicKey();
        // ipk2.setPublicKeyUID(inspector2);
        // UserHelper.getInstance().keyManager.storeInspectorPublicKey(inspector2,
        // ipk2);
        // }
        // if(true) {
        // // UserHelper helper = UserHelper.getInstance();
        // // Credential id1 =
        // readFromResource("/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardAnotherCountry.xml");
        // // helper.userCredentialManager.storeCredential(id1);
        // // Credential id2 =
        // readFromResource("/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardYetAnotherCountry.xml");
        // // helper.userCredentialManager.storeCredential(id2);
        // //
        // // Credential cc1 =
        // readFromResource("/eu/abc4trust/sampleXml/credentials/credentialValidCreditCardTheBestBank.xml");
        // // helper.userCredentialManager.storeCredential(cc1);
        //
        // //
        // helper.userCredentialManager.storeCredential(readFromResource("/eu/abc4trust/sampleXml/credentials/credentialPassport.xml"));
        // }

        // this.engine = UserHelper.getInstance().getEngine();

    }

    private PseudonymWithMetadata createPseudonym(URI secretUid, String scope, URI pseudonymUID,
            BigInteger pseudonymValue) {
        //       System.out.println("PSE BIGINT! : " + pseudonymValue);
        byte[] pv = pseudonymValue.toByteArray();
        Pseudonym pseudonym = this.of.createPseudonym();
        pseudonym.setSecretReference(secretUid);
        pseudonym.setExclusive(true);
        pseudonym.setPseudonymUID(pseudonymUID);
        pseudonym.setPseudonymValue(pv);
        pseudonym.setScope(scope);

        Metadata md = this.of.createMetadata();
        PseudonymMetadata pmd = this.of.createPseudonymMetadata();
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("Pregenerated pseudonym");
        pmd.getFriendlyPseudonymDescription().add(fd);
        pmd.setMetadata(md);
        PseudonymWithMetadata pwm = this.of.createPseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
        pwm.setPseudonymMetadata(pmd);

        CryptoParams cryptoEvidence = this.of.createCryptoParams();
        //                  URI groupParameterId = URI.create("http://www.zurich.ibm.com/security/idmx/v2/gp.xml");

        //                  StructureStore.getInstance().add(groupParameterId.toString(), groupParameters);
        StoredDomainPseudonym dp = new StoredDomainPseudonym(URI.create(scope), secretUid, URI.create(IdemixConstants.groupParameterId));
        cryptoEvidence.getAny().add(XMLSerializer.getInstance().serializeAsElement(dp));
        pwm.setCryptoParams(cryptoEvidence);
        return pwm;
    }


    @GET()
    @Path("/init/{CryptoEngine}")
    @Produces(MediaType.TEXT_PLAIN)
    public String init(@PathParam("CryptoEngine") final String cryptoEngineName, final @QueryParam("clientEngine") CryptoEngine clientEngine, final @QueryParam("user") String user) throws Exception {
        System.out.println("user service.init : " + cryptoEngineName + " - client : " + clientEngine + " - user : " + user);
        CryptoEngine cryptoEngine = CryptoEngine.valueOf(cryptoEngineName);
        this.initUserHelper(cryptoEngine, clientEngine, user);
        return "OK";
    }

    @GET()
    @Path("/reset")
    @Produces(MediaType.TEXT_PLAIN)
    public String reset() {
        System.out.println("Service Reset");
        IssuanceHelper.resetInstance();
        UserHelper.resetInstance();
        VerificationHelper.resetInstance();

        return "OK";
    }

    @POST()
    @Path("/user/canBeSatisfied")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response canBeSatisfied(final PresentationPolicyAlternatives presentationPolicy) throws Exception {

        System.out.println("canBeSatisfied");
        try {
            UserHelper.getInstance().getEngine().createPresentationToken(presentationPolicy);
        } catch(CannotSatisfyPolicyException e){
            System.err.println(" - cannot satisfy policy");
            return Response.notAcceptable(null).build();
        } catch (Exception e) {
            System.err.println(" - internal error");
            e.printStackTrace();
            throw e;
        }
        return Response.status(204).build();
    }


    @POST()
    @Path("/user/createPresentationToken")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response createPresentationToken(
            final PresentationPolicyAlternatives presentationPolicy) throws Exception {

        System.out.println("UserService : Creating presentation token - ClientEngine : " + clientEngine);
        try {
            System.out.println(" - : " + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(presentationPolicy)));
        } catch(Exception e) {
            System.err.println(" - could not validate PresentationPolicy XML!");
            e.printStackTrace();
        }

        PresentationToken pt = null;
        try {
            LocalPolicySelector policySelection = new LocalPolicySelector(0, 0);
            pt = UserHelper.getInstance().getEngine().createPresentationToken(presentationPolicy, policySelection);
        } catch(CannotSatisfyPolicyException e){
            System.err.println(" - cannot satisfy policy");
            return Response.notAcceptable(null).build();
        } catch (Exception e) {
            System.err.println(" - internal error");
            e.printStackTrace();
            throw e;
        }
        if(pt == null) {
            // not satisfied...
            System.out.println(" - PresentationPolicy could not be satisfied!");
            return Response.notAcceptable(null).build();
        }

        System.out.println(" - return pt : " + pt);
        try {
            System.out.println(" - : " + XmlUtils.toXml(this.of.createPresentationToken(pt), true));
        } catch(Exception e) {
            System.err.println(" - could not validate PresentationToken XML!");
            e.printStackTrace();
        }

        return Response.ok(this.of.createPresentationToken(pt)).build();
    }

    @POST()
    @Path("/user/createPresentationToken/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_XML})
    public Response createPresentationToken(@PathParam ("SessionID") final String sessionId,
            final PresentationPolicyAlternatives presentationPolicy) throws Exception {


        if(!UserHelper.getInstance().getEngine().canBeSatisfied(presentationPolicy)){
            System.out.println("cannot satisfy policy, halting!");
            return Response.status(422).build();
        }
        System.out.println("Creating presentation token");

        final IdentitySelectionWrapper isw = new IdentitySelectionWrapper();


        identitySelections.put(sessionId, isw);

        Thread thread = new Thread(new Runnable(){
            public void run(){
                try{
                    presentationTokens.put(sessionId,UserHelper.getInstance().getEngine().createPresentationToken(presentationPolicy, isw));
                }catch(Exception e){
                    System.out.println("internal err");
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            while((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            System.out.println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((presentationTokens.get(sessionId)==null) &&!isw.hasPresentationChoices() && !isw.done) {
                return Response.status(500).build();
            }
        }


        if(isw.done || (presentationTokens.get(sessionId)!=null)) {
            identitySelections.remove(sessionId);
            PresentationToken pt = presentationTokens.remove(sessionId);
            if((isw.getException() == null) && (pt != null)) {
                return Response.ok(this.of.createPresentationToken(pt)).build();
            }
        } else{
            return Response.status(203).entity(this.of.createSelectPresentationTokenDescription(isw.selectPresentationTokenDescription)).build();
        }
        return Response.notAcceptable(null).build();
    }

    @POST()
    @Path("/user/createPresentationTokenIdentitySelection/{SessionID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    public Response createPresentationTokenIdentitySelection(@PathParam ("SessionID") final String sessionId,
            final String choice) throws Exception {

        System.out.println("createPresentationToken-JSON");


        IdentitySelectionWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            System.out.println("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }

        try{
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String,Object> userData = mapper.readValue(choice, Map.class);

            int chosenPresentationToken = ((Integer)userData.get("chosenPresentationToken")).intValue();
            @SuppressWarnings("unchecked")
            Map<URI, PseudonymMetadata> metadataToChange = (Map<URI,PseudonymMetadata>)userData.get("metadataToChange");
            @SuppressWarnings("unchecked")
            List<URI> chosenPseudonyms = (ArrayList<URI>)userData.get("chosenPseudonyms");
            @SuppressWarnings("unchecked")
            List<URI> chosenInspectors = (ArrayList<URI>)userData.get("chosenInspectors");
            isw.selectPresentationToken(chosenPresentationToken, metadataToChange, chosenPseudonyms, chosenInspectors);
        }catch(Exception e){ //Something went wrong demarshalling the received JSON
            System.out.println("Failed to map JSON to SptdReturn");
            e.printStackTrace();
            return Response.status(500).build();
        }

        try{
            while(!isw.done) {
                Thread.sleep(200);
            }
        } catch(InterruptedException e){
            if(!isw.done) {
                System.out.println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            return Response.status(500).build();
        }

        PresentationToken pt = presentationTokens.remove(sessionId);
        identitySelections.remove(sessionId);
        if(pt == null){
            System.out.println("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        return Response.ok(this.of.createPresentationToken(pt)).build();
    }

    @POST()
    @Path("/user/issuanceProtocolStep")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response issuanceProtocolStep(final IssuanceMessage mess) throws Exception {

        System.out.println("UserService : issuanceProtocolStep");
        //        System.out.println("- " + XmlUtils.toXml(of.createIssuanceMessage(mess)));

        IssuMsgOrCredDesc userIm;
        try {
            IdentitySelection policySelector = new LocalPolicySelector(0, 0);
            userIm = UserHelper.getInstance().getEngine().issuanceProtocolStep(mess, policySelector);
        } catch (Exception e) {
            System.err.println("- failed to issuanceProtocolStep");
            e.printStackTrace();
            return Response.notAcceptable(null).build();
        }

        boolean lastmessage = (userIm.cd != null);
        if(lastmessage) {
            System.out.println(" - last step : return 204");
            return Response.status(204).build();
        } else {
            System.out.println(" - send IssuanceMessage back to server");
            //            System.out.println("- " + XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
            return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
        }

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
     * @return XML with status 200, JSON with status 203 or empty message with status 204 or 500
     */
    @POST()
    @Path("/user/issuanceProtocolStep/{SessionID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_XML})
    public Response issuanceProtocolStep(@PathParam ("SessionID") final String sessionId,
            final IssuanceMessage mess) {

        System.out.println("issuanceProtocolStep");

        final IdentitySelectionWrapper isw = new IdentitySelectionWrapper();


        identitySelections.put(sessionId, isw);

        Thread thread = new Thread(new Runnable(){
            public void run(){
                try{
                    issuanceMessages.put(sessionId,UserHelper.getInstance().getEngine().issuanceProtocolStep(mess, isw)); //add to include IdentitySelectionWrapper
                }catch(Exception e){
                    System.out.println("internal err");
                    e.printStackTrace();
                    //put e in isw
                }
            }
        });

        thread.start();
        try {
            while((issuanceMessages.get(sessionId)==null) &&!isw.hasIssuanceChoices() && !isw.done) {Thread.sleep(200);}
        }catch(InterruptedException e){
            System.out.println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
            if((issuanceMessages.get(sessionId)==null) &&!isw.hasIssuanceChoices() && !isw.done) {
                return Response.status(500).build();
            }
        }


        if(isw.done || (issuanceMessages.get(sessionId)!=null)) {
            identitySelections.remove(sessionId);
            IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
            if (userIm.cd != null){
                return Response.status(204).build();
            }else{
                return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
            }
        }else {
            return Response.status(203).entity(this.of.createSelectIssuanceTokenDescription(isw.selectIssuanceTokenDescription)).build();
        }

    }



    /**
     * Only used if the ABC engine required user interaction via
     * the UI. Takes the users choice encoded as JSON and passes
     * it on to the ABC engine via the IdentitySelectionWrapper.
     * Either an IssuanceMessage (encoded as XML) is returned with
     * status 200 or an empty message and status 204 is returned
     * if the issuer does not expect a reply.
     * 
     * All exceptions results in an empty message and status 500.
     * 
     * @param sessionId Current sessionId
     * @param choice JSON encoded SitdReturn
     * @return XML with status 200 or empty message with status 204/500
     */
    @POST()
    @Path("/user/issuanceProtocolStepSelect/{SessionID}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_XML)
    public Response issuanceProtocolStepSelect(@PathParam ("SessionID") final String sessionId,
            final String choice) {

        System.out.println("issuanceProtocolStepSelect: "+sessionId);

        IdentitySelectionWrapper isw = identitySelections.get(sessionId);
        if(isw==null){ //Invalid sessionID
            System.out.println("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }


        try{
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String,Object> userData = mapper.readValue(choice, Map.class);

            int chosenIssuanceToken = ((Integer)userData.get("chosenIssuanceToken")).intValue();
            @SuppressWarnings("unchecked")
            Map<URI, PseudonymMetadata> metadataToChange = (Map<URI,PseudonymMetadata>)userData.get("metadataToChange");
            @SuppressWarnings("unchecked")
            List<URI> chosenPseudonyms = (ArrayList<URI>)userData.get("chosenPseudonyms");
            @SuppressWarnings("unchecked")
            List<URI> chosenInspectors = (ArrayList<URI>)userData.get("chosenInspectors");
            @SuppressWarnings("unchecked")
            List<Object> chosenAttributeValues = (ArrayList<Object>)userData.get("chosenAttributeValues");
            isw.selectIssuanceToken(chosenIssuanceToken, metadataToChange, chosenPseudonyms, chosenInspectors, chosenAttributeValues);
        }catch(Exception e){ //Something went wrong demarshalling the received JSON
            System.out.println("Failed to map JSON to SitdReturn");
            e.printStackTrace();
            return Response.status(500).build();
        }

        try{
            while(!isw.done) {
                Thread.sleep(200);
            }
        } catch(InterruptedException e){
            if(!isw.done) {
                System.out.println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
            }
            return Response.status(500).build();
        }
        IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
        identitySelections.remove(sessionId);
        if(userIm == null){
            System.out.println("Unknown IdentitySelectionWrapper for sessionID: "+sessionId+", ABORTING");
            return Response.status(422).build();
        }
        if (userIm.cd != null){ //The ABC Engine returned a credential description, so the protocol is done
            return Response.status(204).build();
        }else{ //The ABC engine returned a issuancemessage that has to be sent to the issuer
            return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
        }
    }



    @POST()
    @Path("/user/updateNonRevocationEvidence")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public Response updateNonRevocationEvidence() {

        new ObjectFactory();

        System.out.println("updateNonRevocationEvidence");

        try {
            UserHelper.getInstance().getEngine().updateNonRevocationEvidence();
            System.out.println(" - updateNonRevocationEvidence Done");
            return Response.ok().build();
        } catch (Exception e) {
            System.out.println(" - updateNonRevocationEvidence Failed");
            e.printStackTrace();
            return Response.serverError().build();
        }

    }

    @POST()
    @Path("/user/listCredentials")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response listCredentials() throws Exception {

        new ObjectFactory();

        System.out.println("listPseudonyms");
        List<PseudonymWithMetadata> list = UserHelper.getInstance().credentialManager.listPseudonyms("urn:soderhamn:registration", true);

        System.out.println("listPseudonyms :  " + list);

        System.out.println("listCredentials");

        try {
            List<URI> resp = UserHelper.getInstance().getEngine().listCredentials();
            //            List<URI> resp = new ArrayList<URI>();
            //            resp.add(new URI("http://asdf.gh/jkl"));

            System.out.println(" - resp " + resp);
            StringBuilder sb = new StringBuilder();
            if (resp != null) {
                for (URI uri : resp) {
                    sb.append(uri);
                    sb.append("\n");
                }
            }
            System.out.println(" - return :  " + sb);
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
            CredentialDescription resp = UserHelper.getInstance().getEngine().getCredentialDescription(uri);

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

        new ObjectFactory();

        System.out.println("getCredentialDescription : " + creduid);

        URI uri;
        try {
            uri = new URI(creduid);
        } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            boolean result = UserHelper.getInstance().getEngine().deleteCredential(uri);
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


    // TEST METHODS FOR CREAATING JSON

    static int ix = 0;

    @SuppressWarnings("unused")
    @GET()
    @Path("/user/createPresentationTokenIdentitySelection")
    //    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_JSON)
    //    @Produces(MediaType.TEXT_XML)
    public Response createPresentationTokenIdentitySelection() throws Exception {
        //      new DefaultClientConfig().getClasses().add(MyJAXBContextResolver.class);
        //      JSONConfiguration.natural().humanReadableFormatting(true).rootUnwrapping(false).build(); //arrays("ApplicationData").build();

        System.out.println("createPresentationToken-JSON");
        if(true) {
            JSonIdentitySelection jsonIDSelect = this.getJSonPresentationTokenIDSelect("presentationPolicyPseudonymOrCredentialsMultilpleTokenCandsPerPolicy.xml"); // presentationPolicyPseudonymOrCredentials.xml"); // presentationPolicyAlternativesHotel.xml");

            return Response.ok(this.of.createSelectPresentationTokenDescription(jsonIDSelect .selectPresentationTokenDescription)).build();
        } else {
            // TODO: remove dead code?
            String[] xmls = { "AdultLogin_IDCredential_visa_amex_NoPseudonym.xml", "AdultLogin_IDCredential_visa_amex_WithPseudonym.xml", "YouthLogin_IDCredential_NoPseudonym.xml", "YouthLogin_IDCredential_WithPseudonym.xml"};

            String res = "/xml/local/ui/" + xmls[ix % 4];
            ix ++;
            System.out.println("Read Resource : " + ix + " : " + res);

            //        String res = "/xml/local/json.xml";
            SelectPresentationTokenDescription sptd = (SelectPresentationTokenDescription) XmlUtils.getJaxbElementFromXml(UserService.class.getResourceAsStream(res),true).getValue();
            return Response.ok(this.of.createSelectPresentationTokenDescription(sptd)).build();
        }
    }

    @GET()
    @Path("/user/createPresentationTokenIdentitySelectionReturn")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    //    @Produces(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_XML)
    public Response createPresentationTokenIdentitySelectionReturn() throws Exception {
        //      new DefaultClientConfig().getClasses().add(MyJAXBContextResolver.class);
        //      JSONConfiguration.natural().humanReadableFormatting(true).rootUnwrapping(false).build(); //arrays("ApplicationData").build();

        System.out.println("createPresentationToken-Return-JSON");

        JSonIdentitySelection jsonIDSelect = this.getJSonPresentationTokenIDSelect("presentationPolicyAlternativesHotel.xml");

        return Response.ok(this.of.createSelectPresentationTokenDescription(jsonIDSelect .selectPresentationTokenDescription)).build();
    }

    // TODO: remove dead code?
    @SuppressWarnings("unused")
    private JSonIdentitySelection getJSonPresentationTokenIDSelect2() throws Exception {
        System.out.println("getJSonPresentationTokenIDSelect 222 TO");

        //      UserHelper.resetInstance();
        //
        //
        //      String[] credSpecResourceList = {
        ////                                         "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcard.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcardAmex.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationCreditcardVisa.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasCourse.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasUniversity.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCard.xml",
        //                                        "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml" };

        //      String[] credSpecResourceList = { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcardAmex.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasCourse.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasUniversity.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
        //                                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardForHotelBooking.xml" };

        //      UserHelper helper = UserHelper.initInstance("ui_select_user", credSpecResourceList );
        //      helper.userCredentialManager.storeCredential(readFromResource("/eu/abc4trust/sampleXml/credentials/credentialStudentId.xml"));
        //      helper.userCredentialManager.storeCredential(readFromResource("/eu/abc4trust/sampleXml/credentials/credentialPassport.xml"));
        UserHelper helper = UserHelper.getInstance();
        System.out.println("### Credential List : " + helper.getEngine().listCredentials().size());
        System.out.println("### Credential List : " + helper.getEngine().listCredentials());
        for(URI uri : helper.getEngine().listCredentials()) {
            CredentialDescription credential = helper.getEngine().getCredentialDescription(uri);
            System.out.println("Credential : \n" + XmlUtils.toXml(this.of.createCredentialDescription(credential)));
        }

        if(! VerificationHelper.isInit()) {
            System.out.println("getJSonPresentationTokenIDSelect : add policies ");
            String[] presentationPolicyResourceList = { "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyForTestingUI.xml",
            };
            //        String systemParamsResource = null;
            String[] issuerParamsResourceList = new String[0];
            String[] credSpecResourceList =
                { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml" };

            String[] inspectorPublicKeyResourceList = new String[0];
            VerificationHelper.initInstance(CryptoEngine.IDEMIX, /*systemParamsResource, */issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, "target/verify_token", presentationPolicyResourceList);

        }
        //      String applicationData =  "RoomType : Double\n" +
        //          "Bedsize : King\n" +
        //          "ArrivalDate : 2012-04-01\n" +
        //          "NrOfNights :  2\n" +
        //          "ReservationCode : HCJ095\n" +
        //          "I agree to the terms of service and cancellation policy.";

        byte[] nonce = VerificationHelper.getInstance().generateNonce();
        String applicationData = null;

        System.out.println("VerificationHelper.getInstance() :  " + VerificationHelper.getInstance());
        PresentationPolicyAlternatives ppa = VerificationHelper.getInstance().createPresentationPolicy("presentationPolicyForTestingUI.xml", nonce, applicationData, null);
        //      PresentationPolicyAlternatives ppa = VerificationHelper.getInstance().createPresentationPolicy("presentationPolicyAlternativesHotelBooking.xml", null);
        //      PresentationPolicyAlternatives ppa = VerificationHelper.getInstance().createPresentationPolicy("presentationPolicyPseudonymOrCredentials.xml", null);

        JSonIdentitySelection jsonIDSelect = new JSonIdentitySelection();

        boolean canBeSatisfied = helper.getEngine().canBeSatisfied(ppa);
        System.out.println("### CAN BE SATISFIED : " + canBeSatisfied);

        helper.getEngine().createPresentationToken(ppa, jsonIDSelect);

        System.out.println("## ENGINE : " + helper.getEngine());
        System.out.println("## cred man : " + helper.credentialManager);

        System.out.println("getJSonPresentationTokenIDSelect JSON : " + jsonIDSelect);
        System.out.println("getJSonPresentationTokenIDSelect JSON : " + jsonIDSelect.selectPresentationTokenDescription);

        return jsonIDSelect;
    }

    private <T> T readFromResource(String resourceName) throws Exception {
        @SuppressWarnings("unchecked")
        T c = (T) XmlUtils.getObjectFromXML(UserService.class.getResourceAsStream(resourceName), false);
        return c;
    }

    @GET()
    @Path("/user/createIssuanceTokenIdentitySelection")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_JSON)
    //    @Produces(MediaType.TEXT_XML)
    public Response createIssuanceTokenIdentitySelection() throws Exception {

        System.out.println("createIssuanceToken-JSON");

        UserHelper.getInstance();

        //
        SpecAndPolicy university = new SpecAndPolicy("CREDSPEC_UNIVERSITY", "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasUniversity.xml","/eu/abc4trust/sampleXml/issuance/issuancePolicyPatrasUniversity.xml");
        SpecAndPolicy cource = new SpecAndPolicy("CREDSPEC_COURCE", "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasCourse.xml","/eu/abc4trust/sampleXml/issuance/issuancePolicyPatrasCourse.xml");
        SpecAndPolicy attendance = new SpecAndPolicy("CREDSPEC_ATTENDANCE", "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPatrasAttendance.xml","/eu/abc4trust/sampleXml/issuance/issuancePolicyPatrasAttendance.xml");

        String systemAndIssuerParamsPrefix = "target/issue_token";
        String fileStoragePrefix = "target/issue_token";

        IssuanceHelper.initInstance(CryptoEngine.MOCK, systemAndIssuerParamsPrefix, fileStoragePrefix, university, cource, attendance);

        Map<String,Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Universityname", "Patras");
        attributeValueMap.put("Departmentname", "CS");
        attributeValueMap.put("Matriculationnumber", 42);
        attributeValueMap.put("Firstname", "Stewart");
        attributeValueMap.put("Lastname", "Dent");

        IssuanceMessage server_im = IssuanceHelper.getInstance().initIssuance("CREDSPEC_UNIVERSITY", attributeValueMap);
        System.out.println(" - IssuanceMessage from server : : " + XmlUtils.toXml(this.of.createIssuanceMessage(server_im), false));
        @SuppressWarnings("unchecked")
        JAXBElement<IssuancePolicy> ip = (JAXBElement<IssuancePolicy>) server_im.getAny().get(0);
        System.out.println(" - Issuance Policy : " + ip);
        System.out.println(" - Issuance Policy : " + ip.getValue().getPresentationPolicy());
        System.out.println(" - Issuance Policy : " + ip.getValue().getPresentationPolicy().getPseudonym());
        try {
            PseudonymWithMetadata pseudo = UserHelper.getInstance().credentialManager.getPseudonym(new URI("foo-bar-pseudonym-uid"));
            System.out.println(" - pseudo exists! : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pseudo), false));
        } catch (CredentialManagerException e) {
            byte[] pv = new byte[3];
            pv[0] = 42;
            pv[1] = 84;
            pv[2] = 117;
            Pseudonym pseudonym = this.of.createPseudonym();
            pseudonym.setExclusive(false);
            pseudonym.setPseudonymUID(URI.create("foo-bar-pseudonym-uid"));
            pseudonym.setPseudonymValue(pv);
            pseudonym.setScope("http://universitypatras.gr/issuer");

            Metadata md = this.of.createMetadata();
            PseudonymMetadata pmd = this.of.createPseudonymMetadata();
            FriendlyDescription fd = new FriendlyDescription();
            fd.setLang("en");
            fd.setValue("");
            pmd.getFriendlyPseudonymDescription().add(fd);
            pmd.setMetadata(md);
            PseudonymWithMetadata pwm = this.of.createPseudonymWithMetadata();
            pwm.setPseudonym(pseudonym);
            pwm.setPseudonymMetadata(pmd);
            UserHelper.getInstance().credentialManager.storePseudonym(pwm);

            System.out.println(" - pseudo Created! : " + XmlUtils.toXml(this.of.createPseudonymWithMetadata(pwm), false));
        }
        JSonIdentitySelection jsonIDSelect = new JSonIdentitySelection();
        IssuMsgOrCredDesc user_im = UserHelper.getInstance().getEngine().issuanceProtocolStep(server_im, jsonIDSelect);
        System.out.println("USER CD : " + user_im.cd);
        System.out.println("USER IM : " + user_im.cd);


        System.out.println(" - credentials : " + UserHelper.getInstance().getEngine().listCredentials());
        System.out.println(" - credentials : " + UserHelper.getInstance().credentialManager.listPseudonyms("http://universitypatras.gr/issuer", false));

        System.out.println("JSON : " + jsonIDSelect);
        System.out.println("JSON : " + jsonIDSelect.selectPresentationTokenDescription);
        System.out.println("JSON : " + jsonIDSelect.selectIssuanceTokenDescription);

        return Response.ok(this.of.createSelectIssuanceTokenDescription(jsonIDSelect.selectIssuanceTokenDescription)).build();
    }


    /*
     //          UserHelper helper = UserHelper.getInstance();
//          Credential id1 = readFromResource("/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardAnotherCountry.xml");
//          helper.userCredentialManager.storeCredential(id1);
//          Credential id2 = readFromResource("/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardYetAnotherCountry.xml");
//          helper.userCredentialManager.storeCredential(id2);
//
//          Credential cc1 = readFromResource("/eu/abc4trust/sampleXml/credentials/credentialValidCreditCardTheBestBank.xml");
//          helper.userCredentialManager.storeCredential(cc1);

     */

    private static boolean exampleCredentialsAdded = false;

    @GET()
    @Path("/user/jsonPresentationToken")
    //    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_JSON)
    //    @Produces(MediaType.TEXT_XML)
    public Response jsonPresentationToken() throws Exception {
        return this.jsonPresentationToken("presentationPolicyPseudonymOrCredentialsMultilpleTokenCandsPerPolicy.xml");
    }

    @GET()
    @Path("/user/jsonPresentationToken/{presentationPolicy}")
    //    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_JSON)
    //    @Produces(MediaType.TEXT_XML)
    public Response jsonPresentationToken(@PathParam ("presentationPolicy") final String presentationPolicy) throws Exception {
        System.out.println("create JSONPresentationToken - for policy : " + presentationPolicy);

        if(! exampleCredentialsAdded) {
            exampleCredentialsAdded = true;
            String[] example_credentials =
                {
                    "/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCard.xml"
                    , "/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardAnotherCountry.xml"
                    , "/eu/abc4trust/sampleXml/credentials/credentialSimpleIdentityCardYetAnotherCountry.xml"
                    , "/eu/abc4trust/sampleXml/credentials/credentialPassport.xml"
                    , "/eu/abc4trust/sampleXml/credentials/credentialValidCreditCard.xml"
                    , "/eu/abc4trust/sampleXml/credentials/credentialValidCreditCardTheBestBank.xml"
                };
            UserHelper helper = UserHelper.getInstance();
            for(String resource : example_credentials) {
                Credential c = this.readFromResource( resource );
                helper.credentialManager.storeCredential(c);
            }
        }
        System.out.println("jsonPresentationToken : " + presentationPolicy);

        JSonIdentitySelection jsonIDSelect = this.getJSonPresentationTokenIDSelect(presentationPolicy);
        if(jsonIDSelect.selectPresentationTokenDescription == null) {
            System.err.println(" - Policy Could not be met!");
            throw new IllegalStateException("Policy Could not be met! : " + presentationPolicy);
        }
        String xml =
                XmlUtils.toXml(new ObjectFactory()
                .createSelectPresentationTokenDescription(jsonIDSelect.selectPresentationTokenDescription),
                false);
        try {
            // verify xml!!!
            System.out.println(" - verify XML! : \n" + xml);
            XmlUtils.getJaxbElementFromXml(new ByteArrayInputStream(xml.getBytes()), true);
            System.out.println(" - XML OK!!!");
        } catch(Exception e) {
            // genereated XML (for json is illegal!)
            System.err.println(" - XML(-for JSON not valid");
            throw new IllegalStateException("Generated PresentationToken XML (for JSON) is invalid", e);
        }

        return Response.ok(this.of.createSelectPresentationTokenDescription(jsonIDSelect .selectPresentationTokenDescription)).build();
    }

    private JSonIdentitySelection getJSonPresentationTokenIDSelect(String policyName) throws Exception {
        UserHelper userHelper = UserHelper.getInstance();

        if(! VerificationHelper.isInit()) {
            System.out.println("getJSonPresentationTokenIDSelect : add policies ");
            String[] presentationPolicyResourceList = {
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotel.xml",
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotelBooking.xml",
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyHotelBookingCreditCardOnly.xml",
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml",
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyForTestingUI.xml",
                    "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyPseudonymOrCredentials.xml",
            "/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyPseudonymOrCredentialsMultilpleTokenCandsPerPolicy.xml"};
            //        String systemParamsResource = null;
            String[] issuerParamsResourceList = new String[0];

            String[] credSpecResourceList =
                { "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml" };

            String[] inspectorPublicKeyResourceList = new String[0];
            VerificationHelper.initInstance(CryptoEngine.MOCK, /*systemParamsResource, */issuerParamsResourceList, credSpecResourceList, inspectorPublicKeyResourceList, "target/verify_token", presentationPolicyResourceList);
        }

        byte[] nonce = VerificationHelper.getInstance().generateNonce();
        PresentationPolicyAlternatives ppa = VerificationHelper.getInstance().createPresentationPolicy(policyName, nonce, null, null);

        JSonIdentitySelection jsonIDSelect = new JSonIdentitySelection();
        userHelper.getEngine().createPresentationToken(ppa, jsonIDSelect);

        System.out.println("getJSonPresentationTokenIDSelect JSON : " + jsonIDSelect);
        System.out.println("getJSonPresentationTokenIDSelect JSON : " + jsonIDSelect.selectPresentationTokenDescription);

        return jsonIDSelect;
    }

    public class LocalPolicySelector implements IdentitySelection {

        private int selectedPolicyNumber;
        List<URI> chosenInspectors;
        private int selectedPseudonymNumber;
        private String selectedPolicyUID;
        @SuppressWarnings("unused")
        private String selectedPseudonymUID;

        public LocalPolicySelector(int selectedPolicyNumber, int selectedPseudonymNumber) {
            this.selectedPolicyNumber = selectedPolicyNumber;
            this.chosenInspectors = new LinkedList<URI>();
            this.selectedPseudonymNumber = selectedPseudonymNumber;
        }

        public LocalPolicySelector(int selectedPolicyNumber, List<URI> chosenInspectors,
                int selectedPseudonymNumber) {
            this.selectedPolicyNumber = selectedPolicyNumber;
            this.chosenInspectors = chosenInspectors;
            this.selectedPseudonymNumber = selectedPseudonymNumber;
        }

        public LocalPolicySelector(String selectedPolicyUID) {
            this.selectedPolicyUID = selectedPolicyUID;
            this.chosenInspectors = new LinkedList<URI>();
        }

        public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
                Map<URI, CredentialDescription> credentialDescriptions,
                Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
                List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids,
                List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice) {

            java.util.logging.Logger.getLogger("LOG TIHS").log(java.util.logging.Level.WARNING, "XXX");
            System.out.println("### selectPresentationTokenDescription");
            System.out.println("*** - policies               : " + policies.size());
            System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
            System.out.println("*** - pseudonyms             : " + pseudonyms.size());
            for(PseudonymDescription pd : pseudonyms.values()) {
                System.out.println("- pd : " + pd.getScope() + " :  " + pd.getPseudonymUID() + " : " + pd.getPseudonymMetadata());
            }
            System.out.println("*** - inspectors             : " + inspectors.size());
            System.out.println("### - tokens                 : " + tokens.size());
            System.out.println("*** - credentialUids         : " + credentialUids.size());
            System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
            System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());


            if(this.selectedPolicyUID!=null) {
                // policy not selected yet...
                this.selectedPolicyNumber = 0;
                boolean found = false;
                System.out.println("All Tokens : ");
                for(PresentationTokenDescription ptd : tokens) {
                    System.out.println("Token : " + ptd.getPolicyUID());
                }
                for(PresentationTokenDescription ptd : tokens) {
                    if(ptd.getPolicyUID().equals(URI.create(this.selectedPolicyUID))) {
                        found = true;
                        break;
                    }
                    this.selectedPolicyNumber ++;
                }
                if(! found) {
                    throw new IllegalStateException("Error in testcase : stated policyUID not found in PresentationPolicyAlternatives : " + this.selectedPolicyUID);
                }
                System.out.println("Select PolicyNumber : " + this.selectedPolicyNumber);
                this.selectedPseudonymNumber = this.selectedPolicyNumber;
            }

            //        if(selectedPseudonymUID!=null) {
            //          this.selectedPseudonymNumber = 0;
            //          boolean found = false;
            //          for(PresentationTokenDescription ptd : tokens) {
            //            if(ptd.getPolicyUID().equals(URI.create(selectedPolicyUID))) {
            //              found = true;
            //              break;
            //            }
            //            selectedPolicyNumber ++;
            //          }
            //          if(! found) {
            //            throw new IllegalStateException("Error in testcase : stated policyUID not found in PresentationPolicyAlternatives : " + selectedPolicyUID);
            //          }
            //          System.out.println("Select PolicyNumber : " + this.selectedPolicyNumber);
            //        }
            Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI, PseudonymMetadata>();

            List<URI> chosenPseudonyms = null;
            // System.out.println(pseudonymChoice);
            Set<List<URI>> pseudonymChoices = pseudonymChoice.get(this.selectedPolicyNumber);
            System.out.println("PSEUDONYM CHOICES : " + pseudonymChoices + " : " + this.selectedPseudonymNumber + " : " + pseudonymChoices.size());
            Iterator<List<URI>> pseudonymIteratorZ = pseudonymChoices.iterator();
            int select = -1;
            int count = 0;
            while(pseudonymIteratorZ.hasNext()) {
                List<URI> next = pseudonymIteratorZ.next();
                System.out.println("List Of Uris : " + next);
                for(URI u : next) {
                    PseudonymDescription pd = pseudonyms.get(u);
                    System.out.println("- pd : " + pd.getScope() + " :  " + pd.getPseudonymUID() + " : " + pd.getPseudonymMetadata() + (pd.getPseudonymMetadata() == null ? "" : " - we have metadata : " + pd.getPseudonymMetadata().getHumanReadableData()));
                    if((pd.getPseudonymMetadata()!= null) && (pd.getPseudonymMetadata().getHumanReadableData()!=null)) {
                        select  = count;
                        break;
                    }
                }
                count++;
                if(select!=-1) {
                    break;
                }
            }
            if(select!=-1) {
                System.out.println("We have pseudonym with metadata... " + select);
                this.selectedPseudonymNumber = select;
            } else {
                System.out.println("Pseudonym does not have metadata");
            }

            Iterator<List<URI>> pseudonymIterator = pseudonymChoices.iterator();
            for (int inx = 0; inx < (this.selectedPseudonymNumber + 1); inx++) {
                chosenPseudonyms = pseudonymIterator.next();
            }
            System.out.println("XX chosenPseudonyms : " + chosenPseudonyms + " : " + chosenPseudonyms.size());
            // check that all chosen has metadata...
            for(URI pUid : chosenPseudonyms) {
                PseudonymDescription pd = pseudonyms.get(pUid);
                System.out.println("Verify chosen Pseudonym " + pUid + " : " + pd + " : " + pd.getScope() + pd.getPseudonymMetadata());

                if((pd.getPseudonymMetadata() != null) && (pd.getPseudonymMetadata().getHumanReadableData() == null)) {
                    System.out.println(" - Create Metadata for Pseudonym : " + pUid + " : " + pd.getScope());

                    PseudonymMetadata pseudonymMetadata = new PseudonymMetadata();
                    pseudonymMetadata.setHumanReadableData("Autogenerated In Selector");

                    metaDataToChange.put(pUid, pseudonymMetadata);
                } else {
                    System.out.println("- Pseudonym has Metadata : " + pUid + " : " + pd.getScope() + pd.getPseudonymMetadata());
                }
            }
            // System.out.println(chosenPseudonyms);
            if (this.chosenInspectors.isEmpty()) {
                for (List<Set<URI>> uris : inspectorChoice) {
                    for (Set<URI> uriset : uris) {
                        this.chosenInspectors.addAll(uriset);
                    }
                }
            }
            // chosenInspectors.addAll(inspectorChoice.get(0).get(0).iterator());

            SptdReturn r =
                    new SptdReturn(this.selectedPolicyNumber, metaDataToChange, chosenPseudonyms,
                            this.chosenInspectors);
            return r;
        }

        @Override
        public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
                Map<URI, CredentialDescription> credentialDescriptions,
                Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
                List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
                List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
                List<List<Set<URI>>> inspectorChoice) {

            java.util.logging.Logger.getLogger("LOG TIHS").log(java.util.logging.Level.WARNING, "XXX");
            System.out.println("### selectIssuanceTokenDescription");
            System.out.println("*** - policies               : " + policies.size());
            System.out.println("*** - credentialDescriptions : " + credentialDescriptions.size());
            System.out.println("*** - pseudonyms             : " + pseudonyms.size());
            System.out.println("*** - inspectors             : " + inspectors.size());
            System.out.println("### - tokens                 : " + tokens.size());
            System.out.println("*** - credentialUids         : " + credentialUids.size());
            System.out.println("*** - selfClaimedAttributes  : " + selfClaimedAttributes.size());
            System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
            System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());


            Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI, PseudonymMetadata>();

            List<URI> chosenPseudonyms = null;
            // System.out.println(pseudonymChoice);
            Set<List<URI>> pseudonymChoices = pseudonymChoice.get(this.selectedPolicyNumber);
            for (int inx = 0; inx < (this.selectedPseudonymNumber + 1); inx++) {
                chosenPseudonyms = pseudonymChoices.iterator().next();
                System.out.println("Choose Pseudonym : " + inx + " : " + chosenPseudonyms);
            }
            System.out.println("Choose Pseudonym : " + chosenPseudonyms);
            // System.out.println(chosenPseudonyms);
            if (this.chosenInspectors.isEmpty()) {
                for (List<Set<URI>> uris : inspectorChoice) {
                    for (Set<URI> uriset : uris) {
                        this.chosenInspectors.addAll(uriset);
                    }
                }
            }
            // chosenInspectors.addAll(inspectorChoice.get(0).get(0).iterator());

            List<Object> chosenAttributes = new ArrayList<Object>();

            SitdReturn r =
                    new SitdReturn(this.selectedPolicyNumber, metaDataToChange, chosenPseudonyms,
                            this.chosenInspectors, chosenAttributes);
            return r;

        }

    }


}




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

package eu.abc4trust.ri.service.user;

import java.io.File;
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

import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.ri.servicehelper.smartcard.SoftwareSmartcardGenerator;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.ri.servicehelper.verifier.VerificationHelper;
import eu.abc4trust.ri.ui.idSelection.IdentitySelectionWrapper;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;



@Path("/")
public class UserService {

  // private UserAbcEngine engine;
  private final ObjectFactory of = new ObjectFactory();

  // For handling idSelection
  static HashMap<String, PresentationToken> presentationTokens;
  static HashMap<String, IssuMsgOrCredDesc> issuanceMessages;
  static HashMap<String, IdentitySelectionWrapper> identitySelections;
  private static final String USERNAME = "defaultUser";
  
  public UserService() throws Exception {
    System.out.println("UserService ()!");
  }

  public BigInteger initUserHelper(CryptoTechnology cryptoTechnology, String user) throws Exception {
    System.out.println("initUserHelper ! - cryptoTechnology : " + cryptoTechnology
        + " - run with user : " + user);

    Logger.getLogger("eu.abc4trust").setLevel(Level.WARNING);
    UserHelper.resetInstance();
    
    File folder;
    String fileStoragePrefix;
    if (new File("target").exists()) {
      fileStoragePrefix = "target/" + user + "_";
      folder = new File("target");
    } else {
      fileStoragePrefix = "integration-test-user/target/" + user + "_";
      folder = new File("integration-test-user/target");
    }

    presentationTokens = new HashMap<String, PresentationToken>();
    issuanceMessages = new HashMap<String, IssuMsgOrCredDesc>();
    identitySelections = new HashMap<String, IdentitySelectionWrapper>();

    System.out.println("UserService initialised !");
    String[] credSpecResourceList =
        {"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSchool.xml",
            "/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnSubject.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
            "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml"};
    List<CredentialSpecification> credSpecList =
        FileSystem.loadXmlListFromResources(credSpecResourceList);

    String testcase = "patras";

    List<IssuerParameters> issuerParamsList =
        FileSystem.findAndLoadXmlResoucesInDir(folder, "issuer_params", testcase, cryptoTechnology
            .toString().toLowerCase());

    List<InspectorPublicKey> inspectorPublicKeyList = null;
    List<RevocationAuthorityParameters> revAuthParamsList = null;
    String systemParamsResource =
        folder.getAbsolutePath() + "/issuer_" + UserHelper.SYSTEM_PARAMS_XML_NAME;
    SystemParameters systemParams = FileSystem.loadXmlFromResource(systemParamsResource);

    UserHelper.initInstance(systemParams, issuerParamsList, fileStoragePrefix, credSpecList,
        inspectorPublicKeyList, revAuthParamsList);

    URI scope = URI.create("urn:patras:registration");
    SoftwareSmartcard softwareSmartcard =
        SoftwareSmartcardGenerator.initSmartCard(1234, scope, systemParams, issuerParamsList, null,
            0);
    BigInteger pseValue = softwareSmartcard.computeScopeExclusivePseudonym(1234, scope);

    // add to mangager
    UserHelper.getInstance().cardStorage.addSmartcard(softwareSmartcard, 1234);

    return pseValue;
  }



  @GET()
  @Path("/init/{CryptoEngine}")
  @Produces(MediaType.TEXT_PLAIN)
  public String init(@PathParam("CryptoEngine") final String cryptoTechnologyName,
      final @QueryParam("user") String user) throws Exception {
    System.out.println("user service.init : [" + cryptoTechnologyName + "] - user : " + user);
    CryptoTechnology cryptoTechnology = CryptoTechnology.valueOf(cryptoTechnologyName);
    BigInteger pseValue = this.initUserHelper(cryptoTechnology, user);
    return pseValue.toString();
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
  public Response canBeSatisfied(final JAXBElement<PresentationPolicyAlternatives> presentationPolicy_jaxb)
      throws Exception {

    System.out.println("canBeSatisfied");
    PresentationPolicyAlternatives presentationPolicy = presentationPolicy_jaxb.getValue();
    try {
      UserHelper.getInstance().getEngine().createPresentationToken(USERNAME, presentationPolicy);
    } catch (CannotSatisfyPolicyException e) {
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
  public Response createPresentationToken(final JAXBElement<PresentationPolicyAlternatives> presentationPolicy_jaxb)
      throws Exception {

    System.out.println("UserService : Creating presentation token..");
    PresentationPolicyAlternatives presentationPolicy = presentationPolicy_jaxb.getValue();
    try {
      System.out.println(" - : "
          + XmlUtils.toXml(this.of.createPresentationPolicyAlternatives(presentationPolicy)));
    } catch (Exception e) {
      System.err.println(" - could not validate PresentationPolicy XML!");
      e.printStackTrace();
    }

    PresentationToken pt = null;
    try {
      pt =
          UserHelper.getInstance().getEngine()
              .createPresentationTokenFirstChoice(USERNAME, presentationPolicy);
    } catch (CannotSatisfyPolicyException e) {
      System.err.println(" - cannot satisfy policy");
      return Response.notAcceptable(null).build();
    } catch (Exception e) {
      System.err.println(" - internal error");
      e.printStackTrace();
      throw e;
    }
    if (pt == null) {
      // not satisfied...
      System.out.println(" - PresentationPolicy could not be satisfied!");
      return Response.notAcceptable(null).build();
    }

    System.out.println(" - return pt : " + pt);
    try {
      System.out.println(" - : " + XmlUtils.toXml(this.of.createPresentationToken(pt), true));
    } catch (Exception e) {
      System.err.println(" - could not validate PresentationToken XML!");
      e.printStackTrace();
    }

    return Response.ok(this.of.createPresentationToken(pt)).build();
  }

  @POST()
  @Path("/user/createPresentationToken/{SessionID}")
  @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
  public Response createPresentationToken(@PathParam("SessionID") final String sessionId,
      final JAXBElement<PresentationPolicyAlternatives> presentationPolicy_jaxb) throws Exception {

    PresentationPolicyAlternatives presentationPolicy = presentationPolicy_jaxb.getValue();
    if (!UserHelper.getInstance().getEngine().canBeSatisfied(USERNAME, presentationPolicy)) {
      System.out.println("cannot satisfy policy, halting!");
      return Response.status(422).build();
    }
    System.out.println("Creating presentation token");

    final IdentitySelectionWrapper isw = new IdentitySelectionWrapper();


    identitySelections.put(sessionId, isw);

    // TODO(enr): Fix this
    if(1<2) {
      throw new RuntimeException("Todo: fix code below");
    }
    /*
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          presentationTokens.put(sessionId, UserHelper.getInstance().getEngine()
              .createPresentationToken(USERNAME, presentationPolicy, isw));
        } catch (Exception e) {
          System.out.println("internal err");
          e.printStackTrace();
        }
      }
    });
    

    thread.start();
    */
    try {
      while ((presentationTokens.get(sessionId) == null) && !isw.hasPresentationChoices()
          && !isw.done) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      System.out
          .println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
      if ((presentationTokens.get(sessionId) == null) && !isw.hasPresentationChoices() && !isw.done) {
        return Response.status(500).build();
      }
    }


    if (isw.done || (presentationTokens.get(sessionId) != null)) {
      identitySelections.remove(sessionId);
      PresentationToken pt = presentationTokens.remove(sessionId);
      if ((isw.getException() == null) && (pt != null)) {
        return Response.ok(this.of.createPresentationToken(pt)).build();
      }
    } else {
      return Response
          .status(203)
          .entity(
              this.of
                  .createSelectPresentationTokenDescription(isw.selectPresentationTokenDescription))
          .build();
    }
    return Response.notAcceptable(null).build();
  }

  @POST()
  @Path("/user/createPresentationTokenIdentitySelection/{SessionID}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_XML)
  public Response createPresentationTokenIdentitySelection(
      @PathParam("SessionID") final String sessionId, final String choice) throws Exception {

    System.out.println("createPresentationToken-JSON");


    IdentitySelectionWrapper isw = identitySelections.get(sessionId);
    if (isw == null) { // Invalid sessionID
      System.out.println("Unknown IdentitySelectionWrapper for sessionID: " + sessionId
          + ", ABORTING");
      return Response.status(422).build();
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      @SuppressWarnings("unchecked")
      Map<String, Object> userData = mapper.readValue(choice, Map.class);

      int chosenPresentationToken = ((Integer) userData.get("chosenPresentationToken")).intValue();
      @SuppressWarnings("unchecked")
      Map<URI, PseudonymMetadata> metadataToChange =
          (Map<URI, PseudonymMetadata>) userData.get("metadataToChange");
      @SuppressWarnings("unchecked")
      List<URI> chosenPseudonyms = (ArrayList<URI>) userData.get("chosenPseudonyms");
      @SuppressWarnings("unchecked")
      List<URI> chosenInspectors = (ArrayList<URI>) userData.get("chosenInspectors");
      isw.selectPresentationToken(chosenPresentationToken, metadataToChange, chosenPseudonyms,
          chosenInspectors);
    } catch (Exception e) { // Something went wrong demarshalling the received JSON
      System.out.println("Failed to map JSON to SptdReturn");
      e.printStackTrace();
      return Response.status(500).build();
    }

    try {
      while (!isw.done) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      if (!isw.done) {
        System.out
            .println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
      }
      return Response.status(500).build();
    }

    PresentationToken pt = presentationTokens.remove(sessionId);
    identitySelections.remove(sessionId);
    if (pt == null) {
      System.out.println("Unknown IdentitySelectionWrapper for sessionID: " + sessionId
          + ", ABORTING");
      return Response.status(422).build();
    }
    return Response.ok(this.of.createPresentationToken(pt)).build();
  }

  @POST()
  @Path("/user/issuanceProtocolStep")
  @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
  @Produces(MediaType.TEXT_XML)
  public Response issuanceProtocolStep(final JAXBElement<IssuanceMessage> mess_jaxb) throws Exception {

    System.out.println("UserService : issuanceProtocolStep");
    IssuanceMessage mess = mess_jaxb.getValue();
    // System.out.println("- " + XmlUtils.toXml(of.createIssuanceMessage(mess)));

    IssuMsgOrCredDesc userIm;
    try {
      userIm = UserHelper.getInstance().getEngine().issuanceProtocolStepFirstChoice(USERNAME, mess);
    } catch (Exception e) {
      System.err.println("- failed to issuanceProtocolStep");
      e.printStackTrace();
      return Response.notAcceptable(null).build();
    }

    boolean lastmessage = (userIm.cd != null);
    if (lastmessage) {
      System.out.println(" - last step : return 204");
      return Response.status(204).build();
    } else {
      System.out.println(" - send IssuanceMessage back to server");
      // System.out.println("- " + XmlUtils.toXml(of.createIssuanceMessage(userIm.im)));
      return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
    }

  }

  /**
   * Takes an IssuanceMessage and passes it on to the ABC engine. If the ABC engine requires user
   * interaction via the UI, a JSON message is returned with status 203 otherwise an IssuanceMessage
   * (encoded as XML) is returned with status 200 or an empty message and status 204 is returned if
   * the issuer does not expect a reply.
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
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
  public Response issuanceProtocolStep(@PathParam("SessionID") final String sessionId,
      final JAXBElement<IssuanceMessage> mess_jaxb) {

    System.out.println("issuanceProtocolStep");
    IssuanceMessage mess = mess_jaxb.getValue();

    final IdentitySelectionWrapper isw = new IdentitySelectionWrapper();


    identitySelections.put(sessionId, isw);

    // TODO(enr): Fix this
    if(1<2) {
      throw new RuntimeException("Todo: fix code below");
    }
    /*
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          issuanceMessages.put(sessionId, UserHelper.getInstance().getEngine()
              .issuanceProtocolStep(USERNAME, mess, isw)); // add to include IdentitySelectionWrapper
        } catch (Exception e) {
          System.out.println("internal err");
          e.printStackTrace();
          // put e in isw
        }
      }
    });

    thread.start();
    */
    try {
      while ((issuanceMessages.get(sessionId) == null) && !isw.hasIssuanceChoices() && !isw.done) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      System.out
          .println("Interrupted while waiting for idSelectionWrapper to get choices or finish");
      if ((issuanceMessages.get(sessionId) == null) && !isw.hasIssuanceChoices() && !isw.done) {
        return Response.status(500).build();
      }
    }


    if (isw.done || (issuanceMessages.get(sessionId) != null)) {
      identitySelections.remove(sessionId);
      IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
      if (userIm.cd != null) {
        return Response.status(204).build();
      } else {
        return Response.ok(this.of.createIssuanceMessage(userIm.im)).build();
      }
    } else {
      return Response.status(203)
          .entity(this.of.createSelectIssuanceTokenDescription(isw.selectIssuanceTokenDescription))
          .build();
    }

  }



  /**
   * Only used if the ABC engine required user interaction via the UI. Takes the users choice
   * encoded as JSON and passes it on to the ABC engine via the IdentitySelectionWrapper. Either an
   * IssuanceMessage (encoded as XML) is returned with status 200 or an empty message and status 204
   * is returned if the issuer does not expect a reply.
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
  public Response issuanceProtocolStepSelect(@PathParam("SessionID") final String sessionId,
      final String choice) {

    System.out.println("issuanceProtocolStepSelect: " + sessionId);

    IdentitySelectionWrapper isw = identitySelections.get(sessionId);
    if (isw == null) { // Invalid sessionID
      System.out.println("Unknown IdentitySelectionWrapper for sessionID: " + sessionId
          + ", ABORTING");
      return Response.status(422).build();
    }


    try {
      ObjectMapper mapper = new ObjectMapper();
      @SuppressWarnings("unchecked")
      Map<String, Object> userData = mapper.readValue(choice, Map.class);

      int chosenIssuanceToken = ((Integer) userData.get("chosenIssuanceToken")).intValue();
      @SuppressWarnings("unchecked")
      Map<URI, PseudonymMetadata> metadataToChange =
          (Map<URI, PseudonymMetadata>) userData.get("metadataToChange");
      @SuppressWarnings("unchecked")
      List<URI> chosenPseudonyms = (ArrayList<URI>) userData.get("chosenPseudonyms");
      @SuppressWarnings("unchecked")
      List<URI> chosenInspectors = (ArrayList<URI>) userData.get("chosenInspectors");
      @SuppressWarnings("unchecked")
      List<Object> chosenAttributeValues =
          (ArrayList<Object>) userData.get("chosenAttributeValues");
      isw.selectIssuanceToken(chosenIssuanceToken, metadataToChange, chosenPseudonyms,
          chosenInspectors, chosenAttributeValues);
    } catch (Exception e) { // Something went wrong demarshalling the received JSON
      System.out.println("Failed to map JSON to SitdReturn");
      e.printStackTrace();
      return Response.status(500).build();
    }

    try {
      while (!isw.done) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      if (!isw.done) {
        System.out
            .println("idSelectionWrapper waiting for ABC engine (after choice has been made) interrupted without being done");
      }
      return Response.status(500).build();
    }
    IssuMsgOrCredDesc userIm = issuanceMessages.remove(sessionId);
    identitySelections.remove(sessionId);
    if (userIm == null) {
      System.out.println("Unknown IdentitySelectionWrapper for sessionID: " + sessionId
          + ", ABORTING");
      return Response.status(422).build();
    }
    if (userIm.cd != null) { // The ABC Engine returned a credential description, so the protocol is
                             // done
      return Response.status(204).build();
    } else { // The ABC engine returned a issuancemessage that has to be sent to the issuer
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
      UserHelper.getInstance().getEngine().updateNonRevocationEvidence(USERNAME);
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
    List<PseudonymWithMetadata> list =
        UserHelper.getInstance().credentialManager.listPseudonyms(USERNAME, "urn:soderhamn:registration",
            true);

    System.out.println("listPseudonyms :  " + list);

    System.out.println("listCredentials");

    try {
      List<URI> resp = UserHelper.getInstance().getEngine().listCredentials(USERNAME);
      // List<URI> resp = new ArrayList<URI>();
      // resp.add(new URI("http://asdf.gh/jkl"));

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
      CredentialDescription resp =
          UserHelper.getInstance().getEngine().getCredentialDescription(USERNAME, uri);

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
      boolean result = UserHelper.getInstance().getEngine().deleteCredential(USERNAME, uri);
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
      for (PseudonymDescription pd : pseudonyms.values()) {
        System.out.println("- pd : " + pd.getScope() + " :  " + pd.getPseudonymUID() + " : "
            + pd.getPseudonymMetadata());
      }
      System.out.println("*** - inspectors             : " + inspectors.size());
      System.out.println("### - tokens                 : " + tokens.size());
      System.out.println("*** - credentialUids         : " + credentialUids.size());
      System.out.println("*** - pseudonymChoice        : " + pseudonymChoice.size());
      System.out.println("*** - inspectorChoice        : " + inspectorChoice.size());


      if (this.selectedPolicyUID != null) {
        // policy not selected yet...
        this.selectedPolicyNumber = 0;
        boolean found = false;
        System.out.println("All Tokens : ");
        for (PresentationTokenDescription ptd : tokens) {
          System.out.println("Token : " + ptd.getPolicyUID());
        }
        for (PresentationTokenDescription ptd : tokens) {
          if (ptd.getPolicyUID().equals(URI.create(this.selectedPolicyUID))) {
            found = true;
            break;
          }
          this.selectedPolicyNumber++;
        }
        if (!found) {
          throw new IllegalStateException(
              "Error in testcase : stated policyUID not found in PresentationPolicyAlternatives : "
                  + this.selectedPolicyUID);
        }
        System.out.println("Select PolicyNumber : " + this.selectedPolicyNumber);
        this.selectedPseudonymNumber = this.selectedPolicyNumber;
      }

      // if(selectedPseudonymUID!=null) {
      // this.selectedPseudonymNumber = 0;
      // boolean found = false;
      // for(PresentationTokenDescription ptd : tokens) {
      // if(ptd.getPolicyUID().equals(URI.create(selectedPolicyUID))) {
      // found = true;
      // break;
      // }
      // selectedPolicyNumber ++;
      // }
      // if(! found) {
      // throw new
      // IllegalStateException("Error in testcase : stated policyUID not found in PresentationPolicyAlternatives : "
      // + selectedPolicyUID);
      // }
      // System.out.println("Select PolicyNumber : " + this.selectedPolicyNumber);
      // }
      Map<URI, PseudonymMetadata> metaDataToChange = new HashMap<URI, PseudonymMetadata>();

      List<URI> chosenPseudonyms = null;
      // System.out.println(pseudonymChoice);
      Set<List<URI>> pseudonymChoices = pseudonymChoice.get(this.selectedPolicyNumber);
      System.out.println("PSEUDONYM CHOICES : " + pseudonymChoices + " : "
          + this.selectedPseudonymNumber + " : " + pseudonymChoices.size());
      Iterator<List<URI>> pseudonymIteratorZ = pseudonymChoices.iterator();
      int select = -1;
      int count = 0;
      while (pseudonymIteratorZ.hasNext()) {
        List<URI> next = pseudonymIteratorZ.next();
        System.out.println("List Of Uris : " + next);
        for (URI u : next) {
          PseudonymDescription pd = pseudonyms.get(u);
          System.out.println("- pd : "
              + pd.getScope()
              + " :  "
              + pd.getPseudonymUID()
              + " : "
              + pd.getPseudonymMetadata()
              + (pd.getPseudonymMetadata() == null ? "" : " - we have metadata : "
                  + pd.getPseudonymMetadata().getHumanReadableData()));
          if ((pd.getPseudonymMetadata() != null)
              && (pd.getPseudonymMetadata().getHumanReadableData() != null)) {
            select = count;
            break;
          }
        }
        count++;
        if (select != -1) {
          break;
        }
      }
      if (select != -1) {
        System.out.println("We have pseudonym with metadata... " + select);
        this.selectedPseudonymNumber = select;
      } else {
        System.out.println("Pseudonym does not have metadata");
      }

      Iterator<List<URI>> pseudonymIterator = pseudonymChoices.iterator();
      for (int inx = 0; inx < (this.selectedPseudonymNumber + 1); inx++) {
        chosenPseudonyms = pseudonymIterator.next();
      }
      System.out.println("XX chosenPseudonyms : " + chosenPseudonyms + " : "
          + chosenPseudonyms.size());
      // check that all chosen has metadata...
      for (URI pUid : chosenPseudonyms) {
        PseudonymDescription pd = pseudonyms.get(pUid);
        System.out.println("Verify chosen Pseudonym " + pUid + " : " + pd + " : " + pd.getScope()
            + pd.getPseudonymMetadata());

        if ((pd.getPseudonymMetadata() != null)
            && (pd.getPseudonymMetadata().getHumanReadableData() == null)) {
          System.out.println(" - Create Metadata for Pseudonym : " + pUid + " : " + pd.getScope());

          PseudonymMetadata pseudonymMetadata = new PseudonymMetadata();
          pseudonymMetadata.setHumanReadableData("Autogenerated In Selector");

          metaDataToChange.put(pUid, pseudonymMetadata);
        } else {
          System.out.println("- Pseudonym has Metadata : " + pUid + " : " + pd.getScope()
              + pd.getPseudonymMetadata());
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

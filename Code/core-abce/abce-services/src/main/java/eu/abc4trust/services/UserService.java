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

package eu.abc4trust.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.utils.SecretWrapper;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.services.helpers.UserDebugger;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.SmartcardInitializeTool;
import eu.abc4trust.util.DummyForNewABCEInterfaces;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.URISet;
import eu.abc4trust.xml.util.XmlUtils;

@Path("/user")
public class UserService {

    private static final CryptoEngine CRYPTO_ENGINE = CryptoEngine.BRIDGED;

    private final ObjectFactory of = new ObjectFactory();

    private final Logger log = Logger.getLogger(UserService.class.getName());

    private final String fileStoragePrefix = Constants.USER_STORAGE_FOLDER
            + "/";

    /**
     * This method, on input a presentation policy p, decides whether the
     * credentials in the User’s credential store could be used to produce a
     * valid presentation token satisfying the policy p. If so, this method
     * returns true, otherwise, it returns false.
     * 
     * @param p
     * @return
     */
    @POST()
    @Path("/canBeSatisfied/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> canBeSatisfied(
            PresentationPolicyAlternatives p) {
        this.log.info("UserService - canBeSatisfied ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            boolean b = instance.getEngine().canBeSatisfied(p);
            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(b);
            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method, on input a presentation policy alternatives p, returns an
     * argument to be passed to the UI for choosing how to satisfy the policy,
     * or returns an error if the policy cannot be satisfied (if the
     * canBeSatisfied method would have returned false). For returning such an
     * argument, this method will investigate whether the User has the necessary
     * credentials and/or established pseudonyms to create one or more (e.g., by
     * satisfying different alternatives in the policy, or by using different
     * sets of credentials to satisfy one alternative) presentation tokens that
     * satisfiy the policy.
     * 
     * The return value of this method should be passed to the User Interface
     * (or to some other component that is capable of rendering a
     * UiPresentationReturn object from a UiPresentationArguments object). The
     * return value of the UI must then be passed to the method
     * createPresentationToken(UiPresentationReturn) for creating a presentation
     * token.
     * 
     * @param p
     * @return
     * @throws CannotSatisfyPolicyException
     * @throws CredentialManagerException
     * @throws KeyManagerException
     */
    @POST()
    @Path("/createPresentationToken/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<UiPresentationArguments> createPresentationToken(
            PresentationPolicyAlternatives p) {
        this.log.info("UserService - createPresentationToken ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        DummyForNewABCEInterfaces d = null;
        try {
            UiPresentationArguments uiPresentationArguments = instance.getEngine().createPresentationToken(p, d);
            return ObjectFactoryReturnTypes.wrap(uiPresentationArguments);
        } catch (CannotSatisfyPolicyException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (KeyManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @POST()
    @Path("/createPresentationTokenUi/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<PresentationToken> createPresentationToken(
            UiPresentationReturn upr) {
        this.log.info("UserService - createPresentationTokenUi ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            URI uid = URI
                    .create("http://ticketcompany/MyFavoriteSoccerTeam/issuance:idemix");
            IssuerParameters ip = instance.keyManager.getIssuerParameters(uid);
            String s = XmlUtils.toXml(this.of.createIssuerParameters(ip));
            System.out.println(s);
        } catch (KeyManagerException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (JAXBException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (SAXException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        try {
            PresentationToken presentationToken = instance.getEngine()
                    .createPresentationToken(upr);
            return this.of.createPresentationToken(presentationToken);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (CryptoEngineException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method performs one step in an interactive issuance protocol. On
     * input an incoming issuance message im obtained from the Issuer, it either
     * returns the outgoing issuance message that is to be sent back to the
     * Issuer, an object that must be sent to the User Interface (UI) to allow
     * the user to decide how to satisfy a policy (or confirm the only choice),
     * or returns a description of the newly issued credential at successful
     * completion of the protocol. In the first case, the Context attribute of
     * the outgoing message has the same value as that of the incoming message,
     * allowing the Issuer to link the different messages of this issuance
     * protocol.
     * 
     * If this is the first time this method is called for a given context, the
     * method expects the issuance message to contain an issuance policy, and
     * returns an object that is to be sent to the UI (allowing the user to
     * chose his preferred way of generating the presentation token, or to
     * confirm the only possible choice).
     * 
     * This method throws an exception if the policy cannot be satisfied with
     * the user's current credentials.
     * 
     * If this method returns an IssuanceMessage, that message should be
     * forwarded to the Issuer. If this method returns a CredentialDescription,
     * then the issuance protocol was successful. If this method returns a
     * UiIssuanceArguments, that object must be forwarded to the UI (or to some
     * other component that is capable of rendering a UiIssuanceReturn object
     * from a UiIssuanceArguments object); the method
     * issuanceProtocolStep(UiIssuanceReturn) should then be invoked with the
     * object returned by the UI.
     * 
     * @param im
     * @return
     * @throws CannotSatisfyPolicyException
     * @throws CryptoEngineException
     * @throws KeyManagerException
     * @throws CredentialManagerException
     */
    @POST()
    @Path("/issuanceProtocolStep/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceReturn> issuanceProtocolStep(
            JAXBElement<IssuanceMessage> jm) {
        this.log.info("UserService - issuanceProtocolStep - IssuanceMessage");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        IssuanceMessage m = jm.getValue();

        DummyForNewABCEInterfaces d = null;
        try {
            IssuanceReturn issuanceReturn = instance.getEngine()
                    .issuanceProtocolStep(m, d);
            return ObjectFactoryReturnTypes.wrap(issuanceReturn);
        } catch (CannotSatisfyPolicyException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (CryptoEngineException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (KeyManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @POST()
    @Path("/issuanceProtocolStepUi/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> issuanceProtocolStep(
            UiIssuanceReturn uir) {

        this.log.info("UserService - issuanceProtocolStep - UiIssuanceReturn");
        ;

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            IssuanceMessage issuanceMessage = instance.getEngine()
                    .issuanceProtocolStep(uir);
            return new ObjectFactory().createIssuanceMessage(issuanceMessage);
        } catch (CryptoEngineException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method updates the non-revocation evidence associated to all
     * credentials in the credential store. Calling this method at regular time
     * intervals reduces the likelihood of having to update non-revocation
     * evidence at the time of presentation, thereby not only speeding up the
     * presentation process, but also offering improved privacy as the
     * Revocation Authority is no longer “pinged” at the moment of presentation.
     * 
     */
    @POST()
    @Path("/updateNonRevocationEvidence/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public void updateNonRevocationEvidence() {
        this.log.info("UserService - updateNonRevocationEvidence ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            instance.credentialManager.updateNonRevocationEvidence();
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method returns an array of all unique credential identifiers (UIDs)
     * available in the Credential Manager.
     * 
     * @return
     */
    @GET()
    @Path("/listCredentials/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<URISet> listCredentials() {
        this.log.info("UserService - listCredentials ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        List<URI> credentialUids;
        try {
            credentialUids = instance.credentialManager.listCredentials();

            URISet uriList = this.of.createURISet();
            uriList.getURI().addAll(credentialUids);
            return this.of.createURISet(uriList);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * This method returns the description of the credential with the given
     * unique identifier. The unique credential identifier credUid is the
     * identifier which was included in the credential description that was
     * returned at successful completion of the issuance protocol.
     * 
     * @param credUid
     * @return
     */
    @GET()
    @Path("/getCredentialDescription/{credentialUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<CredentialDescription> getCredentialDescription(
            @PathParam("credentialUid") URI credUid) {
        this.log.info("UserService - getCredentialDescription ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            CredentialDescription credDesc = instance.credentialManager
                    .getCredentialDescription(credUid);

            return this.of.createCredentialDescription(credDesc);

        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST()
    @Path("/createSmartcard/{issuerParametersUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public void createSmartcard(
            @PathParam("issuerParametersUid") URI issuerParametersUid) {
        this.log.info("UserService - getCredentialDescription ");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            Parser xmlSerializer = Parser.getInstance();
            Random random = new SecureRandom();

            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);
            SystemParameters systemParameters = keyManager
                    .getSystemParameters();

            Element gpAsElement = (Element) systemParameters.getAny().get(1);

            GroupParameters gp = (GroupParameters) xmlSerializer
                    .parse(gpAsElement);

            systemParameters.getAny().add(1, gp);

            SecretWrapper secretWrapper = new SecretWrapper(
                    CryptoEngine.IDEMIX, random, systemParameters);
            IssuerParameters issuerParameters = keyManager
                    .getIssuerParameters(issuerParametersUid);
            secretWrapper.addIssuerParameters(issuerParameters);
            BasicSmartcard softwareSmartcard = secretWrapper
                    .getSoftwareSmartcard();

            instance.cardStorage.addSmartcard(softwareSmartcard, 1234);

            File file = new File(this.fileStoragePrefix + File.separatorChar
                    + "smartcard");
            SmartcardInitializeTool.storeObjectInFile(softwareSmartcard, file);

        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method deletes the credential with the given identifier from the
     * credential store. If deleting is not possible (e.g. if the referred
     * credential does not exist) the method returns false, and true otherwise.
     * 
     * @param credentialUid
     * @return
     */
    @DELETE()
    @Path("/deleteCredential/{credentialUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> deleteCredential(
            @PathParam("credentialUid") URI credentialUid) {
        this.log.info("UserService - deleteCredential \"" + credentialUid + "\"");

        this.initializeHelper();

        UserHelper instance = UserHelper.getInstance();

        try {
            boolean r = instance.credentialManager.deleteCredential(credentialUid);

            ABCEBoolean createABCEBoolean = this.of
                    .createABCEBoolean();
            createABCEBoolean.setValue(r);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @PUT()
    @Path("/storeCredentialSpecification/{credentialSpecifationUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeCredentialSpecification(
            @PathParam("credentialSpecifationUid") URI credentialSpecifationUid,
            CredentialSpecification credSpec) {
        this.log.info("UserService - storeCredentialSpecification ");

        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);

            boolean r = keyManager.storeCredentialSpecification(
                    credentialSpecifationUid, credSpec);

            ABCEBoolean createABCEBoolean = this.of
                    .createABCEBoolean();
            createABCEBoolean.setValue(r);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST()
    @Path("/storeSystemParameters/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeSystemParameters(
            SystemParameters systemParameters) {
        this.log.info("UserService - storeSystemParameters ");

        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);

            boolean r = keyManager
                    .storeSystemParameters(systemParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            if (r) {
                this.initializeHelper();
            }

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT()
    @Path("/storeIssuerParameters/{issuerParametersUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeIssuerParameters(
            @PathParam("issuerParametersUid") URI issuerParametersUid,
            IssuerParameters issuerParameters) {
        this.log.info("UserService - storeIssuerParameters ");

        this.log.info("UserService - storeIssuerParameters - issuerParametersUid: "
                + issuerParametersUid
                + ", "
                + issuerParameters.getParametersUID());
        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);

            boolean r = keyManager.storeIssuerParameters(issuerParametersUid,
                    issuerParameters);

            ABCEBoolean createABCEBoolean = this.of
                    .createABCEBoolean();
            createABCEBoolean.setValue(r);

            this.log.info("UserService - storeIssuerParameters - done ");

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @PUT()
    @Path("/storeRevocationAuthorityParameters/{revocationAuthorityParametersUid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> storeRevocationAuthorityParameters(
            @PathParam("revocationAuthorityParametersUid") URI revocationAuthorityParametersUid,
            RevocationAuthorityParameters revocationAuthorityParameters) {
        this.log.info("UserService - storeRevocationAuthorityParameters: \""
                + revocationAuthorityParameters + "\"");

        try {
            KeyManager keyManager = UserStorageManager
                    .getKeyManager(this.fileStoragePrefix);

            boolean r = keyManager.storeRevocationAuthorityParameters(
                    revocationAuthorityParametersUid,
                    revocationAuthorityParameters);

            ABCEBoolean createABCEBoolean = this.of.createABCEBoolean();
            createABCEBoolean.setValue(r);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (Exception ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    private void initializeHelper() {
        this.log.info("UserService loading...");

        try {
            // Disable non-device-bound secrets.
            PolicyCredentialMatcherImpl.GENERATE_SECRET_IF_NONE_EXIST = false;

            if (UserHelper.isInit()) {
                this.log.info("UserHelper is initialized");
                AbstractHelper.verifyFiles(false, this.fileStoragePrefix);
            } else {
                this.log.info("Initializing UserHelper...");

                UProveIntegration uproveIntegration = new UProveIntegration();
                uproveIntegration.verify();

                UserHelper.initInstanceForService(CRYPTO_ENGINE,
                        this.fileStoragePrefix);

                this.log.info("UserHelper is initialized");
            }
            UserHelper instance = UserHelper.getInstance();
            Set<URI> keySet = instance.cardStorage
                    .getSmartcards().keySet();
            for (URI uri : keySet) {
                System.out.println("Smartcards: " + uri);
            }
        } catch (Exception ex) {
            System.out.println("Create UserHelper FAILED " + ex);
            ex.printStackTrace();
        }
        UserHelper instance = UserHelper.getInstance();
        URI uid = URI
                .create("http://ticketcompany/MyFavoriteSoccerTeam/issuance:idemix");
        new UserDebugger(instance).validate(uid);
    }

    @POST()
    @Path("/extractIssuanceMessage/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<IssuanceMessage> extractIssuanceMessage(
            IssuanceMessageAndBoolean issuanceMessageAndBoolean)
                    throws JAXBException, SAXException, ParserConfigurationException,
                    IOException {
        IssuanceMessage issuanceMessage = issuanceMessageAndBoolean
                .getIssuanceMessage();

        ObjectFactory of = new ObjectFactory();

        // String issuanceMessageAsXML = XmlUtils.toNormalizedXML(of
        // .createIssuanceMessage(issuanceMessage));
        return of.createIssuanceMessage(issuanceMessage);
    }


}
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

package eu.abc4trust.services;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.CannotSatisfyPolicyException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.util.DummyForNewABCEInterfaces;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.URISet;

@Path("/user")
public class UserService {

    private final ObjectFactory of = new ObjectFactory();

    private final Logger log = Logger
            .getLogger(IssuanceService.class.getName());

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

        UserHelper instance = UserHelper.getInstance();

        DummyForNewABCEInterfaces d = null;
        try {
            UiPresentationArguments uiPresentationArguments = instance.getEngine().createPresentationToken(p, d);
            return new JAXBElement<UiPresentationArguments>(
                    QName.valueOf("UiPresentationArguments"),
                    UiPresentationArguments.class, uiPresentationArguments);
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
    JAXBElement<IssuanceReturn> issuanceProtocolStep(IssuanceMessage m) {
        this.log.info("UserService - issuanceProtocolStep ");

        UserHelper instance = UserHelper.getInstance();

        DummyForNewABCEInterfaces d = null;
        try {
            IssuanceReturn issuanceReturn = instance.getEngine()
                    .issuanceProtocolStep(m, d);
            return new JAXBElement<IssuanceReturn>(
                    QName.valueOf("IssuanceReturn"),
                    IssuanceReturn.class,
                    issuanceReturn);
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
    @Path("/getCredentialDescription/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<CredentialDescription> getCredentialDescription(
            URI credUid) {
        this.log.info("UserService - getCredentialDescription ");

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

    /**
     * This method deletes the credential with the given identifier from the
     * credential store. If deleting is not possible (e.g. if the referred
     * credential does not exist) the method returns false, and true otherwise.
     * 
     * @param credUid
     * @return
     */
    @DELETE()
    @Path("/deleteCredential/")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<ABCEBoolean> deleteCredential(URI credUid) {
        this.log.info("UserService - deleteCredential ");

        UserHelper instance = UserHelper.getInstance();

        try {
            boolean r = instance.credentialManager.deleteCredential(credUid);

            ABCEBoolean createABCEBoolean = this.of
                    .createABCEBoolean();
            createABCEBoolean.setValue(r);

            return this.of.createABCEBoolean(createABCEBoolean);
        } catch (CredentialManagerException ex) {
            throw new WebApplicationException(ex,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

}

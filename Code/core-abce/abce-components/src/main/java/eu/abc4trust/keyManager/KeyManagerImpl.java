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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.keyManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.inject.Inject;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.SystemParameters;

public class KeyManagerImpl implements KeyManager {

  //private static final URI UPROVE_TOKENS_UID = URI.create("abc4trust:uprove_keys_and_tokens_uid");
  public static final String CURRENT_REVOCATION_UID_STR = ":current_revocation_information_uid";
  private static final URI SYSTEM_PARAMETERS_UID = URI.create("abc4trust:system_parameters_uid");
  private final PersistenceStrategy persistenceStrategy;

  private final RevocationProxy revocationProxy;

  @Inject
  public KeyManagerImpl(PersistenceStrategy persistenceStrategy, RevocationProxy revocationProxy) {
    this.persistenceStrategy = persistenceStrategy;
    this.revocationProxy = revocationProxy;
  }

  @Override
  public RevocationInformation getCurrentRevocationInformation(URI raParametersUID)
      throws KeyManagerException {

    RevocationInformation revocationInformation =
        this.loadRevocationInformationFromKeyManager(raParametersUID, null);

    // if (revocationInformation == null) {
    // revocationInformation =
    // this.loadRevocationInformationFromKeyManager(RevocationInformationFacade
    // .getRevocationInformationUID(raParametersUID));
    // }

    // Check whether the revocation information is not expired.
    if ((revocationInformation != null)
        && (revocationInformation.getExpires().compareTo(new GregorianCalendar()) > 0)) {
      return revocationInformation;
    }

    if (raParametersUID == null) {
      throw new KeyManagerException(
          "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
    }

    // TODO note that the user/issuer/verifier MUST use getLatestRevocationInformation next...
    // return this.getLatestRevocationInformation(raParametersUID);
    return revocationInformation;
  }


  /**
   * Retrieves the revocation information from the key manager. If the revocationInformationUID is
   * null, the current revocation information is returned.
   * 
   * @throws KeyManagerException
   */
  private RevocationInformation loadRevocationInformationFromKeyManager(URI raParametersUID,
      URI revocationInformationUID) throws KeyManagerException {
    // retrieve the current one
    URI storageRevocationInformationUID;
    if (revocationInformationUID == null) {
      storageRevocationInformationUID = this.getCurrentRevocationInformationUID(raParametersUID);
    } else {
      storageRevocationInformationUID =
          URI.create(raParametersUID.toString() + revocationInformationUID.toString());
    }

    try {
      return (RevocationInformation) this.persistenceStrategy
          .loadObject(storageRevocationInformationUID);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  /**
   * @param raParametersUID
   * @return
   */
  private URI getCurrentRevocationInformationUID(URI raParametersUID) {
    return URI.create(raParametersUID.toString() + CURRENT_REVOCATION_UID_STR);
  }

  @Override
  public RevocationInformation getLatestRevocationInformation(URI raParametersUID)
      throws KeyManagerException {
    try {
      // Revocation information is either non-existing or expired.

      if (raParametersUID == null) {
        throw new KeyManagerException(
            "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
      }

      RevocationAuthorityParameters revocationAuthorityParameters =
          this.getRevocationAuthorityParameters(raParametersUID);
      if (revocationAuthorityParameters == null) {
        throw new KeyManagerException("Unkown revocation authority parameters : " + raParametersUID);
      }

      RevocationInformation revocationInformation =
          this.getRevocationInformationFromRevocationAuthority(revocationAuthorityParameters, null);

      try {
        this.storeRevocationInformation(revocationInformation.getRevocationInformationUID(),
            revocationInformation);
      } catch (KeyManagerException ex) {
        return null;
      }

      try {
        this.storeRevocationInformation(this.getCurrentRevocationInformationUID(raParametersUID),
            revocationInformation);
      } catch (KeyManagerException ex) {
        return null;
      }

      return revocationInformation;
    } catch (Exception ex) {
      throw new KeyManagerException(ex);
    }
  }

  /**
   * Retrieves the revocation information from the revocation authority. If the
   * revocationInformationUID is null, the latest revocation information is fetched.
   * 
   * @throws KeyManagerException
   */
  private RevocationInformation getRevocationInformationFromRevocationAuthority(
      RevocationAuthorityParameters raParameters, URI revocationInformationUID)
      throws KeyManagerException {
    try {
      // Wrap the request into the Revocation message.
      RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade();
      revocationMessageFacade.setContext(URI.create("NO-CONTEXT"));
      revocationMessageFacade.setRevocationAuthorityParametersUID(raParameters.getParametersUID());
      revocationMessageFacade.setRequestLatestRevocationInformation();
      revocationMessageFacade.setRevocationInformationUID(revocationInformationUID);

      // TODO (pbi) the revocation proxy should be found using the revocation information UID
      // (raParameters.getRevocationInfoReference())

      RevocationMessage rm =
          this.revocationProxy.processRevocationMessage(
              revocationMessageFacade.getDelegateeValue(), raParameters);

      // Unwrap RevocationInformation from RevocationMessage.
      RevocationInformation revocationInformation =
          new RevocationMessageFacade(rm).getRevocationInformation();
      if (revocationInformation == null) {
        throw new KeyManagerException(
            "Revocation information cannot be retrieved from revocation authority: "
                + raParameters.getParametersUID());
      }
      return revocationInformation;

    } catch (Exception ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public RevocationInformation getRevocationInformation(URI raParametersUID,
      URI revocationInformationUID) throws KeyManagerException {
    try {
      RevocationInformation revocationInformation =
          this.loadRevocationInformationFromKeyManager(raParametersUID, revocationInformationUID);

      if (revocationInformation == null) {
        RevocationAuthorityParameters revocationAuthorityParameters =
            this.getRevocationAuthorityParameters(raParametersUID);

        if (revocationAuthorityParameters == null) {
          throw new KeyManagerException("Unkown revocation authority parameters");
        }

        revocationInformation =
            this.getRevocationInformationFromRevocationAuthority(revocationAuthorityParameters,
                revocationInformationUID);

        if (revocationInformation == null) {
          return null;
        }

        try {
          this.storeRevocationInformation(revocationInformation.getRevocationInformationUID(),
              revocationInformation);
        } catch (KeyManagerException ex) {
          return null;
        }
      }
      return revocationInformation;
    } catch (Exception ex) {
      throw new KeyManagerException(ex);
    }
  }

  public void storeRevocationInformation(URI riuid, RevocationInformation revocationInformation)
      throws KeyManagerException {
    try {
      boolean r = this.persistenceStrategy.writeObjectAndOverwrite(riuid, revocationInformation);
      if (!r) {
        throw new KeyManagerException("Something went wrong persisting the revocation information.");
      }
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  // private RevocationInformation getRevocationInformationFromRevocationAuthority(
  // RevocationAuthorityParameters revParams, URI revinfouid) throws Exception {
  // try {
  // // Wrap the request into the Revocation message.
  // RevocationMessage revmsg = new RevocationMessage();
  // revmsg.setContext(URI.create("NO-CONTEXT"));
  // revmsg.setRevocationAuthorityParametersUID(revParams.getParametersUID());
  // revmsg.setCryptoParams(new CryptoParams());
  // revmsg
  // .getCryptoParams()
  // .getAny()
  // .add(
  // RevocationUtility
  // .serializeRevocationMessageType(RevocationMessageType.REQUEST_REVOCATION_INFORMATION));
  //
  // revmsg.getCryptoParams().getAny()
  // .add(RevocationUtility.serializeRevocationInfoUid(revinfouid));
  //
  // RevocationMessage rm = this.revocationProxy.processRevocationMessage(revmsg, revParams);
  //
  // // Unwrap RevInfo from RevocationMessage.
  // JAXBElement<RevocationInformation> ri =
  // (JAXBElement<RevocationInformation>) rm.getCryptoParams().getAny().get(0);
  // return ri.getValue();
  // } catch (Exception ex) {
  // throw new KeyManagerException(ex);
  // }
  // }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#storeCurrentRevocationInformation(java.net.URI,
   * eu.abc4trust.xml.RevocationInformation)
   */
  @Override
  public void storeCurrentRevocationInformation(RevocationInformation revocationInformation)
      throws KeyManagerException {
    RevocationInformationFacade revocationInformationFacade =
        new RevocationInformationFacade(revocationInformation);
    
    // storing the revocation information under the actual UID and the default 'current' UID
    try {
      this.storeRevocationInformation(
          this.getCurrentRevocationInformationUID(revocationInformationFacade
              .getRevocationAuthorityParametersId()),
          revocationInformation);
    } catch (ConfigurationException e) {
      throw new KeyManagerException(e);
    }
    this.storeRevocationInformation(revocationInformationFacade.getRevocationInformationId(),
        revocationInformation);
  }

  @Override
  public InspectorPublicKey getInspectorPublicKey(URI ipkuid) throws KeyManagerException {
    try {
      return (InspectorPublicKey) this.persistenceStrategy.loadObject(ipkuid);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean storeInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey)
      throws KeyManagerException {
    try {
      return this.persistenceStrategy.writeObjectAndOverwrite(ipkuid, inspectorPublicKey);
    } catch (Exception ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public IssuerParameters getIssuerParameters(URI issuid) throws KeyManagerException {
    try {
      return (IssuerParameters) this.persistenceStrategy.loadObject(issuid);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean storeIssuerParameters(URI issuid, IssuerParameters issuerParameters)
      throws KeyManagerException {
    try {
      return this.persistenceStrategy.writeObjectAndOverwrite(issuid, issuerParameters);
    } catch (Exception ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public RevocationAuthorityParameters getRevocationAuthorityParameters(URI rapuid)
      throws KeyManagerException {
    try {
      return (RevocationAuthorityParameters) this.persistenceStrategy.loadObject(rapuid);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean storeRevocationAuthorityParameters(URI rapuid,
      RevocationAuthorityParameters revocationAuthorityParameters) throws KeyManagerException {
    try {
      return this.persistenceStrategy
          .writeObjectAndOverwrite(rapuid, revocationAuthorityParameters);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean storeCredentialSpecification(URI uid,
      CredentialSpecification credentialSpecification) throws KeyManagerException {
    try {
      return this.persistenceStrategy.writeObjectAndOverwrite(uid, credentialSpecification);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public CredentialSpecification getCredentialSpecification(URI credspec)
      throws KeyManagerException {
    try {
      return (CredentialSpecification) this.persistenceStrategy.loadObject(credspec);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean storeSystemParameters(SystemParameters systemParameters)
      throws KeyManagerException {
    try {
      return this.persistenceStrategy.writeObjectAndOverwrite(SYSTEM_PARAMETERS_UID,
          systemParameters);
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public SystemParameters getSystemParameters() throws KeyManagerException {
    try {
      SystemParameters sysParams =
          (SystemParameters) this.persistenceStrategy.loadObject(SYSTEM_PARAMETERS_UID);
      if (sysParams == null) {
        throw new KeyManagerException("Could not find the system parameters in the key manager");
      }
      return sysParams;
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }

  @Override
  public boolean hasSystemParameters() throws KeyManagerException {
    try {

      SystemParameters sysParams =
          (SystemParameters) this.persistenceStrategy.loadObject(SYSTEM_PARAMETERS_UID);
      return sysParams != null;
    } catch (PersistenceException ex) {
      throw new KeyManagerException(ex);
    }
  }
/*
  @Override
  public ArrayList<?> getCredentialTokens(URI uid) {
    try {
      URI uri = URI.create(uid.toString() + UPROVE_TOKENS_UID);
      return (ArrayList<?>) this.persistenceStrategy.loadObject(uri);
    } catch (PersistenceException e) {
      return null;
    }
  }

  @Override
  public void storeCredentialTokens(URI uid, ArrayList<?> tokens) throws KeyManagerException {
    try {
      URI uri = URI.create(uid.toString() + UPROVE_TOKENS_UID);
      this.persistenceStrategy.writeObjectAndOverwrite(uri, tokens);
    } catch (PersistenceException e) {
      throw new KeyManagerException(e);
    }
  }
  */

  @Override
  public List<URI> listIssuerParameters() throws KeyManagerException {
    List<URI> ret = new ArrayList<>();
    List<URI> listOfKeys;
    try {
      listOfKeys = Arrays.asList(this.persistenceStrategy.listObjects());
    } catch (PersistenceException e) {
      throw new KeyManagerException(e);
    }

    for(URI uri: listOfKeys) {
      try {
        Object o = this.persistenceStrategy.loadObject(uri);
        if(o != null && o instanceof IssuerParameters) {
          ret.add(uri);
//          System.out.println(uri);
        }
      } catch (PersistenceException e) {
        continue;
      }
    }
    return ret;
  }
}

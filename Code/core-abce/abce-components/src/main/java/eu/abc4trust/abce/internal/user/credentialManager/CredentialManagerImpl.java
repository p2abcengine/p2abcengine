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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import com.fasterxml.uuid.Generators;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializerObjectGzip;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.SecretBasedSmartcard;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IntegerParameter;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.Signature;
import eu.abc4trust.xml.util.XmlUtils;

@Deprecated
// Does not implement username properly
public class CredentialManagerImpl implements CredentialManager {

  public static final String PSEUDONYM_PREFIX = "pseudonym_";
  private final KeyManager keyManager;
  private final CryptoEngineUser cryptoEngineUser;
  private final CredentialStorage storage;
  private final SecretStorage sstorage;
  private final CardStorage cardStorage;
  private final Random prng;
  private final ImageCache imageCache;
  private final CredentialSerializer credentialSerializer;
  private final PseudonymSerializer pseudonymSerializer;

  // TODO : hgk - FIND SOLUTION : for Soderhamn - save pseudonyms in map - to
  // be able to run through presentation when pseudonyms arenot stored on card
  private final Map<String, PseudonymWithMetadata> soderhamnTmpPseudonymMap =
      new HashMap<String, PseudonymWithMetadata>();

  @Inject
  public CredentialManagerImpl(CredentialStorage credentialStore, SecretStorage secretStore,
      KeyManager keyManager, ImageCache imageCache, CryptoEngineUser cryptoEngineUser,
      @Named("RandomNumberGenerator") Random prng, CardStorage cardStorage,
      CredentialSerializer serializer) {
    // WARNING: Due to circular dependencies you MUST NOT dereference
    // cryptoEngineUser
    // in this constructor.
    // (Guice does some magic to support circular dependencies).

    this.keyManager = keyManager;
    this.cryptoEngineUser = cryptoEngineUser;
    this.storage = credentialStore;
    this.cardStorage = cardStorage;
    this.sstorage = secretStore;
    this.prng = prng;
    this.imageCache = imageCache;
    // CredentialSerializerDelegator wraps injected optimized serializer for
    // Idemix + a standard serializer for UProve
    // this.credentialSerializer = new
    // CredentialSerializerDelegator(serializer, new
    // CredentialSerializerObjectGzip());
    // The optimized serializer should also work for UProve now.
    this.credentialSerializer = serializer;
    this.pseudonymSerializer = new PseudonymSerializerObjectGzip(cardStorage);
  }

  private URI getSmartcardUri() {
    Map<URI, BasicSmartcard> scs = this.cardStorage.getSmartcards();
    for (URI uri : scs.keySet()) {
      if (!(scs.get(uri) instanceof SecretBasedSmartcard)) {
        return uri;
      }
    }
    return null;
  }

  private URI escapeUri(URI uri) {
    if (uri.toString().contains(":") && !uri.toString().contains("_")) {
      uri = URI.create(uri.toString().replaceAll(":", "_")); // change all
      // ':' to
      // '_'
    }
    return uri;
  }

  @Override
  public void attachMetadataToPseudonym(String username, Pseudonym pseudonym, PseudonymMetadata md)
      throws CredentialManagerException {
    try {
      PseudonymWithMetadata pwm = null;
      try {
        pwm = this.getPseudonym(username, pseudonym.getPseudonymUID());
      } catch (Exception e) {
        // pwm not present, creating new.
      }
      if (pwm != null) {
        URI pseudonymUri = pseudonym.getPseudonymUID();
        URI SCuri = this.getSmartcardUri();
        if (SCuri == null) {
          this.storage.deletePseudonymWithMetadata(username, pseudonymUri);
        } else {
          Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(SCuri);
          sc.deletePseudonym(this.cardStorage.getPin(SCuri), pseudonymUri);
        }
      } else {
        pwm = new PseudonymWithMetadata();
        pwm.setPseudonym(pseudonym);
      }
      pwm.setPseudonymMetadata(md);
      this.storePseudonym(username, pwm);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public Credential getCredential(String username, URI creduid) throws CredentialManagerException {
    if (creduid == null) {
      throw new CredentialManagerException("Credential UID is null");
    }
    ObjectInputStream objectInput = null;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      // System.out.println("CredentialManagerImpl - fetching credential. Looking first in SC.");
      for (URI uri : this.cardStorage.getSmartcards().keySet()) {
        BasicSmartcard sc = this.cardStorage.getSmartcards().get(uri);
        if (sc instanceof Smartcard) {
          // System.out.println("CredentialManagerImpl - fetching credential. got a SC. trying to fetch the cred: "
          // + creduid);
          Credential cred =
              sc.getCredential(this.cardStorage.getPin(uri), creduid, this.credentialSerializer);
          if (cred != null) {
            // We have to supply certain things here since they are
            // lost in compression
            CredentialDescription descr = cred.getCredentialDescription();
            descr.setCredentialUID(creduid);
            descr.setSecretReference(uri);
            return cred;
          }
          System.err.println("Fetching credential failed. It was null.. ");
        }
      }
      byte[] tokenBytes = this.storage.getCredential(username, creduid);
      if (tokenBytes == null) {
        throw new CredentialNotInStorageException("Credential with UID: \"" + creduid
            + "\" is not in storage");
      }
      byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
      objectInput = new ObjectInputStream(byteArrayInputStream);
      Credential cred = (Credential) objectInput.readObject();
      return cred;
    } catch (CredentialNotInStorageException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectInput);
      StorageUtil.closeIgnoringException(byteArrayInputStream);
    }
  }

  @Override
  public List<CredentialDescription> getCredentialDescription(String username, List<URI> issuers,
      List<URI> credspecs) throws CredentialManagerException {
    List<CredentialDescription> ls = new LinkedList<CredentialDescription>();
    try {
      List<URI> credUris = this.listCredentials(username);
      for (URI credUri : credUris) {
        Credential cred = this.getCredential(username, credUri);
        CredentialDescription credentialDescription = cred.getCredentialDescription();
        if (issuers.contains(credentialDescription.getIssuerParametersUID())) {
          if (credspecs.contains(credentialDescription.getCredentialSpecificationUID())) {
            // System.out.println("ADDED A CRED DESCRIPTION: " +
            // credentialDescription.getCredentialUID());
            ls.add(credentialDescription);
          }
        }
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
    return ls;
  }

  @Override
  public CredentialDescription getCredentialDescription(String username, URI creduid)
      throws CredentialManagerException {
    try {
      Credential cred = this.getCredential(username, creduid);
      CredentialDescription credDesc = cred.getCredentialDescription();
      return credDesc;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  private PseudonymWithMetadata parseBytesAsPseudonymWithMetaData(byte[] bytes)
      throws CredentialManagerException {
    ByteArrayInputStream byteArrayInputStream = null;
    ObjectInputStream objectInput = null;
    try {
      PseudonymWithMetadata pwm = null;
      if (bytes != null) {
        byteArrayInputStream = new ByteArrayInputStream(bytes);
        objectInput = new ObjectInputStream(byteArrayInputStream);
        pwm = (PseudonymWithMetadata) objectInput.readObject();
      }
      return pwm;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectInput);
      StorageUtil.closeIgnoringException(byteArrayInputStream);
    }
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts)
      throws CredentialManagerException {
    Credential cred = this.getCredential(username, creduid);
    try {
      Credential updatedCred =
          this.cryptoEngineUser
              .updateNonRevocationEvidence(username, cred, revparsuid, revokedatts);
      this.storeCredential(username, updatedCred);
      return false;
    } catch (CredentialWasRevokedException ex) {
      cred.getCredentialDescription().setRevokedByIssuer(true);
      this.storeCredential(username, cred);
      return true;
    } catch (CryptoEngineException ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid,
      List<URI> revokedatts, URI revinfouid) throws CredentialManagerException {
    Credential cred = this.getCredential(username, creduid);
    try {
      Credential updatedCred =
          this.cryptoEngineUser.updateNonRevocationEvidence(username, cred, revparsuid,
              revokedatts, revinfouid);
      this.storeCredential(username, updatedCred);
      return false;
    } catch (CredentialWasRevokedException ex) {
      cred.getCredentialDescription().setRevokedByIssuer(true);
      this.storeCredential(username, cred);
      return true;
    } catch (CryptoEngineException ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public List<URI> listCredentials(String username) throws CredentialManagerException {
    List<URI> ls = new ArrayList<URI>();
    try {
      if (this.cardStorage.getSmartcards().size() != 0) {
        for (URI uri : this.cardStorage.getSmartcards().keySet()) {
          BasicSmartcard bsc = this.cardStorage.getSmartcard(uri);
          if (bsc instanceof SecretBasedSmartcard) {
            break;
          }
          Smartcard sc = (Smartcard) bsc;
          int pin = this.cardStorage.getPin(uri);
          Set<URI> setList = sc.listCredentialsUris(pin);
          ls.addAll(setList);
          return ls;
        }
      }
      return this.storage.listCredentials(username);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public URI storeCredential(String username, Credential cred) throws CredentialManagerException {

    CredentialDescription credentialDescription = cred.getCredentialDescription();
    URI credUid = credentialDescription.getCredentialUID();

    if ((credUid == null) || credUid.equals(URI.create(""))) {
      UUID uuid = Generators.randomBasedGenerator(this.prng).generate();
      credUid = URI.create(uuid.toString());
      credentialDescription.setCredentialUID(credUid);
    }

    this.storeImageAndUpdateCredentialDescription(credentialDescription);

    try {
      URI issuerParametersUID = cred.getCredentialDescription().getIssuerParametersUID();
      IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParametersUID);
      URI cryptoAlgorithm = issuerParameters.getAlgorithmID();
      // if uprove - and # of tokens == 1 - set can reuse to 'true'
      if (cryptoAlgorithm.toASCIIString().indexOf("uprove") != -1) {
        // PublicKey pk = ((JAXBElement<PublicKey>)
        // issuerParameters.getCryptoParams().getContent().get(0)).getValue();
        XmlUtils.fixNestedContent(issuerParameters.getCryptoParams());
        PublicKey pk = (PublicKey) issuerParameters.getCryptoParams().getContent().get(0);
        int numberOfTokens = -1;
        String uproveTokensKey = "urn:idmx:3.0.0:issuer:publicKey:uprove:tokens";
        for (Parameter p : pk.getParameter()) {
          if (uproveTokensKey.equals(p.getName())) {
            IntegerParameter ip = (IntegerParameter) p;
            numberOfTokens = ip.getValue();
            break;
          }
        }
        if (numberOfTokens == 1) {
          Object o = cred.getCryptoParams().getContent().get(0);
          if (o instanceof JAXBElement<?> && ((JAXBElement<?>) o).getValue() instanceof Signature) {
            Signature s = ((JAXBElement<Signature>) o).getValue();
            s.setCanReuseToken(true);
          }
        }
      }
    } catch (Exception ignore) {}

    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      URI cardUid = cred.getCredentialDescription().getSecretReference();
      BasicSmartcard bsc = this.cardStorage.getSmartcards().get(cardUid);
            //if(cardUid == null){
            //  cardUid = this.getSmartcardUri();
            //}
            bsc = this.cardStorage.getSmartcards().get(cardUid);
      if ((cardUid != null) && (bsc != null) && !(bsc instanceof SecretBasedSmartcard)) {
        if (!cred.getCredentialDescription().isRevokedByIssuer()) {
          Smartcard sc = (Smartcard) this.cardStorage.getSmartcards().get(cardUid);
          TimingsLogger.logTiming("Smartcard.storeCredential("
              + cred.getCredentialDescription().getCredentialSpecificationUID() + ")", true);
          SmartcardStatusCode status =
              sc.storeCredential(this.cardStorage.getPin(cardUid), credUid, cred,
                  this.credentialSerializer);
          TimingsLogger.logTiming("Smartcard.storeCredential("
              + cred.getCredentialDescription().getCredentialSpecificationUID() + ")", false);
          if (status != SmartcardStatusCode.OK) {
            throw new CredentialManagerException("Could not store credential. Reason: " + status);
          }
          return credUid;
        } else {
        	// The cryptoengine does not know if a credential is stored on a smartcard or not.
        	// During nonrevocation information updates, the credential will be updated and 
        	// therefore saved. In order to save storage, we do not save the credential if
        	// it is revoked.
          throw new CredentialManagerException(
              "Tried to store a revoked credential on a smartcard, do nothing");
        }
      } else {
        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        objectOutput.writeObject(cred);
        byte[] credBytes = byteArrayOutputStream.toByteArray();
        this.storage.addCredential(username, credUid, credBytes);
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }

    return credUid;
  }

  @Override
  public void updateCredential(String username, Credential cred) throws CredentialManagerException {
    URI credUid = cred.getCredentialDescription().getCredentialUID();
    try {
      URI cardUid = cred.getCredentialDescription().getSecretReference();
      BasicSmartcard bsc = this.cardStorage.getSmartcards().get(cardUid);
      if ((cardUid != null) && (bsc != null) && !(bsc instanceof SecretBasedSmartcard)) {
        Smartcard sc = (Smartcard) this.cardStorage.getSmartcards().get(cardUid);
        if (cred.getCredentialDescription().isRevokedByIssuer()) {
          this.deleteCredential(username, credUid);
        } else {
          sc.removeCredentialUri(this.cardStorage.getPin(cardUid), credUid);
          this.storeCredential(username, cred);
        }
      } else {
        this.deleteCredential(username, credUid);
        this.storeCredential(username, cred);
      }
    } catch (Exception e) {
      throw new CredentialManagerException(e);
    }
  }

  private void storeImageAndUpdateCredentialDescription(CredentialDescription credentialDescription)
      throws CredentialManagerException {
    try {
      URI imageRef = credentialDescription.getImageReference();
      URL url = this.imageCache.getDefaultImage();
      if (imageRef != null) {
        url = this.imageCache.storeImage(credentialDescription.getImageReference());
      }
      try {
        String urlString = "" + url;
        if (urlString.contains(" ")) {
          URL fixed = new URL(("" + url).replaceAll(" ", "%20"));
          url = fixed;
        }
      } catch (Exception e) {
        System.err.println("storeImageAndUpdateCredentialDescription - fix failed : " + url + " : "
            + e);
      }
      // System.out.println("storeImageAndUpdateCredentialDescription : "
      // + url);
      credentialDescription.setImageReference(url.toURI());
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public void storePseudonym(String username, PseudonymWithMetadata pwm)
      throws CredentialManagerException {

    Pseudonym pseudonym = pwm.getPseudonym();

    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      URI pseudonymUri = pseudonym.getPseudonymUID();
      URI scUri = this.getSmartcardUri();
//      System.out.println("Store pseudonym: " + pseudonymUri + ", on: " + scUri);
      // pseudonymUri.toString()+"\n");
      if (scUri == null) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        objectOutput.writeObject(pwm);
        byte[] pwmBytes = byteArrayOutputStream.toByteArray();

        this.storage.addPseudonymWithMetadata(username, pseudonymUri, pwmBytes);
      } else {
        if (pseudonym.isExclusive()) {
          // TODO: hgk : find solution to specify if pseudonyms needs
          // to be stored.
          // System.out.println("For Soederhamn pilot, we do not store scope-exclusive pseudonyms. Returning without storing.");
          String key = scUri + "::" + pseudonymUri;
          // System.out.println("For Soederhamn pilot, we do not store scope-exclusive pseudonyms on Smartcard. Store in Memory Map with key : "
          // + key + " : " + pwm.getPseudonym().getScope());
          this.soderhamnTmpPseudonymMap.put(key, pwm);
          return;
        }
        Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(scUri);
        pseudonymUri = this.escapeUri(pseudonymUri);
        pseudonymUri = URI.create(PSEUDONYM_PREFIX + pseudonymUri.toString());
        SmartcardStatusCode code =
            sc.storePseudonym(this.cardStorage.getPin(scUri), pseudonymUri, pwm,
                this.pseudonymSerializer);
//        System.out.println("Result of storing pseudonym on card: " + code);
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }
  }

  @Override
  public void updateNonRevocationEvidence(String username) throws CredentialManagerException,
      IssuerParametersNotInKeystoreException {
    try {
      for (URI credUid : this.listCredentials(username)) {
        Credential cred = this.getCredential(username, credUid);
        CredentialDescription credDesc = this.getCredentialDescription(username, credUid);

        CredentialSpecification credSpec =
            this.keyManager.getCredentialSpecification(credDesc.getCredentialSpecificationUID());

        if (credSpec.isRevocable()) {
          List<URI> revokedatts = new LinkedList<URI>();
          revokedatts.add(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));

          URI issuerParamUid = credDesc.getIssuerParametersUID();
          IssuerParameters issuerParameters = this.keyManager.getIssuerParameters(issuerParamUid);
          if (issuerParameters == null) {
            throw new IssuerParametersNotInKeystoreException(
                "Issuer parameters are not available in the Keystore");
          }
          URI revocationAuthorityParameters = issuerParameters.getRevocationParametersUID();

          if (revocationAuthorityParameters == null) {
            throw new CredentialManagerException(
                "Revocation parameters not found in issuer parameters");
          }

          cred =
              this.cryptoEngineUser.updateNonRevocationEvidence(username, cred,
                  revocationAuthorityParameters, revokedatts);

          this.updateCredential(username, cred);
        }
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public boolean deleteCredential(String username, URI creduid) throws CredentialManagerException {
    try {
      Credential cred = this.getCredential(username, creduid);
      if (cred != null) {
        URI cardUid = cred.getCredentialDescription().getSecretReference();
        BasicSmartcard sc = this.cardStorage.getSmartcards().get(cardUid);
        if ((cardUid != null) && (sc != null) && !(sc instanceof SecretBasedSmartcard)) {
          sc.deleteCredential(this.cardStorage.getPin(cardUid), creduid);
        } else {
          this.storage.deleteCredential(username, creduid);
        }
        return true;
      }
      return false;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public List<PseudonymWithMetadata> listPseudonyms(String username, String scope,
      boolean onlyExclusive) throws CredentialManagerException {
    try {
      URI scUri = this.getSmartcardUri();
      if (scUri == null) {
        List<byte[]> listPseudonyms = this.storage.listPseudonyms(username);
        List<PseudonymWithMetadata> ls =
            new ArrayList<PseudonymWithMetadata>(listPseudonyms.size());
        for (byte[] bytes : listPseudonyms) {
          PseudonymWithMetadata pwm = this.parseBytesAsPseudonymWithMetaData(bytes);
          Pseudonym pseudonym = pwm.getPseudonym();
          if (pseudonym.getScope().equals(scope) && !(onlyExclusive && !pseudonym.isExclusive())) {
            ls.add(pwm);
          }
        }
        return ls;
      } else {
        List<PseudonymWithMetadata> ls = new ArrayList<PseudonymWithMetadata>();
        Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(scUri);
        Map<URI, SmartcardBlob> blobs = sc.getBlobs(this.cardStorage.getPin(scUri));
        for (URI uri : blobs.keySet()) {
          String uriString = uri.toString();
          if (uriString.startsWith(PSEUDONYM_PREFIX)) {
            URI pseudonymUri = URI.create(uriString.substring(0, uriString.length() - 2));
            PseudonymWithMetadata pwm =
                sc.getPseudonym(this.cardStorage.getPin(scUri), pseudonymUri,
                    this.pseudonymSerializer);
            Pseudonym pseudonym = pwm.getPseudonym();
            if (pseudonym.getScope().equals(scope) && !(onlyExclusive && !pseudonym.isExclusive())) {
              ls.add(pwm);
            }
          }
        }
        if (ls.size() == 0) {
          // try the map:
          for (PseudonymWithMetadata pwm : this.soderhamnTmpPseudonymMap.values()) {
            if (pwm.getPseudonym().getScope().equals(scope)) {
              ls.add(pwm);
            }
          }
        }
        return ls;
      }
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public PseudonymWithMetadata getPseudonym(String username, URI pseudonymUid)
      throws CredentialManagerException, PseudonymIsNoInStorageException {
    if (pseudonymUid == null) {
      throw new CredentialManagerException("Pseudonym UID is null");
    }
    URI scUri = this.getSmartcardUri();
    if (scUri == null) {
      // storage based
      byte[] tokenBytes = null;
      try {
        tokenBytes = this.storage.getPseudonymWithData(username, pseudonymUid);
      } catch (Exception ex) {
        throw new CredentialManagerException(ex);
      }
      if (tokenBytes == null) {
        throw new PseudonymIsNoInStorageException("Pseudonym with UID: \"" + pseudonymUid
            + "\"is not in storage");
      }
      return this.parseBytesAsPseudonymWithMetaData(tokenBytes);
    } else {
      // smartcard based
      try {
        Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(scUri);
        int pin = this.cardStorage.getPin(scUri);
        URI cardPseudonymUid = this.escapeUri(pseudonymUid);
        cardPseudonymUid = URI.create(PSEUDONYM_PREFIX + cardPseudonymUid.toString());
        return sc.getPseudonym(pin, cardPseudonymUid, this.pseudonymSerializer);
      } catch (RuntimeException ex) {
        // TODO : Fixup needed
        // Exception from Smartcard/PseudonymSerializer could have been
        // more specific - eg : PseudonymIsNoInStorageException
        // -1 from inputstream means empty - returns exception like :
        // Cannot unserialize this pseudonym: header was -1 expected
        // header 68
        if (ex.getMessage().toString().indexOf("header was -1") != -1
            || ex instanceof ArrayIndexOutOfBoundsException) {

          String key = scUri + "::" + pseudonymUid;
          PseudonymWithMetadata saved = this.soderhamnTmpPseudonymMap.get(key);
          if (saved != null) {
            // System.out.println("For Soderhamn Pilot. Found Pseudonym in Memory Map : "
            // + key + " - Pwd : " + saved + " : " +
            // saved.getPseudonym().getScope()) ;
            return saved;
          } else {
            // System.out.println("For Soderhamn Pilot. Pseudonym NOT found in Memory Map : "
            // + key );
          }
          throw new PseudonymIsNoInStorageException("Pseudonym is not stored on card!" + scUri);
        } else {
          throw new CredentialManagerException(ex);
        }
      } catch (Exception ex) {
        throw new CredentialManagerException(ex);
      }
    }
  }

  @Override
  public boolean deletePseudonym(String username, URI pseudonymUid)
      throws CredentialManagerException {
    try {
      PseudonymWithMetadata pseudonym = this.getPseudonym(username, pseudonymUid);
      if (pseudonym != null) {
        URI scUri = this.getSmartcardUri();
        if (scUri == null) {
          this.storage.deletePseudonymWithMetadata(username, pseudonymUid);
          return true;
        } else {
          Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(scUri);
          sc.deletePseudonym(this.cardStorage.getPin(scUri), pseudonymUid);
        }
      }
      return false;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public void storeSecret(String username, Secret secret) throws CredentialManagerException {

    ByteArrayOutputStream byteArrayOutputStream = null;
    ObjectOutputStream objectOutput = null;
    try {
      byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutput = new ObjectOutputStream(byteArrayOutputStream);
      objectOutput.writeObject(secret);
      byte[] pwmBytes = byteArrayOutputStream.toByteArray();

      URI secretUri = secret.getSecretDescription().getSecretUID();
      this.sstorage.addSecret(username, secretUri, pwmBytes);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectOutput);
      StorageUtil.closeIgnoringException(byteArrayOutputStream);
    }
  }

  @Override
  public List<SecretDescription> listSecrets(String username) throws CredentialManagerException {

    try {
      List<URI> listSecrets = this.sstorage.listSecrets(username);
      List<SecretDescription> ls = new ArrayList<SecretDescription>(listSecrets.size());
      for (URI uri : listSecrets) {
        ls.add(this.getSecret(username, uri).getSecretDescription());
      }

      URI scUri = this.getSmartcardUri();
      if (scUri != null) {
        Smartcard sc = (Smartcard) this.cardStorage.getSmartcard(scUri);
        SecretDescription secretDescr = new SecretDescription();
        secretDescr.setDeviceBoundSecret(true);
        secretDescr.setSecretUID(sc.getDeviceURI(this.cardStorage.getPin(scUri)));
        FriendlyDescription fd = new FriendlyDescription();
        fd.setLang("en");
        fd.setValue("Placeholder for Smartcard secret");
        secretDescr.getFriendlySecretDescription().add(fd);
        ls.add(secretDescr);
      }

      return ls;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public boolean deleteSecret(String username, URI secuid) throws CredentialManagerException {
    try {
      Secret secret = this.getSecret(username, secuid);
      if (secret != null) {
        this.sstorage.deleteSecret(username, secuid);
        return true;
      }
      return false;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }
  }

  @Override
  public Secret getSecret(String username, URI secuid) throws CredentialManagerException,
      SecretNotInStorageException {
    if (secuid == null) {
      throw new IllegalArgumentException("Secret UID is null");
    }
    ObjectInputStream objectInput = null;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      byte[] secretBytes = this.sstorage.getSecret(username, secuid);
      if (secretBytes == null) {
        throw new SecretNotInStorageException("Secret is not in storage");
      }
      byteArrayInputStream = new ByteArrayInputStream(secretBytes);
      objectInput = new ObjectInputStream(byteArrayInputStream);
      Secret secret = (Secret) objectInput.readObject();
      return secret;
    } catch (SecretNotInStorageException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    } finally {
      // Close the streams.
      StorageUtil.closeIgnoringException(objectInput);
      StorageUtil.closeIgnoringException(byteArrayInputStream);
    }
  }

  @Override
  public void updateSecretDescription(String username, SecretDescription desc)
      throws CredentialManagerException {
    if (desc == null) {
      throw new IllegalArgumentException("Secret Description is null");
    }
    try {
      Secret sec = this.getSecret(username, desc.getSecretUID());
      sec.setSecretDescription(desc);
      this.sstorage.deleteSecret(username, desc.getSecretUID());
      this.storeSecret(username, sec);
    } catch (Exception ex) {
      throw new CredentialManagerException(ex);
    }

  }

}

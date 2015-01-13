//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.inject.Inject;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class NewUserCredentialManager implements CredentialManager {

  private final PersistentStorage ps;

  @Inject
  public NewUserCredentialManager(PersistentStorage ps) {
    this.ps = ps;
  }

  @Override
  public List<CredentialDescription> getCredentialDescription(String username, List<URI> issuers,
      List<URI> credspecs) throws CredentialManagerException {
    List<URI> creds = ps.listCredentials(username, issuers, credspecs);
    List<CredentialDescription> ret = new ArrayList<>();
    for (URI u : creds) {
      Credential cred = (Credential) ByteSerializer.readFromBytes(ps.getCredential(u, username));
      ret.add(cred.getCredentialDescription());
    }
    return ret;
  }

  @Override
  public CredentialDescription getCredentialDescription(String username, URI creduid)
      throws CredentialManagerException {
    Credential cred = getCredential(username, creduid);
    if (cred == null) {
      return null;
    } else {
      return cred.getCredentialDescription();
    }
  }

  @Override
  public void attachMetadataToPseudonym(String username, Pseudonym p, PseudonymMetadata md)
      throws CredentialManagerException {
    PseudonymWithMetadata pwm = getPseudonym(username, p.getPseudonymUID());
    if (pwm == null) {
      pwm = new ObjectFactory().createPseudonymWithMetadata();
      pwm.setPseudonym(p);
      pwm.setPseudonymMetadata(md);
      ps.insertPseudonym(p.getPseudonymUID(), username, p.getScope(), p.isExclusive(),
          p.getPseudonymValue(), ByteSerializer.writeAsBytes(pwm));
    } else {
      pwm.setPseudonymMetadata(md);
      ps.updatePseudonym(p.getPseudonymUID(), username, ByteSerializer.writeAsBytes(pwm));
    }
  }

  @Override
  public Credential getCredential(String username, URI creduid) throws CredentialManagerException {
    final Credential ret =
        (Credential) ByteSerializer.readFromBytes(ps.getCredential(creduid, username));
    if (ret != null && ret.getCryptoParams() != null) {
      XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
    return ret;
  }

  @Override
  public void storePseudonym(String username, PseudonymWithMetadata pwm)
      throws CredentialManagerException {
    ps.insertPseudonym(pwm.getPseudonym().getPseudonymUID(), username, pwm.getPseudonym()
        .getScope(), pwm.getPseudonym().isExclusive(), pwm.getPseudonym().getPseudonymValue(),
        ByteSerializer.writeAsBytes(pwm));
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid,
      List<URI> revokedatts, URI revinfouid) throws CredentialManagerException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateNonRevocationEvidence(String username) throws CredentialManagerException {
    // TODO Auto-generated method stub

  }

  @Override
  public URI storeCredential(String username, Credential cred) throws CredentialManagerException {
    URI credUri = cred.getCredentialDescription().getCredentialUID();
    if (credUri == null) {
      credUri = URI.create("cred-" + UUID.randomUUID().toString());
      cred.getCredentialDescription().setCredentialUID(credUri);
    }
    boolean result =
        ps.insertCredential(credUri, username, cred.getCredentialDescription()
            .getIssuerParametersUID(), cred.getCredentialDescription()
            .getCredentialSpecificationUID(), ByteSerializer.writeAsBytes(cred));

    return result ? credUri : null;
  }

  @Override
  public void updateCredential(String username, Credential cred) throws CredentialManagerException {
    ps.updateCredential(cred.getCredentialDescription().getCredentialUID(), username,
        ByteSerializer.writeAsBytes(cred));
  }

  @Override
  public List<URI> listCredentials(String username) throws CredentialManagerException {
    return ps.listCredentials(username);
  }

  @Override
  public boolean deleteCredential(String username, URI creduid) throws CredentialManagerException {
    return ps.deleteCredential(creduid, username);
  }

  @Override
  public List<PseudonymWithMetadata> listPseudonyms(String username, String scope,
      boolean onlyExclusive) throws CredentialManagerException {
    List<URI> pseudonymUris;
    if (onlyExclusive) {
      pseudonymUris = ps.listPseudonyms(username, scope, true);
    } else {
      pseudonymUris = ps.listPseudonyms(username, scope);
    }
    List<PseudonymWithMetadata> ret = new ArrayList<>();
    for (URI s : pseudonymUris) {
      PseudonymWithMetadata secret = getPseudonym(username, s);
      ret.add(secret);
    }
    return ret;
  }

  @Override
  public PseudonymWithMetadata getPseudonym(String username, URI pseudonymUid)
      throws CredentialManagerException {
    final PseudonymWithMetadata ret =
        (PseudonymWithMetadata) ByteSerializer.readFromBytes(ps
            .getPseudonym(pseudonymUid, username));
    if (ret != null && ret.getCryptoParams() != null) {
      XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
    return ret;
  }

  @Override
  public boolean deletePseudonym(String username, URI pseudonymUid)
      throws CredentialManagerException {
    return ps.deletePseudonym(pseudonymUid, username);
  }

  @Override
  public void storeSecret(String username, Secret cred) throws CredentialManagerException {
    ps.insertSecret(cred.getSecretDescription().getSecretUID(), username,
        ByteSerializer.writeAsBytes(cred));
  }

  @Override
  public List<SecretDescription> listSecrets(String username) throws CredentialManagerException {
    List<URI> secretUris = ps.listSecrets(username);
    List<SecretDescription> ret = new ArrayList<>();
    for (URI s : secretUris) {
      Secret secret = getSecret(username, s);
      ret.add(secret.getSecretDescription());
    }
    return ret;
  }

  @Override
  public boolean deleteSecret(String username, URI secuid) throws CredentialManagerException {
    return ps.deleteSecret(secuid, username);
  }

  @Override
  public Secret getSecret(String username, URI secuid) throws CredentialManagerException {
    return (Secret) ByteSerializer.readFromBytes(ps.getSecret(secuid, username));
  }

  @Override
  public void updateSecretDescription(String username, SecretDescription desc)
      throws CredentialManagerException {
    Secret oldSecret = getSecret(username, desc.getSecretUID());
    if (oldSecret == null) {
      return;
    }
    oldSecret.setSecretDescription(desc);
    ps.updateSecret(desc.getSecretUID(), username, ByteSerializer.writeAsBytes(desc));
  }

}

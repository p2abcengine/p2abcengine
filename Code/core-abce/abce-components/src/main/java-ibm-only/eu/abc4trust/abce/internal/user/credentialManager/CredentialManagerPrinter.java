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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class CredentialManagerPrinter implements CredentialManager {

  private static final String PATH = "target/outputXml/";
  
  private final CredentialManager cm;
  
  @Inject
  CredentialManagerPrinter(@Named("RealCredManager") CredentialManager cm) {
    this.cm = cm;
  }
  
  @Override
  public List<CredentialDescription> getCredentialDescription(String username, List<URI> issuers, List<URI> credspecs)
      throws CredentialManagerException {
    return cm.getCredentialDescription(username, issuers, credspecs);
  }

  @Override
  public CredentialDescription getCredentialDescription(String username, URI creduid)
      throws CredentialManagerException {
    return cm.getCredentialDescription(username, creduid);
  }

  @Override
  public void attachMetadataToPseudonym(String username, Pseudonym p, PseudonymMetadata md)
      throws CredentialManagerException {
    cm.attachMetadataToPseudonym(username, p, md);
  }

  @Override
  public Credential getCredential(String username, URI creduid) throws CredentialManagerException {
    return cm.getCredential(username, creduid);
  }

  @Override
  public void storePseudonym(String username, PseudonymWithMetadata pwm) throws CredentialManagerException {
    cm.storePseudonym(username, pwm);
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts)
      throws CredentialManagerException {
    return cm.hasBeenRevoked(username, creduid, revparsuid, revokedatts);
  }

  @Override
  public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts, URI revinfouid)
      throws CredentialManagerException {
    return cm.hasBeenRevoked(username, creduid, revparsuid, revokedatts, revinfouid);
  }

  @Override
  public void updateNonRevocationEvidence(String username) throws CredentialManagerException {
    cm.updateNonRevocationEvidence(username);
  }

  @Override
  public URI storeCredential(String username, Credential cred) throws CredentialManagerException {
    
    URI credUri = cm.storeCredential(username, cred);
    
    String filename = PATH + "c-" + credUri.toString().replace("/", "_").replace(':', '_');
    try {
      (new File(PATH)).mkdir();
      ObjectFactory of = new ObjectFactory();
      String xml = XmlUtils.toXml(of.createCredential(cred), false);
      PrintWriter out = new PrintWriter(filename + ".xml");
      out.println(xml);
      out.close();
      System.out.println("Stored credential " + credUri + " at " + filename + ".xml");
    } catch(Exception e) {
      try {
        OutputStream file = new FileOutputStream( filename + ".obj" );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );
        output.writeObject(cred);
        output.close();
        System.out.println("Stored credential " + credUri + " at " + filename + ".obj");
      } catch(Exception e2) {
        System.err.println("Cannot store credential " + credUri + " at " + filename + "{.xml,.obj}");
      }
    }
    return credUri;
  }

  @Override
  public List<URI> listCredentials(String username) throws CredentialManagerException {
    return cm.listCredentials(username);
  }

  @Override
  public boolean deleteCredential(String username, URI creduid) throws CredentialManagerException {
    return cm.deleteCredential(username, creduid);
  }

  @Override
  public List<PseudonymWithMetadata> listPseudonyms(String username, String scope, boolean onlyExclusive)
      throws CredentialManagerException {
    return cm.listPseudonyms(username, scope, onlyExclusive);
  }

  @Override
  public PseudonymWithMetadata getPseudonym(String username, URI pseudonymUid) throws CredentialManagerException {
    return cm.getPseudonym(username, pseudonymUid);
  }

  @Override
  public boolean deletePseudonym(String username, URI pseudonymUid) throws CredentialManagerException {
    return cm.deletePseudonym(username, pseudonymUid);
  }

  @Override
  public void storeSecret(String username, Secret cred) throws CredentialManagerException {
    cm.storeSecret(username, cred);
  }

  @Override
  public List<SecretDescription> listSecrets(String username) throws CredentialManagerException {
    return cm.listSecrets(username);
  }

  @Override
  public boolean deleteSecret(String username, URI secuid) throws CredentialManagerException {
    return cm.deleteSecret(username, secuid);
  }

  @Override
  public Secret getSecret(String username, URI secuid) throws CredentialManagerException {
    return cm.getSecret(username, secuid);
  }

  @Override
  public void updateSecretDescription(String username, SecretDescription desc) throws CredentialManagerException {
    cm.updateSecretDescription(username, desc);
  }
  
  @Override
  public void updateCredential(String username, Credential cred) throws CredentialManagerException {
    cm.updateCredential(username, cred);
  }

}

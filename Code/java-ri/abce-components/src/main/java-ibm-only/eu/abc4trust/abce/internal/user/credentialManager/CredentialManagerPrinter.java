//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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
  public List<CredentialDescription> getCredentialDescription(List<URI> issuers, List<URI> credspecs)
      throws CredentialManagerException {
    return cm.getCredentialDescription(issuers, credspecs);
  }

  @Override
  public CredentialDescription getCredentialDescription(URI creduid)
      throws CredentialManagerException {
    return cm.getCredentialDescription(creduid);
  }

  @Override
  public void attachMetadataToPseudonym(Pseudonym p, PseudonymMetadata md)
      throws CredentialManagerException {
    cm.attachMetadataToPseudonym(p, md);
  }

  @Override
  public Credential getCredential(URI creduid) throws CredentialManagerException {
    return cm.getCredential(creduid);
  }

  @Override
  public PseudonymWithMetadata getPseudonymWithMetadata(Pseudonym p)
      throws CredentialManagerException {
    return cm.getPseudonymWithMetadata(p);
  }

  @Override
  public void storePseudonym(PseudonymWithMetadata pwm) throws CredentialManagerException {
    cm.storePseudonym(pwm);
  }

  @Override
  public boolean hasBeenRevoked(URI creduid, URI revparsuid, List<URI> revokedatts)
      throws CredentialManagerException {
    return cm.hasBeenRevoked(creduid, revparsuid, revokedatts);
  }

  @Override
  public boolean hasBeenRevoked(URI creduid, URI revparsuid, List<URI> revokedatts, URI revinfouid)
      throws CredentialManagerException {
    return cm.hasBeenRevoked(creduid, revparsuid, revokedatts, revinfouid);
  }

  @Override
  public void updateNonRevocationEvidence() throws CredentialManagerException {
    cm.updateNonRevocationEvidence();
  }

  @Override
  public URI storeCredential(Credential cred) throws CredentialManagerException {
    
    URI credUri = cm.storeCredential(cred);
    
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
  public List<URI> listCredentials() throws CredentialManagerException {
    return cm.listCredentials();
  }

  @Override
  public boolean deleteCredential(URI creduid) throws CredentialManagerException {
    return cm.deleteCredential(creduid);
  }

  @Override
  public List<PseudonymWithMetadata> listPseudonyms(String scope, boolean onlyExclusive)
      throws CredentialManagerException {
    return cm.listPseudonyms(scope, onlyExclusive);
  }

  @Override
  public PseudonymWithMetadata getPseudonym(URI pseudonymUid) throws CredentialManagerException {
    return cm.getPseudonym(pseudonymUid);
  }

  @Override
  public boolean deletePseudonym(URI pseudonymUid) throws CredentialManagerException {
    return cm.deletePseudonym(pseudonymUid);
  }

  @Override
  public void storeSecret(Secret cred) throws CredentialManagerException {
    cm.storeSecret(cred);
  }

  @Override
  public List<SecretDescription> listSecrets() throws CredentialManagerException {
    return cm.listSecrets();
  }

  @Override
  public boolean deleteSecret(URI secuid) throws CredentialManagerException {
    return cm.deleteSecret(secuid);
  }

  @Override
  public Secret getSecret(URI secuid) throws CredentialManagerException {
    return cm.getSecret(secuid);
  }

  @Override
  public void updateSecretDescription(SecretDescription desc) throws CredentialManagerException {
    cm.updateSecretDescription(desc);
  }
  
  @Override
  public void updateCredential(Credential cred) throws CredentialManagerException {
    cm.updateCredential(cred);
  }

}

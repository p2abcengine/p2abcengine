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

// * Licensed Materials - Property of IBM *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.keyManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class KeyManagerPrinter implements KeyManager {

  private static final String PATH = "target/outputXml/";

  private final KeyManager km;

  @Inject
  KeyManagerPrinter(@Named("RealKeyManager") KeyManager km) {
    this.km = km;
  }

  @Override
  public IssuerParameters getIssuerParameters(URI issuid) throws KeyManagerException {
    return km.getIssuerParameters(issuid);
  }

  @Override
  public boolean storeIssuerParameters(URI issuid, IssuerParameters issuerParameters)
      throws KeyManagerException {

    String filename = PATH + "ip-" + issuid.toString().replace("/", "_").replace(':', '_') + ".obj";

    try {
      (new File(PATH)).mkdir();
      OutputStream file = new FileOutputStream(filename);
      OutputStream buffer = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buffer);
      output.writeObject(issuerParameters);
      output.close();
      System.out.println("Wrote issuer parameters to " + filename);
    } catch (Exception e) {
      System.err.println("Could not write issuer parameters to " + filename);
    }
    return km.storeIssuerParameters(issuid, issuerParameters);
  }

  @Override
  public InspectorPublicKey getInspectorPublicKey(URI ipkuid) throws KeyManagerException {
    return km.getInspectorPublicKey(ipkuid);
  }

  @Override
  public boolean storeInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey)
      throws KeyManagerException {
    return km.storeInspectorPublicKey(ipkuid, inspectorPublicKey);
  }

  @Override
  public RevocationAuthorityParameters getRevocationAuthorityParameters(URI rapuid)
      throws KeyManagerException {
    return km.getRevocationAuthorityParameters(rapuid);
  }

  @Override
  public RevocationInformation getCurrentRevocationInformation(URI rapuid)
      throws KeyManagerException {
    return km.getCurrentRevocationInformation(rapuid);
  }

  @Override
  public void storeRevocationInformation(URI informationUID,
      RevocationInformation revocationInformation) throws KeyManagerException {
    km.storeRevocationInformation(informationUID, revocationInformation);
  }

  @Override
  public RevocationInformation getRevocationInformation(URI rapuid, URI revinfouid)
      throws KeyManagerException {
    return km.getRevocationInformation(rapuid, revinfouid);
  }

  @Override
  public void storeCurrentRevocationInformation(RevocationInformation revocationInformation)
      throws KeyManagerException {

    km.storeCurrentRevocationInformation(revocationInformation);

  }

  @Override
  public boolean storeRevocationAuthorityParameters(URI issuid,
      RevocationAuthorityParameters revocationAuthorityParameters) throws KeyManagerException {

    String filename =
        PATH + "rap-" + issuid.toString().replace("/", "_").replace(':', '_') + ".obj";

    try {
      (new File(PATH)).mkdir();
      OutputStream file = new FileOutputStream(filename);
      OutputStream buffer = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buffer);
      output.writeObject(revocationAuthorityParameters);
      output.close();
      System.out.println("Wrote RA parameters to " + filename);
    } catch (Exception e) {
      System.err.println("Could not write RA parameters to " + filename);
    }

    return km.storeRevocationAuthorityParameters(issuid, revocationAuthorityParameters);
  }

  @Override
  public CredentialSpecification getCredentialSpecification(URI credspec)
      throws KeyManagerException {
    return km.getCredentialSpecification(credspec);
  }

  @Override
  public boolean storeCredentialSpecification(URI uid,
      CredentialSpecification credentialSpecification) throws KeyManagerException {

    boolean ret = km.storeCredentialSpecification(uid, credentialSpecification);
    String filename = PATH + "cs-" + uid.toString().replace("/", "_").replace(':', '_') + ".xml";
    try {
      (new File(PATH)).mkdir();
      ObjectFactory of = new ObjectFactory();
      String xml = XmlUtils.toXml(of.createCredentialSpecification(credentialSpecification), false);
      PrintWriter out = new PrintWriter(filename);
      out.println(xml);
      out.close();
      System.out.println("Stored credential spec " + uid + " at " + filename);
    } catch (Exception e) {
      System.err.println("Cannot store credential spec " + uid + " at " + filename);
    }

    return ret;
  }

  @Override
  public boolean storeSystemParameters(SystemParameters systemParameters)
      throws KeyManagerException {
    BigInteger r = new BigInteger(30, new SecureRandom());
    String filename = PATH + "sp-" + r + ".obj";

    try {
      (new File(PATH)).mkdir();
      OutputStream file = new FileOutputStream(filename);
      OutputStream buffer = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buffer);
      output.writeObject(systemParameters);
      output.close();
      System.out.println("Wrote system parameters to " + filename);
    } catch (Exception e) {
      System.err.println("Could not write sysmtem parameters to " + filename);
    }

    return km.storeSystemParameters(systemParameters);
  }

  @Override
  public SystemParameters getSystemParameters() throws KeyManagerException {
    return km.getSystemParameters();
  }

  @Override
  public boolean hasSystemParameters() throws KeyManagerException {
    return km.hasSystemParameters();
  }

  @Override
  public RevocationInformation getLatestRevocationInformation(URI rapuid)
      throws KeyManagerException {
    return km.getLatestRevocationInformation(rapuid);
  }

  @Override
  public List<URI> listIssuerParameters() throws KeyManagerException {
    return km.listIssuerParameters();
  }
}

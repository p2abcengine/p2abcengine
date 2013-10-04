//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import eu.abc4trust.xml.Credential;

/**
 * This class serializes credentials using java object streams.
 * @author enr
 */
public class CredentialSerializerObject implements CredentialSerializer {

  @Override
  public byte[] serializeCredential(Credential cred) {
    try {
      ByteArrayOutputStream ser = new ByteArrayOutputStream();
      ser.write(magicHeader());
      
      ObjectOutputStream objectOutput = new ObjectOutputStream(ser);
      objectOutput.writeObject(cred);
      
      return ser.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Credential unserializeCredential(byte[] data) {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      int header = bais.read();
      if(header != magicHeader()) {
        throw new RuntimeException("Cannot unserialize this credential: header was " + header +
          " expected header " + magicHeader());
      }
      ObjectInputStream objectInput = new ObjectInputStream(bais);
      return (Credential) objectInput.readObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int magicHeader() {
    return 0x03;
  }
  
  @Override
  public Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri) {
    return unserializeCredential(data);
  }

}

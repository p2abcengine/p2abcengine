//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This class serializes credentials by their gzipped XML value.
 * @author enr
 */
public class CredentialSerializerGzipXml implements CredentialSerializer {

  @Override
  public byte[] serializeCredential(Credential cred) {
    try {
      ByteArrayOutputStream ser = new ByteArrayOutputStream();
      ser.write(magicHeader());
      
      GZIPOutputStream gs = new GZIPOutputStream(ser);

      
      ObjectFactory of = new ObjectFactory();
      ByteArrayOutputStream xml = XmlUtils.toXmlAsBaos(of.createCredential(cred), true);
      gs.write(xml.toByteArray());
      gs.close();
      
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
      GZIPInputStream gs = new GZIPInputStream(bais);
      return (Credential) XmlUtils.getObjectFromXML(gs, true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int magicHeader() {
    return 0x02;
  }
  
  @Override
  public Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri) {
    return unserializeCredential(data);
  }

}

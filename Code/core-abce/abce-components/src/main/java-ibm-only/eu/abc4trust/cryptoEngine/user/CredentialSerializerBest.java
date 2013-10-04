//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.abc4trust.xml.Credential;

/**
 * This class chooses the best credential serializer.
 * @author enr
 *
 */
public class CredentialSerializerBest implements CredentialSerializer {

  private List<CredentialSerializer> getClasses() {
    List<CredentialSerializer> cslist = new ArrayList<CredentialSerializer>();
    cslist.add(new CredentialSerializerXml());
    cslist.add(new CredentialSerializerGzipXml());
    cslist.add(new CredentialSerializerObject());
    cslist.add(new CredentialSerializerObjectGzip());
    return cslist;
  }
  
  @Override
  public byte[] serializeCredential(Credential cred) {
    byte[] best = null;
    for(CredentialSerializer cs: getClasses()) {
      byte[] ser = cs.serializeCredential(cred);
      if(best == null || best.length > ser.length) {
        best = ser;
      }
    }
    return best;
  }

  @Override
  public Credential unserializeCredential(byte[] data) {
    int magicHeader = data[0];
    for(CredentialSerializer cs: getClasses()) {
      if(magicHeader == cs.magicHeader()) {
        return cs.unserializeCredential(data);
      }
    }
    throw new RuntimeException("Unable to unserialize the credential");
  }

  @Override
  public int magicHeader() {
    return 0;
  }
  
  @Override
  public Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri) {
    return unserializeCredential(data);
  }

}

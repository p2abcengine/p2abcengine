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

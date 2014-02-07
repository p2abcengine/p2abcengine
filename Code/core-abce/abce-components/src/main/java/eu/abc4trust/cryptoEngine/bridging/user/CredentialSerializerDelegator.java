//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.cryptoEngine.bridging.user;

import java.net.URI;

import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.xml.Credential;

/**
 * This class just wraps different serializers for Idemix + UProve resp...
 * @see CredentialManagerImpl
 * @author hgk
 */
public class CredentialSerializerDelegator implements CredentialSerializer {
  
  // CredentialSerializerSmartcard 
  final CredentialSerializer idemix;
  final CredentialSerializer uprove;

  public CredentialSerializerDelegator(CredentialSerializer idemix, CredentialSerializer uprove) {
    this.idemix = idemix;
    this.uprove = uprove;
  }

  @Override
  public int magicHeader() {
    return 0x42;
  }

  @Override
  public byte[] serializeCredential(Credential cred) {
//    System.out.println("############# CredentialSerializerDelegator . serializeCredential - " + cred.getCredentialDescription().getIssuerParametersUID());
    if(cred.getCredentialDescription().getIssuerParametersUID().toString().contains("idemix")) {
//      System.out.println("- use IDEIX");
      return idemix.serializeCredential(cred);
    } else {
//      System.out.println("- use UPROVE");
      byte[] data = uprove.serializeCredential(cred);
//      System.out.println("- use UPROVE - data " + data[0] + " : " + data.length);
      return data;
    }
  }

  @Override
  public Credential unserializeCredential(byte[] data) {
//    System.out.println("############# CredentialSerializerDelegator . unserializeCredential - ..." + (data == null || data.length == 0 ? " null ? : " + data : data[0] + " : " + data.length));
    switch(data[0]) {
      case 4 :
//        System.out.println("- use UPROVE");
        return uprove.unserializeCredential(data);
      case 5 :
//        System.out.println("- use IDEIX");
        return idemix.unserializeCredential(data);
    }
    throw new IllegalStateException("CredentialSerializerDelegator - can only handle 4 Idemix specific and 5 uprove - CredentialSerializerObjectGzip  - was : " + data[0]);
  }

  @Override
  public Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri) {
//    System.out.println("#############  CredentialSerializerDelegator . unserializeCredential data,cred,sc " + (data == null || data.length == 0 ? " null ? : " + data : data[0] + " : " + data.length));
    switch(data[0]) {
      case 4 :
//        System.out.println("- use UPROVE");
        return uprove.unserializeCredential(data, credentialUri, smartcardUri);
      case 5 :
//        System.out.println("- use IDEIX");
        return idemix.unserializeCredential(data, credentialUri, smartcardUri);
    }
    throw new IllegalStateException("CredentialSerializerDelegator - unserializeCredential data,cred,sc - can only handle 4 Idemix specific and 5 uprove - CredentialSerializerObjectGzip - was : " + data[0]);
  }

}

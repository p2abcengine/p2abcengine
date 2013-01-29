//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;

import eu.abc4trust.xml.Credential;

public interface CredentialSerializer {

  /**
   * Serialize a credential to binary data.
   * Depending on the implementation, this function might compress the credential using
   * domain-specific knowledge.
   * @param cred
   * @return
   */
  byte[]     serializeCredential(Credential cred);
  
  /**
   * Recover a credential from binary data that was previously serialized with serializeCredential.
   * This function will only work properly if the CredentialSerializer has been configured in the
   * exact same way as during serialization time.
   * This function should be used in case the credentialUri and smartcardUri are serialized
   * by the specific instance of the credential serializer.
   * @param data
   * @return
   */
  Credential unserializeCredential(byte[] data);
  
  /**
   * Return a one-byte identifier (0-255) that identifies the current implementation of the
   * credential serializer.
   * @return
   */
  int magicHeader();

  /**
   * Recover a credential from binary data that was previously serialized with serializeCredential.
   * This function will only work properly if the CredentialSerializer has been configured in the
   * exact same way as during serialization time.
   * This function should be used in case the credentialUri and smartcardUri are NOT serialized
   * by the specific instance of the credential serializer (otherwise the extra parameters are
   * ignored).
   * @param data
   * @return
   */
  Credential unserializeCredential(byte[] data, URI credentialUri, URI smartcardUri);
}

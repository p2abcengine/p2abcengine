//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;

import eu.abc4trust.xml.PseudonymWithMetadata;

public interface PseudonymSerializer {

  /**
   * Serialize a PseudonymWithMetadata to binary data.
   * Depending on the implementation, this function might compress the pseudonym using
   * domain-specific knowledge.
   * @param cred
   * @return
   */
  byte[]     serializePseudonym(PseudonymWithMetadata cred);
  
  /**
   * Recover a PseudonymWithMetadata from binary data that was previously serialized with
   * serializePseudonym. This function will only work properly if the PseudonymSerializer has
   * been configured in the exact same way as during serialization time.
   * @param data
   * @return
   */
  PseudonymWithMetadata unserializePseudonym(byte[] data, URI pseudonymUID);
  
  /**
   * Return a one-byte identifier (0-255) that identifies the current implementation of the
   * pseudonym serializer.
   * @return
   */
  int magicHeader();
}

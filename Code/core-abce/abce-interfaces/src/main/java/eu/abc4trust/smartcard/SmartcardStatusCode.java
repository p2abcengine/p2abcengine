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

package eu.abc4trust.smartcard;

/**
 * Status code returned by the smartcard.
 */
public enum SmartcardStatusCode {
  
  OK(200),
  
  NOT_MODIFIED(304),
  
  BAD_REQUEST(400),
  UNAUTHORIZED(401),
  FORBIDDEN(403),
  NOT_FOUND(404),
  REQUEST_ENTITY_TOO_LARGE(413),
  REQUEST_URI_TOO_LONG(414),
  STALE_NONCE(424),
  
  NOT_INITIALIZED(500),
  INSUFFICIENT_STORAGE(507),
  ;
  
  private final int status;
  
  SmartcardStatusCode(int status) {
    this.status = status;
  }
  
  int getStatus() {
    return status;
  }
}

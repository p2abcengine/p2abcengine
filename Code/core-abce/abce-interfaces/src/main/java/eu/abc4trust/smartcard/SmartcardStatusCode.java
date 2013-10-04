//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

/**
 * Status code returned by the smartcard.
 * @author enr
 *
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

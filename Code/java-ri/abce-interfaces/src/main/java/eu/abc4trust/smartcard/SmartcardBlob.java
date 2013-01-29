//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.io.Serializable;

/**
 * Binary data stored on the smartcard
 * @author enr
 *
 */
public class SmartcardBlob implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3049033796676790320L;
  // If you update this structure, don't forget to change also in eu.abc4trust.smartcard.Utils;
  public byte[] blob;
  
  public int getLength() {
    return blob.length;
  }
}

//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.URI;
import java.util.Random;

public class CredentialOnSmartcard implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -7889698019565662678L;
  public final URI credentialUid;
  public final URI parametersUri;
  public final BigInteger v;
  public boolean issued;
  
  public CredentialOnSmartcard(URI credentialUid, URI parametersUri, BigInteger v) {
    this.credentialUid = credentialUid;
    this.parametersUri = parametersUri;
    this.v = v;
    this.issued = false;
  }
  
  public CredentialOnSmartcard(URI credentialUid, URI parametersUri, Random rand,
                               int sizeOfModulusInBits, int zkStatisticalSizeInBytes) {
    this.credentialUid = credentialUid;
    this.parametersUri = parametersUri;
    
    int credentialRandomizerSizeInBits =
        sizeOfCourseRandomizerInBits(sizeOfModulusInBits, zkStatisticalSizeInBytes);
    this.v = new BigInteger(credentialRandomizerSizeInBits, rand);
    this.issued = false;
  }
  
  static int sizeOfCourseRandomizerInBits(int sizeOfModulusInBits, int zkStatisticalHidingSizeBytes) {
    int lengthModulusBytes = (sizeOfModulusInBits + 7) / 8;
    int credentialRandomizerSizeBytes = lengthModulusBytes + zkStatisticalHidingSizeBytes;
    return credentialRandomizerSizeBytes * 8;
  }
}

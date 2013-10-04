//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ZkProofResponse {
  public BigInteger responseForDeviceSecret;
  //Associates credentialId with response for course randomizer
  public Map<URI, BigInteger> responseForCourses;
  
  public ZkProofResponse() {
    responseForCourses = new HashMap<URI, BigInteger>();
  }
}

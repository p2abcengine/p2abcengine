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

package eu.abc4trust.smartcard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Random;

public class ZkProofSystem {
  
  private static int sizeOfDeviceSecretRandomizerInBits(ZkProofSpecification spec) {
    int len = 8 * (spec.deviceSecretSizeBytes +
        spec.zkChallengeSizeBytes + spec.zkStatisticalHidingSizeBytes);
    return len;
  }
  
  public static ZkProofState firstMove(ZkProofSpecification spec, ZkProofWitness wit, Random rand) {	  
    ZkProofState zkps = new ZkProofState();
    zkps.witnesses = wit;
    
    // Choose randomness
    zkps.randomnessForDeviceSecret = new BigInteger(sizeOfDeviceSecretRandomizerInBits(spec), rand);
    zkps.randomnessForCourses = new HashMap<URI, BigInteger>();
    for(URI courseId: spec.credentialBases.keySet()) {
      int rvalueSize = sizeOfDeviceSecretRandomizerInBits(spec);
      BigInteger r = new BigInteger(rvalueSize, rand);
      zkps.randomnessForCourses.put(courseId, r);
    }
    
    // Choose nonce commitments
    //zkps.nonceOpening = Utils.newNonceCommitment(spec.zkNonceSizeBytes,
    //                                             spec.zkNonceOpeningSizeBytes, rand);

//    zkps.nonce = new byte[spec.zkNonceSizeBytes];
//    rand.nextBytes(zkps.nonce);
    
    // Compute commitments
    zkps.commitment = new ZkProofCommitment();
    for(URI courseId: spec.credentialBases.keySet()) {
      BigInteger rx = zkps.randomnessForDeviceSecret;
      SmartcardParameters gp = spec.credentialBases.get(courseId);
      if(gp.getBaseForCredentialSecretOrNull() != null){
	      BigInteger R0 = gp.getBaseForDeviceSecret();
	      BigInteger S = gp.getBaseForCredentialSecretOrNull();
	      BigInteger n = gp.getModulus();
	      BigInteger rv = zkps.randomnessForCourses.get(courseId);
	      
	      // A = R0^rx * S^rv (mod n)
	      BigInteger A = R0.modPow(rx, n).multiply(S.modPow(rv, n)).mod(n);
	      zkps.commitment.commitmentForCreds.put(courseId, A);
      }else{
        BigInteger R0 = gp.getBaseForDeviceSecret();
        BigInteger n = gp.getModulus();
        
        // A = R0^rx * S^rv (mod n)
        BigInteger A = R0.modPow(rx, n);
        zkps.commitment.commitmentForCreds.put(courseId, A);
      }
    }
    for(URI scope: spec.scopeExclusivePseudonymValues.keySet()) {
      BigInteger base =
          Utils.baseForScopeExclusivePseudonym(scope, spec.parametersForPseudonyms.p,
                                               spec.parametersForPseudonyms.subgroupOrder);
      BigInteger rx = zkps.randomnessForDeviceSecret;
      BigInteger p = spec.parametersForPseudonyms.p;
      
      // A = base^rx (mod p)
      BigInteger A = base.modPow(rx, p);
      zkps.commitment.commitmentForScopeExclusivePseudonyms.put(scope, A);
    }
    if (spec.devicePublicKey != null) {
      BigInteger p = spec.parametersForPseudonyms.p;
      BigInteger g = spec.parametersForPseudonyms.g;
      BigInteger rx = zkps.randomnessForDeviceSecret;
      
      // A = g^rx (mod p)
      BigInteger A = g.modPow(rx, p);
      zkps.commitment.commitmentForDevicePublicKey = A;
    } else {
      zkps.commitment.commitmentForDevicePublicKey = null;
    }

    zkps.commitment.spec = spec;
    return zkps;
  }
  
  public static ZkProofResponse secondMove(ZkProofState state, BigInteger challenge, Random rand) {
    ZkProofResponse zkpr = new ZkProofResponse();
    
    {
      BigInteger rx = state.randomnessForDeviceSecret;
      BigInteger x = state.witnesses.deviceSecret;
      // zx = rx - c * x      
      zkpr.responseForDeviceSecret = rx.subtract(challenge.multiply(x));
    }
    
    for(URI courseId: state.randomnessForCourses.keySet()) {
      BigInteger v = state.witnesses.courseRandomizer.get(courseId);
      BigInteger rv = state.randomnessForCourses.get(courseId);
      // zv = rv - c * v
      BigInteger zv = rv.subtract(challenge.multiply(v));
      zkpr.responseForCourses.put(courseId, zv);
    }
    
    return zkpr;
  }
  
  public static boolean checkProof(ZkProofCommitment com, ZkProofResponse res,
                                   BigInteger c) {
	  ZkProofSpecification spec = com.spec;  	  
    
    // Check size of responses
    if (res.responseForDeviceSecret.bitLength() > 1 + sizeOfDeviceSecretRandomizerInBits(spec)) {
      return false;
    }
    for(URI courseId: spec.credentialBases.keySet()) {
      BigInteger zv = res.responseForCourses.get(courseId);
      int rvalueSize = sizeOfDeviceSecretRandomizerInBits(spec);
      if (zv.bitLength() > 1 + rvalueSize) {
        return false;
      }
    }
    
    // Check commitments
    
    for (URI courseId : spec.credentialBases.keySet()) {
      BigInteger A = com.commitmentForCreds.get(courseId); // g1^(kx)*g2^(kv)
      BigInteger T = spec.credFragment.get(courseId); //

      SmartcardParameters credBases = spec.credentialBases.get(courseId);
      if (credBases.getBaseForCredentialSecretOrNull() != null) {
        BigInteger R0 = credBases.getBaseForDeviceSecret();
        BigInteger S = credBases.getBaseForCredentialSecretOrNull();
        BigInteger n = credBases.getModulus();

        BigInteger zx = res.responseForDeviceSecret;
        BigInteger zv = res.responseForCourses.get(courseId);

        // A =? T^c * R0^zx * S^zv (mod n)
        BigInteger AA = T.modPow(c, n).multiply(R0.modPow(zx, n).multiply(S.modPow(zv, n))).mod(n);
        if (!AA.equals(A)) {
          return false;
        }
      } else {
        // Do nothing
      }
    }
    for(URI scope: spec.scopeExclusivePseudonymValues.keySet()) {
      BigInteger zx = res.responseForDeviceSecret;
      BigInteger p = spec.parametersForPseudonyms.p;
      BigInteger subGroupOrder = spec.parametersForPseudonyms.subgroupOrder;
      BigInteger base = Utils.baseForScopeExclusivePseudonym(scope, p, subGroupOrder);
      
      BigInteger A = com.commitmentForScopeExclusivePseudonyms.get(scope);
      BigInteger T = spec.scopeExclusivePseudonymValues.get(scope);
      
      // A =? T^c * base^zx (mod p)
      BigInteger AA = T.modPow(c, p).multiply(base.modPow(zx, p)).mod(p);
      if ( ! AA.equals(A)) {
        return false;
      }
    }
    
    if (spec.devicePublicKey != null) {
      BigInteger p = spec.parametersForPseudonyms.p;
      BigInteger g = spec.parametersForPseudonyms.g;
      BigInteger T = spec.devicePublicKey; //g^x mod p
      BigInteger zx = res.responseForDeviceSecret; // kx - x*c mod q
      BigInteger A = com.commitmentForDevicePublicKey; //g^kx mod p
      // A = T^c * g^zx (mod p) = g^(x*c) * g^(kx-x*c) mod p = g^kx 
      BigInteger AA = T.modPow(c, p).multiply(g.modPow(zx, p)).mod(p);
      if ( ! AA.equals(A)) {
        return false;
      }
    }
    
    return true;
  }
  
}


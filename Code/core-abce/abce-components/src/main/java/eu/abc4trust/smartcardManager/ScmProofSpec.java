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

package eu.abc4trust.smartcardManager;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.zurich.idmx.smartcard.IdemixProofSpec;

class ScmProofSpec implements IdemixProofSpec {
  
  final private Map<URI, Set<URI>> credList;
  final private Set<URI> pkList;
  final private Map<URI, Set<URI>> sepList;
  
  @Override
  public void addCredentialProof(URI smartcardUri, URI credUri) {
    if(! credList.containsKey(smartcardUri)) {
      credList.put(smartcardUri, new HashSet<URI>());
    }
    credList.get(smartcardUri).add(credUri);
  }

  @Override
  public void addPublicKeyProof(URI smartcardUri) {
    pkList.add(smartcardUri);
  }

  @Override
  public void addScopeExclusivePseudonymProof(URI smartcardUri, URI scope) {
    if(! sepList.containsKey(smartcardUri)) {
      sepList.put(smartcardUri, new HashSet<URI>());
    }
    sepList.get(smartcardUri).add(scope);
  }
  
  // Package private
  ScmProofSpec() {
    credList = new HashMap<URI, Set<URI>>();
    pkList = new HashSet<URI>();
    sepList = new HashMap<URI, Set<URI>>();
  }
  
  Set<URI> computeListOfInvolvedSmartcards() {
    Set<URI> ret = new HashSet<URI>();
    for(URI sc: credList.keySet()) {
      ret.add(sc);
    }
    for(URI sc: pkList) {
      ret.add(sc);
    }
    for(URI sc: sepList.keySet()) {
      ret.add(sc);
    }
    return ret;
  }
  
  Set<URI> getListOfInvolvedCredentials(URI smartcardUri) {
    Set<URI> ret =  credList.get(smartcardUri);
    if (ret == null) {
      return new HashSet<URI>();
    } else {
      return ret;
    }
  }
  
  boolean isProofOfPublicKey(URI smartcardUri) {
    return pkList.contains(smartcardUri);
  }
  
  Set<URI> getListOfScopeExclusivePseudonyms(URI smartcardUri) {
    Set<URI> ret = sepList.get(smartcardUri);
    if (ret == null) {
      return new HashSet<URI>();
    } else {
      return ret;
    }
  }
}

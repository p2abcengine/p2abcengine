//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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

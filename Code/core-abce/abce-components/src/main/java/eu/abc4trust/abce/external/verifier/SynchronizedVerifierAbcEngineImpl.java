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

package eu.abc4trust.abce.external.verifier;

import java.net.URI;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class SynchronizedVerifierAbcEngineImpl implements VerifierAbcEngine {

  private final VerifierAbcEngine engine;

  @Inject
  public SynchronizedVerifierAbcEngineImpl(VerifierAbcEngine engine) {
    this.engine = engine;
  }

  @Override
  public synchronized PresentationTokenDescription verifyTokenAgainstPolicy(
      PresentationPolicyAlternatives p, PresentationToken t, boolean store)
      throws TokenVerificationException, CryptoEngineException {

    return this.engine.verifyTokenAgainstPolicy(p, t, store);
  }

  @Override
  public synchronized PresentationToken getToken(URI tokenUid) {
    return this.engine.getToken(tokenUid);
  }

  @Override
  public synchronized boolean deleteToken(URI tokenUid) {
    return this.engine.deleteToken(tokenUid);
  }

  @Override
  public synchronized RevocationInformation getLatestRevocationInformation(URI revParamsUid)
      throws CryptoEngineException {
    return this.engine.getLatestRevocationInformation(revParamsUid);
  }

  @Override
  public synchronized VerifierParameters createVerifierParameters(SystemParameters sp)
      throws CryptoEngineException {
    return this.engine.createVerifierParameters(sp);
  }

  @Override
  public synchronized byte[] createNonce() {
    // TODO Auto-generated method stub
    return this.engine.createNonce();
  }

  public PresentationTokenDescription verifyToken(PresentationToken t, VerifierParameters vp, 
		boolean store) throws TokenVerificationException, CryptoEngineException {
	return this.engine.verifyToken(t, vp, store);
  }

  @Override
  public boolean verifyTokenDescriptionAgainstPolicyAlternatives(
		PresentationPolicyAlternatives p, PresentationTokenDescription ptd){
	return this.engine.verifyTokenDescriptionAgainstPolicyAlternatives(p, ptd);
  }

}

//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.abce.internal.user.evidenceGeneration;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.exceptions.TokenIssuanceException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class EvidenceGenerationOrchestrationImpl implements EvidenceGenerationOrchestration {

  private final CryptoEngineUser cryptoEngine;
  private final ContextGenerator contextGen;
  private final Logger logger;
  private final KeyManager keyManager;

  @Inject
  public EvidenceGenerationOrchestrationImpl(CryptoEngineUser cryptoEngine, ContextGenerator cg,
      Logger logger, KeyManager keyManager) {
    this.cryptoEngine = cryptoEngine;
    this.contextGen = cg;
    this.logger = logger;
    this.keyManager = keyManager;
//    System.out.println("Hello from EvidenceGenerationOrchestrationImpl()");
  }

  @Override
  public IssuanceMessage createIssuanceToken(String username, IssuanceMessage im, IssuanceTokenDescription itd,
      List<URI> creduids, List<URI> pseudonyms, List<Attribute> atts)
      throws CryptoEngineException {

    return this.cryptoEngine.createIssuanceToken(username, im, itd, creduids, pseudonyms, atts);
  }

  @Override
  public PresentationToken createPresentationToken(String username, PresentationTokenDescription td,
      VerifierParameters vp, List<URI> creds, List<URI> pseudonyms) throws CryptoEngineException {
    return this.cryptoEngine.createPresentationToken(username, td, vp, creds, pseudonyms);
  }

  @Override
  public PseudonymWithMetadata createPseudonym(String username, URI pseudonymUri, String scope, boolean exclusive,
      URI secretReference) throws CryptoEngineException {
    // TODO(enr): Not all crypto engines will support the creation of pseudonyms
    return this.cryptoEngine.createPseudonym(username, pseudonymUri, scope, exclusive, secretReference);
  }

  @Override
  public Credential updateNonRevocationEvidence(String username, Credential cred, URI raparsuid,
      List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {
    return this.cryptoEngine.updateNonRevocationEvidence(username, cred, raparsuid, revokedatts);
  }

  @Override
  public Credential updateNonRevocationEvidence(String username, Credential cred, URI raparsuid,
      List<URI> revokedatts, URI revinfouid) throws CryptoEngineException,
      CredentialWasRevokedException {
    return this.cryptoEngine.updateNonRevocationEvidence(username, cred, raparsuid, revokedatts, revinfouid);
  }

  @Override
  public Secret createSecret(String username) {
    URI newSdUri = this.contextGen.getUniqueContext(URI.create("abc4trust://secret"));
    SecretDescription newSd = new SecretDescription();
    newSd.setDeviceBoundSecret(false);
    newSd.setSecretUID(newSdUri);
    FriendlyDescription fd = new FriendlyDescription();
    fd.setLang("en");
    fd.setValue("New computer-based secret " + newSdUri);
    newSd.getFriendlySecretDescription().add(fd);
    newSd.setMetadata(null);
    this.logger.info("Creating a new secret " + newSdUri);
    Secret s = new Secret();
    SmartcardSystemParameters smartCardSysParams = this.getSystemParameters();
    s.setSystemParameters(smartCardSysParams);
    int deviceSecretSizeBytes = s.getSystemParameters().getDeviceSecretSizeBytes();
    s.setSecretKey(this.contextGen.getRandomNumber(deviceSecretSizeBytes * 8));
    s.setSecretDescription(newSd);
    return s;
  }

  private SmartcardSystemParameters getSystemParameters() {
    SmartcardSystemParameters smartCardSysParams;
    try {
      smartCardSysParams =
          SystemParametersUtil.createSmartcardSystemParameters(this.keyManager
              .getSystemParameters());
    } catch (KeyManagerException ex) {
      throw new RuntimeException(ex);
    }
    return smartCardSysParams;
  }

  @Override
  public IssuancePolicy extractIssuancePolicy(IssuanceMessage im) {
    return cryptoEngine.extractIssuancePolicy(im);
  }
}

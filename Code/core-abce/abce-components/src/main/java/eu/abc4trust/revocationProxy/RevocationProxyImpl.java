//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.revocationProxy;

import com.google.inject.Inject;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationMessage;

public class RevocationProxyImpl implements RevocationProxy {
  private RevocationProxyCommunicationStrategy communicationStrategy = null;

  @Inject
  public RevocationProxyImpl(RevocationProxyCommunicationStrategy communicationStrategy) {
    this.communicationStrategy = communicationStrategy;
  }

  @Override
  public RevocationMessage processRevocationMessage(RevocationMessage m,
      RevocationAuthorityParameters revpars) throws Exception {

    CryptoParams cryptoParams = null;
    RevocationMessageFacade incomingMessageFacade = new RevocationMessageFacade(m);

    if (incomingMessageFacade.revocationHandleRequested()) {
      cryptoParams = this.requestRevocationHandle(m, revpars.getNonRevocationEvidenceReference());
    } else if (incomingMessageFacade.revocationInformationRequested()) {
      cryptoParams = this.requestRevocationInformation(m, revpars.getRevocationInfoReference());
    } else if (incomingMessageFacade.getCurrentRevocationInformation()) {
      cryptoParams = this.getCurrentRevocationInformation(m, revpars.getRevocationInfoReference());
    } else if (incomingMessageFacade.updateRevocationEvidence()) {
      cryptoParams =
          this.revocationEvidenceUpdate(m, revpars.getNonRevocationEvidenceUpdateReference());
    }

    RevocationMessageFacade outgoingMessageFacade = new RevocationMessageFacade();
    outgoingMessageFacade.setContext(incomingMessageFacade.getContext());
    outgoingMessageFacade.setRevocationAuthorityParametersUID(incomingMessageFacade
        .getRevocationAuthorityParametersUID());
    outgoingMessageFacade.setCryptoParams(cryptoParams);


    // RevocationMessage rm = new RevocationMessage();
    // rm.setContext(m.getContext());
    // rm.setRevocationAuthorityParametersUID(m.getRevocationAuthorityParametersUID());
    // rm.setCryptoParams(cryptoParams);

    return outgoingMessageFacade.getDelegateeValue();
  }

  private CryptoParams requestRevocationHandle(RevocationMessage m,
      Reference nonRevocationEvidenceReference) throws Exception {
    return this.communicationStrategy.requestRevocationHandle(m, nonRevocationEvidenceReference);
  }

  private CryptoParams requestRevocationInformation(RevocationMessage m,
      Reference revocationInformationReference) throws Exception {
    return this.communicationStrategy.requestRevocationInformation(m,
        revocationInformationReference);
  }

  private CryptoParams getCurrentRevocationInformation(RevocationMessage m,
      Reference revocationInfoReference) throws Exception {
    return this.communicationStrategy.getCurrentRevocationInformation(m, revocationInfoReference);
  }

  private CryptoParams revocationEvidenceUpdate(RevocationMessage m,
      Reference nonRevocationEvidenceUpdateReference) throws Exception {
    return this.communicationStrategy.revocationEvidenceUpdate(m,
        nonRevocationEvidenceUpdateReference);
  }

}

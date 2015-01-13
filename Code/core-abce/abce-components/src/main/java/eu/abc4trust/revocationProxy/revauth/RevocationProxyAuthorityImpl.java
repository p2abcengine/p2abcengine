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

package eu.abc4trust.revocationProxy.revauth;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.internal.revocation.RevocationUtility;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.revocationProxy.RevocationMessageType;
import eu.abc4trust.revocationProxy.RevocationProxyException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationHandle;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;

public class RevocationProxyAuthorityImpl implements RevocationProxyAuthority {

  private final RevocationAbcEngine engine;

  @Inject
  public RevocationProxyAuthorityImpl(RevocationAbcEngine engine) {
    this.engine = engine;
  }

  @Override
  public RevocationMessageAndBoolean processRevocationMessage(RevocationMessage m)
      throws RevocationProxyException, ConfigurationException {

    CryptoParams cryptoParams = null;

    RevocationMessage revocationResponse = null;
    RevocationMessageFacade incomingMessageFacade = new RevocationMessageFacade(m);
    URI revAuthParamsUid = incomingMessageFacade.getRevocationAuthorityParametersUID();
    // List<Object> cps = m.getCryptoParams().getAny();
    List<Object> cps = incomingMessageFacade.getAdditionalObjectList();

    if (incomingMessageFacade.revocationHandleRequested()) {
      // List<Attribute> attributes = revocationMessageFacade.getAttributeList();
      //
      // revocationResponse =
      // requestRevocationHandle(revpars.getParametersUID(),
      // revpars.getNonRevocationEvidenceReference());
//      JAXBElement<Attribute> jaxb = (JAXBElement<Attribute>) cps.get(0);
      List<Attribute> attributes = new LinkedList<Attribute>();
//      attributes.add(jaxb.getValue());
      attributes.add((Attribute) cps.get(0));
      cryptoParams = this.requestRevocationHandle(revAuthParamsUid, attributes);
    } else if (incomingMessageFacade.revocationInformationRequested()) {
      URI revInfoUid = RevocationUtility.unserializeRevocationInfoUid((Element) cps.get(0));
      cryptoParams = this.requestRevocationInformation(revAuthParamsUid, revInfoUid);
    } else if (incomingMessageFacade.getCurrentRevocationInformation()) {
      cryptoParams = this.getCurrentRevocationInformation(revAuthParamsUid);
    } else if (incomingMessageFacade.updateRevocationEvidence()) {
      Integer epoch = RevocationUtility.unserializeEpoch((Element) cps.get(0));
      cryptoParams = this.updateRevocationEvidence(revAuthParamsUid, epoch);
    }

    RevocationMessageFacade outgoingMessageFacade = new RevocationMessageFacade();
    outgoingMessageFacade.setContext(incomingMessageFacade.getContext());
    outgoingMessageFacade.setRevocationAuthorityParametersUID(incomingMessageFacade
        .getRevocationAuthorityParametersUID());
    outgoingMessageFacade.setCryptoParams(cryptoParams);


    // RevocationMessage rm = new RevocationMessage();
    // rm.setContext(m.getContext());
    // rm.setRevocationAuthorityParametersUID(revAuthParamsUid);
    // rm.setCryptoParams(cryptoParams);

    RevocationMessageAndBoolean revMessage = new RevocationMessageAndBoolean();
    revMessage.lastMessage = true;
    revMessage.revmess = outgoingMessageFacade.getDelegateeValue();
    return revMessage;
  }



  // private RevocationMessage requestRevocationHandle(URI revocationAuthorityId,
  // List<Attribute> attributes) throws RevocationProxyException {
  // RevocationHandle revocationHandle = null;
  // try {
  // // TODO make this properly - what may be contained in Reference???
  // // URI nonRevocationEvidenceId = nonRevocationEvidenceReference.getReferences().get(0);
  // revocationHandle = engine.generateNonRevocationEvidence(revocationAuthorityId, attributes);
  // // newRevocationHandle(revocationAuthorityId, nonRevocationEvidenceId);
  // } catch (Exception e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade();
  //
  // revocationMessageFacade.setRevocationHandle(revocationHandle);
  //
  // return revocationMessageFacade.getDelegateeValue();
  // }


  protected CryptoParams requestRevocationHandle(URI revParamsUid, List<Attribute> attributes)
      throws RevocationProxyException {
    NonRevocationEvidence revInfo;
    try {
      revInfo = this.engine.generateNonRevocationEvidence(revParamsUid, attributes);
    } catch (CryptoEngineException ex) {
      throw new RevocationProxyException(ex);
    }
    CryptoParams cryptoParams = new CryptoParams();
    cryptoParams.getContent().add(new ObjectFactory().createNonRevocationEvidence(revInfo));
    return cryptoParams;
  }

  protected CryptoParams requestRevocationInformation(URI revParamsUid, URI revInfoUid)
      throws RevocationProxyException {
    RevocationInformation revInfo;
    try {
      revInfo = this.engine.getRevocationInformation(revParamsUid, revInfoUid);
    } catch (CryptoEngineException ex) {
      throw new RevocationProxyException(ex);
    }
    CryptoParams cryptoParams = new CryptoParams();
    cryptoParams.getContent().add(new ObjectFactory().createRevocationInformation(revInfo));
    return cryptoParams;
  }

  protected CryptoParams getCurrentRevocationInformation(URI revParamsUid)
      throws RevocationProxyException {
    RevocationInformation revInfo;
    try {
      revInfo = this.engine.updateRevocationInformation(revParamsUid);
    } catch (CryptoEngineException ex) {
      throw new RevocationProxyException(ex);
    }
    CryptoParams cryptoParams = new CryptoParams();
    cryptoParams.getContent().add(new ObjectFactory().createRevocationInformation(revInfo));
    return cryptoParams;
  }

  protected CryptoParams updateRevocationEvidence(URI revParamsUid, int epoch)
      throws RevocationProxyException {
    NonRevocationEvidenceUpdate r;
    try {
      r = this.engine.generateNonRevocationEvidenceUpdate(revParamsUid, epoch);
    } catch (CryptoEngineException ex) {
      throw new RevocationProxyException(ex);
    }
    CryptoParams cryptoParams = new CryptoParams();
    cryptoParams.getContent().add(r);
    return cryptoParams;
  }


}

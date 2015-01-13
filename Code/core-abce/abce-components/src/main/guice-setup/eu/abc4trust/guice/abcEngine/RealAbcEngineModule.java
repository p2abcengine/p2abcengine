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

package eu.abc4trust.guice.abcEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.inspector.InspectorAbcEngineImpl;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngineImpl;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngineImpl;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngineImpl;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngineImpl;
import eu.abc4trust.abce.internal.issuer.issuanceManager.IssuanceManagerIssuer;
import eu.abc4trust.abce.internal.issuer.issuanceManager.IssuanceManagerIssuerImpl;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestration;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestrationImpl;
import eu.abc4trust.abce.internal.user.issuanceManager.IssuanceManagerUser;
import eu.abc4trust.abce.internal.user.issuanceManager.IssuanceManagerUserImpl;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcherImpl;
import eu.abc4trust.abce.internal.verifier.evidenceVerification.EvidenceVerificationOrchestrationVerifier;
import eu.abc4trust.abce.internal.verifier.evidenceVerification.EvidenceVerificationOrchestrationVerifierImpl;
import eu.abc4trust.abce.internal.verifier.policyTokenMatcher.PolicyTokenMatcherVerifier;
import eu.abc4trust.abce.internal.verifier.policyTokenMatcher.PolicyTokenMatcherVerifierImpl;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.CredentialSerializerGzipXml;
import eu.abc4trust.revocationProxy.GenericWebServiceCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.RevocationProxyCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxyImpl;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthorityImpl;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.util.AttributeConverterImpl;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.ContextGeneratorRandom;

public class RealAbcEngineModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bind(VerifierAbcEngine.class).to(VerifierAbcEngineImpl.class).in(Singleton.class);
    this.bind(UserAbcEngine.class).to(UserAbcEngineImpl.class).in(Singleton.class);
    this.bind(RevocationAbcEngine.class).to(RevocationAbcEngineImpl.class).in(Singleton.class);
    this.bind(IssuerAbcEngine.class).to(IssuerAbcEngineImpl.class).in(Singleton.class);
    this.bind(InspectorAbcEngine.class).to(InspectorAbcEngineImpl.class).in(Singleton.class);
    this.bind(IssuanceManagerIssuer.class).to(IssuanceManagerIssuerImpl.class).in(Singleton.class);
    this.bind(EvidenceGenerationOrchestration.class).to(EvidenceGenerationOrchestrationImpl.class).in(Singleton.class);
    this.bind(EvidenceVerificationOrchestrationVerifier.class).to(EvidenceVerificationOrchestrationVerifierImpl.class).in(Singleton.class);
    this.bind(IssuanceManagerUser.class).to(IssuanceManagerUserImpl.class).in(Singleton.class);
    this.bind(PolicyTokenMatcherVerifier.class).to(PolicyTokenMatcherVerifierImpl.class).in(Singleton.class);
    this.bind(PolicyCredentialMatcher.class).to(PolicyCredentialMatcherImpl.class).in(Singleton.class);
    this.bind(RevocationProxy.class).to(RevocationProxyImpl.class).in(Singleton.class);
    this.bind(RevocationProxyAuthority.class).to(RevocationProxyAuthorityImpl.class).in(Singleton.class);
    
    //this.bind(RevocationProxyCommunicationStrategy.class).to(WebServiceCommunicationStrategy.class).in(Singleton.class);
    this.bind(RevocationProxyCommunicationStrategy.class).to(GenericWebServiceCommunicationStrategy.class).in(Singleton.class);
    
   
    this.bind(ContextGenerator.class).to(ContextGeneratorRandom.class).in(Singleton.class);
    this.bind(AttributeConverter.class).to(AttributeConverterImpl.class).in(Singleton.class);
    // revocation...
    this.bind(RevocationProxyAuthority.class).to(RevocationProxyAuthorityImpl.class).in(Singleton.class);
    //this.bind(RevocationProxyCommunicationStrategy.class).to(WebServiceCommunicationStrategy.class).in(Singleton.class);
    this.bind(RevocationProxyCommunicationStrategy.class).to(GenericWebServiceCommunicationStrategy.class).in(Singleton.class);
    this.bind(CredentialSerializer.class).to(CredentialSerializerGzipXml.class).in(Singleton.class);
  }

}

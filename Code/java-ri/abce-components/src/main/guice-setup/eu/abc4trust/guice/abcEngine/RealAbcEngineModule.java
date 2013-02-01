//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.guice.abcEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.ibm.zurich.idmx.smartcard.IdemixSmartcardManager;

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
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.PersistentFileTokenStorageIssuer;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuerImpl;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.abce.internal.revocation.IssuerRevocation;
import eu.abc4trust.abce.internal.revocation.RevocationProof;
import eu.abc4trust.abce.internal.revocation.UserRevocation;
import eu.abc4trust.abce.internal.revocation.VerifierRevocation;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerImpl;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCache;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheImpl;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentCredentialStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentSecretStorage;
import eu.abc4trust.abce.internal.user.credentialManager.SecretStorage;
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
import eu.abc4trust.abce.internal.verifier.tokenManager.PersistentFileTokenStorage;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenManagerImpl;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenStorage;
import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.cryptoEngine.uprove.user.CryptoEngineContext;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.user.ReloadTokensWebServiceCommunicationStrategy;
import eu.abc4trust.cryptoEngine.uprove.user.UProveIssuanceHandling;
import eu.abc4trust.cryptoEngine.uprove.user.UProveIssuanceHandlingImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerImpl;
import eu.abc4trust.keyManager.KeyStorage;
import eu.abc4trust.keyManager.PersistentKeyStorage;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.RevocationProxyCommunicationStrategy;
import eu.abc4trust.revocationProxy.RevocationProxyImpl;
import eu.abc4trust.revocationProxy.WebServiceCommunicationStrategy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthorityImpl;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcardManager.AbcSmartcardManager;
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
    this.bind(TokenManagerVerifier.class).to(TokenManagerImpl.class).in(Singleton.class);
    this.bind(TokenManagerIssuer.class).to(TokenManagerIssuerImpl.class).in(Singleton.class);
    this.bind(KeyManager.class).to(KeyManagerImpl.class).in(Singleton.class);
    this.bind(RevocationProxyCommunicationStrategy.class).to(WebServiceCommunicationStrategy.class).in(Singleton.class);
    this.bind(KeyStorage.class).to(PersistentKeyStorage.class).in(Singleton.class);
    this.bind(CredentialManager.class).to(CredentialManagerImpl.class).in(Singleton.class);
    this.bind(
            eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class)
            .to(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManagerImpl.class)
            .in(Singleton.class);
    this.bind(IssuerRevocation.class).in(Singleton.class);
    this.bind(UserRevocation.class).in(Singleton.class);
    this.bind(VerifierRevocation.class).in(Singleton.class);
    this.bind(RevocationProof.class).in(Singleton.class);
    this.bind(IdemixSmartcardManager.class).to(AbcSmartcardManager.class).in(Singleton.class);
    this.bind(AbcSmartcardManager.class).in(Singleton.class);
    this.bind(
      eu.abc4trust.abce.internal.issuer.credentialManager.CredentialStorage.class)
      .to(eu.abc4trust.abce.internal.issuer.credentialManager.PersistentCredentialStorage.class)
      .in(Singleton.class);
    this.bind(
      eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager.class)
      .to(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerImpl.class).in(Singleton.class);
    this.bind(
      eu.abc4trust.abce.internal.inspector.credentialManager.CredentialStorage.class)
      .to(eu.abc4trust.abce.internal.inspector.credentialManager.PersistentCredentialStorage.class)
      .in(Singleton.class);
    this.bind(
      eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager.class)
      .to(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManagerImpl.class)
      .in(Singleton.class);
    this.bind(
      eu.abc4trust.abce.internal.revocation.credentialManager.CredentialStorage.class)
      .to(eu.abc4trust.abce.internal.revocation.credentialManager.PersistentCredentialStorage.class)
      .in(Singleton.class);
    this.bind(TokenStorage.class).to(PersistentFileTokenStorage.class).in(Singleton.class);
    this.bind(TokenStorageIssuer.class).to(PersistentFileTokenStorageIssuer.class).in(Singleton.class);
    this.bind(CredentialStorage.class)
    .to(PersistentCredentialStorage.class).in(Singleton.class);
    this.bind(SecretStorage.class)
    .to(PersistentSecretStorage.class).in(Singleton.class);
    this.bind(IdemixSmartcardManager.class)
    .to(AbcSmartcardManager.class).in(Singleton.class);
    this.bind(ImageCache.class).to(ImageCacheImpl.class)
    .in(Singleton.class);
    this.bind(ImageCacheStorage.class)
    .to(PersistentImageCacheStorage.class)
    .in(Singleton.class);
    this.bind(CardStorage.class).in(Singleton.class);
    this.bind(ContextGenerator.class).to(ContextGeneratorRandom.class).in(Singleton.class);
    // revocation...
    this.bind(RevocationProxyAuthority.class).to(RevocationProxyAuthorityImpl.class).in(Singleton.class);
    this.bind(RevocationProxyCommunicationStrategy.class).to(WebServiceCommunicationStrategy.class).in(Singleton.class);
    // uprove 
    this.bind(ReloadTokensCommunicationStrategy.class).to(ReloadTokensWebServiceCommunicationStrategy.class).in(Singleton.class);
    this.bind(UProveIssuanceHandling.class).to(UProveIssuanceHandlingImpl.class).in(Singleton.class);
    this.bind(CryptoEngineContext.class).in(Singleton.class);
  }

}

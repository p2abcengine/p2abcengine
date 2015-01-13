//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.cryptoEngine.user;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyCredentialDescription;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.TestCryptoParams;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class MockCryptoEngineUser implements CryptoEngineUser {

    private final KeyManager keyManager;
    private final CredentialManager credManager;
    private final Map<URI, List<Attribute>> attributeCache;
    private final Map<URI, IssuanceTokenDescription> tokenCache;
    private final Map<URI, List<URI>> credCache;
    private final ContextGenerator contextGen;

    @Inject
    public MockCryptoEngineUser(KeyManager keyManager, CredentialManager credManager,
            ContextGenerator contextGen) {
        // WARNING: Due to circular dependencies you MUST NOT dereference credManager
        // in this constructor.
        // (Guice does some magic to support circular dependencies).

        this.keyManager = keyManager;
        this.credManager = credManager;
        this.contextGen = contextGen;
        this.attributeCache = new HashMap<URI, List<Attribute>>();
        this.tokenCache = new HashMap<URI, IssuanceTokenDescription>();
        this.credCache = new HashMap<URI, List<URI>>();
        System.out.println("*** Using mock Crypto Engine for User *** DO NOT USE IN PRODUCTION ***");
    }

    @Override
    public IssuanceMessage createIssuanceToken(String username, IssuanceMessage im,
                                               IssuanceTokenDescription itd,
                                               List<URI> creduids,
             List<URI> pseudonyms, List<Attribute> atts) {
      URI ctxt = im.getContext();
        this.attributeCache.put(ctxt, atts);
        this.tokenCache.put(ctxt, itd);
        this.credCache.put(ctxt, creduids);

        ObjectFactory of = new ObjectFactory();
        IssuanceToken ret = of.createIssuanceToken();
        ret.setCryptoEvidence(this.getBogusCryptoEvidence());
        ret.setVersion("1.0");
        ret.setIssuanceTokenDescription(itd);

        this.updatePseudonyms(ret.getIssuanceTokenDescription().getPresentationTokenDescription());

        IssuanceMessage newIm = new ObjectFactory().createIssuanceMessage();
        newIm.setContext(ctxt);
        newIm.getContent().add(new ObjectFactory().createIssuanceToken(ret));
        return newIm;
    }

    private CryptoParams getBogusCryptoEvidence() {
        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        TestCryptoParams cryptoParams = of.createTestCryptoParams();
        cryptoParams.getData().add("I am MockCryptoEngineUser, and I approve of this message.");
        cryptoEvidence.getContent().add(of.createTestCryptoParams(cryptoParams));
        return cryptoEvidence;
    }

    @Override
    public PresentationToken createPresentationToken(String username, PresentationTokenDescription td, VerifierParameters vp,
            List<URI> creds, List<URI> pseudonyms) {
        ObjectFactory of = new ObjectFactory();

        PresentationToken ret = of.createPresentationToken();
        ret.setPresentationTokenDescription(td);
        ret.setVersion("1.0");
        ret.setCryptoEvidence(this.getBogusCryptoEvidence());

        this.updatePseudonyms(ret.getPresentationTokenDescription());

        return ret;
    }

    private void updatePseudonyms(PresentationTokenDescription presentationTokenDescription) {
        for (PseudonymInToken pit: presentationTokenDescription.getPseudonym()) {
            if(pit.getPseudonymValue() == null) {
                byte[] samplePseudonymValue = new byte[2];
                samplePseudonymValue[0] = 42;
                samplePseudonymValue[1] = 43;
                pit.setPseudonymValue(samplePseudonymValue);
            }
        }
    }

    @Override
    public IssuMsgOrCredDesc issuanceProtocolStep(String username, IssuanceMessage m)
            throws CryptoEngineException {

        ObjectFactory of = new ObjectFactory();
        Credential cred = of.createCredential();
        URI context = m.getContext();

        CredentialDescription credDesc =
                (CredentialDescription) XmlUtils.unwrap(m.getContent(),
                        CredentialDescription.class);
        IssuanceTokenDescription tokenDesc = this.tokenCache.get(context);
        CredentialSpecification credSpec;
        MyCredentialDescription myCredDesc;
        try {
          credSpec = this.keyManager.getCredentialSpecification(
              tokenDesc.getCredentialTemplate().getCredentialSpecUID());
          myCredDesc = new MyCredentialDescription(credDesc, credSpec, this.keyManager);
        } catch (KeyManagerException ex) {
          throw new CryptoEngineException(ex);
        }
        MyCredentialSpecification myCredSpec = new MyCredentialSpecification(credSpec);

        Map<URI, Credential> credentialsFromAlias;
        try {
            credentialsFromAlias = this.fetchCredentials(username, tokenDesc,
                    this.credCache.get(context));
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }

        myCredDesc.addAttributes(this.attributeCache.get(context), true);
        myCredDesc.populateFromTemplate(tokenDesc.getCredentialTemplate(), myCredSpec,
                credentialsFromAlias, this.contextGen, keyManager);
        myCredSpec.validateOrThrow(myCredDesc.getCredentialDesc());

        cred.setCredentialDescription(myCredDesc.getCredentialDesc());
        cred.setCryptoParams(this.getBogusCryptoEvidence());
        // TODO(enr): We don't set NonRevocationEvidenceUID
        URI credentialUri;
        try {
            credentialUri = this.credManager.storeCredential(username, cred);
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }
        cred.getCredentialDescription().setCredentialUID(credentialUri);
        IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc();
        ret.cd = cred.getCredentialDescription();
        ret.im = null;

        return ret;
    }

    private Map<URI, Credential> fetchCredentials(String username, IssuanceTokenDescription tokenDesc,
            List<URI> credentialAssignment) throws CredentialManagerException {
        Map<URI, Credential> ret = new HashMap<URI, Credential>();

        Iterator<URI> credIterator = credentialAssignment.iterator();
        for (CredentialInToken cd : tokenDesc.getPresentationTokenDescription()
                .getCredential()) {
            URI credentialAlias = cd.getAlias();
            URI credentialUri = credIterator.next();
            Credential c = this.credManager.getCredential(username, credentialUri);
            ret.put(credentialAlias, c);
        }
        return ret;
    }

    @Override
    public Credential updateNonRevocationEvidence(String username, Credential cred, URI raparsuid,
            List<URI> revokedatts) {
        return cred;
    }

    @Override
    public Credential updateNonRevocationEvidence(String username, Credential cred, URI raparsuid,
            List<URI> revokedatts, URI revinfouid) {
        return cred;
    }

    @Override
    public PseudonymWithMetadata createPseudonym(String username, URI pseudonymUri, String scope, boolean exclusive,
            URI secretReference) {
        PseudonymWithMetadata pwm = new PseudonymWithMetadata();
        Pseudonym p = new Pseudonym();
        pwm.setPseudonym(p);
        p.setExclusive(exclusive);
        p.setScope(scope.toString());
        p.setSecretReference(secretReference);
        p.setPseudonymUID(pseudonymUri);
        return pwm;
    }

    @Override
    public boolean isRevoked(String username, Credential cred) throws CryptoEngineException {     	
         throw new UnsupportedOperationException();                   
    }
    
    @Override
    public IssuancePolicy extractIssuancePolicy(IssuanceMessage issuanceMessage) {
      return (IssuancePolicy) XmlUtils.unwrap(issuanceMessage.getContent().get(0), IssuancePolicy.class);
    }

}

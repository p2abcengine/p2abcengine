//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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
    public IssuanceToken createIssuanceToken(IssuanceTokenDescription itd, List<URI> creduids,
            List<Attribute> atts, List<URI> pseudonyms, URI ctxt) {
        this.attributeCache.put(ctxt, atts);
        this.tokenCache.put(ctxt, itd);
        this.credCache.put(ctxt, creduids);

        ObjectFactory of = new ObjectFactory();
        IssuanceToken ret = of.createIssuanceToken();
        ret.setCryptoEvidence(this.getBogusCryptoEvidence());
        ret.setVersion("1.0");
        ret.setIssuanceTokenDescription(itd);

        this.updatePseudonyms(ret.getIssuanceTokenDescription().getPresentationTokenDescription());

        return ret;
    }

    private CryptoParams getBogusCryptoEvidence() {
        ObjectFactory of = new ObjectFactory();
        CryptoParams cryptoEvidence = of.createCryptoParams();
        TestCryptoParams cryptoParams = of.createTestCryptoParams();
        cryptoParams.getData().add("I am MockCryptoEngineUser, and I approve of this message.");
        cryptoEvidence.getAny().add(of.createTestCryptoParams(cryptoParams));
        return cryptoEvidence;
    }

    @Override
    public PresentationToken createPresentationToken(PresentationTokenDescription td,
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
    public IssuMsgOrCredDesc issuanceProtocolStep(IssuanceMessage m)
            throws CryptoEngineException {

        ObjectFactory of = new ObjectFactory();
        Credential cred = of.createCredential();
        URI context = m.getContext();

        CredentialDescription credDesc =
                (CredentialDescription) XmlUtils.unwrap(m.getAny(),
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
            credentialsFromAlias = this.fetchCredentials(tokenDesc,
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
            credentialUri = this.credManager.storeCredential(cred);
        } catch (CredentialManagerException ex) {
            throw new CryptoEngineException(ex);
        }
        cred.getCredentialDescription().setCredentialUID(credentialUri);
        IssuMsgOrCredDesc ret = new IssuMsgOrCredDesc();
        ret.cd = cred.getCredentialDescription();
        ret.im = null;

        return ret;
    }

    private Map<URI, Credential> fetchCredentials(IssuanceTokenDescription tokenDesc,
            List<URI> credentialAssignment) throws CredentialManagerException {
        Map<URI, Credential> ret = new HashMap<URI, Credential>();

        Iterator<URI> credIterator = credentialAssignment.iterator();
        for (CredentialInToken cd : tokenDesc.getPresentationTokenDescription()
                .getCredential()) {
            URI credentialAlias = cd.getAlias();
            URI credentialUri = credIterator.next();
            Credential c = this.credManager.getCredential(credentialUri);
            ret.put(credentialAlias, c);
        }
        return ret;
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred, URI raparsuid,
            List<URI> revokedatts) {
        return cred;
    }

    @Override
    public Credential updateNonRevocationEvidence(Credential cred, URI raparsuid,
            List<URI> revokedatts, URI revinfouid) {
        return cred;
    }

    @Override
    public PseudonymWithMetadata createPseudonym(URI pseudonymUri, String scope, boolean exclusive,
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
    public Secret createSecret() {
        Secret s = new Secret();
        s.setSecretDescription(new SecretDescription());
        s.getSecretDescription().setSecretUID(URI.create("secret-uid"));
        return s;
    }

}

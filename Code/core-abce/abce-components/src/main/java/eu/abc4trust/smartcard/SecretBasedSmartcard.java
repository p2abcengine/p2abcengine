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

package eu.abc4trust.smartcard;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.ibm.zurich.idmix.abc4trust.facades.SmartcardParametersFacade;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.user.CredentialSerializer;
import eu.abc4trust.cryptoEngine.user.PseudonymSerializer;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.SmartcardSystemParameters;

/**
 * This class adapts a (non-device-bound) Secret to a basicSmartcard that can be used
 * with the smartcard manager.
 * This class uses the credential manager and key manager to locate credentials.
 * 
 * This class assumes that the credential UID and the "credential UID on smartcard" is the
 * same value.
 * 
 * @author enr
 *
 */
public class SecretBasedSmartcard implements BasicSmartcard {

	// Cache of all currently non-issued credentials, associate credential with issuer URI
	private final Map<URI, URI> cachedCredentials;

	// State of smartcard
	private final Random rand;
	private boolean factoryInit;
	private SystemParameters params;
	private BigInteger deviceSecret;

	private URI deviceUri;
	private ZkProofState zkProofState;

	private final CredentialManager credManager;
	private final KeyManager keyManager;
	private final String username;

	public Secret getSecret() {
		Secret s = new Secret();
		s.setSecretKey(this.deviceSecret);
		s.setSystemParameters(this.params.getSmartcardPublicKey());
		{
			SecretDescription sd = new SecretDescription();
			s.setSecretDescription(sd);

			sd.setDeviceBoundSecret(false);
			FriendlyDescription fd = new FriendlyDescription();
			fd.setLang("en");
			fd.setValue("Secret on disk " + this.deviceUri);
			sd.getFriendlySecretDescription().add(fd);
			sd.setMetadata(null);
			sd.setSecretUID(this.deviceUri);
		}
		return s;
	}

	public SecretBasedSmartcard(String username, CredentialManager credManager, KeyManager keyManager) {
		this(username, new SecureRandom(), credManager, keyManager);
	}

	public SecretBasedSmartcard(String username, Random random, CredentialManager credManager, KeyManager keyManager) {
		this.factoryInit = false;
		this.rand = random;
		this.cachedCredentials = new HashMap<URI, URI>();
		this.credManager = credManager;
		this.keyManager = keyManager;
		this.username = username;
	}

	@Override
	public SmartcardStatusCode deleteCredential(int pin, URI credentialUri) {
		URI del = this.cachedCredentials.remove(credentialUri);
		if (del == null) {
			return SmartcardStatusCode.NOT_FOUND;
		}
		return SmartcardStatusCode.OK;
	}

	@Override
	public boolean wasInit() {
		return this.factoryInit;
	}

	public boolean initNew(eu.abc4trust.xml.SystemParameters sysParams, URI deviceUri) {
		SmartcardSystemParameters smartcardSysParams = SystemParametersUtil.createSmartcardSystemParameters(sysParams);
		return initNew(smartcardSysParams, deviceUri);
	}

	public boolean initNew(SmartcardSystemParameters smartcardSysParams, URI deviceUri) {
		if (this.factoryInit) {
			return false;
		}
		this.params = new SystemParameters(smartcardSysParams);
		this.deviceSecret = new BigInteger(smartcardSysParams.getDeviceSecretSizeBytes() * 8, this.rand);
		this.deviceUri = deviceUri;
		this.factoryInit = true;

		this.zkProofState = null;

		return true;
	}

	public boolean initFromSecret(Secret s) {
		if (this.factoryInit) {
			return false;
		}
		this.params = new SystemParameters(s.getSystemParameters());
		this.deviceSecret = s.getSecretKey();
		this.deviceUri = s.getSecretDescription().getSecretUID();
		this.factoryInit = true;

		this.zkProofState = null;

		return true;
	}

	@Override
	public URI getDeviceURI(int pin) {
		return this.deviceUri;
	}

	@Override
	public SystemParameters getSystemParameters(int pin) {
		return this.params;
	}

	@Override
	public BigInteger computeScopeExclusivePseudonym(int pin, URI scope) {
		// hash(scope) ^ deviceSecret (mod p)
		BigInteger base = Utils.baseForScopeExclusivePseudonym(scope, this.params.p, this.params.subgroupOrder);
		return base.modPow(this.deviceSecret, this.params.p);
	}


	@Override
	public BigInteger computeDevicePublicKey(int pin) {
		// g^deviceSecret (mod p)
		return this.params.g.modPow(this.deviceSecret, this.params.p);
	}

	@Override
	public BigInteger computeCredentialFragment(int pin, URI credentialUri) {
		TrustedIssuerParameters tos = this.getIssuerParameterOfCredential(credentialUri);
		SmartcardParameters credBases = tos.groupParams;
		BigInteger base1 = credBases.getBaseForDeviceSecret();
		BigInteger base2  = credBases.getBaseForCredentialSecretOrNull();
		BigInteger p  = credBases.getModulus();
		if (base2 != null) {
			BigInteger v = BigInteger.ZERO;
			return base1.modPow(this.deviceSecret, p).multiply(base2.modPow(v, p)).mod(p);
		} else {
			return base1.modPow(this.deviceSecret, p);
		}
	}

	private ZkProofSpecification getZkProofSpec(int pin, Set<URI> courseIds,
			Set<URI> scopeExclusivePseudonyms, boolean includeDevicePublicKeyProof) {

		ZkProofSpecification zkps = new ZkProofSpecification(this.params);

		zkps.parametersForPseudonyms = this.params;
		zkps.credentialBases = new HashMap<URI, SmartcardParameters>();
		zkps.credFragment = new HashMap<URI, BigInteger>();
		for(URI courseId: courseIds) {
			TrustedIssuerParameters tos = this.getIssuerParameterOfCredential(courseId);
			zkps.credentialBases.put(courseId, tos.groupParams);
			BigInteger credFragment = this.computeCredentialFragment(pin, courseId);
			zkps.credFragment.put(courseId, credFragment);
		}
		zkps.scopeExclusivePseudonymValues = new HashMap<URI, BigInteger>();
		for (URI scope: scopeExclusivePseudonyms) {
			BigInteger psValue = this.computeScopeExclusivePseudonym(pin, scope);
			zkps.scopeExclusivePseudonymValues.put(scope, psValue);
		}
		if (includeDevicePublicKeyProof) {
			zkps.devicePublicKey = this.computeDevicePublicKey(pin);
		} else {
			zkps.devicePublicKey = null;
		}

		return zkps;
	}

	private ZkProofWitness getZkProofWitness(Set<URI> credentialUris,
			boolean includeDevicePublicKeyProof) {

		ZkProofWitness wit = new ZkProofWitness();
		wit.deviceSecret = this.deviceSecret;
		wit.courseRandomizer = new HashMap<URI, BigInteger>();
		for(URI credId: credentialUris) {
			wit.courseRandomizer.put(credId, BigInteger.ZERO);
		}
		return wit;
	}

	@Override
	public TrustedIssuerParameters getIssuerParametersOfCredential(int pin, URI credentialUri) {
		return this.getIssuerParameterOfCredential(credentialUri);
	}

	private TrustedIssuerParameters getIssuerParameterOfCredential(URI credUri) {
		URI issuerUri = this.getIssuerUriOfCredential(username, credUri);
		if (issuerUri != null) {
			return this.getIssuerParameters(issuerUri);
		}
		return null;
	}

	private TrustedIssuerParameters getIssuerParameters(URI issuer) {
		try {
			IssuerParameters ip = this.keyManager.getIssuerParameters(issuer);
			eu.abc4trust.xml.SystemParameters sp = this.keyManager.getSystemParameters();
			SmartcardParametersFacade spf = new SmartcardParametersFacade(sp, ip);
			SmartcardParameters credBases = spf.getSmartcardParameters();
			TrustedIssuerParameters tip = new TrustedIssuerParameters(issuer, credBases);
			return tip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public SmartcardStatusCode allocateCredential(int pin, URI credentialUri, URI issuerParameters) {
		this.cachedCredentials.put(credentialUri, issuerParameters);    
		return SmartcardStatusCode.OK;
	}

	@Override
	public boolean credentialExists(int pin, URI credentialUri) {
		return this.getIssuerUriOfCredential(username, credentialUri) != null;
	}

	private URI getIssuerUriOfCredential(String username, URI credUri) {
		try {
			if (this.cachedCredentials.containsKey(credUri)) {
				return this.cachedCredentials.get(credUri);
			} else {
				CredentialDescription cd = this.credManager.getCredentialDescription(username, credUri);

				if ((cd != null) && cd.getSecretReference().equals(this.deviceUri)) {
					return cd.getIssuerParametersUID();
				} else {
					// On a different smartcard
					return null;
				}
			}
		} catch (CredentialManagerException e) {
			System.err.println(e);
			return null;
		}
	}

	@Override
	public ZkProofCommitment prepareZkProof(int pin, Set<URI> courseIds,
			Set<URI> scopeExclusivePseudonyms, boolean includeDevicePublicKeyProof) {

		for(URI courseId: courseIds) {

			TrustedIssuerParameters tip = this.getIssuerParameterOfCredential(courseId);

			if (tip == null) {
				throw new RuntimeException("Could not find issuer.");
			}
		}

		ZkProofSpecification spec = this.getZkProofSpec(pin, courseIds, scopeExclusivePseudonyms,
				includeDevicePublicKeyProof);
		if (spec == null) {
			return null;
		}
		ZkProofWitness wit = this.getZkProofWitness(courseIds, includeDevicePublicKeyProof);
		this.zkProofState = ZkProofSystem.firstMove(spec, wit, this.rand);

		return this.zkProofState.commitment;
	}

	@Override
	public ZkProofResponse finalizeZkProof(int pin, BigInteger challenge, Set<URI> courseIds,
			Set<URI> scopeExclusivePseudonyms) {

		if(this.zkProofState == null) {
			return null;
		}

		//    byte[] realNonce = Utils.checkCommitmentsAndGetNonce(nonceCommitments, zkNonceOpenings,
		//      zkProofState.commitment.nonceCommitment);
		//    if (realNonce == null) {
		//      return null;
		//    }

		ZkProofResponse response = ZkProofSystem.secondMove(this.zkProofState, challenge, this.rand);

		this.zkProofState = null;
		return response;
	}

	@Override
	public PseudonymWithMetadata getPseudonym(int pin, URI pseudonymId, PseudonymSerializer serializer) {
		try {
			return this.credManager.getPseudonym(username, pseudonymId);
		} catch (CredentialManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Credential getCredential(int pin, URI credentialId, CredentialSerializer serializer) {
		try {
			return this.credManager.getCredential(username, credentialId);
		} catch (CredentialManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public short getDeviceID(int pin) {
		return 0;
	}
}
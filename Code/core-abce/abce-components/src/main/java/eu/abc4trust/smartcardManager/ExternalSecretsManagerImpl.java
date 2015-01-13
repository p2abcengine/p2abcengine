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

package eu.abc4trust.smartcardManager;

import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.GroupParameters;
import eu.abc4trust.smartcard.HardwareSmartcard;
import eu.abc4trust.smartcard.InsufficientStorageException;
import eu.abc4trust.smartcard.SecretBasedSmartcard;
import eu.abc4trust.smartcard.SmartcardParameters;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SystemParameters;
import eu.abc4trust.smartcard.Utils;
import eu.abc4trust.smartcard.ZkProofCommitment;
import eu.abc4trust.smartcard.ZkProofResponse;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Secret;

public class ExternalSecretsManagerImpl implements ExternalSecretsManager{

	private final CredentialManager credManager;
    private final KeyManager keyManager;
    private final CardStorage storage;
    
    @Inject
    public ExternalSecretsManagerImpl(CredentialManager credManager, KeyManager keyManager, CardStorage storage) {
        this.keyManager = keyManager;
        this.credManager = credManager;
        this.storage = storage;
    }
	
    private void addSecret(String username, Secret s) {
        // If secret is a real smartcard
        //  - Add one of Pascal's Smartcard interface
        //  - Where do we get the PIN from?
        // If it's a "software" smartcard:
        //  - Restore state, add a new Software smartcard

        if (s.getSecretDescription().isDeviceBoundSecret()) {
            throw new UnsupportedOperationException("addSecret not implemented for device bound secrets");
        } else {
            SecretBasedSmartcard sc = new SecretBasedSmartcard(username, this.credManager, this.keyManager);
            sc.initFromSecret(s);
            this.storage.addSmartcard(sc, 0);
        }
    }
    
    private void tryToLoadSecretBasedSmartcard(String username, URI smartcardUri) {
        try {
            Secret s = this.credManager.getSecret(username, smartcardUri);
            if (s != null) {
                this.addSecret(username, s);
            }
        } catch(SecretNotInStorageException ex) {
            return;
        } catch(CredentialManagerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private BasicSmartcard getSmartcard(String username, URI smartcardUri) {    	
        BasicSmartcard sc = this.storage.getSmartcard(smartcardUri);
        if (sc == null) {
            this.tryToLoadSecretBasedSmartcard(username, smartcardUri);
            sc = this.storage.getSmartcard(smartcardUri);
        }
        return sc;
    }
    
	@Override
	public void allocateCredential(String username, URI deviceUid, URI newCredentialUri,
			URI issuerUri, boolean overwrite) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        if(this.doesCredentialExists(username, deviceUid, newCredentialUri)) {
            if(overwrite) {
                SmartcardStatusCode ret = s.deleteCredential(pin, newCredentialUri);
                if (ret != SmartcardStatusCode.OK) {
                    throw new RuntimeException("Credential " + newCredentialUri + " already exists on card "
                            + deviceUid + ". Could not delete it, status code: " + ret.ordinal() + " " +
                            ret.name());
                }
            } else {
                throw new RuntimeException("Credential " + newCredentialUri + " already exists on card "
                        + deviceUid + " (set overwrite=true to overwrite)");
            }
        }
        SmartcardStatusCode ret = s.allocateCredential(pin, newCredentialUri, issuerUri);
        if (ret != SmartcardStatusCode.OK) {
            throw new InsufficientStorageException("Credential " + newCredentialUri + " could not be created on card "
                    + deviceUid + ". Status code: " + ret.ordinal() + " " + ret.name());
        }
	}

	@Override
	public boolean isDeviceLoaded(String username, URI deviceUid) {
		BasicSmartcard sc = this.getSmartcard(username, deviceUid);
        return sc != null;
	}

	@Override
	public boolean doesCredentialExists(String username, URI deviceUid, URI credentialUri) {
		System.out.println("sc uri: " + deviceUid + "\n credUri: " + credentialUri);    	
        BasicSmartcard s = this.getSmartcard(username, deviceUid);                
        if(s == null) {
            return false;
        }        
        Integer pin = this.storage.getPin(deviceUid);        
        return s.credentialExists(pin, credentialUri);
	}

	@Override
	public BigInteger getPublicKeyBase(String username, URI deviceUid) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.getSystemParameters(pin).g;
	}

	@Override
	public BigInteger getPseudonymModulus(String username, URI deviceUid) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.getSystemParameters(pin).p;
	}

	@Override
	public BigInteger getPseudonymSubgroupOrder(String username, URI deviceUid) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.getSystemParameters(pin).subgroupOrder;
	}

	@Override
	public BigInteger getBaseForDeviceSecret(String username, URI deviceUid, URI credentialUri) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        SmartcardParameters groupParams = s.getIssuerParametersOfCredential(pin, credentialUri).groupParams;
        return groupParams.getBaseForDeviceSecret();
	}

	@Override
	public BigInteger getBaseForCredentialSecret(String username, URI deviceUid,
			URI credentialUri) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        SmartcardParameters groupParams = s.getIssuerParametersOfCredential(pin, credentialUri).groupParams;
        BigInteger ret = groupParams.getBaseForCredentialSecretOrNull();
        if(ret == null) {
          return BigInteger.ONE;
        } else {
          return ret;
        }
	}

	@Override
	public BigInteger getModulus(String username, URI deviceUid, URI credentialUri) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.getIssuerParametersOfCredential(pin, credentialUri).groupParams.getModulus();
	}

	@Override
	public int getChallengeSizeBytes(String username, URI deviceUid) {
		Set<Integer> challengeSizes = new HashSet<Integer>();
        for(Entry<URI, BasicSmartcard> s: this.storage.getSmartcards().entrySet()) {
            URI key = s.getKey();
            Integer pin = this.storage.getPin(key);
            BasicSmartcard value = s.getValue();
            SystemParameters systemParameters = value.getSystemParameters(pin);
            challengeSizes.add(systemParameters.zkChallengeSizeBytes);
        }
        if(challengeSizes.size() == 1) {
            return challengeSizes.iterator().next();
        } else if (challengeSizes.size() == 0) {
            throw new RuntimeException("No cards loaded");
        } else {
            throw new RuntimeException("Incompatible cards: challenge size is different");
        }
	}

	@Override
	public int getRandomizerSizeBytes(String username, URI deviceUid) {
		Set<Integer> randSizes = new HashSet<Integer>();
        for(Entry<URI, BasicSmartcard> s: this.storage.getSmartcards().entrySet()) {
            Integer pin = this.storage.getPin(s.getKey());
            SystemParameters systemParameters = s.getValue().getSystemParameters(pin);
            int randValue = systemParameters.zkChallengeSizeBytes+systemParameters.zkStatisticalHidingSizeBytes+
            		systemParameters.deviceSecretSizeBytes;
            randSizes.add(randValue);
        }
        if(randSizes.size() == 1) {
        	return randSizes.iterator().next();
        } else if (randSizes.size() == 0) {
            throw new RuntimeException("No cards loaded");
        } else {
            throw new RuntimeException("Incompatible cards: statistical hiding size is different");
        }		
	}

	@Override
	public int getAttributeSizeBytes(String username, URI deviceUid) {
		Set<Integer> attrSizes = new HashSet<Integer>();
        for(Entry<URI, BasicSmartcard> s: this.storage.getSmartcards().entrySet()) {
            Integer pin = this.storage.getPin(s.getKey());
            SystemParameters systemParameters = s.getValue().getSystemParameters(pin);
            int randValue = systemParameters.deviceSecretSizeBytes;
            attrSizes.add(randValue);
        }
        if(attrSizes.size() == 1) {
        	return attrSizes.iterator().next();
        } else if (attrSizes.size() == 0) {
            throw new RuntimeException("No cards loaded");
        } else {
            throw new RuntimeException("Incompatible cards: statistical hiding size is different");
        }		
	}

	@Override
	public BigInteger getDevicePublicKey(String username, URI deviceUid) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.computeDevicePublicKey(pin);
	}

	@Override
	public BigInteger getCredentialPublicKey(String username, URI deviceUid, URI credentialUri) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.computeCredentialFragment(pin, credentialUri);
	}

	@Override
	public BigInteger getScopeExclusivePseudonym(String username, URI deviceUid, URI scope) {
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + deviceUid);
        }
        Integer pin = this.storage.getPin(deviceUid);
        return s.computeScopeExclusivePseudonym(pin, scope);
	}

	@Override
	public BigInteger getBaseForScopeExclusivePseudonym(String username, URI scope,
			BigInteger modulus, BigInteger subgroupOrder) {
		return Utils.baseForScopeExclusivePseudonym(scope, modulus, subgroupOrder);
	}

	@Override
	public BigInteger getBaseForScopeExclusivePseudonym(String username, URI deviceUid, URI scope) {
		return this.getBaseForScopeExclusivePseudonym(username, scope, this.getPseudonymModulus(username, deviceUid),
                this.getPseudonymSubgroupOrder(username, deviceUid));
	}

	@Override
	public DeviceProofCommitment getPresentationCommitment(
			DeviceProofSpecification spec) {
		if(spec instanceof DeviceProofSpecificationImpl) {
        	TimingsLogger.logTiming("ExternalSecretsManager.getPresentationCommitment", true);
        	DeviceProofSpecificationImpl proofSpec = (DeviceProofSpecificationImpl) spec;

        	DeviceProofCommitmentImpl com = new DeviceProofCommitmentImpl(proofSpec);

            Set<URI> involvedSmartcards = proofSpec.computeListOfInvolvedSmartcards();
            for(URI sc: involvedSmartcards) {
                BasicSmartcard s = this.getSmartcard(((DeviceProofSpecificationImpl) spec).getUsername(), sc);
                if(s == null) {
                    throw new RuntimeException("Unknown smartcard: " + sc);
                }
                Integer pin = this.storage.getPin(sc);

                Set<URI> creds = proofSpec.getListOfInvolvedCredentials(sc);
                Set<URI> sep = proofSpec.getListOfScopeExclusivePseudonyms(sc);
                boolean pkProof = proofSpec.isProofOfPublicKey(sc);

                ZkProofCommitment zkCom = s.prepareZkProof(pin, creds, sep, pkProof);
                if (zkCom == null) {
                    throw new RuntimeException("Cannot do proof with smartcard " + sc);
                }
                for(Entry<URI, BigInteger> e: zkCom.commitmentForCreds.entrySet()) {
                    com.setCommitmentForCredential(sc, e.getKey(), e.getValue());
                }
                for(Entry<URI, BigInteger> e: zkCom.commitmentForScopeExclusivePseudonyms.entrySet()) {
                    com.setCommitmentForScopeExclusivePseudonym(sc, e.getKey(), e.getValue());
                }
                if (zkCom.commitmentForDevicePublicKey != null) {
                    com.setCommitmentForPublicKey(sc, zkCom.commitmentForDevicePublicKey);
                }
            }
            
        	TimingsLogger.logTiming("ExternalSecretsManager.getPresentationCommitment", false);
            return com;
        } else {
            throw new RuntimeException("Incompatible proof spec");
        }
	}

	@Override
	public DeviceProofResponse getPresentationResponse(
			DeviceProofCommitment com, BigInteger challenge) {
		if(com instanceof DeviceProofCommitmentImpl) {
            DeviceProofCommitmentImpl commitment = (DeviceProofCommitmentImpl) com;
            DeviceProofSpecificationImpl spec = commitment.getProofSpec();

            DeviceProofResponseImpl resp = new DeviceProofResponseImpl();

            Set<URI> involvedSmartcards = spec.computeListOfInvolvedSmartcards();
            for(URI sc: involvedSmartcards) {
                BasicSmartcard s = this.getSmartcard(((DeviceProofCommitmentImpl) com).getProofSpec().getUsername(), sc);
                if(s == null) {
                    throw new RuntimeException("Unknown smartcard: " + sc);
                }
                Integer pin = this.storage.getPin(sc);               

                ZkProofResponse zkResp = s.finalizeZkProof(pin, challenge, spec.getListOfInvolvedCredentials(sc), spec.getListOfScopeExclusivePseudonyms(sc));
                if (zkResp == null) {
                    throw new RuntimeException("Cannot do proof with smartcard " + sc);
                }
                for(Entry<URI, BigInteger> e: zkResp.responseForCourses.entrySet()) {
                    resp.setResponseForCredentialRandomizer(sc, e.getKey(), e.getValue());
                }
                if (zkResp.responseForDeviceSecret != null) {
                    resp.setResponseForDeviceSecretKey(sc, zkResp.responseForDeviceSecret);
                }
            }

            return resp;
        } else {
            throw new RuntimeException("Incompatible proof spec");
        }
	}

	@Override
	public DeviceProofSpecification newProofSpec(String username) {
		return new DeviceProofSpecificationImpl(username);
	}

	@Override
	   public void associateIssuer(String username, URI deviceUid, URI credIdOnDevice, 
			   URI issuerUriOnDevice) {	   
		BasicSmartcard s = this.getSmartcard(username, deviceUid);
		if(s instanceof HardwareSmartcard){
			// Nothing to do for real smart cards
		}else{
			System.out.println("AssociateIssuer called.. What to do now?");
		}
	}

}

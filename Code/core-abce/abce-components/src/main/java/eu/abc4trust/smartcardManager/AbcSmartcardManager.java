//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.smartcardManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;
import com.ibm.zurich.idmx.smartcard.IdemixProofCommitment;
import com.ibm.zurich.idmx.smartcard.IdemixProofResponse;
import com.ibm.zurich.idmx.smartcard.IdemixProofSpec;
import com.ibm.zurich.idmx.smartcard.IdemixSmartcardManager;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.credentialManager.SecretNotInStorageException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.CredentialBases;
import eu.abc4trust.smartcard.GroupParameters;
import eu.abc4trust.smartcard.InsufficientStorageException;
import eu.abc4trust.smartcard.SecretBasedSmartcard;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.smartcard.SystemParameters;
import eu.abc4trust.smartcard.Utils;
import eu.abc4trust.smartcard.ZkProofCommitment;
import eu.abc4trust.smartcard.ZkProofResponse;
import eu.abc4trust.util.TimingsLogger;
import eu.abc4trust.xml.Secret;

public class AbcSmartcardManager implements IdemixSmartcardManager {

    private final CredentialManager credManager;
    private final KeyManager keyManager;
    private final CardStorage storage;

    @Inject
    public AbcSmartcardManager(CredentialManager credManager, KeyManager keyManager, CardStorage storage) {
        this.keyManager = keyManager;
        this.credManager = credManager;
        this.storage = storage;
    }

    /**
     * Add smartcard based on a secret
     * @param s
     * @return
     */
    private void addSecret(Secret s) {
        // If secret is a real smartcard
        //  - Add one of Pascal's Smartcard interface
        //  - Where do we get the PIN from?
        // If it's a "software" smartcard:
        //  - Restore state, add a new Software smartcard

        if (s.getSecretDescription().isDeviceBoundSecret()) {
            throw new UnsupportedOperationException("addSecret not implemented for device bound secrets");
        } else {
            SecretBasedSmartcard sc = new SecretBasedSmartcard(this.credManager, this.keyManager);
            sc.initFromSecret(s);
            this.storage.addSmartcard(sc, 0);
        }
    }

    private void tryToLoadSecretBasedSmartcard(URI smartcardUri) {
        try {
            Secret s = this.credManager.getSecret(smartcardUri);
            if (s != null) {
                this.addSecret(s);
            }
        } catch(SecretNotInStorageException ex) {
            return;
        } catch(CredentialManagerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private BasicSmartcard getSmartcard(URI smartcardUri) {
        BasicSmartcard sc = this.storage.getSmartcard(smartcardUri);
        if (sc == null) {
            this.tryToLoadSecretBasedSmartcard(smartcardUri);
            sc = this.storage.getSmartcard(smartcardUri);
        }
        return sc;
    }

    @Deprecated
    /**
     * Add a smartcard to the pool of smartcards managed by this class.
     * Please add the card via the storage.
     * @param sc
     * @param pin The PIN of the card.
     * @return
     */
    public boolean addSmartcard(BasicSmartcard sc, int pin) {
        URI cardUri = sc.getDeviceURI(pin);
        if (null == cardUri) {
            return false;
        }
        this.storage.addSmartcard(sc, Integer.valueOf(pin));

        return true;
    }

    /**
     * Allocate a credential on this smartcard.
     * The Crypto engine needs to call this method before issuing a new credential.
     * @param smartcardUri
     * @param newCredUri Name of the new credential
     * @param issuerUri Name of the issuer of the new credential
     * @param overwrite Set to true to delete a previous credential with the same Uri if
     * it already exists on the card. You will need to do this if the issuance of a
     * credential with attendance check failed (otherwise the card will not let you redo the proof)
     */
    public void allocateCredential(URI smartcardUri, URI newCredUri,
            URI issuerUri, boolean overwrite) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        if(this.credentialExists(smartcardUri, newCredUri)) {
            if(overwrite) {
                SmartcardStatusCode ret = s.deleteCredential(pin, newCredUri);
                if (ret != SmartcardStatusCode.OK) {
                    throw new RuntimeException("Credential " + newCredUri + " already exists on card "
                            + smartcardUri + ". Could not delete it, status code: " + ret.ordinal() + " " +
                            ret.name());
                }
            } else {
                throw new RuntimeException("Credential " + newCredUri + " already exists on card "
                        + smartcardUri + " (set overwrite=true to overwrite)");
            }
        }
        SmartcardStatusCode ret = s.allocateCredential(pin, newCredUri, issuerUri);
        if (ret != SmartcardStatusCode.OK) {
            throw new InsufficientStorageException("Credential " + newCredUri + " could not be created on card "
                    + smartcardUri + ". Status code: " + ret.ordinal() + " " + ret.name());
        }
    }

    private byte[] getRealChallengePreimage(byte[] preimage, byte[] nonce) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(new byte[]{0x01});
            baos.write(nonce);
            baos.write(preimage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    @Override
    public BigInteger computeBaseForScopeExclusivePseudonym(URI scope, BigInteger modulus, BigInteger order) {
        return Utils.baseForScopeExclusivePseudonym(scope, modulus, order);
    }

    @Override
    public BigInteger computeBaseForScopeExclusivePseudonym(URI smartcardUri, URI scope) {
        return this.computeBaseForScopeExclusivePseudonym(scope, this.getPseudonymModulusOfCard(smartcardUri),
                this.getPseudonymSubgroupOrderOfCard(smartcardUri));
    }

    @Override
    public BigInteger computeChallenge(byte[] preimage, byte[] nonce) {
        byte[] realPreimage = this.getRealChallengePreimage(preimage, nonce);
        // TODO(enr): Check that cards agree on challenge size
        int challengeSizeBytes = 256 / 8;
        BigInteger challenge = Utils.hashToBigIntegerWithSize(realPreimage, challengeSizeBytes);

        /*
         * Idemix computes the response as       r + c*x
         * However the smartcard computes it as  r - c*x
         * We therefore need to negate the challenge served to Idemix so that the proof works out
         */
        return challenge.negate();
    }

    @Override
    public BigInteger computeCredentialFragment(URI smartcardUri, URI credentialUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.computeCredentialFragment(pin, credentialUri);
    }

    @Override
    public BigInteger computePublicKeyOfCard(URI smartcardUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.computeDevicePublicKey(pin);
    }

    @Override
    public BigInteger computeScopeExclusivePseudonym(URI smartcardUri, URI scope) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.computeScopeExclusivePseudonym(pin, scope);
    }

    public BigInteger computePseudonym(URI smartcardUri, BigInteger h, BigInteger randomizer) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        BigInteger publicKey = s.computeDevicePublicKey(pin);
        BigInteger p = s.getSystemParameters(pin).p;
        BigInteger res = publicKey.multiply(h.modPow(randomizer, p)).mod(p);
        return res;
    }

    @Override
    public boolean credentialExists(URI smartcardUri, URI credUri) {
        System.out.println("sc uri: " + smartcardUri + "\n credUri: " + credUri);
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            return false;
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.credentialExists(pin, credUri);
    }

    @Override
    public BigInteger getNOfCredential(URI smartcardUri, URI credUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.getIssuerParametersOfCredential(pin, credUri).groupParams.getModulus();
    }

    @Override
    public BigInteger getPseudonymBaseOfCard(URI smartcardUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.getSystemParameters(pin).g;
    }

    @Override
    public BigInteger getPseudonymSubgroupOrderOfCard(URI smartcardUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.getSystemParameters(pin).subgroupOrder;
    }

    @Override
    public BigInteger getPseudonymModulusOfCard(URI smartcardUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        return s.getSystemParameters(pin).p;
    }

    @Override
    public BigInteger getR0OfCredential(URI smartcardUri, URI credUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        GroupParameters groupParams = s.getIssuerParametersOfCredential(pin, credUri).groupParams;
        if(groupParams.isIdemixGroupParameters()){
            return ((CredentialBases)groupParams).R0;
        }else{
            throw new RuntimeException("getR0OfCredential is only meant to be called when the card is an Idemix card. This is UProve");
        }
    }

    @Override
    public BigInteger getSOfCredential(URI smartcardUri, URI credUri) {
        BasicSmartcard s = this.getSmartcard(smartcardUri);
        if(s == null) {
            throw new RuntimeException("Unknown smartcard: " + smartcardUri);
        }
        Integer pin = this.storage.getPin(smartcardUri);
        GroupParameters groupParams = s.getIssuerParametersOfCredential(pin, credUri).groupParams;
        if(groupParams.isIdemixGroupParameters()){
            return ((CredentialBases)groupParams).S;
        }else{
            throw new RuntimeException("getSOfCredential is only meant to be called when the card is an Idemix card. This is UProve");
        }
    }

    @Override
    public IdemixProofSpec idemixProofSpecFactory() {
        return new ScmProofSpec();
    }

    @Override
    public byte[] prepareNonce(IdemixProofCommitment icom) {

        if(icom instanceof ScmProofCommitment) {
            ScmProofCommitment com = (ScmProofCommitment) icom;

            byte[] nonce = com.getNonce();
            if (nonce == null) {
                throw new RuntimeException("Could not generate nonce...");
            }
            return nonce;
        } else {
            throw new RuntimeException("Incompatible proof commitment");
        }
    }

    @Override
    public boolean smartcardLoaded(URI smartcardUri) {
        BasicSmartcard sc = this.getSmartcard(smartcardUri);
        return sc != null;
    }

    @Override
    public IdemixProofCommitment prepareProof(IdemixProofSpec ispec) {
        if(ispec instanceof ScmProofSpec) {
            TimingsLogger.logTiming("AbcSmartcardManager.prepareProof", true);
            ScmProofSpec spec = (ScmProofSpec) ispec;

            ScmProofCommitment com = new ScmProofCommitment(spec);

            Set<URI> involvedSmartcards = spec.computeListOfInvolvedSmartcards();
            for(URI sc: involvedSmartcards) {
                BasicSmartcard s = this.getSmartcard(sc);
                if(s == null) {
                    throw new RuntimeException("Unknown smartcard: " + sc);
                }
                Integer pin = this.storage.getPin(sc);

                Set<URI> creds = spec.getListOfInvolvedCredentials(sc);
                Set<URI> sep = spec.getListOfScopeExclusivePseudonyms(sc);
                boolean pkProof = spec.isProofOfPublicKey(sc);

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
                com.setNonceCommitment(sc, zkCom.nonceCommitment);
            }

            TimingsLogger.logTiming("AbcSmartcardManager.prepareProof", false);
            return com;
        } else {
            throw new RuntimeException("Incompatible proof spec");
        }
    }


    @Override
    public IdemixProofResponse finalizeZkProof(IdemixProofCommitment icom,
            byte[] preimage, byte[] nonce) {
        if(icom instanceof ScmProofCommitment) {
            ScmProofCommitment com = (ScmProofCommitment) icom;
            ScmProofSpec spec = com.getProofSpec();

            if (! Arrays.equals(nonce, com.getNonce())) {
                throw new RuntimeException("Invalid nonce given to finalizeZkProof");
            }

            ScmProofResponse resp = new ScmProofResponse();

            Set<URI> involvedSmartcards = spec.computeListOfInvolvedSmartcards();
            for(URI sc: involvedSmartcards) {
                BasicSmartcard s = this.getSmartcard(sc);
                if(s == null) {
                    throw new RuntimeException("Unknown smartcard: " + sc);
                }
                Integer pin = this.storage.getPin(sc);

                byte[] realNonce = com.getNonce();//Utils.checkCommitmentsAndGetNonce(com.getListOfNonceCommitments(), com.getListOfNonceOpenings(), com.getMyNonce(sc));

                ZkProofResponse zkResp = s.finalizeZkProof(pin, preimage, spec.getListOfInvolvedCredentials(sc), spec.getListOfScopeExclusivePseudonyms(sc), realNonce);
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
    public int getChallengeSizeBytes() {
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
    public int getStatisticalHidingSizeBytes() {
        Set<Integer> shSizes = new HashSet<Integer>();
        for(Entry<URI, BasicSmartcard> s: this.storage.getSmartcards().entrySet()) {
            Integer pin = this.storage.getPin(s.getKey());
            shSizes.add(s.getValue().getSystemParameters(pin).zkStatisticalHidingSizeBytes);
        }
        if(shSizes.size() == 1) {
            return shSizes.iterator().next();
        } else if (shSizes.size() == 0) {
            throw new RuntimeException("No cards loaded");
        } else {
            throw new RuntimeException("Incompatible cards: statistical hiding size is different");
        }
    }

}
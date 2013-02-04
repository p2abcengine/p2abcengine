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

package eu.abc4trust.abce.internal.user.policyCredentialMatcher;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.evidenceGeneration.EvidenceGenerationOrchestration;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.exceptions.TokenIssuanceException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.ui.idSelection.IdentitySelectionUiConverter;
import eu.abc4trust.ui.idSelection.IdentitySelectionUiPrinter;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyCandidateToken;
import eu.abc4trust.util.MyCredentialDescription;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.util.MyInspectableAttribute;
import eu.abc4trust.util.MyPresentationPolicy;
import eu.abc4trust.util.MyUiIssuanceReturn;
import eu.abc4trust.util.MyUiPresentationReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class PolicyCredentialMatcherImpl implements PolicyCredentialMatcher {

    // Flag
    public static boolean GENERATE_SECRET_IF_NONE_EXIST = true;

    private final CredentialManager credentialManager;
    private final KeyManager keyManager;
    private final EvidenceGenerationOrchestration evidenceOrchestration;
    private final ContextGenerator contextGenerator;
    private final Logger logger;
    private final String EXPECTED_ABC_XML_VERSION = "1.0";

    @Inject
    PolicyCredentialMatcherImpl(CredentialManager credentialManager,
            EvidenceGenerationOrchestration evidenceOrchestration, ContextGenerator contextGenerator,
            KeyManager keyManager, Logger logger) {
        this.credentialManager = credentialManager;
        this.evidenceOrchestration = evidenceOrchestration;
        this.contextGenerator = contextGenerator;
        this.keyManager = keyManager;
        this.logger = logger;
    }

    @Override
    public boolean canBeSatisfied(PresentationPolicyAlternatives ppa)
            throws CredentialManagerException, KeyManagerException {
        List<MyCandidateToken> candidateTokens = this.generateCandidateTokens(ppa);
        return candidateTokens.size() > 0;
    }

    @Override
    public IssuanceMessage createIssuanceToken(IssuanceMessage im, IdentitySelection identitySelection)
            throws CredentialManagerException, KeyManagerException, IdentitySelectionException {
      return createIssuanceToken(im, new IdentitySelectionUiPrinter(new IdentitySelectionUiConverter(identitySelection)));
    }
    
    @Override
    public IssuanceMessage createIssuanceToken(IssuanceMessage im, IdentitySelectionUi identitySelection)
            throws CredentialManagerException, KeyManagerException, IdentitySelectionException {
        IssuancePolicy ip = (IssuancePolicy) XmlUtils.unwrap(im.getAny(), IssuancePolicy.class);
        if (ip == null) {
            String errorMessage = "Expected that the issuanceMessage contained an IssuancePolicy.";
            this.logger.severe(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        this.checkVersionOrThrow(ip.getVersion());

        List<MyCandidateToken> candidateTokens = this.generateCandidateTokens(ip);

        updateRevocationInformation(candidateTokens);
        MyUiIssuanceReturn uiReturn = MyCandidateToken.callIssuanceUi(identitySelection, candidateTokens);


        MyCandidateToken chosenCandidateToken = uiReturn.token;

        this.populateInspectors(chosenCandidateToken, uiReturn.chosenInspectors);
        this.populatePseudonyms(chosenCandidateToken, uiReturn.chosenPseudonyms);
        List<URI> chosenCredentials = chosenCandidateToken.getCredentialUriList();

        IssuanceMessage ret;
        try {
            // TODO(enr): Remove empty list of user attributes
            List<Attribute> userAtts = new ArrayList<Attribute>();
            IssuanceTokenDescription chosenToken = chosenCandidateToken.getIssuanceToken();

            ret =
                    this.evidenceOrchestration.createIssuanceToken(chosenToken, chosenCredentials, userAtts,
                            uiReturn.chosenPseudonyms, im.getContext());
        } catch (TokenIssuanceException e) {
            String errorMessage = "Cannot create issuance token: " + e.getMessage();
            this.logger.severe(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }

        this.storePseudonymMetadata(candidateTokens, uiReturn.metadataToChange);
        return ret;
    }

    private List<MyCandidateToken> generateCandidateTokens(PresentationPolicyAlternatives ppa)
            throws CredentialManagerException, KeyManagerException {
        this.checkVersionOrThrow(ppa.getVersion());

        List<MyCandidateToken> candidateTokens = new ArrayList<MyCandidateToken>();

        for (PresentationPolicy pp : ppa.getPresentationPolicy()) {
            this.populateCandidateTokens(candidateTokens, pp, null);
        }

        return candidateTokens;
    }

    private List<MyCandidateToken> generateCandidateTokens(IssuancePolicy ip)
            throws CredentialManagerException, KeyManagerException {
        List<MyCandidateToken> candidateTokens = new ArrayList<MyCandidateToken>();
        this.populateCandidateTokens(candidateTokens, ip.getPresentationPolicy(), ip.getCredentialTemplate());

        return candidateTokens;
    }

    private void checkVersionOrThrow(String version) {
        if (!version.equals(this.EXPECTED_ABC_XML_VERSION)) {
            String errorMessage =
                    "Unknown version, expected '" + this.EXPECTED_ABC_XML_VERSION + "', got '" + version + "'";
            this.logger.severe(errorMessage);
            throw new UnsupportedOperationException(errorMessage);
        }
    }

    private void populateCandidateTokens(List<MyCandidateToken> candidateTokens,
            PresentationPolicy pp, /*Nullable*/ CredentialTemplate ct)
                throws CredentialManagerException, KeyManagerException {

        MyPresentationPolicy mypp = new MyPresentationPolicy(pp);

        // List of all acceptable inspectors
        List<List<MyInspectableAttribute>> inspectorChoice = mypp.computeInspectorChoice(this.keyManager);
        if (inspectorChoice == null) {
            this.logger.severe("Cannot find inspectors for policy: " + mypp.getPolicyUri());
            return;
        }

        this.generateSecretIfNoneExist();

        // List of all acceptable pseudonyms
        List<List<PseudonymWithMetadata>> pseudonymChoice =
                mypp.computePseudonymChoice(this.credentialManager, this.contextGenerator);

        // List of acceptable credential assignments
        List<ArrayList<MyCredentialDescription>> assignments =
                mypp.findCredentialAssignment(this.credentialManager, this.keyManager);

        for (ArrayList<MyCredentialDescription> credAssign : assignments) {
            // For each assignment:
            // - filter pseudonyms based on secret
            // - create candidate token
            List<ArrayList<PseudonymWithMetadata>> pseudonymAssignment =
                    mypp.findPseudonymAssignment(pseudonymChoice, credAssign);

            boolean secretsOk = mypp.filterSecrets(pseudonymAssignment, credAssign);

            if (secretsOk) {
                PresentationTokenDescription ptd =
                        mypp.generateTokenDescription(credAssign, this.contextGenerator);

                MyCandidateToken tk =
                        new MyCandidateToken(mypp, ptd, ct, credAssign, pseudonymAssignment, inspectorChoice);
                candidateTokens.add(tk);
            } else {
                this.logger.warning("Incompatible secret binding");
            }
        }
    }

    private void generateSecretIfNoneExist() {
        if (! GENERATE_SECRET_IF_NONE_EXIST) {
            return;
        }
        try {
            // Only create new secret if there are no  non-device-bound secrets
            boolean hasNonDeviceBoundSecret = false;
            for (SecretDescription sd: this.credentialManager.listSecrets()) {
                if (! sd.isDeviceBoundSecret()) {
                    hasNonDeviceBoundSecret = true;
                    break;
                }
            }
            if (! hasNonDeviceBoundSecret) {
                Secret s = this.evidenceOrchestration.createSecret();
                this.credentialManager.storeSecret(s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    @Override
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives ppa,
            IdentitySelection identitySelection)
            throws CredentialManagerException, CryptoEngineException, KeyManagerException, IdentitySelectionException {
      return createPresentationToken(ppa, new IdentitySelectionUiPrinter(new IdentitySelectionUiConverter(identitySelection)));
    }
    
    @Override
    public PresentationToken createPresentationToken(PresentationPolicyAlternatives ppa,
            IdentitySelectionUi identitySelection)
            throws CredentialManagerException, CryptoEngineException, KeyManagerException, IdentitySelectionException {

        List<MyCandidateToken> candidateTokens = this.generateCandidateTokens(ppa);

        if (candidateTokens.size() == 0) {
            this.logger.warning("Presentation policy cannot be satisfied");
            return null;
        }

        updateRevocationInformation(candidateTokens);
        MyUiPresentationReturn uiReturn = MyCandidateToken.callPresentationUi(identitySelection, candidateTokens);

        MyCandidateToken chosenCandidateToken = uiReturn.token;
        this.populateInspectors(chosenCandidateToken, uiReturn.chosenInspectors);
        this.populatePseudonyms(chosenCandidateToken, uiReturn.chosenPseudonyms);
        List<URI> chosenCredentials = chosenCandidateToken.getCredentialUriList();
        PresentationTokenDescription chosenToken = chosenCandidateToken.getPresentationToken();

        PresentationToken presTok =
                this.evidenceOrchestration.createPresentationToken(chosenToken, chosenCredentials,
                        uiReturn.chosenPseudonyms);

        this.storePseudonymMetadata(candidateTokens, uiReturn.metadataToChange);
        return presTok;
    }

    private void updateRevocationInformation(List<MyCandidateToken> candidateTokens) {
      try {
        Map<URI, URI> issuerToRevocationInfoMap = new HashMap<URI, URI>();
        List<URI> revokedAtts = new ArrayList<URI>();
        revokedAtts.add(URI.create(MyCredentialSpecification.REVOCATION_HANDLE));
        
        Set<URI> credentialsToUpdate = new HashSet<URI>();
        for(MyCandidateToken tok: candidateTokens) {
          for(URI credUri: tok.getCredentialUriList()) {
            credentialsToUpdate.add(credUri);
          }
          // A bit wasteful, if we already processed the same presentation policy alternative
          tok.updateIssuerToRevocationInformationUidMap(issuerToRevocationInfoMap);
        }
        for(URI credUri: credentialsToUpdate) {
          Credential cred = credentialManager.getCredential(credUri);
          URI credSpec = cred.getCredentialDescription().getCredentialSpecificationUID();
          CredentialSpecification spec = keyManager.getCredentialSpecification(credSpec);
          if (spec.isRevocable()) {
            URI issuer = cred.getCredentialDescription().getIssuerParametersUID();
            URI revInfoUri = issuerToRevocationInfoMap.get(issuer);
            IssuerParameters ip = keyManager.getIssuerParameters(issuer);
            URI revAuth = ip.getRevocationParametersUID();
            if ( revInfoUri != null) {
              evidenceOrchestration.updateNonRevocationEvidence(cred, revAuth, revokedAtts, revInfoUri);
            } else {
              evidenceOrchestration.updateNonRevocationEvidence(cred, revAuth, revokedAtts);
            }
            // TODO(enr): This that necessary?
            credentialManager.deleteCredential(cred.getCredentialDescription().getCredentialUID());
            credentialManager.storeCredential(cred);
          }
        }
      } catch(CryptoEngineException e) {
        throw new RuntimeException(e);
      } catch (CredentialManagerException e) {
        throw new RuntimeException(e);
      } catch (KeyManagerException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Updates the metadata of pseudonyms that were changed through the UI
     */
    private void storePseudonymMetadata(List<MyCandidateToken> candidateTokens,
            Map<URI, PseudonymMetadata> metadataToChange) {
        Map<URI, PseudonymWithMetadata> mpwm = new HashMap<URI, PseudonymWithMetadata>();
        for (MyCandidateToken token : candidateTokens) {
            token.populatePseudonymsInMap(mpwm);
        }

        for (URI uri : metadataToChange.keySet()) {
            PseudonymWithMetadata pwm = mpwm.get(uri);
            PseudonymMetadata pm = metadataToChange.get(uri);
            pwm.setPseudonymMetadata(pm);
            try {
                this.credentialManager.attachMetadataToPseudonym(pwm.getPseudonym(), pm);
            } catch (CredentialManagerException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void populateInspectors(MyCandidateToken candidateToken, List<URI> inspectorChoice) {
        PresentationTokenDescription presentationTokenDescription =
                candidateToken.getPresentationToken();
        Iterator<URI> inspectors = inspectorChoice.iterator();
        for (CredentialInToken cit : presentationTokenDescription.getCredential()) {
            for (AttributeInToken ait : cit.getDisclosedAttribute()) {
                if (ait.getInspectionGrounds() != null) {
                    ait.setInspectorPublicKeyUID(inspectors.next());
                }
            }
        }
    }

    private void populatePseudonyms(MyCandidateToken candidateToken, List<URI> chosenPseudonyms) {
        PresentationTokenDescription presToken = candidateToken.getPresentationToken();
        Map<URI, PseudonymWithMetadata> mpwm = new HashMap<URI, PseudonymWithMetadata>();
        candidateToken.populatePseudonymsInMap(mpwm);

        Iterator<URI> pseudonyms = chosenPseudonyms.iterator();
        for (PseudonymInToken pit : presToken.getPseudonym()) {
            PseudonymWithMetadata pwm = mpwm.get(pseudonyms.next());
            if (!pwm.getPseudonym().getScope().equals(pit.getScope())) {
                throw new RuntimeException("UI returned pseudonym with wrong scope.");
            }
            if (pit.isExclusive() != pwm.getPseudonym().isExclusive()) {
                throw new RuntimeException("UI returned pseudonym with wrong isExclusive.");
            }
            byte[] value = this.getPseudonymValueOrCreate(pwm);
            pit.setPseudonymValue(value);
        }

    }

    private byte[] getPseudonymValueOrCreate(PseudonymWithMetadata pwm) {
        if (pwm.getPseudonym().getPseudonymValue() == null) {
            // Pseudonym has not been created yet, asking the crypto engine to create one for us
            URI pseudonymUri = pwm.getPseudonym().getPseudonymUID();
            String scope = pwm.getPseudonym().getScope();
            boolean exclusive  = pwm.getPseudonym().isExclusive();
            URI secretUri = pwm.getPseudonym().getSecretReference();
            PseudonymWithMetadata newpwm = this.evidenceOrchestration.createPseudonym(pseudonymUri, scope, exclusive, secretUri);
            newpwm.setPseudonymMetadata(new PseudonymMetadata());
            // TODO(enr): This code should be moved somewhere else...
            {
                Map<String, String> trans = new HashMap<String, String>();
                //trans.put("en", "Pseudonym with scope %s and secret %s.");
                //trans.put("el", "\u03a8\u03b5\u03c5\u03b4\u03ce\u03bd\u03c5\u03bc\u03bf \u03bc\u03b5 \u03c4\u03bf\u03bd \u03c4\u03bf\u03bc\u03ad\u03b1 %s \u03ba\u03b1\u03b9 \u03bc\u03c5\u03c3\u03c4\u03b9\u03ba\u03ae %s.");
                //trans.put("sv", "Pseudonym med dom\u00e4n %s och hemliga %s.");
                trans.put("en", "Pseudonym %s");
                trans.put("el", "\u03a8\u03b5\u03c5\u03b4\u03ce\u03bd\u03c5\u03bc\u03bf %s");
                trans.put("sv", "Pseudonym %s");

                String now = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
                for(String lang: trans.keySet()) {
                    FriendlyDescription desc = new FriendlyDescription();
                    desc.setLang(lang);
                    desc.setValue(String.format(trans.get(lang), now));
                    System.err.println(desc.getValue());
                    newpwm.getPseudonymMetadata().getFriendlyPseudonymDescription().add(desc);
                }
            }

            try {
                this.credentialManager.storePseudonym(newpwm);
            } catch (CredentialManagerException e) {
                throw new RuntimeException(e);
            }
            return newpwm.getPseudonym().getPseudonymValue();
        } else {
            return pwm.getPseudonym().getPseudonymValue();
        }
    }
}

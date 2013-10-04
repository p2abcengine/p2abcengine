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

package eu.abc4trust.cryptoEngine.idemix.user;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.util.ContextGenerator;
import eu.abc4trust.util.MyAttribute;
import eu.abc4trust.util.MyCredentialSpecification;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialInTokenWithCommitments;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;

public class CryptoEngineUtil {

    private static final Logger logger = Logger
            .getLogger(CryptoEngineUtil.class.getCanonicalName());

    private final CredentialManager credManager;
    @SuppressWarnings("unused")
    private final ContextGenerator contextGen;

    public CryptoEngineUtil(CredentialManager credManager,
            ContextGenerator contextGen) {
        super();
        this.credManager = credManager;
        this.contextGen = contextGen;
    }

    public LinkedHashMap<URI, Credential> fetchCredentialsFromIssuanceToken(
            IssuanceTokenDescription tokenDesc, List<URI> credentialAssignment)
                    throws CredentialManagerException {
        return this.fetchCredentialsFromPresentationToken(
                tokenDesc.getPresentationTokenDescription(),
                credentialAssignment);
    }

    public LinkedHashMap<URI, PseudonymWithMetadata> fetchPseudonymsFromIssuanceToken(
            IssuanceTokenDescription itd, List<URI> pseudonymAssignment) {
        return this.fetchPseudonymsFromPresentationToken(
                itd.getPresentationTokenDescription(), pseudonymAssignment);
    }

    public LinkedHashMap<URI, Credential> fetchCredentialsFromPresentationToken(
            PresentationTokenDescription tokenDesc,
            List<URI> credentialAssignment) {
      LinkedHashMap<URI, Credential> ret = new LinkedHashMap<URI, Credential>();

        Iterator<URI> credIterator = credentialAssignment.iterator();
        int credIndex = 0;
        for (CredentialInToken cd : tokenDesc.getCredential()) {
        	URI credentialAlias = cd.getAlias();
            if (credentialAlias == null) {
                credentialAlias = URI.create("abc4trust.eu/credential/"+ credIndex);
            }
            URI credentialUri = credIterator.next();
            credIndex ++;
            Credential c = null;
            try {
                c = this.credManager.getCredential(credentialUri);
            } catch (CredentialManagerException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.info(sw.toString());
            }
            ret.put(credentialAlias, c);
        }
        return ret;
    }

    public LinkedHashMap<URI, Credential> fetchCredentialsFromPresentationTokenWithCommitments(
            PresentationTokenDescriptionWithCommitments tokenDesc,
            List<URI> credentialAssignment) {
        LinkedHashMap<URI, Credential> ret = new LinkedHashMap<URI, Credential>();

        //THe problem is credentialAssignment only contains 1 URI, however we iterate over all credentialsin the commitment, which might contains more
        // ie. we get a idemix uri, but have both idemix and uprove credential in the token
        //Iterator<URI> credIterator = credentialAssignment.iterator();
        int credIndex = 0;
        for (CredentialInTokenWithCommitments cd : tokenDesc.getCredential()) {
            URI credentialAlias = cd.getAlias();
            if (credentialAlias == null) {
                credentialAlias = URI.create("abc4trust.eu/credential/"+ credIndex);
            }
           // System.out.println("count is now: "+credentialAssignment.size());
            for(URI credentialUri: credentialAssignment){
           		Credential c = null;
           		try {
           			c = this.credManager.getCredential(credentialUri);
           		} catch (CredentialManagerException e) {
           			StringWriter sw = new StringWriter();
           			e.printStackTrace(new PrintWriter(sw));
           			logger.info(sw.toString());
           		}
           		if(c.getCredentialDescription().getCredentialSpecificationUID().equals(cd.getCredentialSpecUID())){
           			ret.put(credentialAlias, c);
           			credIndex ++;
           			break;
           		}
        	}
        }
        return ret;
    }

    public List<Secret> fetchSecretsFromPresentationToken(
            PresentationTokenDescription ptd, Map<URI, Credential> aliasCreds) {
        // TODO: implement managing secrets
        throw new UnsupportedOperationException();
    }

    public LinkedHashMap<URI, PseudonymWithMetadata> fetchPseudonymsFromPresentationToken(
            PresentationTokenDescription ptd, List<URI> pseudonymAssignment) {

        LinkedHashMap<URI, PseudonymWithMetadata> ret = new LinkedHashMap<URI, PseudonymWithMetadata>();
        Iterator<URI> credIterator = pseudonymAssignment.iterator();
        int nymIndex = 0;
        for (PseudonymInToken pit : ptd.getPseudonym()) {
            URI nymAlias = pit.getAlias();
            if (nymAlias == null) {
                nymAlias = URI.create("abc4trust.eu/pseudonym/"+ nymIndex);
            }

            URI nymUri = credIterator.next();
            nymIndex ++;
            PseudonymWithMetadata pwm = null;
            try {
                pwm = this.credManager.getPseudonym(nymUri);
            } catch (CredentialManagerException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.info(sw.toString());
            }
            ret.put(nymAlias, pwm);
        }
        return ret;
    }

    public LinkedHashMap<URI, PseudonymWithMetadata> fetchPseudonymsFromPresentationTokenWithCommitments(
            PresentationTokenDescriptionWithCommitments ptd,
            List<URI> pseudonymAssignment) {

        LinkedHashMap<URI, PseudonymWithMetadata> ret = new LinkedHashMap<URI, PseudonymWithMetadata>();
        Iterator<URI> credIterator = pseudonymAssignment.iterator();
        int nymIndex = 0;
        for (PseudonymInToken pit : ptd.getPseudonym()) {
            URI nymAlias = pit.getAlias();
            if (nymAlias == null) {
                nymAlias = URI.create("abc4trust.eu/pseudonym/"+ nymIndex);
            }
            nymIndex ++;

            URI nymUri = credIterator.next();
            PseudonymWithMetadata pwm = null;
            try {
                pwm = this.credManager.getPseudonym(nymUri);
            } catch (CredentialManagerException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.info(sw.toString());
            }
            ret.put(nymAlias, pwm);
        }
        return ret;
    }

    public URI getSmartcardUidFromPseudonymOrCredentialUri(
            Map<URI, Credential> aliasCreds,
            Map<URI, PseudonymWithMetadata> aliasNyms,
            URI pseudonymOrCredentialAlias) {
        URI secretUid = null;
        if (pseudonymOrCredentialAlias != null) {
            Credential c = aliasCreds.get(pseudonymOrCredentialAlias);
            if (c != null) {
                secretUid = c.getCredentialDescription()
                        .getSecretReference();
            }
            if (secretUid == null) {
                PseudonymWithMetadata p = aliasNyms
                        .get(pseudonymOrCredentialAlias);
                if (p != null) {
                    secretUid = p.getPseudonym().getSecretReference();
                }
            }
            if (secretUid == null) {
                secretUid = this.getFirstSecretUidInCredManager();
            }
        } else {
            secretUid = this.getFirstSecretUidInCredManager();
        }
        return secretUid;
    }

    public URI getSmartcardUri(URI smartcardUid) {
        if (smartcardUid == null) {
            smartcardUid = this.getFirstSecretUidInCredManager();
        }
        return smartcardUid;
    }

    private URI getFirstSecretUidInCredManager() {
        URI secretUid = null;
        try {
            List<SecretDescription> secrets;
            secrets = this.credManager.listSecrets();
            if (!secrets.isEmpty()) {
                SecretDescription secretDescription = secrets.get(0);
                secretUid = secretDescription.getSecretUID();
            } else {
                throw new RuntimeException("There are no secrets.");
            }
        } catch (CredentialManagerException ex) {
            throw new RuntimeException(ex);
        }
        return secretUid;
    }

    private static void setDefaultImage(CredentialSpecification credSpec,
            CredentialDescription cd) {
        if (credSpec.getDefaultImageReference() != null) {
            cd.setImageReference(credSpec.getDefaultImageReference());
        } else {
            cd.setImageReference(URI.create("http://issuer.zyx/default.jpg"));
        }
    }

    public static void setupCredentialDescription(
            CredentialSpecification credSpec, List<MyAttribute> attributeList,
            CredentialTemplate credentialTemplate,
            MyCredentialSpecification myCredSpec, CredentialDescription cd) {
        cd.setCredentialSpecificationUID(credentialTemplate
                .getCredentialSpecUID());
        cd.setCredentialUID(URI.create("no-uid-yet"));

        CryptoEngineUtil.setDefaultImage(credSpec, cd);

        if (credSpec.getFriendlyCredentialName() != null) {
            cd.getFriendlyCredentialName().addAll(
                    credSpec.getFriendlyCredentialName());
        }

        cd.setIssuerParametersUID(credentialTemplate.getIssuerParametersUID());

        for (MyAttribute a : attributeList) {
            // first add the friendly description from the spec.
            List<FriendlyDescription> frienlyAttrDescFromSpec = myCredSpec
                    .getFriendlyDescryptionsForAttributeType(a.getType());

            if ((a.getFriendlyAttributeName() == null)
                    && (frienlyAttrDescFromSpec != null)) {
                a.getFriendlyAttributeName()
                .addAll(frienlyAttrDescFromSpec);
            }
            cd.getAttribute().add(a.getXmlAttribute());
        }
    }
}
